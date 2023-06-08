package com.nipa.createfolderwithpermission

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nipa.createfolderwithpermission.databinding.ActivityMainBinding
import java.io.File
private const val TAG = "PermissionCheck"
private const val STORAGE_PERMISSION_CODE = 100
class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    private val storagePermissionsArray = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.CAMERA
    )
    private fun checkArrayStoragePermissions(): Boolean {
        for (permission in storagePermissionsArray) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
    var permissionGrant=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        //init UI Views

        if(checkReadWritePermission()){
            permissionGrant=true
        }else{
            requestCallPermission()
        }
        //handle click, create folder
        binding.btnCreateFolder.setOnClickListener {
            if (permissionGrant){
                val folderName = binding.etFolderName.text.toString().trim()
                if(folderName.isEmpty()){
                    toast("Please provide folder name")
                    return@setOnClickListener
                }
                createNewFolder()
            }
            else{
                requestCallPermission()
            }
        }
    }
    private fun createNewFolder(){
        //folder name
        val folderName = binding.etFolderName.text.toString().trim()
        //create folder using name we just input
        if(folderName.isEmpty()){
            return
        }
        val folderCreate = File("${Environment.getExternalStorageDirectory()}/$folderName")
        if(folderCreate.exists() && folderCreate.isDirectory()){
            toast("Folder already exist")
        }else{
            //create folder
            val folderCreated = folderCreate.mkdir()

            //show if folder created or not
            if (folderCreated) {
                Log.d("nipaerror",folderCreate.absolutePath)
                toast("Folder Path: ${folderCreate.absolutePath}")
            } else {
                toast("Folder not created. Try again")
            }
        }

    }

    private fun requestCallPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11(R) or above
            try {

                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                sdkUpperActivityResultLauncher.launch(intent)
            }
            catch (e: Exception){
                Log.e(TAG, "error ", e)
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                sdkUpperActivityResultLauncher.launch(intent)
            }
        }else{
            //for below version
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private val sdkUpperActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

        //here we will handle the result of our intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11(R) or above
            if (Environment.isExternalStorageManager()){
                //Manage External Storage Permission is granted
                Log.d(TAG, "Manage External Storage Permission is granted")
                createNewFolder()
            }
            else{
                //Manage External Storage Permission is denied....
                Log.d(TAG, "Permission is denied")
                toast("Manage External Storage Permission is denied....")
            }
        }

    }

    private fun checkReadWritePermission(): Boolean{

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //Android is 11(R) or above
            Environment.isExternalStorageManager()
        }
        else{
            //Permission is below 11(R)
            //  checkBelowPermissionGranted()
            checkArrayStoragePermissions()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }){
                Log.d(TAG, "External Storage Permission granted")
                permissionGrant=true
                createNewFolder()
            }
            else{
                //External Storage Permission denied...
                Log.d(TAG, "Some  Permission denied...")
                toast("Some Storage Permission denied...")
            }
        }
    }


    private fun toast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
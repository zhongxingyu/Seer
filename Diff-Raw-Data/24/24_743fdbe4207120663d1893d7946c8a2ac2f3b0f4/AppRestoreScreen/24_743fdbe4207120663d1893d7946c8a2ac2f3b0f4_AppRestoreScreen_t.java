 package com.caspian.android.removal;
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
import android.widget.Toast;
 
 public class AppRestoreScreen extends AppManagementScreen
 {
     private String restoreDir;
     private boolean handleOdex;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         appManagementDir = "/sdcard/sdx/backup/app/";
         restoreDir = "/system/app/";
         handleOdex = AppSettings.getAssociateOdex();
         super.onCreate(savedInstanceState);
         
         btnCopyFiles.setText("Restore Selected Files");
     }
     
     /**
      * Copy any checked file back to the system dir to restore it
      */
     @Override
     protected boolean copyCheckedFiles()
     {
         boolean success = false;
         ArrayList<String> files = getCheckedFiles();
 
         if (files.size() > 0)
         {
             String fileName = "";
 
             try
             {
                 // make sure /system is mounted
                 if (AppSettings.getAutoMount()) AppRemovalManager.remountSystemDir(true);
                 
                 for (String f : files)
                 {
                     fileName = f;
                     mgr.copyFile(f, appManagementDir, restoreDir, true);
 
                     // handle the odex as well
                     if (handleOdex && f.endsWith(".apk"))
                     {
                         String odexName = f.replace(".apk", ".odex");
 
                         if (mgr.fileExists(appManagementDir + odexName))
                         {
                             mgr.copyFile(odexName, appManagementDir, restoreDir, true);
                         }
                     }
                 }
 
                 success = true;
             }
             catch (Exception exc)
             {
                 String message = "Exception thrown while backing up " + 
                     fileName + ":\n" + exc.getMessage();
 
                 AlertDialog dialog = new AlertDialog.Builder(this).create();
                 dialog.setMessage(message);
                 dialog.setButton(
                     AlertDialog.BUTTON_POSITIVE,
                     "Ok", 
                     (DialogInterface.OnClickListener)null);
 
                 dialog.show();
                 createFileList();
             }
             finally
             {
                 // make sure the system dir is mounted ro
                 try 
                 {
                     if (AppSettings.getAutoMount())  AppRemovalManager.remountSystemDir(false);
                 }
                 catch(Exception e)
                 {
                     // something seriously went wrong, tell the user??
                 }
             }
         }
 
        if (success)
        {
            Toast toast = Toast.makeText(
                    getApplicationContext(), 
                    "Successfully restored files", 
                    Toast.LENGTH_SHORT);
            toast.show();
        }
         return success;
     }
 
     /**
      * Delete any of the backed up files
      */
     @Override
     protected boolean deleteCheckedFiles()
     {
         boolean success = false;
         ArrayList<String> files = getCheckedFiles();
 
         if (files.size() > 0)
         {
             String fileName = "";
 
             try
             {
                 for (String f : files)
                 {
                     fileName = f;
                     mgr.deleteFile(appManagementDir + f, false);
 
                     // handle the odex as well
                     if (handleOdex && f.endsWith(".apk"))
                     {
                         String odexName = f.replace(".apk", ".odex");
 
                         if (mgr.fileExists(appManagementDir + odexName))
                         {
                             mgr.deleteFile(appManagementDir + odexName, true);
                         }
                     }
                 }
 
                 success = true;
             }
             catch (Exception exc)
             {
                 String message = "Exception thrown while deleting " + 
                     fileName + ":\n" + exc.getMessage();
 
                 AlertDialog dialog = new AlertDialog.Builder(this).create();
                 dialog.setMessage(message);
                 dialog.setButton(
                     AlertDialog.BUTTON_POSITIVE,
                     "Ok", 
                     (DialogInterface.OnClickListener)null);
 
                 dialog.show();
             }
             finally
             {
                 createFileList();
             }
         }
 
         return success;
     }
 
 }

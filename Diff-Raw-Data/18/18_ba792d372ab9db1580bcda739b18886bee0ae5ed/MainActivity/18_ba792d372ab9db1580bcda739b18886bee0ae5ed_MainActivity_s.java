 package com.github.dzli.unlockproxy;
 
 import java.io.*;
 import android.app.Activity;
 import android.os.Bundle;
 import android.app.AlertDialog;
 import android.view.View;
 import android.content.Context;
 import android.content.DialogInterface;
 
 public class MainActivity extends Activity
 {
     static final String DATA_DIR = "/data/data/com.github.dzli.unlockproxy";
     final Context context = this;
 
     /** Called when the activity is first created. */
     @Override
         public void onCreate(Bundle savedInstanceState)
         {   
             super.onCreate(savedInstanceState);
             setContentView(R.layout.main);
             copyFilesInAssets("hproxy", DATA_DIR + "/hproxy");
             chgFilePermission(DATA_DIR + "/hproxy", "755");
             copyFilesInAssets("hproxy.conf", DATA_DIR + "/hproxy.conf");
         }   
 
     private void copyFilesInAssets(String fileName, String toPath)
     {   
         try 
         {   
             InputStream myInput = getAssets().open(fileName);
             String outFileName = toPath;
             OutputStream myOutput = new FileOutputStream(outFileName);
             byte[] buffer = new byte[1024];
             int length;
             while ((length = myInput.read(buffer))>0)
             {   
                 myOutput.write(buffer, 0, length);
             }   
 
             myOutput.flush();
             myOutput.close();
             myInput.close();
         }   
         catch(Exception ex) 
         {   
             ex.printStackTrace();
         }   
     }   
 
     private void chgFilePermission(String filePath, String permission)
     {   
         String cmd = "/system/bin/chmod " + permission + " " + filePath;
         String[] strExec = new String[] 
         {   
             "/system/bin/sh", "-c", cmd 
         };  
 
         try {
             Runtime.getRuntime().exec(strExec);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }   
     }   
 
    private void startProxy()
     {
         try {
             Process p =  Runtime.getRuntime().exec("su");
            p.getOutputStream().write((DATA_DIR+"/hproxy start -c "+DATA_DIR+"/hproxy.conf").getBytes());
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     public void startProxy(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                 context);
 
         // set title
         alertDialogBuilder.setTitle("Your Title");
 
         // set dialog message
         alertDialogBuilder
             .setMessage("Click yes to exit!")
             .setCancelable(false)
             .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog,int id) {
                     // if this button is clicked, close
                     // current activity
                     MainActivity.this.finish();
                     }
                     })
         .setNegativeButton("No",new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog,int id) {
                 // if this button is clicked, just close
                 // the dialog box and do nothing
                 dialog.cancel();
                 }
                 });
 
         // create alert dialog
         AlertDialog alertDialog = alertDialogBuilder.create();
 
         // show it
         alertDialog.show();
     }
 
 }
 

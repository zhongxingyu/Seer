 package com.spacecaker.romthemer;
 
 import java.io.DataOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Toast;
 
 
 
 public class Main_activity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.spacecaker_main_activity);
         try {
 			Runtime.getRuntime().exec("su");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.spacecaker_activity, menu);
         return true;
     }
     
     public void copyStream(String assetFilename, String outFileName ) throws IOException
     { 
       InputStream myInput = getAssets().open(assetFilename); 
       OutputStream myOutput = new FileOutputStream(outFileName);    
       byte[] buffer = new byte[2048];
       int length;
       while ((length = myInput.read(buffer))>0)
       {
         myOutput.write(buffer, 0, length);
       }     
       myOutput.flush();
       myOutput.close();
       myInput.close();    
     }                
     
     public void MessageBox(String message)
     {
     	Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
     }   
           
     
     public void themer() throws IOException, InterruptedException
     {
     	    Process mSuProcess;
     	    mSuProcess = Runtime.getRuntime().exec("su");
     	    new DataOutputStream(mSuProcess.getOutputStream()).writeBytes("mount -o remount rw /system\n");
     	    DataOutputStream mSuDataOutputStream = new DataOutputStream(mSuProcess.getOutputStream());
     	    mSuDataOutputStream.writeBytes("cp /sdcard/SystemUI.apk /system/app/SystemUI.apk\n");
     	    MessageBox("Killing this bitch!");
     	    mSuDataOutputStream.writeBytes("killall com.android.systemui\n");
     	    Thread.sleep(2000);
             mSuDataOutputStream.writeBytes("chmod 0644 /sdcard/SystemUI.apk /system/app/SystemUI.apk\n");
     	    mSuDataOutputStream.writeBytes("exit\n");
     	    MessageBox("Starting SystemUI!");
     	    Process proca = Runtime.getRuntime().exec(new String[]{"am","startservice","-n","com.android.systemui.statusbar.StatusBarService"});
     	    proca.waitFor();
     	    	    
     	    MessageBox("Statusbar Done!");
     	   
     }
 
     public void themerframework() throws IOException, InterruptedException
     {
         Process mSuProcess;
         mSuProcess = Runtime.getRuntime().exec("su");
         new DataOutputStream(mSuProcess.getOutputStream()).writeBytes("mount -o remount rw /system\n");
         DataOutputStream mSuDataOutputStream = new DataOutputStream(mSuProcess.getOutputStream());
         mSuDataOutputStream.writeBytes("cp /sdcard/framework-res.apk /system/framework/framework-res.apk\n");
         mSuDataOutputStream.writeBytes("chmod 0644 /sdcard/framework-res.apk /system/framework/framework-res.apk\n");
         mSuDataOutputStream.writeBytes("killall com.android.internal.os.ZygoteInit\n");
         mSuDataOutputStream.writeBytes("exit\n");
         MessageBox("Please Reboot Now !");
 
     }
     
          public void but1(View view) throws IOException, InterruptedException
     {
           copyStream("theme_stock.apk","/sdcard/SystemUI.apk");
           copyStream("theme_stock_framework.apk","/sdcard/framework-res.apk");
           themer();
           themerframework();
     }
            
       public void but2(View view) throws IOException, InterruptedException
       {
     	   copyStream("theme_1.apk","/sdcard/SystemUI.apk");
            copyStream("theme_1_framework.apk","/sdcard/framework-res.apk");
     	   themer();
            themerframework();
       }
       public void but3(View view) throws IOException, InterruptedException
       {
     	   copyStream("theme_2.apk","/sdcard/SystemUI.apk");
            copyStream("theme_2_framework.apk","/sdcard/framework-res.apk");
     	   themer();
            themerframework();
       }
       public void but4(View view) throws IOException, InterruptedException
       {   
     	   copyStream("theme_3.apk","/sdcard/SystemUI.apk");
            copyStream("theme_3_framework.apk","/sdcard/framework-res.apk");
     	   themer();
            themerframework();
       }
       public void but5(View view) throws IOException, InterruptedException
       {
     	   copyStream("theme_4.apk","/sdcard/SystemUI.apk");
            copyStream("theme_4_framework.apk","/sdcard/framework-res.apk");
     	   themer();
            themerframework();
       }
  }


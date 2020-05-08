 package com.tortel.externalize;
 
 import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	public static final boolean D = true;
 	
 	//Dialog box
 	private ProgressDialog copyDialog;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         //Check if the MicroSD is inserted first
         File f = new File(FileMover.externalPath);
         if(!f.exists() || !f.isDirectory()){
         	toast("MicroSD not present - Exiting");
         	this.finish();
         }
         //mSD inserted, continue on
         
         //Check for root
         Shell sh = new Shell();
         if(!sh.root()){
         	toast("No root access - exitting!");
         	this.finish();
         }
         sh.exit();
         
         
         //Set the button and checkbox listeners
         Button move = (Button) findViewById(R.id.moveButton);
         move.setOnClickListener(new MoveButtonListener());
         
         CheckBox enable = (CheckBox) findViewById(R.id.enableBoot);
         enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
         	
         	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         		SharedPreferences prefs = MainActivity.this.getSharedPreferences("default", 0);
         		SharedPreferences.Editor edit =  prefs.edit();
         		if(isChecked){
         			edit.putBoolean("enabled", true);
         			if(D) toast("Enabled");
         		}else{
         			edit.putBoolean("enabled", false);
         			if(D) toast("Disabled");
         		}
         		edit.commit();
         	}
         });
         SharedPreferences prefs = this.getSharedPreferences("default", 0);
         if(prefs.getBoolean("enabled", false)){
         	enable.setChecked(true);
         }
         
     }
     
     
     /**
      * Move Button Listener class
      */
     class MoveButtonListener implements View.OnClickListener{
         /**
          * Button event method
          */
     	public void onClick(View button) {
     		SharedPreferences prefs = MainActivity.this.getSharedPreferences("default", 0);
     		//If were supposed to move images
     		if(prefs.getBoolean("images", true)){
     			FileMover mover = new FileMover(copyProgressHandler, FileMover.IMAGES);
     			moveFiles(mover);
     		}
     		if(prefs.getBoolean("downloads", false)){
     			FileMover mover = new FileMover(copyProgressHandler, FileMover.DOWNLOADS);
     			moveFiles(mover);
     		}
     	}
     	
     	/**
     	 * This method displays the dialog and actually does all the work
     	 * @param mover
     	 */
     	private void moveFiles(FileMover mover){
     		int fileCount = mover.getFileList();
         	copyDialog = new ProgressDialog(MainActivity.this);
         	copyDialog.setCancelable(true);
         	copyDialog.setMessage("Copying...");
             // set the progress to be horizontal
         	copyDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
             // reset the bar to the default value of 0
         	copyDialog.setProgress(0);
         	
             // set the maximum value
         	if(fileCount > 0){
         		copyDialog.setMax(fileCount);
         	} else {
         		copyDialog.setMax(1);
         	}
             //Prevent it from being canceled
         	copyDialog.setCanceledOnTouchOutside(false);
         	copyDialog.setCancelable(false);
             // display the progressbar
         	copyDialog.show();
         	mover.start();
     	}
     }
  
     // handler for the background updating
     Handler copyProgressHandler = new Handler() {
         public void handleMessage(Message msg) {
         	copyDialog.incrementProgressBy(1);
             if(copyDialog.getProgress() == copyDialog.getMax())
             	copyDialog.dismiss();
         }
     };
     
 
     
     
     /**
      * Method to easily make a toast dialog
      * @param msg
      */
     private void toast(String msg){
     	Toast.makeText(this, msg, 3000).show();
     }
 }

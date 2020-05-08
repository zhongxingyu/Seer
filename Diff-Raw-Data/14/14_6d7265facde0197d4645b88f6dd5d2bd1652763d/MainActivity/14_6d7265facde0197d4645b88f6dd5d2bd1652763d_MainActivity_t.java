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
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	public static final boolean D = true;
 	private static final String microSdPath = "/Removable/MicroSD/";
 	private List<File> fileList;
 	private List<File> dirList;
 	
 	private Shell sh;
 	
 	//Dialog box
 	private ProgressDialog copyDialog;
 	private ProgressDialog deleteDialog;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         fileList = new ArrayList<File>();
         dirList = new ArrayList<File>();
         
         //Check if the MicroSD is inserted first
         File f = new File(microSdPath);
         if(!f.exists() || !f.isDirectory()){
         	toast("MicroSD not present - Exiting");
         	this.finish();
         }
         //mSD inserted, continue on
         sh = Shell.get();
         
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
     		/**
     		 * Images
     		 */
     		//Get the file list
     		getFileList(new File("/sdcard/DCIM/"));
     		if(D){
     			toast("File Count: "+fileList.size());
     			toast("Dir Count: "+dirList.size());
     		}
     		moveImagesToCard();
     		//linkImages();
     	}
     }
     
     
     public void onDestroy(){
     	super.onDestroy();
     	//Close the shell
     	sh.exit();
     }
     
     
     /**
      * This moves all files from the 
      * @param cardPath
      */
     private void moveImagesToCard(){
     	copyDialog = new ProgressDialog(this){
     		public void onStop(){
     			super.onStop();
     			linkImages();
     		}
     	};
     	copyDialog.setCancelable(true);
     	copyDialog.setMessage("Copying...");
         // set the progress to be horizontal
     	copyDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
         // reset the bar to the default value of 0
     	copyDialog.setProgress(0);
         // set the maximum value
    	if(fileList.size() > 0){
    		copyDialog.setMax(fileList.size());
    	} else {
    		copyDialog.setMax(1);
    	}
         //Prevent it from being canceled
     	copyDialog.setCanceledOnTouchOutside(false);
     	copyDialog.setCancelable(false);
         // display the progressbar
     	copyDialog.show();
     	
  
         // create a thread for updating the progress bar
         Thread background = new Thread (new Runnable() {
            public void run() {
         	   Log.d("Copying files");
         	   String outDir = microSdPath+"DCIM/";
         	   //This is for the substring to make to cut /sdcard/DCIM/
         	   int index = 13;
         	   File tmp = new File(outDir);
         	   if( !tmp.exists() )
         		   tmp.mkdir();
         	   //Make the dirs
         	   for(File f : dirList){
         		   if(D) Log.d("Making dir: "+outDir+f.toString().substring(index));
         		   new File(outDir+f.toString().substring(index)).mkdir();
         	   }
         	   for(File f: fileList){
         		   	try {
         		   		tmp = new File(outDir+f.toString().substring(index));
         		   		if( !(tmp.exists() && tmp.isDirectory()) )
         		   			copy(f, tmp);
 					} catch (IOException e) {
 						Log.e("Error copying "+f.toString());
 					}
         		   	copyProgressHandler.sendMessage(copyProgressHandler.obtainMessage());
         	   }
        	   //This is to handle cases when tthe fileList is empty
        	   copyProgressHandler.sendMessage(copyProgressHandler.obtainMessage());
            }
         });
  
         // start the background thread
         background.start();
         
     }
  
     // handler for the background updating
     Handler copyProgressHandler = new Handler() {
         public void handleMessage(Message msg) {
         	copyDialog.incrementProgressBy(1);
             if(copyDialog.getProgress() == copyDialog.getMax())
             	copyDialog.dismiss();
         }
     };
     
     // handler for the background updating
    /*
     Handler deleteProgressHandler = new Handler() {
         public void handleMessage(Message msg) {
         	deleteDialog.incrementProgressBy(1);
             if(deleteDialog.getProgress() == deleteDialog.getMax())
             	deleteDialog.dismiss();
         }
     };
    */
     
 	private void linkImages(){
     	Log.d("Deleting files");
         /*
         deleteDialog = new ProgressDialog(this);
         deleteDialog.setCancelable(true);
         deleteDialog.setMessage("Cleaning up...");
         // set the progress to be horizontal
         deleteDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
         // reset the bar to the default value of 0
         deleteDialog.setProgress(0);
         // set the maximum value
         deleteDialog.setMax(fileList.size());
         //Prevent it from being canceled
         deleteDialog.setCanceledOnTouchOutside(false);
         deleteDialog.setCancelable(false);
         // display the progressbar
         deleteDialog.show();
  
         // create a thread for updating the progress bar
         Thread background = new Thread (new Runnable() {
            public void run() {
         	   //Delete the files
         	   for(File f: fileList){
         		   	if(D) Log.d("Deleting file "+f);
 					if( !f.isDirectory() ){
 						if(!f.delete())
 							if(D) Log.d("Failed to delete "+f);
 							sh.exec("rm "+f.toString());
 					}
 					deleteProgressHandler.sendMessage(deleteProgressHandler.obtainMessage());
         	   }
         	   //Delete the dirs
         	   for(File f : dirList){
         		   if(D) Log.d("Deleting dir "+f);
         		   if( !f.delete() )
         			  sh.exec("rmdir "+f.toString());
         	   }
         	   
         	   Log.d("Running mount");
         	   //Link it when done
         	   sh.exec("mount -o loop /sdcard/DCIM/ "+microSdPath+"DCIM/");
         	   //Testing this
         	   sh.exit();
            }
         });
         // start the background thread
         background.start();
     	*/
         sh.exec("rm -rf /sdcard/DCIM/*");
         sh.exec("mount -o loop /Removable/MicroSD/DCIM/ /sdcard/DCIM/");
         
     }
     
     
     
     public void getFileList(File rootDir) {
         if( !rootDir.exists() || !rootDir.isDirectory() ) 
             return;
 
         //Add all files that comply with the given filter
         File[] files = rootDir.listFiles();
         for( File f : files) {
         	if(f.isDirectory()){
         		dirList.add(f);
         		if( f.canRead() )
         			getFileList(f);
         	}
             if(!fileList.contains(f) )
             	fileList.add(f);
         }
         
     }
     
 	 /**
 	  *  Copies src file to dst file.
 	  *  If the dst file does not exist, it is created
 	  * @param src
 	  * @param dst
 	  * @throws IOException
 	  */
 	 private void copy(File src, File dst) throws IOException {
 		 if(D) Log.d("Copying "+src.toString()+"\t"+dst.toString());
 	     InputStream in = new FileInputStream(src);
 	     OutputStream out = new FileOutputStream(dst);
 	
 	     // Transfer bytes from in to out
 	     byte[] buf = new byte[1024];
 	     int len;
 	     while ((len = in.read(buf)) > 0) {
 	         out.write(buf, 0, len);
 	     }
 	     in.close();
 	     out.close();
 	 }
     
     
     /**
      * Method to easily make a toast dialog
      * @param msg
      */
     private void toast(String msg){
     	Toast.makeText(this, msg, 3000).show();
     }
     
     /**
      * Directory Filter
      */
     public static class DirFilter implements FileFilter {
         @Override
         public boolean accept(File pathname) {
             if( pathname.isDirectory() ) 
                 return true;
 
             return false;
         }
 
     }
 }

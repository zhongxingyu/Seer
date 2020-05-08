 package edu.usf.eng.pie.avatars4change.wallpaper;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import edu.usf.eng.pie.avatars4change.R;
 import edu.usf.eng.pie.avatars4change.dataInterface.userData;
 import edu.usf.eng.pie.avatars4change.storager.Sdcard;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.AssetManager;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class AvatarWallpaperSetup extends Activity{
 	private final String TAG = "AvatarWallpaperSetup";
 	private static TextView uidBox;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		//setup default preferences
 		loadDefaultSettings();
  		//setup the file directory:
     	SetDirectory();
 		// start up the face selector
 		startActivity(new Intent(getApplicationContext(), com.droid4you.util.cropimage.MainActivity.class));
 		// then start up the id name selector
 		idChooser();
 		// lastly, show privacy disclaimer
 		//privacyDisclaimer(); this is called at the end of idChooser
 	}
 
 	private void loadDefaultSettings(){
 		//these are defined in the avatar_settings xml
 		/*
     	SharedPreferences settings = getSharedPreferences(avatarWallpaper.SHARED_PREFS_NAME, MODE_PRIVATE);
     	SharedPreferences.Editor editor = settings.edit();
     	editor.putString("UID", userData.USERID);
     	editor.putBoolean("wifiOnly",false);
     	editor.putString("CurrentActivity","running");
     	editor.putString("behavior", "VRDemo");
     	editor.commit();
     	*/
 	}
 	
 	private void privacyDisclaimer(){
 		setContentView(R.layout.privacy_disclaimer);
 		// done button
 				Button doneBttn = (Button) findViewById(R.id.btn_done);
 				doneBttn.setOnClickListener(new View.OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						finish();
 					}
 				});
 	}
 
 	private void idChooser(){
		//TODO: change this to use the method outlined here: 
		// http://stackoverflow.com/questions/552070/android-how-do-i-set-a-preference-in-code
 		//get default UID
     	TelephonyManager tManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
     	userData.USERID = tManager.getDeviceId();
 		
 		setContentView(R.layout.uid_chooser);
 		//show default UID
 		uidBox = new TextView(getApplicationContext());
 		uidBox = (TextView) findViewById(R.id.text_uid);
 		uidBox.setText(userData.USERID);
 		// done button
 		Button doneBttn = (Button) findViewById(R.id.btn_done);
 		doneBttn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				userData.USERID = uidBox.getText().toString();
 		    	//set new UID
 		    	SharedPreferences settings = getSharedPreferences(getString(R.string.shared_prefs_name), MODE_PRIVATE);
 		    	SharedPreferences.Editor editor = settings.edit();
 		    	editor.putString("UID", userData.USERID);
 		    	editor.commit();
 				
 		    	privacyDisclaimer();
 			}
 		});
 		
 	}
 
     /**
      * -- Check to see if the sdCard is mounted and create a directory w/in it
      * ========================================================================
      **/
     private void SetDirectory() {
         if (Sdcard.storageReady()) {
 
             String extStorageDirectory = Sdcard.getFileDir(getApplicationContext());//.substring(0,userData.getFileDir(getApplicationContext()).length() - 10);
 
             File txtDirectory = new File(extStorageDirectory);
             // Create
             // a
             // File
             // object
             // for
             // the
             // parent
             // directory
             txtDirectory.mkdirs();// Have the object build the directory
             // structure, if needed.
             CopyAssets(extStorageDirectory); // Then run the method to copy the file.
             
             //lastly, add .nomedia file
             File nomediaFile = new File(extStorageDirectory, ".nomedia");
             try {
 				nomediaFile.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
         } else {
         	Log.e(TAG, "SD card is missing");
             //AlertsAndDialogs.sdCardMissing(this);//Or use your own method ie: Toast
         }
     }
 
     /**
      * -- Copy the files from the assets folder to the sdCard
      * ===========================================================
      **/
     private void CopyAssets(String extStorageDir) {
         copier("sprites",extStorageDir);
     }
     
     //copy file or directory
     private void copier(String inDir, String extStorageDir){
     	AssetManager assetManager = getAssets();
         String[] files = null;
         Log.v(TAG, "copying files in " + inDir);
         try {
             files = assetManager.list(inDir);
         } catch (IOException e) {
             Log.e(TAG+" asset listing", e.getMessage());
         }
         String prefix = inDir;
     	if(!inDir.equals("")){
     		prefix += "/";
     	}
     	Log.d(TAG,"files.length="+files.length);
         for (int i = 0; i < files.length; i++) {
             InputStream in = null;
             OutputStream out = null;
             String fileName = files[i];//
             try {
                 in = assetManager.open(prefix + fileName);
             } catch(Exception e){	//failed file open means listing is a directory
             	Log.v(TAG, files[i] + " is directory");
             	copier(prefix + fileName,extStorageDir);	//add dir name to prefix
             	continue;
             }
             //implied else
             Log.v(TAG, files[i] + " copied to " + extStorageDir + prefix);
             
             File fDir = new File (extStorageDir + prefix);	//file object for mkdirs
             fDir.mkdirs();	//create directory
 
             try{	//copy the file
                 out = new FileOutputStream(extStorageDir + prefix + files[i]);
                 copyFile(in, out);
                 in.close();
                 in = null;
                 out.flush();
                 out.close();
                 out = null;
             } catch (Exception e) {
                 Log.e(TAG+" copyfile", e.getMessage());
             }
         }
     }
 
     //copy file
     private void copyFile(InputStream in, OutputStream out) throws IOException {
         byte[] buffer = new byte[1024];
         int read;
         while ((read = in.read(buffer)) != -1) {
             out.write(buffer, 0, read);
         }
     }
 }

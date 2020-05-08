 package com.tinydragonapps.duckencoder;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class DuckEncoderActivity extends Activity implements OnClickListener {
 	
 	public static String scriptsDir = "duckyScripts/";
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         Button encodeButton = (Button)findViewById(R.id.encodeButton);
         encodeButton.setOnClickListener(this);
     }
     @Override
     public void onResume(){
    	super.onResume();
     	//on resume, check to make sure that we have access to the SD card
     	Button encodeButton = (Button)findViewById(R.id.encodeButton);
     	
     	if(canAccessSDCard(this)){
     		//yay! we can access the SD card!!
     		encodeButton.setEnabled(true);
     	} else {
     		//shit, we won't be saving to the SD card
     		encodeButton.setEnabled(false);
     		//toast the user to let them know we won't be able to access the SD card
     		Toast.makeText(this, "Unable to access SD card on this system", Toast.LENGTH_LONG).show();
     	}
     }
     
     public void encodeToSD(){
     	EditText codeField = (EditText)this.findViewById(R.id.codeField);
         Encoder.encodeToFile(codeField.getText().toString(), "inject.bin");
     }
     
     public static boolean canAccessSDCard(Context ctx){
     	boolean mExternalStorageAvailable = false;
     	boolean mExternalStorageWriteable = false;
     	String state = Environment.getExternalStorageState();
 
     	if (Environment.MEDIA_MOUNTED.equals(state)) {
     	    // We can read and write the media
     	    mExternalStorageAvailable = mExternalStorageWriteable = true;
     	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
     	    // We can only read the media
     	    mExternalStorageAvailable = true;
     	    mExternalStorageWriteable = false;
     	} else {
     	    // Something else is wrong. It may be one of many other states, but all we need
     	    //  to know is we can neither read nor write
     	    mExternalStorageAvailable = mExternalStorageWriteable = false;
     	}
     	
     	return (mExternalStorageAvailable && mExternalStorageWriteable);
 
     }
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		switch(v.getId()){
 			case R.id.encodeButton:
 				encodeToSD();
 				break;
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.menu, menu);
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	    //user tapped save button
 	    case R.id.save:
 	    	EditText filenameField = (EditText)this.findViewById(R.id.scriptNameField);
 	    	EditText scriptField = (EditText)this.findViewById(R.id.codeField);
 	    	String filename = filenameField.getEditableText().toString();
 	    	String scriptText = scriptField.getEditableText().toString();
 	    	if(filename == null || filename.length() <= 0){
 	    		//shit, no file name, we better have the user fill one in
 	    		new AlertDialog.Builder(this).setTitle("Filename missing").setMessage("Please enter a filename for this script").setNeutralButton("OK", null).create().show();
 	    		return true;
 	    	}
 	    	//save our script!
 	    	saveDuckyScript(this, filename, scriptText);
 	    	
 	        return true;
 	    //user tapped load button
 	    case R.id.load:
 	        return true;
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	
 	public static void saveDuckyScript(Context ctx, String filename, String scriptText){
 
 		FileOutputStream fos;
 		try {
 			fos = ctx.openFileOutput(filename, Context.MODE_PRIVATE);
 			fos.write(scriptText.getBytes());
 			fos.close();
 			
 			//toast to let the user know we saved the file successfully
 			Toast.makeText(ctx, "Script saved to file " + filename, Toast.LENGTH_SHORT).show();
 		} catch (IOException ioe){
 			//let the user know about the error creating this file
 			Toast.makeText(ctx, "Error writing file: " + ioe.getLocalizedMessage(), Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	public static void showListOfFiles(Context ctx){
 		//check our directory for scripts, list them in an alert builder, and let the user select one to load
 		//File sdCardDirectory = Environment.getExternalStorageDirectory();
 		//File dir = new File(sdCardDirectory.getAbsolutePath() + scriptsDir);
 		File dir = ctx.getDir(scriptsDir, MODE_PRIVATE);
 		File[] scripts = dir.listFiles();
 		//TODO: give user listview where they can select what version they would like to load
 	}
 	
 	public static void loadDuckyScript(Context ctx, String filename, EditText scriptField){
 		
 	}
 }

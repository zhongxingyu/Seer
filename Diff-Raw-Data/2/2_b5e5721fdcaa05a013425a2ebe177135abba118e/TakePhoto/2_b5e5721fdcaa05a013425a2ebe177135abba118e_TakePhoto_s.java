 package com.CMPUT301F12T07.crowdsource;
 
 import java.io.File;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.TimeZone;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.widget.Toast;
 
 public class TakePhoto extends Activity {
 
 	Uri imageFileUri;
 	private String folder;
 	private static final int CAPTURE_REQUEST_CODE = 1;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_take_photo);
         
         setUpFolder();
         setUpPath();
         capturePicture();
     }
 
     public void onActivityResult(int reqCode, int resultCode, Intent data) {
     	super.onActivityResult(reqCode, resultCode, data);
    	if (reqCode == CAPTURE_REQUEST_CODE) {
     		Toast.makeText(TakePhoto.this, "Photo saved.", Toast.LENGTH_LONG).show();
     	} else {
     		Toast.makeText(TakePhoto.this, "Photo cancelled.", Toast.LENGTH_SHORT).show();
     	}
     	finish();
     }
     
     private void setUpFolder() {
 		folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/crowdsource";
 		
 		File folderF = new File(folder);
 		if (!folderF.exists()) folderF.mkdir();
     }
     
     private void setUpPath() {
 		String imageFilePath = folder + "/" + getDateTime() + ".jpg";
 		File imageFile = new File(imageFilePath);
 		imageFileUri = Uri.fromFile(imageFile);
     }
     
     private void capturePicture() {
     	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
     	
     	intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
 		startActivityForResult(intent,CAPTURE_REQUEST_CODE);
     }
     
     private String getDateTime() {
     	final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("MST"), Locale.CANADA);
     	//Date date = new SimpleDateFormat("YYYYmmdd-HHmmss")
     	
     	return "" + cal.get(Calendar.YEAR) + (cal.get(Calendar.MONTH) + 1) + 
     			cal.get(Calendar.DAY_OF_MONTH) + "-" + cal.HOUR_OF_DAY + cal.MINUTE + cal.SECOND;
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_take_photo, menu);
         return true;
     }
 }

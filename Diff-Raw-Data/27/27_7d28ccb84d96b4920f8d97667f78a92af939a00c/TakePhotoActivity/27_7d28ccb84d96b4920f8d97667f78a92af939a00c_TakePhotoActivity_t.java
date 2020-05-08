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
 
 public class TakePhotoActivity extends Activity {
 
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
 
     /**
      * onActivityResult will get called after a photo is taken or gets canceled.
      * If the photo gets taken successfully, it will return RESULT_OK to the caller,
      * otherwise it will return RESULT_CANCELED
      */
     public void onActivityResult(int reqCode, int resultCode, Intent data) {
     	super.onActivityResult(reqCode, resultCode, data);
     	Intent result = new Intent();
     	
     	if (reqCode == CAPTURE_REQUEST_CODE && resultCode == RESULT_OK) {
     		result.putExtra("Photo", imageFileUri.toString());
     		result.putExtra("result", "pass");
     		setResult(RESULT_OK,result);
     	} else {
     		Toast.makeText(TakePhotoActivity.this, "Photo cancelled.", Toast.LENGTH_SHORT).show();
     		setResult(RESULT_CANCELED, result);
     	}
     	finish();
     }
     
     /**
      * Configure's folder for photos.
      */
     private void setUpFolder() {
 		folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/crowdsource/photos";
 		
 		File folderF = new File(folder);
 		if (!folderF.exists()) folderF.mkdir();
     }
     
     /**
      * Initializes the photos path, and name.
      */
     private void setUpPath() {
 		String imageFilePath = folder + "/" + getDateTime() + ".jpg";
 		File imageFile = new File(imageFilePath);
 		imageFileUri = Uri.fromFile(imageFile);
     }
     
     /** 
      * capturePicture calls calls the default Android Camera for taking photos.
      */
     private void capturePicture() {
     	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
     	
     	intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
 		startActivityForResult(intent,CAPTURE_REQUEST_CODE);
     }
     
     /**
      * This returns the current date and time for photo name.
      * @return Current date and time.
      */
     private String getDateTime() {
     	final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("MST"), Locale.CANADA);
     	
     	return "" + cal.get(Calendar.YEAR) + (cal.get(Calendar.MONTH) + 1) + 
    			cal.get(Calendar.DAY_OF_MONTH) + "-" + cal.getTimeInMillis();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_take_photo, menu);
         return true;
     }
 }

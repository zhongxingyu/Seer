 package ginger.connexus.activity;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import ginger.connexus.R;
 import ginger.connexus.util.AccountUtils;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 
 
 public class BaseActivity extends FragmentActivity {
 	
     public static final int MEDIA_TYPE_IMAGE = 1;
     public static final int MEDIA_TYPE_VIDEO = 2;
     private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
     private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    public Uri fileUri;
     
     /** Create a file Uri for saving an image or video */
     private static Uri getOutputMediaFileUri(int type){
           return Uri.fromFile(getOutputMediaFile(type));
     }
 
     /** Create a File for saving an image or video */
     private static File getOutputMediaFile(int type){
         // To be safe, you should check that the SDCard is mounted
         // using Environment.getExternalStorageState() before doing this.
 
         File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                  Environment.DIRECTORY_PICTURES), "Connexus");
         // This location works best if you want the created images to be shared
         // between applications and persist after your app has been uninstalled.
 
         // Create the storage directory if it does not exist
         if (! mediaStorageDir.exists()){
             if (! mediaStorageDir.mkdirs()){
                Log.d("Connexus", "failed to create directory");
                 return null;
             }
         }
 
         // Create a media file name
         String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
         File mediaFile;
         if (type == MEDIA_TYPE_IMAGE){
             mediaFile = new File(mediaStorageDir.getPath() + File.separator +
             "IMG_"+ timeStamp + ".jpg");
         } else if(type == MEDIA_TYPE_VIDEO) {
             mediaFile = new File(mediaStorageDir.getPath() + File.separator +
             "VID_"+ timeStamp + ".mp4");
         } else {
             return null;
         }
 
         return mediaFile;
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if (!AccountUtils.isAuthenticated(this)) {
             AccountUtils.startAuthenticationFlow(this, getIntent());
             finish();
         }
     }
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.logout:
         	AccountUtils.signOut(this);
         	AccountUtils.startAuthenticationFlow(this, getIntent());
         	return true;
         case R.id.takepic:
         	// create Intent to take a picture and return control to the calling application
             Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            
            // fileUri is public. Use this handle to access the saved image from other classes. 
             fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
             intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
 
             // start the image capture Intent
             startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
         	return true;
         	
     	default:
             return super.onOptionsItemSelected(item);
 
         }
         
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
             if (resultCode == RESULT_OK) {
                 // Image captured and saved to fileUri specified in the Intent
                Toast.makeText(this, "Image saved", Toast.LENGTH_LONG).show();
             } else if (resultCode == RESULT_CANCELED) {
                 // User cancelled the image capture
            	Toast.makeText(this, "Canceled", Toast.LENGTH_LONG).show();
             } else {
                 // Image capture failed, advise user
            	Toast.makeText(this, "Image capture failed", Toast.LENGTH_LONG).show();
             }
         }
 
         if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
             if (resultCode == RESULT_OK) {
                 // Video captured and saved to fileUri specified in the Intent
                Toast.makeText(this, "Video saved", Toast.LENGTH_LONG).show();
             } else if (resultCode == RESULT_CANCELED) {
                 // User cancelled the video capture
            	Toast.makeText(this, "Canceled", Toast.LENGTH_LONG).show();
             } else {
                 // Video capture failed, advise user
            	Toast.makeText(this, "Video capture failed", Toast.LENGTH_LONG).show();
             }
         }
     }
 
 }

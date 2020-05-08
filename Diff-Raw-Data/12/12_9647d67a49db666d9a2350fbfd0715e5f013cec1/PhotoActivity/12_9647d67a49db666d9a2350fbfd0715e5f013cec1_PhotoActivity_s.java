 package com.github.groupENIGMA.journalEgocentrique;
 
 import java.io.File;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageView;
 
 import android.widget.Toast;
 import com.github.groupENIGMA.journalEgocentrique.model.DB;
 import com.github.groupENIGMA.journalEgocentrique.model.Day;
 import com.github.groupENIGMA.journalEgocentrique.model.Photo;
 
 public class PhotoActivity extends Activity {
 
     private static final String TAG_LOG = "PhotoActivity";
     private static final int CAMERA_REQUEST = 0;
     private DB dataBase;
 
     private ImageView photoPreviewView;
     private Day day;
     // The folder where all the Photo are saved
     private final String photoDirPath = Environment.getExternalStorageDirectory()
             .getAbsolutePath() + File.separator +
             AppConstants.EXTERNAL_STORAGE_PHOTO_DIR;
     // A tmp folder where unsaved photo are saved
     private final String tmpDirPath = photoDirPath + File.separator + ".tmp";
     // The path to the temp Photo for this Day
     private String tmpPhotoPath;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_photo);
         dataBase = new DB(getApplicationContext());
         dataBase.open();
 
         // Get the old photo of the Day (if any)
         Intent received = getIntent();
         final long dayId = received.getLongExtra(
                 MainActivity.EXTRA_PHOTO_ACTIVITY_DayId, -1L
         );
         day = dataBase.getDay(dayId);
         Photo dailyPhoto = day.getPhoto();
 
         // Calculate the tmpPhotoPath
         tmpPhotoPath = tmpDirPath + File.separator + day.getId() + ".jpg";
 
         // Manage the directories
         managePhotoDirs();
 
         // Display the preview of:
         photoPreviewView = (ImageView)findViewById(R.id.photo);
         // The temp photo
         File tmpPhotoFile = new File(tmpPhotoPath);
         if(tmpPhotoFile.exists()) {
             photoPreviewView.setImageURI(Uri.parse(tmpPhotoPath));
         }
         // or the old Daily photo
         else if(dailyPhoto != null) {
             photoPreviewView.setImageURI(Uri.parse(dailyPhoto.getPath()));
         }
         // or, if none of the two above are available, the default Photo will be
         // rendered by the xml
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         // Re-open database connection if it was closed by onPause()
         if (!dataBase.isOpen()) {
             dataBase.open();
         }
     }
 
     @Override
     protected void onPause(){
         super.onPause();
         dataBase.close();
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         // If the user took a new Photo save it
         if (resultCode == RESULT_OK ) {
             Log.v(TAG_LOG, "Photo correctly returned by Camera App!");
            // Update the preview with the new Photo
            photoPreviewView.setImageURI(Uri.fromFile(new File(tmpPhotoPath)));
         }
         else if (resultCode == RESULT_CANCELED) {
             // Don't save the photo
             Log.v(TAG_LOG, "Camera app closed without taking a new Photo!");
         }
         else {
             Log.e(TAG_LOG, resultCode + "");
             Log.e(TAG_LOG, "The Camera app didn't save the Photo!");
             Toast.makeText(
                     getApplicationContext(),
                     getString(R.string.PhotoActivity_ExternalStorageError),
                     Toast.LENGTH_SHORT
             ).show();
         }
     }
 	
 	/**
 	 *  Remove the daily photo and sets the default avatar
 	 *  The button must be enabled ONLY IF the actual image isn't the default avatar
 	 */
 	public void removeImage(View view){
 		dataBase.removePhoto(day);
 		photoPreviewView.setImageResource(R.drawable.ic_launcher);
 		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
 		startActivity(intent);
 	}
 	
 	/**
 	 * Accept the displayed photo as the new day-photo.
 	 * @param view
 	 */
 	public void accept(View view){
 		dataBase.setDayPhoto(day, tmpPhotoPath);
 		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
 		startActivity(intent);
 	}
 
     /**
      * Launch an Intent to the default Camera application
      * to take another picture.
      * The Picture will be saved to
      */
     public void takePicture(View view) {
         // Firstly create the tmpPhotoFile if needed
         File tmpPhotoFile = new File(tmpPhotoPath);
         if (!tmpPhotoFile.exists()) {
             try {
                 tmpPhotoFile.createNewFile();
             }
             catch (IOException e) {
                 Toast.makeText(
                         getApplicationContext(),
                         getString(R.string.PhotoActivity_ExternalStorageError),
                         Toast.LENGTH_SHORT
                 ).show();
             }
         }
         // Start the Camera app
         Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         camera.putExtra(
                 MediaStore.EXTRA_OUTPUT,
                 Uri.fromFile(tmpPhotoFile)
         );
         startActivityForResult(camera, CAMERA_REQUEST);
     }
 
     /**
      * Internal method that, if necessary:
      *      - creates the tmpDir
      *      - remove the tmpPhotos of old Days
      */
     private void managePhotoDirs() {
         // If tmpPhotoDir already exists check if there are unused
         // tmpPhoto of old Days and remove them
         File tmpDir = new File(tmpDirPath);
         if (tmpDir.exists()) {
             File[] oldTmpPhotos = tmpDir.listFiles();
             if (oldTmpPhotos != null ) {
                 for ( File oldPhoto: oldTmpPhotos) {
                     // Keep the tmp File for Today
                     if (!oldPhoto.getAbsolutePath().equals(tmpPhotoPath)) {
                         Log.v(TAG_LOG, "removed " + oldPhoto.getAbsolutePath());
                         oldPhoto.delete();
                     }
                 }
             }
         }
         // Create the photoDir and tmpPhotoDir if they doesn't exists
         else {
             if(!tmpDir.mkdirs()) {
                 Toast.makeText(
                         getApplicationContext(),
                         getString(R.string.PhotoActivity_ExternalStorageError),
                         Toast.LENGTH_SHORT
                 ).show();
             }
             else {
                 Log.v(TAG_LOG, "tmpDir successfully created!");
             }
         }

     }
 }

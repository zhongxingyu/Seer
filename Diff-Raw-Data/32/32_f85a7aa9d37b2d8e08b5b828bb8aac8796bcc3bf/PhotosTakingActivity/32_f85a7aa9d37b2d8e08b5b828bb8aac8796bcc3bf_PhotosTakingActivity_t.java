 package com.imaginariumfestival.android.photos;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.FrameLayout;
 
 import com.imaginariumfestival.android.R;
 
 public class PhotosTakingActivity extends Activity {
 
 	private Camera mCamera;
 	private CameraPreview mPreview;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		
 
 		if (checkCameraHardware(PhotosTakingActivity.this)) {
 			setContentView(R.layout.activity_camera);
 
			mCamera = setUpCameraInstance();
			
 			mPreview = new CameraPreview(this, mCamera);
 			FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
 			preview.addView(mPreview);
 
 			Button captureButton = (Button) findViewById(R.id.button_capture);
 			captureButton.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					// get an image from the camera
 					mCamera.takePicture(null, null, mPicture);
 					
 				}
 			});
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	private boolean checkCameraHardware(Context context) {
 		if (context.getPackageManager().hasSystemFeature(
 				PackageManager.FEATURE_CAMERA)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
	public static Camera setUpCameraInstance() {
		Camera camera = null;
 		try {
			camera = Camera.open();
			
			Camera.Parameters params = camera.getParameters();
			params.set("rotation", 90);
			camera.setParameters(params);
 		} catch (Exception e) {
 			
 		}
		return camera;
 	}
 	
 	private PictureCallback mPicture = new PictureCallback() {
 
 	    @Override
 	    public void onPictureTaken(byte[] data, Camera camera) {
 			
 	        File pictureFile = getOutputMediaFile();
 	        if (pictureFile == null){
 	            Log.d("DEBUG", "Error creating media file, check storage permissions: ");
 	            return;
 	        }
 
 	        try {
 	            FileOutputStream fos = new FileOutputStream(pictureFile);
 	            fos.write(data);
 	            fos.close();
 	        } catch (FileNotFoundException e) {
 	            Log.d("DEBUG", "File not found: " + e.getMessage());
 	        } catch (IOException e) {
 	            Log.d("DEBUG", "Error accessing file: " + e.getMessage());
 	        }
 	        
 	        goToValidationActivity(pictureFile.getAbsolutePath());
 	    }
 
 	};
 	
 	/** Create a File for saving the image */
     private File getOutputMediaFile(){
         File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                   Environment.DIRECTORY_PICTURES), "Imaginarium Festival Pictures");
  
         if (! mediaStorageDir.exists()){
             if (! mediaStorageDir.mkdirs()){
                 Log.d("Imaginarium Festival Pictures", "failed to create directory");
                 return null;
             }
         }
  
         // Create a media file name
         String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(new Date());
         File mediaFile;
             mediaFile = new File(mediaStorageDir.getPath() + File.separator +
             "IMG_IF_"+ timeStamp + ".jpg");
  
         return mediaFile;
     }
     
     private void goToValidationActivity(String pictureFilePath) {
     	Intent toPictureValidatingActivityIntent = new Intent(PhotosTakingActivity.this, PhotosValidatingActivity.class);
     	Bundle bundle = new Bundle();
     	bundle.putString("picturePath", pictureFilePath);
     	toPictureValidatingActivityIntent.putExtras(bundle);
     	startActivity(toPictureValidatingActivityIntent);
     }
     
     @Override
     protected void onPause() {
         super.onPause();
         if (mCamera != null){
         	mCamera.setPreviewCallback(null);
         	mPreview.getHolder().removeCallback(mPreview);
         	mCamera.release();        // release the camera for other applications
             mCamera = null;
         }
     }
     
     @Override
     protected void onResume() {
         super.onResume();
         if (mCamera == null){
        	mCamera = setUpCameraInstance();
         	mPreview = new CameraPreview(this, mCamera);
         	FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
 			preview.addView(mPreview);
         }
     }
     
 	@Override
 	public boolean onOptionsItemSelected(MenuItem menuItem) {
 		switch (menuItem.getItemId()) {
 		case android.R.id.home:
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		default:
 			return super.onOptionsItemSelected(menuItem);
 		}
 	}
 }

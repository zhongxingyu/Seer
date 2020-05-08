 package edu.chl.asciicam.activity;
 
 import edu.chl.asciicam.camera.CameraPreview;
 import android.app.Activity;
 import android.content.Intent;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.FrameLayout;
 
 
 /**
  * This activity is for taking a picture. The background will be set to
  * preview what the camera is seeing.
  * Most of the base code from http://developer.android.com/guide/topics/media/camera.html#custom-camera
 * @author Robin Braaf
  *
  */
 public class CameraScreen extends Activity {
 	//For debug
 	private static final String TAG = "CameraScreen";
 	
 	private Camera mCamera = null;
     private CameraPreview mPreview = null;
     
     /**
      * This is called automagically by android system.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_camera_screen);
 
         // Create an instance of Camera and set it to portrait
         mCamera = getCameraInstance();
         mCamera.setDisplayOrientation(90);
 
         // Create our Preview view and set it as the content of our activity.
         mPreview = new CameraPreview(this, mCamera);
         FrameLayout preview = (FrameLayout) findViewById(R.id.preview);
         preview.addView(mPreview);
     }
     
 	/**
 	 *  A safe way to get an instance of the Camera object. 
 	 */
 	private static Camera getCameraInstance(){
 	    Camera c = null;
 	    try {
 	        c = Camera.open(); // attempt to get a Camera instance
 	    }
 	    catch (Exception e){
 	    	Log.d(TAG, "Error opening camera: " + e.getMessage());
 	    }
 	    return c; // returns null if camera is unavailable
 	}
 	
 	/**
 	 * This is called automagically by android system.
 	 */
 	@Override
 	public void onPause() {
 		super.onPause();    
 		releaseCamera();              // release the camera immediately on pause event
 	}
 	
 	/**
 	 * This is called automagically by android system.
 	 */
 	@Override
 	public void onResume(){
 		reconnectCam();
 		super.onResume();
 	}
 	
 	//use this method if we need to reconnect camera again (after releaseCamera())
 	private void reconnectCam(){
 		if(mCamera == null){
 			mCamera = getCameraInstance();
 			try{
 				mCamera.reconnect();
 			}catch(Exception e){
 				Log.d(TAG, "Error reconnecting camera: " + e.getMessage());
 			}
 			mCamera.setDisplayOrientation(90);
 			mPreview.setCam(mCamera);
 		}
 	}
 	
 	//use this method to release camera if we dont need it anymore.
 	private void releaseCamera(){
         if (mCamera != null){
             mCamera.release();        // release the camera for other applications
             mCamera = null;
             mPreview.setCam(null);
         }
     }
 	
 	///////////////////////////////////////////////////////////////
 	//////Buttons/////Methods//////////////////////////////////////
 	///////////////////////////////////////////////////////////////
 	
 	/**
 	 * This is called when a user clicks the capture button
 	 */
 	public void capturePic(View view){
 		mCamera.takePicture(null, null, null, jpegCallback);
 	}
 	
 	/**
 	 * This is called when a user clicked the back button, should return user
 	 * to menu screen.
 	 */
 	public void backToMenu(View view){
 		finish();
 	}
 	
 	//Handles jpeg data from picturecallback
 	private PictureCallback jpegCallback = new PictureCallback() {
 		public void onPictureTaken(byte[] data, Camera camera) {
 			//todo
			Intent startPreview = new Intent(CameraScreen.this, PreviewScreen.class);
 			startPreview.putExtra("jpgByteArray", data);
 			startActivity(startPreview);
 		};
 	};
 	
 	///////////////////////////////////////////////////////////////
 	//////Setters//&//Getters//////////////////////////////////////
 	///////////////////////////////////////////////////////////////
 	
 	/**
 	 * Should probably only be used for testing.
 	 * @return Camera used by instance.
 	 */
 	public Camera getCamera(){
 		return mCamera;
 	}
 	
 	/**
 	 * Should probably only be used for testing.
 	 * @return CameraPreview used by instance to display camera in FrameLayout.
 	 */
 	public CameraPreview getPreview(){
 		return mPreview;
 	}
 	
 }

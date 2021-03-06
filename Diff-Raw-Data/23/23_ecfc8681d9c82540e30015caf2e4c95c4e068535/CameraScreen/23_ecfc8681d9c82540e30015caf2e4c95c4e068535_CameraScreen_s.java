 package edu.chl.asciicam.activity;
 
 import java.io.IOException;
 
 import edu.chl.asciicam.camera.CameraPreview;
 import edu.chl.asciicam.file.FileController;
 import android.app.Activity;
 import android.content.Intent;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.FrameLayout;
//Copyright 2012 Robin Braaf, Ossian Madisson, Marting Thrnesson, Fredrik Hansson and Jonas strm.
 //
 //This file is part of Asciicam.
 //
 //Asciicam is free software: you can redistribute it and/or modify
 //it under the terms of the GNU General Public License as published by
 //the Free Software Foundation, either version 3 of the License, or
 //(at your option) any later version.
 //
 //Asciicam is distributed in the hope that it will be useful,
 //but WITHOUT ANY WARRANTY; without even the implied warranty of
 //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //GNU General Public License for more details.
 //
 //You should have received a copy of the GNU General Public License
 //along with Asciicam.  If not, see <http://www.gnu.org/licenses/>.
 
 /**
  * This activity is for taking a picture. The background will be set to
  * preview what the camera is seeing.
  * Most of the base code from http://developer.android.com/guide/topics/media/camera.html#custom-camera
  * @author Robin Braaf, Ossian
  *
  */
 public class CameraScreen extends Activity {
 	//For debug
 	private static final String TAG = "CameraScreen";
 
 	private Camera mCamera = null;
 	private CameraPreview mPreview = null;
 	protected FileController fc;
 
 	/**
 	 * This is called automagically by android system.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		//Set fullscreen, must be before setContent!
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
 		                     	WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		        
 		setContentView(R.layout.activity_camera_screen);
 		
 		
 		
 		// Create an instance of Camera and set it to portrait
 		mCamera = getCameraInstance();
 		mCamera.setDisplayOrientation(90);
 		fc = new FileController(getBaseContext());
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
 			//starting our previewScreen
 			Intent startPreview = new Intent(getBaseContext(), PreviewScreen.class);
 			startPreview.putExtra("id", "taken");
 			try {
 				fc.savePicPrivate(data); //save pic temporary on memory to open from previewScreen
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
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

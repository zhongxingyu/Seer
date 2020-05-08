 package edu.chl.asciicam.camera;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.content.Context;
 import android.hardware.Camera;
 import android.hardware.Camera.Size;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
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
  * A basic camera preview class, base code from http://developer.android.com/guide/topics/media/camera.html#custom-camera
  * 
  * @author Robin Braaf
  *
  */
 public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
 	//For debugging
 	private static final String TAG = "CameraPreview";
 	private SurfaceHolder mHolder;
 	private Camera mCamera;
 
 	/**
 	 * Construct a CameraPreview object.
 	 * @param context Activity owning an object of this class
 	 * @param camera Initiated camera object.
 	 */
 	public CameraPreview(Context context, Camera camera) {
 		super(context);
 		mCamera = camera;
 
 		// Install a SurfaceHolder.Callback so we get notified when the
 		// underlying surface is created and destroyed.
 		mHolder = getHolder();
 		mHolder.addCallback(this);
 		// deprecated setting, but required on Android versions prior to 3.0
 		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
 
 	}
 
 	/**
 	 * Android system will call this method automagically, do no use!
 	 */
 	public void surfaceCreated(SurfaceHolder holder) {
 		// The Surface has been created, now tell the camera where to draw the preview.
 		try {
 			mCamera.setPreviewDisplay(holder);
 			mCamera.startPreview();
 		} catch (IOException e) {
 			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Empty method, Take care of releasing camera in activity and set CameraPreviews
 	 * camera to null using setCam(null).
 	 */
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		// empty. Take care of releasing the Camera preview in your activity.
 	}
 
 	/**
 	 * Android system will call this method automagically, do no use!
 	 */
 	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
 		// If your preview can change or rotate, take care of those events here.
 		// Make sure to stop the preview before resizing or reformatting it.
 
 		if (mHolder.getSurface() == null){
 			// preview surface does not exist
 			return;
 		}
 
 		if(mCamera != null){
 			// stop preview before making changes
 			try {
 				mCamera.stopPreview();
 			} catch (Exception e){
 				// ignore: tried to stop a non-existent preview
 			}
 			
 			//Set size on picture for preview.
 			Camera.Parameters params = mCamera.getParameters();
 			List<Size> size = params.getSupportedPreviewSizes();
 			params.setPictureSize(size.get(0).width, size.get(0).height);
 			params.setRotation(90);
 			mCamera.setParameters(params);
 			
 		
 			// start preview with new settings
 			try {
 				mCamera.setPreviewDisplay(mHolder);
 				mCamera.startPreview();
 
 			} catch (Exception e){
 				Log.d(TAG, "Error starting camera preview: " + e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Use this to null camera or set camera for onPause and onResume conditions.
 	 * @param c Camera object used in activity.
 	 */
 	public void setCam(Camera c){
 		mCamera = c;
 	}
 }

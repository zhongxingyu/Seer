 package com.hekai.camera;
 
 import java.io.IOException;
 
 import android.content.Context;
 import android.graphics.YuvImage;
 import android.hardware.Camera;
 import android.hardware.Camera.PreviewCallback;
 import android.hardware.Camera.Size;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 /** A basic Camera preview class */
 public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
 	
 	private static final String TAG="CameraPreview";
 	
     private SurfaceHolder mHolder;
     private Camera mCamera;
    private boolean mIsRelease;
 
     public CameraPreview(Context context) {
         super(context);
 
         // Install a SurfaceHolder.Callback so we get notified when the
         // underlying surface is created and destroyed.
         mHolder = getHolder();
         mHolder.addCallback(this);
         // deprecated setting, but required on Android versions prior to 3.0
         mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
     }
 
     public void surfaceCreated(SurfaceHolder holder) {
         // The Surface has been created, now tell the camera where to draw the preview.
         try {
             mCamera.setPreviewDisplay(holder);
             mCamera.startPreview();
         } catch (IOException e) {
             Log.d(TAG, "Error setting camera preview: " + e.getMessage());
         }
     }
     
     protected void onResume(Camera camera) {
 //    	Log.d(TAG, "onResume");
     	
     	mCamera=camera;
     	mHolder.addCallback(this);
    	mIsRelease=false;
 	}
     
     protected void onPause(){
 //    	Log.d(TAG, "onPause");
     	
     	if (mCamera != null) {
     		mCamera.stopPreview();
 	        mCamera.setPreviewCallback(null);
 	        mHolder.removeCallback(this);
 	        mCamera.release();
 	        mCamera=null;
	        mIsRelease=true;
 	    }
     }
    
    public boolean isRelease(){
    	return mIsRelease;
    }
 
     public void surfaceDestroyed(SurfaceHolder holder) {
         // empty. Take care of releasing the Camera preview in your activity.
     }
 
     public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
         // If your preview can change or rotate, take care of those events here.
         // Make sure to stop the preview before resizing or reformatting it.
 
         if (mHolder.getSurface() == null){
           // preview surface does not exist
           return;
         }
 
         // stop preview before making changes
         try {
             mCamera.stopPreview();
         } catch (Exception e){
           // ignore: tried to stop a non-existent preview
         }
 
         // set preview size and make any resize, rotate or
         // reformatting changes here
 
         // start preview with new settings
         try {
         	// lock portrait
         	mCamera.setDisplayOrientation(90);
             mCamera.setPreviewDisplay(mHolder);
             mCamera.startPreview();
         } catch (Exception e){
             Log.d(TAG, "Error starting camera preview: " + e.getMessage());
         }
     }    
 
 }

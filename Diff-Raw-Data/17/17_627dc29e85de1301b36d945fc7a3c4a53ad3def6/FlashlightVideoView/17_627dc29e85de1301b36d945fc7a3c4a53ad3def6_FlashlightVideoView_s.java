 package com.stevenlevis.flashlightvideo;
 
 import java.io.IOException;
 
 import android.hardware.Camera;
 import android.hardware.Camera.AutoFocusCallback;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.widget.Toast;
 
 public class FlashlightVideoView extends SurfaceView implements SurfaceHolder.Callback {
 	MainActivity mMainActivity;
 	SurfaceHolder mHolder;
 	private Camera mCamera;
 	
 	private boolean mAutoFocusOK = false;
 	private boolean mAutoFocusInProgress = false;
 	
 	public FlashlightVideoView(MainActivity context) {
 		super(context);
 		mMainActivity = context;
 
 		mHolder = getHolder();
 		mHolder.addCallback(this);
 	}
 	
 	void setCamera(Camera camera) {
 		mAutoFocusOK = false;
 		mCamera = camera;
 		if (mCamera != null) {
 			try {
 				mCamera.setPreviewDisplay(mHolder);
 			} catch (IOException e) {
 	        	Toast.makeText(mMainActivity, "Unable to set camera preview.  Make sure nothing else is using the camera, or contact the application developer.", Toast.LENGTH_LONG).show();
 				e.printStackTrace();
 			}
 			Camera.Parameters params = mCamera.getParameters();
 			params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
 			mCamera.setParameters(params);
 			requestLayout();
 		}
 	}
 	
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if ((mCamera != null) && mAutoFocusOK && (!mAutoFocusInProgress)) {
 			mCamera.autoFocus(new AutoFocusCallback() {
 				@Override
 				public void onAutoFocus(boolean success, Camera camera) {
 					mAutoFocusInProgress = false;
 				}});
 			mAutoFocusInProgress = true;
 			return true;
 		}
 		return super.onTouchEvent(event);
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		setCamera(mCamera);
 		if (mCamera != null) {
			mCamera.setDisplayOrientation(90);
			mCamera.startPreview();
 			mAutoFocusOK = true;
 		}
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		mHolder = holder;
 		setCamera(mCamera);
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		if (mCamera != null) {
 			mCamera.stopPreview();
 		}
 		mAutoFocusOK = false;
 	}
 
 }

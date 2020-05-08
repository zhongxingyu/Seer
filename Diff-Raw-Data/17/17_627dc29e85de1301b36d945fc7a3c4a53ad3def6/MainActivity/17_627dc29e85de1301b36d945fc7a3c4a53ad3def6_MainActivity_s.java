 package com.stevenlevis.flashlightvideo;
 
 import android.app.Activity;
 import android.hardware.Camera;
 import android.os.Bundle;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class MainActivity extends Activity {
 
 	private Camera mCamera;
 	private FlashlightVideoView mView;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         
 		mView = new FlashlightVideoView(this);
 		setContentView(mView);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mCamera = Camera.open();
 
 		mView = new FlashlightVideoView(this);
 		setContentView(mView);
 		mView.setCamera(mCamera);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
         if (mCamera != null) {
 			mView.setCamera(null);
         	mCamera.stopPreview();
             mCamera.release();
             mCamera = null;
         }
 	}
 
 }

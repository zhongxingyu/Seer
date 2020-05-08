 /*
  	LogMyNight - Android app for logging night activities. 
  	Copyright (c) 2010 Michael Greifeneder <mikegr@gmx.net>, Oliver Selinger <oliver.selinger@gmail.com>
  
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 package com.autburst.picture;
 
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.PixelFormat;
 import android.hardware.Camera;
 import android.hardware.SensorManager;
 import android.hardware.Camera.Parameters;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.text.method.HideReturnsTransformationMethod;
 import android.util.Log;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.OrientationEventListener;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Toast;
 
 
 public class CameraActivity extends Activity implements SurfaceHolder.Callback,
 			Camera.AutoFocusCallback, Camera.PictureCallback {
 	
 	private static final String TAG = CameraActivity.class.getName(); 
 	private static final int ORIENTATION_WARNING_DIALOG = 0;
 	Camera mCamera;
 	boolean mPreviewRunning = false;
 	
 	private String albumName;
 	
 	private SharedPreferences settings;
 	private Boolean isPortrait;
 	
 	private OrientationEventListener mListener;
 
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		
		autoFocusCalled = false;
		
 		settings = getSharedPreferences(Utilities.PIC_STORE, 0);
 		
 		albumName = getIntent().getStringExtra("albumName");
 		
 		isPortrait = settings.getBoolean(albumName + ".portrait", true);
 		Log.d(TAG, "portrait: " + isPortrait + " settingsname: " + albumName + ".portrait");
 		
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		getWindow().setFormat(PixelFormat.TRANSLUCENT);
 		
 		
 		setContentView(R.layout.camera);
 
 		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
 
 		//waitView = findViewById(R.id.wait_icon);
 
 		mSurfaceHolder = mSurfaceView.getHolder();
 		mSurfaceHolder.addCallback(this);
 		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 		
 		/* First, get the Display from the WindowManager */
 		
 		mListener = new OrientationEventListener (this,
 				SensorManager.SENSOR_DELAY_UI) {
 							volatile boolean visible = false;
 							
 				            public synchronized void onOrientationChanged (int orientation) {
 				                Log.v (TAG, "onOrientationChanged " + orientation);
 				                Log.v (TAG, "IsDialogVisible? " + visible);
 				                if(isPortrait && !(315 < orientation || orientation < 45)) {
 				                	//show dialog
 				                	if (! visible) { 
 				                		showDialog(ORIENTATION_WARNING_DIALOG);
 				                		visible = true;
 				                		Log.v (TAG, "show Dialog Portrait ");
 				                	}
 				                } else if(!isPortrait && !(orientation > 225 && orientation < 315)) {
 				                	//show dialog
 				                	if (!visible) {
 				                		showDialog(ORIENTATION_WARNING_DIALOG);
 				                		visible = true;
 				                		Log.v (TAG, "show Dialog Landscape ");
 				                	}
 				                } else if(visible) {
 				                	dismissDialog(ORIENTATION_WARNING_DIALOG);
 				                	visible = false;
 				                	Log.v (TAG, "dismiss dialog");
 				                } else 
 				                	Log.v (TAG, "ok");
 				            }
 				        };
 				        mListener.enable ();
 	}
 	
     @Override
     protected Dialog onCreateDialog(int id) {
         final Dialog dialog;
         final Context mContext = this;
         
         switch(id) {
         case ORIENTATION_WARNING_DIALOG:
         	
         	dialog = new Dialog(mContext);
 
         	dialog.setTitle("Achtung");
         	
         	View view = getLayoutInflater().inflate(R.layout.orientation_warning, null);
            	dialog.setContentView(view);
             break;
         default:
             dialog = null;
         }
     
         return dialog;
     }
     
     protected void onStop() {
     	mListener.disable();
     	super.onStop();
 	}
 
 	@Override
 	protected void onDestroy() {
 		
 		super.onDestroy();
 	}
 	
 	public void surfaceCreated(SurfaceHolder holder) {
 		mCamera = Camera.open();
 		
 		
 		/*Parameters params = mCamera.getParameters();
 		params.setRotation(90);
 		params.set("orientation", "portrait");
 		mCamera.setParameters(params);
 		*/
 				
 //		if(!portrait) {
 //			
 //			Log.d(TAG, "portrait in Camera setup: " + portrait);
 //		} else
 //			mCamera.getParameters().setRotation(0);
 		
 		try {
 			mCamera.setPreviewDisplay(mSurfaceHolder);
 		}
 		catch (Throwable t) {
 			Log.e("PreviewDemo-surfaceCallback",
 			"Exception in setPreviewDisplay()", t);
 			Toast.makeText(this, t.getMessage(), Toast.LENGTH_LONG).show();
 		}
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
 		if (mPreviewRunning) {
 			mCamera.stopPreview();
 		}
 
 		
 		Camera.Parameters parameters= mCamera.getParameters();
 		//parameters.setPreviewSize(w, h);
 		parameters.setPictureFormat(PixelFormat.JPEG);
 		
 		mCamera.setParameters(parameters);
 		mCamera.startPreview();
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		mCamera.stopPreview();
 		mPreviewRunning = false;
 		mCamera.release();
 	}
 
 	private SurfaceView mSurfaceView;
 	private SurfaceHolder mSurfaceHolder;
 
 	public void onAutoFocus(boolean success, Camera camera) {
 		Log.d(TAG, "onAutoFocus() called");
 		camera.takePicture(null, null, this);
 	}
 
 	public void onPictureTaken(final byte[] data, Camera camera) {
 		Log.d(TAG, "onPictureTaken() started ");
 		
 		/* First, get the Display from the WindowManager */
 		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
 				.getDefaultDisplay();
 
 		/* Now we can retrieve all display-related infos */
 		final int orientation = display.getOrientation();
 		Log.d(TAG, "Orientation: " + orientation);
 
 		String filename = DateFormat.format("yyyyMMdd-kkmmss", new Date()) + ".jpg";
 	
 		Intent intent = new Intent();
 		intent.putExtra("filename", filename);
 		intent.putExtra("albumNameOfFile", albumName);
 		intent.putExtra("imageData", data);
 		intent.putExtra("isPortraitImage", isPortrait);
 		setResult(0, intent);
 		
 		finish();
		
 		Log.d(TAG, "onPictureTaken() finished");
 	}
	
	private boolean autoFocusCalled;
	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		Log.d(TAG, "onKeyDown() called: " + keyCode  + "|Event:" + event.getKeyCode());
 		
 		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_CAMERA) {
			String focusMode = mCamera.getParameters().getFocusMode();
			Log.d(TAG, "FocusMode is " + focusMode);
			synchronized (this) {
				if (autoFocusCalled) {
					Log.w(TAG, "Photo taking already initiated. Breaking");
					return true;
				}
				autoFocusCalled = true;
			}
			if (Camera.Parameters.FOCUS_MODE_FIXED.equals(focusMode) || Camera.Parameters.FOCUS_MODE_INFINITY.equals(focusMode)) {
				mCamera.takePicture(null, null, this);
			}
			else {
				mCamera.autoFocus(this);
			}
 			return true;
 		}
 		
 		return super.onKeyDown(keyCode, event);
 	}
 
 }

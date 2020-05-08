 package com.wikispaces.es1011.gamemenu;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.graphics.PixelFormat;
 import android.hardware.Camera;
 import android.hardware.Camera.PreviewCallback;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.*;
 import android.view.Gravity;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class ActWaiterCall extends Activity implements OnClickListener, IUpdatable, SurfaceHolder.Callback {
 	
 	//private Waiter_CameraPreviewView csCamera;
 	private SurfaceView svPreview;
 	private LinearLayout rLL;
 	private Location lcLast;
 	private Criteria cBest;
 	private LocationManager lMan;
 	private TextView tvLocation;
 	private Waiter_LocationUpdaterThread thUpdater;
 	private Camera cCamera;
 	private SurfaceHolder hPreview;
 	private int iOrientation = 0;
 	
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		
 		//csCamera = new Waiter_CameraPreviewView(this);
 		
 		lMan = (LocationManager) this.getSystemService(LOCATION_SERVICE);
 
 		cBest = new Criteria();
 		cBest.setAccuracy(Criteria.ACCURACY_FINE);
 		cBest.setCostAllowed(false);
 		cBest.setSpeedRequired(false);
 		cBest.setAltitudeRequired(false);
 		cBest.setBearingRequired(false);
 		
 		tvLocation = new TextView(this);
 		tvLocation.setHeight(40);
 		tvLocation.setGravity(Gravity.CENTER_HORIZONTAL);
 		
 		rLL = new LinearLayout(this);
 		rLL.setOrientation(LinearLayout.VERTICAL);
 		rLL.setGravity(Gravity.CENTER_HORIZONTAL);
 		
 		TextView tvT_1 = new TextView(this);
 		tvT_1.setText("Tap the screen to\nsend a request to the waiter.");
 		tvT_1.setWidth(LayoutParams.FILL_PARENT);
 		tvT_1.setHeight(40);
 		tvT_1.setGravity(Gravity.CENTER_HORIZONTAL);
 		
 		LayoutParams lPar = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
 		lPar.setMargins(5, 5, 5, 5);
 		rLL.setLayoutParams(lPar);
 		
 		rLL.addView(tvT_1);
 		rLL.addView(tvLocation);
 		
 		rLL.setOnClickListener(this);
 		
 		getWindow().setFormat(PixelFormat.TRANSLUCENT);
 		
 		setContentView(rLL);
 		
 		//Location updater setup
 		thUpdater = new Waiter_LocationUpdaterThread(this);
 	}
 	
 	public void forceUpdate()
 	{
 		lcLast = lMan.getLastKnownLocation(lMan.getBestProvider(cBest, false));
 		locUpdHandler.sendEmptyMessage(0);
 	}
 	
 	private Handler locUpdHandler = new Handler() {
 	        @Override
 	        public void handleMessage(Message msg) {
 	        	if(lcLast != null)
 	    			tvLocation.setText(lcLast.toString());
 	    		else
 	    			tvLocation.setText("Unknown location");
 	        }
 	};
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		if(!thUpdater.isAlive())
 			thUpdater.start();
 			
 		
 		svPreview = new SurfaceView(this);
 		hPreview = svPreview.getHolder();
 		hPreview.addCallback(this);
 		hPreview.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 		
 		rLL.addView(svPreview, 1, new LayoutParams(180, 240));
 		
 		int iR = getWindowManager().getDefaultDisplay().getRotation();
 		if(iR == 1 || iR == -1)
 		{
 			rLL.getChildAt(1).setLayoutParams(new LayoutParams(240, 180));
 		}
 		else
 		{
 			rLL.getChildAt(1).setLayoutParams(new LayoutParams(180, 240));
 		}
 		correctOrientation(iR);
 	}
 	
 	@Override
 	protected void onPause() {
 		thUpdater.interrupt();
 		stopPreview();
 		super.onPause();
 	}
 	
 	@Override
 	protected void onStop() {
 		thUpdater.interrupt();
 		stopPreview();
 		super.onStop();
 	}
 	
 	public void onConfigurationChanged(Configuration newConfig) {
 	    super.onConfigurationChanged(newConfig);
 	}
 
 	//@Override
 	public void onClick(View arg0) {
 		//TODO Make the request to the waiter
 		if(cCamera != null)
 			cCamera.takePicture(null, mPictureCallback, mPictureCallback);
 		stopPreview();
 	}
 
 
 	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
 		hPreview = arg0;
 		startPreview();
 	}
 
 
 	public void surfaceCreated(SurfaceHolder arg0) {
 		hPreview = arg0;
 		startPreview();
 	}
 
 	public void surfaceDestroyed(SurfaceHolder arg0) {
 		stopPreview();
 	}
 	
 	public void startPreview()
 	{
 		if(hPreview == null || cCamera != null) return;
 		try {
 			cCamera = Camera.open();
 			cCamera.setPreviewDisplay(hPreview);
 			cCamera.setPreviewCallback(new PreviewCallback() {
 						// Called for each frame previewed
 						public void onPreviewFrame(byte[] data, Camera camera) {
 							svPreview.invalidate();
 						}
 					});
 			cCamera.setDisplayOrientation(iOrientation);
 			cCamera.startPreview();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void stopPreview()
 	{
 		if(cCamera != null)
 		{
 			cCamera.stopPreview();
 			
 			//Next two calls are needed before release() because if
 			// a callback fires just after release() has been called
 			// the activity may crash unexpectedly.
 			cCamera.setPreviewCallback(null);
 			try {
 				cCamera.setPreviewDisplay(null);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			cCamera.release();
 			cCamera = null;
 			rLL.removeViewAt(1);
 		}
 	}
 	
 	public void correctOrientation(int rotation) {
 		iOrientation = (rotation * (-90) + 90) % 360;
 	}
 	
 	Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
 		public void onPictureTaken(byte[] imageData, Camera c) {
 
 			if (imageData != null) {
 				try {
 					FileOutputStream fRequestImg = openFileOutput("awc_request_image", Context.MODE_PRIVATE);
 					fRequestImg.write(imageData);
 					fRequestImg.close();
 					FileOutputStream fRequestLoc = openFileOutput("awc_request_loc", Context.MODE_PRIVATE);
					fRequestLoc.write(imageData);
 					fRequestLoc.close();
 					
 				} catch (FileNotFoundException e) {
 					//Silently fail
 					
 				} catch (IOException e) {
 					//Silently fail
 				}
 				
 			}
 		}
 	};
 }

 /***
  * Excerpted from "Hello, Android! 2e",
  * published by The Pragmatic Bookshelf.
  * Copyrights apply to this code. It may not be used to create training material, 
  * courses, books, articles, and the like. Contact us if you are in doubt.
  * We make no guarantees that this code is fit for any purpose. 
  * Visit http://www.pragmaticprogrammer.com/titles/eband2 for more book information.
  ***/
 package org.example.hello;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.pm.ActivityInfo;
 import android.graphics.PixelFormat;
 import android.hardware.Camera;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.OrientationEventListener;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 
 public class Hello extends Activity implements SurfaceHolder.Callback,
 		SensorEventListener {
 	/** Called when the activity is first created. */
 
 	Camera camera;
 	SurfaceView surfaceView;
 	SurfaceHolder surfaceHolder;
 	boolean previewing = false;
 	LayoutInflater controlInflater = null;
 
 	// for orientation change
 	OrientationEventListener mOrientationEventListener;
 	int mDeviceOrientation;
 	public SensorManager sensorManager = null;
 	float accelerationX;
 	float accelerationY;
 
 	// ///Orientation//////
 	static final int lanscapeLeft = 2;
 	static final int lanscapeRight = 1;
 	int orientation = 2;
 
 	View viewControl;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// *******************************************
 		// full screen mode
 		// *******************************************
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		// *******************************************
 		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 
 		setContentView(R.layout.surfaceview);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 
 		getWindow().setFormat(PixelFormat.UNKNOWN);
 		surfaceView = (SurfaceView) findViewById(R.id.camerapreview);
 		surfaceHolder = surfaceView.getHolder();
 		surfaceHolder.addCallback(this);
 		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
 		controlInflater = LayoutInflater.from(getBaseContext());
 		viewControl = controlInflater.inflate(R.layout.main, null);
 		LayoutParams layoutParamsControl = new LayoutParams(
 				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
 		this.addContentView(viewControl, layoutParamsControl);
 
 		mOrientationEventListener = new OrientationEventListener(this,
 				SensorManager.SENSOR_DELAY_NORMAL) {
 			@Override
 			public void onOrientationChanged(int orientation) {
 				mDeviceOrientation = orientation;
 			}
 		};
 
 		if (mOrientationEventListener.canDetectOrientation()) {
 			mOrientationEventListener.enable();
 		}
 	}
 
 	// ///////////////////////////////////////////////////////////////////////////////////
 	// /////////////////////////ACCELEROMETER SET
 	// UP/////////////////////////////////////
 	// /////////////////////////////////////////////////////////////////////////////////
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// Register this class as a listener for the accelerometer sensor
 		sensorManager.registerListener(this,
 				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 				SensorManager.SENSOR_DELAY_GAME);
 
 	}
 
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void onSensorChanged(SensorEvent event) {
 		// TODO Auto-generated method stub
 		synchronized (this) {
 			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
 
 				// high-pass filter to eliminate gravity
 				accelerationX = event.values[0];
 				accelerationY = event.values[1];
 				String accValue = String.format("%.1f", accelerationX);
 				String accValue2 = String.format("%.1f", accelerationY);
 
 				if (mDeviceOrientation <= 300 && mDeviceOrientation >= 240) {
 					animateRootViewToLanscapeLeft(viewControl);
 				}
 				if (mDeviceOrientation >= 60 && mDeviceOrientation <= 120) {
 					animateRootViewToLanscapeRight(viewControl);
 				}
 
 				TextView numbersView = (TextView) findViewById(R.id.numbersview);
 				numbersView.setText(accValue);
 
 				TextView numbersView2 = (TextView) findViewById(R.id.numbersview2);
				numbersView.setText(accValue2);
 			}
 		}
 	}
 
 	public void animateRootViewToLanscapeLeft(View view) {
 		if (orientation == lanscapeRight) {
 			Animation spin = AnimationUtils.loadAnimation(
 					getApplicationContext(), R.anim.tolandscapeleft);
 			view.setAnimation(spin);
 			spin.setFillAfter(true);
 			spin.start();
 			orientation = lanscapeLeft;
 		}
 	}
 
 	public void animateRootViewToLanscapeRight(View view) {
 		if (orientation == lanscapeLeft) {
 			Animation spin = AnimationUtils.loadAnimation(
 					getApplicationContext(), R.anim.tolandscaperight);
 			view.setAnimation(spin);
 			spin.setFillAfter(true);
 			spin.start();
 			orientation = lanscapeRight;
 		}
 	}
 
 	// //////////////////////////////////////////////////////////////////////////////////////////////
 	// //////////////////////////////////////////////////////////////////////////////////////////////
 	// /////////////////SURFACE
 	// VIEW/////////////////////////////////////////////////////////////////
 	// //////////////////////////////////////////////////////////////////////////////////////////////
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		// TODO Auto-generated method stub
 		if (previewing) {
 			camera.stopPreview();
 			previewing = false;
 		}
 
 		if (camera != null) {
 			try {
 				camera.setPreviewDisplay(surfaceHolder);
 				camera.startPreview();
 				previewing = true;
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		// TODO Auto-generated method stub
 		camera = Camera.open();
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		// TODO Auto-generated method stub
 		camera.stopPreview();
 		camera.release();
 		camera = null;
 		previewing = false;
 	}
 
 }

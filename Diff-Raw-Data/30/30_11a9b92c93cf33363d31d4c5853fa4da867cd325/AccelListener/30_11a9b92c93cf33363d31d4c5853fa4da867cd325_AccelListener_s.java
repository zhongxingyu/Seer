 package com.phonegap.demo;
 
 import static android.hardware.SensorManager.DATA_X;
 import static android.hardware.SensorManager.DATA_Y;
 import static android.hardware.SensorManager.DATA_Z;
 import android.hardware.SensorManager;
 import android.content.Context;
 import android.hardware.SensorListener;
 import android.webkit.WebView;
 
 public class AccelListener implements SensorListener{
 
 	WebView mAppView;
 	Context mCtx;
 	private SensorManager sensorManager;
 	
 	private long lastUpdate = -1;
 	
 	AccelListener(Context ctx, WebView appView)
 	{
 		mCtx = ctx;
 		mAppView = appView;
 		sensorManager = (SensorManager) mCtx.getSystemService(Context.SENSOR_SERVICE);
 	}
 	
 	public void start()
 	{
 	    sensorManager.registerListener(this,
 	            SensorManager.SENSOR_ACCELEROMETER,
 	            SensorManager.SENSOR_DELAY_GAME);
 	    sensorManager.registerListener(this,
 	            SensorManager.SENSOR_ORIENTATION,
 	            SensorManager.SENSOR_DELAY_GAME);
 	}
 	
 	public void stop()
 	{
 		sensorManager.unregisterListener(this);
 	}
 	
 	public void onAccuracyChanged(int sensor, int accuracy) {
 		// This should call the FAIL method
 	}
 	
 	public void onSensorChanged(int sensor, float[] values) {
                 if (values.length < 3)
                         return;
 
                 if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
                         float x = values[0];
                         float y = values[1];
                         float z = values[2];
                         mAppView.loadUrl("javascript:gotAcceleration(" + x + ", " + y + "," + z + ")");
                 }
                 else if (sensor == SensorManager.SENSOR_ORIENTATION) {
                         float azimuth = values[0];
                         float pitch = values[1];
                         float roll = values[2];
                        mAppView.loadUrl("javascript:gotOrientation(" + azimuth + ", " + pitch + ", " +
 roll + ")");
                 }
         }
 }

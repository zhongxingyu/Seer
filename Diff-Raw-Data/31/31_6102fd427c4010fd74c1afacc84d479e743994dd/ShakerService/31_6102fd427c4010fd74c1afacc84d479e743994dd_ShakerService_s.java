 package com.mani.emergency;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.IBinder;
 import android.util.Log;
 
 public class ShakerService extends Service{
 	protected static final float THRESHOLD = 13;
 	SensorManager sensorManager;
 	private float mAccel; // acceleration apart from gravity
 	private float mAccelCurrent; // current acceleration including gravity
 	private float mAccelLast; // last acceleration including gravity
 	protected boolean smsSent = false;
 	SensorEventListener sensorEventListener = new SensorEventListener() {
 
 		@Override
 		public void onSensorChanged(SensorEvent event) {
 			float x = event.values[0];
 			float y = event.values[1];
 			float z = event.values[2];
 			mAccelLast = mAccelCurrent;
 			mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
 			float delta = mAccelCurrent - mAccelLast;
 			mAccel = mAccel * 0.9f + delta; // perform low-cut filter
 			if((mAccel > THRESHOLD)&&(smsSent == false)){
 				smsSent = true;
 				Intent intent = new Intent(getBaseContext(), GPSActivity.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				startActivity(intent);
 				Log.e("Tag", "Acceleration: "+mAccel);
 			}
 		}
 
 		@Override
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {
 
 		}
 	};
 
 	@Override
 	public void onCreate() {
		Log.e("Tag", "Service Created");
 		/* do this in onCreate */
 	    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 	    sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
 	    mAccel = 0.00f;
 	    mAccelCurrent = SensorManager.GRAVITY_EARTH;
 	    mAccelLast = SensorManager.GRAVITY_EARTH;
 		super.onCreate();
 	}
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("Tag", "Service Started");
 		sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
 		return super.onStartCommand(intent, flags, startId);
 	}
 	@Override
 	public void onDestroy() {
		Log.e("Tag", "Service Destroyed");
 		sensorManager.unregisterListener(sensorEventListener);
 		smsSent = false;
 		super.onDestroy();
 	}
 	@Override
 	public IBinder onBind(Intent intent) {
		Log.e("Tag", "Service Bound");
 		return null;
 	}
 }

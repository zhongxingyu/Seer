 package com.alin.jbox2dball;
 
 import android.app.Activity;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.view.Surface;
 
 public class Jbox2dBall extends Activity implements SensorEventListener{
 	private SensorManager sensorManager;
 	private Sensor accelerometer;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
         accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
     }
     
     private Jbox2dBallView getJbox2dBallView() {
     	return (Jbox2dBallView) findViewById(R.id.jbox2dBall_view);
     }
     
     @Override
     public void onPause() {
     	super.onPause();
     	sensorManager.unregisterListener(this, accelerometer);
     }
     
     @Override
     public void onResume() {
     	super.onResume();
     	sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
     }
     
     @Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		
 	}
     
     @Override
 	public void onSensorChanged(SensorEvent event) {
 		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;
 		switch (getWindowManager().getDefaultDisplay().getOrientation()) {
 			case Surface.ROTATION_0:
 				getJbox2dBallView().setGravity(-event.values[0], event.values[1]);
 				break;
 			case Surface.ROTATION_90:
 				getJbox2dBallView().setGravity(event.values[1], event.values[0]);
 				break;
 			case Surface.ROTATION_180:
 				getJbox2dBallView().setGravity(event.values[0], -event.values[1]);
 				break;
 			case Surface.ROTATION_270:
 				getJbox2dBallView().setGravity(-event.values[1], -event.values[0]);
 				break;
 		}
 	}
 }

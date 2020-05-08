 package com.tan_nai.javaversionbreakapp;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.widget.TextView;
 
 public class MainActivity extends Activity implements SensorEventListener{
 
 		public final static String TAG = "SensorTest2";
 		protected final static double RAD2DEG = 180/Math.PI;
 		
 		SensorManager sensorManager;
 		
 		float[] rotationMatrix = new float[9];
 		float[] gravity = new float[3];
 		float[] geomagnetic = new float[3];
 		float[] attitude = new float[3];
 		
 		GameSerfaceView surfaceview;
 		
		boolean firstcreate;
		
 		
 		@Override
 		public void onCreate(Bundle savedInstanceState) {
 			super.onCreate(savedInstanceState);
 			surfaceview = new GameSerfaceView(this);
 			  setContentView(surfaceview);
			  firstcreate=true;
 			initSensor();
 		}
 		
 		public void onResume(){
 			super.onResume();
 			sensorManager.registerListener(
 				this,
 				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 				SensorManager.SENSOR_DELAY_GAME);
 			sensorManager.registerListener(
 				this,
 				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
 				SensorManager.SENSOR_DELAY_GAME);
			
			if(!firstcreate){
				surfaceview.runStart();
			}else{
				firstcreate = false;
			}
 		}
 		
 		public void onPause(){
 			super.onPause();
 			sensorManager.unregisterListener(this);
			surfaceview.runStop();
 		}
 		
 		
 		protected void initSensor(){
 			sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
 		}
 
 		@Override
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
 
 		@Override
 		public void onSensorChanged(SensorEvent event) {
 			
 			switch(event.sensor.getType()){
 			case Sensor.TYPE_MAGNETIC_FIELD:
 				geomagnetic = event.values.clone();
 				break;
 			case Sensor.TYPE_ACCELEROMETER:
 				gravity = event.values.clone();
 				break;
 			}
 
 			if(geomagnetic != null && gravity != null){
 				
 				SensorManager.getRotationMatrix(
 					rotationMatrix, null, 
 					gravity, geomagnetic);
 				
 				SensorManager.getOrientation(
 					rotationMatrix, 
 					attitude);
 				/*
 				azimuthText.setText(Integer.toString(
 					(int)(attitude[0] * RAD2DEG)));
 				pitchText.setText(Integer.toString(
 					(int)(attitude[1] * RAD2DEG)));
 				rollText.setText(Integer.toString(
 					(int)(attitude[2] * RAD2DEG)));
 				*/
 				surfaceview.move((float) (attitude[1] * RAD2DEG ));
 			}
 			
 		}
 		
 		@Override
 		public void onStop() {
 			super.onStop();
 			surfaceview.runStop();
 		}
 	
 
 }

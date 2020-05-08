 package com.fruitninjaremote;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Context;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class AccelerometerDataActivity extends Activity implements SensorEventListener {
 
 	float xLinearAcceleration;
 	float yLinearAcceleration;
 	float zLinearAcceleration;
 	float[] gravity = new float[3];
 	TextView display;
 	SensorManager sm;
 	Sensor mAccelerometer;
 	long start;
 	long end;
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate( savedInstanceState);
 		start = System.currentTimeMillis();
 		end = start + 10*1000;
 		display = new TextView(this);
 		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
 		mAccelerometer = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
 		sm.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
 		setContentView(display); 
 	}
 	/*
 	public void onAccuracyChanged(Sensor arg0, int arg1) {	}
 
 	public void onSensorChanged(SensorEvent event) 
 	{
 		display.setText("idiot");
 		if (event.sensor != null && event.sensor.getType()==Sensor.TYPE_ACCELEROMETER)
 		{
 			xLinearAcceleration=event.values[0];
 			yLinearAcceleration=event.values[1];
 			zLinearAcceleration=event.values[2];
 			display.setText(xLinearAcceleration + " / " + yLinearAcceleration + " / " + zLinearAcceleration);
 		}
 	}*/
 	
 	static final float NS2S = 1.0f / 1000000000.0f;
 	float[] last_values = null;
 	float[] velocity = null;
 	float[] position = null;
 	long last_timestamp = 0;
 	float[] linear_acceleration = new float[3];
 	boolean started = true;
 	public void onSensorChanged(SensorEvent event) {
          if (started==true)
          {
 		 gravity[0] = event.values[0];
          gravity[1] = event.values[1];
          gravity[2] = event.values[2];
          started = false;
          }
          
          linear_acceleration[0] = event.values[0] - gravity[0];
          linear_acceleration[1] = event.values[1] - gravity[1];
          linear_acceleration[2] = event.values[2] - gravity[2];
 		if(last_values != null){
 	        float dt = (event.timestamp - last_timestamp) * NS2S;
 
 	        for(int index = 0; index < 3;++index){
 	            velocity[index] += (linear_acceleration[index] + last_values[index])/2 * dt;
 	            position[index] += velocity[index] * dt;
 	        }
 	    }
 	    else{
 	        last_values = new float[3];
 	        velocity = new float[3];
 	        position = new float[3];
 	        velocity[0] = velocity[1] = velocity[2] = 0f;
 	        position[0] = position[1] = position[2] = 0f;
 	    }
 	    System.arraycopy(event.values, 0, last_values, 0, 3);
 	    last_timestamp = event.timestamp;
 	  //  if (System.currentTimeMillis()<end) 
 	    try {
 			UDPSend.sendUDPMessage("1 2");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	    
 	    	display.setText(position[0] + " / " + position[1] + " / " + position[2] + "\n" + event.values[0] + " / " + event.values[1] + " / " + event.values[2] + "\n" + linear_acceleration[0] + " / " + linear_acceleration[1] + " / " + linear_acceleration[2]);
 		
 	}
 	/*public void onAccuracyChanged(int arg0, int arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onSensorChanged(int sensor, float[] values) {
 		//display.setText("idiot");
 		if (sensor==SensorManager.SENSOR_ACCELEROMETER)
 		{
 			xLinearAcceleration=values[0];
 			yLinearAcceleration=values[1];
 			zLinearAcceleration=values[2];
 			display.setText(xLinearAcceleration + " / " + yLinearAcceleration + " / " + zLinearAcceleration);
 			
 		}
 	}*/
 	/*@Override
 	protected void onResume() {
         super.onResume();
       // register this class as a listener for the orientation and accelerometer sensors
         sm.registerListener(this, SensorManager.SENSOR_ACCELEROMETER|SensorManager.SENSOR_ORIENTATION, SensorManager.SENSOR_DELAY_NORMAL);
     }
 	@Override
     protected void onStop() {
         // unregister listener
         sm.unregisterListener(this);
         super.onStop();
     }*/
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 		
 	} 
 
 }

 package com.someclock;
 
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import com.badlogic.gdx.backends.android.AndroidApplication;
 import com.someclock.java.MyFirstTriangle;
 import com.someclock.java.OrthographicCameraController;
 
 import java.util.Arrays;
 
 public class MainActivity extends AndroidApplication implements SensorEventListener {
 
     private OrthographicCameraController cameraController;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         cameraController = new OrthographicCameraController();
         initialize(cameraController, false);
         sensorManager =(SensorManager) getSystemService(SENSOR_SERVICE);
         sensorManager.registerListener(this,
                 sensorManager.getDefaultSensor(type),
                 SensorManager.SENSOR_DELAY_NORMAL);
     }
 
     private SensorManager sensorManager;
     @Override
     public void onSensorChanged(SensorEvent event) {
         float[] values = event.values;
 
         // Movement
         float x = values[0];
         float y = values[1];
         float z = values[2];
         cameraController.setChord(y, z, x);
     }
 
     @Override
     public void onAccuracyChanged(Sensor sensor, int i) {
 
     }
     int type = Sensor.TYPE_ORIENTATION;
     @Override
     protected void onResume() {
         super.onResume();
         // register this class as a listener for the orientation and
         // accelerometer sensors
         sensorManager.registerListener(this,
                 sensorManager.getDefaultSensor(type),
                 SensorManager.SENSOR_DELAY_NORMAL);
     }
 
     @Override
     protected void onPause() {
         // unregister listener
         super.onPause();
         sensorManager.unregisterListener(this);
     }
 }

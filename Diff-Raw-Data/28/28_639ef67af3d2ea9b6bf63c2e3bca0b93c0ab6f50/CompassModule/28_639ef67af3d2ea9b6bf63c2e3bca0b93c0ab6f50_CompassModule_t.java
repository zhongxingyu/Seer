 package com.RoboMobo;
 
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.util.Log;
 
 import java.util.Arrays;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Nats
  * Date: 05.08.13
  * Time: 18:34
  * To change this template use File | Settings | File Templates.
  */
 public class CompassModule implements SensorEventListener
 {
     public float[] accelData = new float[3];
     public float[] magnetData = new float[3];
     public float[] rotationMatrix = new float[16];
     public float[] orientationData = new float[3];
 
     public CompassModule()
     {
         Arrays.fill(accelData, 0);
         Arrays.fill(magnetData, 0);
         Arrays.fill(rotationMatrix, 0);
         Arrays.fill(orientationData, 0);
     }
 
     @Override
     public void onSensorChanged(SensorEvent sensorEvent)
     {
         if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
         {
             accelData = sensorEvent.values;
         }
         if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
         {
             magnetData = sensorEvent.values;
         }
         if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD || sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
         {
             SensorManager.getRotationMatrix(rotationMatrix, null, accelData, magnetData);
             SensorManager.getOrientation(rotationMatrix, orientationData);
         }
 
     }
 
     @Override
     public void onAccuracyChanged(Sensor sensor, int i)
     {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 }

 package com.Norvan.LockPick;
 
 import android.content.Context;
 import android.content.pm.PackageManager;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.SystemClock;
 import android.util.Log;
 
 import java.security.PublicKey;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ngorgi-dev
  * Date: 2/4/12
  * Time: 2:30 PM
  * To change this template use File | Settings | File Templates.
  */
 public class SensorHandler {
     Context context;
     int lastTiltReading = -1;
     float lastAngularVelocity = -1;
     SensorHandlerInterface sensorHandlerInterface;
     long lastTimestampGyro = 0;
     long lastTimestampAccel = 0;
     SensorManager sensorManager;
     boolean gyroExists;
     ArrayList<Float> angularVelocityBuffer;
     int initialSideFacingUp = 0;
     private static final int LEFT_FACING_UP = -1;
     private static final int RIGHT_FACING_UP = 1;
 
     
     private static final int refreshDelay = 10000000;
     public SensorHandler(Context context, SensorHandlerInterface sensorHandlerInterface) {
         this.context = context;
         this.sensorHandlerInterface = sensorHandlerInterface;
         sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
         PackageManager paM = context.getPackageManager();
         gyroExists = paM.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
         angularVelocityBuffer = new ArrayList<Float>();
         angularVelocityBuffer.add(0f);
         angularVelocityBuffer.add(0f);
     }
 
     public void startPolling() {
         if (gyroExists) {
             Sensor sensorOrientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
             Sensor sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
             sensorManager.registerListener(sensorEventListenerWithGyro, sensorOrientation, SensorManager.SENSOR_DELAY_GAME);
             sensorManager.registerListener(sensorEventListenerWithGyro, sensorGyroscope, SensorManager.SENSOR_DELAY_GAME);
         } else {
             Sensor sensorOrientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
             sensorManager.registerListener(sensorEventListenerNoGyro, sensorOrientation, SensorManager.SENSOR_DELAY_GAME);
         }
     }
 
     public void stopPolling() {
         try {
             sensorManager.unregisterListener(sensorEventListenerNoGyro);
         } catch (Exception e) {
 
         }
         try {
             sensorManager.unregisterListener(sensorEventListenerWithGyro);
         } catch (Exception e) {
 
         }
     }
 
     SensorEventListener sensorEventListenerWithGyro = new SensorEventListener() {
         @Override
         public void onSensorChanged(SensorEvent sensorEvent) {
             synchronized (this) {
                 long timestamp = sensorEvent.timestamp;
 
                 if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                     if (timestamp - lastTimestampAccel > refreshDelay) {
 
                         boolean isFacingDown = (Math.abs(sensorEvent.values[1]) > 90);
                         float phoneRightSideHeading = sensorEvent.values[2] + 90;
                         if (isFacingDown) {
                             phoneRightSideHeading = 360 - phoneRightSideHeading;
                         }
                         lastTiltReading = (int) ((phoneRightSideHeading * 10000) / 360);
 
                         if (initialSideFacingUp == 0) {
                             if (lastTiltReading > 3500 && lastTiltReading < 6500) {
                                 initialSideFacingUp = RIGHT_FACING_UP;
                             } else if (lastTiltReading > 8500 || lastTiltReading < 1500) {
                                 initialSideFacingUp = LEFT_FACING_UP;
                             }
                         }
                         if (initialSideFacingUp == LEFT_FACING_UP) {
                             if (lastTiltReading > 5000) {
                                 lastTiltReading = lastTiltReading - 5000;
                             }   else {
                                 lastTiltReading = lastTiltReading + 5000;
                             }
                         }
                         lastTimestampAccel = timestamp;
                     }
                 } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                     if (timestamp - lastTimestampGyro > refreshDelay) {
                         if (initialSideFacingUp != 0) {
                             lastAngularVelocity = Math.abs(sensorEvent.values[1]);
                             angularVelocityBuffer.remove(0);
                             angularVelocityBuffer.add(lastAngularVelocity);
                             lastAngularVelocity = 0;
                             for (float val : angularVelocityBuffer) {
                                 lastAngularVelocity = lastAngularVelocity + val;
                             }
                             lastAngularVelocity = lastAngularVelocity/angularVelocityBuffer.size();
                             sensorHandlerInterface.newValues(lastAngularVelocity, lastTiltReading);
                         }   else{
                             sensorHandlerInterface.notOnSide();
                         }
                         lastTimestampGyro = timestamp;
                     }
 
 
                 }
             }
         }
 
         @Override
         public void onAccuracyChanged(Sensor sensor, int i) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
     };
 
     SensorEventListener sensorEventListenerNoGyro = new SensorEventListener() {
         @Override
         public void onSensorChanged(SensorEvent sensorEvent) {
             synchronized (this) {
                 long timestamp = sensorEvent.timestamp;
 
                 if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                     if (timestamp - lastTimestampAccel > refreshDelay) {
                         int currentTiltReading;
                         boolean isFacingDown = (Math.abs(sensorEvent.values[1]) > 90);
                         float phoneRightSideHeading = sensorEvent.values[2] + 90;
                         if (isFacingDown) {
                             phoneRightSideHeading = 360 - phoneRightSideHeading;
                         }
                         currentTiltReading = (int) ((phoneRightSideHeading * 10000) / 360);
                         if (initialSideFacingUp == 0) {
                             if (currentTiltReading > 3500 && currentTiltReading < 6500) {
                                 initialSideFacingUp = RIGHT_FACING_UP;
                             } else if (currentTiltReading > 8500 || currentTiltReading < 1500) {
                                 initialSideFacingUp = LEFT_FACING_UP;
                             }
                         }
                         if (initialSideFacingUp == LEFT_FACING_UP) {
                             if (currentTiltReading > 5000) {
                                 currentTiltReading = currentTiltReading - 5000;
                             }   else {
                                 currentTiltReading = currentTiltReading + 5000;
                             }
                         }
                         int delta = Math.abs(lastTiltReading - currentTiltReading);
                         lastTiltReading = currentTiltReading;
                         lastTimestampAccel = timestamp;
                         if (initialSideFacingUp != 0) {
                         sensorHandlerInterface.newValues(delta, currentTiltReading);
                         }else{
                             sensorHandlerInterface.notOnSide();
                         }
                         
                     }
                 } 
             }
         }
 
         @Override
         public void onAccuracyChanged(Sensor sensor, int i) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
     };
 
     public interface SensorHandlerInterface {
         public void newValues(float angularVelocity, int tilt);
         public void notOnSide();
 
     }
 
 
 }

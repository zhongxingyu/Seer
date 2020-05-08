 package com.incidentlocator.client;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import com.incidentlocator.client.IncidentLocatorInterface;
 import android.util.Log;
 
 public class GetDirectionListener implements SensorEventListener {
     private static final String TAG = "IncidentLocatorDirectionListener";
     private static final float GRAVITY_THRESHOLD = SensorManager.STANDARD_GRAVITY / 2;
     private static IncidentLocatorInterface app;
 
     // variables to store sensor data from accelerometer
     // and magnitometer respectively
     private float[] gravityMatrix, geomagneticMatrix;
 
     public GetDirectionListener(IncidentLocatorInterface i) {
         app = i;
     }
 
     public void onAccuracyChanged(Sensor sensor, int accuracy) {  }
 
     public void onSensorChanged(SensorEvent event) {
         // get data from sensors, from accelerometer we get
         // the gravity readings used to determine pitch+roll
         // while the magnetometer gives us the earth's magnetic reading
         // which determines the device heading
         if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
             gravityMatrix = event.values.clone();
         }
 
         if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
             geomagneticMatrix = event.values.clone();
         }
 
         if (gravityMatrix != null && geomagneticMatrix != null) {
             float[] rotationMatrix = new float[16];
 
             // we need to check if rotation matrix was calculated correctly in
             // cases like a device in free fall, then acceleration is 0
             boolean success = SensorManager.getRotationMatrix(rotationMatrix,
                                                               null,
                                                               gravityMatrix,
                                                               geomagneticMatrix);
             if (success) {
                 // check if device is on vertical position and remap axes
                 if (gravityMatrix[2] < GRAVITY_THRESHOLD) {
                     // device is in vertical position
                     SensorManager.remapCoordinateSystem(rotationMatrix,
                                                         SensorManager.AXIS_X,
                                                         SensorManager.AXIS_Z,
                                                         rotationMatrix);
                     Log.d(TAG, "Device in vertical position");
                 } else {
                     // device in horizontal position
                     SensorManager.remapCoordinateSystem(rotationMatrix,
                                                         SensorManager.AXIS_X,
                                                         SensorManager.AXIS_Y,
                                                         rotationMatrix);
                     Log.d(TAG, "Device in horizontal position");
                 }
 
                 // calculate device orientation
                 float[] orientation = new float[3];
                 SensorManager.getOrientation(rotationMatrix, orientation);
 
                 // radians -> degrees
                 float azimuth = (float) Math.toDegrees(orientation[0]);
 
                 // normalize result to 0..360 range
                 azimuth = (azimuth + 360) % 360;
 
                 // get heading as integer
                 int heading = Math.round(azimuth);
 
                 // update device direction variable in main activity
                 app.updateDirection(heading);
             }
         }
     }
 }

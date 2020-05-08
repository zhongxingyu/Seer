 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle;
 
 
 import static java.lang.annotation.ElementType.FIELD;
 import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
 
 import android.hardware.SensorListener;
 
 import java.lang.annotation.Retention;
 import java.lang.annotation.Target;
 
 @SuppressWarnings("deprecation")
 public class CompassListener implements SensorListener {
 
     @BindingAnnotation @Target({ FIELD, PARAMETER, METHOD }) @Retention(RUNTIME)
     public @interface Azimuth {}
 
     private final Refresher mRefresher;
     private final LocationControlBuffered mLocationControlBuffered;
     private float mLastAzimuth;
 
     public float getLastAzimuth() {
         return mLastAzimuth;
     }
 
     @Inject
     public CompassListener(Refresher refresher,
             LocationControlBuffered locationControlBuffered, @Azimuth float lastAzimuth) {
         mRefresher = refresher;
         mLocationControlBuffered = locationControlBuffered;
         mLastAzimuth = lastAzimuth;
     }
 
     // public void onAccuracyChanged(Sensor sensor, int accuracy) {
     // }
 
     // public void onSensorChanged(SensorEvent event) {
     // onSensorChanged(SensorManager.SENSOR_ORIENTATION, event.values);
     // }
 
     public void onAccuracyChanged(int sensor, int accuracy) {
     }
 
     public void onSensorChanged(int sensor, float[] values) {
         final float currentAzimuth = values[0];
         if (Math.abs(currentAzimuth - mLastAzimuth) > 5) {
//            Log.d("GeoBeagle", "azimuth now " + sensor + ", " + currentAzimuth);
             mLocationControlBuffered.setAzimuth(((int)currentAzimuth / 5) * 5);
             mRefresher.refresh();
             mLastAzimuth = currentAzimuth;
         }
     }
 }

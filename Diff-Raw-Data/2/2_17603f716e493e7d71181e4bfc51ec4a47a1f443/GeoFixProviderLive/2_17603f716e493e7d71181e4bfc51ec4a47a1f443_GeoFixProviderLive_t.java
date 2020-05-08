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
 
 import android.content.SharedPreferences;
 import android.hardware.SensorListener;
 import android.hardware.SensorManager;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 
 import java.util.ArrayList;
 
 /** Responsible for providing an up-to-date location and compass direction */
 @SuppressWarnings("deprecation")
 public class GeoFixProviderLive implements LocationListener, SensorListener,
         GeoFixProvider {
     private GeoFix mLocation;
     private final LocationManager mLocationManager;
     private float mAzimuth;
     /** A refresh is sent whenever a sensor changes */
     private final ArrayList<Refresher> mObservers = new ArrayList<Refresher>();
     private final SensorManager mSensorManager;
     private boolean mUseNetwork = true;
     private final SharedPreferences mSharedPreferences;
 
     public GeoFixProviderLive(LocationManager locationManager,
             SensorManager sensorManager, SharedPreferences sharedPreferences) {
         mLocationManager = locationManager;
         mSensorManager = sensorManager;
         mSharedPreferences = sharedPreferences;
         // mLocation = getLastKnownLocation(); //work in constructor..
     }
 
     public GeoFix getLocation() {
         if (mLocation == null) {
             Location lastKnownLocation = getLastKnownLocation();
             if (lastKnownLocation != null)
                 mLocation = new GeoFix(lastKnownLocation);
             else
                 mLocation = GeoFix.NO_FIX;
         }
         return mLocation;
     }
 
     public void addObserver(Refresher refresher) {
         if (!mObservers.contains(refresher))
             mObservers.add(refresher);
     }
 
     private void notifyObservers() {
         for (Refresher refresher : mObservers) {
             refresher.refresh();
         }
     }
 
     /**
      * Choose the better of two locations: If one location is newer and more
      * accurate, choose that. (This favors the gps). Otherwise, if one location
      * is newer, less accurate, but farther away than the sum of the two
      * accuracies, choose that. (This favors the network locator if you've
      * driven a distance and haven't been able to get a gps fix yet.)
      */
     private static GeoFix choose(GeoFix oldLocation, GeoFix newLocation) {
         if (oldLocation == null)
             return newLocation;
         if (newLocation == null)
             return oldLocation;
 
         if (newLocation.getTime() > oldLocation.getTime()) {
             float distance = newLocation.distanceTo(oldLocation);
             // Log.d("GeoBeagle", "onLocationChanged distance="+distance +
             // "  provider=" + newLocation.getProvider());
             if (distance < 1) // doesn't take changing accuracy into account
                 return oldLocation;
             // TODO: Handle network and gps different
 
             return newLocation;
             /*
              * if (newLocation.getAccuracy() <= oldLocation.getAccuracy())
              * return newLocation; else if (oldLocation.distanceTo(newLocation)
              * >= oldLocation.getAccuracy() + newLocation.getAccuracy()) {
              * return newLocation; }
              */
         }
         return oldLocation;
     }
 
     @Override
     public void onLocationChanged(Location location) {
        if (location == null)
            return;
         GeoFix chosen = choose(mLocation, new GeoFix(location));
         if (chosen != mLocation) {
             mLocation = chosen;
             notifyObservers();
         }
     }
 
     @Override
     public void onProviderDisabled(String provider) {
         Log.d("GeoBeagle", "onProviderDisabled(" + provider + ")");
     }
 
     @Override
     public void onProviderEnabled(String provider) {
         Log.d("GeoBeagle", "onProviderEnabled(" + provider + ")");
     }
 
     @Override
     public void onStatusChanged(String provider, int status, Bundle extras) {
     }
 
     @Override
     public void onAccuracyChanged(int sensor, int accuracy) {
         // Log.d("GeoBeagle", "onAccuracyChanged " + sensor + " accuracy " +
         // accuracy);
     }
 
     @Override
     public void onSensorChanged(int sensor, float[] values) {
         final float currentAzimuth = values[0];
         if (Math.abs(currentAzimuth - mAzimuth) > 5) {
             // Log.d("GeoBeagle", "azimuth now " + sensor +", " +
             // currentAzimuth);
             mAzimuth = currentAzimuth;
             notifyObservers();
         }
     }
 
     public void onResume() {
         mUseNetwork = mSharedPreferences.getBoolean("use-network-location",
                 true);
 
         mSensorManager.registerListener(this, SensorManager.SENSOR_ORIENTATION,
                 SensorManager.SENSOR_DELAY_UI);
         long minTime = 1000; // ms
         float minDistance = 1; // meters
         mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                 minTime, minDistance, this);
         if (mUseNetwork)
             mLocationManager.requestLocationUpdates(
                     LocationManager.NETWORK_PROVIDER, minTime, minDistance,
                     this);
 
         onLocationChanged(getLastKnownLocation());
     }
 
     public void onPause() {
         mSensorManager.unregisterListener(this);
         mLocationManager.removeUpdates(this);
     }
 
     public boolean isProviderEnabled() {
         return mLocationManager.isProviderEnabled("gps")
                 || (mUseNetwork && mLocationManager
                         .isProviderEnabled("network"));
     }
 
     private Location getLastKnownLocation() {
         Location gpsLocation = mLocationManager
                 .getLastKnownLocation(LocationManager.GPS_PROVIDER);
         if (gpsLocation != null)
             return gpsLocation;
         if (mUseNetwork)
             return mLocationManager
                     .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
         return null;
     }
 
     public float getAzimuth() {
         return mAzimuth;
     }
 }

 package com.weatherornot.weatherornot;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 
 
 //  This class wraps all the code we will use to manage getting updates on the location
 
 public class UserLocationManager implements LocationListener {
 
     public GetWeatherDataTask getWeatherDataTask;
     LocationManager lm;
 
 
     ////6
     public UserLocationManager(GetWeatherDataTask x) {
         super();
 
 ////7
         getWeatherDataTask = x;   // use getWeatherDataTask when onLocationChanged is called
         lm = (LocationManager) getWeatherDataTask.pantsWeatherDisplay.getSystemService(Context.LOCATION_SERVICE);
 
         try {
             lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,this);
 
         } catch (Exception e) {
             e.printStackTrace();
             Log.e("Look----------------------------------------------------------------------", e.getMessage(), e);
         }
     }
 
 
     ////8
     @Override
     public void onLocationChanged(Location location) {
 
 
         lm.removeUpdates(this);
 
         getWeatherDataTask.receiveUserLocation(location);
 
 //13
         Log.e("LOOK-----------------------------Get weather data task", getWeatherDataTask.toString());
 
 
     }
 
     @Override
     public void onStatusChanged(String s, int i, Bundle bundle) {
 
     }
 
     @Override
     public void onProviderEnabled(String s) {
 
     }
 
     @Override
     public void onProviderDisabled(String s) {
 
     }
 
 }

 package com.feedme.service;
 
 import android.content.Context;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.widget.Toast;
 
 import java.security.Provider;
 
 /**
  * User: dayelostraco
  * Date: 2/12/12
  * Time: 6:39 PM
  *
  * This Service implements the Android LocationListener while also providing the LocationManager. This is a singleton
  * class so this service needs to only be instantiated once in the application and will then on be available for every
  * class.
  *
  * NOTE: This is pretty hacky so it may cause some undesirable effects. Time will tell...
  */
 public class FeedMeLocationService implements LocationListener {
 
     private static FeedMeLocationService INSTANCE;
 
     private Context context;
     private LocationManager locationManager;
     private String provider;
     private Criteria criteria;
     
     private Double latitude;
     private Double longitude;
 
     /**
      * Defeats Instantiation
      * 
      * @param context
      * @param locationManager
      */
     protected FeedMeLocationService(Context context, LocationManager locationManager) {
 
         this.context = context;
         this.locationManager = locationManager;
 
         criteria = new Criteria();
         criteria.setAccuracy(Criteria.ACCURACY_FINE);
         criteria.setAltitudeRequired(false);
         criteria.setBearingRequired(false);
         criteria.setCostAllowed(true);
         criteria.setPowerRequirement(Criteria.POWER_LOW);
 
         provider = locationManager.getBestProvider(criteria, true);

        if(provider!=null){
            locationManager.requestLocationUpdates(provider, 0, 0, this);
        }
     }
 
     public static FeedMeLocationService getInstance(Context context, LocationManager locationManager) {
         if(INSTANCE == null) {
             INSTANCE = new FeedMeLocationService(context, locationManager);
         }
 
         return INSTANCE;
     }
 
     @Override
     public void onLocationChanged(Location loc) {
         latitude = loc.getLatitude();
         longitude = loc.getLongitude();
 
         provider = locationManager.getBestProvider(criteria, true);
         locationManager.requestLocationUpdates(provider, 0, 0, this);
     }
 
     @Override
     public void onProviderDisabled(String provider) {
         Toast.makeText(context, "Gps Disabled", Toast.LENGTH_SHORT).show();
     }
 
     @Override
     public void onProviderEnabled(String provider) {
         Toast.makeText(context, "Gps Enabled", Toast.LENGTH_SHORT).show();
     }
 
     @Override
     public void onStatusChanged(String provider, int status, Bundle extras) {
         Toast.makeText(context, "GPS Status Changed", Toast.LENGTH_SHORT).show();
     }
 
     public Context getContext() {
         return context;
     }
 
     public void setContext(Context context) {
         this.context = context;
     }
 
     public LocationManager getLocationManager() {
         return locationManager;
     }
 
     public void setLocationManager(LocationManager locationManager) {
         this.locationManager = locationManager;
     }
 
     public Double getLatitude() {
         if(latitude==null){
             return 0.0;
         }
         return latitude;
     }
 
     public void setLatitude(Double latitude) {
         this.latitude = latitude;
     }
 
     public Double getLongitude() {
         if(longitude==null){
             return 0.0;
         }
         return longitude;
     }
 
     public void setLongitude(Double longitude) {
         this.longitude = longitude;
     }
 }

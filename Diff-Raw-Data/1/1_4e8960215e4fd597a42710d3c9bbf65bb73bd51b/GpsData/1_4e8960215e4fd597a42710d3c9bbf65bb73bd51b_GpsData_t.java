 package edu.incense.android.datatask.data;
 
 import android.location.Location;
 import android.os.Bundle;
 
 public class GpsData extends Data {
     // altitude longitude
     private double altitude;
     private double latitude;
     private double longitude;
     private float accuracy;
     private int satellites; // the number of satellites used to derive the
                                // fix
     private double speed;
     private String provider;
 
     public GpsData(Location location) {
         super(DataType.GPS);
         latitude = location.getLatitude();
         longitude = location.getLongitude();
         altitude = location.getAltitude();
         accuracy = location.getAccuracy();
         setProvider(location.getProvider());
         Bundle bundle = location.getExtras();
         if (bundle != null)
             satellites = bundle.getInt("satellites");
        //TODO Convert UTC time from location.getTime() to Pacific Time or whatever the device is using.
         setTimestamp(location.getTime());
     }
 
     public void setAltitude(double altitude) {
         this.altitude = altitude;
     }
 
     public void setLatitude(double latitude) {
         this.latitude = latitude;
     }
 
     public void setLongitude(double longitude) {
         this.longitude = longitude;
     }
 
     public void setAccuracy(float accuracy) {
         this.accuracy = accuracy;
     }
 
     public void setSpeed(double speed) {
         this.speed = speed;
     }
 
     public void setProvider(String provider) {
         this.provider = provider;
     }
 
     public double getAltitude() {
         return altitude;
     }
 
     public double getLatitude() {
         return latitude;
     }
 
     public double getLongitude() {
         return longitude;
     }
 
     public float getAccuracy() {
         return accuracy;
     }
 
     public int getSatellites() {
         return satellites;
     }
 
     public double getSpeed() {
         return speed;
     }
 
     public String getProvider() {
         return provider;
     }
 
 }

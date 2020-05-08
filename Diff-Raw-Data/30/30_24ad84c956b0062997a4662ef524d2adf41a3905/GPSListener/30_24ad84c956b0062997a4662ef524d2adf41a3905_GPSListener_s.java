 package makeit.phonemonitoring.listener;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.os.Bundle;
 
 public class GPSListener implements LocationListener {
 
     private Double latitude;
     private Double longitude;
 
     @Override
     public void onLocationChanged(Location location) {
         if (location != null) {
             System.out.println("new location : " + location);
 
             this.latitude = location.getLatitude();
             this.longitude = location.getLongitude();
         }
 
     }
 
     @Override
     public void onProviderDisabled(String provider) {
         //
     }
 
     @Override
     public void onProviderEnabled(String provider) {
         //
     }
 
     @Override
     public void onStatusChanged(String provider, int status, Bundle extras) {
         //
     }
 
     public Double getLatitude() {
         return latitude;
     }
 
     public Double getLongitude() {
         return longitude;
     }
 
 }

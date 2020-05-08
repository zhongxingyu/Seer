 package com.idamobile.map.google.v2;
 
 import android.location.Location;
 import android.os.Handler;
 import com.google.android.gms.maps.GoogleMap;
 import com.idamobile.map.AbstractMyLocationOverlay;
 import com.idamobile.map.IGeoPoint;
 import com.idamobile.map.OverlayBase;
 
 class MyLocationOverlayAdapter extends AbstractMyLocationOverlay implements OverlayAdapter {
 
     private GoogleMap googleMap;
 
     private MarkerList dummyList;
 
     private boolean updating = true;
     private Handler handler = new Handler();
     private Location lastLocation;
     private Runnable checkLocationUpdateTask = new Runnable() {
         @Override
         public void run() {
             if (updating) {
                 Location curLocation = googleMap.getMyLocation();
                 if (curLocation != null) {
                     if (lastLocation == null || !curLocation.equals(lastLocation)) {
                         lastLocation = curLocation;
                         notifyMyLocationListeners(getMyLocation());
                     }
                 }
                 startLocationCheck();
             }
         }
     };
 
     public MyLocationOverlayAdapter(GoogleMap googleMap) {
         this.googleMap = googleMap;
         this.dummyList = new MarkerList(googleMap);
     }
 
     private void startLocationCheck() {
         updating = true;
         handler.postDelayed(checkLocationUpdateTask, 1000);
     }
 
     private void cancelLocationCheck() {
         updating = false;
         handler.removeCallbacks(checkLocationUpdateTask);
     }
 
     @Override
     public MarkerList getResultOverlay() {
         return dummyList;
     }
 
     @Override
     public void enableMyLocation() {
         startLocationCheck();
         googleMap.setMyLocationEnabled(true);
     }
 
     @Override
     public void disableMyLocation() {
         cancelLocationCheck();
         googleMap.setMyLocationEnabled(false);
     }
 
     @Override
     public boolean isMyLocationEnabled() {
         return googleMap.isMyLocationEnabled();
     }
 
     @Override
     public IGeoPoint getMyLocation() {
        return (isMyLocationEnabled() && googleMap.getMyLocation() != null)
                 ? new UniversalGeoPoint(googleMap.getMyLocation())
                 : null;
     }
 
     @Override
     public OverlayBase getBaseOverlay() {
         return this;
     }
 
     @Override
     public void release() {
         disableMyLocation();
     }
 
 }

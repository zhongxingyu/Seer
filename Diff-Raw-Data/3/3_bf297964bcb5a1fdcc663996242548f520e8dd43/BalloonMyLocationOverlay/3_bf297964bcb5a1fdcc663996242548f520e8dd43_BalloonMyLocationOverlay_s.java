 /*
  * Copyright (c) 2010 BlipIt Committers
  * All rights reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  *
  * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
  * explanation of the license and how it is applied.
  */
 
 package com.thoughtworks.blipit.overlays;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationManager;
 import android.view.View;
 import android.view.ViewGroup;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.thoughtworks.blipit.R;
 import com.thoughtworks.blipit.activities.BlipItActivity;
 import com.thoughtworks.blipit.utils.BlipItUtils;
 import com.thoughtworks.blipit.views.BalloonOverlayView;
 
 public class BalloonMyLocationOverlay extends MyLocationOverlay {
     private BalloonOverlayView balloonView;
     private MapView mapView;
     private BlipItActivity blipItActivity;
     private MapController mapController;
     private Runnable firstFixRunnable;
 
     public BalloonMyLocationOverlay(BlipItActivity blipItActivity, MapView mapView, int minTimeForLocUpdates) {
         super(blipItActivity, mapView);
         this.mapView = mapView;
         this.blipItActivity = blipItActivity;
         this.mapController = mapView.getController();
         enableCompass();
         enableMyLocation(minTimeForLocUpdates);
         firstFixRunnable = new Runnable() {
             public void run() {
                 mapController.animateTo(getMyLocation());
             }
         };
         runOnFirstFix(firstFixRunnable);
     }
 
     private void enableMyLocation(int minTimeForLocUpdates) {
         LocationManager locationManager = (LocationManager) blipItActivity.getSystemService(Context.LOCATION_SERVICE);
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeForLocUpdates, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTimeForLocUpdates, 0, this);
     }
 
     @Override
     protected boolean dispatchTap() {
         GeoPoint geoPoint = getMyLocation();
         boolean isRecycled;
         if (balloonView == null) {
             balloonView = new BalloonOverlayView(mapView.getContext(), 0);
             isRecycled = false;
         } else {
             isRecycled = true;
         }
         balloonView.setVisibility(View.GONE);
         hideBalloon();
         balloonView.setData(getTitle(), getSnippet());
         MapView.LayoutParams params = new MapView.LayoutParams(
                 ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, geoPoint,
                 MapView.LayoutParams.BOTTOM_CENTER);
         params.mode = MapView.LayoutParams.MODE_MAP;
 
         balloonView.setVisibility(View.VISIBLE);
 
         if (isRecycled) {
             balloonView.setLayoutParams(params);
         } else {
             mapView.addView(balloonView, params);
         }
 
         mapController.animateTo(geoPoint);
 
         return true;
 
     }
 
     private String getSnippet() {
         return blipItActivity.getString(R.string.blip_snippet);
     }
 
     private String getTitle() {
         return blipItActivity.getString(R.string.blip_title);
     }
 
     /**
      * Sets the visibility of this overlay's balloon view to GONE.
      */
     private void hideBalloon() {
         if (balloonView != null) {
             balloonView.setVisibility(View.GONE);
         }
     }
 
     @Override
     public void onLocationChanged(Location location) {
         super.onLocationChanged(location);
         blipItActivity.sendUserLocationUpdate(BlipItUtils.asGeoPoint(location));
         firstFixRunnable.run();
     }
 }

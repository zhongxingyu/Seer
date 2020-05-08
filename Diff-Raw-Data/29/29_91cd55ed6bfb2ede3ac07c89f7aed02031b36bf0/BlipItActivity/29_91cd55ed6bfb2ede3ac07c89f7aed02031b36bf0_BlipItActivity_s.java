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
 
 package com.thoughtworks.blipit.activities;
 
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
import android.widget.Toast;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 import com.thoughtworks.blipit.R;
 import com.thoughtworks.blipit.overlays.BalloonMyLocationOverlay;
 import com.thoughtworks.blipit.overlays.BlipOverlay;
 import com.thoughtworks.blipit.services.BlipNotificationService;
 import com.thoughtworks.blipit.utils.BlipItServiceHelper;
 import com.thoughtworks.blipit.utils.BlipItUtils;
 import com.thoughtworks.contract.Blip;
 import com.thoughtworks.contract.BlipItSubscribeResource;
 
 import java.util.List;
 
 public class BlipItActivity extends MapActivity {
     private MapView mapView;
     private BlipNotificationClientHandler blipNotificationClientHandler;
     private Messenger blipItNotificationService;
     private Messenger blipNotificationHandler;
     private BlipItSubscribeResource blipItResource;
     private BalloonMyLocationOverlay userLocationOverlay;
     private BlipOverlay blipOverlay;
     private static final int SETTINGS_ID = Menu.FIRST;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         initBlipNotifications();
         initMapsAndOverlays();
     }
 
     private void initBlipNotifications() {
         blipNotificationClientHandler = new BlipNotificationClientHandler(this);
         bindService(new Intent(this, BlipNotificationService.class), blipNotificationClientHandler, BIND_AUTO_CREATE);
         blipNotificationHandler = new Messenger(blipNotificationClientHandler);
         blipItResource = new BlipItServiceHelper().getBlipItResource();
     }
 
     public void setBlipItNotificationService(Messenger blipItNotificationService) {
         this.blipItNotificationService = blipItNotificationService;
     }
 
     private void initMapsAndOverlays() {
         initMapView();
         initUserLocationOverlay();
         initBlipOverlay();
     }
 
     private void initBlipOverlay() {
         Drawable drawable = this.getResources().getDrawable(R.drawable.marker);
         blipOverlay = new BlipOverlay(drawable, mapView);
         blipOverlay.addBlip(new OverlayItem(new GeoPoint(0, 0), "title", "message"));
         mapView.getOverlays().add(blipOverlay);
     }
 
     private void initUserLocationOverlay() {
         userLocationOverlay = new BalloonMyLocationOverlay(this, mapView);
         mapView.getOverlays().add(userLocationOverlay);
     }
 
     private void initMapView() {
         mapView = (MapView) this.findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         userLocationOverlay.enableMyLocation();
         userLocationOverlay.enableCompass();
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         userLocationOverlay.disableMyLocation();
         userLocationOverlay.disableCompass();
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         unbindService(blipNotificationClientHandler);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         menu.add(0, SETTINGS_ID, 0, R.string.menu_settings);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == SETTINGS_ID) {
             startActivity(new Intent(this, BlipItPrefActivity.class));
         }
         return true;
     }
 
     public Messenger getBlipNotificationHandler() {
         return blipNotificationHandler;
     }
 
     public void updateBlips(Bundle data) {
         blipOverlay.addBlips((List<Blip>) data.get(BlipItUtils.BLIPS));
        Toast.makeText(this, R.string.blip_notification_received, Toast.LENGTH_SHORT).show();
     }
 
     public void sendUserLocationUpdate(GeoPoint geoPoint) {
         try {
             if (blipItNotificationService != null)
                 blipItNotificationService.send(BlipItUtils.getMessageWithGeoPoint(geoPoint, BlipItUtils.MSG_USER_LOCATION_UPDATED));
         } catch (RemoteException e) {
            Log.e(BlipItUtils.APP_TAG, "An error occured while updating user location", e);
         }
     }
 }

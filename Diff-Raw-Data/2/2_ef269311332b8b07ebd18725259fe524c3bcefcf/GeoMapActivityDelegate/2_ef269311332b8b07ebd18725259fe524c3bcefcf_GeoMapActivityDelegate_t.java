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
 
 package com.google.code.geobeagle.activity.map;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.activity.MenuAction;
 import com.google.code.geobeagle.activity.MenuActions;
 import com.google.code.geobeagle.activity.cachelist.CacheList;
 import com.google.code.geobeagle.activity.main.GeoUtils;
 import com.google.code.geobeagle.database.GeocachesSql;
 import com.google.code.geobeagle.database.WhereFactoryNearestCaches;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import java.util.List;
 
 public class GeoMapActivityDelegate {
     static class MenuActionCacheList implements MenuAction {
         private final Activity mActivity;
 
         MenuActionCacheList(Activity activity) {
             mActivity = activity;
         }
 
         @Override
         public void act() {
             mActivity.startActivity(new Intent(mActivity, CacheList.class));
         }
     }
 
     public static class MenuActionToggleSatellite implements MenuAction {
         private final MapView mMapView;
 
         public MenuActionToggleSatellite(MapView mapView) {
             mMapView = mapView;
         }
 
         @Override
         public void act() {
             mMapView.setSatellite(!mMapView.isSatellite());
         }
     }
 
     private final Context mContext;
     private final MapView mMapView;
     private final MenuActions mMenuActions;
     private final MyLocationOverlay mMyLocationOverlay;
     private static boolean fZoomed = false;
 
     public GeoMapActivityDelegate(Activity parent, MapView mapView, Context context,
             MyLocationOverlay myLocationOverlay, MenuActions menuActions) {
         mContext = context;
         mMapView = mapView;
         mMyLocationOverlay = myLocationOverlay;
         mMenuActions = menuActions;
     }
 
     public void initialize(Intent intent, GeocachesSql geocachesSql,
             WhereFactoryNearestCaches whereFactory, MapItemizedOverlay cachesOverlay,
             MapController mapController, List<Overlay> mapOverlays) {
         mMapView.setBuiltInZoomControls(true);
         // mMapView.setOnLongClickListener()
         mMapView.setSatellite(false);
         double latitude = intent.getFloatExtra("latitude", 0);
         double longitude = intent.getFloatExtra("longitude", 0);
         GeoPoint center = new GeoPoint((int)(latitude * GeoUtils.MILLION),
                 (int)(longitude * GeoUtils.MILLION));
 
         mapController.setCenter(center);
         if (!fZoomed) {
             mapController.setZoom(14);
             fZoomed = true;
         }
 
         mapOverlays.add(cachesOverlay);
         mapOverlays.add(mMyLocationOverlay);
 
         cachesOverlay.addCaches(mContext, latitude, longitude, geocachesSql, whereFactory);
     }
 
     public boolean onMenuOpened(int featureId, Menu menu) {
         menu.findItem(R.id.menu_toggle_satellite).setTitle(
                 mMapView.isSatellite() ? R.string.map_view : R.string.satellite_view);
         return true;
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
         return mMenuActions.act(item.getItemId());
     }
 
     public void onPause() {
         mMyLocationOverlay.disableMyLocation();
         mMyLocationOverlay.disableCompass();
     }
 
     public void onResume() {
         mMyLocationOverlay.enableMyLocation();
         mMyLocationOverlay.enableCompass();
     }
 }

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
 
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.actions.MenuAction;
 import com.google.code.geobeagle.actions.MenuActions;
 
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class GeoMapActivityDelegate {
     
     public static class MenuActionCenterLocation implements MenuAction {
         private final MapView mMapView;
         private final MyLocationOverlay mMyLocationOverlay;
 
         public MenuActionCenterLocation(MapView mapView,
                 MyLocationOverlay myLocationOverlay) {
             mMapView = mapView;
             mMyLocationOverlay = myLocationOverlay;
         }
 
         @Override
         public void act() {
             mMapView.getController().animateTo(mMyLocationOverlay.getMyLocation());
         }
         
         @Override
         public String getLabel() {
             return mMapView.getResources().getString(R.string.menu_center_location);
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
         @Override
         public String getLabel() {
            int stringId = mMapView.isSatellite() ? R.string.map_view : R.string.menu_toggle_satellite;
            return mMapView.getResources().getString(stringId);
         }
     }
 
     private final MenuActions mMenuActions;
 
    public GeoMapActivityDelegate(MenuActions menuActions) {
         mMenuActions = menuActions;
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         return mMenuActions.onCreateOptionsMenu(menu);
     }
 
     public boolean onMenuOpened(Menu menu) {
        return mMenuActions.onMenuOpened(menu);
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
         return mMenuActions.act(item.getItemId());
     }
 
 }

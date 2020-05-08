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
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.Overlay;
 import com.google.code.geobeagle.CacheFilter;
 import com.google.code.geobeagle.Geocache;
 import com.google.code.geobeagle.GeocacheFactory;
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.actions.MenuActionCacheList;
 import com.google.code.geobeagle.actions.MenuActionChooseFilter;
 import com.google.code.geobeagle.actions.MenuActions;
 import com.google.code.geobeagle.activity.main.GeoUtils;
 import com.google.code.geobeagle.activity.map.DensityMatrix.DensityPatch;
 import com.google.code.geobeagle.database.CachesProviderArea;
 import com.google.code.geobeagle.database.CachesProviderLazyArea;
 import com.google.code.geobeagle.database.DbFrontend;
 import com.google.code.geobeagle.xmlimport.GpxImporterDI.Toaster;
 
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class GeoMapActivity extends MapActivity {
 
     private static class NullOverlay extends Overlay {
     }
 
     private static final int DEFAULT_ZOOM_LEVEL = 14;
 
     private static boolean fZoomed = false;
     private DbFrontend mDbFrontend;
     private GeoMapActivityDelegate mGeoMapActivityDelegate;
     private GeoMapView mMapView;
     private MyLocationOverlay mMyLocationOverlay;
     private OverlayManager mOverlayManager;
 
     @Override
     protected boolean isRouteDisplayed() {
         // This application doesn't use routes
         return false;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.map);
 
         // Set member variables first, in case anyone after this needs them.
         mMapView = (GeoMapView)findViewById(R.id.mapview);
         mDbFrontend = new DbFrontend(this, new GeocacheFactory());
         mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
 
         mMapView.setBuiltInZoomControls(true);
         mMapView.setSatellite(false);
 
         final Resources resources = getResources();
         final Drawable defaultMarker = resources.getDrawable(R.drawable.pin_default);
         final CacheItemFactory cacheItemFactory = new CacheItemFactory(resources);
 
         final CacheFilter cacheFilter = new CacheFilter(this);
         
         final List<Overlay> mapOverlays = mMapView.getOverlays();
         //menuActions.add(new MenuActionChooseFilter(this));
 
         final Intent intent = getIntent();
         final MapController mapController = mMapView.getController();
         final double latitude = intent.getFloatExtra("latitude", 0);
         final double longitude = intent.getFloatExtra("longitude", 0);
         final Overlay nullOverlay = new GeoMapActivity.NullOverlay();
         final GeoPoint nullGeoPoint = new GeoPoint(0, 0);
         String geocacheId = intent.getStringExtra("geocacheId");
         if (geocacheId != null) {
             Geocache selected = mDbFrontend.loadCacheFromId(geocacheId);
             cacheItemFactory.setSelectedGeocache(selected);
         }
 
         mapOverlays.add(nullOverlay);
         mapOverlays.add(mMyLocationOverlay);
 
         final List<DensityPatch> densityPatches = new ArrayList<DensityPatch>();
         final Toaster toaster = new Toaster(this, R.string.too_many_caches, Toast.LENGTH_SHORT);
         CachesProviderArea cachesProviderArea = new CachesProviderArea(mDbFrontend, cacheFilter);
         final CachesProviderLazyArea lazyArea = new CachesProviderLazyArea(cachesProviderArea, toaster, 1.0);
         final DensityOverlayDelegate densityOverlayDelegate = DensityOverlay.createDelegate(
                 densityPatches, nullGeoPoint, lazyArea);
         final DensityOverlay densityOverlay = new DensityOverlay(densityOverlayDelegate);
         
         final ArrayList<Geocache> geocacheList = new ArrayList<Geocache>();
         final CachePinsOverlay cachePinsOverlay = new CachePinsOverlay(cacheItemFactory, this,
                 defaultMarker, geocacheList);
         //Pin overlay and Density overlay can't share providers because the provider wouldn't report hasChanged() when switching between them
         CachesProviderArea cachesProviderAreaPins = new CachesProviderArea(mDbFrontend, cacheFilter);
         final CachesProviderLazyArea lazyAreaPins = new CachesProviderLazyArea(cachesProviderAreaPins, toaster, 1.0);
         final CachePinsOverlayFactory cachePinsOverlayFactory = new CachePinsOverlayFactory(
                 mMapView, this, defaultMarker, cacheItemFactory, cachePinsOverlay, lazyAreaPins);
         final GeoPoint center = new GeoPoint((int)(latitude * GeoUtils.MILLION),
                 (int)(longitude * GeoUtils.MILLION));
         mapController.setCenter(center);
         mOverlayManager = new OverlayManager(mMapView, mapOverlays,
                 densityOverlay, cachePinsOverlayFactory, false, cachesProviderArea);
         mMapView.setScrollListener(mOverlayManager);
 
         final MenuActions menuActions = new MenuActions();
         menuActions.add(new GeoMapActivityDelegate.MenuActionToggleSatellite(mMapView));
         menuActions.add(new GeoMapActivityDelegate.MenuActionCenterLocation(mMapView, mMyLocationOverlay));
         menuActions.add(new MenuActionCacheList(this));
         menuActions.add(new MenuActionChooseFilter(this, cacheFilter, cachesProviderArea, mOverlayManager));
         
        mGeoMapActivityDelegate = new GeoMapActivityDelegate(menuActions);
 
         if (!fZoomed) {
             mapController.setZoom(DEFAULT_ZOOM_LEVEL);
             fZoomed = true;
         }
 
         mOverlayManager.selectOverlay();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         return mGeoMapActivityDelegate.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onMenuOpened(int featureId, Menu menu) {
         return mGeoMapActivityDelegate.onMenuOpened(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         return mGeoMapActivityDelegate.onOptionsItemSelected(item);
     }
 
     @Override
     public void onPause() {
         mMyLocationOverlay.disableMyLocation();
         mMyLocationOverlay.disableCompass();
         mDbFrontend.closeDatabase();
         super.onPause();
     }
 
     @Override
     public void onResume() {
         super.onResume();
         mOverlayManager.forceRefresh();  //The cache filter might have changed
         mMyLocationOverlay.enableMyLocation();
         mMyLocationOverlay.enableCompass();
         mDbFrontend.openDatabase();
     }
 }

 package eu.ttbox.osm.ui.map;
 
 import android.app.ActivityManager;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.widget.Toast;
 
 import org.osmdroid.DefaultResourceProxyImpl;
 import org.osmdroid.ResourceProxy;
 import org.osmdroid.api.IGeoPoint;
 import org.osmdroid.api.IMapController;
 import org.osmdroid.tileprovider.tilesource.ITileSource;
 import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
 import org.osmdroid.util.BoundingBoxE6;
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.MapView;
 import org.osmdroid.views.overlay.MinimapOverlay;
 import org.osmdroid.views.overlay.Overlay;
 import org.osmdroid.views.overlay.ScaleBarOverlay;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import eu.ttbox.osm.core.GeoLocHelper;
 import eu.ttbox.osm.core.LocationUtils;
 import eu.ttbox.osm.ui.map.mylocation.MyLocationOverlay2;
 
 
 public abstract class OsmMapFragment extends Fragment {
 
     private static final String TAG = "OsmMapFragment";
     
     // Map
     public IMapController mapController;
     public MapView mapView;
     public ResourceProxy mResourceProxy;
 
     // Overlay
     public MyLocationOverlay2 myLocation = null;
     public MinimapOverlay miniMapOverlay = null;
     public ScaleBarOverlay mScaleBarOverlay= null;
 
 
     // ===========================================================
     // Message Handler
     // ===========================================================
     public static final int UI_MAPMSG_TOAST = 1;
     public static final int UI_MAPMSG_TOAST_ERROR = 2;
 
     public static final int UI_MAPMSG_ANIMATE_TO_GEOPOINT = 10;
     public static final int UI_MAPMSG_MAP_ZOOM_MAX = 11;
 
     private boolean isThreadRunnning() {
         return mapView!=null;
     }
 
     private Handler uiMapHandler = new Handler() {
         public void handleMessage(Message msg) {
             super.handleMessage(msg);
             if (isThreadRunnning()) {
                 switch (msg.what) {
                     case UI_MAPMSG_ANIMATE_TO_GEOPOINT: {
                         GeoPoint geoPoint = (GeoPoint) msg.obj;
                         if (geoPoint != null) {
                             if (myLocation != null) {
                                 myLocation.disableFollowLocation();
                             }
                             Log.d(TAG, "uiHandler center to GeoPoint : " + geoPoint);
                             mapController.setCenter(geoPoint);
                             int zoom = (mapView!=null && mapView.getTileProvider() !=null )  ? mapView.getTileProvider().getMaximumZoomLevel() : -1;
                             if (zoom>0) {
                                 Log.d(TAG, "uiHandler Set GeoPoint Zoom Level : " + zoom);
                                mapController.setZoom(zoom);
                             }
                         }
                     }
                     break;
                     case UI_MAPMSG_MAP_ZOOM_MAX: {
                         Integer msgObj = msg.obj!=null ? (Integer) msg.obj : mapView.getTileProvider().getMaximumZoomLevel();
                         int maxZoom =  msgObj.intValue();
                         Log.d(TAG, "uiHandler Set Zoom Level : " + maxZoom);
                         mapController.setZoom(maxZoom);
                     }
                     break;
                     case UI_MAPMSG_TOAST: {
                         String msgToast = (String) msg.obj;
                         Toast.makeText(getActivity(), msgToast, Toast.LENGTH_SHORT).show();
                     }
                     break;
                     case UI_MAPMSG_TOAST_ERROR: {
                         String msgToastError = (String) msg.obj;
                         Toast.makeText(getActivity(), msgToastError, Toast.LENGTH_SHORT).show();
                     }
                     break;
                     default : {
                         Log.w(TAG, "Not Handle UI Map Message : " + msg.what);
                     }
                 }
             }
        }
     };
 
 
     // ===========================================================
     // Constructor
     // ===========================================================
 
 
 
     public void initMap() {
         ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
         // Map Controler
         this.mResourceProxy = new DefaultResourceProxyImpl(getActivity().getApplicationContext());
         ITileSource tileSource = getPreferenceMapViewTile();
         this.mapView = MapViewFactory.createOsmMapView(getActivity().getApplicationContext(), mResourceProxy, tileSource, activityManager);
         this.mapController = mapView.getController();
 
 
     }
 
     public abstract ITileSource getPreferenceMapViewTile();
 
 
     // ===========================================================
     // Life Cycle
     // ===========================================================
 
     @Override
     public void onResume() {
         Log.i(TAG, "### ### ### ### ### onResume call ### ### ### ### ###");
         super.onResume();
         // read preference
       //  ITileSource tileSource = getPreferenceMapViewTile();
       //  mapView.setTileSource(tileSource);
         if (mapView != null) {
             //  mapView.onResume();
         }
         // Overlay MyLocation
         if (myLocation != null) {
             myLocation.onResume();
         }
     }
 
 
 
 
 
     @Override
     public void onPause() {
         Log.i(TAG, "### ### ### ### ### onPause call ### ### ### ### ###");
 
         // Overlay May Location
         if (myLocation != null) {
             myLocation.onPause();
         }
         if (mapView!=null) {
            // mapView.onPause();
         }
 
         super.onPause();
     }
 
 
     public ITileSource getPreferenceMapViewTileSource(SharedPreferences privateSharedPreferences) {
         final String tileSourceName = privateSharedPreferences.getString(MapConstants.PREFS_TILE_SOURCE, TileSourceFactory.DEFAULT_TILE_SOURCE.name());
         ITileSource tileSource = null;
         try {
             tileSource = TileSourceFactory.getTileSource(tileSourceName);
         } catch (final IllegalArgumentException ignore) {
         }
         if (tileSource==null) {
             tileSource = TileSourceFactory.DEFAULT_TILE_SOURCE;
         }
         return tileSource;
     }
 
 
 
 
     @Override
     public void onDestroy() {
         Log.i(TAG, "### ### ### ### ### onDestroy call ### ### ### ### ###");
         if (myLocation!=null) {
             myLocation.disableCompass();
             myLocation.disableMyLocation();
         }
         if (mapView!=null) {
 //            mapView.onDestroy();
         }
         super.onDestroy();
     }
     // ===========================================================
     // Save
     // ===========================================================
 
     @Override
     public void onSaveInstanceState(android.os.Bundle outState) {
         Log.d(TAG, "--- ---------------------------- ---");
         Log.d(TAG, "--- on Save Instance State       ---");
         Log.d(TAG, "--- ---------------------------- ---");
         if (mapView != null) {
             //  mapView.onSaveInstanceState(outState);
         }
 
         outState.putString(MapConstants.PREFS_TILE_SOURCE, mapView.getTileProvider().getTileSource().name());
         outState.putInt(MapConstants.PREFS_ZOOM_LEVEL, mapView.getZoomLevel());
         outState.putInt(MapConstants.PREFS_SCROLL_X, mapView.getScrollX());
         outState.putInt(MapConstants.PREFS_SCROLL_Y, mapView.getScrollY());
         // Status
         boolean isMyLocationEnabled = myLocation!=null ?  myLocation.isMyLocationEnabled() : false;
         boolean isCompassEnabled = myLocation!=null ?  myLocation.isCompassEnabled() : false;
         outState.putBoolean(MapConstants.PREFS_SHOW_LOCATION, isMyLocationEnabled);
         outState.putBoolean(MapConstants.PREFS_SHOW_COMPASS, isCompassEnabled);
         // Overlay
         outState.putBoolean(MapConstants.PREFS_SHOW_OVERLAY_MINIMAP, isOverlayMinimap());
         outState.putBoolean(MapConstants.PREFS_SHOW_OVERLAY_SCALEBAR, isOverlayScaleBar());
 
         super.onSaveInstanceState(outState);
         Log.d(TAG, "--- ---------------------------- ---");
     }
 
 
 
 
     public void saveMapPreference(SharedPreferences.Editor outState) {
         Log.d(TAG, "--- ---------------------------- ---");
         Log.d(TAG, "--- Save Map Preference          ---");
         Log.d(TAG, "--- ---------------------------- ---");
         outState.putLong(MapConstants.PREFS_SAVE_DATE_IN_MS, System.currentTimeMillis());
         // Tile
         String tileProviderName = mapView.getTileProvider().getTileSource().name();
         outState.putString(MapConstants.PREFS_TILE_SOURCE, tileProviderName);
         Log.d(TAG, "--- Map TileName : " + tileProviderName);
         // Zoom
         int zoom =  mapView.getZoomLevel();
         Log.d(TAG, "--- Map Zoom : " + zoom);
         outState.putInt(MapConstants.PREFS_ZOOM_LEVEL,zoom);
         // Center
         int scrollX = mapView.getScrollX();
         int scrollY = mapView.getScrollY();
         outState.putInt(MapConstants.PREFS_SCROLL_X, scrollX);
         outState.putInt(MapConstants.PREFS_SCROLL_Y,scrollY);
         Log.d(TAG, "--- Map scrollXY : " + scrollX + ";" + scrollY);
         // Status
         boolean isMyLocationEnabled = myLocation!=null ?  myLocation.isMyLocationEnabled() : false;
         boolean isCompassEnabled = myLocation!=null ?  myLocation.isCompassEnabled() : false;
         outState.putBoolean(MapConstants.PREFS_SHOW_LOCATION, isMyLocationEnabled);
         outState.putBoolean(MapConstants.PREFS_SHOW_COMPASS, isCompassEnabled);
         Log.d(TAG, "--- Map isMyLocationEnabled : " + isMyLocationEnabled);
         Log.d(TAG, "--- Map isCompassEnabled : " + isCompassEnabled);
 
         // Overlay
         boolean isOverlayMinimap = isOverlayMinimap();
         boolean isOverlayScaleBar = isOverlayScaleBar();
         outState.putBoolean(MapConstants.PREFS_SHOW_OVERLAY_MINIMAP, isOverlayMinimap);
         outState.putBoolean(MapConstants.PREFS_SHOW_OVERLAY_SCALEBAR, isOverlayScaleBar );
         Log.d(TAG, "--- Map Overlay  Minimap : " + isOverlayMinimap);
         Log.d(TAG, "--- Map Overlay  ScaleBar : " + isOverlayScaleBar);
         Log.d(TAG, "--- ---------------------------- ---");
     }
 
 
 
     public void saveMapPreference(SharedPreferences privateSharedPreferences) {
         final SharedPreferences.Editor localEdit = privateSharedPreferences.edit();
         saveMapPreference(localEdit);
         localEdit.commit();
     }
 
     public void onRestoreSaveInstanceState(android.os.Bundle savedInstanceState) {
         Log.d(TAG, "--- ---------------------------- ---");
         Log.d(TAG, "--- Restore SaveInstanceState    ---");
         Log.d(TAG, "--- ---------------------------- ---");
 
         if (savedInstanceState!=null) {
             // Tile Source
             String tileName = savedInstanceState.getString(MapConstants.PREFS_TILE_SOURCE);
             if (tileName!=null) {
                 setMapViewTileSourceName(tileName);
                 Log.d(TAG, "--- Map TileName : " + tileName);
             }
             // Zoom
             int zoom = savedInstanceState.getInt(MapConstants.PREFS_ZOOM_LEVEL, -1);
             if (zoom>-1) {
                 mapController.setZoom(zoom);
                 Log.d(TAG, "--- Map Zoom : " + zoom);
             }
             // Center
             if (savedInstanceState.containsKey(MapConstants.PREFS_SCROLL_X) && savedInstanceState.containsKey(MapConstants.PREFS_SCROLL_Y)) {
                 int scrollX = savedInstanceState.getInt(MapConstants.PREFS_SCROLL_X, Integer.MIN_VALUE);
                 int scrollY = savedInstanceState.getInt(MapConstants.PREFS_SCROLL_Y, Integer.MIN_VALUE);
                 if (Integer.MIN_VALUE != scrollX && Integer.MIN_VALUE != scrollY) {
                     Log.d(TAG, "--- Map scrollXY center : " + scrollX + ";" + scrollY);
                     mapView.scrollTo(scrollX, scrollY);
                 }
             }
         }
         Log.d(TAG, "--- ---------------------------- ---");
     }
 
 
     public void restoreMapPreference(SharedPreferences prefs) {
         Log.d(TAG, "--- ---------------------------- ---");
         Log.d(TAG, "--- Restore Map Preference       ---");
         Log.d(TAG, "--- ---------------------------- ---");
         // --- Map Preference
         // --- -----------------
         // Tile
         ITileSource tileSource = getPreferenceMapViewTileSource(prefs);
         mapView.setTileSource(tileSource);
 
         // Zoom 1 is world view
         int zoom = prefs.getInt(MapConstants.PREFS_ZOOM_LEVEL, tileSource.getMaximumZoomLevel());
         mapController.setZoom(zoom);
         Log.d(TAG, "--- Zoom : " + zoom );
 
 
         // My Location
         // ---------------
         boolean isMyLocationEnabled = prefs.getBoolean(MapConstants.PREFS_SHOW_LOCATION, false);
         boolean isCompassEnabled = prefs.getBoolean(MapConstants.PREFS_SHOW_COMPASS, false);
         addOverlayMyLocation(isMyLocationEnabled);
 
         if (this.myLocation!=null) {
             if (isMyLocationEnabled) {
                 this.myLocation.enableMyLocation();
             }
           //  this.myLocation.enableCompass(isCompassEnabled);
         }
 
         Log.d(TAG, "--- Map isMyLocationEnabled : " + isMyLocationEnabled);
         Log.d(TAG, "--- Map isCompassEnabled : " + isCompassEnabled);
 
         // Center
         // ---------------
         int scrollX = prefs.getInt(MapConstants.PREFS_SCROLL_X, Integer.MIN_VALUE);
         int scrollY = prefs.getInt(MapConstants.PREFS_SCROLL_Y, Integer.MIN_VALUE);
         if (Integer.MIN_VALUE != scrollX && Integer.MIN_VALUE != scrollY) {
             Log.d(TAG, "--- Map scrollXY : " + scrollX + ";" + scrollY);
             mapView.scrollTo(scrollX, scrollY);
         } else {
             final LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
             GeoPoint geoPoint = LocationUtils.getLastKnownLocationAsGeoPoint(locationManager);
             if (geoPoint != null) {
                 Log.d(TAG, "--- Map Center on LastPos : " + geoPoint);
                 mapController.setCenter(geoPoint);
             }
         }
 
         // Overlay
         // ---------------
         boolean isOverlayMinimap =   prefs.getBoolean(MapConstants.PREFS_SHOW_OVERLAY_MINIMAP, false);
         boolean isOverlayScaleBar = prefs.getBoolean(MapConstants.PREFS_SHOW_OVERLAY_SCALEBAR, false);
         addOverlayMinimap(isOverlayMinimap);
         addOverlayScaleBar(isOverlayScaleBar);
         Log.d(TAG, "--- Map Overlay  Minimap : " + isOverlayMinimap);
         Log.d(TAG, "--- Map Overlay  ScaleBar : " + isOverlayScaleBar);
 
         Log.d(TAG, "--- ---------------------------- ---");
     }
 
 
     // ===========================================================
     // Accessor
     // ===========================================================
 
 
     // ===========================================================
     // Map Tile
     // ===========================================================
 
 
 
     public ITileSource getMapViewTileSource() {
         return mapView.getTileProvider().getTileSource();
     }
 
     public void setMapViewTileSourceName(String tileSourceName) {
         ITileSource tileSource = null;
         if (tileSourceName!=null && tileSourceName.length()>0) {
             try {
                 tileSource = TileSourceFactory.getTileSource(tileSourceName);
             } catch (final IllegalArgumentException ignore) {
             }
         }
         if (tileSource!=null) {
             mapView.setTileSource(tileSource);
         }
     }
 
     public void setMapViewTileSource(ITileSource tileSource) {
         IGeoPoint center =  mapView.getMapCenter();
         mapView.setTileSource(tileSource);
         mapController.setCenter(center);
     }
 
 
 
     // ===========================================================
     // Map Overlays
     // ===========================================================
 
     public MyLocationOverlay2 switchOverlayMyLocation() {
         boolean toAdd = isOverlayMyLocation();
        return  addOverlayMyLocation(!toAdd);
     }
     
     public MyLocationOverlay2 addOverlayMyLocation(boolean toAdd) {
         if (toAdd) {
             // Add
             if ( this.myLocation==null) {
                 this.myLocation  = new MyLocationOverlay2(getActivity(), this.mapView);
             }
             List<Overlay> overlays  =mapView.getOverlays();
             if (!overlays.contains(myLocation)) {
               //  myLocation.enableMyLocation();
                 overlays.add(myLocation);
             }
         } else {
             // Delete
             if (myLocation!=null) {
                 myLocation.disableMyLocation();
                 mapView.getOverlays().remove(myLocation);
             }
         }
         return myLocation;
     }
 
     public boolean isOverlayMyLocation() {
         boolean result = (myLocation!=null && mapView.getOverlays().contains(myLocation));
         return result;
     }
 
     public void addOverlayScaleBar(boolean toAdd) {
         if (toAdd) {
             // Add
             if (mScaleBarOverlay==null) {
                 this.mScaleBarOverlay = new ScaleBarOverlay(getActivity(), mResourceProxy);
                 this.mScaleBarOverlay.setMetric();
                 // Scale bar tries to draw as 1-inch, so to put it in the top center, set x offset to
                 // half screen width, minus half an inch.
                 this.mScaleBarOverlay.setScaleBarOffset(getResources().getDisplayMetrics().widthPixels
                         / 2 - getResources().getDisplayMetrics().xdpi / 2, 10);
             }
             mapView.getOverlays().add(mScaleBarOverlay);
         } else {
             // Delete
             if (mScaleBarOverlay!=null) {
                 mapView.getOverlays().remove(mScaleBarOverlay);
             }
         }
     }
 
     public boolean isOverlayScaleBar() {
         boolean result = (mScaleBarOverlay!=null && mapView.getOverlays().contains(mScaleBarOverlay));
         return result;
     }
 
     public void addOverlayMinimap(boolean toAdd) {
         if (toAdd) {
             // Add
             if (miniMapOverlay==null) {
                 miniMapOverlay = new MinimapOverlay(getActivity(),  mapView.getTileRequestCompleteHandler());
             }
             mapView.getOverlays().add(miniMapOverlay);
         } else {
             // Delete
             if (miniMapOverlay!=null) {
                 mapView.getOverlays().remove(miniMapOverlay);
             }
         }
     }
 
     public boolean isOverlayMinimap() {
         boolean result = (miniMapOverlay!=null && mapView.getOverlays().contains(miniMapOverlay));
         return result;
     }
 
 
     // ===========================================================
     // Map Configuration
     // ===========================================================
 
     public String getMapViewTileSourceName(ITileSource tileSource) {
         return tileSource.localizedName(mResourceProxy);
     }
 
     public ArrayList<ITileSource> getMapViewTileSources() {
         return TileSourceFactory.getTileSources();
     }
 
 
     // ===========================================================
     // Map Action
     // ===========================================================
 
 
     public void mapAnimateTo(GeoPoint geoPoint) {
         if (geoPoint != null) {
             Message msg = uiMapHandler.obtainMessage(UI_MAPMSG_ANIMATE_TO_GEOPOINT, geoPoint);
             uiMapHandler.sendMessage(msg);
         }
     }
 
 
 
     public boolean isGpsLocationProviderIsEnable() {
         boolean result = false;
         if (myLocation != null) {
             result = myLocation.isGpsLocationProviderIsEnable();
         }
         return result;
     }
 
 
 
     // ===========================================================
     // MyLocation Action
     // ===========================================================
 
 
     public void myLocationFollow(boolean isFollow) {
         if (isFollow) {
             if (myLocation==null) {
               addOverlayMyLocation(true);
             }
             myLocation.enableMyLocation();
         } else {
             if (myLocation!=null) {
                 myLocation.disableFollowLocation();
             }
         }
     }
 
     public void centerOnLocation(Location location) {
         if (location!=null) {
             GeoPoint geoPoint =  GeoLocHelper.convertLocationAsGeoPoint(location);
             int accuracy = (int)location.getAccuracy();
             Log.d(TAG, "centerOnLocation Location: " + location + " with accuracy +/- " + accuracy + " m.");
             centerOnLocation(geoPoint, accuracy);
         }
     }
 
     public void centerOnLocation( GeoPoint geoPoint ) {
         centerOnLocation(geoPoint, -1);
     }
 
     public void centerOnLocation( GeoPoint geoPoint,  int accuracy ) {
        // Center
         if (geoPoint!=null) {
            mapController.setCenter(geoPoint);
            Log.d(TAG, "centerOnLocation geoPoint: " + geoPoint + " with accuracy +/- " + accuracy + " m.");
         }
         // Zoom
         // <a href="http://wiki.openstreetmap.org/wiki/Zoom_levels">Zoom levels</a>
         if (accuracy>-1  && geoPoint!=null) {
             BoundingBoxE6 boundyBox = mapView.getBoundingBox();
             int diagInM =  boundyBox.getDiagonalLengthInMeters();
             if (accuracy>diagInM) {
                 // compute zooem
                 // TODO accuracy/diagInM;
                 int wantedZoom = mapView.getZoomLevel() -1;
                 // Send Zoom Request
                 Message msg = uiMapHandler.obtainMessage(UI_MAPMSG_MAP_ZOOM_MAX);
                 msg.obj = wantedZoom;
                 uiMapHandler.sendMessage(msg);
             }
         }
     }
 
     public void centerOnMyLocationFix() {
         Log.d(TAG, "Ask centerOnMyPosition");
 
         addOverlayMyLocation(true);
 
         if (!myLocation.isMyLocationEnabled()) {
             myLocation.enableMyLocation(true);
             Log.d(TAG, "Ask centerOnMyPosition = do enableMyLocation");
         } else{
             if (!myLocation.isFollowLocationEnabled()) {
                 myLocation.enableFollowLocation();
             } else {
                 uiMapHandler.sendEmptyMessage(UI_MAPMSG_MAP_ZOOM_MAX);
             }
         }
 //        mapView.getScroller().forceFinishedforceFinished(true);
         myLocation.animateToLastFix();
 
         if (false) {
             myLocation.runOnFirstFix(new Runnable() {
 
                 @Override
                 public void run() {
                     uiMapHandler.sendEmptyMessage(UI_MAPMSG_MAP_ZOOM_MAX);
                 }
             });
         }
     }
 
 
 
     // ===========================================================
     // Key Event
     // ===========================================================
 
     public boolean onKeyDown(int keyCode, KeyEvent event) {
 
         return false;
     }
 }
 
 
 

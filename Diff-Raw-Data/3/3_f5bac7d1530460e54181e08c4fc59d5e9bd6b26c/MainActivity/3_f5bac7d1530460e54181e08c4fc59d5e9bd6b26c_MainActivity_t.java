 package info.eigenein.openwifi.activities;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.location.*;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.Toast;
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.android.maps.*;
 import info.eigenein.openwifi.R;
 import info.eigenein.openwifi.helpers.entities.Area;
 import info.eigenein.openwifi.helpers.entities.Cluster;
 import info.eigenein.openwifi.helpers.entities.ClusterList;
 import info.eigenein.openwifi.helpers.entities.Network;
 import info.eigenein.openwifi.helpers.location.L;
 import info.eigenein.openwifi.helpers.location.LocationProcessor;
 import info.eigenein.openwifi.helpers.location.LocationTracker;
 import info.eigenein.openwifi.helpers.map.*;
 import info.eigenein.openwifi.helpers.scan.ScanResultTracker;
 import info.eigenein.openwifi.helpers.scan.ScanServiceManager;
 import info.eigenein.openwifi.persistency.MyScanResult;
 import org.apache.commons.collections.map.MultiKeyMap;
 
 import java.util.*;
 
 /**
  * Main application activity with the map.
  */
 public class MainActivity extends MapActivity {
     private static final String LOG_TAG = MainActivity.class.getCanonicalName();
 
     private final static int DEFAULT_ZOOM = 17;
 
     private TrackableMapView mapView = null;
     private MyLocationOverlay myLocationOverlay = null;
     private ClusterListOverlay clusterListOverlay = null;
 
     private RefreshScanResultsAsyncTask refreshScanResultsAsyncTask = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Setup default values for the settings.
         PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 
         // Setup view.
         setContentView(R.layout.main);
 
         // Setup action bar.
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             getActionBar().setDisplayShowTitleEnabled(false);
         }
 
         // Setup map.
         mapView = (TrackableMapView)findViewById(R.id.map_view);
         mapView.setBuiltInZoomControls(false);
         mapView.addMovedOrZoomedObserver(new MapViewListener() {
             @Override
             public void onMovedOrZoomed() {
                 Log.d(LOG_TAG + ".onCreate", "onMovedOrZoomed");
                 updateZoomButtonsState();
                 startRefreshingScanResultsOnMap();
             }
         });
         mapView.invalidateMovedOrZoomed();
         // Setup map controller.
         final MapController mapController = mapView.getController();
         // Setup current location.
         myLocationOverlay = new TrackableMyLocationOverlay(this, mapView);
         myLocationOverlay.runOnFirstFix(new Runnable() {
             public void run() {
                 Log.d(LOG_TAG + ".onCreate", "runOnFirstFix");
                 // Zoom in to current location
                 mapController.setZoom(DEFAULT_ZOOM);
                 mapController.animateTo(myLocationOverlay.getMyLocation());
                 mapView.invalidateMovedOrZoomed();
             }
         });
         // Setup my location button.
         findViewById(R.id.button_my_location).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 GeoPoint myLocation = myLocationOverlay.getMyLocation();
                 if (myLocation == null) {
                     // Try to obtain current location from the location tracker.
                     final Location location = LocationTracker.getInstance().getLocation(MainActivity.this);
                     if (location != null) {
                         myLocation = L.toGeoPoint(location.getLatitude(), location.getLongitude());
                     }
                 }
                 if (myLocation != null) {
                     mapController.animateTo(myLocation);
                     mapView.invalidateMovedOrZoomed();
                 } else {
                     Toast.makeText(MainActivity.this, R.string.my_location_is_unavailable, Toast.LENGTH_SHORT).show();
                 }
             }
         });
         // Setup zoom buttons.
         findViewById(R.id.button_zoom_out).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 mapController.zoomOut();
                 mapView.invalidateMovedOrZoomed();
             }
         });
         findViewById(R.id.button_zoom_in).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 mapController.zoomIn();
                 mapView.invalidateMovedOrZoomed();
             }
         });
         // Setup overlays.
         final List<Overlay> overlays = mapView.getOverlays();
         clusterListOverlay = new ClusterListOverlay();
         overlays.add(clusterListOverlay);
        overlays.add(myLocationOverlay);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         final MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
 
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         super.onPrepareOptionsMenu(menu);
 
         boolean isServiceStarted = ScanServiceManager.isStarted(this);
         menu.findItem(R.id.start_scan_menuitem).setVisible(!isServiceStarted);
         menu.findItem(R.id.pause_scan_menuitem).setVisible(isServiceStarted);
 
         return true;
     }
 
     @Override
     public void onStart() {
         super.onStart();
 
         // Initialize my location.
         if (myLocationOverlay != null) {
             // Enable my location.
             myLocationOverlay.enableMyLocation();
             myLocationOverlay.enableCompass();
         }
         // Update overlays.
         startRefreshingScanResultsOnMap();
 
         EasyTracker.getInstance().activityStart(this);
     }
 
     @Override
     public void onStop() {
         super.onStop();
 
         if (myLocationOverlay != null) {
             // Disable my location to avoid using of location services.
             myLocationOverlay.disableCompass();
             myLocationOverlay.disableMyLocation();
         }
 
         EasyTracker.getInstance().activityStop(this);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.settings_menuitem:
                 startActivity(new Intent(this, SettingsActivity.class));
                 return true;
             case R.id.start_scan_menuitem:
                 ScanServiceManager.restart(this);
                 Toast.makeText(this, R.string.scan_started, Toast.LENGTH_LONG).show();
                 invalidateOptionsMenu();
                 return true;
             case R.id.pause_scan_menuitem:
                 ScanServiceManager.stop(this);
                 Toast.makeText(this, R.string.scan_paused, Toast.LENGTH_SHORT).show();
                 invalidateOptionsMenu();
                 return true;
             case R.id.map_view_menuitem:
                 final CharSequence[] items = getResources().getTextArray(R.array.map_views);
                 new AlertDialog.Builder(this)
                         .setTitle(getString(R.string.map_view))
                         .setItems(items, new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int item) {
                                 switch (item) {
                                     case 0:
                                         mapView.setSatellite(false);
                                         break;
                                     case 1:
                                         mapView.setSatellite(true);
                                         break;
                                 }
                             }
                         })
                         .show();
                 return true;
             case R.id.about_menuitem:
                 startActivity(new Intent(this, AboutActivity.class));
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public void invalidateOptionsMenu() {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             // Added in API level 11
             super.invalidateOptionsMenu();
         }
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
     /**
      * Updates zoom buttons enabled/disabled state for the current zoom level.
      */
     private void updateZoomButtonsState() {
         final ImageButton zoomOutButton = (ImageButton)findViewById(R.id.button_zoom_out);
         zoomOutButton.setEnabled(mapView.getZoomLevel() != 1);
         final ImageButton zoomInButton = (ImageButton)findViewById(R.id.button_zoom_in);
         zoomInButton.setEnabled(mapView.getZoomLevel() != mapView.getMaxZoomLevel());
     }
 
     /**
      * Refreshes the scan results on the map.
      */
     private void startRefreshingScanResultsOnMap() {
         Log.d(LOG_TAG, "startRefreshingScanResultsOnMap");
 
         // Check if the task is already running.
        cancelRefreshScanResultsAsyncTask();
 
         // Check map bounds.
         if (mapView.getLatitudeSpan() == 0 || mapView.getLongitudeSpan() == 0) {
             Log.w(LOG_TAG, "Zero mapView span.");
             return;
         }
         // Get map bounds.
         final Projection mapViewProjection = mapView.getProjection();
         GeoPoint nwGeoPoint = mapViewProjection.fromPixels(0, 0);
         GeoPoint seGeoPoint = mapViewProjection.fromPixels(mapView.getWidth(), mapView.getHeight());
         // Run task to retrieve the scan results and process them into a cluster list.
         refreshScanResultsAsyncTask = new RefreshScanResultsAsyncTask(
                 L.fromE6(seGeoPoint.getLatitudeE6()),
                 L.fromE6(nwGeoPoint.getLongitudeE6()),
                 L.fromE6(nwGeoPoint.getLatitudeE6()),
                 L.fromE6(seGeoPoint.getLongitudeE6()),
                 0.0005 * Math.pow(2.0, 20.0 - mapView.getZoomLevel())
         );
         refreshScanResultsAsyncTask.execute();
     }
 
     private synchronized void cancelRefreshScanResultsAsyncTask() {
         if (refreshScanResultsAsyncTask != null) {
             // Cancel old task.
             refreshScanResultsAsyncTask.cancel(true);
             refreshScanResultsAsyncTask = null;
         }
     }
 
     /**
      * Used to aggregate the scan results from the application database.
      */
     public class RefreshScanResultsAsyncTask extends AsyncTask<Void, Void, ClusterList> {
         private final String LOG_TAG = RefreshScanResultsAsyncTask.class.getCanonicalName();
 
         /**
          * Defines a "border" for selecting scan results within the specified area.
          * Without this border a cluster "jumps" when one of its scan results
          * goes off the visible area.
          */
         private static final double BORDER_WIDTH = 0.002;
 
         private final double minLatitude;
 
         private final double minLongitude;
 
         private final double maxLatitude;
 
         private final double maxLongitude;
 
         private final double gridSize;
 
         /**
          * Groups scan results into the grid by their location.
          * (int, int) -> StoredScanResult
          */
         private final MultiKeyMap cellToScanResultCache = new MultiKeyMap();
 
         public RefreshScanResultsAsyncTask(
                 final double minLatitude,
                 final double minLongitude,
                 final double maxLatitude,
                 final double maxLongitude,
                 final double gridSize) {
             Log.d(LOG_TAG, String.format(
                     "RefreshScanResultsAsyncTask[minLat=%s, minLon=%s, maxLat=%s, maxLon=%s, gridSize=%s]",
                     minLatitude,
                     minLongitude,
                     maxLatitude,
                     maxLongitude,
                     gridSize));
             this.minLatitude = minLatitude;
             this.minLongitude = minLongitude;
             this.maxLatitude = maxLatitude;
             this.maxLongitude = maxLongitude;
             this.gridSize = gridSize;
         }
 
         @Override
         protected ClusterList doInBackground(Void... params) {
             // Retrieve scan results.
             final long getScanResultsStartTime = System.currentTimeMillis();
             final List<MyScanResult> scanResults = ScanResultTracker.getScanResults(
                     MainActivity.this,
                     minLatitude - BORDER_WIDTH,
                     minLongitude - BORDER_WIDTH,
                     maxLatitude + BORDER_WIDTH,
                     maxLongitude + BORDER_WIDTH
             );
             Log.d(LOG_TAG + ".doInBackground", String.format(
                     "fetched %d results in %sms.",
                     scanResults.size(),
                     System.currentTimeMillis() - getScanResultsStartTime
             ));
             // Process them if we're still not cancelled.
             if (isCancelled()) {
                 return null;
             }
             for (final MyScanResult scanResult : scanResults) {
                 // Check if we're cancelled.
                 if (isCancelled()) {
                     return null;
                 }
                 addScanResult(scanResult);
             }
             return buildClusterList();
         }
 
         @Override
         protected synchronized void onPostExecute(final ClusterList clusterList) {
             Log.d(LOG_TAG + ".onPostExecute", clusterList.toString());
 
             clusterListOverlay.clearClusterOverlays();
             for (final Cluster cluster : clusterList) {
                 ClusterOverlay clusterOverlay = new ClusterOverlay(
                         MainActivity.this,
                         cluster
                 );
                 clusterListOverlay.addClusterOverlay(clusterOverlay);
             }
             mapView.invalidate();
         }
 
         @Override
         protected void onCancelled(final ClusterList result) {
             Log.d(LOG_TAG + ".onCancelled", "cancelled");
         }
 
         private void addScanResult(final MyScanResult scanResult) {
             final int key1 = (int)Math.floor(scanResult.getLatitude() / gridSize);
             final int key2 = (int)Math.floor(scanResult.getLongitude() / gridSize);
 
             List<MyScanResult> subCache = (List<MyScanResult>)cellToScanResultCache.get(key1, key2);
             if (subCache == null) {
                 subCache = new ArrayList<MyScanResult>();
                 cellToScanResultCache.put(key1, key2, subCache);
             }
 
             subCache.add(scanResult);
         }
 
         private ClusterList buildClusterList() {
             final ClusterList clusterList = new ClusterList();
 
             // Iterate through grid cells.
             for (final Object o : cellToScanResultCache.values()) {
                 // Check if we're cancelled.
                 if (isCancelled()) {
                     return null;
                 }
 
                 final List<MyScanResult> subCache = (List<MyScanResult>)o;
                 final HashMap<String, HashSet<String>> ssidToBssidCache = new HashMap<String, HashSet<String>>();
 
                 LocationProcessor locationProcessor = new LocationProcessor();
                 for (final MyScanResult scanResult : subCache) {
                     // Check if we're cancelled.
                     if (isCancelled()) {
                         return null;
                     }
                     // Combine BSSIDs from the same SSIDs.
                     HashSet<String> bssids = ssidToBssidCache.get(scanResult.getSsid());
                     if (bssids == null) {
                         bssids = new HashSet<String>();
                         ssidToBssidCache.put(scanResult.getSsid(), bssids);
                     }
                     bssids.add(scanResult.getBssid());
                     // Track the location.
                     locationProcessor.add(scanResult);
                 }
 
                 // Initialize a cluster.
                 final Area area = locationProcessor.getArea();
                 final Cluster cluster = new Cluster(area);
                 // And fill it with networks.
                 for (final Map.Entry<String, HashSet<String>> entry : ssidToBssidCache.entrySet()) {
                     // Check if we're cancelled.
                     if (isCancelled()) {
                         return null;
                     }
                     cluster.add(new Network(entry.getKey(), entry.getValue()));
                 }
                 // Finally, add the cluster to the cluster list.
                 clusterList.add(cluster);
                 Log.d(LOG_TAG, "clusterList.add " + cluster);
             }
 
             return clusterList;
         }
     }
 }

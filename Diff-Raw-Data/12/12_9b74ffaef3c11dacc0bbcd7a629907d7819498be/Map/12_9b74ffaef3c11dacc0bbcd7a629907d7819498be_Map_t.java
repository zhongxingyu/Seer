 package de.jensnistler.routemap.activities;
 
 import java.io.File;
 import java.text.DecimalFormat;
 import java.util.Iterator;
 import java.util.List;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 import de.jensnistler.routemap.R;
 import de.jensnistler.routemap.helper.GPXParser;
 import de.jensnistler.routemap.helper.RotateViewGroup;
 import de.jensnistler.routemap.helper.RouteMapViewGroup;
 import de.jensnistler.routemap.helper.DirectionPathOverlay;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.provider.Settings;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.WindowManager;
 import android.widget.FrameLayout.LayoutParams;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 public class Map extends MapActivity implements LocationListener {
     private static final int UPDATE_LOCATION = 1;
 
     private static final String BRIGHTNESS_NOCHANGE = "nochange";
     private static final String BRIGHTNESS_AUTOMATIC = "automatic";
     private static final String BRIGHTNESS_MAXIMUM = "maximum";
     private static final String BRIGHTNESS_MEDIUM = "medium";
     private static final String BRIGHTNESS_LOW = "low";
 
     private static final String DISTANCE_MILES = "mi";
     private static final String DISTANCE_KILOMETERS = "km";
 
     private boolean mPreferenceStandby;
     private boolean mPreferenceRotateMap;
     private String mPreferenceBrightness;
     private String mPreferenceRouteFile;
     private String mPreferenceDistanceUnit;
     private int mUserBrightnessMode;
 
     private MapView mMapView;
     private RotateViewGroup mMapViewGroup; 
     private RouteMapViewGroup mViewGroup;
     private RelativeLayout mImageViewGroup;
     private LocationManager mLocationManager;
     private double mLatitude;
     private double mLongitude;
     private Thread mThread;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         mViewGroup = new RouteMapViewGroup(this);
 
         // add map
         mMapView = new MapView(this, "0jeqOULMZgwc-uSv37wipOJh6umeCscVH48yV9Q");
         mMapView.getController().setZoom(18);
         mMapView.setBuiltInZoomControls(true);
         mMapView.setSatellite(false);
         mMapView.setTraffic(false);
         mMapView.setEnabled(true);
 
         // rotate view
         mMapViewGroup = new RotateViewGroup(this);
         mMapViewGroup.addView(mMapView);
         mViewGroup.addView(mMapViewGroup);
 
         // position marker
         LayoutParams positionViewLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
         ImageView positionImageView = new ImageView(this);
         positionImageView.setScaleType(ImageView.ScaleType.CENTER);
         Drawable positionDrawable = this.getResources().getDrawable(R.drawable.mymarker);
         positionImageView.setImageDrawable(positionDrawable);
 
         // google logo on map, original is out of sight
         ImageView googleLogoImageView = new ImageView(this);
         googleLogoImageView.setScaleType(ImageView.ScaleType.CENTER);
         Drawable googleLogoDrawable = this.getResources().getDrawable(R.drawable.google);
         googleLogoImageView.setImageDrawable(googleLogoDrawable);
         RelativeLayout.LayoutParams googleLogoViewLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
         googleLogoViewLayout.leftMargin = 20;
         googleLogoViewLayout.bottomMargin = 20;
         googleLogoViewLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 
         // add image view to main view
         mImageViewGroup = new RelativeLayout(this);
         mImageViewGroup.addView(positionImageView, positionViewLayout);
         mImageViewGroup.addView(googleLogoImageView, googleLogoViewLayout);
         mViewGroup.addView(mImageViewGroup);
 
         // set content view
         setContentView(mViewGroup);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         // load preference data
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
         mPreferenceStandby = prefs.getBoolean("standby", false);
         mPreferenceRotateMap = prefs.getBoolean("rotateMap", false);
         mPreferenceBrightness = prefs.getString("brightness", BRIGHTNESS_NOCHANGE).trim();
         mPreferenceRouteFile = prefs.getString("routeFile", null);
         mPreferenceDistanceUnit = prefs.getString("mPreferenceDistanceUnit", DISTANCE_MILES);
 
         // save user brightness
         try {
             mUserBrightnessMode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
         }
         catch (Settings.SettingNotFoundException e) {
             // setting not found, default can not be restored
             mUserBrightnessMode = -1;
         }
 
         // disable standby
         if (true == mPreferenceStandby) {
             getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         }
 
         // disable map rotation
         if (false == mPreferenceRotateMap) {
             mMapViewGroup.setHeading(0.0f);
         }
 
         loadRouteFile();
         handleBrightnessPreferences();
 
         // start gps thread
         initializeLocationAndStartGpsThread();
     }
 
     /**
      * restore settings on pause
      */
     @Override
     protected void onPause() {
         // reset standby mode
         if (true == mPreferenceStandby) {
             getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         }
 
         // reset brightness to user default
         restoreBrightness();
 
         // stop gps update handler
         mThread.interrupt();
 
         super.onPause();
     }
 
     private void loadRouteFile() {
         // load route file
         if (null != mPreferenceRouteFile) {
             File routeFile = new File(mPreferenceRouteFile.trim());
             if (!routeFile.exists()) {
                 Toast.makeText(this, "Failed to load route file: " + mPreferenceRouteFile, Toast.LENGTH_LONG).show();
                 SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                 SharedPreferences.Editor editor = prefs.edit();
                 editor.putString("routeFile", null);
                 editor.commit();
             }
             else {
                 renderRouteFile(routeFile);
             }
         }
     }
 
     /**
      * handle preferences and change system values
      */
     private void handleBrightnessPreferences() {
         // restore user settings
         if (mPreferenceBrightness.equals(BRIGHTNESS_NOCHANGE)) {
             restoreBrightness();
         }
         // automatic mode
         else if (mPreferenceBrightness.equals(BRIGHTNESS_AUTOMATIC)) {
             Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
         }
         // manual mode
         else {
             WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
             Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
             if (mPreferenceBrightness.equals(BRIGHTNESS_MAXIMUM)) {
                 layoutParams.screenBrightness = 1.0f;
             }
             else if (mPreferenceBrightness.equals(BRIGHTNESS_MEDIUM)) {
                 layoutParams.screenBrightness = 0.6f;
             }
             else if (mPreferenceBrightness.equals(BRIGHTNESS_LOW)) {
                 layoutParams.screenBrightness = 0.2f;
             }
             getWindow().setAttributes(layoutParams);
         }
     }
 
     private void restoreBrightness() {
         if (Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL != mUserBrightnessMode && Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC != mUserBrightnessMode) {
             Toast toast = Toast.makeText(this, "Failed to restore brightness settings", Toast.LENGTH_LONG);
             toast.show();
             return;
         }
 
         Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, mUserBrightnessMode);
         if (mUserBrightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
             WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
             layoutParams.screenBrightness = -1.0f;
             getWindow().setAttributes(layoutParams);
         }
     }
 
     /**
      * set location to last known position and start
      * thread to handle position changes
      */
     private void initializeLocationAndStartGpsThread() {
         this.setCurrentGpsLocation(null);
         mThread = new Thread(new LocationThreadRunner());
         mThread.start();
         this.updateMyLocation();
     }
 
     /**
      * send a message to the update handler with either the current location
      * or the last known location.
      * 
      * @param location either null or the current location
      */
     private void setCurrentGpsLocation(Location location) {
         if (location == null) {
             mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
             mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
             location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 
             setTitle("Waiting for GPS signal...");
         }
 
         try {
             mLongitude = location.getLongitude();
             mLatitude = location.getLatitude();
             Message msg = Message.obtain();
             msg.what = UPDATE_LOCATION;
             de.jensnistler.routemap.activities.Map.this.updateHandler.sendMessage(msg);
 
             // rotate map
             if (true == mPreferenceRotateMap && location.hasBearing()) {
                 mMapViewGroup.setHeading(location.getBearing());
             }
 
             // show speed in activity title
             if (true == location.hasSpeed()) {
                 // meters per second
                 float speed = location.getSpeed();
                 // meters per hour
                 speed = speed * 60 * 60;
                 // kilometers per hour
                 speed = speed / 1000;
 
                 // reduce to miles
                if (mPreferenceDistanceUnit.equals(DISTANCE_MILES)) {
                     speed = speed / 1.609344f;
                 }
 
                 String speedText = new DecimalFormat("###.##").format(speed);
                if (mPreferenceDistanceUnit.equals(DISTANCE_MILES)) {
                     speedText = speedText + " mph";
                 }
                else if (mPreferenceDistanceUnit.equals(DISTANCE_KILOMETERS)) {
                     speedText = speedText + " km/h";
                 }
 
                 setTitle(speedText);
             }
         } catch (NullPointerException e) {
             // don't update location
         }
     }
 
     /**
      * handles gps updates
      */
     Handler updateHandler = new Handler() {
         /** Gets called on every message that is received */
         // @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
             case UPDATE_LOCATION: {
                 de.jensnistler.routemap.activities.Map.this.updateMyLocation();
                 break;
             }
             }
             super.handleMessage(msg);
         }
     };
 
     /**
      * handles location updates in the background.
      */
     class LocationThreadRunner implements Runnable {
         // @Override
         public void run() {
             while (!Thread.currentThread().isInterrupted()) {
                 Message m = Message.obtain();
                 m.what = 0;
                 de.jensnistler.routemap.activities.Map.this.updateHandler.sendMessage(m);
                 try {
                     Thread.sleep(100);
                 } catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                 }
             }
         }
     }
 
     /**
      * move map to my current location
      */
     private void updateMyLocation() {
        GeoPoint point = new GeoPoint((int) (mLatitude * 1E6), (int) (mLongitude * 1E6));
 
         MapController mapController = mMapView.getController();
         mapController.animateTo(point);
     }
 
     /**
      * invoked by the location service when phone's location changes
      */
     public void onLocationChanged(Location newLocation) {
         setCurrentGpsLocation(newLocation);
     }
 
     /**
      * resets the gps location whenever the provider is enabled
      */
     public void onProviderEnabled(String provider) {
         setCurrentGpsLocation(null);
     }
 
     /**
      * resets the gps location whenever the provider is disabled
      */
     public void onProviderDisabled(String provider) {
         setCurrentGpsLocation(null);
     }
 
     /**
      * resets the gps location whenever the provider status changes
      * we don't care about the details
      */
     public void onStatusChanged(String provider, int status, Bundle extras) {
         setCurrentGpsLocation(null);
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
     private void renderRouteFile(File routeFile) {
         List<Overlay> overlays = mMapView.getOverlays();
         if (!overlays.isEmpty()) {
             overlays.removeAll(overlays);
         }
 
         GPXParser parser = new GPXParser(routeFile);
         List<List<Location>> tracks = parser.getTracks();
 
         Iterator<List<Location>> trackIterator = tracks.iterator();
         while (trackIterator.hasNext()) {
             List<Location> waypoints = trackIterator.next();
 
             Iterator<Location> waypointIterator = waypoints.iterator();
             Location lastWaypoint = null;
             while (waypointIterator.hasNext()) {
                 Location waypoint = waypointIterator.next();
 
                 if (null != lastWaypoint) {
                     GeoPoint startGeoPoint = new GeoPoint((int) (lastWaypoint.getLatitude() * 1E6), (int) (lastWaypoint.getLongitude() * 1E6));
                     GeoPoint targetGeoPoint = new GeoPoint((int) (waypoint.getLatitude() * 1E6), (int) (waypoint.getLongitude() * 1E6));
 
                     DirectionPathOverlay overlay = new DirectionPathOverlay(startGeoPoint, targetGeoPoint);
                     overlays.add(overlay);
                 }
 
                 lastWaypoint = waypoint;
             }
         }
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.mainmenu, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.mainmenu_settings:
             Intent preferencesActivity = new Intent(getBaseContext(), Preferences.class);
             startActivity(preferencesActivity);
             return true;
         case R.id.mainmenu_loadfromsd:
             Intent loadfromsdActivity = new Intent(getBaseContext(), LoadRouteFromSD.class);
             startActivity(loadfromsdActivity);
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 }

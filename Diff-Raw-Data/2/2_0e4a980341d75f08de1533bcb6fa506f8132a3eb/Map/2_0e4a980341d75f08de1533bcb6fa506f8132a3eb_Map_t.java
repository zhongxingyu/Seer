 package de.jensnistler.trackmap.activities;
 
 import java.io.File;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.mapsforge.android.maps.MapActivity;
 import org.mapsforge.android.maps.MapView;
 import org.mapsforge.android.maps.MapViewPosition;
 import org.mapsforge.android.maps.overlay.ListOverlay;
 import org.mapsforge.android.maps.overlay.Overlay;
 import org.mapsforge.android.maps.overlay.OverlayItem;
 import org.mapsforge.android.maps.overlay.PolygonalChain;
 import org.mapsforge.android.maps.overlay.Polyline;
 import org.mapsforge.core.model.GeoPoint;
 import org.mapsforge.core.model.MapPosition;
 import org.mapsforge.map.reader.header.FileOpenResult;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.FrameLayout.LayoutParams;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 import de.jensnistler.trackmap.R;
 import de.jensnistler.trackmap.helper.GPSUpdateHandler;
 import de.jensnistler.trackmap.helper.GPXParser;
 import de.jensnistler.trackmap.helper.LocationThreadRunner;
 import de.jensnistler.trackmap.helper.ViewGroupRotate;
 import de.jensnistler.trackmap.helper.ViewGroupTrackMap;
 
 public class Map extends MapActivity implements LocationListener {
     public static final String BRIGHTNESS_NOCHANGE = "nochange";
     public static final String BRIGHTNESS_MAXIMUM = "maximum";
     public static final String BRIGHTNESS_MEDIUM = "medium";
     public static final String BRIGHTNESS_LOW = "low";
 
     public static final String DIM_NEVER = "never";
     public static final String DIM_15 = "15";
     public static final String DIM_30 = "30";
     public static final String DIM_60 = "60";
 
     public static final String DISTANCE_MILES = "mi";
     public static final String DISTANCE_KILOMETERS = "km";
 
     private static final int ID_ZOOM_IN = 1;
     private static final int ID_ZOOM_OUT = 2;
 
     private boolean mPreferenceStandby;
     private boolean mPreferenceRotateMap;
     private String mPreferenceBrightness;
     private String mPreferenceRouteFile;
     private String mPreferenceDistanceUnit;
     private String mPreferenceDim;
     private String mPreferenceMapFile;
 
     private MapView mMapView;
     private ViewGroupRotate mMapViewGroup;
     private ViewGroupTrackMap mViewGroup;
     private RelativeLayout mImageViewGroup;
     private LocationManager mLocationManager;
     private double mLatitude;
     private double mLongitude;
     private Thread mLocationThread;
     private LocationThreadRunner mLocationThreadRunner;
     private Handler mHandler = new Handler();
     private Runnable mDimRunnable;
     private GPSUpdateHandler mUpdateHandler;
     private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // add map
         mMapView = new MapView(this);
 
         MapViewPosition position = mMapView.getMapViewPosition();
         MapPosition newMapPosition = new MapPosition(position.getCenter(), (byte)18);
         position.setMapPosition(newMapPosition);
 
         mMapView.setBuiltInZoomControls(false);
         mMapView.setClickable(false);
 
         // set individual style
         /*
         File themeFile = new File(cacheDir, "rendertheme.xml");
         try {
             mMapView.setRenderTheme(themeFile);
         }
         catch (FileNotFoundException e) {
             Toast.makeText(this, "Theme file not found, using default theme", Toast.LENGTH_SHORT).show();
         }
         */
 
         // rotate view
         mMapViewGroup = new ViewGroupRotate(this);
         mMapViewGroup.addView(mMapView);
 
         mViewGroup = new ViewGroupTrackMap(this);
         mViewGroup.addView(mMapViewGroup);
 
         // position marker
         LayoutParams positionViewLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
         ImageView positionImageView = new ImageView(this);
         positionImageView.setScaleType(ImageView.ScaleType.CENTER);
         Drawable positionDrawable = this.getResources().getDrawable(R.drawable.mymarker);
         positionImageView.setImageDrawable(positionDrawable);
 
         // zoom out
         ImageButton zoomOut = new ImageButton(this);
         zoomOut.setImageResource(R.drawable.minus);
         zoomOut.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 mMapView.getMapViewPosition().zoomOut();
             }
         });
         zoomOut.setId(ID_ZOOM_OUT);
         RelativeLayout.LayoutParams zoomOutViewLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
         zoomOutViewLayout.leftMargin = 20;
         zoomOutViewLayout.bottomMargin = 20;
         zoomOutViewLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
         zoomOutViewLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 
         // zoom in
         ImageButton zoomIn = new ImageButton(this);
         zoomIn.setImageResource(R.drawable.plus);
         zoomIn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 mMapView.getMapViewPosition().zoomIn();
             }
         });
         zoomIn.setId(ID_ZOOM_IN);
         RelativeLayout.LayoutParams zoomInViewLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
         zoomInViewLayout.rightMargin = 20;
         zoomInViewLayout.bottomMargin = 20;
         zoomInViewLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
         zoomInViewLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 
         // add image view to main view
         mImageViewGroup = new RelativeLayout(this);
         mImageViewGroup.addView(positionImageView, positionViewLayout);
         mImageViewGroup.addView(zoomOut, zoomOutViewLayout);
         mImageViewGroup.addView(zoomIn, zoomInViewLayout);
         mViewGroup.addView(mImageViewGroup);
 
         // set content view
         setContentView(mViewGroup);
 
         // set gps updatehandler
         mUpdateHandler = new GPSUpdateHandler(this);
 
         mDimRunnable = new Runnable() {
             public void run() {
                 WindowManager.LayoutParams params = getWindow().getAttributes();
                 params.screenBrightness = 0.05f;
                 getWindow().setAttributes(params);
 
                 if (mLocationThreadRunner instanceof LocationThreadRunner) {
                     mLocationThreadRunner.setWaitTimeout(LocationThreadRunner.TIMEOUT_LONG);
                 }
             }
         };
 
         // preference handler
         loadPreferences();
     }
 
     private void loadPreferences() {
         // load preference data
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         mPreferenceStandby = prefs.getBoolean("standby", false);
         mPreferenceRotateMap = prefs.getBoolean("rotateMap", false);
         mPreferenceBrightness = prefs.getString("brightness", BRIGHTNESS_NOCHANGE).trim();
         mPreferenceRouteFile = prefs.getString("routeFile", null);
         mPreferenceDistanceUnit = prefs.getString("distance", DISTANCE_MILES);
         mPreferenceDim = prefs.getString("dim", DIM_NEVER);
         mPreferenceMapFile = prefs.getString("mapFile", null);
 
         mPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
             public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                 if (key.equals("standby")) {
                     mPreferenceStandby = prefs.getBoolean("standby", false);
                     handlePreferenceStandby();
                 }
                 else if (key.equals("rotateMap")) {
                     mPreferenceRotateMap = prefs.getBoolean("rotateMap", false);
                     handlePreferenceRotateMap();
                 }
                 else if (key.equals("brightness")) {
                     mPreferenceBrightness = prefs.getString("brightness", BRIGHTNESS_NOCHANGE).trim();
                     handlePreferenceBrightness();
                 }
                 else if (key.equals("routeFile")) {
                     mPreferenceRouteFile = prefs.getString("routeFile", null);
                     loadRouteFile();
                 }
                 else if (key.equals("distance")) {
                     mPreferenceDistanceUnit = prefs.getString("distance", DISTANCE_MILES);
                 }
                 else if (key.equals("dim")) {
                     mPreferenceDim = prefs.getString("dim", DIM_NEVER);
                     handlePreferenceDim();
                 }
                 else if (key.equals("mapFile")) {
                     mPreferenceMapFile = prefs.getString("mapFile", null);
                     loadMapFile();
                 }
             }
         };
         prefs.registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
 
         // initially handle preferences
         handlePreferenceStandby();
         handlePreferenceRotateMap();
         handlePreferenceBrightness();
         handlePreferenceDim();
         loadMapFile();
         loadRouteFile();
     }
 
     private void handlePreferenceStandby() {
         if (true == mPreferenceStandby) {
             getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         }
         else {
             getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         }
     }
 
     private void handlePreferenceRotateMap() {
         if (false == mPreferenceRotateMap) {
             mMapViewGroup.setHeading(0.0f);
         }
     }
 
     private void handlePreferenceBrightness() {
         WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
 
         // default or automatic mode
         if (mPreferenceBrightness.equals(BRIGHTNESS_NOCHANGE)) {
             layoutParams.screenBrightness = -1.0f;
         }
         // manual mode
         else {
             
             if (mPreferenceBrightness.equals(BRIGHTNESS_MAXIMUM)) {
                 layoutParams.screenBrightness = 1.0f;
             }
             else if (mPreferenceBrightness.equals(BRIGHTNESS_MEDIUM)) {
                 layoutParams.screenBrightness = 0.6f;
             }
             else if (mPreferenceBrightness.equals(BRIGHTNESS_LOW)) {
                 layoutParams.screenBrightness = 0.2f;
             }
         }
 
         getWindow().setAttributes(layoutParams);
     }
 
     private void handlePreferenceDim() {
        if (true == mPreferenceStandby && !mPreferenceDim.equals(DIM_NEVER)) {
             mHandler.postDelayed(mDimRunnable, Integer.parseInt(mPreferenceDim) * 1000L);
         }
         else {
             mHandler.removeCallbacks(mDimRunnable);
         }
     }
 
     private void loadMapFile() {
         File cacheDir = getExternalCacheDir();
         File mapFile = new File(cacheDir, mPreferenceMapFile.replace("/", "_") + ".map");
 
         FileOpenResult fileOpenResult = mMapView.setMapFile(mapFile);
         if (!fileOpenResult.isSuccess()) {
             Toast.makeText(this, R.string.failedToOpenMap + " " + fileOpenResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
             SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
             SharedPreferences.Editor editor = prefs.edit();
             editor.putString("mapFile", null);
             editor.commit();
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         handlePreferenceBrightness();
         handlePreferenceDim();
 
         // start gps thread
         this.setCurrentGpsLocation(null);
         mLocationThreadRunner = new LocationThreadRunner(mUpdateHandler);
         mLocationThread = new Thread(mLocationThreadRunner);
         mLocationThread.start();
         this.updateMyLocation();
     }
 
     @Override
     public void onUserInteraction() {
         super.onUserInteraction();
 
         handlePreferenceBrightness();
 
         // reset dim timeout
         mHandler.removeCallbacks(mDimRunnable);
         handlePreferenceDim();
 
         // get fast gps updates
         if (mLocationThreadRunner instanceof LocationThreadRunner) {
             mLocationThreadRunner.setWaitTimeout(LocationThreadRunner.TIMEOUT_LONG);
         }
     }
 
     @Override
     protected void onPause() {
         // remove dim timeout
         mHandler.removeCallbacks(mDimRunnable);
 
         // stop gps update handler
         mLocationThread.interrupt();
 
         super.onPause();
     }
 
     private void loadRouteFile() {
         if (null != mPreferenceRouteFile) {
             File routeFile = new File(mPreferenceRouteFile.trim());
             if (!routeFile.exists()) {
                 Toast.makeText(this, R.string.failedToOpenTrack + " " + mPreferenceRouteFile, Toast.LENGTH_LONG).show();
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
 
     private void setCurrentGpsLocation(Location location) {
         Boolean isLastLocation = false;
         if (location == null) {
             isLastLocation = true;
 
             mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
             mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
             location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 
             setTitle(R.string.waitingForGps);
         }
 
         try {
             mLongitude = location.getLongitude();
             mLatitude = location.getLatitude();
             Message msg = Message.obtain();
             msg.what = GPSUpdateHandler.UPDATE_LOCATION;
             mUpdateHandler.sendMessage(msg);
 
             // rotate map
             if (true == mPreferenceRotateMap && location.hasBearing()) {
                 mMapViewGroup.setHeading(location.getBearing());
             }
 
             // show speed in activity title
             if (false == isLastLocation && true == location.hasSpeed()) {
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
 
                 String speedText = new DecimalFormat("###").format(speed);
                 if (mPreferenceDistanceUnit.equals(DISTANCE_MILES)) {
                     speedText = speedText + " " + getResources().getString(R.string.mph);
                 }
                 else if (mPreferenceDistanceUnit.equals(DISTANCE_KILOMETERS)) {
                     speedText = speedText + " " + getResources().getString(R.string.kmh);
                 }
 
                 setTitle(speedText);
             }
         } catch (NullPointerException e) {
             // don't update location
         }
     }
 
     public void updateMyLocation() {
         GeoPoint point = new GeoPoint(mLatitude, mLongitude);
         MapViewPosition position = mMapView.getMapViewPosition();
         MapPosition newMapPosition = new MapPosition(point, position.getZoomLevel());
         position.setMapPosition(newMapPosition);
     }
 
     /**
      * invoked by the location service when phone's location changes
      */
     public void onLocationChanged(Location newLocation) {
         setCurrentGpsLocation(newLocation);
     }
 
     /**
      * updates the gps location whenever the provider is enabled
      */
     public void onProviderEnabled(String provider) {
         setCurrentGpsLocation(null);
     }
 
     /**
      * updates the gps location whenever the provider is disabled
      */
     public void onProviderDisabled(String provider) {
         setCurrentGpsLocation(null);
     }
 
     /**
      * updates the gps location whenever the provider status changes
      */
     public void onStatusChanged(String provider, int status, Bundle extras) {
         setCurrentGpsLocation(null);
     }
 
     /**
      * render all tracks contained in a gpx file
      *
      * @param routeFile
      */
     private void renderRouteFile(File routeFile) {
         List<Overlay> overlays = mMapView.getOverlays();
         if (!overlays.isEmpty()) {
             overlays.removeAll(overlays);
         }
 
         // create the default paint objects for overlay ways
         Paint wayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
         wayPaint.setStyle(Paint.Style.STROKE);
         wayPaint.setColor(Color.BLUE);
         wayPaint.setAlpha(128);
         wayPaint.setStrokeWidth(10);
         wayPaint.setStrokeJoin(Paint.Join.ROUND);
 
         GPXParser parser = new GPXParser(routeFile);
         List<List<Location>> tracks = parser.getTracks();
 
         ListOverlay listOverlay = new ListOverlay();
         List<OverlayItem> overlayItems = listOverlay.getOverlayItems();
 
         Iterator<List<Location>> trackIterator = tracks.iterator();
         while (trackIterator.hasNext()) {
             List<Location> waypoints = trackIterator.next();
             List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
 
             Iterator<Location> waypointIterator = waypoints.iterator();
 
             while (waypointIterator.hasNext()) {
                 Location waypoint = waypointIterator.next();
 
                 GeoPoint geoPoint = new GeoPoint(waypoint.getLatitude(), waypoint.getLongitude());
                 geoPoints.add(geoPoint);
             }
 
             PolygonalChain way = new PolygonalChain(geoPoints);
             Polyline polyline = new Polyline(way, wayPaint);
             overlayItems.add(polyline);
         }
 
         overlays.add(listOverlay);
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.mainmenu, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_preferences:
                 startActivity(new Intent(getBaseContext(), Preferences.class));
                 return true;
             case R.id.menu_managemaps:
                 startActivity(new Intent(getBaseContext(), ManageMaps.class));
                 return true;
             case R.id.menu_loadfromgpsies:
                 startActivity(new Intent(getBaseContext(), LoadTrack.class));
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 }

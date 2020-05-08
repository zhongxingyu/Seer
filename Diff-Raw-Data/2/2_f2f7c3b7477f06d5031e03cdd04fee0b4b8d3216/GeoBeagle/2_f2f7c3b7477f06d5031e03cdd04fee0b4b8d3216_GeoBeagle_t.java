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
 
 package com.google.code.geobeagle.activity.main;
 
 import com.google.code.geobeagle.CacheType;
 import com.google.code.geobeagle.CompassListener;
 import com.google.code.geobeagle.ErrorDisplayer;
 import com.google.code.geobeagle.Geocache;
 import com.google.code.geobeagle.GeocacheFactory;
 import com.google.code.geobeagle.LocationControlBuffered;
 import com.google.code.geobeagle.LocationControlDi;
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.Refresher;
 import com.google.code.geobeagle.ResourceProvider;
 import com.google.code.geobeagle.GeocacheFactory.Source;
 import com.google.code.geobeagle.activity.cachelist.GeocacheListController;
 import com.google.code.geobeagle.activity.main.intents.GeocacheToCachePage;
 import com.google.code.geobeagle.activity.main.intents.GeocacheToGoogleMap;
 import com.google.code.geobeagle.activity.main.intents.IntentFactory;
 import com.google.code.geobeagle.activity.main.intents.IntentStarterRadar;
 import com.google.code.geobeagle.activity.main.intents.IntentStarterViewUri;
 import com.google.code.geobeagle.activity.main.view.GeocacheViewer;
 import com.google.code.geobeagle.activity.main.view.Misc;
 import com.google.code.geobeagle.activity.main.view.OnCacheButtonClickListenerBuilder;
 import com.google.code.geobeagle.activity.main.view.WebPageAndDetailsButtonEnabler;
 import com.google.code.geobeagle.activity.main.view.GeocacheViewer.AttributeViewer;
 import com.google.code.geobeagle.activity.main.view.GeocacheViewer.LabelledAttributeViewer;
 import com.google.code.geobeagle.activity.main.view.GeocacheViewer.NameViewer;
 import com.google.code.geobeagle.activity.main.view.GeocacheViewer.UnlabelledAttributeViewer;
 import com.google.code.geobeagle.database.Database;
 import com.google.code.geobeagle.database.DatabaseDI;
 import com.google.code.geobeagle.database.LocationSaver;
 import com.google.code.geobeagle.database.DatabaseDI.SQLiteWrapper;
 import com.google.code.geobeagle.location.LocationLifecycleManager;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.hardware.SensorManager;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.net.UrlQuerySanitizer;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import java.io.File;
 
 /*
  * Main Activity for GeoBeagle.
  */
 public class GeoBeagle extends Activity {
     static class NullRefresher implements Refresher {
         public void refresh() {
         }
     }
 
     private static int ACTIVITY_REQUEST_TAKE_PICTURE = 1;
     private CompassListener mCompassListener;
     private final Database mDatabase;
     private final ErrorDisplayer mErrorDisplayer;
     private GeoBeagleDelegate mGeoBeagleDelegate;
     private Geocache mGeocache;
     private GeocacheFactory mGeocacheFactory;
     private GeocacheFromPreferencesFactory mGeocacheFromPreferencesFactory;
     private GeocacheViewer mGeocacheViewer;
     private LocationControlBuffered mLocationControlBuffered;
     private LocationSaver mLocationSaver;
     private RadarView mRadar;
     private final ResourceProvider mResourceProvider;
     private SensorManager mSensorManager;
     private final SQLiteWrapper mSqliteWrapper;
     private WebPageAndDetailsButtonEnabler mWebPageButtonEnabler;
 
     public GeoBeagle() {
         super();
         mErrorDisplayer = new ErrorDisplayer(this);
         mResourceProvider = new ResourceProvider(this);
 
         mSqliteWrapper = new SQLiteWrapper(null);
         mDatabase = DatabaseDI.create(this);
     }
 
     private void getCoordinatesFromIntent(Intent intent) {
         try {
             if (intent.getType() == null) {
                 final String query = intent.getData().getQuery();
                 final CharSequence sanitizedQuery = Util.parseHttpUri(query,
                         new UrlQuerySanitizer(), UrlQuerySanitizer
                                 .getAllButNulAndAngleBracketsLegal());
                 final CharSequence[] latlon = Util.splitLatLonDescription(sanitizedQuery);
                 mGeocache = mGeocacheFactory.create(latlon[2], latlon[3], Util
                         .parseCoordinate(latlon[0]), Util.parseCoordinate(latlon[1]),
                         Source.WEB_URL, null, CacheType.NULL, 0, 0, 0);
                 mSqliteWrapper.openWritableDatabase(mDatabase);
                 mLocationSaver.saveLocation(mGeocache);
                 mSqliteWrapper.close();
                 mGeocacheViewer.set(mGeocache);
             }
         } catch (final Exception e) {
             mErrorDisplayer.displayErrorAndStack(e);
         }
     }
 
     public Geocache getGeocache() {
         return mGeocache;
     }
 
     RadarView getRadar() {
         return mRadar;
     }
 
     private boolean maybeGetCoordinatesFromIntent() {
         final Intent intent = getIntent();
         if (intent != null) {
             final String action = intent.getAction();
             if (action != null) {
                 if (action.equals(Intent.ACTION_VIEW)) {
                     getCoordinatesFromIntent(intent);
                     return true;
                 } else if (action.equals(GeocacheListController.SELECT_CACHE)) {
                     mGeocache = intent.<Geocache> getParcelableExtra("geocache");
                     if (mGeocache == null)
                         mGeocache = new Geocache("", "", 0, 0, Source.MY_LOCATION, "",
                                 CacheType.NULL, 0, 0, 0);
                     mGeocacheViewer.set(mGeocache);
                     mWebPageButtonEnabler.check();
                     return true;
                 }
             }
         }
         return false;
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == ACTIVITY_REQUEST_TAKE_PICTURE) {
             Log.v("GeoBeagle", "camera intent has returned.");
         } else if (resultCode == 0)
             setIntent(data);
     }
 
     private void onCameraStart() {
         String filename = "/sdcard/GeoBeagle/" + mGeocache.getId()
                 + DateFormat.format(" yyyy-MM-dd kk.mm.ss.jpg", System.currentTimeMillis());
         Log.v("GeoBeagle", "capturing image to " + filename);
         Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(filename)));
         startActivityForResult(intent, ACTIVITY_REQUEST_TAKE_PICTURE);
     }
 
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Log.v("GeoBeagle", "GeoBeagle onCreate");
 
         setContentView(R.layout.main);
         mLocationSaver = new LocationSaver(DatabaseDI.createCacheWriter(mSqliteWrapper));
         mWebPageButtonEnabler = Misc.create(this, findViewById(R.id.cache_page),
                 findViewById(R.id.cache_details));
 
         final LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
         mLocationControlBuffered = LocationControlDi.create(locationManager);
         mGeocacheFactory = new GeocacheFactory();
         mGeocacheFromPreferencesFactory = new GeocacheFromPreferencesFactory(mGeocacheFactory);
         final TextView gcid = (TextView)findViewById(R.id.gcid);
         final AttributeViewer gcIcon = new UnlabelledAttributeViewer(
                 GeocacheViewer.CACHE_TYPE_IMAGES, ((ImageView)findViewById(R.id.gcicon)));
         final AttributeViewer gcDifficulty = new LabelledAttributeViewer(
                 GeocacheViewer.STAR_IMAGES, (TextView)findViewById(R.id.gc_text_terrain),
                 (ImageView)findViewById(R.id.gc_difficulty));
         final AttributeViewer gcTerrain = new LabelledAttributeViewer(GeocacheViewer.STAR_IMAGES,
                 (TextView)findViewById(R.id.gc_text_terrain),
                 (ImageView)findViewById(R.id.gc_terrain));
         final UnlabelledAttributeViewer gcContainer = new UnlabelledAttributeViewer(
                 GeocacheViewer.CONTAINER_IMAGES, (ImageView)findViewById(R.id.gccontainer));
         final NameViewer gcName = new NameViewer(((TextView)findViewById(R.id.gcname)));
         mRadar = (RadarView)findViewById(R.id.radarview);
         mRadar.setUseMetric(true);
         mRadar.setDistanceView((TextView)findViewById(R.id.radar_distance),
                 (TextView)findViewById(R.id.radar_bearing),
                 (TextView)findViewById(R.id.radar_accuracy));
         mGeocacheViewer = new GeocacheViewer(mRadar, gcid, gcName, gcIcon, gcDifficulty, gcTerrain,
                 gcContainer);
 
         mLocationControlBuffered.onLocationChanged(null);
         setCacheClickListeners();
 
         // Register for location updates
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, mRadar);
         locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, mRadar);
 
         AppLifecycleManager appLifecycleManager = new AppLifecycleManager(
                 getPreferences(MODE_PRIVATE), new LifecycleManager[] {
                         new LocationLifecycleManager(mLocationControlBuffered, locationManager),
                         new LocationLifecycleManager(mRadar, locationManager)
                 });
         mGeoBeagleDelegate = GeoBeagleDelegate.buildGeoBeagleDelegate(this, appLifecycleManager,
                 mGeocacheViewer, mErrorDisplayer);
         mGeoBeagleDelegate.onCreate();
 
         mCompassListener = new CompassListener(new NullRefresher(), mLocationControlBuffered,
                 -1440f);
         mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
         if (getLastNonConfigurationInstance() != null) {
             setIntent((Intent)getLastNonConfigurationInstance());
         }
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         super.onCreateDialog(id);
         return mGeoBeagleDelegate.onCreateDialog(id);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main_menu, menu);
         return true;
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_CAMERA && event.getRepeatCount() == 0) {
             onCameraStart();
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         return mGeoBeagleDelegate.onOptionsItemSelected(item);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         Log.v("GeoBeagle", "GeoBeagle onPause");
         mGeoBeagleDelegate.onPause();
         mSensorManager.unregisterListener(mCompassListener);
         mSensorManager.unregisterListener(mRadar);
         mSqliteWrapper.close();
     }
 
     /*
      * (non-Javadoc)
      * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
      */
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
         mGeoBeagleDelegate.onRestoreInstanceState(savedInstanceState);
     }
 
     @Override
     protected void onResume() {
         try {
             super.onResume();
             Log.v("GeoBeagle", "GeoBeagle onResume");
 
             mRadar.handleUnknownLocation();
             mGeoBeagleDelegate.onResume();
             mSqliteWrapper.openWritableDatabase(mDatabase);
             mSensorManager.registerListener(mCompassListener, SensorManager.SENSOR_ORIENTATION,
                     SensorManager.SENSOR_DELAY_UI);
             mSensorManager.registerListener(mRadar, SensorManager.SENSOR_ORIENTATION,
                     SensorManager.SENSOR_DELAY_UI);
 
             maybeGetCoordinatesFromIntent();
             // Possible fix for issue 53.
             if (mGeocache == null)
                 mGeocache = new Geocache("", "", 0, 0, Source.MY_LOCATION, "", CacheType.NULL, 0,
                         0, 0);
             mGeocacheViewer.set(mGeocache);
             mWebPageButtonEnabler.check();
         } catch (final Exception e) {
             mErrorDisplayer.displayErrorAndStack(e);
         }
     }
 
     public void onResume(SharedPreferences preferences) {
         Log.v("GeoBeagle", "onResume");
         setGeocache(mGeocacheFromPreferencesFactory.create(preferences));
     }
 
     /*
      * (non-Javadoc)
      * @see android.app.Activity#onRetainNonConfigurationInstance()
      */
     @Override
     public Object onRetainNonConfigurationInstance() {
         return getIntent();
     }
 
     /*
      * (non-Javadoc)
      * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
      */
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         mGeoBeagleDelegate.onSaveInstanceState(outState);
     }
 
     private void setCacheClickListeners() {
         IntentFactory intentFactory = new IntentFactory(new UriParser());
 
         OnCacheButtonClickListenerBuilder cacheClickListenerSetter = new OnCacheButtonClickListenerBuilder(
                 this, mErrorDisplayer);
 
         cacheClickListenerSetter.set(R.id.maps, new IntentStarterViewUri(this, intentFactory,
                 mGeocacheViewer, new GeocacheToGoogleMap(mResourceProvider)), "");
         cacheClickListenerSetter.set(R.id.cache_page, new IntentStarterViewUri(this, intentFactory,
                 mGeocacheViewer, new GeocacheToCachePage(mResourceProvider)), "");
         cacheClickListenerSetter.set(R.id.radarview, new IntentStarterRadar(this),
                 "\nPlease install the Radar application to use Radar.");
         findViewById(R.id.menu_log_find).setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 showDialog(R.id.menu_log_find);
             }
         });
         findViewById(R.id.menu_log_dnf).setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 showDialog(R.id.menu_log_dnf);
             }
         });
     }
 
     void setGeocache(Geocache geocache) {
         mGeocache = geocache;
         mGeocacheViewer.set(getGeocache());
     }
 }

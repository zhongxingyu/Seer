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
 import android.content.SharedPreferences.Editor;
 import android.hardware.SensorManager;
 import android.location.LocationManager;
 import android.net.UrlQuerySanitizer;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 
 /*
  * Main Activity for GeoBeagle.
  */
 public class GeoBeagle extends Activity implements LifecycleManager {
     static class NullRefresher implements Refresher {
         public void refresh() {
         }
     }
 
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
                         Source.WEB_URL, null);
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
                     mGeocacheViewer.set(mGeocache);
                     mWebPageButtonEnabler.check();
                     return true;
                 }
             }
         }
         return false;
     }
 
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onRetainNonConfigurationInstance()
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        return getIntent();
    }

     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (resultCode == 0)
             setIntent(data);
     }
 
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         // try {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         mLocationSaver = new LocationSaver(DatabaseDI.createCacheWriter(mSqliteWrapper));
         mWebPageButtonEnabler = Misc.create(this, findViewById(R.id.cache_page),
                 findViewById(R.id.cache_details));
 
         final LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
         mLocationControlBuffered = LocationControlDi.create(locationManager);
         mGeocacheFactory = new GeocacheFactory();
         mGeocacheFromPreferencesFactory = new GeocacheFromPreferencesFactory(mGeocacheFactory);
         final TextView gcid = (TextView)findViewById(R.id.gcid);
         final TextView gcname = (TextView)findViewById(R.id.gcname);
         mRadar = (RadarView)findViewById(R.id.radarview);
         mRadar.setUseMetric(true);
         // mRadar.startSweep();
         mRadar.setDistanceView((TextView)findViewById(R.id.radar_distance),
                 (TextView)findViewById(R.id.radar_bearing),
                 (TextView)findViewById(R.id.radar_accuracy));
         mGeocacheViewer = new GeocacheViewer(mRadar, gcid, gcname);
 
         mLocationControlBuffered.onLocationChanged(null);
         setCacheClickListeners();
 
         // Register for location updates
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, mRadar);
         locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, mRadar);
 
         AppLifecycleManager appLifecycleManager = new AppLifecycleManager(
                 getPreferences(MODE_PRIVATE), new LifecycleManager[] {
                         this,
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
     public boolean onOptionsItemSelected(MenuItem item) {
         return mGeoBeagleDelegate.onOptionsItemSelected(item);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         mGeoBeagleDelegate.onPause();
         mSensorManager.unregisterListener(mCompassListener);
         mSensorManager.unregisterListener(mRadar);
     }
 
     public void onPause(Editor editor) {
         getGeocache().writeToPrefs(editor);
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
             mRadar.handleUnknownLocation();
             mGeoBeagleDelegate.onResume();
             mSqliteWrapper.openWritableDatabase(mDatabase);
             mSensorManager.registerListener(mCompassListener, SensorManager.SENSOR_ORIENTATION,
                     SensorManager.SENSOR_DELAY_UI);
             mSensorManager.registerListener(mRadar, SensorManager.SENSOR_ORIENTATION,
                     SensorManager.SENSOR_DELAY_UI);
 
             maybeGetCoordinatesFromIntent();
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

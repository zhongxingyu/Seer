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
 
 package com.google.code.geobeagle;
 
 import com.google.code.geobeagle.data.Geocache;
 import com.google.code.geobeagle.data.GeocacheFactory;
 import com.google.code.geobeagle.data.GeocacheFromPreferencesFactory;
 import com.google.code.geobeagle.data.GeocacheFactory.Source;
 import com.google.code.geobeagle.intents.GeocacheToCachePage;
 import com.google.code.geobeagle.intents.GeocacheToGoogleMap;
 import com.google.code.geobeagle.intents.IntentFactory;
 import com.google.code.geobeagle.intents.IntentStarterLocation;
 import com.google.code.geobeagle.intents.IntentStarterRadar;
 import com.google.code.geobeagle.intents.IntentStarterViewUri;
 import com.google.code.geobeagle.io.DatabaseDI;
 import com.google.code.geobeagle.io.LocationSaver;
 import com.google.code.geobeagle.io.DatabaseDI.SQLiteWrapper;
 import com.google.code.geobeagle.ui.ContentSelector;
 import com.google.code.geobeagle.ui.EditCacheActivity;
 import com.google.code.geobeagle.ui.ErrorDisplayer;
 import com.google.code.geobeagle.ui.GeocacheViewer;
 import com.google.code.geobeagle.ui.GpsStatusWidget;
 import com.google.code.geobeagle.ui.Misc;
 import com.google.code.geobeagle.ui.MyLocationProvider;
 import com.google.code.geobeagle.ui.OnCacheButtonClickListenerBuilder;
 import com.google.code.geobeagle.ui.OnContentProviderSelectedListener;
 import com.google.code.geobeagle.ui.UpdateGpsWidgetRunnableDI;
 import com.google.code.geobeagle.ui.WebPageAndDetailsButtonEnabler;
 import com.google.code.geobeagle.ui.GpsStatusWidget.MeterFormatter;
 import com.google.code.geobeagle.ui.GpsStatusWidget.MeterView;
 import com.google.code.geobeagle.ui.GpsStatusWidget.UpdateGpsWidgetRunnable;
 import com.google.code.geobeagle.ui.cachelist.GeocacheListController;
 import com.google.code.geobeagle.ui.cachelist.GeocacheListOnClickListener;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.UrlQuerySanitizer;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /*
  * Main Activity for GeoBeagle.
  */
 public class GeoBeagle extends Activity implements LifecycleManager {
     private ContentSelector mContentSelector;
     private final ErrorDisplayer mErrorDisplayer;
     private GeoBeagleDelegate mGeoBeagleDelegate;
     private Geocache mGeocache;
     private GeocacheFactory mGeocacheFactory;
     private GeocacheFromPreferencesFactory mGeocacheFromPreferencesFactory;
     private GeocacheViewer mGeocacheViewer;
     private GpsStatusWidget mGpsStatusWidget;
     private LocationControlBuffered mLocationControlBuffered;
     private LocationSaver mLocationSaver;
     private final ResourceProvider mResourceProvider;
     private CombinedLocationListener mStatusWidgetLocationListener;
     private UpdateGpsWidgetRunnable mUpdateGpsWidgetRunnable;
     private WebPageAndDetailsButtonEnabler mWebPageButtonEnabler;
 
     public GeoBeagle() {
         super();
         mErrorDisplayer = new ErrorDisplayer(this);
         mResourceProvider = new ResourceProvider(this);
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
                 mLocationSaver.saveLocation(mGeocache);
                 mGeocacheViewer.set(mGeocache);
             }
         } catch (final Exception e) {
            mErrorDisplayer.displayError("Error: " + e.getMessage());
         }
     }
 
     public Geocache getGeocache() {
         return mGeocache;
     }
 
     private TextView getTextView(int id) {
         return (TextView)findViewById(id);
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
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (resultCode == 0)
             setIntent(data);
     }
 
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         try {
             super.onCreate(savedInstanceState);
             setContentView(R.layout.main);
             GeoBeagleBuilder builder = new GeoBeagleBuilder(this);
             mContentSelector = builder.createContentSelector(getPreferences(Activity.MODE_PRIVATE));
            final SQLiteWrapper sqliteWrapper = new SQLiteWrapper(null);
            mLocationSaver = new LocationSaver(DatabaseDI.createCacheWriter(sqliteWrapper));
             mWebPageButtonEnabler = Misc.create(this, findViewById(R.id.cache_page),
                     findViewById(R.id.cache_details));
 
             final LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
             final CombinedLocationManager combinedLocationManager = new CombinedLocationManager(
                     locationManager);
             mGpsStatusWidget = new GpsStatusWidget(mResourceProvider, new MeterView(
                     getTextView(R.id.location_viewer), new MeterFormatter()),
                     getTextView(R.id.provider), getTextView(R.id.lag), getTextView(R.id.accuracy),
                     getTextView(R.id.status), new Misc.Time(), new Location(""),
                     combinedLocationManager);
             mLocationControlBuffered = LocationControlDi.create(locationManager);
             mStatusWidgetLocationListener = new CombinedLocationListener(mLocationControlBuffered,
                     mGpsStatusWidget);
             mGeocacheFactory = new GeocacheFactory();
             mGeocacheFromPreferencesFactory = new GeocacheFromPreferencesFactory(mGeocacheFactory);
             final TextView gcid = (TextView)findViewById(R.id.gcid);
             final TextView gcname = (TextView)findViewById(R.id.gcname);
             final TextView gccoords = (TextView)findViewById(R.id.gccoords);
             mGeocacheViewer = new GeocacheViewer(gcid, gcname, gccoords);
 
             mLocationControlBuffered.onLocationChanged(null);
             setCacheClickListeners();
 
             ((Button)findViewById(R.id.go_to_list))
                     .setOnClickListener(new GeocacheListOnClickListener(this));
 
             AppLifecycleManager appLifecycleManager = new AppLifecycleManager(
                     getPreferences(MODE_PRIVATE),
                     new LifecycleManager[] {
                             this,
                             new LocationLifecycleManager(mStatusWidgetLocationListener,
                                     locationManager),
                             new LocationLifecycleManager(mLocationControlBuffered, locationManager),
                             mContentSelector
                     });
             mGeoBeagleDelegate = GeoBeagleDelegate.buildGeoBeagleDelegate(this,
                     appLifecycleManager, mGeocacheViewer, mErrorDisplayer);
             mGeoBeagleDelegate.onCreate();
 
             ((Spinner)findViewById(R.id.content_provider))
                     .setOnItemSelectedListener(new OnContentProviderSelectedListener(
                             mResourceProvider, (TextView)findViewById(R.id.select_cache_prompt)));
 
             mUpdateGpsWidgetRunnable = UpdateGpsWidgetRunnableDI.create(mGpsStatusWidget);
             mUpdateGpsWidgetRunnable.run();
         } catch (final Exception e) {
             mErrorDisplayer.displayErrorAndStack(e);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(R.string.menu_edit_geocache);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Intent intent = new Intent(this, EditCacheActivity.class);
         intent.putExtra("geocache", mGeocache);
         startActivityForResult(intent, 0);
         return true;
     }
 
     @Override
     public void onPause() {
         super.onPause();
         mGeoBeagleDelegate.onPause();
     }
 
     public void onPause(Editor editor) {
         getGeocache().writeToPrefs(editor);
     }
 
     @Override
     protected void onResume() {
         try {
             super.onResume();
 
             mGeoBeagleDelegate.onResume();
             maybeGetCoordinatesFromIntent();
             mWebPageButtonEnabler.check();
             final Location location = mLocationControlBuffered.getLocation();
             if (location != null)
                 mStatusWidgetLocationListener.onLocationChanged(location);
         } catch (final Exception e) {
             mErrorDisplayer.displayErrorAndStack(e);
         }
     }
 
     public void onResume(SharedPreferences preferences) {
         setGeocache(mGeocacheFromPreferencesFactory.create(preferences));
     }
 
     private void setCacheClickListeners() {
         IntentFactory intentFactory = new IntentFactory(new UriParser());
         Toast getCoordsToast = Toast.makeText(this, R.string.get_coords_toast, Toast.LENGTH_LONG);
         MyLocationProvider myLocationProvider = new MyLocationProvider(mLocationControlBuffered,
                 mErrorDisplayer);
 
         OnCacheButtonClickListenerBuilder cacheClickListenerSetter = new OnCacheButtonClickListenerBuilder(
                 this, mErrorDisplayer);
 
         cacheClickListenerSetter.set(R.id.object_map, new IntentStarterLocation(this,
                 mResourceProvider, intentFactory, myLocationProvider, mContentSelector,
                 R.array.map_objects, getCoordsToast), "");
         cacheClickListenerSetter.set(R.id.nearest_objects, new IntentStarterLocation(this,
                 mResourceProvider, intentFactory, myLocationProvider, mContentSelector,
                 R.array.nearest_objects, getCoordsToast), "");
 
         cacheClickListenerSetter.set(R.id.maps, new IntentStarterViewUri(this, intentFactory,
                 mGeocacheViewer, new GeocacheToGoogleMap(mResourceProvider)), "");
         cacheClickListenerSetter.set(R.id.cache_page, new IntentStarterViewUri(this, intentFactory,
                 mGeocacheViewer, new GeocacheToCachePage(mResourceProvider)), "");
         cacheClickListenerSetter.set(R.id.radar, new IntentStarterRadar(this),
                 "\nPlease install the Radar application to use Radar.");
     }
 
     void setGeocache(Geocache geocache) {
         mGeocache = geocache;
         mGeocacheViewer.set(getGeocache());
     }
 }

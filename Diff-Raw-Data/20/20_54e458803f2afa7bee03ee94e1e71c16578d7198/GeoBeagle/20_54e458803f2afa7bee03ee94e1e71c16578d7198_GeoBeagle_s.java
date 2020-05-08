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
 
 import com.google.code.geobeagle.LocationControl.LocationChooser;
 import com.google.code.geobeagle.data.Geocache;
 import com.google.code.geobeagle.data.di.GeocacheFromTextFactory;
 import com.google.code.geobeagle.intents.GeocacheToCachePage;
 import com.google.code.geobeagle.intents.GeocacheToGoogleMap;
 import com.google.code.geobeagle.intents.IntentFactory;
 import com.google.code.geobeagle.intents.IntentStarterLocation;
 import com.google.code.geobeagle.intents.IntentStarterRadar;
 import com.google.code.geobeagle.intents.IntentStarterViewUri;
 import com.google.code.geobeagle.io.CacheWriter;
 import com.google.code.geobeagle.io.Database;
 import com.google.code.geobeagle.io.LocationSaver;
 import com.google.code.geobeagle.io.di.DatabaseDI;
 import com.google.code.geobeagle.io.di.DatabaseDI.SQLiteWrapper;
 import com.google.code.geobeagle.ui.CacheListDelegate;
 import com.google.code.geobeagle.ui.CachePageButtonEnabler;
 import com.google.code.geobeagle.ui.ContentSelector;
 import com.google.code.geobeagle.ui.EditButtonOnClickListener;
 import com.google.code.geobeagle.ui.ErrorDisplayer;
 import com.google.code.geobeagle.ui.GeocacheListOnClickListener;
 import com.google.code.geobeagle.ui.GetCoordsToast;
 import com.google.code.geobeagle.ui.GeocacheViewer;
 import com.google.code.geobeagle.ui.GpsStatusWidget;
 import com.google.code.geobeagle.ui.MockableTextView;
 import com.google.code.geobeagle.ui.MyLocationProvider;
 import com.google.code.geobeagle.ui.OnCacheButtonClickListenerBuilder;
 import com.google.code.geobeagle.ui.OnContentProviderSelectedListener;
 import com.google.code.geobeagle.ui.GpsStatusWidget.MeterFormatter;
 import com.google.code.geobeagle.ui.GpsStatusWidget.MeterView;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.UrlQuerySanitizer;
 import android.os.Bundle;
 import android.os.Handler;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 /*
  * Main Activity for GeoBeagle.
  */
 public class GeoBeagle extends Activity {
     private CachePageButtonEnabler mCachePageButtonEnabler;
     private ContentSelector mContentSelector;
     private final ErrorDisplayer mErrorDisplayer;
     private GeoBeagleDelegate mGeoBeagleDelegate;
     private LocationControl mGpsControl;
     private final Handler mHandler;
     private GeoBeagleLocationListener mLocationListener;
     private GeocacheViewer mLocationSetter;
     private GpsStatusWidget mLocationViewer;
     private final ResourceProvider mResourceProvider;
 
     private final Runnable mUpdateTimeTask = new Runnable() {
         public void run() {
             mLocationViewer.refreshLocation();
             mHandler.postDelayed(mUpdateTimeTask, 100);
         }
     };
 
     public GeoBeagle() {
         super();
         mErrorDisplayer = new ErrorDisplayer(this);
         mResourceProvider = new ResourceProvider(this);
         mHandler = new Handler();
     }
 
     private MockableTextView createTextView(int id) {
         return new MockableTextView((TextView)findViewById(id));
     }
 
     private void getCoordinatesFromIntent(GeocacheViewer geocacheViewer, Intent intent,
             ErrorDisplayer errorDisplayer) {
         try {
             if (intent.getType() == null) {
                 final String query = intent.getData().getQuery();
                 final CharSequence sanitizedQuery = Util.parseHttpUri(query,
                         new UrlQuerySanitizer(), UrlQuerySanitizer
                                 .getAllButNulAndAngleBracketsLegal());
                 final CharSequence[] latlon = Util.splitLatLonDescription(sanitizedQuery);
                 geocacheViewer.setLocation(Util.parseCoordinate(latlon[0]), Util
                         .parseCoordinate(latlon[1]), latlon[2]);
             }
         } catch (final Exception e) {
             errorDisplayer.displayError("Error: " + e.getMessage());
         }
     }
 
     private boolean maybeGetCoordinatesFromIntent() {
         final Intent intent = getIntent();
        final String action = intent.getAction();
        if (action != null) {
            if (action.equals(Intent.ACTION_VIEW)) {
                getCoordinatesFromIntent(mLocationSetter, intent, mErrorDisplayer);
                return true;
            } else if (action.equals(CacheListDelegate.SELECT_CACHE)) {
                mLocationSetter.set(intent.<Geocache> getParcelableExtra("geocache"));
                mCachePageButtonEnabler.check();
                return true;
             }
         }
         return false;
     }
 
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         try {
             super.onCreate(savedInstanceState);
             setContentView(R.layout.main);
             GeoBeagleBuilder builder = new GeoBeagleBuilder(this);
             mContentSelector = builder.createContentSelector(getPreferences(Activity.MODE_PRIVATE));
 
             final TextView txtLocation = (TextView)findViewById(R.id.go_to);
             mCachePageButtonEnabler = CachePageButtonEnabler.create(txtLocation,
                     findViewById(R.id.cache_page), findViewById(R.id.cache_details),
                     mResourceProvider);
             final ImageButton btnEdit = (ImageButton)findViewById(R.id.edit);
             btnEdit.setOnClickListener(new EditButtonOnClickListener(this, txtLocation));
 
             mLocationViewer = new GpsStatusWidget(mResourceProvider, new MeterView(
                     createTextView(R.id.location_viewer), new MeterFormatter()),
                     createTextView(R.id.provider), createTextView(R.id.lag),
                     createTextView(R.id.accuracy), createTextView(R.id.status),
                     new GpsStatusWidget.Time(), new Location(""));
             final LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
             mGpsControl = new LocationControl(locationManager, new LocationChooser());
             mLocationListener = new GeoBeagleLocationListener(mGpsControl, mLocationViewer);
             final GeocacheFromTextFactory geocacheFromTextFactory = new GeocacheFromTextFactory(
                     mResourceProvider);
             final Database database = DatabaseDI.create(this);
 
             final SQLiteWrapper sqliteWrapper = new SQLiteWrapper(null);
             final CacheWriter cacheWriter = DatabaseDI.createCacheWriter(sqliteWrapper);
             final LocationSaver locationSaver = new LocationSaver(database,
                     geocacheFromTextFactory, mErrorDisplayer, sqliteWrapper, cacheWriter);
             final String initialDestination = getString(R.string.initial_destination);
             mLocationSetter = new GeocacheViewer(this, txtLocation, mGpsControl,
                     geocacheFromTextFactory, initialDestination, mErrorDisplayer, locationSaver);
 
             setCacheClickListeners();
 
             ((Button)findViewById(R.id.go_to_list))
                     .setOnClickListener(new GeocacheListOnClickListener(this));
 
             AppLifecycleManager appLifecycleManager = new AppLifecycleManager(
                     getPreferences(MODE_PRIVATE), new LifecycleManager[] {
                             mLocationSetter,
                             new LocationLifecycleManager(mLocationListener, locationManager),
                             mContentSelector
                     });
             mGeoBeagleDelegate = GeoBeagleDelegate.buildGeoBeagleDelegate(this,
                     appLifecycleManager, mLocationSetter, mErrorDisplayer);
             mGeoBeagleDelegate.onCreate();
 
             ((Spinner)findViewById(R.id.content_provider))
                     .setOnItemSelectedListener(new OnContentProviderSelectedListener(
                             mResourceProvider, new MockableTextView(
                                     (TextView)findViewById(R.id.select_cache_prompt)),
                             new MockableTextView((TextView)findViewById(R.id.cache_prompt))));
 
             mHandler.removeCallbacks(mUpdateTimeTask);
             mHandler.postDelayed(mUpdateTimeTask, 1000);
         } catch (final Exception e) {
             mErrorDisplayer.displayErrorAndStack(e);
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (resultCode == 0)
             setIntent(data);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         mGeoBeagleDelegate.onPause();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         mGeoBeagleDelegate.onResume();
         maybeGetCoordinatesFromIntent();
         mCachePageButtonEnabler.check();
         final Location location = mGpsControl.getLocation();
         if (location != null)
             mLocationListener.onLocationChanged(location);
     }
 
     private void setCacheClickListeners() {
         IntentFactory intentFactory = new IntentFactory(new UriParser());
         GetCoordsToast getCoordsToast = new GetCoordsToast(this);
         MyLocationProvider myLocationProvider = new MyLocationProvider(mGpsControl, mErrorDisplayer);
 
         OnCacheButtonClickListenerBuilder cacheClickListenerSetter = new OnCacheButtonClickListenerBuilder(
                 this, mErrorDisplayer);
 
         cacheClickListenerSetter.set(R.id.object_map, new IntentStarterLocation(this,
                 mResourceProvider, intentFactory, myLocationProvider, mContentSelector,
                 R.array.map_objects, getCoordsToast), "");
         cacheClickListenerSetter.set(R.id.nearest_objects, new IntentStarterLocation(this,
                 mResourceProvider, intentFactory, myLocationProvider, mContentSelector,
                 R.array.nearest_objects, getCoordsToast), "");
 
         cacheClickListenerSetter.set(R.id.maps, new IntentStarterViewUri(this, intentFactory,
                 mLocationSetter, new GeocacheToGoogleMap(mResourceProvider)), "");
         cacheClickListenerSetter.set(R.id.cache_page, new IntentStarterViewUri(this, intentFactory,
                 mLocationSetter, new GeocacheToCachePage(mResourceProvider, mContentSelector)), "");
         cacheClickListenerSetter.set(R.id.radar, new IntentStarterRadar(this, intentFactory,
                 mLocationSetter), "\nPlease install the Radar application to use Radar.");
     }
 
 }

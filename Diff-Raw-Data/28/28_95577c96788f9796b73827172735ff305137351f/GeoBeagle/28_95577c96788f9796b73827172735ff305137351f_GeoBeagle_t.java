 
 package com.google.code.geobeagle;
 
 import com.google.code.geobeagle.intents.DestinationToCachePage;
 import com.google.code.geobeagle.intents.DestinationToGoogleMap;
 import com.google.code.geobeagle.intents.IntentFactory;
 import com.google.code.geobeagle.intents.IntentStarterLocation;
 import com.google.code.geobeagle.intents.IntentStarterRadar;
 import com.google.code.geobeagle.intents.IntentStarterViewUri;
 import com.google.code.geobeagle.ui.CachePageButtonEnabler;
 import com.google.code.geobeagle.ui.DestinationListOnClickListener;
 import com.google.code.geobeagle.ui.ErrorDisplayer;
 import com.google.code.geobeagle.ui.GetCoordsToast;
 import com.google.code.geobeagle.ui.LocationOnKeyListener;
 import com.google.code.geobeagle.ui.LocationSetter;
 import com.google.code.geobeagle.ui.LocationViewer;
 import com.google.code.geobeagle.ui.MockableContext;
 import com.google.code.geobeagle.ui.MockableEditText;
 import com.google.code.geobeagle.ui.MockableTextView;
 import com.google.code.geobeagle.ui.MyLocationProvider;
 import com.google.code.geobeagle.ui.OnCacheButtonClickListenerBuilder;
 import com.google.code.geobeagle.ui.TooString;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.UrlQuerySanitizer;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class GeoBeagle extends Activity {
    public GeoBeagle() {
        super();
        mErrorDisplayer = new ErrorDisplayer(this);
    }

    private final ErrorDisplayer mErrorDisplayer;
     private GpsControl mGpsControl;
     private GpsLocationListener mGpsLocationListener;
     private LifecycleManager mLifecycleManager;
     private LocationSetter mLocationSetter;
     private LocationViewer mLocationViewer;
     
 //    private final byte[] getFileAsBytes(final File file) throws IOException {
 //        final BufferedInputStream bis = new BufferedInputStream( 
 //            new FileInputStream(file));
 //        final byte [] bytes = new byte[(int) file.length()];
 //        bis.read(bytes);
 //        bis.close();
 //        return bytes;
 //    }
     private void getCoordinatesFromIntent(LocationSetter locationSetter, Intent intent,
             ErrorDisplayer errorDisplayer) {
         try {
             if (intent.getType() == null) {
                 final String query = intent.getData().getQuery();
                 final String sanitizedQuery = Util.parseHttpUri(query, new UrlQuerySanitizer(),
                         UrlQuerySanitizer.getAllButNulAndAngleBracketsLegal());
                 final String[] latlon = Util.getLatLonFromQuery(sanitizedQuery);
                 locationSetter.setLocation(Util.parseDecimalDegreesStringToDegrees(latlon[0]), Util
                         .parseDecimalDegreesStringToDegrees(latlon[1]), latlon[2]);
            }
            // else if (intent.getType() != null
 //                    && intent.getType().contentEquals("application/xml-loc")) {
 //                String path = intent.getData().getPath();
 //                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
 //                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
 //                File file = new File(path);
 //                byte b[] = getFileAsBytes(file);
 //                String s = new String(b);
 //                documentBuilder.parse(s);
            // }
         } catch (final Exception e) {
             errorDisplayer.displayError("Error: " + e.getMessage());
         }
     }
 
     private boolean maybeGetCoordinatesFromIntent() {
         final Intent intent = getIntent();
         final String action = intent.getAction();
         if ((action != null) && action.equals(Intent.ACTION_VIEW)) {
             getCoordinatesFromIntent(mLocationSetter, intent, mErrorDisplayer);
             return true;
         }
         return false;
     }
 
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         try {
             super.onCreate(savedInstanceState);
             setContentView(R.layout.main);
 
             final EditText txtLocation = (EditText)findViewById(R.id.go_to);
             final CachePageButtonEnabler cachePageButtonEnabler = new CachePageButtonEnabler(
                     new TooString(txtLocation), findViewById(R.id.cache_page));
             txtLocation.setOnKeyListener(new LocationOnKeyListener(cachePageButtonEnabler));
 
             mLocationViewer = new LocationViewer(new MockableContext(this), new MockableTextView(
                     (TextView)findViewById(R.id.location_viewer)), new MockableTextView(
                     (TextView)findViewById(R.id.last_updated)), new MockableTextView(
                     (TextView)findViewById(R.id.status)));
             mGpsLocationListener = new GpsLocationListener(mLocationViewer);
             mGpsControl = new GpsControl(
                     (LocationManager)getSystemService(Context.LOCATION_SERVICE),
                     mGpsLocationListener);
             mLocationSetter = new LocationSetter(this, new MockableEditText(txtLocation),
                     mGpsControl);
 
             setCacheClickListeners();
             final Button btnGoToList = (Button)findViewById(R.id.go_to_list);
             btnGoToList.setOnClickListener(new DestinationListOnClickListener(mLocationSetter
                     .getDescriptionsAndLocations(), mLocationSetter, new AlertDialog.Builder(this),
                     mErrorDisplayer, cachePageButtonEnabler));
 
             mLifecycleManager = new LifecycleManager(mGpsControl, mLocationSetter,
                     getPreferences(MODE_PRIVATE));
 
         } catch (final Exception e) {
             ((TextView)findViewById(R.id.debug)).setText(e.toString() + "\n"
                     + Util.getStackTrace(e));
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         mLifecycleManager.onPause();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         mLifecycleManager.onResume(mErrorDisplayer, getString(R.string.initial_destination));
         maybeGetCoordinatesFromIntent();
         final Location location = mGpsControl.getLocation();
         if (location != null)
             mGpsLocationListener.onLocationChanged(location);
     }
 
     private void setCacheClickListeners() {
         ResourceProvider resourceProvider = new ResourceProvider(this);
         IntentFactory intentFactory = new IntentFactory(new UriParser());
         GetCoordsToast getCoordsToast = new GetCoordsToast(this);
         MyLocationProvider myLocationProvider = new MyLocationProvider(mGpsControl, mErrorDisplayer);
 
         OnCacheButtonClickListenerBuilder cacheClickListenerSetter = new OnCacheButtonClickListenerBuilder(
                 this, mErrorDisplayer);
         cacheClickListenerSetter.set(R.id.geocaching_map, new IntentStarterLocation(this,
                 resourceProvider, intentFactory, myLocationProvider, R.string.geocaching_maps_url,
                 getCoordsToast), "");
         cacheClickListenerSetter.set(R.id.nearest_caches, new IntentStarterLocation(this,
                 resourceProvider, intentFactory, myLocationProvider, R.string.nearest_caches_url,
                 getCoordsToast), "");
         cacheClickListenerSetter.set(R.id.maps, new IntentStarterViewUri(this, intentFactory,
                 mLocationSetter, new DestinationToGoogleMap(resourceProvider)), "");
         cacheClickListenerSetter.set(R.id.cache_page, new IntentStarterViewUri(this, intentFactory,
                 mLocationSetter, new DestinationToCachePage(resourceProvider)), "");
         cacheClickListenerSetter.set(R.id.radar, new IntentStarterRadar(this, intentFactory,
                 mLocationSetter), "\nPlease install the Radar application to use Radar.");
     }
 }

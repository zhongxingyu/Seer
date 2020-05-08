 package com.onelightstudio.onebike;
 
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.FragmentActivity;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.GestureDetector;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 import com.onelightstudio.onebike.model.Station;
 import com.onelightstudio.onebike.model.StationMarker;
 import com.onelightstudio.onebike.ws.WSDefaultHandler;
 import com.onelightstudio.onebike.ws.WSRequest;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 
 public class MainActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, View.OnClickListener {
 
 
     //----------------------------------------------------------------
     //
     //  INNER CLASS
     //
     //----------------------------------------------------------------
 
 
     private class SearchPanelGestureListener extends GestureDetector.SimpleOnGestureListener {
 
         private final int SWIPE_MIN_DISTANCE = 50;
         private final int SWIPE_THRESHOLD_VELOCITY = 200;
 
         @Override
         public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
             // Detect bottom to top gesture
             if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                 hideSearchView();
                 return true;
             }
             return false;
         }
 
         @Override
         public boolean onSingleTapUp(MotionEvent e) {
             // Detect button click
             hideSearchView();
             return true;
         }
 
         @Override
         public boolean onDown(MotionEvent e) {
             // Returns true to allow fling detection
             return true;
         }
     }
 
 
 
     //----------------------------------------------------------------
     //
     //  CLASS PROPERTIES
     //
     //----------------------------------------------------------------
 
 
     private static final int FIELD_DEPARTURE = 0;
     private static final int FIELD_ARRIVAL = 1;
     private final static String FORCE_CAMERA_POSITION = "ForceCameraPosition";
 
     private GoogleMap map;
     private boolean forceCameraPosition;
     private LocationClient locationClient;
 
     // Global list
     private ArrayList<Station> stations;
 
     // Properties to load stations
     private Handler timer;
     private Runnable timeRunnable;
     private Long pausedTime;
     private int loadStationsTry = 0;
     private boolean loadingStations;
     private AsyncTask displayStationsTask;
 
     // UI items
     private View searchView;
     private View mapView;
     private boolean searchViewVisible;
     private MenuItem actionSearchMenuItem;
     private MenuItem actionClearSearchMenuItem;
     private AutoCompleteTextView departureField;
     private ImageButton departureLocationButton;
     private ProgressBar departureLocationProgress;
     private EditText departureBikesField;
     private AutoCompleteTextView arrivalField;
     private ImageButton arrivalLocationButton;
     private ProgressBar arrivalLocationProgress;
     private EditText arrivalStandsField;
     private Button searchButton;
     private LatLng departureLocation;
     private LatLng arrivalLocation;
 
     // Markers and search mode stuff
     private boolean searchMode;
     private ArrayList<Station> searchModeDepartureStations;
     private ArrayList<Station> searchModeArrivalStations;
     private Station searchModeDepartureStation;
     private Station searchModeArrivalStation;
     private Polyline searchModePolyline;
     private ArrayList<Marker> normalModeCurrentMarkers;
    private HashMap<Marker, LatLngBounds> normalModeClusterBounds;
 
 
     //----------------------------------------------------------------
     //
     //  ACTIVITY LIFECYCLE
     //
     //----------------------------------------------------------------
 
 
     @Override
     public void onAttachedToWindow() {
         super.onAttachedToWindow();
         setUpMapIfNeeded();
     }
 
     @Override
     protected void onCreate(Bundle pSavedInstanceState) {
         super.onCreate(pSavedInstanceState);
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.activity_main);
         getActionBar().setDisplayShowTitleEnabled(false);
 
         stations = new ArrayList<Station>();
 
         //Station loading task
         timeRunnable = new Runnable() {
 
             @Override
             public void run() {
                 Log.e("Refresh Map Tick");
 
                 boolean stationsIsEmpty = false;
                 synchronized (stations) {
                     stationsIsEmpty = stations.isEmpty();
                 }
 
                 if (pausedTime == null) {
                     loadStations(!stationsIsEmpty);
                     timer.postDelayed(this, Constants.MAP_TIMER_REFRESH_IN_MILLISECONDES);
                 }
             }
         };
         timer = new Handler();
 
         // Init view and elements
         searchView = findViewById(R.id.search_view);
         mapView = findViewById(R.id.map_view);
         departureField = (AutoCompleteTextView) findViewById(R.id.departure_field);
         departureLocationButton = (ImageButton) findViewById(R.id.departure_mylocation_button);
         departureLocationProgress = (ProgressBar) findViewById(R.id.departure_mylocation_progress);
         departureBikesField = (EditText) findViewById(R.id.departure_bikes);
         arrivalField = (AutoCompleteTextView) findViewById(R.id.arrival_field);
         arrivalLocationButton = (ImageButton) findViewById(R.id.arrival_mylocation_button);
         arrivalLocationProgress = (ProgressBar) findViewById(R.id.arrival_mylocation_progress);
         arrivalStandsField = (EditText) findViewById(R.id.arrival_stands);
         searchButton = (Button) findViewById(R.id.search_button);
         View hideButton = findViewById(R.id.hide_search_view_button);
 
         final GestureDetector swipeClickDetector = new GestureDetector(new SearchPanelGestureListener());
         hideButton.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 return swipeClickDetector.onTouchEvent(event);
             }
         });
 
         departureField.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                 // Nothing
             }
 
             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                 // Nothing
             }
 
             @Override
             public void afterTextChanged(Editable editable) {
                 if (departureField.getAdapter() == null) {
                     departureField.setAdapter(new AddressAdapter(MainActivity.this, R.layout.list_item));
                 }
             }
         });
 
         departureField.setOnKeyListener(new View.OnKeyListener() {
 
             public boolean onKey(View v, int keyCode, KeyEvent event) {
             // If the event is a key-down event on the "enter" button
             if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                     (keyCode == KeyEvent.KEYCODE_ENTER)) {
                 // Perform action on Enter key press
                 departureField.clearFocus();
                 arrivalField.requestFocus();
                 return true;
             }
             return false;
             }
         });
 
         departureBikesField.setOnKeyListener(new View.OnKeyListener() {
 
             public boolean onKey(View v, int keyCode, KeyEvent event) {
             // If the event is a key-down event on the "enter" button
             if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                     (keyCode == KeyEvent.KEYCODE_ENTER)) {
                 // Perform action on Enter key press
                 departureBikesField.clearFocus();
                 arrivalStandsField.requestFocus();
                 return true;
             }
             return false;
             }
         });
 
         arrivalField.setOnKeyListener(new View.OnKeyListener() {
 
             public boolean onKey(View v, int keyCode, KeyEvent event) {
             // If the event is a key-down event on the "enter" button
             if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                     (keyCode == KeyEvent.KEYCODE_ENTER)) {
                 // Perform action on Enter key press
                 searchModeStartSearch();
                 return true;
             }
             return false;
             }
         });
 
         arrivalStandsField.setOnKeyListener(new View.OnKeyListener() {
 
             public boolean onKey(View v, int keyCode, KeyEvent event) {
             // If the event is a key-down event on the "enter" button
             if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                     (keyCode == KeyEvent.KEYCODE_ENTER)) {
                 // Perform action on Enter key press
                 searchModeStartSearch();
                 return true;
             }
             return false;
             }
         });
 
         arrivalField.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                 // Nothing
             }
 
             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                 // Nothing
             }
 
             @Override
             public void afterTextChanged(Editable editable) {
                 if (arrivalField.getAdapter() == null) {
                     arrivalField.setAdapter(new AddressAdapter(MainActivity.this, R.layout.list_item));
                 }
             }
         });
 
         departureBikesField.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                 // Nothing
             }
 
             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                 // Nothing
             }
 
             @Override
             public void afterTextChanged(Editable editable) {
                 arrivalStandsField.setText(editable.toString());
             }
         });
 
         // Default map markers
         normalModeCurrentMarkers = new ArrayList<Marker>();
 
         // Station request
         loadingStations = false;
 
         if (pSavedInstanceState != null) {
             forceCameraPosition = pSavedInstanceState.getBoolean(FORCE_CAMERA_POSITION);
         } else {
             forceCameraPosition = true;
         }
 
         locationClient = new LocationClient(this, this, this);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         if (!locationClient.isConnected()) {
             locationClient.connect();
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         pausedTime = System.currentTimeMillis();
 
         // Remove any refresh task previously posted
         timer.removeCallbacks(timeRunnable);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         if (pausedTime != null) {
             if ((System.currentTimeMillis() - pausedTime) > Constants.MAP_TIMER_REFRESH_IN_MILLISECONDES) {
                 // Too much time has passed, a refresh is needed
                 loadStations(true, null);
                 timer.postDelayed(timeRunnable, Constants.MAP_TIMER_REFRESH_IN_MILLISECONDES);
             } else {
                 timer.postDelayed(timeRunnable, Constants.MAP_TIMER_REFRESH_IN_MILLISECONDES - (System.currentTimeMillis() - pausedTime));
             }
 
             pausedTime = null;
         }
     }
 
     @Override
     protected void onSaveInstanceState(Bundle pOutState) {
         super.onSaveInstanceState(pOutState);
         pOutState.putBoolean(FORCE_CAMERA_POSITION, false);
     }
 
 
     //----------------------------------------------------------------
     //
     //  LISTENER CALLBACKS
     //
     //----------------------------------------------------------------
 
 
     @Override
     public void onConnected(Bundle bundle) {
         boolean stationsIsEmpty = false;
         synchronized (stations) {
             stationsIsEmpty = stations.isEmpty();
         }
 
         if (stationsIsEmpty) {
             loadStations(false);
         }
 
         animateCameraOnMapUserLocEnable();
     }
 
     @Override
     public void onConnectionFailed(ConnectionResult connectionResult) {
         //Launch full loading
         timer.post(timeRunnable);
     }
 
     @Override
     public void onDisconnected() {
         // Nothing
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.map, menu);
         this.actionSearchMenuItem = menu.findItem(R.id.action_search);
         this.actionClearSearchMenuItem = menu.findItem(R.id.action_clear_search);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_search:
                 toggleSearchViewVisible();
                 return true;
             case R.id.action_info:
                 startActivity(new Intent(this, InfoActivity.class));
                 return true;
             case R.id.action_clear_search:
                 quitSearchMode();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public void onClick(View view) {
         switch (view.getId()) {
             case R.id.departure_mylocation_button:
                 fillAddressFieldWithCurrentLocation(FIELD_DEPARTURE);
                 break;
             case R.id.arrival_mylocation_button:
                 fillAddressFieldWithCurrentLocation(FIELD_ARRIVAL);
                 break;
             case R.id.search_button:
                 searchModeStartSearch();
                 break;
             case R.id.hide_search_view_button:
                 hideSearchView();
                 break;
         }
     }
 
     @Override
     public void onBackPressed() {
         if (searchViewVisible) {
             hideSearchView();
         } else {
             super.onBackPressed();
         }
     }
 
     public LocationClient getLocationClient() {
         return locationClient;
     }
 
 
     //----------------------------------------------------------------
     //
     //  PRIVATE METHODS
     //
     //----------------------------------------------------------------
 
 
     private void setUpMapIfNeeded() {
         // Do a null check to confirm that we have not already instantiated the map.
         if (map == null) {
             map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
             // Check if we were successful in obtaining the map.
             if (map != null) {
                 map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                     @Override
                     public void onCameraChange(CameraPosition cameraPosition) {
                         //Don't display on each move in search mode cause all the informations are displayed at one time
                         if (!searchMode) {
                             displayStations();
                         }
                     }
                 });
                 map.setMyLocationEnabled(true);
                 animateCameraOnMapUserLocEnable();
                 map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                     @Override
                     public boolean onMarkerClick(Marker marker) {
                         if (searchMode) {
                             // Do not update the route if clicked marker is already the start or finish point
                             if (!marker.equals(searchModeDepartureStation.searchMarker) && !marker.equals(searchModeArrivalStation.searchMarker)) {
                                 for (Station station : searchModeDepartureStations) {
                                     if (station.searchMarker.equals(marker)) {
                                         searchModeDepartureStation = station;
                                         break;
                                     }
                                 }
                                 for (Station station : searchModeArrivalStations) {
                                     if (station.searchMarker.equals(marker)) {
                                         searchModeArrivalStation = station;
                                         break;
                                     }
                                 }
 
                                 searchModeDisplayRoute();
                             }
                         } else {
                            LatLngBounds bounds = normalModeClusterBounds.get(marker);
                            if (bounds != null) {
                                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, getResources().getDimensionPixelSize(R.dimen.padding_zoom_cluster)));
                            } else {
                                marker.showInfoWindow();
                            }
                         }
 
                         return true;
                     }
                 });
             } else {
                 // Tell the user to check its google play services
                 Toast.makeText(this, R.string.error_google_play_service, Toast.LENGTH_LONG).show();
             }
         }
     }
 
     private void animateCameraOnMapUserLocEnable() {
         if (map != null && locationClient.isConnected()) {
             if (forceCameraPosition == true) {
                 Location userLocation = locationClient.getLastLocation();
                 if (userLocation != null) {
                     map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), Constants.MAP_DEFAULT_USER_ZOOM), Constants.MAP_ANIMATE_TIME, null);
                 } else {
                     map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Constants.TLS_LAT, Constants.TLS_LNG), Constants.MAP_DEFAULT_NO_LOCATION_ZOOM), Constants.MAP_ANIMATE_TIME, null);
                     if (Util.isOnline(this)) {
                         Toast.makeText(this, R.string.location_not_shared, Toast.LENGTH_LONG).show();
                     }
                 }
             }
         }
     }
 
     /**
      * Call loadStations with no contract.
      * If the stations list is empty, search a contract first, then either call loadStations with the contract or with no contract.
      * @param executeInBackground
      */
     private void loadStations(final boolean executeInBackground) {
         boolean stationsIsEmpty = false;
         synchronized (stations) {
             stationsIsEmpty = stations.isEmpty();
         }
 
         if (stationsIsEmpty) {
             //Looking for a contract
             Location userLocation = locationClient.getLastLocation();
             if (userLocation == null) {
                 // Default contract
                 loadStations(false, Constants.JCD_DEFAULT_CONTRACT_KEY);
             } else {
                 // Retrieve city name
                 WSRequest request = new WSRequest(MainActivity.this, Constants.GOOGLE_API_GEOCODE_URL);
                 request.withParam(Constants.GOOGLE_API_LATLNG, userLocation.getLatitude() + "," + userLocation.getLongitude());
                 request.withParam(Constants.GOOGLE_API_SENSOR, "true");
                 request.handleWith(new WSDefaultHandler(false) {
 
                     @Override
                     public void onResult(Context context, JSONObject result) {
                         String jcdContract = null;
                         String cityName = null;
                         JSONArray addresses = (JSONArray) result.opt("results");
                         if (addresses.length() > 0) {
                             JSONObject address = (JSONObject) addresses.opt(0);
                             JSONArray addressComponents = address.optJSONArray("address_components");
                             if (addressComponents != null) {
                                 localitySearch:
                                 for (int i = 0; i < addressComponents.length(); i++) {
                                     JSONObject component = addressComponents.optJSONObject(i);
                                     if (component != null) {
                                         JSONArray types = component.optJSONArray("types");
                                         if (types != null) {
                                             for (int j = 0; j < types.length(); j++) {
                                                 String type = types.optString(j);
                                                 if ("locality".equals(type)) {
                                                     cityName = component.optString("long_name");
                                                     break localitySearch;
                                                 }
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                         if (cityName != null) {
                             // Retrieve contract covering this city
                             jcdContract = Util.getContractNameForCity(cityName, MainActivity.this);
                         }
                         loadStations(executeInBackground, jcdContract);
                     }
 
                     @Override
                     public void onException(Context context, Exception e) {
                         loadStations(executeInBackground, null);
                     }
 
                     @Override
                     public void onError(Context context, int errorCode) {
                         loadStations(executeInBackground, null);
                     }
 
                 });
                 request.call();
             }
         } else {
             loadStations(executeInBackground, null);
         }
     }
 
     private void loadStations(final boolean executeInBackground, final String contract) {
         loadStationsTry++;
 
         if(contract != null) {
             Log.e(contract);
         }
         if (!loadingStations) {
             loadingStations = true;
             WSRequest request = new WSRequest(this, Constants.JCD_URL);
             request.withParam(Constants.JCD_API_KEY, ((App) getApplication()).getApiKey(Constants.JCD_APP_API_KEY));
             if (contract != null) {
                 request.withParam(Constants.JCD_CONTRACT, contract);
             }
             request.handleWith(new WSDefaultHandler(executeInBackground) {
                 @Override
                 public void onResult(Context context, JSONObject result) {
                     loadStationsTry = 0;
                     loadStationsParseJSONResult(result, executeInBackground, contract != null);
                 }
 
                 @Override
                 public void onException(Context context, Exception e) {
                     loadingStations = false;
                     if (loadStationsTry < 3) {
                         loadStations(executeInBackground, contract);
                     } else {
                         if (!Util.isOnline(MainActivity.this)) {
                             Toast.makeText(MainActivity.this, R.string.internet_not_available, Toast.LENGTH_LONG).show();
                         } else {
                             Toast.makeText(MainActivity.this, R.string.ws_stations_not_availabel, Toast.LENGTH_LONG).show();
                         }
                         loadStationsTry = 0;
                     }
                 }
 
                 @Override
                 public void onError(Context context, int errorCode) {
                     loadingStations = false;
                     if (loadStationsTry < 3) {
                         loadStations(executeInBackground, contract);
                     } else {
                         if (!Util.isOnline(MainActivity.this)) {
                             Toast.makeText(MainActivity.this, R.string.internet_not_available, Toast.LENGTH_LONG).show();
                         } else {
                             Toast.makeText(MainActivity.this, R.string.ws_stations_not_availabel, Toast.LENGTH_LONG).show();
                         }
                         loadStationsTry = 0;
                     }
                 }
             });
             request.call();
         }
     }
 
     private void loadStationsParseJSONResult(final JSONObject result, final boolean inBackground, final boolean relaunchStationLoading) {
         new AsyncTask<Void, Void, Void>() {
             @Override
             protected synchronized Void doInBackground(Void... voids) {
                 if (result != null) {
                     JSONArray stationsJSON = (JSONArray) result.opt("list");
                     synchronized (stations) {
                         stations.clear();
                         for (int i = 0; i < stationsJSON.length(); i++) {
                             Station station = new Station(stationsJSON.optJSONObject(i));
                             stations.add(station);
                         }
                     }
                 }
                 return null;
             }
 
             @Override
             protected void onPreExecute() {
                 if (!inBackground) {
                     setProgressBarIndeterminateVisibility(true);
                 }
             }
 
             @Override
             protected void onPostExecute(Void aVoid) {
                 if (!inBackground) {
                     setProgressBarIndeterminateVisibility(false);
                 }
 
                 if (map != null) {
                     displayStations(relaunchStationLoading);
                     loadingStations = false;
                 }
             }
 
             @Override
             protected void onCancelled() {
                 loadingStations = false;
             }
 
             @Override
             protected void onCancelled(Void aVoid) {
                 loadingStations = false;
             }
         }.execute();
     }
 
     private void displayStations() {
         displayStations(false);
     }
 
     private void displayStations(boolean relaunchStationLoading) {
         if (searchMode) {
             searchModeUpdateStations();
         } else {
             normalModeDisplayStations(relaunchStationLoading);
         }
     }
 
     private void normalModeDisplayStations(final boolean relaunchStationLoading) {
         if (displayStationsTask != null && displayStationsTask.getStatus() != AsyncTask.Status.FINISHED) {
             displayStationsTask.cancel(true);
         }
 
         displayStationsTask = new AsyncTask<Void, Void, HashMap<MarkerOptions, LatLngBounds>>() {
             private LatLngBounds bounds;
             private float zoomLevel;
             private float maxZoomLevel;
 
             @Override
             protected void onPreExecute() {
                 bounds = map.getProjection().getVisibleRegion().latLngBounds;
                 zoomLevel = map.getCameraPosition().zoom;
                 maxZoomLevel = map.getMaxZoomLevel();
             }
 
             @Override
             protected HashMap<MarkerOptions, LatLngBounds> doInBackground(Void... voids) {
                 // Keep the stations in the viewport only
                 ArrayList<Station> stationsInViewport = new ArrayList<Station>();
                 synchronized (stations) {
                     for (Station station : stations) {
                         if (isCancelled()) {
                             return null;
                         }
 
                         if (bounds.contains(station.latLng)) {
                             stationsInViewport.add(station);
                         }
                     }
                 }
 
                 // Create the markers, clustering if needed
                normalModeClusterBounds = new HashMap<Marker, LatLngBounds>();
                 HashMap<MarkerOptions, LatLngBounds> markers = new HashMap<MarkerOptions, LatLngBounds>();
                 ArrayList<Station> unprocessedStations = (ArrayList<Station>) stationsInViewport.clone();
                 for (Station station : stationsInViewport) {
                     if (isCancelled()) {
                         return null;
                     }
 
                     if (unprocessedStations.contains(station)) {
                         unprocessedStations.remove(station);
                         LatLngBounds.Builder clusterBoundsBuilder = new LatLngBounds.Builder().include(station.latLng);
                         int n = 1;
                         for (Iterator<Station> otherIt = unprocessedStations.iterator(); otherIt.hasNext(); ) {
                             if (isCancelled()) {
                                 return null;
                             }
 
                             Station otherStation = otherIt.next();
                             // Source : http://gis.stackexchange.com/questions/7430/google-maps-zoom-level-ratio
                             int maxDistance = (int) (Math.pow(2, maxZoomLevel - zoomLevel) * 2.5);
                             if (Util.getDistanceInMeters(station.latLng, otherStation.latLng) < maxDistance) {
                                 clusterBoundsBuilder.include(otherStation.latLng);
                                 otherIt.remove();
                                 n++;
                             }
                         }
                         if (n > 1) {
                             LatLngBounds bounds = clusterBoundsBuilder.build();
                             markers.put(StationMarker.createCluster(bounds), bounds);
                         } else {
                             markers.put(StationMarker.createMarker(MainActivity.this, station), null);
                         }
                     }
                 }
 
                 return markers;
             }
 
             protected void onPostExecute(HashMap<MarkerOptions, LatLngBounds> markers) {
                 ArrayList<Marker> tmpAddedMarkers = new ArrayList<Marker>();
 
                 for (Map.Entry<MarkerOptions, LatLngBounds> markerEntry : markers.entrySet()) {
                     Marker marker = map.addMarker(markerEntry.getKey());
                     tmpAddedMarkers.add(marker);
 
                     if (markerEntry.getValue() != null) {
                        normalModeClusterBounds.put(marker, markerEntry.getValue());
                     }
                 }
 
                 for (Marker stationMarker : normalModeCurrentMarkers) {
                     stationMarker.remove();
                 }
 
                 normalModeCurrentMarkers = tmpAddedMarkers;
 
                 if (relaunchStationLoading) {
                     timer.post(timeRunnable);
                 }
             }
         }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
     }
 
     private void clearMap() {
         if (map != null) {
             map.clear();
             normalModeCurrentMarkers.clear();
         }
     }
 
     private void toggleSearchViewVisible() {
         if (!searchViewVisible) {
             searchViewVisible = true;
             searchView.setVisibility(View.VISIBLE);
             this.actionSearchMenuItem.setVisible(false);
             this.actionClearSearchMenuItem.setVisible(false);
             mapView.animate().translationY(searchView.getHeight());
         } else {
             hideSearchView();
         }
     }
 
     private void quitSearchMode() {
         searchMode = false;
         actionClearSearchMenuItem.setVisible(false);
         clearMap();
         displayStations();
     }
 
     private void hideSearchView() {
         searchViewVisible = false;
         this.actionSearchMenuItem.setVisible(true);
 
         if (searchMode) {
             this.actionClearSearchMenuItem.setVisible(true);
         }
 
         mapView.animate().translationY(0);
         InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(FragmentActivity.INPUT_METHOD_SERVICE);
         View focus = getCurrentFocus();
         if (inputMethodManager != null && focus != null) {
             inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
         }
     }
 
     private void fillAddressFieldWithCurrentLocation(int field) {
         Location userLocation = locationClient.getLastLocation();
         if (userLocation == null) {
             Toast.makeText(this, R.string.location_unavailable, Toast.LENGTH_LONG).show();
         } else {
             if (field == FIELD_DEPARTURE) {
                 fillAddressFieldCallRequest(userLocation, departureField, departureLocationButton, departureLocationProgress);
             } else {
                 fillAddressFieldCallRequest(userLocation, arrivalField, arrivalLocationButton, arrivalLocationProgress);
             }
         }
     }
 
     private void fillAddressFieldCallRequest(Location userLocation, final AutoCompleteTextView fieldText, final ImageButton locationButton, final ProgressBar locationProgress) {
         locationButton.setVisibility(View.GONE);
         locationProgress.setVisibility(View.VISIBLE);
         fieldText.setAdapter((ArrayAdapter<String>) null);
 
         WSRequest request = new WSRequest(MainActivity.this, Constants.GOOGLE_API_GEOCODE_URL);
         request.withParam(Constants.GOOGLE_API_LATLNG, userLocation.getLatitude() + "," + userLocation.getLongitude());
         request.withParam(Constants.GOOGLE_API_SENSOR, "true");
         request.handleWith(new WSDefaultHandler(false) {
             @Override
             public void onResult(Context context, JSONObject result) {
                 locationButton.setVisibility(View.VISIBLE);
                 locationProgress.setVisibility(View.GONE);
 
                 JSONArray addresses = (JSONArray) result.opt("results");
                 if (addresses.length() > 0) {
                     JSONObject address = (JSONObject) addresses.opt(0);
                     fieldText.setText(address.opt("formatted_address").toString());
                 } else {
                     Toast.makeText(MainActivity.this, R.string.address_not_found, Toast.LENGTH_LONG).show();
                 }
             }
 
             @Override
             public void onError(Context context, int errorCode) {
                 locationButton.setVisibility(View.VISIBLE);
                 locationProgress.setVisibility(View.GONE);
                 Toast.makeText(MainActivity.this, R.string.address_not_found, Toast.LENGTH_LONG).show();
             }
 
             @Override
             public void onException(Context context, Exception e) {
                 locationButton.setVisibility(View.VISIBLE);
                 locationProgress.setVisibility(View.GONE);
                 Toast.makeText(MainActivity.this, R.string.address_not_found, Toast.LENGTH_LONG).show();
             }
         });
         request.call();
     }
 
     private void searchModeStartSearch() {
         boolean doSearch = false;
         searchButton.setEnabled(false);
 
         synchronized (stations) {
             if (departureField.getText().toString().trim().length() == 0) {
                 Toast.makeText(this, R.string.departure_unavailable, Toast.LENGTH_LONG).show();
             } else if (arrivalField.getText().toString().trim().length() == 0) {
                 Toast.makeText(this, R.string.arrival_unavailable, Toast.LENGTH_LONG).show();
             } else if (stations.isEmpty()) {
                 Toast.makeText(this, R.string.stations_not_available, Toast.LENGTH_LONG).show();
             } else {
                 doSearch = true;
             }
         }
 
         if (doSearch) {
             setProgressBarIndeterminateVisibility(true);
 
             departureLocation = null;
             arrivalLocation = null;
             searchModeDepartureStation = null;
             searchModeArrivalStation = null;
             searchModeDepartureStations = null;
             searchModeArrivalStations = null;
 
             // Close keyboard
             InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
             inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 
             searchModeLoadFieldLocation(departureField.getText().toString().trim(), FIELD_DEPARTURE);
             searchModeLoadFieldLocation(arrivalField.getText().toString().trim(), FIELD_ARRIVAL);
         } else {
             searchButton.setEnabled(true);
             setProgressBarIndeterminateVisibility(false);
         }
     }
 
     private void searchModeLoadFieldLocation(String address, final int fieldId) {
         WSRequest request = new WSRequest(MainActivity.this, Constants.GOOGLE_API_GEOCODE_URL);
         request.withParam(Constants.GOOGLE_API_ADDRESS, address);
         request.withParam(Constants.GOOGLE_API_SENSOR, "true");
         request.handleWith(new WSDefaultHandler(true) {
             @Override
             public void onError(Context context, int errorCode) {
                 Toast.makeText(MainActivity.this, R.string.ws_google_search_route_fail, Toast.LENGTH_LONG).show();
                 searchButton.setEnabled(true);
                 setProgressBarIndeterminateVisibility(false);
             }
 
             @Override
             public void onException(Context context, Exception e) {
                 Toast.makeText(MainActivity.this, R.string.ws_google_search_route_fail, Toast.LENGTH_LONG).show();
                 searchButton.setEnabled(true);
                 setProgressBarIndeterminateVisibility(false);
             }
 
             @Override
             public void onResult(Context context, JSONObject result) {
                 JSONArray addressLatLng = (JSONArray) result.opt("results");
                 if (addressLatLng != null && addressLatLng.length() > 0) {
                     JSONObject geometry = addressLatLng.optJSONObject(0).optJSONObject("geometry");
                     if (geometry != null) {
                         JSONObject location = geometry.optJSONObject("location");
                         if (location != null) {
                             double lat = location.optDouble(Constants.GOOGLE_LAT_KEY);
                             double lng = location.optDouble(Constants.GOOGLE_LNG_KEY);
                             if (fieldId == FIELD_DEPARTURE) {
                                 departureLocation = new LatLng(lat, lng);
                             }
                             if (fieldId == FIELD_ARRIVAL) {
                                 arrivalLocation = new LatLng(lat, lng);
                             }
 
                             // This function is called for departure and arrival fields
                             // Once we have the 2 location, the stations and route loading can continue
                             if (departureLocation != null && arrivalLocation != null) {
                                 if (searchModeLoadStationsAndRoute()) {
                                     searchModeDisplayStationsAndRoute();
                                 }
                             }
                         } else {
                             searchButton.setEnabled(true);
                             setProgressBarIndeterminateVisibility(false);
                             if (fieldId == FIELD_DEPARTURE) {
                                 Toast.makeText(MainActivity.this, R.string.arrival_unavailable, Toast.LENGTH_LONG).show();
                             }
                             if (fieldId == FIELD_ARRIVAL) {
                                 Toast.makeText(MainActivity.this, R.string.arrival_unavailable, Toast.LENGTH_LONG).show();
                             }
                         }
                     }
                 }
             }
         });
         request.call();
     }
 
     private boolean searchModeLoadStationsAndRoute() {
         searchModeDepartureStations = getStationsNearLocation(departureLocation, arrivalLocation, Integer.valueOf(departureBikesField.getText().toString()), FIELD_DEPARTURE);
         searchModeArrivalStations = getStationsNearLocation(departureLocation, arrivalLocation, Integer.valueOf(arrivalStandsField.getText().toString()), FIELD_ARRIVAL);
         if (map != null && searchModeDepartureStations.size() > 0 && searchModeArrivalStations.size() > 0) {
             searchMode = true;
             actionClearSearchMenuItem.setVisible(true);
             searchModeDepartureStation = searchModeDepartureStations.get(0);
             searchModeArrivalStation = searchModeArrivalStations.get(0);
         } else {
             Toast.makeText(MainActivity.this, R.string.path_impossible, Toast.LENGTH_LONG).show();
             searchButton.setEnabled(true);
             setProgressBarIndeterminateVisibility(false);
             return false;
         }
 
         return true;
     }
 
     private void searchModeUpdateStations() {
         Log.e("Update the displayed markers");
 
         boolean doASearch = false;
 
         synchronized (stations) {
             loopToUpdateSearchModeStations:
             for (Station updatedStation : stations) {
                 for (Station station : searchModeDepartureStations) {
                     if (station.lng == updatedStation.lng && station.lat == updatedStation.lat) {
                         if (updatedStation.availableBikes < Integer.valueOf(departureBikesField.getText().toString())) {
                             doASearch = true;
                             break loopToUpdateSearchModeStations;
                         } else {
                             station.updateDynamicDatasWithStation(updatedStation);
                         }
                     }
                 }
                 for (Station station : searchModeArrivalStations) {
                     if (station.lng == updatedStation.lng && station.lat == updatedStation.lat) {
                         // If there is not enough stands, do not display it anymore
                         if (updatedStation.availableBikeStands < Integer.valueOf(arrivalStandsField.getText().toString())) {
                             doASearch = true;
                             break loopToUpdateSearchModeStations;
                         } else {
                             station.updateDynamicDatasWithStation(updatedStation);
                         }
 
                     }
                 }
             }
         }
 
         if (doASearch) {
             searchModeStartSearch();
         } else {
             searchModeDisplayStations(false);
         }
     }
 
     private void searchModeDisplayStationsAndRoute() {
         clearMap();
         searchModeDisplayRoute(true);
     }
 
     private void searchModeDisplayRoute() {
         searchModeDisplayRoute(false);
     }
 
     private void searchModeDisplayRoute(final boolean callDisplayStationsAfter) {
         WSRequest request = new WSRequest(MainActivity.this, Constants.GOOGLE_API_DIRECTIONS_URL);
         request.withParam(Constants.GOOGLE_API_ORIGIN, searchModeDepartureStation.lat + "," + searchModeDepartureStation.lng);
         request.withParam(Constants.GOOGLE_API_DESTINATION, searchModeArrivalStation.lat + "," + searchModeArrivalStation.lng);
         request.withParam(Constants.GOOGLE_API_MODE_KEY, Constants.GOOGLE_API_MODE_VALUE);
         request.withParam(Constants.GOOGLE_API_SENSOR, "true");
         request.handleWith(new WSDefaultHandler(true) {
             @Override
             public void onError(Context context, int errorCode) {
                 Toast.makeText(MainActivity.this, R.string.ws_google_search_route_fail, Toast.LENGTH_LONG).show();
                 searchButton.setEnabled(true);
                 setProgressBarIndeterminateVisibility(false);
             }
 
             @Override
             public void onException(Context context, Exception e) {
                 Toast.makeText(MainActivity.this, R.string.ws_google_search_route_fail, Toast.LENGTH_LONG).show();
                 searchButton.setEnabled(true);
                 setProgressBarIndeterminateVisibility(false);
             }
 
             @Override
             public void onResult(Context context, JSONObject result) {
                 if ("OK".equals(result.optString("status"))) {
                     JSONArray routeArray = result.optJSONArray("routes");
                     JSONObject routes = routeArray.optJSONObject(0);
                     JSONObject overviewPolylines = routes.optJSONObject("overview_polyline");
                     String encodedString = overviewPolylines.optString("points");
                     List<LatLng> list = Util.decodePoly(encodedString);
                     // Add the location of the departure and arrival stations
                     list.add(0, searchModeDepartureStation.latLng);
                     list.add(searchModeArrivalStation.latLng);
 
                     PolylineOptions options = new PolylineOptions().addAll(list).width(getResources().getDimensionPixelSize(R.dimen.polyline_width)).color(getResources().getColor(R.color.green)).geodesic(true);
                     if (searchModePolyline != null) {
                         searchModePolyline.remove();
                     }
                     searchModePolyline = map.addPolyline(options);
 
                     // Close view
                     hideSearchView();
 
                     if (callDisplayStationsAfter) {
                         searchModeDisplayStations();
                     }
 
                     searchModeMoveCameraOnSearchItems();
                 } else {
                     Toast.makeText(MainActivity.this, R.string.ws_google_search_route_fail, Toast.LENGTH_LONG).show();
                 }
                 searchButton.setEnabled(true);
                 setProgressBarIndeterminateVisibility(false);
             }
         });
         request.call();
     }
 
     private void searchModeDisplayStations() {
         searchModeDisplayStations(true);
     }
 
     private void searchModeDisplayStations(boolean addDepartureArrivalMarkers) {
         for (Station station : searchModeDepartureStations) {
             if (station.searchMarker != null) {
                 station.searchMarker.remove();
             }
             station.searchMarker = map.addMarker(StationMarker.createMarker(MainActivity.this, station));
         }
         for (Station station : searchModeArrivalStations) {
             if (station.searchMarker != null) {
                 station.searchMarker.remove();
             }
             station.searchMarker = map.addMarker(StationMarker.createMarker(MainActivity.this, station));
         }
 
         if (addDepartureArrivalMarkers) {
             map.addMarker(new MarkerOptions().position(departureLocation).title(getString(R.string.departure)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_departure)));
             map.addMarker(new MarkerOptions().position(arrivalLocation).title(getString(R.string.arrival)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_arrival)));
         }
     }
 
     private void searchModeMoveCameraOnSearchItems() {
         LatLngBounds.Builder bld = new LatLngBounds.Builder();
         for (Station station : searchModeDepartureStations) {
             bld.include(station.latLng);
         }
         for (Station station : searchModeArrivalStations) {
             bld.include(station.latLng);
         }
 
         bld.include(departureLocation);
         bld.include(arrivalLocation);
         LatLngBounds bounds = bld.build();
         map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, getResources().getDimensionPixelSize(R.dimen.padding_zoom_search_result)));
     }
 
     private ArrayList<Station> getStationsNearLocation(LatLng startLocation, LatLng finishLocation, int bikesNumber, int fieldId) {
         int matchingStationNumber = 0;
         LatLng location;
         ArrayList<Station> matchingStations = new ArrayList<Station>();
 
         if (fieldId == FIELD_DEPARTURE) {
             location = startLocation;
         } else {
             location = finishLocation;
         }
 
         long radiusDist = Util.getDistanceInMeters(startLocation, finishLocation) / 2;
         if (radiusDist > Constants.STATION_SEARCH_MAX_RADIUS_IN_METERS) {
             radiusDist = Constants.STATION_SEARCH_MAX_RADIUS_IN_METERS;
         }
         if (radiusDist < Constants.STATION_SEARCH_MIN_RADIUS_IN_METERS) {
             radiusDist = Constants.STATION_SEARCH_MIN_RADIUS_IN_METERS;
         }
 
         Map<Station, Long> distanceStations = new HashMap<Station, Long>();
         // Find all stations distance for a radius
         synchronized (stations) {
             for (Station station : stations) {
                 if (!Double.isNaN(station.lat) && !Double.isNaN(station.lng)) {
                     Long distance = Long.valueOf(Util.getDistanceInMeters(location, station.latLng));
                     if (distance.longValue() <= radiusDist) {
                         if (fieldId == FIELD_DEPARTURE) {
                             if (station.availableBikes >= bikesNumber) {
                                 distanceStations.put(station, distance);
                             }
                         } else {
                             if (station.availableBikeStands >= bikesNumber) {
                                 distanceStations.put(station, distance);
                             }
                         }
                     }
                 }
             }
         }
 
         // Sort station by distance and get the first SEARCH_RESULT_MAX_STATIONS_NUMBER stations
         distanceStations = Util.sortMapByValues(distanceStations);
         for (Map.Entry<Station, Long> entry : distanceStations.entrySet()) {
             if (matchingStationNumber < Constants.SEARCH_RESULT_MAX_STATIONS_NUMBER) {
                 if (!matchingStations.contains(entry.getKey())) {
                     matchingStations.add(entry.getKey());
                     matchingStationNumber++;
                 }
             } else {
                 // Station max number is reached for this location
                 break;
             }
         }
 
         return matchingStations;
     }
 }

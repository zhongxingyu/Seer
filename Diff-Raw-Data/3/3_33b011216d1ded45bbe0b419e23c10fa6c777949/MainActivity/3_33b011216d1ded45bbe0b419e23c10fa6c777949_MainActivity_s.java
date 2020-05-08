 package com.onelightstudio.onebike;
 
 import android.content.Context;
 import android.content.Intent;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Build;
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
 import android.view.ViewTreeObserver;
 import android.view.Window;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ProgressBar;
 import android.widget.TextView;
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
 import com.onelightstudio.onebike.model.Contract;
 import com.onelightstudio.onebike.model.Station;
 import com.onelightstudio.onebike.model.StationMarker;
 import com.onelightstudio.onebike.ws.WSDefaultHandler;
 import com.onelightstudio.onebike.ws.WSRequest;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 
 public class MainActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, View.OnClickListener {
 
     private static final int ANIM_DURATION = 250;
 
 
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
     private static final String FORCE_CAMERA_POSITION = "ForceCameraPosition";
     private static final String TEST_STATION_NAME = "TEST EDOS";
 
     private GoogleMap map;
     private boolean forceCameraPosition;
     private LocationClient locationClient;
     private Geocoder geocoder;
 
     // Global list
     private ArrayList<Station> stations;
     private List<Contract> contracts;
 
     // Properties to load stations
     private Handler timer;
     private Runnable timeRunnable;
     private Long pausedTime;
     private int loadStationsTry = 0;
     private boolean loadingStations;
     private AsyncTask displayStationsTask;
     private Contract currentContract = null;
     private boolean displayingContracts;
 
     // UI items
     private View searchView;
     private View searchInfo;
     private TextView searchInfoDistance;
     private TextView searchInfoDuration;
     private View mapView;
     private boolean searchViewVisible;
     private boolean searchInfoVisible;
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
     private boolean waitLoadStationForLauchSearch;
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
         contracts = new ArrayList<Contract>();
 
         //Station loading task
         timeRunnable = new Runnable() {
 
             @Override
             public void run() {
                 Log.i("Refresh Map Tick");
 
                 boolean stationsIsEmpty = false;
                 synchronized (stations) {
                     stationsIsEmpty = stations.isEmpty();
                 }
 
                 if (pausedTime == null) {
                     loadStationsForCurrentDisplayedLocation(!stationsIsEmpty);
                     timer.postDelayed(this, Constants.MAP_TIMER_REFRESH_IN_MILLISECONDES);
                 }
             }
         };
         timer = new Handler();
 
         geocoder = new Geocoder(this);
 
         // Init view and elements
         searchView = findViewById(R.id.search_view);
         departureField = (AutoCompleteTextView) findViewById(R.id.departure_field);
         departureLocationButton = (ImageButton) findViewById(R.id.departure_mylocation_button);
         departureLocationProgress = (ProgressBar) findViewById(R.id.departure_mylocation_progress);
         departureBikesField = (EditText) findViewById(R.id.departure_bikes);
         arrivalField = (AutoCompleteTextView) findViewById(R.id.arrival_field);
         arrivalLocationButton = (ImageButton) findViewById(R.id.arrival_mylocation_button);
         arrivalLocationProgress = (ProgressBar) findViewById(R.id.arrival_mylocation_progress);
         arrivalStandsField = (EditText) findViewById(R.id.arrival_stands);
         searchButton = (Button) findViewById(R.id.search_button);
         searchInfo = findViewById(R.id.search_info);
         searchInfoDistance = (TextView) findViewById(R.id.search_info_distance_text);
         searchInfoDuration = (TextView) findViewById(R.id.search_info_duration_text);
         View hideButton = findViewById(R.id.hide_search_view_button);
 
         final View content = findViewById(android.R.id.content);
         content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
             @Override
             public void onGlobalLayout() {
                 searchView.setY(-searchView.getHeight());
                 searchView.setVisibility(View.VISIBLE);
                 content.getViewTreeObserver().removeGlobalOnLayoutListener(this);
             }
         });
 
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
                 if (!editable.toString().isEmpty()) {
                     if(Integer.valueOf(editable.toString()) == 0) {
                         departureBikesField.setText("1");
                         arrivalStandsField.setText("1");
                     } else {
                         arrivalStandsField.setText(editable.toString());
                     }
                 }
             }
         });
 
         arrivalStandsField.addTextChangedListener(new TextWatcher() {
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
                 if (!editable.toString().isEmpty() && Integer.valueOf(editable.toString()) == 0) {
                     arrivalStandsField.setText("1");
                 }
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
 
         timer.postDelayed(timeRunnable, Constants.MAP_TIMER_REFRESH_IN_MILLISECONDES);
 
         AppRater.appLaunched(this);
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
                 loadStationsForCurrentDisplayedLocation(true);
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
 
         // Move to user location
         boolean animated = animateCameraOnMapUserLocEnable(new GoogleMap.CancelableCallback() {
             @Override
             public void onFinish() {
                 loadStationsForCurrentDisplayedLocation(false);
             }
 
             @Override
             public void onCancel() {
                 loadStationsForCurrentDisplayedLocation(false);
             }
         });
         if (!animated) {
             loadStationsForCurrentDisplayedLocation(false);
         }
 
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
             if(searchMode){
                 quitSearchMode();
             } else {
                 super.onBackPressed();
             }
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
                 map.getUiSettings().setZoomControlsEnabled(false);
                 map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                     @Override
                     public void onCameraChange(CameraPosition cameraPosition) {
                         //Don't display on each move in search mode cause all the informations are displayed at one time
                         if (!searchMode) {
                             // If the new location is no longer in the current displayed contract, search for the new one
                             if (currentContract == null
                                     || map.getCameraPosition().zoom < Constants.MAP_DEFAULT_ZOOM_TO_DISPLAY_CONTRACTS
                                     || Util.getDistanceInMeters(cameraPosition.target, currentContract.getCenter()) > currentContract.getRadius()) {
                                 loadStationsForCurrentDisplayedLocation(false);
                             } else {
                                 displayStations();
                             }
                         }
                     }
                 });
                 map.setMyLocationEnabled(true);
                 animateCameraOnMapUserLocEnable(null);
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
                                 // If the marker is a contracts marker, zoom to min zoom for display station center by the marker position
                                 if(map.getCameraPosition().zoom < Constants.MAP_DEFAULT_ZOOM_TO_DISPLAY_CONTRACTS){
                                     map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), Double.valueOf(Constants.MAP_DEFAULT_ZOOM_TO_DISPLAY_CONTRACTS).floatValue()));
                                 } else {
                                     map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, getResources().getDimensionPixelSize(R.dimen.padding_zoom_cluster)));
                                 }
                             } else {
                                 if(map.getCameraPosition().zoom < Constants.MAP_DEFAULT_ZOOM_TO_DISPLAY_CONTRACTS){
                                     map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), Double.valueOf(Constants.MAP_DEFAULT_ZOOM_TO_DISPLAY_CONTRACTS).floatValue()));
                                 } else {
                                     marker.showInfoWindow();
                                 }
                             }
                         }
 
                         return true;
                     }
                 });
             } else {
                 // Tell the user to check its google play services
                 Toast.makeText(this, R.string.error_google_play_service, Toast.LENGTH_LONG).show();
                 finish();
             }
         }
     }
 
     private boolean animateCameraOnMapUserLocEnable(GoogleMap.CancelableCallback callback) {
         boolean launchedAnimation = false;
         if (map != null && locationClient.isConnected()) {
             if (forceCameraPosition) {
                 Location userLocation = locationClient.getLastLocation();
                 if (userLocation != null) {
                     map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), Constants.MAP_DEFAULT_USER_ZOOM), Constants.MAP_ANIMATE_TIME, callback);
                 } else {
                     map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Constants.TLS_LAT, Constants.TLS_LNG), Constants.MAP_DEFAULT_NO_LOCATION_ZOOM), Constants.MAP_ANIMATE_TIME, callback);
                     if (Util.isOnline(this)) {
                         Toast.makeText(this, R.string.location_not_shared, Toast.LENGTH_LONG).show();
                     }
                 }
                 launchedAnimation = true;
             }
         }
         return launchedAnimation;
     }
 
     /**
      * Call loadStations with no contract.
      * If the stations list is empty, search a contract first, then either call loadStations with the contract or with no contract.
      * @param executeInBackground set the execution type
      */
       private void loadStationsForCurrentDisplayedLocation(final boolean executeInBackground) {
 
         if (map != null) {
             float zoomLevel = map.getCameraPosition().zoom;
             if (zoomLevel < Constants.MAP_DEFAULT_ZOOM_TO_DISPLAY_CONTRACTS) {
                 // Load contracts !
                 if (contracts.isEmpty()) {
                     contracts = Util.getContracts(this);
                 }
                 currentContract = null;
                 if (!displayingContracts) {
                     displayContracts();
                 }
 
             } else {
                 // Get viewport center to find current contract
                 LatLng centerCoordinates = map.getCameraPosition().target;
 
                 Contract tmpContract = Util.getContractForLocation(centerCoordinates, this);
                 if(tmpContract != null) {
                     currentContract = tmpContract;
                 }
                 if (currentContract == null) {
                     Log.d("Could not find contract for location " + centerCoordinates.latitude + ", " + centerCoordinates.longitude);
                 } else {
                     loadStationsForContract(executeInBackground, currentContract);
                 }
             }
         }
     }
 
     private void loadStationsForContract(final boolean executeInBackground, final Contract contract) {
         loadStationsTry++;
 
         if (!loadingStations) {
             loadingStations = true;
 
             String url = contract.getUrl();
             if (contract.getProvider() == Contract.Provider.JCDECAUX) {
                 url += "&" + Constants.JCD_API_KEY + "=" + ((App) getApplication()).getApiKey(Constants.JCD_APP_API_KEY);
             }
             WSRequest request = new WSRequest(this, url);
             request.handleWith(new WSDefaultHandler(executeInBackground) {
                 @Override
                 public void onResult(Context context, JSONObject result) {
                     loadStationsTry = 0;
                     loadStationsParseJSONResult(result, executeInBackground, contract);
                 }
 
                 @Override
                 public void onException(Context context, Exception e) {
                     loadingStations = false;
                     if (loadStationsTry < 3) {
                         loadStationsForContract(executeInBackground, contract);
                     } else {
                         if (!Util.isOnline(MainActivity.this)) {
                             Toast.makeText(MainActivity.this, R.string.internet_not_available, Toast.LENGTH_LONG).show();
                         } else {
                             Toast.makeText(MainActivity.this, R.string.ws_stations_not_availabel, Toast.LENGTH_LONG).show();
                         }
                         loadStationsTry = 0;
                         waitLoadStationForLauchSearch = false;
                     }
                 }
 
                 @Override
                 public void onError(Context context, int errorCode) {
                     loadingStations = false;
                     if (loadStationsTry < 3) {
                         loadStationsForContract(executeInBackground, contract);
                     } else {
                         if (!Util.isOnline(MainActivity.this)) {
                             Toast.makeText(MainActivity.this, R.string.internet_not_available, Toast.LENGTH_LONG).show();
                         } else {
                             Toast.makeText(MainActivity.this, R.string.ws_stations_not_availabel, Toast.LENGTH_LONG).show();
                         }
                         loadStationsTry = 0;
                         waitLoadStationForLauchSearch = false;
                     }
                 }
             });
             request.call();
         }
     }
 
     private void loadStationsParseJSONResult(final JSONObject result, final boolean inBackground, final Contract contract) {
         new AsyncTask<Void, Void, Void>() {
             @Override
             protected synchronized Void doInBackground(Void... voids) {
                 if (result != null) {
                     JSONArray stationsJSON = (JSONArray) result.opt("list");
                     synchronized (stations) {
                         stations.clear();
                         for (int i = 0; i < stationsJSON.length(); i++) {
                             Station station = new Station(stationsJSON.optJSONObject(i), contract.getProvider());
                             if ((station.lat != 0 || station.lng != 0) && !station.name.equals(TEST_STATION_NAME)) {
                                 stations.add(station);
                             }
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
                     loadingStations = false;
 
                     if(waitLoadStationForLauchSearch){
                         if (searchModeLoadStationsAndRoute()) {
                             searchModeDisplayStationsAndRoute();
                         }
                         waitLoadStationForLauchSearch = false;
                     } else {
                         displayStations();
                     }
 
                 }
             }
 
             @Override
             protected void onCancelled() {
                 loadingStations = false;
                 waitLoadStationForLauchSearch = false;
             }
 
             @Override
             protected void onCancelled(Void aVoid) {
                 loadingStations = false;
                 waitLoadStationForLauchSearch = false;
             }
         }.execute();
     }
 
 
 
     private void displayStations() {
         displayingContracts = false;
         if (searchMode) {
             searchModeUpdateStations();
         } else {
             normalModeDisplayStations();
         }
     }
 
     private void displayContracts() {
         ArrayList<Marker> tmpAddedMarkers = new ArrayList<Marker>();
         normalModeClusterBounds = new HashMap<Marker, LatLngBounds>();
         for (Contract contract : contracts) {
             MarkerOptions mo = new MarkerOptions().position(contract.getCenter()).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_station_cluster)).anchor(StationMarker.MARKER_ANCHOR_U, StationMarker.MARKER_ANCHOR_V);
             Marker marker = map.addMarker(mo);
             tmpAddedMarkers.add(marker);
             normalModeClusterBounds.put(marker, null);
         }
         for (Marker marker : normalModeCurrentMarkers) {
             marker.remove();
         }
         displayingContracts = true;
         normalModeCurrentMarkers = tmpAddedMarkers;
     }
 
     private void normalModeDisplayStations() {
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
             this.actionSearchMenuItem.setVisible(false);
             this.actionClearSearchMenuItem.setVisible(false);
             if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                 searchView.setY(0);
             } else {
                 searchView.animate().translationYBy(searchView.getHeight()).setDuration(ANIM_DURATION);
             }
         } else {
             hideSearchView();
         }
     }
 
     private void quitSearchMode() {
         searchMode = false;
 
         //Reset fields
         departureField.setText("");
         arrivalField.setText("");
         departureBikesField.setText("1");
         arrivalStandsField.setText("1");
 
         actionClearSearchMenuItem.setVisible(false);
         setSearchInfoVisible(false);
         clearMap();
        displayStations();
     }
 
     private void hideSearchView() {
         if(searchViewVisible){
             searchViewVisible = false;
             this.actionSearchMenuItem.setVisible(true);
 
             if (searchMode) {
                 this.actionClearSearchMenuItem.setVisible(true);
             }
 
             if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                 searchView.setY(-searchView.getHeight());
             } else {
                 searchView.animate().translationYBy(-searchView.getHeight()).setDuration(ANIM_DURATION);
             }
             InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(FragmentActivity.INPUT_METHOD_SERVICE);
             View focus = getCurrentFocus();
             if (inputMethodManager != null && focus != null) {
                 inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
             }
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
 
         if (Geocoder.isPresent()) {
             try {
                 List<Address> addresses = geocoder.getFromLocation(userLocation.getLatitude(), userLocation.getLongitude(), 1);
                 if (addresses != null && !addresses.isEmpty()) {
                     locationButton.setVisibility(View.VISIBLE);
                     locationProgress.setVisibility(View.GONE);
                     Address address = addresses.get(0);
                     fieldText.setText(String.format(
                             "%s, %s, %s",
                             // If there's a street address, add it
                             address.getMaxAddressLineIndex() > 0 ?
                                     address.getAddressLine(0) : "",
                             // Locality is usually a city
                             address.getLocality(),
                             // The country of the address
                             address.getCountryName()));
                 } else {
                     locationButton.setVisibility(View.VISIBLE);
                     locationProgress.setVisibility(View.GONE);
                     Toast.makeText(MainActivity.this, R.string.address_not_found, Toast.LENGTH_LONG).show();
                 }
             } catch (IOException e) {
                 Log.e("Could not retrieve user address", e);
                 fillAddressFieldWithGoogleWebservices(userLocation, fieldText, locationButton, locationProgress);
             }
         } else {
             Log.i("Geocoder not present, falling back to Google webservices");
             fillAddressFieldWithGoogleWebservices(userLocation, fieldText, locationButton, locationProgress);
         }
     }
 
     private void fillAddressFieldWithGoogleWebservices(Location userLocation, final AutoCompleteTextView fieldText, final ImageButton locationButton, final ProgressBar locationProgress) {
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
         if (Geocoder.isPresent()) {
             try {
                 List<Address> addresses = geocoder.getFromLocationName(address, 1);
                 if (addresses != null && !addresses.isEmpty()) {
                     Address addr = addresses.get(0);
                     double lat = addr.getLatitude();
                     double lng = addr.getLongitude();
                     if (fieldId == FIELD_DEPARTURE) {
                         departureLocation = new LatLng(lat, lng);
                     }
                     if (fieldId == FIELD_ARRIVAL) {
                         arrivalLocation = new LatLng(lat, lng);
                     }
 
                     // This function is called for departure and arrival fields
                     // Once we have the 2 location, the stations and route loading can continue
                     if (departureLocation != null && arrivalLocation != null) {
                         //search the contract for the locations
                         Contract departureContract = Util.getContractForLocation(departureLocation, MainActivity.this);
                         Contract arrivalContract = Util.getContractForLocation(arrivalLocation, MainActivity.this);
 
                         if(departureContract != null && arrivalContract != null){
                             if(departureContract.getName().equals(arrivalContract.getName())){
                                 if(currentContract != null && departureContract.getName().equals(currentContract.getName())){
                                     if (searchModeLoadStationsAndRoute()) {
                                         searchModeDisplayStationsAndRoute();
                                     }
                                 } else {
                                     currentContract = departureContract;
                                     waitLoadStationForLauchSearch = true;
                                     loadStationsForContract(false, currentContract);
                                 }
                             } else {
                                 searchButton.setEnabled(true);
                                 setProgressBarIndeterminateVisibility(false);
                                 Toast.makeText(MainActivity.this, R.string.serach_not_on_same_contract, Toast.LENGTH_LONG).show();
                             }
                         } else {
                             searchButton.setEnabled(true);
                             setProgressBarIndeterminateVisibility(false);
                             Toast.makeText(MainActivity.this, R.string.unsupported_contract, Toast.LENGTH_LONG).show();
                         }
                     }
                 } else {
                     searchButton.setEnabled(true);
                     setProgressBarIndeterminateVisibility(false);
                 }
             } catch (IOException e) {
                 Log.e("Could not retrieve user address", e);
                 searchModeLoadFieldLocationWithGoogleWebservices(address, fieldId);
             }
         } else {
             Log.i("Geocoder not present, falling back to Google webservices");
             searchModeLoadFieldLocationWithGoogleWebservices(address, fieldId);
         }
     }
 
     private void searchModeLoadFieldLocationWithGoogleWebservices(String address, final int fieldId) {
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
 
                                 //search the contract for the locations
                                 Contract departureContract = Util.getContractForLocation(departureLocation, MainActivity.this);
                                 Contract arrivalContract = Util.getContractForLocation(arrivalLocation, MainActivity.this);
 
                                 if(departureContract != null && arrivalContract != null){
                                     if(departureContract.getName().equals(arrivalContract.getName())){
                                         if(currentContract != null && departureContract.getName().equals(currentContract.getName())){
                                             if (searchModeLoadStationsAndRoute()) {
                                                 searchModeDisplayStationsAndRoute();
                                             }
                                         } else {
                                             currentContract = departureContract;
                                             waitLoadStationForLauchSearch = true;
                                             loadStationsForContract(false, currentContract);
                                         }
                                     } else {
                                         searchButton.setEnabled(true);
                                         setProgressBarIndeterminateVisibility(false);
                                         Toast.makeText(MainActivity.this, R.string.serach_not_on_same_contract, Toast.LENGTH_LONG).show();
                                     }
                                 } else {
                                     searchButton.setEnabled(true);
                                     setProgressBarIndeterminateVisibility(false);
                                     Toast.makeText(MainActivity.this, R.string.unsupported_contract, Toast.LENGTH_LONG).show();
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
         Log.i("Update the displayed markers");
 
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
                             station.updateDynamicDataWithStation(updatedStation);
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
                             station.updateDynamicDataWithStation(updatedStation);
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
                     JSONObject route = routeArray.optJSONObject(0);
                     JSONArray legs = route.optJSONArray("legs");
                     JSONObject leg = legs.optJSONObject(0);
                     JSONObject overviewPolylines = route.optJSONObject("overview_polyline");
                     String encodedString = overviewPolylines.optString("points");
                     String distance = leg.optJSONObject("distance").optString("text");
                     long seconds = leg.optJSONObject("duration").optLong("value") / 2;  // we've asked for a walking route
                     long hours = TimeUnit.SECONDS.toHours(seconds);
                     long minutes = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.HOURS.toMinutes(hours);
                     String duration = "";
                     if (hours > 1) {
                         duration += hours + " " + getString(R.string.hours);
                     } else if (hours > 0) {
                         duration += hours + " " + getString(R.string.hour);
                     }
                     if (minutes > 0) {
                         if (!duration.isEmpty()) {
                             duration += " ";
                         }
                         if (minutes > 1) {
                             duration += minutes + " " + getString(R.string.minutes);
                         } else {
                             duration += minutes + " " + getString(R.string.minute);
                         }
                     }
 
                     searchInfoDuration.setText(duration);
                     searchInfoDistance.setText(distance);
                     setSearchInfoVisible(true);
 
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
                 station.clearMarker();
             }
 
             station.searchMarker = map.addMarker(StationMarker.createMarker(MainActivity.this, station));
         }
         for (Station station : searchModeArrivalStations) {
             if (station.searchMarker != null) {
                 station.clearMarker();
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
 
     private void setSearchInfoVisible(boolean setVisible) {
         if (setVisible && !searchInfoVisible) {
             if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                 searchInfo.setTranslationY(1 - searchInfo.getHeight());
             } else {
                 searchInfo.animate().translationYBy(1 - searchInfo.getHeight()).setDuration(ANIM_DURATION);
             }
         } else if (!setVisible && searchInfoVisible) {
             if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                 searchInfo.setTranslationY(searchInfo.getHeight() - 1);
             } else {
                 searchInfo.animate().translationYBy(searchInfo.getHeight() - 1).setDuration(ANIM_DURATION);
             }
         }
         searchInfoVisible = setVisible;
     }
 }

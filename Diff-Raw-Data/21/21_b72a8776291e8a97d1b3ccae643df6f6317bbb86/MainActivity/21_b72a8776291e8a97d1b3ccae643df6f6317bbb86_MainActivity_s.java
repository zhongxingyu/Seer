 package com.onelightstudio.velibnroses;
 
 import android.content.Context;
 import android.location.Location;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.FragmentActivity;
 import android.view.Window;
 import android.widget.Toast;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.onelightstudio.velibnroses.model.Station;
 import com.onelightstudio.velibnroses.ws.WSDefaultHandler;
 import com.onelightstudio.velibnroses.ws.WSRequest;
 import com.onelightstudio.velibnroses.ws.WSSilentHandler;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import java.security.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 
 public class MainActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
 
     private final static String FORCE_CAMERA_POSITION = "ForceCameraPosition";
 
     private GoogleMap mMap;
     private boolean mForceCameraPosition;
     private LocationClient mLocationClient;
     private ArrayList<Station> stations;
     private boolean mStationsRequestSended;
     private Handler timer;
     private Runnable timeRunnable;
     private Long pausedTime;
 
     @Override
     public void onAttachedToWindow() {
         super.onAttachedToWindow();
 
         setUpMapIfNeeded();
     }
 
     @Override
     public void onConnected(Bundle bundle) {
         if (mForceCameraPosition == true) {
             Location userLocation = mLocationClient.getLastLocation();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), Constants.MAP_DEFAULT_USER_ZOOM), Constants.MAP_ANIMATE_TIME, null);
         }
     }
 
     @Override
     public void onDisconnected() {
         //TODO
     }
 
     @Override
     public void onConnectionFailed(ConnectionResult connectionResult) {
         //TODO
     }
 
     @Override
     protected void onCreate(Bundle pSavedInstanceState) {
         super.onCreate(pSavedInstanceState);
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.activity_main);
 
         //Start timer
         timeRunnable = new Runnable() {
 
             @Override
             public void run() {
                Log.d(this.getClass().getName(), "Refresh Map Tick");
                 if (pausedTime == null) {
                     setMapStationsOnTick();
 
                     timer.postDelayed(this, Constants.MAP_TIMER_REFRESH_IN_MILLISECONDES);
                 }
             }
         };
         timer = new Handler();
         timer.post(timeRunnable);
 
         mStationsRequestSended = false;
 
         if (pSavedInstanceState != null) {
             mForceCameraPosition = pSavedInstanceState.getBoolean(FORCE_CAMERA_POSITION);
         } else {
             mForceCameraPosition = true;
         }
 
         mLocationClient = new LocationClient(this, this, this);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
 
         if (!mLocationClient.isConnected()) {
             mLocationClient.connect();
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
 
         pausedTime = System.currentTimeMillis();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         if (pausedTime != null) {
             if ((System.currentTimeMillis() - pausedTime) > Constants.MAP_TIMER_REFRESH_IN_MILLISECONDES) {
                 //Too much time has passed, a refresh is needed
                 setMapStationsOnTick();
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
 
     private void setUpMapIfNeeded() {
         // Do a null check to confirm that we have not already instantiated the map.
         if (mMap == null) {
             mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
             // Check if we were successful in obtaining the map.
             if (mMap != null) {
                 mMap.setMyLocationEnabled(true);
                 mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                     @Override
                     public void onCameraChange(CameraPosition cameraPosition) {
                    setMapStations(false);
                     }
                 });
 
                 if (mForceCameraPosition) {
                     mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(Constants.MAP_DEFAULT_LAT), Constants.MAP_DEFAULT_LNG), Constants.MAP_DEFAULT_ZOOM));
                 }
             } else {
                 //Tell the user to check its google play services
                 Toast.makeText(this, R.string.error_google_play_service, Toast.LENGTH_LONG).show();
             }
         }
     }
 
     private void setMapStationsOnTick() {
         if (stations != null) {
            Log.d(this.getClass().getName(), "Call stations WS");
             stations = null;
             setMapStations();
         }
     }
 
     private void setMapStationsRequest(boolean pDoInBackbround) {
         if (mStationsRequestSended == false) {
             mStationsRequestSended = true;
             WSRequest request = new WSRequest(this, Constants.JCD_URL);
             request.withParam(Constants.JCD_API_KEY, ((App) getApplication()).getApiKey(Constants.JCD_APP_API_KEY));
             if (pDoInBackbround == true) {
                 request.handleWith(new WSSilentHandler() {
                     @Override
                     public void onResult(Context context, JSONObject result) {
                         setMapStationsResult(result);
                     }
                 });
             } else {
                 request.handleWith(new WSDefaultHandler() {
                     @Override
                     public void onResult(Context context, JSONObject result) {
                         setMapStationsResult(result);
                     }
                 });
             }
 
             request.call();
         }
     }
 
     private void setMapStationsResult(JSONObject result) {
         JSONArray stationsJSON = (JSONArray) result.opt("list");
 
        Log.i(this.getClass().getName(), "Stations received : " + stationsJSON.length() + " stations");
 
         stations = new ArrayList<Station>();
         for (int i = 0; i < stationsJSON.length(); i++) {
             stations.add(new Station(stationsJSON.optJSONObject(i)));
         }
 
         mStationsRequestSended = false;
 
         mMap.clear();
         setMapStations();
     }
 
     private void setMapStations() {
         setMapStations(true);
     }
 
     private void setMapStations(boolean pDoInBackbround) {
         if (stations == null) {
             setMapStationsRequest(pDoInBackbround);
         } else {
            Log.d(this.getClass().getName(), "Set up position");
             LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
             LatLng mapCenter = mMap.getCameraPosition().target;
             LatLng userPos = new LatLng(mLocationClient.getLastLocation().getLatitude(), mLocationClient.getLastLocation().getLongitude());
 
             for (Station station : stations) {
                 if (bounds.contains(station.latLng)
                         && (getDistance(mapCenter, station.latLng) <= Constants.MAP_STATIONS_DIST_LIMIT
                         || getDistance(userPos, station.latLng) <= Constants.MAP_STATIONS_DIST_LIMIT)
                         ) {
                     station.addMarker(mMap);
                 } else {
                     station.removeMarker();
                 }
             }
         }
     }
 
     private static long getDistance(LatLng point1, LatLng point2) {
         double lat1 = point1.latitude;
         double lng1 = point1.longitude;
         double lat2 = point2.latitude;
         double lng2 = point2.longitude;
         double dLat = Math.toRadians(lat2 - lat1);
         double dLng = Math.toRadians(lng2 - lng1);
         double radLat1 = Math.toRadians(lat1);
         double radLat2 = Math.toRadians(lat2);
 
         double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLng / 2) * Math.sin(dLng / 2) * Math.cos(radLat1) * Math.cos(radLat2);
         double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
         return (long) (Constants.EARTH_RADIUS * c);
     }
 }

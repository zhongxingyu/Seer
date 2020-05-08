 package com.example.mobicanavigate.activities;
 
 import android.content.Intent;
 import android.graphics.Color;
 import android.location.Location;
 import android.location.LocationManager;
 import android.location.LocationListener;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.mobicanavigate.Constants;
 import com.example.mobicanavigate.R;
 import com.example.mobicanavigate.dialogs.ConfirmDialog;
 import com.example.mobicanavigate.gps.GMapV2Direction;
 import com.example.mobicanavigate.services.NetworkService;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 import org.w3c.dom.Document;
 
 import java.util.ArrayList;
 
 /**
  * @author Miłosz Skalski
  */
 
 public class MapActivity extends FragmentActivity implements LocationListener {
     private Boolean mIsCameraMap;
     private Menu mMenu;
     private GoogleMap mGoogleMap;
     private SupportMapFragment mSupportMapFragment;
     private TextView textViewDistance;
     private LocationManager mLocationManager;
     private Marker mMarkerJenkins;
     private Marker mMarkerMobicaOffice;
     private double mDistance;
     private GMapV2Direction mMapDirection;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         setContentView(R.layout.activity_map);
         mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
         mGoogleMap = mSupportMapFragment.getMap();
         textViewDistance = (TextView) findViewById(R.id.textViewDistance);
         mIsCameraMap = getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, MODE_PRIVATE).getBoolean(Constants.SHARED_PREFERENCES_CAMERA_MAP, true);
 
         Intent intent = getIntent();
         double latitude = intent.getDoubleExtra("latitude", 0.0);
         double longitude = intent.getDoubleExtra("longitude", 0.0);
         LatLng coordsMobicaOffice = new LatLng(latitude, longitude);
         mMarkerMobicaOffice = mGoogleMap.addMarker(new MarkerOptions().position(coordsMobicaOffice).icon(BitmapDescriptorFactory.fromResource(R.drawable.mobica)));
         mGoogleMap.getUiSettings().setRotateGesturesEnabled(false);
         mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordsMobicaOffice, 10));
 
         mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.map, menu);
         this.mMenu = menu;
         MenuItem actionCamera = mMenu.findItem(R.id.action_camera_position);
         if (mIsCameraMap) {
             actionCamera.setTitle(getResources().getString(R.string.action_camera_jenkins));
         } else {
             actionCamera.setTitle(getResources().getString(R.string.action_camera_map));
         }
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
        String info = Constants.EMPTY_STRING;
        if (!NetworkService.isInternetOn(this)) {
            info += getResources().getString(R.string.no_internet_connection);
        }
        if (!NetworkService.isGpsOn(this)) {
            if (!info.isEmpty()) {
                info += Constants.NEW_LINE;
            }
            info += getResources().getString(R.string.no_gps_connection);
        }
        if (!info.isEmpty()) {
            Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
             return super.onOptionsItemSelected(item);
         }
 
         switch (item.getItemId()) {
             case R.id.action_camera_position:
                 mIsCameraMap = !mIsCameraMap;
                 getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, MODE_PRIVATE).edit().putBoolean(Constants.SHARED_PREFERENCES_CAMERA_MAP, mIsCameraMap).commit();
                 if (mIsCameraMap) {
                     item.setTitle(getResources().getString(R.string.action_camera_jenkins));
                     calculatedCamera();
                 } else {
                     item.setTitle(getResources().getString(R.string.action_camera_map));
                     cameraCenterJenkins();
                 }
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onLocationChanged(Location location) {
         if (mMarkerJenkins == null) {
             mMarkerJenkins = mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.jenkins)));
         } else {
             mMarkerJenkins.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
         }
         mMapDirection = new GMapV2Direction();
         mMapDirection.setParams(new LatLng(location.getLatitude(), location.getLongitude()), new LatLng(mMarkerMobicaOffice.getPosition().latitude, mMarkerMobicaOffice.getPosition().longitude), mGoogleMap);
         mMapDirection.execute();
 
         if (mIsCameraMap) {
             calculatedCamera();
         } else {
             cameraCenterJenkins();
         }
         if (mDistance <= 20) {
             routeEnd();
         }
         textViewDistance.setText(getResources().getString(R.string.distance) + (int) mDistance + "m");
     }
 
     @Override
     public void onStatusChanged(String provider, int status, Bundle extras) {
     }
 
     @Override
     public void onProviderEnabled(String provider) {
     }
 
     @Override
     public void onProviderDisabled(String provider) {
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.REFRESH_TIME_GPS, 0, this);
         mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.REFRESH_TIME_GPS, 0, this);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         if (mLocationManager != null) {
             mLocationManager.removeUpdates(this);
         }
     }
 
     @Override
     public void onBackPressed() {
         finish();
     }
 
     private void calculatedCamera() {
         distance();
         float[] results = new float[1];
         Location.distanceBetween(mMarkerMobicaOffice.getPosition().latitude, mMarkerMobicaOffice.getPosition().longitude,
                 mMarkerJenkins.getPosition().latitude, mMarkerJenkins.getPosition().longitude, results);
         mDistance = results[0];
         Log.d(Constants.TAG, Constants.TAG + " MapActivity Distance: " + mDistance);
 
         double lat1 = mMarkerJenkins.getPosition().latitude;
         double lng1 = mMarkerJenkins.getPosition().longitude;
         double lat2 = mMarkerMobicaOffice.getPosition().latitude;
         double lng2 = mMarkerMobicaOffice.getPosition().longitude;
 
         if (lat1 > lat2) {
             double temp = lat1;
             lat1 = lat2;
             lat2 = temp;
         }
         if (lng1 > lng2) {
             double temp = lng1;
             lng1 = lng2;
             lng2 = temp;
         }
         LatLngBounds latLonBound = new LatLngBounds(new LatLng(lat1, lng1), new LatLng(lat2, lng2)); // southwest, northeast
         DisplayMetrics metrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(metrics);
         mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLonBound, metrics.widthPixels, metrics.widthPixels, 0));
     }
 
     private void cameraCenterJenkins() {
         distance();
         mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mMarkerJenkins.getPosition().latitude, mMarkerJenkins.getPosition().longitude), 16));
     }
 
     private void distance() {
         float[] results = new float[1];
         Location.distanceBetween(mMarkerMobicaOffice.getPosition().latitude, mMarkerMobicaOffice.getPosition().longitude,
                 mMarkerJenkins.getPosition().latitude, mMarkerJenkins.getPosition().longitude, results);
         mDistance = results[0];
         Log.d(Constants.TAG, Constants.TAG + " MapActivity Distance: " + mDistance);
     }
 
     private void routeEnd() {
         if (mLocationManager != null) {
             mLocationManager.removeUpdates(this);
         }
         ConfirmDialog fragment = new ConfirmDialog(getResources().getString(R.string.route_end));
         fragment.show(getSupportFragmentManager(), null);
     }
 
 }

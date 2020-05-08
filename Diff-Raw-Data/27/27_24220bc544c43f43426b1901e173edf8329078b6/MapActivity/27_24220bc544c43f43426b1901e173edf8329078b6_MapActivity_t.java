 package com.example.MyFirstMap;
 
 import android.app.Activity;
 import android.content.Context;
 import android.location.*;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.widget.TextView;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Locale;
 
 public class MapActivity extends Activity implements LocationListener{
 
     private LocationManager mLocationManager;
     private String mProvider;
     private TextView mLocationTextView;
     private TextView mAddressTextView;
     private boolean mIsReverseGeocoding = false;
 
     private GoogleMap map;
     private Marker marker;
 
     @Override
     public void onLocationChanged(Location location) {
         updateLocationView(location);
         if(marker != null){
             LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
             marker.setPosition(pos);
         }
     }
 
     @Override
     public void onStatusChanged(String s, int i, Bundle bundle) {
     }
 
     @Override
     public void onProviderEnabled(String s) {
     }
 
     @Override
     public void onProviderDisabled(String s) {
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.map_layout);
 
        boolean gps_enabled, network_enabled;

         map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
         map.setMyLocationEnabled(true);
 
         mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         mProvider = LocationManager.GPS_PROVIDER;
         mLocationTextView = (TextView)findViewById(R.id.location);
         mAddressTextView = (TextView)findViewById(R.id.address);
 
        gps_enabled = mLocationManager.isProviderEnabled(mProvider);
        network_enabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 
        if(gps_enabled && network_enabled){
            Location location = mLocationManager.getLastKnownLocation(mProvider);
            updateLocationView(location);

            if(map != null){
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                marker = map.addMarker(new MarkerOptions().position(pos));
 
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 20));
            }
        }
        else{
            mLocationTextView.setText("Enable Location Access");
         }
     }
 
     private void updateLocationView(Location location){
         if(location == null){
             mLocationTextView.setText("Null Location");
             return;
         }
         else{
             StringBuilder sb = new StringBuilder();
             sb.append(location.getProvider())
                     .append(": ")
                     .append(location.getLatitude())
                     .append(',')
                     .append(location.getLongitude())
                     .append(" (")
                     .append(location.getAccuracy())
                     .append(" )");
 
             mLocationTextView.setText(sb.toString());
         }
 
         if(!mIsReverseGeocoding){
             mIsReverseGeocoding = true;
             new ReverseGeocoderTask().execute(location);
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         if(mLocationManager.isProviderEnabled(mProvider)){
             mLocationManager.requestLocationUpdates(mProvider, 0, 0, this);
         }
         else{
             mLocationTextView.setText("LocationProvider disabled");
         }
 
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         mLocationManager.removeUpdates(this);
     }
 
     private class ReverseGeocoderTask extends AsyncTask<Location, Void, String> {
 
         @Override
         protected String doInBackground(Location... locations) {
             Location location = locations[0];
             Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
             List<Address> addresses = null;
             try {
                 addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
 
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
             if(addresses != null && !addresses.isEmpty()){
                 Address address = addresses.get(0);
                 return address.getLocality();
             }
 
             return null;
         }
 
         @Override
         protected void onPostExecute(String result) {
             if(TextUtils.isEmpty(result)){
                 mAddressTextView.setText("Could not reverse geocode");
             }
             else{
                 mAddressTextView.setText(result);
             }
 
             mIsReverseGeocoding = false;
         }
 
     }
 }

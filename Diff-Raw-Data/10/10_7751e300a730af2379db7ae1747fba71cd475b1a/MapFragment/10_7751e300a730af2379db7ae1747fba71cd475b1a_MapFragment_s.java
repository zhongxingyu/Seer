 package org.geekhub.cherkassy.fragments;
 
 import org.geekhub.cherkassy.R;
 import org.geekhub.cherkassy.activity.MapActivity;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
 import com.google.android.gms.maps.MapView;
 import com.google.android.gms.maps.MapsInitializer;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class MapFragment extends SherlockFragment  implements LocationListener{
     private MapView mMapView;
     private GoogleMap mMap;
     private Bundle mBundle;
     private LocationManager locationManager;
     private static final long MIN_TIME = 400;
     private static final float MIN_DISTANCE = 1000;
     private  MapActivity mapActivity;
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         mapActivity = (MapActivity)getSherlockActivity();
         mMap.setMyLocationEnabled(true);
         mMap.setOnMapClickListener(new OnMapClickListener() {
             @Override
             public void onMapClick(LatLng latlng) {
                 mMap.clear();
                 mMap.addMarker(new MarkerOptions().position(latlng).snippet("Location"));
                 mapActivity.setIssuePosition(latlng);
             }
         });
         try {
             MapsInitializer.initialize(getSherlockActivity());
         } catch (GooglePlayServicesNotAvailableException e) {
             e.printStackTrace();
         }
         mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(49.432259, 32.074257) , 13.0f) );
         locationManager = (LocationManager) getSherlockActivity().getSystemService(Context.LOCATION_SERVICE);
         locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER        
 
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.map_frag, container,false);
         if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getSherlockActivity()) == 0){
             mMapView = (MapView) v.findViewById(R.id.map);
             mMapView.onCreate(mBundle);
             mMap = ((MapView) v.findViewById(R.id.map)).getMap();
         }
         return v;
     }
 
     @Override
     public void onLocationChanged(Location location) {
         LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
         CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
         mMap.animateCamera(cameraUpdate);
         mMap.addMarker(new MarkerOptions().position(latLng).snippet("Location"));
         mapActivity.setIssuePosition(latLng);
         locationManager.removeUpdates(this);
     }
 
     @Override
     public void onProviderDisabled(String provider) {
     }
 
     @Override
     public void onProviderEnabled(String provider) {
     }
 
     @Override
     public void onStatusChanged(String provider, int status, Bundle extras) {
     }
 
     @Override
     public void onResume() {
         super.onResume();
         mMapView.onResume();
     }
 
     @Override
     public void onPause() {
         super.onPause();
         mMapView.onPause();
     }
 
     @Override
     public void onDestroy() {
         mMapView.onDestroy();
         super.onDestroy();
     }
 }

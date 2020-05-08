 package edu.hastings.hastingscollege;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Toast;
 
 import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapsInitializer;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 
 /**
  * Created by Alex on 7/21/13.
  */
 public class FragmentMap extends Fragment {
 
     static final LatLng HASTINGS = new LatLng(40.591713, -98.373454);
     private GoogleMap mMap;
     private SupportMapFragment mMapFragment;
 
     public FragmentMap(){
         // Empty constructor required for fragment subclasses
     }
 
     public static Fragment newInstance(Context context) {
         FragmentMap f = new FragmentMap();
         return f;
     }
 
     @Override
     public void onResume() {
         super.onResume();
         if (mMap == null)
         {
             boolean connection = hasConnection(getActivity().getApplicationContext());
 
             if (connection)
             {
                 mMap = mMapFragment.getMap();
 
                 if  (mMap != null) {
                     mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
 
 
                     // Move the camera instantly to Hastings with a zoom of 15
                     mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HASTINGS, 15));
                     // Zoom in, animating the camera.
                     mMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
 
                     // Set marker on Hastings
                     mMap.addMarker(new MarkerOptions().position(HASTINGS)
                             .title("Hastings College")
                             .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));
                 }
             }
             else
             {
                 Toast.makeText(getActivity().getApplicationContext(),
                         "Check Your Internet Connection, Wifi Or Mobile Data Must Be Enabled",
                         Toast.LENGTH_LONG).show();
             }
         }
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
         if (mMapFragment == null){
             mMapFragment = SupportMapFragment.newInstance();
             getChildFragmentManager().beginTransaction().replace(R.id.map, mMapFragment).commit();
         }
     }
 
     public static boolean hasConnection(Context c) {
         ConnectivityManager cm = (ConnectivityManager) c
                 .getSystemService(Context.CONNECTIVITY_SERVICE);
 
         NetworkInfo wifiNetwork = cm
                 .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
         if (wifiNetwork != null && wifiNetwork.isConnected()) {
             return true;
         }
 
         NetworkInfo mobileNetwork = cm
                 .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
         if (mobileNetwork != null && mobileNetwork.isConnected()) {
             return true;
         }
 
         NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
         return (activeNetwork != null && activeNetwork.isConnected());
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);
         ViewGroup root = (ViewGroup) inflater.inflate(R.layout.map, null);
         return root;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         try {
             MapsInitializer.initialize(this.getActivity());
         } catch (GooglePlayServicesNotAvailableException e){
             e.printStackTrace();
         }
     }
 }

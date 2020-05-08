 package com.bettername.thepokemonone;
 
import java.util.List;

 import android.app.Activity;
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import com.bettername.thepokemoneone.data.POI;
import com.bettername.thepokemoneone.model.Place;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class MapActivity extends Activity implements POI.CallBackListener
 {
     LatLng you;
     Marker youMarker;
     CameraPosition cameraPosition;
     GoogleMap map;
     LocationManager locManager;
     LocationListener locationListener;
     
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_map);
         // Show the Up button in the action bar.
         getActionBar().setDisplayHomeAsUpEnabled(true);
         
         locationListener = new LocationListener()
         {
             public void onLocationChanged(Location location)
             {
                 you = new LatLng(location.getLatitude(),
                         location.getLongitude());
                 youMarker.setPosition(you);
             }
             
             public void onProviderDisabled(String provider)
             {
                 
             }
             
             public void onProviderEnabled(String provider)
             {
                 
             }
             
             public void onStatusChanged(String provider, int status,
                     Bundle extras)
             {
             }
         };
         
         locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L,
                 500.0f, locationListener);
         Location location = locManager
                 .getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
         you = new LatLng(location.getLatitude(), location.getLongitude());
         
         POI poi = new POI();
         poi.getPOIs(location.getLatitude(), location.getLongitude(),
                 (double) location.getAccuracy(), this);
         
         GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(
                 R.id.map)).getMap();
         
         youMarker = map.addMarker(new MarkerOptions()
                 .position(you)
                 .title("You")
                 .snippet("Your creature")
                 .icon(BitmapDescriptorFactory
                         .fromResource(R.drawable.rinodogsmaller)));
         youMarker.showInfoWindow();
         
         cameraPosition = new CameraPosition.Builder().target(you).zoom(17)
                 .bearing(0).tilt(90).build();
         map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
     }
     
     @Override
     protected void onPause()
     {
         locManager.removeUpdates(locationListener);
         super.onPause();
     }
     
     @Override
     protected void onStop()
     {
         locManager.removeUpdates(locationListener);
         super.onStop();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_map, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch (item.getItemId())
         {
             case android.R.id.home:
                 // This ID represents the Home or Up button. In the case of this
                 // activity, the Up button is shown. Use NavUtils to allow users
                 // to navigate up one level in the application structure. For
                 // more details, see the Navigation pattern on Android Design:
                 //
                 // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                 //
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
     
     @Override
    public void callBack(List<Place> p)
     {
         
     }
     
 }

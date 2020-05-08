 package com.liketivist.fithack;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polygon;
 import com.google.android.gms.maps.model.PolygonOptions;
 
 public class MainActivity extends FragmentActivity implements OnClickListener {
 
    private Button _theButton;
    private GoogleMap map;
    LocationManager _locationManager;
    double _latitude;
    double _longitude;
    boolean _ready;
    static private String _distance = "2.0";
    private SharedPreferences _preferences;
 
    private final LocationListener _locationListener = new LocationListener() {
 
       public void onLocationChanged(Location location) {
          updateWithNewLocation(location);
          Log.d("FitHack",String.format("onLocationChanged: %f,%f", location.getLatitude(), location.getLongitude()));
          Log.d("FitHack",String.format("Accuracy: %f", location.getAccuracy()));
          stopLocationRequests();
       }
 
       public void onProviderDisabled(String provider) {
          updateWithNewLocation(null);
       }
 
       public void onProviderEnabled(String provider) {
       }
 
       public void onStatusChanged(String provider, int status, Bundle extras) {
       }
    };
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       this.requestWindowFeature(Window.FEATURE_NO_TITLE);
       setContentView(R.layout.activity_main);
       _theButton = (Button) this.findViewById(R.id.theButton);
       _theButton.setOnClickListener(this);
 
       _preferences = PreferenceManager.getDefaultSharedPreferences(this);
 
       _ready = false;
       
       _locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
       if(_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
          _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 500.0f, _locationListener);
       } else if(_locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
          _locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 500.0f, _locationListener);
       } else {
          Intent gpsOptionsIntent = new Intent(  
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
            startActivity(gpsOptionsIntent);
       }
       
 //      Location lastLocationGPS = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 //      if (lastLocationGPS != null) {
 //         double latitude = lastLocationGPS.getLatitude();
 //         double longitude = lastLocationGPS.getLongitude();
 //         Log.d("FitHack",String.format("onCreate GPSLastLocation: %f,%f", lastLocationGPS.getLatitude(), lastLocationGPS.getLongitude()));
 //      }
 //      
 //      Location lastLocationNetwork = _locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 //      if (lastLocationNetwork != null) {
 //         double latitude = lastLocationNetwork.getLatitude();
 //         double longitude = lastLocationNetwork.getLongitude();
 //         Log.d("FitHack",String.format("onCreate networkLastLocation: %f,%f", lastLocationNetwork.getLatitude(), lastLocationNetwork.getLongitude()));
 //      }
       
    }
    
    private void stopLocationRequests() {
       _locationManager.removeUpdates(_locationListener);
    }
 
    private void updateWithNewLocation(Location location) {
       if (location != null) {
          _latitude = location.getLatitude();
          _longitude = location.getLongitude();
          _ready = true;
       }
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       getMenuInflater().inflate(R.menu.main, menu);
       return true;
    }
 
 
    public void drawTheDataOnTheMap(LatLng CURRENT_LOCATION, ArrayList<RoutePoint> routePoints) {
 	    
 	    map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
 	    
 	 // Instantiates a new Polygon object and set options
 	    PolygonOptions rectOptions = new PolygonOptions();
 	    rectOptions.strokeColor(Color.BLUE);
 
 	    
 	    //ADD THE POINTS HERE
 		for (int i = 0; i < routePoints.size(); i++) {
 			final LatLng THIS_POINT = new LatLng(routePoints.get(i).getLatitude(), routePoints.get(i).getLongitude());
 		    rectOptions.add(THIS_POINT);
 		}
 		
 		// Get back the Polygon AND ATTACH IT TO THE MAP
 		Polygon polygon = map.addPolygon(rectOptions);
 
    		//DROP A MARKER ON THE CURRENT LOCATION
 		Marker currentMarker = map.addMarker(new MarkerOptions()
 			.position(CURRENT_LOCATION)
 		   .title("Current Location")
 	        .icon(BitmapDescriptorFactory
 	        .fromResource(R.drawable.ic_launcher)));
 
 	    // Move the camera instantly to THE CURRENT LOCATION with a zoom of 15.
 	    map.moveCamera(CameraUpdateFactory.newLatLngZoom(CURRENT_LOCATION, 15));
 
 	    // Zoom in, animating the camera.
 	    map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
 
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       // TODO Auto-generated method stub
       switch (item.getItemId()) {
       case R.id.settings:
          Intent intent = new Intent(this, SettingsActivity.class);
          startActivity(intent);
          return true;
       case R.id.exit:
          finish();
          return true;
       default:
          return super.onOptionsItemSelected(item);
       }
    }
 		   
    @Override
    public void onClick(View v) {
       if (v == _theButton) {
          if(_ready) {
             final Context c = this;
            RelativeLayout start_layout = (RelativeLayout) findViewById(R.id.start_layout);
             //start_layout.setVisibility(View.GONE);
 
             MapMyRunQuery mmrq = new MapMyRunQuery(this) {
 
                @Override
                public void onDone(Route route) {
                   // Make call to maps API here
                   if(route == null) {
                      Toast.makeText(c, "Could not find any routes near you", Toast.LENGTH_LONG).show();
                   } else {
                      ArrayList<RoutePoint> routePoints = route.getRoutePoints();
                      Toast.makeText(c, route.getRouteOverview(), Toast.LENGTH_LONG).show();
                     findViewById(R.id.start_layout).setVisibility(View.GONE);
                      final LatLng CURRENT_LOCATION = new LatLng(_latitude, _longitude);
                      drawTheDataOnTheMap(CURRENT_LOCATION, routePoints);
                   }
                }
 
             };
             
             mmrq.getRoute(Double.parseDouble(_preferences.getString("runningDistance", _distance)),
                   Double.parseDouble(_preferences.getString("searchRadius", "1.0")),
                   _latitude, _longitude);          
          } else {
             Toast.makeText(this, "Waiting for current location", Toast.LENGTH_LONG).show();
          }
       }
    }
 
 }

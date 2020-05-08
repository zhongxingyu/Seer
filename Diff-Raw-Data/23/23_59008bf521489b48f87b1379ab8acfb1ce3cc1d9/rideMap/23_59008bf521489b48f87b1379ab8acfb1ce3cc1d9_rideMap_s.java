 package com.nanolabs.cuuenbici;
 
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.Gravity;
 import android.widget.TextView;
 
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class rideMap extends FragmentActivity {
 	//variables
 	GoogleMap map;
 	double lat_;
 	double long_;
 	LatLng mylocation;
 	Marker mylocationstart;
 	Marker mylocationcurrent;
 	String url = "http://10.0.1.39:8080"; 
 	httpHandler handler;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.map_ride);
 		
 		handler =  new httpHandler();  //new http handler
 		
 		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap(); //referencing map
 		//setting normal type map
 		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
 		
 		// Add a marker at start point.	it have random lat and long values but it doesn´t affect	  
 		  mylocationcurrent = map.addMarker(new MarkerOptions()
		  	.position(new LatLng(28.641787,-106.076431))
 		     .title("Punto Actual")
 		     .snippet("Este es tu punto actual"));
		
 		// Acquire a reference to the system Location Manager
 		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 		
 		//LOCATION LISTENERS
 		LocationListener locationlistener = new LocationListener() {
 			
 			@Override
 			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onProviderEnabled(String arg0) {
 				
 				
 			}
 			
 			@Override
 			public void onProviderDisabled(String arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onLocationChanged(Location arg0) {
 				//getting current lat & long values
 				lat_ = arg0.getLatitude();
 		    	long_ = arg0.getLongitude();
 		    	
 		    	//setting lat & long values to a my location object and displaying the position on the map
 		    	 mylocation = new LatLng(lat_, long_);
 		    	 mylocationcurrent.setPosition(mylocation);	
 		    	 
		    	//Move the camera instantly to current location
		    	 map.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 18));

		    	 // Zoom in, animating the camera.
		    	 map.animateCamera(CameraUpdateFactory.zoomTo(18), 2000, null); 
		    	 
 		    	//sending http request + lat & long params to server and getting response to checkHttp string
 			     String checkHttp = handler.post(url,Double.toString(lat_),Double.toString(long_));
 			     
 			     //check if http was received for the server
 			     //if server responds with OK, server receive http else Fail receiving 
 			     if(checkHttp.equals("OK")){
 			    	 System.out.println("Received by SERVER");
 			     } else {
 			    	 System.out.println("Can't Find Server");
 			     }    
 			}
 		};
 		
 		// Register the listener with the Location Manager to receive location updates
 		//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationlistener);
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationlistener);	
 	}
 }

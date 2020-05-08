 package com.rt.ui;
 
 
 import android.os.Bundle;
 import com.rt.R;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
 import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationListener;
 import com.google.android.gms.location.LocationRequest;
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.UiSettings;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import android.location.Location;
 import android.location.LocationManager;
 import android.content.Intent;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.widget.ToggleButton;
 
 
 public class PlanningActivity extends FragmentActivity
 implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnMapClickListener, OnMarkerClickListener {
 	
 	private GoogleMap map;
 	private LocationManager lm;
 	private LocationClient lc;
 	private boolean placeWpOn;
 	private ToggleButton placeWaypointsButton;
 	private ToggleButton delWaypointsButton;
 	private UiSettings uisets;
 	private Marker firstMarkerSelected;
 	private Marker secondMarkerSelected;
 	
 	
 	//in testing
 	private Location currentLoc;
 	//end testing
 	
 	private static final LocationRequest REQUEST = LocationRequest.create()
 		      .setInterval(5000)         // 5 seconds
 		      .setFastestInterval(16)    // 16ms = 60fps
 		      .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_planning);
 		
 		placeWaypointsButton = (ToggleButton)findViewById(R.id.place_waypoints);
 		delWaypointsButton = (ToggleButton)findViewById(R.id.del_waypoints);
 		placeWpOn = true;
 		
 		firstMarkerSelected = null;
 		secondMarkerSelected = null;
 		
 	}
 	
 	@Override
 	  protected void onResume() {
 	    super.onResume();
 	    setUpMapIfNeeded();
 	 
 	    map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
 	    uisets.setZoomControlsEnabled(false);
 	    
 	    setUpLocationClientIfNeeded();
 	    lc.connect();
 	    
 	    
 	    
 	   
 	    
    }
 	
    void setUpMapIfNeeded(){
 	   if(map == null){
 		// Try to obtain the map from the SupportMapFragment.
 		      map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
 		             .getMap();
 		      // Check if we were successful in obtaining the map.
 		      if (map != null) {
 		        map.setMyLocationEnabled(true);
 		        uisets = map.getUiSettings();
 		        map.setOnMapClickListener(this);
 		        map.setOnMarkerClickListener(this);
 		      }
 	   }
    }
    
    private void setUpLocationClientIfNeeded() {
 	    if (lc == null) {
 	      lc = new LocationClient(
 	          getApplicationContext(),
 	          this,  // ConnectionCallbacks
 	          this); // OnConnectionFailedListener
 	    }
    }
 
 	@Override
 	public void onLocationChanged(Location location) {
 		//Change camera to view the user immediately
 	    
 		
 	}
 	
 	@Override
 	public void onConnectionFailed(ConnectionResult result) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void onConnected(Bundle connectionHint) {
 		lc.requestLocationUpdates(
 		        REQUEST,
 		        this);
 		
 		currentLoc = lc.getLastLocation();
 		    
 	    if(currentLoc != null){
 	    	CameraUpdate startCam = CameraUpdateFactory.newLatLngZoom(new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude()), 15f);
 	    	map.moveCamera(startCam);
 	    	
 	    	/* REMOVED BECAUSE OF ANNOYANCES
 	    	//Put the starter marker on the map for convenience
 	    	selectFirstMarker(map.addMarker(new MarkerOptions()
 	        .position(new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude()))
 	        .title("Starting Waypoint")
 	        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));
 	    	*/
 	    }
 		
 	}
 	
 	@Override
 	public void onDisconnected() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void switchToRunning(View view) {
 		Intent j = new Intent(this, RunningActivity.class);
 		startActivity(j);
 	}
 
 	public void swapButtons(View view) {
 		
 		if(view.getId() == R.id.place_waypoints){
 			if(placeWpOn){
 				placeWaypointsButton.setChecked(false);
 				delWaypointsButton.setChecked(true);
 			}
 			else {
 				placeWaypointsButton.setChecked(true);
 				delWaypointsButton.setChecked(false);
 			}
 		}
 		
 		else {
 			if(placeWpOn){
 				delWaypointsButton.setChecked(true);
 				placeWaypointsButton.setChecked(false);
 				
 			}
 			else {
 				delWaypointsButton.setChecked(false);
 				placeWaypointsButton.setChecked(true);
 				
 			}
 			
 		}
 		
 		placeWpOn= !placeWpOn;
 		
 	}
 
 	@Override
 	public void onMapClick(LatLng point) {
 		//Place a waypoint here
 		if(placeWpOn){
 			 map.addMarker(new MarkerOptions()
 	        .position(point)
 	        .title("Waypoint")
 	        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
 			
 		}
 		
 		
 	}
 
 	@Override
 	public boolean onMarkerClick(Marker marker) {
 	
 		boolean first = marker.equals(firstMarkerSelected);
 		boolean second = marker.equals(secondMarkerSelected);
 		
 		
 		//If delete waypoints is on
 		if(!placeWpOn){
 			
 			if(first){
 				firstMarkerSelected = null;
 				
 				//If there is a second marker and the first will be deleted, make the second marker the new first
 				if(secondMarkerSelected != null){
 					selectFirstMarker(secondMarkerSelected);
 				}
 			}
 			
 			else if(second)
 				secondMarkerSelected = null;
 			
 			marker.setVisible(false);
 			marker.remove();
 		}
 		
 		//If this marker is already selected, deselect it
 		else if(first || second){
 			
 			
 			if(first){
 				deselectFirstMarker(marker);
 			}
 			
 			else {
 				deselectSecondMarker(marker);
 			}
 			
 		}
 		
 		//Else, mark the marker as selected
 		else {
 			
 			//If no markers are selected currently, make this the first
 			if(firstMarkerSelected == null){
 				selectFirstMarker(marker);
 			}
 			
 			//If the first is selected currently, make this the second
 			else if(secondMarkerSelected == null){
 				selectSecondMarker(marker);
 			}
 			
 			//If two are already selected, transition the current second to the new first,
 			//and mark the current marker the new second marker
 			else {
 				//Transition the second to the first
				deselectFirstMarker(firstMarkerSelected);
 				selectFirstMarker(secondMarkerSelected);
 				selectSecondMarker(marker);
 			}
 			
 		}
 		//Returning false keeps default marker behavior
 		return false;
 	}
 	
 	private void selectFirstMarker(Marker marker){
 		marker.setSnippet("SELECTED WAYPOINT (FROM)");
 		marker.showInfoWindow();
 		marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
 		firstMarkerSelected = marker;
 	}
 	
 	private void selectSecondMarker(Marker marker){
 		marker.setSnippet("SELECTED WAYPOINT (TO)");
 		marker.showInfoWindow();
 		marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
 		secondMarkerSelected = marker; 
 	}
 	
 	private void deselectFirstMarker(Marker marker){
 		marker.setSnippet("");
 		marker.hideInfoWindow();
 		marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
 		marker.setVisible(false);
 		marker.setVisible(true);
 		firstMarkerSelected = null;
 	}
 	
 	private void deselectSecondMarker(Marker marker){
 		marker.setSnippet("");
 		marker.hideInfoWindow();
 		marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
 		marker.setVisible(false);
 		marker.setVisible(true);
 		secondMarkerSelected = null;
 	}
 	
 	//Don't do anything for right now
 	public void connectWaypoints(View view){
 		
 	}
 	
 	//Deselect button calls this
 	public void deselectBoth(View view){
 		if(firstMarkerSelected != null){
 			deselectFirstMarker(firstMarkerSelected);
 		}
 		if(secondMarkerSelected != null){
 			deselectSecondMarker(secondMarkerSelected);
 		}
 	}
 	
 	
 	
 	
 
 
 }

 package com.rt.ui;
 
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import com.rt.R;
 import com.rt.core.Leg;
 import com.rt.core.MapDataManager;
 import com.rt.core.MapElement;
 import com.rt.core.Waypoint;
 import com.rt.runtime.maps.GMapsInterfacer;
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
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 import android.location.Location;
 import android.location.LocationManager;
 import android.content.Intent;
 import android.graphics.Color;
 import android.view.View;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 
 public class PlanningActivity extends AbstractRuntimeActivity
 implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnMapClickListener, OnMarkerClickListener  {
 	
 	private GoogleMap map;
 	private LocationManager lm;
	private GMapsInterfacer gmi;
 	private LocationClient lc;
 	private boolean placeWpOn, delWpOn;
 	private ToggleButton placeWaypointsButton;
 	private ToggleButton delWaypointsButton;
 	private UiSettings uisets;
 	private Marker firstMarkerSelected;
 	private Marker secondMarkerSelected;
 	private ArrayList<Polyline> lines;
 	
 	
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
 		delWpOn = false;
 		
 		firstMarkerSelected = null;
 		secondMarkerSelected = null;
 		
 		mdm = new MapDataManager();
		gmi = new GMapsInterfacer();
 		lines = new ArrayList<Polyline>();
 		
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
 	    	CameraUpdate startCam = CameraUpdateFactory.newLatLngZoom(new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude()), 17f);
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
 
 	//Handles Place Waypoint and Delete Waypoint button clicks.
 	//Makes sure both of them can not be on at the same time.
 	public void swapButtons(View view) {
 		
 		if(view.getId() == R.id.place_waypoints){
 			placeWpOn = !placeWpOn;
 			placeWaypointsButton.setChecked(placeWpOn);
 			
 			if(placeWpOn){
 				if(delWpOn){
 					delWpOn = false;
 					delWaypointsButton.setChecked(false);
 				}
 			}
 		}
 		
 		else {
 			
 		    delWpOn = !delWpOn;
 			delWaypointsButton.setChecked(delWpOn);
 			
 			if(delWpOn){
 				if(placeWpOn){
 					placeWaypointsButton.setChecked(false);
 					placeWpOn = false;
 				}
 			}	
 		}
 		
 	}
 
 	@Override
 	public void onMapClick(LatLng point) {
 		//Place a waypoint here
 		if(placeWpOn){
 			map.addMarker(new MarkerOptions()
 	        .position(point)
 	        .title("Waypoint")
 	        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
 			
 			//Sends data to MDM
 			mdm.addWaypoint(new Waypoint(point));
 			
 			//This is to force a map redraw, so that the currently placed marker can be clicked.
 			CameraPosition position = map.getCameraPosition();
 			LatLng temp = position.target;
 			CameraUpdate moveToFirst = CameraUpdateFactory.newLatLng(new LatLng(temp.latitude+.000001, temp.longitude+.000001));
 	    	map.animateCamera(moveToFirst);	
 		}
 		
 		//This will only happen if the user is (hopefully) trying to delete a leg
 		if(delWpOn){
 			Leg possibleLeg = mdm.getLeg(point);
 			
 			//A matching leg has been found within a reasonable distance of the click
 			if(possibleLeg != null){
 				
 				//Use the points stored in leg to recreate a similar polyline, then compare it to stored polylines
 				Polyline tempLine = map.addPolyline(new PolylineOptions()
 			     .addAll((possibleLeg.points))
 			     .width(5)
 			     .color(Color.RED));
 				
 				//Find it, remove it.
 				for(int i=0; i<lines.size(); i++){
 					if(lines.get(i).equals(tempLine)){
 						tempLine.remove();
 						lines.get(i).remove();
 						lines.remove(i);
 						mdm.removeLeg(possibleLeg);
 						break;
 					}
 				}
 				
 			}
 		}
 		
 		
 	}
 
 	@Override
 	public boolean onMarkerClick(Marker marker) {
 	
 		boolean first = marker.equals(firstMarkerSelected);
 		boolean second = marker.equals(secondMarkerSelected);
 		
 		
 		//If delete waypoints is on
 		if(delWpOn){
 			
 			if(first){
 				firstMarkerSelected = null;
 				
 				//If there is a second marker and the first will be deleted, make the second marker the new first
 				if(secondMarkerSelected != null){
 					selectFirstMarker(secondMarkerSelected);
 				}
 			}
 			
 			else if(second)
 				secondMarkerSelected = null;
 			
 			//Remove this waypoint from the MDM
 			mdm.removeWaypoint(mdm.getWaypoint(marker.getPosition()));
 				
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
 				Toast toast = Toast.makeText(getApplicationContext(), "Transitioning Selection", Toast.LENGTH_SHORT);
 				toast.show();
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
 		
 		/*Crash does not occur after camera moves, keeping this code in case.
 		marker.hideInfoWindow();
 		marker.setVisible(false);
 		reDraw = true;
 		onMapClick(marker.getPosition()); //put another marker (prevents crash instead of recoloring, don't ask me why)
 		reDraw = false;
 		marker.remove();
 		*/
 		firstMarkerSelected = null;
 	}
 	
 	private void deselectSecondMarker(Marker marker){
 		marker.setSnippet("");
 		marker.hideInfoWindow();
 		marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
 		secondMarkerSelected = null;
 	}
 	
 	//Don't do anything for right now
 	public void connectWaypoints(View view){
 		
 		//If there are two markers that can be connected
 		if(firstMarkerSelected != null && secondMarkerSelected != null){
 			
 		    //Get GmapsInterfacer to do the work for us
 			LatLng firstPos = firstMarkerSelected.getPosition();
 			LatLng secondPos = secondMarkerSelected.getPosition();
 					
			Leg leg = gmi.getPath(mdm.getWaypoint(firstPos), mdm.getWaypoint(secondPos));
 			
 			//POLYLINE FOR PATH
 			lines.add(map.addPolyline(new PolylineOptions()
 		     .addAll((leg.points))
 		     .width(5)
 		     .color(Color.RED)));
 
 		}
 			
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
 
 	@Override
 	void updateMapElements(ArrayList<MapElement> elements,
 			ArrayList<MapElement> selectedElements) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	
 	
 	
 
 
 }

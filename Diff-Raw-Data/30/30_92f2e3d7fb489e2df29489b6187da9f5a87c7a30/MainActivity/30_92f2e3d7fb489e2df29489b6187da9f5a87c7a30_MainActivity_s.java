 /*     Copyright (c) 2012 Johannes Wikner, Anton Lindgren, Victor Lindhe,
  *         Niklas Andreasson, John Hult
  *
  *     Licensed to the Apache Software Foundation (ASF) under one
  *     or more contributor license agreements.  See the NOTICE file
  *     distributed with this work for additional information
  *     regarding copyright ownership.  The ASF licenses this file
  *     to you under the Apache License, Version 2.0 (the
  *     "License"); you may not use this file except in compliance
  *     with the License.  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *     Unless required by applicable law or agreed to in writing,
  *     software distributed under the License is distributed on an
  *     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *     KIND, either express or implied.  See the License for the
  *     specific language governing permissions and limitations
  *     under the License.
  */
 
 package com.pifive.makemyrun;
 
 import java.util.Observable;
 import java.util.Observer;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewStub;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.pifive.makemyrun.drawing.CurrentLocationArtist;
 import com.pifive.makemyrun.drawing.MapDrawer;
 import com.pifive.makemyrun.drawing.RouteArtist;
 
 public class MainActivity extends MapActivity implements Observer {
 	private MapView mapView;
 	private View overlay;
 	private ViewStub viewStub;
 	private ViewStub runViewStub;
 	private Button stopRunButton;
 	private boolean inCatchBackState = false;
 	private MapDrawer mapDrawer;
 	private DistanceTracker distanceTracker;
     private Timer timer;
 	private LoadingStatus loadingStatus;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Setup view correctly
         setContentView(R.layout.activity_main);
         mapView = (MapView) findViewById(R.id.mapview);
         viewStub = (ViewStub) findViewById(R.id.postGeneratedStub);
         runViewStub = (ViewStub) findViewById(R.id.runningInterface);
         overlay = findViewById(R.id.overlayMenu);
         mapDrawer = new MapDrawer(mapView);
         showStartScreen();
         displayCurrentLocation();
     }
     
     /**
      * Shows the start screen (entry point for application)
      */
     private void showStartScreen() {
         overlay.setVisibility(View.VISIBLE);     
     }
     
     private Location getCurrentLocation() {
 		// Get location manager from system
 		LocationManager locManager = 
 				(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 	
 		// Fetch last known location to provide as best guess
 		LocationProvider provider = locManager.getProvider(LocationManager.GPS_PROVIDER);
 		android.location.Location currentLocation = 
 					locManager.getLastKnownLocation(provider.getName());
     	
 		if (currentLocation == null) {
 			throw new NoLocationException("Location unavailable");
 		}
 		return currentLocation;
     }
     
     /**
      * Shows the middle screen and activates the state where back button takes you backwards
      */
     private void showMiddleScreen() {
     	inCatchBackState = true;
         viewStub.setVisibility(View.VISIBLE);
         Button runButton = (Button) findViewById(R.id.runbutton);
         runButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View arg0) {
 				((Button)arg0).setVisibility(View.GONE);
 				startRun();
 				
 			}
         	
         });
     }
     
     /**
      * Starting the run
      */
     private void startRun() {
     	viewStub.setVisibility(View.GONE);
     	runViewStub.setVisibility(View.VISIBLE);
 
         stopRunButton = (Button) findViewById(R.id.stoprunbutton);
     	stopRunButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View arg0) {
 				cleanUpRun();
 				stepBackwards();
 			}
 		});
     	
     	// Start to track the distance and sets this activity to recieve distance updates
     	trackDistance();
         distanceTracker.addObserver(MainActivity.this);
 
         // Start counting the clockwatch for this run
         timer = new Timer((TextView) findViewById(R.id.clocktext));
     	timer.start();
     	
         mapView.requestFocus();
 		mapView.requestFocusFromTouch();
 		mapView.setClickable(true);
     }
     
     /**
      * To be called when a generated route is to be disposed of.
      */
     private void cleanUpRun() {
     	mapDrawer.clearDrawer();
     	mapView.invalidate();
    	timer.stop();
     }
     
     
     /**
      * Catches back button when we want it to just step back in application
      */
     @Override
     public void onBackPressed() {
     	if(inCatchBackState) {
     		cleanUpRun();
     		stepBackwards();
     	} else {
     		finish();
     	}
     }
     
     public void generateRoute(View v) {
     	overlay.setVisibility(View.GONE);
 		loadingStatus = new LoadingStatus(mapView.getContext());
 		try {
 			// send the current location to routegenerator
 			Location currentLocation = getCurrentLocation();
 			String query = RouteGenerator.generateRoute(
 							new com.pifive.makemyrun.geo.Location(currentLocation.getLatitude(), currentLocation.getLongitude()));
 			startDirectionsTask(query);
 			displayCurrentLocation();
 		} catch (RuntimeException e) {
 			loadingStatus.remove();
 			Toast.makeText(getApplicationContext(), "ERROR: "+e.getMessage(), Toast.LENGTH_LONG).show();
 			e.printStackTrace();
 			return;
 		}
 		
 		showMiddleScreen();
 		
 		View overlay = findViewById(R.id.overlayMenu);
 		overlay.setVisibility(View.GONE);
 		mapView.requestFocus();
 		mapView.requestFocusFromTouch();
 		mapView.setClickable(true);
 		
     }
 
     /**
      * Process for stepping backwards and resetting application state.
      */
     private void stepBackwards() {
     	inCatchBackState = false;
     	viewStub.setVisibility(View.GONE);
     	runViewStub.setVisibility(View.GONE);
    		mapView.getOverlays().clear();
    		mapView.setClickable(false);
    		mapView.clearFocus();
 		showStartScreen();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
 	@Override	
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	/**
 	 * Queries Google Directions with supplied query through a task.
 	 * @param query The query to execute DirectionsTask with.
 	 */
 	private void startDirectionsTask(String query) {
         DirectionsTask directionsTask = new DirectionsTask(this, DirectionsTask.GOOGLE_URL);
         directionsTask.setLoadingStatus(loadingStatus);
         JSONObject googleRoute = directionsTask.simpleGet(query);
         
         try {
 			Route route = new Route(googleRoute);
 
 			// Center on our starting point
 			com.pifive.makemyrun.geo.Location location = route.getWaypoints().get(0);
 			GeoPoint geoPoint = new GeoPoint(
 									location.getMicroLat(),
 									location.getMicroLng());
 			mapView.getController().animateTo(geoPoint);
 			
 			// Add an artist to draw our route
 			RouteArtist routeArtist = new RouteArtist(route.getWaypoints());
 			mapDrawer.addArtist(routeArtist);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Constructs a MyLocationDrawer to draw current location.
 	 */
 	private void displayCurrentLocation() {
 		
 		// Get location manager from system
 		LocationManager locManager = 
 				(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	
		// Fetch last known location to provide as best guess
		LocationProvider provider = locManager.getProvider(LocationManager.GPS_PROVIDER);
		android.location.Location bestGuess = 
					locManager.getLastKnownLocation(provider.getName());
		
 		// Construct our location artist
 		CurrentLocationArtist locationArtist = new CurrentLocationArtist(mapDrawer);
 		mapDrawer.addArtist(locationArtist);
 		
		// Make it aware of location updates every seconds
		locManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER,
				0, 
				0, 
				locationArtist);
 	}
 	
 	/**
 	 * Constructs a DistanceTracker to track distance
 	 */
 	private void trackDistance() {
 		// Get location manager from system
 		LocationManager locManager = 
 				(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 	
 		// Get the starting position
 		String provider = LocationManager.GPS_PROVIDER;
 		android.location.Location currentLocation = 
 					locManager.getLastKnownLocation(provider);
 			
 		distanceTracker = new DistanceTracker(currentLocation);
 		// set distanceTracker to retrieve location updates
 		locManager.requestLocationUpdates(provider, 0, 0, distanceTracker);
 	}
 
 	@Override
 	public void update(Observable observable, Object data) {
 		TextView distance = (TextView) findViewById(R.id.distancetext);
 		distance.setText(Math.round(distanceTracker.getTotalDistanceInMeters()) + " m");
 	}
 }

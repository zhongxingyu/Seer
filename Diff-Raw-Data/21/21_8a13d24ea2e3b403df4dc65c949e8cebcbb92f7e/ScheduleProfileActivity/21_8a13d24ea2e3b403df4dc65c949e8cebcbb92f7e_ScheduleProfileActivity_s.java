 package com.ksj.bamft.activity;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.format.Time;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import com.ksj.bamft.R;
 import com.ksj.bamft.constants.Constants;
 import com.ksj.bamft.constants.GoogleMapsConstants;
 import com.ksj.bamft.database.DatabaseHandler;
 import com.ksj.bamft.hubway.HubwayHelpers;
 import com.ksj.bamft.maps.MapHelpers;
 import com.ksj.bamft.maps.MapOverlays;
 import com.ksj.bamft.mbta.MbtaHelpers;
 import com.ksj.bamft.model.HubwayStation;
 import com.ksj.bamft.model.Landmark;
 import com.ksj.bamft.model.MbtaStation;
 import com.ksj.bamft.model.Schedule;
 import com.ksj.bamft.model.SimpleLocation;
 import com.ksj.bamft.model.Truck;
 
 public class ScheduleProfileActivity extends MapActivity {
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 		return false;
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 	        
 	    // Grab stuff passed from last activity 
         Bundle receivedExtras = this.getIntent().getExtras();
         Schedule schedule = (Schedule) receivedExtras.getSerializable(Constants.SCHEDULE);
         final double userLatitude = receivedExtras.getDouble(Constants.USER_LATITUDE);
         final double userLongitude = receivedExtras.getDouble(Constants.USER_LONGITUDE);
         
         //Open database connect
         final DatabaseHandler db = new DatabaseHandler(this);
         
         
         
         //Grab the relevant data
         final Truck truck = db.getTruck(schedule.getTruckId());
         Landmark landmark = db.getLandmark(schedule.getLandmarkId());
         
         // Finally, fill the layout
         setContentView(R.layout.truck_profile);
         
         TextView truckNameTextView = (TextView)findViewById(R.id.truckNameText);
         TextView landmarkNameTextView = (TextView)findViewById(R.id.landmarkNameText);
         TextView landmarkDistanceTextView = (TextView)findViewById(R.id.landmarkDistanceText);
         TextView truckDescriptionTextView = (TextView)findViewById(R.id.truckDescriptionText);
         
         //TODO: make it calculate real distance...lawl...
         //float make_x = new Float(landmark.getXcoord());
         //float make_y = new Float(landmark.getYcoord());
         //float make_distance = make_x / make_y;
         
         // temporarily set make_distance to 0 until we get correct data
         // from API
         float make_distance = 0;
         String distance_string = String.format("%.2g", make_distance) + " mi";
         
         truckNameTextView.setText(truck.getName());
         landmarkNameTextView.setText(landmark.getName());
         landmarkDistanceTextView.setText(distance_string);
         truckDescriptionTextView.setText(truck.getDescription());
         
         //set scrolling for description
         truckDescriptionTextView.setMovementMethod(new ScrollingMovementMethod());
         
         
         // Map view
         
         MapView mapView = (MapView) findViewById(R.id.truckProfileMap);
         mapView.setBuiltInZoomControls(true);
         
         List<Overlay> overlayToDisplay = mapView.getOverlays();
         Drawable overlayMarker =
         		this.getResources().getDrawable(R.drawable.androidmarker);
         MapOverlays overlay = new MapOverlays(overlayMarker, this);
         
         int truckLat = MapHelpers.degreesToMicrodegrees(Double.parseDouble(landmark.getYcoord()));
         int truckLon = MapHelpers.degreesToMicrodegrees(Double.parseDouble(landmark.getXcoord()));
         
         GeoPoint truckLocation = new GeoPoint(truckLat, truckLon);
         OverlayItem overlayItem = new OverlayItem(truckLocation, "Hola, Mundo!", "I'm in Mexico City!");
         
         overlay.addOverlay(overlayItem);
         overlay.populateNow();
         overlayToDisplay.add(overlay);
         
         MapController mapController = mapView.getController();
         mapController.setCenter(truckLocation);
         mapController.setZoom(17);
         
         // Google Maps button -- temporary, only here for testing intents to Maps!
         
         Time now = new Time();
     	now.setToNow();
         
         final String dayOfWeek = BamftActivity.getDayOfWeek(now);
         final String timeOfDay = BamftActivity.getMealOfDay(now);
         
         // MBTA Stats
         List<MbtaStation> mbtaStationList = MbtaHelpers.getAllMbtaStations(getBaseContext());
         MbtaStation nearestUserMbtaStation = MbtaHelpers.getNearestMbtaStation(mbtaStationList, userLatitude, userLongitude);
        MbtaStation nearestTruckMbtaStation = MbtaHelpers.getNearestMbtaStation(mbtaStationList, Double.parseDouble(landmark.getXcoord()), Double.parseDouble(landmark.getYcoord()));
         Log.d("Nearest MBTA", "The nearest MBTA is " + nearestUserMbtaStation.getStopName() + " to " + nearestTruckMbtaStation.getStopName());
         
         
         Button mapsButton = (Button) findViewById(R.id.truckProfileMapsButton);
         mapsButton.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View arg0) {
 				Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
 						Uri.parse("geo:0,0?q=" + userLatitude + "," + userLongitude + "(you are here)"));
 		        
 		        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
 		        
 		        startActivity(intent);
 			}
 		});
         
         // Hubway button
         
         createHubwayButton(
         		userLatitude,
         		userLongitude,
         		Double.parseDouble(landmark.getYcoord()),
         		Double.parseDouble(landmark.getXcoord()));
         
         // Walking directions button
         
         createWalkingDirectionsButton(
         		userLatitude,
         		userLongitude,
         		Double.parseDouble(landmark.getYcoord()),
         		Double.parseDouble(landmark.getXcoord()));
         
         // Schedule button
         
         Button scheduleButton = (Button) findViewById(R.id.truckProfileScheduleButton);
 		scheduleButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
         		// Create the intent
         		Intent loadTruckScheduleIntent = new Intent(ScheduleProfileActivity.this, TruckScheduleListActivity.class);
         		
         		// Create extras bundle
         		Bundle extras = new Bundle();
         		extras.putString(Constants.DAY_OF_WEEK, dayOfWeek);
         		extras.putString(Constants.TIME_OF_DAY, timeOfDay);
         		extras.putSerializable(Constants.TRUCK, truck);
         		loadTruckScheduleIntent.putExtras(extras);
         	
         		// Start the activity
         		ScheduleProfileActivity.this.startActivity(loadTruckScheduleIntent);
 			}
 		});
 		
 		// Menu button
 		Button menuButton = (Button) findViewById(R.id.truckProfileMenuButton);
 		menuButton.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				//Create intent
 				Intent loadTruckMenuIntent = new Intent(ScheduleProfileActivity.this, TruckMenuListActivity.class);
 				
 				// Create extras bundle
 				Bundle extras = new Bundle();
 				extras.putInt(Constants.TRUCK_ID, truck.getId()); // we'll pass the truck ID and let TruckMenuListActivity handle it.
 				loadTruckMenuIntent.putExtras(extras);
 				
 				// Start the activity
 				ScheduleProfileActivity.this.startActivity(loadTruckMenuIntent);
 				
 			}
 			
 		});
 		
 		// Twitter button
 		
 		Button twitterButton = (Button) findViewById(R.id.truckProfileTwitterButton);
 		twitterButton.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				//Create intent
 				Intent loadTruckTwitterIntent = new Intent(ScheduleProfileActivity.this, TruckTwitterListActivity.class);
 				
 				// Create extras bundle
 				Bundle extras = new Bundle();
 				extras.putString(Constants.TWITTER_HANDLE, truck.getTwitter());
 				loadTruckTwitterIntent.putExtras(extras);
 				
 				// Start the activity
 				ScheduleProfileActivity.this.startActivity(loadTruckTwitterIntent);
 				
 			}
 			
 		});
 		
 		// Yelp Button
         Button yelpButton = (Button) findViewById(R.id.truckProfileYelpButton);
         yelpButton.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				// Create intent
 				Intent loadTruckYelpIntent = new Intent(ScheduleProfileActivity.this, TruckYelpListActivity.class);
 				
 				// Create extras bundle
 				Bundle extras = new Bundle();
 				extras.putString(Constants.YELP_HANDLE, truck.getYelp());
 				loadTruckYelpIntent.putExtras(extras);
 				
 				// Star the activity
 				ScheduleProfileActivity.this.startActivity(loadTruckYelpIntent);
 
 				
 			}
 		});
 	 }
 	
 	/**
 	 * Set up Hubway button functionality. 
 	 */
 	private void createHubwayButton(final double userLat, final double userLon,
 			final double truckLat, final double truckLon) {
 
         Button hubwayButton = (Button) findViewById(R.id.truckProfileHubwayButton);
         hubwayButton.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View arg0) {
 				
 				List<HubwayStation> stations = HubwayHelpers.getAvailableStations();
 				
 				HubwayStation nearestStationToUser = HubwayHelpers.getNearestStation(stations,
 						userLat, userLon);
 				
 				HubwayStation nearestStationToTruck = HubwayHelpers.getNearestStation(stations,
 						truckLat, truckLon);
 				
 				// open intent to google maps with directions from
 				// user -> nearestStationToUser -> nearestStationToTruck -> truck
 				
 				Log.d("NearestUserHubway", nearestStationToUser.getName());
 				Log.d("NearestUserHubwayLat", Double.toString(nearestStationToUser.getLatitude()));
 				Log.d("NearesetUserHubwayLon", Double.toString(nearestStationToUser.getLongitude()));
 				
 				Log.d("NearestTruckHubway", nearestStationToTruck.getName());
 				Log.d("NearestTruckHubwayLat", Double.toString(nearestStationToTruck.getLatitude()));
 				Log.d("NearestTruckHubwayLon", Double.toString(nearestStationToTruck.getLongitude()));
 				
 				if (stations != null) {
 					for (HubwayStation station : stations) {
 						Log.d("StationName", station.getName());
 						Log.d("StationLat", Double.toString(station.getLatitude()));
 						Log.d("StationLon", Double.toString(station.getLongitude()));
 					}
 				}
 			}
 		});
 	}
 	
 	/**
 	 * Set up walking directions button functionality. 
 	 */
 	private void createWalkingDirectionsButton(final double userLat, final double userLon,
 			final double truckLat, final double truckLon) {
 
         Button hubwayButton = (Button) findViewById(R.id.truckProfileWalkingButton);
         hubwayButton.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View arg0) {
 				SimpleLocation userLocation = new SimpleLocation(userLat, userLon);
 				SimpleLocation truckLocation = new SimpleLocation(truckLat, truckLon);
 				
 				List<SimpleLocation> truckLocationList = new LinkedList<SimpleLocation>();
 				truckLocationList.add(truckLocation);
 				
 				String directions = MapHelpers.getDirections(userLocation, truckLocationList,
 						GoogleMapsConstants.WALKING_ROUTE);
 				
 				Intent intent = new Intent(
 						android.content.Intent.ACTION_VIEW, Uri.parse(directions));
 		        
 		        intent.setClassName(GoogleMapsConstants.PACKAGE, GoogleMapsConstants.CLASS);
 		        startActivity(intent);
 			}
 		});
 	}
 }
 

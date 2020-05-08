 package com.ksj.bamft.activity;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.ksj.bamft.R;
 import com.ksj.bamft.adapter.ScheduleRowAdapter;
 import com.ksj.bamft.constants.Constants;
 import com.ksj.bamft.database.DatabaseHandler;
 import com.ksj.bamft.maps.MapHelpers;
 import com.ksj.bamft.maps.SimpleLocationListener;
 import com.ksj.bamft.model.Landmark;
 import com.ksj.bamft.model.Schedule;
 import com.ksj.bamft.model.Truck;
 
 public class ScheduleListActivity extends ListActivity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         Location userLocation = getUserLocation();
         
         double testLatitude;
         double testLongitude;
         
         try {
         	testLatitude = userLocation.getLatitude();
        	Toast.makeText(getBaseContext(), "Debugging: Location locked.", Toast.LENGTH_SHORT).show();
         } catch(NullPointerException e) {
         	testLatitude = 42.37;
        	Toast.makeText(getBaseContext(), "Debugging: Couldn't Get a lock on your location, using default...", Toast.LENGTH_SHORT).show();
         	e.printStackTrace();
         }
         
         try {
         	testLongitude = userLocation.getLongitude();
         } catch(NullPointerException e) {
         	testLongitude = -71.03;
         	e.printStackTrace();
         }
         
         final double userLatitude = testLatitude;
         final double userLongitude = testLongitude;
         
         
         
         //dynamically load time of day based on previous Activity.
         String timeOfDay = "Afternoon"; //set this as default for safety
         String dayOfWeek = "Thursday"; //set this as a default for safety
         Bundle timeBundle = this.getIntent().getExtras();
         timeOfDay = timeBundle.getString(Constants.TIME_OF_DAY);
         dayOfWeek = timeBundle.getString(Constants.DAY_OF_WEEK);
         
         //First, we get the food truck data from the API
         final DatabaseHandler db = new DatabaseHandler(this); 
         //TODO: Port database handler into Application object - 
         //see http://stackoverflow.com/questions/3433883/creating-a-service-to-share-database-connection-between-all-activities-in-androi
         	
         // Get list of schedules for trucks, sorted by trucks' distances from user
         
         final List<Schedule> scheduleList = db.getSchedulesByDayAndTime(dayOfWeek, timeOfDay);
         
         final HashMap<Schedule, Double> scheduleToDistanceMap =
         		buildSchedulesToDistancesMap(db, scheduleList, userLatitude, userLongitude);
         
         Collections.sort(
         		scheduleList, 
         		getDistanceComparator(db, scheduleToDistanceMap, userLatitude, userLongitude));
         
         
         //this part is for displaying it in the ListView
         //note that we still use R.layout.truck_row
         ScheduleRowAdapter adapter = new ScheduleRowAdapter(this, R.layout.truck_row,
         		scheduleList, scheduleToDistanceMap);
         setListAdapter(adapter);
         
         // Here is where we do the actual display.
         ListView lv = getListView();
         lv.setTextFilterEnabled(true);
         
         //ListView "toast" functionality - for each item being clicked
         lv.setOnItemClickListener(new OnItemClickListener() {
         	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
         		//Proof of concept...
         		int truck_id = scheduleList.get(position).getTruckId();
         		Truck truck = db.getTruck(truck_id);
         		
         		int landmark_id = scheduleList.get(position).getLandmarkId();
         		Landmark landmark = db.getLandmark(landmark_id);
         		
         		String location_string = landmark.getName() + " at (" + landmark.getYcoord() + ", " + landmark.getXcoord() + ")";
         		Toast.makeText(getApplicationContext(), location_string, Toast.LENGTH_SHORT).show();
         		
         		// Create the intent
         		Intent loadScheduleProfileIntent = new Intent(ScheduleListActivity.this, ScheduleProfileActivity.class);
         		
         		// create the schedule bundle
         		Bundle extras = new Bundle();
         		extras.putSerializable(Constants.SCHEDULE, scheduleList.get(position));
         		extras.putDouble(Constants.USER_LATITUDE, userLatitude);
         		extras.putDouble(Constants.USER_LONGITUDE, userLongitude);
         		loadScheduleProfileIntent.putExtras(extras);
         	
         		// Start the activity
         		ScheduleListActivity.this.startActivity(loadScheduleProfileIntent);
         	}
         });
     }
     
     /**
      * Return the user's coordinates in a Location object.
      * 
      * @return
      */
     private Location getUserLocation() {
     	
     	// Create a location listener and manager to get the user's location
 
         LocationListener locationListener = new SimpleLocationListener();
     	
     	LocationManager locationManager = 
     			(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
     	
     	// Get enabled location provider with the best accuracy
     	
     	Criteria criteria = new Criteria();
     	criteria.setAccuracy(Criteria.ACCURACY_FINE);
     	
     	String bestLocationProvider = locationManager.getBestProvider(criteria, true);
     	
     	// If user has location provider enabled, get user location
         
     	if (bestLocationProvider != null && locationManager.isProviderEnabled(bestLocationProvider)) {
     		locationManager.requestLocationUpdates(
             		LocationManager.NETWORK_PROVIDER,
             		Constants.LOCATION_REFRESH_TIME,
             		Constants.LOCATION_REFRESH_DISTANCE,
             		locationListener);
     	}
     	
     	// Else prompt user to turn on a location provider
     	// TODO: fix this! pause ScheduleListActivity if settings page is called
     	
     	else {
     		Toast.makeText(getApplicationContext(), "enable a location provider!", Toast.LENGTH_LONG).show();
             Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
             ScheduleListActivity.this.startActivity(myIntent);
     	}
         
         Location userLocation = locationManager.getLastKnownLocation(bestLocationProvider);
         locationManager.removeUpdates(locationListener);
         
         return userLocation;
     }
     
     /**
      * Create a HashMap of Schedules mapped to the distance between
      * the user and the landmark.
      * 
      * @param schedules
      * @return
      */
     private HashMap<Schedule, Double> buildSchedulesToDistancesMap(DatabaseHandler db,
     		List<Schedule> schedules, double userLat, double userLon) {
     	
     	HashMap<Schedule, Double> schedulesToDistancesMap =
     			new HashMap<Schedule, Double>(schedules.size());
     	
     	for (Schedule schedule : schedules) {
     		double distance = getDistanceFromUser(schedule, db, userLat, userLon);
     		schedulesToDistancesMap.put(schedule, distance);
     	}
     	
     	return schedulesToDistancesMap;
     }
     
     /**
      * Return a Comparator to compare two Schedule objects. A Schedule is less than another
      * if its landmark is closer to the user than another Schedule, greater than another 
      * Schedule if it is farther, or equal if they are equidistant from the user.
      * 
      * @param db
      * @param userLatitude
      * @param userLongitude
      * @return
      */
     
     private Comparator<Schedule> getDistanceComparator(final DatabaseHandler db,
     		final HashMap<Schedule, Double> scheduleToDistanceMap,
     		final double userLatitude, final double userLongitude) {
     	
     	 return new Comparator<Schedule>() {
 			 public int compare(Schedule a, Schedule b) {
 				 
 				 double distanceA = scheduleToDistanceMap.get(a);
 				 double distanceB = scheduleToDistanceMap.get(b);
 				 
 				 if (distanceA < distanceB)
 					 return -1;
 				 
 				 else if (distanceA > distanceB)
 					 return 1;
 				 
 				 return 0;
 			 }
 		 };
     }
     
     /**
      * Returns the distance between a user and a landmark.
      * 
      * @param schedule
      * @param db
      * @param userLatDegrees
      * @param userLonDegrees
      * @return
      */
     
     private double getDistanceFromUser(Schedule schedule, DatabaseHandler db,
     		double userLatDegrees, double userLonDegrees) {
     	
     	int landmarkId = schedule.getLandmarkId();
     	Landmark landmark = db.getLandmark(landmarkId);
     	
     	// Get coordinates of landmark
     	
     	String landmarkXCoord = landmark.getXcoord();
     	String landmarkYCoord = landmark.getYcoord();
     	
     	double landmarkLatDegrees = 0.0;
     	double landmarkLonDegrees = 0.0;
     	
     	if (landmarkXCoord != null && landmarkXCoord.length() > 0 &&
     			landmarkYCoord != null && landmarkYCoord.length() > 0) {
     		
     		landmarkLatDegrees = Double.parseDouble(landmark.getYcoord());    	
         	landmarkLonDegrees = Double.parseDouble(landmark.getXcoord());
     	}
     	
     	// If we don't have coordinates for this landmark, assume it's farthest away
     	
     	else
     		return Double.MAX_VALUE;
 
     	return MapHelpers.calculateDistance(userLatDegrees, landmarkLatDegrees,
     			userLonDegrees, landmarkLonDegrees);
     }
 }

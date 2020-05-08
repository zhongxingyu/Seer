 package uq.deco7381.runspyrun.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import uq.deco7381.runspyrun.R;
 import uq.deco7381.runspyrun.model.Course;
 import uq.deco7381.runspyrun.model.Guard;
 import uq.deco7381.runspyrun.model.Obstacle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.UiSettings;
 import com.google.android.gms.maps.model.LatLng;
 import com.parse.FindCallback;
 import com.parse.GetCallback;
 import com.parse.ParseException;
 import com.parse.ParseGeoPoint;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 import com.parse.SaveCallback;
 /**
  * This is an Activity with Defense mode, it inherit from OnMyLocationChangeListener
  * which is going to track user's current location.
  *  
  * There are three state for user to get into this Activity:
  * 1. Create a new course
  * 2. Create new mission from a existing course
  * 3. Existing mission
  * 
  * Functionality:
  * 1. Display user current location
  * 2. Display zone of the course
  * 3. Display obstacle in the zone
  * 
  * @author Jafo
  *
  */
 public class DefenceActivity extends Activity implements OnMyLocationChangeListener {
 	
 	private GoogleMap map;
 	private LocationManager status;
 	private Location currentLocation;
 	private String isFrom; 				// Determine which Activity is user coming from
 	private ArrayList<Obstacle> newObstaclesOnCourse;	// Save the obstacle when user create new.
 	private Course course;				// Course of this mode
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_defence);
 		newObstaclesOnCourse = new ArrayList<Obstacle>();
 		Intent intent = getIntent();
 		
 		/*
 		 * Get the Course's center point (where to put data stream) from intent
 		 */
 		double latitude = intent.getDoubleExtra("latitude", 0.0);
 		double longtitude = intent.getDoubleExtra("longtitude", 0.0);
 		isFrom = intent.getStringExtra("isFrom");
 
 		/*
 		 *  Map set up
 		 */
 		status = (LocationManager) (this.getSystemService(Context.LOCATION_SERVICE));
 		if (status.isProviderEnabled(LocationManager.GPS_PROVIDER) || status.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
 			/*
 			 *  Check the map is exist or not
 			 */
 			if (map == null){
 				map = ((MapFragment) getFragmentManager().findFragmentById(R.id.defence_map)).getMap();
 			}
 			setUpMap();
 		}else{
 			Toast.makeText(this, "Please open the GPS", Toast.LENGTH_LONG).show();
 			startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
 		}
 		
 		/*
 		 * Set the course (make it visible on the map)
 		 */
 		setCourse(new ParseGeoPoint(latitude,longtitude));
 		if(isFrom.equals("exsitMission") || isFrom.equals("exsitCourse")){
 			displayObstacle();
 		}
 	}
 
 	/**
 	 * onClick method triggered by "Create"
 	 * 1. Determine which method to execute base on user's state
 	 * 
 	 * @see newCourse()
 	 * @see existMission()
 	 * @see existCourse()
 	 * 
 	 * @param v   
 	 * 
 	 */
 	public void missionDecide(View v){
 		if(isFrom.equals("newCourse")){
 			newCourse();
 		}else if(isFrom.equals("existMission")){
 			existMission();
 		}else if(isFrom.equals("existCourse")){
 			existCourse();
 		}
 		
 		Intent intent = new Intent(this, DashboardActivity.class);
 		/*
 		 * Because some database connection time issue.
 		 * Force program sleep for 5 second.
 		 */
 		try {
 			Thread.sleep(5000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		startActivity(intent);
 	}
 	/**
 	 * This function would be called when user come from "New mission" and "Existing mission" which the course is already developed.
 	 * 
 	 * 1. Fetch Course base on location which pass from last Activity
 	 * 2. Fetch the list of Obstacle base on Course
 	 * 3. Create each Obstacle object depends on the "Type" retrieved from database
 	 * 4. Add the obstacle into Map.
 	 * 
 	 */
 	private void displayObstacle(){
 		/*
 		 *  Make query "SELECT FROM "Course" WHERE "location" = course center location "
 		 */
 		ParseQuery<ParseObject> course = ParseQuery.getQuery("Course");
 		course.whereNear("location", this.course.getParseGeoPoint());
 		course.setLimit(1);
 		
 		// Make query "SELECT FROM "Obstacle" WHERE "course" = course"
 		ParseQuery<ParseObject> obstacleQuery = ParseQuery.getQuery("Obstacle");
 		obstacleQuery.whereMatchesQuery("course", course);
 		obstacleQuery.findInBackground(new FindCallback<ParseObject>() {
 			@Override
 			public void done(List<ParseObject> objects, ParseException e) {
 				// TODO Auto-generated method stub
 				if(objects.size() != 0 && e == null){
 					for(ParseObject obstacle: objects){
 						/*
 						 * Get location of obstacle (latitude, longitude, altitude)
 						 */
 						final ParseGeoPoint location = obstacle.getParseGeoPoint("location");
 						final double altitude = obstacle.getDouble("altitude");
 						/*
 						 * Retrieve the creator of this obstacle
 						 */
 						obstacle.getParseUser("creator")
 							.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
 								@Override
 								public void done(ParseObject object,
 										ParseException e) {
 									// TODO Auto-generated method stub
 									if(e == null){
 										String username = object.getString("username");
 										int level = object.getInt("level");
 										/*
 										 * Set up a obstacle object and add it into map
 										 */
 										Guard g1 = new Guard(location.getLatitude(), location.getLongitude(), altitude, username, level);
 										map.addMarker(g1.getMarkerOptions());
 									}
 								}
 							});
 	
 					}
 				}else{
 					System.out.println(e.getMessage());
 				}
 			}
 		});
 		
 	}
 	/**
 	 * Set up a Google map.
 	 * Remove the button: zoom
 	 * Remove the button: find my location
 	 * Initiate the zoom of camera to 15
 	 */
 	private void setUpMap(){
 		map.setMyLocationEnabled(true);
 		map.setOnMyLocationChangeListener(this);
 		map.animateCamera(CameraUpdateFactory.zoomTo(15));
 		UiSettings uiSettings = map.getUiSettings();
 		uiSettings.setMyLocationButtonEnabled(false);
 		uiSettings.setZoomControlsEnabled(false);
 	}
 	
 	
 	/**
 	 * Set up a course:
 	 * 1. Display the zone
 	 * 2. Set the course object
 	 * 
 	 * @param latLng
 	 */
 	private void setCourse(ParseGeoPoint location){
 		ParseUser currentUser = ParseUser.getCurrentUser();
 		course= new Course(location, currentUser.getUsername(),currentUser.getString("organization"));
 		map.addCircle(course.getCourseZone());
 	}
 	/**
 	 * Set up a obstacle: Guard:
 	 * Base on your current location
 	 * 
 	 * @param v
 	 */
 	public void setGuard(View v){;
 		ParseUser currentUser = ParseUser.getCurrentUser();
		Guard g1 = new Guard(currentLocation.getLatitude(),currentLocation.getLongitude(),currentLocation.getAltitude(),currentUser.getUsername(), currentUser.getInt("level"));
 		map.addMarker(g1.getMarkerOptions());
 		
 		/*
 		 *  Add to the list of new obstacle
 		 */
 		newObstaclesOnCourse.add(g1);
 	}
 	
 	/**
 	 * Set "currentLocation" to the actual current location
 	 * 1. Transform the type of location to ParseGeoPoint
 	 * 
 	 * @param location: Actual current location from OnMyLocationChange
 	 * @see onMyLocationChange(Location lastKnownLocation)
 	 */
 	private void setCurrentLocation(Location location){
 		this.currentLocation = location;
 	}
 	
 	@Override
 	public void onMyLocationChange(Location lastKnownLocation) {
 		// TODO Auto-generated method stub
 		setCurrentLocation(lastKnownLocation);
 		/*
 		 *  Getting latitude of the current location
 		 */
         double latitude = lastKnownLocation.getLatitude();
  
         /*
          *  Getting longitude of the current location
          */
         double longitude = lastKnownLocation.getLongitude();
  
         /*
          *  Creating a LatLng object for the current location
          */
         LatLng latLng = new LatLng(latitude, longitude);
 		
         map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(15));
 		
 		map.setOnCameraChangeListener(null);
 		
 	}
 
 	/**
 	 * This method will called by missionDecide() when user's state is "Create a new course"
 	 * 1. Save new course.
 	 * 2. Save new obstacle
 	 * 3. Save new mission
 	 * 4. Update the number of equipment user have (only available for datasourse in the moment)
 	 * 
 	 * @see missionDecide(View v)
 	 */
 	public void newCourse(){
 		/*
 		 *  Save new Course
 		 */
 		ParseObject parseCourse = new ParseObject("Course");
 		parseCourse.put("owner",ParseUser.getCurrentUser());
 		parseCourse.put("location", course.getParseGeoPoint());
 		parseCourse.put("level", course.getLevel());
 		parseCourse.put("organization", course.getOrg());
 		parseCourse.saveInBackground();
 		
 		/*
 		 *  Save new Obstacle
 		 */
 		for(Obstacle obstacle: newObstaclesOnCourse){
 			ParseObject object = new ParseObject("Obstacle");
 			object.put("creator", ParseUser.getCurrentUser());
 			object.put("energy", obstacle.getEnergy());
 			object.put("location", obstacle.getParseGeoPoint());
 			object.put("altitude", obstacle.getAltitude());
 			object.put("type", obstacle.getType());
 			object.put("course", parseCourse);
 			object.saveInBackground(new SaveCallback() {
 				@Override
 				public void done(ParseException e) {
 					// TODO Auto-generated method stub
 					if(e == null){
 						System.out.println("SUCCESS");
 					}else{
 						Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
 					}
 				}
 			});
 		}
 		/*
 		 *  Save new mission to mission list
 		 */
 		ParseObject mission = new ParseObject("Mission");
 		mission.put("course", parseCourse);
 		mission.put("username", ParseUser.getCurrentUser());
 		mission.saveInBackground();
 		
 		/*
 		 *  Update equipment
 		 */
 		ParseQuery<ParseObject> equipment = ParseQuery.getQuery("equipment");
 		equipment.whereEqualTo("username", ParseUser.getCurrentUser());
 		equipment.findInBackground(new FindCallback<ParseObject>() {
 			@Override
 			public void done(List<ParseObject> objects, ParseException e) {
 				// TODO Auto-generated method stub
 				for(ParseObject equipmentObject: objects){
 					String eType = equipmentObject.getString("eq_name");
 					if(eType.equals("Datasource")){
 						equipmentObject.put("number", equipmentObject.getInt("number")-1);
 						equipmentObject.saveInBackground();
 					}else{
 						/*
 						for(Obstacle obstacle: obstaclesOnCourse){
 							if(obstacle.getType().equals(eType)){
 								equipmentObject.put("number", equipmentObject.getInt("number")-1);
 							}
 						}*/
 					}
 				}
 			}
 		});
 
 	}
 	/**
 	 * This method will called by missionDecide() when user's state is "Existing Mission"
 	 * 1. Save Obstacle
 	 * 
 	 * @see missionDecide(View v)
 	 */
 	public void existMission(){
 		/*
 		 * Get the current course data from database
 		 */
 		ParseQuery<ParseObject> courseQuery = ParseQuery.getQuery("Course");
 		courseQuery.whereNear("location", this.course.getParseGeoPoint());
 		courseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
 			@Override
 			public void done(ParseObject course, ParseException e) {
 				// TODO Auto-generated method stub
 				if(e == null){
 					for(Obstacle obstacle: newObstaclesOnCourse){
 						/*
 						 * Save each new obstacle to database
 						 */
 						ParseObject temp = new ParseObject("Obstacle");
 						course.put("creator", ParseUser.getCurrentUser());
 						course.put("energy", obstacle.getEnergy());
 						course.put("location", obstacle.getParseGeoPoint());
 						course.put("altitude", obstacle.getAltitude());
 						course.put("type", obstacle.getType());
 						course.put("course", course);
 						temp.saveInBackground(new SaveCallback() {
 							@Override
 							public void done(ParseException e) {
 								// TODO Auto-generated method stub
 								if(e == null){
 									System.out.println("SUCCESS");
 								}else{
 									Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
 								}
 							}
 						});
 					}
 				}else{
 					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
 				}
 			}
 		});
 	}
 	
 	/**
 	 * This method will called by missionDecide() when user's state is "New Mission with existing course"
 	 * 1. Save Obstacle
 	 * 2. Save Mission
 	 * @see missionDecide(View v)
 	 */
 	public void existCourse(){
 		ParseQuery<ParseObject> courseQuery = ParseQuery.getQuery("Course");
 		courseQuery.whereEqualTo("location", this.course.getParseGeoPoint());
 		courseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
 			@Override
 			public void done(ParseObject course, ParseException e) {
 				// TODO Auto-generated method stub
 				if(e == null){
 					/*
 					 *  Save the obstacle
 					 */
 					for(Obstacle obstacle: newObstaclesOnCourse){
 						ParseObject temp = new ParseObject("Obstacle");
 						course.put("creator", ParseUser.getCurrentUser());
 						course.put("energy", obstacle.getEnergy());
 						course.put("location", obstacle.getParseGeoPoint());
 						course.put("altitude", obstacle.getAltitude());
 						course.put("type", obstacle.getType());
 						course.put("course", course);
 						temp.saveInBackground(new SaveCallback() {
 							@Override
 							public void done(ParseException e) {
 								// TODO Auto-generated method stub
 								if(e == null){
 									System.out.println("SUCCESS");
 								}else{
 									Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
 								}
 							}
 						});
 					}
 					/*
 					 *  Save new mission to mission list
 					 */
 					ParseObject mission = new ParseObject("Mission");
 					mission.put("course",course);
 					mission.put("username", ParseUser.getCurrentUser());
 					mission.saveInBackground();
 				}else{
 					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
 				}
 			}
 		});
 		
 		
 	}
 
 
 }

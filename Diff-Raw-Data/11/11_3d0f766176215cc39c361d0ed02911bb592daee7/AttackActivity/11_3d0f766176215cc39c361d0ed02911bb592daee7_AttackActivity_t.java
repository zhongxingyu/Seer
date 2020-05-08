 
 package uq.deco7381.runspyrun.activity;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import uq.deco7381.runspyrun.R;
 import uq.deco7381.runspyrun.model.Course;
 import uq.deco7381.runspyrun.model.Obstacle;
 import uq.deco7381.runspyrun.model.ParseDAO;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.hardware.SensorManager;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Vibrator;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.UiSettings;
 import com.google.android.gms.maps.model.LatLng;
 import com.parse.ParseGeoPoint;
 import com.parse.ParseUser;
 import com.wikitude.architect.ArchitectUrlListener;
 import com.wikitude.architect.ArchitectView;
 import com.wikitude.architect.ArchitectView.ArchitectConfig;
 import com.wikitude.architect.SensorAccuracyChangeListener;
 /**
  * This class will shows up when user select a course in the the AttackCourseListActivty.
  * Displaying zone on a google map if user is out of zone or start inside the zone.
  * Displaying AR view when user go from outside of zone to inside of zone.
  * 
  * @author Jafo
  * @author Peter
  * @version 1.3
  * @since 15/10/2013
  * 
  * @see uq.deco.runspyrun.activity.AttackCourseListActivity
  *
  */
 public class AttackActivity extends Activity implements  OnMyLocationChangeListener, ArchitectUrlListener{
 
 	private GoogleMap map;
 	private MapFragment mMapFragment;
 	protected ArchitectView architectView;
 	protected SensorAccuracyChangeListener sensorAccuracyListener;
 	protected Location lastKnownLocaton;
 	protected LocationManager locationManager;
 	protected JSONArray poiData = new JSONArray();
 	private Course course;
 	private ParseDAO dao;
 	private int viewFlag = 1; //1 = Map View 2 = Architect View
 	private boolean firstLoc;
 	private boolean outsideZone; // Detec user start the game  out of the zone;
 	private int counter1 = 1; // counter for checking distance to obstacles
 	private HashMap<Obstacle, Boolean> obshash = new HashMap<Obstacle, Boolean>();
 	private Boolean triggered = false; // for checking if defense already triggered
 	private int dog_dist; // starting distance for guard dog
 	private ArrayList<Obstacle> obstacles;
 	private RelativeLayout viewGroup;
 	private String alertmessage = "In mission - undetected";
 	private int userEnergy;
 
 	private ParseGeoPoint previouslocation; // for motion detector to work out distance moved
 	private Boolean bitten = false; // for dog when triggered
 	private String alertgraphicshow = "off"; // for AR view to turn on or off the alert graphic
 	private Float guardSightBearing = (float) 30.0; // starting guard sight bearing
 	private Boolean seenByGuard = false; // for guard behavior
 
 	private String alertFlag = "";
 
 
 
 	private ImageView reachData;	// Button of hacking task
 	private ProgressBar hackProgressBar;	// Progress of hacking task
 	private int hackProgress;	// Hacking progress
 	private Handler handler = new Handler();
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_attack);
 		Intent intent = getIntent();
 		dao = new ParseDAO();
 		firstLoc = false;
 		outsideZone = false;
 		
 		/*
 		 * Get the Course's center point (where to put data stream) from intent
 		 */
 		double latitude = intent.getDoubleExtra("latitude", 0.0);
 		double longitude = intent.getDoubleExtra("longtitude", 0.0);
 		course = dao.getCourseByLoc(latitude, longitude);
 		obstacles = dao.getObstaclesByCourse(course);
 		userEnergy = ParseUser.getCurrentUser().getInt("energyLevel");
 		
 		locationManager = (LocationManager) (this.getSystemService(Context.LOCATION_SERVICE));
 		/*
 		 *  Check is GPS available
 		 */
 		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
 			/*
 			 *  Check the map is exist or not
 			 */
 			if (map == null){
 				mMapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.db_map));
 				map = mMapFragment.getMap();
 				if(map != null){
 					setUpMap();
 				}
 			}
 		}else{
 			/*
 			 *  If GPS is not available, direct user to Setting  
 			 */
 			Toast.makeText(this, "Please open the GPS", Toast.LENGTH_LONG).show();
 			startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
 		}
 		
 		/*
 		 * creates initial view using wikitude architectview
 		 */
 		viewGroup = (RelativeLayout)findViewById(R.id.RelativeLayout1);
 		architectView = (ArchitectView)this.findViewById( R.id.architectView );
 		final ArchitectConfig config = new ArchitectConfig("S21hdBHKcTzOVEwj0WC/LWuveQEV4++9h6OxmlYTnV3c740F29gZ81Rhvj8XUva0J6S5VmDZYiTefRLzlmidxKYnw14S4QXxyyn7D6GkgU0XB46PX7Cbd15DQ0rabH/cdeQBJWmd86BeS54UwrD9/av4y7nCOaKsBgAcb54SS8BTYWx0ZWRfX8rymlXhkqpd3yQU1W+l0InsllmLNu9YO09WGsmNSbH1qLrNeWrrfxhliDGgBqcIq2jfRp6G5SgBGVqA4YHz6+EPRF6AjiKB8I19qYxXKookzVEkLe7683JmPkdxOis6o5pljhXn0TBjAP8iVynpYhM6IvyTgZjlCIDGwwvke2YGjVTd2wWO3OUeuy+a48twUfMGgjkp0mqkV/UK0icjrDXvP1vbD66m14jeQUAufWyFSRXJ/QMDdljPAKee34XmGiOtDiwEWxSdox2v/L9gf1hER4y8VBZzG5MtjnRdwEUQ8Z3rKa+TisBwwP8TGigc+slbFQJcQvtBMeclHE4vfbl7FJ7SSC5oiSYzjPyZr1jFU9kMtOZy/CxBi80ccEWBb0hIk6/Hu+OCjAZgJQfGgh4U5AcKZBCxrLlfqXj2CdKOdZkxSOVnupHw01xuRNL+MWuRJwKcRHqBTB7BVntKSGH3l806JEMOO+XP9jpt42SwVQm6EjSAUCE=");
 		architectView.onCreate( config );
 		architectView.setVisibility(View.GONE);
 		
 		
 		
 		
 			
 		/*
 		 * initializes a listener to check for accuracy of the compass - important for the positioning of the AR
 		 * objects in the device view
 		 * 
 		 * advises user of accuracy level
 		 */
 		this.sensorAccuracyListener = new SensorAccuracyChangeListener() {
 			@Override
 			public void onCompassAccuracyChanged( int accuracy ) {
 				/* UNRELIABLE = 0, LOW = 1, MEDIUM = 2, Height = 3 */
 				if ( accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM && AttackActivity.this != null && !AttackActivity.this.isFinishing() ) {
 					Toast.makeText( AttackActivity.this, "Accuracy low", Toast.LENGTH_LONG ).show();
 				}
 			}
 			
 		};
 		
 		this.architectView.registerSensorAccuracyChangeListener( this.sensorAccuracyListener );
 	}
 
 	/**
 	 * Set up the google map for the map view.
 	 * 
 	 * @see onCreate()
 	 */
 	private void setUpMap(){
 		map.setMyLocationEnabled(true);
 		map.setOnMyLocationChangeListener(this);
 		map.animateCamera(CameraUpdateFactory.zoomTo(15));
 		UiSettings uiSettings = map.getUiSettings();
 		uiSettings.setMyLocationButtonEnabled(false);
 		uiSettings.setZoomControlsEnabled(false);
 		
 		map.addCircle(course.getCourseZone());
 	}
 	/**
 	 * Start the special task when user reach the data stream.
 	 * 
 	 * @see onMyLocationChange()
 	 */
 	private void reachData(){
 		/*
 		 * Setup special task view for people who reach the data stream 
 		 */
 		RelativeLayout viewGroup = (RelativeLayout)findViewById(R.id.reachData);
 		viewGroup.setVisibility(View.VISIBLE);
 		reachData = (ImageView)findViewById(R.id.imageView1);
 		hackProgressBar = (ProgressBar)findViewById(R.id.progressBar1);
 		hackProgress = hackProgressBar.getProgress();
 		if(hackProgress == 0){
 			reachData.setOnTouchListener(new ReachData());
 			handler.postDelayed(runnable, 500);
 		}
 		
 	}
 	/**
 	 * Show the missionComplete dialog when user hack the datasource.
 	 */
 	private void missionComplete(boolean complete){
 		RelativeLayout viewLayout = (RelativeLayout)findViewById(R.id.RelativeLaout);
 		viewLayout.setVisibility(View.VISIBLE);
 		Button get = (Button)viewLayout.findViewById(R.id.button1);
 		TextView title = (TextView)findViewById(R.id.missionNum);
 		if(complete){
 			get.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					/*
 					 * Delete the course and update the user lv.
 					 */
 					ProgressDialog progressDialog = new ProgressDialog(AttackActivity.this);
 			        progressDialog.setTitle("Loading...");
 			        progressDialog.setCancelable(false);
 			        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 			        progressDialog.show();
 			        
 					Intent intent = new Intent(AttackActivity.this, DashboardActivity.class);
 					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 					dao.deleteCourse(AttackActivity.this.course);
 					dao.updateUserLevel(ParseUser.getCurrentUser());
 					dao.updateGetEquipment(ParseUser.getCurrentUser(), AttackActivity.this.obstacles);
 					dao.pushNotification(ParseUser.getCurrentUser());
 					startActivity(intent);
 				}
 			});
 		}else{
 			title.setText("Mission Failed");
 			get.setText("Back to dashboard");
 			get.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					/*
 					 * Delete the course and update the user lv.
 					 */
 					ProgressDialog progressDialog = new ProgressDialog(AttackActivity.this);
 			        progressDialog.setTitle("Loading...");
 			        progressDialog.setCancelable(false);
 			        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 			        progressDialog.show();
 			        
 					Intent intent = new Intent(AttackActivity.this, DashboardActivity.class);
 					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 					startActivity(intent);
 				}
 			});
 		}
 		
 	}
 
 	/**
 	 * Set up the screen in normal condition
 	 * 1. Set content visible
 	 * 2. set loading progress invisible
 	 */
 	private void showMap(){
 		viewFlag = 1;
 		
 		mMapFragment.getView().setVisibility(View.VISIBLE);
 		architectView.setVisibility(View.INVISIBLE);
 		
 		this.viewGroup.removeView( this.architectView );
 		
 		
 		// Force the View redraw
 		architectView.invalidate();
 	}
 	
 	
 	/**
 	 * Set up the screen in loading condition
 	 * 1. Set loading progress visible
 	 * 2. Set content invisible
 	 */
 	private void showAR(){
 		viewFlag = 2;
 		
 		if(this.viewGroup.getChildCount() < 3){
 			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
 			this.viewGroup.addView(this.architectView, params);
 		}
 
 		architectView.setVisibility(View.VISIBLE);
 		mMapFragment.getView().setVisibility(View.GONE);
 		
 		// Force the View redraw
 		architectView.invalidate();
 	}
 
 	@Override
 	public boolean urlWasInvoked(String arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if ( this.architectView != null ) {
 			this.architectView.onResume();
 		}
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if ( this.architectView != null ) {
 			this.architectView.onPause();
 		}
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if ( this.architectView != null ) {
 			if ( this.sensorAccuracyListener != null ) {
 				this.architectView.unregisterSensorAccuracyChangeListener( this.sensorAccuracyListener );
 			}
 			this.architectView.onDestroy();
 		}
 	}
 	
 	@Override
 	public void onLowMemory() {
 		super.onLowMemory();
 		if ( this.architectView != null ) {
 			this.architectView.onLowMemory();
 		}
 	}
 	
 	/**
 	 * Starts the Attack view in the device using wikitude architect view overlaying the html file with the AR objects in different thread
 	 * 
 	 * @throws IOException if loading index.html failed.
 	 * @see loadData
 	 * 
 	 */
 	
 	@Override
 	protected void onPostCreate( final Bundle savedInstanceState ) {
 		super.onPostCreate( savedInstanceState );
 		if ( this.architectView != null) {
 			this.architectView.onPostCreate();
 		}
 
 		try {
 			this.architectView.load("index.html");
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		
 		if (!isLoading) {
 			final Thread t = new Thread(loadData);
 			t.start();
 		}
 		
 		
 	}
 
 boolean isLoading = false;
 	
 	final Runnable loadData = new Runnable() {
 		
 		@Override
 		public void run() {
 			
 			isLoading = true;
 			
 			final int WAIT_FOR_LOCATION_STEP_MS = 2000;
 			
 			/**
 			 * Loop to ensure that location data is obtained before data from parse is loaded
 			 */
 			while (AttackActivity.this.lastKnownLocaton==null && !AttackActivity.this.isFinishing()) {
 			
 				AttackActivity.this.runOnUiThread(new Runnable() {
 					
 					@Override
 					public void run() {
 						Toast.makeText(AttackActivity.this, "Scanning", Toast.LENGTH_SHORT).show();	
 					}
 				});
 
 				try {
 					Thread.sleep(WAIT_FOR_LOCATION_STEP_MS);
 				} catch (InterruptedException e) {
 					break;
 				}
 			}
 			
 			/*
 			 * Gets data from parse database for course - preset course as this project is developed
 			 * separately to the other elements of the application
 			 * 
 			 * To be changed when integrated into the main application
 			 */
 			HashMap<String, String> courseHashMap = new HashMap<String, String>();
 			courseHashMap.put("id", course.getObjectID());
 			courseHashMap.put("name", "Datastream");
 			courseHashMap.put("description", "Objective");
 			courseHashMap.put("latitude", String.valueOf(course.getLatitude()));
 			courseHashMap.put("longitude", String.valueOf(course.getLongitude()));
 			poiData.put(new JSONObject(courseHashMap));
 			
 			
 			for(Obstacle obstacle: obstacles){
 				HashMap<String, String> obstacleHashMap = new HashMap<String, String>();
 				obstacleHashMap.put("id", obstacle.getObjectId());
 				obstacleHashMap.put("name", obstacle.getType());
 				obstacleHashMap.put("description", "Objective");
 				obstacleHashMap.put("latitude",String.valueOf(obstacle.getLatitude()));
 				obstacleHashMap.put("longitude", String.valueOf(obstacle.getLongitude()));
 				//testHashMap.put("altitude", String.valueOf(70f));
 				poiData.put(new JSONObject(obstacleHashMap));
 			}
 			
 			/**
 			 * If location is obtained calls javascrit file which displays the Attack View on the device
 			 * Passes poiData into the javascript function
 			 * 
 			 * @see callJavaScript
 			 */
 			if (AttackActivity.this.lastKnownLocaton!=null && !AttackActivity.this.isFinishing()) {
 				// TODO: you may replace this dummy implementation and instead load POI information e.g. from your database
 				System.out.print(poiData.toString());
 				AttackActivity.this.callJavaScript("World.loadPoisFromJsonData", new String[] { poiData.toString() });
 				
 			}
 			isLoading = false;
 		}
 	};
 
 	/**
 	 * Adds data from to the javascript file name to enable the data to be passed to the javascript file
 	 * 
 	 * @param methodName - the name of the javascript file to be called
 	 * @param arguments - the data to be passed to the javascript function in this case poiData - information about defences
 	 */
 	private void callJavaScript(final String methodName, final String[] arguments) {
 		final StringBuilder argumentsString = new StringBuilder("");
 		for (int i= 0; i<arguments.length; i++) {
 			argumentsString.append(arguments[i]);
 			if (i<arguments.length-1) {
 				argumentsString.append(", ");
 			}
 		}
 		
 		if (this.architectView!=null) {
 			final String js = ( methodName + "( " + argumentsString.toString() + " );" );
 			this.architectView.callJavascript(js);
 		}
 	}
 
 	@Override
 	public void onMyLocationChange(Location location) {
 		// TODO Auto-generated method stub
 			/*
 			 * Make camera on map keep tracking user.
 			 * But only setup zoom level in first time.
 			 */
 			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
 			map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
 			if(firstLoc == false){
 				map.animateCamera(CameraUpdateFactory.zoomTo(15));
 				firstLoc = true;
 			}
 			map.setOnCameraChangeListener(null);
 			
 			/*
			 * Detecting user is in the zone or not to switch between AR view  and Map view.
 			 */
 			ParseGeoPoint currentLoc = new ParseGeoPoint(location.getLatitude(),location.getLongitude());
 			double distance = course.getParseGeoPoint().distanceInKilometersTo(currentLoc);
 			//System.out.println(distance);
 			if(distance*1000 > 400){
 				outsideZone = true;
 				if(viewFlag == 2){
 					showMap();
 				}
 			}else{
 				if(viewFlag == 1){
 					if(outsideZone == true){
 						showAR();
 					}else{
 						Toast.makeText(this, "Please start from outside of the zone.", Toast.LENGTH_LONG).show();
 					}
 				}
 			}
 			/*
 			 * Set location for AR view.
 			 */
 			if (location!=null) {
 				this.lastKnownLocaton = location;
 				if ( this.architectView != null ) {
 					if ( location.hasAltitude() ) {
 						this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.getAltitude(), location.hasAccuracy() ? location.getAccuracy() : 1000 );
 					} else {
 						this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.hasAccuracy() ? location.getAccuracy() : 1000 );
 						
 					}
 				}
 			}
 			
 			/*
 			 * check to see if obstacle has been triggered 
 			 */
 			if (viewFlag == 2) {
 				
 				System.out.println("Checking distance to obstacles");
 
 				triggered = false;
 				
 				counter1 = 1;
 				
 				int obs_energycost = 0;
 				
 				// check whether obstacle has been triggered
 				for(Obstacle obstacle: obstacles){
 					//check distance between current location and all obstacles
 				
 					// get obstacle type
 					String obs_type = obstacle.getType();
 					double obs_triggerdistance = obstacle.getTriggerDistance();
 					//System.out.println(obs_triggerdistance);
 				
 					// get distance from currentLoc to obstacle
 					double obsdistance = (obstacle.getParseGeoPoint().distanceInKilometersTo(currentLoc) * 1000);
 					System.out.println("Dis to obs"+counter1+" "+obs_type+" is "+obsdistance);
 					System.out.println("Trigger distance is "+obs_triggerdistance);
 					
 					// get bearing obstacle location to current location
 					Location obstaclelocation = new Location("");
 					obstaclelocation.setLatitude(obstacle.getLatitude());
 					obstaclelocation.setLongitude(obstacle.getLongitude());
 					float userbearing = obstaclelocation.bearingTo(location);
 					System.out.println("Bearing from obstacle to user is "+userbearing);
 					
 					// check if obstacle already triggered
 					triggered = obshash.containsKey(obstacle);
 					System.out.println("triggered value is "+triggered);
 
 					// check if user within distance to trigger obstacle
 					// only do this if not already triggered
 					// reduce users energy
 					if ((obsdistance < obs_triggerdistance) && (triggered==false)) {
 						// obstacle is triggered
 						
 						if (obs_type=="Guard") {
 							seenByGuard = checkIfSeenByGuard(guardSightBearing, userbearing);
 							if (seenByGuard) {
 								alertmessage = "Detection: Guard - energy reduction: "+(obstacle.getEnergyCost());
 							} else {
 								alertmessage = "Warning: In Range of Guard not within field of vision";
 							}
 						}
 						if (obs_type=="Dog") {
 							alertmessage = "Detection: Dog - energy reduction: "+(obstacle.getEnergyCost());
 							dog_dist = 30;
 						}
 						if (obs_type=="MotionDetector") {
 							alertmessage ="Detection: Motion - no energy lost: move slowly";
 							previouslocation = currentLoc;
 						}
 						
 						obshash.put(obstacle, true);
 						/*
 						 * When obstacle be triggered, it will reduce energy of user who trigger this obstacle.
 						 * Also put half of stolen energy to player who create it.
 						 * No energy is taken when a motion detector is triggered at this stage
 						 */
 						if (obs_type!="MotionDetector" || (obs_type=="Guard" && seenByGuard != false)) {
 							obs_energycost += (obstacle.getEnergyCost());
 							dao.updateObstacleEnergy(obstacle, obstacle.getEnergyCost()/2);
 						}
 						// vibrate phone 
 						Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 						vibrator.vibrate(500);
 						
 						// set alertFlag for passing to the AR view
 						alertgraphicshow = "on";
 						System.out.println("First Triggered - "+alertmessage);
 					}
 					
 					// obstacle no longer triggered
 					// reset obshash so that obstacle can be triggered again
 					if (obsdistance > obs_triggerdistance && triggered){
 						obshash.remove(obstacle);
 						bitten = false; // reset the dog
 						seenByGuard = false;
 						alertmessage ="Mission active - Clear of all defences";
 						alertgraphicshow = "off"; // turn off alert graphic
 					}
 					
 					// events when already detected but still within trigger distance 
 					if ((obsdistance < obs_triggerdistance) && (triggered)) {
 						if (obs_type=="Guard") {
 							guardSightBearing = calcGuardSightbearing(guardSightBearing);
 							seenByGuard = checkIfSeenByGuard(guardSightBearing, userbearing);
 							if (seenByGuard) {
 								alertmessage = "Detection: Seen by Guard - energy reduction "+obstacle.getEnergyCost();
 								obs_energycost += (obstacle.getEnergyCost());
 								dao.updateObstacleEnergy(obstacle, obstacle.getEnergyCost()/2);
 								alertgraphicshow = "on";
 							} else {
 								alertmessage = "Warning: In Range of Guard not within field of vision";
 								alertgraphicshow = "off";
 							}
 						}
 						
 						// Dog behavior when triggered - chases and bites
 						if (obs_type=="Dog") {
 							if (dog_dist == 5 && !(bitten)) {
 								bitten = true;
 								Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 								vibrator.vibrate(500);
 								alertmessage = "You have been attacked by the dog - energy reduction "+obstacle.getEnergyCost();
 								obs_energycost += (obstacle.getEnergyCost());
 								dao.updateObstacleEnergy(obstacle, obstacle.getEnergyCost()/2);
 								alertgraphicshow = "on";
 							} else if (dog_dist > 0 && !(bitten)) {
 								dog_dist -= 5;
 								alertmessage = "Dog chasing you - "+dog_dist+"m away";
 								alertgraphicshow = "off";
 							} else if ((dog_dist < 6) && (bitten)) {
 								alertmessage = "You have been attacked by the dog - move away";
 								alertgraphicshow = "off";
 							}
 						}
 						// Motion Detector only triggered if the user moves more than 10m between location checks
 						if (obs_type=="MotionDetector") {
 							double moveddistance = (previouslocation.distanceInKilometersTo(currentLoc)*1000);
 							if (moveddistance > 10) {
 								alertgraphicshow = "on";
 								alertmessage = "Excessive movement detected - energy reduction "+obstacle.getEnergyCost();
 								obs_energycost += (obstacle.getEnergyCost());
 								dao.updateObstacleEnergy(obstacle, obstacle.getEnergyCost()/2);
 							} else {
 								alertmessage = "In range of motion detector: move slowly";
 								alertgraphicshow = "off";
 							}
 						}
 						System.out.println("Second Trigger - "+alertmessage);
 					}
 
 					counter1 += 1;
 				}
 				/*
				 * If user have trigger any obstacle, call to update user's energy in the Server
 				 */
 				if(obs_energycost > 0){
 					userEnergy -= obs_energycost;
 					if(userEnergy < 0){
 						userEnergy = 0;
 						missionComplete(false);
 					}
 					dao.updateEnergyByEnergy(ParseUser.getCurrentUser(), userEnergy);
 				}
 				
 				
 
 				/**
 				 * Check to see if at data stream
 				 */
 				if (distance*1000<10) {
 					alertmessage = "You have reached the data stream";
 
 					alertFlag = "ALERT!";
 					reachData();
 				}
 				
 				// update status, energy, energy bar and alerts in AR view 
 				if (this.architectView!=null) {
 					this.architectView.callJavascript("World.updateEnergyValue( '"+userEnergy+"' );");			
 					final String alertRightText = ( "World.updateAlertElementRight( '"+alertmessage.toString()+"' );" );
 					this.architectView.callJavascript(alertRightText);
 					final String alertGraphicFlag = ( "World.updateAlertGraphic( '"+alertgraphicshow.toString()+"' );" );
 					this.architectView.callJavascript(alertGraphicFlag);
 				}
 			}
 	}
 	
 
 	/**
 	 * Converts userbearing to 360 degrees
 	 * The checks to see if userbearing is within +/- 30 degrees of the guard's sight bearing
 	 * 
 	 * @param guardSightBearing2
 	 * @param userbearing
 	 * @return
 	 */
 	private Boolean checkIfSeenByGuard(Float guardSightBearing2,
 			float userbearing) {
 		// TODO Auto-generated method stub
 		
 		// convert userbearing to 360
 		System.out.println("User bearing before conversion: "+userbearing);
 		if (userbearing <0) {
 			userbearing = 180 + (180 + userbearing);
 		}
 		System.out.println("User bearing after conversion: "+userbearing);
 		
		// check if user is within sight of the guard
 		if (userbearing >= (guardSightBearing2-30.0f) && userbearing <= (guardSightBearing2+30.0f)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Calulates a new bearing (based on 360 degrees) for where the guard is looking
 	 * adds 60 degrees and then checks if the value is above 360 then adjusts
 	 * 
	 * @author worthyp
	 * 
 	 * @param guardSightBearing2
 	 * @return
 	 */
 	public Float calcGuardSightbearing(Float guardSightBearing2) {
 		guardSightBearing2 += 60;
 		if (guardSightBearing2>360) {
 			guardSightBearing2 -= 360;
 		}
 		return guardSightBearing2;
 	}
 
 
 	/**
 	 * This inner class set up a TouchListner for special task view.
 	 * Shows up when user reach the data source.
 	 * 
 	 * @author Jafo
 	 *
 	 */
 
 	private class ReachData implements OnTouchListener{
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			// TODO Auto-generated method stub
 			switch (event.getAction()) {   
 	            case MotionEvent.ACTION_MOVE:
 	            	//System.out.println(lastDuration);
 	            	if(hackProgress < 10000){
 	            		hackProgress += 20;
 	            		hackProgressBar.setProgress(hackProgress);
 	            	}else{
 	            		System.out.println("DEGUGGER");
 	            		handler.removeCallbacks(runnable);
 	            		missionComplete(true);
 	            	}
 	            	break;
             }
             return true;
 		}
 		
 	}
 	/**
 	 * This Runnable is to setup a Timer to reduce the progress status when 
 	 * special task view display.
 	 * 
 	 */
 	private Runnable runnable = new Runnable() {
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			if(hackProgress > 0 || hackProgress < 10000){
 				hackProgress -= 50;
 				if(hackProgress < 0){
 					hackProgress = 0;
 				}
 				hackProgressBar.setProgress(hackProgress);
 				handler.postDelayed(this, 500);
 			}
 			//System.out.println("show:" + hackProgress);
 		}
 	};
 
 }

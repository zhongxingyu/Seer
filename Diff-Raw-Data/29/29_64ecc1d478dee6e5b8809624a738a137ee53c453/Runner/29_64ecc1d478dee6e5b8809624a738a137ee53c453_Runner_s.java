 package pl.jacbar.runner;
 
 import android.location.GpsStatus;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.GpsStatus.Listener;
 import android.os.Bundle;
 import android.os.Handler;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import java.security.acl.LastOwnerException;
 import java.util.*;
 
 public class Runner extends Activity {
 
 
 	
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.runner);
         
         preferences = getSharedPreferences(Config.RUNNER_PREFERENCES, Activity.MODE_PRIVATE);
         if(preferences.getString("username", "").equals("") || preferences.getString("password", "").equals("")){
         	Intent loginIntent = new Intent(getApplicationContext(), Login.class);
 			startActivity(loginIntent);
         }
         // get items
         db = new DatabaseHandler(this);
         lat = (TextView)findViewById(R.id.lat);
         longi = (TextView)findViewById(R.id.longi);
         gpsStatus = (TextView)findViewById(R.id.gpsStatus);
         
 
 
         
         // load types to spinner
         types = (Spinner) findViewById(R.id.types);
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.workout_types, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         types.setAdapter(adapter);
         types.setOnItemSelectedListener(new TypseSelectListener());
         
         // set startup text
         lat.setText(String.format("Time : %02d:%02d:%02d",hour,min,sec));
         longi.setText("Disatnce : 0 m");
         
         
         LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         LocationListener locationListener = new GPSListener();
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0, 0, locationListener); 
         locationManager.addGpsStatusListener(new Listener() {
 			public void onGpsStatusChanged(int event) {
 		           switch (event) 
 		           {
 		              case GpsStatus.GPS_EVENT_SATELLITE_STATUS:break;
 		              case GpsStatus.GPS_EVENT_FIRST_FIX:   // this means you  found GPS Co-ordinates  
 		            	  gpsStatus.setText("GPS ok");
 		            	  gpsEnabled = true;
 		            	  break;
 		              case GpsStatus.GPS_EVENT_STARTED:
 		            	  //gpsEnabled = true;
 		            	  break;
 		              case GpsStatus.GPS_EVENT_STOPPED:
 		            	  gpsEnabled = false;break;
 		            }
 			}
 		});
         
         
         gpsButton = (Button)findViewById(R.id.button);
         gpsButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				buttonClicked = !buttonClicked;
 				if(buttonClicked){
 					
 					gpsButton.setText("Stop tracking");
 					longi.setText("Distance : 0 m");
 					workout = new Workout();
 					workout.setStart(new Date());
 					workout.setUser(preferences.getString("username", ""));
 					workout.setType(selectedType);
 					workout.setDistance(0);
 					workout.setDuration(0);
 					workout = db.addWorkout(workout);
 					hour = min = sec = 0;
 					distance = 0;
 					firstTime = true;
 					lat.setText(String.format("Time : %02d:%02d:%02d",hour,min,sec));
 					timer = new Timer();
 			        handler = new Handler();
 					timer.schedule(new TimerTask(){
 
 						@Override
 						public void run() {
 							handler.post(new Runnable(){
 								public void run(){
 									timerTaskFunc();
 								}
 							});
 						}
 					},1000,1000); 
 					
 				} else {
 					gpsButton.setText("Start tracking");
 					workout.setEnd(new Date());
 					workout.setDuration(3600*hour + 60*min + sec);
 					workout.setDistance(distance);
 					db.updateWorkout(workout);
 					timer.cancel();
 				}
 			}
 		});
         
         workoutsButton = (Button)findViewById(R.id.workoutsBtn);
         workoutsButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent workoutsIntent = new Intent(getApplicationContext(), Workouts.class);
 				startActivity(workoutsIntent);
 			}
 		});
     }
     
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
             case R.id.SignOut :
             	SharedPreferences.Editor preferencesEditor = preferences.edit();
 				preferencesEditor.clear();
 				preferencesEditor.commit();
 	        	Intent loginIntent = new Intent(getApplicationContext(), Login.class);
 				startActivity(loginIntent);
             	return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
    public void onStop(){
    	System.exit(0);
    }
    
    public void onDestroy(){
    	System.exit(0);
    }
    
    @Override
 
 
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.runner, menu);
         return true;
     }
     
     private void timerTaskFunc(){
     	sec++;
     	if(sec == 60){
     		min++;
     		sec=0;
     	}
     	
     	if(min == 60){
     		hour++;
     		min=0;
     	}
     	
     	lat.setText(String.format("Time : %02d:%02d:%02d",hour,min,sec));
     }
     
 
     
         
     private class TypseSelectListener implements AdapterView.OnItemSelectedListener {
 		public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
 				long arg3) {
 			selectedType = pos;
 		}
 		public void onNothingSelected(AdapterView<?> arg0) {}
     }
     
     private class GPSListener implements LocationListener{
 
 		public void onLocationChanged(Location loc) {
 			if(buttonClicked){
 				
 				latitude = loc.getLatitude();
 				longitude = loc.getLongitude();
 
 				currentDate = new Date();
 				
 				if(firstTime && gpsEnabled){
 					firstTime = false;
 					oldLat = latitude;
 					oldLong = longitude;
 					
 				} else if(gpsEnabled) {
 					Location locationA = new Location("point A");
 
 					locationA.setLatitude(oldLat);
 					locationA.setLongitude(oldLong);
 
 					Location locationB = new Location("point B");
 
 					locationB.setLatitude(latitude);
 					locationB.setLongitude(longitude);
 
 					distance += locationA.distanceTo(locationB);
 					oldLat = latitude;
 					oldLong = longitude;
 					
 					longi.setText(String.format("Disatnce : %.0f m", distance));
 				}
 			
 				if(gpsEnabled && (lastUpdate == null || ((currentDate.getTime() - lastUpdate.getTime())/1000) > 10)){
 					lastUpdate = currentDate;
 					WorkoutPart wp = new WorkoutPart();
 					wp.setDate(new Date());
 					wp.setLatitude(latitude);
 					wp.setLongitude(longitude);
 					wp.setWorkoutId(workout.getId());
 					db.addWorkoutPart(wp);
 				}
 			
 		}
 			
 		}
 
 		public void onProviderDisabled(String provider) {}
 		public void onProviderEnabled(String provider) {}
 		public void onStatusChanged(String provider, int status, Bundle extras) {}
     	
     }
     
 	private Boolean buttonClicked = false;
 	private Button gpsButton;
 	private Button workoutsButton;
 	private TextView lat;
 	private TextView longi;
 	private TextView gpsStatus;
 	private DatabaseHandler db;
 	private Workout workout;
 	private Boolean gpsEnabled = false;
 
 	private double longitude = 0, oldLong = 0;
 	private double latitude = 0, oldLat = 0;
 	private Boolean firstTime = true;
 	
 	private Date lastUpdate;
 	private Date currentDate;
 	
 	private SharedPreferences preferences;
 	private Spinner types;
 	private int selectedType = 0;
 	
 	private int hour = 0, min = 0, sec = 0;
 	private double distance;
 	private Timer timer;
 	private Handler handler;
 	
 
 }

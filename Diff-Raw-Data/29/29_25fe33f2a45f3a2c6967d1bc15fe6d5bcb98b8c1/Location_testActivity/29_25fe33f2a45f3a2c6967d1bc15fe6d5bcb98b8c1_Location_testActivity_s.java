 package com.abr.testing;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.content.Context;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
 import android.widget.TextView;
 
 public class Location_testActivity extends Activity {
     /** Called when the activity is first created. */
 	public final static String EXTRA_MESSAGE = "com.example.myapp.MESSAGE";
     
     //Global variables
     public LocationManager locationManager;
     public LocationListener locationListener;
     public boolean startRace = true; //Shall be set to true by a button
     public boolean raceStarted = false; //Start race clock and set this to true
     public boolean debugOn = true; //Shall be set to true by a button
     public boolean usingBearing = true; //Counting laps using the bearing to start location
     public float bearingSetting = 45; // Difference between lastLocation bearing to and current bearing to must exceed
     public boolean usingDistance = false; //Counting laps using distance to start location
     public float distanceSetting = 20; // The radius in which a location must be before distance exceeds again
     public int totalLaps = 0; //Number of laps
     public long raceStartTime = 0; //The utc time when the race was started
     public long lapStartTime = 0; //The utc time when the lap started
     public long currentLapTime = 0; //The current running utc time minus lapStartTime
     public long latestLapTime = 0; // The time of the last completed lap
     public Location startLocation; //The start location for reference long and lat
     public Location lastLocation; //The last location visited to compare with current location (passed the starting line)
     public boolean leftStartLine = false; // Will be set to true when the car has left the start/finish line
     public float startLineCriteria = 40; // Meters from starting point to be possible to register new lap
     public boolean newLap = true; // Will be set to true when new lap is completed and set to false when leftStartLine is set to true
     
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         // Acquire a reference to the system Location Manager
         locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
         
     }
    
     @Override
     public void onStart(){
     	super.onStart();
         // Define a listener that responds to location updates
         locationListener = new LocationListener() {
             public void onLocationChanged(Location location) {
             	// Called when a new location is found by the network location provider.
             	if(startRace){
             		checkIfNewLap(location);
             	}
             	if(debugOn){
             		printDebug(location);
             	}
             	//Set the clock
             	TextView clock = (TextView) findViewById(R.id.raceClock);
     	    	clock.setText(getTime(location.getTime(),"HH:mm:ss"));
             }
 
             public void onStatusChanged(String provider, int status, Bundle extras) {}
 
             public void onProviderEnabled(String provider) {}
 
             public void onProviderDisabled(String provider) {}
          };
       
 
         // Register the listener with the Location Manager to receive location updates
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
     }
     
     @Override
     public void onStop(){
     	super.onStop();
     	// Remove the listener you previously added
     	locationManager.removeUpdates(locationListener);
     }
     
     public void checkIfNewLap(Location location){
     	
     	if(!raceStarted){
     		raceStarted = true;
     		raceStartTime = location.getTime();
     		lapStartTime = raceStartTime;
     		startLocation = location;
     		lastLocation = startLocation;
     	}
     	else{
     		currentLapTime = location.getTime() - lapStartTime;
     		TextView textViewCurrentLapTime = (TextView) findViewById(R.id.currentLaptime);
 	    	textViewCurrentLapTime.setText(getTime(currentLapTime,"mm:ss.SS"));
 	    	
 	    	if(newLap){
 	    		float distanceFromStart = location.distanceTo(startLocation);
 	    		if(distanceFromStart > startLineCriteria && distanceFromStart > location.getAccuracy()){
 	    			leftStartLine = true;
 	    			newLap = false;
 	    		}
 	    	}
     		
     		if(usingBearing && leftStartLine){
     			float lastBearingTo = lastLocation.bearingTo(startLocation);
     			float currentBearingTo = location.bearingTo(startLocation);
     			float bearingDiff = Math.abs(lastBearingTo-currentBearingTo);
    			if(bearingDiff > bearingSetting){
     				totalLaps = totalLaps + 1;
     				//
     				newLap = true;
     				leftStartLine = false;
     				TextView laps = (TextView) findViewById(R.id.laps);
         	    	String lapString = totalLaps + " laps";
         	    	laps.setText(lapString);
     				//Later add stint laps
     				
     				compensateLocation(location);
     			}    			
     		}
     		else if(usingDistance && leftStartLine){
     			
     		}
     		else{
     			//Just for testing the function
     	    	/*TextView laps = (TextView) findViewById(R.id.laps);
     	    	String lapString = totalLaps + " laps";
     	    	laps.setText(lapString);*/
     		}
     		lastLocation = location;
     	}
     }
     
     public void printDebug(Location location){
     	
     	String debugString;
 
     	double longitude = location.getLongitude();
     	double latitude = location.getLatitude();
     	String longitudeMin = location.convert(longitude,Location.FORMAT_MINUTES);
     	String latitudeMin = location.convert(latitude,Location.FORMAT_MINUTES);
     	long time = location.getTime();
     	String timeFormatted = getTime(time,"yyyy-MM-dd HH:mm:ss.SS");
     	
     	debugString = "Time (ms from 1970): " + timeFormatted + "\nLongitude: " + longitudeMin + "\nLatitude: " + latitudeMin;
     	
     	if(location.hasAltitude()){
     		double altitude = location.getAltitude();
     		debugString = debugString + "\nAltitude: " + altitude;
     	}
     	if(location.hasAccuracy()){
     		float accuracy = location.getAccuracy();
     		debugString = debugString + "\nAccuracy (m): " + accuracy;
     	}
     	if(location.hasSpeed()){
     		float speed = location.getSpeed();
     		debugString = debugString + "\nSpeed: " + speed;
     	}
     	if(location.hasBearing()){
     		float bearing = location.getBearing();
     		debugString = debugString + "\nBearing: " + bearing;
     	} 
     	
     	if(raceStarted){
     		double startLongitude = startLocation.getLongitude();
         	double startLatitude = startLocation.getLatitude();
         	String startLongitudeMin = location.convert(startLongitude,Location.FORMAT_MINUTES);
         	String startLatitudeMin = location.convert(startLatitude,Location.FORMAT_MINUTES);
         	float bearingToStart = location.bearingTo(startLocation);
         	String start = "Bearing to start: " + bearingToStart + "\nStart longitude: " + startLongitudeMin + "\nStart latitude: " + startLatitudeMin;
         	debugString = start + "\n" + debugString;
     	}
     	//Check the newLap and leftStartLine booleans
     	debugString = debugString + "\nNew lap: " + newLap + "\nLeft start/finish: " + leftStartLine;
     	
     	TextView debugInfo = (TextView) findViewById(R.id.debugInfo);
     	debugInfo.setText(debugString);
     }
     
     public static String getTime(long milliSeconds, String dateFormat)
     {
         // Create a DateFormatter object for displaying date in specified format.
         DateFormat formatter = new SimpleDateFormat(dateFormat);
 
         // Create a calendar object that will convert the date and time value in milliseconds to date. 
          Calendar calendar = Calendar.getInstance();
          calendar.setTimeInMillis(milliSeconds);
          return formatter.format(calendar.getTime());
     }
     
     public void compensateLocation(Location location){
     	//Recalculate when passing finish line
 		if(location.hasSpeed()){
 			float lastToStart = lastLocation.distanceTo(startLocation);
 			float currentToStart = location.distanceTo(startLocation);
 			float travelledDistance = location.distanceTo(lastLocation);
 			float weightedDistance = ( currentToStart / (currentToStart + lastToStart) ) * travelledDistance;
 			long timeCompensate = (long) ( (weightedDistance / location.getSpeed()) / 1000 ); //get it in milliseconds
 			// Reset location to finish line
 			location.setTime(location.getTime()-timeCompensate);
 			location.setLatitude(startLocation.getLatitude());
 			location.setLongitude(startLocation.getLongitude());
 		}
 		latestLapTime = location.getTime() - lapStartTime;
 		lapStartTime = location.getTime();
 		//Add check if laptime is the driver's best one.
 		
 		//Add latest laptime to textfield
 		TextView textViewlastLapTime = (TextView) findViewById(R.id.latestLaptime);
     	textViewlastLapTime.setText(getTime(latestLapTime,"mm:ss.SS"));
     }
     
     
 }

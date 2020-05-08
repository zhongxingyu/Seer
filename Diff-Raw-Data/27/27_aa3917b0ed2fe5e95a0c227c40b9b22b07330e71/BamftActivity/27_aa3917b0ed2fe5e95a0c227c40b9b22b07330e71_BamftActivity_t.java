 package com.ksj.bamft;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import android.widget.Toast;
 
 
 
 
 public class BamftActivity extends Activity {
     /** Called when the activity is first created. */
 	
 	private static final String LANDMARKS_DUMP_URL = "http://bamftserver.heroku.com/landmarks/full_dump";
 	private static final String TRUCKS_DUMP_URL = "http://bamftserver.heroku.com/trucks/full_dump";
 	private static final String SCHEDULES_DUMP_URL = "http://bamftserver.heroku.com/schedules/full_dump";
 	
 	public static final String BAMFT_PREFS_NAME = "BamftPrefsFile";
 	public static final String PREFS_CACHE_UPDATED = "cacheUpdated"; // preference for last update of cache. type is Long.
 	private static long CACHE_LIFE = 5 * 60 * 1000; // how long the cached SQLite life should be (in millis).
 	
 	private static final String[] DAYS_OF_WEEK = new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
 	private static final String[] TIMES_OF_DAY = new String[] {"Morning", "Afternoon", "Evening"};
 	
 	public static final String MORNING_MEAL_STRING = "Morning";
 	public static final String AFTERNOON_MEAL_STRING = "Afternoon";
 	public static final String EVENING_MEAL_STRING = "Evening";
 	public static final String CLOSED_MEAL_STRING = "Closed";
 	
 	private static final int MORNING_START_HOUR = 7;
 	private static final int AFTERNOON_START_HOUR = 10;
 	private static final int EVENING_START_HOUR = 15;
 	private static final int CLOSING_HOUR = 23;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
      
         // Prepares the internal SQLite database, if need be (dictated by CACHE_LIFE).
         prepareData();
         
         /* Testing area 
         final DatabaseHandler db = new DatabaseHandler(this);
         List<Schedule> scheduleList = db.getAllSchedules();
                 
         for (Schedule s : scheduleList) {
         	Log.d("Schedule: ", "ID: " + s.getId() + "Truck: " + s.getTruckId() + "Landmark: " + s.getLandmarkId());
         }
         */
         
         SharedPreferences settings = getSharedPreferences(BAMFT_PREFS_NAME, 0);
         
         
         setContentView(R.layout.main);
         
         
         // Home page grid view
         //GridView gridview = (GridView) findViewById(R.id.gridview);
         //gridview.setAdapter(new ImageAdapter(this));
         
         // Calculate the formatted day of week and time of day
         Time now = new Time();
     	now.setToNow();
     	
         final String dayOfWeek = getDayOfWeek(now);
         final String timeOfDay = getMealOfDay(now);
         
         
         
         //For debugging purposes - showing the life of the cache and today's day.
         
     	long cacheBirthday = settings.getLong(PREFS_CACHE_UPDATED, 0);
     	String testingToastText = "It is currently " + dayOfWeek + " " + timeOfDay + ". Cache should be updated in : " + (cacheBirthday + CACHE_LIFE - now.toMillis(true))/1000 + " seconds.";
     	Toast.makeText(BamftActivity.this, testingToastText, Toast.LENGTH_LONG).show();
         
     	
     	/*
         gridview.setOnItemClickListener(new OnItemClickListener() {
         	//listen for what button is getting pushed...
         	
         	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 
         		Bundle timeBundle = new Bundle();
         		timeBundle.putString("dayOfWeek", dayOfWeek);
         		timeBundle.putString("timeOfDay", timeOfDay); //default just in case...note that putString automatically overwrites existing values too
         		
         		Intent loadTruckListIntent = new Intent(BamftActivity.this, TruckListActivity.class);
         		
         		switch(position) {
         		case 0:
         			//Load Morning trucks
         			Toast.makeText(BamftActivity.this, "" + dayOfWeek + " Morning trucks", Toast.LENGTH_SHORT).show();
         			timeBundle.putString("timeOfDay", "Morning");
         			loadTruckListIntent.putExtras(timeBundle);
             		BamftActivity.this.startActivity(loadTruckListIntent);
         			break;
         		case 1:
         			//Load Afternoon trucks
         			Toast.makeText(BamftActivity.this,  "" + dayOfWeek + " Afternoon trucks", Toast.LENGTH_SHORT).show();
         			timeBundle.putString("timeOfDay", "Afternoon");
         			loadTruckListIntent.putExtras(timeBundle);
             		BamftActivity.this.startActivity(loadTruckListIntent);
         			break;
         		case 2:
         			//Load Evening trucks
         			Toast.makeText(BamftActivity.this,  "" + dayOfWeek + " Evening trucks", Toast.LENGTH_SHORT).show();
         			timeBundle.putString("timeOfDay", "Evening");
         			loadTruckListIntent.putExtras(timeBundle);
             		BamftActivity.this.startActivity(loadTruckListIntent);
         			break;
         		case 3:
         			// Force data cache
         			
         			SharedPreferences settings = getSharedPreferences(BAMFT_PREFS_NAME, 0);
         	    	SharedPreferences.Editor editor = settings.edit();
         			editor.putLong(PREFS_CACHE_UPDATED, 0);
         	    	editor.commit();
         			prepareData();
         			
         			Toast.makeText(BamftActivity.this, "Updated Cache...", Toast.LENGTH_SHORT).show();
 
         			break;
         		}        				
         		
         		
         	}
         	
         });
         */
         
         
         
     }
     
     
     /**
      * Prepares the data by querying the bamftserver and updating internal SQL database
      * 
      */
     public void prepareData() {
     	
     	SharedPreferences settings = getSharedPreferences(BAMFT_PREFS_NAME, 0);
     	SharedPreferences.Editor editor = settings.edit();
     	
     	Time now = new Time();
     	now.setToNow();
     	
     	long cacheBirthday = settings.getLong("cacheUpdated", 0);
     	if ((now.toMillis(true) - cacheBirthday) > CACHE_LIFE) {
     		// now minus the birthday is bigger than the expected age - so we should re-grab from cache
     		
     	
 	    	
 	    	DatabaseHandler db = new DatabaseHandler(this);
 	    	
 	    	// First, we pull get the data from the Landmark, Truck, and Schedule tables.
 	    	String landmarksDumpData = readServerData(LANDMARKS_DUMP_URL);
 	    	String trucksDumpData = readServerData(TRUCKS_DUMP_URL);
 	    	String schedulesDumpData = readServerData(SCHEDULES_DUMP_URL);
 	    	
 	    	// Dump the database
 	    	db.recreateTables();
 	    	
 	    	//Then, we open the JSON
 	    	
 	    	
 	    	//Landmarks
 	    	try {
 	    		JSONArray landmarksArray = new JSONArray(landmarksDumpData);
 	    		
 	    		for (int i = 0; i < landmarksArray.length(); i++) {
 	    			//Iterate through each entry and save to DB
 	    			JSONObject landmarkObject = landmarksArray.getJSONObject(i).getJSONObject("landmark");
 	    			Landmark landmark = new Landmark(
 	    					landmarkObject.getInt("id"), 
 	    					landmarkObject.getString("name"), 
 	    					landmarkObject.getString("xcoord"), 
 	    					landmarkObject.getString("ycoord"));
 	    			db.addLandmark(landmark);
 	    			
 	    		}
 	    		
 	    	} catch (Exception e) {
 	    		e.printStackTrace();
 	    	}
 	    	
 	    	//Trucks
 	    	try {
 	    		JSONArray trucksArray = new JSONArray(trucksDumpData);
 	    		
 	    		for (int i = 0; i < trucksArray.length(); i++) {
 	    			//Iterate through each entry and save to DB
 	    			JSONObject truckObject = trucksArray.getJSONObject(i).getJSONObject("truck");
 	    			Truck truck = new Truck(
 	    					truckObject.getInt("id"), 
 	    					truckObject.getString("name"), 
 	    					truckObject.getString("cuisine"), 
 	    					truckObject.getString("description"));
 	    			
 	    			//just for now
 	    			truck.setCuisine("Cuiseinetest " + truck.getId());
 	    			truck.setDescription("Descriptiontest " + truck.getId());
 	    			
 	    			db.addTruck(truck);
 	    			
 	    		}
 	    		
 	    	} catch (Exception e) {
 	    		e.printStackTrace();
 	    	}
 	    	
 	    	//Schedules
 	    	try {
 	    		JSONArray schedulesArray = new JSONArray(schedulesDumpData);
 	    		
 	    		for (int i = 0; i < schedulesArray.length(); i++) {
 	    			//Iterate through each entry and save to DB
 	    			JSONObject scheduleObject = schedulesArray.getJSONObject(i).getJSONObject("schedule");
 	    			Schedule schedule = new Schedule(
 	    					scheduleObject.getInt("id"), 
 	    					scheduleObject.getString("day_of_week"), 
 	    					scheduleObject.getString("time_of_day"), 
 	    					scheduleObject.getInt("truck_id"),
 	    					scheduleObject.getInt("landmark_id"));
 	    			db.addSchedule(schedule);
 	    			
 	    		}
 	    		
 	    	} catch (Exception e) {
 	    		e.printStackTrace();
 	    	}
 	    	
 	    	
 	    	editor.putLong(PREFS_CACHE_UPDATED, now.toMillis(true));
 	    	editor.commit();
 	    	Toast.makeText(BamftActivity.this, "Cache was updated at " + now.toMillis(true), Toast.LENGTH_SHORT);
 	    	Log.d("CACHE", "Cache was updated at " + now.toMillis(true));
 	    	
     	} else {
     		
     		Log.d("CACHE", "Cache was not updated.");
     		
     	}
     }
     
     public static String getDayOfWeek(Time now) {
 
     	
         return DAYS_OF_WEEK[now.weekDay - 1]; // remember, 0 indexes	
     }
     
     /**
      * Gets the appropriate name of the meal based upon the hour specified.
      * @param 
      * @return "Morning", "Afternoon", "Evening"
      */
     public static String getMealOfDay(Time now) {
     	
     	
     	
     	Time morning_start = new Time(now);
    	morning_start.set(0, 0, MORNING_START_HOUR, now.monthDay, now.month, now.year);
     	
     	Time afternoon_start = new Time(now);
    	afternoon_start.set(0, 0, AFTERNOON_START_HOUR, now.monthDay, now.month, now.year);
     	
     	Time evening_start = new Time(now);
    	evening_start.set(0, 0, EVENING_START_HOUR, now.monthDay, now.month, now.year);
     	
     	Time closing = new Time(now);
     	closing.set(now.second, now.minute, CLOSING_HOUR, now.monthDay, now.month, now.year);
     	
    	Log.d("MEAL", "Now: " + now.hour + ":" + now.minute + " Morning: " + morning_start.hour + ":" + morning_start.minute + " Afternoon: " + afternoon_start.hour + ":" + afternoon_start.minute + " Evening: " + evening_start.hour + ":" + evening_start.minute + " Closing: " + closing.hour + ":" + closing.minute);
    	
     	if (now.after(morning_start) && now.before(afternoon_start)) {
     		// morning service
    		Log.d("MEAL", "MORNING");
     		return MORNING_MEAL_STRING;
    		
     	} else if (now.after(afternoon_start) && now.before(evening_start)) {
     		// afternoon service
    		Log.d("MEAL", "AFTERNOON");
     		return AFTERNOON_MEAL_STRING;
    		
     	} else if (now.after(evening_start) && now.before(closing)) {
     		// evneing service
    		Log.d("MEAL", "EVENING");
     		return EVENING_MEAL_STRING;
     		
    		
     	} else {
     		// closed
    		Log.d("MEAL", "CLOSED");
     		return CLOSED_MEAL_STRING;
    		
     	}
     	
     	
     }
     
     /**
      * Reads the server data from specified URL. Essentially a web page http-get function. 
      * @param dumpUrl
      * @return data from the queried URL
      */
     public String readServerData(String dumpUrl) {
     	StringBuilder builder = new StringBuilder();
     	HttpClient client = new DefaultHttpClient();
     	
 		
     	HttpGet httpGet = new HttpGet(dumpUrl);
     	try {
 			HttpResponse response = client.execute(httpGet); //run the Get
 			
 			//get the status
 			StatusLine statusLine = response.getStatusLine();
 			int statusCode = statusLine.getStatusCode();
 			if (statusCode == 200) { //HTTP 200 = "OKAY"
 				HttpEntity entity = response.getEntity();
 				InputStream content = entity.getContent();
 				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
 				String line;
 				while ((line = reader.readLine()) != null) {
 					//basic "while not EOF, keep reading"
 					builder.append(line);
 				}
 			} else {
 				Log.e(BamftActivity.class.toString(), "Failed to download file");
 			}
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return builder.toString();
     }
     
     public void menuClickFunction(final View v) {
     	
     	Time now = new Time();
     	now.setToNow();
     	
         final String dayOfWeek = getDayOfWeek(now);
         final String timeOfDay = getMealOfDay(now);
         
     	Bundle timeBundle = new Bundle();
 		timeBundle.putString("dayOfWeek", dayOfWeek);
 		timeBundle.putString("timeOfDay", timeOfDay); //default just in case...note that putString automatically overwrites existing values too
 		
 		
 		
 		switch(v.getId()) {
 		
 		case R.id.menu_item_search_nearby:
 			//Load Morning trucks
 			//Toast.makeText(BamftActivity.this, "" + dayOfWeek + " Morning trucks", Toast.LENGTH_SHORT).show();
 			//timeBundle.putString("timeOfDay", timeOfDay);
 			Toast.makeText(BamftActivity.this,  "Nearby open trucks (for " + dayOfWeek + " " + timeOfDay + ")", Toast.LENGTH_SHORT).show();
 			
 			Intent loadScheduleListIntent = new Intent(BamftActivity.this, ScheduleListActivity.class);
 			loadScheduleListIntent.putExtras(timeBundle);
     		BamftActivity.this.startActivity(loadScheduleListIntent);
     		
 			break;
 		case R.id.menu_item_list_all:
 			//Load Afternoon trucks
 			Toast.makeText(BamftActivity.this,  "All trucks (open and closed)", Toast.LENGTH_SHORT).show();
 			
 			Intent loadTruckListIntent = new Intent(BamftActivity.this, TruckListActivity.class);
 			timeBundle.putString("timeOfDay", "Afternoon");
 			loadTruckListIntent.putExtras(timeBundle);
     		BamftActivity.this.startActivity(loadTruckListIntent);
 			break;
 		case R.id.menu_item_map_view:
 			//Load Evening trucks
 			Toast.makeText(BamftActivity.this,  "Not functional yet...", Toast.LENGTH_SHORT).show();
 			/*
 			timeBundle.putString("timeOfDay", "Evening");
 			loadScheduleListIntent.putExtras(timeBundle);
     		BamftActivity.this.startActivity(loadScheduleListIntent);
     		*/
 			
 			
 			
 			
 			
 			break;
 		case R.id.menu_item_surprise_me:
 			// Force data cache
 			
 			SharedPreferences settings = getSharedPreferences(BAMFT_PREFS_NAME, 0);
 	    	SharedPreferences.Editor editor = settings.edit();
 			editor.putLong(PREFS_CACHE_UPDATED, 0);
 	    	editor.commit();
 			prepareData();
 			
 			Toast.makeText(BamftActivity.this, "Updated Cache...", Toast.LENGTH_SHORT).show();
 
 			break;
 		}        		
     	
     	
     	
     }
     
    
         
 }

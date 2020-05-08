 package com.engine9;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.io.*;
 import java.util.Date;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonArray;
 
 import com.engine9.R;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class TimetableActivity extends Activity {
 	private String vehicleID;
 	/* The local store (save some timetable) allow user to do the quick search, but only 
 	 * allow  around 10 places/service (can add the time limit so that if user did not use
 	 * for a while, delete that)*/
 	//private LinkedHashMap<String, List<String>> timetable = new LinkedHashMap();
 	/* The global store (save all timetable) allow user to search, but really depends on
 	 * Internet so that it might be slow */
 	private JsonElement jData;
 	private ArrayList<Listing> times = new ArrayList<Listing>();
 
 	private Date time;
 	private DateFormat formatter = new SimpleDateFormat("hh:mm:ss");
 	
 	private ListView timeList;
 	private TimeAdapter adapter;
 	
 	private BroadcastReceiver br;
 	
 	//Temporary favourites array
 	//private String[] favourites = {"412", "411"};
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_timetable);
 		
 		//Grab url from intent and make request
 		Intent intent = getIntent();
 		String iurl = intent.getStringExtra("timeURL");
 		new TimeRequest().execute(iurl);
 		
 		timeList = (ListView) findViewById(R.id.list_view);
 		
 	}
 	
	public void favOnlyButtonPush() {
 		//Check to see if times isn't empty
 		if(times.size() != 0){
 			
 			//A list to hold listing that will be deleted
 			ArrayList<Listing> removeList = new ArrayList<Listing>();
 			
 			//The adapter for the ListView
 			TimeAdapter ta = (TimeAdapter) timeList.getAdapter();
 			
 			//Loop through every listing
 			for(int i = 0; i < timeList.getCount(); i ++ ){
 				Listing l = (Listing) ta.getItem(i);
 				
 				//Check if they are within the favourites array
 				Boolean listCheck = false;
 				for(String fav: FavouriteManager.getFavourites(getApplicationContext())){
 					if(l.code.equals(fav)){
 						listCheck = true;
 					}
 				}
 				
 				//Add non-favourite lists to the removeList
 				if(!listCheck){
 					removeList.add(l);
 				}
 			}
 			
 			//Remove everything in removeList
 			for(int j = 0; j < removeList.size(); j++){
 				ta.remove(removeList.get(j));
 			}
 			
 			//Update ListView
 			ta.notifyDataSetChanged();
 		}
 	}
 	@Override
 	protected void onStop(){
 		super.onStop();
 		if(br != null){
 			unregisterReceiver(br);
 		}
 	}
 
 	/**
 	 * Add and store the vehicle timetable to the local device
 	 * 	   @throws InvalidPointerException 
 	 * 	        if the vehicleID is invalid
 	 *     @throws NullPointerException
 	 * 	        if the vehicleID or time is empty or null
 	 * */
 	private void addTimetable(String vehicleID, List<String> time) 
 			throws InvalidPointerException {
 		if (vehicleID == null || vehicleID.length() == 0 || time == null || time.size() == 0) {
 			throw new NullPointerException();
 		}
 		if (vehicleID.length() > 4) {
 			throw new InvalidPointerException();
 		}
 		
 		//jData.add(vehicleID, (JsonElement) time);
 	}
 
 	public String toString() {
 		return jData.toString();
 	}
 
 	/**
 	 * Count the time and highlight the service if approaching within 5 mim
 	 * */
 	private void timeCountDown() {
 
 	}
 
 	/**
 	 * Get the favourites from user define
 	 * */
 	private void getFav(){
 		
 	}
 	
 
 	
 	private void updateList(){
 		
 		ArrayList<Listing> toBeDeleted = new ArrayList<Listing>();
 		for(Listing l : times){
 			if((l.time* 10  - System.currentTimeMillis())/ 60000 < -5){
 				toBeDeleted.add(l);
 				
 			}
 		}
 		for(Listing li : toBeDeleted){
 			adapter.remove(li);
 			times.remove(li);
 		}
 		adapter.notifyDataSetChanged();
 	}
 	
 	//Test function (will be modified later) that outputs all relevant data from JSON file
 	private void findTimes() {
 		JsonArray st =jData.getAsJsonObject().getAsJsonArray("StopTimetables"); //Get the Stop info
 		//Get the particular trip info
 		JsonArray trips = st.get(0).getAsJsonObject().get("Trips").getAsJsonArray(); 
 		
 		//Loop all the elements and get each single trip info
 		for(int i = 0; i < trips.size(); i++){
 			JsonObject trip = trips.get(i).getAsJsonObject();
 			
 			JsonObject route = trip.getAsJsonObject("Route");
 			
 			//Use the long type to store the departure time with its UTC time zone
 			long d = Long.parseLong(trip.get("DepartureTime").getAsString().substring(6, 18));
 			//Get single service info and add to the list
 			if((d * 10  - System.currentTimeMillis())/ 60000 > -5)
 			{
 				Listing l = new Listing(d, route.get("Code").getAsString(),  route.get("Direction").getAsInt());
 				times.add(l);
 			}
 			
 		}
 		
 		adapter = new TimeAdapter(getApplicationContext(), 
 				times);
 		timeList.setAdapter(adapter);
 	}
 	
 
 	/**
 	 * It extends the Request class (which handles getRequests)
 	 * the onPostExecute function is overwritten so that the returned JSON
 	 * data can be handled specifically for this activity (to get Time info)
 	 * */
 	private class TimeRequest extends Request{
 		@Override
 		public void onPostExecute(String result) {
 			try {
 				jData = JParser2.main(result);
 			} catch (Exception e) {
 				Log.e("Error", "Parsing error");
 				e.printStackTrace();
 			}
 			findTimes();
 			br = new BroadcastReceiver(){
 
 				@Override
 				public void onReceive(Context context, Intent intent) {
 					 if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
 						 updateList();
 					 }
 					
 				}
 				
 			};
 			
 			registerReceiver(br, new IntentFilter(Intent.ACTION_TIME_TICK));
 		}
 	}
 	
 }

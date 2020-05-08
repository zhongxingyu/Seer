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
 
 import android.os.AsyncTask;
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
import android.widget.Toast;
 
 public class TimetableActivity extends Activity {
 	private JsonElement jData; //To store the timetable based on the route
 	private ArrayList<Listing> times = new ArrayList<Listing>(); 
 	
 	private ListView timeList;
 	private TimeAdapter adapter;
 	
 	private BroadcastReceiver br;
 	private Boolean registered = false;
 	
 	public TimeRequest tRequest;
 	
 	//Temporary favourites array
 	//private String[] favourites = {"412", "411"};
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_timetable);
 		
 		//Grab url from intent and make request
 		Intent intent = getIntent();
 		String iurl = intent.getStringExtra("timeURL");
 		tRequest = new TimeRequest();
 		tRequest.execute(iurl);
 		
 		timeList = (ListView) findViewById(R.id.list_view);
 		
 	}
 	
 	protected void onResume(Bundle savedInstanceState){
 		super.onResume();
 		
 		if(tRequest.getStatus() == AsyncTask.Status.FINISHED){
 			registered = true;
 		}
 		
 		registerReceiver(br, new IntentFilter(Intent.ACTION_TIME_TICK));
 	}
 	
 	protected void onPause(Bundle savedInstanceState){
 		super.onPause();
 		
 		 if(tRequest != null && tRequest.getStatus() != AsyncTask.Status.FINISHED) {
 			 	tRequest.cancel(true);
 		    }
 	}
 	
 	protected void onStop(Bundle savedInstanceState){
 		super.onStop();
 		
 		if(tRequest != null && tRequest.getStatus() != AsyncTask.Status.FINISHED) {
 		 	tRequest.cancel(true);
 	    }
 		
 		if(br != null && !registered){
 			unregisterReceiver(br);
 			registered = false;
 		}
 	}
 	
 	/**
 	 * Set the favourite button function
 	 * */
 	public void favOnlyButtonPush(View view) {
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
 	
 
 	public void onFavouriteButtonPush(View view) {
 		TextView tv = (TextView) view.findViewById(R.id.code);
 		
 		if(!FavouriteManager.inFavourites(getApplicationContext(), tv.getText().toString())){
 			FavouriteManager.AddFavourite(tv.getText().toString(), getApplicationContext());
 		}
 	}
 
 	public String toString() {
 		return jData.toString();
 	}
 
 	/**
 	 * Update the timetable
 	 * */
 	private void updateList(){
 		
 		ArrayList<Listing> toBeDeleted = new ArrayList<Listing>();
 		for(Listing l : times){
 			/* If the bus leaves the stop over 5 min, add to the remove list */
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
 				Listing l = new Listing(d, route.get("Code").getAsString(),  route.get("Direction").getAsInt(), 
 						route.get("Vehicle").getAsInt(), trip.get("TripId").getAsString());
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
 	public class TimeRequest extends Request{
 		@Override
 		public void onPostExecute(String result) {
 			try {
 				jData = JParser2.main(result);
 			} catch (Exception e) {
 				Log.e("Error", "Parsing error");
 				e.printStackTrace();
				Toast toast = Toast.makeText(getApplicationContext(), "Error receiving request", Toast.LENGTH_SHORT);
				toast.show();
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
 			registered = true;
 		}
 	}
 	
 }

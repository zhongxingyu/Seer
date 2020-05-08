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
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
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
 	
 	private Boolean favsOnly = false;
 	
 	public TimeRequest tRequest;
 	
 	//Temporary favourites array
 	//private String[] favourites = {"412", "411"};
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_timetable);
 		
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayShowHomeEnabled(false);
 		actionBar.setDisplayShowTitleEnabled(true);
 		actionBar.setTitle("Timetable");
 		actionBar.setDisplayHomeAsUpEnabled(false);
 		
 		//Grab url from intent and make request
 		Intent intent = getIntent();
 		String iurl = intent.getStringExtra("timeURL");
 		tRequest = new TimeRequest();
 		tRequest.execute(iurl);
 		
 		TextView description = (TextView) findViewById(R.id.time_description);
 		description.setText(intent.getStringExtra("description"));
 		
 		timeList = (ListView) findViewById(R.id.list_view);
 		timeList.setOnItemClickListener(new OnItemClickListener(){
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View view, int pos,
 					long arg3) {
 				Listing l = times.get(pos);
 				Intent i = new Intent(view.getContext(), MapActivity.class);
 				i.putExtra("route", "http://deco3801-005.uqcloud.net/cache/network/rest/route-map-path/?route=" + l.code + "&type=" + l.type);
 				i.putExtra("stops", "http://deco3801-005.uqcloud.net/stops-from-tripID/?tripID=" + l.id);
 				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				view.getContext().startActivity(i);
 				
 			}
 			
 		});
 		
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
 				
 			//The adapter for the ListView
 			TimeAdapter ta = (TimeAdapter) timeList.getAdapter();
 				
 			if(!favsOnly){
 				view.setBackgroundResource(R.drawable.staroutline);
 				
 				//A list to hold listing that will be deleted
 				ArrayList<Listing> removeList = new ArrayList<Listing>();
 				
 				//Loop through every listing
 				for(int i = 0; i < timeList.getCount(); i ++ ){
 					Listing l = (Listing) ta.getItem(i);
 					
 					//Check if they are within the favourites array
 					Boolean listCheck = false;
 					for(FavouriteInfo fav: FavouriteManager.getFavourites(getApplicationContext())){
 						if(l.code.equals(fav.name)){
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
 			else{
 				view.setBackgroundResource(R.drawable.starfull);
 				ta.clear();
 				ta.addAll(times);
 				ta.notifyDataSetChanged();
 			}
 		}
 	}
 	
 	/*
 	public void onFavouriteButtonPush(View view) {
 		
 		TextView tv = (TextView) view.findViewById(R.id.code);
 		
 		if(!FavouriteManager.inFavourites(getApplicationContext(), tv.getText().toString())){
 			FavouriteManager.AddFavourite(tv.getText().toString(), getApplicationContext());
 		}
 	}*/
 
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
 						route.get("Vehicle").getAsInt(), trip.get("TripId").getAsString(), route.get("Name").getAsString());
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
 		ProgressDialog dialog;
 		@Override
 		public void onPreExecute(){
 			dialog= ProgressDialog.show(TimetableActivity.this, "Downloading timetable","Please wait a moment", true);
 		}
 		
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
 			dialog.dismiss();
 		}
 		
 		
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    // Inflate the menu items for use in the action bar
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.stop_map_actions, menu);
 	    return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle presses on the action bar items
 	    switch (item.getItemId()) {
 	        case R.id.action_map:
 	        	startActivity(new Intent(TimetableActivity.this, StopMapActivity.class));
 	            return true;
 	        case R.id.action_favourite:
 	        	startActivity(new Intent(TimetableActivity.this, FavouriteActivity.class));
 	            return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	
 	
 }

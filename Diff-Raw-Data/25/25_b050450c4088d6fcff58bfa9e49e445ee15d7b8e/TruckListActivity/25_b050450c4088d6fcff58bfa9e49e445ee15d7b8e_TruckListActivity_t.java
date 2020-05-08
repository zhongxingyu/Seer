 package com.ksj.bamft;
 
 import java.util.List;
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class TruckListActivity extends ListActivity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         
         //dynamically load time of day based on previous Activity.
         String timeOfDay = "Afternoon"; //set this as default for safety
         Bundle timeOfDayBundle = this.getIntent().getExtras();
         timeOfDay = timeOfDayBundle.getString("timeOfDay");
         
         //First, we get the food truck data from the API
         final DatabaseHandler db = new DatabaseHandler(this);
         final List<Schedule> scheduleList = db.getSchedulesByDayAndTime("Thursday", timeOfDay);
         
         //this part is for displaying it in the ListView
         TruckRowAdapter adapter = new TruckRowAdapter(this.getBaseContext(), R.layout.truck_row, scheduleList);
         setListAdapter(adapter);
         
         // Here is where we do the actual display.
         ListView lv = getListView();
         lv.setTextFilterEnabled(true);
         
         //ListView "toast" functionality - for each item being clicked
         lv.setOnItemClickListener(new OnItemClickListener() {
         	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
         		//Proof of concept...
         		int truck_id = scheduleList.get(position).truck_id;
         		Truck truck = db.getTruck(truck_id);
         		
         		int landmark_id = scheduleList.get(position).landmark_id;
         		Landmark landmark = db.getLandmark(landmark_id);
         		
         		String location_string = landmark.getName() + " at (" + landmark.getXcoord() + ", " + landmark.getYcoord() + ")";
         		Toast.makeText(getApplicationContext(), location_string, Toast.LENGTH_SHORT).show();
         	}
         });
         
         
     }
     
         
 }

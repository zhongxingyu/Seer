 package com.parse.starter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.parse.FindCallback;
 import com.parse.ParseGeoPoint;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseException;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.Contacts.People;
 import android.provider.ContactsContract;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.support.v4.app.*;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.SimpleCursorAdapter;
 
 
 public class ListEventsActivity extends ListActivity {
 
 	private ArrayList<ParseObject> foodEvents;
 	ProgressDialog loader;
 	String [] toDisplay;
 	ListView listView;
 	private Handler doneHandler = new Handler();
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		
 		loader = ProgressDialog.show(this, "", "Loading food events...", true);
 		new Thread(new Runnable() {
 			public void run() {
 				if (ParseApplication.checkConn(ListEventsActivity.this)) {
 					foodEvents = ParseApplication.getFoodItems();
 				}
 				else {
 					foodEvents = new ArrayList<ParseObject> ();
 					Toast.makeText(ListEventsActivity.this, "HoosEating needs an internet connection. Please enable wifi/data!", Toast.LENGTH_LONG).show();
 				}
 				doneHandler.post(new Runnable() {
 					public void run() {
 						setupListView();
 					}
 				});
 			}}).start();
 	}
 
 	protected void setupListView() {
 		loader.dismiss();
 		setListAdapter(new FoodEventAdapter(this, foodEvents));
 		listView = getListView();
 		listView.setTextFilterEnabled(true);
 		listView.setBackgroundResource(R.drawable.bg);
 		listView.setDivider(null);
 		listView.setDividerHeight(0);
 		listView.setCacheColorHint(0);
 
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 
 				Intent myIntent = new Intent(ListEventsActivity.this, ShowEventInfoActivity.class);
 				myIntent.putExtra("com.parse.starter.foodEvent_name", foodEvents.get(position).getString("name"));
 				myIntent.putExtra("com.parse.starter.foodEvent_location", foodEvents.get(position).getString("location"));
 				myIntent.putExtra("com.parse.starter.foodEvent_description", foodEvents.get(position).getString("description"));
 				myIntent.putExtra("com.parse.starter.foodEvent_start", foodEvents.get(position).getString("start_time"));
 				myIntent.putExtra("com.parse.starter.foodEvent_end", foodEvents.get(position).getString("end_time"));
 				ParseGeoPoint coords = (ParseGeoPoint) foodEvents.get(position).get("coordinates");
 				double lat = coords.getLatitude();
 				double lon = coords.getLongitude();
 				myIntent.putExtra("com.parse.starter.foodEvent_lat", lat);
 				myIntent.putExtra("com.parse.starter.foodEvent_lon", lon);
 				ListEventsActivity.this.startActivity(myIntent);
 			}
 		});
 	}
 }
 

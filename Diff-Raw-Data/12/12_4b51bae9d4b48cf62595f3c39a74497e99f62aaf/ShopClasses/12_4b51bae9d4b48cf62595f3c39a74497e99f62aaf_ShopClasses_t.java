 package com.parse.starter;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.AdapterView.OnItemClickListener;
 
 
 import com.parse.FindCallback;
 import com.parse.ParseAnalytics;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 import com.parse.ParseObject;
 import com.parse.ParseException;
 
 public class ShopClasses extends Activity {
 	private TextView test;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		ParseUser currentUser = ParseUser.getCurrentUser();
 		if (currentUser == null) {
 			Intent intent = new Intent(this, Login.class);
 			startActivity(intent);
 		}
 		setContentView(R.layout.shop);
 		ParseAnalytics.trackAppOpened(getIntent());
 		TimePicker tp = (TimePicker) findViewById(R.id.timePicker);
 
 		test = (TextView) findViewById(R.id.testing);
 
 
 		tp.setCurrentHour(Calendar.HOUR_OF_DAY);
 		tp.setCurrentMinute(Calendar.MINUTE);
 		tp.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
 
 			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
 				updateDisplay(hourOfDay, minute);
 			}
 		});
 
 
 
 		//testing code for course catalog
 		/*
 		ArrayList<String> classes = new ArrayList<String>();
 		//will turn into ArrayList<Class> classes? can parse do this
 		classes.add("testing1");
 		classes.add("testing2");
 		// have to edit the adapter to alter classroom and time
 		classList.setAdapter(new ListAdapter(this, classes));
 		 */
 
 		ParseQuery query = new ParseQuery("Course");
		

		//query.whereExists("meetings");
		
		query.whereMatches("cat_num", "4949");
 		query.findInBackground(new FindCallback() {
 			public void done(List<ParseObject> courseList, ParseException e) {
 				if (e == null) {	
 					Log.d("score", "Retrieved " + courseList.size() + " scores");
 					ListView classList = (ListView) findViewById(R.id.class_list);
 					classList.setAdapter(new ListAdapter(ShopClasses.this, courseList));
 				} else {
 					Log.d("score", "Error: " + e.getMessage());
 				}
 			}
 		});
 
 		// set each row on listview clickable to lead to individual session
 		// screens
 		/*
 		courseList.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 
 				//Open up class details
 
 			}
 		});
 		*/
 
 
 	}
 
 	private void updateDisplay(int hour, int min) {
 		//this is to test whether changing the hour does anything
 		test.setText(
 				new StringBuilder()
 				.append(pad(hour)).append(":")
 				.append(pad(min)));
 
 
 		//WHAT THIS SHOULD DO:
 		//	query and change listview according to time set
 
 	}
 
 	//helper function for int to string
 	//just saying there's a function in java that gets ascii values for you
 	private static String pad(int c) {
 		if (c >= 10)
 			return String.valueOf(c);
 		else
 			return "0" + String.valueOf(c);
 	}
 
 }

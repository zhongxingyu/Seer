 package com.example.taximap.map;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.example.taximap.*;
 
 public class ListViewActivity extends Activity implements
 		android.view.View.OnClickListener {
 	private static Activity context;
 	private static String sortField = "name";
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.content_list_layout);
 		((Button) findViewById(R.id.sort_by_name)).setOnClickListener(this);
 		((Button) findViewById(R.id.sort_by_company)).setOnClickListener(this);
 		((Button) findViewById(R.id.sort_by_rating)).setOnClickListener(this);
 		((Button) findViewById(R.id.sort_by_distance)).setOnClickListener(this);
 		context = this;
 	}
 
 	public void onResume() {
 		super.onResume();
 		createList();
 	}
 
 	public static void createList() {
 		// bind ListView and use it as the container for listitem
 		if (context == null) {
 			return;
 		}
 		ListView list = (ListView) context.findViewById(R.id.listview);
 		list.setAdapter(null);
 		ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
 		// sort DriverList by name default
 		if (MapViewActivity.markerType == Constants.DRIVER) {
 			if (MapViewActivity.driverLst == null) {
 				return;
 			}
 			if (sortField.equals("name")) {
 				Collections.sort(MapViewActivity.driverLst,
 						new Comparator<Driver>() {
 							public int compare(Driver o1, Driver o2) {
 								int compResult = o1.name.compareTo(o2.name);
 								return compResult;
 							}
 						});
 			} else if (sortField.equals("company")) {
 				Collections.sort(MapViewActivity.driverLst,
 						new Comparator<Driver>() {
 							public int compare(Driver o1, Driver o2) {
 								int compResult = o1.company
 										.compareTo(o2.company);
 								return compResult;
 							}
 						});
 			}
 			if (sortField.equals("rating")) {
 				Collections.sort(MapViewActivity.driverLst,
 						new Comparator<Driver>() {
 							public int compare(Driver o1, Driver o2) {
 								return o1.rating < o2.rating ? 1 : -1;
 							}
 						});
 			}
 			if (sortField.equals("distance")) {
 				Collections.sort(MapViewActivity.driverLst,
 						new Comparator<Driver>() {
 							public int compare(Driver o1, Driver o2) {
 								return o1.distance > o2.distance ? 1 : -1;
 							}
 						});
 			}
 			for (Driver d : MapViewActivity.driverLst) {
 				if (d.isActive) {
 					HashMap<String, String> map = new HashMap<String, String>();
 					map.put("itemtitle", d.name);
 					map.put("itemtext", d.snippet());
 					mylist.add(map);
 				}
 
 			}
 			SimpleAdapter adapter = new SimpleAdapter(context, mylist, // data
 																		// source
 					R.layout.listviewitem, // ListItem XML
 					// key correspondence
 					new String[] { "itemtitle", "itemtext" }, new int[] {
 							R.id.itemtitle, R.id.itemtext });
 			// add and display
 			list.setAdapter(adapter);
 			/*
 			 * Toast.makeText(this, Integer.toString(list.getCount()),
 			 * Toast.LENGTH_SHORT).show();
 			 */
 
 			list.setOnItemClickListener(new OnItemClickListener() {
 
 				@Override
 				public void onItemClick(AdapterView<?> parent, View view,
 						int position, long id) {
 					for (int i = 0; i < parent.getChildCount(); i++) {
 						parent.getChildAt(i).setBackgroundColor(0x000000);
 					}
 					parent.getChildAt(position)
 							.setBackgroundColor(
 									context.getResources().getColor(
 											R.color.WhiteSmoke));
 				}
 
 			});
 		}
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		switch (arg0.getId()) {
 		case R.id.sort_by_name:
 			sortField = "name";
 			createList();
 			break;
 		case R.id.sort_by_company:
 			sortField = "company";
 			createList();
 			break;
 		case R.id.sort_by_rating:
 			sortField = "rating";
 			createList();
 			break;
 		case R.id.sort_by_distance:
 			sortField = "distance";
 			createList();
 			break;
 		}
 
 	}
 
 	private boolean doubleBackToExitPressedOnce = false;
 
 	@Override
 	public void onBackPressed() { // this handler helps to reset the variable
 									// after 2 second.
 		if (doubleBackToExitPressedOnce) {
			super.onBackPressed();
 			Login.exitStatus = true;
 			MapViewActivity.diableLocationUpdate(); // remove location updates
 													// after app exits
 			return;
 		}
 		// super.onBackPressed();
 		this.doubleBackToExitPressedOnce = true;
 		Toast.makeText(this, "Please click BACK again to exit",
 				Toast.LENGTH_SHORT).show();
 		new Handler().postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				doubleBackToExitPressedOnce = false;
 			}
 		}, 2000);
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		MapViewActivity.diableLocationUpdate(); // remove location updates after
 												// app exits
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		FragmentTabsActivity.currentTabIndex = 1;
 	}
 }

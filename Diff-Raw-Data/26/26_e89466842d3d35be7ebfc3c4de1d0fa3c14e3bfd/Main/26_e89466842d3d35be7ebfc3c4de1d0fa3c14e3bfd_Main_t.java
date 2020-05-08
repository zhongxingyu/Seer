 package edu.vanderbilt.vuphone.android.dining;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class Main extends ListActivity {
 	
 	public static final boolean DEBUG = true; // austin added this to toggle the more inane log statements
 	
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setListAdapter(new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1, RESTAURANTS));
 		getListView().setTextFilterEnabled(true);
 
 	}
 	
 	private static final int VIEW_MAP = 0;
 	private static final int ETC = 1;
 	
 	static final String[] RESTAURANTS = { 
 		"Rand Dining Center", "The Commons Food Gallery", "The Common Grounds",
 			"Chef James Bistro", "Center Smoothie", "Pub at Overcup Oak", "C.T. West", "Quiznos Sub - Towers",
 			"Quiznos Sub - Morgan", "Ro*Tiki", "Starbucks", "Grins Vegetarian Cafe", "Suzie's Cafe - Engineering",
 			"Suzie's Cafe - Blair", "Suzie's Cafe - Divinity", "Nectar", "McTyeire", "Varsity - Branscomb", "Varsity - Towers", "Varsity - Morgan",
 			"Varsity - Saratt", "Hemingway Market"};
 
 	/*
 	 * Currently, only the hours for restaurants indexed 0 to 4 have been filled, and if another restaurant is 
 	 * currently clicked then application fails. Application won't fail if rest of hours arrays are filled.
 	 * Need to be able to convert String arrays below to actual Calendar objects so that it will be possible to 
 	 * compare times a restaurant opens and closes with the internal system clock.
 	 * 
 	 * Database should fix above problems,      austin: In the meantime, wouldnt it be easier to just reperesent 
 	 * 													the below with calendar objects to begin with, then convert to string?
 	 * 
 	 * Problem not addressed is that Rand like a few other restaurants opens and closes more than once in a day
 	 * 
 	 * Need to address also that in the main.xml file, I am unable to change the font color of the names of the 
 	 * restaurants in the listview. This will be necessary in order to show whether or not a restaurant is
 	 * open or closed. White font and position near top of list will indicate that a restaurant is open 
 	 * and grey font and position near the bottom of the list will indicate that a restaurant is closed.
 	 * Later ideas could include placing green light next to open names and red lights next to closed names.
 	 * 
 	 * 
 	 */
 	static final String[] SUNDAY_START = {"10:00 A.M.", "11:00 A.M.", "Closed", "4:00 P.M.", "Closed"};
 	static final String[] SUNDAY_END = {"2:00 P.M.", "8:00 P.M.", "", "7:00 P.M.", "" };
 	static final String[] MONDAY_START = {"7:00 A.M.", "7:00 A.M.", "24 hours", "7:00 A.M.", "7:00 A.M."};
 	static final String[] MONDAY_END = {"2:30 P.M.", "8:30 P.M.", "", "7:30 P.M.", "9:00 P.M." };
 	static final String[] TUESDAY_START = {"7:00 A.M.", "7:00 A.M.", "24 hours", "7:00 A.M.", "7:00 A.M."};
 	static final String[] TUESDAY_END = {"7:30 P.M.", "8:30 P.M.", "", "7:30 P.M.", "9:00 P.M."};
 	static final String[] WEDNESDAY_START = {"7:00 A.M.", "7:00 A.M.", "24 hours", "7:00 A.M.", "7:00 A.M."};
 	static final String[] WEDNESDAY_END = {"2:30 P.M.", "8:30 P.M.", "", "7:30 P.M.", "9:00 P.M."};
 	static final String[] THURSDAY_START = {"7:00 A.M.", "7:00 A.M.", "24 hours", "7:00 A.M.", "7:00 A.M."};
 	static final String[] THURSDAY_END = {"7:30 P.M.", "8:30 P.M.", "", "7:30 P.M.", "9:00 P.M."};
 	static final String[] FRIDAY_START = {"7:00 A.M.", "7:00 A.M.", "24 hours", "11:00 A.M.", "7:00 A.M."};
 	static final String[] FRIDAY_END = {"2:30 P.M.", "8:00 P.M.", "", "3:00 P.M.", "3:00 P.M."};
 	static final String[] SATURDAY_START = {"10:00 A.M.", "11:00 A.M.", "Closed", "4:00 P.M.", "Closed"};
 	static final String[] SATURDAY_END = {"2:00 P.M.", "7:00 P.M.", "", "7:00 P.M.", ""};
 	
 	/*I've only added the GPS locations for first two locations but the code can easily scale*/
	static final int[] LOCATION_LONGITUDES= {36146344 ,36141717};
	static final int[] LOCATION_LATITUDES = {-86802968,-86797094};
 	
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 
 		Log.i("dining", "position " + position + " was clicked");
 
 		Intent toDetails = new Intent(this, RestaurantDetails.class);
 		toDetails.putExtra("restaurant", position);
 
 		startActivity(toDetails);
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 		 menu.add(0, VIEW_MAP, 0, "View Map");
 	     menu.add(0, ETC, 0, "Etc..");
 	     return true;
     }
 	    
 	public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
     	switch (item.getItemId()) {
         case VIEW_MAP:
         	Intent i = new Intent(this, Map.class);
         	i.putExtra("longitudes", LOCATION_LONGITUDES);
         	i.putExtra("latitudes", LOCATION_LATITUDES);
         	i.putExtra("locations", RESTAURANTS);
         	startActivity(i);
             return true;
         case ETC:
             return true;
         }
         return false;
     }
 
 	
 }

 package com.seawolfsanctuary.keepingtracks.stats;
 
 import java.util.ArrayList;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
import android.widget.ListView;
 import android.widget.Toast;
 
 import com.seawolfsanctuary.keepingtracks.MenuActivity;
 import com.seawolfsanctuary.keepingtracks.R;
 import com.seawolfsanctuary.keepingtracks.UserPrefsActivity;
 
 public class StatsActivity extends org.holoeverywhere.app.ListActivity {
 	private ArrayList<String> names = new ArrayList<String>();
 	private ArrayList<String> activities = new ArrayList<String>();
 
 	SharedPreferences settings;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		settings = getSharedPreferences(UserPrefsActivity.APP_PREFS,
 				MODE_PRIVATE);
 
 		names.add(getApplicationContext().getString(
 				R.string.stats_jouneys_month));
 		names.add(getApplicationContext().getString(
 				R.string.stats_favourite_stations));
 
 		activities.add("stats.JourneysByMonth");
 		activities.add("stats.FavouriteStations");
 
 		if (settings.getBoolean("AdvancedJourneys", false) == true) {
 			names.add(getApplicationContext().getString(
 					R.string.stats_classes_used));
 			activities.add("stats.ClassesUsed");
 		}
 
 		setListAdapter(new ArrayAdapter<String>(this,
 				R.layout.stats_activity_list, names));
 
 		ListView lv = getListView();
 		registerForContextMenu(lv);
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				String className = "com.seawolfsanctuary.keepingtracks."
 						+ activities.get(position);
 				try {
 					Intent intent;
 					intent = new Intent(view.getContext(), Class
 							.forName(className));
 					startActivity(intent);
 				} catch (ClassNotFoundException e) {
 					Toast.makeText(
 							getApplicationContext(),
 							getString(R.string.stats_activity_error, className),
 							Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 		MenuActivity.hideLoader();
 	}
 }

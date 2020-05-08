 package com.seawolfsanctuary.keepingtracks.stats;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Toast;
 
 import com.seawolfsanctuary.keepingtracks.Helpers;
 import com.seawolfsanctuary.keepingtracks.R;
 import com.seawolfsanctuary.keepingtracks.UserPrefsActivity;
 import com.seawolfsanctuary.keepingtracks.database.Journey;
 
 public class ClassesUsed extends ListActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.stats_classes_used);
 
 		ArrayList<String> allClasses = loadSavedEntries(true);
 		Hashtable<String, Integer> classesUsed = new Hashtable<String, Integer>();
 		for (String journeyClasses : allClasses) {
 			for (String classUsed : Journey
 					.classesStringToArrayList(journeyClasses)) {
 				int count = 0;
 				if (classesUsed.keySet().contains(classUsed)) {
 					count = classesUsed.get(classUsed);
 				}
 				classesUsed.put(classUsed, 1 + count);
 			}
 		}
 
 		ArrayList<String> listContents = new ArrayList<String>();
 		for (String classUsed : classesUsed.keySet()) {
			if (classesUsed.get(classUsed) == 1) {
 				listContents.add(getString(
 						R.string.stats_classes_used_line_single, classUsed,
 						classesUsed.get(classUsed)));
 			} else {
 				listContents.add(getString(
 						R.string.stats_classes_used_line_multiple, classUsed,
 						classesUsed.get(classUsed)));
 			}
 		}
 		Collections.sort(listContents);
 
 		setListAdapter(new ArrayAdapter<String>(this,
 				R.layout.stats_classes_used_list, listContents));
 
 		getListView().setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> adapterView, View childView,
 					int position, long id) {
 				Object itemView = adapterView.getItemAtPosition(position);
 				String itemContent = itemView.toString();
 				String classNo = itemContent.substring(0,
 						itemContent.indexOf("(") - 1);
 				File f = new File(Helpers.dataDirectoryPath + "/class_photos/",
 						classNo);
 				if (f.exists()) {
 					Intent i = new Intent(Intent.ACTION_VIEW);
 					i.setDataAndType(
 							Uri.parse(Helpers.dataDirectoryURI
 									+ "/class_photos/" + classNo), "image/*");
 					startActivity(i);
 				} else {
 					Toast.makeText(
 							getBaseContext(),
 							getString(R.string.class_info_download_unavailable),
 							Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 	}
 
 	private ArrayList<String> loadSavedEntries(boolean showToast) {
 		ArrayList<String> allClasses = new ArrayList<String>();
 		SharedPreferences settings = getSharedPreferences(
 				UserPrefsActivity.APP_PREFS, MODE_PRIVATE);
 
 		Journey db_journeys = new Journey(this);
 		db_journeys.open();
 
 		Cursor c;
 		if (settings.getBoolean("AlwaysUseStats", false) == true) {
 			c = db_journeys.getAllJourneys();
 		} else {
 			c = db_journeys.getAllStatsJourneys();
 		}
 
 		if (c.moveToFirst()) {
 			do {
 				System.out.println("Reading row #" + c.getInt(0) + "...");
 				allClasses.add(c.getString(13));
 				;
 			} while (c.moveToNext());
 		}
 		db_journeys.close();
 
 		if (showToast) {
 			if (allClasses.size() == 1) {
 				Toast.makeText(
 						getBaseContext(),
 						getString(R.string.list_saved_loaded_single,
 								allClasses.size()), Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(
 						getBaseContext(),
 						getString(R.string.list_saved_loaded_multiple,
 								allClasses.size()), Toast.LENGTH_SHORT).show();
 			}
 		}
 
 		return allClasses;
 	}
 }

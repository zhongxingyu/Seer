 package no.group09.ucsoftwarestore;
 
 /*
  * Licensed to UbiCollab.org under one or more contributor
  * license agreements.  See the NOTICE file distributed 
  * with this work for additional information regarding
  * copyright ownership. UbiCollab.org licenses this file
  * to you under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import no.group09.ucsoftwarestore.R;
 import no.group09.database.DatabaseHandler;
 import no.group09.database.Save;
 import no.group09.fragments.MyFragmentPagerAdapter;
 import no.group09.utils.Devices;
 import no.group09.utils.Preferences;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class MainActivity extends FragmentActivity {
 
 	private String TAG = "MainActivity";
 
 	public static MyFragmentPagerAdapter pagerAdapter;
 	public static ViewPager pager;
 	private Save save;
 
 	//Name of the preference file
 	public static final String PREFS_NAME = "PreferenceFile";
 
 	private SharedPreferences sharedPref = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		/** Getting a reference to the ViewPager defined the layout file */
 		pager = (ViewPager) findViewById(R.id.pager);
 
 		/** Getting fragment manager */
 		FragmentManager fm = getSupportFragmentManager();
 
 		/** Instantiating FragmentPagerAdapter */
 		pagerAdapter = new MyFragmentPagerAdapter(fm);
 
 		/** Setting the pagerAdapter to the pager object */
 		pager.setAdapter(pagerAdapter);
 
 		//Initializing the settings for the application
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 
 		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 
		/** Create the database if it does not excist, or copy it into the application */
		save = new Save(getBaseContext());

 		//This clears the database
 //		getBaseContext().deleteDatabase(DatabaseHandler.DATABASE_NAME);
 
 		//This populates the database: false because we dont want to use content provider
 //		save.populateDatabase(true);
 	}
 
 	@Override
 	public void onPause(){
 		super.onPause();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main_menu, menu);
 
 		Editor edit = sharedPref.edit();
 
 		//When the menu is created, check the preferences and set the correct text
 		if(sharedPref.getBoolean("hide_incompatible", false)){
 			menu.getItem(1).setTitle("Show incompatible");
 		}
 		else{
 			menu.getItem(1).setTitle("Hide incompatible");
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 
 		case R.id.toggle_incompitable:
 
 			//Prepare to edit the setting
 			Editor edit = sharedPref.edit();
 
 			//Fetches the current value of the 'hide incompatible' option in the preference file
 			boolean hideIncompatible = sharedPref.getBoolean("hide_incompatible", false);
 
 			if(hideIncompatible == true){
 
 				//Changes the value and commits the changes
 				edit.putBoolean("hide_incompatible", false);
 				edit.commit();
 
 				item.setChecked(false);
 
 				//Set new text when item is clicked
 				item.setTitle("Hide incompatible");
 			}
 			else{
 
 				//Changes the value and commits the changes
 				edit.putBoolean("hide_incompatible", true);
 				edit.commit();
 
 				item.setChecked(true);
 
 				//Set new text when item is clicked
 				item.setTitle("Show incompatible");
 			}
 
 			return true;
 
 			//Start the preferences class
 		case R.id.settings:
 			//Create an intent to start the preferences activity
 			Intent myIntent = new Intent(getApplicationContext(), Preferences.class);
 			this.startActivity(myIntent);
 			return true;
 
 
 			//Show the device list
 		case R.id.device_list:
 			Intent intent = new Intent(this, Devices.class);	//FIXME: is 'this' an Activity?
 			startActivity(intent);
 			return true;
 
 			//		case R.id.add_device:
 			//			startActivity(new Intent(this, AddDeviceScreen.class));
 			//			return true;
 
 			//This is just for testing purposes. Remove when done.
 			//TODO: Remove when done testing the application view.
 			//		case R.id.application_view:
 			//			startActivity(new Intent(this, AppView.class));
 			//			return true;
 			//		}
 			//
 			//		//The item was none of the following
 		default : return false;
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 }

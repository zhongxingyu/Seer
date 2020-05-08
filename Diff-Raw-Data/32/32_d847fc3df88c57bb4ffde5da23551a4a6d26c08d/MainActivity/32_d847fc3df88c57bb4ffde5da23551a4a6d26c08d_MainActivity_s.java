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
 import no.group09.database.Save;
 import no.group09.fragments.MyFragmentPagerAdapter;
 import no.group09.fragments.Page;
 import no.group09.utils.BtArduinoService;
 import no.group09.utils.Devices;
 import no.group09.utils.Preferences;
 import android.annotation.SuppressLint;
 import android.app.SearchManager;
 import android.content.Context;
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
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.SearchView;
 import android.widget.Toast;
 
 public class MainActivity extends FragmentActivity {
 
 	/** The fragment adapter for the tabs */
 	public static MyFragmentPagerAdapter pagerAdapter;
 
 	/** The ViewPager from xml */
 	public static ViewPager pager;
 
 	/** Name of the preference file */
 	public static final String PREFS_NAME = "PreferenceFile";
 
 	/** The shared preference object */
 	private SharedPreferences sharedPref = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		//Getting a reference to the ViewPager defined the layout file
 		pager = (ViewPager) findViewById(R.id.pager);
 
 		//Getting fragment manager
 		FragmentManager fm = getSupportFragmentManager();
 
 		//Instantiating FragmentPagerAdapter
 		pagerAdapter = new MyFragmentPagerAdapter(fm, this);
 
 		//Setting the pagerAdapter to the pager object
 		pager.setAdapter(pagerAdapter);
 
 		//Initializing the settings for the application
 		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 
 		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
<<<<<<< HEAD
=======
 
 		//This clears the database
 		//		getBaseContext().deleteDatabase(DatabaseHandler.DATABASE_NAME);
 
 		/** Create the database if it does not excist, or copy it into the application */
		save = new Save(getBaseContext());
 
 		//This populates the database: false because we dont want to use content provider
 		// save.populateDatabase();
 
 		//		sets up search with search dialog. For older versions!
 		Intent intent = getIntent();
 		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
 			String query = intent.getStringExtra(SearchManager.QUERY);
 			//	      Do the search here
 			Toast.makeText(getBaseContext(), "You searched for " + query, Toast.LENGTH_SHORT).show();
 		}
 
 
>>>>>>> Adding search functionality
 	}
 
 	@Override
 	public void onPause(){
 		super.onPause();
 	}
 
 	@SuppressLint("NewApi")
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main_menu, menu);
 
 		//When the menu is created, check the preferences and set the correct text
 		if(sharedPref.getBoolean("hide_incompatible", false)){
 			menu.getItem(1).setTitle("Show incompatible");
 		}
 		else{
 			menu.getItem(1).setTitle("Hide incompatible");
 		}
 
 		//		Search bar for versions over API level 11
 		int SDK_INT = android.os.Build.VERSION.SDK_INT; //gets the version of the device
 		if(SDK_INT >= 11){ 
 			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 			SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
 			searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
 			searchView.setSubmitButtonEnabled(true);
 
 
 		}
 
 		return true;
 	}
 	
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 
 //		Makes sure search works for older versions
 		case R.id.menu_search:
             onSearchRequested();
 
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
 
 			pagerAdapter.all.update();
 			pagerAdapter.topHits.update();
 			pagerAdapter.notifyDataSetChanged();
 
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
 
 		case R.id.populateDatabase:
 			Save save = new Save(getBaseContext());
 			save.populateDatabase();
 
 			pagerAdapter.all.update();
 			pagerAdapter.topHits.update();
 			pagerAdapter.notifyDataSetChanged();
 
 		default : return false;
 		}
 	}
 
 	/** Add the connected device name to the title if connected */
 	public void setActivityTitle(){
 		String appName = sharedPref.getString("connected_device_name", "null");
 
 		//Get the enum type for what type (category) is selected
 		MyFragmentPagerAdapter pageAdapter = MainActivity.pagerAdapter;
 
 		//Get the name of the selected category
 		String category = Page.getCategoryFromType(pageAdapter.page1);
 
 		if(BtArduinoService.getBtService() != null){
 			if(BtArduinoService.getBtService().getBluetoothConnection() != null){
 				setTitle(category + " - " + appName);
 			}
 			else{
 				setTitle(category);
 			}
 		}
 		else{
 			setTitle(category);
 		}
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		setActivityTitle();
 	}
 }

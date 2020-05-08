 package com.Akkad.AndroidBackup;
 
 import android.app.TabActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 
 import com.stericson.RootTools.RootTools;
 
 public class AndroidBackupActivity extends TabActivity {
 	/** Called when the activity is first created. */
 	public void onCreate(Bundle savedInstanceState) {
 		RootTools.debugMode = true;
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		TabHost tabHost = getTabHost();
 
 		// Tab for Device Information
 		TabSpec informationSpec = tabHost.newTabSpec(getResources().getString(R.string.information_tab_title));
 		informationSpec.setIndicator(getResources().getString(R.string.information_tab_title));
 		Intent photosIntent = new Intent(this, InformationActivity.class);
 		informationSpec.setContent(photosIntent);
 
 		// Tab for Tasks
 		TabSpec tasksSpec = tabHost.newTabSpec(getResources().getString(R.string.tasks_tab_title));
 		tasksSpec.setIndicator(getResources().getString(R.string.tasks_tab_title));
 		Intent tasksIntent = new Intent(this, TasksActivity.class);
 		tasksSpec.setContent(tasksIntent);
 
 		// Tab for Applications
 		TabSpec applicationSpec = tabHost.newTabSpec(getResources().getString(R.string.applications_tab_title));
 		applicationSpec.setIndicator(getResources().getString(R.string.applications_tab_title));
 		Intent applicationsIntent = new Intent(this, ApplicationsActivity.class);
 		applicationSpec.setContent(applicationsIntent);
 
 		// Tab for Schedule
 		TabSpec scheduleSpec = tabHost.newTabSpec(getResources().getString(R.string.schedule_tab_title));
 		scheduleSpec.setIndicator(getResources().getString(R.string.schedule_tab_title));
 		Intent scheduleIntent = new Intent(this, ScheduleActivity.class);
 		scheduleSpec.setContent(scheduleIntent);
 
 		if (RootTools.isRootAvailable() && RootTools.isAccessGiven() && RootTools.isBusyboxAvailable()) {
 			// Add all TabSpec to TabHost
 			tabHost.addTab(informationSpec); // Add Information tab
 			tabHost.addTab(tasksSpec); // Add Tasks tab
 			tabHost.addTab(applicationSpec); // Add Applications tab
 			tabHost.addTab(scheduleSpec); // Add Schedule tab
 		} else {
 			tabHost.setClickable(false);
 			tabHost.addTab(informationSpec); // Add Information tab
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 	}
 
 	/**
 	 * Called first time user clicks on the menu button
 	 */
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 		return true;
 	}
 
 }

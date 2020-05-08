 package com.raouldc.uoacompsci;
 
 
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuItem;
 
 
 public class MainActivity extends FragmentActivity implements
 		ActionBar.TabListener {
 
 	Tab tab;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Set up the action bar.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         // Create first Tab
         tab = actionBar.newTab().setTabListener(new HomeSectionFragment());
         // Create your own custom icon
         tab.setText(R.string.title_section1);
         actionBar.addTab(tab);
  
         // Create Second Tab
         tab = actionBar.newTab().setTabListener(new CoursesSectionFragment());
         // Set Tab Title
         tab.setText(R.string.courses_section);
         actionBar.addTab(tab);
  
         // Create Third Tab
         tab = actionBar.newTab().setTabListener(new StaffSectionFragment());
         // Set Tab Title
         tab.setText(R.string.staff_section);
         actionBar.addTab(tab);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
	//create activity when settings is pressed
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.action_settings:
             Intent i = new Intent(this, SettingsActivity.class);
             startActivityForResult(i, 1);
             break;
         }
         return true;
     }
 
 	@Override
 	public void onTabSelected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 
 	}
 
 	@Override
 	public void onTabUnselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 
 	@Override
 	public void onTabReselected(ActionBar.Tab tab,
 			FragmentTransaction fragmentTransaction) {
 	}
 	
 }

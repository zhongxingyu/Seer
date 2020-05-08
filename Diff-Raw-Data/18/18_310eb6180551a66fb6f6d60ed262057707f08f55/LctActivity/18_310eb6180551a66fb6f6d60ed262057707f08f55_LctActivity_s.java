 package com.typeiisoft.lct;
 
 import java.io.IOException;
 import java.util.Calendar;
 
 import com.mhuss.AstroLib.Astro;
 import com.typeiisoft.lct.db.DataBaseHelper;
 import com.typeiisoft.lct.utils.AppPreferences;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 public class LctActivity extends FragmentActivity {
 	/** The application preferences object. */
 	private AppPreferences appPrefs;
 	private static final String TAG = "LctActivity";
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         // Set up the ActionBar
         final ActionBar actionBar = getActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         // Set the observation date and time into the shared preferences.
     	this.appPrefs = new AppPreferences(this);
     	Calendar now = Calendar.getInstance();
     	int offset = now.getTimeZone().getOffset(now.getTimeInMillis()) / Astro.MILLISECONDS_PER_HOUR;
 		// Set time to UTC, offset will allow calc of local time later.
 		now.add(Calendar.HOUR_OF_DAY, -offset);
 		
 		this.appPrefs.setDateTime(now.get(Calendar.DATE), 
 				now.get(Calendar.MONTH)+1, now.get(Calendar.YEAR), 
 				now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), 
 				now.get(Calendar.SECOND), offset);
 
         // Copy the Moon information database
         DataBaseHelper moonDB = new DataBaseHelper(this);
         try {
         	moonDB.createDataBase();
         }
         catch (IOException ioe) {
         	throw new Error("Unable to open Moon info database.");
         }
         /**
         // Open the handle to the Moon information
         try {
         	moonDB.openDataBase();
         }
         catch (SQLException sqle) {
         	throw sqle;
         }
         // DB should now be open and ready
         */
         // Create a tab to set a Fragment (to be reused)
         Tab tab = actionBar.newTab()
         		.setText(R.string.moon_info_tab)
         		.setTabListener(new MainTabListener<MoonInfoFragment>(
         				this, "mooninfo", MoonInfoFragment.class));
         actionBar.addTab(tab);
         		
         tab = actionBar.newTab()
         		.setText(R.string.lunar_club_tab)
         		.setTabListener(new MainTabListener<LunarClubFragment>(
         				this, "lunarclub", LunarClubFragment.class));
         actionBar.addTab(tab);
         
         tab = actionBar.newTab()
         		.setText(R.string.lunar2_club_tab)
         		.setTabListener(new MainTabListener<LunarTwoFeaturesFragment>(
         				this, "lunar2club", LunarTwoFeaturesFragment.class));
         actionBar.addTab(tab);
         
         //moonDB.close();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.menu, menu);
     	return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     	case R.id.about:
     		startActivity(new Intent(this, About.class));
     		return true;
     	default:
     		return super.onOptionsItemSelected(item);
     	}
     	
     }
 }

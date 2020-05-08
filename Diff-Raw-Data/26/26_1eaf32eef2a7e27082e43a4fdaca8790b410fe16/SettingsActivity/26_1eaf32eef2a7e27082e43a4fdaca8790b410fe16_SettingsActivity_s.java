 package com.alvarosantisteban.pathos.preferences;
 
 import android.app.ActionBar;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.util.Log;
 import android.view.MenuItem;
 
 import com.alvarosantisteban.pathos.DateActivity;
 import com.alvarosantisteban.pathos.R;
 
 /**
  * Displays a limited set of settings if the device is older than Honeycomb or the settings fragment if the device is newer.
  * 
  * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
  *
  */
 public class SettingsActivity extends PreferenceActivity  {
 	
 	/**
 	 * Used for logging purposes
 	 */
 	private static final String TAG = "SettingsActivity";
 	
 	/**
 	 * Preference for the list with possible organizations
 	 */
 	public static final String KEY_PREF_LIST_ORGANIZATIONS = "possible_organizations_list";
 	ListPreference organizationList; 
 	Context context;
 	
 	@SuppressWarnings("deprecation")
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
         // Display the fragment as the main content.
         getFragmentManager().beginTransaction()
                 .replace(android.R.id.content, new SettingsFragment())
                 .commit();
 	   
         // Enable the app's icon to act as home
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		} else{
 			// Settings for older versions
 			context = this;
 			addPreferencesFromResource(R.xml.old_devices_preferences);
 			organizationList = (ListPreference) findPreference(KEY_PREF_LIST_ORGANIZATIONS); 
 			// Set the intent for the Manifesto preference (triggered when clicked)
 	        findPreference("manifesto").setIntent(new Intent(this, ManifestoActivity.class));
 	        // Set the intent for the About preference (triggered when clicked)
 	        findPreference("about").setIntent(new Intent(this, AboutActivity.class));
 	        // Set the intent for the Legal Notices preference (triggered when clicked)
 	        findPreference("legal").setIntent(new Intent(this, LegalNoticesActivity.class));
 	        organizationList.setOnPreferenceChangeListener(preferenceListener); 
 		}
 
     }
 	
 	/**
 	 * The listener for the changes of the preference list of possible organizations 
 	 */
 	private OnPreferenceChangeListener preferenceListener = new Preference.OnPreferenceChangeListener() {
 
   	  public boolean onPreferenceChange(Preference preference, Object newValue) {
   		  Log.d(TAG, "onPreferenceChange");
   		  Intent i = new Intent(context, DateActivity.class);
   		  i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
   		  startActivity(i);
   		  return true;
   	  }
 	};
 	
 	/**
 	 * Checks which item from the menu has been clicked
 	 */
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == android.R.id.home) {
 			// app icon in action bar clicked; go home
 			Intent intent = new Intent(this, DateActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 		}
         return true;
     }
 }

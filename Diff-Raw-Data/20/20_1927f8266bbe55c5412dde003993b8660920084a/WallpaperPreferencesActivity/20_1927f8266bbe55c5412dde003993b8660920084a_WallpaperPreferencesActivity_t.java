 package com.supervaca.wallpaperChanger;
 
 import com.lamerman.FileDialogWithDirectories;
 import com.lamerman.SelectionMode;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.util.Log;
 
 public class WallpaperPreferencesActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
     private static final int SELECT_DIRECTORY_RESULT = 777;
 	public static final String SERVICE_ACTIVE = "SERVICE_ACTIVE";
     public static final String UPDATE_INTERVAL = "UPDATE_INTERVAL";
     public static final String SELECTED_DIRECTORY = "SELECTED_DIRECTORY";
 
     private ListPreference updateIntervalListPref;
     private CheckBoxPreference serviceActiveCheckBoxPref;
     private Preference selectedDirectoryPreference;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Load the XML preferences file
         addPreferencesFromResource(R.xml.preferences);
 
         // Get a reference to the preferences
         updateIntervalListPref = (ListPreference) getPreferenceScreen().findPreference(UPDATE_INTERVAL);
         serviceActiveCheckBoxPref = (CheckBoxPreference) getPreferenceScreen().findPreference(SERVICE_ACTIVE);
         selectedDirectoryPreference = getPreferenceScreen().findPreference(SELECTED_DIRECTORY);
         
         // Manually add a listener to selectDirectoryPreference, this is the only way to
         // invoke startActivityForResult and pass the right extras
 		selectedDirectoryPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 			@Override
 			public boolean onPreferenceClick(Preference preference) {
 				Intent intent = new Intent(getBaseContext(), FileDialogWithDirectories.class);
 				intent.putExtra(FileDialogWithDirectories.CAN_SELECT_DIR, true);
 				intent.putExtra(FileDialogWithDirectories.SELECTION_MODE, SelectionMode.MODE_OPEN);
 
 				startActivityForResult(intent, SELECT_DIRECTORY_RESULT);
 
 				return true;
 			}
 		});
     }
 
     @Override
 	protected void onResume() {
 		super.onResume();
 
 		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		
		updatePreferencesEnabled();
 
 		// Setup the initial values
 		updateIntervalListPref.setSummary(updateIntervalListPref.getEntry());
 		selectedDirectoryPreference.setSummary(sharedPreferences.getString(SELECTED_DIRECTORY,
 				getText(R.string.directory_selection_none_selected).toString()));
 
 		// Set up a listener whenever a key changes
 		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
 	}
 
    /**
     * Enable/disable serviceActive if an interval or directory has not been selected yet. 
     */
	private void updatePreferencesEnabled() {
		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		
		if(sharedPreferences.getString(SELECTED_DIRECTORY, "").equals("") || updateIntervalListPref.getValue().equals("")) {
			serviceActiveCheckBoxPref.setEnabled(false);
		} else {
			serviceActiveCheckBoxPref.setEnabled(true);
		}
	}

     @Override
     protected void onPause() {
         super.onPause();
 
         // Unregister the listener whenever a key changes
         getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
     }
 
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
         if (key.equals(UPDATE_INTERVAL)) {
             updateIntervalListPref.setSummary(updateIntervalListPref.getEntry());
            updatePreferencesEnabled();
         } else if (key.equals(SERVICE_ACTIVE)) {
             Log.i("wallpaperChanger", serviceActiveCheckBoxPref.isChecked() + "");
 
             // Create the intents
             Intent intent = new Intent(WallpaperService.REFRESH_WALLPAPER);
             PendingIntent pendingIntent = PendingIntent.getService(WallpaperPreferencesActivity.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 
             // Schedule the alarm!
             long firstTime = SystemClock.elapsedRealtime();
             AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
 
             if (serviceActiveCheckBoxPref.isChecked()) {
             	Long refreshIntervalMs = Long.parseLong(sharedPreferences.getString(UPDATE_INTERVAL, "0"));
             	Log.d("refreshIntervalMs", refreshIntervalMs.toString());
                 // Schedule the alarm
 				am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, firstTime, refreshIntervalMs, pendingIntent);
             } else {
                 // Cancel the alarm
                 am.cancel(pendingIntent);
             }
         } else if (key.equals(SELECTED_DIRECTORY)) {
        	updatePreferencesEnabled();
         	selectedDirectoryPreference.setSummary(sharedPreferences.getString(SELECTED_DIRECTORY,
     				getText(R.string.directory_selection_none_selected).toString()));
         }
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	if(SELECT_DIRECTORY_RESULT == requestCode) {
     		// The path selected in the dialog
 			String resultPath = data.getStringExtra(FileDialogWithDirectories.RESULT_PATH);
 			
 			// We need to listen for changes
 			SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
 			sharedPreferences.registerOnSharedPreferenceChangeListener(this);
 			
 			// Update the property with the resultPath
 			selectedDirectoryPreference.getEditor().putString(SELECTED_DIRECTORY, resultPath).commit();
     	}
     }
 }

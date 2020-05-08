 package tk.crazysoft.ego;
 
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.support.v4.content.LocalBroadcastManager;
 import android.view.View;
 import android.widget.BaseAdapter;
 import android.widget.ProgressBar;
 
 import tk.crazysoft.ego.io.ExternalStorage;
 import tk.crazysoft.ego.preferences.DataImportPreference;
 import tk.crazysoft.ego.preferences.Preferences;
 import tk.crazysoft.ego.services.DataImportReceiver;
 import tk.crazysoft.ego.services.DataImportService;
 
 public class PreferencesActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
     private DataImportReceiver importReceiver;
     private ProgressBar progressBar;
 
     private static final int ITEM_IMPORT_ADDRESSES = 0;
     private static final int ITEM_IMPORT_HOSPITAL_ADMITTANCES = 1;
     private static final int ITEM_IMPORT_DOCTOR_STANDBY = 2;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         addPreferencesFromResource(R.xml.preferences);
         setContentView(R.layout.preferences_activity);
 
         progressBar = (ProgressBar)findViewById(R.id.preferences_progressBar);
 
         IntentFilter importFilter = new IntentFilter(DataImportService.BROADCAST_ERROR);
         importFilter.addAction(DataImportService.BROADCAST_PROGRESS);
         importFilter.addAction(DataImportService.BROADCAST_RESULT_IMPORT);
         importFilter.addAction(DataImportService.BROADCAST_RESULT_POSTPROCESS);
         importReceiver = new DataImportReceiver(this);
         LocalBroadcastManager.getInstance(this).registerReceiver(importReceiver, importFilter);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
 
         String sdPath = ExternalStorage.getSdCardPath();
         if (sdPath == null) {
             sdPath = ExternalStorage.getSdCardPath(true);
         }
 
         DataImportPreference addressPreference = (DataImportPreference)getPreferenceManager().findPreference(Preferences.PREFERENCE_IMPORT_ADDRESSES);
         DataImportPreference hospitalsPreference = (DataImportPreference)getPreferenceManager().findPreference(Preferences.PREFERENCE_IMPORT_HOSPITALS);
         DataImportPreference doctorsPreference = (DataImportPreference)getPreferenceManager().findPreference(Preferences.PREFERENCE_IMPORT_DOCTORS);
        ListPreference apiPreference = (ListPreference)getPreferenceManager().findPreference(Preferences.PREFERENCE_NAVIGATION_API);
 
         addressPreference.setSummary(String.format(addressPreference.getSummary().toString(), sdPath));
         addressPreference.setBroadcastReceiver(importReceiver);
         hospitalsPreference.setSummary(String.format(hospitalsPreference.getSummary().toString(), sdPath));
         hospitalsPreference.setBroadcastReceiver(importReceiver);
         doctorsPreference.setSummary(String.format(doctorsPreference.getSummary().toString(), sdPath));
         doctorsPreference.setBroadcastReceiver(importReceiver);
        apiPreference.setSummary(getResources().getString(R.string.preferences_activity_navigation_api_summary, apiPreference.getEntry()));
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
     }
 
     @Override
     protected void onDestroy() {
         LocalBroadcastManager.getInstance(this).unregisterReceiver(importReceiver);
         super.onDestroy();
     }
 
     public void setManagementProgressBarVisibility(boolean visible) {
         progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
     }
 
     public void setManagementProgress(int progress) {
         progressBar.setProgress(progress);
     }
 
     @Override
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
         Preference pref = findPreference(key);
         if (key.equals(Preferences.PREFERENCE_NAVIGATION_API)) {
             ListPreference apiPreference = (ListPreference)getPreferenceScreen().findPreference(key);
             pref.setSummary(getResources().getString(R.string.preferences_activity_navigation_api_summary, apiPreference.getEntry()));
         } else if (key.equals(Preferences.PREFERENCE_HOSPITALS_DOCTORS_TAKEOVER)) {
             ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
         }
     }
 }

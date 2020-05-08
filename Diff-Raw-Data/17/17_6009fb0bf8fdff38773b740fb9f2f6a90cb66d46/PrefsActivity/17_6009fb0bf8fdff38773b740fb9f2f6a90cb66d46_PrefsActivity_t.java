 package com.indivisible.tortidy;
 
 import android.content.*;
 import android.os.*;
 import android.preference.*;
//import android.preference.OnPreferenceClickListener;
import android.widget.Toast;
 import android.util.*;
 import java.io.*;
 
 public class PrefsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener
 {
     private static final String TAG = "com.indivisible.tortidy";
 	
     //// preferences
 	private EditTextPreference prefEtDirDownloads;
 	private EditTextPreference prefEtDirUploads;
 	private EditTextPreference prefEtLabels;
 	
 	private Preference prefClearPrefs;
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.preferences);
 		initPrefs();
 	}
 	
 	/** initialise preferences and tie to ui **/
 	private void initPrefs() {
 		prefEtDirDownloads = (EditTextPreference) findPreference(getString(R.string.pref_dirs_down_key));
 		prefEtDirUploads   = (EditTextPreference) findPreference(getString(R.string.pref_dirs_up_key));
 		
 		prefEtLabels       = (EditTextPreference) findPreference(getString(R.string.pref_labels_key));
 		
 		prefClearPrefs     = findPreference(getString(R.string.pref_debug_clearprefs_key));
		prefClearPrefs.setOnPreferenceClickListener(this);
 		
 		populateDirPrefs(this.getApplicationContext());
 	}
 	
 	
 	private void populateDirPrefs(Context ctx) {
 		if (prefEtDirDownloads.getText().equals("")) {
 			Log.d(TAG, "Download dir not set. Saving default");
 			File defDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
 			prefEtDirDownloads.setText(defDownloads.getAbsolutePath());
 		}
 		if (prefEtDirUploads.getText().equals("")) {
 			Log.d(TAG, "Upload dir not set. Saving default");
 			File defDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
 			prefEtDirUploads.setText(defDownloads.getAbsolutePath());
 		}
 	}
 	
 	/** special handling of preference clicks **/
	@Override
 	public boolean onPreferenceClick(Preference p1)
 	{
		Log.i(TAG, "pref click: " +p1.getKey());
		Toast.makeText(this, "pref click: " +p1.getKey(), Toast.LENGTH_SHORT).show();
		
 		if (p1.equals(prefClearPrefs)) {
 			Log.d(TAG, "clearing all preferences");
			Toast.makeText(this, "Preferences cleared", Toast.LENGTH_SHORT).show();
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 			SharedPreferences.Editor editor = prefs.edit();
 			editor.clear();
 			editor.commit();
			finish();
 		}
 		// notify click handled
 		return true; //false to pass on?
 	}
 	
 }

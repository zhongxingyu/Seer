 package com.delin.speedlogger.Activities;
 
 import java.io.File;
 
 import com.delin.speedlogger.R;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.ListPreference;
 import android.preference.PreferenceActivity;
 
 public class DevPrefsActivity extends PreferenceActivity {
 	static final String CREATOR_VALUE =		"SpeedLogger";
 	static final String STORAGE_DIR = 		Environment.getExternalStorageDirectory().getPath() +"/"+CREATOR_VALUE;
 	static final String GPS_DIR_NAME = 		"FileGPS";
 	static final String GPS_DIR_PATH = 		STORAGE_DIR + "/" + GPS_DIR_NAME;
 	ListPreference gpxFiles;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 	    super.onCreate(savedInstanceState);
 	    String prefsName = getString(R.string.DevPrefs);
 	    getPreferenceManager().setSharedPreferencesName(prefsName);
 	    addPreferencesFromResource(R.xml.dev_preferences);
 	    gpxFiles = (ListPreference) findPreference(getString(R.string.FileWithGPS));
 	    FindGPX();
 	}
 	
 	// adds all filenames from GPS folder into the gpx list
 	private void FindGPX() {
 		File dir = new File(GPS_DIR_PATH);
 		try{ if(!dir.exists()) dir.mkdirs();} catch(Exception e){return;}
 		String[] filenames = dir.list();
 		if (filenames.length > 0) {
			gpxFiles.setEntries(filenames);
 			for(int i=0; i<filenames.length; ++i) {
 				filenames[i] = GPS_DIR_NAME + "/" + filenames[i];
 			}
 			gpxFiles.setEntryValues(filenames);
 			if (gpxFiles.getValue() == null) gpxFiles.setValueIndex(0);
 		}
 	}
 
 }

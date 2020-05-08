 package com.seawolfsanctuary.tmt;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Toast;
 
 public class UserPrefsActivity extends Activity {
	public static final String PREFS_APP = "ApplicationPrefs";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.user_prefs_activity);
 
		SharedPreferences settings = getSharedPreferences(PREFS_APP, 0);
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 
		SharedPreferences settings = getSharedPreferences(PREFS_APP, 0);
 		SharedPreferences.Editor editor = settings.edit();
		// boolean preferenceValue = ... ;
		// editor.putBoolean("preferenceName", preferenceValue);
 
 		editor.commit();
 	}
 
 	public void clearCrashReports(View v) {
 		File appFiles = getFilesDir(); // actually directory
 		File[] listOfFiles = appFiles.listFiles();
 		System.out.println("Found " + listOfFiles.length + " file(s) in "
 				+ appFiles.getAbsolutePath());
 
 		if (listOfFiles.length == 0) {
 			Toast.makeText(getBaseContext(), "No previous reports found.",
 					Toast.LENGTH_SHORT).show();
 		} else {
 			String fileInList;
 			int filesRemoved = 0;
 			for (File file : listOfFiles) {
 				if (file.isFile()) {
 					fileInList = file.getName();
 					if (fileInList.endsWith(".stacktrace")) {
 						System.out.println("Removing: " + fileInList);
 						file.delete();
 						filesRemoved += 1;
 					}
 				}
 			}
 			Toast.makeText(getBaseContext(),
 					"" + filesRemoved + " previous reports removed.",
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 }

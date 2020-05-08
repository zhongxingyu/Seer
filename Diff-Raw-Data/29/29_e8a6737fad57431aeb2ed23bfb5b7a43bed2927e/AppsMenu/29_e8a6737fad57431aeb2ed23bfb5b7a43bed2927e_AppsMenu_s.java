 package com.t3hh4xx0r.setup;
 
 import java.io.File;
 
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 
 public class AppsMenu extends PreferenceActivity {
 	private IconPreferenceScreenRight mWallpapers;
 	private IconPreferenceScreenLeft mGodMode;
 	private IconPreferenceScreenRight mLauncher;
 	Context mContext;
 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.layout.apps_menu);
 		mGodMode = (IconPreferenceScreenLeft) findPreference("godmode");		
 		mWallpapers = (IconPreferenceScreenRight) findPreference("wallpapers");
 		mLauncher = (IconPreferenceScreenRight) findPreference("launcher");
 
 		File wallpapersf = new File("/system/app/Wallpapers.apk");
 		if (!wallpapersf.exists()) {
 			mWallpapers.setEnabled(true);
 			mWallpapers.setSummary(R.string.uninstalled_apps);
 		}
 		
		/**File godmodef = new File("/system/app/God_Mode.apk");
 		if (!godmodef.exists()) {
 			mGodMode.setEnabled(true);
 			mGodMode.setSummary(R.string.uninstalled_apps);
		}**/
 		
		/**File launcherf = new File("/system/app/OMFGB-Launcher.apk");
 		if (!launcherf.exists()) {
 			mLauncher.setEnabled(true);
 			mLauncher.setSummary(R.string.uninstalled_apps);
		}**/
 		
 	}
 	
 	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
 
 		if (preference == mWallpapers) {
 			Intent marketApp = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.t3hh4xx0r.wallpapers"));
 			marketApp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
  			try{
 				startActivity(marketApp);
 			}catch(ActivityNotFoundException e){
 				e.printStackTrace();
 			}
 		}
  			return true;	
 	}
 	
 }

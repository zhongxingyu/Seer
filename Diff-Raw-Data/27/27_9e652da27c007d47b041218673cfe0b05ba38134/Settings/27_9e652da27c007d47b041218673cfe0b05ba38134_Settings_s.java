 package com.lsg.app;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.view.KeyEvent;
 
 public class Settings extends PreferenceActivity {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		Functions.setTheme(false, false, this);
 		getWindow().setBackgroundDrawableResource(R.layout.background);
 		
         addPreferencesFromResource(R.xml.login_settings);
         addPreferencesFromResource(R.xml.vplan_settings);
         addPreferencesFromResource(R.xml.list_settings);
         
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         int i = 0;
         boolean showonlywhitelist = false;
         while(i < Functions.exclude.length) {
         	if(Functions.exclude[i].equals(prefs.getString(Functions.class_key, "")))
         		showonlywhitelist = true;
         	i++;
         }
         if(!showonlywhitelist) {
        	PreferenceCategory prefCat = (PreferenceCategory) findPreference(getString(R.string.vplan));
         	CheckBoxPreference onlywhitelist = (CheckBoxPreference) findPreference("showonlywhitelist");
         	prefCat.removePreference(onlywhitelist);
         }
    	PreferenceCategory prefCat = (PreferenceCategory) findPreference(getString(R.string.vplan));
    	CheckBoxPreference onlywhitelist = (CheckBoxPreference) findPreference("dark_actionbar");
    	prefCat.removePreference(onlywhitelist);
         
         Preference blacklist = (Preference) findPreference("blacklist");
         blacklist.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	public boolean onPreferenceClick(Preference preference) {
         				Intent intent = new Intent(Settings.this, BlackWhiteList.class);
         				intent.putExtra(Functions.BLACKWHITELIST, Functions.BLACKLIST);
         				startActivity(intent);
         				return true;
         				}
         	});
         
         Preference whitelist = (Preference) findPreference("whitelist");
         whitelist.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	public boolean onPreferenceClick(Preference preference) {
         				Intent intent = new Intent(Settings.this, BlackWhiteList.class);
         				intent.putExtra(Functions.BLACKWHITELIST, Functions.WHITELIST);
         				startActivity(intent);
         				return true;
         				}
         	});
 	}
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 	    	Intent intent = new Intent(this, VPlan.class);
 	    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 	    	startActivity(intent);
 	        return false;
 	    }
 	    else
 	    	return super.onKeyDown(keyCode, event);
 	}
 }

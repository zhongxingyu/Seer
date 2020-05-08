 package com.blklb.mpdhd;
 
 import android.os.Bundle;
import android.preference.PreferenceActivity;
 
 
import com.blklb.mpdhd.R;
 
 public class SettingsActivity extends PreferenceActivity {
	
	//private String tag = "SettingsActivity";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.prefrences);		
 	}
 }

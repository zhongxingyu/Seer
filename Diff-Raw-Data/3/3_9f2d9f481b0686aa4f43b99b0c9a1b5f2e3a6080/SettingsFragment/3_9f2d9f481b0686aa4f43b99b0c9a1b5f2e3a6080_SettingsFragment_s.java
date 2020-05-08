 package ch.hsr.hsrlunch.ui;
 
 import ch.hsr.hsrlunch.R;
 import android.annotation.SuppressLint;
 import android.os.Bundle;
 import android.preference.PreferenceFragment;
 import android.preference.SwitchPreference;
 
 @SuppressLint("NewApi")
 public class SettingsFragment extends PreferenceFragment {
 	 @Override
 	    public void onCreate(Bundle savedInstanceState) {
 	        super.onCreate(savedInstanceState);
 
 	        // Load the preferences from an XML resource
 	        addPreferencesFromResource(R.xml.userpreference);
 	        SwitchPreference switchPreference = (SwitchPreference) findPreference("pref_badge");
	        if(switchPreference.isChecked()) {
	        	
	        }
 	    }
 
 }

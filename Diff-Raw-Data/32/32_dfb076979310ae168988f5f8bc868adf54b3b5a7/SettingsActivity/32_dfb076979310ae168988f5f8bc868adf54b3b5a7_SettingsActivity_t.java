 package no.ntnu.tdt4240.activities;
 
 import no.ntnu.tdt4240.R;
 import android.app.Activity;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class SettingsActivity extends PreferenceActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	    
 		// hide title
 
 //        requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         // Hide notification bar
 //        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         addPreferencesFromResource(R.xml.settings);

 	}
 	
 }

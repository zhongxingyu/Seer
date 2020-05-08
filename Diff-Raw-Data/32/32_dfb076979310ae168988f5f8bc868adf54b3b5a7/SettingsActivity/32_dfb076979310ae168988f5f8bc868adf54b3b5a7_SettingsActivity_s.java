 package no.ntnu.tdt4240.activities;
 
 import no.ntnu.tdt4240.R;
 import android.app.Activity;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.view.Window;
 import android.view.WindowManager;
 
<<<<<<< HEAD
public class SettingsActivity extends PreferenceActivity{
=======
 public class SettingsActivity extends PreferenceActivity {
>>>>>>> 969ad607cfb9402b28af8b03cd8566ec549a91c0
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	    
 		// hide title
<<<<<<< HEAD
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
 
        // Hide notification bar
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.settings);
        
=======
 //        requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         // Hide notification bar
 //        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         addPreferencesFromResource(R.xml.settings);
>>>>>>> 969ad607cfb9402b28af8b03cd8566ec549a91c0
 	}
 	
 }

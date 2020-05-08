 /**
  * Package of menus
  */
 package com.japtar.chessclock.menus;
 
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 
import com.japtar.chessclock.R;
 import com.japtar.chessclock.Global;
 
 /**
  * Sets up the menu for options
  * @author japtar10101
  */
 public class OptionsMenu extends PreferenceActivity {
 	/* ===========================================================
 	 * Overrides
 	 * =========================================================== */
 	/**
      * Called when the activity is first created.
      * @see android.app.Activity#onCreate(android.os.Bundle)
      */
     @Override
     public void onCreate(final Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    
 	    // First, setup the UI
 		this.addPreferencesFromResource(R.layout.options);
 	}
     
     /**
      * Called when the activity pauses.
      * @see android.app.Activity#onPause()
      */
     @Override
     public void onPause() {
     	// Do whatever is in the super class first
         super.onPause();
         this.recallSettings();
     }
     
     /* ===========================================================
 	 * Private/Protected Methods
 	 * =========================================================== */
     /**
      * Recalls the preferences values into Global.Options
      */
     private void recallSettings() {
     	// Retrieve the preference page
 		final SharedPreferences settings =
 			PreferenceManager.getDefaultSharedPreferences(this);
     	
 		// Update the options
     	Global.OPTIONS.recallSettings(settings);
     }
 }

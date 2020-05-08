 /**
  * 
  */
 package com.madhackerdesigns.neverlate;
 
import android.content.Context;
import android.content.Intent;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 
 /**
  * @author flintinatux
  *
  */
 public class NeverLateSettings extends PreferenceActivity {
 	
 	/* (non-Javadoc)
 	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// pull preferences from prefs file
 		addPreferencesFromResource(R.xml.settings);
 		setTitle(R.string.app_title);
 		
 	}
 
	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
//		Toast.makeText(this, "Settings are paused, but alarms will be set", Toast.LENGTH_LONG).show();
		
		Context context = getApplicationContext();
		Intent intent = new Intent(context, StartupReceiver.class);
		sendBroadcast(intent);
	}

 }

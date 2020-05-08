 package no.group09.utils;
 
 import no.group09.arduinoair.R;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.widget.TextView;
 
 public class Preferences extends PreferenceActivity {
 
 	TextView l;
 
 
	@SuppressWarnings("deprecation")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
		/*This method is deprecated, but have to be used when developing for
		 * Android 3.0 or lower.
		 */
		addPreferencesFromResource(R.xml.preferences);
 		
//	
//		setContentView(R.layout.preferences);
//		
//		l = (TextView)findViewById(R.id.pin_tv);
//		
 	}
 }

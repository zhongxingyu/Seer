 package net.analogyc.wordiary;
 
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.view.Menu;
 import android.widget.Toast;
 
 public class PreferencesActivity extends PreferenceActivity{
 	
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		addPreferencesFromResource(R.xml.preferences); 
 		
 		findPreference("grace_period").setOnPreferenceChangeListener(
 			new Preference.OnPreferenceChangeListener() {
 				
 				@Override
 				public boolean onPreferenceChange(Preference preference, Object newValue) {
 					int value;
 					
 					try {
 						value = Integer.parseInt((String) newValue);
						if (!newValue.toString().matches("^([1-9][0-9]*)|([0])$")) {
 							throw new NumberFormatException();
 						}
 					}
 					catch(NumberFormatException e) {
 						Toast toast1 = Toast.makeText(getBaseContext(), "Only values between 0 and 168 allowed", 1000);
 						toast1.show();
 						return false;
 					}
 
 					return true;
 				}
 			});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.user_preferences, menu);
 		return true;
 	}
 	
 
 }

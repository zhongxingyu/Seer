 package no.group09.utils;
 
 import no.group09.arduinoair.R;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.widget.TextView;
 
 public class Preferences extends PreferenceActivity {
 
 	TextView l;
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.preferences);
 		
		l = (TextView)findViewById(R.id.pin_tv);
 		
 	}
 }

 package fr.keuse.rightsalert.preference;
 
 import fr.keuse.rightsalert.R;
import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.view.MenuItem;
 
 public class RightsalertPreference extends PreferenceActivity {
 
	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.main);
 		
 		if(Build.VERSION.SDK_INT >= 11) {
 			ActionBar actionbar = getActionBar();
 			actionbar.setDisplayHomeAsUpEnabled(true);
 		}
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case android.R.id.home:
 			finish();
 		}
 		return super.onOptionsItemSelected(item);
 	}
 }

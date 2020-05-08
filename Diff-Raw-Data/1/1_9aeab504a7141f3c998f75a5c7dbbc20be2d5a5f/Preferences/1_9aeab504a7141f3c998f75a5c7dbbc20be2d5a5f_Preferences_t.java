 package nl.johndekroon.dma;
 
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.view.MenuItem;
 
 public class Preferences extends PreferenceActivity {
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			finish();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 }

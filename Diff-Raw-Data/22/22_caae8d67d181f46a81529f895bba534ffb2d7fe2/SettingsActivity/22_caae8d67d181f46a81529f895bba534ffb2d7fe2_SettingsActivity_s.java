 package com.buildbotwatcher;
 
 import android.preference.PreferenceActivity;
 import android.view.MenuItem;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class SettingsActivity extends PreferenceActivity {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
        addPreferencesFromResource(R.xml.preferences);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	        case android.R.id.home:
 	            Intent intent = new Intent(this, BuildersActivity.class);
 	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 	            startActivity(intent);
 	            return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 }

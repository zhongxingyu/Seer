 package com.asa.easysal.ui;
 
import android.annotation.TargetApi;
import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.MenuItem;
 
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
 public class PostHCPreferenceActivity extends FragmentActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Since this is onjly going to be show on API 11+, we don't need to use
 		// ABS.
 		getActionBar().setHomeButtonEnabled(true);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		// Display the fragment as the main content.
 		getFragmentManager().beginTransaction()
 				.replace(android.R.id.content, new PreferencesFragment())
 				.commit();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			finish();
 			return true;
 		}
 		return false;
 	}
 
 }

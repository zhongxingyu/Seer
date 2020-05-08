 package org.hermitcrab.ui.phone;
 
 import org.hermitcrab.ui.AppListFragment;
 import org.hermitcrab.ui.BaseSinglePaneActivity;
 
import android.os.Bundle;
import android.support.v4.app.ActionBar;
 import android.support.v4.app.Fragment;
 
 public class AppListActivity extends BaseSinglePaneActivity {
 
 	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(false);
		}
	}

	@Override
 	protected Fragment onCreatePane() {
 		return new AppListFragment();
 	}
 
 }

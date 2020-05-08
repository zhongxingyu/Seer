 package org.hermitcrab.ui.phone;
 
 import org.hermitcrab.ui.AppListFragment;
 import org.hermitcrab.ui.BaseSinglePaneActivity;
 
 import android.support.v4.app.Fragment;
 
 public class AppListActivity extends BaseSinglePaneActivity {
 
 	@Override
 	protected Fragment onCreatePane() {
 		return new AppListFragment();
 	}
 
 }

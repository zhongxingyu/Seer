 // $codepro.audit.disable unnecessaryImport
 package net.rdrei.android.wakimail.ui;
 
 import net.rdrei.android.wakimail.R;
 import net.rdrei.android.wakimail.data.MailDatabase;
 import net.rdrei.android.wakimail.data.MailPreferences;
 import net.rdrei.android.wakimail.robo.RoboSherlockFragmentActivity;
 import net.rdrei.android.wakimail.ui.MailListFragment.OnLogoutRequestedListener;
 import roboguice.util.Ln;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 
import com.actionbarsherlock.view.Window;
 import com.google.inject.Inject;
 
 public class MailListActivity extends RoboSherlockFragmentActivity implements
 		OnLogoutRequestedListener {
 	
 	@Inject
 	protected FragmentManager mFragmentManager;
 
 	/**
 	 * Delete all user information from the storage and ends the current
 	 * activity;
 	 */
 	public void onLogoutRequested() {
 		// Clear the preferences.
 		final SharedPreferences preferences = getSharedPreferences(
 				MailPreferences.KEY, MODE_PRIVATE);
 		preferences.edit().clear().commit();
 		// Drop the database.
 		if (!this.deleteDatabase(MailDatabase.DB_NAME)) {
 			Ln.e("Could not delete sqlite database!");
 		}
 		this.finish();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setSupportProgressBarIndeterminateVisibility(false);
 		
 		setContentView(R.layout.activity_singlepane_empty);
 
 		// Create fragment to embed in activity.
 		final FragmentTransaction ft = mFragmentManager.beginTransaction();
 		final MailListFragment newFragment = new MailListFragment();
 		ft.replace(R.id.root_container, newFragment);
 		ft.commit();
 	}
 }

 package net.rdrei.android.wakimail.ui;
 
 import net.rdrei.android.wakimail.R;
 import net.rdrei.android.wakimail.data.MailDatabase;
 import net.rdrei.android.wakimail.data.MailPreferences;
 import net.rdrei.android.wakimail.ui.MailListFragment.OnLogoutRequestedListener;
 import roboguice.activity.RoboFragmentActivity;
 import roboguice.util.Ln;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 
 public class MailListActivity extends RoboFragmentActivity implements
 		OnLogoutRequestedListener {
 
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
 
		setContentView(R.layout.activity_singlepane_empty);

 		if (savedInstanceState == null) {
 			// First-time init; create fragment to embed in activity.
 			FragmentTransaction ft = getSupportFragmentManager()
 					.beginTransaction();
 			MailListFragment newFragment = new MailListFragment();
 			ft.replace(R.id.root_container, newFragment);
 			ft.commit();
 		}
 	}
 }

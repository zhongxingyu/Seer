 package net.rdrei.android.wakimail.ui;
 
 import net.rdrei.android.wakimail.R;
 import net.rdrei.android.wakimail.data.MailPreferences;
 import net.rdrei.android.wakimail.wak.SessionManager;
 import net.rdrei.android.wakimail.wak.User;
 import roboguice.activity.RoboActivity;
import roboguice.activity.RoboFragmentActivity;
 import roboguice.inject.InjectView;
 import roboguice.util.Ln;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.google.inject.Inject;
 
public class DashboardActivity extends RoboFragmentActivity {
 
 	private static final int LOGIN_REQUEST = 1;
 	@Inject
 	private SessionManager mSessionManager;
 
 	@Inject
 	private SharedPreferences mPreferences;
 
 	@InjectView(R.id.dashboard_sign_btn)
 	private Button mSignInButton;
	
 	@Inject
 	private ActionBar mActionBar;
 
 	protected void bindSignInButton() {
 		mSignInButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				final Intent intent = new Intent(DashboardActivity.this,
 						LoginActivity.class);
 				startActivityForResult(intent, LOGIN_REQUEST);
 			}
 		});
 	}
 
 	/**
 	 * Loads user credentials and sets the singleton accordingly. Returns null
 	 * if the data hasn't been saved yet.
 	 * 
 	 * @return User object or null.
 	 */
 	private User loadUserCredentials() {
 		// If the preferences can't be loaded, we obviously can't restore
 		// the user settings.
 		if (mPreferences == null) {
 			return null;
 		}
 
 		// The three values are always committed all at once, so we don't
 		// need to check for all of them. If one is missing, the user
 		// manipulated the storage.
 		if (mPreferences.contains(MailPreferences.USER_EMAIL)) {
 			final User user = new User();
			user.setEmail(mPreferences.getString(
					MailPreferences.USER_EMAIL, ""));
 			user.setPassword(mPreferences.getString(
 					MailPreferences.USER_PASSWORD, ""));
 			user.setSessionId(mPreferences.getString(
 					MailPreferences.USER_SESSIONID, ""));
			user.setName(mPreferences.getString(MailPreferences.USER_NAME,
					""));
 
 			mSessionManager.setUser(user);
 			return user;
 		}
 
 		return null;
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (requestCode == LOGIN_REQUEST
 				&& resultCode == RoboActivity.RESULT_OK) {
 			final Bundle extras = data.getExtras();
 			final User user = (User) extras
 					.getSerializable(LoginActivity.USER_EXTRA_KEY);
 
 			this.saveUserCredentials(user);
 			this.skipDashboard();
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		final User user = this.loadUserCredentials();
 		if (user != null) {
 			Ln.d("User credentials are saved. Skipping dashboard.");
 			this.skipDashboard();
 		}
		
 		mActionBar.setDisplayShowTitleEnabled(true);
		
 
 		Ln.d("No user credentials saved.");
 		setContentView(R.layout.activity_dashboard);
 		this.bindSignInButton();
 	}
 
 	private void saveUserCredentials(User user) {
 		mSessionManager.setUser(user);
 
 		final Editor editor = mPreferences.edit();
 		editor.putString(MailPreferences.USER_EMAIL, user.getEmail());
 		editor.putString(MailPreferences.USER_PASSWORD, user.getPassword());
 		editor.putString(MailPreferences.USER_SESSIONID, user.getSessionId());
 		editor.putString(MailPreferences.USER_NAME, user.getName());
 		editor.commit();
 
 		Ln.d("Saved user credentials to preferences.");
 	}
 
 	/**
 	 * User is logged in, skip to next activity.
 	 */
 	private void skipDashboard() {
 		final Intent intent = new Intent(this, MailListActivity.class);
 
 		// Replaces the current activity.
 		startActivity(intent);
 		finish();
 	}
 }

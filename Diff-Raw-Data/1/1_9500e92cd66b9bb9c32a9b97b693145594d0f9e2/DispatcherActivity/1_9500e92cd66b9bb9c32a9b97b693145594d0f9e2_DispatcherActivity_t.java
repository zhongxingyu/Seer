 package uk.co.vurt.hakken.activities;
 
 import uk.co.vurt.hakken.Constants;
 import uk.co.vurt.hakken.authenticator.AuthenticatorActivity;
 import uk.co.vurt.hakken.providers.TaskProvider;
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 
 public class DispatcherActivity extends Activity {
 
 	public final static String RETURN_TO_START_KEY = "uk.co.vurt.hakken.activities.ReturnToStart";
 	protected AccountManager accountManager;
 	
 	/**
 	 * Check to see if there is an existing Hakken account registered.
 	 * If so, use that account.
 	 * If not, invoke the account setup activity
 	 */
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		
 		//check for sync server setting
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		accountManager = AccountManager.get(this);
 		Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE); //retrieve all Hakken accounts
 		Intent intent;
 		if(preferences.getString("sync_server", null) == null){
 			intent = new Intent(this, PreferencesActivity.class);
 			intent.putExtra(RETURN_TO_START_KEY, true);
 		} else if(accounts.length <= 0){
 			//no accounts registered, invoke registration
 			intent = new Intent(this, AuthenticatorActivity.class);
 			intent.putExtra(RETURN_TO_START_KEY, true);
 		} else {
 			//Account found, so carry on as normal.
 //			intent = new Intent(this, JobList.class);
 			intent = new Intent(this, SelectorActivity.class);

 			Bundle bundle = new Bundle();
 			//900 seconds = 15 minutes
 			ContentResolver.addPeriodicSync(accounts[0], TaskProvider.AUTHORITY, bundle, 900); //TODO make this period configurable and handle multiple accounts
 		}
 		startActivity(intent);
 		finish();
 	}
 }

 package net.fhtagn.zoobeditor;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.os.Bundle;
import android.os.SystemClock;
 import android.preference.ListPreference;
 import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
 
 public class Preferences extends PreferenceActivity {
	static private String TAG = "Preferences";
 	@Override
 	protected void onCreate (Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.layout.preferences);
 		
 		ListPreference accounts = (ListPreference)findPreference("account");
 		fillAccountsList(accounts);
		//activateAccountsPref(accounts);
	}
	
	protected void activateAccountsPref (ListPreference accountPref) {
		//FIXME: doesn't quite work
		Log.i(TAG, "Forcing accounts prefs");
		View v = findViewById(accountPref.getLayoutResource());
		v.performClick();
 	}
 	
 	protected void fillAccountsList (ListPreference accountsList) {
 		final Account[] accounts = AccountManager.get(this).getAccountsByType(EditorConstants.ACCOUNT_TYPE);
 		final String[] accountsNames = new String[accounts.length];
 		for (int i=0; i<accounts.length; i++)
 			accountsNames[i] = accounts[i].name;
 		accountsList.setEntries(accountsNames);
 		accountsList.setEntryValues(accountsNames);
 	}
 }

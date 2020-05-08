 package net.fhtagn.zoobeditor;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.PreferenceActivity;
 
 public class Preferences extends PreferenceActivity {
	
 	@Override
 	protected void onCreate (Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.layout.preferences);
 		
 		ListPreference accounts = (ListPreference)findPreference("account");
 		fillAccountsList(accounts);
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

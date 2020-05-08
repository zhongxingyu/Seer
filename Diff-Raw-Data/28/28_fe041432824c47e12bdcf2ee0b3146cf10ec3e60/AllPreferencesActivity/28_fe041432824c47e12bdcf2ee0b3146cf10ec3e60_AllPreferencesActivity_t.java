 package com.moac.android.opensecretsanta.activity;
 
 import android.accounts.*;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.ListPreference;
 import android.preference.PreferenceActivity;
import android.util.Log;
 import com.moac.android.opensecretsanta.R;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class AllPreferencesActivity extends PreferenceActivity {
 
     private final static String TAG = "AllPreferencesActivity";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.preferences);
 
         AccountManager.get(this).getAccountsByTypeAndFeatures(Constants.ACCOUNT_TYPE_GOOGLE, Constants.FEATURES_MAIL,
          new AccountManagerCallback<Account[]>() {
               @Override
               public void run(AccountManagerFuture<Account[]> future) {
                      ListPreference gmailLp = ((ListPreference) getPreferenceManager().findPreference(getString(R.string.gmail_account_preference)));
                      List<String> accountEntries = new ArrayList<String>();
                   try {
                       Account[] accounts = future.getResult();
                       if(accounts != null && accounts.length > 0) {
                          for(int i = 0; i < accounts.length; i++) {
                              accountEntries.add(accounts[i].name);
                           }
                      }
 
                   } catch(Exception e) {
                      Log.e(TAG, "onCreate() - An error occurred populating the account list", e);
                   }
                  String[] entries = accountEntries.toArray(new String[accountEntries.size()]);
                  gmailLp.setEntries(entries);
                  gmailLp.setEntryValues(entries);
               }
           }, new Handler());
     }
 }

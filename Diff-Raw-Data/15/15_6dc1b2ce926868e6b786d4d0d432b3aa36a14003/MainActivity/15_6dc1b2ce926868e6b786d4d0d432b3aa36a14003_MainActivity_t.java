 package com.sourcefish.projectmanagement;
 
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.sourcefish.tools.SourceFishConfig;
 import com.sourcefish.tools.io.AsyncDataLoad;
 import com.sourcefish.tools.login.SourceFishAuthenticatorActivity;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.widget.Toast;
 
 public class MainActivity extends SherlockActivity{
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
         setTheme(SourceFishConfig.MAINTHEME); //Used for theme switching in samples
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);

		AccountManager am = AccountManager.get(getApplicationContext());
		Account[] accounts = am.getAccountsByType("com.sourcefish.authenticator");
 		
		boolean hasLoggedIn = (accounts.length > 0);
 		
 		//TODO read json file from tha webz
 		if(isOnline())
 		{			
 			// check for First time use
 			if(! hasLoggedIn)
 			{
 				firstRun();
 			}
 			// user has logged in before, get current projects.
 			else
 			{
			    String user = accounts[0].name;
				String pass = am.getPassword(accounts[0]);
 				
 				AsyncDataLoad load = new AsyncDataLoad(user, pass, getApplicationContext());
 				load.execute("");
 			}
 		}
 		else
 		{
 			if(hasLoggedIn)
 			{
 				Toast toast = Toast.makeText(getApplicationContext(), "offline mode", Toast.LENGTH_LONG);
 				toast.show();
 			}
 			else
 			{
 				Toast toast = Toast.makeText(getApplicationContext(), "You are not logged in and can't save your projects to the cloud! please update your settings", Toast.LENGTH_LONG);
 				toast.show();
 			}
 		}
 	}
 	
 	private boolean isOnline()
 	{
 		ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo netInfo = cm.getActiveNetworkInfo();
         if (netInfo != null && netInfo.isConnectedOrConnecting()) {
             return true;
         }
         return false;
 	}
 	
 	private void firstRun()
 	{
 		Intent i = new Intent(getApplicationContext(), SourceFishAuthenticatorActivity.class);
 		startActivity(i);
 	}
 }

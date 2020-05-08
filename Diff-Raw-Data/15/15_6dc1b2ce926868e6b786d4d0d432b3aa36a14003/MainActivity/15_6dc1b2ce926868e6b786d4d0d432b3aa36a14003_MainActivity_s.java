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
		SharedPreferences prefs = getSharedPreferences(SourceFishConfig.PREFFILE, 0);
 		
		boolean hasLoggedIn = prefs.getBoolean("loggedin", false);
 		
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
				AccountManager am = AccountManager.get(getApplicationContext());
				
			    Account[] aca = am.getAccountsByType("com.sourcefish.authenticator");
			    String user = aca[0].name;
				String pass = am.getPassword(aca[0]);
 				
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

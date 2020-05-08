 package org.mustard.android.activity;
 
 import org.mustard.android.MustardApplication;
 import org.mustard.android.MustardDbAdapter;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 /**
  * Activity to save account settings to the internal db.<br/>
  * Format of url: <code>statusnet://create/{username}/{server host}/{oauth2 token}</code><br/>
  * Example: <code>statusnet://create/bob/145.96.2.97/abcdef01234567890</code>
  */
 public class CreateAccount extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 
 	Intent intent = getIntent();
 	if (intent != null) {
 	    if (intent.getDataString() != null && intent.getDataString().startsWith("statusnet://")) {
 		//get url segments
 		String dataString = intent.getDataString();
 		Log.i("JEROEN", "Data:" + intent.getDataString());
 		String[] segments = dataString.split("/");
 		String userName = segments[3];
 		String serverHost = segments[4];
 		String token = segments[5];
 
 		//overwrite account settings in internal DB
 		MustardDbAdapter dbAdapter = new MustardDbAdapter(getApplicationContext());
 		dbAdapter.open();
 		dbAdapter.createAccount(2L, userName, "password", "http://" + serverHost + "/statusnet-oauth2/", 1, "1.0.1");
 		MustardApplication application = (MustardApplication) getApplication();
 		application.checkAccount(dbAdapter);
 		dbAdapter.close();
 
 		//save token to preferences
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 		Editor edit = preferences.edit();
 		edit.putString("oauth2_access_token", token);
 		edit.commit();
 
 		//open main view
 		Context context = getApplicationContext();
		Intent intent2 = new Intent(context, MustardMain.class);
 		intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		context.startActivity(intent2);
 	    }
 
 	}
     }
 }

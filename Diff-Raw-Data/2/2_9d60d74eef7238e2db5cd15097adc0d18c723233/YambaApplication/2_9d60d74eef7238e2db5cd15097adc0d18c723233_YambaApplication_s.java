 package com.marakana.yamba;
 
 import com.marakana.android.yamba.clientlib.YambaClient;
 
 import android.app.Application;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class YambaApplication extends Application implements OnSharedPreferenceChangeListener{
 	private static final String TAG = YambaApplication.class.getSimpleName();
 	
 	YambaClient cloud = null;
 	SharedPreferences prefs;
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		//register on shared preferences changes
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		prefs.registerOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	public void onTerminate() {
 		super.onTerminate();
 	}
 	
 	synchronized YambaClient getYambaClient()
 	{
 		//check if we already initialized the YambaClient object
 		if (cloud == null)
 		{
 			//use the preference values for username, password, etc
 			String username, password, apiRoot;
 			username = prefs.getString("username", "student");
 			password = prefs.getString("password", "password");
 			apiRoot = prefs.getString("apiRoot", "");
 			
 			//create an instance because it is null
 			cloud = new YambaClient(username, password);
 		}
 		
 		return(cloud);
 	}
 
 	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		//back from preference activity, it is possible that the credentials are changed
 		cloud = null;
 		Log.d(TAG, "Back from the preference activity");
 	}
 	
 }

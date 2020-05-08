 package com.cisco.yamba;
 
 import java.util.List;
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.marakana.android.yamba.clientlib.YambaClient;
 import com.marakana.android.yamba.clientlib.YambaClient.Status;
 import com.marakana.android.yamba.clientlib.YambaClientException;
 
 public class RefreshService extends IntentService {
 
 	private static final String TAG = "RefreshService";
 	private YambaClient yamba;
 
 	public RefreshService() {
 		super(TAG);
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 
 		// Get the login info from the preferences
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		String username = prefs.getString("username", null);
 		String password = prefs.getString("password", null);
 		if (username == null || password == null) {
 			yamba = null;
 		} else {
 			yamba = new YambaClient(username, password);
 		}
 
 		Log.d(TAG, "onCreated");
 	}
 
 	/** Executes on a worker thread. */
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		Log.d(TAG, "onHandleIntent");
 		try {
 			List<Status> timeline = yamba.getTimeline(20);
 			for (Status status : timeline) {
 				Log.d(TAG,
 						String.format("%s: %s", status.getUser(),
 								status.getMessage()));
 			}
 		} catch (YambaClientException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Log.d(TAG, "onDestroyed");
 	}
 
 }

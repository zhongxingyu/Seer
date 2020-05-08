 package com.frienemy.services;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.Facebook;
 import com.frienemy.activities.FrienemyActivity;
 import com.frienemy.activities.FriendsActivity;
 import com.frienemy.models.Friend;
 import com.frienemy.requests.FriendsRequestListener;
 import com.frienemy.requests.WallRequestListener;
 import com.frienemy.requests.FriendsRequestListener.FriendRequestListenerResponder;
 import com.frienemy.requests.UserRequestListener;
 import com.frienemy.requests.UserRequestListener.UserRequestListenerResponder;
 import com.frienemy.requests.WallRequestListener.WallRequestListenerResponder;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 
 public class FrienemyService extends Service implements UserRequestListenerResponder, FriendRequestListenerResponder, WallRequestListenerResponder {
 
 	private NotificationManager notificationManager;
 
 	private static final String TAG = FrienemyService.class.getSimpleName();
 	private Timer timer;
 	Facebook facebook = new Facebook("124132700987915");
 	private AsyncFacebookRunner asyncRunner;
 	String FILENAME = "AndroidSSO_data";
 	private SharedPreferences mPrefs;
 	private Context context;
 	private UserRequestListener userRequestListener;
 	private FriendsRequestListener friendsRequestListener;
 	private List<FrienemyServiceListener> listeners = new ArrayList<FrienemyServiceListener>();
 
 	private FrienemyServiceAPI.Stub apiEndpoint = new FrienemyServiceAPI.Stub() {
 		public void addListener(FrienemyServiceListener listener)
 				throws RemoteException {
 			synchronized (listeners) {
 				listeners.add(listener);
 			}
 		}
 
 		public void removeListener(FrienemyServiceListener listener)
 				throws RemoteException {
 			synchronized (listeners) {
 				listeners.remove(listener);
 			}
 		}
 
 	};
 
 	private TimerTask updateTask = new TimerTask() {
 		@Override
 		public void run() {
 			Log.i(TAG, "Timer task doing work");
 			refreshPreferences();
 			String access_token = mPrefs.getString("access_token", null);
 			long expires = mPrefs.getLong("access_expires", 0);
 			if(access_token != null) {
 				facebook.setAccessToken(access_token);
 			}
 			if(expires != 0) {
 				facebook.setAccessExpires(expires);
 			}
 			if (facebook.isSessionValid()) {
 				// First, lets get the info about the current user
 				asyncRunner.request("me", userRequestListener);
 			}
 		}
 	};
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		if (FrienemyService.class.getName().equals(intent.getAction())) {
 			Log.d(TAG, "Bound by intent " + intent);
 			return apiEndpoint;
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		Log.i(TAG, "Service creating");
 		refreshPreferences();
 		context = this.getBaseContext();
 		asyncRunner = new AsyncFacebookRunner(facebook);
 		userRequestListener = new UserRequestListener(context, this);
 		friendsRequestListener = new FriendsRequestListener(context, this);
 		timer = new Timer("FrienemyServiceTimer");
 		timer.schedule(updateTask, 1000L, 60 * 60000L);
 	}
 
 	public void refreshPreferences() {
 		mPrefs = this.getSharedPreferences(FILENAME, MODE_PRIVATE);
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Log.i(TAG, "Service destroying");
 
 		timer.cancel();
 		timer = null;
 	}
 
 	private void notifyListeners() {
 		synchronized (listeners) {
 			Log.d(TAG, "Notify Listeners");
 			for (FrienemyServiceListener listener : listeners) {
 				try {
 					listener.handleFriendsUpdated();
 				} catch (RemoteException e) {
 					Log.w(TAG, "Failed to notify listener " + listener, e);
 				}
 			}
 		}
 	}
 
 	/*To update notification bar call this method
 		Notification type is to be used to separate 
 		from different notifications, Notifications with 
 		the same type will replace and old notification 
 		with the same type.
 	 */
 
 	private void showNotification(String title, String message, int iconId, int notificationType) {
 
 		notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
 		Intent enemyIntent = new Intent(FrienemyService.this,FrienemyActivity.class);
 		Intent friendIntent = new Intent(FrienemyService.this,FriendsActivity.class);
 
 		// Set the icon, scrolling text and time stamp
 		Notification notification = new Notification(iconId, message, System.currentTimeMillis());
 		PendingIntent contentIntent;
 
 		// The PendingIntent to launch our activity if the user selects this notification
 		if(title.equalsIgnoreCase("New Frenemy"))
 		{
 			contentIntent = PendingIntent.getActivity(this, 0, enemyIntent, 0);
 		}
 		else
 		{
 			contentIntent = PendingIntent.getActivity(this, 0, friendIntent, 0);	
 		}
 
 		// Set the info for the views that show in the notification panel.
 		notification.setLatestEventInfo(this, title, message, contentIntent);
 
 		// Send the notification.
 		notificationManager.notify(notificationType, notification);
 	}
 
 	public void userRequestDidFinish() {
 		// Get the user's friend list
 		Bundle parameters = new Bundle();
		parameters.putString("limit", "5000");
 		parameters.putString("fields", "id,name,first_name,middle_name,last_name,gender,link,username,bio,birthday,education,email,hometown,interested_in,location,political,quotes,relationship_status,religion,significant_other,website,work");
 		asyncRunner.request("me/friends", parameters, friendsRequestListener);
 	}
 
 	public void userRequestDidFail() {
 		Log.e(TAG, "Failed to get user");
 	}
 
 	public void friendRequestDidFinish(int totalFriends) {
 		ArrayList<Friend> changedArray = Friend.changedFriends(context);
 		String [] frienemyList= FriendsRequestListener.getList();
 		int l=1;
 		//Check to see if there are any new frienemies and notifies the user
 		for( l=1; frienemyList.length>l; l++)
 		{
 			this.showNotification("New Frenemy",frienemyList[l], com.frienemy.activities.R.drawable.icon, l);
 		}
 		
 		for(int k=0; changedArray.size() > k; k++)
 		{
 			if(!changedArray.get(k).changedFields.equalsIgnoreCase(""))
 			{
 				this.showNotification("Stalked Friend Changed", changedArray.get(k).name + " " + changedArray.get(k).changedFields, com.frienemy.activities.R.drawable.icon, k+l);
 			}
 		}
 
 		notifyListeners();
 
 		// Get the user's wall
 		//asyncRunner.request("me/feed", wallRequestListener);
 	}
 
 	public void friendRequestDidFail() {
 		Log.e(TAG, "Failed to get friends list");
 	}
 
 	public void wallRequestDidFinish() {
 		notifyListeners();
 	}
 
 	public void wallRequestDidFail() {
 		// TODO Auto-generated method stub
 
 	}
 
 }

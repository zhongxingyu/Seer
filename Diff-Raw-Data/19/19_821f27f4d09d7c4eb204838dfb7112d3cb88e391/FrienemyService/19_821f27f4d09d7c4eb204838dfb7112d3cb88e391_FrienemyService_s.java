 package com.frienemy.services;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.Facebook;
 import com.frienemy.activities.EnemyActivity;
 import com.frienemy.models.Friend;
 import com.frienemy.requests.FriendsRequestListener;
 import com.frienemy.requests.FriendsRequestListener.FriendRequestListenerResponder;
 import com.frienemy.requests.UserRequestListener;
 import com.frienemy.requests.UserRequestListener.UserRequestListenerResponder;
 import com.frienemy.tasks.FacebookBatchRequest;
 import com.frienemy.tasks.FacebookBatchRequest.FacebookBatchRequestResponder;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 
 public class FrienemyService extends Service implements UserRequestListenerResponder, FriendRequestListenerResponder, FacebookBatchRequestResponder {
 
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
 			notifyListeners();
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
 		timer.schedule(updateTask, 1000L, 1 * 60000L);
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
 		Intent enemyIntent = new Intent(FrienemyService.this,EnemyActivity.class);
 
 		// Set the icon, scrolling text and time stamp
 		Notification notification = new Notification(iconId, message, System.currentTimeMillis());
 		PendingIntent contentIntent;
 
 		// The PendingIntent to launch our activity if the user selects this notification
 		contentIntent = PendingIntent.getActivity(this, 0, enemyIntent, 0);
 
 		// Set the info for the views that show in the notification panel.
 		notification.setLatestEventInfo(this, title, message, contentIntent);
 
 		// Send the notification.
 		notificationManager.notify(notificationType, notification);
 	}
 
 	public void userRequestDidFinish() {
 		// Get the user's friend list
 		asyncRunner.request("me/friends", friendsRequestListener);
 	}
 
 	public void userRequestDidFail() {
 		Log.e(TAG, "Failed to get user");
 	}
 
 	@SuppressWarnings("unchecked")
 	public void friendRequestDidFinish(int totalFriends) {
 		//Check to see if there are any new frienemies and notifies the user
 		if(!FriendsRequestListener.getList().equalsIgnoreCase(""))
 		{
 			this.showNotification("New Frenemies",FriendsRequestListener.getList(), com.frienemy.activities.R.drawable.icon, 0);
 		}
 		try {
 			ArrayList<Friend> friends = Friend.allFriends(getBaseContext());
			new FacebookBatchRequest(getBaseContext(), facebook, this).execute(friends);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void friendRequestDidFail() {
 		Log.e(TAG, "Failed to get friends list");
 	}
 
 	public void friendDetailsLoading() {
 		
 	}
 
 	public void friendDetailsLoadCancelled() {
 		
 	}
 
 	public void friendDetailsLoaded() {
 		
 	}
 
 }

 package com.monstersfromtheid.imready.service;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.json.JSONArray;
 
 import android.app.IntentService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.os.PowerManager;
 
 import com.monstersfromtheid.imready.IMReady;
 import com.monstersfromtheid.imready.IMeetingChangeReceiver;
 import com.monstersfromtheid.imready.MyMeetings;
 import com.monstersfromtheid.imready.R;
 import com.monstersfromtheid.imready.client.Meeting;
 import com.monstersfromtheid.imready.client.ServerAPI;
 import com.monstersfromtheid.imready.client.ServerAPICallFailedException;
 
 public class CheckMeetingsService extends IntentService {
 
 	private static PowerManager.WakeLock lock = null;
 	public static final String LOCK_NAME = "com.monstersfromtheid.imready.service.CheckMeetingsService";
 	private static ServerAPI api;
 	private static String userName;
 
 	public CheckMeetingsService(String name) {
 		super(name);
 	}
 
 	public CheckMeetingsService() {
 		this("CheckMeetingsService");
 	}
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		if( IMReady.isAccountDefined(this) ){
 			userName = IMReady.getUserName(this);
 			api = new ServerAPI(userName);
 		}
 	}
 	
 	/**
 	 * Compare the provided JSON with the last seen JSON.  Use any differences (and the
 	 * current notification level) to generate notifications of the changes
 	 * 
 	 * @param latestJSON
 	 */
 	protected void generateNotifications(Intent intent){
		if ( !IMReady.isAccountDefined(this) ) return;
		if( api == null ){
 			userName = IMReady.getUserName(this);
 			api = new ServerAPI(userName);
 		}
 		
 		JSONArray latestJSON = new JSONArray();
 		try {
 			latestJSON = api.userMeetings(api.getRequestingUserId());
 		} catch (ServerAPICallFailedException e) {
 			// Silently ignore failures to get meetings.
 			return;
 		}
 
 		int notificationLevel = IMReady.getNotificationLevel(this);
 
 		ArrayList<Meeting> latestMeetings = IMReady.toMeetingList(latestJSON);
 		
 		// Want to know numbers for:
 		//  New     - A meeting that is previously unknown to the user.
 		//  Ready   - Meeting a user was aware of that has become ready.  Note there can't be a "New" meeting
 		//            that is ready as the user would have had to mark it ready, and thus be aware of it.
 		//  Changed - Meetings that the user is aware of that have seen some change since they were last aware 
 		//            of it.
 		// If anything has changed, send a broadcast.
 		// If notification level is sufficient, create a notification
 
 		ArrayList<Meeting> newMeetingList = IMReady.rollupMeetingLists(latestMeetings, this);
 		int newM = 0;
 		int readyM = 0;
 		int changeM = 0;
 		
 		Iterator<Meeting> newMeetingListIter = newMeetingList.iterator();
 		while(newMeetingListIter.hasNext()){
 			Meeting m = newMeetingListIter.next();
 			if( m.isNewToUser() ) {
 				newM++; 
 			}
 			if( m.isChangedToUser() ) {
 				if( m.getState() == 1 ){
 					readyM++;
 				} else {
 					changeM++;
 				}
 			}
 		}
 
 		// Broadcast to any registered receivers that we've got the new latest info.
 		// There may or may not be changes, but they should redraw
 		Intent broadcastIntent = new Intent();
 		broadcastIntent.setAction(IMeetingChangeReceiver.ACTION_RESP);
 		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
 		sendBroadcast(broadcastIntent);
 
 		// Assume the notification lasts until it is acted on by the user.
 		// This means that I only create a new notification if I saw something change since I last looked.
 		// There is a special case: New meeting X, delete meeting X.  In this case, I delete the notification.
 		// Otherwise I try to list number of meetings that are:
 
 		if (notificationLevel == 0) {
 			return;
 		}
 
 		// Now we know what's changed, it's time to generate a notification
 		// [(n) new[,] ][(r) ready[ and] ][(c) changed]
 		String notificationMessage = "";
 		NotificationManager notMgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
 		if( newM > 0 ){
 			if( readyM > 0 ){
 				if( changeM > 0 ){
 					notificationMessage = "(" + newM + ") new, (" + readyM + ") ready and (" + changeM + ") changed";
 				} else {
 					notificationMessage = "(" + newM + ") new and (" + readyM + ") ready";								
 				}
 			} else {
 				if( changeM > 0 ){
 					notificationMessage = "(" + newM + ") new and (" + changeM + ") changed";
 				} else {
 					notificationMessage = "(" + newM + ") new";								
 				}
 			}
 		} else {
 			if( readyM > 0 ){
 				if( changeM > 0 ){
 					notificationMessage = "(" + readyM + ") ready and (" + changeM + ") changed";
 				} else {
 					notificationMessage = "(" + readyM + ") ready";								
 				}
 			} else {
 				if( changeM > 0 ){
 					// Add a preferences check to see if we should display this.
 					notificationMessage = "(" + changeM + ") changed";
 				} else {
 					// There is nothing to report
 					return;
 				}
 			}
 		}
 
 		Notification notification = new Notification(R.drawable.notification, "IMReady Meetings", System.currentTimeMillis());
 		notification.flags |= Notification.FLAG_AUTO_CANCEL;
 
 		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
 		notification.ledOnMS = 300;
 		notification.ledOffMS = 1100;				
 
 		CharSequence notificationTitle = getString( R.string.app_name );
 		Context context = getApplicationContext();
 		Intent notificationIntent = new Intent(this, MyMeetings.class);
 		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
 		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
 		notification.setLatestEventInfo(context, notificationTitle, notificationMessage, pendingIntent);
 
 		notMgr.notify(IMReady.NOTIFICATION_ID, notification);
 	}
 
 	protected void onHandleIntent(Intent intent) {
 		try {
 			setNextWakeup(intent);
 
 			// QUESTION - does this checking work?
 			// If background data settings is off then do nothing.
 			ConnectivityManager cmgr = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
 			if( cmgr.getBackgroundDataSetting() ) {
 				// For ICE_CREAM_SANDWICH, cmgr.getBackgroundDataSetting() always returns true...
 				// But the code bellow, won't soding work!
 				//if( cmgr.getActiveNetworkInfo() == null || !cmgr.getActiveNetworkInfo().isAvailable() || !cmgr.getActiveNetworkInfo().isConnected() ) {
 				//	return;
 				//}
 				generateNotifications( intent );
 			}
 		} finally {
 			getLock(this).release();
 		}
 	}
 	
 	protected void setNextWakeup(Intent intent) {
 		// If we're polling in dynamic mode, then set the next poll alarm.
 		if( IMReady.getPollingInterval(this) == 2 ){
 			IMReady.setNextAlarm(this);
 		}
 	}
 
 	synchronized private static PowerManager.WakeLock getLock(Context context) {
 		if (lock == null) {
 			PowerManager pmgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
 			lock = pmgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME);
 			lock.setReferenceCounted(true);
 		}
 		return (lock);
 	}
 
 	public static void acquireLock(Context context) {
 		getLock(context).acquire();
 	}
 
 }

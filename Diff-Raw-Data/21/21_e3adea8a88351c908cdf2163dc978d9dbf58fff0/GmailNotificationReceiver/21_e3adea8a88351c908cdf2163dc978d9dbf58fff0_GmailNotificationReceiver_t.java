 /* Copyright (C) 2011 Eduardo Gonzalez See LICENSE for details. */
 
 package com.yumusoft.bettermail;
 
 import java.util.Date;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.net.Uri;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import com.yumusoft.bettermail.R;
 
 import static com.yumusoft.bettermail.Globals.DEBUG;
 
 public class GmailNotificationReceiver extends BroadcastReceiver {
 	
 	private Context _context;
 	private String _account;
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
		if (DEBUG) Log.d("BetterMailReceiver", "Unread Count Changed");
 
 		SharedPreferences pref = 
 			PreferenceManager.getDefaultSharedPreferences(context);
 		
 		
 		_context = context;
 		_account = pref.getString(stringFor(R.string.pref_email), null);
 		
 		NotificationManager nm = 
 			(NotificationManager)context.getSystemService(
 					Context.NOTIFICATION_SERVICE);
 		nm.cancel(0);
 		
		String unread = null;
		try {
			unread = checkUnread(prefPriorityInbox(pref));
		} catch (Throwable e) {
			// TODO: Need to find out why this fails sometimes.
		}
		
 		if (unread == null) {
 			return;
 		}
 		
 		int unreadCount = Integer.parseInt(unread);
 		
		if (DEBUG) Log.d("BetterMailReceiver", "Unread Count: " + unreadCount);
 
 		
 		int lastUnreadCount = pref.getInt("lastUnreadCount", 0);
 		if (DEBUG) 
			Log.d("BetterMailReceiver", "Last Unread Count: " + 
 					lastUnreadCount);
 		
 		SharedPreferences.Editor editor = pref.edit();
 		editor.putInt("lastUnreadCount", unreadCount);
 		editor.commit();
 		
 		if (unreadCount == 0) {
 			return;
 		}
 		
 		if (pref.getBoolean(stringFor(R.string.pref_show_notification), false)) {
 			Notification notification = 
 				new Notification(R.drawable.stat_notify_email_generic, 
 						String.format(stringFor(
 								R.string.notification_new_unread), 
 								unreadCount),	
 					System.currentTimeMillis());
 			
 			if (unreadCount > lastUnreadCount) {
 				if (prefVibrate(pref)) {
 					notification.defaults |= Notification.DEFAULT_VIBRATE;
 				} else {
 					notification.vibrate = new long[] { 0 };
 				}
 			
 				if (prefSound(pref)) {
 					notification.defaults |= Notification.DEFAULT_SOUND;
 				} else {
 					notification.sound = null;
 				}
 				
 				if (prefLED(pref)) {
 					// Doesn't seem to do anything.
 					//notification.defaults |= Notification.DEFAULT_LIGHTS;
 					
 					notification.flags |= Notification.FLAG_SHOW_LIGHTS;
 					notification.ledARGB = Color.GREEN;
 					notification.ledOffMS = 300;
 					notification.ledOnMS = 200;
 				} else {
 					notification.ledARGB = Color.TRANSPARENT;
 					notification.ledOffMS = 0;
 					notification.ledOnMS = 0;
 				}
 			}
 			
 			Intent launchBetterGmailIntent = new Intent(context, 
 					BetterMailActivity.class);
 				
 			PendingIntent pending = PendingIntent.getActivity(context, 0,
 					launchBetterGmailIntent, 0);
 		
 			notification.setLatestEventInfo(context, 
 					stringFor(R.string.app_name),
 					String.format(stringFor(R.string.notification_unread_count), 
 							unreadCount), 
 					pending);
 
 			nm.notify(0, notification);
 		}
 	}
 	
 	private String stringFor(int pref) {
 		return _context.getResources().getString(pref);
 	}
 	
 	private boolean prefVibrate(SharedPreferences pref) {
 		if (prefSleepMode(pref)) {
 			return false;
 		}
 		
 		return pref.getBoolean(stringFor(R.string.pref_vibrate), true);
 	}
 	
 	private boolean prefSound(SharedPreferences pref) {
 		if (prefSleepMode(pref)) {
 			return false;
 		}
 		
 		return pref.getBoolean(stringFor(R.string.pref_sound_notification), 
 				true);
 	}
 	
 	private boolean prefLED(SharedPreferences pref) {
 		return pref.getBoolean(stringFor(R.string.pref_led_notification), true);
 	}
 	
 	/**
 	 * Return true if the current time falls inside sleep mode.
 	 * @param pref SharedPreferences
 	 * @return false if not currently sleep mode, true if currently sleep mode.
 	 */
 	private boolean prefSleepMode(SharedPreferences pref) {
 		boolean sleepMode = 
 			pref.getBoolean(stringFor(R.string.pref_quiet_mode), false);
 		String startString = 
 			pref.getString(stringFor(R.string.pref_quiet_start), "00:00");
 		String endString = 
 			pref.getString(stringFor(R.string.pref_quiet_end), "00:00");
 		int startHour = Integer.parseInt(startString.split(":")[0]);
 		int startMin = Integer.parseInt(startString.split(":")[1]);
 		
 		int endHour = Integer.parseInt(endString.split(":")[0]);
 		int endMin = Integer.parseInt(endString.split(":")[1]);
 		
 		if (sleepMode) {
 			Date now = new Date();
 			Date start = new Date(now.getYear(), now.getMonth(), now.getDate(), 
 					startHour, startMin);
 			Date end;
 			
 			if (startHour > endHour) {
 				end = new Date(now.getYear(), now.getMonth(), now.getDate() + 1,
 						endHour, endMin);
 			} else {
 				end = new Date(now.getYear(), now.getMonth(), now.getDate(), 
 						endHour, endMin);
 			}
 			
 			if ((now.after(start)) && (now.before(end))) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	private boolean prefPriorityInbox(SharedPreferences pref) {
 		return pref.getBoolean(stringFor(R.string.pref_priority_inbox), true);
 	}
 	
 
 	private String checkUnread(boolean priorityInbox) {
 		if (_account == null) {
 			return null;
 		}
 		
 		ContentResolver contentResolver = _context.getContentResolver();
 		
 		Uri labelsUri = Uri.parse("content://gmail-ls/labels/");
		// Crash reports here... Need to find out why.
		Uri accountUri = Uri.withAppendedPath(labelsUri, _account); 
 	    	    
 		Cursor cursor = contentResolver.query(accountUri, null, null, null, 
 				null);
 
 		int unreadColumn = 
 				cursor.getColumnIndex(LabelColumns.NUM_UNREAD_CONVERSATIONS);
 		int nameColumn = cursor.getColumnIndex(LabelColumns.NAME);
 			
 		if (cursor.moveToFirst()) {
 			do {
 				String nameColVal = cursor.getString(nameColumn);
 				
 				if (priorityInbox && nameColVal.equals("^iim")) {
 						return cursor.getString(unreadColumn);
 				} else if ((!priorityInbox) && nameColVal.equals("^i")) {
 						return cursor.getString(unreadColumn);
 				}
 			} while (cursor.moveToNext());
 		}
 
 		return null;
 	}
 	
 	private static final class LabelColumns {
         public static final String NAME = "name";
         public static final String NUM_UNREAD_CONVERSATIONS =
         	"numUnreadConversations";
     }
 }

 package de.geotweeter;
 
 import com.alibaba.fastjson.JSONException;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 import com.google.android.gcm.GCMBaseIntentService;
 
 import de.geotweeter.R;
 import de.geotweeter.activities.NewTweetActivity;
 import de.geotweeter.activities.TimelineActivity;
 import de.geotweeter.exceptions.UnknownJSONObjectException;
 import de.geotweeter.timelineelements.DirectMessage;
 import de.geotweeter.timelineelements.TimelineElement;
 import de.geotweeter.timelineelements.Tweet;
 
 public class GCMIntentService extends GCMBaseIntentService {
 	private static final String LOG = "GCMIntentService";
 
 	@Override
 	protected void onError(Context context, String error_id) {
 		Log.d(LOG, "onError - " + error_id);
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onMessage(Context context, Intent intent) {
 		Log.d(LOG, "onMessage");
 		SharedPreferences pref = getSharedPreferences(Constants.PREFS_APP, 0);
 		if (!pref.getBoolean("pref_notifications_enabled", true)) {
 			return;
 		}
 		
 		if (pref.getBoolean("pref_notifications_silent_time_enabled", false)) {
 			long start = pref.getLong("pref_notifications_silent_time_start", -1);
 			long end   = pref.getLong("pref_notifications_silent_time_end",   -1);
 			long now   = System.currentTimeMillis() % (24*60*60*1000);
 			Log.d(LOG, "" + start + " " + end + " " + now);
 			if (start >= 0 && end >= 0) {
 				if (start > end) {
 					/* Start ist vor Mitternach, Ende danach. */
 					if (now >= start || now <= end) {
 						return;
 					}
 				} else {
 					/* Start und Ende sind am gleichen Tag. */
 					if (now >= start && now <= end) {
 						return;
 					}
 				}
 			}
 		}
 		
 		TimelineElement t;
 		try {
 			t = Utils.jsonToNativeObject(intent.getExtras().getString("data"));
 		} catch (JSONException e) {
 			return;
 		} catch (UnknownJSONObjectException e) {
 			return;
 		}
 		
 		if (!t.showNotification()) {
 			return;
 		}
 		
 		int id = t.hashCode();
 		String type = intent.getExtras().getString("type");
 		
 		if ("mention".equals(type) && !pref.getBoolean("pref_notifications_types_mentions", true)) {
 			return;
 		} else if ("dm".equals(type) && !pref.getBoolean("pref_notifications_types_direct_messages", true)) {
 			return;
 		} else if ("favorite".equals(type) && !pref.getBoolean("pref_notifications_types_favorites", false)) {
 			return;
 		} else if ("retweet".equals(type) && !pref.getBoolean("pref_notifications_types_retweets", false)) {
 			return;
 		}
 		
 		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		CharSequence notificationText = t.getNotificationText(type);
 		Notification notification = new Notification(R.drawable.ic_launcher, notificationText, System.currentTimeMillis());
 		CharSequence contentTitle = t.getNotificationContentTitle(type);
 		CharSequence contentText = t.getNotificationContentText(type);
 		Intent notificationIntent;
 		if (t instanceof Tweet || t instanceof DirectMessage) {
 			notificationIntent = new Intent(this, NewTweetActivity.class);
 			notificationIntent.putExtra("de.geotweeter.reply_to_tweet", t);
 		} else {
 			notificationIntent = new Intent(this, TimelineActivity.class);
 		}
 		PendingIntent contentIntent = PendingIntent.getActivity(this, (int)System.currentTimeMillis(), notificationIntent, 0);
 		notification.flags |= Notification.FLAG_AUTO_CANCEL;
 		
 		RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_with_small_text);
 		contentView.setTextViewText(R.id.txtTitle, contentTitle);
 		contentView.setTextViewText(R.id.txtText, contentText);
 		notification.contentView = contentView;
 		//notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
 		notification.contentIntent = contentIntent;
 		
 		
 		if (pref.getBoolean("pref_notifications_sound_enabled", true)) {
 			String sound = pref.getString("pref_notifications_sound_ringtone", "DEFAULT_RINGTONE_URI");
 			if (! "".equals(sound)) {
 				notification.sound = Uri.parse(sound);
 			}
 		}
 		
 		if (pref.getBoolean("pref_notifications_vibration_enabled", true)) {
 			notification.vibrate = new long[] {0, 200, 500, 200};
 		}
 		
 		if (pref.getBoolean("pref_notifications_led_enabled", false)) {
			notification.ledARGB = Integer.parseInt(pref.getString("pref_notifications_led_color", "4278190335"));
 			notification.ledOnMS = 200;
 			notification.ledOffMS = 1000;
 			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
 		}
 		notificationManager.notify(id, notification);
 	}
 
 	@Override
 	protected void onRegistered(Context context, String reg_id) {
 		Log.d(LOG, "onRegistered - " + reg_id);
 		TimelineActivity.reg_id = reg_id;
 		for (Account a : Account.all_accounts) {
 			a.registerForGCMMessages();
 		}
 	}
 
 	@Override
 	protected void onUnregistered(Context context, String reg_id) {
 		Log.d(LOG, "onUnregistered - " + reg_id);
 	}
 
 }

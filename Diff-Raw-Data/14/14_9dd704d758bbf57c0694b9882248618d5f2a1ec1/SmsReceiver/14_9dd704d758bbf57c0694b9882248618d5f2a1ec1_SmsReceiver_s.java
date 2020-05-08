 package com.snot.smswakeup;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.telephony.SmsMessage;
 import android.database.Cursor;
 import android.util.Log;
 import android.app.NotificationManager;
 import android.support.v4.app.NotificationCompat;
 import android.app.Notification;
 import android.app.PendingIntent;
 
 import android.media.AudioManager;
 
 import com.snot.smswakeup.database.Blacklist;
 import com.snot.smswakeup.database.Provider;
 
 /**
  * @author snot
  *
  * TODO: refactor alarm handling into a method of it's own
  */
 public class SmsReceiver extends BroadcastReceiver {
 
 	private final static String TAG = "SmsReceiver";
 	Context context;
 
 	SharedPreferences prefs;
 	public static final int NOTIFICATION_ID = 1;
 	NotificationCompat.Builder builder;
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		this.context = context;
 
 		prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		// TODO: dont hard code strings
		String wakeUpCommand = prefs.getString("wakeup_cmd", "WAKEUP");
		boolean caseSensetiveCompare = prefs.getBoolean("case_sensetive_cmp", false);
 
 		Bundle pudsBundle = intent.getExtras();
 		Object[] pdus = (Object[]) pudsBundle.get("pdus");
 		SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdus[0]);
 
 		String phoneNumber = sms.getOriginatingAddress();
 		String message = sms.getMessageBody().trim();
 
		if(!caseSensetiveCompare)
 		{
 			wakeUpCommand = wakeUpCommand.toLowerCase();
 			message = message.toLowerCase();
 		}
 
 		Log.d(TAG, phoneNumber);
 		boolean isBlacklisted = isBlacklisted(phoneNumber);
 		Log.d(TAG, "isBlacklisted: " + isBlacklisted);
 
 		if(message.equals(wakeUpCommand) && !isBlacklisted)
 		{
 			AlarmNotification("Wake up msg from " + phoneNumber);
 			soundAlarm();
 		}
 		if(isBlacklisted)
 		{
 			BlacklistNotification("Message from blacklisted number: " + phoneNumber);
 		}
 	}
 
 
 	private void soundAlarm()
 	{
 		boolean maximizeVolume = prefs.getBoolean("maximize_volume", true);
 		// Maximize volume
 		if(maximizeVolume)
 		{
 			AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
 			am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
 		}
 		// Sound alarm
 		SoundAlarm.getInstance().initalizeMediaPlayer(context);
 		SoundAlarm.getInstance().start();
 	}
 	
 	private boolean isBlacklisted(String phoneNumber)
 	{
 		// get contact id from phonenumber
 		long[] ids = ContactUtil.getContactIdsByPhoneNumber(context, phoneNumber);
 		// see if contact id is listed in our db
 		// TODO: don't query for each... use sql IN clause
 		for(long id : ids)
 		{
 			Cursor cursor = context.getContentResolver().query(Provider.URI_BLACKLIST,
 				new String[] { Blacklist.COL_CONTACT_ID},
 				Blacklist.COL_CONTACT_ID + " = ?",
 				new String[] { String.valueOf(id) },
 				null);
 			if(cursor.getCount() > 0)
 			{
 				return(true);
 			}
 		}
 		return false;
 	}
 
 	private void BlacklistNotification(String msg) {
 		Log.d(TAG, "BlacklistNotification");
 		Intent intent = new Intent(context, BlacklistActivity.class);
 		sendNotification(msg, intent);
 	}
 
 	private void AlarmNotification(String msg) {
 		Log.d(TAG, "AlarmNotification");
 		Intent intent = new Intent(context, MainActivity.class);
 		intent.putExtra(MainActivity.INTENT_SILENCE, true);
 		sendNotification(msg, intent);
 	}
 
 	private void sendNotification(String msg, Intent intent) {
 		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
 
 		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 
 		Notification notification = new NotificationCompat.Builder(context)
 			.setSmallIcon(R.drawable.ic_launcher)
 			.setContentTitle(context.getString(R.string.app_name))
 			.setStyle(new NotificationCompat.BigTextStyle())
 			.setContentText(msg)
 			.setContentIntent(contentIntent)
 			.build();
 
 		notification.flags |= Notification.FLAG_AUTO_CANCEL;
 
 		notificationManager.notify(NOTIFICATION_ID, notification);
 	}
 
 }
 

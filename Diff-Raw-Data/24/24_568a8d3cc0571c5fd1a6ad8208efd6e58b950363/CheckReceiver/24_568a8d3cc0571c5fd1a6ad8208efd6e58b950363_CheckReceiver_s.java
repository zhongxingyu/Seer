 package de.ub0r.android.networkchecker;
 
 import android.app.AlarmManager;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.SystemClock;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NotificationCompat;
 import android.telephony.TelephonyManager;
 import android.text.TextUtils;
 import android.util.Log;
 
 /**
  * @author flx
  */
 public class CheckReceiver extends BroadcastReceiver {
 
     private static final String TAG = "CheckReceiver";
 
     private static final String ACTION_NOTFY = "de.ub0r.android.networkchecker.NOTIFY";
 
     static final String ACTION_SNOOZE = "de.ub0r.android.networkchecker.SNOOZE";
 
     private static final long DELAY_FIRST_CHECK = 10 * 1000;
 
     private static final long DELAY_NEXT_CHECK = 60 * 1000;
 
    private static final long DELAY_SNOOZE = 10 * 60 * 60 * 1000;
 
     private static final int ARGB = 0xffff;
 
     private static final int MSON = 2000;
 
     private static final int MSOFF = 1000;
 
     private static final long[] VIBRATE = new long[]{2000, 1000, 500, 1000, 500};
 
     @Override
     public void onReceive(final Context context, final Intent intent) {
         Log.d(TAG, "onReceive(" + intent.getAction() + ")");
         if (ACTION_NOTFY.equals(intent.getAction())) {
             checkAndNotify(context, intent);
         } else if (ACTION_SNOOZE.equals(intent.getAction())) {
             snooze(context);
         } else {
             schedCheck(context, intent);
         }
     }
 
     private void snooze(Context context) {
         SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(context).edit();
         e.putLong("snooze_until", System.currentTimeMillis() + DELAY_SNOOZE);
         e.commit();
     }
 
     private long getDelay(final Context context, final Intent intent) {
         if (ACTION_NOTFY.equals(intent.getAction())) {
             String s = PreferenceManager.getDefaultSharedPreferences(context)
                     .getString("delay", "60");
             long delay;
             try {
                 delay = Long.parseLong(s.trim()) * 1000;
             } catch (NumberFormatException e) {
                 Log.w(TAG, "not a number: " + s);
                 delay = DELAY_NEXT_CHECK;
             }
             return delay;
         } else {
             return DELAY_FIRST_CHECK;
         }
     }
 
     private void schedCheck(final Context context, final Intent intent) {
         Log.d(TAG, "schedCheck()");
         final PendingIntent pi = PendingIntent.getBroadcast(context, 0,
                 new Intent(ACTION_NOTFY, null, context, CheckReceiver.class),
                 PendingIntent.FLAG_CANCEL_CURRENT);
         final long t = SystemClock.elapsedRealtime() + getDelay(context, intent);
         final AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
         mgr.set(AlarmManager.ELAPSED_REALTIME, t, pi);
     }
 
     private void checkAndNotify(final Context context, final Intent intent) {
         Log.d(TAG, "checkAndNotify()");
         if (check(context)) {
             notify(context, intent);
         } else {
             cancelNotification(context);
         }
     }
 
     private boolean check(final Context context) {
         Log.d(TAG, "check()");
 
         // telephony
         TelephonyManager tp = (TelephonyManager) context.getSystemService(
                 Context.TELEPHONY_SERVICE);
         if (TextUtils.isEmpty(tp.getNetworkOperator()) && TextUtils.isEmpty(
                 tp.getNetworkOperatorName())
                 && tp.getNetworkType() == TelephonyManager.NETWORK_TYPE_UNKNOWN) {
             Log.w(TAG, "dead phone!");
             return true;
         }
 
         return false;
     }
 
     private void notify(final Context context, final Intent intent) {
         Log.d(TAG, "notify()");
         SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
         if (!p.getBoolean("enable", true)) {
             Log.i(TAG, "notifications are disabled");
             return;
         }
 
         if (System.currentTimeMillis() > p.getLong("snooze_until", 0)) {
             NotificationCompat.Builder b = new NotificationCompat.Builder(context);
             b.setContentIntent(PendingIntent.getActivity(context, 0,
                     new Intent(context, InfoActivity.class), PendingIntent.FLAG_CANCEL_CURRENT));
             b.setSmallIcon(android.R.drawable.stat_sys_warning);
             b.setContentText(context.getString(R.string.notification_message));
             b.setContentTitle(context.getString(R.string.notification_title));
             b.addAction(0, context.getString(R.string.snooze), PendingIntent
                     .getBroadcast(context, 0,
                             new Intent(ACTION_SNOOZE, null, context, CheckReceiver.class),
                             PendingIntent.FLAG_UPDATE_CURRENT));
 
             b.setLights(ARGB, MSON, MSOFF);
 
             String s = p.getString("sound", null);
             if (s != null) {
                 b.setSound(Uri.parse(s));
             }
 
             if (p.getBoolean("vibrate", true)) {
                 b.setVibrate(VIBRATE);
             }
 
             NotificationManager nm = (NotificationManager) context
                     .getSystemService(Context.NOTIFICATION_SERVICE);
             nm.notify(0, b.build());
         } else {
             Log.i(TAG, "snoozing");
         }
 
         // schedule next notification
         schedCheck(context, intent);
     }
 
     private void cancelNotification(final Context context) {
         Log.d(TAG, "cancelNotification()");
         NotificationManager nm = (NotificationManager) context
                 .getSystemService(Context.NOTIFICATION_SERVICE);
         nm.cancelAll();
     }
 }

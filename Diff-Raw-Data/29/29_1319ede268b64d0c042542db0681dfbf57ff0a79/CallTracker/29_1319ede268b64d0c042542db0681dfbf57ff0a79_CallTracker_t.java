 package in.manki.android.callcounter;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.provider.CallLog;
import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 
 public class CallTracker extends BroadcastReceiver {
 
   private static final String TAG = "CallTracker";
   private static final long WAIT_TIME_FOR_CALL_LOG_UPDATE = 5 * 1000;
   private static final int WARN_MINUTES = 10;
   private static final String[] SELECTED_COLS = new String[] {
       CallLog.Calls.DATE,
       CallLog.Calls.CACHED_NAME,
       CallLog.Calls.NUMBER,
       CallLog.Calls.DURATION,
   };
 
   @Override
   public void onReceive(final Context ctx, Intent intent) {
     final Storage storage = Storage.get(ctx);
 
     if (!isInIdleState(intent)) {
       return;
     }
 
     new Thread(new Runnable() {
       @Override
       public void run() {
         // Wait for call log database to be updated.
         // Otherwise we won't find the call that has just ended.
         try {
           Log.d(TAG, "Sleeping for call log to be updated.");
           Thread.sleep(WAIT_TIME_FOR_CALL_LOG_UPDATE);
         } catch (InterruptedException e) {
           Thread.currentThread().interrupt();
           return;
         }
 
         long limit = Math.max(
             storage.getTrackMinCallTime(), storage.getLastKnownCallTime());
         if (limit == 0 || !storage.isTrackingEnabled()) {
           Log.d(TAG, "Tracking disabled; exiting.");
           return;
         }
 
         Cursor cursor = ctx.getContentResolver().query(
             CallLog.Calls.CONTENT_URI,
             SELECTED_COLS,
             CallLog.Calls.TYPE + " = ? AND "
                 + CallLog.Calls.DATE + " > ?",
                 new String[] {
                 String.valueOf(CallLog.Calls.OUTGOING_TYPE),
                 String.valueOf(limit)},
                 CallLog.Calls.DATE);
         while (cursor.moveToNext()) {
           long callTime = cursor.getLong(
               cursor.getColumnIndex(CallLog.Calls.DATE));
           String name = noNull(cursor.getString(
               cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)));
           String number = cursor.getString(
               cursor.getColumnIndex(CallLog.Calls.NUMBER));
           long seconds = cursor.getLong(
               cursor.getColumnIndex(CallLog.Calls.DURATION));
           boolean tracked =
               storage.isTrackingEnabled() && isTrackableCall(storage, number);
           long minutes = (long) Math.ceil(seconds / 60.0);
           storage.track(name, number, callTime, minutes, tracked);
           Log.d(TAG, String.format("Tracking %d minutes long call to %s.",
               minutes, obfuscate(number)));
         }
 
         notifyIfLowBalance(ctx, storage);
       }
 
       private void notifyIfLowBalance(Context ctx, Storage storage) {
         long remaining = storage.getRemainingMinutes();
         if (remaining <= WARN_MINUTES) {
           NotificationManager nm = (NotificationManager)
               ctx.getSystemService(Context.NOTIFICATION_SERVICE);
 
           Resources res = ctx.getResources();
           String lowBalance = res.getString(R.string.low_discount_call_balance);
           String msg =
               String.format(res.getString(R.string.n_minutes_left), remaining);
 
           PendingIntent pi = PendingIntent.getActivity(
               ctx,
               0,
               new Intent(ctx, CallCounterActivity.class),
               Notification.FLAG_AUTO_CANCEL);

          NotificationCompat.Builder notif = new NotificationCompat.Builder(ctx)
              .setSmallIcon(R.drawable.ic_launcher)
              .setTicker(lowBalance)
              .setContentText(msg)
              .setContentIntent(pi);
          nm.notify(NotificationId.LOW_BALANCE.get(), notif.getNotification());
         }
       }
 
       private String noNull(String str) {
         return str == null ? "" : str;
       }
 
       private String obfuscate(String number) {
         int len = number.length();
         int mid = len / 2;
         int start = mid - mid / 2;
         int end = mid + mid / 2;
         return number.substring(0, start)
             + times("*", len - (end - start))
             + number.substring(end);
       }
 
       private String times(String str, int n) {
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < n; ++i) {
           sb.append(str);
         }
         return sb.toString();
       }
     }).start();
   }
 
   private boolean isInIdleState(Intent intent) {
     String state = intent.getExtras().getString("state");
     return "IDLE".equals(state);
   }
 
   private boolean isTrackableCall(Storage storage, String number) {
     for (String prefix : storage.getTrackabelNumberPrefixes()) {
       if (number.startsWith(prefix)) {
         return true;
       }
     }
     return false;
   }
 }

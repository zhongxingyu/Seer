 package com.jeffthefate.dmbquiz.receiver;
 
 import java.util.Date;
 import java.util.TimeZone;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences.Editor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.wifi.WifiManager;
 import android.net.wifi.WifiManager.MulticastLock;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 
 import com.jeffthefate.dmbquiz.ApplicationEx;
 import com.jeffthefate.dmbquiz.ApplicationEx.ResourcesSingleton;
 import com.jeffthefate.dmbquiz.ApplicationEx.SharedPreferencesSingleton;
 import com.jeffthefate.dmbquiz.Constants;
 import com.jeffthefate.dmbquiz.R;
 import com.jeffthefate.dmbquiz.activity.ActivityMain;
 
 public class PushReceiver extends BroadcastReceiver {
     
     private NotificationCompat.Builder nBuilder;
     private NotificationManager nManager;
     private WakeLock wakeLock;
 	private MulticastLock multicastLock;
     
     @Override
     public void onReceive(Context context, Intent intent) {
     	Log.i(Constants.LOG_TAG, "PUSH RECEIVED!");
     	PowerManager pm = 
                 (PowerManager) context.getSystemService(Context.POWER_SERVICE);
         wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                 Constants.PUSH_WAKE_LOCK);
         wakeLock.acquire();
         WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
         multicastLock = wm.createMulticastLock(Constants.PUSH_WIFI_LOCK);
         multicastLock.acquire();
         if (Build.VERSION.SDK_INT <
                 Build.VERSION_CODES.HONEYCOMB)
         	new ReceiveTask(intent).execute();
         else
         	new ReceiveTask(intent).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
     }
     
     private static String getUpdatedDateString(long millis) {
         ApplicationEx.df.setTimeZone(TimeZone.getDefault());
         Date date = new Date();
         date.setTime(millis);
         return ApplicationEx.df.format(date.getTime());
     }
     
     private class ReceiveTask extends AsyncTask<Void, Void, Void> {
     	private Intent intent;
     	
     	private ReceiveTask(Intent intent) {
     		this.intent = intent;
     	}
     	
         @Override
         protected Void doInBackground(Void... nothing) {
         	// {"action":"com.jeffthefate.dmb.ACTION_NEW_QUESTIONS"}
         	Log.i(Constants.LOG_TAG, "TASK STARTED!");
             nManager = (NotificationManager) ApplicationEx.getApp()
                     .getSystemService(Context.NOTIFICATION_SERVICE);
             String action = intent.getAction();
             if (action.equals(Constants.ACTION_NEW_QUESTIONS)) {
             	Log.i(Constants.LOG_TAG, "NEW QUESTIONS!");
                 if (!ApplicationEx.isActive() && SharedPreferencesSingleton.instance().getBoolean(
                         ApplicationEx.getApp().getString(R.string.notification_key),
                         true)) {
                     // Show notification that starts app
                     Intent notificationIntent = new Intent(
                             ApplicationEx.getApp(), ActivityMain.class);
                     //ParseAnalytics.trackAppOpened(notificationIntent);
                     notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                             Intent.FLAG_ACTIVITY_SINGLE_TOP);
                     PendingIntent pendingIntent = PendingIntent.getActivity(
                                 ApplicationEx.getApp(), 0, notificationIntent,
                                 Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                     nBuilder = new NotificationCompat.Builder(
                             ApplicationEx.getApp());
                     nBuilder.setLargeIcon(BitmapFactory.decodeResource(ResourcesSingleton.instance(),
                                 R.drawable.notification_large)).
                         setSmallIcon(R.drawable.notification_large).
                         setWhen(System.currentTimeMillis()).
                         setContentTitle("New questions added").
                         setContentText("Touch to play DMB Trivia").
                         setContentIntent(pendingIntent);
                     nManager.cancel(Constants.NOTIFICATION_NEW_QUESTIONS);
                     nManager.notify(null, Constants.NOTIFICATION_NEW_QUESTIONS,
                     		nBuilder.build());
                 }
             }
             else if (action.equals(Constants.ACTION_NEW_SONG)) {
             	Log.i(Constants.LOG_TAG, "NEW SONG!");
                 JSONObject json = null;
                 String latestSong = "";
                 try {
                 	if (!intent.hasExtra("com.parse.Data"))
                 		throw new JSONException("No data sent!");
                     json = new JSONObject(intent.getExtras().getString(
                             "com.parse.Data"));
                     latestSong = json.getString("song");
                     Editor editor = SharedPreferencesSingleton.instance()
                     		.edit();
                     editor.putString(ResourcesSingleton.instance().getString(
                     		R.string.lastsong_key), latestSong);
                     editor.putString(ResourcesSingleton.instance().getString(
                     		R.string.setlist_key), json.getString("setlist"));
                     StringBuilder sb = new StringBuilder();
                     sb.append("Updated:\n");
                     sb.append(getUpdatedDateString(
                     		Long.parseLong(json.getString("timestamp"))));
                     editor.putString(ResourcesSingleton.instance().getString(
                     		R.string.setstamp_key), sb.toString());
                     if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
                     	editor.commit();
                     else
                     	editor.apply();
                     /*
                     StringBuilder sb = new StringBuilder();
                     sb.append("Updated:\n");
                     sb.append(getUpdatedDateString(System.currentTimeMillis()));
                     ApplicationEx.setlistStamp = sb.toString();
                     */
                     Intent setIntent = new Intent(Constants.ACTION_UPDATE_SETLIST);
                     setIntent.putExtra("success", true);
                     ApplicationEx.getApp().sendBroadcast(setIntent);
                     /*
                     ApplicationEx.parseSetlist();
                     ParseQuery setlistQuery = new ParseQuery("Setlist");
                     setlistQuery.addDescendingOrder("setDate");
                     setlistQuery.setLimit(1);
                     setlistQuery.findInBackground(new FindCallback() {
                         @Override
                         public void done(List<ParseObject> setlists, ParseException e) {
                             Intent intent = new Intent(Constants.ACTION_UPDATE_SETLIST);
                             if (e != null) {
                                 Log.e(Constants.LOG_TAG, "Error getting setlist!", e);
                                 intent.putExtra("success", false);
                             }
                             else {
                                 ApplicationEx.df.setTimeZone(TimeZone.getDefault());
                                 StringBuilder sb = new StringBuilder();
                                 sb.append("Updated:\n");
                                 sb.append(DateFormat.format(ApplicationEx.df.toLocalizedPattern(), setlists.get(0).getUpdatedAt()));
                                 ApplicationEx.setlistStamp = sb.toString();
                                 intent.putExtra("success", true);
                             }
                             ApplicationEx.getApp().sendBroadcast(intent);
                         }
                     });
                     */
                 } catch (JSONException e) {
                     Log.e(Constants.LOG_TAG, "Bad push notification data!", e);
                 }
                 if (latestSong != null && !latestSong.equals("null") &&
                         !latestSong.equals("") && SharedPreferencesSingleton
                         .instance().getBoolean(ApplicationEx.getApp().getString(
                                 R.string.notification_key), true)) {
                	nManager.cancel(Constants.NOTIFICATION_NEW_SONG);
                 	Log.i(Constants.LOG_TAG, "LATEST SONG: " + latestSong);
                 	ApplicationEx.findMatchingAudio(latestSong);
                     nBuilder = new NotificationCompat.Builder(
                             ApplicationEx.getApp());
                     Bitmap largeIcon = ApplicationEx.resizeImage(ResourcesSingleton.instance(),
                             ApplicationEx.findMatchingImage(latestSong));
                     nBuilder.setLargeIcon(largeIcon).
                         setSmallIcon(R.drawable.notification_large).
                         setWhen(System.currentTimeMillis()).
                         setTicker(latestSong).
                         setContentTitle(latestSong).
                         setLights(0xffff0000, 2000, 10000);
                     switch (SharedPreferencesSingleton.instance().getInt(
                     		ResourcesSingleton.instance().getString(R.string.notificationsound_key), 0)) {
                     case 0:
                         nBuilder.setVibrate(new long[]{0, 500, 100, 500});
                         nBuilder.setSound(ApplicationEx.notificationSound);
                         break;
                     case 1:
                         nBuilder.setSound(ApplicationEx.notificationSound);
                         break;
                     case 2:
                         nBuilder.setVibrate(new long[]{0, 500, 100, 500});
                         break;
                     }
                     Intent notificationIntent = new Intent(
                             ApplicationEx.getApp(), ActivityMain.class);
                     notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                             Intent.FLAG_ACTIVITY_SINGLE_TOP);
                     notificationIntent.putExtra("setlist", true);
                     PendingIntent pendingIntent = PendingIntent.getActivity(
                                 ApplicationEx.getApp(), 0, notificationIntent,
                                 Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                     nBuilder.setContentIntent(pendingIntent);
                     StringBuilder sb = new StringBuilder();
                     if (ApplicationEx.setlistList != null &&
                             !ApplicationEx.setlistList.isEmpty()) {
                         sb.append(ApplicationEx.setlistList.get(2));
                         sb.append(" - ");
                         sb.append(ApplicationEx.setlistList.get(3));
                         nBuilder.setContentText(sb.toString());
                     }
                     nManager.notify(null, Constants.NOTIFICATION_NEW_SONG,
                     		nBuilder.build());
                 }
             }
             /*
             else if (action.equals(Constants.ACTION_UPDATE_AUDIO)) {
             	Log.i(Constants.LOG_TAG, "UPDATE_AUDIO!");
             	JSONObject json = null;
                 try {
                 	if (!intent.hasExtra("com.parse.Data"))
                 		throw new JSONException("No data sent!");
                     json = new JSONObject(intent.getExtras().getString(
                             "com.parse.Data"));
                     JSONArray songsArray = json.getJSONArray("songs");
                     Gson gson = new Gson();
                     Type type = new TypeToken<List<String>>(){}.getType();
                     List<String> list = gson.fromJson(songsArray.toString(), type);
                     if (!ApplicationEx.isDownloading())
                     	ApplicationEx.downloadSongClips(list);
             	} catch (JSONException e) {
             		Log.e(Constants.LOG_TAG, "Bad JSON data!", e);
             	}
             }
             */
             /*
              * JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
              */
             return null;
         }
         
         @Override
         protected void onCancelled(Void nothing) {
         }
         
         @Override
         protected void onPostExecute(Void nothing) {
         	multicastLock.release();
         	wakeLock.release();
         }
     }
 }

 package org.nsdev.glitchskills;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.Date;
 import org.json.JSONException;
 import org.json.JSONObject;
 import android.accounts.Account;
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.SystemClock;
 import android.util.Log;
 import android.view.View;
 import android.widget.RemoteViews;
 
 public class LearningWidget extends AppWidgetProvider
 {
     private boolean alarmSet = false;
 
     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
     {
         Intent updateIntent = new Intent(context, UpdateService.class);
         updateIntent.setAction("FastUpdate");
         context.startService(updateIntent);
 
         if (!alarmSet)
         {
             alarmSet = true;
             final AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
 
             // Fast update
             final Intent intent2 = new Intent(context, UpdateService.class);
             intent2.setAction("FastUpdate");
             final PendingIntent pending2 = PendingIntent.getService(context, 0, intent2, 0);
             alarm.cancel(pending2);
             long interval2 = 1000 * 60;
             alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), interval2, pending2);
         }
     }
 
     public static class UpdateService extends Service
     {
         private static Bitmap cachedIconBitmap;
         private static String cachedIconUrl;
         
         private JSONObject currentSkill;
 
         private final class LoadIconAsyncTask extends AsyncTask<String, Object, Bitmap>
         {
             private static final String TAG = "LoadIconAsyncTask";
             private final Context context;
             private final RemoteViews updateViews;
 
             private LoadIconAsyncTask(Context context, RemoteViews updateViews)
             {
                 this.context = context;
                 this.updateViews = updateViews;
             }
 
             @Override
             protected Bitmap doInBackground(String... args)
             {
                 String iconUrl = args[0];
 
                 if (iconUrl != null)
                 {
                     if (cachedIconUrl != null && iconUrl.equals(cachedIconUrl) && cachedIconBitmap != null)
                     {
                         if (Constants.DEBUG)
                             Log.d(TAG, "Returning cached icon bitmap.");
                         return cachedIconBitmap;
                     }
 
                     Bitmap b = null;
                     try
                     {
                         if (Constants.DEBUG)
                             Log.d(TAG, "Downloading icon: " + iconUrl);
                         b = BitmapFactory.decodeStream(((java.io.InputStream)new java.net.URL(iconUrl).getContent()));
                         cachedIconUrl = iconUrl;
                         return b;
                     }
                     catch (MalformedURLException e)
                     {
                     }
                     catch (IOException e)
                     {
                     }
                     catch (NullPointerException e)
                     {
                     }
                 }
                 return null;
             }
 
             @Override
             protected void onPostExecute(Bitmap result)
             {
                 updateIconAndUpdateAppWidget(context, updateViews, result);
                 cachedIconBitmap = result;
             }
         }
 
         @Override
         public IBinder onBind(Intent arg0)
         {
             return null;
         }
 
         @Override
         public void onStart(Intent intent, int startId)
         {
             if (intent == null)
                 return;
 
             String action = intent.getAction();
 
             if ("FinishedNotification".equals(action))
             {
                 String text = intent.getStringExtra("text");
                 NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                 Notification notification = new Notification(R.drawable.ic_learning_finished, text, System.currentTimeMillis());
                 notification.flags |= Notification.FLAG_AUTO_CANCEL;
                 notification.flags |= Notification.FLAG_SHOW_LIGHTS;
                 notification.defaults = 0;
                 notification.defaults |= Notification.DEFAULT_SOUND;
                 notification.ledARGB = 0xFF00FF00;
                 notification.ledOnMS = 250;
                 notification.ledOffMS = 250;
                 notification.vibrate = new long[] {0L, 200L, 100L, 200L, 100L, 200L, 100L, 200L, 100L};
 
                 CharSequence contentTitle = "Glitch Skills";
                 CharSequence contentText = text;
                 Intent notificationIntent = new Intent(this, GlitchSkillsActivity.class);
                 PendingIntent contentIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, notificationIntent, notification.flags);
                 notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
 
                 nm.notify(0, notification);
                 
                 // Schedule a sync so that we know we're done
                 String playerName = getPlayerName();
                 if (playerName != null)
                 {
                     Account account = new Account(playerName, Constants.ACCOUNT_TYPE);
                     Bundle bundle = new Bundle();
                     bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
 
                     ContentResolver.requestSync(account, Constants.AUTHORITY, bundle);
                 }
 
             }
             else
             {
                 buildUpdate(this);
             }
         }
 
         public void buildUpdate(final Context context)
         {
             final RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.appwidget_learning);
 
             Intent intent = new Intent(context, GlitchSkillsActivity.class);
             intent.setAction("org.nsdev.apps.glitchskills.LEARN");
             PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
             updateViews.setOnClickPendingIntent(R.id.skill_icon, pendingIntent);
 
             JSONObject response = ContentHelper.getContent(context, Constants.SKILLS_LIST_LEARNING);
             if (response != null && response.optInt("ok") == 1)
             {
                 JSONObject learning = response.optJSONObject("learning");
                 if (learning != null)
                 {
                     updateSkill(context, updateViews, learning, false);
                 }
                 else
                 {
                     // Let's see if we're unlearning something right now.
                     response = ContentHelper.getContent(context, Constants.SKILLS_LIST_UNLEARNING);
                     if (response != null && response.optInt("ok") == 1)
                     {
                         JSONObject unlearning = response.optJSONObject("unlearning");
                         if (unlearning != null)
                         {
                             updateSkill(context, updateViews, unlearning, true);
                         }
                         else
                         {
                             updateNotLearning(updateViews);
                         }
                     }
                 }
             }
             else
             {
                 // Set up the default since we probably don't know the user yet
                 updateViews.setImageViewResource(R.id.skill_icon, R.drawable.icon);
                 updateViews.setTextViewText(R.id.skill_title, context.getResources().getText(R.string.app_name));
                 updateViews.setTextViewText(R.id.skill_description, context.getString(R.string.widget_unknown_glitch));
 
                 if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD)
                 {
                     updateViews.setViewVisibility(R.id.skill_progress, View.INVISIBLE);
                     updateViews.setViewVisibility(R.id.skill_time_to_learn, View.INVISIBLE);
                     updateViews.setViewVisibility(R.id.skill_time_to_learn_label, View.INVISIBLE);
                 }
 
                 ComponentName thisWidget = new ComponentName(context, LearningWidget.class);
                 AppWidgetManager manager = AppWidgetManager.getInstance(context);
                 manager.updateAppWidget(thisWidget, updateViews);
             }
         }
 
         private void updateSkill(final Context context, final RemoteViews updateViews, JSONObject newSkill, boolean isUnlearning)
         {
             String name = newSkill.names().optString(0);
             
             if (name == null)
                 return;
             
             JSONObject skill = newSkill.optJSONObject(name);
 
             // Remember the current skill and use that remembered version until a change is
             // detected so we can track unlearning better.
             if (currentSkill == null || !currentSkill.optString("class_tsid").equals(skill.optString("class_tsid")))
             {
                 cachedIconBitmap = null;
                 cachedIconUrl = null;
                 currentSkill = skill;
             }
 
             updateSkillText(updateViews, currentSkill, isUnlearning);
 
             resetLearningFinishedNotification(context, currentSkill);
 
             LoadIconAsyncTask iconTask = new LoadIconAsyncTask(context, updateViews);
             iconTask.execute(currentSkill.optString("icon_100"));
         }
 
         private void resetLearningFinishedNotification(Context ctx, JSONObject skill)
         {
             if (skill != null && skill.has("name") && skill.has("time_complete"))
             {
                 String text = String.format(ctx.getString(R.string.widget_skill_s_finished_learning), skill.optString("name"));
                 Intent alarm = new Intent(ctx, UpdateService.class);
                 alarm.setAction("FinishedNotification");
                 alarm.putExtra("text", text);
                 PendingIntent pend = PendingIntent.getService(ctx, 0, alarm, PendingIntent.FLAG_CANCEL_CURRENT);
                 AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
                 am.cancel(pend);
 
                 long timeComplete = skill.optLong("time_complete") * 1000;
 
                 // Use for debugging notifications only
                 // timeComplete = System.currentTimeMillis()
                 // + 2000;
 
                 Date d = new Date(timeComplete);
                Log.i("GlitchSkills", "Scheduled notification at " + d.toLocaleString());
 
                 am.set(AlarmManager.RTC_WAKEUP, timeComplete, pend);
             }
         }
 
         private void updateNotLearning(RemoteViews updateViews)
         {
             // Forget we were learning something
             currentSkill = null;
             
             JSONObject response = ContentHelper.getContent(getApplicationContext(), Constants.PLAYERS_INFO);
 
             if (response != null && response.optInt("ok") == 1)
             {
                 if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD)
                 {
                     updateViews.setViewVisibility(R.id.skill_progress, View.INVISIBLE);
                     updateViews.setViewVisibility(R.id.skill_time_to_learn, View.INVISIBLE);
                     updateViews.setViewVisibility(R.id.skill_time_to_learn_label, View.INVISIBLE);
                 }
 
                 updateViews.setTextViewText(R.id.skill_title, response.optString("player_name"));
                 updateViews.setTextViewText(R.id.skill_description, getString(R.string.widget_not_learning));
 
                 LoadIconAsyncTask iconTask = new LoadIconAsyncTask(getApplicationContext(), updateViews);
                 iconTask.execute(response.optString("avatar_url"));
             }
         }
 
         public void updateSkillText(final RemoteViews updateViews, JSONObject skill, boolean isUnlearning)
         {
             if (isUnlearning)
             {
                 updateViews.setTextViewText(R.id.skill_title, "Unlearning: " + skill.optString("name"));
             }
             else
             {
                 updateViews.setTextViewText(R.id.skill_title, skill.optString("name"));
             }
             updateViews.setTextViewText(R.id.skill_description, skill.optString("description"));
 
             int timeRemaining = skill.optInt("time_remaining");
             long timeStart = skill.optLong("time_start");
             long timeComplete = skill.optLong("time_complete");
 
             int elapsed;
             int total;
             int remaining;
 
             long currentTime = System.currentTimeMillis() / 1000L;
 
             if (timeStart != 0 && timeComplete != 0)
             {
                 elapsed = (int)(currentTime - timeStart);
                 total = (int)(timeComplete - timeStart);
                 remaining = total - elapsed;
             }
             else
             {
                 total = skill.optInt("total_time");
                 elapsed = total - timeRemaining;
                 remaining = timeRemaining;
                 
                 if (isUnlearning && timeRemaining != 0)
                 {
                     try
                     {
                     skill.put("time_start", currentTime);
                     skill.put("time_complete", currentTime + timeRemaining);
                     } 
                     catch (JSONException ex)
                     {
                         ex.printStackTrace();
                     }
                 }
                 else if (isUnlearning && timeRemaining == 0)
                 {
                     remaining = total;
                     elapsed = 0;
                 }
             }
 
             if (timeRemaining != 0 || isUnlearning)
             {
                 if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD)
                 {
                     updateViews.setViewVisibility(R.id.skill_progress, View.VISIBLE);
                     updateViews.setViewVisibility(R.id.skill_time_to_learn, View.VISIBLE);
                     updateViews.setViewVisibility(R.id.skill_time_to_learn_label, View.VISIBLE);
                 }
 
                 updateViews.setProgressBar(R.id.skill_progress, total, elapsed, false);
                 updateViews.setTextViewText(R.id.skill_time_to_learn, SkillsAdapter.formatDuration(remaining, false));
             }
             else
             {
                 if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD)
                 {
                     updateViews.setViewVisibility(R.id.skill_progress, View.INVISIBLE);
                     updateViews.setViewVisibility(R.id.skill_time_to_learn, View.INVISIBLE);
                     updateViews.setViewVisibility(R.id.skill_time_to_learn_label, View.INVISIBLE);
                 }
             }
         }
 
         public void updateIconAndUpdateAppWidget(Context context, RemoteViews updateViews, Bitmap iconBitmap)
         {
             if (iconBitmap != null)
                 updateViews.setBitmap(R.id.skill_icon, "setImageBitmap", iconBitmap);
 
             ComponentName thisWidget = new ComponentName(context, LearningWidget.class);
             AppWidgetManager manager = AppWidgetManager.getInstance(context);
             manager.updateAppWidget(thisWidget, updateViews);
         }
         
         private String getPlayerName()
         {
             JSONObject response = ContentHelper.getContent(getApplicationContext(), Constants.PLAYERS_INFO);
 
             if (response != null && response.optInt("ok") == 1)
             {
                 return response.optString("player_name");
             }
             
             return null;
         }
     }
 }

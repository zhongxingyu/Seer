 /*
  * This file is part of verfluchter-android.
  *
  * verfluchter-android is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * verfluchter-android is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package pl.xsolve.verfluchter.services;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;
 import pl.xsolve.verfluchter.R;
 import pl.xsolve.verfluchter.activities.VerfluchterActivity;
 import pl.xsolve.verfluchter.tools.AutoSettings;
 import pl.xsolve.verfluchter.tools.Constants;
 import pl.xsolve.verfluchter.tools.SoulTools;
 import pl.xsolve.verfluchter.tools.WorkStatus;
 
 import java.util.GregorianCalendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import static pl.xsolve.verfluchter.tools.SoulTools.isTrue;
 import static pl.xsolve.verfluchter.tools.SoulTools.workTimeIsOver;
 
 /**
  * @author Konrad Ktoso Malawski
  */
 public class WorkTimeNotifierService extends Service {
 
     // logger tag
     private static final String TAG = "WorkTimeNotifierService";
 
     // time checking interval
     private static final long INTERVAL = 10 * Constants.MINUTE;
 
     private Timer timer = new Timer();
     public static final String INTENT_HEY_STOP_WORKING = "HEY_STOP_WORKING";
 
     NotificationManager notificationManager;
 
     /**
      * Called on service creation, will start the timer
      */
     @Override
     public void onCreate() {
         super.onCreate();
 
         notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 
         start();
 
         Log.v(TAG, "Service started");
     }
 
     /**
      * Called on service destruction, will stop the timer
      */
     @Override
     public void onDestroy() {
         super.onDestroy();
         stop();
 
         Log.v(TAG, "Service stopped");
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     private void start() {
         timer.scheduleAtFixedRate(new TimerTask() {
             public void run() {
                 process();
             }
         }, Constants.MINUTE, INTERVAL);
     }
 
     private void process() {
         Log.v(TAG, "WorkTimeNotifierService is processing.");
 
         GregorianCalendar now = new GregorianCalendar();
 //        if (isWorking()) {

         if (SoulTools.itsWeekend(now)) {
             //todo remove this
             notifyUser(WorkStatus.YOU_CAN_STOP_WORKING);
             return;
         }
 
         if (workTimeIsOver(now)) {
             notifyUser(WorkStatus.YOU_CAN_STOP_WORKING);
         }
 //        }
     }
 
     private void notifyUser(WorkStatus workStatus) {
         Log.d(TAG, "Displaying notification for working status: " + workStatus);
         long when = System.currentTimeMillis();
         int icon = R.drawable.icon;
         CharSequence titleText;
 
         Context context = getApplicationContext();
         CharSequence contentTitle;
         CharSequence contentText;
 
         // setup strings etc
         switch (workStatus) {
             case YOU_CAN_STOP_WORKING:
                 titleText = getString(workStatus.contextTitle);
                 contentTitle = getString(workStatus.contextTitle);
                 contentText = getString(workStatus.contentText);
                 break;
             default:
                 return;
         }
 
         // build the notification/intent
         Notification notification = new Notification(icon, titleText, when);
         Intent notificationIntent = new Intent(this, VerfluchterActivity.class);
         PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
         notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
 
         // this Notification will be canceled after the user clicks it
         notification.defaults |= Notification.FLAG_AUTO_CANCEL;
 
 //        if(isTrue(autoSettings.getSetting(AutoSettings.USE_SOUND_B, Boolean.class))){
 //            notification.defaults |= Notification.DEFAULT_SOUND;
         // or even better: notification.sound = Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "6");
         // http://developer.android.com/guide/topics/ui/notifiers/notifications.html
 //        }
 
         // pass to the notification manager to display the notification
         notificationManager.notify(workStatus.ordinal(), notification);
     }
 
     private void stop() {
         notificationManager.cancelAll();
 
         if (timer != null) {
             timer.cancel();
         }
     }
 }

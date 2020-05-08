 /*
  * Copyright (C) 2011 The original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.zapta.apps.maniana.util;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 
 import com.zapta.apps.maniana.R;
 import com.zapta.apps.maniana.annotations.ApplicationScope;
 import com.zapta.apps.maniana.main.MainActivity;
 
 /**
  * Notifcation operations.
  * 
  * @author Tal Dayan
  */
 @ApplicationScope
 public class NotificationUtil {
     /** Arbitrary notification ID, unique within this app. */
     private static final int NOTIFICATION_ID = 1000;
 
     /**
      * Send pending items notification. Ok to call multiple times (they do not accumulate).
      * pendingItemsCount should be >= 1. Should be called only if notifications are enabled in app
      * settings.
      */
     public static void sendPendingItemsNotification(Context context, int pendingItemsCount) {
         LogUtil.debug("*** sending notification");
 
         final NotificationManager notificationManager = (NotificationManager) context
                 .getSystemService(Context.NOTIFICATION_SERVICE);
 
         // TODO: set this to midnight.
         final long when = System.currentTimeMillis();
 
        final String title = (pendingItemsCount == 1) ? context
                .getString(R.string.notification_title_single_task) : context.getString(
                R.string.notification_title_d_tasks, pendingItemsCount);
 
        // NOTE: we use title also as the temporary ticker string.
         final Notification notification = new Notification(R.drawable.app_notification_icon,
                title, when);
 
         notification.flags |= Notification.FLAG_AUTO_CANCEL;
         notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
         notification.number = pendingItemsCount;
                       
        final String content = context.getString(R.string.notification_content);
 
                         
         final Intent notificationIntent = new Intent(context, MainActivity.class);
         final PendingIntent pendingItent = PendingIntent.getActivity(context, 0,
                 notificationIntent, 0);
 
         notification.setLatestEventInfo(context, title, content, pendingItent);
 
         notificationManager.notify(NOTIFICATION_ID, notification);
     }
 
     /** Clear any pending notification. */
     public static void clearPendingItemsNotification(Context context) {
         final NotificationManager notificationManager = (NotificationManager) context
                 .getSystemService(Context.NOTIFICATION_SERVICE);
         notificationManager.cancel(NOTIFICATION_ID);
     }
 }

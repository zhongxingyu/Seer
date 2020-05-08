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
 
 package com.zapta.apps.maniana.services;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.text.format.Time;
 
 import com.zapta.apps.maniana.annotations.ApplicationScope;
 import com.zapta.apps.maniana.util.LogUtil;
 import com.zapta.apps.maniana.widget.BaseWidgetProvider;
 
 /**
  * Provides a trigger shortly after midnight. Use to trigger widgets update, 
  * notifications, etc.
  * <p>
  * TODO: have this receiver responding also to date or time zone change by user. Since they may
  * also affect the widgets.
  * 
  * @author Tal Dayan
  */
 @ApplicationScope
 public class MidnightTicker extends BroadcastReceiver {
 
     /** Should match AndroidManifest.xml. */
     private static final String MIDNIGHT_TRIGGER_ACTION = "com.zapta.apps.maniana.MIDNIGHT_TRIGGER_ACTION";
 
     /** Trigger slightly after midnight to avoid truncation and timing errors, etc. */
     private static final int MIDNIGHT_MARGIN_MILLIS = 60000;
 
     @Override
     public void onReceive(Context context, Intent intent) {
         LogUtil.info("MidnightTicker onRecieve: " + intent);
         
         // Payload1: update widgets
         BaseWidgetProvider.updateAllWidgetsFromContext(context);
     }
 
     /**
      * Schedule or reschedule midnight trigger.
      * 
      * Called from few hooks to make sure we still have a pending midnight alarm.
      */
     public static final void scheduleMidnightTicker(Context context) {
         //LogUtil.debug("Scheduling midnight ticker");
         Intent intent = new Intent(MIDNIGHT_TRIGGER_ACTION);
         PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
 
         final long startTimeUtcMillis = utcMillisNextMidnight() + MIDNIGHT_MARGIN_MILLIS;
 
         final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTimeUtcMillis, AlarmManager.INTERVAL_DAY,
                 pendingIntent);
     }
 
     /** Get time of next midnight in UTC millis. */
     private static final long utcMillisNextMidnight() {
         Time t = new Time();
         t.setToNow();
         t.monthDay++;
         t.hour = 0;
         t.minute = 0;
         t.second = 0;
         return t.normalize(true);
     }
 
 }

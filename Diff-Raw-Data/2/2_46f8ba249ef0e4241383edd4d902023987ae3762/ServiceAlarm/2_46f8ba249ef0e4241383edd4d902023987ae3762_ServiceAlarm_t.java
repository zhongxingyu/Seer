 /*
  * Wifi Fixer for Android
  *     Copyright (C) 2010-2013  David Van de Ven
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see http://www.gnu.org/licenses
  */
 
 package org.wahtod.wififixer.utility;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.os.SystemClock;
 import org.wahtod.wififixer.WFMonitorService;
 import org.wahtod.wififixer.prefs.PrefConstants.Pref;
 import org.wahtod.wififixer.prefs.PrefUtil;
 
 public final class ServiceAlarm extends Object {
 
     /*
      * Notifies Service that start intent comes from ServiceAlarm
      */
     public static final String ALARM_START = "ALARM_SERVICE_START";
 
     public static final String FIRST_RUN = "FIRST_RUN";
 
     public static final long PERIOD = 300000;
     public static final long STARTDELAY = 30000;
     private static final long NODELAY = 0;
 
     public static boolean alarmExists(Context context, Intent intent) {
         return (PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE) != null);
     }
 
     private static PendingIntent createPendingIntent(Context context,
                                                      int flag) {
         Intent intent = new Intent(context, WFMonitorService.class);
         intent.setFlags(Intent.FLAG_FROM_BACKGROUND);
         intent.putExtra(ALARM_START, ALARM_START);
         PendingIntent pendingintent = PendingIntent.getService(context, 0,
                 intent, flag);
         return pendingintent;
     }
 
     /*
      * Makes sure that if package is updated LogService and WFMonitorService
      * respect disabled state
      */
     public static void enforceServicePrefs(Context context) {
         if (PrefUtil.readBoolean(context, Pref.DISABLE_KEY.key()))
             setComponentEnabled(context, WFMonitorService.class, false);
         else
             setComponentEnabled(context, WFMonitorService.class, true);
 
         if (PrefUtil.readBoolean(context, Pref.LOG_KEY.key()))
             setComponentEnabled(context, LogService.class, true);
         else
             setComponentEnabled(context, LogService.class, false);
 
     }
 
     public static void setComponentEnabled(Context context,
                                            Class<?> cls, Boolean state) {
         PackageManager pm = context.getPackageManager();
         ComponentName service = new ComponentName(context, cls);
         if (state)
             pm.setComponentEnabledSetting(service,
                     PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                     PackageManager.DONT_KILL_APP);
         else {
             pm.setComponentEnabledSetting(service,
                     PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                     PackageManager.DONT_KILL_APP);
             context.stopService(new Intent(context, cls));
         }
     }
 
     public static void setServiceAlarm(Context c,
                                        boolean initialdelay) {
         addAlarm(c, initialdelay, true, PERIOD,
                 createPendingIntent(c, PendingIntent.FLAG_UPDATE_CURRENT));
     }
 
     public static void addAlarm(Context c, long delay,
                                 boolean repeating, long period, PendingIntent p) {
         registerAlarm(c, delay, repeating, period, p);
     }
 
     public static void addAlarm(Context c, boolean initialdelay,
                                 boolean repeating, long period, PendingIntent p) {
         if (initialdelay)
             registerAlarm(c, PERIOD, repeating, period, p);
         else
             registerAlarm(c, NODELAY, repeating, period, p);
     }
 
     private static void registerAlarm(Context c, long delay,
                                       boolean repeating, long period, PendingIntent p) {
         AlarmManager mgr = (AlarmManager) c
                 .getSystemService(Context.ALARM_SERVICE);
         if (repeating)
            mgr.setRepeating(AlarmManager.ELAPSED_REALTIME,
                     SystemClock.elapsedRealtime() + delay, period, p);
         else
             mgr.set(AlarmManager.ELAPSED_REALTIME,
                     SystemClock.elapsedRealtime() + delay, p);
     }
 
     public static void unsetAlarm(Context c) {
         AlarmManager mgr = (AlarmManager) c
                 .getSystemService(Context.ALARM_SERVICE);
         mgr.cancel(createPendingIntent(c, 0));
     }
 
     public static void unsetAlarm(Context c, PendingIntent p) {
         AlarmManager mgr = (AlarmManager) c
                 .getSystemService(Context.ALARM_SERVICE);
         mgr.cancel(p);
     }
 }

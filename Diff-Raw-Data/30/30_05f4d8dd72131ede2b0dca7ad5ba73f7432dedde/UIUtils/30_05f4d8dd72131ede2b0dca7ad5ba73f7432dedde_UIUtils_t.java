 package com.dgsd.android.ShiftTracker.Util;
 
 import android.content.Context;
 import android.view.ViewConfiguration;
 
 import java.lang.reflect.Field;
 
 public class UIUtils {
 
     public static void tryForceMenuOverflow(Context context) {
         try {
             ViewConfiguration config = ViewConfiguration.get(context);
             Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
             if(menuKeyField != null) {
                 menuKeyField.setAccessible(true);
                 menuKeyField.setBoolean(config, false);
             }
         } catch (Exception ex) {
             //Thats a shame .. o well..
         }
     }

    public static String getDurationAsHours(long minutes) {
        if(minutes < 60) {
            return ((minutes < 10) ? "0:0" : "0:") + minutes;
        }

        final int hours = (int) Math.floor(minutes / 60);

        final long mins = minutes % 60;
        return hours + (mins < 10 ? ":0" : ":")  + mins;
    }
 }

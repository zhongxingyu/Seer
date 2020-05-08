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
 }

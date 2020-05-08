 package com.roma3.infovideo.utility;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 /**
  * Version 1.2
  * Copyright (C) 2012 Enrico Candino ( enrico.candino@gmail.com )
  *
  * This file is part of "Roma Tre".
  * "Roma Tre" is released under the General Public Licence V.3 or later
  *
  * @author Enrico Candino
  */
 public class ThemeManager {
 
     public static void setTheme(Activity activity) {
         SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        String theme = pref.getString("theme", "DarkTheme");
         int customTheme = activity.getResources().getIdentifier(theme, "style", activity.getPackageName());
         activity.setTheme(customTheme);
     }
 }

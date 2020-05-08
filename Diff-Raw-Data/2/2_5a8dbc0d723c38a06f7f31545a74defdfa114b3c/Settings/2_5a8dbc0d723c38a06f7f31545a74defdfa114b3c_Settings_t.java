 /*
  * Copyright (C) 2012 Jérémy Compostella
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.oux.SmartGPSLogger;
 
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.content.res.Resources;
 import android.content.Context;
 
 public class Settings
 {
     private Resources res;
     private SharedPreferences pref;
 
     public int minFreq()
     {
         return Integer.valueOf(pref.getString("min_freq",
                                               res.getString(R.string.MinFreq)));
     }
 
     public int maxFreq()
     {
         return Integer.valueOf(pref.getString("max_freq",
                                               res.getString(R.string.MaxFreq)));
     }
 
     public float minDist()
     {
         return Float.valueOf(pref.getString("min_dist",
                                             res.getString(R.string.MinDist)));
     }
 
     public int gpsTimeout()
     {
         return Integer.valueOf(pref.getString("gps_timeout",
                                               res.getString(R.string.GpsTimeout)));
     }
 
     public boolean onBoot()
     {
         return pref.getBoolean("onboot", true);
     }
 
     public static Settings getInstance()
     {
         return instance;
     }
 
     private Settings(Context context)
     {
         res = context.getResources();
         pref = PreferenceManager.getDefaultSharedPreferences(context);
     }
 
     public static Settings getInstance(Context context)
     {
         if (instance != null)
             return instance;
        return instance = new Settings(context);
     }
 
     private static Settings instance = null;
 }

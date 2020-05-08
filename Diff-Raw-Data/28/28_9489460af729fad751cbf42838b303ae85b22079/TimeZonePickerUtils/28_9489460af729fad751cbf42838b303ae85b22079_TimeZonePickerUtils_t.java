 /*
  * Copyright (C) 2013 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.timezonepicker;
 
import android.content.Context;
import android.os.Build;
 import android.text.format.DateUtils;
 import android.text.format.Time;
 
 import java.util.Locale;
 import java.util.TimeZone;
 
 public class TimeZonePickerUtils {
 
    private static String mDstSymbol;

     /**
      * Given a timezone id (e.g. America/Los_Angeles), returns the corresponding timezone
      * display name (e.g. (GMT-7.00) Pacific Time).
      *
      * @param id The timezone id
      * @param millis The time (daylight savings or not)
      * @return The display name of the timezone.
      */
     public static String getGmtDisplayName(String id, long millis) {
         TimeZone timezone = TimeZone.getTimeZone(id);
         if (timezone == null) {
             return null;
         }
         return buildGmtDisplayName(timezone, millis);
     }
 
     private static String buildGmtDisplayName(TimeZone tz, long timeMillis) {
         Time time = new Time(tz.getID());
         time.set(timeMillis);
 
         StringBuilder sb = new StringBuilder();
         sb.append("(GMT");
 
         final int gmtOffset = tz.getOffset(timeMillis);
         if (gmtOffset < 0) {
             sb.append('-');
         } else {
             sb.append('+');
         }
 
         final int p = Math.abs(gmtOffset);
         sb.append(p / DateUtils.HOUR_IN_MILLIS); // Hour
 
         final int min = (p / (int) DateUtils.MINUTE_IN_MILLIS) % 60;
         if (min != 0) { // Show minutes if non-zero
             sb.append(':');
             if (min < 10) {
                 sb.append('0');
             }
             sb.append(min);
         }
         sb.append(") ");
 
         // tz.inDaylightTime(new Date(timeMillis))
         String displayName = tz.getDisplayName(time.isDst != 0, TimeZone.LONG,
                 Locale.getDefault());
         sb.append(displayName);
 
         if (tz.useDaylightTime()) {
            String dstSymbol = getDstSymbol();
            sb.append(" ");
            sb.append(dstSymbol); // Sun symbol
         }
         return sb.toString();
     }
 
    public static String getDstSymbol() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return "\u2600"; // The Sun emoji icon.
        } else {
            return "*";
        }
    }

 }

 package org.muckebox.android.ui.utils;
 
 import android.annotation.SuppressLint;
 
 import java.util.concurrent.TimeUnit;
 
 @SuppressLint("DefaultLocale")
 public class TimeFormatter {
     public static String formatDuration(int duration) {
         long hours = TimeUnit.SECONDS.toHours(duration);
        long minutes = TimeUnit.SECONDS.toMinutes(duration - hours * 60 * 60);
         long seconds = duration - ((hours * 60) + minutes) * 60;
         
         if (hours > 0)
             return String.format("%d:%02d:%02d", hours, minutes, seconds);
         else
             return String.format("%d:%02d", minutes, seconds);
     }
 }

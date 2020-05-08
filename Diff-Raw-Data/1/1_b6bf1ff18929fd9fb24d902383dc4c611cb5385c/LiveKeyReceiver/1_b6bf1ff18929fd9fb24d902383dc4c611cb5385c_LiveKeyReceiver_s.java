 package com.ogp.clock;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 
 import java.util.Calendar;
 
 public class LiveKeyReceiver extends BroadcastReceiver {
 
     @Override
     public void onReceive(Context context, Intent intent) {
         int state = intent.getIntExtra(LiveKeyConstants.EXTRA_STATE, -1);
         if (state != 0) { // If not pressed
             return;
         }
 
         Intent say = new Intent(context, Say.class);
         say.putExtra(Say.SAY_TEXT, getTime(context));
 
         context.startService(say);
     }
 
     private String getTime(Context context) {
         Calendar now = Calendar.getInstance();
 
         int hour = now.get(Calendar.HOUR);
         int minute = now.get(Calendar.MINUTE);
 
         minute = (minute + 2) / 5 * 5;
         if (minute >= 60) {
             minute = 0;
         }
 
         Resources res = context.getResources();
 
         String text;
         if (minute == 0) {
             text = String.format(res.getString(R.string.time_hour), minute, hour);
         } else if (minute < 30) {
             text = String.format(res.getString(R.string.time_past), minute, hour);
             } else if (minute > 30) {
             text = String.format(res.getString(R.string.time_to), 60 - minute, hour + 1);
         } else {
             text = String.format(res.getString(R.string.time_half), minute, hour);
         }
 
         return String.format(text, minute, hour);
     }
 }

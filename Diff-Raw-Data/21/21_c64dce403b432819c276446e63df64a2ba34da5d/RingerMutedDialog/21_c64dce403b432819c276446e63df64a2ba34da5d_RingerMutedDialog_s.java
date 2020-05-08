 /**
  * Copyright (C) 2009 Jesse Wilson
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.publicobject.shush;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.Dialog;
 import android.app.PendingIntent;
 import android.content.*;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.text.format.DateUtils;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 
 import java.text.DateFormat;
 
 import static android.app.AlarmManager.ELAPSED_REALTIME;
 import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
 import static android.media.AudioManager.EXTRA_RINGER_MODE;
 import static android.media.AudioManager.RINGER_MODE_NORMAL;
 import static android.view.Gravity.BOTTOM;
 
 /**
  * A dialog to schedule the ringer back on after a specified duration.
  */
 public class RingerMutedDialog extends Activity implements DialogInterface.OnCancelListener {
 
     private Dialog dialog;
 
     /**
      * If the user turns the ringer back on, dismiss the dialog and exit.
      */
     private final BroadcastReceiver dismissFromVolumeUp = new BroadcastReceiver() {
         public void onReceive(Context context, Intent intent) {
             int newRingerMode = intent.getIntExtra(EXTRA_RINGER_MODE, -1);
             if (RINGER_MODE_NORMAL == newRingerMode) {
                 dialog.dismiss();
                 finish();
             }
         }
     };
 
    @Override
    protected void onCreate(Bundle bundle) {
         super.onCreate(bundle);
 
         final ArrayAdapter<Duration> durationsView = new ArrayAdapter<Duration>(this, 0) {
             @Override public View getView(int position, View convertView, ViewGroup viewGroup) {
                 TextView result;
                 if (convertView == null) {
                     result = new TextView(getApplicationContext());
                     result.setTextSize(20);
                     result.setPadding(15, 5, 15, 5);
                 } else {
                     result = (TextView) convertView;
                 }
                 result.setText(getItem(position).toString());
                 return result;
             }
         };
         for (Duration duration : Duration.values()) {
             durationsView.add(duration);
         }
 
         ListView durationsList = new ListView(this);
         durationsList.setAdapter(durationsView);
         durationsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                 Duration duration = durationsView.getItem(index);
                 if (duration == Duration.NEVER) {
                     cancelRinger();
                 } else {
                     scheduleRingerOn(duration);
                 }
                 dialog.dismiss();
                 finish();
             }
         });
 
         dialog = new Dialog(this, android.R.style.Theme_Dialog);
         dialog.setCanceledOnTouchOutside(true);
         dialog.setOnCancelListener(this);
         dialog.setTitle("Turn ringer back on?");
         dialog.setContentView(durationsList);
         dialog.setCancelable(true);
         dialog.getWindow().setGravity(BOTTOM);
     }
 
     /**
      * When the dialog is cancelled, cancel the ringer and exit.
      */
     public void onCancel(DialogInterface dialogInterface) {
         cancelRinger();
         finish();
     }
 
     /**
      * Cancels the scheduled TurnRingerOn action.
      */
     private void cancelRinger() {
         AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
         alarmManager.cancel(createIntent());
 
         Toast.makeText(getApplicationContext(), "Ringer shushed indefinitely!", Toast.LENGTH_LONG).show();
     }
 
     private PendingIntent createIntent() {
         Context context = getApplicationContext();
         return PendingIntent.getBroadcast(context, 0, new Intent(context, TurnRingerOn.class), FLAG_CANCEL_CURRENT);
     }
 
     private void scheduleRingerOn(Duration duration) {
         AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
         alarmManager.set(ELAPSED_REALTIME, SystemClock.elapsedRealtime() + duration.millis(), createIntent());
 
         long now = System.currentTimeMillis();
         CharSequence message = "Ringer shushed 'til " + DateUtils.formatSameDayTime(
                 now + duration.millis(), now, DateFormat.SHORT, DateFormat.SHORT);
         Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
     }
 
     @Override protected void onStart() {
         super.onStart();
         dialog.show();
         registerReceiver(dismissFromVolumeUp, new IntentFilter("android.media.RINGER_MODE_CHANGED"));
     }
 
     @Override protected void onStop() {
         unregisterReceiver(dismissFromVolumeUp);
         super.onStop();
     }
 }

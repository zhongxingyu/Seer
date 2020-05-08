 package com.github.browep.wegu;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import com.github.browep.wegu.util.AlarmAlertWakeLock;
 import com.github.browep.wegu.util.Log;
 
 
 /**
  * Created by IntelliJ IDEA.
  * User: paul
  * Date: 1/22/11
  * Time: 4:23 PM
  * To change this template use File | Settings | File Templates.
  */
 public class AlarmReceiver extends BroadcastReceiver {
       // Display an alert that we've received a message.
     @Override
     public void onReceive(Context context, Intent intent){

        Dao dao = new Dao(context.getSharedPreferences(Constants.PREFS_FILE_NAME,0));

        dao.setBooleanPreference(Constants.DO_ALARM,true);

         Log.i("AlarmReceiver received an alarm");
         AlarmAlertWakeLock.acquireCpuWakeLock(context);
 
         Intent playAlarm = new Intent(context,MainAlarm.class);
         playAlarm.putExtra(Constants.FROM_ALARM,true);
         playAlarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         playAlarm.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 
         context.startActivity(playAlarm);
 
    }
 
 
 
 }

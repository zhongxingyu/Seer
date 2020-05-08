 package com.varunarl.invisibletouch.internal;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import com.varunarl.invisibletouch.view.MainMenuActivity;
 
 /**
  * Created by vlekamwasam on 6/10/13.
  */
 public class SignalReceiver extends BroadcastReceiver {
 
     public static final String BOOT_COMPLETED = Intent.ACTION_BOOT_COMPLETED;
    public static final String HOME_SCREEN_LAUNCHED = "com.varunarl.invisibletouch.HOME_LAUNCHED";
 
     public void onReceive(Context context, Intent intent) {
         String action = intent.getAction();

        if (action.equals(BOOT_COMPLETED) || action.equals(HOME_SCREEN_LAUNCHED)) {
             Intent i = new Intent(context, MainMenuActivity.class);
             i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            context.startActivity(i);
         }
     }
 }

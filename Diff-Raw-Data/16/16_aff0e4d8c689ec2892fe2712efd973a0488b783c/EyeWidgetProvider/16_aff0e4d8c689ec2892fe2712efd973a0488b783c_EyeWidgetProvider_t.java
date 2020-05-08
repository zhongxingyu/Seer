 package com.studiomobile;
 
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.*;
 import android.os.IBinder;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 
 public class EyeWidgetProvider extends AppWidgetProvider {
 
     @Override
     public void onDisabled(Context context) {
         context.stopService(new Intent(context, UpdateService.class));
        super.onDisabled(context);
     }
 
     @Override
     public void onEnabled(Context context) {
         super.onEnabled(context);
         context.startService(new Intent(UpdateService.ACTION_UPDATE));
     }
 
     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                          int[] appWidgetIds) {
         super.onUpdate(context, appWidgetManager, appWidgetIds);
         context.startService(new Intent(UpdateService.ACTION_UPDATE));
     }
 
     /**
      * Setups a BroadcastReceiver and listens
      * for the time change events, then update the views accordingly.
      */
     public static class UpdateService extends Service {
 
         static final String ACTION_UPDATE = "android.appwidget.action.UPDATE";
         private final static IntentFilter intentFilter;
         static {
             intentFilter = new IntentFilter();
             intentFilter.addAction(Intent.ACTION_TIME_TICK);
             intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
         }
 
 
         public final BroadcastReceiver timeChangedReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 final String action = intent.getAction();
                 if(action.equals(Intent.ACTION_TIME_TICK)){
                     update();
                 }
                 if(action.equals("android.provider.Telephony.SMS_RECEIVED")){
                     reactOnSms(context);
                 }
             }
         };
 
         @Override
         public void onCreate() {
             super.onCreate();
             registerReceiver(timeChangedReceiver, intentFilter);
         }
 
         @Override
         public void onDestroy() {
             super.onDestroy();
             unregisterReceiver(timeChangedReceiver);
         }
 
         @Override
         public void onStart(Intent intent, int startId) {
             super.onStart(intent, startId);
             if (ACTION_UPDATE.equals(intent.getAction())) {
                 update();
             }
         }
 
         @Override
         public IBinder onBind(Intent intent) {
             return null;
         }
 
         /**
          * Updates and redraws the Widget.
          */
         private void update() {
             DateFormat format = new SimpleDateFormat("hh:mm");
             RemoteViews views = new RemoteViews(getPackageName(), R.layout.main);
             views.setTextViewText(R.id.widget_textview, format.format(new Date()));
 
             ComponentName widget = new ComponentName(this, EyeWidgetProvider.class);
             AppWidgetManager manager = AppWidgetManager.getInstance(this);
             manager.updateAppWidget(widget, views);
         }

         private void reactOnSms(Context context){
             Toast.makeText(context, "new sms", Toast.LENGTH_SHORT).show();
         }
     }
 
 }

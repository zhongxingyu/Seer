 package com.jdevelop.mobile.Mobile3GSwitch;
 
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 
 /**
  * User: Eugene Dzhurinsky
  * Date: 3/16/13
  */
 public class Switch3GWidgetProvider extends AppWidgetProvider {
 
     private static final String LOG = "Switch3G";
 
     private static final String ACTION_WIDGET_NOTIF = "com.jdevelop.mobile.Mobile3GSwitch.Switch3GWidgetProvider.UPDATE_ICON";
 
     public static RemoteViews rview;
 
     private static final void d(String msg) {
         Log.d(LOG, msg);
     }
 
     private static final void e(String msg, Throwable exc) {
         Log.e(LOG, msg, exc);
     }
 
     @Override
     public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
         d("Registering");
         updateWidgetState(context, "");
     }
 
     @Override
     public void onReceive(Context context, Intent intent) {
         d("Processing event");
         String str = intent.getAction();
         boolean isEnabled = getMobileDataEnabled(context);
         if (str.equals(ACTION_WIDGET_NOTIF)) {
             setMobileDataEnabled(context, ! isEnabled);
             updateWidgetState(context, str);
         } else {
             d("Event " + intent.getAction());
             super.onReceive(context, intent);
         }
     }
 
     private static final class Pair {
 
         public final Object src;
 
         public final Method m;
 
         private Pair(Object src, Method m) {
             this.src = src;
             this.m = m;
         }
     }
 
     private static Pair getConnectionMethod(Context context, String methodName, Class<?>... types) throws Exception {
         final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
         final Class conmanClass = Class.forName(conman.getClass().getName());
         final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
         iConnectivityManagerField.setAccessible(true);
         final Object iConnectivityManager = iConnectivityManagerField.get(conman);
         final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
         final Method method = iConnectivityManagerClass.getDeclaredMethod(methodName, types);
         method.setAccessible(true);
         return new Pair(iConnectivityManager, method);
     }
 
     private static void setMobileDataEnabled(Context context, boolean enabled) {
         try {
             Pair p = getConnectionMethod(context, "setMobileDataEnabled", Boolean.TYPE);
             p.m.invoke(p.src, enabled);
         } catch (Exception e) {
             e("Can not set state", e);
         }
     }
 
     private static boolean getMobileDataEnabled(Context context) {
         try {
             Pair p = getConnectionMethod(context, "getMobileDataEnabled");
             return (Boolean) p.m.invoke(p.src);
         } catch (Exception e) {
             e("Can not set state", e);
         }
         return false;
     }
 
     static void updateWidgetState(Context paramContext, String paramString) {
         d("Update widget state");
         RemoteViews localRemoteViews = buildUpdate(paramContext, paramString); //CALL HERE
         ComponentName localComponentName = new ComponentName(paramContext, Switch3GWidgetProvider.class);
         AppWidgetManager.getInstance(paramContext).updateAppWidget(localComponentName, localRemoteViews);
     }
 
     private static RemoteViews buildUpdate(Context paramContext, String paramString) {
         rview = new RemoteViews(paramContext.getPackageName(), R.layout.main);
         Intent active = new Intent(paramContext, Switch3GWidgetProvider.class);
         active.setAction(ACTION_WIDGET_NOTIF);
         PendingIntent actionPendingIntent = PendingIntent.getBroadcast(paramContext, 0, active, 0);
         rview.setOnClickPendingIntent(R.id.imageButton, actionPendingIntent);
         d("Parameter string: " + paramString);
         boolean isEnabled = getMobileDataEnabled(paramContext);
         if (paramString.equals(ACTION_WIDGET_NOTIF)) {
             if (!isEnabled) {
                 rview.setImageViewResource(R.id.imageButton, R.drawable.off);
             } else {
                 rview.setImageViewResource(R.id.imageButton, R.drawable.on);
             }
         } else {
            if (isEnabled) {
                 d("Setting ON icon by default");
                 rview.setImageViewResource(R.id.imageButton, R.drawable.on);
             } else {
                 d("Mobile network is disabled");
             }
         }
         return rview;
     }
 
 }

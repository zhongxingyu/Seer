 package com.scurab.android.gcm;
 
 import android.Manifest;
 import android.R;
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.graphics.BitmapFactory;
 import android.support.v4.app.NotificationCompat;
 import com.scurab.android.RespondService;
 import com.scurab.gwt.rlw.shared.model.PushMessage;
 
 class NotificationHelper {
 
     private static final String CLICK = "Click";
     private static final String DELETE = "Delete";
     public static final int ICON_RES_ID = R.drawable.stat_sys_warning;
 
     /**
      * For vibration is {@link Manifest.permission#VIBRATE} necessary in
      * manifest!
      *
      * @param context
     * @param iconResId
      * @param subj
      * @param msg
      * @return
      */
     public static Notification createSimpleNotification(Context context,
                                                         PushMessage pm, String subj, String msg) {
         NotificationCompat.Builder b = new NotificationCompat.Builder(context);
 
         int defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
         if (context.checkCallingOrSelfPermission(Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
             defaults |= Notification.DEFAULT_VIBRATE;
         }
 
         b.setContentTitle(subj)
                 .setAutoCancel(true)
                 .setContentText(msg)
                 .setSmallIcon(ICON_RES_ID)
                 .setLargeIcon(
                         BitmapFactory.decodeResource(context.getResources(),
                                 ICON_RES_ID))
                 .setDefaults(defaults)
                 .setDeleteIntent(createIntent(context, pm, DELETE))
                 .setContentIntent(createIntent(context, pm, CLICK));
 
         return b.build();
     }
 
     public static Notification createQuestionNotification(Context context,
                                                           PushMessage pm, String subj, String msg, String... actions) {
         NotificationCompat.Builder b = new NotificationCompat.Builder(context);
 
         int defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
         if (context.checkCallingOrSelfPermission(Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
             defaults |= Notification.DEFAULT_VIBRATE;
         }
 
         b.setContentTitle(subj)
                 .setAutoCancel(true)
                 .setContentText(msg)
                 .setSmallIcon(ICON_RES_ID)
                 .setLargeIcon(
                         BitmapFactory.decodeResource(context.getResources(),
                                 ICON_RES_ID))
                 .setDefaults(defaults)
                 .setDeleteIntent(createIntent(context, pm, DELETE))
                 .setContentIntent(createIntent(context, pm, CLICK));
 
         for (String action : actions) {
             b.addAction(0, action, createIntent(context, pm, action));
         }
         return b.build();
     }
 
     private static PendingIntent createIntent(Context c, PushMessage pushMessage, String action) {
         if (pushMessage == null) {
             throw new IllegalArgumentException("pushMessage is null!");
         }
         if (action == null) {
             throw new IllegalArgumentException("action is null!");
         }
         Intent i = new Intent(c, RespondService.class);
         i.setAction(action);
         i.putExtra(PushMessage.class.getSimpleName(), pushMessage);
         return PendingIntent.getService(c, (int) System.currentTimeMillis(), i, PendingIntent.FLAG_UPDATE_CURRENT);
     }
 }

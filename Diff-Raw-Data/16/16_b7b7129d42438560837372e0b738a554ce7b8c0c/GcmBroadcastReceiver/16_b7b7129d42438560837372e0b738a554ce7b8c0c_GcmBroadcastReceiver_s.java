 package com.kosbrother.lyric;
 
 import com.google.android.gms.gcm.GoogleCloudMessaging;
 
 import android.app.Activity;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.app.NotificationCompat;
 
 public class GcmBroadcastReceiver extends BroadcastReceiver{
 	
 	static final String TAG = "GCMDemo";
     public static final int NOTIFICATION_ID = 1;
     private NotificationManager mNotificationManager;
     NotificationCompat.Builder builder;
     Context ctx;
     
     @Override
     public void onReceive(Context context, Intent intent) {
         GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
         ctx = context;
         String messageType = gcm.getMessageType(intent);
         if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
 //            sendNotification("Send error: " + intent.getExtras().toString());
         } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
 //            sendNotification("Deleted messages on server: " +
 //                    intent.getExtras().toString());
         } else {
         	try{
         		sendNotification(intent);
         	}catch(Exception e){
         		
         	}
         }
         setResultCode(Activity.RESULT_OK);
     }
     
     int openActivity = 0; // 0: MainActivity, 1: MyNovelActivity, 2: BookmarkActivity , 3: CategoryActivity, 4: NovelIntroduceActivity
     PendingIntent contentIntent;
 
     // Put the GCM message into a notification and post it.
     private void sendNotification(Intent intent) {
         mNotificationManager = (NotificationManager)
                 ctx.getSystemService(Context.NOTIFICATION_SERVICE);
         
         openActivity = Integer.parseInt(intent.getStringExtra("activity"));
         
         switch(openActivity){
         	case 0:
        		contentIntent = PendingIntent.getActivity(ctx, 0, new Intent(ctx, NewAlbumActivity.class), 0);
         		break;
         	case 1:
        		contentIntent = PendingIntent.getActivity(ctx, 0, new Intent(ctx, HotAlbumActivity.class), 0);
         		break;
         	case 2:
        		contentIntent = PendingIntent.getActivity(ctx, 0, new Intent(ctx, TopListActivity.class), 0);
         		break;
         	case 3:
        		contentIntent = PendingIntent.getActivity(ctx, 0, new Intent(ctx, RecommendSongActivity.class), 0);
         		break;
         	case 4:
         		Intent activity_intent2 = new Intent(ctx, TopListSongsActivity.class);
         		activity_intent2.putExtra("TopListName",intent.getStringExtra("top_list_name"));
         		activity_intent2.putExtra("TopListId",Integer.parseInt(intent.getStringExtra("top_list_id")));
        		contentIntent = PendingIntent.getActivity(ctx, 0, activity_intent2, 0);
         		break;
         	
         }
         
 
         NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx)
         .setSmallIcon(R.drawable.app_icon_72)
         .setContentTitle(intent.getStringExtra("title"))
         .setStyle(new NotificationCompat.BigTextStyle()
         .bigText(intent.getStringExtra("big_text")))
         .setContentText(intent.getStringExtra("content"))
         .setAutoCancel(true);
 
         mBuilder.setContentIntent(contentIntent);
         mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
     }
 }

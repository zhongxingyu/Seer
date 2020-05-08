 package com.novel.reader;
 
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
        		contentIntent = PendingIntent.getActivity(ctx, 0, new Intent(ctx, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
         		break;
         	case 1:
        		contentIntent = PendingIntent.getActivity(ctx, 0, new Intent(ctx, MyNovelActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
         		break;
         	case 2:
         		Intent activity_intent = new Intent(ctx, BookmarkActivity.class);
         		if(intent.getStringExtra("is_resent").equals("true"))
         		    activity_intent.putExtra("IS_RECNET", true);
         		else
         			activity_intent.putExtra("IS_RECNET", false);
        		contentIntent = PendingIntent.getActivity(ctx, 0, activity_intent, PendingIntent.FLAG_UPDATE_CURRENT);
         		break;
         	case 3:
         		Intent activity_intent1 = new Intent(ctx, CategoryActivity.class);
         		activity_intent1.putExtra("CategoryName", intent.getStringExtra("category_name"));
         		activity_intent1.putExtra("CategoryId", Integer.parseInt(intent.getStringExtra("category_id")));
        		contentIntent = PendingIntent.getActivity(ctx, 0, activity_intent1, PendingIntent.FLAG_UPDATE_CURRENT);
         		break;
         	case 4:
         		Intent activity_intent2 = new Intent(ctx, NovelIntroduceActivity.class);
         		activity_intent2.putExtra("NovelName",intent.getStringExtra("novel_name"));
         		activity_intent2.putExtra("NovelAuthor",intent.getStringExtra("novel_author"));
         		activity_intent2.putExtra("NovelDescription",intent.getStringExtra("novel_description"));
         		activity_intent2.putExtra("NovelUpdate",intent.getStringExtra("novel_update"));
         		activity_intent2.putExtra("NovelPicUrl",intent.getStringExtra("novel_pic_url"));
         		activity_intent2.putExtra("NovelArticleNum",intent.getStringExtra("novel_article_num"));
         		activity_intent2.putExtra("NovelId",Integer.parseInt(intent.getStringExtra("novel_id")));
        		contentIntent = PendingIntent.getActivity(ctx, 0, activity_intent2, PendingIntent.FLAG_UPDATE_CURRENT);
         		break;
         	
         }
         
 
         NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx)
         .setSmallIcon(R.drawable.noti_app_icon)
         .setContentTitle(intent.getStringExtra("title"))
         .setStyle(new NotificationCompat.BigTextStyle()
         .bigText(intent.getStringExtra("big_text")))
         .setContentText(intent.getStringExtra("content"))
        .setTicker(intent.getStringExtra("content"))
         .setAutoCancel(true);
 
         mBuilder.setContentIntent(contentIntent);
         mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
     }
 }

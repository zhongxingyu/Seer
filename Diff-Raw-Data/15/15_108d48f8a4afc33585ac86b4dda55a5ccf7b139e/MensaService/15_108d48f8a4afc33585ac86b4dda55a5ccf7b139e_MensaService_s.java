 package com.ese2013.mensaunibe.notificationservice;
 
 
 import java.util.ArrayList;
 
 import android.annotation.SuppressLint;
 import android.app.IntentService;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 
 import com.ese2013.mensaunibe.R;
 import com.ese2013.mensaunibe.model.data.PreferenceRequest;
 import com.ese2013.mensaunibe.notification.NotificationHolder;
 import com.ese2013.mensaunibe.notification.NotificationResultActivity;
 import com.ese2013.mensaunibe.notification.WordNotificationUtil;
 
 public class MensaService extends IntentService {
 
 	public MensaService() {
 		super("Mensa@Unibe");
 	}
 	
  @Override
 	public void onStart(Intent intent, int startId) {
 	      super.onStart(intent, startId);
 	   }
 
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		// TODO Auto-generated method stub
 		
 		//here we can add a server Query. 
 		PreferenceRequest pr = new PreferenceRequest();
		WordNotificationUtil wu = new WordNotificationUtil();
		ArrayList<NotificationHolder> result = wu.compareToKeywords( pr.readNotificationKeywords() );
		if(result.size()>0) this.sendNotification(this, result);
 	}
 	
 	@SuppressLint("NewApi")
 	private void sendNotification(Context context, ArrayList<NotificationHolder> keywordResultList) {
 		Intent notificationIntent = new Intent(context, NotificationResultActivity.class);
 		notificationIntent.putParcelableArrayListExtra("keywordResultList", keywordResultList);
 	      
 		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
 		stackBuilder.addParentStack(NotificationResultActivity.class);
 		stackBuilder.addNextIntent(notificationIntent);
 		
 		PendingIntent contentIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
 
 		NotificationManager notificationMgr =
 	               (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
 	            	        .setContentTitle(getString(R.string.notification_header))
 	            	        .setContentText(getString(R.string.notification_ingredients)+getMatchedKeywords(keywordResultList))
 	            	        .setSmallIcon(R.drawable.ic_launcher)
 	            	        .setContentIntent(contentIntent)
 	            	        .setAutoCancel(true);
 	      				
 	            
 		notificationMgr.notify(0, notification.build());
 	}
 	 //returns a String with all keywords
 	 private String getMatchedKeywords(ArrayList<NotificationHolder> keywordResultList){
 		String matchedKeywords = "";
 		 for(int i=0; i <keywordResultList.size();i++){
 			 if(matchedKeywords.contains(keywordResultList.get(i).getKeyword())==false)
 			matchedKeywords += keywordResultList.get(i).getKeyword();
 			 if(keywordResultList.size()>1 && i<keywordResultList.size())matchedKeywords += ", ";
 		}
 		return matchedKeywords;
 	 }
 }

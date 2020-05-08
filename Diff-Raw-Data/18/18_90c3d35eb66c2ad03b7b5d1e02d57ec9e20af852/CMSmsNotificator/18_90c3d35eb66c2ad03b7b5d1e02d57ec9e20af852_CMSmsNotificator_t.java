 package com.softmo.smssafe.main.notificator;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
import android.util.Log;
 
 import com.softmo.smssafe.R;
 import com.softmo.smssafe.SmsMainActivity;
 import com.softmo.smssafe.main.TTypEvent;
 import com.softmo.smssafe.main.TTypEventStrings;
 
 public class CMSmsNotificator implements IMSmsNotificator {
 	
 	private Context mContext;
 	private NotificationManager mNotifyMgr;
 	private Notification mNotification;
 	private Intent mNotificationIntent;
 	private PendingIntent mNotificationPendingIntent;
 	private CharSequence mTickerText;
 	
 	public void Init(Context context) {
 		mContext = context;
 		
 		mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 	
         mNotificationIntent = new Intent(context, SmsMainActivity.class);
         mNotificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         mNotificationIntent.putExtra(TTypEventStrings.EVENT_TYP, TTypEvent.ESmsRecieved.getValue());
         
         mNotificationPendingIntent = PendingIntent.getActivity(context, 0, mNotificationIntent, 0);
         
         mTickerText =   mContext.getResources().getText( R.string.sms_new );
 	}
 	
 	public void Popup(int cnt_newsms) {
 	    int icon = R.drawable.notificator;
 		long when = System.currentTimeMillis();
 		mNotification = new Notification(icon, mTickerText, when);
 	    mNotification.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
 	    
 	    CharSequence contentText=mContext.getResources().getText( R.string.sms_new_cnt )+Integer.toString(cnt_newsms);
         mNotification.setLatestEventInfo(mContext, mTickerText, contentText, mNotificationPendingIntent);
         mNotifyMgr.cancel(TTypEvent.ESmsRecieved.getValue());
 		mNotifyMgr.notify(TTypEvent.ESmsRecieved.getValue(), mNotification);
		
		Log.d("!!!", "NOTIF Popup: "+contentText);
 	}
 
 	public void Update(int cnt_newsms) {
 		if (cnt_newsms==0) {
 			mNotifyMgr.cancel(TTypEvent.ESmsRecieved.getValue());
 			mNotification=null;
			Log.d("!!!", "NOTIF cancel");
 		} else if (mNotification != null) {
 			CharSequence contentText=mContext.getResources().getText( R.string.sms_new_cnt )+Integer.toString(cnt_newsms);
 	        mNotification.setLatestEventInfo(mContext, mTickerText, contentText, mNotificationPendingIntent);
	        mNotification.defaults &= ~Notification.DEFAULT_SOUND;
	        mNotification.defaults &= ~Notification.DEFAULT_VIBRATE;
	        mNotifyMgr.notify(TTypEvent.ESmsRecieved.getValue(), mNotification);
	        
	        Log.d("!!!", "NOTIF update: "+contentText);
 		} else {
 			Popup(cnt_newsms);
 		}
 	}
 
 }

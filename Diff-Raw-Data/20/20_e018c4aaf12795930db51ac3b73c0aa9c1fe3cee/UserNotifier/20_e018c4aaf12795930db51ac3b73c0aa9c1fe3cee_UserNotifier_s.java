 package com.linkingenius.voodoo.observers;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.linkingenius.voodoo.MainActivity;
 import com.linkingenius.voodoo.R;
 import com.linkingenius.voodoo.utils.CallInfo;
 
 public enum UserNotifier implements CallObserver {
 
 	INSTANCE;
 		
 	private static final String TAG = "UserHelper";
 	
 	// Notifications
 	private static final int CALL_SCANNER_SVC_NOTIFICATION = 0;
 	
 	public enum CallScannerNotification { 
 		INIT {
 			Notification buildNotification(Context context) {
 				int icon = R.drawable.skullnbones;
 				CharSequence tickerText = context.getResources().getString(R.string.starting_ics_service);
 				long when = System.currentTimeMillis();
 
 				Notification notification = new Notification(icon, tickerText, when);
 				notification.flags |= Notification.DEFAULT_VIBRATE | 
 						Notification.FLAG_NO_CLEAR;
 				long[] vibrate = {0,100,200,300};
 				notification.vibrate = vibrate;
 				return notification;
 			}
 		}, 
 		INCOMING_CALL {
 			Notification buildNotification(Context context) {
 				int icon = R.drawable.redskullnbones;
 				CharSequence tickerText = context.getResources().getString(R.string.incoming_call_received);
 				long when = System.currentTimeMillis();
 
 				Notification notification = new Notification(icon, tickerText, when);
 				notification.flags |= Notification.FLAG_NO_CLEAR;
 				return notification;
 			}
 		};
 		
 		abstract Notification buildNotification(Context context);
 	}
 	
 	public void showCallScannerNotification(Context context, CallScannerNotification notificationType) {
 		NotificationManager mNotificationManager = 
 				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 
 		Notification notification = notificationType.buildNotification(context);
 
 		CharSequence contentTitle = context.getResources().getString(R.string.service_name);
 		CharSequence contentText = context.getResources().getString(R.string.service_name_description);
 		Intent notificationIntent = new Intent(context, MainActivity.class);
 		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
 
 		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
 
 		mNotificationManager.notify(CALL_SCANNER_SVC_NOTIFICATION, notification);
 	}
 
 	public void cancelCallScannerNotification(Context context) {
 		NotificationManager mNotificationManager = 
 				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		mNotificationManager.cancel(CALL_SCANNER_SVC_NOTIFICATION);
 	}
 
 	@Override
 	public void callNotification(CallInfo callInfo) {
 		CharSequence text = 
 				callInfo.getTime() + " "
 				+ String.format(callInfo.getContext().getResources().getString(R.string.call_message), callInfo.getCaller());
 		showToastWithImage(callInfo.getContext(), text, R.drawable.app_icon);
 		Log.d(TAG, "User notified");
 	}
 
 	// --------------------------- Private methods ----------------------------
 
 	private void showToastWithImage(Context context, CharSequence textToShow, int imageResourceId) {
 		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View layout = inflater.inflate(R.layout.toast_layout, null);
 
 		ImageView image = (ImageView) layout.findViewById(R.id.image);
 		image.setImageResource(imageResourceId);
 		TextView text = (TextView) layout.findViewById(R.id.text);
 		text.setText(textToShow);
 
 		Toast toast = new Toast(context);
 		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
 		toast.setDuration(Toast.LENGTH_LONG);
 		toast.setView(layout);
 		toast.show();
 	}
 	
 }

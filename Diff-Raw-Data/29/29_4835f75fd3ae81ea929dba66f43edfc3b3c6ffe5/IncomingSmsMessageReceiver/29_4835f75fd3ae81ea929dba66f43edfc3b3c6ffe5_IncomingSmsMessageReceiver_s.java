 package edu.utsa.cs.smsmessenger.receiver;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 
 import edu.utsa.cs.smsmessenger.R;
 import edu.utsa.cs.smsmessenger.activity.ConversationActivity;
 import edu.utsa.cs.smsmessenger.model.ContactContainer;
 import edu.utsa.cs.smsmessenger.model.MessageContainer;
 import edu.utsa.cs.smsmessenger.util.AppConstants;
 import edu.utsa.cs.smsmessenger.util.ContactsUtil;
 import edu.utsa.cs.smsmessenger.util.SmsMessageHandler;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.telephony.SmsMessage;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * This class listens for incoming SMS messages and broadcasts intents to
  * activities.
  *
  * @author Michael Madrigal
  * @version 1.0
  * @since 1.0
  *
  */
 public class IncomingSmsMessageReceiver extends BroadcastReceiver {
 
 	private SmsMessageHandler smsMessageHandler;
 	private Context context;
 
 	private class SaveNewMessagesToDbTask extends AsyncTask<Object, Void, Void> {
 		@Override
 		protected Void doInBackground(Object... objects) {
 			if (context != null) {
 				for (Object msg : objects) {
 					getSmsMessageHandler(context).saveSmsToDB(
 							(MessageContainer) msg);
 				}
 				getSmsMessageHandler(context).close();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			Intent newMsgIntent = new Intent(
 					SmsMessageHandler.UPDATE_MSG_INTENT);
 			context.sendBroadcast(newMsgIntent);
 		}
 	}
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		this.context = context;
 		final Bundle bundle = intent.getExtras();
 
 		SharedPreferences preferences;
 		preferences = context.getSharedPreferences(AppConstants.APP_PREF_KEY,
 				Context.MODE_PRIVATE);
 		boolean allowSMSPropagation = preferences.getBoolean(
 				AppConstants.APP_PREF_SMS_PROPAGATION_KEY, true);
 
 		ArrayList<MessageContainer> newMsgList = new ArrayList<MessageContainer>();
 		try {
 			if (bundle != null) {
 				final Object[] pdusObj = (Object[]) bundle.get("pdus");
 
 				for (int i = 0; i < pdusObj.length; i++) {
 
 					SmsMessage currentMessage = SmsMessage
 							.createFromPdu((byte[]) pdusObj[i]);
 
 					MessageContainer msg = new MessageContainer(
 							SmsMessageHandler.MSG_TYPE_IN);
 					msg.setPhoneNumber(currentMessage
 							.getDisplayOriginatingAddress());
 					msg.setPhoneNumber(ContactsUtil.getStrippedPhoneNumber(msg.getPhoneNumber()));
 					msg.setBody(currentMessage.getDisplayMessageBody());
 
 					// This is how it should be done, but emulator times are all
 					// messed up
 					//Calendar cal = Calendar.getInstance();
 					//cal.setTimeInMillis(currentMessage.getTimestampMillis());
 					//msg.setDate(cal.getTimeInMillis());
 
 					// This is the way to correct emulator times
 					msg.setDate(Calendar.getInstance().getTimeInMillis());
 
 					ContactContainer contact = ContactsUtil.getContactByPhoneNumber(context.getContentResolver(), msg.getPhoneNumber());
 					
 					msg.setPhoneNumber(contact.getPhoneNumber());
 					if(contact.getId()!=-1)
 						msg.setContactId(contact.getId());
 
 					Log.d("IncomingSMSReceiver",
 							"phoneNumber: " + msg.getPhoneNumber()
									+ "; message: " + msg.getBody());
 
 					String notificationStr = String.format(context.getResources().getString(R.string.message_recevied_notification), 
 							(contact.getDisplayName()!=null?contact.getDisplayName():msg.getPhoneNumber()));
 					
 					Toast toast = Toast.makeText(context, notificationStr, Toast.LENGTH_LONG);
 					toast.show();
 
 					newMsgList.add(msg);
 
 					sendNotification(context, msg, contact);
 				}
 			}
 		} catch (Exception e) {
 			Log.e("IncomingSMSReceiver", "Exception in onReceive(): " + e);
 		}
 
 		Log.d("IncomingSMSReceiver", "New Msg List Size: " + newMsgList.size());
 
 		SaveNewMessagesToDbTask saveThread = new SaveNewMessagesToDbTask();
 		saveThread.execute(newMsgList.toArray());
 
 		// Stop sms propagation to native messenger
 		if (!allowSMSPropagation)
 			this.abortBroadcast();
 	}
 
 	private SmsMessageHandler getSmsMessageHandler(Context context) {
 		if (smsMessageHandler == null)
 			smsMessageHandler = new SmsMessageHandler(context);
 		return smsMessageHandler;
 	}
 
 	private void sendNotification(Context context, MessageContainer msg, ContactContainer contact) {
 		Intent intent = new Intent(context, ConversationActivity.class);
 		intent.putExtra(SmsMessageHandler.COL_NAME_PHONE_NUMBER,
 				msg.getPhoneNumber());
 		intent.putExtra(SmsMessageHandler.COL_NAME_CONTACT_ID,
 				msg.getContactId());
 
 		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
 				PendingIntent.FLAG_UPDATE_CURRENT);
 
 		String notificationStr = String.format(context.getResources().getString(R.string.message_recevied_notification), 
 				(contact.getDisplayName()!=null?contact.getDisplayName():msg.getPhoneNumber()));
 		
 		// Create Notification
 		Notification.Builder builder = new Notification.Builder(context)
 				.setContentTitle(notificationStr)
 				.setContentText(msg.getBody())
 				.setSmallIcon(R.drawable.hg_launcher_icon).setContentIntent(pIntent)
 				.setAutoCancel(true)
 				.setDefaults(Notification.DEFAULT_ALL);
 
 		// Create Notification Manager
 		NotificationManager notificationmanager = (NotificationManager) context
 				.getSystemService(Context.NOTIFICATION_SERVICE);
 		
 		// Build Notification with Notification Manager
 		notificationmanager.notify((int)msg.getContactId(), builder.getNotification());
 	}
 
 }

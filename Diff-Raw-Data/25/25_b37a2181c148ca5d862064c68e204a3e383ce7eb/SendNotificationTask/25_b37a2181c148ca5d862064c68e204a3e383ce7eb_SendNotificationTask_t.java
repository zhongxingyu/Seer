 package com.hannah.arduinomotiondetector.tasks;
 
 import java.io.File;
 import java.util.Calendar;
 
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.telephony.SmsManager;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.hannah.arduinomotiondetector.GMailSender;
 import com.hannah.arduinomotiondetector.NotificationPreferences;
 import com.hannah.arduinomotiondetector.WebSender;
 
 public class SendNotificationTask extends AsyncTask<File, Void, Void> {
 
 	private static final String SENDER_EMAIL = "uoftarduinonotifier@gmail.com";
 	private static final String SENDER_PASSWORD = "996883975";
 
 	private Context mContext;
 
 	public SendNotificationTask(Context context) {
 		mContext = context;
 	}
 
 	@Override
 	protected Void doInBackground(File... arg0) {
 		if (NotificationPreferences.getEmailOn(mContext)) {
 			File file = arg0.length > 0 ? arg0[0] : null;
 
 			try {
 				GMailSender sender = new GMailSender(SENDER_EMAIL, SENDER_PASSWORD);
 				sender.sendMail("Arduino Alert!", "New alert at " + Calendar.getInstance().getTime(), file, SENDER_EMAIL, NotificationPreferences.getEmail(mContext));
 			} catch (Exception e) {
 				Log.e("SendMail", e.getMessage(), e);
 			}
 		}
 
 		if (NotificationPreferences.getSMSOn(mContext)) {
 			sendSMS(mContext, NotificationPreferences.getPhone(mContext), "Arduino Motion Alert at " + Calendar.getInstance().getTime());
 		}
 		
		//new WebSender(NotificationPreferences.getIP(mContext)).execute(WebSender.getAlertXMLMessage(mContext));
 
 		return null;
 	}
 
 	private static void sendSMS(Context context, String phoneNumber, String message) {
 		TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
 		int simState = telMgr.getSimState();
 
 		if (simState == TelephonyManager.SIM_STATE_READY) {
 			PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, SendNotificationTask.class), 0);
 			SmsManager sms = SmsManager.getDefault();
 			sms.sendTextMessage(phoneNumber, null, message, pi, null);
 		} else {
 			Toast.makeText(context, "Phone cannot send text message", Toast.LENGTH_LONG).show();
 		}
 	}
 
 }

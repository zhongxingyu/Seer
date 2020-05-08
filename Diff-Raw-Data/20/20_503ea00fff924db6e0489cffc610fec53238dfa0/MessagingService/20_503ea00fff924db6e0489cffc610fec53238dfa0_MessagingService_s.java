 package com.androidproductions.ics.sms.service;
 
 import android.app.Activity;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.IBinder;
 import android.os.Looper;
 import android.os.Message;
 import android.telephony.PhoneStateListener;
 import android.telephony.SmsManager;
 import android.telephony.TelephonyManager;
 import android.widget.Toast;
 
 import com.androidproductions.ics.sms.Constants;
 import com.androidproductions.ics.sms.R;
 import com.androidproductions.ics.sms.SmsDialog;
 import com.androidproductions.ics.sms.SmsNotify;
 import com.androidproductions.ics.sms.preferences.ConfigurationHelper;
 import com.androidproductions.ics.sms.receivers.MyPhoneStateListener;
 import com.androidproductions.ics.sms.receivers.SmsUpdateReceiver;
 import com.androidproductions.ics.sms.transactions.NotificationHelper;
 import com.androidproductions.ics.sms.utils.LogHelper;
 import com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants.Action;
 import com.androidproductions.libs.sms.SmsMessage;
 import com.androidproductions.libs.sms.com.androidproductions.libs.sms.constants.SmsUri;
 import com.androidproductions.libs.sms.Transaction;
 
 import static android.content.Intent.ACTION_BOOT_COMPLETED;
 
 public class MessagingService extends Service{
 
 	private static final String TAG = "ICSSMSService";
     private ServiceHandler mServiceHandler;
 
     private static final Handler mToastHandler = new Handler();
 
     private static final String SMS_URI = "SMSURI";
     
 	@Override
 	public IBinder onBind(final Intent arg0) {
 		return null;
 	}
 	
 	@Override
     public void onCreate() {
         // Start up the thread running the service.  Note that we create a
         // separate thread because the service normally runs in the process's
         // main thread, which we don't want to block.
 		
         final HandlerThread thread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
         thread.start();
 
         final Looper mServiceLooper = thread.getLooper();
         mServiceHandler = new ServiceHandler(mServiceLooper,this);
     }
 	
 	public int onStartCommand(final Intent intent, final int flags, final int startId)
 	{
         final int mResultCode = intent != null ? intent.getIntExtra("result", 0) : 0;
 
         if (intent != null) {
             LogHelper.t(this, intent.getAction());
         }
         else {
             LogHelper.w("Starting to process message with no intent");
         }
         final Message msg = mServiceHandler.obtainMessage();
         msg.arg1 = startId;
         msg.obj = intent;
         msg.arg2 = mResultCode;
         mServiceHandler.sendMessage(msg);
         return Service.START_STICKY;
 	}
 	
 	@SuppressWarnings("deprecation")
     @Override
 	public void onStart(final Intent intent, final int startId) {
 		onStartCommand(intent, startId, 0);
 	}
 	
 	private static final class ServiceHandler extends Handler {
 		private final Service context;
         private final ConfigurationHelper mConfig;
         private MyPhoneStateListener phoneListener;
         public ServiceHandler(final Looper looper, final Service con) {
             super(looper);
             context = con;
             mConfig = ConfigurationHelper.getInstance();
         }
 
         /**
          * Handle incoming transaction requests.
          * The incoming requests are initiated by the MMSC Server or by the MMS Client itself.
          */
         @Override
         public void handleMessage(final Message msg) {
             final int serviceId = msg.arg1;
             final int resultCode = msg.arg2;
             final Intent intent = (Intent)msg.obj;
             if (intent != null) {
                 final String action = intent.getAction();
                 final int error = intent.getIntExtra("errorCode", 0);
                 if (Action.SENT.equals(action)) {
                     handleSmsSent(intent, error, resultCode);
                 } else if (Constants.SMS_RECEIVED_ACTION.equals(action)) {
                     handleSmsRecieved(intent);
                 } else if (ACTION_BOOT_COMPLETED.equals(action)) {
                     handleBootCompleted();
                 } else if (Constants.ACTION_SERVICE_STATE_IN_SERVICE.equals(action)) {
                     handleServiceStateChanged();
                 } else if (Action.SEND.endsWith(action)) {
                     handleSendMessage();
                 }
             }
             // NOTE: We MUST not call stopSelf() directly, since we need to
             // make sure the wake lock acquired by AlertReceiver is released.
             SmsUpdateReceiver.finishStartingService(context, serviceId);
         }
         
         
         private void handleServiceStateChanged() {
         	unRegisterForServiceStateChanges();
         	sendFirstQueuedMessage();
         	NotificationHelper.getInstance(context).cancelSendFailed();
 		}
 
 		private void handleBootCompleted() {
 			sendFirstQueuedMessage();
 		}
 
 		private void handleSendMessage() {
 			sendFirstQueuedMessage();
 		}
 
 		private void handleSmsSent(final Intent intent, final int error, final int resultCode) {
             //noinspection ConstantConditions
             final Uri uri = (Uri)intent.getExtras().get(SMS_URI);
             final Transaction trans = new Transaction(context);
 	        if (resultCode == Activity.RESULT_OK) {
                 trans.sentMessage(uri);
 	        } else if ((resultCode == SmsManager.RESULT_ERROR_RADIO_OFF) ||
 	                (resultCode == SmsManager.RESULT_ERROR_NO_SERVICE)) {
 	            // We got an error with no service or no radio. Register for state changes so
 	            // when the status of the connection/radio changes, we can try to send the
 	            // queued up messages.
 	            registerForServiceStateChanges();
 	            // We couldn't send the message, put in the queue to retry later.
 	            trans.requeueMessage(uri);
 	            mToastHandler.post(new Runnable() {
 	                public void run() {
 	                    Toast.makeText(context, context.getString(R.string.message_queued),
 	                            Toast.LENGTH_SHORT).show();
 	                }
 	            });
 	            
	        } else {
 	            messageFailedToSend(uri, error);
 	            sendFirstQueuedMessage();
 	        }
 	    }
 
 	    private void sendFirstQueuedMessage() {
 	    	final Cursor c = context.getContentResolver().query(SmsUri.QUEUED_URI, null, null,
 					null, "date ASC");
 	    	if (c!= null)
             {
                 if (c.moveToFirst())
                 {
                     final String address = c.getString(c.getColumnIndex("address"));
                     final long id = c.getLong(c.getColumnIndex("_id"));
                     final long date = c.getLong(c.getColumnIndex("date"));
                     final String body = c.getString(c.getColumnIndex("body"));
                     sendMessage(new SmsMessage(body, address,date, id));
                 }
                 c.close();
             }
 		}
 	    
 		private void sendMessage(final SmsMessage sms) {
             final Transaction trans = new Transaction(context);
             trans.sendMessage(sms,null);
 		}
 
 		private void messageFailedToSend(final Uri uri, final int error) {
             LogHelper.e("Message sending failed with error code:"+error);
             final Transaction trans = new Transaction(context);
             trans.failedMessage(uri);
 	        NotificationHelper.getInstance(context).notifySendFailed();
 	    }
 
 		public void handleSmsRecieved(final Intent intent)
         {
             //noinspection ConstantConditions
             final SmsMessage message = new SmsMessage((Object[]) intent.getExtras().get("pdus"));
 
             // Save message if needed
            if (mConfig.getBooleanValue(ConfigurationHelper.DISABLE_OTHER_NOTIFICATIONS))
                 new Transaction(context).SaveIncoming(message);
 
             // Display Dialog
             if (mConfig.getBooleanValue(ConfigurationHelper.DIALOG_ENABLED))
                 displayDialog(context, message);
             context.sendBroadcast(new Intent("com.androidproductions.ics.sms.UPDATE_DIALOG"));
         }
         
         private void displayDialog(final Context context, final SmsMessage sms) {
         	final Intent dialogIntent;
     		if (mConfig.getStringValue(ConfigurationHelper.DIALOG_TYPE).equals("2"))
     			dialogIntent = new Intent(context, SmsNotify.class);
     		else
     			dialogIntent = new Intent(context, SmsDialog.class);
     		dialogIntent.putExtra(Constants.SMS_RECEIVE_LOCATION, sms.getAddresses()[0]);
     		dialogIntent.putExtra(Constants.SMS_MESSAGE, sms.getBody());
     		dialogIntent.putExtra(Constants.MESSAGE_TYPE, "SMS");
     		dialogIntent.putExtra(Constants.SMS_TIME, System.currentTimeMillis());
     		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// |Intent.FLAG_ACTIVITY_SINGLE_TOP);
     		context.startActivity(dialogIntent);
     	}
         
         private void registerForServiceStateChanges() {
         	phoneListener = new MyPhoneStateListener(context);
             final TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
             telephony.listen(phoneListener, PhoneStateListener.LISTEN_SERVICE_STATE);
         }
 
         private void unRegisterForServiceStateChanges() {
         	final TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
             telephony.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
         }
     }
 	
 	
 
 }

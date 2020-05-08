 package net.anei.cadpage;
 
 import net.anei.cadpage.donation.DonationManager;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.telephony.SmsMessage;
 import android.telephony.TelephonyManager;
 import android.telephony.SmsMessage.MessageClass;
 
 public class SmsReceiver extends BroadcastReceiver {
   
   private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
   
   private static final String EXTRA_TIMEOUT = "net.anei.cadpage.SmsReceive.MSG_TIMEOUT";
   private static final String EXTRA_TIMEOUT_ID = "net.anei.cadapge.SmsReceive.TIMEOUT_ID";
   private static final String EXTRA_REPEAT_LAST = "net.anei.cadpage.SmsReceive.REPEAT_LAST";
 
   @Override
   public synchronized void onReceive(Context context, Intent intent) {
     if (Log.DEBUG) Log.v("SMSReceiver: onReceive()");
     
     // If initialization failure in progress, shut down without doing anything
     if (TopExceptionHandler.isInitFailure()) return;
     
     // If one of our message accumulator timeouts has tripped, report it
     SmsMsgAccumulator accumulator = SmsMsgAccumulator.instance();
     if (intent.getBooleanExtra(EXTRA_TIMEOUT, false)) {
       long id = intent.getLongExtra(EXTRA_TIMEOUT_ID, 0);
       if (id != 0) accumulator.timeout(id);
     }
     
     // Otherwise pick up and process a new message
     else {
 
       // If repeat_last flag is set, this is a fake intent instructing us
       // to reprocess the most recently received message (that passed the 
       // sender address filter
 
       SmsMmsMessage message = null;
       if (intent.getBooleanExtra(EXTRA_REPEAT_LAST, false)) {
         message = SmsMsgLogBuffer.getInstance().getLastMessage();
         if (message != null) message.setRead(false);
       }
        
       // Otherwise convert Intent into an SMS/MSS message
       else if (ACTION_SMS_RECEIVED.equals(intent.getAction())) {
         SmsMessage[] messages = SmsPopupUtils.getMessagesFromIntent(intent);
         if (messages == null) return;
         message = new SmsMmsMessage( messages,System.currentTimeMillis());
        if (message.getMessageBody() == null) return;
         
         // See if this is a vendor discovery query.  If it is, make it go away
         if (message.isDiscoveryQuery(context)) {
           abortBroadcast();
           return;
         }
         
         // Save message for future test or error reporting use
         // If message is rejected as duplicate, don't do anything except call
         // abortbroadcast to keep it from going to anyone else
         if (! SmsMsgLogBuffer.getInstance().add(message)) {
           if (! ManagePreferences.smspassthru()) abortBroadcast();
           return;
         }
       }
       
       // If we didn't get a message, bail out
       if (message == null) return;
       
       // If this was an incomplete MMS message picked up by the process last
       // message option, bail out
       if (message.getMessageBody() == null) return;
       
       // Class 0 SMS, let the system handle this
       if (message.getMessageType() == SmsMmsMessage.MESSAGE_TYPE_SMS &&
           message.getMessageClass() == MessageClass.CLASS_0) return;
       
       // Pass message to accumulator
       // If the accumulator accepted it, and we aren't passing messages to the
       // default messaging app, abort broadcast to any further receivers
       boolean grabbed = accumulator.addMsg(message);
       if (grabbed && !ManagePreferences.smspassthru()) abortBroadcast();
     }
       
     // Set or clear any timeouts requested by msg accumulator
     long id = accumulator.startTimeoutId();
     if (id > 0) setReminder(context, id, false);
     id = accumulator.cancelTimeoutId();
     if (id > 0) setReminder(context, id, true);
     
     // See if we have a live message to process, and if so, do it
     SmsMmsMessage msg = accumulator.nextMsg();
     if (msg != null) processCadPage(context, msg);
   }
 
 
   /**
    * Set up an alarm to timeout and process a reconstructed message if the
    * expected other parts of the page never show up
    * @param msgTimeout timeout interval in seconds
    */
   private void setReminder(Context context, long id, boolean cancel) {
     
 
     Intent intent = new Intent("android.provider.Telephony.SMS_RECEIVED");
     intent.setClass(context, SmsReceiver.class);
     intent.putExtra(EXTRA_TIMEOUT, true);
     intent.putExtra(EXTRA_TIMEOUT_ID, id);
     
     // We don't really use it, but it keeps the alarm intents functionally distinct
     intent.setData(Uri.parse("Cadpage://" + id));
 
     int flags = (cancel ? PendingIntent.FLAG_NO_CREATE : PendingIntent.FLAG_ONE_SHOT);
     PendingIntent pendIntent =
       PendingIntent.getBroadcast(context, 0, intent, flags);
     if (cancel) {
       if (pendIntent != null) pendIntent.cancel();
     } else {
       AlarmManager myAM = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
       int msgTimeout = ManagePreferences.partMsgTimeout();
       long triggerTime = System.currentTimeMillis() + (msgTimeout * 1000);
       myAM.set(AlarmManager.RTC_WAKEUP, triggerTime, pendIntent);
     }
   }
 
 
   /**
    * Final process of message once we have determined it is a CAD page message
    * This will be called by MmsTransactionService to handle MMS messages
    * @param context current context
    * @param message message to be processed
    */
   public static void processCadPage(Context context, SmsMmsMessage message) {
     
     // Add new message to the message queue
     SmsMessageQueue.getInstance().addNewMsg(message);
     
     // If running in restricted mode, that's all we do
     if (! DonationManager.instance().isEnabled()) return;
     
     // Publish message contents if so requested
     if (ManagePreferences.publishPages()) message.broadcastIntent(context, false);
   
     // Notify user if so configured
     boolean notify = ManageNotification.show(context, message);
     
     // Determine if application should pop up right now
     boolean process = startApp(context);
     
     // If either a screen display or notification was generated by this
     // message, it is time to wake up the device.
     // After which we can release the power lock that kept the power on
     // until we got this far
     if (process ||  notify) ManageWakeLock.acquireFull(context);
 
     // And finally, launch the main application screen
     if (process) CallHistoryActivity.launchActivity(context, message);
   }
 
   /**
    * Determine if application should be launched when CAD page is received
    * @param context
    * @return
    */
   private static boolean startApp(Context context) {
     
 
     // If popup isn't enabled, this is as afar as we go
     if (! ManagePreferences.popupEnabled()) return false;
 
     // Should popup should only show when keyguard is on
     if (ManagePreferences.onlyShowOnKeyguard()) {
       ManageKeyguard.initialize(context);
       if (! ManageKeyguard.inKeyguardRestrictedInputMode()) return false;
     }
 
     // Fetch call state, if the user is in a call or the phone is ringing we don't want to show the popup
     if (ManagePreferences.noShowInCall()) {
       TelephonyManager mTM = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
       boolean callStateIdle = mTM.getCallState() == TelephonyManager.CALL_STATE_IDLE;
       if (!callStateIdle) return false;
     }
     
     // Otherwise OK
     return true;
   }
   
   /**
    * Request most recently received page be reprocessed
    * @param context requesting context
    */
   public static void repeatLastPage(Context context) {
     Intent intent = new Intent("android.provider.Telephony.SMS_RECEIVED");
     intent.setClass(context, SmsReceiver.class);
     intent.putExtra(EXTRA_REPEAT_LAST, true);
     context.sendOrderedBroadcast(intent, null);
   }
 }
 

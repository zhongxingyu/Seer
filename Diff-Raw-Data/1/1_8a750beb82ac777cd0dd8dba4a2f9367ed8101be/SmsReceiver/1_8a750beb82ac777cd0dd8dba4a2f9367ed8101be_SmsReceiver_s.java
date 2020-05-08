 package net.anei.cadpage;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.telephony.SmsMessage;
 import android.telephony.SmsMessage.MessageClass;
 
 public class SmsReceiver extends BroadcastReceiver {
   
   private static String MSG_FILENAME = "last.msg";
 
   @Override
   public void onReceive(Context context, Intent intent) {
     if (Log.DEBUG) Log.v("SMSReceiver: onReceive()");
     
     intent.setClass(context, SmsReceiverService.class);
     intent.putExtra("result", getResultCode());
 
     SmsMmsMessage message = null;
     
     // If repeat_last flag is set, this is a fake intent instructing us
     // to reprocess the most recently recieved message (that passed the 
     // sender address filter
     if (intent.getBooleanExtra("repeat_last", false)) {
       ObjectInputStream is = null;
       try {
         is = new ObjectInputStream(
           context.openFileInput(MSG_FILENAME));
         message = SmsMmsMessage.readObject(context, is);
       } catch (FileNotFoundException ex) {
       } catch (Exception ex) {
         Log.e(ex);
       } finally {
         if (is != null) try {is.close();} catch (IOException ex) {}
       }
     }
     // Otherwise convert Intent into an SMS/MSS message
     else {
       SmsMessage[] messages = SmsPopupUtils.getMessagesFromIntent(intent);
       message = new SmsMmsMessage(context, messages,System.currentTimeMillis());
     }
 
     // And determine if this is a CAD Page call
     if (message != null && cadPageCall(context, message)){
     	
       // If it is, abort broadcast to any other receivers
       // and launch the main page with this message
       abortBroadcast();
       CallHistoryActivity.launchActivity(context, message);
     }
   }
 
   /**
    * Determine if this intent message is a CAD page call
    * @param context message context
    * @param intent Intent
    * @return true if message is a CAD page call that should be processed further
    */
   private boolean cadPageCall(Context context, SmsMmsMessage message){
 
     String strMessage = message.getMessageFull();
     
     // Class 0 SMS, let the system handle this
     if (message.getMessageType() == SmsMmsMessage.MESSAGE_TYPE_SMS &&
         message.getMessageClass() == MessageClass.CLASS_0) return false;
 
     // First look at from Filter.
     String sFilter = ManagePreferences.filter();
     String sAddress = message.getAddress();
     if (! match(sAddress, sFilter)) return false;
     if (Log.DEBUG) Log.v("SMSReceiver/CadPageCall: Filter Matches checking call Location -" + sFilter);
     
     // Save message to file for future test use
     ObjectOutputStream os = null;
     try {
       os = new ObjectOutputStream(
         context.openFileOutput(MSG_FILENAME, Context.MODE_PRIVATE));
       os.writeObject(message);
     } catch (IOException ex) {
       Log.e(ex);
     } finally {
       if (os != null) try {os.close();} catch (IOException ex) {}
     }
     
     return SmsMsgParser.isPageMsg(strMessage);
   }
 
   private boolean match(String address, String filter) {
     return (filter == null || filter.length() == 0 ||
              filter.equals("*")  || address.contains(filter));
   }
 }
 

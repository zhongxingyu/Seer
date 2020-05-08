 package realtalk.util.gcm;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 import com.google.android.gcm.GCMBaseIntentService;
 
 import realtalk.controller.ChatController;
 import realtalk.util.MessageInfo;
 import realtalk.util.RequestParameters;
 
 /**
  * IntentService responsible for handling GCM messages
  * 
  * @author Colin Kho
  *
  */
 public class GCMIntentService extends GCMBaseIntentService {
     
     public GCMIntentService() {
        super();
     }
     
     @Override
     protected void onError(Context context, String stRegId) {
         Log.v("GCMIntentService", "Registration Error");
         GCMUtilities.sendRegistrationResult(context, stRegId, GCMUtilities.ERROR);
         
     }
 
     @Override
     protected void onMessage(Context context, Intent intent) {
         Log.v("GCMIntentService", "Received Message");
         handleMessage(context, intent);
     }
 
     @Override
     protected void onRegistered(Context context, String stRegId) {
         Log.v("GCMIntentService", "Registration Successful");
         GCMUtilities.sendRegistrationResult(context, stRegId, GCMUtilities.SUCCESS);       
     }
 
     @Override
     protected void onUnregistered(Context context, String stRegId) {
         // TODO        
     }
     
     /**
      * Helper method that handles the message received by GCM.
      * 
      * @param context Context it is received in.
      * @param intent  Intent containing GCM message.
      */
     private void handleMessage(Context context, Intent intent) {
         Log.v("GCMIntentService", "Handling Message");
         String stSender = intent.getStringExtra(RequestParameters.PARAMETER_MESSAGE_SENDER);
         String stTimestamp = intent.getStringExtra(RequestParameters.PARAMETER_MESSAGE_TIMESTAMP);
         String stBody = intent.getStringExtra(RequestParameters.PARAMETER_MESSAGE_BODY);
         //String stRoomName = intent.getStringExtra(RequestParameters.PARAMETER_ROOM_NAME);
         String stRoomId = intent.getStringExtra(RequestParameters.PARAMETER_ROOM_ID);
         long timestamp = Long.parseLong(stTimestamp);
         MessageInfo msginfo = new MessageInfo(stBody, stSender, timestamp);
         ChatController.getInstance().addMessageToRoom(msginfo, stRoomId, context);
     }
 }

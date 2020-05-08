 package com.onb.eventHowler.service;
 
 import com.onb.eventHowler.application.*;
 import com.onb.eventHowler.domain.EventHowlerParticipant;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.os.IBinder;
 import android.telephony.SmsManager;
 import android.util.Log;
 import android.widget.Toast;
 
 public class EventHowlerSenderService extends Service{
 	 
 	private static EventHowlerApplication application;
 	private static EventHowlerOpenDbHelper openHelper;
 	
 	private static final int PARTICIPANT_COLUMN_PNUMBER  = 0, 
 			PARTICIPANT_COLUMN_STATUS  = 1,
 			PARTICIPANT_COLUMN_TRANSACTION_ID =2,
 			PARTICIPANT_COLUMN_MESSAGE  = 3,
 			COLUMN_MESSAGES = 1,
 			INITIAL_POSITION = 0;
 	
 	private Cursor participantCursor;
 	private Cursor messageCursor;
 	private String invitationMessage;
 	private BroadcastReceiver sentSMSActionReceiver;
 	private BroadcastReceiver deliveredSMSActionReceiver;
 	
 	// DO NOT TOUCH THE VALUES, IT IS USE IN SUBSTRINGS. CAN CAUSE RUNTIME ERROR
 	private String SENT_SMS_ACTION = "SENT_SMS_ACTION";
 	private String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
 	
 
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		Toast.makeText(this, "creating event Howler Sender service",
 				Toast.LENGTH_SHORT).show();
 		super.onCreate();
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 				
 		openHelper = new EventHowlerOpenDbHelper(getApplicationContext());
 		
 		sentSMSActionReceiver = new BroadcastReceiver(){
 			@Override
 			public void onReceive(Context _context, Intent intent){
 				String transactionId;
 				transactionId = intent.getAction().toString().substring(16);
 				switch (getResultCode()){
 				case Activity.RESULT_OK:
 					sentSMSBroadcastReceiverAssistant(1, transactionId);
 					break;
 				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
 					sentSMSBroadcastReceiverAssistant(2, transactionId);
 					break;
 				case SmsManager.RESULT_ERROR_RADIO_OFF:
 					sentSMSBroadcastReceiverAssistant(3, transactionId);
 					break;
 				case SmsManager.RESULT_ERROR_NULL_PDU:
 					sentSMSBroadcastReceiverAssistant(4, transactionId);
 					break;
 				}
 			}
 		};
 		
 		deliveredSMSActionReceiver = new BroadcastReceiver() {	
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				String transactionId;
 				transactionId = intent.getAction().toString().substring(21);
 				switch (getResultCode()) {
 				case Activity.RESULT_OK:
 					Log.d("delevery intent", "delivery intent received, successful");
 					deliveredSMSBroadcastReceiver(1, transactionId);
 					break;
 				case Activity.RESULT_CANCELED:
 					Log.d("delivery intent", "delivery intent received, unsuccessful");
 					deliveredSMSBroadcastReceiver(1, transactionId);
 					break;
 				}	
 			}
 		};
 		
 		//test data
 		/*openHelper.insertParticipant(new EventHowlerParticipant("15555215556", "2134", "FOR_SEND_INVITATION"));
 		openHelper.insertParticipant(new EventHowlerParticipant("15555215558", "3314", "FOR_SEND_INVITATION"));
 		openHelper.updateStatus(new EventHowlerParticipant("15555215558", "6839", "FOR_SEND_REPLY"), "thank you for attending you confirmation code is 35dh2h");
 		openHelper.insertParticipant(new EventHowlerParticipant("15555215560", "9863", "FOR_SEND_INVITATION"));
 		openHelper.insertParticipant(new EventHowlerParticipant("15555215562", "5324", "FOR_SEND_INVITATION"));
 		*///openHelper.populateMessages("Hello fella, i would like to invite for a pack party ");
 		//test data 
 		
 		Toast.makeText(this, "event Howler Sender service started",
 				Toast.LENGTH_SHORT).show();
 		application = (EventHowlerApplication)getApplication();
 		
 		participantCursor = openHelper.getAllParticipantsWithUnsentMessages();
 		messageCursor = openHelper.getAllMesssages();
 		messageCursor.moveToPosition(INITIAL_POSITION);
 		invitationMessage = messageCursor.getString(COLUMN_MESSAGES);
 		
 		startSeekingForDataToBeSent();
 		
 		return Service.START_NOT_STICKY;
 		
 	}
 
 	private void startSeekingForDataToBeSent() {
 		Runnable forSendSeeker = new Runnable() {
 			
 			public void run() {
 				
 				participantCursor.moveToFirst();
 				while(true){
 					if(participantCursor.getCount() == 0){
 						Log.d("startSeekingForDataToBeSent", "loop running if part");
 						threadSleep();
 						
 						if(!application.hasOngoingEvent()){
 							participantCursor.close();
 							messageCursor.close();
 							openHelper.resetDatabase();
 							break;
 						}
 						participantCursor.close();
 						participantCursor = openHelper.getAllParticipantsWithUnsentMessages();
 						
 					}
 					else{
 						Log.d("startSeekingForDataToBeSent", "loop running else part");
 						
 						registerReceiver(deliveredSMSActionReceiver, 
 								new IntentFilter(DELIVERED_SMS_ACTION + "_" + participantCursor.getString(PARTICIPANT_COLUMN_TRANSACTION_ID)));
 						registerReceiver(sentSMSActionReceiver, 
 								new IntentFilter(SENT_SMS_ACTION + "_" + participantCursor.getString(PARTICIPANT_COLUMN_TRANSACTION_ID)));
 						
 						if(participantCursor.getString(PARTICIPANT_COLUMN_STATUS).equals("FOR_SEND_INVITATION")){
 							
 							sendSMS(participantCursor.getString(PARTICIPANT_COLUMN_PNUMBER),
 									invitationMessage, participantCursor.getString(PARTICIPANT_COLUMN_TRANSACTION_ID));
 							Log.d("startSeekingForDataToBeSent", "sending invitation to " + participantCursor.getString(PARTICIPANT_COLUMN_PNUMBER));
 							
 						}
 						else if(participantCursor.getString(PARTICIPANT_COLUMN_STATUS).equals("FOR_SEND_REPLY")){
 							
 							sendSMS(participantCursor.getString(PARTICIPANT_COLUMN_PNUMBER),
 									participantCursor.getString(PARTICIPANT_COLUMN_MESSAGE), participantCursor.getString(PARTICIPANT_COLUMN_TRANSACTION_ID));
 							Log.d("startSeekingForDataToBeSent", "sending reply to " + participantCursor.getString(PARTICIPANT_COLUMN_PNUMBER));
 						}
 						
						threadSleep();
						
 						if(!participantCursor.isLast()){
							Log.d("participant number", participantCursor.getString(0));
 							participantCursor.moveToNext();
 						}
 						else if(application.hasOngoingEvent() && participantCursor.isLast()){
 							participantCursor.close();
 							participantCursor = openHelper.getAllParticipantsWithUnsentMessages();
 							participantCursor.moveToFirst();
 						}
 						else{
 							participantCursor.close();
 							messageCursor.close();
 							openHelper.resetDatabase();
 							break;
 						}
 					}
 				}
 				unregisterReceiver(deliveredSMSActionReceiver);
 				unregisterReceiver(sentSMSActionReceiver);
 				application.setRunning(false);
 				stopSelf();
 			}
 
 		};
 		new Thread(forSendSeeker).start();
 	}
 
 	private void threadSleep() {
 		try {
 			Thread.sleep(2000);
 		}
 		catch (Exception e) {Log.d("startSeekingForDataToBeSent", "UNABLE TO SLEEP");}
 	}
 	
 	private void sentSMSBroadcastReceiverAssistant(int resultCode,
 			String transactionId) {
 		
 		String phoneNumber = openHelper.findNumberWithTransactionId(transactionId);
 		if(phoneNumber != "NONE"){
 			switch(resultCode){
 			case 1:
 				Log.d("sentSMSBroadcastReceiverAssistant", "code 1, RESULT_OK " + transactionId);
 				openHelper.updateStatus(new EventHowlerParticipant(
 						phoneNumber, transactionId, "SENT"), "");
 				break;
 			case 2:
 				Log.d("sentSMSBroadcastReceiverAssistant", "code 2");
 				openHelper.updateStatus(new EventHowlerParticipant(
 						phoneNumber, transactionId, "RESULT_ERROR_GENERIC_FAILURE"), "");
 				break;
 			case 3:
 				Log.d("sentSMSBroadcastReceiverAssistant", "code 3");
 				openHelper.updateStatus(new EventHowlerParticipant(
 						phoneNumber, transactionId, "RESULT_ERROR_RADIO_OFF"), "");
 				break;
 			case 4:
 				Log.d("sentSMSBroadcastReceiverAssistant", "code 4");
 				openHelper.updateStatus(new EventHowlerParticipant(
 						phoneNumber, transactionId, "RESULT_ERROR_NULL_PDU"), "");
 				break;
 			}
 		}
 	}
 	
 	private void deliveredSMSBroadcastReceiver(int resultCode,
 			String transactionId) {
 		
 		String phoneNumber = openHelper.findNumberWithTransactionId(transactionId);
 		if(phoneNumber != "NONE"){
 			switch(resultCode){
 			case 1:
 				Log.d("deliverySMSBroadcastReceiver", "code 1, RESULT_OK " + transactionId);
 				openHelper.updateStatus(new EventHowlerParticipant(
 						phoneNumber, transactionId, "SUCCESSFUL_DELIVERY"), "");
 				break;
 			case 2:
 				Log.d("deliverySMSBroadcastReceiver", "code 2");
 				openHelper.updateStatus(new EventHowlerParticipant(
 						phoneNumber, transactionId, "UNSUCESSESFUL_DELIVERY"), "");
 				break;
 			}
 		}
 	}
 	
 	private void sendSMS(String phoneNumber, String message, String transactionId) {
 		
 		Log.d("sendSMS", phoneNumber);
 		PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0,
 				new Intent(SENT_SMS_ACTION + "_" + transactionId), 0);
 		PendingIntent deliveredPI = PendingIntent.getBroadcast(getApplicationContext(), 0,
 				new Intent(DELIVERED_SMS_ACTION + "_" + transactionId), 0);
 	    SmsManager sms = SmsManager.getDefault();
 	    sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
 	}
 	
 	@Override
 	public void onDestroy() {
 		Toast.makeText(this, "event Howler sending service destroyed",
 				Toast.LENGTH_SHORT).show();
 		openHelper.close();
 		super.onDestroy();
 	}
 	
 }

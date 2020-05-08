 /*
  * Copyright (C) 2009 Dimagi Inc., UNICEF
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  *
  */
 
 /**
  * 
  */
 package org.rapidandroid.receiver;
 
 import java.sql.Timestamp;
 import java.util.Date;
 
import org.rapidandroid.RapidAndroidApplication;
 import org.rapidandroid.SystemHealthTracking;
 import org.rapidandroid.SystemHealthTracking.SagesEventType;
import org.rapidandroid.activity.Dashboard;
 import org.rapidandroid.content.translation.MessageTranslator;
 import org.rapidandroid.data.RapidSmsDBConstants;
import org.rapidandroid.data.controller.MessageBodyParser;
import org.rapidandroid.data.controller.MessageBodyParser.SagesPdu;
 import org.rapidandroid.service.QueueAndPollService;
 import org.rapidsms.java.core.model.Message;
 import org.rapidsms.java.core.model.Monitor;
 
 import android.content.BroadcastReceiver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 //import android.telephony.gsm.SmsMessage;
 import android.telephony.SmsMessage;
 import android.util.Log;
 
 /**
  * 
  * Initial broadcast receiver for RapidAndroid.
  * 
  * Gets triggered on Android SMS receive event, gets a handle to the message and
  * does the following: - verify that it's what the app wants to process - save
  * message to rapidandroid's db via the content provider - save a new
  * mMonitorString if necessary (that's handled by the content provider save) -
  * delete message from inbox because we don't want it to be in duplicate - upon
  * successful save, trigger a separate event to tell the next process that a
  * save was done.
  * 
  * 
  * 
  * @author Daniel Myung dmyung@dimagi.com
  * @created Jan 12, 2009
  * 
  * 
  * 
  * 
  */
 public class SmsReceiver extends BroadcastReceiver {
 	private static SystemHealthTracking healthTracker = new SystemHealthTracking(SmsReceiver.class);
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
 	 * android.content.Intent)
 	 */
 
 	Uri uriSms = Uri.parse("content://sms/inbox");
 	
 	static Context mContext;
 	static long lastTimeRun;
 	static Thread timerThread = null;
 
 	/**
 	 * Runs for 2 minutes OR as long as the sages multisms worktable is "dirty" (i.e. contains
 	 * incomplete messages)
 	 * 
 	 * @author SAGES/POKUAM1
 	 * @return Thread that cleans up stale multisms messages from sages worktable
 	 */
 	Thread threadFactory(){
 		return new Thread(new Runnable(){
 
 			int i = 5;
 			
 			@Override
 			synchronized public void run() {
 				//Uri writeMessageUri = RapidSmsDBConstants.MultiSmsWorktable.CONTENT_URI;
 				long timeelapsed = 0;
 				
 				while (/*timeelapsed <= 2000 120000 ||*/ QueueAndPollService.isDirty == true){
 					timeelapsed = new Date().getTime() - lastTimeRun;
 					Log.d("sages", i + "===HEY BOO, THIS IS MY THREAD THIS IS MY THREAD WOOT!");
 					i--;
 					Intent intentQueuePollTimer = new Intent(mContext, QueueAndPollService.class);
 					intentQueuePollTimer.putExtra("timerMode", true);
 //					intentQueuePoll.putExtra(RapidSmsDBConstants.MultiSmsWorktable.MONITOR_MSG_ID, Integer.valueOf(msgUri1.getPathSegments().get(1)));
 //					intentQueuePoll.putExtra(RapidSmsDBConstants.Message.PHONE, mesg.getOriginatingAddress());
 
 					mContext.startService(intentQueuePollTimer);
 					try {
 						//Thread.sleep(30000);
 						Thread.sleep(10000);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					
 				}
 			}
 			
 		});
 	}
 
 	private void insertMessageToContentProvider(Context context, SmsMessage mesg) {
 		
 		Uri writeMessageUri = RapidSmsDBConstants.Message.CONTENT_URI;
 
 		ContentValues messageValues = new ContentValues();
 		messageValues.put(RapidSmsDBConstants.Message.MESSAGE, mesg.getMessageBody());
 
 		Timestamp ts = new Timestamp(mesg.getTimestampMillis());
 
 		Monitor monitor = MessageTranslator.GetMonitorAndInsertIfNew(context, mesg.getOriginatingAddress());
 
 		messageValues.put(RapidSmsDBConstants.Message.MONITOR, monitor.getID());
 		messageValues.put(RapidSmsDBConstants.Message.TIME, Message.SQLDateFormatter.format(ts)); // expensive
 																									// string
 																									// formatting
 																									// operation.
 		// messageValues.put(RapidSmsDBConstants.Message.TIME,
 		// mesg.getTimestampMillis()); //longs don't store as datetimes
 		messageValues.put(RapidSmsDBConstants.Message.IS_OUTGOING, false);
 		Date now = new Date();
 		messageValues.put(RapidSmsDBConstants.Message.RECEIVE_TIME, Message.SQLDateFormatter.format(now)); // profile
 																											// has
 																											// shown
 																											// this
 																											// is
 																											// an
 																											// expensive
 																											// operation
 		// messageValues.put(RapidSmsDBConstants.Message.RECEIVE_TIME,
 		// now.getTime()); //but this doesn't fracking work to convert to a
 		// datetime value.
 		boolean successfulSave = false;
 		Uri msgUri = null;
 		try {
 			msgUri = context.getContentResolver().insert(writeMessageUri, messageValues);
 			successfulSave = true;
 		} catch (Exception ex) {
 
 		}
 
 		if (successfulSave) {
 			Intent broadcast = new Intent("org.rapidandroid.intents.SMS_SAVED");
 		
 			broadcast.putExtra("from", mesg.getOriginatingAddress());
 			broadcast.putExtra("body", mesg.getMessageBody());
 			broadcast.putExtra("msgid", Integer.valueOf(msgUri.getPathSegments().get(1)));
 			//DeleteSMSFromInbox(context, mesg);
 //			context.sendBroadcast(broadcast);
 			
 			// check for multi-part attributes & send SMSMULTI_SAVED
 			if (true) {
 				SagesPdu mesgAsPdu = MessageBodyParser.extractSegmentAsPdu(mesg.getMessageBody(), mesg.getOriginatingAddress());
 				if (mesgAsPdu == null) { // means didn't match multipart criteria
 					context.sendBroadcast(broadcast); //means it was a regular SMS but not multipart!
 					// return
 					// break?
 				} else { // this did match multipart criteria
 					// write into worktable
 					insertMessageToSagesWorkTable(context, mesgAsPdu, mesg, now, monitor, msgUri);
 				}
 
 				
 				mContext = context;
 					
 				timerThread = (timerThread == null) ? threadFactory() : timerThread;
 				if (!timerThread.isAlive()) {
 					Log.d("sages", timerThread.getState().toString());
 					lastTimeRun = new Date().getTime();
 					if (!timerThread.getState().equals(Thread.State.valueOf("NEW"))) {
 						timerThread = threadFactory();
 						timerThread.start();
 					} else {
 						timerThread.start();
 					}
 				}
 				
 //				Intent intentQueuePollTimer = new Intent(mContext, QueueAndPollService.class);
 //				intentQueuePollTimer.putExtra("timerMode", true);
 //				mContext.startService(intentQueuePollTimer);
 
 				
 			}
 		}
 	}
 
 	/**
 	 * @author SAGES/pokuam1
 	 * 
 	 * If SMS is determined to be a multipart sms, then it is written into the sages multisms worktable
 	 * 
 	 * @param context
 	 * @param pdu
 	 * @param mesg
 	 * @param date
 	 * @param monitor
 	 * @param msgUri1
 	 */
 	private void insertMessageToSagesWorkTable(Context context, SagesPdu pdu, SmsMessage mesg, Date date, Monitor monitor, Uri msgUri1) {
 		Uri writeMessageUri = RapidSmsDBConstants.MultiSmsWorktable.CONTENT_URI;
 
 		ContentValues messageValues = new ContentValues();
 		messageValues.put(RapidSmsDBConstants.MultiSmsWorktable.SEGMENT_NUMBER, pdu.getSegmentNumber());
 		messageValues.put(RapidSmsDBConstants.MultiSmsWorktable.TOTAL_SEGMENTS, pdu.getTotalSegments());
 		messageValues.put(RapidSmsDBConstants.MultiSmsWorktable.TX_ID, pdu.getTxId());
 		messageValues.put(RapidSmsDBConstants.MultiSmsWorktable.PAYLOAD, pdu.getPayload());
 		messageValues.put(RapidSmsDBConstants.MultiSmsWorktable.TX_TIMESTAMP, Message.SQLDateFormatter.format(date));
 		messageValues.put(RapidSmsDBConstants.MultiSmsWorktable.MONITOR_MSG_ID, Integer.valueOf(msgUri1.getPathSegments().get(1)));
 		
 		boolean successfulSave = false;
 		Uri msgUri = null;
 		try {
 			msgUri = context.getContentResolver().insert(writeMessageUri, messageValues);
 			successfulSave = true;
 			Intent intentQueuePoll = new Intent(context, QueueAndPollService.class);
 			intentQueuePoll.putExtra(RapidSmsDBConstants.MultiSmsWorktable.MONITOR_MSG_ID, monitor.getID());
 			intentQueuePoll.putExtra(RapidSmsDBConstants.MultiSmsWorktable.MONITOR_MSG_ID, Integer.valueOf(msgUri1.getPathSegments().get(1)));
 			intentQueuePoll.putExtra(RapidSmsDBConstants.Message.PHONE, mesg.getOriginatingAddress());
 			context.startService(intentQueuePoll);
 			
 		} catch (Exception ex) {
 			Log.d("SmsReceiver.insertMessageToSagesWorkTable", "Error writing into worktable: " + ex.getMessage());
 			healthTracker.logError("SmsReceiver.insertMessageToSagesWorkTable--Error writing into worktable: " + ex.getMessage());
 
 		}
 	}
 
 	private void DeleteSMSFromInbox(Context context, SmsMessage mesg) {
 		try {
 
 			StringBuilder sb = new StringBuilder();
 			sb.append("address='" + mesg.getOriginatingAddress() + "' AND ");
 			sb.append("body='" + mesg.getMessageBody() + "'");
 			// sb.append("time='" + mesg.getTimestamp() + "'"); //doesn't seem
 			// to be supported
 			Cursor c = context.getContentResolver().query(uriSms, null, sb.toString(), null, null);
 			c.moveToFirst();
 			// String id = c.getString(0);
 			int thread_id = c.getInt(1);
 			context.getContentResolver().delete(Uri.parse("content://sms/conversations/" + thread_id), null, null);
 			c.close();
 		} catch (Exception ex) {
 			// deletions don't work most of the time since the timing of the
 			// receipt and saving to the inbox
 			// makes it difficult to match up perfectly. the SMS might not be in
 			// the inbox yet when this receiver triggers!
 			Log.d("SmsReceiver", "Error deleting sms from inbox: " + ex.getMessage());
 		}
 	}
 
 	@Override
 	// source: http://www.devx.com/wireless/Article/39495/1954
 	public void onReceive(Context context, Intent intent) {
 		if (!intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {// {
 			Log.d("Test Intent","The intent was not a Telephony intent");
 			return;
 			
 		}
 		
 		healthTracker.logInfo(SagesEventType.SMS_RECEIVED);
 
 		SmsMessage msgs[] = getMessagesFromIntent(intent);
 
 		for (int i = 0; i < msgs.length; i++) {
 			String message = msgs[i].getDisplayMessageBody();
 
 			if (message != null && message.length() > 0) {
 				Log.d("MessageListener", message);
 				healthTracker.logDebug(SagesEventType.SMS_RECEIVED, "message= " + message);
 
 				// //Our trigger message must be generic and human redable
 				// because it will end up
 				// //In the SMS inbox of the phone.
 				// if(message.startsWith("dimagi"))
 				// {
 				// //DO SOMETHING
 				// }
 
 				insertMessageToContentProvider(context, msgs[i]);
 			}
 		}
 
 	}
 
 	// source: http://www.devx.com/wireless/Article/39495/1954
 	private SmsMessage[] getMessagesFromIntent(Intent intent) {
 		SmsMessage retMsgs[] = null;
 		Bundle bdl = intent.getExtras();
 		try {
 			Object pdus[] = (Object[]) bdl.get("pdus");
 			retMsgs = new SmsMessage[pdus.length];
 			for (int n = 0; n < pdus.length; n++) {
 				byte[] byteData = (byte[]) pdus[n];
 				retMsgs[n] = SmsMessage.createFromPdu(byteData);
 			}
 
 		} catch (Exception e) {
 			Log.e("GetMessages", "fail", e);
 		}
 		return retMsgs;
 	}
 
 }

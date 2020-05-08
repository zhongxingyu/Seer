 /*
  * Copyright () 2012 The Johns Hopkins University Applied Physics Laboratory.
  * All Rights Reserved.  
  */
 package org.rapidandroid.service;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.Vector;
 
 import org.apache.commons.lang.StringUtils;
 import org.rapidandroid.ApplicationGlobals;
 import org.rapidandroid.RapidAndroidApplication;
 import org.rapidandroid.SystemHealthTracking;
 import org.rapidandroid.SystemHealthTracking.SagesEventType;
 import org.rapidandroid.activity.CsvOutputScheduler;
 import org.rapidandroid.content.translation.MessageTranslator;
 import org.rapidandroid.content.translation.ParsedDataTranslator;
 import org.rapidandroid.data.RapidSmsDBConstants;
 import org.rapidandroid.data.controller.WorktableDataLayer;
 import org.rapidandroid.receiver.SmsParseReceiver;
 import org.rapidandroid.receiver.SmsReceiver;
 import org.rapidandroid.receiver.SmsReplyReceiver;
 import org.rapidsms.java.core.model.Form;
 import org.rapidsms.java.core.model.Monitor;
 import org.rapidsms.java.core.parser.IParseResult;
 import org.rapidsms.java.core.parser.service.ParsingService;
 
import edu.jhuapl.sages.mobile.lib.rapidandroid.Demodulator;
 import android.app.IntentService;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.telephony.SmsManager;
 import android.util.Log;
 
 /**
  * Service to queue pieces of multi-part sms and poll until all received prior to the 
  * configurable time threshold. After the time threshold is reached for any multi-part sms, then
  * incomplete pieces are deleted. A NACK is sent to the sender.
  *   
  * @author POKUAM1
  * @created Feb 7, 2012
  */
 public class QueueAndPollService extends IntentService {
 	private static SystemHealthTracking healthTracker = new SystemHealthTracking(QueueAndPollService.class);
 
 	private static String[] prefixes = null;
 	private static Form[] forms = null;
 	private static SmsManager smsManager = null;
 	public static boolean isDirty = true; //TODO: protect this better
 	public static boolean isTiming = false;
 	private static Map<Long,String> phoneLookupMap = null;
 	
 	public static final String t = QueueAndPollService.class.getSimpleName();
 	
 	/**
 	 * Constructor for {@link QueueAndPollService}
 	 */
 	public QueueAndPollService() {
 		// must call super with a supplied name
 		super("QueueAndPollService");
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.app.IntentService#onCreate()
 	 */
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		Log.i(t, "Service creating.");
 		SmsParseReceiver.initFormCache(super.getApplication());
 		prefixes = SmsParseReceiver.getPrefixes();
 		forms = SmsParseReceiver.getForms();
 		smsManager = SmsManager.getDefault();
 		phoneLookupMap = WorktableDataLayer.buildSenderPhonesLookupForTxIds(this, null);
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.app.Service#onLowMemory()
 	 */
 	@Override
 	public void onLowMemory() {
 		super.onLowMemory();
 		Log.e(t, "low memory point occurred. fyi.");
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
 	 */
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		
 		if (intent.getBooleanExtra("timerMode", false)){
 			healthTracker.logInfo(SagesEventType.MULTIPART_SMS, "QueueAndPollService cleanup timer.");
 
 			Log.d(t, "timer mode...calling cleanupTimer()....");
 			healthTracker.logDebug(SagesEventType.MULTIPART_SMS, "timer mode...calling cleanupTimer()....");
 
 			cleanupTimer();
 			return;
 		}
 		
 		healthTracker.logInfo(SagesEventType.MULTIPART_SMS, "QueueAndPollService normal mode.");
 
 		int monitor_msg_id = intent.getIntExtra(RapidSmsDBConstants.MultiSmsWorktable.MONITOR_MSG_ID, 0);
 		String current_sender_phone = intent.getStringExtra(RapidSmsDBConstants.Message.PHONE);
 		//String sender_phone = phoneLookupMap.get(key);
 		
 		Log.d(t, "normal mode running");
 		Map<String, List<Long>> statusMap = null;
 		Map<String, List<Long>> ttlMap = null;
 		List<Long> completed = null;
 		List<Long> incompleted = null;
 		// get status Map(complete, incomplete, bad)
 		//     and THEN
 		// get ttl Map(stale incomplete, live incomplete)
 		try {
 			// process completed ("complete & live", "complete & stale")
 			statusMap = WorktableDataLayer.categorizeCompleteVsIncomplete(this);
 			completed = statusMap.get(WorktableDataLayer.label_complete);
 			incompleted = statusMap.get(WorktableDataLayer.label_incomplete);
 			
 			// delete remaining stale records ("incomplete & stale", 
 			ttlMap = WorktableDataLayer.categorizeStaleVsLive(this, incompleted);
 			
 		} catch (Exception e) {
 			Log.e(t, "Unable to retrieve ttl statuses. Contact admin.");
 			healthTracker.logError(SagesEventType.MULTIPART_SMS, "QueueAndPollService-- error getting ttl status -- " + e.getMessage());
 
 			e.printStackTrace();
 			smsManager.sendTextMessage(current_sender_phone, null, "Receiver had trouble processing stale data. Contact admin.", null, null);
 		}
 		// Delete & NACK messages for incomplete and stale txIds
 		Log.d(t, "TTL results are in.");
 		String debugTTLStale = StringUtils.join(ttlMap.get(WorktableDataLayer.label_ttlStale), ",");
 		String debugTTLLive = StringUtils.join(ttlMap.get(WorktableDataLayer.label_ttlLive), ",");
 		
 		Log.d(t, "Stale tx_ids: " + debugTTLStale);
 		Log.d(t, "Live tx_ids: " + debugTTLLive);
 		
 		// TODO: pokuam1 ensure no collision on txIds from different senders.
 		phoneLookupMap = WorktableDataLayer.buildSenderPhonesLookupForTxIds(this, null);
 		String sender_phone = "";
 		WorktableDataLayer.beginTransaction(this);
 		Log.d("Q&P","BEGIN TX[1]");
 		try {
 			// Delete messages for incomplete and stale txIds
 			if (!ttlMap.get(WorktableDataLayer.label_ttlStale).isEmpty()) {
 				WorktableDataLayer.beginTransaction(this);
 				Log.d("Q&P","BEGIN TX[2]-deleteTxIds");
 
 				WorktableDataLayer.deleteTxIds(this, ttlMap.get(WorktableDataLayer.label_ttlStale));
 		
 				WorktableDataLayer.setTransactionSuccessful(); //endTransaction() is called in the finally branch
 				Log.d("Q&P","SET SUCCESS TX[2]-deleteTxIds");
 				WorktableDataLayer.endTransaction();
 				Log.d("Q&P","END TX[2]-deleteTxIds");
 				// Send NACK for all incomplete and stale txIds
 				int i = 1;
 				for (Long txId: ttlMap.get(WorktableDataLayer.label_ttlStale)){
 					sender_phone = phoneLookupMap.get(txId);
 					smsManager.sendTextMessage(sender_phone, null, i + "_NACK--txid=" + txId + " was stale", null, null);
 					i++;
 				}
 
 			}
 			
 			// If incomplete txIds are present, system is considered "dirty"
 			if (!incompleted.isEmpty()){ // got to check first, return on empty completeds.
 				isDirty = true;
 			} else {
 				isDirty = false;
 			}
 			
 			// If complete message txIds are empty, there is nothing to process. End db-transaction in finally block. 
 			if (completed.isEmpty()){
 				WorktableDataLayer.setTransactionSuccessful(); //endTransaction() is called in the finally branch
 				Log.d("Q&P","SET SUCCESS TX[1]");
 				Log.d(t, "no complete records, exiting.");
 				return;
 			}
 
 			Log.d(t, "For complete txIds: concat -> DeMod -> Parse&Val -> Nack/Ack");
 
 			Map<Long, String> concatMap = WorktableDataLayer.getConcatenatedMessagesForTxIds(this, statusMap.get(WorktableDataLayer.label_complete));
 			final Map<Long, String[]> demodedBlobs = Demodulator.deModulateBlobMap(concatMap);
 			
 			// Pass demodedBlobs to the Parsing & Validation steps 
 			Intent parseOutcome = null;
 			Intent broadcast = new Intent("android.provider.Telephony.SMS_RECEIVED");
 			Set<Form> outputCsvSet = new HashSet<Form>();
 
 			for (Entry<Long, String[]> allBlobs : demodedBlobs.entrySet()){
 				
 				String successfulFormsTxId = "";
 				Long txId = allBlobs.getKey();
 				String[] parseableBlobs = allBlobs.getValue();
 				
 //				WorktableDataLayer.beginTransaction(this);
 //				Log.d("Q&P","BEGIN TX[3]-demodedBlobs");
 				String sender_phone_for_blobs = phoneLookupMap.get(txId);
 				boolean badData = false;
 				for (String body: parseableBlobs){
 					badData = false;
 
 					broadcast.putExtra("body", body);
 					broadcast.putExtra("txId", txId);
 					broadcast.putExtra("from", sender_phone_for_blobs);
 					broadcast.putExtra(RapidSmsDBConstants.MultiSmsWorktable.MONITOR_MSG_ID, monitor_msg_id);
 					
 					parseOutcome = SmsParseUtility.parseOnCall(this, broadcast, this.forms, this.prefixes);
 					long rowOutcome = parseOutcome.getLongExtra("rowid", -1);
 					Form formToCsvOutput = (Form)parseOutcome.getSerializableExtra("form");
 
 					if (rowOutcome == -1){
 						badData = true;
 //						
 //						// throw ERROR SEND NACK MESSAGE FOR SURE! -- PARSE ON CALL SENDS THE NACK!!!
 //						if (ApplicationGlobals.doReplyOnFail()){
 //							smsManager.sendTextMessage(sender_phone, null, "NACK--txid=" + txId + " error in parsing", null, null);
 //						}
 //
 //						// delete problematic ones? OR JUST LET IT GET STALE???
 						// I THINK SHOULD DELETE O.W. WILL GET REPROCESSED EACH TIME!!!!
 						WorktableDataLayer.beginTransaction(this);
 						Log.d("Q&P","BEGIN TX[3]-deleteTxId(badData)");
 						WorktableDataLayer.deleteTxIds(this, Arrays.asList(new Long[]{txId}));
 						WorktableDataLayer.setTransactionSuccessful();
 						Log.d("Q&P","SET SUCCESS[3] TX-deleteTxId(badData)");
 						break;
 					} else {
 						if (formToCsvOutput != null) outputCsvSet.add(formToCsvOutput);
 					} 
 				}
 				
 				if (badData) {
 					WorktableDataLayer.endTransaction(); //setTransactionSuccessful()?
 					Log.d("Q&P","END TX[3]--deleteTxId(badData)");
 				} else {
 					
 					// Delete processed messages (for complete txIds) from multisms_worktable
 					if (!statusMap.get(WorktableDataLayer.label_complete).isEmpty()){
 						WorktableDataLayer.beginTransaction(this);
 						Log.d("Q&P","BEGIN TX--deleteTxIds(processed)");
 						
 						WorktableDataLayer.deleteTxIds(this, statusMap.get(WorktableDataLayer.label_complete));
 
 						// Commit changes to multisms_worktable
 						WorktableDataLayer.setTransactionSuccessful();
 						Log.d("Q&P","SET SUCCESS TX--deleteTxIds(processed)");
 						WorktableDataLayer.endTransaction();
 						Log.d("Q&P","END TX--deleteTxIds(processed)");
 					}	
 					
 
 					// Trigger CsvOutputService for formIds in the stack of processed txIds
 					String successfulForms = "";
 					for (Form form: outputCsvSet){
 						successfulForms += form.getPrefix() + " ";
 						int formId = form.getFormId();
 						String formPrefix = form.getPrefix();
 						Intent csvIntent = new Intent(this, CsvOutputService.class);
 						csvIntent.putExtra("formId", formId);
 						csvIntent.putExtra("formPrefix", formPrefix);
 						startService(csvIntent);
 					}
 					
 					successfulFormsTxId = successfulForms;
 				}
 					
 				if (!badData && ApplicationGlobals.doReplyOnParse()){
 					smsManager.sendTextMessage(sender_phone_for_blobs, null, txId + " was completely successful for: " + successfulFormsTxId, null, null);
 					healthTracker.logInfo(SagesEventType.MULTIPART_SMS_PARSE_SUCCESS, "QueueAndPollService-- SmsParseReceiver.");
 
 				}
 			}
 			
 			WorktableDataLayer.setTransactionSuccessful();
 			Log.d("Q&P","SET SUCCESS TX[1]");
 		} catch (Exception e){
 			e.printStackTrace();
 			Log.e(t, e.getMessage());
 			healthTracker.logError( SagesEventType.MULTIPART_SMS, t + " " + e.getMessage() + " -- and using the new System Health Tracking Log");
 			
 		} finally {
 			WorktableDataLayer.endTransaction();
 			Log.d("Q&P","END TX[1]");
 		}
 	}
 	
 	
 	protected void cleanupTimer(){
 		Map<String, List<Long>> statusMap = null;
 		Map<String, List<Long>> ttlMap = null;
 		List<Long> completed = null;
 		List<Long> incompleted = null;
 		
 		try {
 			ttlMap = WorktableDataLayer.categorizeStaleVsLive(this, null);
 			statusMap = WorktableDataLayer.categorizeCompleteVsIncomplete(this);
 			completed = statusMap.get(WorktableDataLayer.label_complete);
 			incompleted = statusMap.get(WorktableDataLayer.label_incomplete);
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 		
 		
 			// DELETE: incomplete and stale
 			
 			// DELETE: incomplete
 			if (!ttlMap.get(WorktableDataLayer.label_ttlStale).isEmpty()) {
 		
 			boolean isEnded = false;
 			boolean transactionExists = false;
 			try {
 					WorktableDataLayer.beginTransaction(this);
 					transactionExists=true;
 					WorktableDataLayer.deleteTxIds(this, ttlMap.get(WorktableDataLayer.label_ttlStale));
 					//WorktableDataLayer.deleteStaleIncompleteTxIds(this, ttlMap.get(WorktableDataLayer.label_ttlStale));
 					
 					//http://stackoverflow.com/questions/5036939/how-to-properly-use-yieldifcontendedsafely-in-an-android-multithreaded-applica
 					WorktableDataLayer.getDb().yieldIfContendedSafely();	
 					WorktableDataLayer.setTransactionSuccessful();
 					WorktableDataLayer.endTransaction();
 					isEnded = true;
 					
 					int i = 1;
 					SmsManager smsManager = SmsManager.getDefault();
 					for (Long txId: ttlMap.get(WorktableDataLayer.label_ttlStale)){
 //						String sender_phone = "2404759981";
 						String sender_phone = phoneLookupMap.get(txId);
 						smsManager.sendTextMessage(sender_phone, null, i + "_NACK--txid=" + txId + " was stale", null, null);
 						i++;
 					}				
 		} catch(Exception e) {
 			Log.e(t, e.getMessage());
 			Thread.yield();
 		} finally {
 			if (!isEnded && transactionExists)WorktableDataLayer.endTransaction();
 		}
 	}else {
 		if (incompleted.isEmpty()) {
 			// used to end timer thread in SmsReceiver
 			QueueAndPollService.isDirty = false;
 		}
 	}
 	}
 
 /**
  * This is a modified version of the {@link SmsParseReceiver}, but stripped down to provide 
  * the functionality needed solely for the {@link QueueAndPollService}	
  * 
  * @author pokuam1
  * @created Apr 20, 2012
  */
 public static class SmsParseUtility {
 	private static SystemHealthTracking healthTracker = new SystemHealthTracking(SmsParseUtility.class);
 
 	private static String[] prefixes = null;
 	private static Form[] forms = null;
 	
 
 	public synchronized static void initFormCache(Form[] forms, String[] prefixes) {
 		setFormsAndPrefixes(forms, prefixes);
 //		forms = ModelTranslator.getAllForms();
 //		prefixes = new String[forms.length];
 //		for (int i = 0; i < forms.length; i++) {
 //			prefixes[i] = forms[i].getPrefix();
 //		}
 	}
 
 	public static Form determineForm(String message) {
 		int len = prefixes.length;
 		for (int i = 0; i < len; i++) {
 			String prefix = prefixes[i];
 			if (message.toLowerCase().trim().startsWith(prefix + " ")) {
 				return forms[i];
 			}
 		}
 		return null;
 	}
 
 	public static void setFormsAndPrefixes(Form[] tmpForms, String[] tmpPrefixes){
 		prefixes = tmpPrefixes;
 		forms = tmpForms;
 	}
 
 	/**
 	 * Upon message receipt, determine the form in question, then call the
 	 * corresponding parsing logic.
 	 * 
 	 * This is a modified version of the original onReceive() of {@link SmsReceiver}
 	 * 
 	 * @param context
 	 * @param intent
 	 * @param tmpForms
 	 * @param tmpPrefixes
 	 * @return Intent that is processed by {@link QueueAndPollService}
 	 */
 	public static Intent parseOnCall(Context context, Intent intent, Form[] tmpForms, String[] tmpPrefixes) {
 		ApplicationGlobals.initGlobals(context);
 		Intent outcome = new Intent();
 		long rowid = -1;
 		outcome.putExtra("rowid", rowid);
 		
 		Long txId = intent.getLongExtra("txId", 0);
 		if (prefixes == null) {
 			initFormCache(tmpForms, tmpPrefixes); // profiler shows us that this is being called
 								// frequently on new messages.
 		}
 		String sms_body = intent.getStringExtra("body");
 
 		if (sms_body.startsWith("notifications@dimagi.com /  / ")) {
 			sms_body = sms_body.replace("notifications@dimagi.com /  / ", "");
 			Log.d("SmsParseReceiver", "Debug, snipping out the email address");
 		}
 
 		int msgid = intent.getIntExtra(RapidSmsDBConstants.MultiSmsWorktable.MONITOR_MSG_ID, 0);
 
 		Form form = determineForm(sms_body);
 		if (form == null) {			
 			if (ApplicationGlobals.doReplyOnFail()) {
 				Intent broadcast = new Intent("org.rapidandroid.intents.SMS_REPLY");
 				broadcast.putExtra(SmsReplyReceiver.KEY_DESTINATION_PHONE, intent.getStringExtra("from"));
 				broadcast.putExtra(SmsReplyReceiver.KEY_MESSAGE, ApplicationGlobals.getParseFailText() + ", form is null from txId("+ txId +").");
 				context.sendBroadcast(broadcast);
 			}
 			outcome.putExtra("rowid", -1);
 			return outcome;
 		} else {
 			//TODO -- need to pull this out. it's a CRUD operation and will break things. wonder if can pass in db con?
 			if (false){
 				Monitor mon = MessageTranslator.GetMonitorAndInsertIfNew(context, intent.getStringExtra("from"));
 			}
 			// if(mon.getReplyPreference()) {
 			if (ApplicationGlobals.doReplyOnParseInProgress()) {
 				// for debug purposes, we'll just ack every time.
 				Intent broadcast = new Intent("org.rapidandroid.intents.SMS_REPLY");
 				broadcast.putExtra(SmsReplyReceiver.KEY_DESTINATION_PHONE, intent.getStringExtra("from"));
 				broadcast.putExtra(SmsReplyReceiver.KEY_MESSAGE, ApplicationGlobals.getParseInProgressText());
 				//context.sendBroadcast(broadcast);
 				outcome.putExtra("strategy_ReplyOnParseInProgress", broadcast);
 			
 
 			}
 			
 			SharedPreferences pref = context.getSharedPreferences(CsvOutputScheduler.sharedPreferenceFilename, Context.MODE_PRIVATE);
 			Vector<IParseResult> results = ParsingService.ParseMessage(form, sms_body);
 
 			// Parse success reply
 			if (results != null) {
 				if (ApplicationGlobals.doReplyOnParse() && results != null) {
 					// for debug purposes, we'll just ack every time.
 					Intent broadcast = new Intent("org.rapidandroid.intents.SMS_REPLY");
 					broadcast.putExtra(SmsReplyReceiver.KEY_DESTINATION_PHONE, intent.getStringExtra("from"));
 					broadcast.putExtra(SmsReplyReceiver.KEY_MESSAGE, ApplicationGlobals.getParseSuccessText());
 					if (false){context.sendBroadcast(broadcast);} // TODO SAGES/pokuam1: figure strategy for this. 
 				}
 			   	healthTracker.logInfo( SagesEventType.MULTIPART_SMS_PARSE_SUCCESS, t + " SmsParseReceiver.");
 			}
 
 			// Parse failure reply (if StrictParser was used, results would be null) 
 			if (results == null) {
 			   	healthTracker.logInfo( SagesEventType.MULTIPART_SMS_PARSE_FAIL, t + " SmsParseReceiver -- results of parse were null.");
 			   	 
 				if (ApplicationGlobals.doReplyOnFail()){
 					Intent broadcast = new Intent("org.rapidandroid.intents.SMS_REPLY");
 					broadcast.putExtra(SmsReplyReceiver.KEY_DESTINATION_PHONE, intent.getStringExtra("from"));
 					broadcast.putExtra(SmsReplyReceiver.KEY_MESSAGE, ApplicationGlobals.getParseFailText(form) + ", null results from txId("+ txId +").");
 					context.sendBroadcast(broadcast);
 				}
 				return outcome;
 			}
 			
 			if (false){ //TODO SAGES/pokuam1: delete when fixed. Determine whether WorktableDataLayer is suitable 
 				ParsedDataTranslator.InsertFormData(context, form, msgid, results);
 			}
 			
 			 rowid = WorktableDataLayer.InsertFormData(context, form, msgid, results);
 		   	 healthTracker.logInfo( SagesEventType.MULTIPART_SMS, t + " SmsParseReceiver.");
 	
 			 outcome.putExtra("rowid", rowid);
 			 outcome.putExtra("form", form);
 			 
 			 Log.d(t+".parseOnCall()", "Successful parse & insertion into "+ form.getPrefix() +"_formdata table.");
 
 			// Setup broadcast to trigger CsvOutputService for form
 			Intent broadcastStartCsvOutput = new Intent("org.rapidandroid.intents.SMS_REPLY_CSV_GO");
 			broadcastStartCsvOutput.putExtra("formId", form.getFormId());
 			broadcastStartCsvOutput.putExtra("formName", form.getFormName());
 			broadcastStartCsvOutput.putExtra("formPrefix", form.getPrefix());
 			
 			// TODO: SAGES/pokuam1 Decide whether to delete--doing it in the QueueAndPollService currently
 			// Send broadcast to trigger CsvOutputService 
 			boolean skip = true;
 			if (!skip){ 
 				context.sendBroadcast(broadcastStartCsvOutput);
 			}
 			
 			// Setup broadcast for the SMS Forwarding Activity //TODO: SAGES/pokuam1 Decide whether to delete
 			final int formId = form.getFormId();
 			boolean isFwdOn = pref.getBoolean(formId + CsvOutputScheduler.FORWARDING_VAR, false);
 			if (isFwdOn){
 				Intent broadcastForwardSMS= new Intent("org.rapidandroid.intents.SMS_FORWARD");
 				broadcastForwardSMS.putExtra("msg", sms_body);
 				broadcastForwardSMS.putExtra("forwardNums", pref.getString(form.getFormId() + CsvOutputScheduler.FORWARDING_NUMS, "").split(",")); 
 				context.sendBroadcast(broadcastForwardSMS);
 			}
 		}
 		return outcome;
 	}
   }	
 }

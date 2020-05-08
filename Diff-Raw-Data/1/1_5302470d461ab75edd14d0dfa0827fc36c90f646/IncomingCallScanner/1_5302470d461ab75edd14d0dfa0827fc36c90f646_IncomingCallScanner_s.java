 package org.acl.root;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.acl.root.core.BlackList;
 import org.acl.root.observers.CallObserver;
 import org.acl.root.observers.Logger;
 import org.acl.root.observers.UserNotifier;
 import org.acl.root.utils.CallInfo;
 import org.acl.root.utils.Contact;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.media.AudioManager;
 import android.os.Binder;
 import android.os.IBinder;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * This is the service responsible of capturing incoming calls and 
  * perform the required actions.
  * 
  * @author Francisco Prez-Sorrosal (fperez)
  *
  */
 public class IncomingCallScanner extends Service {
 
 	private static final String TAG = "IncomingCallScanner";
 
 	private boolean filterAllCalls = false;
 	
 	private AudioManager am;
 	private int currentAudioMode;
 	
 	/****************************************
 	 * Binder class
 	 ****************************************/
 	public class LocalBinder extends Binder {
 		IncomingCallScanner getService() {
 			return IncomingCallScanner.this;
 		}
 	}
 
 	private final IBinder mBinder = new LocalBinder();
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 	
 	private BroadcastReceiver phoneStateReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
 				String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
 				Log.d(TAG, "Call State=" + state);
 
 				if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
 					Log.d(TAG, "Idle");
 					am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
 				} else if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
 					String incomingNumber = intent
 							.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
 					Log.d(TAG, "Incoming call " + incomingNumber);
 					
 					if(filterAllCalls || BlackList.INSTANCE.containsContact(incomingNumber)) {
 						// Telephony actions
 						if (!killCall(context)) {
 							Log.e(TAG, "Unable to kill incoming call");
 						}
 						// Get relevant call info and notify observers
 						CallInfo.Builder callInfoBuilder = new CallInfo.Builder(getApplicationContext(), incomingNumber);
 						Contact contact = BlackList.INSTANCE.getContact(incomingNumber);
 						if (contact != null) {
 							callInfoBuilder.caller(contact.getName()).
 							emailAddresses(contact.getEmailAddresses());
 						}
 						notifyCallObservers(callInfoBuilder.build());
 						UserNotifier.INSTANCE.showCallScannerNotification(getApplicationContext(),
 								UserNotifier.CallScannerNotification.INCOMING_CALL);
 					} else {
 						am.setRingerMode(currentAudioMode);
 					}
 
 				} else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
 					Log.d(TAG, "Offhook");
 				}
 			} else if (intent.getAction().equals(
 					"android.intent.action.NEW_OUTGOING_CALL")) {
 				// Outgoing call: DO NOTHING at this time
 				// String outgoingNumber = intent
 				// .getStringExtra(Intent.EXTRA_PHONE_NUMBER);
 				// Log.d(TAG, "Outgoing call " + outgoingNumber);
 				// setResultData(null); // Kills the outgoing call
 			} else {
 				Log.e(TAG, "Unexpected intent.action=" + intent.getAction());
 			}
 		}
 	};
 
 	// ------------------------- Lifecycle -------------------------
 
 	@Override
 	public void onCreate() {
 
 		// Add the log and UserNotifier as default observers
 		addCallObserver(Logger.INSTANCE);
 		addCallObserver(UserNotifier.INSTANCE);
 		BlackList.INSTANCE.loadFromFile(getApplicationContext());
 		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		currentAudioMode = am.getRingerMode();
 		am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
 		IntentFilter filter = new IntentFilter();
 		filter.addAction("android.intent.action.PHONE_STATE");
 		filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
 		registerReceiver(phoneStateReceiver, filter);
 		Log.d(TAG, "Created");
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.d(TAG, "Received start id " + startId + ": " + intent);
 		UserNotifier.INSTANCE.showCallScannerNotification(getApplicationContext(),
 				UserNotifier.CallScannerNotification.INIT);
 		// Service will continue running (sticky) until it's explicitly stopped
 		return START_STICKY;
 	}
 
 	@Override
 	public void onDestroy() {
 		BlackList.INSTANCE.saveToFile(getApplicationContext());
 		// Stop filtering calls, otherwise they'll continue to be filtered
 		am.setRingerMode(currentAudioMode);
 		am = null;
 		unregisterReceiver(phoneStateReceiver);
 		removeCallObserver(Logger.INSTANCE);
 		removeCallObserver(UserNotifier.INSTANCE);
 		UserNotifier.INSTANCE.cancelCallScannerNotification(getApplicationContext());
 		Toast.makeText(getApplicationContext(), R.string.ics_service_stopped, Toast.LENGTH_SHORT).show();
 		Log.d(TAG, "Stopped");
 	}
 
 	// ------------------------ End Lifecycle ---------------------------------
 	
 	private boolean killCall(Context context) {
 		try {
 			// Get the boring old TelephonyManager
 			TelephonyManager telephonyManager = (TelephonyManager) context
 					.getSystemService(Context.TELEPHONY_SERVICE);
 
 			// Get the getITelephony() method
 			Class classTelephony = Class.forName(telephonyManager.getClass()
 					.getName());
 			Method methodGetITelephony = classTelephony
 					.getDeclaredMethod("getITelephony");
 
 			// Ignore that the method is supposed to be private
 			methodGetITelephony.setAccessible(true);
 
 			// Invoke getITelephony() to get the ITelephony interface
 			Object telephonyInterface = methodGetITelephony
 					.invoke(telephonyManager);
 
 			// Get the endCall method from ITelephony
 			Class telephonyInterfaceClass = Class.forName(telephonyInterface
 					.getClass().getName());
 			Method methodEndCall = telephonyInterfaceClass
 					.getDeclaredMethod("endCall");
 
 			// Invoke endCall()
 			methodEndCall.invoke(telephonyInterface);
 
 		} catch (Exception e) { // Many things can go wrong with reflection
 			Log.e(TAG, e.toString());
 			return false;
 		}
 		return true;
 	}
 
 	public void filterAllCalls(boolean decision) {
 		filterAllCalls = decision;
 	}
 	
 	public boolean isAllCallsFilterEnabled() {
 		return (filterAllCalls == true);
 	}
 	
 	/**
 	 * Observer pattern for call observers
 	 */
 	private List<CallObserver> callObservers =
 			new ArrayList<CallObserver>();
 	
 	public int nofObservers() {
 		return callObservers.size();
 	}
 	
 	public void addCallObserver(CallObserver observer) {
 		callObservers.add(observer);
 	}
 	
 	public void removeCallObserver(CallObserver observer) {
 		callObservers.remove(observer);
 	}
 
 	public boolean containsObserver(CallObserver observer) {
 		return (callObservers.contains(observer));
 	}
 	
 	private void notifyCallObservers(CallInfo callInfo) {
 		for(CallObserver observer : callObservers) {
 			observer.callNotification(callInfo);
 		}	
 	}
 	
 }
 

 package org.acl.root;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.acl.root.utils.InstrumentedConcurrentMap;
 
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.android.internal.telephony.ITelephony;
 
 /**
  * This is the service responsible of capturing incoming calls and 
  * perform the required actions.
  * 
  * @author Francisco Prez-Sorrosal (fperez)
  *
  */
 public class IncomingCallScanner extends Service {
 
 	private static final String TAG = "IncomingCallScanner";
 
 	private static final String FILE = "blacklist.txt";
 
 	private static final String JAVI_PHONE = "630445705";
 
 	private TelephonyManager tm;
 	private com.android.internal.telephony.ITelephony telephonyService;
 
 	private ConcurrentMap<String, String> blackList = new InstrumentedConcurrentMap<String, String>(new ConcurrentHashMap<String, String>());
 
 	private boolean serviceRunning = false;
 
 	public boolean isServiceRunning() {
 		return serviceRunning;
 	}
 
 	private Twitter twitter = null;
 
 	/****************************************
 	 * Binder class
 	 *
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
 
 	private PhoneStateListener phoneStateListener = new PhoneStateListener() {
 		@Override
 
 		public void onCallStateChanged(int state, String incomingNumber) {
 			super.onCallStateChanged(state, incomingNumber);
 
 			switch (state){
 			case TelephonyManager.CALL_STATE_RINGING:
 				Log.d(TAG, "ringing " + incomingNumber);
 				String plainPhoneNumber = incomingNumber.replaceAll("[\\s\\-()]", "");
 				if(blackList.containsKey(plainPhoneNumber)) {
 					// Telephony actions
 					try {
 						telephonyService.silenceRinger();
 						telephonyService.endCall();
 					} catch (RemoteException e) {
 						Log.e(TAG, "Can't access Telephony Service");
 						e.printStackTrace();
 					}
 					Calendar calendar = Calendar.getInstance();
 					// Get relevant call info
 					String name = blackList.get(plainPhoneNumber);
 					CharSequence text = 
 							(calendar.getTime().toString() // To avoid tweet discard 
 							+ " " 
 							+ getResources().getString(R.string.twitter_message_1)
 							+ " " + name + " " 
							+  getResources().getString(R.string.twitter_message_2));
 					// Log actions					
 					saveLogToFile(calendar.getTime().toString() + " " + name + " " + plainPhoneNumber+"\n");
 					// Twitter actions
 					if(isTwitterEnabled()) {
 						try {
 							Log.d(TAG, "Sending tweet...");
 							if(plainPhoneNumber.equals(JAVI_PHONE)) {
 								String message = getResources().getString(R.string.javi_twitter_message);
 								twitter.updateStatus(message);
 							} else {
 								twitter.updateStatus(text.toString());
 							}
 							Log.d(TAG, "Tweet sent");
 						} catch (TwitterException e) {
 							Log.e(TAG, "Can't send Tweet");
 							e.printStackTrace();
 						}
 					}
 					// Inform user
 					UserNotification.showToastWithImage(getApplicationContext(), text, R.drawable.app_icon);
 				}
 				break;
 
 			case TelephonyManager.CALL_STATE_IDLE:
 				Log.d(TAG, "idle");
 				break;
 
 			case TelephonyManager.CALL_STATE_OFFHOOK :
 				Log.d(TAG, "offhook");
 				break;
 			}
 		}
 	};
 
 	// ------------------------- Lifecycle -------------------------
 
 	@Override
 	public void onCreate() {
 
 		if(!serviceRunning) {
 			Log.d(TAG, "Loading map with blacklist...");
 			loadMapFromFile();
 			tm = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
 			tm.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
 			try {
 				telephonyService = getTelephonyService();
 			} catch (Exception e) {
 				Log.i(TAG, "Can't get Telephony Service");
 				e.printStackTrace();
 			}
 			serviceRunning = true;			
 			Log.d(TAG, "Created");
 		}
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.d(TAG, "Received start id " + startId + ": " + intent);
 		UserNotification.showNotification(getApplicationContext());
 		// We want this service to continue running until it is explicitly
 		// stopped, so return sticky.
 		return START_STICKY;
 	}
 
 
 	@Override
 	public void onDestroy() {
 		if(serviceRunning) {
 			saveMapToFile();
 			blackList.clear();
 			// Stop filtering calls, otherwise they'll continue to be filtered
 			tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE); 
 			tm = null;
 			UserNotification.cancelNotification(getApplicationContext(), UserNotification.SVC_STARTED_NOTIFICATION);
 			serviceRunning = false;
 			Log.d(TAG, "Stopped");
 			// Tell the user we stopped.
 			Toast.makeText(getApplicationContext(), R.string.ics_service_stopped, Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	// ------------------------- Lifecycle -------------------------
 
 	// ------------------- Black List Management -------------------
 
 	public ArrayList<CharSequence> getBlackList() {
 		return  new ArrayList<CharSequence>(blackList.values());
 	}
 
 	public String addContactToBlackList(Contact contact) {
 		String name = contact.getName();
 		String phone = contact.getPhoneNumbers().get(0);
 		String previousValue = blackList.putIfAbsent(phone, name);
 		Log.d(TAG, name + " " + phone + " added to Black List in service method");
 		return previousValue;
 	}
 
 	public String removeContactFromBlackList(String key) {
 		String previousValue = blackList.remove(key);
 		Log.d(TAG, previousValue + " removed from Black List in service method");
 		return previousValue;
 	}
 
 	// ------------------END Black List Management -----------------
 
 
 	// ------------------- Twitter Management -------------------
 
 	public void setTwitterConnection(Twitter newTwitter) {
 		Log.d(TAG, "New Twitter reference set");
 		twitter = newTwitter;
 	}
 
 	public boolean isTwitterEnabled() {
 		return (twitter != null);
 	}
 
 	public void discardTwitterConnection() {
 		twitter = null;
 	}
 
 	// ------------------END Twitter Management -----------------
 
 	@SuppressWarnings("unchecked")
 	private ITelephony getTelephonyService() throws Exception {
 		// Set up communication with the telephony service (thanks to Tedd's Droid Tools!)
 		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
 		Class c = Class.forName(tm.getClass().getName());
 		Method m = c.getDeclaredMethod("getITelephony");
 		m.setAccessible(true);
 		return (ITelephony)m.invoke(tm);
 	}
 
 	private void loadMapFromFile(){
 		FileInputStream fis = null;
 		InputStreamReader inputreader = null;
 		BufferedReader buffreader = null;
 		
 		try {
 			fis = openFileInput(FILE);
 			inputreader = new InputStreamReader(fis);
 			buffreader = new BufferedReader(inputreader);
 
 			String line;
 
 			// read every line of the file into the line-variable, on line at the time
 			while ((line = buffreader.readLine()) != null) {
 				// do something with the settings from the file
 				Log.d(TAG, "Reading " + line);
 				String [] data = line.split("-");	
 				blackList.putIfAbsent(data[0], data[1]);
 			}
 		} catch (FileNotFoundException e) { 
 			Toast.makeText(this, "File Still not created", Toast.LENGTH_SHORT).show();
 		} catch (IOException e) {
 			Toast.makeText(this, "Exception" + e.toString(), Toast.LENGTH_SHORT).show();
 		} finally {
 			try {
 				if(buffreader != null) buffreader.close();
 				if(inputreader != null) inputreader.close();
 				if(fis != null) fis.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 	private void saveMapToFile() {
 		FileOutputStream fos = null;
 		OutputStreamWriter outputwriter = null;
 		BufferedWriter buffwriter = null;
 
 		try {
 			fos = openFileOutput(FILE, Context.MODE_PRIVATE);
 
 			outputwriter = new OutputStreamWriter(fos);
 			buffwriter = new BufferedWriter(outputwriter);
 
 			for (Map.Entry<String, String> entry : blackList.entrySet()) {
 				String data = entry.getKey() + "-" + entry.getValue() + "\n";
 				Log.d(TAG, "Writting " + data);
 				buffwriter.write(data);
 			}
 			Toast.makeText(this, "Black List Saved", Toast.LENGTH_SHORT).show();
 		} catch (FileNotFoundException e) {
 			Toast.makeText(this, "FileNotFoundException" + e.toString(),
 					Toast.LENGTH_SHORT).show();
 		} catch (IOException e) {
 			Toast.makeText(this, "IOException" + e.toString(),
 					Toast.LENGTH_SHORT).show();
 		} finally {
 			try {
 				if (buffwriter != null) buffwriter.close();
 				if (outputwriter != null) outputwriter.close();
 				if (fos != null) fos.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	private void saveLogToFile(String data) {
 		FileOutputStream fos = null;
 		OutputStreamWriter outputwriter = null;
 		BufferedWriter buffwriter = null;
 
 		try {
 			fos = openFileOutput(ShowLogActivity.LOGFILE, 
 					Context.MODE_PRIVATE | Context.MODE_APPEND);
 
 			outputwriter = new OutputStreamWriter(fos);
 			buffwriter = new BufferedWriter(outputwriter);
 
 			buffwriter.append(data);
 			Toast.makeText(this, "Call saved in log", Toast.LENGTH_SHORT)
 					.show();
 		} catch (FileNotFoundException e) {
 			Toast.makeText(this, "FileNotFoundException" + e.toString(),
 					Toast.LENGTH_SHORT).show();
 		} catch (IOException e) {
 			Toast.makeText(this, "IOException" + e.toString(),
 					Toast.LENGTH_SHORT).show();
 		} finally {
 			try {
 				if (buffwriter != null) buffwriter.close();
 				if (outputwriter != null) outputwriter.close();
 				if (fos != null) fos.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
 

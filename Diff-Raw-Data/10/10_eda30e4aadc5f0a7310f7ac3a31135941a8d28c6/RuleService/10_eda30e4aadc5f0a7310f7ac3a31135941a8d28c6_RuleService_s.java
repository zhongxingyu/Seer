 package edu.stanford.mercury;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import net.holyc.HolyCIntent;
 
 import org.openflow.protocol.OFMatch;
 
 import edu.stanford.lal.permission.PermissionInquiry;
 import edu.stanford.lal.permission.PermissionResponse;
 import edu.stanford.mercury.activity.MercuryList;
 
 import com.google.gson.Gson;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.util.Log;
 
 public class RuleService extends Service {
 	
 	private String TAG = "RuleService";
 	private Gson mGson = new Gson();
 	private PermissionInquiry mInquiry;
 	private String mApplicationName;
 	private OFMatch mOFMatch;
 	private RuleDatabaseHelper mDatabaseHelper;
 	private ArrayList<Messenger> mClients = new ArrayList<Messenger>();
 	private NotificationManager mNotificationManager;
 	MercuryMessage mMercuryMessage = new MercuryMessage();	
 	
 	BroadcastReceiver mPermissionRequestReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {		
 			Log.i(TAG, "Received Broadcast Intent! " + intent);
 			if (intent.getAction().equals(HolyCIntent.LalPermInquiry.action)) {
 				String response_json = intent.getStringExtra(HolyCIntent.LalPermInquiry.str_key);
 				mInquiry = mGson.fromJson(response_json, PermissionInquiry.class);
 				mOFMatch = mInquiry.getOFMatch();
 				mApplicationName = mInquiry.getAppName();
 				Log.i(TAG, "App Name! " + mApplicationName);
 				checkDatabase();
 			} else if (intent.getAction().equals(MercuryMessage.Query.action))
 		    {
 				Log.i(TAG, "Received Database query!");
 				//Query request
 				String q_json = intent.getStringExtra(MercuryMessage.Query.str_key);
 				MercuryMessage.RuleQuery query = mGson.fromJson(q_json, MercuryMessage.RuleQuery.class);
 				Cursor c = mDatabaseHelper.query(
 									    "rules",
 									    query.columns,
 									    query.selection,
 									    query.selectionArgs,
 									    query.groupBy,
 									    query.having,
 									    query.orderBy);
 				//Read result
 				Vector r = new Vector();
 				c.moveToFirst();
 				
 				while (!c.isAfterLast())
 				{
 					Vector<String> v = new Vector<String>();
 					int count = c.getColumnCount();
 					for (int i = 0; i < count; i++)
 					    v.add(c.getString(i));
 					r.add(v);
 				    c.moveToNext();
 				}
 				
 				c.close();
 
 				//Create result
 				MercuryMessage.RuleResult result = mMercuryMessage.new RuleResult();
 				result.columns = query.columns;
 				result.results = r;
 				String result_str = mGson.toJson(result, MercuryMessage.RuleResult.class);
 
 				//Return result
 				Intent i = new Intent(MercuryMessage.Result.action);
 				i.setPackage(context.getPackageName());
 				i.putExtra(MercuryMessage.Result.str_key, result_str);
 				sendBroadcast(i);
 		    } else if (intent.getAction().equals(MercuryMessage.NetworkActionResult.action))
 		    {
 				Log.i(TAG, "Received Network Query Response!");
 				String networkAction = intent.getStringExtra(MercuryMessage.NetworkActionResult.str_key);
 				String response_json = intent.getStringExtra(HolyCIntent.LalPermInquiry.str_key);
 				PermissionInquiry nInquiry = mGson.fromJson(response_json, PermissionInquiry.class);
 				
 				OFMatch nOFMatch = nInquiry.getOFMatch();
 				
 				if (networkAction.equals("Allow")) {
 					// Add to firewall database
 					mDatabaseHelper.insertRow(mApplicationName, Integer.valueOf(nOFMatch.getDataLayerType()), 
 							Integer.valueOf(nOFMatch.getNetworkProtocol()), Integer.valueOf(nOFMatch.getTransportDestination()), 
 							Integer.valueOf(nOFMatch.getTransportSource()), Integer.valueOf(nOFMatch.getInputPort()), "allow");
 					// send result to controller with allow
 					sendFirewallResponse(true, nInquiry);
 				} else if (networkAction.equals("Deny")) {
 					// Add to firewall database
 					mDatabaseHelper.insertRow(mApplicationName, Integer.valueOf(nOFMatch.getDataLayerType()), 
 							Integer.valueOf(nOFMatch.getNetworkProtocol()), Integer.valueOf(nOFMatch.getTransportDestination()), 
 							Integer.valueOf(nOFMatch.getTransportSource()), Integer.valueOf(nOFMatch.getInputPort()), "deny");
 					// send result to controller with deny
 					sendFirewallResponse(false, nInquiry);
 				}
 
 		    }
 		}
 	};
 	
 	@Override
 	public IBinder onBind(Intent intent)
     {
 		return mMessenger.getBinder();
     }
 	
 	@Override
 	public void onDestroy()
     {
 		mDatabaseHelper.close();
 
 		unregisterReceiver(mPermissionRequestReceiver);
 
 		super.onDestroy();		
     }
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		
 		startForeground(0, null);
 		
 		IntentFilter mIntentFilter = new IntentFilter();
 		mIntentFilter.addAction(HolyCIntent.LalPermInquiry.action);
 		registerReceiver(mPermissionRequestReceiver, mIntentFilter);
 		
 		IntentFilter qIntentFilter = new IntentFilter();
 		qIntentFilter.addAction(MercuryMessage.Query.action);
 		registerReceiver(mPermissionRequestReceiver, qIntentFilter);
 		
 		IntentFilter nIntentFilter = new IntentFilter();
 		nIntentFilter.addAction(MercuryMessage.NetworkActionResult.action);
 		registerReceiver(mPermissionRequestReceiver, nIntentFilter);
 		
 		mDatabaseHelper = new RuleDatabaseHelper(this);
 		
 		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		
 		try {
 			mDatabaseHelper.createDatabase();
 		} catch (IOException e) {
 			Log.i(TAG, "IOException when creating database " + e);
 		}
 		
 		try {
       		mDatabaseHelper.openDatabase();
      	} catch(SQLException sqle) {
       		throw sqle;
       	}
 	}
 	
 	private void sendFirewallResponse(boolean response, PermissionInquiry inquiry) {
 		PermissionResponse mPermissionResponse = new PermissionResponse(inquiry.getOFMatch(), 
 				inquiry.getOFPacketIn(), response, inquiry.getSocketChannelNumber());
 		
 		Intent responseIntent = new Intent(HolyCIntent.LalPermResponse.action);
 		responseIntent.setPackage(getPackageName());
 		responseIntent.putExtra(HolyCIntent.LalPermResponse.str_key, mGson.toJson(mPermissionResponse, 
 				PermissionResponse.class));
 		sendBroadcast(responseIntent);
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.i(TAG, "Received start id " + startId + ": " + intent);
 		// We want this service to continue running until it is explicitly
 		// stopped, so return sticky.
 		return START_STICKY;
 	}
 	
 	/** Handler of incoming messages from (Activities). */
 	class IncomingHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MercuryMessage.REGISTER_CLIENT.type:
 				mClients.add(msg.replyTo);
 				break;
 			case MercuryMessage.UNREGISTER_CLIENT.type:
 				mClients.remove(msg.replyTo);
 				break;	
 			default:
 				super.handleMessage(msg);
 			}
 		}
 	}
 	
 	/**
 	 * Target we publish for clients to send messages to IncomingHandler.
 	 */
 	final Messenger mMessenger = new Messenger(new IncomingHandler());
 	
 	private void checkDatabase() {
 		//Query Database
 		Cursor mCursor = mDatabaseHelper.getDecision(mApplicationName);
 		
 		if (mCursor.getCount() == 0) {
 			Log.i(TAG, "No Database Match!!!!");
 			//Notify user (right now just add to test database)
 			//new NotificationTask().execute();
 			ruleNotification();
 		} else {
 			Log.i(TAG, "Database Match");
 			// Verify Match
 			mCursor.moveToFirst();
 			boolean action = false;
 			boolean endLoop = false;
 			while (!endLoop) {
 				if (mApplicationName.equals(mCursor.getString(1)) && 
 						mCursor.getString(2).equals(mOFMatch.getDataLayerType()) && 
 						mCursor.getString(3).equals(mOFMatch.getNetworkProtocol()) && 
 						mCursor.getString(4).equals(mOFMatch.getTransportDestination()) &&
 						mCursor.getString(5).equals(mOFMatch.getTransportSource()) && 
 						mCursor.getString(6).equals(mOFMatch.getInputPort())) {
 					// Check Action
 					if (mCursor.getString(7).equals("allow")) {
 						action = true;
 					} 
 				}
 				
				if (mCursor.isAfterLast() == false) {
					mCursor.moveToNext();
				} else {
					endLoop = true;
				}					
 			}
 			
 			sendFirewallResponse(action, mInquiry);
 		}
 		
 		mCursor.close();
 	}
 	
 	private void ruleNotification() {
 		
 		String mMessage = mApplicationName;
 		
 		Notification notification = new Notification(R.drawable.icon, getText(R.string.rule_miss),
 				System.currentTimeMillis());
 		Intent mIntent = new Intent(this, MercuryList.class);		
 		mIntent.putExtra(MercuryMessage.NetworkActionQuery.str_key, mGson.toJson(mInquiry, PermissionInquiry.class));		
 		PendingIntent contentIntent = PendingIntent.getActivity(this, mInquiry.getAppCookie(),
 				mIntent, 0);
 		notification.setLatestEventInfo(this,
 				getText(R.string.rule_miss), mMessage, contentIntent);
 		mNotificationManager.notify(mInquiry.getAppCookie(), notification);
 	}
 	
 	private class NotificationTask extends AsyncTask<Void, Void, Void> {
 
         protected Void doInBackground(Void... voids) {
         	String mMessage = mApplicationName;
     		
     		Notification notification = new Notification(R.drawable.icon, getText(R.string.rule_miss),
     				System.currentTimeMillis());
     		Intent mIntent = new Intent(getApplicationContext(), MercuryList.class);
     		mIntent.setData(Uri.parse(mApplicationName));
     		mIntent.putExtra(MercuryMessage.NetworkActionQuery.str_key, mGson.toJson(mInquiry, PermissionInquiry.class));		
     		PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), mInquiry.getAppCookie(),
     				mIntent, 0);
     		notification.setLatestEventInfo(getApplicationContext(),
     				getText(R.string.rule_miss), mMessage, contentIntent);
     		mNotificationManager.notify(R.string.rule_miss, notification);
             return null;
         }
 
         
         protected void onPostExecute(Void result) {
         	Log.i("OnPostExecute()", "");
         }
 	}
 	
 }

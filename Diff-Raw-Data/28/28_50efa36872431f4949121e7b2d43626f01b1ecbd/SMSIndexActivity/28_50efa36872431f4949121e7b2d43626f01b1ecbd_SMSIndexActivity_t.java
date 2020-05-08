 package sk.uniza.fri.activities;
 
 import sk.uniza.fri.R;
 import sk.uniza.fri.comp.SMSBase;
 import sk.uniza.fri.comp.SMSBroadcastReceiver;
 import sk.uniza.fri.comp.SMSSQLiteOpenHelper;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 public class SMSIndexActivity extends Activity {
	private static final boolean DEBUG = false;
 	private static final String TAG = SMSIndexActivity.class.getName();
 	private static boolean isAlreadyEnabled = false;
 
 	private SMSBase mDb;
 
 	private Cursor mCursorReceived;
 	private Cursor mCursorSent;
 
 	private SimpleCursorAdapter mAdapterReceived;
 	private SimpleCursorAdapter mAdapterSent;
 
 	final Handler mHandler = new Handler();
 	final Runnable mUpdater = new Runnable() {
 		public void run() {
 			mCursorReceived.requery();
 			mAdapterReceived.notifyDataSetChanged();
			if (DEBUG) Log.e(TAG, "Updated");
 
 			((TextView) findViewById(R.id.activity_smslist_tv_received_count))
 					.setText("" + mAdapterReceived.getCount());
 
 			mHandler.postDelayed(this, mUpdateInterval);
 		}
 	};
 
 	private SharedPreferences mPrefs;
 	private int mUpdateInterval = 10000;
 	private static final SMSBroadcastReceiver sbr = new SMSBroadcastReceiver();
 
 	private ListView mReceivedListView;
 	private ListView mSentListView;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_smslist);
 
 		mDb = new SMSBase(this);
 		mDb.open();
 
 		mCursorReceived = mDb.getAllSMSReceived();
 		startManagingCursor(mCursorReceived);
 
 		String[] columns = new String[] {
 				SMSSQLiteOpenHelper.K_SMS_RECEIVED_TEL,
 				SMSSQLiteOpenHelper.K_SMS_RECEIVED_TEXT,
 				SMSSQLiteOpenHelper.K_SMS_RECEIVED_TIME };
 
 		int[] to = new int[] { 
 				R.id.view_smslist_item_tel,
 				R.id.view_smslist_item_text, 
 				R.id.view_smslist_item_time };
 
 		mReceivedListView = (ListView) findViewById(R.id.activity_smslist_lv_received);
 		mSentListView = (ListView) findViewById(R.id.activity_smslist_lv_sent);
 
 		mAdapterReceived = new SimpleCursorAdapter(this,
 				R.layout.view_smslist_item, mCursorReceived, columns, to);
 		mReceivedListView.setAdapter(mAdapterReceived);
 
 		((TextView) findViewById(R.id.activity_smslist_tv_received_count))
 				.setText("" + mAdapterReceived.getCount());
 
 		// mCursorSent = mDb.getAllSMSSent()
 		//
 		// mAdapterSent = new SimpleCursorAdapter(this,
 		// R.layout.view_smslist_item,
 		// mCursorSent, columns, to);
 		// mReceivedListView.setAdapter(mAdapterSent);
 		// ((TextView) findViewById(R.id.activity_smslist_tv_sent_count))
 		// .setText("" + mAdapterSent.getCount());
 
 		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 	}
 
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_MENU) {
 			startActivity(new Intent(this, SettingsPreferenceActivity.class));
		} else if (keyCode == KeyEvent.KEYCODE_BACK)
			finish();
 
 		return true;
 	}
 
 	@Override
 	protected void onResume() {
 		mDb.open();
 		startManagingCursor(mCursorReceived);
 
 		boolean isEnabled = mPrefs.getBoolean(
 				SettingsPreferenceActivity.PREFERENCE_RECEIVER, true);
 		
 		String updateInterval = mPrefs.getString(
 				SettingsPreferenceActivity.PREFERENCE_UPDATE, "10000");
 		mUpdateInterval = Integer.valueOf(updateInterval);
 
 		String serverUrl = mPrefs.getString(
				SettingsPreferenceActivity.PREFERENCE_SERVER_URL, SettingsPreferenceActivity.SERVER_URL);
 		
 		mHandler.postDelayed(mUpdater, mUpdateInterval);
 
 		Log.e(TAG, "update interval: " + updateInterval);
 		Log.e(TAG, "server url: " + serverUrl);
 
 		if (isEnabled && !isAlreadyEnabled) {
 			IntentFilter filter = new IntentFilter();
 			filter.addAction("android.provider.Telephony.SMS_RECEIVED");
 
 			isAlreadyEnabled = true;
 			registerReceiver(sbr, filter);
 			Log.e(TAG, "Registered: " + isEnabled);
 		} else if (!isEnabled && isAlreadyEnabled) {
 
 			isAlreadyEnabled = false;
 			unregisterReceiver(sbr);
 			Log.e(TAG, "Unregistered: " + isEnabled);
 		}
 
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		mHandler.removeCallbacks(mUpdater);
 
 		stopManagingCursor(mCursorReceived);
 		mDb.close();
 		super.onPause();
 	}
	
	@Override
	protected void onDestroy() {
		
		unregisterReceiver(sbr);
		Log.e(TAG, "Closed & Unregistered");
		
		super.onDestroy();
	}
 }

 package edu.umich.eecs.gridwatch;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.content.LocalBroadcastManager;
 import android.view.Menu;
 import android.view.View;
 import android.webkit.URLUtil;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class GridWatch extends Activity {
 	TextView mStatus;
 	TextView mPendingCount;
 	EditText mAlertServerEditText;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_grid_watch);
 		
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 		String alertServer = settings.getString("alert_server",
 				getString(R.string.default_alert_server));
 		
 		mStatus = (TextView) findViewById(R.id.status_text);
 		mPendingCount = (TextView) findViewById(R.id.pending_count);
 		mAlertServerEditText = (EditText) findViewById(R.id.alert_server);
 		mAlertServerEditText.setHint(alertServer);
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		Intent intent = new Intent(this, GridWatchService.class);
 		startService(intent);
 		
 		LocalBroadcastManager.getInstance(this).registerReceiver(mServiceMessageReceiver, 
 				new IntentFilter("GridWatch-update-event"));
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		LocalBroadcastManager.getInstance(this).unregisterReceiver(mServiceMessageReceiver);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.grid_watch, menu);
 		return true;
 	}
 	
 	// This handles the callbacks from the service class.
 	private BroadcastReceiver mServiceMessageReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			mPendingCount.setText(" "+Integer.toString(intent.getIntExtra("pending_queue_len", 0)));
 			if (intent.hasExtra("event_info")) {
 				String status = "Most recent report:\n";
 				String event_info = intent.getStringExtra("event_info");
 				String[] info_items = event_info.split("\\|\\|");
 				for (String item : info_items) {
 					String[] kv = item.split("\\|");
 					status += kv[0] + ": " + kv[1] + "\n";
 				}
 				status += "\n";
 				status += "Transmission: " + intent.getStringExtra("event_transmission");
 				mStatus.setText(status);
 			} else {
 				mStatus.setText("No incidents reported since startup");
 			}
 		}
 	};
 	
 	@TargetApi(Build.VERSION_CODES.GINGERBREAD) public void setAlertServer(View view) {
 		String alertServer = mAlertServerEditText.getText().toString();
 		if (!URLUtil.isValidUrl(alertServer)) {
 			Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString("alert_server", alertServer);
 		editor.apply();
 		
 		mAlertServerEditText.setText("");
 		mAlertServerEditText.setHint(alertServer);
 		Toast.makeText(this, "Alert Server Updated", Toast.LENGTH_SHORT).show();
 	}
 	
 	@TargetApi(Build.VERSION_CODES.GINGERBREAD) public void resetAlertServer(View view) {
 		String alertServer = getString(R.string.default_alert_server);
 		
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString("alert_server", alertServer);
 		editor.apply();
 		
 		mAlertServerEditText.setText("");
 		mAlertServerEditText.setHint(alertServer);
 		Toast.makeText(this, "Alert Server Updated", Toast.LENGTH_SHORT).show();
 	}
 }

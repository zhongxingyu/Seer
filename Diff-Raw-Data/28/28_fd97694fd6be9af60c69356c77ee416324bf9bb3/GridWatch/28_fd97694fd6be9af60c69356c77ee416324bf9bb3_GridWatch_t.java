 package edu.umich.eecs.gridwatch;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Color;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.content.LocalBroadcastManager;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class GridWatch extends Activity {
 	private final static String INTENT_NAME = "GridWatch-update-event";
 	private final static String INTENT_EXTRA_EVENT_TYPE = "event_type";
 	private final static String INTENT_EXTRA_EVENT_INFO = "event_info";
 	private final static String INTENT_EXTRA_EVENT_TIME = "event_time";
 
	// This is the main page view
	View mMainView = null;
 
 	// This is the entire log view that we create so we can add log
 	// messages to it. Whenever the log needs to be displayed, call
 	// setContentView(mLogView);
 	View mLogView = null;
 
 
 	//TextView mPendingCount;
 	//EditText mAlertServerEditText;
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB) @Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
		LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

 		// Display the main homepage view and hide the back button
		mMainView = inflater.inflate(R.layout.activity_grid_watch, null);
		setContentView(mMainView);
 		getActionBar().setDisplayHomeAsUpEnabled(false);
 
 
 		//SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 		//String alertServer = settings.getString("alert_server",
 		//		getString(R.string.default_alert_server));
 
		//mStatus = (TextView) findViewById(R.id.txt_status);
 		//mPendingCount = (TextView) findViewById(R.id.pending_count);
 		//mAlertServerEditText = (EditText) findViewById(R.id.alert_server);
 		//mAlertServerEditText.setHint(alertServer);
 
 		// "Inflate" the log view so that we can append
 		// log messages to it. Also save a reference to it
 		// so we can display this copy of the log.

 		mLogView = inflater.inflate(R.layout.log, null);
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		Intent intent = new Intent(this, GridWatchService.class);
 		startService(intent);
 
 		LocalBroadcastManager.getInstance(this).registerReceiver(mServiceMessageReceiver,
 				new IntentFilter(INTENT_NAME));
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
 			//mPendingCount.setText(" "+Integer.toString(intent.getIntExtra("pending_queue_len", 0)));
 
 			// Append to the log
 
 			// Add the divider for each log item
 			LinearLayout log_linear_layout = (LinearLayout) mLogView.findViewById(R.id.log_linear);
 			View ruler = new View(context);
 			ruler.setBackgroundColor(Color.DKGRAY);
 			log_linear_layout.addView(ruler, 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
 			// Add the date timestamp
 			TextView text_log_time = new TextView(context);
 			text_log_time.setBackgroundColor(Color.WHITE);
 			text_log_time.setTextColor(Color.BLACK);
 			text_log_time.setText(intent.getStringExtra(INTENT_EXTRA_EVENT_TIME));
 			log_linear_layout.addView(text_log_time, 2);
 			// Add any information about what happened
 			TextView text_log_entry = new TextView(context);
 			text_log_entry.setBackgroundColor(Color.WHITE);
 			text_log_entry.setTextColor(Color.BLACK);
 			String log_entry = intent.getStringExtra(INTENT_EXTRA_EVENT_TYPE);
 			if (intent.hasExtra(INTENT_EXTRA_EVENT_INFO)) {
 				log_entry += " - " + intent.getStringExtra(INTENT_EXTRA_EVENT_INFO);
 			}
 			text_log_entry.setText(log_entry);
 			log_linear_layout.addView(text_log_entry, 3);
 
 
 			// Update the front display
 			if (intent.getStringExtra(INTENT_EXTRA_EVENT_TYPE) == "event_post") {
				((TextView) mMainView.findViewById(R.id.txt_status)).setText(intent.getStringExtra(INTENT_EXTRA_EVENT_INFO));
 			}
 /*
 
 			// Display the event info on the home screen of the app
 			// event_info looks like: key|value||key|value
 			if (intent.hasExtra("event_info")) {
 			//	String status = "Most recent report:\n";
 				String status = "";
 				String event_info = intent.getStringExtra("event_info");
 				String[] info_items = event_info.split("\\|\\|");
 				for (String item : info_items) {
 					String[] kv = item.split("\\|");
 					//status += kv[0] + ": " + kv[1] + "\n";
 					if (kv[0].equals("event_type")) {
 						if (kv[1].equals("plugged")) {
 							status = "Device was plugged in.";
 						} else if (kv[1].equals("unplugged_still")) {
 							status = "No movement, power outage!";
 						} else if (kv[1].equals("unplugged_moved")) {
 							status = "Device moved, everything is fine.";
 						}
 					}
 				}
 			//	status += "\n";
 			//	status += "Transmission: " + intent.getStringExtra("event_transmission");
 				mStatus.setText(status);
 			} else {
 				mStatus.setText(R.string.no_messages);
 			}*/
 		}
 	};
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB) @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 			// Handle when the log item is selected from the menu
 			case R.id.action_log:
 				// Display the log layout and put the back button in the header
 				setContentView(mLogView);
 				getActionBar().setDisplayHomeAsUpEnabled(true);
 				return true;
 
 			// Handle when the back button is pressed
 			case android.R.id.home:
 				// Display the homepage and removed the back icon in the header
				setContentView(mMainView);
 				getActionBar().setDisplayHomeAsUpEnabled(false);
 				return true;
			default:
				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/*
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
 	}*/
 	/*
 	@TargetApi(Build.VERSION_CODES.GINGERBREAD) public void resetAlertServer(View view) {
 		String alertServer = getString(R.string.default_alert_server);
 
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString("alert_server", alertServer);
 		editor.apply();
 
 		mAlertServerEditText.setText("");
 		mAlertServerEditText.setHint(alertServer);
 		Toast.makeText(this, "Alert Server Updated", Toast.LENGTH_SHORT).show();
 	}*/
 }

 package org.mockup.wvuta;
 
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PRTReportStatus extends Activity implements OnClickListener {
 	private SharedPreferences reportTracker;
 
 	private boolean upDown = false;
 	private boolean location = false;
 
 	private String statusString = null;
 	private String locString = null;
 	private static final String TAG = "WVUTA::PRTREPORTSTATUS";
 
 	private TextView reportText;
 	private ReportingReceiver receiver;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.prtreportstatus);
 
 		Log.d(TAG, "PRTReportStatus onCreate");
 
 		// initialize preferences
 		reportTracker = getSharedPreferences(Constants.TABLE_NAME,
 				Context.MODE_PRIVATE);
 
 		// set clicklisteners for all views in layout
 		reportText = (TextView) findViewById(R.id.PRTReportStatusLastReport);
 		View submit = findViewById(R.id.prtSubmitButton);
 		submit.setOnClickListener(this);
 
 		View downButton = findViewById(R.id.downRB);
 		View runningButton = findViewById(R.id.runningRB);
 
 		downButton.setOnClickListener(this);
 		runningButton.setOnClickListener(this);
 		Spinner locations = (Spinner) findViewById(R.id.location_spinner);
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 				this, R.array.location_spinner_options,
 				android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		locations.setAdapter(adapter);
 		locations.setOnItemSelectedListener(new SpinnerSelectedListener());
 
 		// set initial text display
 		setNewReportText();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		Log.d(TAG, "PRTReportStatus onResume");
 		// Setup receiver to know when ReportingService completes task
 		IntentFilter filter = new IntentFilter(ReportingService.REPORTING);
 		receiver = new ReportingReceiver();
 		registerReceiver(receiver, filter);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		Log.d(TAG, "PRTReportStatus onPause");
 		// unregister receiver when activity in background
 		unregisterReceiver(receiver);
 	}
 
 	private void setNewReportText() {
 		if (reportTracker.contains(Constants.TIME)) {
 			long lastTime = reportTracker.getLong(Constants.TIME, -1);
 			long deltaTime = System.currentTimeMillis() - lastTime;
 			int minutesAgo = (int) (deltaTime / 60 / 1000);
 			String newText = "unknown";
 			if (minutesAgo <= 0) {
 				newText = "Last report was 1 minute ago";
 			} else if (minutesAgo < 60 && minutesAgo > 0) {
 				newText = "Last report was " + (minutesAgo + 1)
 						+ " minutes ago";
 			} else {
 				newText = "Last report was greater than one hour ago";
 			}
 			reportText.setText(newText);
 		}
 	}
 
 	public void onClick(View v) {
 		Editor editor = reportTracker.edit();
 		switch (v.getId()) {
 		case R.id.downRB:
 			upDown = true;
 			statusString = "Down";
 			break;
 		case R.id.runningRB:
 			upDown = true;
 			statusString = "Up";
 			break;
 		case R.id.prtSubmitButton:
 			// status & location selected = accept
 			if (upDown && location) {
 				long currentTime = System.currentTimeMillis();
 				long deltaTime = currentTime
 						- (reportTracker.getLong(Constants.TIME, 0));
 				Calendar time = Calendar.getInstance();
 				// for use with limiting reports per hour
 				if (deltaTime < 300000) {
 					Toast toast = Toast.makeText(this,
 							R.string.submissionLimitExceededText,
 							Toast.LENGTH_SHORT);
 					toast.setGravity(Gravity.CENTER, 0, 0);
 					toast.show();
 				} else if (time.get(Calendar.HOUR_OF_DAY) > 21
 						|| time.get(Calendar.HOUR_OF_DAY) < 6
 						|| time.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
					Toast toast = Toast.makeText(this, "PRT currently closed",
 							Toast.LENGTH_SHORT);
 					toast.setGravity(Gravity.CENTER, 0, 0);
 					toast.show();
 				} else {
 					Log.d(TAG, "Submitting report");
 					editor.putLong(Constants.TIME, System.currentTimeMillis());
 					editor.putString(Constants.STATUS, statusString);
 					editor.putString(Constants.LOCATION, locString);
 					editor.commit();
 					reportText.setText("Submitting...");
 					sendReportToDB();
 				}
 				// one or none of status & location selected = deny
 			} else {
 				Toast toast = Toast.makeText(this, R.string.denySubmissionText,
 						Toast.LENGTH_SHORT);
 				toast.setGravity(Gravity.CENTER, 0, 0);
 				toast.show();
 			}
 			break;
 		}
 	}
 
 	private Intent serviceIntent;
 
 	public boolean sendReportToDB() {
 		// start Reporting Service with selected criteria
 		serviceIntent = new Intent(this, ReportingService.class);
 		startService(serviceIntent);
 		return true;
 	}
 
 	public void acceptToast() {
 		if (sendReportToDB()) {
 			Toast toast = Toast.makeText(this, R.string.acceptedSubmissionText,
 					Toast.LENGTH_SHORT);
 			toast.setGravity(Gravity.CENTER, 0, 0);
 			toast.show();
 		}
 	}
 
 	private class SpinnerSelectedListener implements OnItemSelectedListener {
 
 		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
 				long arg3) {
 			if (arg2 == 0) {
 				locString = null;
 				location = false;
 			} else {
 				locString = arg0.getItemAtPosition(arg2).toString();
 				location = true;
 			}
 		}
 
 		public void onNothingSelected(AdapterView<?> arg0) {
 		}
 
 	}
 
 	private class ReportingReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Log.d(TAG, "Received broadcast");
 			// update text
 			setNewReportText();
 
 			// create a toast to let user know submission was successful
 			acceptToast();
 		}
 
 	}
 }

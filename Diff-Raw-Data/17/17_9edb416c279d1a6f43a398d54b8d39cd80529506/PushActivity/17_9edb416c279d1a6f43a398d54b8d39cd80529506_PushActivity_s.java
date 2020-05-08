 package com.appcelerator.cloud.pushdemo;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.appcelerator.cloud.push.CCPushService;
 import com.appcelerator.cloud.push.DeviceTokenCallback;
 
 public class PushActivity extends Activity {
 	public final static String LOG_TAG = PushActivity.class.getName();
 	//TODO Update this line to your app_key
 	public final static String APP_KEY = "n3tYszgTYeM9lpydM7W5Wg3DdKBbWtsa";
 	
 	private String mDeviceID;
 	private Handler activityHandler = new Handler();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		// Get device token synchronously
 		// mDeviceID = CCPushService.getDeviceToken(getBaseContext(), APP_KEY);
 		// ((TextView) findViewById(R.id.target_text)).setText(mDeviceID);
 		
 		// Get device token asynchronously
		CCPushService.getDeviceTokenAsnyc(getBaseContext(), APP_KEY, new DeviceTokenCallback(){
 			public void receivedDeviceToken(final String deviceToken) {
 				activityHandler.post(new Runnable(){
 					public void run() {
 						mDeviceID = deviceToken;
 						((TextView) findViewById(R.id.target_text)).setText(mDeviceID);
 					}
 				});
 			}
 			public void failedReceiveDeviceToken(Throwable exception) {
 				Log.e(LOG_TAG, exception.getMessage());
 			}
 		});
 
 		// send a keep-alive signal
 		if (CCPushService.getInstance().ifEnabled(getBaseContext())) {
			CCPushService.getInstance().keepaliveService(getBaseContext());
 		}
 
 		final Button startButton = ((Button) findViewById(R.id.start_button));
 		final Button stopButton = ((Button) findViewById(R.id.stop_button));
 
 		startButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				PushNotificationsManager.startPush(getBaseContext());
 				startButton.setEnabled(false);
 				stopButton.setEnabled(true);
 			}
 		});
 		stopButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				PushNotificationsManager.stopPush(getApplicationContext());
 				startButton.setEnabled(true);
 				stopButton.setEnabled(false);
 			}
 		});
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		boolean enabled = CCPushService.getInstance().ifEnabled(getBaseContext());
 		((Button) findViewById(R.id.start_button)).setEnabled(!enabled);
 		((Button) findViewById(R.id.stop_button)).setEnabled(enabled);
 	}
 }

 package cmusv.mr.carbon;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import cmusv.mr.carbon.data.algorithm.DataAnalyst.DataType;
 import cmusv.mr.carbon.io.sendToServer.ClientHelper;
 import cmusv.mr.carbon.service.sensors.SensorLogService;
 import cmusv.mr.carbon.utils.ShareTools;
 import cmusv.mr.carbon.utils.SharepreferenceHelper;
 
 public class TrafficLog extends Activity {
 	private Button startButton;
 	private Button stopButton;
 	// private Spinner trafficSpinner;
 	// private String[] trafficModeList = { "walk", "bike", "car", "light rail"
 	// };
 	private String useChoice = null;
 	private BroadcastReceiver receiver;
 	private ImageView statusImage;
 	private ImageView bgImage;
 	private TextView statusText;
 	public static final String ACTION = "android.intent.action.cmusv.mr.carbon.dataTransmit";
 	private final String TAG = TrafficLog.class.getSimpleName();
 	private String movingStatus = "moving:false";
 	private String activityStatus = "dataType:unknown";
	private String activityLevelStatus = "activityLevel:0"; 
 	private SharepreferenceHelper preferenceHelper;
 	private ImageAnimation animation;
 	private WakeLock wakeLock;
 
 	@Override
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 
 		setContentView(R.layout.main);
 		SharedPreferences settings = getSharedPreferences("account",
 				MODE_PRIVATE);
 		preferenceHelper = new SharepreferenceHelper(settings);
 		String account = preferenceHelper.getUserAccount();
 		if (account == null) {
 			Intent i = new Intent(getApplicationContext(), LoginActivity.class);
 			startActivity(i);
 		}
 		// trafficSpinner = (Spinner) findViewById(R.id.position);
 		startButton = (Button) findViewById(R.id.startButton);
 		stopButton = (Button) findViewById(R.id.stopButton);
 		startButton.setOnClickListener(startClickListener);
 		stopButton.setOnClickListener(stopClickListener);
 		statusText = (TextView) findViewById(R.id.status_text);
 		// setPlaceAdaper();
 		serviceStateSetting();
 		if (!ShareTools.isSDCardExist()) {
 			Toast.makeText(this, "SD card is not mounted!", Toast.LENGTH_SHORT)
 					.show();
 			finish();
 		}
 
 		statusImage = (ImageView) findViewById(R.id.status_img);
 		bgImage = (ImageView) findViewById(R.id.bg_img);
 		animation = new ImageAnimation(this, bgImage);
 		animation.setAnimation(animation.bg, 1000);
 		/*
 		 * server upload file template
 		 * 
 		 * Thread t = new Thread(){
 		 * 
 		 * @Override public void run(){ try{
 		 * clientHelper.uploadFile("d35528c14af11f08881c8b924de396e4",
 		 * "/sdcard/MyTracks/csv/louis.csv"); } catch(Exception e){
 		 * e.printStackTrace(); } } }; t.start();
 		 */
 		setupBroadcastReceiver();
 		ShareTools.checkFilesToBeUpload(this);
 
 	}
 
 	public void onResume() {
 		acquireWakeLock();
 		super.onResume();
 	}
 
 	@Override
 	public void onPause() {
 		releaseWakeLock();
 		super.onPause();
 	}
 
 	@Override
 	public void onDestroy() {
 		if (receiver != null)
 			unregisterReceiver(receiver);
 		super.onDestroy();
 	}
 
 	private void acquireWakeLock() {
 		if (wakeLock == null) {
 
 			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 			wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this
 					.getClass().getCanonicalName());
 			wakeLock.acquire();
 		}
 
 	}
 
 	private void releaseWakeLock() {
 		if (wakeLock != null && wakeLock.isHeld()) {
 			wakeLock.release();
 			wakeLock = null;
 		}
 
 	}
 
 	private void setupBroadcastReceiver() {
 		receiver = new BroadcastReceiver() {
 
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				if (intent.hasExtra("isMoving")) {
 
 					boolean isMoving = intent
 							.getBooleanExtra("isMoving", false);
 					movingStatus = "ismoving:" + isMoving;
 					if (isMoving) {
 						animation.startAnimation();
 					} else {
 						animation.stopAnimation();
 					}
 
 				}
 				if (intent.hasExtra("dataType")) {
 					DataType dataType = (DataType) intent
 							.getSerializableExtra("dataType");
 
 					activityStatus = "dataType:" + dataType.toString();
 					switch (dataType) {
 					case WALKING:
 						statusImage.setImageResource(R.drawable.mrc_walk);
 						break;
 					case BIKING:
 						statusImage.setImageResource(R.drawable.mrc_bicycle);
 						break;
 					case TRAIN:
 						statusImage.setImageResource(R.drawable.mrc_train);
 						break;
 					case DRIVING:
 						statusImage.setImageResource(R.drawable.mrc_car);
 						break;
 					case ERROR:
 						// it should not happened...
 						Log.e(TAG, "error, wrong dataType");
 						break;
 					}
 
 				}
 				if (intent.hasExtra("activityLevel")) {
 					float activityLevel = intent.getFloatExtra("activityLevel",-1);
 					if(activityLevel<0){
 						activityLevelStatus = "activityLevel:"+"missing data";
 					}else{
 						activityLevelStatus = "activityLevel:"+activityLevel;	
 					}
 					
 				}
 
 				statusText.setText(movingStatus + "\n" + activityStatus + "\n" + activityLevelStatus);
 			}
 
 		};
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(ACTION);
 		registerReceiver(receiver, filter);
 	}
 
 	/*
 	 * private void setPlaceAdaper() { ArrayAdapter<String> positionAdapter =
 	 * new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
 	 * trafficModeList); positionAdapter
 	 * .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	 * trafficSpinner.setAdapter(positionAdapter); trafficSpinner
 	 * .setOnItemSelectedListener(new Spinner.OnItemSelectedListener() { public
 	 * void onItemSelected(AdapterView adapterView, View view, int position,
 	 * long id) { useChoice = trafficModeList[position]; }
 	 * 
 	 * public void onNothingSelected(AdapterView adapterView) {
 	 * 
 	 * } }); }
 	 */
 
 	private void serviceStateSetting() {
 		if (isMyServiceRunning()) {
 			startButton.setEnabled(false);
 			stopButton.setEnabled(true);
 		} else {
 			startButton.setEnabled(true);
 			stopButton.setEnabled(false);
 		}
 	}
 
 	private boolean isMyServiceRunning() {
 		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
 		for (RunningServiceInfo service : manager
 				.getRunningServices(Integer.MAX_VALUE)) {
 			if ("cmusv.mr.carbon.service.sensors.SensorLogService"
 					.equals(service.service.getClassName())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private Button.OnClickListener startClickListener = new Button.OnClickListener() {
 		public void onClick(View arg0) {
 
 			Bundle bundle = new Bundle();
 			bundle.putString("position", useChoice);
 			Intent intent = new Intent(TrafficLog.this, SensorLogService.class);
 			intent.putExtras(bundle);
 			startService(intent);
 			startButton.setEnabled(false);
 			stopButton.setEnabled(true);
 		}
 	};
 
 	private Button.OnClickListener stopClickListener = new Button.OnClickListener() {
 		public void onClick(View arg0) {
 
 			Intent intent = new Intent(TrafficLog.this, SensorLogService.class);
 			statusText.setText("Mr.carbon");
			movingStatus = "moving:false";
			activityStatus = "dataType:unknown";
			activityLevelStatus = "activityLevel:0";
 			animation.stopAnimation();
 			stopService(intent);
 			startButton.setEnabled(true);
 			stopButton.setEnabled(false);
 
 		}
 	};
 }

 package com.emdoor.autotest;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.GridLayout;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements OnClickListener {
 	protected static final String TAG = "MainActivity";
 	private WifiHelper mWifiHelper;
 	private boolean isTargetAPExist;
 	private boolean isTargetWifiConnected;
 	private LinearLayout progressLayout;
 	private LinearLayout operateLayout;
 	private RelativeLayout mainLayout;
 	private GridLayout buttonLayout;
 	//private Button button;
 	private TextView textStatus;
 	private Menu menu;
 	private ConnectivityManager cm;
 
 	public static final int COLOR_RED = R.drawable.red;
 	public static final int COLOR_GREEN = R.drawable.green;
 	public static final int COLOR_BLUE = R.drawable.blue;
 	public static final int COLOR_WHITE = R.drawable.white;
 	public static final int COLOR_BLACK = R.drawable.black;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		Settings.init(this);
 		progressLayout = (LinearLayout) findViewById(R.id.progress_panel);
 		operateLayout = (LinearLayout) findViewById(R.id.operate_panel);
 		mainLayout = (RelativeLayout) findViewById(R.id.layout_main);
 		buttonLayout = (GridLayout) findViewById(R.id.button_layout);
 		textStatus = (TextView) findViewById(R.id.statusText);
 
 		for (int i = 0; i < buttonLayout.getChildCount(); i++) {
 
 			Button button = (Button) buttonLayout.getChildAt(i);
 			button.setOnClickListener(this);
 			button.setTag(i);
 		}
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
 		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
 		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
 		filter.addAction(Intents.ACTION_FULLSCREEN_STATE_CHANGE);
 		filter.addAction(Intents.ACTION_TCP_CONNECT_STATE_CHANGE);
 		this.registerReceiver(wifiBroadcastReceiver, filter);
 		mWifiHelper = WifiHelper.getInstance(this);
 		cm = (ConnectivityManager) this
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 
 	}
 
 	@Override
 	protected void onResume() {
 		isTargetWifiConnected = mWifiHelper.isTargetWifiConnected();
 
 		progressLayout.setVisibility(isTargetWifiConnected ? View.GONE
 				: View.VISIBLE);
 		operateLayout.setVisibility(isTargetWifiConnected ? View.VISIBLE
 				: View.GONE);
 		if (!isTargetWifiConnected) {
 			this.connectWifi();
 		} else {
 			showButton();
 			updateButton();
 		}
 
 		super.onResume();
 	}
 
 	@Override
 	protected void onDestroy() {
 		this.unregisterReceiver(wifiBroadcastReceiver);
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		this.menu = menu;
 		this.menu.getItem(0).setVisible(AutoTestService.isConnected());
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_disconnect:
 			Dialog alertDialog = new AlertDialog.Builder(this)
 					.setTitle("Stop Test")
 					.setMessage("Are you sure to disconnect and stop test?")
 					.setIcon(R.drawable.ic_launcher)
 					.setNegativeButton("Cancel", null)
 					.setPositiveButton("OK",
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									AutoTestService
 											.disconnect(MainActivity.this);
 
 								}
 							}).create();
 			alertDialog.show();
 
 			break;
 
 		case R.id.menu_settings:
 			Intent intent = new Intent();
 			intent.setClass(this, SettingsActivity.class);
 			this.startActivity(intent);
 			break;
 		default:
 			break;
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	private void connectWifi() {
 
 		if (!mWifiHelper.isWifiEnabled()) {
 
 			mWifiHelper.turnOnWifi();
 			Log.d(TAG, "turn on wifi");
 			return;
 		}
 		isTargetWifiConnected = mWifiHelper.isTargetWifiConnected();
 		if (isTargetWifiConnected) {
 
 			return;
 		}
 		isTargetAPExist = mWifiHelper.isTargetAPExist();
 		Log.d(TAG, "isTargetAPExist:" + isTargetAPExist);
 
 		if (!isTargetAPExist) {
 			mWifiHelper.scanAPList();
 			return;
 		}
 		if (mWifiHelper.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
 
 			mWifiHelper.connectWifi();
 		}
 	}
 
 	private BroadcastReceiver wifiBroadcastReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent
 					.getAction())) {
 				isTargetAPExist = mWifiHelper.isTargetAPExist();
 				Log.d(TAG, "SCAN RESULTS AVAILABLE,isTargetAPExist:"
 						+ isTargetAPExist);
 				if (isTargetAPExist) {
 
 					isTargetWifiConnected = mWifiHelper.isTargetWifiConnected();
 					if (!isTargetWifiConnected) {
 						connectWifi();
 
 					}
 				}
 
 			} else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent
 					.getAction())) {
 
 				if (mWifiHelper.getWifiManager().isWifiEnabled()) {
 
 					isTargetAPExist = mWifiHelper.isTargetAPExist();
 					if (!isTargetAPExist) {
 						mWifiHelper.scanAPList();
 					}
 				}
 			} else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent
 					.getAction())) {
 
 				NetworkInfo wifi = cm
 						.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 
 				if (wifi != null && wifi.isConnected()) {
 					isTargetWifiConnected = mWifiHelper.isTargetWifiConnected();
 					Log.d(TAG, "WIFI_STATE_ENABLED,isTargetWifiConnected="
 							+ isTargetWifiConnected);
 					if (isTargetWifiConnected) {
 						showButton();
 					}
 				}
 			} else if (Intents.ACTION_FULLSCREEN_STATE_CHANGE.equals(intent
 					.getAction())) {
 
 				int color = intent.getIntExtra("background_color", COLOR_WHITE);
 				boolean fullScreen = intent.getBooleanExtra("full_screen",
 						false);
 				if (fullScreen) {
 					setBackgroundColor(color);
 					setFullScreen();
 				} else {
 					quitFullScreen();
 				}
 
 			} else if (Intents.ACTION_TCP_CONNECT_STATE_CHANGE.equals(intent
 					.getAction())) {
 				updateButton();
 				if (!AutoTestService.isConnected()) {
 					Intent service = new Intent();
 					service.setClass(MainActivity.this, AutoTestService.class);
 					stopService(service);
 				}
 			}else if(Intents.ACTION_WIFI_AP_CHANGE.equals(intent.getAction())){
 				onResume();
 				connectWifi();
 			}
 
 		}
 
 	};
 
 	private void updateButton() {
 		if (menu != null) {
 			menu.getItem(0).setVisible(AutoTestService.isConnected());
 		}
 		for (int i = 0; i < buttonLayout.getChildCount(); i++) {
 
 			boolean isCurrent=(i==Commands.deviceIndex);
 			Button button = (Button) buttonLayout.getChildAt(i);
 
 			
 			button.setEnabled(!AutoTestService.isConnected());
 			button.setBackgroundColor(AutoTestService.isConnected()&&isCurrent ? Color.BLUE
 					:Color.LTGRAY);
 			int buttonIndex =Integer.parseInt( button.getTag().toString())+1;
 			button.setText(AutoTestService.isConnected() &&isCurrent? "Testing No."
 					+ buttonIndex : "Start No."+buttonIndex);
 		}
 
 
 	}
 
 	private void showButton() {
 		operateLayout.setVisibility(View.VISIBLE);
 		progressLayout.setVisibility(View.GONE);
 		textStatus.setText("");
 		textStatus.append(getString(R.string.network) + Settings.getSSID());
 		textStatus.append("\n");
 		textStatus.append(getString(R.string.server) + Settings.getServerHost()
 				+ ":" + Settings.getPort());
 		updateButton();
 	}
 
 	boolean isFullScreen = false;
 
 	@Override
 	public void onClick(View v) {
 
 		Button button = (Button) v;
 		int deviceIndex =Integer.parseInt(button.getTag().toString());
 		
 		Commands.deviceIndex=deviceIndex;
 		Intent service = new Intent();
 		service.setClass(this, AutoTestService.class);
 		startService(service);
 		button.setText("Starting");
 		button.setEnabled(false);
 		for (int i = 0; i < buttonLayout.getChildCount(); i++) {
 
 			buttonLayout.getChildAt(i).setEnabled(false);
 
 		}
 		
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_DOWN) {
 			if (isFullScreen) {
 				quitFullScreen();
 			}
 		}
 		return super.onTouchEvent(event);
 	}
 
 	private void setBackgroundColor(int color) {
 		Drawable drawable = getResources().getDrawable(color);
 		this.getWindow().setBackgroundDrawable(drawable);
 	}
 
 	private void setFullScreen() {
 		isFullScreen = true;
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		mainLayout.setVisibility(View.GONE);
 		getActionBar().hide();
 	}
 
 	private void quitFullScreen() {
 		isFullScreen = false;
 		setBackgroundColor(COLOR_WHITE);
 		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
 
 		attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		getWindow().setAttributes(attrs);
 
 		getWindow()
 				.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
 		getActionBar().show();
 		mainLayout.setVisibility(View.VISIBLE);
 
 	}
 
 }

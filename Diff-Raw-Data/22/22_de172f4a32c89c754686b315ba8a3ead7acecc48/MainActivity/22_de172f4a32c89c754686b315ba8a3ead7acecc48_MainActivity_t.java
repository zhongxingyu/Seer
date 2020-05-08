 package com.spydiko.socialwifi;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Color;
 import android.net.wifi.WifiManager;
import android.os.Build;
 import android.os.Bundle;
 import android.text.InputType;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
 import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
 
 public class MainActivity extends Activity implements View.OnClickListener, PullToRefreshAttacher.OnRefreshListener {
 
 	private final static String TAG = MainActivity.class.getSimpleName();
 	private SocialWifi socialWifi;
 	//        private Button buttonScan;
 	private SimpleAdapter simpleAdapter;
 	private ArrayList<HashMap<String, String>> arrayList;
 	private HashMap<String, String> clickedWifi;
 	private ListView lv;
 	private String ITEM_KEY = "key", BSSID_KEY = "bssid", EXISTS_KEY = "exist", EXTRAS_KEY = "extra", IMAGE_KEY = "image", SIGNAL_KEY = "signal", ENTERPRISE_CONFIG_KEY = "enterprise";
 	private BroadcastReceiver broadcastReceiver;
 	private FileOutputStream outputStream;
 	private Context context;
 	private TextView usernameTextView, swipe2Refresh;
 	private CheckBox showPasswordCB;
 	private PullToRefreshAttacher mPullToRefreshAttacher;
 	private FileOutputStream outputStreamPy;
	private boolean priorThatICS;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		socialWifi = (SocialWifi) getApplication();
 		usernameTextView = (TextView) findViewById(R.id.usernameTextView);
 		usernameTextView.setOnClickListener(this);
 		swipe2Refresh = (TextView) findViewById(R.id.swipe2refresh);
 		if (!socialWifi.getSharedPreferenceBoolean("notFirstTime")) {
 			Intent intent = new Intent(this, LoginActivity.class);
 			startActivityForResult(intent, 1);
 		}
 
 		context = this;
 
 
 		usernameTextView.setText(socialWifi.getSharedPreferenceString("username") + " ");
 		// ----------- Read all the saved wifi --------------
 		File file = new File(getFilesDir(), "local.xml");
 		if (file.exists()) {
 			socialWifi.setWifies(socialWifi.readFromXML("local.xml"));
 
 			Log.d(TAG, "exists");
 		} else {
 			Log.d(TAG, "doesn't exist");
 		}
 		file = new File(getFilesDir(), "mine.xml");
 		if (file.exists()) {
 			socialWifi.setPyWifies(socialWifi.readFromXMLPython("mine.xml"));
 		} else {
 			Log.d(TAG, "doesn't exist");
 		}
 		// --------------------------------------------------
 		arrayList = new ArrayList<HashMap<String, String>>();
 		lv = (ListView) findViewById(R.id.list_scan);
 		simpleAdapter = new SimpleAdapter(this, arrayList, R.layout.row, new String[]{ITEM_KEY, IMAGE_KEY}, new int[]{R.id.list_value, R.id.signal}) {
 			@Override
 			public View getView(int position, View convertView, ViewGroup parent) {
 				View row = super.getView(position, convertView, parent);
 				ImageView right = (ImageView) row.findViewById(R.id.right);
 				right.setTag(position);
 				right.setOnClickListener(MainActivity.this);
 				TextView list_value = (TextView) row.findViewById(R.id.list_value);
 				list_value.setTag(position);
 				if (arrayList.get(position).get(EXISTS_KEY).equals("y")) {
 					list_value.setTextColor(Color.parseColor("#00FF00"));
 					list_value.setBackgroundResource(R.drawable.mybutton);
 					list_value.setOnClickListener(MainActivity.this);
 					right.setImageResource(android.R.drawable.ic_dialog_alert);
 				} else {
 					list_value.setTextColor(Color.parseColor("#FFFFFF"));
 					list_value.setBackgroundResource(android.R.color.transparent);
 					list_value.setOnClickListener(null);
 					right.setImageResource(android.R.drawable.ic_menu_upload);
 				}
 				return row;
 			}
 
 		};
 
 
 		broadcastReceiver = new
 
 				BroadcastReceiver() {
 					@Override
 					public void onReceive(Context context, Intent intent) {
 						swipe2Refresh.setVisibility(View.GONE);
 						arrayList.clear();
 						arrayList.addAll(WifiUtils.getScanResults(socialWifi.getWifiManager(), socialWifi.getWifies()));
 						if (arrayList.size() == 0) {
 							Log.d(TAG, "No Wifi networks found...");
 							Toast.makeText(context, "No Wifi networks found...", Toast.LENGTH_LONG).show();
 							swipe2Refresh.setVisibility(View.VISIBLE);
 							lv.setVisibility(View.GONE);
 						} else {
 							//							Log.d(TAG, "Broadcast setAdapter...Arraylist size: "+arrayList.size());
 							lv.setAdapter(simpleAdapter);
 							lv.setVisibility(View.VISIBLE);
 							simpleAdapter.notifyDataSetChanged();
							if (!priorThatICS) mPullToRefreshAttacher.setRefreshComplete();
 						}
 					}
 				};
 
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			priorThatICS = false;
			// Create a PullToRefreshAttacher instance
 		mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
 
 		// Retrieve the PullToRefreshLayout from the content view
 		PullToRefreshLayout ptrLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
 
 		// Give the PullToRefreshAttacher to the PullToRefreshLayout, along with a refresh listener.
 		ptrLayout.setPullToRefreshAttacher(mPullToRefreshAttacher, this);
		} else {
			priorThatICS = true;
		}
 
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		Log.d(TAG, "onPause");
 		try {
 			outputStream = openFileOutput("local.xml", Context.MODE_PRIVATE);
 			OutputStreamWriter osw = new OutputStreamWriter(outputStream);
 			outputStreamPy = openFileOutput("mine.xml", Context.MODE_PRIVATE);
 			OutputStreamWriter oswPy = new OutputStreamWriter(outputStreamPy);
 			socialWifi.createXMLString(osw, socialWifi.getWifies());
 			socialWifi.createXMLString(oswPy, socialWifi.getPyWifies());
 			Log.d(TAG, "create XML file");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		unregisterReceiver(broadcastReceiver);
 		socialWifi.savePreferences();
 
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		if (requestCode == 1) {
 
 			if (resultCode == RESULT_OK) {
 				socialWifi.setSharedPreferenceBoolean("notFirstTime", true);
 			}
 			if (resultCode == RESULT_CANCELED) {
 				finish();
 			}
 
 			usernameTextView.setText(socialWifi.getSharedPreferenceString("username") + " ");
 		}
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		registerReceiver(broadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
 		Log.d(TAG, "onResume");
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.itemToggleService: // Refresh from server
 				socialWifi.getLocation();
 				UpdateThread updateThread = new UpdateThread(this, socialWifi);
 				updateThread.execute();
 				break;
 			case R.id.preferences:
 				startActivity(new Intent(this, PrefsActivity.class));
 				break;
 			case R.id.map_pref:
 				startActivity(new Intent(this, Map.class));
 				break;
 			case R.id.logout:
 				socialWifi.logout();
 				Intent intent = new Intent(this, LoginActivity.class);
 				startActivityForResult(intent, 1);
 				break;
			case R.id.scan_for_wifi:
				socialWifi.getWifiManager().startScan();
				break;
 			//			case R.id.setOnWifi:
 			//				if (socialWifi.getBoot()) {
 			//					item.setChecked(false);
 			//					socialWifi.setBoot(false);
 			//					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
 			//						item.setIcon(android.R.drawable.button_onoff_indicator_off);
 			//					// if(AppSpecificOrientation.LOG) Log.d(TAG, "onBoot set to false");
 			//				} else {
 			//					item.setChecked(true);
 			//					socialWifi.setBoot(true);
 			//					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
 			//						item.setIcon(android.R.drawable.button_onoff_indicator_on);
 			//					// if(AppSpecificOrientation.LOG) Log.d(TAG, "onBoot set to true");
 			//				}
 			//
 
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
		if (priorThatICS) menu.findItem(R.id.scan_for_wifi).setVisible(true);

 
 		//		if (socialWifi.getBoot()) {
 		//			menu.findItem(R.id.setOnWifi).setChecked(true);
 		//			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
 		//				menu.findItem(R.id.setOnWifi).setIcon(android.R.drawable.button_onoff_indicator_on);
 		//		} else {
 		//			menu.findItem(R.id.setOnWifi).setChecked(false);
 		//			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
 		//				menu.findItem(R.id.setOnWifi).setIcon(android.R.drawable.button_onoff_indicator_off);
 		//		}
 		return true;
 	}
 
 	@Override
 	public void onRefreshStarted(View view) {
 		if (socialWifi.getWifiManager().isWifiEnabled() == false) {
 			Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
 			socialWifi.getWifiManager().setWifiEnabled(true);
 		}
 		socialWifi.getWifiManager().startScan();
 	}
 
 	@Override
 	public void onClick(View v) {
 
 
 		switch (v.getId()) {
 			case (R.id.usernameTextView):
 				showUserInfo();
 				return;
 
 		}
 
 
 		if (socialWifi.getWifiManager().isWifiEnabled() == false) {
 			Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
 			socialWifi.getWifiManager().setWifiEnabled(true);
 		}
 		switch (v.getId()) {
 			case (R.id.right):
 				clickedWifi = (HashMap<String, String>) simpleAdapter.getItem((Integer) v.getTag());
 				if (clickedWifi.get(EXISTS_KEY).equals("y")) {
 					Toast.makeText(this, "Error button clicked...\n" + clickedWifi.get(ITEM_KEY), Toast.LENGTH_SHORT).show();
 					reportNewPassword();
 				} else {
 					Toast.makeText(this, "Upload button clicked...\n" + clickedWifi.get(ITEM_KEY), Toast.LENGTH_SHORT).show();
 					uploadPassword();
 				}
 				break;
 			case (R.id.list_value):
 				clickedWifi = (HashMap<String, String>) simpleAdapter.getItem((Integer) v.getTag());
 				connectWithPassword();
 				break;
 			default:
 				break;
 		}
 	}
 
 	private void showUserInfo() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		LayoutInflater inflater = this.getLayoutInflater();
 		final View view = inflater.inflate(R.layout.user_info_dialog, null);
 		TextView usernameTextView = (TextView) view.findViewById(R.id.dialog_username);
 		usernameTextView.setText(socialWifi.getSharedPreferenceString("username"));
 		TextView numOfUploadsTextview = (TextView) view.findViewById(R.id.number_of_uploads_textview);
 		ProgressBar numOfUploadsProgressBar = (ProgressBar) view.findViewById(R.id.number_of_uploads_progressbar);
 		//		numOfUploadsProgressBar.setVisibility(View.VISIBLE);
 
 		builder.setView(view)
 				.setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						socialWifi.logout();
 						dialog.dismiss();
 						Intent intent = new Intent(context, LoginActivity.class);
 						startActivityForResult(intent, 1);
 					}
 				});
 
 		Dialog uploadDialog = builder.create();
 
 		uploadDialog.show();
 		UserInfoThread userInfoThread = new UserInfoThread(context, socialWifi, view);
 		userInfoThread.execute();
 
 	}
 
 	private void reportNewPassword() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		LayoutInflater inflater = this.getLayoutInflater();
 		final View view = inflater.inflate(R.layout.upload_dialog, null);
 		showPasswordCB = (CheckBox) view.findViewById(R.id.show_password);
 		showPasswordCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
 				EditText password = (EditText) view.findViewById(R.id.dialog_password);
 				if (b) {
 					password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
 				} else {
 					password.setInputType(129);
 				}
 			}
 		});
 		builder.setView(view)
 				.setPositiveButton(R.string.uploadPassword, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						EditText password = (EditText) view.findViewById(R.id.dialog_password);
 						socialWifi.getLocation();
 						ReportThread reportThread = new ReportThread(context, socialWifi, clickedWifi.get(ITEM_KEY), clickedWifi.get(BSSID_KEY), password.getText().toString(), clickedWifi.get(EXTRAS_KEY));
 						reportThread.execute();
 						dialog.dismiss();
 					}
 				})
 				.setNegativeButton(R.string.cancelUploadPassword, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.dismiss();
 					}
 				});
 		TextView ssid = (TextView) view.findViewById(R.id.dialog_ssid);
 		ssid.setText(clickedWifi.get(ITEM_KEY));
 		Dialog uploadDialog = builder.create();
 		uploadDialog.show();
 	}
 
 	private void connectWithPassword() {
 		String extraInfo = clickedWifi.get(EXTRAS_KEY);
 		String ssid = clickedWifi.get(ITEM_KEY);
 		String bssid = clickedWifi.get(BSSID_KEY);
 		String password = null;
 		for (WifiPass wifiPass : socialWifi.getWifies()) {
 			if (wifiPass.getSsid().equals(ssid)) {
 				if (wifiPass.getBssid().equals(bssid)) {
 					password = wifiPass.getPassword();
 				}
 			}
 		}
 		WifiUtils.removeNetwork(ssid, socialWifi.getWifiManager());
 		WifiUtils.connect(WifiUtils.createConfigFile(ssid, password, extraInfo), socialWifi.getWifiManager(), socialWifi.getConnectivityManager());
 	}
 
 	private void uploadPassword() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		LayoutInflater inflater = this.getLayoutInflater();
 		final View view = inflater.inflate(R.layout.upload_dialog, null);
 		showPasswordCB = (CheckBox) view.findViewById(R.id.show_password);
 		showPasswordCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
 				EditText password = (EditText) view.findViewById(R.id.dialog_password);
 				if (b) {
 					password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
 				} else {
 					password.setInputType(129);
 				}
 			}
 		});
 		builder.setView(view)
 				.setPositiveButton(R.string.uploadPassword, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						EditText password = (EditText) view.findViewById(R.id.dialog_password);
 						socialWifi.getLocation();
 						UploadPasswordThread uploadPasswordThread = new UploadPasswordThread(context, socialWifi, clickedWifi.get(ITEM_KEY), clickedWifi.get(BSSID_KEY), password.getText().toString(), clickedWifi.get(EXTRAS_KEY));
 						uploadPasswordThread.execute();
 						dialog.dismiss();
 					}
 				})
 				.setNegativeButton(R.string.cancelUploadPassword, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.dismiss();
 					}
 				});
 		TextView ssid = (TextView) view.findViewById(R.id.dialog_ssid);
 		ssid.setText(clickedWifi.get(ITEM_KEY));
 		Dialog uploadDialog = builder.create();
 		uploadDialog.show();
 	}
 
 }

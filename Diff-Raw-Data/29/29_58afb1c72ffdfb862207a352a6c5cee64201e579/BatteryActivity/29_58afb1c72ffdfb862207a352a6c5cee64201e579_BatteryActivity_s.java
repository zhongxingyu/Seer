 package com.example.solarpanelmanager;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.example.Constants;
 import com.example.bluetooth.Callback;
 import com.example.bluetooth.SetChargeConstraintsHandler;
 import com.example.bluetooth.SnapshotHandler;
 import com.example.bluetooth.ViewChargeConstraintsHandler;
 import com.example.solarpanelmanager.api.responses.BaseResponse;
 import com.example.solarpanelmanager.api.responses.SnapshotResponse;
 import com.example.solarpanelmanager.api.responses.ViewChargeConstraintsResponse;
 
 public class BatteryActivity extends SherlockActivity {
 
 	private int minVal;
 	private int maxVal;
 	private SeekBar min;
 	private SeekBar max;
 	private int level;
 	private View activityIndicator;
 	private TextView minvalue;
 	private TextView maxvalue;
 	private TextView snapshot;
 	private ImageView battery_image;
 	private BatteryLevel batteryLevel;
 	private double battery_voltage;
 	private double battery_current;
 	private double pvcurrent;
 	private double pvvoltage;
 	private long timestamp;
 	private volatile int apiCallsRunning;
 	private boolean dialogShowing = false;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_battery);
 
 		minVal = 0;
 		maxVal = 100;
 		level = 1;
 		battery_voltage = 0;
 		battery_current = 0;
 		pvcurrent = 0;
 		pvvoltage = 0;
 		timestamp = 0;
 		batteryLevel = new BatteryLevel(getApplicationContext(), BatteryLevel.SIZE_NOTIFICATION);
 
 		getUI();
 		setupUI();
 
 		getData();
 	}
 
 	private void getUI() {
 		activityIndicator = findViewById(R.id.activityIndicator);
 		minvalue = (TextView) findViewById(R.id.minView);
 		maxvalue = (TextView) findViewById(R.id.maxView);
 		snapshot = (TextView) findViewById(R.id.snapshot);
 		min = (SeekBar) findViewById(R.id.minbar);
 		max = (SeekBar) findViewById(R.id.maxbar);
 		battery_image = (ImageView) findViewById(R.id.currVoltage);
 	}
 
 	private void setupUI() {
 		min.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				// TODO Auto-generated method stub
				minvalue.setText(R.string.min +":" + progress);
 				minVal = progress;
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 				System.out.println("min: " + minVal + ", max: " + maxVal);
 				updateLevels();
 			}
 		});
 
 		max.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				// TODO Auto-generated method stub
				maxvalue.setText(R.string.max +":" + progress);
 				maxVal = progress;
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				// TODO Auto-generated method stub
 				System.out.println("min: " + minVal + ", max: " + maxVal);
 				updateLevels();
 			}
 		});
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (apiCallsRunning == 0) {
 			getData();
 		}
 	}
 
 	private void showUI() {
 		boolean powerUserEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
 				Constants.POWER_USER_PREFERENCE, false);
 
 		if (!powerUserEnabled) {
 			min.setVisibility(View.INVISIBLE);
 			max.setVisibility(View.INVISIBLE);
 			activityIndicator.setVisibility(View.GONE);
 			battery_image.setVisibility(View.VISIBLE);
 			snapshot.setVisibility(View.INVISIBLE);
 			maxvalue.setVisibility(View.INVISIBLE);
 			minvalue.setVisibility(View.INVISIBLE);
 		} else {
 			min.setVisibility(View.VISIBLE);
 			max.setVisibility(View.VISIBLE);
 			activityIndicator.setVisibility(View.GONE);
 			battery_image.setVisibility(View.VISIBLE);
 			snapshot.setVisibility(View.VISIBLE);
 			maxvalue.setVisibility(View.VISIBLE);
 			minvalue.setVisibility(View.VISIBLE);
 		}
 	}
 
 	private void hideUI() {
 		activityIndicator.setVisibility(View.VISIBLE);
 
 		min.setVisibility(View.INVISIBLE);
 		max.setVisibility(View.INVISIBLE);
 		battery_image.setVisibility(View.INVISIBLE);
 		snapshot.setVisibility(View.INVISIBLE);
 		maxvalue.setVisibility(View.INVISIBLE);
 		minvalue.setVisibility(View.INVISIBLE);
 	}
 
 	private void getData() {
 		String pass = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.PASS_PHRASE_PREFERENCE,
 				null);
 		String deviceId = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.CURRENT_DEVICE, null);
 		if (deviceId == null) {
 			// TODO - Tell the user that something is wrong
 		}
 
 		hideUI();
 		apiCallsRunning = 2;
 		// TODO use the deviceid when calling the handler
 		ViewChargeConstraintsHandler handler = new ViewChargeConstraintsHandler(
 				new Callback<ViewChargeConstraintsResponse>() {
 
 					@Override
 					public void onComplete(ViewChargeConstraintsResponse response) {
 						apiCallsRunning--;
 						if (apiCallsRunning == 0) {
 							showUI();
 						}
 
 						if (response.getResult() == 200) {
 							minVal = response.getMin();
 							maxVal = response.getMax();
 							System.out.println("start: " + minVal + ", " + maxVal);
 							min.setProgress(minVal);
 							min.refreshDrawableState();
 							max.setProgress(maxVal);
 							max.refreshDrawableState();
 						} else if (response.getResult() == 403) {
 							showForbiddenErrorDialog();
 						} else {
 							System.out.println("failure in communication");
 						}
 					}
 
 				}, deviceId, pass);
 		SnapshotHandler snapshotHandler = new SnapshotHandler(new Callback<SnapshotResponse>() {
 
 			@Override
 			public void onComplete(SnapshotResponse response) {
 				apiCallsRunning--;
 				if (apiCallsRunning == 0) {
 					showUI();
 				}
 
 				if (response.getResult() == 200) {
 					level = response.getBatteryPercent();
 					battery_voltage = response.getBatteryVoltage();
 					battery_current = response.getBatteryCurrent();
 					pvvoltage = response.getPVVoltage();
 					pvcurrent = response.getPVCurrent();
 					timestamp = response.getTimestamp();
 
 					batteryLevel.setLevel(level);
 					battery_image.setImageBitmap(batteryLevel.getBitmap());
 					// TODO Don't use raw strings here
					minvalue.setText(R.string.battery_min + ":" + minVal);
					maxvalue.setText(R.string.battery_max + ":"+ maxVal);
					snapshot.setText(R.string.battery_voltage+ ":" + battery_voltage + R.string.battery_curr+ ":" + battery_current
							+ "\n"+ R.string.PV_voltage + ":" + pvvoltage + R.string.PV_current + pvcurrent + "\n"+ R.string.Timestamp+ ":" + timestamp);
 					batteryLevel.setLevel(level);
 					battery_image.setImageBitmap(batteryLevel.getBitmap());
 				} else if (response.getResult() == 403) {
 					showForbiddenErrorDialog();
 				} else {
 					System.out.println("failure in communication");
 				}
 			}
 
 		}, deviceId, pass);
 
 		snapshotHandler.performAction();
 		handler.performAction();
 
 	}
 
 	private void updateLevels() {
 		String pass = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.PASS_PHRASE_PREFERENCE,
 				null);
 		String deviceId = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.CURRENT_DEVICE, null);
 		if (deviceId == null) {
 			// TODO - Tell the user that something is wrong
 		}
 
 		SetChargeConstraintsHandler call = new SetChargeConstraintsHandler(new Callback<BaseResponse>() {
 			@Override
 			public void onComplete(BaseResponse response) {
 				if (response.getResult() == 403) {
 					showForbiddenErrorDialog();
 				}
 				// System.out.println(response.getResult());
 			}
 		}, deviceId, pass, maxVal, minVal);
 		call.performAction();
 	}
 
 	private void showForbiddenErrorDialog() {
 		if (!dialogShowing) {
 			dialogShowing = true;
 			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
 					.setTitle(R.string.forbidden_error_title).setMessage(R.string.forbidden_error)
 					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							dialogShowing = false;
 						}
 
 					}).setOnCancelListener(new DialogInterface.OnCancelListener() {
 						@Override
 						public void onCancel(DialogInterface dialog) {
 							dialogShowing = false;
 						}
 					}).show();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.activity_battery_settings_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.menu_change_device) {
 			Intent intent = new Intent(this, ConnectActivity.class);
 			startActivity(intent);
 		} else if (item.getItemId() == R.id.menu_settings) {
 			Intent intent = new Intent(this, PreferencesActivity.class);
 			startActivity(intent);
 		} else if (item.getItemId() == R.id.menu_history) {
 			Intent intent = new Intent(this, MainActivity.class);
 			startActivity(intent);
 		}
 		return super.onOptionsItemSelected(item);
 	}
 }

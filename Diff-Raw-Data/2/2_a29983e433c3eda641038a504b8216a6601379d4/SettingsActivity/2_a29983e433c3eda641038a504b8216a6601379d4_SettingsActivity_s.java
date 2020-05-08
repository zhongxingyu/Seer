 package com.summaphoto;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import com.summaphoto.R;
 import Common.Constants;
 import Common.Tester;
 import Generator.AbstractTemplate;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TimePicker;
 
 public class SettingsActivity extends Activity {
 
 	private static final String TAG = SettingsActivity.class.getName();
 
 	// static final fields
 	public static final File ROOT = new File(Environment.getExternalStorageDirectory(), "DCIM");
 
 	// public static fields
 	public static Context CONTEXT = null;
 	public static int MODE = 0;
 	public static int COLLAGE_TYPE = 1;
 
 	// private fields
 	private RadioGroup modeGroup;
 	private RadioButton offRadioButton;
 	private RadioButton dailyRadioBtn;
 	private RadioButton smartRadioButton;
 	private RadioButton mapRadioButton;
 	private RadioButton blocksRadioButton;
 
 	private RadioButton lastCheckedButton;
 
 	private int pickerHour = -1;
 	private int pickerMin = -1;
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_settings);
 
 		CONTEXT = this;
 
 		createAppFolders();
		saveLogcatToFile();
 		
 		if (SmartModeFlow.lastCollageTime == -1) { // SmartModeFlow should have the app launch time at first
 			SmartModeFlow.lastCollageTime = new Date().getTime();
 		}
 
 		// getting radio buttons
 		modeGroup = (RadioGroup) findViewById(R.id.radioMode);
 		offRadioButton = (RadioButton) findViewById(R.id.radioOff);
 		smartRadioButton = (RadioButton) findViewById(R.id.radioSmart);
 		dailyRadioBtn = (RadioButton) findViewById(R.id.radioDaily);
 		mapRadioButton = (RadioButton) findViewById(R.id.radioMapType);
 		blocksRadioButton = (RadioButton) findViewById(R.id.radioBlocksType);
 
 		lastCheckedButton = offRadioButton;
 
 		if (savedInstanceState != null) {
 			onRestoreInstanceState(savedInstanceState);
 		}
 		
 		OnClickListener listener = new ScheduledModeListener(); // use same listener every time
 		dailyRadioBtn.setOnClickListener(listener);
 		
 
 	}
 
 
 
 	@Override
 	public void onBackPressed() {
 		// consume
 	}
 
 
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 
 		outState.putBoolean("off", offRadioButton.isChecked());
 		outState.putBoolean("smart", smartRadioButton.isChecked());
 		outState.putBoolean("daily", dailyRadioBtn.isChecked());
 		outState.putBoolean("map", mapRadioButton.isChecked());
 		outState.putBoolean("blocks", blocksRadioButton.isChecked());
 
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 
 		offRadioButton.setChecked(savedInstanceState.getBoolean("off"));
 		smartRadioButton.setChecked(savedInstanceState.getBoolean("smart"));
 		dailyRadioBtn.setChecked(savedInstanceState.getBoolean("daily"));
 		mapRadioButton.setChecked(savedInstanceState.getBoolean("map"));
 		blocksRadioButton.setChecked(savedInstanceState.getBoolean("blocks"));
 	}
 
 	/**
 	 * create necessary folders for app
 	 * @return
 	 */
 	private boolean createAppFolders() {
 
 		if (!Common.Utils.isExternalStorageWritable())
 			return false;
 
 		//create folders for app
 		File tmpFile = new File(Constants.APP_PHOTO_DIR);
 		if (!tmpFile.exists()) {
 			tmpFile.mkdirs();
 		}
 
 		tmpFile = new File(Constants.APP_TEMP_DIR);
 		if (!tmpFile.exists()) {
 			tmpFile.mkdirs();
 		}
 
 		File nomediaFile = new File(tmpFile, ".nomedia");
 		if (!nomediaFile.exists()) {
 			try {
 				nomediaFile.createNewFile();
 			} catch (IOException e) {
 				Log.e(TAG, "Could not create .nomedia file");
 			}
 		}
 
 		tmpFile = null;
 
 
 		return true;
 	}
 
 	/**
 	 * sends an intent to start running the PhotoListenerService
 	 */
 	private void startObserverService() {
 		// start camera folder observer 
 		Intent i= new Intent(this, PhotoListenerService.class);
 		i.putExtra("path", Constants.PHOTO_DIR);
 		//		i.putExtra("path", Constants.ROOT + File.separator + "Watched" + File.separator);
 		startService(i);
 	}
 
 	/**
 	 * creates and directs log to file
 	 */
 	public static void saveLogcatToFile() {    
 		String fileName =  "log.txt";
 		File outputFile = new File(Constants.APP_TEMP_DIR,fileName);
 		if (!outputFile.exists()) {
 			try {
 				outputFile.createNewFile();
 			} catch (IOException e1) {
 				Log.e(TAG, "Error when creating log file: "  + outputFile.getAbsolutePath());
 			}
 		}
 		try {
 			@SuppressWarnings("unused")
 			Process process = Runtime.getRuntime().exec("logcat -f "+outputFile.getAbsolutePath());
 		} catch (IOException e) {
 			Log.e(TAG, "Error when executing routing to log file: "  + outputFile.getAbsolutePath());
 		}
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.settings, menu);
 		return true;
 	}
 
 	public void onRadioButtonClicked(View view) {
 
 		lastCheckedButton = (RadioButton) view;
 		boolean checked = lastCheckedButton.isChecked();
 
 		// Check which radio button was clicked
 		switch(view.getId()) {
 		case R.id.radioSmart:
 			if (checked) {
 				smartButtonClicked();
 			}
 			break;
 
 		case R.id.radioOff: 
 			if (checked) {
 				offButtonClicked();
 			}
 		default: {
 
 		}
 		}
 	}
 
 	public void onTypeRadioButtonClicked(View view) {
 		lastCheckedButton = (RadioButton) view;
 		boolean checked = lastCheckedButton.isChecked();
 
 		// Check which radio button was clicked
 		switch(view.getId()) {
 		case R.id.radioMapType:
 			if (checked) {
 				mapTypeButtonClicked();
 			}
 			break;
 		case R.id.radioBlocksType:
 			if (checked) {
 				blocksTypeButtonClicked();
 			}
 			break;
 		default: {
 
 		}
 		}
 	}
 
 	/**
 	 * when off button is pressed, services need to be turned off
 	 */
 	private void offButtonClicked() {
 
 		MODE = 0;
 
 		// turn off active modes
 		if (PhotoListenerService.isObserving()) {
 			stopService(new Intent(this, PhotoListenerService.class));
 		}
 		if (SmartModeFlow.isFlowRunning()) {
 			turnOffSmartMode();
 		}
 		if (ScheduledModeService.isServiceRunning()) {
 			turnOffDailyMode();
 		}
 	}
 
 	private void dailyButtonClicked() {
 
 		if (!PhotoListenerService.isObserving()) {
 			startObserverService();
 		}
 
 		if (SmartModeFlow.isFlowRunning())
 			turnOffSmartMode();
 
 		MODE = 2;
 
 		Thread thread = new Thread() {
 
 			@Override
 			public void run() {
 				ScheduledModeService.startScheduledMode(SettingsActivity.this,
 						SettingsActivity.this.pickerHour, 
 						SettingsActivity.this.pickerMin);
 			}
 		};
 
 		thread.run();
 	}
 
 	/**
 	 * when smart button clicked, need to start observing and turn off daily mode
 	 */
 	private void smartButtonClicked() {
 
 		if (!PhotoListenerService.isObserving()) {
 			startObserverService();
 		}
 
 		turnOffDailyMode();
 
 		MODE = 1;
 
 		Thread thread = new Thread() {
 
 			@Override
 			public void run() {
 				if (!SmartModeFlow.isFlowRunning()) {
 					SmartModeFlow.startFlow(); 
 				}
 			}
 		};
 
 		thread.run();
 
 	}
 
 	private void mapTypeButtonClicked() {
 		COLLAGE_TYPE = AbstractTemplate.MAP_TYPE;
 	}
 
 	private void blocksTypeButtonClicked() {
 		COLLAGE_TYPE = AbstractTemplate.BLOCK_TYPE;
 	}
 
 	private void turnOffSmartMode() {
 		if (SmartModeFlow.isFlowRunning())
 			SmartModeFlow.stopService();
 	}
 
 	private void turnOffDailyMode() {
 		if (ScheduledModeService.isServiceRunning())
 			ScheduledModeService.stopService();
 	}
 
 	/**
 	 * listener for the daily mode button, opens up the time picker dialog
 	 * @author yonatan
 	 *
 	 */
 	private class ScheduledModeListener implements View.OnClickListener { 
 
 
 		@Override
 		public void onClick(View v) {
 
 			final TimePicker timePickerDialog = new TimePicker(v.getContext());
 			timePickerDialog.setIs24HourView(true);
 			if (pickerHour == -1 && pickerMin == -1) { // first time
 				timePickerDialog.setCurrentHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY ) + 1);
 				timePickerDialog.setCurrentMinute(0);
 			}
 			else {
 				timePickerDialog.setCurrentHour(pickerHour);
 				timePickerDialog.setCurrentMinute(pickerMin);
 			}
 
 			// creating AlertDialog because of no cancel button in TimePickerDialog
 			new AlertDialog.Builder(v.getContext())
 			.setTitle("Choose Time...")
 			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					SettingsActivity.this.pickerHour = timePickerDialog.getCurrentHour();
 					SettingsActivity.this.pickerMin = timePickerDialog.getCurrentMinute();
 					dailyButtonClicked();
 				}
 			})
 			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog,	int which) {
 					modeGroup.check(lastCheckedButton.getId());
 				}
 			}).setView(timePickerDialog).show();
 		}
 	}
 }

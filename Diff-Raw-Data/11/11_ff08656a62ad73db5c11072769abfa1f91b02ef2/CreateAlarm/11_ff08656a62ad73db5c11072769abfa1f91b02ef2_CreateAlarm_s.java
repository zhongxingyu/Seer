 /*
  * Copyright (C) 2012 Joakim Persson, Daniel Augurell, Adrian Bjugard, Andreas Rolen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.chalmers.dat255.group09.Alarmed.activity;
 
 import java.util.Calendar;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.TimePicker;
 import edu.chalmers.dat255.group09.Alarmed.R;
 import edu.chalmers.dat255.group09.Alarmed.constants.Constants;
 import edu.chalmers.dat255.group09.Alarmed.modules.factory.ModuleFactory;
 import edu.chalmers.dat255.group09.Alarmed.utils.AlarmUtils;
 import edu.chalmers.dat255.group09.Alarmed.utils.AudioHelper;
 
 /**
  * Activity to create a new alarm.
  * 
  * @author Daniel Augurell
  * @author Joakim Persson
  * @author Adrian Bjugrd
  * 
  */
 public class CreateAlarm extends Activity {
 	private static final int SET_ALARM_TONE = 999;
 
 	private AudioHelper hAudio;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			enableActionbarBackButton();
 		}
 
 		setContentView(R.layout.activity_create_alarm);
 		initTimePicker();
 		initTaskSpinner();
 		hAudio = new AudioHelper(this, getIntent());
 	}
 
 	/**
 	 * If the platform is Honeycomb or greater the ActionBarBackButton is
 	 * enabled.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void enableActionbarBackButton() {
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 
 	/**
 	 * Initiates the TimePicker as an 24h view and sets it to the current time.
 	 */
 	private void initTimePicker() {
 		TimePicker timePicker = (TimePicker) findViewById(R.id.createAlarmTimePicker);
 
 		timePicker.setIs24HourView(true);
 		setTimepickerToCurrentTime(timePicker);
 	}
 
 	/**
 	 * Sets the time of a TimePicker to the current time or the time of the
 	 * alarm to be edited.
 	 * 
 	 * @param timePicker
 	 *            The TimePicker to be changed
 	 */
 	private void setTimepickerToCurrentTime(TimePicker timePicker) {
 		Calendar calendar = Calendar.getInstance();
 		int hour = calendar.get(Calendar.HOUR_OF_DAY);
 		int minute = calendar.get(Calendar.MINUTE);
 		Intent intent = this.getIntent();
 		if (intent.getIntExtra(Constants.REQUESTCODE, -1) == Constants.EDIT_ALARM_REQUEST_CODE) {
 			hour = intent.getIntExtra(Constants.HOURS, hour);
 			minute = intent.getIntExtra(Constants.MINUTES, minute);
 		}
 		timePicker.setCurrentHour(hour);
 		timePicker.setCurrentMinute(minute);
 
 	}
 
 	/**
 	 * Initiates the TaskSpinner with all the modulenames.
 	 */
 	private void initTaskSpinner() {
 		Spinner spinner = (Spinner) findViewById(R.id.activity_create_alarm_task_spinner);
 		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
 				this, android.R.layout.simple_spinner_item,
 				ModuleFactory.getModuleNames());
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 		spinner.setAdapter(adapter);

 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			onBackPressed();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 
 	}
 
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 		overrideTransition();
 	}
 
 	/**
 	 * Opens the alarm tone selector.
 	 * 
 	 * @param view
 	 *            The parent View of the dialog
 	 */
 	public void onAlarmToneBtnPressed(View view) {
 		Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
 		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getResources()
 				.getString(R.string.select_alarm_tone));
 		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
 		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
 		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
 				RingtoneManager.TYPE_ALL);
 		this.startActivityForResult(intent, SET_ALARM_TONE);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (resultCode == RESULT_OK) {
 			Uri uri = data
 					.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
 			if (uri != null) {
 				String ringTonePath = uri.toString();
 				Log.d("Path", ringTonePath);
 				getIntent().putExtra(Constants.TONEURI, ringTonePath);
 			}
 		}
 	}
 
 	/**
 	 * Opens the volume and vibration control dialog.
 	 * 
 	 * @param view
 	 *            The view that has been pressed
 	 */
 	public void onVolumeBtnPressed(View view) {
 		hAudio.getVolumeDialog().show();
 	}
 
 	/**
 	 * Finishes the activity and return to the previous activity with the given
 	 * result.
 	 * 
 	 * @param view
 	 *            The button that was pressed.
 	 */
 	public void onSetAlarmBtnPressed(View view) {
 		TimePicker timePicker = (TimePicker) findViewById(R.id.createAlarmTimePicker);
 		Spinner spinner = (Spinner) findViewById(R.id.activity_create_alarm_task_spinner);
 
 		int hours = timePicker.getCurrentHour();
 		int minutes = timePicker.getCurrentMinute();
 
 		String module = (String) spinner.getSelectedItem();
 
 		Intent intent = getIntent();
 		intent.putExtra(Constants.HOURS, hours);
 		intent.putExtra(Constants.MINUTES, minutes);
 		intent.putExtra(Constants.MODULE, module);
 		intent.putExtra(Constants.DAYS, AlarmUtils
 				.getIntegerFromBooleanArray(intent
 						.getBooleanArrayExtra(Constants.DAYSOFWEEK)));
 
 		this.setResult(RESULT_OK, intent);
 		finish();
 		overrideTransition();
 	}
 
 	/**
 	 * Flashy transition between views.
 	 */
 	private void overrideTransition() {
 		int fadeIn = android.R.anim.fade_in;
 		int fadeOut = android.R.anim.fade_out;
 		overridePendingTransition(fadeIn, fadeOut);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		TimePicker timePicker = (TimePicker) findViewById(R.id.createAlarmTimePicker);
 		setTimepickerToCurrentTime(timePicker);
 	}
 
 	/**
 	 * Creates a dialog with a list of the days of the week. You can enable and
 	 * disable every day and the selected days will be the days that is
 	 * recurring.
 	 * 
 	 * @param view
 	 *            The button that is pressed.
 	 */
 	public void onDayOfWeekClick(View view) {
 		new AlertDialog.Builder(this)
 				.setTitle(R.string.recurring_alarms)
 				.setMultiChoiceItems(R.array.days_of_week,
 						getIntent().getBooleanArrayExtra(Constants.DAYSOFWEEK),
 						new MultiClickListener())
 				.setPositiveButton(R.string.ok_button,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int whichButton) {
 							}
 						}).create().show();
 
 	}
 
 	/**
 	 * A listener that changes the state of the selected days of the week.
 	 * 
 	 * @author Daniel Augurell
 	 * 
 	 */
 	private class MultiClickListener implements
 			DialogInterface.OnMultiChoiceClickListener {
 
 		@Override
 		public void onClick(DialogInterface dialog, int which, boolean isChecked) {
 			Intent intent = getIntent();
 			boolean[] daysOfWeek = intent
 					.getBooleanArrayExtra(Constants.DAYSOFWEEK);
 			daysOfWeek[which] = isChecked;
 
 		}
 
 	}
 
 }

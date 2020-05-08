 package com.tzachsolomon.spendingtracker;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import static com.tzachsolomon.spendingtracker.ClassCommonUtilities.*;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.RadioGroup;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.RelativeLayout;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 public class FragmentRemindersTime extends SherlockFragment implements
 		OnCheckedChangeListener, OnClickListener, OnSeekBarChangeListener {
 
 	public interface TimeReminderListener {
 		public void onAddTimeReminderClicked(
 				ArrayList<ClassTypeReminderTime> values);
 
 		public void onManagerReminderTimeClicked();
 	}
 
 	private RelativeLayout relativeLayoutDayCheckboxes;
 	private RadioGroup radioGroupReminders;
 	private EditText editTextDayInMonth;
 
 	private TimeReminderListener mButtonAddEntrySpentListener;
 	private Button buttonAddTimeReminder;
 	private Button buttonManageTimeReminderEntries;
 
 	private CheckBox checkBoxSunday;
 	private CheckBox checkBoxMonday;
 	private CheckBox checkBoxTuesday;
 	private CheckBox checkBoxWednesday;
 	private CheckBox checkBoxThursday;
 	private CheckBox checkBoxFriday;
 	private CheckBox checkBoxSaturday;
 	private SeekBar seekBarHours;
 	private SeekBar seekBarMinutes;
 	private TextView textViewHours;
 	private TextView textViewMinutes;
 	private SherlockFragmentActivity mActivity;
 	private BroadcastReceiver mTickReceiver;
 	private CheckBox checkBoxWorkingDays;
 	private SharedPreferences mSharedPreferences;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		//
 		super.onCreate(savedInstanceState);
 
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		//
 		super.onAttach(activity);
 		try {
 			mButtonAddEntrySpentListener = (TimeReminderListener) activity;
 		} catch (ClassCastException e) {
 			throw new ClassCastException(activity.toString()
 					+ " must implement TimeReminderListener ");
 		}
 
 		mActivity = (SherlockFragmentActivity) activity;
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		//
 		View view = inflater.inflate(R.layout.fragment_reminders_time, null);
 
 		initializeVariables(view);
 
 		return view;
 
 	}
 
 	private void initializeVariables(View view) {
 
 		mSharedPreferences = PreferenceManager
 				.getDefaultSharedPreferences(mActivity.getBaseContext());
 
 		relativeLayoutDayCheckboxes = (RelativeLayout) view
 				.findViewById(R.id.relativeLayoutDayCheckboxes);
 
 		radioGroupReminders = (RadioGroup) view
 				.findViewById(R.id.radioGroupReminders);
 
 		radioGroupReminders.setOnCheckedChangeListener(this);
 
 		editTextDayInMonth = (EditText) view
 				.findViewById(R.id.editTextDayInMonth);
 
 		buttonAddTimeReminder = (Button) view
 				.findViewById(R.id.buttonAddTimeReminder);
 		buttonAddTimeReminder.setOnClickListener(this);
 		buttonManageTimeReminderEntries = (Button) view
 				.findViewById(R.id.buttonManageTimeReminderEntries);
 		buttonManageTimeReminderEntries.setOnClickListener(this);
 
 		seekBarHours = (SeekBar) view.findViewById(R.id.seekBarHours);
 		seekBarMinutes = (SeekBar) view.findViewById(R.id.seekBarMinutes);
 
 		textViewHours = (TextView) view.findViewById(R.id.textViewHours);
 		textViewMinutes = (TextView) view.findViewById(R.id.textViewMinutes);
 
 		seekBarHours.setOnSeekBarChangeListener(this);
 		seekBarMinutes.setOnSeekBarChangeListener(this);
 
 		initTimePicker();
 
 		checkBoxSunday = (CheckBox) view.findViewById(R.id.checkBoxSunday);
 		checkBoxMonday = (CheckBox) view.findViewById(R.id.checkBoxMonday);
 		checkBoxTuesday = (CheckBox) view.findViewById(R.id.checkBoxTuesday);
 		checkBoxWednesday = (CheckBox) view
 				.findViewById(R.id.checkBoxWednesday);
 		checkBoxThursday = (CheckBox) view.findViewById(R.id.checkBoxThursday);
 		checkBoxFriday = (CheckBox) view.findViewById(R.id.checkBoxFriday);
 		checkBoxSaturday = (CheckBox) view.findViewById(R.id.checkBoxSaturday);
 
 		checkBoxWorkingDays = (CheckBox) view
 				.findViewById(R.id.checkBoxWorkingDays);
 
 		checkBoxWorkingDays.setOnClickListener(this);
 	}
 
 	@Override
 	public void onResume() {
 		//
 		super.onResume();
 		initTimePicker();
 	}
 
 	public void onCheckedChanged(RadioGroup group, int checkedId) {
 		//
 		switch (checkedId) {
 		case R.id.radioButtonEveryday:
 			relativeLayoutDayCheckboxes.setVisibility(View.GONE);
 
 			editTextDayInMonth.setVisibility(View.GONE);
 			break;
 
 		case R.id.radioButtonWeekly:
 			relativeLayoutDayCheckboxes.setVisibility(View.VISIBLE);
 			editTextDayInMonth.setVisibility(View.GONE);
 			setCheckAccordingToDate();
 
 			break;
 
 		case R.id.radioButtonMonthly:
 
 			relativeLayoutDayCheckboxes.setVisibility(View.GONE);
 			editTextDayInMonth.setVisibility(View.VISIBLE);
 			break;
 
 		default:
 			break;
 		}
 	}
 
 	private void setCheckAccordingToDate() {
 		//
 		Calendar cal = Calendar.getInstance();
 
 		switch (cal.get(Calendar.DAY_OF_WEEK)) {
 		case Calendar.SUNDAY:
 			checkBoxSunday.setChecked(true);
 			break;
 
 		case Calendar.MONDAY:
 			checkBoxMonday.setChecked(true);
 			break;
 
 		case Calendar.TUESDAY:
 			checkBoxTuesday.setChecked(true);
 			break;
 
 		case Calendar.WEDNESDAY:
 			checkBoxWednesday.setChecked(true);
 			break;
 
 		case Calendar.THURSDAY:
 			checkBoxThursday.setChecked(true);
 			break;
 
 		case Calendar.FRIDAY:
 			checkBoxFriday.setChecked(true);
 			break;
 
 		case Calendar.SATURDAY:
 			checkBoxSaturday.setChecked(true);
 			break;
 
 		default:
 			break;
 		}
 
 	}
 
 	private void initTimePicker() {
 
 		seekBarHours.setProgress(Calendar.getInstance().get(
 				Calendar.HOUR_OF_DAY));
 		seekBarMinutes.setProgress(Calendar.getInstance().get(Calendar.MINUTE));
 
 		setHourMinuteText(textViewMinutes, seekBarMinutes);
 		setHourMinuteText(textViewHours, seekBarHours);
 
 	}
 
 	@Override
 	public void setUserVisibleHint(boolean isVisibleToUser) {
 		//
 		super.setUserVisibleHint(isVisibleToUser);
 		if (isVisibleToUser) {
 
 			mTickReceiver = new BroadcastReceiver() {
 				public void onReceive(android.content.Context context,
 						Intent intent) {
 					final String action = intent.getAction();
 					if (action.contentEquals(Intent.ACTION_TIME_TICK)) {
 						initTimePicker();
 					}
 				}
 
 			};
 
 			IntentFilter filter = new IntentFilter();
 			filter.addAction(Intent.ACTION_TIME_CHANGED);
 			filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
 			filter.addAction(Intent.ACTION_TIME_TICK);
 
 			mActivity.registerReceiver(mTickReceiver, filter);
 
 		} else {
 			if (mTickReceiver != null) {
 				mActivity.unregisterReceiver(mTickReceiver);
 				mTickReceiver = null;
 			}
 
 		}
 	}
	
	@Override
	public void onDestroy() {
		// 
		super.onDestroy();
		if (mTickReceiver != null) {
			mActivity.unregisterReceiver(mTickReceiver);
			mTickReceiver = null;
		}
	}
 
 	public void onClick(View v) {
 		//
 		switch (v.getId()) {
 		case R.id.checkBoxWorkingDays:
 			checkBoxWorkingDays_Clicked();
 			break;
 		case R.id.buttonAddTimeReminder:
 			buttonAddTimeReminder_Clicked();
 			break;
 		case R.id.buttonManageTimeReminderEntries:
 			buttonManageTimeReminderEntries_Clicked();
 			break;
 		}
 
 	}
 
 	private void checkBoxWorkingDays_Clicked() {
 		//
 		if (checkBoxWorkingDays.isChecked()) {
 			checkBoxSunday.setChecked(mSharedPreferences.getBoolean(
 					"prefWorkdaySunday", false));
 			checkBoxMonday.setChecked(mSharedPreferences.getBoolean(
 					"prefWorkdayMonday", true));
 			checkBoxTuesday.setChecked(mSharedPreferences.getBoolean(
 					"prefWorkdayTuesday", true));
 			checkBoxWednesday.setChecked(mSharedPreferences.getBoolean(
 					"prefWorkdayWednesday", true));
 			checkBoxThursday.setChecked(mSharedPreferences.getBoolean(
 					"prefWorkdayThursday", true));
 			checkBoxFriday.setChecked(mSharedPreferences.getBoolean(
 					"prefWorkdayFriday", true));
 			checkBoxSaturday.setChecked(mSharedPreferences.getBoolean(
 					"prefWorkdaySaturday", false));
 		} else {
 			checkBoxFriday.setChecked(false);
 			checkBoxMonday.setChecked(false);
 			checkBoxSaturday.setChecked(false);
 			checkBoxSunday.setChecked(false);
 			checkBoxThursday.setChecked(false);
 			checkBoxTuesday.setChecked(false);
 			checkBoxWednesday.setChecked(false);
 		}
 
 	}
 
 	private void buttonManageTimeReminderEntries_Clicked() {
 		//
 		if (mButtonAddEntrySpentListener != null) {
 			mButtonAddEntrySpentListener.onManagerReminderTimeClicked();
 		}
 
 	}
 
 	private void buttonAddTimeReminder_Clicked() {
 		//
 
 		boolean valid = false;
 
 		if (mButtonAddEntrySpentListener != null) {
 			ArrayList<ClassTypeReminderTime> values = new ArrayList<ClassTypeReminderTime>();
 			String currentHour = String.valueOf(seekBarHours.getProgress());
 			String currentMinute = String.valueOf(seekBarMinutes.getProgress());
 
 			switch (radioGroupReminders.getCheckedRadioButtonId()) {
 			case R.id.radioButtonEveryday:
 				values.add(new ClassTypeReminderTime("-1",
 						TYPE_REMINDER_TIME_EVERYDAY, currentHour,
 						currentMinute, TYPE_REMINDER_TIME_DAY_DONT_CARE, "", ""));
 				valid = true;
 				break;
 
 			case R.id.radioButtonWeekly:
 				if (checkBoxSunday.isChecked()) {
 					values.add(new ClassTypeReminderTime("-1",
 							TYPE_REMINDER_TIME_WEEKLY, currentHour,
 							currentMinute, TYPE_REMINDER_TIME_SUNDAY, "", ""));
 					valid = true;
 
 				}
 				if (checkBoxMonday.isChecked()) {
 					values.add(new ClassTypeReminderTime("-1",
 							TYPE_REMINDER_TIME_WEEKLY, currentHour,
 							currentMinute, TYPE_REMINDER_TIME_MONDAY, "", ""));
 					valid = true;
 
 				}
 				if (checkBoxTuesday.isChecked()) {
 					values.add(new ClassTypeReminderTime("-1",
 							TYPE_REMINDER_TIME_WEEKLY, currentHour,
 							currentMinute, TYPE_REMINDER_TIME_TUESDAY, "", ""));
 					valid = true;
 				}
 				if (checkBoxWednesday.isChecked()) {
 					values.add(new ClassTypeReminderTime("-1",
 							TYPE_REMINDER_TIME_WEEKLY, currentHour,
 							currentMinute, TYPE_REMINDER_TIME_WEDNESDAY, "", ""));
 					valid = true;
 				}
 				if (checkBoxThursday.isChecked()) {
 					values.add(new ClassTypeReminderTime("-1",
 							TYPE_REMINDER_TIME_WEEKLY, currentHour,
 							currentMinute, TYPE_REMINDER_TIME_THURSDAY, "", ""));
 					valid = true;
 				}
 				if (checkBoxFriday.isChecked()) {
 					values.add(new ClassTypeReminderTime("-1",
 							TYPE_REMINDER_TIME_WEEKLY, currentHour,
 							currentMinute, TYPE_REMINDER_TIME_FRIDAY, "", ""));
 					valid = true;
 				}
 				if (checkBoxSaturday.isChecked()) {
 					values.add(new ClassTypeReminderTime("-1",
 							TYPE_REMINDER_TIME_WEEKLY, currentHour,
 							currentMinute, TYPE_REMINDER_TIME_SATURDAY, "", ""));
 					valid = true;
 				}
 
 				if (!valid) {
 					Toast.makeText(getSherlockActivity(),
 							"Make sure at least one day is checked",
 							Toast.LENGTH_LONG).show();
 				}
 
 				break;
 
 			case R.id.radioButtonMonthly:
 				if (editTextDayInMonth.getText().length() > 0) {
 					values.add(new ClassTypeReminderTime("-1",
 							TYPE_REMINDER_TIME_MONTHLY, currentHour,
 							currentMinute, editTextDayInMonth.getText()
 									.toString(), "", ""));
 					valid = true;
 				} else {
 					Toast.makeText(getSherlockActivity(),
 							"Please fill in day in month", Toast.LENGTH_LONG)
 							.show();
 				}
 
 				break;
 
 			default:
 				break;
 			}
 			if (valid) {
 				mButtonAddEntrySpentListener.onAddTimeReminderClicked(values);
 			}
 		}
 
 	}
 
 	public void setHourMinuteText(TextView textView, SeekBar seekBar) {
 		int value = seekBar.getProgress();
 		String strValue = Integer.toString(value);
 		if (value < 10) {
 			strValue = "0" + value;
 		}
 		textView.setText(strValue);
 	}
 
 	public void onProgressChanged(SeekBar seekBar, int progress,
 			boolean fromUser) {
 		//
 		switch (seekBar.getId()) {
 		case R.id.seekBarHours:
 			setHourMinuteText(textViewHours, seekBarHours);
 			break;
 		case R.id.seekBarMinutes:
 			setHourMinuteText(textViewMinutes, seekBarMinutes);
 			break;
 		}
 
 	}
 
 	public void onStartTrackingTouch(SeekBar seekBar) {
 		//
 		switch (seekBar.getId()) {
 
 		case R.id.seekBarHours:
 			Toast.makeText(mActivity, "Sets hour in time reminder",
 					Toast.LENGTH_SHORT).show();
 			break;
 		case R.id.seekBarMinutes:
 			Toast.makeText(mActivity, "Sets minutes in time reminder",
 					Toast.LENGTH_SHORT).show();
 			break;
 		}
 
 	}
 
 	public void onStopTrackingTouch(SeekBar seekBar) {
 		//
 
 	}
 
 }

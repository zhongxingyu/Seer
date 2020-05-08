 /*******************************************************************************
  * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
  * Copyright (C) 2012  WMG, University of Warwick
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You may obtain a copy of the GNU General Public License at  
  * <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     John Lawson - initial API and implementation
  ******************************************************************************/
 package uk.co.jwlawson.nof1.activities;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Random;
 
 import org.apache.commons.codec.binary.Hex;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.http.protocol.HTTP;
 
 import uk.co.jwlawson.nof1.BuildConfig;
 import uk.co.jwlawson.nof1.Keys;
 import uk.co.jwlawson.nof1.R;
 import uk.co.jwlawson.nof1.Scheduler;
 import uk.co.jwlawson.nof1.fragments.CheckArray;
 import uk.co.jwlawson.nof1.fragments.StartDate;
 import uk.co.jwlawson.nof1.fragments.TimeSetter;
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.backup.BackupManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.app.NavUtils;
 import android.support.v4.app.TaskStackBuilder;
 import android.util.Log;
 import android.view.ContextThemeWrapper;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 /**
  * Configuration activity that starts with a login for the doctor, then allows changes to treatment schedule and
  * questions.
  * 
  * @author John Lawson
  * 
  */
 public class DoctorConfig extends SherlockFragmentActivity implements AdapterView.OnItemSelectedListener, TextView.OnEditorActionListener {
 
 	private static final String TAG = "DoctorConfig";
 	private static final boolean DEBUG = true && BuildConfig.DEBUG;
 
 	private static final int REQUEST_FORM = 12;
 
 	private static final String SCHEDULE_FILE = "schedule.rtf";
 
 	/** EditText with doctor's email */
 	private EditText mDocEmail;
 
 	/** EditText with pharmacist's email */
 	private EditText mPharmEmail;
 
 	/** EditText with patient name */
 	private EditText mPatientName;
 
 	/** EditText with number of days in treatment period */
 	private EditText mPeriodLength;
 
 	/** Integer value of number of days in treatment period. Check whether mPeriodLength is visible before using this */
 	private int mIntPeriodLength;
 
 	/** EditText with number of treatment periods */
 	private EditText mPeriodNumber;
 
 	/** Integer value of number of treatment periods. Check whether mPeriodNumber is visible before using this */
 	private int mIntPeriodNumber;
 
 	/** True if the questionnaire form is built */
 	private boolean mFormBuilt;
 
 	/** Layout containing timescale config info */
 	private RelativeLayout mTimescaleLayout;
 
 	/** Most recently shown dialog. Kept so it can be closed if activity detroyed */
 	private Dialog mDialog;
 
 	private CheckArray mArray;
 
 	/** Backup manager instance */
 	private BackupManager mBackupManager;
 
 	/** True if want backup */
 	private boolean mBackup;
 
 	/** StartDate fragment instance */
 	private StartDate mDate;
 
 	/** EditText for TreatmentA entry */
 	private EditText mTreatmentA;
 
 	/** EditText for TreatmentB entry */
 	private EditText mTreatmentB;
 
 	/** EditText for any treatment notes */
 	private EditText mAnyNotes;
 
 	/** TimeSetter fragment */
 	private TimeSetter mTimeSetter;
 
 	/** EditText containing doctors name */
 	private EditText mDocName;
 
 	/** File handle for the saved schedule file */
 	private File mCache;
 
 	@TargetApi(8)
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.config_doctor);
 
 		SharedPreferences sp = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
 
 		mFormBuilt = sp.getBoolean(Keys.CONFIG_BUILT, false);
 
 		if (mFormBuilt) {
 			// If config is already filled in, allow user to go back
 			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 
 		Intent intent = getIntent();
 		String email = intent.getStringExtra(Keys.INTENT_EMAIL);
 
 		mDocEmail = (EditText) findViewById(R.id.config_doctor_details_edit_doc_email);
 		if (email != null && email.length() != 0) mDocEmail.setText(email);
 		if (DEBUG) mDocEmail.setText("doc@jwlawson.co.uk");
 
 		mPharmEmail = (EditText) findViewById(R.id.config_doctor_details_edit_pharm_email);
 		if (DEBUG) mPharmEmail.setText("test@jwlawson.co.uk");
 
 		mPatientName = (EditText) findViewById(R.id.config_doctor_details_edit_name);
 		if (sp.contains(Keys.CONFIG_PATIENT_NAME)) {
 			mPatientName.setText(sp.getString(Keys.CONFIG_PATIENT_NAME, ""));
 		}
 
 		mDocName = (EditText) findViewById(R.id.config_doctor_details_edit_doc_name);
 		if (sp.contains(Keys.CONFIG_DOCTOR_NAME)) {
 			mDocName.setText(sp.getString(Keys.CONFIG_DOCTOR_NAME, ""));
 		}
 
 		mPeriodLength = (EditText) findViewById(R.id.config_timescale_edit_period);
 		mPeriodLength.setOnEditorActionListener(this);
 
 		Spinner spinLength = (Spinner) findViewById(R.id.config_timescale_spinner_length);
 		spinLength.setOnItemSelectedListener(this);
 
 		int saved = sp.getInt(Keys.CONFIG_PERIOD_LENGTH, -1);
 		boolean flag = true;
 		// Go through spinner, if the value is in the spinner set to that
 		for (int j = 0; j < spinLength.getCount() && flag; j++) {
 			if (((String) spinLength.getItemAtPosition(j)).equals("" + saved)) {
 				spinLength.setSelection(j);
 				flag = false;
 			}
 		}
 		// Otherwise if the value is valid, set spinner to "other" and use edittext
 		if (saved >= 0 && flag) {
 			spinLength.setSelection(spinLength.getCount() - 1);
 			mPeriodLength.setText("" + saved); // string cat needed, otherwise android thinks its an R.id.*
 		}
 
 		mPeriodNumber = (EditText) findViewById(R.id.config_timescale_edit_number_periods);
 		Spinner spinNumber = (Spinner) findViewById(R.id.config_timescale_spinner_periods);
 		spinNumber.setOnItemSelectedListener(this);
 
 		int saved1 = sp.getInt(Keys.CONFIG_NUMBER_PERIODS, -1);
 		flag = true;
 		// Go through spinner, if the value is in the spinner set to that
 		for (int j = 0; j < spinNumber.getCount() && flag; j++) {
 			if (((String) spinNumber.getItemAtPosition(j)).equals("" + saved1)) {
 				spinNumber.setSelection(j);
 				flag = false;
 			}
 		}
 		// Otherwise if the value is valid, set spinner to "other" and use edittext
 		if (saved1 >= 0 && flag) {
 			spinNumber.setSelection(spinNumber.getCount() - 1);
 			mPeriodNumber.setText("" + saved1);
 		}
 
 		mTimescaleLayout = (RelativeLayout) findViewById(R.id.config_timescale_layout);
 
 		Button btnCreate = (Button) findViewById(R.id.config_doctor_btn_create);
 		btnCreate.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(DoctorConfig.this, FormBuilder.class);
 				startActivityForResult(i, REQUEST_FORM);
 			}
 		});
 
 		mArray = (CheckArray) getSupportFragmentManager().findFragmentById(R.id.config_timescale_check_array);
 
 		mDate = (StartDate) getSupportFragmentManager().findFragmentById(R.id.config_doctor_date_frag);
 
 		SharedPreferences userPrefs = getSharedPreferences(Keys.DEFAULT_PREFS, MODE_PRIVATE);
 		mBackup = userPrefs.getBoolean(Keys.DEFAULT_BACKUP, false);
 
 		if (mBackup && Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
 			mBackupManager = new BackupManager(this);
 		}
 
 		mTreatmentA = (EditText) findViewById(R.id.config_doctor_medicine_edit_treatmenta);
 		mTreatmentA.setText(sp.getString(Keys.CONFIG_TREATMENT_A, ""));
 
 		mTreatmentB = (EditText) findViewById(R.id.config_doctor_medicine_edit_treatmentb);
 		mTreatmentB.setText(sp.getString(Keys.CONFIG_TREATMENT_B, ""));
 
 		mAnyNotes = (EditText) findViewById(R.id.config_doctor_medicine_edit_notes);
 		mAnyNotes.setText(sp.getString(Keys.CONFIG_TREATMENT_NOTES, ""));
 
 		mTimeSetter = (TimeSetter) getSupportFragmentManager().findFragmentById(R.id.config_doctor_time_frag);
 		int num = 0;
 		while (sp.contains(Keys.CONFIG_TIME + num)) {
 			num++;
 		}
 		String[] times = new String[num];
 		for (int i = 0; i < num; i++) {
 			times[i] = sp.getString(Keys.CONFIG_TIME + i, "12:00");
 		}
 		mTimeSetter.setTimes(times);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.menu_doctor_config, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (DEBUG) Log.d(TAG, "Menu item selected: " + item.getTitle());
 		switch (item.getItemId()) {
 		case R.id.menu_doctor_config_done:
 			// Config done. Save data and email stuff away.
 
 			// Check that all config is actually done
 			String[] errors = checkFilledIn();
 			if (errors.length == 0) {
 				if (mFormBuilt) {
 					save();
 					makeTreatmentPlan();
 					email();
 					runScheduler();
 					setResult(RESULT_OK);
 					finish();
 				} else {
 					// Questionnaire not built yet
 					Toast.makeText(this, R.string.questionnaire_not_made, Toast.LENGTH_LONG).show();
 				}
 			} else {
 				// Not all fields filled in
 				Resources res = getResources();
 				StringBuilder sb = new StringBuilder(res.getString(R.string.fill_in_prompt));
 				sb.append(" ").append(errors[0]);
 				for (int i = 1; i < errors.length; i++) {
 					sb.append(", ").append(errors[i]);
 				}
 				Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
 			}
 
 			return true;
 		case R.id.menu_doctor_config_login:
 			// Change login details
 			changeLogin();
 			return true;
 
 		case android.R.id.home:
 			// up / home action bar button pressed
 			Intent upIntent = new Intent(this, UserPrefs.class);
 			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
 				// This activity is not part of the application's task, so create a new task
 				// with a synthesized back stack.
 				TaskStackBuilder.create(this).addNextIntent(new Intent(this, HomeScreen.class)).addNextIntent(upIntent).startActivities();
 				finish();
 			} else {
 				// This activity is part of the application's task, so simply
 				// navigate up to the hierarchical parent activity.
 				NavUtils.navigateUpTo(this, upIntent);
 			}
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 
 		case REQUEST_FORM:
 			// FormBuilder result
 			if (resultCode == RESULT_OK) {
 				mFormBuilt = true;
 			}
 			return;
 
 		default:
 			// Not my request
 			super.onActivityResult(requestCode, resultCode, data);
 		}
 
 	}
 
 	@Override
 	public void onBackPressed() {
 		// If config not finished, don't want to allow user to go back
 		if (mFormBuilt && checkFilledIn().length == 0) {
 			super.onBackPressed();
 		}
 	}
 
 	/** Run the scheduler for the first time */
 	private void runScheduler() {
 		Intent intent = new Intent(this, Scheduler.class);
 		intent.putExtra(Keys.INTENT_FIRST, true);
 		startService(intent);
 	}
 
 	/** Create a random treatment plan, which must stay hidden but sent to the pharmacist. */
 	@SuppressLint("WorldReadableFiles")
 	private void makeTreatmentPlan() {
 		// Make treatment plan
 
 		// Get basic information
 		SharedPreferences sp = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
 		int number = sp.getInt(Keys.CONFIG_NUMBER_PERIODS, 0);
 		int length = sp.getInt(Keys.CONFIG_PERIOD_LENGTH, 0);
 		int perDay = mTimeSetter.getTimes().length;
 		String start = mDate.getDate();
 
 		// Set up fields
 		Random rand = new Random();
 		StringBuilder sb = new StringBuilder();
 		StringBuilder dates = new StringBuilder();
 
 		// Set calendar instance to start date
 		Calendar cal = Calendar.getInstance();
 		String[] startArr = start.split(":");
 		int[] startInt = new int[] { Integer.parseInt(startArr[0]), Integer.parseInt(startArr[1]), Integer.parseInt(startArr[2]) };
 		cal.set(startInt[2], startInt[1], startInt[0]);
 
 		/*
 		 * For each pair of treatment periods, randomly assign A or B first
 		 * Record data in the string builders.
 		 * SB contains a short version either AB or BA
 		 * dates contains a list of which dates go with which medicine
 		 */
 		for (int i = 0; i < number; i++) {
 			if (rand.nextBoolean()) {
 				sb.append("AB");
 				// Get start date
 				dates.append(cal.get(Calendar.DAY_OF_MONTH)).append("/").append(cal.get(Calendar.MONTH) + 1).append("/")
 						.append(cal.get(Calendar.YEAR)).append(" - ");
 				// Get end date
 				cal.add(Calendar.DAY_OF_MONTH, length - 1);
 				dates.append(cal.get(Calendar.DAY_OF_MONTH)).append("/").append(cal.get(Calendar.MONTH) + 1).append("/")
 						.append(cal.get(Calendar.YEAR)).append(": ");
 				dates.append(mTreatmentA.getText().toString()).append("\n");
 				// add a day for next start date
 				cal.add(Calendar.DAY_OF_MONTH, 1);
 
 				dates.append(cal.get(Calendar.DAY_OF_MONTH)).append("/").append(cal.get(Calendar.MONTH) + 1).append("/")
 						.append(cal.get(Calendar.YEAR)).append(" - ");
 				cal.add(Calendar.DAY_OF_MONTH, length - 1);
 				dates.append(cal.get(Calendar.DAY_OF_MONTH)).append("/").append(cal.get(Calendar.MONTH) + 1).append("/")
 						.append(cal.get(Calendar.YEAR)).append(": ");
 				dates.append(mTreatmentB.getText().toString()).append("\n");
 				cal.add(Calendar.DAY_OF_MONTH, 1);
 			} else {
 				sb.append("BA");
 				dates.append(cal.get(Calendar.DAY_OF_MONTH)).append("/").append(cal.get(Calendar.MONTH) + 1).append("/")
 						.append(cal.get(Calendar.YEAR)).append(" - ");
 				cal.add(Calendar.DAY_OF_MONTH, length - 1);
 				dates.append(cal.get(Calendar.DAY_OF_MONTH)).append("/").append(cal.get(Calendar.MONTH) + 1).append("/")
 						.append(cal.get(Calendar.YEAR)).append(": ");
 				dates.append(mTreatmentB.getText().toString()).append("\n");
 				cal.add(Calendar.DAY_OF_MONTH, 1);
 
 				dates.append(cal.get(Calendar.DAY_OF_MONTH)).append("/").append(cal.get(Calendar.MONTH) + 1).append("/")
 						.append(cal.get(Calendar.YEAR)).append(" - ");
 				cal.add(Calendar.DAY_OF_MONTH, length - 1);
 				dates.append(cal.get(Calendar.DAY_OF_MONTH)).append("/").append(cal.get(Calendar.MONTH) + 1).append("/")
 						.append(cal.get(Calendar.YEAR)).append(": ");
 				dates.append(mTreatmentA.getText().toString()).append("\n");
 				cal.add(Calendar.DAY_OF_MONTH, 1);
 			}
 			sb.append("|");
 		}
 
 		Resources res = getResources();
 
 		sb.append("\n\n");
 		sb.append("\n").append(res.getText(R.string.doc_name)).append(": ").append(mDocName.getText().toString());
 		sb.append("\n").append(res.getText(R.string.patient_name)).append(": ").append(mPatientName.getText().toString());
 		sb.append("\n\n").append(res.getText(R.string.treatment_a)).append(": ").append(mTreatmentA.getText().toString());
 		sb.append("\n").append(res.getText(R.string.treatment_b)).append(": ").append(mTreatmentB.getText().toString());
 		sb.append("\n\n").append(res.getText(R.string.treatment_notes)).append(mAnyNotes.getText().toString());
 		sb.append("\n\n").append(res.getText(R.string.medicine_taken)).append(perDay).append(res.getText(R.string.times_a_day)).append("\n\n");
 		sb.append(dates);
 
 		/*
 		 * We want to write the schedule file to external storage, as that way we know the email app will be able to
 		 * find it.
 		 * However if it cannot, use internal storage and a world readable file. This may or may not work depending on
 		 * the email client used.
 		 */
 		// Check state of external storage
 		boolean storageWriteable = false;
 		boolean storageInternal = false;
 		String state = Environment.getExternalStorageState();
 
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 			// We can read and write the media
 			storageWriteable = true;
 		} else {
 			// Something else is wrong. It may be one of many other states, but all we need
 			// to know is we can neither read nor write
 			storageWriteable = false;
 		}
 		File dir = null;
 
 		if (storageWriteable && Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
 			// Eclair has no support for getExternalCacheDir()
 			// Save file to /Android/data/uk.co.jwlawson.nof1/cache/
 			File temp = Environment.getExternalStorageDirectory();
 
 			dir = new File(temp.getAbsoluteFile() + "/Android/data/uk.co.jwlawson.nof1/cache");
 
 		} else if (storageWriteable) {
 			dir = getExternalCacheDir();
 
 		} else {
 			// Toast.makeText(this, "No external storage found. Using internal storage which may not work.",
 			// Toast.LENGTH_LONG).show();
 			storageInternal = true;
 		}
 
 		if (!storageInternal) {
 			try {
 				mCache = new File(dir, SCHEDULE_FILE);
 
 				BufferedWriter writer = new BufferedWriter(new FileWriter(mCache));
 				writer.write(sb.toString());
 				writer.close();
 			} catch (IOException e) {
 				Toast.makeText(this, R.string.problem_saving_file, Toast.LENGTH_SHORT).show();
 			}
 
 		} else {
 			try {
 				// File should be world readable so that GMail (or whatever the user uses) can read it
 				mCache = new File(getFilesDir(), SCHEDULE_FILE);
 				FileOutputStream fos = openFileOutput(SCHEDULE_FILE, MODE_WORLD_READABLE);
 				fos.write(sb.toString().getBytes());
 				fos.close();
 			} catch (IOException e) {
 				Toast.makeText(this, R.string.problem_saving_file, Toast.LENGTH_SHORT).show();
 			}
 		}
 		if (DEBUG) Log.d(TAG, mCache.getAbsolutePath());
 	}
 
 	/** Save the data to file. */
 	private boolean save() {
 		boolean result = true;
 		// Put config data into shared preferences editor
 		SharedPreferences.Editor editor = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE).edit();
 
 		int number = 0;
 		int length = 0;
 
 		editor.putString(Keys.CONFIG_PATIENT_NAME, mPatientName.getText().toString());
 		editor.putString(Keys.CONFIG_DOCTOR_NAME, mDocName.getText().toString());
 
 		if (mPeriodNumber.getVisibility() == View.VISIBLE) {
 			try {
 				number = Integer.parseInt(mPeriodNumber.getText().toString());
 			} catch (NumberFormatException e) {
 				Toast.makeText(this, R.string.invalid_period_input, Toast.LENGTH_LONG).show();
 				result = false;
 			}
 		} else {
 			number = mIntPeriodNumber;
 		}
 		editor.putInt(Keys.CONFIG_NUMBER_PERIODS, number);
 
 		if (mPeriodLength.getVisibility() == View.VISIBLE) {
 			try {
 				length = Integer.parseInt(mPeriodLength.getText().toString());
 			} catch (NumberFormatException e) {
 				Toast.makeText(this, R.string.invalid_length_input, Toast.LENGTH_LONG).show();
 				result = false;
 			}
 		} else {
 			length = mIntPeriodLength;
 		}
 		editor.putInt(Keys.CONFIG_PERIOD_LENGTH, length);
 
 		editor.putBoolean(Keys.CONFIG_BUILT, mFormBuilt);
 
 		editor.putString(Keys.CONFIG_START, mDate.getDate());
 
 		// Save times to remind to take medicine
 		String[] times = mTimeSetter.getTimes();
 		for (int i = 0; i < times.length; i++) {
 			editor.putString(Keys.CONFIG_TIME + i, times[i]);
 		}
 
 		editor.putString(Keys.CONFIG_TREATMENT_A, mTreatmentA.getText().toString());
 		editor.putString(Keys.CONFIG_TREATMENT_B, mTreatmentB.getText().toString());
 		editor.putString(Keys.CONFIG_TREATMENT_NOTES, mAnyNotes.getText().toString());
 
 		// save checked boxes
 		int[] arr = mArray.getSelected();
 		for (int j = 1; j < length + 1; j++) {
 			boolean contains = false;
 			for (int k = 0; k < arr.length; k++) {
 				if (j == arr[k]) contains = true;
 			}
 			editor.putBoolean(Keys.CONFIG_DAY + j, contains);
 		}
 
 		// Save changes
 		result &= editor.commit();
 		// Ask for backup
 		backup();
 
 		return result;
 	}
 
 	/** Helper to ask for backup, if supported */
 	@TargetApi(8)
 	private void backup() {
 		if (mBackup && Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
 			if (DEBUG) Log.d(TAG, "Requesting backup");
 			mBackupManager.dataChanged();
 		}
 	}
 
 	/** email the information to the doctor and pharmacist */
 	private void email() {
 		Toast.makeText(this, R.string.about_to_send_emails, Toast.LENGTH_SHORT).show();
 		// Make and Send email
 		String pharmEmail = mPharmEmail.getText().toString();
 		String docEmail = mDocEmail.getText().toString();
 
 		Resources res = getResources();
 
 		SharedPreferences prefs = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
 
 		// Make email text
 		StringBuilder sb = new StringBuilder(res.getText(R.string.thanks_for_using));
 		sb.append("\n").append(res.getText(R.string.patient_name)).append(": ").append(mPatientName.getText().toString());
 		sb.append("\n").append(res.getText(R.string.treatment_a)).append(": ").append(mTreatmentA.getText().toString());
 		sb.append("\n").append(res.getText(R.string.treatment_b)).append(": ").append(mTreatmentB.getText().toString());
 		sb.append("\n").append(res.getText(R.string.treatment_notes)).append(mAnyNotes.getText().toString());
 
 		// Medicine schedule
 		sb.append("\n\n").append(res.getText(R.string.medicine_schedule));
 		String[] times = mTimeSetter.getTimes();
 		for (int i = 0; i < times.length; i++) {
 			sb.append(times[i]).append(" ");
 		}
 		// Start date
 		String start = mDate.getDate();
 		String[] startArr = start.split(":");
 		sb.append("\n\n").append(res.getText(R.string.trials_start)).append(startArr[0]).append("/").append(Integer.parseInt(startArr[1]) + 1)
 				.append("/").append(startArr[2]);
 
 		// Treatment length
 		sb.append("\n").append(res.getText(R.string.number_periods)).append(prefs.getInt(Keys.CONFIG_NUMBER_PERIODS, 0));
 		sb.append("\n").append(res.getText(R.string.length_periods)).append(prefs.getInt(Keys.CONFIG_PERIOD_LENGTH, 0));
 
 		// Recording days
 		sb.append("\n").append(res.getText(R.string.days_to_record));
 		int[] days = mArray.getSelected();
 		for (int i = 0; i < days.length; i++) {
 			sb.append(days[i]).append(" ");
 		}
 
 		// Questions
 		sb.append("\n\n").append(res.getText(R.string.questions)).append("\n");
 
 		SharedPreferences ques = getSharedPreferences(Keys.QUES_NAME, MODE_PRIVATE);
 		for (int i = 0; ques.contains(Keys.QUES_TEXT + i); i++) {
 			sb.append(ques.getString(Keys.QUES_TEXT + i, "")).append("\n");
 		}
 
 		// Set up doctor email
 		Intent intent1 = new Intent(Intent.ACTION_SEND);
 		intent1.setType(HTTP.PLAIN_TEXT_TYPE);
 		intent1.putExtra(Intent.EXTRA_EMAIL, new String[] { docEmail });
 		intent1.putExtra(Intent.EXTRA_SUBJECT, res.getText(R.string.doc_email_subject));
 		intent1.putExtra(Intent.EXTRA_TEXT, sb.toString());
 
 		// Make pharmacist email text
 		StringBuilder sb1 = new StringBuilder(res.getText(R.string.find_attached));
 		sb1.append("\n").append(res.getText(R.string.doctors_name)).append(": ").append(mDocName.getText().toString());
 		sb1.append("\n").append(res.getText(R.string.patient_name)).append(": ").append(mPatientName.getText().toString());
 		sb1.append("\n\n").append(res.getText(R.string.treatment_a)).append(": ").append(mTreatmentA.getText().toString());
 		sb1.append("\n").append(res.getText(R.string.treatment_b)).append(": ").append(mTreatmentB.getText().toString());
 		sb1.append("\n\n").append(res.getText(R.string.treatment_notes)).append(mAnyNotes.getText().toString());
 
 		// Set up pharmacist email
 		Intent intent2 = new Intent(Intent.ACTION_SEND);
 		intent2.setType(HTTP.PLAIN_TEXT_TYPE);
 		intent2.putExtra(Intent.EXTRA_EMAIL, new String[] { pharmEmail });
 		intent2.putExtra(Intent.EXTRA_SUBJECT, res.getText(R.string.pharm_email_subject));
 		intent2.putExtra(Intent.EXTRA_TEXT, sb1.toString());
 
 		// Add schedule attachment
 		Uri uri2 = Uri.fromFile(mCache);
 		if (DEBUG) Log.d(TAG, "uri2 " + uri2.toString());
 
 		intent2.putExtra(Intent.EXTRA_STREAM, uri2);
 
 		startActivities(new Intent[] { intent1, intent2 });
 
 	}
 
 	/** Loads AlertDialog to change the login details of doctor */
 	private void changeLogin() {
 
 		final SharedPreferences sharedPrefs = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.dialog_theme));
 
 		View view = getInflater().inflate(R.layout.config_change_login, null, false);
 
 		final EditText email = (EditText) view.findViewById(R.id.config_change_login_edit_cur_email);
 		email.setText(mDocEmail.getText().toString());
 
 		final EditText newEmail = (EditText) view.findViewById(R.id.config_change_login_edit_new_email);
 
 		final EditText pass = (EditText) view.findViewById(R.id.config_change_login_edit_cur_password);
 
 		final EditText newPass = (EditText) view.findViewById(R.id.config_change_login_edit_new_password);
 		final EditText newPass2 = (EditText) view.findViewById(R.id.config_change_login_edit_new_pass2);
 
 		builder.setView(view).setTitle(R.string.change_login_details).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				String emailHash = new String(Hex.encodeHex(DigestUtils.sha512(email.getText().toString())));
 				String passHash = new String(Hex.encodeHex(DigestUtils.sha512(pass.getText().toString())));
 
 				if (emailHash.equals(sharedPrefs.getString(Keys.CONFIG_EMAIL, null))
 						&& passHash.equals(sharedPrefs.getString(Keys.CONFIG_PASS, null))) {
 					// Login correct
 
 					String passStr = newPass.getText().toString();
 					SharedPreferences.Editor editor = sharedPrefs.edit();
 
 					if (passStr != "" && passStr != null && passStr.equals(newPass2.getText().toString())) {
 						// Change password
 
 						editor.putString(Keys.CONFIG_PASS, new String(Hex.encodeHex(DigestUtils.sha512(passStr))));
 
 					}
 
 					String newEmailStr = newEmail.getText().toString();
 					if (newEmailStr != null && newEmailStr != "") {
 						// Change email
 						editor.putString(Keys.CONFIG_EMAIL, new String(Hex.encodeHex(DigestUtils.sha512(newEmailStr))));
 						setEmail(newEmailStr);
 					}
 					// Save changes
 					editor.commit();
 					// Request backup
 					backup();
 				} else {
 					// Incorrect login
 					Toast.makeText(getApplicationContext(), R.string.incorrect_login, Toast.LENGTH_SHORT).show();
 					changeLogin();
 				}
 			}
 		});
 		builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				// Close dialog
 				dialog.cancel();
 			}
 		});
 		mDialog = builder.create();
 		mDialog.show();
 	}
 
 	@Override
 	protected void onDestroy() {
 		// Close dialog if open to prevent leak
 		if (mDialog != null) {
 			mDialog.dismiss();
 		}
 		SharedPreferences sp = getSharedPreferences(Keys.DEFAULT_PREFS, MODE_PRIVATE);
 		if (!sp.contains(Keys.DEFAULT_FIRST)) {
 			SharedPreferences.Editor edit = sp.edit();
 			edit.putBoolean(Keys.DEFAULT_FIRST, true);
 			edit.commit();
 			backup();
 		}
 		super.onDestroy();
 	}
 
 	/** Used to set the email field in EditText from an onClick call */
 	private void setEmail(String email) {
 		mDocEmail.setText(email);
 	}
 
 	/** Nasty hack to ensure text in alertdialog is readable */
 	private LayoutInflater getInflater() {
 		LayoutInflater inflater;
 		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
 			// Old devices use nasty dark AlertDialog theme, so inflater needs to make text white.
 			// ApplicationContext for some reason doesn't have the light theme applied, so will do nicely.
 			inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		} else {
 			// Newer devices use shiny holo light dialogs. Easy.
 			inflater = this.getLayoutInflater();
 		}
 		return inflater;
 	}
 
 	/**
 	 * Go through form and check everything has been filled in and completed. If not, add name to returned array and
 	 * highlight view
 	 */
 	private String[] checkFilledIn() {
 		ArrayList<String> list = new ArrayList<String>();
 		Resources res = getResources();
 
 		if (mDocEmail.getText().length() == 0) {
 			list.add(res.getString(R.string.doctor_email));
 			mDocEmail.setBackgroundColor(0x20FF0000);
 		} else {
 			mDocEmail.setBackgroundColor(0x00000000);
 		}
 		if (mPharmEmail.getText().length() == 0) {
 			list.add(res.getString(R.string.pharmacist_email));
 			mPharmEmail.setBackgroundColor(0x20FF0000);
 		} else {
 			mPharmEmail.setBackgroundColor(0x00000000);
 		}
 		if (mPatientName.getText().length() == 0) {
 			list.add(res.getString(R.string.patient_name));
 			mPatientName.setBackgroundColor(0x20FF0000);
 		} else {
 			mPatientName.setBackgroundColor(0x00000000);
 		}
 		if (mIntPeriodLength < 0 && mPeriodLength.getText().length() == 0) {
 			list.add(res.getString(R.string.treatment_period));
 			mPeriodLength.setBackgroundColor(0x20FF0000);
 		} else {
 			mPeriodLength.setBackgroundColor(0x00000000);
 		}
 		if (mIntPeriodNumber < 0 && mPeriodNumber.getText().length() == 0) {
 			list.add(res.getString(R.string.config_no_treatment_periods));
 			mPeriodNumber.setBackgroundColor(0x20FF0000);
 		} else {
 			mPeriodNumber.setBackgroundColor(0x00000000);
 		}
 		if (mArray.getSelected().length == 0) {
 			list.add(res.getString(R.string.config_days_record));
 			mArray.getView().setBackgroundColor(0x20FF0000);
 		} else {
 			mArray.getView().setBackgroundColor(0x00000000);
 		}
 		if (mTreatmentA.getText().length() == 0) {
 			list.add(res.getString(R.string.treatment_a));
 			mTreatmentA.setBackgroundColor(0x20FF0000);
 		} else {
 			mTreatmentA.setBackgroundColor(0x00000000);
 		}
 		if (mTreatmentB.getText().length() == 0) {
 			list.add(res.getString(R.string.treatment_b));
 			mTreatmentB.setBackgroundColor(0x20FF0000);
 		} else {
 			mTreatmentB.setBackgroundColor(0x00000000);
 		}
 		String[] result = new String[list.size()];
 		return list.toArray(result);
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 		String item;
 
 		switch (parent.getId()) {
 
 		case R.id.config_timescale_spinner_length:
 			// Length spinner
 			item = (String) parent.getItemAtPosition(position);
			String[] arr = getResources().getStringArray(R.array.treatment_period_arr);
 			int num = 0;
 			// arr[length-1] is "other" ie no number selected
 			if (item.equalsIgnoreCase(arr[arr.length - 1])) {
 				mPeriodLength.setVisibility(View.VISIBLE);
 				mIntPeriodLength = -1;
 				String text = mPeriodLength.getText().toString();
 				if (text.length() != 0) {
 					num = Integer.parseInt(text);
 					mArray.setNumber(num);
 				}
 
 			} else {
 				mPeriodLength.setVisibility(View.GONE);
 				mIntPeriodLength = Integer.parseInt(item);
 				mArray.setNumber(mIntPeriodLength);
 				num = mIntPeriodLength;
 			}
 			// Set which boxes in check array are selected
 			SharedPreferences sp = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
 
 			boolean[] selected = new boolean[num];
 			for (int i = 0; i < num; i++) {
 				selected[i] = sp.getBoolean(Keys.CONFIG_DAY + (i + 1), false);
 			}
 			mArray.setSelected(selected);
 			mTimescaleLayout.requestLayout();
 
 			break;
 
 		case R.id.config_timescale_spinner_periods:
 			// Number spinner
 			item = (String) parent.getItemAtPosition(position);
 			String[] arr1 = getResources().getStringArray(R.array.treatment_period_arr);
 			// arr[length - 1] is "other"
 			if (item.equalsIgnoreCase(arr1[arr1.length - 1])) {
 				mPeriodNumber.setVisibility(View.VISIBLE);
 				mIntPeriodNumber = -1;
 			} else {
 				mPeriodNumber.setVisibility(View.GONE);
 				mIntPeriodNumber = Integer.parseInt(item);
 			}
 			mTimescaleLayout.requestLayout();
 			break;
 		}
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 		// Don't care
 	}
 
 	@Override
 	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 		if (DEBUG) Log.d(TAG, "Editor action caught " + v.getId() + ":" + mPeriodLength.getId());
 		// Get the number entered and set the check array
 		String text = mPeriodLength.getText().toString();
 		if (text.length() != 0) {
 
 			int num = Integer.parseInt(text);
 			mArray.setNumber(num);
 
 			// Set which boxes in check array are selected
 			SharedPreferences sp = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
 
 			boolean[] selected = new boolean[num];
 			for (int i = 0; i < num; i++) {
 				selected[i] = sp.getBoolean(Keys.CONFIG_DAY + (i + 1), false);
 			}
 			mArray.setSelected(selected);
 		}
 		return false;
 	}
 }

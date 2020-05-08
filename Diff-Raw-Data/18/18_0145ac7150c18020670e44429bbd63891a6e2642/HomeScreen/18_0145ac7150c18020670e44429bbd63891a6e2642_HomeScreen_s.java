 /*******************************************************************************
  * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
  * Copyright (C) 2012 John Lawson
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
 package org.nof1trial.nof1.activities;
 
 import java.io.File;
 
 import org.apache.http.protocol.HTTP;
 import org.nof1trial.nof1.BuildConfig;
 import org.nof1trial.nof1.Keys;
 import org.nof1trial.nof1.R;
 import org.nof1trial.nof1.app.Util;
 import org.nof1trial.nof1.services.FinishedService;
 import org.nof1trial.nof1.services.Saver;
 
 import android.content.ActivityNotFoundException;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.res.Resources;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.app.TaskStackBuilder;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.Window;
 
 /**
  * The main home screen that users see when they open the app. On first run will
  * also set up the task stack to allow
  * doctors to input data, then patients
  * preferences, then back to this screen.
  * 
  * @author John Lawson
  * 
  */
 public class HomeScreen extends SherlockActivity {
 
 	private static final String TAG = "HomeScreen";
 	private static final boolean DEBUG = BuildConfig.DEBUG;
 
 	private static final int ACCOUNT_REQUEST = 101;
 
 	/** Current context */
 	private final Context mContext = this;
 
 	private Button btnEmail;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		super.onCreate(savedInstanceState);
 
 		getSupportActionBar().setHomeButtonEnabled(false);
 		setSupportProgressBarIndeterminateVisibility(false);
 
 		SharedPreferences accPrefs = Util.getSharedPreferences(mContext);
 
 		if (accPrefs.getString(Util.ACCOUNT_NAME, null) == null) {
 			// No account set up for app yet
 			if (DEBUG) Log.d(TAG, "No account found. Loading account activity");
 
 			Intent account = new Intent(mContext, AccountsActivity.class);
 			startActivityForResult(account, ACCOUNT_REQUEST);
 
 		} else {
 			createRedirectOrUi();
 		}
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		switch (requestCode) {
 		case ACCOUNT_REQUEST:
 			if (DEBUG) Log.d(TAG, "Account request finished");
 			switch (resultCode) {
 			case RESULT_OK:
 				// New account set up
 				createRedirectOrUi();
 				break;
 			case RESULT_CANCELED:
 				// No account set up
 				finish();
 				break;
 			}
 			return;
 		}
 
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.home_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (DEBUG) Log.d(TAG, "Menu item selected: " + item.getTitle());
 
 		switch (item.getItemId()) {
 
 		case R.id.menu_home_settings:
 			Intent intent = new Intent(this, UserPrefs.class);
 			startActivity(intent);
 			return true;
 
 		case R.id.menu_home_about:
 			Intent intent1 = new Intent(this, About.class);
 			startActivity(intent1);
 			return true;
 
 		case R.id.menu_home_accounts:
 			Intent intent2 = new Intent(this, AccountsActivity.class);
 			startActivity(intent2);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void sendEmail(File file) {
 		Uri uri = Uri.fromFile(file);
 		Resources res = getResources();
 		SharedPreferences sp = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
 
 		try {
 			Intent intent = new Intent(Intent.ACTION_SEND);
 			intent.setType(HTTP.PLAIN_TEXT_TYPE);
 			intent.putExtra(Intent.EXTRA_EMAIL, "");
 			intent.putExtra(Intent.EXTRA_SUBJECT, res.getText(R.string.trial_data));
			intent.putExtra(Intent.EXTRA_TEXT, res.getText(R.string.results_attached) + sp.getString(Keys.CONFIG_PATIENT_NAME, ""));
 			intent.putExtra(Intent.EXTRA_STREAM, uri);
 			startActivity(intent);
 		} catch (ActivityNotFoundException e) {
 			// No suitable email activity found
 			Toast.makeText(HomeScreen.this, R.string.no_email_app_found, Toast.LENGTH_SHORT).show();
 		}
 
 		if (btnEmail != null) {
 			btnEmail.setEnabled(true);
 			btnEmail.setText(R.string.email_csv);
 		}
 	}
 
 	private File findCSV() {
 		String state = Environment.getExternalStorageState();
 
 		File dir;
 		File file;
 
		if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) || Environment.MEDIA_MOUNTED.equals(state)) {
 			// External storage readable
 			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
 				// Eclair has no support for getExternalCacheDir()
 				// Look for file in /Android/data/org.nof1trial.nof1/files/
 				File temp = Environment.getExternalStorageDirectory();
 
 				dir = new File(temp.getAbsoluteFile() + "/Android/data/org.nof1trial.nof1/files");
 
 			} else {
 
 				dir = getExternalFilesDir(null);
 			}
 
 			file = new File(dir, FinishedService.CVS_FILE);
 
 			if (file.exists()) {
 				// Found the file
 				return file;
 			}
 		}
 
 		// Search internal storage
 		dir = getFilesDir();
 
 		file = new File(dir, FinishedService.CVS_FILE);
 
 		if (file.exists()) {
 			// Found the file
 			return file;
 		}
 		// File not found :(
 		return null;
 	}
 
 	private void findUpdates(SharedPreferences prefs) {
 
 		// If no password hash, then the app has not been run before, so don't
 		// need to update
 		if (prefs.contains(Keys.CONFIG_PASS)) {
 
 			int lastVersion = prefs.getInt(Keys.DEFAULT_VERSION, 0);
 
 			try {
 				PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
 
 				int currentVersion = info.versionCode;
 
 				if (lastVersion < currentVersion) {
 					// Have update to do
					boolean done = handleUpdates(lastVersion, currentVersion);

					if (done) prefs.edit().putInt(Keys.DEFAULT_VERSION, currentVersion);
 				}
 
 			} catch (NameNotFoundException e) {
 				// I would hope that this package exists, seeing as it's what's
 				// running this code
 			}
 		}
 	}
 
 	private boolean handleUpdates(int lastVersion, int currentVersion) {
 
 		boolean result = true;
 
 		if (lastVersion <= 5) {
 			// v1 of app. Need to update database and upload config data to
 			// server
 			SharedPreferences prefs = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
 			SharedPreferences.Editor edit = prefs.edit();
 			// Increment db version to force update
 			edit.putInt(Keys.CONFIG_DB_VERSION, prefs.getInt(Keys.CONFIG_DB_VERSION, 1) + 1);
 			edit.commit();
 
 			Intent saveConfig = new Intent(mContext, Saver.class);
 			saveConfig.setAction(Keys.ACTION_UPLOAD_ALL);
 			startService(saveConfig);
 		}
 
 		return result;
 
 	}
 
 	private void createRedirectOrUi() {
 
 		SharedPreferences sp = getSharedPreferences(Keys.DEFAULT_PREFS, MODE_PRIVATE);
 
 		// Want account set up before configuring updates
 		findUpdates(sp);
 
 		if (!sp.contains(Keys.DEFAULT_FIRST)) {
 			if (DEBUG) Log.d(TAG, "App launched for the first time");
 
 			redirectToLogin();
 
 		} else {
 			// Not the first time app is run
 			createUi();
 		}
 	}
 
 	private void redirectToLogin() {
 		TaskStackBuilder builder = TaskStackBuilder.create(this);
 
 		builder.addNextIntent(new Intent(this, HomeScreen.class));
 		builder.addNextIntent(new Intent(this, UserPrefs.class));
 		builder.addNextIntent(new Intent(this, DoctorLogin.class));
 
 		builder.startActivities();
 	}
 
 	private void createUi() {
 		setContentView(R.layout.home_layout);
 		SharedPreferences quesPrefs = getSharedPreferences(Keys.QUES_NAME, MODE_PRIVATE);
 
 		Button btnData = (Button) findViewById(R.id.home_btn_data);
 		btnData.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Launch questionnaire
 				Intent intent = new Intent(HomeScreen.this, Questionnaire.class);
 				startActivity(intent);
 			}
 		});
 
 		Button btnGraphs = (Button) findViewById(R.id.home_btn_graph);
 
 		if (quesPrefs.contains(Keys.QUES_TEXT + 0)) {
 			// Enable viewing graphs, as questionnaire built
 
 			btnGraphs.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					// Launch graph activity
 					Intent intent = new Intent(HomeScreen.this, GraphChooser.class);
 					startActivity(intent);
 				}
 			});
 		} else {
 			// Questionnaire not made, so don't want to create empty
 			// database
 			btnGraphs.setEnabled(false);
 		}
 
 		Button btnComment = (Button) findViewById(R.id.home_btn_comment);
 		btnComment.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(HomeScreen.this, CommentList.class);
 				startActivity(intent);
 			}
 		});
 
 		Button btnNewNote = (Button) findViewById(R.id.home_btn_add_note);
 		btnNewNote.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(HomeScreen.this, AddNote.class);
 				startActivity(intent);
 			}
 		});
 
 		SharedPreferences schedprefs = getSharedPreferences(Keys.SCHED_NAME, MODE_PRIVATE);
 		if (schedprefs.getBoolean(Keys.SCHED_FINISHED, false)) {
 			// Trial finished.
 
 			// Disable data input button
 			btnData.setEnabled(false);
 
 			// Show email csv button
 			btnEmail = (Button) findViewById(R.id.home_btn_email);
 			btnEmail.setVisibility(View.VISIBLE);
 
 			btnEmail.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 
 					File file = findCSV();
 
 					btnEmail.setEnabled(false);
 
 					if (file == null) {
 						// File not found
 
 						btnEmail.setText(R.string.creating_file);
 
 						Intent maker = new Intent(mContext, FinishedService.class);
 						maker.setAction(Keys.ACTION_MAKE_FILE);
 						startService(maker);
 
 						LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
 						manager.registerReceiver(new FileReceiver(), new IntentFilter());
 
 					} else {
 
 						btnEmail.setText(R.string.sending_email);
 						sendEmail(file);
 
 					}
 
 				}
 			});
 
 			Button btnSchedule = (Button) findViewById(R.id.home_btn_schedule);
 			btnSchedule.setVisibility(View.VISIBLE);
 			btnSchedule.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					// Start schedule viewer activity
 					Intent schedule = new Intent(mContext, ScheduleViewer.class);
 					startActivity(schedule);
 				}
 			});
 
 			// Reload relative layout to ensure button is shown
 			((RelativeLayout) btnEmail.getParent()).requestLayout();
 		}
 	}
 
 	private class FileReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (Keys.ACTION_MAKE_FILE.equals(intent.getAction())) {
 				// File made successfully
 
 				// Only want to send email if user still on home screen,
 				// otherwise it would be a bit jarring
 				if (HomeScreen.this != null) {
 					File file = findCSV();
 					if (file != null) {
 						sendEmail(file);
 					}
 				}
 
 				LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
 				manager.unregisterReceiver(this);
 
 			} else if (Keys.ACTION_ERROR.equals(intent.getAction())) {
 				// Some error
 
 			}
 		}
 
 	}
 }

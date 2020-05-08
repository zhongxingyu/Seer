 /*******************************************************************************
  * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
  * Copyright (C) 2012 John Lawson WMG, University of Warwick
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
 
import org.apache.http.protocol.HTTP;

 import uk.co.jwlawson.nof1.DataSource;
 import uk.co.jwlawson.nof1.FinishedService;
 import uk.co.jwlawson.nof1.Keys;
 import uk.co.jwlawson.nof1.R;
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.app.TaskStackBuilder;
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
  * The main home screen that users see when they open the app. On first run will also set up the task stack to allow
  * doctors to input data, then patients preferences, then back to this screen.
  * 
  * @author John Lawson
  * 
  */
 public class HomeScreen extends SherlockActivity {
 
 	private static final String TAG = "HomeScreen";
 	private static final boolean DEBUG = false;
 
 	private File mFile;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		super.onCreate(savedInstanceState);
 
 		getSupportActionBar().setHomeButtonEnabled(false);
 		setSupportProgressBarIndeterminateVisibility(false);
 
 		SharedPreferences sp = getSharedPreferences(Keys.DEFAULT_PREFS, MODE_PRIVATE);
 
 		if (!sp.contains(Keys.DEFAULT_FIRST)) {
 			if (DEBUG) Log.d(TAG, "App launched for the first time");
 
 			TaskStackBuilder builder = TaskStackBuilder.create(this);
 
 			builder.addNextIntent(new Intent(this, HomeScreen.class));
 			builder.addNextIntent(new Intent(this, UserPrefs.class));
 			builder.addNextIntent(new Intent(this, DoctorLogin.class));
 
 			builder.startActivities();
 
 		} else {
 			// Not the first time app is run
 
 			setContentView(R.layout.home_layout);
 
 			Button btnData = (Button) findViewById(R.id.home_btn_data);
 			btnData.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					// Launch questionnaire
 					Intent intent = new Intent(HomeScreen.this, Questionnaire.class);
 					startActivity(intent);
 				}
 			});
 
 			SharedPreferences quesPrefs = getSharedPreferences(Keys.QUES_NAME, MODE_PRIVATE);
 
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
 				// Questionnaire not made, so don't want to create empty database
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
				// Trial finished. Show email csv button
 				final Button btnEmail = (Button) findViewById(R.id.home_btn_email);
 				btnEmail.setVisibility(View.VISIBLE);
 
 				btnEmail.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 
 						File file = findCSV();
 
 						if (file == null) {
 							// File not found
							btnEmail.setEnabled(false);
 							new Loader().execute();

 						} else {
 							Uri uri = Uri.fromFile(file);
 							Resources res = getResources();
 							SharedPreferences sp = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
 
 							try {
 								Intent intent = new Intent(Intent.ACTION_SEND);
								intent.setType(HTTP.PLAIN_TEXT_TYPE);
 								intent.putExtra(Intent.EXTRA_EMAIL, "");
								intent.putExtra(Intent.EXTRA_SUBJECT, res.getText(R.string.trial_data));
 								intent.putExtra(Intent.EXTRA_TEXT,
 										res.getText(R.string.results_attached) + sp.getString(Keys.CONFIG_PATIENT_NAME, ""));
 								intent.putExtra(Intent.EXTRA_STREAM, uri);
 								startActivity(intent);
 							} catch (ActivityNotFoundException e) {
 								// No suitable email activity found
 								Toast.makeText(HomeScreen.this, R.string.no_email_app_found, Toast.LENGTH_SHORT).show();
 							}
 						}
 
 					}
 				});
 				// Reload relative layout to ensure button is shown
 				((RelativeLayout) btnEmail.getParent()).requestLayout();
 			}
 
 		}
 
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
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private File findCSV() {
 		String state = Environment.getExternalStorageState();
 
 		File dir;
 		File file;
 
 		if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) || Environment.MEDIA_MOUNTED.equals(state)) {
 			// External storage readable
 			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
 				// Eclair has no support for getExternalCacheDir()
 				// Look for file in /Android/data/uk.co.jwlawson.nof1/files/
 				File temp = Environment.getExternalStorageDirectory();
 
 				dir = new File(temp.getAbsoluteFile() + "/Android/data/uk.co.jwlawson.nof1/files");
 
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
 
 	@SuppressLint("WorldReadableFiles")
 	@TargetApi(8)
 	private boolean createCVS(Cursor cursor) {
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
 
 			dir = new File(temp.getAbsoluteFile() + "/Android/data/uk.co.jwlawson.nof1/files");
 
 		} else if (storageWriteable) {
 
 			dir = getExternalFilesDir(null);
 
 		} else {
 			// Toast.makeText(this, "No external storage found. Using internal storage which may not work.",
 			// Toast.LENGTH_LONG).show();
 			storageInternal = true;
 			dir = getFilesDir();
 		}
 		mFile = new File(dir, FinishedService.CVS_FILE);
 
 		if (storageInternal) {
 			try {
 				// File should be world readable so that GMail (or whatever the user uses) can read it
 				FileOutputStream fos = openFileOutput(FinishedService.CVS_FILE, MODE_WORLD_READABLE);
 				fos.write(getCVSString(cursor).getBytes());
 				fos.close();
 
 			} catch (IOException e) {
 				Toast.makeText(this, R.string.problem_saving_file, Toast.LENGTH_SHORT).show();
 				return false;
 			}
 
 		} else {
 			try {
 				// Write file to external storage
 				BufferedWriter writer = new BufferedWriter(new FileWriter(mFile));
 				writer.write(getCVSString(cursor));
 				writer.close();
 
 			} catch (IOException e) {
 				Toast.makeText(this, R.string.problem_saving_file, Toast.LENGTH_SHORT).show();
 				return false;
 			}
 		}
 		return true;
 
 	}
 
 	private String getCVSString(Cursor cursor) {
 		StringBuilder sb = new StringBuilder();
 
 		cursor.moveToFirst();
 		String[] headers = cursor.getColumnNames();
 		int size = headers.length;
 
 		// Add column headers
 		for (int i = 0; i < size; i++) {
 			sb.append(headers[i]).append(", ");
 		}
 		sb.append("\n");
 
 		// Add data
 		while (!cursor.isAfterLast()) {
 			for (int i = 0; i < size; i++) {
 				sb.append(cursor.getString(i)).append(", ");
 			}
 			sb.append("\n");
 
 			cursor.moveToNext();
 		}
 
 		return sb.toString();
 
 	}
 
 	private class Loader extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			setSupportProgressBarIndeterminateVisibility(true);
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			DataSource data = new DataSource(HomeScreen.this);
 			data.open();
 
 			Cursor cursor = data.getAllColumns();
 
 			createCVS(cursor);
 
 			cursor.close();
 			data.close();
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			super.onPostExecute(result);
 			setSupportProgressBarIndeterminateVisibility(false);
 
 			// Send file
 			if (mFile != null) {
 				Uri uri = Uri.fromFile(mFile);
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
 			}
 			((Button) findViewById(R.id.home_btn_email)).setEnabled(true);
 		}
 	}
 }

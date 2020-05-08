 package com.tonyhuangjun.homework;
 
 import java.util.Calendar;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 
 public class MainActivity extends SherlockActivity {
 	// Preferences file accessor id's.
 	final static String CLASS_TITLE = "class_title_";
 	final static String CLASS_BODY = "class_body_";
 	// Class Status: true = unfinished(bold), false = finished(normal)
 	final static String CLASS_STATUS = "class_status_";
 	final static String NUMBER_OF_CLASSES = "number_of_classes";
 	final static String REMINDER_TIMER = "timer";
 	final static String NOTIFICATION = "notification";
 	private final static String FIRST_RUN = "first_run";
 
 	final static int TITLE_UNFINISHED = Color.parseColor("#CC990000");
 	final static int BODY_UNFINISHED = Color.parseColor("#CCCC0000");
 	final static int TITLE_FINISHED = Color.parseColor("#CC33CC00");
 	final static int BODY_FINISHED = Color.parseColor("#CC99CC00");
 
 	// As of right now, the possible ints are 1, 2, and 4.
 	private int numberOfClasses;
 	private final static String TAG = "MainActivity";
 
 	private SharedPreferences settings;
 	private Editor editor;
 
 	private TextView classTitle1, classTitle2, classTitle3, classTitle4,
 			classTitle5, classTitle6, classTitle7, classTitle8;
 	private TextView classBody1, classBody2, classBody3, classBody4,
 			classBody5, classBody6, classBody7, classBody8;
 
 	// For notifications.
 	AlarmManager am;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Get preferences file and editor.
 		settings = getSharedPreferences("Default", MODE_PRIVATE);
 		editor = settings.edit();
 
 		Log.d(TAG, "Interval = " + settings.getString(REMINDER_TIMER, "Null"));
 
 		am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 
 		// Create dummy titles in settings.
 		if (settings.getBoolean(FIRST_RUN, true))
 			populateDebug();
 
 	}
 
 	protected void onResume() {
 		super.onResume();
 
 		Log.d(TAG, "MainActivity resuming...");
 		Log.d(TAG, "Previous number of classes = " + numberOfClasses);
 		Log.d(TAG, "Refreshing number of classes....");
 		refreshNumberOfClasses();
 		Log.d(TAG, "Refresh completed. New number of classes = "
 				+ numberOfClasses);
 		refreshLayout();
 
 		getHandlers();
 		fillData();
 
 		refreshTimer();
 
 	}
 
 	private void refreshNumberOfClasses() {
 		numberOfClasses = Integer.valueOf(settings.getString(NUMBER_OF_CLASSES,
 				"1"));
 	}
 
 	private void refreshTimer() {
 		Log.d(TAG,
 				"Reminder interval = "
 						+ settings.getString(REMINDER_TIMER, "Null"));
 		// Set up calendar instance to reference 12pm.
 		Calendar calendar = Calendar.getInstance();
 		calendar.set(Calendar.HOUR_OF_DAY, 12);
 		calendar.set(Calendar.MINUTE, 0);
 		calendar.set(Calendar.SECOND, 0);
 		Log.d(TAG, "Current time in millis = " + System.currentTimeMillis());
 		// Sets repeating alarm.
 		Intent intent = new Intent(this, Alarm.class);
 		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
 				intent, 0);
 		Log.d(TAG, "Setting repeating alarm.");
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
 				Integer.valueOf(settings.getString(REMINDER_TIMER, "1800000")),
 				pendingIntent);
 	}
 
 	// Applies the layout corresponding to the current number of classes.
 	private void refreshLayout() {
 		switch (numberOfClasses) {
 		// 8 and 7 classes have the same layout, but different handlers.
 		case 8:
 		case 7:
 			setContentView(R.layout.main_eight);
 			break;
 		case 6:
 		case 5:
 			setContentView(R.layout.main_six);
 			break;
 		case 4:
 		case 3:
 			setContentView(R.layout.main_four);
 			break;
 		case 2:
 			setContentView(R.layout.main_two);
 			break;
 		default:
 			setContentView(R.layout.main_one);
 			break;
 		}
 
 		Log.d(TAG, "Layout refreshed! Currenet number of classes = "
 				+ numberOfClasses);
 		getHandlers();
 		fillData();
 	}
 
 	private void getHandlers() {
 		switch (numberOfClasses) {
 		case 8:
 			// Get title and body handlers and set text.
 			classTitle8 = (TextView) findViewById(R.id.ClassTitle8);
 			classBody8 = (TextView) findViewById(R.id.ClassBody8);
 		case 7:
 			classTitle7 = (TextView) findViewById(R.id.ClassTitle7);
 			classBody7 = (TextView) findViewById(R.id.ClassBody7);
 		case 6:
 			classTitle6 = (TextView) findViewById(R.id.ClassTitle6);
 			classBody6 = (TextView) findViewById(R.id.ClassBody6);
 		case 5:
 			classTitle5 = (TextView) findViewById(R.id.ClassTitle5);
 			classBody5 = (TextView) findViewById(R.id.ClassBody5);
 		case 4:
 			classTitle4 = (TextView) findViewById(R.id.ClassTitle4);
 			classBody4 = (TextView) findViewById(R.id.ClassBody4);
 		case 3:
 			classTitle3 = (TextView) findViewById(R.id.ClassTitle3);
 			classBody3 = (TextView) findViewById(R.id.ClassBody3);
 		case 2:
 			classTitle2 = (TextView) findViewById(R.id.ClassTitle2);
 			classBody2 = (TextView) findViewById(R.id.ClassBody2);
 		default:
 			classTitle1 = (TextView) findViewById(R.id.ClassTitle1);
 			classBody1 = (TextView) findViewById(R.id.ClassBody1);
 			break;
 		}
 	}
 
 	private void fillData() {
 		// Initialize the handlers and populate the TextViews.
 		switch (numberOfClasses) {
 		case 8:
 			classBody8.setMovementMethod(new ScrollingMovementMethod());
 			classTitle8.setText(settings.getString(CLASS_TITLE + 8, "Null"));
 			classBody8.setText(settings.getString(CLASS_BODY + 8, "Null"));
 		case 7:
 			classBody7.setMovementMethod(new ScrollingMovementMethod());
 			classTitle7.setText(settings.getString(CLASS_TITLE + 7, "Null"));
 			classBody7.setText(settings.getString(CLASS_BODY + 7, "Null"));
 		case 6:
 			classBody6.setMovementMethod(new ScrollingMovementMethod());
 			classTitle6.setText(settings.getString(CLASS_TITLE + 6, "Null"));
 			classBody6.setText(settings.getString(CLASS_BODY + 6, "Null"));
 		case 5:
 			classBody5.setMovementMethod(new ScrollingMovementMethod());
 			classTitle5.setText(settings.getString(CLASS_TITLE + 5, "Null"));
 			classBody5.setText(settings.getString(CLASS_BODY + 5, "Null"));
 		case 4:
 			classBody4.setMovementMethod(new ScrollingMovementMethod());
 			classTitle4.setText(settings.getString(CLASS_TITLE + 4, "Null"));
 			classBody4.setText(settings.getString(CLASS_BODY + 4, "Null"));
 		case 3:
 			classBody3.setMovementMethod(new ScrollingMovementMethod());
 			classTitle3.setText(settings.getString(CLASS_TITLE + 3, "Null"));
 			classBody3.setText(settings.getString(CLASS_BODY + 3, "Null"));
 		case 2:
 			classBody2.setMovementMethod(new ScrollingMovementMethod());
 			classTitle2.setText(settings.getString(CLASS_TITLE + 2, "Null"));
 			classBody2.setText(settings.getString(CLASS_BODY + 2, "Null"));
 		default:
 			classBody1.setMovementMethod(new ScrollingMovementMethod());
 			classTitle1.setText(settings.getString(CLASS_TITLE + 1, "Null"));
 			classBody1.setText(settings.getString(CLASS_BODY + 1, "Null"));
 			break;
 		}
 		style();
 	}
 
 	// Add colors and typefaces base on class status.
 	private void style() {
 		switch (numberOfClasses) {
 		case 8:
 			// UnFinished? If true, bold. If not, normal.
 			// Then add color!
 			if (settings.getBoolean(CLASS_STATUS + 8, true)) {
 				classTitle8.setTypeface(Typeface.DEFAULT_BOLD);
 				classTitle8.setBackgroundColor(TITLE_UNFINISHED);
 				classBody8.setBackgroundColor(BODY_UNFINISHED);
 			} else {
 				classTitle8.setBackgroundColor(TITLE_FINISHED);
 				classBody8.setBackgroundColor(BODY_FINISHED);
 			}
 		case 7:
 			if (settings.getBoolean(CLASS_STATUS + 7, true)) {
 				classTitle7.setTypeface(Typeface.DEFAULT_BOLD);
 				classTitle7.setBackgroundColor(TITLE_UNFINISHED);
 				classBody7.setBackgroundColor(BODY_UNFINISHED);
 			} else {
 				classTitle7.setBackgroundColor(TITLE_FINISHED);
 				classBody7.setBackgroundColor(BODY_FINISHED);
 			}
 		case 6:
 			if (settings.getBoolean(CLASS_STATUS + 6, true)) {
 				classTitle6.setTypeface(Typeface.DEFAULT_BOLD);
 				classTitle6.setBackgroundColor(TITLE_UNFINISHED);
 				classBody6.setBackgroundColor(BODY_UNFINISHED);
 			} else {
 				classTitle6.setBackgroundColor(TITLE_FINISHED);
 				classBody6.setBackgroundColor(BODY_FINISHED);
 			}
 		case 5:
 			if (settings.getBoolean(CLASS_STATUS + 5, true)) {
 				classTitle5.setTypeface(Typeface.DEFAULT_BOLD);
 				classTitle5.setBackgroundColor(TITLE_UNFINISHED);
 				classBody5.setBackgroundColor(BODY_UNFINISHED);
 			} else {
 				classTitle5.setBackgroundColor(TITLE_FINISHED);
 				classBody5.setBackgroundColor(BODY_FINISHED);
 			}
 		case 4:
 			if (settings.getBoolean(CLASS_STATUS + 4, true)) {
 				classTitle4.setTypeface(Typeface.DEFAULT_BOLD);
 				classTitle4.setBackgroundColor(TITLE_UNFINISHED);
 				classBody4.setBackgroundColor(BODY_UNFINISHED);
 			} else {
 				classTitle4.setBackgroundColor(TITLE_FINISHED);
 				classBody4.setBackgroundColor(BODY_FINISHED);
 			}
 		case 3:
 			if (settings.getBoolean(CLASS_STATUS + 3, true)) {
 				classTitle3.setTypeface(Typeface.DEFAULT_BOLD);
 				classTitle3.setBackgroundColor(TITLE_UNFINISHED);
 				classBody3.setBackgroundColor(BODY_UNFINISHED);
 			} else {
 				classTitle3.setBackgroundColor(TITLE_FINISHED);
 				classBody3.setBackgroundColor(BODY_FINISHED);
 			}
 		case 2:
 			if (settings.getBoolean(CLASS_STATUS + 2, true)) {
 				classTitle2.setTypeface(Typeface.DEFAULT_BOLD);
 				classTitle2.setBackgroundColor(TITLE_UNFINISHED);
 				classBody2.setBackgroundColor(BODY_UNFINISHED);
 			} else {
 				classTitle2.setBackgroundColor(TITLE_FINISHED);
 				classBody2.setBackgroundColor(BODY_FINISHED);
 			}
 		default:
 			if (settings.getBoolean(CLASS_STATUS + 1, true)) {
 				classTitle1.setTypeface(Typeface.DEFAULT_BOLD);
 				classTitle1.setBackgroundColor(TITLE_UNFINISHED);
 				classBody1.setBackgroundColor(BODY_UNFINISHED);
 			} else {
 				classTitle1.setBackgroundColor(TITLE_FINISHED);
 				classBody1.setBackgroundColor(BODY_FINISHED);
 			}
 			break;
 		}
 	}
 
 	// If settings is empty, populates preferences file to contain
 	// class titles, bodies, and statuses for debugging.
 	private void populateDebug() {
 		editor.putString(NUMBER_OF_CLASSES, "1");
 		editor.putString(REMINDER_TIMER, "1800000");
 
 		for (int i = 1; i < 9; i++) {
 			editor.putString(CLASS_TITLE + i, "Class " + i);
 		}
 		for (int j = 1; j < 9; j++) {
 			editor.putString(CLASS_BODY + j, "Assignments.");
 		}
 		for (int k = 1; k < 9; k++) {
 			editor.putBoolean(CLASS_STATUS + k, false);
 		}
 
 		editor.putBoolean(FIRST_RUN, false);
 		editor.commit();
 	}
 
 	// ###OnClick managers for Titles and Bodies of classes###
 	public void classTitle8Click(View v) {
 		flip(8);
 	}
 
 	public void classTitle7Click(View v) {
 		flip(7);
 	}
 
 	public void classTitle6Click(View v) {
 		flip(6);
 	}
 
 	public void classTitle5Click(View v) {
 		flip(5);
 	}
 
 	public void classTitle4Click(View v) {
 		flip(4);
 	}
 
 	public void classTitle3Click(View v) {
 		flip(3);
 	}
 
 	public void classTitle2Click(View v) {
 		flip(2);
 	}
 
 	public void classTitle1Click(View v) {
 		flip(1);
 	}
 
 	public void classBody8Click(View v) {
 		view(8);
 	}
 
 	public void classBody7Click(View v) {
 		view(7);
 	}
 
 	public void classBody6Click(View v) {
 		view(6);
 	}
 
 	public void classBody5Click(View v) {
 		view(5);
 	}
 
 	public void classBody4Click(View v) {
 		view(4);
 	}
 
 	public void classBody3Click(View v) {
 		view(3);
 	}
 
 	public void classBody2Click(View v) {
 		view(2);
 	}
 
 	public void classBody1Click(View v) {
 		view(1);
 	}
 
 	// Flips class status and applies new typeface and color.
 	public void flip(int i) {
 		if (settings.getBoolean(CLASS_STATUS + i, true)) {
 			editor.putBoolean(CLASS_STATUS + i, false);
 			switch (i) {
 			case 1:
 				classTitle1.setTypeface(Typeface.DEFAULT);
 				break;
 			case 2:
 				classTitle2.setTypeface(Typeface.DEFAULT);
 				break;
 			case 3:
 				classTitle3.setTypeface(Typeface.DEFAULT);
 				break;
 			case 4:
 				classTitle4.setTypeface(Typeface.DEFAULT);
 				break;
 			case 5:
 				classTitle5.setTypeface(Typeface.DEFAULT);
 				break;
 			case 6:
 				classTitle6.setTypeface(Typeface.DEFAULT);
 				break;
 			case 7:
 				classTitle7.setTypeface(Typeface.DEFAULT);
 				break;
 			case 8:
 				classTitle8.setTypeface(Typeface.DEFAULT);
 				break;
 			}
 		} else {
 			editor.putBoolean(CLASS_STATUS + i, true);
 			switch (i) {
 			case 1:
 				classTitle1.setTypeface(Typeface.DEFAULT_BOLD);
 				break;
 			case 2:
 				classTitle2.setTypeface(Typeface.DEFAULT_BOLD);
 				break;
 			case 3:
 				classTitle3.setTypeface(Typeface.DEFAULT_BOLD);
 				break;
 			case 4:
 				classTitle4.setTypeface(Typeface.DEFAULT_BOLD);
 				break;
 			case 5:
 				classTitle5.setTypeface(Typeface.DEFAULT_BOLD);
 				break;
 			case 6:
 				classTitle6.setTypeface(Typeface.DEFAULT_BOLD);
 				break;
 			case 7:
 				classTitle7.setTypeface(Typeface.DEFAULT_BOLD);
 				break;
 			case 8:
 				classTitle8.setTypeface(Typeface.DEFAULT_BOLD);
 				break;
 			}
 		}
 
 		editor.commit();
 		style();
 	}
 
 	public void view(int i) {
 		Intent intent = new Intent(this, EditActivity.class);
 		intent.putExtra("ID", i);
 		Log.d(TAG, "Starting EditActivity...");
 		startActivity(intent);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem menuItem) {
 		switch (menuItem.getItemId()) {
 		case R.id.menu_settings:
 			startActivity(new Intent(this, Preferences.class));
 			break;
 		}
 		return true;
 	}
 }

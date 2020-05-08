 package com.example.tomatroid;
 
 import java.util.ArrayList;
 
 import com.example.tomatroid.chrono.Counter;
 import com.example.tomatroid.dialog.AboutDialog;
 import com.example.tomatroid.digram.Axis;
 import com.example.tomatroid.digram.Bar;
 import com.example.tomatroid.sql.SQHelper;
 import com.example.tomatroid.util.AlarmReceiver;
 import com.example.tomatroid.util.NavigationBarManager;
 
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.provider.Settings;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.widget.Chronometer;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TableLayout;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	final int ACTIVITYNUMBER = 0;
 
 	public static final int TYPE_POMODORO = 0;
 	public static final int TYPE_SHORTBREAK = 1;
 	public static final int TYPE_LONGBREAK = 2;
 	public static final int TYPE_TRACKING = 3;
 	public static final int TYPE_SLEEPING = 4;
 	
 	public static final String COLOR_STRING_POMODORO = "#fdf700";
 	public static final String COLOR_STRING_BREAK = "#04B404";
 	public static final String COLOR_STRING_TRACKING = "#800080";
 	public static final String COLOR_STRING_SLEEP = "#A4A4A4";
 	public static final String COLOR_STRING_BLUE = "#6495ED";
 	public static final String COLOR_STRING_RED = "#DC143C";
 	
 	public static final int COLOR_POMODORO = Color.parseColor(COLOR_STRING_POMODORO);
 	public static final int COLOR_BREAK =Color.parseColor(COLOR_STRING_BREAK);
 	public static final int COLOR_TRACKING = Color.parseColor(COLOR_STRING_TRACKING);
 	public static final int COLOR_SLEEP = Color.parseColor(COLOR_STRING_SLEEP);
 	public static final int COLOR_BLUE = Color.parseColor(COLOR_STRING_BLUE);
 	public static final int COLOR_RED = Color.parseColor(COLOR_STRING_RED);
 	
 	LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
 			LayoutParams.MATCH_PARENT);
 
 	LayoutParams barParams = new TableLayout.LayoutParams(
 			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
 
 	SQHelper sqhelper = new SQHelper(this);
 
 	RelativeLayout digram;
 	LinearLayout headline;
 
 	Chronometer timeText;
 	TextView pomodorosNumText;
 	Counter counter;
 	ControlListener controlListener;
 	DialogManager dialogManager;
 
 	AlarmManager alarmmanager;
 	AlarmReceiver alarmreceiver;
 	
 	ArrayList<View> bars = new ArrayList<View>();
 
 	int pomodoroTime;
 	int shortBreakTime;
 	int longBreakTime;
 	int rememberTime;
 	String pomodoroTheme, breakTheme;
 	boolean tracking = false;
 	boolean airplanemode;
 	boolean firstStartUpTutorial = false;
 
 	int pomodorosNum = 1;
 	int pomodorosUntilLongBreakNum;
 
 	Bar pomodoroBar, breakBar, trackBar;
 	Axis axis;
 	SharedPreferences settings;
 
 	static final String KEY_THEME = "theme";
 	static final String KEY_CHRONOSTATE = "chrono";
 	static final String KEY_TRACKINGSTATE = "tracking";
 	static final String KEY_ACTIVEBUTTON = "button";
 	static final String KEY_POMODOROTHEME = "pomodoroTheme";
 	static final String KEY_BREAKTHEME = "breakTheme";
 
 	public static final String KEY_VIBRATE = "vibrate";
 	public static final String KEY_PLAYSOUND = "playsound";
 	static final String KEY_AIRAIRPLANEMODE = "airplanemode";
 	static final String KEY_APPVERSION = "appversion";
 	
 	static final String KEY_POMODOROTIME = "pomodorotime";
 	static final String KEY_SHORTBREAKTIME = "shortbreaktime";
 	static final String KEY_LONGBREAKTIME = "longbreaktime";
 	static final String KEY_POMODORO_UNTIL_BREAK = "pomodorountilbreak";
 	public static final String KEY_REMEMBERTIME = "remembertime";
 	static final String KEY_TAG = "tag";
 	static final String KEY_COUNTER = "counter";
 	static final String KEY_COUNTERPAUSETIME= "counterpausetime";
 	static final String KEY_COUNTERTIMEBASE = "countertimebase";
 	static final String KEY_COUNTERTIMELEFT = "countertimeleft";
 	static final String KEY_COUNTERCOUNTUP= "countercountup";
 
 	public static final String PREFS_NAME = "MyPrefsFile";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		NavigationBarManager navi = new NavigationBarManager(this,
 				ACTIVITYNUMBER);
 		
 		digram = (RelativeLayout) findViewById(R.id.digram);
 		headline = (LinearLayout) findViewById(R.id.headline);
 		timeText = (Chronometer) findViewById(R.id.timetext);
 //		ProgressBar progress = (ProgressBar) findViewById(R.id.progressbar);
 //		progress.setIndeterminate(false);
 //		progress.setMax(100);
 //		progress.setProgress(50);
 		pomodorosNumText = (TextView) findViewById(R.id.pomodorosNum);
 
 		pomodorosNum = sqhelper.getTodayCountOf(SQHelper.TYPE_POMODORO);
 		pomodorosNumText.setTextColor(COLOR_POMODORO);
 		pomodorosNumText.setText("" + pomodorosNum);
 
 		LinearLayout digramLayout = new LinearLayout(this);
 		digram.addView(digramLayout);
 		LinearLayout axisLayout = new LinearLayout(this);
 		digram.addView(axisLayout);
 
 		// Calculate for Bars
 		int startPomodoroTime = sqhelper.getTodaySumOf(SQHelper.TYPE_POMODORO);
 		int startBreakTime = sqhelper.getTodaySumOf(SQHelper.TYPE_SHORTBREAK) + sqhelper.getTodaySumOf(SQHelper.TYPE_LONGBREAK);
 		int startTrackingTime = sqhelper.getTodaySumOf(SQHelper.TYPE_TRACKING);
 		float maxStartTime = Math.max(startTrackingTime, Math.max(startPomodoroTime, startBreakTime));
 		int maxValue = (int) Math.max(60, ((maxStartTime / 100) * 125));
 
 		axis = new Axis(digram.getContext(), maxValue);
 		axisLayout.addView(axis);
 
 		pomodoroBar = new Bar(this, digram.getContext(), startPomodoroTime,
 				COLOR_POMODORO, maxValue);
 		digramLayout.addView(pomodoroBar, barParams);
 
 		breakBar = new Bar(this, digram.getContext(), startBreakTime,
 				COLOR_BREAK, maxValue);
 		digramLayout.addView(breakBar, barParams);
 
 		trackBar = new Bar(this, digram.getContext(), startTrackingTime,
 				COLOR_TRACKING, maxValue);
 		digramLayout.addView(trackBar, barParams);
 
 		controlListener = new ControlListener(this, sqhelper);
 		dialogManager = new DialogManager(this);
 
 		timeText.setText("00:00");
 		Typeface tf = Typeface.createFromAsset(getAssets(), "telegrama.otf");
 		// Typeface tf = Typeface.createFromAsset(getAssets(), "wwDigital.ttf");
 		timeText.setTypeface(tf);
 		timeText.setTextColor(COLOR_BLUE);
 
 		controlListener.themePomodoroText.setText(pomodoroTheme);
 		controlListener.themeBreakText.setText(breakTheme);
 		
 		settings = getSharedPreferences(PREFS_NAME, 0);
 		try {
 			int oldVersionCode = settings.getInt(KEY_APPVERSION, 1);
 			int newVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
 			
 			if((oldVersionCode != newVersionCode) && !firstStartUpTutorial){
 				showAboutDialog(getString(R.string.update_dialog_title));
 				SharedPreferences.Editor editor = settings.edit();
 				editor.putInt(KEY_APPVERSION, newVersionCode);
 				editor.commit();
 			}
 			
 		} catch (NameNotFoundException e) {
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString(KEY_POMODOROTHEME, pomodoroTheme);
 		editor.putString(KEY_BREAKTHEME, breakTheme);
 		editor.putInt(KEY_TAG, controlListener.activeButton);
 		
 		if(tracking){
 			long elapsedMillis = timeText.getBase();
 			editor.putLong(KEY_CHRONOSTATE, elapsedMillis);
 		}
 		editor.putBoolean(KEY_TRACKINGSTATE, tracking);
 
 		if (counter != null) {
 			counter.cancel();
 			saveCounterState(editor);
 			AlarmReceiver.startAlarmManager(this, counter.getMilliesLeft(), controlListener.activeButton);
 		} else {
 			editor.putBoolean(KEY_COUNTER, false);
 		}
 
 		editor.commit();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		controlListener.themeListAdapter.getCursor().requery();
 		controlListener.themeListAdapter.notifyDataSetChanged();
 		getActionBar().setSelectedNavigationItem(ACTIVITYNUMBER);
 		
 		pomodoroTheme = settings.getString(KEY_POMODOROTHEME, sqhelper.getTheme(1));
 		breakTheme = settings.getString(KEY_BREAKTHEME, sqhelper.getTheme(1));
 		pomodoroTime = settings.getInt(KEY_POMODOROTIME, 25);
 		shortBreakTime = settings.getInt(KEY_SHORTBREAKTIME, 5);
 		longBreakTime = settings.getInt(KEY_LONGBREAKTIME, 35);
 		pomodorosUntilLongBreakNum = settings.getInt(KEY_POMODORO_UNTIL_BREAK, 4);
 		rememberTime = settings.getInt(KEY_REMEMBERTIME, 10);
 		airplanemode = settings.getBoolean(KEY_AIRAIRPLANEMODE, false);
 		
 		controlListener.themePomodoroText.setText(pomodoroTheme);
 		controlListener.themeBreakText.setText(breakTheme);
 		int tag = settings.getInt(KEY_TAG, -1);
 		controlListener.toogle(tag);
 
 		if(tag == TYPE_TRACKING){
 			timeText.setTextColor(COLOR_TRACKING);
		} else if( tag == TYPE_LONGBREAK || tag == TYPE_SHORTBREAK){
 			timeText.setTextColor(COLOR_BREAK);
 		} else if(tag == TYPE_SLEEPING){
 			timeText.setTextColor(COLOR_SLEEP);
 		}
 		
 		tracking = settings.getBoolean(KEY_TRACKINGSTATE, false);
 		if (tracking) {
 			timeText.setBase(settings.getLong(KEY_CHRONOSTATE, 0));
 			controlListener.toogle(settings.getInt(KEY_ACTIVEBUTTON, -1));
 			timeText.start();
 		}
 		
 		if(settings.getBoolean(KEY_COUNTER, false)){
 			AlarmReceiver.stopAlarmManager(this);
 			counter = loadCounterState(settings);
 			counter.start();
 		}
 		
 		// If the Activity was startet over notification, the counterFinish should not vibrate.
 //		vibrate = settings.getBoolean(AlarmReceiver.KEY_VIBRATE, false);
 		AlarmReceiver.cancelNotification(this);
 	}
 	
 	public void saveCounterState(SharedPreferences.Editor editor){
 		editor.putBoolean(KEY_COUNTER, true);
 		editor.putLong(KEY_COUNTERPAUSETIME, SystemClock.elapsedRealtime());
 		editor.putLong(KEY_COUNTERTIMELEFT, counter.getMilliesLeft());
 		editor.putLong(KEY_COUNTERTIMEBASE, counter.getMilliesRawBase()+counter.getMilliesPast());
 		editor.putBoolean(KEY_COUNTERCOUNTUP, counter.isCountUp());
 	}
 	
 	public Counter loadCounterState(SharedPreferences settings){
 		long milliesSincePause = SystemClock.elapsedRealtime() - settings.getLong(KEY_COUNTERPAUSETIME, SystemClock.elapsedRealtime());
 		
 		Counter counter = new Counter(settings.getLong(KEY_COUNTERTIMELEFT, 10000)-milliesSincePause, this, timeText, settings.getInt(KEY_TAG, 0));
 		counter.setBaseTime(settings.getLong(KEY_COUNTERTIMEBASE, 0)+milliesSincePause);
 
 		if(settings.getBoolean(KEY_COUNTERCOUNTUP, false)){
 			counter.toggleCountUp();
 		}
 		return counter;
 	}
 
 	public void startCounter(int minutes, int type) {
 		if (counter != null)
 			counter.cancel();
 		counter = new Counter(minutes, this, timeText, type);
 		counter.start();
 	}
 
 	public void stopCounter() {
 		if (counter != null)
 			counter.cancel();
 		counter = null;
 	}
 
 	public void counterFinish(int tag) {
 		long milliesBase = counter.getMilliesBase();
 		Counter newCounter = new Counter(rememberTime, this, timeText, tag);
 		newCounter.toggleCountUp();
 		newCounter.setBaseTime(milliesBase);
 		newCounter.start();
 
 //		if(vibrate){
 //			AlarmReceiver.fireVibration(this);
 //		}
 //		AlarmReceiver.setVibration(this, true);
 		
 		counter = newCounter;
 //		AlarmReceiver.startAlarmManager(this, counter.getMilliesLeft(), controlListener.activeButton);
 		dialogManager.show(tag, checkOnLongBreak());
 	}
 
 	/**
 	 * Start the appropiate counter.
 	 * 
 	 * @param tag
 	 */
 	public void start(int tag) {
 		switch (tag) {
 		case TYPE_POMODORO:
 			startCounter(pomodoroTime, tag);
 			break;
 		case TYPE_SHORTBREAK:
 			startCounter(shortBreakTime, tag);
 			timeText.setTextColor(COLOR_BREAK);
 			break;
 		case TYPE_LONGBREAK:
 			startCounter(longBreakTime, tag);
 			timeText.setTextColor(COLOR_BREAK);
 			break;
 		case TYPE_TRACKING:
 			timeText.setBase(SystemClock.elapsedRealtime());
 			timeText.start();
 			timeText.setTextColor(COLOR_TRACKING);
 			tracking = true;
 			break;
 		case TYPE_SLEEPING:
 			timeText.setBase(SystemClock.elapsedRealtime());
 			timeText.start();
 			timeText.setTextColor(COLOR_SLEEP);
 			tracking = true;
 			if(airplanemode)
 				airplanemode(true);
 			break;
 		}
 	}
 
 	/**
 	 * Cancel the counter. Write minutes past to database.
 	 * 
 	 * @param tag
 	 */
 	public void end(int tag) {
 		int minutes;
 		if (tracking) {
 			timeText.stop();
 			long myElapsedMillis = SystemClock.elapsedRealtime()
 					- timeText.getBase();
 			minutes = (int) (myElapsedMillis / 60000);
 			tracking = false;
 		} else {
 			minutes = counter.getMinutesPast();
 			stopCounter();
 		}
 
 		resetTimeText();
 
 		if (tag == TYPE_SLEEPING) {
 			if(airplanemode)
 				airplanemode(false);
 		}
 		
 		// #######
 //		 minutes = 10;
 		// #######
 		
 		if (minutes > 0) {
 			if (tag == TYPE_POMODORO) {
 				pomodorosNum++;
 				pomodorosNumText.setText("" + pomodorosNum);
 				pomodoroBar.addValue(minutes);
 				sqhelper.insertDate(tag, minutes, pomodoroTheme);
 			} else if (tag == TYPE_SHORTBREAK) {
 				breakBar.addValue(minutes);
 				sqhelper.insertDate(tag, minutes, "");
 			} else if (tag == TYPE_LONGBREAK) {
 				breakBar.addValue(minutes);
 				sqhelper.insertDate(tag, minutes, breakTheme);
 			} else if (tag == TYPE_TRACKING) {
 				trackBar.addValue(minutes);
 				sqhelper.insertDate(tag, minutes, breakTheme);
 			} else if (tag == TYPE_SLEEPING) {
 				sqhelper.insertDate(tag, minutes, "");
 			}
 		}
 	}
 
 	/**
 	 * Cancel the current counter.
 	 * 
 	 * @param tag
 	 */
 	public void stop() {
 		if (tracking) {
 			timeText.stop();
 			tracking = false;
 		} else {
 			stopCounter();
 		}
 		resetTimeText();
 	}
 
 	public void resetTimeText() {
 		timeText.setTextColor(COLOR_BLUE);
 		timeText.setText("00:00");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			Intent i = new Intent(this, SettingsActivity.class);
 			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(i);
 			break;
 		case R.id.delete_history:
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("This will erase all data");
 			builder.setCancelable(true);
 			builder.setPositiveButton("I agree", new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					sqhelper.renewTables();
 					onCreate(null);
 				}
 			});
 			builder.setNegativeButton("No, no", new OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 				}
 
 			});
 			AlertDialog dialog = builder.create();
 			dialog.show();
 
 			break;
 		case R.id.menu_void:
 			stop();
 			controlListener.stop();
 			break;
 		case R.id.menu_about:
 			showAboutDialog(getString(R.string.about));
 			break;
 		default:
 			break;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	public boolean checkOnLongBreak() {
 		if (pomodorosNum == 0)
 			return false;
 		if ((pomodorosNum + 1) % pomodorosUntilLongBreakNum == 0)
 			return true;
 		return false;
 	}
 	
 	public void airplanemode(boolean bool){
 		boolean isEnabled = Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
 		
 		if(bool != isEnabled){
 			Log.e("MainActiviry", "toogle airplanemode");
 			Settings.System.putInt(getBaseContext().getContentResolver(), Settings.System.AIRPLANE_MODE_ON, bool ? 1 : 0); 
 			Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
 			intent.putExtra("state", bool ? 1 : 0);
 			sendBroadcast(intent);
 		}
 	}
 	
 	public void showAboutDialog(String title){
 		AboutDialog about = new AboutDialog(this);
 //		about.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.btn_dialog);
 		about.setTitle(title);
 		about.show();
 	}
 
 	public void barExceededLimit(int oldMax) {
 		float cal = ((float) oldMax / 100f) * 125f;
 		int newMax = (int) cal;
 		pomodoroBar.adjustToNewMaximum(newMax);
 		breakBar.adjustToNewMaximum(newMax);
 		trackBar.adjustToNewMaximum(newMax);
 		axis.adjustToNewMaximum(newMax);
 	}
 }

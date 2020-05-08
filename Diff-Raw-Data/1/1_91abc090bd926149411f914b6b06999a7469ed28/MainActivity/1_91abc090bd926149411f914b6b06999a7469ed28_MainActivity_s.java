 package com.example.tomatroid;
 
 import java.util.ArrayList;
 
 import com.example.tomatroid.chrono.Counter;
 import com.example.tomatroid.digram.Axis;
 import com.example.tomatroid.digram.Bar;
 import com.example.tomatroid.sql.SQHelper;
 import com.example.tomatroid.util.NavigationBarManager;
 
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.os.Vibrator;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Chronometer;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TableLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	final int ACTIVITYNUMBER = 0; 
 	
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
 
 	ArrayList<View> bars = new ArrayList<View>();
 
 	int pomodoroTime;
 	int shortBreakTime;
 	int longBreakTime;
 	int rememberTime;
 	String pomodoroTheme, breakTheme;
 	boolean tracking = false;
 
 	int pomodorosNum = 1;
 	int pomodorosUntilLongBreakNum = 4;
 
 	Bar pomodoroBar, breakBar, trackBar;
 	Axis axis;
 
 	final String KEY_THEME = "theme";
 	final String KEY_CHRONOSTATE = "chrono";
 	final String KEY_TRACKINGSTATE = "tracking";
 	final String KEY_ACTIVEBUTTON = "button";
 	final String KEY_POMODOROTHEME = "pomodoroTheme";
 	final String KEY_BREAKTHEME = "breakTheme";
 	
 	final String KEY_POMODOROTIME = "pomodorotime";
 	final String KEY_SHORTBREAKTIME = "shortbreaktime";
 	final String KEY_LONGBREAKTIME = "longbreaktime";
 	final String KEY_REMEMBERTIME = "remembertime";
 	
 	public static final String PREFS_NAME = "MyPrefsFile";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		NavigationBarManager navi = new NavigationBarManager(this, ACTIVITYNUMBER);
 		
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		pomodoroTheme = settings.getString(KEY_POMODOROTHEME, sqhelper.getTheme(1));
 		breakTheme = settings.getString(KEY_BREAKTHEME, sqhelper.getTheme(1));
 		pomodoroTime = settings.getInt(KEY_POMODOROTIME, 25);
 		shortBreakTime = settings.getInt(KEY_SHORTBREAKTIME, 5);
 		longBreakTime = settings.getInt(KEY_LONGBREAKTIME, 35);
 		rememberTime = settings.getInt(KEY_REMEMBERTIME, 10);
 
 		digram = (RelativeLayout) findViewById(R.id.digram);
 		headline = (LinearLayout) findViewById(R.id.headline);
 		timeText = (Chronometer) findViewById(R.id.timetext);
 		pomodorosNumText = (TextView) findViewById(R.id.pomodorosNum);
 
 		pomodorosNum = sqhelper.getStartUpPomodoroCount();
 		pomodorosNumText.setTextColor(Color.parseColor("#fdf700"));
 		pomodorosNumText.setText("" + pomodorosNum);
 
 		LinearLayout digramLayout = new LinearLayout(this);
 		digram.addView(digramLayout);
 		LinearLayout axisLayout = new LinearLayout(this);
 		digram.addView(axisLayout);
 
 		// Calculate for Bars
 		int startPomodoroTime = sqhelper.getStartUpPomodoroTime();
 		int startBreakTime = sqhelper.getStartUpBreakTime();
 		int startTrackingTime = sqhelper.getStartUpTrackingCount();
 		float maxStartTime = Math.max(startPomodoroTime, startBreakTime);
 		int maxValue = (int) Math.max(60, ((maxStartTime / 100) * 125));
 
 		axis = new Axis(digram.getContext(), maxValue);
 		axisLayout.addView(axis);
 
 		pomodoroBar = new Bar(this, digram.getContext(), startPomodoroTime,
 				"#fdf700", maxValue);
 		digramLayout.addView(pomodoroBar, barParams);
 
 		breakBar = new Bar(this, digram.getContext(), startBreakTime,
 				"#04B404", maxValue);
 		digramLayout.addView(breakBar, barParams);
 
 		trackBar = new Bar(this, digram.getContext(), startTrackingTime,
 				"#800080", maxValue);
 		digramLayout.addView(trackBar, barParams);
 		
 		controlListener = new ControlListener(this, sqhelper);
 		dialogManager = new DialogManager(this);
 
 		timeText.setText("00:00");
 		Typeface tf = Typeface.createFromAsset(getAssets(), "telegrama.otf");
 //		Typeface tf = Typeface.createFromAsset(getAssets(), "wwDigital.ttf");
 		timeText.setTypeface(tf);
 		timeText.setTextColor(Color.parseColor("#6495ED"));
 		
 		controlListener.themePomodoroText.setText(pomodoroTheme);
 		controlListener.themeBreakText.setText(breakTheme);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString(KEY_POMODOROTHEME, pomodoroTheme);
 		editor.putString(KEY_BREAKTHEME, breakTheme);
 		editor.putInt(KEY_POMODOROTIME, pomodoroTime);
 		editor.putInt(KEY_SHORTBREAKTIME, shortBreakTime);
 		editor.putInt(KEY_LONGBREAKTIME, longBreakTime);
 		editor.putInt(KEY_REMEMBERTIME, rememberTime);
 		editor.commit();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		controlListener.themeListAdapter.getCursor().requery();
 		controlListener.themeListAdapter.notifyDataSetChanged();
 		getActionBar().setSelectedNavigationItem(ACTIVITYNUMBER);
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putString(KEY_POMODOROTHEME, pomodoroTheme);
 		outState.putString(KEY_BREAKTHEME, breakTheme);
 
 		long elapsedMillis = timeText.getBase();
 		outState.putLong(KEY_CHRONOSTATE, elapsedMillis);
 		outState.putBoolean(KEY_TRACKINGSTATE, tracking);
 		outState.putInt(KEY_ACTIVEBUTTON, controlListener.activeButton);
 	}
 	
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		pomodoroTheme = savedInstanceState.getString(KEY_POMODOROTHEME);
 		breakTheme = savedInstanceState.getString(KEY_BREAKTHEME);
 		controlListener.themePomodoroText.setText(pomodoroTheme);
 		controlListener.themeBreakText.setText(breakTheme);
 
 		tracking = savedInstanceState.getBoolean(KEY_TRACKINGSTATE);
 		if (tracking) {
 			timeText.setBase(savedInstanceState.getLong(KEY_CHRONOSTATE));
 			controlListener.toogle(savedInstanceState.getInt(KEY_ACTIVEBUTTON));
 			timeText.start();
 		}
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
 	}
 
 	public synchronized void counterFinish(int tag) {
 		resetTimeText();
 		int minutespast = counter.getMinutesPast();
 		long milliesBase = counter.getMilliesBase();
 
 		Counter newCounter = new Counter(rememberTime, this, timeText, tag);
 		newCounter.toggleCountUp();
 		newCounter.setBaseTime(milliesBase);
 
 		// timeText = null;
 		// context = null;
 		// mA = null;
 		newCounter.start();
 
 		// // SOS
 		int dot = 200; // Length of a Morse Code "dot" in milliseconds
 		// int dash = 500; // Length of a Morse Code "dash" in milliseconds
 		int short_gap = 200; // Length of Gap Between dots/dashes
 		// int medium_gap = 500; // Length of Gap Between Letters
 		// int long_gap = 1000; // Length of Gap Between Words
 		// long[] pattern = { 0, // Start immediately
 		// dot, short_gap, dot, short_gap, dot };
 		long[] pattern = { 0, // Start immediately
 				dot };
 
 		Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
 		// Only perform this pattern one time (-1 means "do not repeat")
 		v.vibrate(pattern, -1);
 
 		counter = newCounter;
 
 		Toast.makeText(this, minutespast + "mins", Toast.LENGTH_SHORT).show();
 		dialogManager.show(tag, checkOnLongBreak());
 	}
 
 	/**
 	 * Start the appropiate counter.
 	 * 
 	 * @param tag
 	 */
 	public void start(int tag) {
 		switch (tag) {
 		// Pomodoro
 		case 0:
 			startCounter(pomodoroTime, tag);
 			Toast.makeText(this, "Start Pomodoro", Toast.LENGTH_SHORT).show();
 			break;
 		// Short Break
 		case 1:
 			startCounter(shortBreakTime, tag);
 			Toast.makeText(this, "Start ShortBreak", Toast.LENGTH_SHORT).show();
 			break;
 		// Long Break
 		case 2:
 			startCounter(longBreakTime, tag);
 			Toast.makeText(this, "Start LongBreak", Toast.LENGTH_SHORT).show();
 			break;
 		// Tracking
 		case 3:
 			timeText.setBase(SystemClock.elapsedRealtime());
 			timeText.start();
 			tracking = true;
 			break;
 		// Sleeping
 		case 4:
 			timeText.setBase(SystemClock.elapsedRealtime());
 			timeText.start();
 			tracking = true;
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
 			stopCounter();
 			minutes = counter.getMinutesPast();
 		}
 
 		resetTimeText();
 
 		NotificationCompat.Builder mBuilder =
 		        new NotificationCompat.Builder(this)
 		        .setSmallIcon(R.drawable.ic_launcher)
 		        .setContentTitle("My notification")
 		        .setContentText("Hello World!");
 		Intent resultIntent = new Intent(this, MainActivity.class);
 		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
 		stackBuilder.addParentStack(MainActivity.class);
 		stackBuilder.addNextIntent(resultIntent);
 		PendingIntent resultPendingIntent =
 		        stackBuilder.getPendingIntent(
 		            0,
 		            PendingIntent.FLAG_UPDATE_CURRENT
 		        );
 		mBuilder.setContentIntent(resultPendingIntent);
 		NotificationManager mNotificationManager =
 		    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		// mId allows you to update the notification later on.
 		mNotificationManager.notify(0, mBuilder.build());
 //		 #######
 //		 minutes = 10;
 //		 #######
 
 		if (minutes > 0) {
 			if (tag == 0) {
 				pomodorosNum++;
 				pomodorosNumText.setText("" + pomodorosNum);
 				pomodoroBar.addValue(minutes);
 				sqhelper.insertDate(tag, minutes, pomodoroTheme);
 			} else if (tag == 1 || tag == 2) {
 				breakBar.addValue(minutes);
 				sqhelper.insertDate(tag, minutes, breakTheme);
 			} else if(tag == 3){
 				trackBar.addValue(minutes);
 				sqhelper.insertDate(tag, minutes, breakTheme);
 			} else if(tag == 4){
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
 		timeText.setTextColor(Color.parseColor("#6495ED"));
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
 
 	public void barExceededLimit(int oldMax) {
 		float cal = ((float) oldMax / 100f) * 125f;
 		int newMax = (int) cal;
 		pomodoroBar.adjustToNewMaximum(newMax);
 		breakBar.adjustToNewMaximum(newMax);
 		axis.adjustToNewMaximum(newMax);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("This will end the activity");
 			builder.setCancelable(true);
 			builder.setPositiveButton("I agree", new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					if (counter != null)
 						counter.cancel();
 					MainActivity.this.finish();
 				}
 			});
 			builder.setNegativeButton("No, no", new OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 				}
 
 			});
 			AlertDialog dialog = builder.create();
 			dialog.show();
 			return true;
 		} else {
 			return super.onKeyDown(keyCode, event);
 		}
 	}
 	
 	@Override
 	protected void onDestroy() {
 		if (counter != null)
 			counter.cancel();
 		super.onDestroy();
 	}
 }

 /**
  *  Kitchen Clock
  *  Copyright (C) 2012 Alexander Pastukhov
  *  
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *  
  */
 package com.op.kclock;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.EditTextPreference;
 import android.preference.PreferenceManager;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TableLayout;
 import android.widget.TextView;
 
 import com.markupartist.android.widget.ActionBar.Action;
 import com.markupartist.android.widget.ActionBar.IntentAction;
 import com.markupartist.android.widget.ActionBar;
 import com.op.kclock.alarm.AlarmSingleServiceImpl;
 //import com.op.kclock.alarm.AlarmService;
 //import com.op.kclock.alarm.AlarmServiceImpl;
 import com.op.kclock.alarm.WakeUpLock;
 import com.op.kclock.cookconst.SettingsConst;
 import com.op.kclock.dialogs.TimePickDialog;
 import com.op.kclock.misc.Log;
 import com.op.kclock.model.AlarmClock;
 import com.op.kclock.model.AlarmClock.TimerState;
 import com.op.kclock.ui.TextViewWithMenu;
 import com.op.kclock.utils.DBHelper;
 
 public class MainActivity extends Activity implements OnClickListener,
 		OnSharedPreferenceChangeListener {
 
 	public static final String SMALLFIRST = "smallfirst";
 	public static final String UNSORTED = "unsorted";
 	public static final String RUNNEDFIRST = "runnedfirst";
 	private TimePickDialog timePickDialog = null;
 	public final static String TAG = "AlarmaClockActivity";
 	private static NotificationManager mNotificationManager;
 	private SharedPreferences mPrefs;
 	private ActionBar actionBar;
 	private List<AlarmClock> alarmList = new ArrayList<AlarmClock>();
 
 	// ACTIONBAR actions
 	private Action settingsButtonAction;
 	private Action delallAction;
 	private Action addButtonAction;
 	private Action refreshButtonAction;
 	private Action presetsButtonAction;
 	private Action runAllButtonAction;
 
 	private Handler handler;
 	private static Thread thread;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.alarmclock);
 
 		mPrefs = PreferenceManager.getDefaultSharedPreferences(this
 				.getApplicationContext());
 		mPrefs.registerOnSharedPreferenceChangeListener(this);
 
 		// Eula.show(this);
 		// Changelog.show(this);
 		initActionBar();
 
 		if (alarmList == null) {
 			alarmList = new ArrayList<AlarmClock>();
 		} 
 		
 
 		Log.d(TAG, "start");
 		WakeUpLock.acquire(this);
 
 		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
 		notification();
 
 		if (this.getIntent() != null) {
 			AlarmClock alarm = (AlarmClock) this.getIntent()
 					.getParcelableExtra("alarm_extra");
 			if (alarm != null) {
 				
 				boolean needAdd = true;
 				for (AlarmClock calarm : alarmList) {
 					if (calarm.getId() == alarm.getId()){
 						needAdd = false;
 						break;
 					}
 				}
 				if (needAdd){
 					alarm.setElement(null);
 					alarm.setContext(getApplicationContext());
 					deleteAllAlarms(false);
 					addAlarm(alarm);
 				}
 			}
 		}
 		
 		if (alarmList.size() > 0) {
 
 			drawAlarms();
 		} else {
 			if (mPrefs.getBoolean(
 					getApplicationContext().getString(
 							R.string.pref_savesession_key), false)) {
 				Log.d(TAG, "db read true!!");
 				DBHelper alarmClockDAO = new DBHelper(getApplicationContext());
 				alarmList = alarmClockDAO.getAlarmsList();
 				drawAlarms();
 				alarmClockDAO.close();
 			}
 
 			if (mPrefs.getBoolean(
 					getApplicationContext().getString(
 							R.string.pref_addalarmonstart_key), true)) {
 				addAlarmDialog();
 			}
 		}
 		
 		AlarmSingleServiceImpl alarmService = new AlarmSingleServiceImpl(this,
 				handler, alarmList);
 		if (thread == null || (thread != null && thread.getState() != Thread.State.RUNNABLE)) {
 			thread = new Thread(alarmService);
 			thread.start();
 		}
 
 	}
 
 	private void initActionBar() {
 		actionBar = (ActionBar) findViewById(R.id.actionbar);
 		actionBar
 				.setTitle(getApplicationContext().getString(R.string.app_name));
 
 		settingsButtonAction = new IntentAction(this, new Intent(this,
 				SettingsActivity.class), R.drawable.ic_menu_preferences);
 		presetsButtonAction = new IntentAction(this, new Intent(this,
 				PresetsActivity.class), R.drawable.ic_menu_list);
 		delallAction = new Action() {
 			@Override
 			public void performAction(View view) {
 				deleteAllAlarms(true);
 			}
 
 			@Override
 			public int getDrawable() {
 				return R.drawable.ic_menu_delete;
 			}
 		};
 		addButtonAction = new Action() {
 			@Override
 			public void performAction(View view) {
 				addAlarmDialog();
 			}
 
 			@Override
 			public int getDrawable() {
 				return R.drawable.ic_menu_add;
 			}
 		};
 		refreshButtonAction = new Action() {
 			@Override
 			public void performAction(View view) {
 				refreshAllAlarms();
 			}
 
 			@Override
 			public int getDrawable() {
 				return R.drawable.ic_menu_refresh;
 			}
 		};
 		runAllButtonAction = new Action() {
 			@Override
 			public void performAction(View view) {
 				runAllTimers();
 			}
 
 			@Override
 			public int getDrawable() {
 				return R.drawable.ic_menu_play;
 			}
 		};
 
 		if (mPrefs
 				.getBoolean(
 						getApplicationContext().getString(
 								R.string.pref_showaddbtn_key), true)) {
 			actionBar.addAction(addButtonAction);
 		}
 		if (mPrefs.getBoolean(
 				getApplicationContext()
 						.getString(R.string.pref_showsettbtn_key), false)) {
 			actionBar.addAction(settingsButtonAction);
 		}
 		if (mPrefs
 				.getBoolean(
 						getApplicationContext().getString(
 								R.string.pref_showdelall_key), false)) {
 			actionBar.addAction(delallAction);
 		}
 		if (mPrefs.getBoolean(
 				getApplicationContext().getString(
 						R.string.pref_showrefreshbtn_key), false)) {
 			actionBar.addAction(refreshButtonAction);
 		}
 		if (mPrefs.getBoolean(
 				getApplicationContext().getString(
 						R.string.pref_showpresetsbtn_key), true)) {
 			actionBar.addAction(presetsButtonAction);
 		}
 		if (mPrefs.getBoolean(
 				getApplicationContext()
 						.getString(R.string.pref_showplaybtn_key), true)) {
 			actionBar.addAction(runAllButtonAction);
 		}
 
 	}
 
 	public void appendAddButton() {
 		LinearLayout mainL = (LinearLayout) findViewById(R.id.alarm_layout);
 		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 		LinearLayout itemView = (LinearLayout) inflater.inflate(
 				R.layout.alarm_incl, null);
 		TextViewWithMenu txtView = (TextViewWithMenu) itemView.getChildAt(1);
 		txtView.setText(R.string.add);
 		// itemView.setId(121212);
 
 		if (!mPrefs.getBoolean(
 				getApplicationContext()
 						.getString(R.string.pref_showsettbtn_key), false)) {
 			itemView.setVisibility(View.GONE);
 		}
 
 		itemView.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				addAlarmDialog();
 
 			}
 
 		});
 		mainL.addView(itemView, new TableLayout.LayoutParams(
 				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 	}
 
 	public void notification() {
 		int icon = R.drawable.stat_notify_alarm;
 		CharSequence mTickerText = getString(R.string.timer_started);
 		long when = System.currentTimeMillis();
 		Notification notification = new Notification(icon, mTickerText, when);
 		CharSequence mContentTitle = getString(R.string.app_name);
 		CharSequence mContentText = getString(R.string.click_to_open);
 		Intent clickIntent = new Intent(this, MainActivity.class);
 		clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
 				| Intent.FLAG_ACTIVITY_SINGLE_TOP
 				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 				clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 		notification.setLatestEventInfo(getApplicationContext(), mContentTitle,
 				mContentText, contentIntent);
 		notification.ledARGB = 0x00000000;
 		notification.ledOnMS = 0;
 		notification.ledOffMS = 0;
 		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
 		notification.flags |= Notification.FLAG_ONGOING_EVENT;
 		notification.flags |= Notification.FLAG_NO_CLEAR;
 
 		mNotificationManager.notify(SettingsConst.APP_NOTIF_ID, notification);
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		int height = getWindowManager().getDefaultDisplay().getHeight();
 		for (AlarmClock alarm : alarmList) {
 			updateAlarmSize(alarm);
 		}
 		if (timePickDialog != null && timePickDialog.isShowing()) {
 			LinearLayout subscr = (LinearLayout) timePickDialog
 					.findViewById(R.id.pick_text);
 			if (height < 500) {
 				subscr.setVisibility(View.GONE);
 			} else {
 				subscr.setVisibility(View.VISIBLE);
 			}
 		}
 
 		stopAllActiveAlarms();
 	}
 
 	public void stopAllActiveAlarms() {
 		if (mPrefs.getBoolean(
 				getApplicationContext().getString(
 						R.string.pref_stoponrotate_key), true)) {
 			for (AlarmClock alarm : alarmList) {
 				if (alarm.getState() == AlarmClock.TimerState.ALARMING) {
 					alarm.alarmSTOP();
 				}
 			}
 		}
 	}
 
 	protected void runAllTimers() {
 		for (AlarmClock alarm : alarmList) {
 			if (alarm.getState() != AlarmClock.TimerState.ALARMING
 					&& alarm.getTime() > 0) {
 				alarm.setState(TimerState.RUNNING);
 			}
 		}
 	}
 
 	// Store the instance of an object
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 
 		Log.d(TAG, "retain");
 
 		if (alarmList != null) // Check that the object exists
 			return (alarmList);
 		return super.onRetainNonConfigurationInstance();
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		
 
 		Log.d(TAG, "MainActivity: onStart()");
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 
 
 		WakeUpLock.acquire(this);
 		if (alarmList.size() == 0) {
 			if (mPrefs.getBoolean(
 					getApplicationContext().getString(
 							R.string.pref_addalarmonstart_key), true)) {
 
 				addAlarmDialog();
 			}
 		}
 		Log.d(TAG, "MainActivity: onResume()");
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (isTimerActive())
 			WakeUpLock.release();
 		Log.d(TAG, "MainActivity: onPause()");
 	}
 
 	private boolean isTimerActive() {
 		for (AlarmClock alarm : alarmList) {
 			if (alarm.getState() != AlarmClock.TimerState.RUNNING) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		// save to db
 
 		Log.d(TAG, "MainActivity: onStop()");
 	}
 
 	/**
 	 * this method sets alarm manager to try wake up on given time
 	 * 
 	 * @param time
 	 *            when to try wake up next time
 	 */
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if (mPrefs.getBoolean(
 				getApplicationContext()
 						.getString(R.string.pref_savesession_key), true)) {
 			DBHelper dbHelper = new DBHelper(getApplicationContext());
 			dbHelper.open();
 			// select min alarm and make caller
 			dbHelper.truncateAlarms();
 			// HistoryDAO historyDAO = new HistoryDAO(getApplicationContext());
 			for (AlarmClock alarm : alarmList) {
 				dbHelper.insertAlarm(alarm);
 			}
 			dbHelper.close();
 		}
 		alarmList.clear();
 		Log.d(TAG, "MainActivity: onDestroy()");
 	}
 
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		Log.d("oo", "onrestore");
 		super.onRestoreInstanceState(savedInstanceState);
 		// alarmList =
 		// savedInstanceState.getParcelableArrayList("SAVE_SELECTED");
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		Log.d("oo", "onsave");
 		super.onSaveInstanceState(savedInstanceState);
 		// savedInstanceState.putParcelableArrayList("SAVE_SELECTED",
 		// alarmList);
 	}
 
 	private void drawAlarms() {
 		// sort
 		String sortType = mPrefs.getString(
 				getApplicationContext().getString(R.string.pref_sortlist_key),
 				UNSORTED);
 		if (sortType.equals(RUNNEDFIRST)) {
 			AlarmClock.ActiveFirstComparator comparator = new AlarmClock.ActiveFirstComparator();
 			Collections.sort(alarmList, comparator);
 		} else if (sortType.equals(SMALLFIRST)) {
 			AlarmClock.NearestActiveFirstComparator comparator = new AlarmClock.NearestActiveFirstComparator();
 			Collections.sort(alarmList, comparator);
 		}
 		for (AlarmClock alarm : alarmList) {
 			drawAlarm(alarm);
 		}
 	}
 
 	private void addAlarm(AlarmClock newAlarm) {
 		if (newAlarm.getElement() == null) {
 			drawAlarm(newAlarm);
 			if(newAlarm.getElement() != null){
 				alarmList.add(newAlarm);			
 			}
 		}
 	}
 
 	private LinearLayout drawAlarm(AlarmClock alarm) {
 		LinearLayout mainL = (LinearLayout) findViewById(R.id.alarm_layout);
 		boolean isnew = false;
 		Log.e("dd", "drawalarm");
 		if (alarm.getElement() == null) {
 			isnew = true;
 			LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 			LinearLayout itemView = (LinearLayout) inflater.inflate(
 					R.layout.alarm_incl, null);
 			alarm.setElement(itemView);
 			mainL.addView(alarm.getElement(),// mainL.getChildCount() - 1
 					new TableLayout.LayoutParams(LayoutParams.FILL_PARENT,
 							LayoutParams.WRAP_CONTENT));
 			TextViewWithMenu textView = (TextViewWithMenu) (alarm.getWidget());
 			textView.setAlarm(alarm);
 		} else {
 			if (alarm.getElement().getParent() == null) {
 				mainL.addView(alarm.getElement(), new TableLayout.LayoutParams(
 						LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 			}
 		}
 		alarm.updateElement();
 		alarm.getElement().setOnClickListener(this);
 		Log.e("dd", "drawalarme3");
 
 		if (!mPrefs.getBoolean(
 				getApplicationContext().getString(R.string.pref_shownames_key),
 				false)) {
 			final TextView widgetLbl = (TextView) alarm.getElement()
 					.getChildAt(0);
 			widgetLbl.setVisibility(View.INVISIBLE);
 		}
 		Log.e("dd", "drawalarm4");
 
 		if (alarm.getState() == AlarmClock.TimerState.STOPPED) {
 			if (mPrefs.getBoolean(
 					getApplicationContext().getString(
 							R.string.pref_autostart_key), true)) {
 				if (alarm.getState() != AlarmClock.TimerState.PAUSED
 						&& alarm.getTime() > 0) {
 					alarm.setState(AlarmClock.TimerState.RUNNING);
 				}
 
 			} else {
 				alarm.setState(AlarmClock.TimerState.PAUSED);
 			}
 		}
 
 		registerForContextMenu(alarm.getWidget());
 		// add the itemView
 		alarm.updateState();
 
 		updateAlarmSize(alarm);
 
 		if (alarm.getElement() == null) throw new IllegalArgumentException();
 		return alarm.getElement();
 	}
 
 	private void updateAlarmSize(AlarmClock alarm) {
 		int width = getWindowManager().getDefaultDisplay().getWidth();
 		alarm.getWidget().setTextSize(width / 8);
 		((TextView) alarm.getElement().getChildAt(0)).setTextSize(width / 24);
 	}
 
 	// ON-CLICK
 
 	public void onClick(View v) {
 
 		for (AlarmClock alarm : alarmList) {
 			TextViewWithMenu tvTimer = (TextViewWithMenu) alarm.getElement()
 					.getChildAt(1);
 			if (tvTimer == v) {
 				if (alarm.getState() == AlarmClock.TimerState.RUNNING) {
 					alarm.setState(AlarmClock.TimerState.PAUSED);
 				} else if (alarm.getState() == AlarmClock.TimerState.PAUSED) {
 					alarm.setState(AlarmClock.TimerState.RUNNING);
 				} else if (alarm.getState() == AlarmClock.TimerState.ALARMING) {
 					alarm.alarmSTOP();
 				}
 				break;
 			}
 		}
 
 		String sortType = mPrefs.getString(
 				getApplicationContext().getString(R.string.pref_sortlist_key),
 				UNSORTED);
 		if (sortType != UNSORTED) {
 			LinearLayout mainL = (LinearLayout) findViewById(R.id.alarm_layout);
 			mainL.removeAllViews();
 			this.drawAlarms();
 		}
 	}
 
 	// ============================================================
 	// ==================== MENUS ===============================
 	// ============================================================
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		new MenuInflater(this).inflate(R.menu.alarm_clock_options, menu);
 		return (super.onCreateOptionsMenu(menu));
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenu.ContextMenuInfo menuInfo) {
 		MenuInflater inflater = getMenuInflater();
 		if (v.getId() == 2) {
 
 		}
 		inflater.inflate(R.menu.alarm_context, menu);
 		super.onCreateContextMenu(menu, v, menuInfo);
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings: {
 			goSettings();
 			return true;
 
 		}
 		case R.id.menu_add: {
 			addAlarmDialog();
 			return true;
 		}
 		case R.id.menu_delete_all: {
 			deleteAllAlarms(true);
 			return true;
 		}
 		case R.id.menu_exit: {
 			// mNotificationManager.cancel(SettingsConst.APP_NOTIF_ID);
 			mNotificationManager.cancelAll();
 			Intent intent = new Intent(Intent.ACTION_MAIN);
 			intent.addCategory(Intent.CATEGORY_HOME);
 			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			startActivity(intent);
 			// onStop();
 			finish();
 		}
 
 		}
 		return false;
 	}
 
 	private Intent goSettings() {
 		Intent i3 = new Intent(this, SettingsActivity.class);
 		startActivity(i3);
 		return i3;
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		TextViewWithMenu.TextViewMenuInfo menuInfo = (TextViewWithMenu.TextViewMenuInfo) item
 				.getMenuInfo();
 		TextViewWithMenu text = (TextViewWithMenu) menuInfo.targetView;
 
 		switch (item.getItemId()) {
 		case R.id.settime: {
 			setAlarmDialog(text.getAlarm());
 			return true;
 		}
 		case R.id.remove: {
 			deleteAlarm(text);
 			return true;
 		}
 		}
 		return super.onContextItemSelected(item);
 	}
 
 	// ============================================================
 	// ==================== END MENUS ===============================
 	// ============================================================
 	private void deleteAllAlarms(boolean allowDialog) {
 
 		DBHelper dbHelper = new DBHelper(getApplicationContext());
 		dbHelper.open();
 		for (final AlarmClock alarm : alarmList) {
 			if (alarm.getState().equals(AlarmClock.TimerState.ALARMING))
 				alarm.alarmSTOP();
 			
 			if (alarm.getElement() != null) alarm.getElement().setVisibility(View.GONE);
 			if (alarm.getTime() > 0) dbHelper.insertHistory(alarm);
 			if (alarm.getId() > 0) {
 				dbHelper.deleteAlarm(alarm.getId());
 			}
 			alarm.setState(AlarmClock.TimerState.STOPPED);
 			alarm.setElement(null); // TODO clean!
 			alarm.setTime(-1);
 		}
 		dbHelper.close();
 		alarmList.clear();
 		if (allowDialog && mPrefs.getBoolean(
 				getApplicationContext().getString(
 						R.string.pref_addalarmonstart_key), true)) {
 			addAlarmDialog();
 		}
 
 	}
 
 	private void refreshAllAlarms() {
 		for (final AlarmClock alarm : alarmList) {
 			if (alarm.getState().equals(AlarmClock.TimerState.ALARMING)) {
 				alarm.alarmSTOP();
 			}
 			alarm.setState(AlarmClock.TimerState.PAUSED);
 			alarm.restart();
 			alarm.updateElement();
 		}
 
 	}
 
 	private void deleteAlarm(final TextViewWithMenu text) {
 		AlarmClock nalarm = null;
 		for (final AlarmClock alarm : alarmList) {
 			if (alarm.getElement() != null && alarm.getElement().getChildAt(1) == (TextViewWithMenu) text) {
 				// final AlarmClock falarm = alarm;
 				Animation hyperspaceJump = AnimationUtils.loadAnimation(this,
 						R.anim.hsjump);
 				hyperspaceJump
 						.setAnimationListener(new Animation.AnimationListener() {
 							@Override
 							public void onAnimationEnd(Animation arg0) {
 								alarmList.remove(alarm);
 								DBHelper dbHelper = new DBHelper(
 										getApplicationContext());
 								dbHelper.open();
 								if (alarm.getTime() > 0) dbHelper.insertHistory(alarm);
 								dbHelper.close();
 
 								if (alarm.getState().equals(
 										AlarmClock.TimerState.ALARMING))
 									alarm.alarmSTOP();
 								alarm.setState(AlarmClock.TimerState.STOPPED);
 								if (alarm.getElement() != null)
 									alarm.getElement().setVisibility(View.GONE);
 								if (alarm.getId() > 0) {
 									DBHelper alarmClockDAO = new DBHelper(
 											getApplicationContext());
 									alarmClockDAO.open();
 									alarmClockDAO.deleteAlarm(alarm.getId());
 									alarmClockDAO.close();
 									
 								}
 								alarm.setTime(-1);
 								alarm.setElement(null); // TODO clean!
 								// mainL.removeView(((LinearLayout)alarm.getElement()
 
 							}
 
 							@Override
 							public void onAnimationStart(Animation animation) {
 							}
 
 							public void onAnimationRepeat(Animation animation) {
 							}
 
 						});
 
 				alarm.getElement().startAnimation(hyperspaceJump);
 				
 				break;
 			}
 		}
 	}
 	
 	
 //************************** GET SET *************************
 	public TimePickDialog getTimePickDialog() {
 		return timePickDialog;
 	}
 	
 	
 	public List<AlarmClock> getAlarmList() {
 		return alarmList;
 	}
 	
 //************************* end Getters Setters ************
 
 
 	private void addAlarmDialog() {
 		if (timePickDialog == null || (timePickDialog != null && !timePickDialog.isShowing())){
 			setAlarmDialog(null);
 		}
 	}
 
 	private void setAlarmDialog(AlarmClock alarm) {
 
 		timePickDialog = new TimePickDialog(MainActivity.this);
 		timePickDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		timePickDialog.setAlarm(alarm);
 
 		timePickDialog.setDialogResult(new TimePickDialog.OnMyDialogResult() {
 			public void finish(AlarmClock newAlarm) {
 				addAlarm(newAlarm);
 				newAlarm.updateElement();
 				if (newAlarm.getState() == AlarmClock.TimerState.STOPPED) {
 					drawAlarm(newAlarm);
 				}
 			}
 		});
 		timePickDialog.show();
 	}
 
 	/*	*//**
 	 * Creates new MusicHandler and starts playing, Also creates
 	 * ShowStopper.
 	 */
 	/*
 	 * private void playMusic() { music = new MusicHandler();
 	 * music.setMusic(this); music.play(true);
 	 * 
 	 * ShowStopper stopper = new
 	 * ShowStopper(PreferenceService.getAlarmLength(this), music, vibrator);
 	 * showStopperThread = new Thread(stopper); showStopperThread.start();
 	 * 
 	 * }
 	 */
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
 		if (key.equals("pref_shownames_key")) {
 			for (AlarmClock alarm : alarmList) {
 				final TextView widgetLbl = (TextView) alarm.getElement()
 						.getChildAt(0);
 				if (!mPrefs.getBoolean(
 						getApplicationContext().getString(
 								R.string.pref_shownames_key), false)) {
 					widgetLbl.setVisibility(View.INVISIBLE);
 				} else {
 					widgetLbl.setVisibility(View.VISIBLE);
 				}
 			}
 		} else if (key.equals("pref_showsettbtn_key")) {
 			if (mPrefs.getBoolean(
 					getApplicationContext().getString(
 							R.string.pref_showsettbtn_key), false)) {
 				actionBar.addAction(settingsButtonAction);
 			} else {
 				actionBar.removeAction(settingsButtonAction);
 			}
 		} else if (key.equals("pref_showdelall_key")) {
 			if (mPrefs.getBoolean(
 					getApplicationContext().getString(
 							R.string.pref_showdelall_key), false)) {
 				actionBar.addAction(delallAction);
 			} else {
 				actionBar.removeAction(delallAction);
 			}
 		} else if (key.equals("pref_showaddbtn_key")) {
 			if (mPrefs.getBoolean(
 					getApplicationContext().getString(
 							R.string.pref_showaddbtn_key), false)) {
 				actionBar.addAction(addButtonAction);
 			} else {
 				actionBar.removeAction(addButtonAction);
 			}
 		} else if (key.equals("pref_showrefreshbtn_key")) {
 			if (mPrefs.getBoolean(
 					getApplicationContext().getString(
 							R.string.pref_showrefreshbtn_key), false)) {
 				actionBar.addAction(refreshButtonAction);
 			} else {
 				actionBar.removeAction(refreshButtonAction);
 			}
 		} else if (key.equals("pref_showpresetsbtn_key")) {
 			if (mPrefs.getBoolean(
 					getApplicationContext().getString(
 							R.string.pref_showpresetsbtn_key), false)) {
 				actionBar.addAction(presetsButtonAction);
 			} else {
 				actionBar.removeAction(presetsButtonAction);
 			}
 		} else if (key.equals("pref_showplaybtn_key")) {
 			if (mPrefs.getBoolean(
 					getApplicationContext().getString(
 							R.string.pref_showplaybtn_key), false)) {
 				actionBar.addAction(runAllButtonAction);
 			} else {
 				actionBar.removeAction(runAllButtonAction);
 			}
 		}
 	}
 
 	/**
 	 * Finishes the activity, also closes the various things started by
 	 * onCreate.
 	 */
 	@Override
 	public void finish() {
 		Log.v(TAG, "finish");
 	}
 
 }

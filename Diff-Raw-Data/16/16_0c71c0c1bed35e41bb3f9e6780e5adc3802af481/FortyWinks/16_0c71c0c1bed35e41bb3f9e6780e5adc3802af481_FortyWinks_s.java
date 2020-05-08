 package com.fernferret.android.fortywinks;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.format.DateFormat;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.DigitalClock;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SlidingDrawer;
 import android.widget.SlidingDrawer.OnDrawerCloseListener;
 import android.widget.SlidingDrawer.OnDrawerOpenListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * The primary application class that is allowed to be launched from the homescreen.
  * 
  * @author Eric Stokes
  * 
  */
 public class FortyWinks extends Activity {
 	
 	private static final int NEXT_ALARM_TEXT_SIZE = 20;
 	
 	// General Class Variables
 	Resources mResources;
 	
 	// TextViews
 	private DigitalClock mBigTime;
 	private TextView mTimeLeftToSleep;
 	private TextView mRemainingCycles;
 	
 	// Buttons
 	private Button mDoSleepButton;
 	
 	// ListViews
 	private ListView mQuickAlarmList;
 	
 	// LinearLayout
 	private LinearLayout mNextAlarmContainer;
 	
 	// Drawer
 	private SlidingDrawer mDrawer;
 	
 	private PreferenceViewAdapter mQuickAlarmAdapter;
 	
 	private ArrayList<ProposedAlarm> mQuickProposedAlarms;
 	
 	// Database Objects
 	private DBAdapter mDatabaseAdapter;
 	
 	// Important values used in generating the drawer items
 	int mNumberOfAlarms;
 	int mCycleTime;
 	SharedPreferences mSettings;
 	
 	private static final int SINGLE_ALARM_RC = 0;
 	private static final int MULTI_ALARM_RC = 1;
 	private static final int NO_FLAGS = 0;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		// Load Resources
 		mResources = getResources();
 		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		mDatabaseAdapter = new DBAdapter(this);
 		
 		// Load UI Elements
 		mBigTime = (DigitalClock) findViewById(R.id.main_big_time);
 		
 		mTimeLeftToSleep = (TextView) findViewById(R.id.main_time_left_to_sleep);
 		mRemainingCycles = (TextView) findViewById(R.id.main_remaining_cycles);
 		
 		mDoSleepButton = (Button) findViewById(R.id.main_do_sleep_button);
 		
 		mQuickAlarmList = (ListView) findViewById(R.id.main_quick_alarms_list);
 		
 		mNextAlarmContainer = (LinearLayout) findViewById(R.id.main_next_alarm_container);
 		
 		mDrawer = (SlidingDrawer) findViewById(R.id.main_drawer);
 		
 		mQuickAlarmList.setAdapter(mQuickAlarmAdapter);
 		// Set Listeners
 		mDrawer.setOnDrawerOpenListener(mDrawerOpenListener);
 		mDrawer.setOnDrawerCloseListener(mDrawerCloseListener);
 		mDoSleepButton.setOnClickListener(mOnButtonClickListener);
 		mQuickAlarmList.setOnItemClickListener(mListViewListener);
 		mQuickAlarmList.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				ProposedAlarm a = mQuickProposedAlarms.get(position);
 				
 				mDatabaseAdapter.saveAlarm(new Alarm(a));
 				setPowerNap();
				mDrawer.close();
 			}
 		});
 		
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		try {
 			mNumberOfAlarms = Integer.parseInt(mSettings.getString(getString(R.string.key_number_quick_alarms), "6"));
 		} catch (NumberFormatException e) {
 			mNumberOfAlarms = 6;
 		}
 		
 		try {
 			mCycleTime = Integer.parseInt(mSettings.getString(getString(R.string.key_cycle_time), "90"));
 		} catch (NumberFormatException e) {
 			// Someone entered NaN for their default value of REM cycle, let's use 90, the industry standard!
 			mCycleTime = 90;
 		}
 		generateAlarms();
 		mQuickAlarmAdapter = new PreferenceViewAdapter(this, R.layout.preference_view, R.id.preference_view_left_text, mQuickProposedAlarms);
 		mQuickAlarmAdapter.notifyDataSetChanged();
 		mQuickAlarmList.setAdapter(mQuickAlarmAdapter);
 		
 	}
 	
 	private void setPowerNap() {
 		Intent singleAlarmIntent = new Intent(FortyWinks.this, SingleAlarm.class);
 		Alarm a = mDatabaseAdapter.getPowerNap();
 		Calendar calendar = Calendar.getInstance();
 		long futureTime = a.getNextAlarmTime();
 		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 		// Iterate through all followups with the following
 		PendingIntent singleAlarmPendingIntent = PendingIntent.getBroadcast(FortyWinks.this, SINGLE_ALARM_RC, singleAlarmIntent, NO_FLAGS);
 		calendar.setTimeInMillis(futureTime);
 		alarmManager.set(AlarmManager.RTC_WAKEUP, a.getNextAlarmTime(), singleAlarmPendingIntent);
 		// End iteration
 		mNextAlarmContainer.removeAllViews();
 		TextView newAlarm = new TextView(this);
 		
 		newAlarm.setText(getFriendlyCalendarString(calendar));
 		newAlarm.setTextSize(NEXT_ALARM_TEXT_SIZE);
 		mNextAlarmContainer.addView(newAlarm);
 		Toast.makeText(FortyWinks.this, "Your alarm has been set for" + getFriendlyTimeTillAlarm(calendar), Toast.LENGTH_SHORT).show();
 	}
 	
 	private String getFriendlyCalendarString(Calendar c) {
 		c.getTime();
 		Time t = new Time();
 		t.set(c.getTimeInMillis());
 		String format = "%l:%M %p";
 		return t.format(format).toUpperCase();
 	}
 	
 	private String getFriendlyTimeTillAlarm(Calendar c) {
 		Calendar current = Calendar.getInstance();
 		int days = c.get(Calendar.DAY_OF_YEAR) - current.get(Calendar.DAY_OF_YEAR);
 		int hours = c.get(Calendar.HOUR_OF_DAY) - current.get(Calendar.HOUR_OF_DAY);
 		int minutes = c.get(Calendar.MINUTE) - current.get(Calendar.MINUTE);
 		if (hours < 0) {
 			hours += 24;
 			days--;
 		}
 		if (minutes < 0) {
 			minutes += 60;
 			hours--;
 		}
 		return days + "days, " + hours + "hours, " + minutes + "minutes";
 	}
 	
 	private void generateAlarms() {
 		ArrayList<ProposedAlarm> listItems = new ArrayList<ProposedAlarm>();
 		long currentTime = System.currentTimeMillis();
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTimeInMillis(currentTime);
 		
 		for (int i = 1; i <= mNumberOfAlarms; i++) {
 			calendar.add(Calendar.MINUTE, mCycleTime);
 			listItems.add(new ProposedAlarm(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), i, mCycleTime, ProposedAlarm.ProposedAlarmType.PowerNap));
 		}
 		mQuickProposedAlarms = listItems;
 		
 	}
 	
 	private void refreshQuickAlarms() {
 		Log.w("40W", "40W - Refreshing Alarms");
 		Calendar calendar = Calendar.getInstance();
 		for (ProposedAlarm a : mQuickProposedAlarms) {
 			calendar.add(Calendar.MINUTE, mCycleTime);
			a.setTimeTill(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
 		}
 		mQuickAlarmAdapter.notifyDataSetChanged();
 	}
 	
 	private View.OnClickListener mOnButtonClickListener = new View.OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			
 		}
 	};
 	private OnItemClickListener mListViewListener = new OnItemClickListener() {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		}
 	};
 	private OnDrawerOpenListener mDrawerOpenListener = new OnDrawerOpenListener() {
 		@Override
 		public void onDrawerOpened() {
 			refreshQuickAlarms();
 			ImageView button = (ImageView) findViewById(R.id.main_drawer_button);
 			button.setImageDrawable(mResources.getDrawable(R.drawable.drawer_open));
 		}
 	};
 	
 	private OnDrawerCloseListener mDrawerCloseListener = new OnDrawerCloseListener() {
 		@Override
 		public void onDrawerClosed() {
 			ImageView button = (ImageView) findViewById(R.id.main_drawer_button);
 			button.setImageDrawable(mResources.getDrawable(R.drawable.drawer_closed));
 		}
 	};
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		
 		MenuInflater inflater = new MenuInflater(this);
 		inflater.inflate(R.menu.main_menu, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		// TODO Auto-generated method stub
 		return super.onPrepareOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_options:
 				Intent options = new Intent(this, FortyPrefs.class);
 				startActivity(options);
 				break;
 		}
 		return true;
 	}
 	
 }

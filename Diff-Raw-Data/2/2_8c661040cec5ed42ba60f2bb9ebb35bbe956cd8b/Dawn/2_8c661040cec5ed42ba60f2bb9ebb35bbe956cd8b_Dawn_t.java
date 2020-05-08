 /**
  *   Copyright 2012 Francesco Balducci
  *
  *   This file is part of FakeDawn.
  *
  *   FakeDawn is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   FakeDawn is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with FakeDawn.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.balau.fakedawn;
 
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View.OnClickListener;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class Dawn extends Activity implements OnClickListener {
 
 	private static int TIMER_TICK_SECONDS = 10;
 	private static final String ALARM_START_MILLIS = "ALARM_START_MILLIS";
 	private static int COLOR_OPAQUE = 0xFF000000;
 
 	private long m_alarmStartMillis;
 	private long m_alarmEndMillis;
 	private Timer m_timer;
 
 	private int m_dawnColor;
 
 	private Calendar getAlarmStart(SharedPreferences pref)
 	{
 		Calendar rightNow = Calendar.getInstance();
 
 		long rightNowMillis = rightNow.getTimeInMillis();
 		int hour = pref.getInt("dawn_start_hour", 8);
 		int minute = pref.getInt("dawn_start_minute", 0);
 		Calendar alarmStart = (Calendar) rightNow.clone();
 		alarmStart.set(Calendar.HOUR_OF_DAY, hour);
 		alarmStart.set(Calendar.MINUTE, minute);
 		long halfDayMillis = 1000L*60L*60L*12L; 
 		long alarmStartMillis;
 		alarmStartMillis = alarmStart.getTimeInMillis();
 
 		if(alarmStartMillis - rightNowMillis > halfDayMillis)
 		{
 			alarmStart.add(Calendar.DAY_OF_YEAR, -1);
 		}
 		else if(alarmStartMillis - rightNowMillis < -halfDayMillis)
 		{
 			alarmStart.add(Calendar.DAY_OF_YEAR, 1);
 		}
 
 		return alarmStart;
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.dawn);
 
 		getWindow().addFlags(
 				WindowManager.LayoutParams.FLAG_FULLSCREEN|
 				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
 				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
 				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
 				WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
 
 		findViewById(R.id.dawn_background).setOnClickListener(this);
 
 		SharedPreferences pref = getApplicationContext().getSharedPreferences("main", MODE_PRIVATE);
 		String day;
 		Calendar alarmStart = getAlarmStart(pref);
 
 		switch (alarmStart.get(Calendar.DAY_OF_WEEK)) {
 		case Calendar.MONDAY:
 			day = "mondays";
 			break;
 		case Calendar.TUESDAY:
 			day = "tuesdays";
 			break;
 		case Calendar.WEDNESDAY:
 			day = "wednesdays";
 			break;
 		case Calendar.THURSDAY:
 			day = "thursdays";
 			break;
 		case Calendar.FRIDAY:
 			day = "fridays";
 			break;
 		case Calendar.SATURDAY:
 			day = "saturdays";
 			break;
 		case Calendar.SUNDAY:
 			day = "sundays";
 			break;
 		default:
 			day = "NON_EXISTING_WEEKDAY";
 			break;
 		}
 		if(!pref.getBoolean(day, false))
 		{
 			this.finish();
 		}
 		else
 		{
 			long dawnStartMillis = alarmStart.getTimeInMillis();
 			m_alarmStartMillis =
 					dawnStartMillis + (pref.getInt("light_start", 0)*1000L*60L);
 			if(savedInstanceState != null)
 			{
 				if(savedInstanceState.containsKey(ALARM_START_MILLIS))
 				{
 					m_alarmStartMillis = savedInstanceState.getLong(ALARM_START_MILLIS);
 				}
 			}
 			m_alarmEndMillis = dawnStartMillis + (1000L*60L*pref.getInt("light_max", 15));
 
 			m_dawnColor = pref.getInt("color", 0x4040FF);
 			Intent sound = new Intent(getApplicationContext(), DawnSound.class);
 			sound.putExtra(DawnSound.EXTRA_VIBRATE, pref.getBoolean("vibrate", false));
 			long soundStart = dawnStartMillis + (pref.getInt("sound_start", 15)*1000L*60L);
			long soundEnd = dawnStartMillis + (pref.getInt("sound_max", 15)*1000L*60L);
 			sound.putExtra(DawnSound.EXTRA_SOUND_START_MILLIS, soundStart);
 			sound.putExtra(DawnSound.EXTRA_SOUND_END_MILLIS, soundEnd);
 			sound.putExtra(DawnSound.EXTRA_SOUND_URI, pref.getString("sound", ""));
 			if(pref.contains("volume"))
 				sound.putExtra(DawnSound.EXTRA_SOUND_VOLUME, pref.getInt("volume", 0));			
 			startService(sound);
 
 			updateBrightness(System.currentTimeMillis());
 
 			m_timer = new Timer();
 			m_timer.schedule(
 					new TimerTask() {
 
 						@Override
 						public void run() {
 							runOnUiThread(
 									new Runnable() {
 										public void run() {
 											updateBrightness(System.currentTimeMillis());
 										}
 									});
 						}
 					}, TIMER_TICK_SECONDS*1000, TIMER_TICK_SECONDS*1000);
 
 		}
 	}
 
 	private void stopDawn()
 	{
 		Intent sound = new Intent(getApplicationContext(), DawnSound.class);
 		stopService(sound);
 		this.finish();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		stopDawn();
 		return super.onKeyDown(keyCode, event);
 	}
 
 	public void onClick(View v) {
 		stopDawn();
 	}
 
 	private int getColor(int rgb, int percent)
 	{
 		int r, g, b;
 		int rgb_new;
 
 		r = (rgb >> 16)&0xFF;
 		g = (rgb >>  8)&0xFF;
 		b = (rgb >>  0)&0xFF;
 
 		if(percent > 100) percent = 100;
 		if(percent < 0) percent = 0;
 
 		r = (r*percent)/100;
 		g = (g*percent)/100;
 		b = (b*percent)/100;
 
 		rgb_new = (r<<16) | (g<<8) | (b<<0);
 
 		return rgb_new;
 	}
 
 	private void updateBrightness(long currentTimeMillis)
 	{
 		long level_percent;
 		long millis_from_start;
 		long dawnDurationMillis;
 		int rgb;
 
 		millis_from_start = currentTimeMillis - m_alarmStartMillis; 
 		dawnDurationMillis = m_alarmEndMillis - m_alarmStartMillis; 
 		if(dawnDurationMillis > 0)
 		{
 			level_percent = (100 * millis_from_start) / dawnDurationMillis;
 			if(level_percent < 0) level_percent = 0;
 			if(level_percent > 100) level_percent = 100;
 		}
 		else
 		{
 			level_percent = (millis_from_start >= 0)?100:0;
 		}
 		rgb = COLOR_OPAQUE | getColor(m_dawnColor, (int)level_percent);
 		findViewById(R.id.dawn_background).setBackgroundColor(rgb);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onStop()
 	 */
 	@Override
 	protected void onStop() {
 		super.onStop();
 		m_timer.cancel();
 		Log.d("FakeDawn", "Dawn Stopped.");
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
 	 */
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putLong(ALARM_START_MILLIS, m_alarmStartMillis);
 	}
 
 }

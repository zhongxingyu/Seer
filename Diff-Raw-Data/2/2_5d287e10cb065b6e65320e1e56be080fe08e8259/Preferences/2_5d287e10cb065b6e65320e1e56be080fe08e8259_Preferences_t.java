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
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.balau.fakedawn.ColorPickerDialog.OnColorChangedListener;
 import org.balau.fakedawn.TimeSlider.DawnTime;
 import org.balau.fakedawn.TimeSlider.OnTimesChangedListener;
 
 import android.app.Activity;
 import android.app.TimePickerDialog;
 import android.app.TimePickerDialog.OnTimeSetListener;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.PorterDuff;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.TimePicker;
 
 /**
  * @author francesco
  *
  */
 public class Preferences extends Activity implements OnClickListener, OnSeekBarChangeListener, OnColorChangedListener, OnTimeSetListener, OnTimesChangedListener {
 
 	private static final int REQUEST_PICK_SOUND = 0;
 	private static final int COLOR_OPAQUE = 0xFF000000;
 	private static final int COLOR_RGB_MASK = 0x00FFFFFF;
 
 	private static final int TIME_DAWN_START = 0;
 	private static final int TIME_DAWN_END = 1;
 	private static final int TIME_SOUND_START = 2;
 	private static final int TIME_SOUND_END = 3;
 	
 	private Uri m_soundUri = null;
 	private VolumePreview m_preview = new VolumePreview();
 	private int m_dawnColor;
 	private int m_clickedTime;
 	
 	private Timer m_resizeSlidersScheduler = null;
 	private static final int RESIZE_SLIDERS_DELAY_MILLIS = 1000;
 	private static final int SLIDERS_PADDING_MINUTES = 10;
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.preferences);
 
 		Button saveButton = (Button) findViewById(R.id.buttonSave);
 		saveButton.setOnClickListener(this);
 		Button discardButton = (Button) findViewById(R.id.buttonDiscard);
 		discardButton.setOnClickListener(this);
 		Button soundButton = (Button) findViewById(R.id.buttonSound);
 		soundButton.setOnClickListener(this);
 		
 		SeekBar seekBarVolume = (SeekBar)findViewById(R.id.seekBarVolume);
 		seekBarVolume.setOnSeekBarChangeListener(this);
 
 		SharedPreferences pref = getApplicationContext().getSharedPreferences("main", MODE_PRIVATE);
 
 		CheckBox cb;
 
 		cb = (CheckBox) findViewById(R.id.checkBoxAlarmEnabled);
 		cb.setChecked(pref.getBoolean("enabled", false));
 		cb.requestFocus();
 
 		TimeSlider ts = (TimeSlider)findViewById(R.id.timeSlider1);
 		ts.setOnClickListener(this);
 		ts.setOnTimesChangedListener(this);
 		
 		ts.setLeftTime(
 				pref.getInt("hour", 8),
 				pref.getInt("minute", 0));
 		DawnTime dawnEnd = new DawnTime( 
 				ts.getLeftTime().getMinutes() + pref.getInt("duration", 15));
 		ts.setRightTime(dawnEnd.getHour(), dawnEnd.getMinute());
 		
 		ts = (TimeSlider)findViewById(R.id.timeSlider2);
 		ts.setOnClickListener(this);
 		ts.setOnTimesChangedListener(this);
 		DawnTime soundStart = new DawnTime( 
 				dawnEnd.getMinutes() + pref.getInt("sound_delay", 15));
 		ts.setLeftTime(soundStart.getHour(), soundStart.getMinute());
 		DawnTime soundEnd = new DawnTime( 
				soundStart.getMinutes() + pref.getInt("sound_duration", 0));
 		ts.setRightTime(soundEnd.getHour(), soundEnd.getMinute());
 
 		cb = (CheckBox) findViewById(R.id.checkBoxMondays);
 		cb.setChecked(pref.getBoolean("mondays", true));
 		cb = (CheckBox) findViewById(R.id.checkBoxTuesdays);
 		cb.setChecked(pref.getBoolean("tuesdays", true));
 		cb = (CheckBox) findViewById(R.id.checkBoxWednesdays);
 		cb.setChecked(pref.getBoolean("wednesdays", true));
 		cb = (CheckBox) findViewById(R.id.checkBoxThursdays);
 		cb.setChecked(pref.getBoolean("thursdays", true));
 		cb = (CheckBox) findViewById(R.id.checkBoxFridays);
 		cb.setChecked(pref.getBoolean("fridays", true));
 		cb = (CheckBox) findViewById(R.id.checkBoxSaturdays);
 		cb.setChecked(pref.getBoolean("saturdays", false));
 		cb = (CheckBox) findViewById(R.id.checkBoxSundays);
 		cb.setChecked(pref.getBoolean("sundays", false));
 
 		updateColor(pref.getInt("color", 0x4040FF));
 
 		String sound = pref.getString("sound", "");
 		if(sound.isEmpty())
 		{
 			m_soundUri = null;
 		}
 		else
 		{
 			m_soundUri = Uri.parse(sound);
 		}
 
 		AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
 		int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
 		seekBarVolume.setMax(maxVolume);
 		int volume = pref.getInt("volume", maxVolume/2);
 		if(volume < 0) volume = 0;
 		if(volume > maxVolume) volume = maxVolume;
 		seekBarVolume.setProgress(volume);
 
 		cb = (CheckBox) findViewById(R.id.checkBoxVibrate);
 		cb.setChecked(pref.getBoolean("vibrate", false));
 
 		updateSoundViews();
 
 		resizeSliders();
 
 		Log.d("FakeDawn", "Preferences loaded.");
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onStart()
 	 */
 	@Override
 	protected void onStart() {
 		super.onStart();
 
 	}
 
 	private void resizeSliders()
 	{
 		TimeSlider lightSlider = (TimeSlider)findViewById(R.id.timeSlider1);
 		TimeSlider soundSlider = (TimeSlider)findViewById(R.id.timeSlider2);
 		
 		DawnTime light_start = lightSlider.getLeftTime();
 		DawnTime light_end = lightSlider.getRightTime();
 		DawnTime sound_start;
 		DawnTime sound_end;
 		
 		if(soundSlider.isEnabled())
 		{
 			sound_start = soundSlider.getLeftTime();
 			sound_end  = soundSlider.getRightTime();
 		}
 		else
 		{
 			//If disabled, it follows the end of the light slider.
 			sound_start = lightSlider.getRightTime();
 			sound_end  = new DawnTime(sound_start.getMinutes());
 			soundSlider.setLeftTime(sound_start.getHour(), sound_start.getMinute());
 			soundSlider.setRightTime(sound_end.getHour(), sound_end.getMinute());
 		}
 		
 		int minTime = Math.max(
 				Math.min(
 						light_start.getMinutes(),
 						sound_start.getMinutes()) - SLIDERS_PADDING_MINUTES,
 						0);
 		int maxTime = Math.max(
 				light_end.getMinutes(),
 				sound_end.getMinutes()) + SLIDERS_PADDING_MINUTES;
 		
 		int minutes_in_day = 60*24;
 		if(minTime + SLIDERS_PADDING_MINUTES >= minutes_in_day)
 		{
 			// shift everything to the day
 			int days = (minTime+SLIDERS_PADDING_MINUTES)/minutes_in_day; //floor
 			int minutes_to_subtract = days*minutes_in_day;
 			
 			minTime = Math.max(minTime - minutes_to_subtract, 0);
 			maxTime -= minutes_to_subtract;
 			light_start = new DawnTime(light_start.getMinutes() - minutes_to_subtract);
 			light_end = new DawnTime(light_end.getMinutes() - minutes_to_subtract);
 			sound_start = new DawnTime(sound_start.getMinutes() - minutes_to_subtract);
 			sound_end = new DawnTime(sound_end.getMinutes() - minutes_to_subtract);
 			
 			lightSlider.setLeftTime(light_start.getHour(), light_start.getMinute());
 			lightSlider.setRightTime(light_end.getHour(), light_end.getMinute());
 			soundSlider.setLeftTime(sound_start.getHour(), sound_start.getMinute());
 			soundSlider.setRightTime(sound_end.getHour(), sound_end.getMinute());
 			
 		}
 		DawnTime start = new DawnTime(minTime);
 
 		lightSlider.setStartTime(start.getHour(), start.getMinute());
 		lightSlider.setSpanTime(maxTime - minTime);
 
 		soundSlider.setStartTime(start.getHour(), start.getMinute());
 		soundSlider.setSpanTime(maxTime - minTime);
 		
 	}
 	
 	private void updateColor(int color)
 	{
 		m_dawnColor = color & COLOR_RGB_MASK;
 		TimeSlider ts = (TimeSlider)findViewById(R.id.timeSlider1);
 		ts.setRectColor(m_dawnColor|COLOR_OPAQUE);
 	}
 	
 	public void onClick(View v) {
 		ColorPickerDialog colorDialog;
 		TimeSlider ts;
 		TimePickerDialog tpd;
 		
 		switch(v.getId())
 		{
 		case R.id.buttonSave:
 			SharedPreferences pref = getApplicationContext().getSharedPreferences("main", MODE_PRIVATE);
 			SharedPreferences.Editor editor = pref.edit();
 			TimeSlider lightSlider = (TimeSlider)findViewById(R.id.timeSlider1);
 			TimeSlider soundSlider = (TimeSlider)findViewById(R.id.timeSlider2);
 
 			editor.putInt("hour", lightSlider.getLeftTime().getHourOfDay());
 			editor.putInt("minute", lightSlider.getLeftTime().getMinute());
 
 			editor.putInt("color", m_dawnColor);
 			
 			CheckBox cb;
 
 			cb = (CheckBox) findViewById(R.id.checkBoxAlarmEnabled);
 			editor.putBoolean("enabled", cb.isChecked());
 
 			cb = (CheckBox) findViewById(R.id.checkBoxMondays);
 			editor.putBoolean("mondays", cb.isChecked());
 			cb = (CheckBox) findViewById(R.id.checkBoxTuesdays);
 			editor.putBoolean("tuesdays", cb.isChecked());
 			cb = (CheckBox) findViewById(R.id.checkBoxWednesdays);
 			editor.putBoolean("wednesdays", cb.isChecked());
 			cb = (CheckBox) findViewById(R.id.checkBoxThursdays);
 			editor.putBoolean("thursdays", cb.isChecked());
 			cb = (CheckBox) findViewById(R.id.checkBoxFridays);
 			editor.putBoolean("fridays", cb.isChecked());
 			cb = (CheckBox) findViewById(R.id.checkBoxSaturdays);
 			editor.putBoolean("saturdays", cb.isChecked());
 			cb = (CheckBox) findViewById(R.id.checkBoxSundays);
 			editor.putBoolean("sundays", cb.isChecked());
 
 			editor.putInt("duration",
 					lightSlider.getRightTime().getMinutes() - lightSlider.getLeftTime().getMinutes());
 			if(m_soundUri == null)
 			{
 				editor.putString("sound", "");
 			}
 			else
 			{
 				editor.putString("sound", m_soundUri.toString());
 			}
 
 			editor.putInt("sound_delay",
 					soundSlider.getLeftTime().getMinutes() - lightSlider.getRightTime().getMinutes());
 			editor.putInt("sound_duration",
 					soundSlider.getRightTime().getMinutes() - soundSlider.getLeftTime().getMinutes());
 			
 			SeekBar sb = (SeekBar)findViewById(R.id.seekBarVolume);
 			editor.putInt("volume", sb.getProgress());
 			
 			cb = (CheckBox) findViewById(R.id.checkBoxVibrate);
 			editor.putBoolean("vibrate", cb.isChecked());
 
 			editor.commit();
 
 			Intent updateAlarm = new Intent(getApplicationContext(), Alarm.class);
 			getApplicationContext().startService(updateAlarm);
 			Log.d("FakeDawn", "Preferences saved.");
 			finish();
 			break;
 		case R.id.buttonDiscard:
 			finish();
 			break;
 		case R.id.buttonSound:
 			Intent pickSound = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
 			pickSound.putExtra(
 					RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,
 					true);
 			pickSound.putExtra(
 					RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,
 					false);
 			pickSound.putExtra(
 					RingtoneManager.EXTRA_RINGTONE_TYPE,
 					RingtoneManager.TYPE_ALL);
 			pickSound.putExtra(
 					RingtoneManager.EXTRA_RINGTONE_TITLE,
 					"Pick Alarm Sound");
 			if(m_soundUri != null)
 			{
 				pickSound.putExtra(
 						RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
 						m_soundUri);
 			}
 			startActivityForResult(pickSound, REQUEST_PICK_SOUND);
 			break;
 		case R.id.timeSlider1:
 			ts = (TimeSlider)v;
 			switch(ts.getLastTouched())
 			{
 			case TimeSlider.TOUCH_ALL:
 				colorDialog = new ColorPickerDialog(this, this, m_dawnColor);
 				colorDialog.show();
 				break;
 			case TimeSlider.TOUCH_LEFT:
 				tpd = new TimePickerDialog(
 						this, 
 						this, 
 						ts.getLeftTime().getHourOfDay(),
 						ts.getLeftTime().getMinute(),
 						true);
 				m_clickedTime = TIME_DAWN_START;
 				tpd.show();
 				break;
 			case TimeSlider.TOUCH_RIGHT:
 				tpd = new TimePickerDialog(
 						this, 
 						this, 
 						ts.getRightTime().getHourOfDay(),
 						ts.getRightTime().getMinute(),
 						true);
 				m_clickedTime = TIME_DAWN_END;
 				tpd.show();
 				break;
 			}
 			break;
 		case R.id.timeSlider2:
 			ts = (TimeSlider)v;
 			switch(ts.getLastTouched())
 			{
 			case TimeSlider.TOUCH_ALL:
 				break;
 			case TimeSlider.TOUCH_LEFT:
 				tpd = new TimePickerDialog(
 						this, 
 						this, 
 						ts.getLeftTime().getHourOfDay(),
 						ts.getLeftTime().getMinute(),
 						true);
 				m_clickedTime = TIME_SOUND_START;
 				tpd.show();
 				break;
 			case TimeSlider.TOUCH_RIGHT:
 				tpd = new TimePickerDialog(
 						this, 
 						this, 
 						ts.getRightTime().getHourOfDay(),
 						ts.getRightTime().getMinute(),
 						true);
 				m_clickedTime = TIME_SOUND_END;
 				tpd.show();
 				break;
 			}
 			break;
 		}
 	}
 
 	private void updateSoundViews()
 	{
 		Button soundButton = (Button) findViewById(R.id.buttonSound);
 		SeekBar seekBarVolume = (SeekBar)findViewById(R.id.seekBarVolume);
 		CheckBox checkBoxVibrate = (CheckBox) findViewById(R.id.checkBoxVibrate);
 		TimeSlider soundSlider = (TimeSlider)findViewById(R.id.timeSlider2);
 
 		boolean soundViewsEnabled = (m_soundUri != null);
 		
 		if(soundViewsEnabled)
 		{
 			String soundTitle = RingtoneManager.getRingtone(this, m_soundUri).getTitle(this);	
 			soundButton.setText(soundTitle);
 		}
 		else
 		{
 			soundButton.setText("Silent");
 		}
 		seekBarVolume.setEnabled(soundViewsEnabled);
 		checkBoxVibrate.setEnabled(soundViewsEnabled);
 		soundSlider.setEnabled(soundViewsEnabled);
 		
 		m_preview.setSoundUri(this, m_soundUri);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if(requestCode == REQUEST_PICK_SOUND)
 		{
 			if(resultCode == RESULT_OK)
 			{
 				m_soundUri = (Uri) data.getParcelableExtra(
 						RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
 				updateSoundViews();
 			}
 		}
 	}
 
 	@Override
 	public void onProgressChanged(SeekBar seekBar, int progress,
 			boolean fromUser) {
 		if(seekBar.getId() == R.id.seekBarVolume)
 		{
 			if(fromUser)
 			{
 				m_preview.previewVolume(progress);
 			}
 		}
 	}
 
 	@Override
 	public void onStartTrackingTouch(SeekBar seekBar) {	}
 
 	@Override
 	public void onStopTrackingTouch(SeekBar seekBar) {	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onStop()
 	 */
 	@Override
 	protected void onStop() {
 		super.onStop();
 		m_preview.stop();
 	}
 
 	private class VolumePreview implements OnPreparedListener, OnCompletionListener, OnErrorListener {
 
 		/**
 		 * 
 		 */
 		public VolumePreview() {
 			m_player.setOnErrorListener(this);
 			m_player.setOnPreparedListener(this);
 			m_player.setOnCompletionListener(this);
 			m_player.reset();
 			m_player.setAudioStreamType(AudioManager.STREAM_ALARM);
 		}
 
 		private MediaPlayer m_player = new MediaPlayer();
 		private boolean m_playerReady = false;
 
 		public void setSoundUri(Context context, Uri soundUri) {
 			m_player.reset();
 			if(soundUri != null)
 			{
 				try {
 					m_player.setDataSource(context, soundUri);
 					m_playerReady = true;
 				} catch (IllegalArgumentException e) {
 					e.printStackTrace();
 				} catch (SecurityException e) {
 					e.printStackTrace();
 				} catch (IllegalStateException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		public void stop()
 		{
 			if(m_playerReady)
 			{
 				if(m_player.isPlaying())
 				{
 					m_player.stop();
 				}
 			}
 		}
 
 		@Override
 		public void onPrepared(MediaPlayer mp) {
 			m_player.start();
 		}
 
 		public void previewVolume(int volume)
 		{
 			if(m_playerReady)
 			{
 				if(!m_player.isPlaying())
 				{
 					m_player.prepareAsync();
 				}
 				AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
 				int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
 				if(volume < 0) volume = 0;
 				if(volume > maxVolume) volume = maxVolume;
 				am.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0);
 			}
 		}
 
 		@Override
 		public boolean onError(MediaPlayer mp, int what, int extra) {
 			m_player.reset();
 			m_playerReady = false;
 			return true;
 		}
 
 		@Override
 		public void onCompletion(MediaPlayer mp) {
 			m_player.stop();
 		}
 
 	}
 
 	@Override
 	public void colorChanged(int color) {
 		updateColor(color);		
 	}
 
 	@Override
 	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 		TimeSlider lightSlider = (TimeSlider)findViewById(R.id.timeSlider1);
 		TimeSlider soundSlider = (TimeSlider)findViewById(R.id.timeSlider2);
 		
 		switch(m_clickedTime)
 		{
 		case TIME_DAWN_START:
 			lightSlider.setLeftTime(hourOfDay, minute);
 			break;
 		case TIME_DAWN_END:
 			lightSlider.setRightTime(hourOfDay, minute);
 			break;
 		case TIME_SOUND_START:
 			soundSlider.setLeftTime(hourOfDay, minute);
 			break;
 		case TIME_SOUND_END:
 			soundSlider.setRightTime(hourOfDay, minute);
 			break;
 		}
 		resizeSliders();
 	}
 
 	@Override
 	public void onTimesChanged(TimeSlider s) {
 		if(m_resizeSlidersScheduler != null)
 		{
 			m_resizeSlidersScheduler.cancel();
 		}
 		m_resizeSlidersScheduler = new Timer();
 		m_resizeSlidersScheduler.schedule(
 				new TimerTask() {
 
 					@Override
 					public void run() {
 						runOnUiThread(
 								new Runnable() {
 									public void run() {
 										resizeSliders();
 										m_resizeSlidersScheduler = null;
 									}
 								});
 					}
 				}, RESIZE_SLIDERS_DELAY_MILLIS);	}
 }

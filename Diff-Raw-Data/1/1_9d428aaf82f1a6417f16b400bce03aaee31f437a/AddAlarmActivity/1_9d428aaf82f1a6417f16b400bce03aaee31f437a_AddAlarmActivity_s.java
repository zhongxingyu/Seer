 /**
  * Copyright (C) 2012 Emil Edholm, Emil Johansson, Johan Andersson, Johan Gustafsson
  * 
  * This file is part of dat255-bearded-octo-lama
  *
  *  dat255-bearded-octo-lama is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  dat255-bearded-octo-lama is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with dat255-bearded-octo-lama.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package it.chalmers.dat255_bearded_octo_lama.activities;
 
 import it.chalmers.dat255_bearded_octo_lama.Alarm;
 import it.chalmers.dat255_bearded_octo_lama.AlarmController;
 import it.chalmers.dat255_bearded_octo_lama.R;
 import it.chalmers.dat255_bearded_octo_lama.R.array;
 import it.chalmers.dat255_bearded_octo_lama.R.id;
 import it.chalmers.dat255_bearded_octo_lama.R.layout;
 import it.chalmers.dat255_bearded_octo_lama.games.GameManager;
 import it.chalmers.dat255_bearded_octo_lama.utilities.Filter;
 import it.chalmers.dat255_bearded_octo_lama.utilities.RingtoneFinder;
 import it.chalmers.dat255_bearded_octo_lama.utilities.Time;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import android.content.res.TypedArray;
 import android.media.Ringtone;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.Spinner;
 import android.widget.TabHost;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Lets the user selects an alarm time and add it.
  * @author Emil Edholm
  * @date 27 sep 2012
  * @modified by Emil Johansson
  * @date 14 oct 2012
  */
 public final class AddAlarmActivity extends AbstractActivity implements OnItemSelectedListener {
 
 	private Button currentTimeButton;
 	private final TimeFilter filter = new TimeFilter();
 	private boolean setAlarmAT = true; // if false, set alarm to an interval instead.
 	private List<Ringtone> tones = new ArrayList<Ringtone>();
 	private final List<Ringtone> selectedTones = new ArrayList<Ringtone>();
 	private final ArrayList<String> gamesList= new ArrayList<String>();
 	private final ArrayList<Integer> snoozeList= new ArrayList<Integer>();
 	private String choosenGame;
 	private CheckBox vibration, sound, games;
 	private int snoozeInterval;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(layout.activity_alarm_menu);
 		
 		initTabs();
 		initSettings();
 		
 		Spinner spinner = (Spinner)findViewById(id.time_options_spinner);
 		spinner.setOnItemSelectedListener(this);
 
 		// Set to the first (hour 0) button.
 		selectTimeButton(id.h0);
 	}
 
 	
 	private void initSettings() {		
 		//Checkboxes for turning on/off sound, vibration and games
 		vibration = (CheckBox)findViewById(id.vibration);
 		sound = (CheckBox)findViewById(id.sound);
 		games = (CheckBox)findViewById(id.games);
 
 		//Get all spinner views from the xml.
 		Spinner soundSpinner = (Spinner)findViewById(id.sound_list_spinner);
 		Spinner gameSpinner = (Spinner)findViewById(id.games_list_spinner);
 		Spinner snoozeSpinner = (Spinner)findViewById(id.snooze_list_spinner);
 
 		// Init the sound spinner.
 		ArrayList<String> songs = new ArrayList<String>();
 		tones = RingtoneFinder.getRingtones(this);
 		for(Ringtone r:tones){
 			songs.add(r.getTitle(getBaseContext()));
 		}
 		soundSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, songs));
 		soundSpinner.setOnItemSelectedListener(new SoundSpinnerListener());
 		
 		//Init the game spinner.
 		String[] tempGamesString = GameManager.getAvailableGamesStrings();
 		for(int i=0; i < tempGamesString.length; i++){
 			gamesList.add(tempGamesString[i]);
 		}
 		gameSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, gamesList));
 		gameSpinner.setOnItemSelectedListener(new GameSpinnerListener());
 		
 		//Init the snooze interval spinner.
 		for(int i = 1; i <= 10; i++){
 			snoozeList.add(i);
 		}
 		snoozeSpinner.setAdapter(new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, snoozeList));
 		snoozeSpinner.setOnItemSelectedListener(new SnoozeSpinnerListener());
 		
 	}
 	
 	/**
 	 * This method will create customized tab views overriding the old bland android tab view.
 	 */
 	private void initTabs() {
 		TabHost tabs = (TabHost) findViewById(R.id.tabhost);
 		tabs.setup();
 		
 		TabHost.TabSpec spec;
 		
 		//Setup first tab with specified text.
 		spec = tabs.newTabSpec("tag1");
 		spec.setContent(R.id.tab1);
 		createTabView(spec, "Add alarm");
 		tabs.addTab(spec);
 		
 		//Setup second tab with specified text.
 		spec = tabs.newTabSpec("tag2");
 		spec.setContent(R.id.tab2);
 		createTabView(spec, "Settings");
 		tabs.addTab(spec);
 	}
 	
 	/**
 	 * Helper method for creating tabs with unique text
 	 */
 	private View createTabView(TabHost.TabSpec spec, String text) {
 		View view = LayoutInflater.from(this).inflate(R.layout.tabs_layout, null);
 		TextView tabText = (TextView) view.findViewById(R.id.tabText);
 		tabText.setText(text);
 		
 		spec.setIndicator(view);
 		
 		return view;
 	}
 
 	/**
 	 * Retrieves the time from the "time" buttons.
 	 * @return returns an integer array with four values representing the time in hh:mm format where {@code h0 = int[0]; h1 = int[1]} etc.
 	 */
 	private int[] queryTimeValues() {
 		int numberOfBtns = 4;
 		int[] time = new int[numberOfBtns];
 
 		time[0] = getButtonNumber(R.id.h0);
 		time[1] = getButtonNumber(R.id.h1);
 		time[2] = getButtonNumber(R.id.m0);
 		time[3] = getButtonNumber(R.id.m1);
 
 		return time;
 	}
 
 	/** Select a specific time button based on ID */
 	private void selectTimeButton(int id) {
 		if(currentTimeButton != null) {
 			currentTimeButton.setBackgroundColor(getResources().getColor(R.color.white));
 		}
 
 		currentTimeButton = (Button)findViewById(id);
 		filter.setTimeButtonId(id);
 		currentTimeButton.setBackgroundColor(getResources().getColor(R.color.green));
 	}
 
 	/** Selects the next "time" button */
 	private void selectNextTimeButton() {
 		switch(currentTimeButton.getId()) {
 		case R.id.h0: 
 			selectTimeButton(id.h1);
 			break;
 		case R.id.h1:
 			selectTimeButton(id.m0);
 			break;
 		case R.id.m0:
 			selectTimeButton(id.m1);
 			break;
 		default:
 			selectTimeButton(id.h0);
 			break;
 		}
 	}
 
 	/** When the user clicks the add button (ie. when he is finished) */
 	private void addAlarm() {
 		int[] time = queryTimeValues();
 
 		int timeBase = 10;
 		int hour, minute;
 		// Combine values from format hh:mm to h:m.
 		if(setAlarmAT) {
 			hour   = time[0] * timeBase + time[1];
 			minute = time[2] * timeBase + time[3];
 		}
 		else {
 			hour   = time[0] * timeBase + time[1];
 			minute = time[2] * timeBase + time[3];
 
 			// Time now + value selected equals sometime in the future.
 			Calendar cal = Calendar.getInstance();
 			cal.add(Calendar.HOUR_OF_DAY, hour);
 			cal.add(Calendar.MINUTE, minute);
 
 			hour   = cal.get(Calendar.HOUR_OF_DAY);
 			minute = cal.get(Calendar.MINUTE);
 		}
 		AlarmController ac = AlarmController.INSTANCE;
 		// Defines the options for the alarm.
 		Alarm.Extras extras = new Alarm.Extras.Builder()
 								.useVibration(vibration.isChecked())
 								.useSound(sound.isChecked())
 								.gameNotification(games.isChecked())
 								.gameName(choosenGame)
 								.snoozeInterval(snoozeInterval)
 								.build();
 		Uri uri = ac.addAlarm(getApplicationContext(), true, hour, minute, extras);
 		Alarm a = ac.getAlarm(this, ac.extractIDFromUri(uri));
 
 		Toast.makeText(getApplicationContext(), "Alarm added at " + hour + ":" + minute + ". Time left: " + Time.getTimeLeft(a.getTimeInMS()), Toast.LENGTH_SHORT).show();
 		finish();
 	}
 
 	/**
 	 * Event handler for when one of the buttons on the numbpad is clicked.
 	 * @param view the button that was clicked.
 	 */
 	public void onNumpadClick(View view) {
 		// Check if allowed number and if so, select next time button
 		int numClicked = getButtonNumber((Button)view);
 		int h1 = getButtonNumber(id.h1);
 		if(filter.accept(numClicked)) {
 			currentTimeButton.setText(numClicked + "");
 
 			if(currentTimeButton.getId() == R.id.h0 && numClicked == 2 && h1 > 3) {
 				Button b = (Button) findViewById(R.id.h1);
 				b.setText("0");
 			}
 			selectNextTimeButton();
 		}
 	}
 
 	/**
 	 * @return number on the button
 	 */
 	private int getButtonNumber(Button button) {
 		return Integer.parseInt(button.getText().toString());
 	}
 
 	/**
 	 * @return the number on the button, -1 if not a button or error occurred.
 	 */
 	private int getButtonNumber(int id) {
 		View v = findViewById(id);
 		if(v instanceof Button) {
 			return getButtonNumber((Button)v);
 		}
 
 		return -1;
 	}
 
 	/**
 	 * Event handler for when one of the buttons indicating the time is clicked.
 	 * @param view the button that was clicked.
 	 */
 	public void onTimeClick(View view) {
 		selectTimeButton(view.getId());
 	}
 
 	/** What happens when an item is selected on the options spinner */
 	public void onItemSelected(AdapterView<?> parent, View view, 
 			int pos, long id) {
 		String option = String.valueOf(parent.getItemAtPosition(pos));
 		TypedArray options = getResources().obtainTypedArray(array.time_options_array);
 
 		// Assumes a static position of the options value and "Alarm at" at position 0 and "Alarm in" at pos 1.
 		// There is probably a better way to do this...
 
 		setAlarmAT = option.equals(options.getString(0));
 	}
 
 	public void onNothingSelected(AdapterView<?> parent) { /* Do nothing  */ }
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_add:
 			addAlarm(); 
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.activity_add_alarm, menu);
 		return true;
 	}
 
 	private static class TimeFilter implements Filter<Integer> {
 		private int selectedTimeButtonId, h0;
 
 		public boolean accept(Integer i) {
 			switch(selectedTimeButtonId) {
 			case id.h0:
 				h0 = i;
 				return i <= 2;
 			case id.h1:
 				return i <= 3 || h0 != 2;
 			case id.m0:
 				return i <= 5;
 			case id.m1:
 				return i <= 9;
 			}
 			return false;
 		}
 
 		public void setTimeButtonId(int id) {
 			selectedTimeButtonId = id;
 		}
 
 	}
 
 	public void addTestAlarm(View v) {
 		AlarmController ac = AlarmController.INSTANCE;
 		int countdown = 5;
 		
 		Calendar cal = Calendar.getInstance();
 		cal.add(Calendar.SECOND, countdown);
 		
 		// Defines the options for the test alarm.
 		Alarm.Extras extras = new Alarm.Extras.Builder()
 									.useVibration(vibration.isChecked())
 									.useSound(sound.isChecked())
 									.gameNotification(games.isChecked())
 									.gameName(choosenGame)
 									.snoozeInterval(snoozeInterval)
 									.addRingtoneIDs(RingtoneFinder.findRingtoneID(this, selectedTones))
 									.build();
 
 		ac.addAlarm(this, true, cal.getTimeInMillis(), extras);
 
 		Toast.makeText(getApplicationContext(), "Alarm added 5 seconds from now", Toast.LENGTH_SHORT).show();
 		finish();
 	}
 	
 
 	/**
 	 * Private class for listening to the Spinner in settings that chooses which sound to play
 	 * @author e
 	 *
 	 */
 	private class SoundSpinnerListener implements OnItemSelectedListener {
 
 		public void onItemSelected(AdapterView<?> parent, View view, int pos,
 				long id) {
 			selectedTones.clear();
 			Log.d("AddAlarmActivity", tones.get(pos).getTitle(getApplicationContext()));
 			selectedTones.add(tones.get(pos));
 		}
 		public void onNothingSelected(AdapterView<?> parent) {
 			// Do Nothing		
 		}
 
 	}
 	/**
 	 * Private class for listening to the Spinner in settings that chooses which game to play
 	 * @author e
 	 *
 	 */
 	private class GameSpinnerListener implements OnItemSelectedListener {
 
 		public void onItemSelected(AdapterView<?> parent, View view, int pos,
 				long id) {
 			choosenGame = gamesList.get(pos);
 		}
 		public void onNothingSelected(AdapterView<?> parent) {
 			// Do Nothing		
 		}
 
 	}
 	/**
 	 * Private class for listening to the Spinner in settings that chooses which game to play
 	 * @author e
 	 *
 	 */
 	private class SnoozeSpinnerListener implements OnItemSelectedListener {
 
 		public void onItemSelected(AdapterView<?> parent, View view, int pos,
 				long id) {
 			snoozeInterval = snoozeList.get(pos);
 		}
 		public void onNothingSelected(AdapterView<?> parent) {
 			// Do Nothing		
 		}
 
 	}
 
 }

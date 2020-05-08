 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.activity;
 
 import java.util.Locale;
 
 import net.engio.mbassy.listener.Handler;
 
 import org.joda.time.DateTime;
 
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.SFApplication;
 import se.chalmers.dat255.sleepfighter.adapter.AlarmAdapter;
 import se.chalmers.dat255.sleepfighter.android.utils.DialogUtils;
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.Alarm.Field;
 import se.chalmers.dat255.sleepfighter.model.Alarm.ScheduleChangeEvent;
 import se.chalmers.dat255.sleepfighter.model.AlarmList;
 import se.chalmers.dat255.sleepfighter.model.AlarmTimestamp;
 import se.chalmers.dat255.sleepfighter.model.challenge.ChallengeType;
 import se.chalmers.dat255.sleepfighter.receiver.AlarmReceiver;
 import se.chalmers.dat255.sleepfighter.utils.DateTextUtils;
 import se.chalmers.dat255.sleepfighter.utils.android.IntentUtils;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	private AlarmList manager;
 	private AlarmAdapter alarmAdapter;
 
 	/**
 	 * <p>
 	 * Returns the SFApplication.
 	 * </p>
 	 * 
 	 * <p>
 	 * Thank the genius programmers @ google for making<br/>
 	 * {@link #getApplication()} final removing the option of covariant return
 	 * type.
 	 * </p>
 	 * 
 	 * @return the SFApplication.
 	 */
 	public SFApplication app() {
 		return SFApplication.get();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		this.setContentView(R.layout.activity_main);
 
 		this.manager = this.app().getAlarms();
 		this.alarmAdapter = new AlarmAdapter(this, this.manager);
 
 		this.app().getBus().subscribe(this);
 
 		this.setupListView();
		
		this.updateChallengePoints();
 		this.updateEarliestText();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		this.updateEarliestText();
 	}
 
 	private void setupListView() {
 		ListView listView = (ListView) findViewById(R.id.mainAlarmsList);
 		listView.setAdapter(this.alarmAdapter);
 		listView.setOnItemClickListener(listClickListener);
 
 		// Register to get context menu events associated with listView
 		this.registerForContextMenu(listView);
 	}
 
 	private long getNow() {
 		return new DateTime().getMillis();
 	}
 
 	private OnItemClickListener listClickListener = new OnItemClickListener() {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			Alarm clickedAlarm = MainActivity.this.alarmAdapter
 					.getItem(position);
 			startAlarmEdit(clickedAlarm, false);
 
 		}
 	};
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		if (v.getId() == R.id.mainAlarmsList) {
 			String[] menuItems = getResources().getStringArray(
 					R.array.main_list_context_menu);
 			for (int i = 0; i < menuItems.length; i++) {
 				menu.add(0, i, i, menuItems[i]);
 			}
 			// temporarily add an extra context menu item for starting an alarm
 			// TODO remove
 			menu.add(0, menuItems.length, menuItems.length, "Start alarm");
 		}
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
 				.getMenuInfo();
 		Alarm selectedAlarm = (alarmAdapter.getItem(info.position));
 
 		switch (item.getOrder()) {
 		case 0:
 			startAlarmEdit(selectedAlarm, false);
 			return true;
 		case 1:
 			deleteAlarm(selectedAlarm);
 			return true;
 		case 2:
 			copyAlarm(selectedAlarm);
 			return true;
 		case 3:
 			// TODO remove case
 			startAlarm(selectedAlarm);
 			return true;
 		default:
 			return false;
 
 		}
 	}
 
 	private void startAlarmEdit(Alarm alarm, boolean isNew) {
 		Intent intent = new Intent(this, AlarmSettingsActivity.class);
 		new IntentUtils(intent).setAlarmId(alarm);
 
 		intent.putExtra(AlarmSettingsActivity.EXTRA_ALARM_IS_NEW, isNew);
 
 		startActivity(intent);
 	}
 
 	private void startAlarm(Alarm alarm) {
 		// Send intent directly to receiver
 		Intent intent = new Intent(this, AlarmReceiver.class);
 		new IntentUtils(intent).setAlarmId(alarm.getId());
 		sendBroadcast(intent);
 	}
 
 	private void startGlobalSettings() {
 		Intent intent = new Intent(this, GlobalSettingsActivity.class);
 		startActivity(intent);
 	}
 
 	private void deleteAlarm(final Alarm alarm) {
 		String message = getString(R.string.confirm_delete);
 		DialogUtils.showConfirmationDialog(message, this,
 				new OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						MainActivity.this.manager.remove(alarm);
 					}
 				});
 	}
 
 	private void copyAlarm(Alarm alarm) {
 		this.newAlarm(new Alarm(alarm), false);
 	}
 
 	private void addAlarm() {
 		this.newAlarm(app().getFromPresetFactory().createAlarm(), true);
 	}
 
 	private void newAlarm(Alarm alarm, boolean isAdded) {
 		if (alarm.isUnnamed()) {
 			alarm.setUnnamedPlacement(this.manager.findLowestUnnamedPlacement());
 		}
 
 		this.manager.add(alarm);
 		this.startAlarmEdit(alarm, isAdded);
 	}
 
 	/**
 	 * Handles a change to an alarm's name by refreshing the list.
 	 * 
 	 * @param event
 	 *            the event
 	 */
 	@Handler
 	public void handleAlarmNameChange(Alarm.MetaChangeEvent event) {
 		// Ignore other than name change events
 		if (event.getModifiedField() != Field.NAME) {
 			return;
 		}
 
 		// Refresh the list items
 		this.alarmAdapter.notifyDataSetChanged();
 	}
 
 	/**
 	 * Handles a change in time related data in any alarm.
 	 * 
 	 * @param evt
 	 *            the event.
 	 */
 	@Handler
 	public void handleScheduleChange(ScheduleChangeEvent evt) {
 		final MainActivity self = this;
 		this.runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				self.updateEarliestText();
 			}
 		});
 
 		// Refresh the list items
 		this.alarmAdapter.notifyDataSetChanged();
 	}
 
 	/**
 	 * Handles a change in the list of alarms (the list itself, deletion,
 	 * insertion, etc, not edits in an alarm).
 	 * 
 	 * @param evt
 	 *            the event.
 	 */
 	@Handler
 	public void handleListChange(AlarmList.Event evt) {
 		final MainActivity self = this;
 		this.runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				self.updateEarliestText();
 				self.alarmAdapter.notifyDataSetChanged();
 			}
 		});
 	}
 
 	/**
 	 * Sets the earliest time text.
 	 */
 	private void updateEarliestText() {
 		long now = this.getNow();
 
 		TextView earliestTimeText = (TextView) findViewById(R.id.earliestTimeText);
 		AlarmTimestamp stamp = this.manager.getEarliestAlarm(now);
 
 		Resources res = this.getResources();
 		String text = this.app().getPrefs().displayPeriodOrTime() ? DateTextUtils
 				.getTime(res, now, stamp, Locale.getDefault()) : DateTextUtils
 				.getTimeToText(res, now, stamp);
 
 		earliestTimeText.setText(text);
 	}
 
	private void updateChallengePoints() {
		TextView cpText = (TextView) findViewById(R.id.mainChallengePoints);
		cpText.setText(this.app().getPrefs().getChallengePoints() + " C. Pts.");
	}
	
 	/**
 	 * Sends the user to the activity for editing GPSFilterArea:s.
 	 */
 	private void startGPSFilterAreaEdit() {
 		Intent i = new Intent( this, ManageGPSFilterAreasActivity.class );
 		this.startActivity( i );
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.action_add:
 			this.addAlarm();
 			return true;
 
 		case R.id.action_settings:
 			this.startGlobalSettings();
 			return true;
 
 		case R.id.action_start_challenge:
 			startDebugChallenge();
 			return true;
 
 		case R.id.action_gpsfilter_area_edit:
 			this.startGPSFilterAreaEdit();
 			return true;
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/**
 	 * Debug method for launching dialog where any challenge defined in
 	 * {@link ChallengeType} can be started.
 	 */
 	private void startDebugChallenge() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		final ChallengeType[] types = ChallengeType.values();
 
 		// Making string array "copy" of types for use with standard dialog
 		String[] items = new String[types.length];
 		for (int i = 0; i < types.length; i++) {
 			items[i] = types[i].name();
 		}
 
 		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				// The clicked challenge type
 				ChallengeType type = types[which];
 
 				// Start the selected challenge
 				Intent i = new Intent(MainActivity.this, ChallengeActivity.class);
 				new IntentUtils( i ).setAlarmId( SFApplication.get().getFromPresetFactory().getPreset() ).setSettingPresetAlarm( true );
 				i.putExtra(ChallengeActivity.BUNDLE_CHALLENGE_TYPE, type);
 				startActivity(i);
 			}
 		};
 		builder.setItems(items, listener);
 		AlertDialog dialog = builder.create();
 		dialog.show();
 	}
 }

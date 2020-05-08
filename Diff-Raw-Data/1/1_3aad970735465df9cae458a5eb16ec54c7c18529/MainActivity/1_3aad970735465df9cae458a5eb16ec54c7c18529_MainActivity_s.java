 /*
  * Copyright (C) 2012 Joakim Persson, Daniel Augurell, Adrian Bjugrd, Andreas Roln
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.chalmers.dat255.group09.Alarmed.activity;
 
 import java.util.Arrays;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.CheckBox;
 import android.widget.ListView;
 import android.widget.Toast;
 import edu.chalmers.dat255.group09.Alarmed.R;
 import edu.chalmers.dat255.group09.Alarmed.adapter.BrowseAlarmAdapter;
 import edu.chalmers.dat255.group09.Alarmed.controller.AlarmController;
 import edu.chalmers.dat255.group09.Alarmed.model.Alarm;
 
 /**
  * The main activity of Alarmed.
  * 
  * @author Daniel Augurell
  * @author Joakim Persson
  * 
  */
 public class MainActivity extends Activity {
 
 	public static final int ADD_ALARM_REQUEST_CODE = 1;
 	public static final int EDIT_ALARM_REQUEST_CODE = 2;
 	private static final int DELETE_ALARM_MENU = 1;
 	private static final int EDIT_ALARM_MENU = 2;
 	private BrowseAlarmAdapter alarmAdapter;
 	private AlarmController aControl;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_main);
 		initController();
 		initAdapter();
 	}
 
 	/**
 	 * Init method to initiate controllers.
 	 */
 	private void initController() {
 		aControl = AlarmController.getInstance();
 		aControl.init(this);
 	}
 
 	/**
 	 * Init method to initiate adapters.
 	 */
 	private void initAdapter() {
 		ListView listView = (ListView) findViewById(R.id.alarms_list);
 		alarmAdapter = new BrowseAlarmAdapter(this, R.layout.alarms_list_item,
 				aControl.getAllAlarms());
 		alarmAdapter.setContexMenuListner(this);
 		listView.setAdapter(alarmAdapter);
 		registerForContextMenu(listView);
 	}
 
 	/**
 	 * Toggle the alarms state to be the same as the view when changed.
 	 * 
 	 * @param view
 	 *            The view that has been pressed.
 	 */
 	public void onAlarmEnable(View view) {
 		if (view instanceof CheckBox) {
 			CheckBox box = (CheckBox) view;
 			aControl.enableAlarm((Integer) box.getTag(), box.isChecked());
 		}
 
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		if (v.getId() == R.id.alarms_list) {
 			menu.add(Menu.NONE, 2, 1, R.string.edit_alarm);
 			menu.add(Menu.NONE, 1, 2, R.string.delete_alarm);
 		}
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
 				.getMenuInfo();
 		ListView listView = (ListView) findViewById(R.id.alarms_list);
 		Alarm alarm = (Alarm) listView.getAdapter().getItem(info.position);
 
 		switch (item.getItemId()) {
 		case DELETE_ALARM_MENU:
 			aControl.deleteAlarm(alarm.getId());
 			break;
 		case EDIT_ALARM_MENU:
 			editAlarm(alarm);
 			break;
 		default:
 			break;
 		}
 		updateList();
 		return false;
 	}
 
 	/**
 	 * Starts a new activity to edit an alarms settings.
 	 * 
 	 * @param alarm
 	 *            The alarm to be edited
 	 */
 	private void editAlarm(Alarm alarm) {
 		Intent intent = new Intent(this, CreateAlarm.class);
 		intent.putExtra("ID", alarm.getId());
 		intent.putExtra("requestCode", EDIT_ALARM_REQUEST_CODE);
 		intent.putExtra("time",
 				alarm.getAlarmHours() + ":" + alarm.getAlarmMinutes());
 		intent.putExtra("daysOfWeek", alarm.getBooleanArrayDayOfWeek());
 		intent.putExtra("volume", alarm.getVolume());
 		startActivityForResult(intent, EDIT_ALARM_REQUEST_CODE);
 		overrideTransition();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.menu_add_alarm) {
 			boolean[] days = new boolean[Alarm.DAYS_OF_WEEK];
 			Arrays.fill(days, false);
 			startActivityForResult(
 					new Intent(this, CreateAlarm.class).putExtra("daysOfWeek",
 							days), ADD_ALARM_REQUEST_CODE);
 			overrideTransition();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * Makes the transition between views smoother by animating them.
 	 */
 	private void overrideTransition() {
 		int fadeIn = android.R.anim.fade_in;
 		int fadeOut = android.R.anim.fade_out;
 		overridePendingTransition(fadeIn, fadeOut);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (isResonseValid(resultCode)) {
 			if (requestCode == EDIT_ALARM_REQUEST_CODE) {
 				aControl.deleteAlarm(data.getIntExtra("ID", -1));
 			}
 			if (requestCode == ADD_ALARM_REQUEST_CODE
 					|| requestCode == EDIT_ALARM_REQUEST_CODE) {
 				createAlarm(data.getIntExtra("hours", -1),
 						data.getIntExtra("minutes", -1),
 						data.getIntExtra("days", 0),
 						data.getStringExtra("module"),
 						data.getIntExtra("volume", 1));
 			}
 
 		}
 	}
 
 	/**
 	 * Checks if the result code from the Activity that finishes is valid.
 	 * 
 	 * @param resultCode
 	 *            The code returned from onActivityResult
 	 * @return true if the resultCode was valid.
 	 */
 	private boolean isResonseValid(int resultCode) {
 		return resultCode == RESULT_OK;
 	}
 
 	/**
 	 * Creates a new alarm and then updates the view.
 	 * 
 	 * @param hour
 	 *            The hour of the new alarm
 	 * @param minute
 	 *            The minute of the new alarm
 	 * @param dayOfWeek
 	 *            The days the be recurring as bits in a integer
 	 * @param module
 	 *            The module to be started on the alarms activation
 	 * @param volume
 	 *            Volume of the alarm
 	 */
 
 	private void createAlarm(int hour, int minute, int dayOfWeek,
 			String module, int volume) {
 		Alarm alarm = new Alarm(hour, minute, 0, module, volume);
 		alarm.setDaysOfWeek(dayOfWeek);
 		aControl.addAlarm(alarm);
 		Toast.makeText(this, alarm.toString(), Toast.LENGTH_LONG).show();
 		updateList();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		updateList();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		aControl.destroy();
 	}
 
 	/**
 	 * Updates the list of the alarms to the current list.
 	 */
 	private void updateList() {
 		alarmAdapter.updateList(aControl.getAllAlarms());
 	}
 }

 package se.chalmers.dat255.sleepfighter.activities;
 
 import net.engio.mbassy.listener.Handler;
 
 import org.joda.time.DateTime;
 import org.joda.time.MutableDateTime;
 
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.SFApplication;
 import se.chalmers.dat255.sleepfighter.debug.Debug;
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.Alarm.DateChangeEvent;
 import se.chalmers.dat255.sleepfighter.model.AlarmTimestamp;
 import se.chalmers.dat255.sleepfighter.model.AlarmsManager;
 import se.chalmers.dat255.sleepfighter.utils.DateTextUtils;
 import se.chalmers.dat255.sleepfighter.utils.collect.ObservableList;
 import se.chalmers.dat255.sleepfighter.utils.message.Message;
 import se.chalmers.dat255.sleepfighter.utils.message.MessageBus;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.ToneGenerator;
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
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	private AlarmsManager manager;
 	private AlarmAdapter alarmAdapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.activity_main);
 
 		this.manager = ((SFApplication) getApplication()).getAlarmsManager();
 		this.alarmAdapter = new AlarmAdapter(this, this.manager);
 
 		// this.immedateTestAlarmSchedule();
 
 		MessageBus<Message> bus = ((SFApplication) getApplication()).getBus();
 		bus.subscribe(this);
 
 		ListView listView = (ListView) findViewById(R.id.mainAlarmsList);
 
 		listView.setAdapter(this.alarmAdapter);
 
 		listView.setOnItemClickListener(listClickListener);
 
 		// Register to get context menu events associated with listView
 		registerForContextMenu(listView);
 
 		this.updateEarliestText();
 	}
 
 	private void immedateTestAlarmSchedule() {
 		// For testing purposes, we want an alarm 5 seconds in the future,
 		// calculate that time.
 		MutableDateTime time = new MutableDateTime();
 		time.addSeconds(6);
 
 		// Make an alarm with that time.
 		Alarm alarm = new Alarm(time);
 		alarm.setId(1);
 		this.manager.add(alarm);
 		long scheduleTime = alarm.getNextMillis(this.getNow());
 
 		// Make pending intent.
 		Intent intent = new Intent(this, AlarmReceiver.class);
 		intent.putExtra("alarm_id", alarm.getId());
 		PendingIntent pi = PendingIntent.getBroadcast(this, -1, intent,
 				PendingIntent.FLAG_UPDATE_CURRENT);
 
 		// Schedule alarm.
 		AlarmManager androidAM = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		androidAM.set(AlarmManager.RTC_WAKEUP, scheduleTime, pi);
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
 			startAlarmEdit(clickedAlarm);
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
 		}
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
 				.getMenuInfo();
 		Alarm selectedAlarm = (alarmAdapter.getItem(info.position));
 
 		// TODO perhaps use something other than order
 		switch (item.getOrder()) {
 		case 0:
 			startAlarmEdit(selectedAlarm);
 			return true;
 		case 1:
 			deleteAlarm(selectedAlarm);
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	private void startAlarmEdit(Alarm alarm) {
 		Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
 		// TODO Launch alarm edit intent
 	}
 
 	private void deleteAlarm(Alarm alarm) {
 		this.manager.remove(alarm);
 
 		// TODO only do this when receiving a ObservableList.Event in a handler
 		// method (couln't get it to work)
 		this.alarmAdapter.notifyDataSetChanged();
 	}
 
 	/**
 	 * Handles a change in time related data in any alarm.
 	 * 
 	 * @param evt
 	 *            the event.
 	 */
 	@Handler
 	public void handleDateChange(DateChangeEvent evt) {
 		this.runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				MainActivity.this.updateEarliestText();
 			}
 		});
 	}
 
 	/**
 	 * Sets the earliest time text.
 	 * 
 	 * @param now
 	 *            the current time.
 	 */
 	private void updateEarliestText() {
 		Debug.d("updateEarliestText");
 
 		long now = this.getNow();
 
 		TextView earliestTimeText = (TextView) findViewById(R.id.earliestTimeText);
 		AlarmTimestamp stamp = this.manager.getEarliestAlarm(now);
 		String text = DateTextUtils.getTimeToText(this.getResources(), now,
 				stamp);
 
 		earliestTimeText.setText(text);
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
 			Intent intent = new Intent(this, AlarmSettingsActivity.class);
 			/*
 			 * // Preliminary code to add a new alarm and fetch its ID
 			 * manager.add(new Alarm(0, 0)); Bundle b = new Bundle();
 			 * b.putInt("id", manager.getLatestAlarmId()); intent.putExtras(b);
 			 */
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void challenge(View view) {
 		Intent intent = new Intent(this, AlarmActivity.class);
 		startActivity(intent);
 	}
 }

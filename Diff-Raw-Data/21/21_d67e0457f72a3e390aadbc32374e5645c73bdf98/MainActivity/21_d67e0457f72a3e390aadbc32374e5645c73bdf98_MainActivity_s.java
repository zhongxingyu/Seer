 package se.chalmers.dat255.sleepfighter.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.engio.mbassy.listener.Handler;
 
 import org.joda.time.DateTime;
 import org.joda.time.MutableDateTime;
 
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.debug.Debug;
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.Alarm.DateChangeEvent;
 import se.chalmers.dat255.sleepfighter.model.AlarmsManager;
 import se.chalmers.dat255.sleepfighter.utils.DateTextUtils;
 import se.chalmers.dat255.sleepfighter.utils.message.Message;
 import se.chalmers.dat255.sleepfighter.utils.message.MessageBus;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
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
 
 		// Hard code in some sample alarms
 		// TODO fetch from where actual alarms will be stored
 		Alarm namedAlarm = new Alarm(13, 37);
 		namedAlarm.setName("Named alarm");
 		namedAlarm.setEnabledDays( new boolean[] { true, false, true, false, true, false, true } );
 
 		Alarm alarm2 = new Alarm(8, 30);
 		alarm2.setName("Untitled Alarm");
 		alarm2.setActivated( false );
 
 		Alarm alarm3 = new Alarm(7, 0);
 		alarm3.setName("Untitled Alarm");
 
 		Alarm alarm4 = new Alarm(7, 0);
 		alarm4.setName("Untitled Alarm");
 		alarm4.setEnabledDays( new boolean[7] );
 
 		List<Alarm> alarms = new ArrayList<Alarm>();
 		alarms.add(namedAlarm);
 		alarms.add(alarm2);
 		alarms.add(alarm3);
 		alarms.add(alarm4);
 
 		MessageBus<Message> bus = new MessageBus<Message>();
 		bus.subscribe( this );
 
 		this.manager = new AlarmsManager( alarms );
 		this.manager.setMessageBus( bus );
 
 		//this.immedateTestAlarmSchedule();
 
 		ListView listView = (ListView) findViewById(R.id.mainAlarmsList);
 
 		this.alarmAdapter = new AlarmAdapter(this, this.manager);
 		listView.setAdapter(this.alarmAdapter);
 
 		listView.setOnItemClickListener(listClickListener);
 
 		// Register to get context menu events associated with listView
 		registerForContextMenu(listView);
 
 		this.updateEarliestText();
 	}
 
 	private void immedateTestAlarmSchedule() {
 		// For testing purposes, we want an alarm 5 seconds in the future, calculate that time.
 		MutableDateTime time = new MutableDateTime();
 		time.addSeconds( 6 );
 
 		// Make an alarm with that time.
 		Alarm alarm = new Alarm( time );
 		alarm.setId( 1 );
 		this.manager.add( alarm );
 		long scheduleTime = alarm.getNextMillis( this.getNow() );
 
 		// Make pending intent.
 		Intent intent = new Intent(this, AlarmReceiver.class);
 		intent.putExtra( "alarm_id", alarm.getId() );
 		PendingIntent pi = PendingIntent.getBroadcast( this, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT );
 
 		// Schedule alarm.
 		AlarmManager androidAM = (AlarmManager) getSystemService( Context.ALARM_SERVICE );
 		androidAM.set( AlarmManager.RTC_WAKEUP, scheduleTime, pi );
 	}
 
 	private long getNow() {
 		return new DateTime().getMillis();
 	}
 	
 	private OnItemClickListener listClickListener = new OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			Alarm clickedAlarm = MainActivity.this.alarmAdapter.getItem(position);
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
 		Toast.makeText(this, "Not yet implemented", Toast.LENGTH_SHORT).show();
 		// TODO Delete alarm
 	}
 
 	/**
 	 * Handles a change in time related data in any alarm.
 	 *
 	 * @param evt the event.
 	 */
 	@Handler
 	public void handleDateChange( DateChangeEvent evt ) {
 		this.runOnUiThread( new Runnable() {
 			@Override
 			public void run() {
 				MainActivity.this.updateEarliestText();
 			}
 		});
 	}
 
 	/**
 	 * Sets the earliest time text.
 	 *
 	 * @param now the current time.
 	 */
 	private void updateEarliestText() {
 		Debug.d( "updateEarliestText" );
 
 		long now = this.getNow();
 
 		TextView earliestTimeText = (TextView) findViewById( R.id.earliestTimeText );
		earliestTimeText.setText( DateTextUtils.getTimeToText( this.getResources(), now, this.manager.getEarliestAlarm( now ) ) );
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
 	    		startActivity(intent);
 	            return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 }

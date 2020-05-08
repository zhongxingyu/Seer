 package se.chalmers.dat255.sleepfighter.activity;
 
 import net.engio.mbassy.listener.Handler;
 
 import org.joda.time.DateTime;
 
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.SFApplication;
 import se.chalmers.dat255.sleepfighter.adapter.AlarmAdapter;
import se.chalmers.dat255.sleepfighter.audio.AlarmAudioManager;
 import se.chalmers.dat255.sleepfighter.audio.VibrationManager;
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.Alarm.ScheduleChangeEvent;
 import se.chalmers.dat255.sleepfighter.model.AlarmList;
 import se.chalmers.dat255.sleepfighter.model.AlarmTimestamp;
 import se.chalmers.dat255.sleepfighter.utils.DateTextUtils;
 import se.chalmers.dat255.sleepfighter.utils.android.IntentUtils;
 import android.app.Activity;
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
 
 public class MainActivity extends Activity {
 	private AlarmList manager;
 	private AlarmAdapter alarmAdapter;
 
 	/**
 	 * <p>Returns the SFApplication.</p>
 	 *
 	 * <p>Thank the genius programmers @ google for making<br/>
 	 * {@link #getApplication()} final removing the option of covariant return type.</p>
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
 
 		this.updateEarliestText();
		
		AlarmAudioManager.getInstance().setup(this);
 		VibrationManager.getInstance().setup(this);
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
 
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			Alarm clickedAlarm = MainActivity.this.alarmAdapter.getItem(position);
 			startAlarmEdit(clickedAlarm, false);
 
 		}
 	};
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		if (v.getId() == R.id.mainAlarmsList) {
 			String[] menuItems = getResources().getStringArray(R.array.main_list_context_menu);
 			for (int i = 0; i < menuItems.length; i++) {
 				menu.add(0, i, i, menuItems[i]);
 			}
 		}
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 		Alarm selectedAlarm = (alarmAdapter.getItem(info.position));
 
 		// TODO perhaps use something other than order
 		switch (item.getOrder()) {
 			case 0:
 				startAlarmEdit(selectedAlarm, false);
 				return true;
 			case 1:
 				deleteAlarm(selectedAlarm);
 				return true;
 			case 2:
				AlarmAudioManager.getInstance().play();
				return true;
			case 3:
				AlarmAudioManager.getInstance().stop();	
				return true;
			
			case 4:
 				VibrationManager.getInstance().startVibrate();
 				return true;
			case 5:
 				VibrationManager.getInstance().stopVibrate();	
 				return true;
			case 6:
 				copyAlarm(selectedAlarm);
 				return true;
 
				
 			default:
 				return false;
 
 		}
 	}
 
 	private void startAlarmEdit( Alarm alarm, boolean isNew ) {
 		Intent intent = new Intent(this, AlarmSettingsActivity.class );
 		new IntentUtils( intent ).setAlarmId( alarm );
 
 		intent.putExtra( AlarmSettingsActivity.EXTRA_ALARM_IS_NEW, isNew );
 
 		startActivity( intent );
 	}
 
 	private void startGlobalSettings( ) {
 		Intent intent = new Intent(this, GlobalSettingsActivity.class );
 		startActivity( intent );
 	}
 
 	private void deleteAlarm( Alarm alarm ) {
 		this.manager.remove( alarm );
 	}
 
 	private void copyAlarm( Alarm alarm ) {
 		this.newAlarm( new Alarm( alarm ), false );
 	}
 
 	private void addAlarm() {
 		this.newAlarm( new Alarm(), true );
 	}
 
 	private void newAlarm( Alarm alarm, boolean isAdded ) {
 		if ( alarm.isUnnamed() ) {
 			alarm.setUnnamedPlacement( this.manager.findLowestUnnamedPlacement() );
 		}
 
 		this.manager.add( alarm );
 		this.startAlarmEdit( alarm, isAdded );
 	}
 
 	/**
 	 * Handles a change in time related data in any alarm.
 	 * 
 	 * @param evt the event.
 	 */
 	@Handler
 	public void handleDateChange( ScheduleChangeEvent evt ) {
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
 	 * Handles a change in the list of alarms (the list itself, deletion, insertion, etc, not edits in an alarm).
 	 *
 	 * @param evt the event.
 	 */
 	@Handler
 	public void handleListChange( AlarmList.Event evt ) {
 		final MainActivity self = this;
 		this.runOnUiThread( new Runnable() {
 			@Override
 			public void run() {
 				self.updateEarliestText();
 				self.alarmAdapter.notifyDataSetChanged();
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
 		long now = this.getNow();
 
 		TextView earliestTimeText = (TextView) findViewById( R.id.earliestTimeText );
 		AlarmTimestamp stamp = this.manager.getEarliestAlarm( now );
 		String text = DateTextUtils.getTimeToText( this.getResources(), now,  stamp);
 		
 
 		earliestTimeText.setText(text);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected( MenuItem item ) {
 		// Handle item selection
 		switch ( item.getItemId() ) {
 		case R.id.action_add:
 			this.addAlarm();
 			return true;
 		case R.id.action_settings:
 			this.startGlobalSettings();
 			return true;
 		default:
 			return super.onOptionsItemSelected( item );
 		}	
 	}
 }

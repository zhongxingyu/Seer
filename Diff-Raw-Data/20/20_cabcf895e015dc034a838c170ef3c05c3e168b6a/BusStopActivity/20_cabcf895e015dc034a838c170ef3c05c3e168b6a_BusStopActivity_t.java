 package uk.ac.cam.cl.dtg.android.time.BusTimetables;
 
 import uk.ac.cam.cl.dtg.android.time.buses.BusArrival;
 import uk.ac.cam.cl.dtg.android.time.buses.BusArrivalData;
 import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
 import uk.ac.cam.cl.dtg.android.time.data.TransportDataException;
 import uk.ac.cam.cl.dtg.android.time.data.TransportDataProvider;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnCreateContextMenuListener;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class BusStopActivity extends Activity implements Runnable {
 
 	// Thread to update the times
 	Thread updater;
 	private BusStop currStop;
 	DataStore db;
 	BusArrivalData nextBuses;
 	boolean keepUpdating = true;
 	TextView textLastUpdate;
 
 	// Constants for context menu
 	private static final int REMIND_ME = 0;
 	
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Open DataStore
 		db = new DataStore(this, true);
 
 		// Check to see if we were started by an implicit intent...
 		Intent i = getIntent();
 		if(i.getData() != null) {
 
 			// See if the intent that invoked us was meant for us (probably redundant but best to be sure)
 			if(i.getData().getHost().equals("uk.ac.cam.cl.dtg.android.time.BusStopApp")) {
 
 				// Grab the stop ref our of intent's URI
 				String ref = i.getData().getPath().replaceFirst("/", "");
 				Log.i("BusStopActivity","StopRef from activity is: "+ref);
 
 				// Load stop from database
 				try {
 
 					currStop = db.getStop(ref);
 
 				} catch (DataStoreException e) {
 
 					// The stop is not in our database. Show error and die gracefully.
 					e.printStackTrace();
 				}
 
 			}
 
 		} else {
 
 			// Fetch the bus stop we're looking at from intent's extras
 			currStop = (BusStop) this.getIntent().getSerializableExtra("stop");
 
 		}
 
 		// Debug
 		Log.i("BusStopActivity","Stop is: "+currStop);
 
 		// Request indeterminate progress wheel
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 
 		// Load layout
 		setContentView(R.layout.busstopactivity);
 
 		// Blank title bar at first
 		this.setProgressBarIndeterminateVisibility(false);
 		this.setTitle("");
 
 		// Set title
 		TextView textStopName = (TextView) findViewById(R.id.textStopName_StopAct);
 		textStopName.setText(currStop.getName());
 
 		// Set last updated time
 		textLastUpdate = (TextView) findViewById(R.id.textLastUpdate);
 		textLastUpdate.setText("");
 
 		// Setup the starred check-star
 		CheckBox favouriteCheckbox = (CheckBox) findViewById(R.id.FavouriteCheckbox);
 
 		// Are we already a favourite? Set it now
 		boolean isFav = db.isFavourite(currStop);
 		favouriteCheckbox.setChecked(isFav);
 
 		// Any future changes to the checkbox go through our listener (but previous change ^^^ doesn't)
 		favouriteCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener()
 		{
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
 			{
 				Log.i("Fav checked!", "Should we save? "+isChecked);
 
 				favouriteCheckChanged(isChecked);  	        	
 
 			}
 		});
 
 		// Set up context menu for long clicks on a departure
 		ListView list = (ListView) findViewById(R.id.NextBuses); 
 		list.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
 			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
 				menu.setHeaderIcon(R.drawable.bus);
 				menu.setHeaderTitle("Bus options...");
				menu.add(0, REMIND_ME, 0, "Set reminder");
 			}
 		}); 
 
 	}
 
 	/**
 	 * Invoked when the 'starred' checkbox changes state
 	 * 
 	 * @param favourited New value of checkbox
 	 */
 	private void favouriteCheckChanged(boolean favourited) {
 
 		// If we've been set, save it to the database
 		if(favourited) {
 
 			Log.i("BSMD",currStop.toString());
 			db.setFavourite(currStop);
 
 		} else {
 
 			// Otherwise remove favourite from database
 			db.clearFavourite(currStop);
 
 		}
 
 		// A bit of haptic feedback
 		boolean haptic = Preferences.getBool("haptics", true);
 		if(haptic) {
 			Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
 			vibrator.vibrate(50);
 		}
 	}
 
 
 	//TODO: Update underlying adapter rather than recreating
 	private void updateArrivals() {
 
 		ListView list = (ListView) findViewById(R.id.NextBuses);   
 
 		Log.i("UpdateArrivals","Update the screen with "+nextBuses.NextBuses.size()+" buses");
 
 		BusArrivalAdapter adapt = new BusArrivalAdapter(this, nextBuses);
 		adapt.rowView = R.layout.busstoparrivallarge;
 		list.setAdapter(adapt);
 
 		if(nextBuses.NextBuses.size() == 0) textLastUpdate.setText("Sorry, there is no timetable information available for this stop at present.");
 	}
 
 	protected void onPause() {
 		super.onPause();
 
 
 		keepUpdating = false;
 	}
 
 
 	protected void onResume() {
 
 		super.onResume();
 
 		// Fire off the thread to fetch new arrivals		
 		updater = new Thread(this);
 		updater.start();
 		updater.setName("ArrivalsUpdater");
 		keepUpdating = true;
 
 	}
 
 	public boolean onContextItemSelected(MenuItem item) {
 
 		AdapterView.AdapterContextMenuInfo selectedMenuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo(); 
 
 		switch (item.getItemId()) {
 		case REMIND_ME:
 
 			// To get the id of the clicked item in the list use menuInfo.id
 			Log.d("ContextMenu", "Remind me with ringtone: "+Preferences.getString("alarmtone", ""));
 			setAlarm(selectedMenuInfo.position);
 
 			break;
 
 		default:
 
 			return super.onContextItemSelected(item);
 
 		}
 		return true;
 	} 
 
 	/**
 	 * Sets an alarm for the arrival at position listPosition
 	 * @param listPosition
 	 */
 	private void setAlarm(int listPosition) {
 
 		// Get the arrival data out of list
 		ListView list = (ListView) findViewById(R.id.NextBuses);
 		BusArrival arrival = (BusArrival) list.getAdapter().getItem(listPosition);
 
 		Log.d("SetAlarm","List position: "+listPosition+" Arrival: "+arrival);
 
 		long currTime = System.currentTimeMillis();
 		
 		// When to set alarm for?
 		long reminder_int = Long.parseLong(Preferences.getString("reminder_interval", "6000"));
 		boolean dueAlarm = Preferences.getBool("reminder_due", true);
 		boolean followUpAlarm = Preferences.getBool("reminder_followup", false);//TODO: change back to true
 		Log.i("SetAlarm","Long interval: "+reminder_int);
 		Log.i("SetAlarm","Due alarm? "+dueAlarm);
 		Log.i("SetAlarm","Follow up? "+followUpAlarm);
 
 		// Times at which the alarms should go off
 		long target_long = arrival.getDueTime().getTime() - reminder_int;
 		long target_1min = arrival.getDueTime().getTime() - (60*1000);
 		long target_followup = arrival.getDueTime().getTime() + (10*1000);
 		
 		// Alarm at 10 mins
 		Intent i_long = new Intent(AlarmReceiver.BUS_ARRIVAL_REMINDER);
 		i_long.setData(Uri.parse("reminder://uk.ac.cam.cl.dtg.android.time.BusStopApp/"+Long.toString(target_long)));
 		i_long.putExtra("stop", currStop);
 		i_long.putExtra("arrival", arrival);		
 		PendingIntent alarm_long = PendingIntent.getBroadcast(this, 0, i_long, 0);
 
 		// Alarm just before the bus arrives
 		Intent i_1min = new Intent(AlarmReceiver.BUS_ARRIVAL_REMINDER);
 		i_1min.setData(Uri.parse("reminder://uk.ac.cam.cl.dtg.android.time.BusStopApp/"+Long.toString(target_1min)));
 		i_1min.putExtra("stop", currStop);
 		i_1min.putExtra("arrival", arrival);		
 		PendingIntent alarm_1min = PendingIntent.getBroadcast(this, 0, i_1min, 0);
 
 		// Alarm after bus should have arrived, asking if the bus is on time
 		Intent i_follow = new Intent(AlarmReceiver.BUS_ARRIVAL_FOLLOWUP);
 		i_follow.setData(Uri.parse("reminder://uk.ac.cam.cl.dtg.android.time.BusStopApp/"+Long.toString(target_followup)));
 		i_follow.putExtra("stop", currStop);
 		i_follow.putExtra("arrival", arrival);		
 		PendingIntent alarm_follow = PendingIntent.getBroadcast(this, 0, i_follow, 0);
 
 		
 		/* NOW SET THE ALARMS */
 
 		// Get alarm manager + set an alarm
 		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 		if(currTime < target_long) alarmManager.set(AlarmManager.RTC_WAKEUP, target_long, alarm_long);
 		if(dueAlarm && currTime < target_1min) alarmManager.set(AlarmManager.RTC_WAKEUP, target_1min, alarm_1min);
 		if(followUpAlarm && currTime < target_followup) alarmManager.set(AlarmManager.RTC_WAKEUP, target_followup, alarm_follow);
 		
 		// Feedback to user
 		Toast.makeText(this, "Reminder set", Toast.LENGTH_SHORT).show();
 
 	}
 
 	/*
 	 * For dealing with all	the IPC!
 	 */
 	private Handler handler = new Handler() {
 
 		//@Override
 		public void handleMessage(Message msg) {
 
 
 			Log.i("Message handler","Got message: "+msg.arg1);
 
 			if(msg.arg1 == 0) {
 				setTitle((String)msg.obj);
 				setProgressBarIndeterminateVisibility(true);
 			}
 			if(msg.arg1 == 1) {
 				updateArrivals();
 			}
 			if(msg.arg1 == 2) {
 				setTitle((String)msg.obj);
 				setProgressBarIndeterminateVisibility(false);
 			}
 
 		}
 
 	};
 
 	/**
 	 * This method gets new arrival data, sleeps for 1 minute, then tries again.
 	 *
 	 * (non-Javadoc)
 	 * @see java.lang.Runnable#run()
 	 */
 	public void run() {
 		
 		TransportDataProvider tdp = new TransportDataProvider(DataStore.apiKey, DataStore.feedURL);
 
 		while(keepUpdating) {
 
 			// Switch on the progress wheel			
 			Message m = Message.obtain();
 			m.arg1 = 0;
 			m.obj = "Updating arrival data...";
 			handler.sendMessage(m);
 
 			// Get arrival data
 			if(currStop != null) {
 				try {
 					nextBuses = tdp.getBusArrivalData(currStop.getStopRef(), 30);
 
 					// Tell thread to update arrivals
 					m = Message.obtain();
 					m.arg1 = 1;
 					handler.sendMessage(m);
 
 				} catch (TransportDataException e1) {
 					Log.e("FetchingLMData",e1.getMessage());
 				}
 
 
 				m = Message.obtain();
 				m.arg1 = 2;
 				m.obj = "";
 				handler.sendMessage(m);
 
 
 				try {
 					Thread.sleep(30000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 
 			}
 		}
 	}
 
 
 }

 package edu.calpoly.ai.skynest;
 
 import android.app.TimePickerDialog;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 import android.widget.TimePicker;
 
 public class SetSchedule extends FragmentActivity implements TimePickerDialog.OnTimeSetListener, 
 OnClickListener {
 	private ScheduleManager schedMan;
 	private int[] times;
 	
 	private int timeBeingEdited;
 	private TextView viewBeingEdited;
 	/*
 *	 * Each TextView has its onClicked set to this, where an interface/method will handle click.
 *	 * Method will gather which TextView was clicked, store the TextView and an index into time array.
 *	 * Method will then start the TimePickerDialog.
 *	 * TimePickerDialog will set this activity as the callback.
 *	 * When TimePickerDialog finishes, it calls the method in this activity.
 *	 * That method checks the index set and modifies the stored time, then modifies the TextView.
 *	 * When the SetSched button is clicked, it sends the updated int array to ScheduleManager.
 	 */
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_display_schedule);
 		
 		// get the schedule manager, and the int array of times
 		schedMan = new ScheduleManager(this.getSharedPreferences(MainActivity.PREF_FILE, MODE_PRIVATE));
 		times = schedMan.getScheduleInts();
 		
 		initLayout();
 	}
 	
 	private void initLayout() {
 		// attach an onClick to each TextView that opens a time dialog
 		findViewById(R.id.sunday_departure_time).setOnClickListener(this);
 		findViewById(R.id.sunday_arrival_time).setOnClickListener(this);
 		findViewById(R.id.monday_departure_time).setOnClickListener(this);
 		findViewById(R.id.monday_arrival_time).setOnClickListener(this);
 		findViewById(R.id.tuesday_departure_time).setOnClickListener(this);
 		findViewById(R.id.tuesday_arrival_time).setOnClickListener(this);
 		findViewById(R.id.wednesday_departure_time).setOnClickListener(this);
 		findViewById(R.id.wednesday_arrival_time).setOnClickListener(this);
 		findViewById(R.id.thursday_departure_time).setOnClickListener(this);
 		findViewById(R.id.thursday_arrival_time).setOnClickListener(this);
 		findViewById(R.id.friday_departure_time).setOnClickListener(this);
 		findViewById(R.id.friday_arrival_time).setOnClickListener(this);
 		findViewById(R.id.saturday_departure_time).setOnClickListener(this);
 		findViewById(R.id.saturday_arrival_time).setOnClickListener(this);
 		
 		// attach onClick to set sched button to update schedule manager
 		findViewById(R.id.set_sched_button).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				for (int i = 0; i < times.length; i++) {
 					schedMan.setTimeSlot(i, times[i]);
 				}
 			}
 		});
 		
 		// attach onCLick to update sched button to update scheduler manager
 		findViewById(R.id.update_sched_button).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				for (int i = 0; i < times.length; i++) {
 					schedMan.updateTimeSlot(i,  times[i]);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		// ensure the instructions and submit button are visible
 		findViewById(R.id.set_sched_instructions).setVisibility(View.VISIBLE);
 		findViewById(R.id.set_sched_button).setVisibility(View.VISIBLE);
 		findViewById(R.id.update_sched_button).setVisibility(View.VISIBLE);
 		
 		// set all the times appropriately
 		DisplaySchedule.setTextViewTime((TextView) findViewById(R.id.sunday_departure_time), times[0]);
 		DisplaySchedule.setTextViewTime((TextView) findViewById(R.id.sunday_arrival_time), times[1]);
 		DisplaySchedule.setTextViewTime((TextView) findViewById(R.id.monday_departure_time), times[2]);
 		DisplaySchedule.setTextViewTime((TextView) findViewById(R.id.monday_arrival_time), times[3]);
 		DisplaySchedule.setTextViewTime((TextView) findViewById(R.id.tuesday_departure_time), times[4]);
 		DisplaySchedule.setTextViewTime((TextView) findViewById(R.id.tuesday_arrival_time), times[5]);
 		DisplaySchedule.setTextViewTime((TextView) findViewById(R.id.wednesday_departure_time), times[6]);
 		DisplaySchedule.setTextViewTime((TextView) findViewById(R.id.wednesday_arrival_time), times[7]);
 		DisplaySchedule.setTextViewTime((TextView) findViewById(R.id.thursday_departure_time), times[8]);
 		DisplaySchedule.setTextViewTime((TextView) findViewById(R.id.thursday_arrival_time), times[9]);
 		DisplaySchedule.setTextViewTime((TextView) findViewById(R.id.friday_departure_time), times[10]);
 		DisplaySchedule.setTextViewTime((TextView) findViewById(R.id.friday_arrival_time), times[11]);
 		DisplaySchedule.setTextViewTime((TextView) findViewById(R.id.saturday_departure_time), times[12]);
 		DisplaySchedule.setTextViewTime((TextView) findViewById(R.id.saturday_arrival_time), times[13]);
 		
 	}
 
 	/**
 	 * this is the callback for the TimePicker, when the user has selected the time
 	 */
 	@Override
 	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 		// update the times array and the textview cell in the table
 		times[timeBeingEdited] = hourOfDay * 60 + minute;
		viewBeingEdited.setText(String.format("%02d:%02d", hourOfDay, minute));
 	}
 
 	/**
 	 * This is called whenever a TextView cell is tapped. It stores which cell is tapped
 	 * and the associated index into the times array. It creates an args bundle with the
 	 * time current time to pass to display in the TimePicker. It makes and shows the
 	 * TimePicker.
 	 */
 	@Override
 	public void onClick(View v) {
 		viewBeingEdited = (TextView) v;
 		
 		// I wish there were a better way... :[
 		switch (v.getId()) {
 		case R.id.sunday_departure_time:
 			timeBeingEdited = 0;
 			break;
 		case R.id.sunday_arrival_time:
 			timeBeingEdited = 1;
 			break;
 		case R.id.monday_departure_time:
 			timeBeingEdited = 2;
 			break;
 		case R.id.monday_arrival_time:
 			timeBeingEdited = 3;
 			break;
 		case R.id.tuesday_departure_time:
 			timeBeingEdited = 4;
 			break;
 		case R.id.tuesday_arrival_time:
 			timeBeingEdited = 5;
 			break;
 		case R.id.wednesday_departure_time:
 			timeBeingEdited = 6;
 			break;
 		case R.id.wednesday_arrival_time:
 			timeBeingEdited = 7;
 			break;
 		case R.id.thursday_departure_time:
 			timeBeingEdited = 8;
 			break;
 		case R.id.thursday_arrival_time:
 			timeBeingEdited = 9;
 			break;
 		case R.id.friday_departure_time:
 			timeBeingEdited = 10;
 			break;
 		case R.id.friday_arrival_time:
 			timeBeingEdited = 11;
 			break;
 		case R.id.saturday_departure_time:
 			timeBeingEdited = 12;
 			break;
 		case R.id.saturday_arrival_time:
 			timeBeingEdited = 13;
 			break;
 		default:
 			break;
 		}
 		
 		// build arguments, passing in hour and minute ints
 		Bundle args = new Bundle();
 		args.putInt(TimePickerFragment.hourOfDayKey, times[timeBeingEdited]/60);
 		args.putInt(TimePickerFragment.minuteKey, times[timeBeingEdited]%60);
 		
 		TimePickerFragment timePicker = new TimePickerFragment();
 		timePicker.setArguments(args);
 		timePicker.show(getSupportFragmentManager(), "timePicker");
 	}
 	
 }

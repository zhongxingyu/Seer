 package edu.calpoly.ai.skynest;
 
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.TextView;
 
 public class DisplaySchedule extends Activity {
 	private SharedPreferences sp;
 	private ScheduleManager schedMan;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_display_schedule);
 		sp = this.getSharedPreferences(MainActivity.PREF_FILE, MODE_PRIVATE);
 		schedMan = new ScheduleManager(sp);
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		// remove the instructions textview and set sched button
 		findViewById(R.id.set_sched_instructions).setVisibility(View.GONE);
 		findViewById(R.id.set_sched_button).setVisibility(View.GONE);
 		findViewById(R.id.update_sched_button).setVisibility(View.GONE);
 		
 		// set all the times appropriately
 		setTextViewTime((TextView) findViewById(R.id.sunday_departure_time), schedMan.getTimeSlot(0));
 		setTextViewTime((TextView) findViewById(R.id.sunday_arrival_time), schedMan.getTimeSlot(1));
 		setTextViewTime((TextView) findViewById(R.id.monday_departure_time), schedMan.getTimeSlot(2));
 		setTextViewTime((TextView) findViewById(R.id.monday_arrival_time), schedMan.getTimeSlot(3));
 		setTextViewTime((TextView) findViewById(R.id.tuesday_departure_time), schedMan.getTimeSlot(4));
 		setTextViewTime((TextView) findViewById(R.id.tuesday_arrival_time), schedMan.getTimeSlot(5));
 		setTextViewTime((TextView) findViewById(R.id.wednesday_departure_time), schedMan.getTimeSlot(6));
 		setTextViewTime((TextView) findViewById(R.id.wednesday_arrival_time), schedMan.getTimeSlot(7));
 		setTextViewTime((TextView) findViewById(R.id.thursday_departure_time), schedMan.getTimeSlot(8));
 		setTextViewTime((TextView) findViewById(R.id.thursday_arrival_time), schedMan.getTimeSlot(9));
 		setTextViewTime((TextView) findViewById(R.id.friday_departure_time), schedMan.getTimeSlot(10));
 		setTextViewTime((TextView) findViewById(R.id.friday_arrival_time), schedMan.getTimeSlot(11));
 		setTextViewTime((TextView) findViewById(R.id.saturday_departure_time), schedMan.getTimeSlot(12));
 		setTextViewTime((TextView) findViewById(R.id.saturday_arrival_time), schedMan.getTimeSlot(13));
 	}
 	
 	static void setTextViewTime(TextView view, int time) {
 		int hour = time / 60;
 		int minutes = time % 60;
 		
 		// NOTE: currently in 24hr format.
		String timeString = String.format(Locale.ENGLISH ,"%2d:%2d", hour, minutes);
 		
 		view.setText(timeString);
 	}
 }

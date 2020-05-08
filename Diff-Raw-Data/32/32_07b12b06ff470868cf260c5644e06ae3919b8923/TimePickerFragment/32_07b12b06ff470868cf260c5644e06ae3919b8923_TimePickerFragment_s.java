 package edu.calpoly.ai.skynest;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 
 public class TimePickerFragment extends DialogFragment {
 	
 	private SetSchedule activity;
 	private int hourOfDay, minute;
 	public static final String hourOfDayKey = "HOUR_OF_DAY";
 	public static final String minuteKey = "MINUTE";
 	
 	/**
 	 * Empty on purpose, keep this here. Required by dialogs. 
 	 */
 	public TimePickerFragment() {
 	}
 	
 	@Override
 	/**
 	 * Called after onAttach, before the dialog is displayed.
 	 */
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		return new TimePickerDialog(activity, activity, hourOfDay, minute, true);
 	}
 	
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		
 		this.hourOfDay = getArguments().getInt(hourOfDayKey);
 		this.minute = getArguments().getInt(minuteKey);
 		
 		this.activity = (SetSchedule) activity;
 	}
 }

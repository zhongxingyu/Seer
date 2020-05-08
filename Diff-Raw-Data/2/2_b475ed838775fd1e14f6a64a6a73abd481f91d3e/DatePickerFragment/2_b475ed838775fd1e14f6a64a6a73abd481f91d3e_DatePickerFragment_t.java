 package com.example.criminalintent;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.view.View;
 import android.widget.DatePicker;
 import android.widget.DatePicker.OnDateChangedListener;
 
 public class DatePickerFragment extends DialogFragment 
 {
 	public static final String EXTRA_DATE = "com.example.criminalintent.date";
 	
 	private Date mDate;
 	
 	public static DatePickerFragment newInstance(Date date)
 	{
 		Bundle args = new Bundle();
 		args.putSerializable(EXTRA_DATE, date);
 
 		DatePickerFragment dpf = new DatePickerFragment();
 		dpf.setArguments(args);
 
 		return dpf;
 	}
 	
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstace)
 	{
 		// We use a layout here and not directly the DatePicker View
 		// because it makes modification easy if we want to change the 
 		// content of the Dialog later
 		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_date, null);
 		
 		mDate = (Date) getArguments().getSerializable(EXTRA_DATE);
 		
 		// Date is more a timestamp and cannot provide the integers required
 		// to initialize DatePicker ..we will use Calendar for this purpose
 		Calendar calendar = Calendar.getInstance();
 		calendar.setTime(mDate);
 		
 		int year = calendar.get(Calendar.YEAR);
 		int month = calendar.get(Calendar.MONTH);
 		int day = calendar.get(Calendar.DAY_OF_MONTH);
 		
 		DatePicker dp = (DatePicker) v.findViewById(R.id.dialog_date_datePicker);
 		dp.init(year, month, day, new OnDateChangedListener() {
 			@Override
 			public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
 				// Translate year, month, day into a Date object using a GregorianCalendar
 				mDate = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();
 				
 				// NOTE: Update argument to preserve selected value on rotation
 				// It is a simpler way to preserve the value than saving the state in onSavedInstanceState()
 				// DialogFragment has a bug that causes retained instances to misbehave
 				getArguments().putSerializable(EXTRA_DATE, mDate);
 			}
 		});
 		
 		return new AlertDialog.Builder(getActivity()).
 				setTitle(R.string.date_picker_title).
 				// setPositiveButton accepts a string Resource and a DialogInterface.OnClickListener
 				// not to be confused with View.OnClickListener
 				setPositiveButton(
 						android.R.string.ok, 
 						new OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						sendResult(Activity.RESULT_OK);
 					}
 				}).
 				setView(v).
 				create();
 	}
 	
 	private void sendResult(int resultCode)
 	{
 		if (getTargetFragment() == null)
 			return;
 
 		// NOTE: Intent is created only with EXTRA data
 		Intent i = new Intent();
 		i.putExtra(EXTRA_DATE , mDate);
 		
 		// Passing the result from a Fragment to another Fragment : 
 		// it may seem weird, but we will use Fragment.onActivityResult()
 		// to pass the result back to the caller fragment
 		// NOTE: call getTargetFragment and getTargetRequestCode
		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
 	}
 }

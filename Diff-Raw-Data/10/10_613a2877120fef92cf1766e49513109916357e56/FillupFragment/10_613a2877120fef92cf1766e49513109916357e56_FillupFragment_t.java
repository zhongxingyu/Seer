 /**
  * 
  */
 package com.kulkarni.gasmileagecalculator;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import com.kulkarni.gasmileagecalculator.data.Fillup;
 import com.kulkarni.gasmileagecalculator.data.FillupVector;
 import com.kulkarni.gasmileagecalculator.helpers.TextValidator;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Adapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * @author Amey Kulkarni
  *
  */
 public class FillupFragment extends Fragment implements
 	View.OnClickListener
 {
 	
 	@SuppressLint("ValidFragment")
 	public class DatePickerFragment extends DialogFragment implements
 			DatePickerDialog.OnDateSetListener {
 		
 		@Override
 		public Dialog onCreateDialog (Bundle savedInstanceState) {
 			Long curDate = getArguments().getLong("setDate");
 			
 			final Calendar c = Calendar.getInstance();
 			c.setTimeInMillis(curDate);
 			int year  = c.get(Calendar.YEAR);
 			int month = c.get(Calendar.MONTH);
 			int day   = c.get(Calendar.DAY_OF_MONTH);
 			
 			return new DatePickerDialog(getActivity(), this, year, month, day);
 		}
 
 		@Override
 		public void onDateSet(DatePicker view, int year, int month, int day) {
 			// TODO Auto-generated method stub
 			Calendar c = Calendar.getInstance();
 			c.set(year, month, day);
 			
 			if (isDateInFuture(c)) {
 				Toast.makeText(getActivity(), "Date cannot be in the future", Toast.LENGTH_SHORT).show();
 				return;
 			}
 			
 			mDate = new Date (c.getTimeInMillis());
 			
 			TextView dateTextView = (TextView) getActivity().findViewById(R.id.textView_fillup_date);
 			dateTextView.setText(getDateString());
 		}
 		
 		private boolean isDateInFuture (Calendar c) {
 			Calendar today = Calendar.getInstance();
 			
 			return today.before(c);
 		}
 	}
 
 	public Date mDate;
 	public boolean notToppedUp = false;
 	boolean errorRate = false;
 	boolean errorVol = false;
 	boolean errorOdo = false;
 	
 	private TextView dateTextView;
 	private TextView vehicleName;
 	private Button   enterFillup;
 	private EditText fuelRate;
 	private EditText fuelVolume;
 	private EditText odometer;
 	private CheckBox checkTopFillup;
 
 	/**
 	 * 
 	 */
 	public FillupFragment() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	@Override
 	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fillup_fragment, container, false);
 		
 		Calendar c = Calendar.getInstance();
 		mDate = new Date (c.getTimeInMillis());
 		
 		dateTextView = (TextView) rootView.findViewById(R.id.textView_fillup_date);
 		dateTextView.setText(getDateString ());
 		dateTextView.setOnClickListener(this);
 		
 		vehicleName = (TextView) rootView.findViewById(R.id.textVehicleName);
 		vehicleName.setOnClickListener(this);
 		
 		enterFillup = (Button) rootView.findViewById(R.id.button_enter_fillup);
 		enterFillup.setOnClickListener(this);
 		
 		fuelRate = (EditText) rootView.findViewById(R.id.edit_fuel_rate);
 		fuelRate.addTextChangedListener(new TextValidator(fuelRate) {
 			
 			@Override
 			public void validate(EditText edit, String text) {
 				// TODO Auto-generated method stub
				if (!TextUtils.isEmpty(text) && Double.valueOf(text) <= 0.0) {
 					edit.setError("Fuel Price must be greater than zero");
 					errorRate = true;
 				}
 				else
 					errorRate = false;
 			}
 		});
 		
 		fuelVolume = (EditText) rootView.findViewById(R.id.edit_fuel_volume);
 		fuelVolume.addTextChangedListener(new TextValidator(fuelVolume) {
 			
 			@Override
 			public void validate(EditText edit, String text) {
 				// TODO Auto-generated method stub
 				errorVol = true;
 				
				if (!TextUtils.isEmpty(text) && Double.valueOf(text) <= 0.0)
 					edit.setError("Fuel Volume must be greater than zero");
 				else
 					errorVol = false;
 			}
 		});
 		
 		odometer = (EditText) rootView.findViewById(R.id.edit_odometer);
 		odometer.addTextChangedListener(new TextValidator(odometer) {
 		
 			@Override
 			public void validate(EditText edit, String text) {
 				errorOdo = true;
 				
				if (!TextUtils.isEmpty(text) && Double.valueOf(text) <= 0.0)
 					edit.setError("Odometer must be greater than zero");
 				else
 					errorOdo = false;
 			}
 		});
 		
 		checkTopFillup = (CheckBox) rootView.findViewById(R.id.checkBox_fillup_topped);
 		checkTopFillup.setChecked(notToppedUp);
 		
 		return rootView;
 	}
 
 	
 	private CharSequence getDateString () {
 		SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateInstance();
 		return sdf.format(mDate);
 	}
 	
 	public void onDateClicked (View v) {
 		Bundle currentDate = new Bundle ();
 		currentDate.putLong("setDate", mDate.getTime());
 		
 		DialogFragment newFragment = new DatePickerFragment ();
 		newFragment.setArguments(currentDate);
 		
 		newFragment.show(getFragmentManager(), "datePicker");
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		switch (v.getId()) {
 		
 		case R.id.textView_fillup_date:
 			onDateClicked(v);
 			break;
 			
 		case R.id.checkBox_fillup_topped:
 			onCheckedTop (v);
 			break;
 		
 		case R.id.textVehicleName:
 			break;
 			
 		case R.id.button_enter_fillup:
 			onEnterFillupClicked (v);
 			break;
 		}
 	}
 
 	private void onEnterFillupClicked(View v) {
 		// TODO Auto-generated method stub
 		fuelRate = (EditText) getActivity().findViewById(R.id.edit_fuel_rate);
 		fuelVolume  = (EditText) getActivity().findViewById(R.id.edit_fuel_volume);
 		odometer  = (EditText) getActivity().findViewById(R.id.edit_odometer);
 		
 		String text;
 		
 		text = fuelRate.getText().toString();
 		if (text.equals(""))	{
 			fuelRate.setError(getResources().getString(R.string.fuel_rate_error));
 			errorRate = true;
 		}
 		
 		text = fuelVolume.getText().toString();
 		if (text.equals("")) {
 			fuelVolume.setError(getResources().getString(R.string.fuel_vol_error));
 			errorVol = true;
 		}
 		
 		text = odometer.getText().toString();
 		if (text.equals("")) {
 			odometer.setError(getResources().getString(R.string.odometer_error));
 			errorOdo = true;
 		}
 		
 		if (errorOdo || errorRate || errorVol) {
 			Toast.makeText(getActivity(), "Make sure the entered values are valid", Toast.LENGTH_LONG).show();
 			return;
 		}
 		
 		double fuelRate   = Double.parseDouble(this.fuelRate.getText().toString());
 		double fuelVolume = Double.parseDouble(this.fuelVolume.getText().toString());
 		double odometer   = Double.parseDouble(this.odometer.getText().toString());
 		
 		Fillup fillup = new Fillup(mDate, fuelRate, fuelVolume, odometer, !notToppedUp);
 		
 		if (FillupVector.fillups.add(fillup)) {
 			Toast.makeText(getActivity(), "Fillup added", Toast.LENGTH_SHORT).show();
 			
 			ListView historylist = (ListView) getActivity().findViewById(R.id.history_list);
 			((HistoryAdapter) historylist.getAdapter()).notifyDataSetChanged ();
 			
 			clearForm ();
 		}
 		else {
 			Toast.makeText(getActivity(), "Error adding fillup", Toast.LENGTH_LONG).show();
 			return;
 		}
 	}
 
 	private void clearForm() {
 		// TODO Auto-generated method stub
 		fuelRate.getText().clear();
 		fuelVolume.getText().clear();
 		odometer.getText().clear();
 		
 		mDate = null;
 		Calendar c = Calendar.getInstance();
 		mDate = new Date (c.getTimeInMillis());
 		dateTextView.setText(getDateString ());
 		
 		notToppedUp = false;
 		checkTopFillup.setChecked(notToppedUp);
 	}
 
 	private void onCheckedTop(View v) {
 		// TODO Auto-generated method stub
 		checkTopFillup = (CheckBox) v;
 		
 		notToppedUp = !notToppedUp;
 		checkTopFillup.setChecked(notToppedUp);
 	}
 	
 }

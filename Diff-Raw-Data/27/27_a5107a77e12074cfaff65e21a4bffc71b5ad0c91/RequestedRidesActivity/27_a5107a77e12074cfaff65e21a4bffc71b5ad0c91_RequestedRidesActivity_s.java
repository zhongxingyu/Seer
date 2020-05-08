 package com.example.getconnected.activities;
 
 import com.example.getconnected.R;
 
 import android.app.DialogFragment;
 import android.os.Bundle;
 import android.view.View;
 
 import com.util.getconnected.ActivityHelper.DatePickerFragment;
 
 public class RequestedRidesActivity extends BaseActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_requestedrides);

 	}
 
 	public void showDatePickerDialog(View v) {
 		DialogFragment newFragment = new DatePickerFragment();
 		newFragment.show(getFragmentManager(), "datePicker");
 	}
 
 }

  package com.techtalk4geeks.blogspot;
 
 import java.sql.Date;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.TextView;
 import android.widget.Spinner;
 import android.widget.DatePicker;
 
 public class MainActivity extends Activity
 {
 DatePicker myDatePicker;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.setup);
//		myDatePicker = ((DatePicker)findViewById(R.id.typeSelecter));
 		Button doneButton = (Button) this.findViewById(R.id.done_button);
 		doneButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				String name = ((TextView)findViewById(R.id.nameField)).getText().toString();
 				String rank = ((Spinner)findViewById(R.id.typeSelecter)).getSelectedItem().toString();
 				int age = Integer.parseInt(((TextView)findViewById(R.id.ageField)).getText().toString());
 				User user = new User(name, rank, age);
 		setContentView(R.layout.activity_main);
             }
         });
 		
 		
 		
 //		User alex = new User("Alex Baratti", Rank.FROG, 1999);
 //		TextView nameView = (TextView) this.findViewById(R.id.nameValue);
 //		nameView.setText(alex.getName());
 //		TextView levelView = (TextView) this.findViewById(R.id.levelValue);
 //		levelView.setText(String.valueOf(alex.getLevel()));
 //		TextView hpView = (TextView) this.findViewById(R.id.hpValue);
 //		hpView.setText(String.valueOf(alex.getHP()));
 //		TextView spView = (TextView) this.findViewById(R.id.spValue);
 //		spView.setText(String.valueOf(alex.getSP()));
 //		TextView powView = (TextView) this.findViewById(R.id.powValue);
 //		powView.setText(String.valueOf(alex.getPOW()));
 //		TextView defView = (TextView) this.findViewById(R.id.defValue);
 //		defView.setText(String.valueOf(alex.getDEF()));
 //		TextView speedView = (TextView) this.findViewById(R.id.speedValue);
 //		speedView.setText(String.valueOf(alex.getSPEED()));
 		
 //		alex.setName(view.getText().toString());
 	}
 
 //	@Override
 //	public boolean onCreateOptionsMenu(Menu menu)
 //	{
 //		// Inflate the menu; this adds items to the action bar if it is present.
 //		getMenuInflater().inflate(R.menu.main, menu);
 //		return true;
 //	}
 //	
 //	private DatePickerDialog.OnDateSetListener datePickerListener 
 //    = new DatePickerDialog.OnDateSetListener() {
 //
 //// when dialog box is closed, below method will be called.
 //public void onDateSet(DatePicker view, int selectedYear,
 //	int selectedMonth, int selectedDay) {
 //int year = selectedYear;
 //int month = selectedMonth;
 //int day = selectedDay;
 //
 //// set selected date into textview
 //tvDisplayDate.setText(new StringBuilder().append(month + 1)
 //   .append("-").append(day).append("-").append(year)
 //   .append(" "));
 //
 //// set selected date into datepicker also
 //dpResult.init(year, month, day, null);
 //
 //}
 //};
 
 
 }

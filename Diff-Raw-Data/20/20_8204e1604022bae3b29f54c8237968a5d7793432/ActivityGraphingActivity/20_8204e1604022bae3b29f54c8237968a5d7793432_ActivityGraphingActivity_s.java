 package com.example.project3;
 
 
 import java.util.Calendar;
 
 import org.achartengine.GraphicalView;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.DatePicker;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.DatePicker.OnDateChangedListener;
 import android.widget.Toast;
 
 public class ActivityGraphingActivity extends Activity {
 	String activity = "";
 	private static final String TAG = "MenuActivity";
 	private DatePicker graphStartDate;
 	private DatePicker graphEndDate;
 	
 	private int startDay;
 	private int startMonth;
 	private int startYear;
 	
 	private int endDay;
 	private int endMonth;
 	private int endYear;
 	
 	private TextView activityDisplay;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activitygraphing);
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 		     activity = extras.getString("ActivityType");
 		}
 		Toast.makeText(getApplicationContext(), activity, Toast.LENGTH_LONG).show();
 		Log.d(TAG, activity);
 		
 		activityDisplay =(TextView) findViewById(R.id.activityDisplay);
 		activityDisplay.setText(activity);
 	
 	
 		BarGraph bar = new BarGraph();
 
 		GraphicalView gview = bar.getView(this);
 
 		LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
 
 		layout.addView(gview);
 	
 	
 	graphEndDate = (DatePicker) findViewById(R.id.graphDateEnd);
 	
 	
 	final Calendar c = Calendar.getInstance();
 	endYear = c.get(Calendar.YEAR);
 	endMonth = c.get(Calendar.MONTH);
 	endDay = c.get(Calendar.DAY_OF_MONTH);
 	
 	graphEndDate.init(endYear, endMonth, endDay, new OnDateChangedListener(){
 
 		@Override
 		public void onDateChanged(DatePicker arg0, int arg1, int arg2, int arg3) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 	});
 	
 	graphEndDate.setCalendarViewShown(false);
 	
 	graphStartDate = (DatePicker) findViewById(R.id.graphDateStart);
 	
 	
 	final Calendar c2 = Calendar.getInstance();
 
 	c2.add(c2.DAY_OF_MONTH, -7);
 
 	startYear = c2.get(Calendar.YEAR);
 	startMonth = c2.get(Calendar.MONTH);
 	startDay = c2.get(Calendar.DAY_OF_MONTH);
 	
 	graphEndDate.init(startYear, startMonth, startDay, new OnDateChangedListener(){
  
 		@Override
 		public void onDateChanged(DatePicker arg0, int arg1, int arg2, int arg3) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 	});
 	
 	graphStartDate.setCalendarViewShown(false);
 	
 	}
 	
 	public void onStudentsBtnClick(View arg0){
 		Intent intent = new Intent(this, StudentsInActivityActivity.class);
		intent.putExtra("Activity", activity);
 		startActivity(intent);
 	}
 	
 
 }

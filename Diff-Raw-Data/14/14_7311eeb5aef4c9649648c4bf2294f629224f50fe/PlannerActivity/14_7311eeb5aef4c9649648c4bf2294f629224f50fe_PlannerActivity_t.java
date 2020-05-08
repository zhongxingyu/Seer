 package pt.up.fe.cmov;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import pt.up.fe.cmov.entities.Appointment;
 import pt.up.fe.cmov.entities.Schedule;
 import pt.up.fe.cmov.entities.SchedulePlan;
 import pt.up.fe.cmov.operations.DoctorOperations;
 import pt.up.fe.cmov.operations.ScheduleOperations;
 import pt.up.fe.cmov.operations.SchedulePlanOperations;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PlannerActivity extends Activity {
 	
 	public static final String PLANNER_DOCTOR_ID = "plannerDoctorId";
 	private final int searchBtnId = Menu.FIRST;
 	
 	private OnClickListener newRowListener =    
 		new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 	        addPlannerRow();        
 		}
 
     };
     
     private OnClickListener eraseRowListener =    
 		new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 	        View row = (View) v.getParent(); 
 	        LinearLayout parent = (LinearLayout)row.getParent();
 	        if (parent.getChildCount() > 1)
 	        	parent.removeView(row);
 		}
 
     };
     
     private OnClickListener callDatePickerListener =    
 		new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			final Calendar c = Calendar.getInstance();
 	        mYear = c.get(Calendar.YEAR);
 	        mMonth = c.get(Calendar.MONTH);
 	        mDay = c.get(Calendar.DAY_OF_MONTH);
 	        
 	        showDialog(DATE_DIALOG_ID);
 		}
 
     };
     
     private OnClickListener cancelListener =    
 		new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			PlannerActivity.this.finish();
 		}
     };
     
     private OnClickListener confirmListener =    
 		new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			if (mYear == 0)	{
 				Toast.makeText(PlannerActivity.this, "Please add a Start Date", Toast.LENGTH_LONG).show();
 				return;
 			}
 			Date date = new Date(mYear-1900, mMonth, mDay);
 			
 			Log.i("DATE", "" + date.toString());
 			
 			int WEEKDAY_POS = 0;
 			int START_HOUR_POS = 1;
 			int END_HOUR_POS = 3;
 
 			TableLayout plannerRows = (TableLayout) findViewById(R.id.plannerRows);
 			
 			SchedulePlan plan = new SchedulePlan(-1, mDoctorId, date);
 			
 			ArrayList<Schedule> schedules = new ArrayList<Schedule>();
 
 			for (int i = 0; i < plannerRows.getChildCount(); i++) {
 				TableRow row = (TableRow) plannerRows.getChildAt(i);
 				
 				Spinner weekdaySpinner = (Spinner)row.getChildAt(WEEKDAY_POS);
 				int weekday = (int) weekdaySpinner.getSelectedItemId();
 				Log.i("Planner", "Week: " + weekday);
 				
 				Spinner startHourSpinner = (Spinner)row.getChildAt(START_HOUR_POS);
 				int startHour = (int) startHourSpinner.getSelectedItemId();
 				Log.i("Planner", "Start Hour: " + startHour);
 				
 				Spinner endHourSpinner = (Spinner)row.getChildAt(END_HOUR_POS);
 				int endHour = (int) endHourSpinner.getSelectedItemId();
 				Log.i("Planner", "End Hour: " + endHour);
 				
 				if (startHour == endHour) {
 					Toast.makeText(PlannerActivity.this, "Start and End hour cannot be the same", Toast.LENGTH_LONG).show();
 					return;
 				}
 				
 				Date startDate = calculateDate(date, weekday, startHour);
 				
 				Log.i("Planner", "Start date: " + startDate.toString());
 				if (startHour > endHour)
 					endHour += 48;	//next day
 
 				Date endDate = calculateDate(date, weekday, endHour);
 
 				Log.i("Planner", "End date: " + endDate.toString());
 				
 				Schedule schedule = new Schedule(-1, startDate, endDate);
 				schedules.add(schedule);
 			}
 			
 			int planId = SchedulePlanOperations.createSchedulePlan(PlannerActivity.this, plan, true);
 			
 			if (planId <= 0) {
 				Toast.makeText(PlannerActivity.this, "Schedule Creation failed", Toast.LENGTH_LONG).show();
 				return;
 			}
 			
 			for (int i=0; i < schedules.size(); i++) {
 				Schedule schedule = schedules.get(i);
 				schedule.setSchedulePlanId(planId);
 				ScheduleOperations.createSchedule(PlannerActivity.this, schedule, true);
 			}
 			
 			Toast.makeText(PlannerActivity.this, "Schedule creation starting at " + date.toString() + " succeeded!", Toast.LENGTH_LONG).show();
 			PlannerActivity.this.finish();
 
 		}
 
     };
     
 
     public static final int DATE_DIALOG_ID = 0;
 
     private int mDoctorId = 0;
     
     private int mYear = 0, mMonth = 0, mDay = 0;
     
 	private DatePickerDialog.OnDateSetListener mDateSetListener =
 	      new DatePickerDialog.OnDateSetListener() {
 
       public void onDateSet(DatePicker view, int year, 
                             int monthOfYear, int dayOfMonth) {
     	  
     	  Appointment furthest = DoctorOperations.getFurthestAppointment(PlannerActivity.this, mDoctorId);
     	  Date furthest_date = null;
     	  Date this_date = null;
     	  
     	  if (furthest != null)
     	  {
 	    	  furthest_date = furthest.getDate();
 	    	  this_date = new Date(year-1900, monthOfYear, dayOfMonth);
     	  }
     	  if (furthest == null || this_date.getTime() > furthest_date.getTime()) {
 	          mYear = year;
 	          mMonth = monthOfYear;
 	          mDay = dayOfMonth;
 	          
 	          Button b = (Button) findViewById(R.id.datePickerButton);
 	          String text = "" + mDay + "/" + (mMonth+1) + "/" + mYear;
 	          b.setText(text);
     	  }
     	  else {
     		  Toast.makeText(PlannerActivity.this, "Your start date must be after the date of your furthest appointment: " + furthest_date.toString(), Toast.LENGTH_LONG).show();
     	  }
       }
 
   };
     
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.planner);
         
         mDoctorId = getIntent().getIntExtra(PLANNER_DOCTOR_ID, 0);
         
         if (mDoctorId == 0)
         {
         	Toast.makeText(this, "No doctor associated with this planner", Toast.LENGTH_LONG).show();
         }
         
         Button b = (Button) findViewById(R.id.datePickerButton);
         b.setOnClickListener(callDatePickerListener);
         
         Button confirm = (Button) findViewById(R.id.confirmButton);
         confirm.setOnClickListener(confirmListener);
         
         Button cancel = (Button) findViewById(R.id.cancelButton);
         cancel.setOnClickListener(cancelListener);
         
         addPlannerRow();           
 	}
 	
 	@Override
 	  public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuItem searchMItm = menu.add(Menu.NONE,searchBtnId ,searchBtnId,"Logout");
 	    searchMItm.setIcon(R.drawable.logout);
 	    return super.onCreateOptionsMenu(menu);
 	  }
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	        case searchBtnId:
 	        	Intent intent = new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        	LoginActivity.loginDoctor = null;
 	        	finish();
 	        	startActivity(intent);
 	        break;
 	    }
 	    return true;
 	}
 	
 	public void addPlannerRow() {
 		TableRow plannerRow = new TableRow(this);
 		
 		plannerRow.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 		//plannerRow.setPadding(0, 10, 0, 10);
 		
 		Spinner weekdaySpinner = new Spinner(this);
 		weekdaySpinner.setLayoutParams(new TableRow.LayoutParams(0));
 		weekdaySpinner.setPromptId(R.string.weekdays_prompt);
 	    weekdaySpinner.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.btn_default));
 
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 	            this, R.array.weekdays_array, android.R.layout.simple_spinner_item);
 	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    weekdaySpinner.setAdapter(adapter);
 	    
 	    Spinner startHourSpinner = new Spinner(this);
 	    startHourSpinner.setLayoutParams(new TableRow.LayoutParams(1));
 	    startHourSpinner.setPromptId(R.string.hours_prompt);
 	    startHourSpinner.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.btn_default));
 
 		ArrayAdapter<CharSequence> hourAdapter = ArrayAdapter.createFromResource(
 	            this, R.array.hours_array, android.R.layout.simple_spinner_item);
 	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    
 	    startHourSpinner.setAdapter(hourAdapter);
 	    
 	    TextView toLabel = new TextView(this);
 	    toLabel.setLayoutParams(new TableRow.LayoutParams(2));
 	    toLabel.setText(R.string.plannerToLabel);
 	    toLabel.setGravity(Gravity.CENTER_VERTICAL);
 	    toLabel.setPadding(10, 0, 10, 0);
 	    toLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
 	    
 	    Spinner endHourSpinner = new Spinner(this);
 	    endHourSpinner.setLayoutParams(new TableRow.LayoutParams(3));
 	    endHourSpinner.setPromptId(R.string.hours_prompt);
 	    endHourSpinner.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.btn_default));
 	    
 	    endHourSpinner.setAdapter(hourAdapter);
 	    
 	    Button addRowButton = new Button(this);
 	    addRowButton.setLayoutParams(new TableRow.LayoutParams(4));
 	    addRowButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.addbutton));
 	    addRowButton.setOnClickListener(newRowListener);
 	    
 	    Button eraseRowButton = new Button(this);
 	    eraseRowButton.setLayoutParams(new TableRow.LayoutParams(5));
 	    eraseRowButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.deletebutton));
 	    eraseRowButton.setOnClickListener(eraseRowListener);
 
 	    plannerRow.addView(weekdaySpinner);
 	    plannerRow.addView(startHourSpinner);
 	    plannerRow.addView(toLabel);
 	    plannerRow.addView(endHourSpinner);
 	    plannerRow.addView(addRowButton); 
 	    plannerRow.addView(eraseRowButton);
 	    
         TableLayout plannerRows = (TableLayout) findViewById(R.id.plannerRows);
         plannerRows.addView(plannerRow);
 	}
 	
 	@Override
 	protected Dialog onCreateDialog(int id) {
 	    switch (id) {
 	    case DATE_DIALOG_ID:
 	        return new DatePickerDialog(this,
 	                    mDateSetListener,
 	                    mYear, mMonth, mDay);
 	    }
 	    return null;
 	}
 	
 	public static Date calculateDate(Date startDate, int weekday, int hour) {
 		Calendar c1 = Calendar.getInstance();
 		c1.setTime(startDate);
 		int startWeekday = c1.get(Calendar.DAY_OF_WEEK);
 		weekday = (weekday == 6) ? 0 : weekday+1;	//adjust indexes  
 		
 		int addWeek = weekday - startWeekday + 1;
 		if (addWeek < 0)
 			addWeek += 7;
 		
 		c1.add(Calendar.DAY_OF_WEEK, addWeek);
 		c1.add(Calendar.MINUTE, hour*30);
 		return c1.getTime();
 	}
 	
 }

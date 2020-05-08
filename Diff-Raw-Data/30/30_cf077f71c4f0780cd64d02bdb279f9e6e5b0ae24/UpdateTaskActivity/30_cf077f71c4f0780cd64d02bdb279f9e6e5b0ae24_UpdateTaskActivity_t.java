 package com.CMPUT301F12T07.crowdsource.viewupdatetask;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.CMPUT301F12T07.crowdsource.R;
 import com.CMPUT301F12T07.crowdsource.taskmodeldb.DBHandler;
 import com.CMPUT301F12T07.crowdsource.taskmodeldb.Task;
 
 /**
  * @author  jsmereka
  */
 public class UpdateTaskActivity extends Activity {
 
 	/**
 	 * @uml.property  name="currentTask"
 	 * @uml.associationEnd  
 	 */
 	private Task currentTask;
 	private EditText taskTitle;
 	private TextView startDate;
 	private TextView endDate;
 	private EditText taskQuantity;
 	private EditText taskDesc;
 	/**
 	 * @uml.property  name="db"
 	 * @uml.associationEnd  
 	 */
 	private DBHandler db;
 
 	private Button selectDate;
 
 	private int cYear;
 	private int cMonth;
 	private int cDay;
 
 	private int year;
 	private int month;
 	private int day;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_update_task);
 		// getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		db = new DBHandler(this);
 		Long taskID = getIntent().getExtras().getLong("taskID");
 		this.currentTask = db.getTask(taskID.toString(), DBHandler.LOCAL_FLAG);
 
 		populateViews();
 
 		launchListeners();
 
 		saveButtonListener();
 
 		initializeDates();
 	}
 
 	/**
 	 * 
 	 */
 	public void populateViews()
 	{
 
 		// Getting the task title field
 		this.taskTitle = (EditText) findViewById(R.id.textEditTitle);
 		taskTitle.setText(currentTask.get_title());
 
 		// Getting the start Date field
 		this.startDate = (TextView) findViewById(R.id.textEditCreatedDate);
 		startDate.setText(currentTask.get_dateCreate());
 
 		// Getting the end Date field
 		this.endDate = (TextView) findViewById(R.id.textEditDueDate);
 		endDate.setText(currentTask.get_dateDue());
 
 		// Getting the task quantity field
 		this.taskQuantity = (EditText) findViewById(R.id.textEditQuantity);
 		taskQuantity.setText(Integer.toString(currentTask.get_quantity()));
 
 		// Getting the task description field
 		this.taskDesc = (EditText) findViewById(R.id.textEditDescription);
 		taskDesc.setText(currentTask.get_description());
 	}
 
 	/**
 	 * 
 	 */
 	public void launchListeners()
 	{
 
 		final Button Cancel = (Button) findViewById(R.id.buttonCancel);
 		Cancel.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(v.getContext(), ViewTaskActivity.class);
 				intent.putExtra("taskID", currentTask.get_tid());
 				startActivity(intent);
 				finish();
 			}
 		});
 		
 		final CheckBox Public = (CheckBox) findViewById(R.id.checkboxPublic);
 		if (currentTask.get_visibility() == 1)
 			Public.setChecked(true);
 		
 		Public.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (Public.isChecked())
 					currentTask.set_visibility(1);
 				else
 					currentTask.set_visibility(0);
 			}
 		});
 	}
 
 	/**
 	 * 
 	 */
 	public void saveButtonListener()
 	{
 
 		Button Save = (Button) findViewById(R.id.buttonSave);
 		Save.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (checkDate() == false) {
 					Toast.makeText(v.getContext(),
 							"Minimum date is " + startDate.getText().toString(), Toast.LENGTH_SHORT)
 							.show();
 				} else {
					CheckBox Public = (CheckBox) findViewById(R.id.checkboxPublic);
					if (Public.isChecked())
						currentTask.set_visibility(1);
					else 
						currentTask.set_visibility(0);
 					currentTask.set_title(taskTitle.getText().toString());
 					currentTask.set_dateCreate(startDate.getText().toString(), Task.TASK_LOCAL);
 					currentTask.set_dateDue(endDate.getText().toString(), Task.TASK_LOCAL);
 					currentTask.set_quantity(Integer.parseInt(taskQuantity
 							.getText().toString()));
 					currentTask.set_description(taskDesc.getText().toString());
 
 					try {
 						db.updateTask(currentTask);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					
 					Intent intent = new Intent(v.getContext(), ViewTaskActivity.class);
 					intent.putExtra("taskID", currentTask.get_tid());
 					startActivity(intent);
 					finish();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Initializes the date fields, and sets up the DatePicker dialog.
 	 */
 	private void initializeDates() {
 		selectDate = (Button) findViewById(R.id.selectDate);
 		java.util.Date d1 = dateParser();
 		final Calendar cal = Calendar.getInstance();
 
 		year = cal.get(Calendar.YEAR);
 		month = cal.get(Calendar.MONTH);
 		day = cal.get(Calendar.DAY_OF_MONTH);
 
 		cYear = year;
 		cMonth = month;
 		cDay = day;
 		
 		cal.setTime(d1);
 		year = cal.get(Calendar.YEAR);
 		month = cal.get(Calendar.MONTH);
 		day = cal.get(Calendar.DAY_OF_MONTH);
 
 		selectDate.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				DatePickerDialog c = new DatePickerDialog(v.getContext(),
 						mDateSetListener, year, month, day);
 				c.show();
 			}
 
 		});
 	}
 
 	private java.util.Date dateParser()
 	{
 
 		endDate = (TextView) findViewById(R.id.textEditDueDate);
 		String stringToParse = endDate.getText().toString();
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 		java.util.Date d1 = null;
 		try
 		{
 			d1 = format.parse(stringToParse);
 		} catch (java.text.ParseException e)
 		{
 			e.printStackTrace();
 		}
 		return d1;
 	}
 
 	/**
 	 * This is the pop-up for when the button Select Date. It takes the data
 	 * from the user's input and copies it into year, month, and day.
 	 */
 	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
 		public void onDateSet(DatePicker v, int inyear, int inmonth, int inday) {
 			year = inyear;
 			month = inmonth;
 			day = inday;
 
 			String dateDue = year + "-" + (month + 1) + "-" + day;
 			endDate.setText(dateDue);
 
 			Toast.makeText(v.getContext(),
 					"Date set to: " + endDate.getText().toString(),
 					Toast.LENGTH_SHORT).show();
 		}
 	};
 
 	/**
 	 * Checks for a valid input date.
 	 * 
 	 * @return Returns true if date is the current date or after. Returns false
 	 *         otherwise.
 	 */
 	private boolean checkDate() {
 		if (year < cYear)
 			return false;
 		if (year == cYear && month < cMonth)
 			return false;
 		if (year == cYear && cMonth == month && day < cDay)
 			return false;
 
 		return true;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_update_task, menu);
 		return true;
 	}
 
 }

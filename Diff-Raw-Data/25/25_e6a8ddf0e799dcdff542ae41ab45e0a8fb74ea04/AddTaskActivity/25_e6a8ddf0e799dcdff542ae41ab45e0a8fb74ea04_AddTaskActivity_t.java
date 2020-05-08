 package com.CMPUT301F12T07.crowdsource;
 
 import java.util.Calendar;
 
 import android.os.Bundle;
 import android.provider.Settings.Secure;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class AddTaskActivity extends Activity {
 
 	private String deviceId;
 	
 	private Button selectDate;
 	private TextView selectedDate;
 	private String dateCreate;
 	private String dateDue;
 	private int year;
 	private int month;
 	private int day;
 	
 	private EditText titleText;
 	private EditText descriptionText;
 	private EditText quantityText;
 	private String title;
 	private String description;
 	private int quantity;
 	
 	private Spinner typeSpinner;
 	private String type;
 	
 	private CheckBox privacyCheckBox;
 	private int visibility;
 	
 	private Button save;
 	
     // intializes listeners and gets android id on startup
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_add_task);
         
         deviceId = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
         initializeListeners();
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_add_task, menu);
         return true;
     }
     
     private void initializeListeners() {
     	initializeDates();
     	initializeTextFields();
     	initializeSpinners();
     	initializeCheckBox();
     	initializeSave();
     }
 
 	private void initializeDates() {
     	selectDate = (Button) findViewById(R.id.selectDateButton);
     	selectedDate = (TextView) findViewById(R.id.dateTextView);
     	final Calendar cal = Calendar.getInstance();
     	
     	year = cal.get(Calendar.YEAR);
     	month = cal.get(Calendar.MONTH);
     	day = cal.get(Calendar.DAY_OF_MONTH);
     	
     	selectedDate.setText(year + "-" + (month+1) + "-" + day);
     	dateCreate = year + "-" + (month+1) + "-" + day;
     	
     	selectDate.setOnClickListener(new OnClickListener() {
     		
 			@Override
 			public void onClick(View v) {
 				DatePickerDialog c = new DatePickerDialog(v.getContext(), mDateSetListener, year, month,  day);
 				c.show();
 			}
     		
     	});
     }
     
     private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
 	    public void onDateSet(DatePicker v, int inyear, int inmonth, int inday) {
 	    	year = inyear;
 	        month = inmonth;
 	        day = inday;
 	        
 	        selectedDate.setText(year + "-" + (month+1) + "-" + day);
 	        dateDue = year + "-" + (month+1) + "-" + day;
 	        
 	        Toast.makeText(v.getContext(), "Date set to: " + year + "-" + (month+1) + "-" + day, Toast.LENGTH_SHORT).show();
 	    }
     };
     
     
     private void initializeTextFields() {
     	titleText = (EditText) findViewById(R.id.titleText);
     	descriptionText = (EditText) findViewById(R.id.descriptionText);
     	quantityText = (EditText) findViewById(R.id.quantityText);
     	
     	title = "";
     	description = "";
     	quantity = 0;
 		
     	titleText.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void afterTextChanged(Editable s) {
 				title = s.toString();
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence arg0, int arg1,
 					int arg2, int arg3) {}
 
 			@Override
 			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
 					int arg3) {}
     	});
     	
     	descriptionText.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void afterTextChanged(Editable s) {
 				description = s.toString();
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence arg0, int arg1,
 					int arg2, int arg3) {}
 
 			@Override
 			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
 					int arg3) {}
     	});
     	
     	quantityText.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void afterTextChanged(Editable s) {
				if (s.toString().compareTo("") == 0)
					quantity = 0;
				else
 					quantity = Integer.parseInt(s.toString());
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence arg0, int arg1,
 					int arg2, int arg3) {}
 
 			@Override
 			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
 					int arg3) {}
     	});
 	}
     
     // initializes dropdown type boxes
     private void initializeSpinners() {
     	typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
     	
     	typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view,
 					int pos, long id) {
 				type = parent.getItemAtPosition(pos).toString();
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> parent) {
 				type = parent.getItemAtPosition(0).toString();
 			}
     		
     	});
     }
     
     private void initializeCheckBox() {
     	privacyCheckBox = (CheckBox) findViewById(R.id.privacyCheckbox);
     	
     	privacyCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(CompoundButton button, boolean checked) {
 				if (checked) visibility = 1;
 				else visibility = 0;
 			}
     		
     	});
     }
     
     private void initializeSave() {
     	save = (Button) findViewById(R.id.saveButton);
     	
     	save.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
				// can only save if quantity > 0, title and description is not blank
				if (title.compareTo("") != 0 && description.compareTo("") != 0 && quantity > 0) {
					LocalDB db = new LocalDB(v.getContext());
					Task newTask = new Task(deviceId, title, description, dateCreate, dateDue, type, visibility, quantity);
					
					db.createTask(newTask);
					finish();
				} else if (quantity == 0) {
					Toast.makeText(v.getContext(), "Quantity has to be at least one.", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(v.getContext(), "Missing fields.", Toast.LENGTH_SHORT).show();
				}
 			}
     		
     	});
     }
 
 }

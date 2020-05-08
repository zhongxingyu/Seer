 package com.CMPUT301F12T07.crowdsource;
 
 import java.util.Calendar;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class AddTaskActivity extends Activity {
 
 	private Button selectDate;
 	private TextView selectedDate;
 	private int year;
 	private int month;
 	private int day;
 	
 	private EditText titleText;
 	private EditText descriptionText;
 	private EditText quantityText;
 	private String title;
 	private String description;
 	private int quantity;
 	
 	private CheckBox privacyCheckBox;
 	private int visibility;
 	
 	private Button save;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_add_task);
         
         initializeListeners();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_add_task, menu);
         return true;
     }
     
     private void initializeListeners() {
     	initializeDates();
     	initializeTextFields();
     	initializeCheckBox();
     	initializeSave();
 //    	initializeQuantity();
     }
 
 	private void initializeDates() {
     	selectDate = (Button) findViewById(R.id.selectDateButton);
     	selectedDate = (TextView) findViewById(R.id.dateTextView);
     	final Calendar cal = Calendar.getInstance();
     	
     	year = cal.get(Calendar.YEAR);
     	month = cal.get(Calendar.MONTH);
     	day = cal.get(Calendar.DAY_OF_MONTH);
     	
     	selectedDate.setText((month+1) + "/" + day + "/" + year);
     	
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
 	        
 	        selectedDate.setText((month+1) + "/" + day + "/" + year);
 	        
 	        Toast.makeText(v.getContext(), "Date set to: " + (month+1) + "/" + day + "/" + year, Toast.LENGTH_SHORT).show();
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
				//Task newTask = Task();
 				
 			}
     		
     	});
     }

 }

 package com.schedulerapp.activities;
  
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.schedulerapp.adapters.CampusAdapter;
 import com.schedulerapp.adapters.DepartmentAdapter;
 import com.schedulerapp.adapters.TimeSlotAdapter;
 import com.schedulerapp.models.Campus;
 import com.schedulerapp.models.Department;
 import com.schedulerapp.models.DepartmentTimeslotLinkage;
 import com.schedulerapp.tasks.GetCampusesByClient;
 import com.schedulerapp.tasks.GetDepartementByCampus;
 import com.schedulerapp.tasks.GetDepartmentTimeSlot;
 import com.schedulerapp.tasks.SaveAppointment;
  
 public class BookAppointment extends Activity {
 	
 	TextView txtData;
 	EditText txtPurpose;
 	List<Campus> campuses;
 	List<Department> departments;
 	List<DepartmentTimeslotLinkage> timeslots;
 	Spinner campusSpinner;
 	Spinner deptSpinner;
 	Spinner timeSlotSpinner;
 	Button btnPickDate;
 	String current = "";
 	
 	Campus selectedCampus;
 	Department selectedDepartment;
 	DepartmentTimeslotLinkage selectedDepartmentTimeslotLinkage;
  
     private int mYear;
     private int mMonth;
     private int mDay;
     static final int DATE_DIALOG_ID = 0;
  
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_book_appointment);
 		campusSpinner = (Spinner) findViewById(R.id.campus_spinner);
 		deptSpinner = (Spinner) findViewById(R.id.department_spinner);
 		timeSlotSpinner = (Spinner) findViewById(R.id.timeslot_spinner);
 		btnPickDate = (Button) findViewById(R.id.button2);
 		txtPurpose = (EditText) findViewById(R.id.editText1);
 		txtData = (TextView) findViewById(R.id.txtDate);
 		campusSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view,
 					int pos, long id) {
 				selectedCampus = (Campus) parent.getItemAtPosition(pos);
 				if(selectedCampus != null && selectedCampus.getCampusId() != -1) {
 					getDepartments();
 				}				
 			}
 			@Override
 			public void onNothingSelected(AdapterView<?> parent) {
 			}
 		});
  
 		deptSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view,
 					int pos, long id) {
 				selectedDepartment = (Department) parent.getItemAtPosition(pos);
 				if(selectedDepartment != null && selectedDepartment.getDepartmentId() != -1) {
 					getDepartmentTimeSlots();
 				}				
 			}
 			@Override
 			public void onNothingSelected(AdapterView<?> parent) {
 			}
 		});
  
 		timeSlotSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view,
 					int pos, long id) {
 				selectedDepartmentTimeslotLinkage = (DepartmentTimeslotLinkage) parent.getItemAtPosition(pos);
 			}
 			@Override
 			public void onNothingSelected(AdapterView<?> parent) {
 			}
 		});
 		btnPickDate.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 showDialog(DATE_DIALOG_ID);
             }
         });
  
         // get the current date
         final Calendar c = Calendar.getInstance();
         mYear = c.get(Calendar.YEAR);
         mMonth = c.get(Calendar.MONTH);
         mDay = c.get(Calendar.DAY_OF_MONTH);
  
         current = (new StringBuilder()
         .append(mYear).append("-")
         .append(mMonth + 1).append("-")
         .append(mDay).append("")).toString();
         // display the current date (this method is below)
         updateDisplay();
         
 		getCampuses();
 	}
  
 	private void updateDisplay() {
 		txtData.setText((
             new StringBuilder()
                     .append(mYear).append("-")
                     .append(mMonth + 1).append("-")
                     .append(mDay).append("")).toString());
     }
  
 	private DatePickerDialog.OnDateSetListener mDateSetListener =
             new DatePickerDialog.OnDateSetListener() {
  
                 public void onDateSet(DatePicker view, int year,
                                       int monthOfYear, int dayOfMonth) {
                     mYear = year;
                     mMonth = monthOfYear;
                     mDay = dayOfMonth;
                     updateDisplay();
                 }
             };
  
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DATE_DIALOG_ID:
 			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
 					mDay);
 		}
 		return null;
 	}
 	
 	public void getCampuses() {
 		Long clientId = 1L;
 		GetCampusesByClient task = new GetCampusesByClient();
 		try {
 			campuses = task.execute(clientId.toString()).get();
 			campusSpinner.setAdapter(new CampusAdapter(this, campuses));			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
  
 	public void getDepartments() {
 		GetDepartementByCampus task = new GetDepartementByCampus();
 		try {			
 			departments = task.execute(Integer.toString(selectedCampus.getCampusId())).get();
 			deptSpinner.setAdapter(new DepartmentAdapter(this, departments));			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
  
 	public void getDepartmentTimeSlots() {
 		GetDepartmentTimeSlot task = new GetDepartmentTimeSlot();
 		try {
 			String params = Integer.toString(selectedDepartment.getDepartmentId()) + "/" + txtData.getText();
 			timeslots = task.execute(params).get();
 			timeSlotSpinner.setAdapter(new TimeSlotAdapter(this, timeslots));			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void saveAppointmentClick(View v) throws InterruptedException, ExecutionException, ParseException {
 		
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
     	Date currentDate = sdf.parse(current);
     	Date selected = sdf.parse(txtData.getText().toString());
     	if(selected.compareTo(currentDate) < 0){
 			Toast.makeText(getApplicationContext(), "Selected date must be greater than or equal to curent date", Toast.LENGTH_LONG).show();
 			return;    		
     	}
     	
 		if(selectedCampus == null || selectedCampus.getCampusId() == -1) {
 			Toast.makeText(getApplicationContext(), "Please select Campus", Toast.LENGTH_LONG).show();
 			return;
 		}
 		if(selectedDepartment == null || selectedDepartment.getDepartmentId() == -1) {
 			Toast.makeText(getApplicationContext(), "Please select Department", Toast.LENGTH_LONG).show();
 			return;
 		}
 		if(selectedDepartmentTimeslotLinkage == null || selectedDepartmentTimeslotLinkage.getDepartmentTimeId() == -1) {
 			Toast.makeText(getApplicationContext(), "Please select Timeslot", Toast.LENGTH_LONG).show();
 			return;
 		}		
 		if(txtPurpose.getText().toString().trim().equals("")) {
 			Toast.makeText(getApplicationContext(), "Please enter purpose", Toast.LENGTH_LONG).show();
 			return;
 		}		
 		final JSONObject jsonObjectUser = new JSONObject();
 		try {			
             jsonObjectUser.put("departmentTimeId", selectedDepartmentTimeslotLinkage.getDepartmentTimeId());
             jsonObjectUser.put("purposeOfVisit", txtPurpose.getText().toString());
             jsonObjectUser.put("userId", "1");
             jsonObjectUser.put("meetingFinished", "N");
            jsonObjectUser.put("appointmentDate", txtData.getText().toString());
 	    } catch (JSONException e) {
 	            e.printStackTrace();
 	    }	
 			SaveAppointment task = new SaveAppointment();
 			String result = task.execute(jsonObjectUser).get();
 			
 			if(result.equals("success")) {
 				Toast.makeText(getApplicationContext(), "Record Updated", Toast.LENGTH_SHORT).show();
 			} else if(result.equals("fail")) {
 				Toast.makeText(getApplicationContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
 			}
 	}	
}

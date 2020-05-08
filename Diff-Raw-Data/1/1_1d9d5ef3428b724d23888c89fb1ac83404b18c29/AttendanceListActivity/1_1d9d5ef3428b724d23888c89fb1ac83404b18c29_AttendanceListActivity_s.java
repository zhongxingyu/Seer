 package com.photon.connecttodoor;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Calendar;
 
 import org.json.JSONException;
 
 import com.photon.datamodel.AttendanceModel;
 import com.photon.datamodel.ReportAttendanceModel;
 import com.photon.uiadapter.ListGeneratedAttendanceListArrayAdapter;
 import com.photon.uiadapter.ListGeneratedReportArrayAdapter;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 
 public class AttendanceListActivity extends Activity {
 	private EditText startFromDateTxt;
 	private EditText untilFromDateTxt;
 	private ImageView startFromDateImage;
 	private ImageView untilFromDateImage;
 	private Button searchButton;
 	private EditText inputCategory;
 	private Button backButton;
 	private Button signOutButton;
 	private int pYear;
 	private int pMonth;
 	private int pDay;
 	private boolean isSetStartDateText = true;
 	private ListView attendanceListReport;
 	
 
 	private String category[] = {"Date","Name","Project ID","Employee ID"};
 
 	private String[] month = {"January","February","March","April","May","June","July","August","September","October","November","December"};
 
 	/** This integer will uniquely define the dialog to be used for displaying date picker.*/
 	static final int DATE_DIALOG_ID = 0;
 
 	Spinner spinnerCategory;
 
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_attendancelist);
 
 		startFromDateTxt = (EditText)findViewById(R.id.startFromDateTxt);
 		untilFromDateTxt = (EditText)findViewById(R.id.untilFromDateTxt);
 		startFromDateImage = (ImageView)findViewById(R.id.startFromDateImage);
 		untilFromDateImage = (ImageView)findViewById(R.id.untilFromDateImage);
 		inputCategory = (EditText)findViewById(R.id.inputCategory);
 		attendanceListReport = (ListView)findViewById(R.id.table_report_attendancelist);
 		searchButton = (Button)findViewById(R.id.searchButton);
 		spinnerCategory = (Spinner)findViewById(R.id.spinnerCategory);
 		backButton = (Button)findViewById(R.id.back_buttonattlist);
 		signOutButton = (Button)findViewById(R.id.signout_buttonattlist);
 		
 		String response = LoadAccount(R.raw.daily);
 		AttendanceModel attendance = new AttendanceModel(response);
 		
 		try {
 			attendance.parseSource();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		ListGeneratedAttendanceListArrayAdapter tableReport = new ListGeneratedAttendanceListArrayAdapter(this,attendance.getAttendanceListModels());
 		attendanceListReport.setAdapter(tableReport);
 		
 		backButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				goToWelcomeScreen();
 				
 			}
 		});
 		
 		signOutButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				goToLogin();
 				
 			}
 		});
 		
 
 		ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item,category);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinnerCategory.setAdapter(adapter);
 
 
 		this.getCurrentDate();
 		this.actionButton();
 
 		spinnerCategory.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
 				String selectCategory = spinnerCategory.getSelectedItem().toString();
 
 				try {
 
 					if(selectCategory.equals("Date")){
 						inputCategory.setVisibility(View.GONE);	
 						((EditText) findViewById(R.id.inputCategory)).setText("");
 					}else if(selectCategory.equals("Name")){
 						inputCategory.setVisibility(View.VISIBLE);
 						((EditText) findViewById(R.id.inputCategory)).setText("");
 					}else if(selectCategory.equals("Project ID")){
 						inputCategory.setVisibility(View.VISIBLE);
 						((EditText) findViewById(R.id.inputCategory)).setText("");
 					}else if(selectCategory.equals("Employee ID")){
 						inputCategory.setVisibility(View.VISIBLE);
 						((EditText) findViewById(R.id.inputCategory)).setText("");
 					}
 				}catch(NumberFormatException nfe) {
 					System.out.println("Could not parse " + nfe);
 				} 
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				//total.setText("Anda Belum Memilih");   
 			}
 		});
 		
 	}
 	
 	protected String LoadAccount(int resourceId) {
         // The InputStream opens the resourceId and sends it to the buffer
         InputStream is = this.getResources().openRawResource(resourceId);
         BufferedReader br = new BufferedReader(new InputStreamReader(is));
         String line;
         StringBuilder text = new StringBuilder();
         
         try {
             //While the BufferedReader readLine is not null
             while ((line = br.readLine()) != null) {
                Log.i("JSON", line);
                 text.append(line);
                 text.append('\n');
             }    
             //Close the InputStream and BufferedReader
             is.close();
             br.close();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         return text.toString();
     }
 
 	//go to welcome screen
 	private void goToWelcomeScreen(){
 		Intent intentWelcomeScreen = new Intent(AttendanceListActivity.this, WelcomeScreenActivity.class);
 		startActivity(intentWelcomeScreen);
 	}
 	//go to login
 		private void goToLogin(){
 			Intent intentLoginPage = new Intent(AttendanceListActivity.this, LoginActivity.class);
 			startActivity(intentLoginPage);
 		}
 	
 
 	/** 
 	 * Get the current date or reset date to current date after used 
 	 * 
 	 */
 	private void getCurrentDate(){
 		final Calendar cal = Calendar.getInstance();
 		pYear = cal.get(Calendar.YEAR);
 		pMonth = cal.get(Calendar.MONTH);
 		pDay = cal.get(Calendar.DAY_OF_MONTH);
 	}
 
 	private void actionButton(){
 		startFromDateImage.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				isSetStartDateText = true;
 				getCurrentDate();
 				showDialog(DATE_DIALOG_ID);
 
 			}
 		});
 		untilFromDateImage.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				isSetStartDateText = false;
 				getCurrentDate();
 				showDialog(DATE_DIALOG_ID);
 
 			}
 		});
 	}
 	/** Callback received when the user "picks" a date in the dialog */
 	private DatePickerDialog.OnDateSetListener pDateSetListener =
 			new DatePickerDialog.OnDateSetListener() {
 
 		@Override
 		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
 			pYear = year;
 			pMonth = monthOfYear;
 			pDay = dayOfMonth;
 
 			updateDisplay();
 		}
 	};
 
 	/** Updates the date in the TextView */
 	private void updateDisplay() {
 
 		if(isSetStartDateText){
 			startFromDateTxt.setText(getDateEditText());
 		}else{
 			untilFromDateTxt.setText(getDateEditText());
 		}
 	}
 	/**
 	 * Create Date for startDate editText and untilDate editText 
 	 * @return getDateEditText as StringBuilder
 	 */
 	private StringBuilder getDateEditText(){
 		// Month is 0 based so add 1
 		String pMonthString = String.valueOf((pMonth+1)).trim().toString();
 		String pDayString = String.valueOf(pDay).trim().toString();
 		String pYearString = String.valueOf(pYear).trim().toString();
 
 		if(pMonthString.length() < 2){
 			pMonthString = "0"+pMonthString;
 		}
 
 		if(pDayString.length() < 2){
 			pDayString = "0"+pDayString;
 		}
 
 		return new StringBuilder()
 		.append(pDayString).append(" / ")
 		.append(pMonthString).append(" / ")
 		.append(pYearString).append(" ");
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DATE_DIALOG_ID:
 			return new DatePickerDialog(this,
 					pDateSetListener,
 					pYear, pMonth, pDay);
 		}
 
 		return null;
 	}
 
 }

 package com.photon.connecttodoor.activity;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.json.JSONException;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Spinner;
 
 import com.photon.connecttodoor.R;
 import com.photon.connecttodoor.controller.AttendanceListService;
 import com.photon.connecttodoor.datamodel.AttendanceModel;
 import com.photon.connecttodoor.uiadapter.ListGeneratedAttendanceListArrayAdapter;
 import com.photon.connecttodoor.utils.ApplicationConstant;
 import com.photon.connecttodoor.utils.Utility;
 
 
 public class AttendanceListActivity extends Activity {
 	private EditText startFromDateTxt;
 	private EditText untilFromDateTxt;
 	private ImageView startFromDateImage;
 	private ImageView untilFromDateImage;
 	private Button searchButton;
 	private EditText inputCategory;
 	private Button backButton;
 	private Button signOutButton;
 	private LinearLayout headerList;
 	private int pYear;
 	private int pMonth;
 	private int pDay;
 	private boolean isSetStartDateText = true;
 	private ListView attendanceListReport;
 	String responseAttendanceList;
 	String selectCategory;
 	String searchParameters;
 	String searchValues;
 	private String category[] = {ApplicationConstant.CAT_DATE,ApplicationConstant.CAT_NAME,ApplicationConstant.CAT_PROJECT_ID,ApplicationConstant.CAT_EMPLOYEE_ID};
 
 	private static final int DATE_DIALOG_ID = 0;
 	private static final String USERNAME = "username";
 	private static final String PROJECT_ID = "projectID";
 	private static final String EMPLOYEE_ID = "employeeID";
 	private static final String STRIP = "-";
 	private static final String SLASH = "/";
 	private static final String NUMBER_DATE = "0";
 
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
 		headerList = (LinearLayout)findViewById(R.id.headerList);
 
 		backButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				goToWelcomeScreen();
 			}
 		});
 
 		signOutButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				LoginActivity.onClickLogout();
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
 				selectCategory = spinnerCategory.getSelectedItem().toString();
 				try {
 					if(selectCategory.equals(ApplicationConstant.CAT_NAME)){
 						setValeuForSelectedName();
 					}else if(selectCategory.equals(ApplicationConstant.CAT_PROJECT_ID)){
 						setValeuForSelectedProjectId();
					}else if(selectCategory.equals(ApplicationConstant.EMPLOYEE_ID)){
 						setValeuForSelectedEmployeeId();
 					}else{
 						setValeuForSelectedDate();
 					}
 				}catch(NumberFormatException nfe) {
 					System.out.println("Could not parse " + nfe);
 				} 
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 			}
 		});
 
 	}
 
 	private void setValeuForSelectedDate(){
 		inputCategory.setVisibility(View.GONE);	
 		inputCategory.setText("");
 		searchParameters = selectCategory;
 		inputCategory.getText().toString();
 	}
 	private void setValeuForSelectedName(){
 		inputCategory.setVisibility(View.VISIBLE);
 		inputCategory.setText("");
 		searchParameters = USERNAME;
 		inputCategory.getText().toString();
 	}
 	private void setValeuForSelectedProjectId(){
 		inputCategory.setVisibility(View.VISIBLE);
 		inputCategory.setText("");
 		searchParameters = PROJECT_ID;
 		inputCategory.getText().toString();
 	}
 	private void setValeuForSelectedEmployeeId(){
 		inputCategory.setVisibility(View.VISIBLE);
 		inputCategory.setText("");
 		searchParameters = EMPLOYEE_ID;
 		inputCategory.getText().toString();
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
 		searchButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				int empty = 0;
 				int inputCategoryLength = inputCategory.getText().length();
 				if(inputCategory.isShown()){
 					if(inputCategoryLength == empty && startFromDateTxt.getText().length() == empty && untilFromDateTxt.getText().length() == empty){
 						Utility.alertMessage(ApplicationConstant.ERR_FILL_THE_BLANK,AttendanceListActivity.this);
 
 					}
 					else if(inputCategoryLength == empty  && ((startFromDateTxt.getText().length() == empty && untilFromDateTxt.getText().length() != empty )
 							|| (startFromDateTxt.getText().length() != empty && untilFromDateTxt.getText().length() == empty ))){
 						Utility.alertMessage(ApplicationConstant.ERR_FILL_THE_BLANK,AttendanceListActivity.this);
 					}
 					else if(startFromDateTxt.getText().length() == empty && ((inputCategoryLength == empty && untilFromDateTxt.getText().length() != empty)
 							|| (inputCategoryLength != empty && untilFromDateTxt.getText().length() == empty))){
 						Utility.alertMessage(ApplicationConstant.ERR_FILL_START_DATE,AttendanceListActivity.this);
 					}
 					else if(untilFromDateTxt.getText().length() == empty && ((inputCategoryLength == empty && startFromDateTxt.getText().length() != empty)
 							|| (inputCategoryLength != empty && startFromDateTxt.getText().length() == empty))){
 						Utility.alertMessage(ApplicationConstant.ERR_FILL_UNTIL_DATE,AttendanceListActivity.this);
 					}
 					else if(inputCategoryLength == empty  && startFromDateTxt.getText().length() != empty  && untilFromDateTxt.getText().length() != empty){
 						Utility.alertMessage(ApplicationConstant.ERR_INPUT_BLANK,AttendanceListActivity.this);
 					}
 					else if(inputCategoryLength != empty && startFromDateTxt.getText().length() == empty && untilFromDateTxt.getText().length() != empty){
 						Utility.alertMessage(ApplicationConstant.ERR_FILL_START_DATE,AttendanceListActivity.this);
 					}
 					else if(inputCategoryLength != empty && startFromDateTxt.getText().length() != empty && untilFromDateTxt.getText().length() == empty ){
 						Utility.alertMessage(ApplicationConstant.ERR_FILL_UNTIL_DATE,AttendanceListActivity.this);
 					}
 					else{
 
 						if(inputCategoryLength != empty && startFromDateTxt.getText().length() != empty && untilFromDateTxt.getText().length() != empty){
 
 							boolean isDateValidated = validateDateSection();
 
 							if(isDateValidated){
 								new CallServiceAttendanceListTask().execute();
 								headerList.setVisibility(View.VISIBLE);
 							}else{
 								Utility.alertMessage(ApplicationConstant.ERR_LAST_DATE_LESS_THAN_START_DATE,AttendanceListActivity.this);
 							}
 
 						}
 						else if(inputCategoryLength!= empty){
 
 							Utility.alertMessage(ApplicationConstant.ERR_FILL_THE_BLANK,AttendanceListActivity.this);
 
 						}
 						else if(startFromDateTxt.getText().length() != empty && untilFromDateTxt.getText().length() != empty){
 
 							boolean isDateValidated = validateDateSection();
 
 							if(isDateValidated){
 								new CallServiceAttendanceListTask().execute();
 								headerList.setVisibility(View.VISIBLE);
 							}else{
 								Utility.alertMessage(ApplicationConstant.ERR_LAST_DATE_LESS_THAN_START_DATE,AttendanceListActivity.this);
 							}
 
 						}
 						else{
 							Utility.alertMessage(ApplicationConstant.ERROR,AttendanceListActivity.this);
 						}
 					}
 
 				}else{
 					if(startFromDateTxt.getText().length() == empty || untilFromDateTxt.getText().length() == empty ){
 						Utility.alertMessage(ApplicationConstant.ERR_FILL_DATE,AttendanceListActivity.this);
 					}else{
 
 						if(startFromDateTxt.getText().length() != empty && untilFromDateTxt.getText().length() != empty){
 
 							boolean isDateValidated = validateDateSection();
 
 							if(isDateValidated){
 								new CallServiceAttendanceListTask().execute();
 								headerList.setVisibility(View.VISIBLE);
 							}else{
 								Utility.alertMessage(ApplicationConstant.ERR_LAST_DATE_LESS_THAN_START_DATE,AttendanceListActivity.this);
 							}
 
 						}
 						else{
 							Utility.alertMessage(ApplicationConstant.ERROR,AttendanceListActivity.this);
 						}
 
 					}
 				}
 
 			}
 		});
 	}
 
 	/**
 	 * Validate if startDate is less than endDate in Date section
 	 * @return isValidated as boolean
 	 */
 	@SuppressLint("SimpleDateFormat")
 	private boolean validateDateSection(){
 		String startDateString = startFromDateTxt.getText().toString().replace(STRIP, SLASH);
 		String endDateString = untilFromDateTxt.getText().toString().replace(STRIP, SLASH);;
 		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ApplicationConstant.DATE_PATTERN);
 
 		boolean isValidated = false;
 
 		try {
 			Date startDate = simpleDateFormat.parse(startDateString);
 			Date endDate = simpleDateFormat.parse(endDateString);
 			if(startDate.getTime() < endDate.getTime()){
 				isValidated = true;
 			}
 
 			return isValidated;
 
 		} catch (ParseException e) {
 			throw new RuntimeException(e);
 		}
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
 			pMonthString = NUMBER_DATE+pMonthString;
 		}
 
 		if(pDayString.length() < 2){
 			pDayString = NUMBER_DATE+pDayString;
 		}
 
 		return new StringBuilder()
 		.append(pDayString).append(SLASH)
 		.append(pMonthString).append(SLASH)
 		.append(pYearString).append("");
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
 
 	private class CallServiceAttendanceListTask extends AsyncTask<Void, Void, String> {
 
 		private ProgressDialog dialog;
 
 		protected void onPreExecute() {
 			this.dialog = ProgressDialog.show(AttendanceListActivity.this,
 					"List Process", "Please Wait...", true);
 		}
 
 		@Override
 		protected String doInBackground(Void... params) {
 			// TODO Auto-generated method stub
 			String searchingValue = inputCategory.getText().toString();;
 			String searchParameter = searchParameters;
 			String startDateParam = untilFromDateTxt.getText().toString();
 			String endDateParam = startFromDateTxt.getText().toString();
 			AttendanceListService attendanceListService = new AttendanceListService();
 			String response = attendanceListService.handleRequestAttendanceList(searchParameter, searchingValue,startDateParam,endDateParam);
 			return response;
 		}
 
 		protected void onPostExecute(String result) {
 			AttendanceModel attendance = new AttendanceModel(result);
 			try {
 				attendance.parseSource();
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			ListGeneratedAttendanceListArrayAdapter tableReport = new ListGeneratedAttendanceListArrayAdapter(AttendanceListActivity.this, attendance.getAttendanceListModels());
 			attendanceListReport.setAdapter(tableReport);
 			tableReport.notifyDataSetChanged();
 			this.dialog.dismiss();
 		}
 	}
 
 }

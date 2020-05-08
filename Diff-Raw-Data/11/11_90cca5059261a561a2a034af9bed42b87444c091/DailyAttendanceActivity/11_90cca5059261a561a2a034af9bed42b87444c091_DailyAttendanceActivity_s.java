 package com.photon.connecttodoor.activity;
 
 import java.util.Calendar;
 
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 
 import com.photon.connecttodoor.R;
 import com.photon.connecttodoor.controller.DailyAttendanceService;
 import com.photon.connecttodoor.datamodel.DailyAttendanceModel;
 import com.photon.connecttodoor.uiadapter.ListGeneratedDailyArrayAdapter;
 import com.photon.connecttodoor.utils.Utility;
 
 public class DailyAttendanceActivity extends Activity{
 
 	private ImageButton backButton;
 	private ImageButton signOutButton;
 	private EditText startFromDateTxt;
 	private ImageView startFromDateImage;
 	private ListView dailyReport;
 	private int pYear;
 	private int pMonth;
 	private int pDay;
 
 	static final int DATE_DIALOG_ID = 0;
 	private static final String STRIP = "-";
 	private static final String SLASH = "/";
 	private static final String NUMBER_DATE = "0";
 	private static final String EMPLOYEE_ID = "employeeId";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.daily_attandance);
 		backButton = (ImageButton) findViewById(R.id.backButton);
 		signOutButton = (ImageButton) findViewById(R.id.signOutButton);
 		startFromDateTxt = (EditText)findViewById(R.id.startFromDateTxt);
 		startFromDateImage = (ImageView)findViewById(R.id.startFromDateImage);
 		dailyReport = (ListView) findViewById(R.id.table_report);
		startFromDateTxt.setText(getDateEditText());
 		actionButton();
 		getCurrentDate();
 		new CallServiceAttendanceListTask().execute(getDateEditText().toString());
 		backButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				goToWelcomePage();
 			}
 		});
 
 		signOutButton.setOnClickListener(new OnClickListener() {	
 			@Override
 			public void onClick(View v) {
 				LoginActivity.onClickLogout();
 				goToLoginPage();
 			}
 		});
 	}
 
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
 			startFromDateTxt.setText(getDateEditText());
 			new CallServiceAttendanceListTask().execute(getDateEditText().toString());
 		}
 	};
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
 		.append(pDayString).append(STRIP)
 		.append(pMonthString).append(STRIP)
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
 
 	private String changeFormatDate(String date){
 		String [] formatDate = date.split(STRIP);
 		String day = formatDate[0];
 		String month = formatDate[1];
 		String year = formatDate[2];
 		return year+STRIP+month+STRIP+day;
 	}
 
 	private class CallServiceAttendanceListTask extends AsyncTask<String, Void, String> {
 
 		private ProgressDialog dialog;
 
 		protected void onPreExecute() {
 			this.dialog = ProgressDialog.show(DailyAttendanceActivity.this,
 					"List Process", "Please Wait...", true);
 		}
 
 		@Override
 		protected String doInBackground(String... params) {
 			// TODO Auto-generated method stub
 			String searchValues = params[0];
 			String employeeId = Utility.loadStringPreferences(EMPLOYEE_ID, getApplicationContext());
 			String startDateParam = changeFormatDate(searchValues);
 			DailyAttendanceService dailyAttendanceService = new DailyAttendanceService();
 			String response = dailyAttendanceService.handleRequestDailyAttendance(employeeId,startDateParam);
 			return response;
 		}
 
 		protected void onPostExecute(String result) {
 			DailyAttendanceModel dailyModel = new DailyAttendanceModel(result);
 			try {
 				dailyModel.parseSource();
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			ListGeneratedDailyArrayAdapter tableReport = new ListGeneratedDailyArrayAdapter(DailyAttendanceActivity.this,dailyModel.getDailyAttendanceListModels());
 			dailyReport.setAdapter(tableReport);
 			tableReport.notifyDataSetChanged();
 			this.dialog.dismiss();
 		}
 	}
 
 	private void goToWelcomePage(){
 		Intent intentWelcomeScreen = new Intent(DailyAttendanceActivity.this, WelcomeScreenActivity.class);
 		startActivity(intentWelcomeScreen);
 	}
 
 	private void goToLoginPage(){
 		Intent intentLoginPage = new Intent(DailyAttendanceActivity.this, LoginActivity.class);
 		startActivity(intentLoginPage);
 	}
 }

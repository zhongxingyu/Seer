 package com.photon.connecttodoor.activity;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.json.JSONException;
 
 import android.annotation.SuppressLint;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 import com.photon.connecttodoor.R;
 import com.photon.connecttodoor.controller.CheckInOutService;
 import com.photon.connecttodoor.datamodel.CheckPresentModel;
 import com.photon.connecttodoor.datamodel.LoginDataModel;
 import com.photon.connecttodoor.utils.ApplicationConstant;
 
 @SuppressLint("SimpleDateFormat")
 public class WelcomeScreenActivity extends MainActivity {
 
 	private ImageButton profilButton;
 	private ImageButton voucherButton;
 	private ImageButton dailyButton;
 	private ImageButton attendanceListButton;
 	private ImageButton attendanceReportButton;
 	private ImageButton attendanceFormButton;
 	private ImageButton signOutButton;
 	private TextView checkInText;
 	private TextView checkOutText;
 	private TextView currentTime;
 	private TextView username;
 	LoginDataModel loginDataModel;
 	private static final String ADMIN = "Admin";
 	private static final String EMPLOYEE_ID = "employeeId";
 	private static final String CHECK_STATUS = "check-status";
 	CheckPresentModel checkPresentModel;
 	String status;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.welcome_screen);
 		attendanceListButton = (ImageButton)findViewById(R.id.attandanceListButton);
 		dailyButton = (ImageButton)findViewById(R.id.dailyAttandanceButton);
 		profilButton = (ImageButton) findViewById(R.id.profileButton);
 		voucherButton = (ImageButton) findViewById(R.id.voucherButton);
 		attendanceReportButton = (ImageButton) findViewById(R.id.attandanceReportButton);
 		attendanceFormButton = (ImageButton) findViewById(R.id.attandanceFormButton);
 		signOutButton = (ImageButton)findViewById(R.id.signOutButton);
 		checkInText = (TextView) findViewById(R.id.checkInText);
 		checkOutText = (TextView) findViewById(R.id.checkOutText);
 		currentTime = (TextView) findViewById(R.id.currentTime);
 		username = (TextView) findViewById(R.id.username);
 		status = CHECK_STATUS;
 
 		checkPresentModel = new CheckPresentModel();
 
 		/**check internet connection before check attendance status */
		if(connectionAvailable()){
			setDataLogin();
			setUIWelcomeScreen();
			new CallServiceCheckInOut().execute();
		}else{
			alertMessage(ApplicationConstant.NO_INTERNET_CONNECTION, WelcomeScreenActivity.this);
		}

 		actionButton();
 	}
 	/**
 	 * registered action all button
 	 */
 	private void actionButton(){
 		/**
 		 * on click to daily attendance page
 		 */
 		dailyButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				goToDailyAttandancePage();
 			}
 		});
 		/**
 		 * on click to voucher page
 		 */
 		voucherButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				goToVoucherPage();
 			}
 		});
 		/**
 		 * on click to profile page
 		 */
 		profilButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				goToProfilPage();
 			}
 		});
 		/**
 		 * on click to list attendance page
 		 */
 		attendanceListButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				goToAttendancePage();
 
 			}
 		});
 		/**
 		 * on click to report page
 		 */
 		attendanceReportButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				goToReportPage();
 			}
 		});
 		/**
 		 * on click to form account page
 		 */
 		attendanceFormButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				goToFormPage();
 			}
 		});
 		/**
 		 * on click to sign out page
 		 */
 		signOutButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				/**check internet connection before sign out application */
 				if(connectionAvailable()){
 					LoginActivity.onClickLogout();
 					goToLoginPage();
 				}else{
 					alertMessage(ApplicationConstant.NO_INTERNET_CONNECTION, WelcomeScreenActivity.this);
 				}
 			}
 		});
 	}
 	/**
 	 * set login data model
 	 */
 	private void setDataLogin(){
 		String datalogin = loadStringPreferences("responseLogin", getApplicationContext());
 		if(!datalogin.equalsIgnoreCase("")){
 			loginDataModel = new LoginDataModel();
 			try {
 				loginDataModel.parseJSON(datalogin);
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	/**
 	 * set ui for check present status
 	 */
 	private void checkPresentstatus(){
 		String currentdate = null;
 		String time = null;
 		if (!checkPresentModel.getCheckoutPresent().equals("") && checkPresentModel.getCheckoutPresent() != null){
 			currentdate = checkPresentModel.getCheckoutPresent(); 
 			time = changeTimeAMPM(currentdate);
 			checkOutText.setVisibility(View.VISIBLE);
 			currentTime.setText(time);
 			currentTime.setVisibility(View.VISIBLE);
 			checkInText.setVisibility(View.GONE);
 		}else{
 			currentdate = checkPresentModel.getCheckinPresent();
 			time = changeTimeAMPM(currentdate);
 			checkInText.setVisibility(View.VISIBLE);
 			currentTime.setText(time);
 			currentTime.setVisibility(View.VISIBLE);
 			checkOutText.setVisibility(View.GONE);
 		}
 
 	}
 	/**
 	 * change format time to AM/PM
 	 * @param time
 	 * @return
 	 */
 	private String changeTimeAMPM(String time){
 		Date d = null;
 		try {
 			//change time from string to format date
 			 d = new SimpleDateFormat("HH:mm").parse(time);
 		} catch (ParseException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		//change time to am pm
 		SimpleDateFormat df = new SimpleDateFormat("hh:mm aaa");
 		df.format(d);
 		return df.format(d);
 	}
 	/**
 	 * set ui if employee not yet check in
 	 */
 	private void setUIDefaultStatus(){
 		checkInText.setVisibility(View.INVISIBLE);
 		checkOutText.setVisibility(View.INVISIBLE);
 		currentTime.setVisibility(View.INVISIBLE);
 	}
 	/**
 	 * set first name from username
 	 * @param username
 	 * @return
 	 */
 	private String setFirstName(String username){
 		String formatName [] = username.split(" ");
 		String firstName = formatName[0];
 		return firstName;
 	}
 	/**
 	 * set ui for welcome screen page 
 	 */
 	private void setUIWelcomeScreen(){
 		String firstname = setFirstName(loginDataModel.getUsername().toString());
 		username.setText(firstname);
 		String previlage = loginDataModel.getPrevilage();
 		if(previlage.equals(ADMIN)){
 			attendanceReportButton.setVisibility(View.VISIBLE);
 			attendanceFormButton.setVisibility(View.VISIBLE);
 		}else{
 			attendanceReportButton.setVisibility(View.GONE);
 			attendanceFormButton.setVisibility(View.GONE);
 		}
 	}
 	/**
 	 * 
 	 * call service for check status 
 	 *
 	 */
 	private class CallServiceCheckInOut extends AsyncTask<Void, Void, String> {
 
 		private ProgressDialog dialog;
 
 		protected void onPreExecute() {
 			this.dialog = ProgressDialog.show(WelcomeScreenActivity.this,
 					"On Process", "Please Wait...", true);
 		}
 
 		@Override
 		protected String doInBackground(Void... params) {
 			// TODO Auto-generated method stub
 			String employeeId = loadStringPreferences(EMPLOYEE_ID, getApplicationContext());
 			CheckInOutService checkInService = new CheckInOutService();
 			String response = checkInService.handleCheckInRequest(employeeId, status);
 			return response;
 		}
 
 		protected void onPostExecute(String result) {
 			String response = result;
 			if(response != null){
 				if(!response.equals("{}")){
 					try {
 						checkPresentModel.parseSource(response);
 						checkPresentstatus();
 					} catch (JSONException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}else{
 					setUIDefaultStatus();
 				}
 			}
 			this.dialog.dismiss();
 		}
 	}
 
 	private void goToAttendancePage(){
 		Intent intentAttandanceList = new Intent(WelcomeScreenActivity.this, AttendanceListActivity.class);
 		startActivity(intentAttandanceList);
 		finish();
 	}
 	private void goToDailyAttandancePage(){
 		Intent intentDailyAttandance = new Intent(WelcomeScreenActivity.this, DailyAttendanceActivity.class);
 		startActivity(intentDailyAttandance);
 		finish();
 	}
 	private void goToProfilPage(){
 		Intent intentProfilPage = new Intent(WelcomeScreenActivity.this, ProfilActivity.class);
 		startActivity(intentProfilPage);
 		finish();
 	}
 	private void goToVoucherPage(){
 		Intent intentVoucherPage = new Intent(WelcomeScreenActivity.this, VoucherActivity.class);
 		startActivity(intentVoucherPage);
 		finish();
 	}
 	private void goToReportPage(){
 		Intent intentReportPage = new Intent(WelcomeScreenActivity.this, ReportActivity.class);
 		startActivity(intentReportPage);
 		finish();
 	}
 	private void goToFormPage(){
 		Intent intentFormPage = new Intent(WelcomeScreenActivity.this,AttendanceFormActivity.class);
 		startActivity(intentFormPage);
 		finish();
 	}
 	private void goToLoginPage(){
 		Intent loginPage = new Intent(WelcomeScreenActivity.this,LoginActivity.class);
 		startActivity(loginPage);
 		finish();
 	}
 	public void onBackPressed() {
 		// your code.
 	}
 }

 package com.photon.connecttodoor.activity;
 
 import org.json.JSONException;
 
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
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 
 import com.photon.connecttodoor.R;
 import com.photon.connecttodoor.controller.CreateandEditAccountService;
 import com.photon.connecttodoor.controller.DeleteAccountService;
 import com.photon.connecttodoor.controller.ProfileService;
 import com.photon.connecttodoor.datamodel.ProfileModel;
 import com.photon.connecttodoor.utils.ApplicationConstant;
 
 public class AttendanceFormActivity extends MainActivity {
 
 	private String searchCategory[] = {ApplicationConstant.CAT_USERNAME, ApplicationConstant.CAT_EMPLOYEE_ID };
 	private String roleCategory[] = {"",ApplicationConstant.CAT_GENERAL_MANAGER, ApplicationConstant.CAT_FINANCE, ApplicationConstant.CAT_ADMIN, 
 			ApplicationConstant.CAT_PROJECT_MANAGER, ApplicationConstant.CAT_EMPLOYEE };
 	private String genderCategory[] = {"",ApplicationConstant.CAT_MALE, ApplicationConstant.CAT_FEMALE};
 	Spinner dropDownCategory,dropDownRole,dropDownGender;
 	private Button createButton, backButton, signOutButton, saveButton,deleteButton,editButton,deleteButtonAcc,searchButton;
 	private EditText editTextName, editTextEmployee, editTextProject, editTextStartWork,
 	editTextEmail, editTextAnnual, editTextMarried, editTextPaternity,
 	editTextCoff, editTextMaternity, editTextSick, editTextCondolences, editTextOnsite,
 	editTextUsername, editTextFacebookId, editTextSignature, editTextUserId, editSearchCategory;
 	private ImageView imgFacebook, imgDropDwnRole, imgDropDwnGender,imageCalendar;
 	private LinearLayout editSection;
 	String selectSearchCategory, selectRole,selectGender;
 	String status,searchBy;
 	ProfileModel profileDataModel;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_attendace_form);
 
 		backButton = (Button)findViewById(R.id.back_button);
 		signOutButton = (Button)findViewById(R.id.signout_button);
 		createButton = (Button)findViewById(R.id.button_create);
 		imageCalendar = (ImageView)findViewById(R.id.image_button_calendar);
 		editTextName = (EditText)findViewById(R.id.edit_text_name);
 		editTextEmployee = (EditText)findViewById(R.id.edit_text_employee);
 		editTextEmail = (EditText)findViewById(R.id.edit_text_email);
 		editTextStartWork = (EditText)findViewById(R.id.edit_text_start_working);
 		editTextProject = (EditText)findViewById(R.id.edit_text_project);
 		editTextAnnual = (EditText)findViewById(R.id.edit_text_annual);
 		editTextMarried = (EditText)findViewById(R.id.edit_text_married);
 		editTextPaternity = (EditText)findViewById(R.id.edit_text_paternity);
 		editTextMaternity = (EditText)findViewById(R.id.edit_text_maternity);
 		editTextSick = (EditText)findViewById(R.id.edit_text_sick);
 		editTextCoff = (EditText)findViewById(R.id.edit_text_coff);
 		editTextCondolences = (EditText)findViewById(R.id.edit_text_condolences);
 		editTextOnsite = (EditText)findViewById(R.id.edit_text_onsite);
 		editTextUsername = (EditText)findViewById(R.id.edit_user_name);
 		editTextFacebookId = (EditText)findViewById(R.id.edit_text_facebookId);
 		editTextSignature =(EditText)findViewById(R.id.edit_text_signature);
 		editTextUserId =(EditText)findViewById(R.id.edit_text_userId);
 		dropDownCategory = (Spinner)findViewById(R.id.spinnerSearchCategory);
 		dropDownRole =(Spinner)findViewById(R.id.spinnerRole);
 		dropDownGender =(Spinner)findViewById(R.id.spinnerGender);
 		imgFacebook =(ImageView)findViewById(R.id.imgFacebookid);
 		imgDropDwnRole =(ImageView)findViewById(R.id.imgDropdownRole);
 		imgDropDwnGender =(ImageView)findViewById(R.id.imgDroddownGender);
 		saveButton =(Button)findViewById(R.id.button_save);
 		deleteButton =(Button)findViewById(R.id.button_delete);
 		editButton =(Button)findViewById(R.id.button_edit);
 		editSection =(LinearLayout)findViewById(R.id.editLiniarLayout);
 		deleteButtonAcc =(Button)findViewById(R.id.button_deleteacc);
 		searchButton =(Button)findViewById(R.id.button_search);
 		editSearchCategory =(EditText)findViewById(R.id.category_id);
 		imageCalendar.setEnabled(false);
 		saveButton.setEnabled(false);
 		dropDownRole.setEnabled(false);
 		dropDownGender.setEnabled(false);
 		setDropdownSearchCategory();
 		setDropdownRoleCategory();
 		setDropdownGenderCategory();
 		selectSearchCategory();
 		selectGender();
 		selectRole();
 		getCurrentDate();
 		actionButton();
 	}
 
 	private void actionButton(){
 		/**
 		 * show calendar dialog
 		 */
 		imageCalendar.setOnClickListener(new OnClickListener() {
 
 			@SuppressWarnings("deprecation")
 			@Override
 			public void onClick(View v) {
 				getCurrentDate();
 				showDialog(DATE_DIALOG_ID);
 			}
 		});
 
 		/**
 		 * onClick to create account
 		 */
 		createButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				status = ApplicationConstant.CREATE;
 				editSection.setVisibility(View.INVISIBLE);
 				editSearchCategory.setText("");
 				setFormActive();
 				clearValue();
 			}
 		});
 		/**
 		 * onClick to edit account
 		 */
 		editButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				status = ApplicationConstant.UPDATE;
 				deleteButtonAcc.setVisibility(View.INVISIBLE);
 				setFormInactive();
 				clearValue();
 			}
 		});
 		/**
 		 * onClick to show delete button
 		 */
 		deleteButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				deleteButtonAcc.setVisibility(View.VISIBLE);
 				setFormInactive();
 				clearValue();
 			}
 		});
 		/**
 		 * onClick to delete account
 		 */
 		deleteButtonAcc.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				status = ApplicationConstant.DELETE;
 
 				/**check internet connection before delete form */
 				if(connectionAvailable()){
 					if(editSearchCategory.getText().toString().length() > 0){
 						new CallServiceDeleteAccount().execute();
 					}else{
 						alertMessage(ApplicationConstant.ERR_CATEGORY_BLANK, AttendanceFormActivity.this);
 					}
 				}else{
 					alertMessage(ApplicationConstant.NO_INTERNET_CONNECTION, AttendanceFormActivity.this);
 				}
 			}
 		});
 		/**
 		 * onClick to launch menu attendance
 		 */
 		backButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				goToWelcomePage();
 			}
 		});
 		/**
 		 * onClick to sign out application
 		 */
 		signOutButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				/**check internet connection before sign out application */
 				if(connectionAvailable()){
 					LoginActivity.onClickLogout();
 					goToLoginPage();
 				}else{
 					alertMessage(ApplicationConstant.NO_INTERNET_CONNECTION, AttendanceFormActivity.this);
 				}
 			}
 		});
 		/**
 		 * onClick to submit account
 		 */
 		saveButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				/**check internet connection before save attendance form */
 				if(connectionAvailable()){
 					new CallServiceCreateandEditAccount().execute();
 				}else{
 					alertMessage(ApplicationConstant.NO_INTERNET_CONNECTION, AttendanceFormActivity.this);
 				}
 			}
 		});
 		/**
 		 * onClick to search account
 		 */
 		searchButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				/**check internet connection before search attendance form */
 				if(connectionAvailable()){
 					onRequestSearchAccount();
 				}else{
 					alertMessage(ApplicationConstant.NO_INTERNET_CONNECTION, AttendanceFormActivity.this);
 				}
 			}
 		});
 	}
 	/**
 	 * call request for edit account
 	 */
 	private void onRequestSearchAccount(){
 		if(editSearchCategory.getText().toString().length() > 0){
 			new CallServiceSearchAccount().execute();
 			clearValue();
 			setFormActive();
 		}else{
 			alertMessage(ApplicationConstant.ERR_CATEGORY_BLANK, AttendanceFormActivity.this);
 		}
 	}
 	/**
 	 * launch menu attendance
 	 */
 	private void goToWelcomePage(){
 		Intent intentWelcomeScreen = new Intent(AttendanceFormActivity.this, WelcomeScreenActivity.class);
 		startActivity(intentWelcomeScreen);
 		finish();
 	}
 	/**
 	 * launch login page
 	 */
 	private void goToLoginPage(){
 		Intent intentLoginPage = new Intent(AttendanceFormActivity.this, LoginActivity.class);
 		startActivity(intentLoginPage);
 		finish();
 	}
 	@Override
 	public void onBackPressed() {
 		Intent intentWelcomeScreen = new Intent(AttendanceFormActivity.this, WelcomeScreenActivity.class);
 		startActivity(intentWelcomeScreen);
 		finish();
 	}
 	/**
 	 * show drop down search category
 	 */
 	private void setDropdownSearchCategory(){
 		createDropdownCategory(this, searchCategory, dropDownCategory);
 	}
 	/**
 	 * show drop down role category
 	 */
 	private void setDropdownRoleCategory(){
 		createDropdownCategory(this, roleCategory, dropDownRole);
 	}
 	/**
 	 * show drop down gender category
 	 */
 	private void setDropdownGenderCategory() {
 		createDropdownCategory(this, genderCategory, dropDownGender);
 	}
 	/**
 	 * set all input form active
 	 */
 	private void setFormActive(){
 		imageCalendar.setBackgroundResource(R.drawable.icon_calendar_start_working);
 		imageCalendar.setEnabled(true);
 		editTextName.setBackgroundResource(R.drawable.box_add_text_n_e_p_active);
 		editTextName.setEnabled(true);
 		editTextEmployee.setBackgroundResource(R.drawable.box_add_text_n_e_p_active);
 		editTextEmployee.setEnabled(true);
 		editTextProject.setBackgroundResource(R.drawable.box_add_text_n_e_p_active);
 		editTextProject.setEnabled(true);
 		editTextStartWork.setBackgroundResource(R.drawable.box_add_text_form_start_working_active);
 		editTextStartWork.setEnabled(true);
 		editTextStartWork.setText("");
 		editTextEmail.setBackgroundResource(R.drawable.box_add_text_form_email_active);
 		editTextEmail.setEnabled(true);
 		editTextAnnual.setBackgroundResource(R.drawable.box_add_text_form_small_active);
 		editTextAnnual.setEnabled(true);
 		editTextMarried.setBackgroundResource(R.drawable.box_add_text_form_small_active);
 		editTextMarried.setEnabled(true);
 		editTextPaternity.setBackgroundResource(R.drawable.box_add_text_form_small_active);
 		editTextPaternity.setEnabled(true);
 		editTextCoff.setBackgroundResource(R.drawable.box_add_text_form_small_active);
 		editTextCoff.setEnabled(true);
 		editTextMaternity.setBackgroundResource(R.drawable.box_add_text_form_small_active);
 		editTextMaternity.setEnabled(true);
 		editTextCondolences.setBackgroundResource(R.drawable.box_add_text_form_small_active);
 		editTextCondolences.setEnabled(true);
 		editTextSick.setBackgroundResource(R.drawable.box_add_text_form_small_active);
 		editTextSick.setEnabled(true);
 		editTextOnsite.setBackgroundResource(R.drawable.box_add_text_form_small_active);
 		editTextOnsite.setEnabled(true);	
 		editTextUsername.setBackgroundResource(R.drawable.box_add_text_n_e_p_active);
 		editTextUsername.setEnabled(true);
 		editTextFacebookId.setBackgroundResource(R.drawable.box_add_text_n_e_p_active);
 		editTextFacebookId.setEnabled(true);
 		dropDownGender.setBackgroundResource(R.drawable.box_add_text_n_e_p_active);
 		dropDownGender.setEnabled(true);
 		dropDownRole.setBackgroundResource(R.drawable.box_add_text_n_e_p_active);
 		dropDownRole.setEnabled(true);
 		editTextSignature.setBackgroundResource(R.drawable.box_add_text_n_e_p_active);
 		editTextSignature.setEnabled(true);
 		editTextUserId.setBackgroundResource(R.drawable.box_add_text_n_e_p_active);
 		editTextUserId.setEnabled(true);
 		saveButton.setEnabled(true);
 		imgDropDwnGender.setVisibility(View.VISIBLE);
 		imgDropDwnRole.setVisibility(View.VISIBLE);
 		imgFacebook.setVisibility(View.VISIBLE);
 	}
 	/**
 	 * set all input form inactive
 	 */
 	private void setFormInactive(){
 		imageCalendar.setBackgroundResource(R.drawable.icon_calendar_in_active);
 		imageCalendar.setEnabled(false);
 		editTextName.setBackgroundResource(R.drawable.box_add_text_n_e_p_in_active);
 		editTextName.setEnabled(false);
 		editTextEmployee.setBackgroundResource(R.drawable.box_add_text_n_e_p_in_active);
 		editTextEmployee.setEnabled(false);
 		editTextProject.setBackgroundResource(R.drawable.box_add_text_n_e_p_in_active);
 		editTextProject.setEnabled(false);
 		editTextStartWork.setBackgroundResource(R.drawable.box_add_text_form_start_working_in_active);
 		editTextStartWork.setEnabled(false);
 		editTextEmail.setBackgroundResource(R.drawable.box_add_text_form_email_in_active);
 		editTextEmail.setEnabled(false);
 		editTextAnnual.setBackgroundResource(R.drawable.box_add_text_form_small_in_active);
 		editTextAnnual.setEnabled(false);
 		editTextMarried.setBackgroundResource(R.drawable.box_add_text_form_small_in_active);
 		editTextMarried.setEnabled(false);
 		editTextPaternity.setBackgroundResource(R.drawable.box_add_text_form_small_in_active);
 		editTextPaternity.setEnabled(false);
 		editTextCoff.setBackgroundResource(R.drawable.box_add_text_form_small_in_active);
 		editTextCoff.setEnabled(false);
 		editTextMaternity.setBackgroundResource(R.drawable.box_add_text_form_small_in_active);
 		editTextMaternity.setEnabled(false);
 		editTextCondolences.setBackgroundResource(R.drawable.box_add_text_form_small_in_active);
 		editTextCondolences.setEnabled(false);
 		editTextSick.setBackgroundResource(R.drawable.box_add_text_form_small_in_active);
 		editTextSick.setEnabled(false);
 		editTextOnsite.setBackgroundResource(R.drawable.box_add_text_form_small_in_active);
 		editTextOnsite.setEnabled(false);	
 		editTextUsername.setBackgroundResource(R.drawable.box_add_text_n_e_p_in_active);
 		editTextUsername.setEnabled(false);
 		editTextFacebookId.setBackgroundResource(R.drawable.box_add_text_n_e_p_in_active);
 		editTextFacebookId.setEnabled(false);
 		dropDownGender.setBackgroundResource(R.drawable.box_add_text_n_e_p_in_active);
 		dropDownGender.setEnabled(false);
 		dropDownRole.setBackgroundResource(R.drawable.box_add_text_n_e_p_in_active);
 		dropDownRole.setEnabled(false);
 		editTextSignature.setBackgroundResource(R.drawable.box_add_text_n_e_p_in_active);
 		editTextSignature.setEnabled(false);
 		editTextUserId.setBackgroundResource(R.drawable.box_add_text_n_e_p_in_active);
 		editTextUserId.setEnabled(false);
 		saveButton.setEnabled(false);
 		imgDropDwnGender.setVisibility(View.INVISIBLE);
 		imgDropDwnRole.setVisibility(View.INVISIBLE);
 		imgFacebook.setVisibility(View.INVISIBLE);
 		editSection.setVisibility(View.VISIBLE);
 	}
 
 	/** Callback received when the user "picks" a date in the dialog */
 	private DatePickerDialog.OnDateSetListener pDateSetListener =
 			new DatePickerDialog.OnDateSetListener() {
 
 		@Override
 		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
 			pYear = year;
 			pMonth = monthOfYear;
 			pDay = dayOfMonth;		
 			editTextStartWork.setText(getDateEditText());
 		}
 	};
 
 
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
 	/**
 	 * action dropdown each  search category 
 	 */
 	private void selectSearchCategory(){
 		dropDownCategory.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
 				selectSearchCategory = dropDownCategory.getSelectedItem().toString();
 				editSearchCategory.setText("");
 				try {
 					if(selectSearchCategory.equals(ApplicationConstant.CAT_USERNAME)){
 						searchBy = ApplicationConstant.USERNAME;
 					}else{
 						searchBy = ApplicationConstant.EMPLOYEE_ID;
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
 	/**
 	 * action dropdown each  gender category 
 	 */
 	private void selectGender(){
 		dropDownGender.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
 				selectGender = dropDownGender.getSelectedItem().toString();
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 
 			}
 		});
 	}
 	/**
 	 * action dropdown each  role category 
 	 */
 	private void selectRole(){
 		dropDownRole.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3){
 				selectRole = dropDownRole.getSelectedItem().toString();
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 
 			}
 		});
 	}
 	/**
 	 * get all value account
 	 */
 	private void getValueForEditAccount(){
 		editTextName.setText(profileDataModel.getEmployeeName());
 		editTextUsername.setText(profileDataModel.getUsername());
 		editTextEmployee.setText(profileDataModel.getEmployeeId());
 		editTextProject.setText(profileDataModel.getProjectId());
 		editTextStartWork.setText(profileDataModel.getEmployeeStartWork());
 		editTextAnnual.setText(profileDataModel.getAnnual());
 		editTextCoff.setText(profileDataModel.getcOff());
 		editTextCondolences.setText(profileDataModel.getCondolences());
 		editTextMarried.setText(profileDataModel.getMarried());
 		editTextPaternity.setText(profileDataModel.getPaternity());
 		editTextMaternity.setText(profileDataModel.getMaternity());
 		editTextOnsite.setText(profileDataModel.getOnsite());
 		editTextSick.setText(profileDataModel.getSick());
 		editTextFacebookId.setText(profileDataModel.getFacebookId());
 		editTextEmail.setText(profileDataModel.getEmployeeEmail());
 		editTextSignature.setText(profileDataModel.getSignature());
 	}
 	/**
 	 * clear all value account
 	 */
 	private void clearValue(){
 		editTextName.setText("");
 		editTextUsername.setText("");
 		editTextEmployee.setText("");
 		editTextProject.setText("");
 		editTextStartWork.setText("");
 		editTextAnnual.setText("");
 		editTextCoff.setText("");
 		editTextCondolences.setText("");
 		editTextMarried.setText("");
 		editTextPaternity.setText("");
 		editTextMaternity.setText("");
 		editTextOnsite.setText("");
 		editTextSick.setText("");
 		editTextFacebookId.setText("");
 		editTextEmail.setText("");
 		editTextSignature.setText("");
		editTextUserId.setText("");
 	}
 	/***
 	 * check all input field
 	 */
 	private Boolean checkInputField(String statusAccount,String employeeID,String username,String projectID,String name,String emailPhoton,
 			String facebookId,String startWork,String jobRole,String annual,String coff,String condolences,String married,String maternity,
 			String paternity,String onsite,String sick,String dataURLSignature,String gender)
 	{
 		if(!statusAccount.matches("")&&!employeeID.matches("")&&!username.matches("")&&!projectID.matches("")&&!name.matches("")&&!emailPhoton.matches("")
 				&&!facebookId.matches("")&&!startWork.matches("")&&!jobRole.matches("")&&!annual.matches("")&&!coff.matches("")&&!condolences.matches("")&&
 				!married.matches("")&&!maternity.matches("")&&!paternity.matches("")&&!onsite.matches("")&&!sick.matches("")&&!dataURLSignature.matches("")&&
 				!gender.matches("")){
 			return true;
 		}
 		return false;
 	}
 	/**
 	 * request all about service account (create and edit)
 	 * each service difference by status
 	 *
 	 */
 	private class CallServiceCreateandEditAccount extends AsyncTask<Void, Void, String> {
 
 		private ProgressDialog dialog;
 
 		protected void onPreExecute() {
 			this.dialog = ProgressDialog.show(AttendanceFormActivity.this,
 					"Account Process", "Please Wait...", true);
 		}
 
 		@Override
 		protected String doInBackground(Void... params) {
 			CreateandEditAccountService createAndEditAccountService = new CreateandEditAccountService();
 			String statusAccount = status;
 			String employeeID = editTextEmployee.getText().toString();
 			String username = editTextUsername.getText().toString();
 			String projectID = editTextProject.getText().toString();
 			String name = editTextName.getText().toString();
 			String emailPhoton = editTextEmail.getText().toString();
 			String facebookId = editTextFacebookId.getText().toString();
 			String startWork = changeFormatDate(editTextStartWork.getText().toString());
 			String jobRole = selectRole;
 			String annual = editTextAnnual.getText().toString();
 			String coff = editTextCoff.getText().toString();
 			String condolences = editTextCondolences.getText().toString();
 			String married = editTextMarried.getText().toString();
 			String maternity = editTextMaternity.getText().toString();
 			String paternity = editTextPaternity.getText().toString();
 			String onsite = editTextOnsite.getText().toString();
 			String sick = editTextSick.getText().toString();
 			String dataURLSignature = editTextSignature.getText().toString();
 			String gender = selectGender;
 			String response = "" ;
 			if(checkInputField(statusAccount, employeeID, username, projectID, name, emailPhoton, facebookId, startWork, jobRole, annual, coff, condolences, married, maternity, paternity, onsite, sick, dataURLSignature, gender) == true)
 			{
 				response = createAndEditAccountService.handleCreateandEditAccountRequest(statusAccount, employeeID, username, projectID, name, emailPhoton, facebookId, 
 						startWork, jobRole, annual, coff, condolences, married, maternity, paternity, onsite, sick, dataURLSignature, gender);
 			}
 			return response;
 		}
 
 		protected void onPostExecute(String result) {
 			String response = result;
 			if(response == null || response == ""){
 				alertMessage("Please fill all field", AttendanceFormActivity.this);
 			}
 			this.dialog.dismiss();
 		}
 	}
 	/**
 	 * request delete  account service
 	 */
 	private class CallServiceDeleteAccount extends AsyncTask<Void, Void, String> {
 
 		private ProgressDialog dialog;
 
 		protected void onPreExecute() {
 			this.dialog = ProgressDialog.show(AttendanceFormActivity.this,
 					"Delete Process", "Please Wait...", true);
 		}
 
 		@Override
 		protected String doInBackground(Void... params) {
 			String statusDel = status;
 			String value = editSearchCategory.getText().toString();
 			DeleteAccountService deleteAccountService = new DeleteAccountService();
 			String response = deleteAccountService.handleDeleteAccountRequest(statusDel, value);
 			return response;
 		}
 
 		protected void onPostExecute(String result) {
 			this.dialog.dismiss();
 		}
 	}
 	/**
 	 * request profile service
 	 *
 	 */
 	private class CallServiceSearchAccount extends AsyncTask<Void, Void, String> {
 
 		private ProgressDialog dialog;
 
 		protected void onPreExecute() {
 			this.dialog = ProgressDialog.show(AttendanceFormActivity.this,
 					"Search Process", "Please Wait...", true);
 		}
 
 		@Override
 		protected String doInBackground(Void... params) {
 			String value = editSearchCategory.getText().toString();
 			String searchParameter = searchBy;
 			ProfileService profileService = new ProfileService();
 			String response = profileService.handleProfileRequest(searchParameter, value);
 			return response;
 		}
 
 		protected void onPostExecute(String result) {
 			profileDataModel = new ProfileModel();
 			try {
 				profileDataModel.parseJSON(result);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			getValueForEditAccount();
 			this.dialog.dismiss();
 		}
 	}
 }

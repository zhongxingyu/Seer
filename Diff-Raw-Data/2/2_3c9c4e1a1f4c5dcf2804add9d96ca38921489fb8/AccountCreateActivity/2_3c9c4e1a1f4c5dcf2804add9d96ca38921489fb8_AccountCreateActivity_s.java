 /*
  * Copyright (C) 2011 AlarmApp.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at 
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  */
 
 package org.alarmapp.activities;
 
 import org.alarmapp.AlarmApp;
 import org.alarmapp.R;
 import org.alarmapp.model.classes.PersonData;
 import org.alarmapp.util.ActivityUtil;
 import org.alarmapp.util.IntentUtil;
 import org.alarmapp.util.LogEx;
 import org.alarmapp.web.WebClient;
 import org.alarmapp.web.WebException;
 import org.alarmapp.web.json.WebResult;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 /**
  * @author frank
  * 
  */
 public class AccountCreateActivity extends Activity {
 
 	private static final String LAST_NAME_INVALID_ERROR = "Der Nachname muss aus 2-30 Buchstaben bestehen.";
 	private static final String FIRST_NAME_INVALID_ERROR = "Der Vorname muss aus 2-30 Buchstaben bestehen.";
 	private static final String PASSWPRD_MATCH_ERROR = "Die Passworte stimmen nicht überein.";
 	private static final String PASSWORD_LENGTH_INVALID = "Das Passwort muss zwischen 6 und 30 Zeichen lang sein.";
 	private static final String EMAIL_EXISTS_ERROR = "Es existiert bereits ein Benutzer mit dieser Email-Adresse.";
 	private static final String USERNAME_EXISTS_ERROR = "Es existiert bereits ein Benutzer mit diesem Benutzernamen";
 	private static final String USERNAME_INVALID_ERROR = "Der Benutzername darf aus Buchstaben, +, -, @, . und _ bestehen und muss zwischen 2 und 30 Zeichen lang sein";
 	private static final String CREATE_USER_FAILED_ERROR = "Das Anlegen eines neuen Benutzers ist gescheitert.";
 
 	EditText etFirstName;
 	EditText etLastName;
 	EditText etUserName;
 	EditText etEmail;
 	EditText etPassword;
 	EditText etPassword2;
 	Button btAccountCreate;
 	WebClient httpWebClient;
 	ProgressDialog progressDialog;
 
 	OnClickListener accountCreateClick = new OnClickListener() {
 		public void onClick(View v) {
 			LogEx.info("Creating User Account");
 			progressDialog = ProgressDialog.show(AccountCreateActivity.this,
 					"", "Benutzeraccount wird erzeugt. Bitte warten...", true);
 			new Thread(createUserRunnable).start();
 		}
 	};
 
 	private Runnable createUserRunnable = new Runnable() {
 
 		public void run() {
 			String username = etUserName.getText().toString();
 			String firstName = etFirstName.getText().toString();
 			String lastName = etLastName.getText().toString();
 			String email = etEmail.getText().toString();
 			String password = etPassword.getText().toString();
 			String passwordConfirmation = etPassword2.getText().toString();
 
 			try {
 				final PersonData p = AlarmApp.getWebClient().createUser(
 						username, firstName, lastName, email, password,
 						passwordConfirmation);
 
 				runOnUiThread(new Runnable() {
 
 					public void run() {
 						userCreateSuccessful(p);
 					}
 				});
 			} catch (WebException e) {
 				LogEx.exception("Creating a new User failed!", e);
 				progressDialog.cancel();
 				displayError(CREATE_USER_FAILED_ERROR);
 			}
 		}
 
 	};
 
 	private void userCreateSuccessful(PersonData value) {
 		LogEx.info("User " + value.getFullName() + " created. Id is "
 				+ value.getId());
 		progressDialog.cancel();
 
 		AlarmApp.setUser(value);
 
 		IntentUtil.displayJoinDepartmentActivity(this, value);
 	}
 
 	OnEditorActionListener createEditorActionListener(
 			final Runnable validationAction) {
 		return new OnEditorActionListener() {
 
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event) {
 				if (actionId == EditorInfo.IME_ACTION_NEXT) {
 					new Thread(validationAction).start();
 				}
 				return false;
 			}
 		};
 	}
 
 	/**
 	 * @param string
 	 */
 	protected void displayError(String string) {
 		ActivityUtil.displayToast(this, string, 30);
 
 	}
 
 	OnEditorActionListener createRegexValidator(final String regex,
 			final String errorMesssage) {
 		return new OnEditorActionListener() {
 
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event) {
 				if (actionId == EditorInfo.IME_ACTION_NEXT) {
 					if (!v.getText().toString().matches(regex)) {
 						v.setError(errorMesssage);
 					} else {
 						clearErrors(v);
 					}
 				}
 				return false;
 			}
 		};
 	}
 
 	private Runnable validateUsername = new Runnable() {
 		public void run() {
 			LogEx.verbose("Validating the User name");
 			String userName = etUserName.getText().toString();
 
 			if (!userName.matches("([\\w.+-_@]){1,30}"))
 				displayError(etUserName, USERNAME_INVALID_ERROR);
 
 			try {
 				WebResult result = AlarmApp.getWebClient().checkUserName(
 						userName);
 
 				if (failedWithError(result, "username_exists")) {
 					displayError(etUserName, USERNAME_EXISTS_ERROR);
 					return;
 				}
 				clearErrors(etUserName);
 
 			} catch (WebException e) {
 				LogEx.exception("Failed to check the Email adress", e);
 			}
 
 		}
 	};
 
 	private Runnable validateEmail = new Runnable() {
 
 		public void run() {
 			LogEx.verbose("Validating Email address");
 
 			try {
 				WebResult result = AlarmApp.getWebClient().checkEmailAdress(
 						etEmail.getText().toString());
 
 				if (failedWithError(result, "email_exists")) {
 					displayError(etEmail, EMAIL_EXISTS_ERROR);
 					return;
 				}
 
 				clearErrors(etEmail);
 			} catch (WebException e) {
 				LogEx.exception("Failed to check the Email adress", e);
 			}
 		}
 	};
 
 	private boolean failedWithError(WebResult r, String tag) {
 		return !r.wasSuccessful() && r.getTag().equals(tag);
 	}
 
 	private Runnable validatePassword = new Runnable() {
 
 		public void run() {
 			LogEx.verbose("Validating Password");
 
 			String pass1 = etPassword.getText().toString();
 			String pass2 = etPassword2.getText().toString();
 
			if (pass1.length() < 6 && pass1.length() > 30) {
 				displayError(etPassword, PASSWORD_LENGTH_INVALID);
 				return;
 			}
 			clearErrors(etPassword);
 
 			if (!pass1.equals(pass2)) {
 				displayError(etPassword2, PASSWPRD_MATCH_ERROR);
 				return;
 			}
 			clearErrors(etPassword2);
 		}
 	};
 
 	private void clearErrors(final TextView et) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				et.setError(null);
 			}
 		});
 	}
 
 	private void displayError(final EditText et, final String errorMessage) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				et.setError(errorMessage);
 			}
 		});
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		this.setContentView(R.layout.account_create);
 
 		this.etEmail = (EditText) findViewById(R.id.etEmail);
 		this.etFirstName = (EditText) findViewById(R.id.etFirstName);
 		this.etLastName = (EditText) findViewById(R.id.etLastName);
 		this.etUserName = (EditText) findViewById(R.id.etName);
 		this.etPassword = (EditText) findViewById(R.id.etPassword);
 		this.etPassword2 = (EditText) findViewById(R.id.etPassword2);
 
 		this.btAccountCreate = (Button) findViewById(R.id.btAccountCreate);
 
 		this.etPassword2.setImeOptions(EditorInfo.IME_ACTION_DONE);
 
 		btAccountCreate.setOnClickListener(accountCreateClick);
 
 		this.etUserName
 				.setOnEditorActionListener(createEditorActionListener(validateUsername));
 		this.etPassword2
 				.setOnEditorActionListener(createEditorActionListener(validatePassword));
 		this.etEmail
 				.setOnEditorActionListener(createEditorActionListener(validateEmail));
 		this.etFirstName.setOnEditorActionListener(createRegexValidator(
 				"\\w{2,30}", FIRST_NAME_INVALID_ERROR));
 		this.etLastName.setOnEditorActionListener(createRegexValidator(
 				"\\w{2,30}", LAST_NAME_INVALID_ERROR));
 	}
 }

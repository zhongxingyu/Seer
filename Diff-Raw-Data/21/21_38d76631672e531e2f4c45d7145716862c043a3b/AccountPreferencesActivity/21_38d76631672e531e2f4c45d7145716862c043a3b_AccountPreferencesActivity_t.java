 /*
  * The MIT License
  * 
  * Copyright (c) 2010 Petar Petrov
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package net.vexelon.myglob.configuration;
 
 import net.vexelon.myglob.R;
 import net.vexelon.myglob.users.AccountType;
 import net.vexelon.myglob.users.User;
 import net.vexelon.myglob.users.UsersManager;
 import net.vexelon.myglob.utils.Utils;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.DialogInterface.OnClickListener;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.DialogInterface.OnKeyListener;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.text.TextUtils;
 import android.view.KeyEvent;
 
 public class AccountPreferencesActivity extends PreferenceActivity {
 	
 	PreferenceActivity _activity;
 	EditTextPreference _accountNamePref;
 	EditTextPreference _accountNumberPref;
 	ListPreference _accountOperatorPref;
 	EditTextPreference _accountPasswordPref;
 	CheckBoxPreference _accountSavePasswordPref;
 	Preference _accountDeletePref;
 	
 	User _editUser = null;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setResult(RESULT_CANCELED);
 		addPreferencesFromResource(R.xml.prefs);
 		
 		_activity = this;
 		
 		// get components refs
 		_accountNamePref = (EditTextPreference) findPreference(getResString(R.string.pref_account_name));
 		_accountOperatorPref = (ListPreference) findPreference(getResString(R.string.pref_account_operator));
 		_accountNumberPref = (EditTextPreference) findPreference(getResString(R.string.pref_account_phonenumber));
 		_accountPasswordPref = (EditTextPreference) findPreference(getResString(R.string.pref_account_password));
 //		_accountSavePasswordPref = (CheckBoxPreference) findPreference(getResString(R.string.pref_account_save_password)); 
 		_accountDeletePref = (Preference) findPreference(getResString(R.string.pref_account_delete));
 		
 		Intent intent = getIntent();
 		if (intent.getBooleanExtra(Defs.INTENT_ACCOUNT_ADD, false)) {
 			_accountNamePref.setText("");
 			_accountNumberPref.setText("089");
 			_accountOperatorPref.setValueIndex(0);
 			_accountPasswordPref.setText("");
 //			_accountSavePasswordPref.setChecked(false);
 			_accountDeletePref.setEnabled(false);
 		}
 		else if (intent.getBooleanExtra(Defs.INTENT_ACCOUNT_EDIT, false)) {
 			// edit account
 			String phone = intent.getStringExtra(Defs.INTENT_ACCOUNT_PHONENUMBER);
 			User user = UsersManager.getInstance().getUserByPhoneNumber(phone);
 			_editUser = user;
 			_accountNamePref.setText(user.getAccountName());
 			_accountNumberPref.setText(user.getPhoneNumber());
 			_accountNumberPref.setEnabled(false); // NO NUMBER EDITING ALLOWED !
 			_accountPasswordPref.setText(Defs.DUMMY_PASSWORD); // PASSWORD FILED IS 'EMPTY'
 			_accountOperatorPref.setValueIndex(0);
 			
 			_accountDeletePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 				@Override
 				public boolean onPreferenceClick(Preference preference) {
 					
 					AlertDialog.Builder alertBuilder = new AlertDialog.Builder(_activity);
 					alertBuilder.setTitle(R.string.dlg_account_delete_title)
 						.setMessage(R.string.dlg_account_delete_msg)
 						.setIcon(R.drawable.ic_dialog_alert)
 						.setPositiveButton(getResString(R.string.dlg_msg_yes), new OnClickListener() {
 							
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
								User removedUser = UsersManager.getInstance().removeUser(_accountNumberPref.getText());
 								
 								// save all users
 								SharedPreferences prefs = _activity.getSharedPreferences(Defs.PREFS_USER_PREFS, 0);
 								UsersManager.getInstance().save(prefs);
 								_activity.setResult(Defs.INTENT_RESULT_ACCOUT_DELETED);
 								_activity.finish();
 								
								// set new default selection
								if (GlobalSettings.getInstance().getLastSelectedPhoneNumber().equals(removedUser
										.getPhoneNumber()) && UsersManager.getInstance().getUsersCount() > 0) {
									String[] users = UsersManager.getInstance().getUsersPhoneNumbersList();
									GlobalSettings.getInstance().setLastSelectedPhoneNumber(users[0]);
								}
								
 								dialog.dismiss();
 							}
 						})
 						.setNegativeButton(getResString(R.string.dlg_msg_no), new OnClickListener() {
 							
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								dialog.dismiss();
 							}
 						}).show();					
 
 					return false;
 				}
 			});
 		}
 		else {
 			// no proper operation type passed
 			setResult(RESULT_CANCELED);
 			finish();
 		}
 	}
 	
 	private void saveSettings() throws Exception {
 		// save all account data
 		
 		User user = null;
 		String phoneNumber = _accountNumberPref.getText().trim();
 		
 		if (getIntent().getBooleanExtra(Defs.INTENT_ACCOUNT_ADD, false)) {
 //		if (_editUser == null) {
 			user = new User().setAccountName(_accountNamePref.getText())
 				.setPhoneNumber(phoneNumber)
 				.setAccountType(getAccountTypeFromListPrefs());
 //			UsersManager.getInstance().addUser(user);
 			if (UsersManager.getInstance().isUserExists(phoneNumber)) 
 				throw new Exception(getResString(R.string.err_msg_user_already_exists));
 			
 		} else {
 			user = _editUser; // get instance of user being edited
 			user.setAccountName(_accountNamePref.getText());
 			user.setAccountType(getAccountTypeFromListPrefs());
 		}
 			
 		//Log.v(Defs.LOG_TAG, "Saved raw pass: " + _accountPasswordPref.getText() + " Enc pass: " + user.getEncodedPassword());
 		
 		if (!TextUtils.equals(_accountPasswordPref.getText(), Defs.DUMMY_PASSWORD)) {
 			UsersManager.getInstance().setUserPassword(user, _accountPasswordPref.getText());
 		}
 		
 		if (getIntent().getBooleanExtra(Defs.INTENT_ACCOUNT_ADD, false)) {
 			UsersManager.getInstance().addUser(user);
 		}
 
 		// save all users
 		SharedPreferences prefs = this.getSharedPreferences(Defs.PREFS_USER_PREFS, 0);
 		UsersManager.getInstance().save(prefs);
 		
 		// set default selected
 		GlobalSettings.getInstance().setLastSelectedPhoneNumber(phoneNumber);
 	}
 	
 	
 	private boolean isSettingsValid() {
 		return !TextUtils.isEmpty(_accountNamePref.getText()) &&
 			!TextUtils.isEmpty(_accountNumberPref.getText()) &&
 			!TextUtils.isEmpty(_accountOperatorPref.getValue()) &&
 			!TextUtils.isEmpty(_accountPasswordPref.getText());
 	}
 	
 	private AccountType getAccountTypeFromListPrefs() {
 		String[] operators = getResStringArray(R.array.array_available_operators);
 		if (_accountOperatorPref.getValue().equals(operators[0])) {
 			return AccountType.Globul;
 		}
 //		else if (_accountOperatorPref.getValue().equals(operators[1])) {
 //			return AccountType.M_Tel;
 //		}
 		
 		return null;
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			
 			if (isSettingsValid()) {
 				try {
 					saveSettings();
 					setResult(RESULT_OK);
 					finish();					
 				} catch(Exception e) {
 					// show error msg & exit
 					
 					AlertDialog dialog = Utils.createAlertDialog(this, e.getMessage(), getResString(R.string.dlg_error_msg_title));
 					dialog.setOnDismissListener(new OnDismissListener() {
 						
 						@Override
 						public void onDismiss(DialogInterface dialog) {
 							_activity.finish();
 						}
 					});
 					dialog.setOnKeyListener(new OnKeyListener() {
 						
 						@Override
 						public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
 							// cancel key-down events
 							return false;
 						}
 					});
 					dialog.show();
 				}
 			} else {
 				// One or more obligatory settings is missing. Show continue yes/no dialog.
 				
 				AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
 				alertBuilder.setTitle(R.string.dlg_account_validate_title)
 					.setMessage(R.string.dlg_account_validate_msg)
 					.setIcon(R.drawable.ic_dialog_alert)
 					.setPositiveButton(getResString(R.string.dlg_msg_yes), new OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							//Log.i(Defs.LOG_TAG, "Nothing was saved!");
 							dialog.dismiss();
 							_activity.setResult(RESULT_CANCELED);
 							finish();
 						}
 					})
 					.setNegativeButton(getResString(R.string.dlg_msg_no), new OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.dismiss();
 						}
 					}).show();
 			}
 			
 			return true;
 		}
 		
 		return super.onKeyDown(keyCode, event);
 	}
 
 	private String getResString(int id) {
 		return this.getResources().getString(id);
 	}	
 	
 	private String[] getResStringArray(int id) {
 		return this.getResources().getStringArray(id);
 	}	
 	
 }

 package com.edwise.dedicamns;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.edwise.dedicamns.asynctasks.AppData;
 import com.edwise.dedicamns.asynctasks.ConnectionAsyncTask;
 import com.edwise.dedicamns.asynctasks.LoginConstants;
 import com.edwise.dedicamns.connections.ConnectionFacade;
 import com.edwise.dedicamns.menu.MenuUtils;
 
 public class LoginActivity extends Activity {
     private static final String LOGTAG = LoginActivity.class.toString();
 
     private ProgressDialog pDialog = null;
 
     private EditText userLoginEditText;
     private EditText passLoginEditText;
     private CheckBox rememberMeCheckBox;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.login);
 	Log.d(LOGTAG, "onCreate: Comenzando...");
 	AppData.setCurrentActivity(this);
 
 	initFields();
 
 	if (savedInstanceState != null && savedInstanceState.getBoolean("pDialogON")) {
 	    // El pDialog estaba activo, mostrarlo otra vez
 	    showDialog();
 	}
 
	if (!checkIfLogout()) {
	    chargeSavedData();
	    ConnectionFacade.createWebConnection(this);
	}
     }
 
     private void initFields() {
 	userLoginEditText = (EditText) findViewById(R.id.userMNText);
 	passLoginEditText = (EditText) findViewById(R.id.passMNText);
 	rememberMeCheckBox = (CheckBox) findViewById(R.id.rememberUserCheck);
     }
 
     private void chargeSavedData() {
 	SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
 	String user = sharedPref.getString(LoginConstants.USER_TAG, null);
 	String pass = sharedPref.getString(LoginConstants.PASS_TAG, null);
 	Boolean remember = sharedPref.getBoolean(LoginConstants.REMEMBER_TAG, false);
 
 	if (user != null) {
 	    userLoginEditText.setText(user);
 	}
 	if (pass != null) {
 	    passLoginEditText.setText(pass);
 	}
 	rememberMeCheckBox.setChecked(remember);
     }
 
     private boolean checkIfLogout() {
 	return getIntent().getBooleanExtra(LoginConstants.IS_LOGOUT_TAG, false);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 	getMenuInflater().inflate(R.menu.login_menu, menu);
 	return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 	boolean returned = false;
 	switch (item.getItemId()) {
 	case R.id.menu_about_us:
 	    MenuUtils.goToAbout(this);
 	    returned = true;
 	    break;
 	default:
 	    returned = super.onOptionsItemSelected(item);
 	}
 
 	return returned;
     }
 
     public void doLogin(View view) {
 	Log.d(LOGTAG, "doLogin: Click en conectar...");
 
 	if (checkFieldsFilled()) {
 	    accesWebWithLoginData();
 	} else {
 	    Toast toast = Toast.makeText(this, getString(R.string.msgUserPassError), Toast.LENGTH_LONG);
 	    toast.setGravity(Gravity.CENTER, 0, 100);
 	    toast.show();
 
 	}
     }
 
     private void accesWebWithLoginData() {
 	HashMap<String, String> accessData = new HashMap<String, String>();
 	String userLogin = userLoginEditText.getText().toString();
 	accessData.put(LoginConstants.USER_TAG, userLogin);
 
 	String passLogin = passLoginEditText.getText().toString();
 	accessData.put(LoginConstants.PASS_TAG, passLogin);
 
 	String checked = Boolean.toString(rememberMeCheckBox.isChecked());
 	accessData.put(LoginConstants.CHECK_TAG, checked);
 
 	Log.d(LOGTAG, "doLogin: User insertado: " + userLogin + " / " + checked);
 
 	hideKeyboard();
 	showDialog();
 
 	callConnectionAsyncTask(accessData);
     }
 
     private void showDialog() {
 	pDialog = ProgressDialog.show(LoginActivity.this, getString(R.string.msgConnecting),
 		getString(R.string.msgConnectingAlert), true);
     }
 
     private void hideKeyboard() {
 	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 	imm.hideSoftInputFromWindow(passLoginEditText.getWindowToken(), 0);
     }
 
     @SuppressWarnings("unchecked")
     private void callConnectionAsyncTask(Map<String, String> accessData) {
 	Log.d(LOGTAG, "callConnectionAsyncTask: Llamada al asyncTask...");
 
 	AsyncTask<Map<String, String>, Integer, Integer> connectionAsyncTask = new ConnectionAsyncTask();
 	connectionAsyncTask.execute(accessData);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	super.onActivityResult(requestCode, resultCode, data);
 	Log.d(LOGTAG, "onActivityResult: en el onActivityResult...");
     }
 
     @Override
     protected void onDestroy() {
 	super.onStop();
 	Log.d(LOGTAG, "onDestroy: en el onDestroy...");
 	// Borra preferences si el check está desactivado
 	removeLoginDataIfNeeded();
     }
 
     @Override
     public void onBackPressed() {
 	moveTaskToBack(true);
 	super.onBackPressed();
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
 	Log.d(LOGTAG, "onSaveInstanceState");
 	if (pDialog != null) {
 	    pDialog.cancel();
 	    outState.putBoolean("pDialogON", true);
 	}
 	super.onSaveInstanceState(outState);
     }
 
     @Override
     protected void onRestart() {
 	super.onRestart();
 	AppData.setCurrentActivity(this);
     }
 
     public void closeDialog() {
 	if (pDialog != null) {
 	    pDialog.dismiss();
 	    pDialog = null;
 	}
     }
 
     private void removeLoginDataIfNeeded() {
 	if (!rememberMeCheckBox.isChecked()) {
 	    SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
 	    sharedPref.edit().clear().commit();
 	    Log.d(LOGTAG, "removeLoginDataIfNeeded: Borrado el sharedPreferences!");
 	}
     }
 
     private boolean checkFieldsFilled() {
 	boolean filled = true;
 	String userLogin = userLoginEditText.getText().toString();
 	String passLogin = passLoginEditText.getText().toString();
 
 	if (StringUtils.isBlank(userLogin) || StringUtils.isBlank(passLogin)) {
 	    filled = false;
 	}
 
 	return filled;
     }
 }

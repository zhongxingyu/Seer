 package com.wu.androidfileclient;
 
 import android.app.Activity;
 import android.content.Intent;
import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.provider.Settings.Secure;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 import com.wu.androidfileclient.async.PerformCheckCredentialAsyncTask;
 import com.wu.androidfileclient.async.PerformRegisterDeviceAsyncTask;
 import com.wu.androidfileclient.listeners.CancelTaskOnCancelListener;
 import com.wu.androidfileclient.models.Credential;
 import com.wu.androidfileclient.utils.ProgressDialogHandler;
 import com.wu.androidfileclient.utils.Utilities;
 
 public class LoginActivity extends Activity {
 	private ProgressDialogHandler progressDialog;
 	private EditText userNameBox;
 	private EditText passwordBox;
 	private Button   loginBtn;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_login);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
         final Credential credential = Utilities.getCredential(this);
         credential.setDeviceId(Secure.getString(this.getContentResolver(), Secure.ANDROID_ID));
 
         userNameBox = (EditText) findViewById(R.id.user_name);
 		passwordBox = (EditText) findViewById(R.id.password);
 		loginBtn    = (Button) findViewById(R.id.login);
 
 		if (!credential.getUserName().isEmpty() && !credential.getDeviceCode().isEmpty()) {
 			checkCredential(credential);
 		}
 
 		loginBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				credential.setUserName(userNameBox.getText().toString());
 				credential.setPassword(passwordBox.getText().toString());
 				registerDevice(credential);
 			}
         });
     }
 
     public void checkCredential(Credential credential) {
     	showProgressDialog(ProgressDialogHandler.CHECKING_CREDENTIAL);
 		PerformCheckCredentialAsyncTask task = new PerformCheckCredentialAsyncTask(this, credential);
 		task.execute();
 		progressDialog.setOnCancelListener(new CancelTaskOnCancelListener(task));
     }
 
     public void registerDevice(Credential credential) {
     	showProgressDialog(ProgressDialogHandler.LOGGING_IN);
 		PerformRegisterDeviceAsyncTask task = new PerformRegisterDeviceAsyncTask(this, credential);
 		task.execute();
 		progressDialog.setOnCancelListener(new CancelTaskOnCancelListener(task));
     }
     
     public void openMainActivity(boolean allowed) {
     	if (allowed) {
     		Intent mainIntent = new Intent(this, MainActivity.class);
     		startActivity(mainIntent);
     		this.finish();
     	}
     	else {
     		Utilities.longToast(this, "Credential not recognised, please login again");
     	}
 		dismissProgressDialog();
     }
     
     public void saveCredential(Credential credential) {
     	Utilities.saveCredential(this, credential);
     	dismissProgressDialog();
     }
     
     public void showProgressDialog(int type) {
 		progressDialog = new ProgressDialogHandler(this);
 		progressDialog.createProgressDialog(type);
     	if (progressDialog != null) progressDialog.show();
     }
     
     public void dismissProgressDialog() {
     	if (progressDialog != null) progressDialog.dismiss();
     }
 }

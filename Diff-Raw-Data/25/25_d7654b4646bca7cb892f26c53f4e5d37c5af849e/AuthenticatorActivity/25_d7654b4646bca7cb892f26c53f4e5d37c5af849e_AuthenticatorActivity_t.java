 package com.four_envelope.android.activity;
 
 import com.four_envelope.android.R;
 import com.four_envelope.android.operation.AuthorizeOperation;
 import com.four_envelope.android.operation.UpdateListener;
 import com.four_envelope.android.store.StoreClient;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.View;
 import android.view.Window;
 import android.widget.EditText;
 import android.widget.TextView;
 
 /**
  * Activity which displays login screen to the user.
  */
 public class AuthenticatorActivity extends Activity implements UpdateListener {
 
 	private AuthorizeOperation mAuthorizeOperation;
     
     private TextView mMessage;
 
     private String mUsername, mPassword;
     private EditText mUsernameEdit, mPasswordEdit;
 
 
     @Override
     public void onCreate(Bundle icicle) {        
         super.onCreate(icicle);
 
         mAuthorizeOperation = new AuthorizeOperation(this);
         
         mUsername = StoreClient.getLogin();
         
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 
         setContentView(R.layout.login_activity);
         
         mUsernameEdit = (EditText) findViewById(R.id.username_edit);
         mPasswordEdit = (EditText) findViewById(R.id.password_edit);
 
        mUsernameEdit.setText(mUsername);
        mPasswordEdit.setText(StoreClient.getPassword());
 
         mMessage = (TextView) findViewById(R.id.message);
         mMessage.setText(getMessage());
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         final ProgressDialog dialog = new ProgressDialog(this);
         
         dialog.setMessage(getText(R.string.ui_activity_authenticating));
         dialog.setIndeterminate(true);
         dialog.setCancelable(true);
         
         dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
             public void onCancel(DialogInterface dialog) {
             	mAuthorizeOperation.cancel();
             }
         });
         
         return dialog;
     }
     
     /**
      * Handles onClick event on the Submit button. Sends username/password to
      * the server for authentication.
      * 
      * @param view The Submit button for which this method is invoked
      */
     public void handleLogin(View view) {
         mUsername = mUsernameEdit.getText().toString();
         mPassword = mPasswordEdit.getText().toString();
 
         if (TextUtils.isEmpty(mUsername) || TextUtils.isEmpty(mPassword)) {
             mMessage.setText(getMessage());
         } else {
         	showProgress();
 
             // Start authenticating...
             mAuthorizeOperation.execute( mUsername, mPassword );
         }
     }
 
     /**
      * 
      * Called when response is received from the server for authentication request. 
      * Sets the AccountAuthenticatorResult which is sent back to the caller. 
      * @param the confirmCredentials result.
      */
     protected void finishLogin() {
         final Intent intent = new Intent();
         setResult(RESULT_OK, intent);
         finish();
     }
 
     /**
      * Returns the message to be displayed at the top of the login dialog box.
      */
     private CharSequence getMessage() {
         // If no username, then we ask the user to log in using an appropriate service.
         if (TextUtils.isEmpty(mUsername))
         	return getText(R.string.new_account_text);
 
         // We have an account but no password
         if (TextUtils.isEmpty(mPassword))
             return getText(R.string.login_fail_text_password_missing);
 
         return null;
     }
 
     /**
      * Shows the progress UI for a lengthy operation.
      */
     protected void showProgress() {
         showDialog(0);
     }
     
     /**
      * Hides the progress UI for a lengthy operation.
      */
     protected void hideProgress() {
         dismissDialog(0);
     }
     
 	@Override
 	public void onUpdate() {
 		if ( mAuthorizeOperation.isInProgress() )
 			return; 
 		else
 			hideProgress();
 
		if (!mAuthorizeOperation.isComplited() && !mAuthorizeOperation.isSuccessLogin) {
 			mMessage.setText( getText(R.string.login_fail_text_password_only) );
			StoreClient.logout();			
		}
 		
 		// Called when the authentication process completes.
 		if (mAuthorizeOperation.isComplited() && mAuthorizeOperation.isSuccessLogin)
 			finishLogin();
 	}
 
 	@Override
 	public Context getUpdateContext() {
 		return getApplicationContext();
 	}
 
 }

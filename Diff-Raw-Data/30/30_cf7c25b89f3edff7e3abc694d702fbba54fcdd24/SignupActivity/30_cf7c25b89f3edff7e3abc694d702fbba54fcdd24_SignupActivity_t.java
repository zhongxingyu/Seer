 package mx.ferreyra.dogapp;
 
 import java.io.IOException;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 
 public class SignupActivity extends Activity {
 
 	private EditText emailValueField;
 	private EditText passwordValueField;
 	private EditText passwordConfirmationValueField;
 
 	public static final int PASSWORD_MIN_SIZE = 3;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_signup);
 
 		emailValueField = (EditText)findViewById(R.id.email_value);
 		passwordValueField = (EditText)findViewById(R.id.password_value);
 		passwordConfirmationValueField = (EditText)findViewById(R.id.password_confirmation_value);
 	}
 
 	public void onClickSignup(View view) {
 		if(!validateEmail()) {
 			// TODO notify user that email is not valid
 		} else if(!validatePassword()) {
 			// TODO notify user that password is not valid
 		}
 
 		Signup task = new Signup(this);
 		String[] args = {
 				emailValueField.getText().toString(),
 				passwordValueField.getText().toString() };
 		task.execute(args);
 	}
 
 	public void onClickSignupWithFacebook(View view) {
 		// TODO implement this method
 	}
 
 	private void dispatchResult(Integer result) {

 		Intent intent = new Intent();
         intent.putExtra("ID_USER", result);

 		setResult(RESULT_OK, intent);
 		finish();

 		/*
 		if(result < 0) {
 			// -1 => Some data wrong
 			// -2 => User registered
 			// -3 => Server error
 			UI.showAlertDialog(getString(R.string.ups),
 					getString(R.string.an_error_ocurred),
 					getString(android.R.string.ok),
 					this, null);
 		} else {
 			// ID user
 			// TODO select an intent
 			UI.showToast("ID => " + result, this);
 		}
 		*/
 	}
 
 	private boolean validateEmail() {
 		String email = emailValueField.getText().toString();
 		return email.length() > 5;
 	}
 
 	private boolean validatePassword() {
 		String password = passwordValueField.getText().toString();
 		String confirmation = passwordConfirmationValueField.getText().toString();
 
 		if(password == null || password.trim().length() < PASSWORD_MIN_SIZE)
 			return false;
 
 		return password.equals(confirmation);
 	}
 
 	protected class Signup extends AsyncTask<String, Integer, Integer> {
		private final Context context;
 		private ProgressDialog dialog;
 
 		public Signup(Context context) {
 			this.context = context;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			dialog = new ProgressDialog(context);
 			dialog.setMessage(context.getString(R.string.please_wait_signing_up));
 			dialog.show();
 		}
 
 		@Override
 		protected Integer doInBackground(String... params) {
			WsDogUtils ws = new WsDogUtils();
 			try {
 				return ws.insertUsers(params[0],params[1],false);
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (XmlPullParserException e) {
 				e.printStackTrace();
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Integer result) {
 			super.onPostExecute(result);
 			dialog.dismiss();
 			dispatchResult(result);
 		}
 	}
 }

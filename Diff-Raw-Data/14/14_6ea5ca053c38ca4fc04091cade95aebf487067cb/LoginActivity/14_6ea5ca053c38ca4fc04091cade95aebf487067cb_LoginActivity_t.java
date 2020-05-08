 package de.fhb.maus.android.todolist;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.util.ArrayList;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.PopupWindow;
 import android.widget.TextView;
 import android.widget.Toast;
 import de.fhb.maus.android.todolist.database.CustomHttpClient;
 import de.fhb.maus.android.todolist.database.IO;
 import de.fhb.maus.android.todolist.database.Timestamps;
 import de.fhb.maus.android.todolist.date.MillisecondToDate;
 import de.fhb.maus.android.todolist.helpers.PATHs;
 import de.fhb.maus.android.todolist.server.ServerAvailability;
 import de.fhb.maus.android.todolist.validator.EmailValidator;
 
 public class LoginActivity extends Activity {
 
 	private Button mLogIn, mLogInLocal;
 	private boolean toastAlreadyShown = false;
 	private EditText mEmailField, mPwField;
 	private TextView mError, mServerAvailability, mServerTimestamp,
 			mDeviceTimestamp;
 	private EmailValidator mEv;
 	private PopupWindow pw;
 	private String phpAddress = "http://10.0.2.2/login/login.php",
 			serverAddress = "10.0.2.2";
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.login);
 		mEmailField = (EditText) findViewById(R.id.editTextEmail);
 		mPwField = (EditText) findViewById(R.id.editTextPassword);
 		mLogIn = (Button) findViewById(R.id.buttonLogin);
 		mLogIn.setEnabled(false);
 		mError = (TextView) findViewById(R.id.textViewError);
 		mServerAvailability = (TextView) findViewById(R.id.textViewServerAvailability);
 		mServerTimestamp = (TextView) findViewById(R.id.textViewServerTimestamp);
 		mDeviceTimestamp = (TextView) findViewById(R.id.textViewDeviceTimestamp);
 		mLogInLocal = (Button) findViewById(R.id.buttonLoginLocal);
 
		// Timestamps.createTimestampOnDevice();
 		// Timestamps.deleteTimestampOnDevice();
 		// Timestamps.importTimestampFromServer();
		// Timestamps.exportTimestampToServer();
 		// Timestamps.getTimestampFromDevice();
 		// Timestamps.getTimestampFromServer();
 
 		mServerTimestamp.setText(MillisecondToDate.getDate(Long
 				.valueOf(Timestamps.getTimestampFromServer())));
 		// mDeviceTimestamp.setText(MillisecondToDate.getDate(Long
 		// .valueOf(Timestamps.getTimestampFromDevice())));
//		System.out.println(Timestamps.getTimestampFromDevice());
 
 		// set a watcher on Email and Password
 		TextWatcher watcher = new LocalTextWatcher();
 		mEmailField.addTextChangedListener(watcher);
 		mPwField.addTextChangedListener(watcher);
 
 		if (ServerAvailability.isReachable(serverAddress))
 			mServerAvailability.setText("Server online!");
 		else
 			mServerAvailability.setText("Server offline!");
 
 		mEmailField.setOnKeyListener(new OnKeyListener() {
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if ((event.getAction() == KeyEvent.ACTION_DOWN)
 						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
 					String email = mEmailField.getText().toString();
 					mEv = new EmailValidator();
 					if (!mEv.validate(email)) {
 						Toast.makeText(getApplicationContext(),
 								getResources().getString(R.string.login_toast),
 								Toast.LENGTH_LONG).show();
 					}
 					return true;
 				}
 				return false;
 			}
 		});
 
 		mPwField.setOnKeyListener(new OnKeyListener() {
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if ((event.getAction() == KeyEvent.ACTION_DOWN)
 						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
 					String pw = mPwField.getText().toString();
 					if (pw.length() != 6) {
 						Toast.makeText(
 								getApplicationContext(),
 								getResources().getString(
 										R.string.passwort_to_short),
 								Toast.LENGTH_LONG).show();
 					}
 					return true;
 				}
 				return false;
 			}
 		});
 
 		mLogIn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				String username = mEmailField.getText().toString();
 				String password = mPwField.getText().toString();
 				ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
 				postParameters.add(new BasicNameValuePair("name", username));
 				postParameters.add(new BasicNameValuePair("pw", password));
 				String response = null;
 				try {
 					response = CustomHttpClient.executeHttpPost(phpAddress,
 							postParameters);
 					String res = response.toString();
 					res = res.replaceAll("\\s+", "");
 					if (res.equals("1")) {
 						mError.setText("Login accepted");
 						Toast.makeText(
 								getApplicationContext(),
 								getResources().getString(
 										R.string.login_hint_download),
 								Toast.LENGTH_LONG).show();
 						try {
 							IO.importDatabase();
 						} catch (IllegalStateException e1) {
 							e1.printStackTrace();
 						} catch (MalformedURLException e1) {
 							e1.printStackTrace();
 						} catch (ProtocolException e1) {
 							e1.printStackTrace();
 						} catch (IOException e1) {
 							e1.printStackTrace();
 						}
 						startActivity(new Intent(LoginActivity.this,
 								TodoListActivity.class));
 					} else {
 						mError.setText("Login not accepted");
 						mLogIn.setEnabled(false);
 					}
 				} catch (Exception e) {
 					Log.e("Database", e.toString());
 				}
 			}
 		});
 
 		mLogInLocal.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				initiatePopupWindow();
 
 			}
 		});
 	}
 
 	private void updateButtonState() {
 		mEv = new EmailValidator();
 		boolean enabled = mEv.validate(mEmailField.getText().toString())
 				&& checkEditText(mPwField);
 		mLogIn.setEnabled(enabled);
 	}
 	private boolean checkEditText(EditText edit) {
 		if (edit.getText().toString().length() == 6)
 			return true;
 		return false;
 	}
 
 	private class LocalTextWatcher implements TextWatcher {
 
 		@Override
 		public void afterTextChanged(Editable arg0) {
 			updateButtonState();
 			mError.setText("");
 		}
 
 		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count,
 				int after) {
 		}
 
 		@Override
 		public void onTextChanged(CharSequence s, int start, int before,
 				int count) {
 		}
 
 	}
 
 	private void initiatePopupWindow() {
 
 		LayoutInflater inflater = (LayoutInflater) LoginActivity.this
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View layout = inflater.inflate(R.layout.popup_window,
 				(ViewGroup) findViewById(R.id.popup_element));
 		pw = new PopupWindow(layout, 100, 100, true);
 		pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
 		Button cancelButton = (Button) layout
 				.findViewById(R.id.end_data_send_button);
 		cancelButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				pw.dismiss();
 				startActivity(new Intent(LoginActivity.this,
 						TodoListActivity.class));
 			}
 		});
 
 	}
 }

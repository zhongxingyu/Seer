 package fr.insalyon.pyp.gui.login;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import fr.insalyon.pyp.R;
 import fr.insalyon.pyp.gui.common.BaseActivity;
 import fr.insalyon.pyp.gui.common.popup.Popups;
 import fr.insalyon.pyp.network.ServerConnection;
 import fr.insalyon.pyp.tools.AppTools;
 import fr.insalyon.pyp.tools.Constants;
 import fr.insalyon.pyp.tools.PYPContext;
 import fr.insalyon.pyp.tools.TerminalInfo;
 
 public class RegisterActivity extends BaseActivity {
 	private LinearLayout abstractView;
 	private LinearLayout mainView;
 	private Button registerButton;
 	private Button cancelButton;
 	private TextView usernameField;
 	private TextView passwordField;
 	private TextView repasswrodField;
 	private TextView emailField;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState, Constants.REGISTER_CONST);
 		AppTools.info("on create RegisterActivity");
 		initGraphicalInterface();
 	}
 
 	private void initGraphicalInterface() {
 		// set layouts
 		LayoutInflater mInflater = LayoutInflater.from(this);
 		abstractView = (LinearLayout) findViewById(R.id.abstractLinearLayout);
 		mainView = (LinearLayout) mInflater.inflate(R.layout.register_activity,
 				null);
 		abstractView.addView(mainView);
 		registerButton = (Button) findViewById(R.id.Register_buttonConfirm);
 		usernameField = (TextView) findViewById(R.id.Register_UserName);
 		passwordField = (TextView) findViewById(R.id.Register_Password);
 		emailField = (TextView) findViewById(R.id.Register_Email);
 		repasswrodField = (TextView) findViewById(R.id.Register_RePassword);
 		Drawable background = ((LinearLayout) findViewById(R.id.register_backgorund))
 				.getBackground();
 		background.setAlpha(95);
 		hideHeader(false);
 		registerButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if (usernameField.getText().toString().equals("")
 						|| passwordField.getText().toString().equals("")
 						|| emailField.getText().toString().equals("")) {
 					Popups.showPopup(Constants.IncompleatData);
 					return;
 				}
 				if (passwordField.getText().toString()
 						.equals(repasswrodField.getText().toString()) == false) {
 					Popups.showPopup(Constants.passwordsDontMatch);
 					return;
 				}
 				if (passwordField.getText().length() < 4) {
 					Popups.showPopup(Constants.passwordTooShort);
 					return;
 				}
				if (!AppTools.isEmailValid(emailField.getText().toString())) {
 					Popups.showPopup(Constants.InvalidEmail);
 					return;
 				}
				new RegisterTask().execute();
 			}
 		});
 	}
 
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 
 	private class RegisterTask extends AsyncTask<Void, Void, Void> {
 
 		ProgressDialog mProgressDialog;
 		JSONObject res;
 
 		@Override
 		protected void onPostExecute(Void result) {
			//mProgressDialog.dismiss();
			
 			if (res != null) {
 				try {
 					if (res.has("error")) {
 						// Error
 						String error;
 						error = res.getString("error");
 						if (error.equals("Invalid email")) {
 							Popups.showPopup(Constants.InvalidEmail);
 						}
 						if (error.equals("Password too short")) {
 							Popups.showPopup(Constants.passwordTooShort);
 						}
 						if (error.equals("Email already used")) {
 							Popups.showPopup(Constants.EmailAlreadyUsed);
 						}
 						if (error.equals("User already registered")) {
 							Popups.showPopup(Constants.UsernameAlreadyUsed);
 						}
 						if (error.equals("Incomplete data")) {
 							Popups.showPopup(Constants.IncompleatData);
 						}
 					}
 
 					else {
 						String[] params = new String[2];
 						params[0] = usernameField.getText().toString();
 						params[1] = passwordField.getText().toString();
 						Intent intent = new Intent(PYPContext.getContext(),
 								RegisterActivity.class);
 						intent.putExtra(Constants.PARAMNAME,params);
 						setResult(RESULT_OK,intent);
 						RegisterActivity.this.finish();
 					}
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		@Override
 		protected void onPreExecute() {
			//mProgressDialog = ProgressDialog.show(RegisterActivity.this,
			//		getString(R.string.app_name), getString(R.string.loading));
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			// Send request to server for login
 			ServerConnection srvCon = ServerConnection.GetServerConnection();
 			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
 			parameters.add(new BasicNameValuePair("username", usernameField
 					.getText().toString()));
 			parameters.add(new BasicNameValuePair("password", passwordField
 					.getText().toString()));
 			parameters.add(new BasicNameValuePair("email", emailField.getText()
 					.toString()));
 			try {
 				res = srvCon.connect(ServerConnection.REGISTER, parameters);
 			} catch (Exception e) {
				if (e.getMessage()!= null && e.getMessage().equals("403")) {
 					SharedPreferences settings = getSharedPreferences(
 							Constants.TAG, 0);
 					settings.edit().remove("auth_token");
 				} else {
 					e.printStackTrace();
 				}
 			}
 			return null;
 		}
 	}
 }

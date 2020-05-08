 package moses.client;
 
 import moses.client.com.ConnectionParam;
 import moses.client.com.NetworkJSON;
 import moses.client.com.NetworkJSON.BackgroundException;
 import moses.client.com.ReqTaskExecutor;
 import moses.client.com.requests.RequestLogin;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 /**
  * This activity is the main activity of our app
  * providing login etc.
  * @author Jaco
  *
  */
 public class MosesActivity extends Activity {
 	private class ReqClass implements ReqTaskExecutor {
 
 		@Override
 		public void handleException(Exception e) {
 			txtSuccess.setText("FAILURE: " + e.getMessage());
 		}
 
 		@Override
 		public void postExecution(String s) {
 			JSONObject j = null;
 			try {
 				j = new JSONObject(s);
 				if (RequestLogin.loginValid(j, txtUname.getText().toString())) {
 					Intent logoutDialog = new Intent(view,
 							LoggedInViewActivity.class);
 					startActivity(logoutDialog);
 				} else {
 					txtSuccess.setText("NOT GRANTED: " + j.toString());
 				}
 			} catch (JSONException e) {
 				this.handleException(e);
 			}
 		}
 
 		@Override
 		public void updateExecution(BackgroundException c) {
 			if (c.c != ConnectionParam.EXCEPTION) {
 				txtSuccess.setText(c.c.toString());
 			} else {
 				handleException(c.e);
 			}
 		}
 	}
 	private EditText txtUname;
 
 	private EditText txtPW;
 
 	private TextView txtSuccess;
 	private Button btnconnect;
 
 	private Button btnExit;
 	private CheckBox chkLoginAuto;
 
 	private CheckBox chkSaveUnamePW;
 
 	private SharedPreferences settings;
 
 	private Activity view = this;
 
 	private void connect() {
 		SharedPreferences.Editor editor = settings.edit();
 		if (chkSaveUnamePW.isChecked()) {
 			editor.putString("uname", txtUname.getText().toString());
 			editor.putString("password", txtPW.getText().toString());
 		}
 		editor.putBoolean("loginauto", chkLoginAuto.isChecked());
 		editor.putBoolean("saveunamepw", chkSaveUnamePW.isChecked());
 		editor.commit();
 		RequestLogin r = new RequestLogin(new ReqClass(), txtUname.getText()
 				.toString(), txtPW.getText().toString());
 		r.send();
 	}
 
 	private void initControls() {
 		txtUname = (EditText) findViewById(R.id.uname);
 		txtPW = (EditText) findViewById(R.id.pword);
 
 		txtSuccess = (TextView) findViewById(R.id.success);
 
 		chkLoginAuto = (CheckBox) findViewById(R.id.loginauto);
 		chkSaveUnamePW = (CheckBox) findViewById(R.id.saveunamepw);
 
 		btnconnect = (Button) findViewById(R.id.connect_button);
 		btnconnect.setOnClickListener(new Button.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				connect();
 			}
 		});
 
 		btnExit = (Button) findViewById(R.id.exitbutton);
 		btnExit.setOnClickListener(new Button.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				finish();
 			}
 		});
 	}
 
 	private void loadConfig() {
 		settings = getSharedPreferences("MoSeS.cfg", 0);
 		txtUname.setText(settings.getString("uname", ""));
 		txtPW.setText(settings.getString("password", ""));
 		NetworkJSON.url = settings.getString("url",
				"http://212.72.183.71/moses/test.php");
 		chkLoginAuto.setChecked(settings.getBoolean("loginauto", false));
 		chkSaveUnamePW.setChecked(settings.getBoolean("saveunamepw", false));
 	}
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		initControls();
 		loadConfig();
 		if (chkLoginAuto.isChecked())
 			connect();
 	}
 
 }

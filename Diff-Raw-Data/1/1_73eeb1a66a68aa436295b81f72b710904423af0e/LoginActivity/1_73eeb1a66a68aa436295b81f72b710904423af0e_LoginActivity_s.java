 package activity.login;
 
 import util.LoginUtil;
 import com.ebay.ebayfriend.R;
 import activity.MainActivity;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.*;
 
 public class LoginActivity extends Activity {
 	
 	private EditText etUsername, etPassword;
 	private MyHandler myHandler;
 	private Button btLogin;
 
 	private LoginUtil loginUtil;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		loginUtil = new LoginUtil(getFilesDir().toString());
 		myHandler = new MyHandler();
 		
 		if (loginUtil.isSessionExist()) {
 			String password = loginUtil.getPassword();
 			String username = loginUtil.getUsername();
 			Thread checkThread = new CheckSessionThread(password, username);
 			checkThread.start();
 //			startMainActivity();
 		} else {
 			setContentView(R.layout.login);
 			initView();
 		}
 	}
 
 	protected void initView() {
 		etUsername = (EditText) findViewById(R.id.login_username);
 		etPassword = (EditText) findViewById(R.id.login_password);
 		btLogin = (Button) findViewById(R.id.login_btLogin);
 		btLogin.setOnClickListener(loginClick);
 		Button sbButton = (Button) findViewById(R.id.login_btSB);
 		sbButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				startMainActivity();
 			}
 
 		});
 	}
 
 	OnClickListener loginClick = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			String username = etUsername.getText().toString();
 			String password = etPassword.getText().toString();
 			Thread checkThread = new CheckSessionThread(password, username);
 			checkThread.start();
 
 		}
 
 	};
 
 	// start MainActivity
 	private void startMainActivity() {
 		Intent intent = new Intent(this, MainActivity.class);
 		intent.putExtra("NOTIFICATION", false);
 		startActivity(intent);
 		finish();
 	}
 
 	class CheckSessionThread extends Thread {
 		private String password, username;
 
 		public CheckSessionThread(String username, String password) {
 			this.password = password;
 			this.username = username;
 		}
 
 		public void run() {
 			boolean isSessionAvailable = loginUtil
 					.checkUser(username, password);
 			Message msg = new Message();
 			Bundle b = new Bundle();
 			b.putBoolean("isSessionAvailable", isSessionAvailable);
 			msg.setData(b);
 			Log.e("Thread","sessionAvalable"+isSessionAvailable);
 			LoginActivity.this.myHandler.sendMessage(msg);
 		}
 	}
 
 	class MyHandler extends Handler {
 		public MyHandler() {
 
 		}
 
 		public MyHandler(Looper l) {
 			super(l);
 		}
 
 		@Override
 		public void handleMessage(Message msg) {
 			super.handleMessage(msg);
 			Bundle b = msg.getData();
 			boolean isSessionAvailable = b.getBoolean("isSessionAvailable");
 			if (isSessionAvailable) {
 				// log in success, then send the session to MainActivity
 				Toast.makeText(LoginActivity.this.getApplicationContext(),
 						"Login Successfully", Toast.LENGTH_SHORT).show();
 
 				startMainActivity();
 			} else {
 				// log in failed, then inform user the error message
 				Toast.makeText(LoginActivity.this.getApplicationContext(),
 						"login failed", Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 }

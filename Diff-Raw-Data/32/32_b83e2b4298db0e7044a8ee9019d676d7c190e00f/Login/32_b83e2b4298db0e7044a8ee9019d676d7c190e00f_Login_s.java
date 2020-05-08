 package com.kmware.hrm;
 
 import com.kmware.hrm.R;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class Login extends ZActivity implements OnClickListener {
 
 	private static final String LOGTAG = Login.class.getSimpleName();
 
 	private static final String LOGIN_ADMIN = "admin";
 	private static final String LOGIN_GUEST = "guest";
 
 	private Button btn_Login;
 	private EditText et_Login;
 	private EditText et_Password;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		setContentView(R.layout.login);
 		showMenu = false;
 		super.onCreate(savedInstanceState);
 		init();
 	}
 
 	private void init() {
 
 		et_Login = (EditText) findViewById(R.id.etLogin);
 		et_Password = (EditText) findViewById(R.id.etPassword);
 		btn_Login = (Button) findViewById(R.id.btnLogin);
 		btn_Login.setOnClickListener(this);
 
 	}
 
 	@Override
 	public void onClick(View v) {
 
 		switch (v.getId()) {
 
 		case R.id.btnLogin:
 			if (checkLogin().equals(LOGIN_ADMIN)) {
 				Intent intent = new Intent(this, DashboardDesignActivity.class);
 				startActivity(intent);
 				finish();
 			} else if (checkLogin().equals(LOGIN_GUEST)) {
 				Intent intent = new Intent(this, InterviewGuest.class);
 				startActivity(intent);
				finish();
 			}
 
 			break;
 
 		}
 
 	}
 
 	private String checkLogin() {
 		if (et_Login.getText().toString().trim().length() == 0) {
 			getDialog().showError(getApplicationContext(),
 					getResources().getString(R.string.err_login));
 			return Extras.EMPTY_STRING;
 		}
 		if (et_Login.getText().toString().trim().equals(LOGIN_ADMIN)) {
 			return LOGIN_ADMIN;
 		} else {
 			return LOGIN_GUEST;
 
 		}
 	}
 
 }

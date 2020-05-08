 package com.eventosmanager;
 
 import com.facebook.LoggingBehavior;
 import com.facebook.Session;
 import com.facebook.SessionState;
 import com.facebook.Settings;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class LoginActivity extends Activity {
 	
 	public final String PREFERENCES_NAME = "mypreferences";
 	public final String IS_LOG = "isLogged";
 
 	private ImageButton backButton;
 	private Button loginlogoutButton;
 	private boolean isLogged = false;
 	private Session.StatusCallback callback = new Session.StatusCallback() {
 		
 		@Override
 		public void call(Session session, SessionState state, Exception exception) {
 			updateView();
 		}
 	};
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);	
 		setContentView(R.layout.login);
 		SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
 		
 		isLogged = settings.getBoolean(IS_LOG, false);
 		backButtonListener();
 
 		loginlogoutButton = (Button)findViewById(R.id.loginlogoutButton);
 		
 		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
 
 		Session session = Session.getActiveSession();
 		if (session == null) {
 			if (savedInstanceState != null) {
 				session = Session.restoreSession(this, null, callback, savedInstanceState);
 			}
 			session = new Session(this);
 			Session.setActiveSession(session);
 			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
 				session.openForRead(new Session.OpenRequest(this).setCallback(callback));
 			}
 		}
 		updateView();
 	}
 	
 	public void backButtonListener(){
 		SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
 		isLogged = settings.getBoolean(IS_LOG, false);
 		
 		backButton = (ImageButton) findViewById(R.id.backButton);
 		
 		backButton.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				if (isLogged == true) {
 					setResult(1);
 				}else {
 					setResult(-1);
 				}
 				finish();
 			}
 		});
 	}
 	
 	@Override
 	public void onBackPressed() {
 		SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
 		isLogged = settings.getBoolean(IS_LOG, false);
 		if (isLogged == true) {
 			setResult(1);
 		}else {
 			setResult(-1);
 		}
 		finish();
 	}
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 		Session.getActiveSession().addCallback(callback);
 	}
 	
 	@Override
 	public void onStop() {
 		super.onStop();
 		Session.getActiveSession().removeCallback(callback);
 	}
 		
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
 	}
 		
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		Session session = Session.getActiveSession();
 		Session.saveSession(session, outState);
 	}
 	
 	private void updateView() {
 		SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
 		Editor edit = settings.edit();
         Session session = Session.getActiveSession();
         if (session.isOpened()) {
         	settings.edit().putBoolean(IS_LOG, true);
         	loginlogoutButton.setText(R.string.logoutButton);
             loginlogoutButton.setOnClickListener(new OnClickListener() {
             public void onClick(View view) { onClickLogout(); }
             });
         } else {
         	settings.edit().putBoolean(IS_LOG, false);
             loginlogoutButton.setText(R.string.loginButton);
             loginlogoutButton.setOnClickListener(new OnClickListener() {
             public void onClick(View view) { onClickLogin(); }
             });
         }
         edit.commit();
 	}
 	
 	private void onClickLogin() {
 		SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
 		Editor edit = settings.edit();
         Session session = Session.getActiveSession();
         if (!session.isOpened() && !session.isClosed()) {
             session.openForRead(new Session.OpenRequest(this).setCallback(callback));
             settings.edit().putBoolean(IS_LOG, true);
         } else {
             Session.openActiveSession(this, true, callback);
             settings.edit().putBoolean(IS_LOG, true);
         }
         edit.commit();
     }
 
     private void onClickLogout() {
     	SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
         Session session = Session.getActiveSession();
         if (!session.isClosed()) {
             session.closeAndClearTokenInformation();
            settings.edit().putBoolean(IS_LOG, false).commit();
            //settings.edit().clear().commit();
         }
     }
         
 }

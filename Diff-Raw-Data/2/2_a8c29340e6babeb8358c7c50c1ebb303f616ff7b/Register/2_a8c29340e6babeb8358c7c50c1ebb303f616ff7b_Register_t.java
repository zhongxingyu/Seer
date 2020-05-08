 package com.chess.genesis;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Toast;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class Register extends Activity implements OnTouchListener, OnClickListener, OnLongClickListener
 {
 	private static Register self;
 
 	private NetworkClient net;
 
 	private Handler handle = new Handler()
 	{
 		public void handleMessage(Message msg)
 		{
 			JSONObject json = (JSONObject) msg.obj;
 
 			try {
 				if (json.getString("result").equals("error")) {
 					Toast.makeText(self, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 					return;
 				}
 				Toast.makeText(getApplication(), json.getString("reason"), Toast.LENGTH_LONG).show();
 				
 				Editor settings = PreferenceManager.getDefaultSharedPreferences(self).edit();
 				settings.putBoolean("isLoggedIn", true);
 				settings.putString("username", json.getString("username"));
 				settings.putString("passhash", json.getString("passhash"));
 
 				settings.commit();
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		self = this;
 
 		// set only portrait
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
 		// create network client instance
 		net = new NetworkClient(handle);
 
 		// set content view
 		setContentView(R.layout.register);
 
 		// setup click listeners
 		ImageView image = (ImageView) findViewById(R.id.register);
 		image.setOnTouchListener(this);
 		image.setOnClickListener(this);
 
 		image = (ImageView) findViewById(R.id.topbar);
 		image.setOnTouchListener(this);
 		image.setOnLongClickListener(this);
 	}
 
 	public void onClick(View v)
 	{
 		switch (v.getId()) {
 		case R.id.register:
 			register_user();
 			break;
 		}
 	}
 
 	public boolean onLongClick(View v)
 	{
 		switch (v.getId()) {
 		case R.id.topbar:
 			finish();
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	public boolean onTouch(View v, MotionEvent event)
 	{
 		switch (v.getId()) {
 		case R.id.topbar:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				((ImageView) v).setImageResource(R.drawable.topbar_pressed);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				((ImageView) v).setImageResource(R.drawable.topbar);
 			break;
 		case R.id.register:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				((ImageView) v).setImageResource(R.drawable.rsubmit_pressed);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				((ImageView) v).setImageResource(R.drawable.rsubmit);
 			break;
 		}
 		return false;
 	}
 
 	private void register_user()
 	{
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 
 		EditText txt = (EditText) findViewById(R.id.username);
 		String username = txt.getText().toString();
 
 		txt = (EditText) findViewById(R.id.password);
 		String password = txt.getText().toString();
 
 		txt = (EditText) findViewById(R.id.password2);
 		String password2 = txt.getText().toString();
 
 		txt = (EditText) findViewById(R.id.email);
 		String email = txt.getText().toString();
 
 		if (!valid_username(username))
 			return;
 		if (!valid_password(password, password2))
 			return;
 		if (!valid_email(email))
 			return;
 
 		net.register(username, password, email);
 		(new Thread(net)).start();
 		Toast.makeText(this, "Connecting to server...", Toast.LENGTH_LONG).show();
 	}
 
 	private boolean valid_username(String name)
 	{
 		if (name.length() < 3) {
 			Toast.makeText(this, "Username too short", Toast.LENGTH_LONG).show();
 			return false;
 		} else if (!name.matches("[a-zA-Z0-9]+")) {
 			Toast.makeText(this, "Username can only contain letters or numbers", Toast.LENGTH_LONG).show();
 			return false;
 		}
 		return true;
 	}
 
 	private boolean valid_password(String pass1, String pass2)
 	{
 		if (!pass1.equals(pass2)) {
 			Toast.makeText(getApplication(), "Passwords don't match", Toast.LENGTH_LONG).show();
 			return false;
 		}
 		if (pass1.length() < 4) {
 			Toast.makeText(getApplication(), "Password too short", Toast.LENGTH_LONG).show();
 			return false;
 		}
 		for (int i = 0; i < pass1.length(); i++) {
 			if (pass1.charAt(i) < 32 || pass1.charAt(i) > 126) {
 				Toast.makeText(getApplication(), "Password can only contain ASCII characters", Toast.LENGTH_LONG).show();
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private boolean valid_email(String email)
 	{
		if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
 			Toast.makeText(this, "Invalid email address", Toast.LENGTH_LONG).show();
 			return false;
 		}
 		return true;
 	}
 }

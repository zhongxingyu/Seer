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
 
 public class Login extends Activity implements OnTouchListener, OnClickListener, OnLongClickListener
 {
 	private static Login self;
 
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
 				switch (msg.what) {
 				case NetworkClient.LOGIN:
 					Toast.makeText(self, json.getString("reason"), Toast.LENGTH_LONG).show();
 				
 					Editor settings = PreferenceManager.getDefaultSharedPreferences(self).edit();
 					settings.putBoolean("isLoggedIn", true);
 					settings.putString("username", json.getString("username"));
 					settings.putString("passhash", json.getString("passhash"));
 					settings.commit();
 
 					SyncGameList sync = new SyncGameList(self, handle, json.getString("username"));
 					sync.setFullSync(true);
 					(new Thread(sync)).start();
 
 					Toast.makeText(self, "Syncing active games", Toast.LENGTH_LONG).show();
 					break;
 				case SyncGameList.MSG:
 					Toast.makeText(self, "Syncing complete", Toast.LENGTH_LONG).show();
 					break;
 				}
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
 		setContentView(R.layout.login);
 
 		// setup click listeners
 		ImageView image = (ImageView) findViewById(R.id.login);
 		image.setOnTouchListener(this);
 		image.setOnClickListener(this);
 
 		image = (ImageView) findViewById(R.id.register);
 		image.setOnTouchListener(this);
 		image.setOnClickListener(this);
 
 		image = (ImageView) findViewById(R.id.topbar);
 		image.setOnTouchListener(this);
 		image.setOnLongClickListener(this);
 
 		// set text
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 
 		if (settings.getBoolean("isLoggedIn", false)) {
 			EditText txt = (EditText) findViewById(R.id.username);
 			txt.setText(settings.getString("username", ""));
 
 			txt = (EditText) findViewById(R.id.password);
 			txt.setText(settings.getString("password", ""));
 		}
 	}
 
 	public void onClick(View v)
 	{
 		switch (v.getId()) {
 		case R.id.login:
 			EditText txt = (EditText) findViewById(R.id.username);
 			String username = txt.getText().toString();
 
 			txt = (EditText) findViewById(R.id.password);
 			String password = txt.getText().toString();
 
 			net.login_user(username, password);
 			(new Thread(net)).start();
 			Toast.makeText(this, "Connecting to server...", Toast.LENGTH_LONG).show();
 			break;
 		case R.id.register:
 			startActivityForResult(new Intent(this, Register.class), 1);
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
 		case R.id.login:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
 				((ImageView) v).setImageResource(R.drawable.login_pressed);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
 				((ImageView) v).setImageResource(R.drawable.login);
 			break;
 		case R.id.register:
 			if (event.getAction() == MotionEvent.ACTION_DOWN)
				((ImageView) v).setImageResource(R.drawable.signup_pressed);
 			else if (event.getAction() == MotionEvent.ACTION_UP)
				((ImageView) v).setImageResource(R.drawable.signup);
 			break;
 		}
 		return false;
 	}
 
 	public void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 
 		if (settings.getBoolean("isLoggedIn", false)) {
 			EditText txt = (EditText) findViewById(R.id.username);
 			txt.setText(settings.getString("username", ""));
 
 			txt = (EditText) findViewById(R.id.password);
 			txt.setText(settings.getString("password", ""));
 		} else {
 			EditText txt = (EditText) findViewById(R.id.username);
 			txt.setText("");
 
 			txt = (EditText) findViewById(R.id.password);
 			txt.setText("");
 		}
 	}
 }

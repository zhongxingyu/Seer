 package com.chess.genesis;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Toast;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class Register extends BasePhoneActivity implements OnClickListener
 {
 	private Context context;
 	private NetworkClient net;
 	private ProgressMsg progress;
 
 	private final Handler handle = new Handler()
 	{
 		public void handleMessage(final Message msg)
 		{
 			switch (msg.what) {
 			case NetworkClient.REGISTER:
 				final JSONObject json = (JSONObject) msg.obj;
 
 				try {
 					if (json.getString("result").equals("error")) {
 						progress.remove();
 						Toast.makeText(context, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 						return;
 					}
 					progress.setText("Registration Successfull");
 					(new RegisterActivation(context, handle)).show();
 				} catch (JSONException e) {
 					e.printStackTrace();
 					throw new RuntimeException();
 				}
 				break;
 			case RegisterConfirm.MSG:
 				progress.setText("Sending Registration");
 				final Bundle data = (Bundle) msg.obj;
 
 				net.register(data.getString("username"), data.getString("password"), data.getString("email"));
 				(new Thread(net)).start();
 				break;
 			case RegisterActivation.MSG:
 				finish();
 				break;
 			}
 		}
 	};
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState, R.layout.activity_register);
 		context = this;
 
 		// create network client instance
 		net = new NetworkClient(this, handle);
 		progress = new ProgressMsg(this);
 
 		// setup click listeners
 		ImageView image = (ImageView) findViewById(R.id.register);
 		image.setOnClickListener(this);
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		NetActive.inc();
 		AdsHandler.run(this);
 	}
 
 	@Override
 	public void onPause()
 	{
 		NetActive.dec();
 		super.onPause();
 	}
 
 	public void onClick(final View v)
 	{
 		if (v.getId() == R.id.register)
 			register_validate();
 	}
 
 	private void register_validate()
 	{
 		EditText txt = (EditText) findViewById(R.id.username);
 		final String username = txt.getText().toString().trim();
 
 		txt = (EditText) findViewById(R.id.password);
 		final String password = txt.getText().toString();
 
 		txt = (EditText) findViewById(R.id.password2);
 		final String password2 = txt.getText().toString();
 
 		txt = (EditText) findViewById(R.id.email);
 		final String email = txt.getText().toString().trim();
 
 		if (!valid_username(username))
 			return;
 		if (!valid_password(password, password2))
 			return;
 		if (!valid_email(email))
 			return;
 
 		final Bundle bundle = new Bundle();
 		bundle.putString("username", username);
 		bundle.putString("password", password);
 		bundle.putString("email", email);
 
 		(new RegisterConfirm(this, handle, bundle)).show();
 	}
 
 	private boolean valid_username(final String name)
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
 
 	private boolean valid_password(final String pass1, final String pass2)
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
 
 	private boolean valid_email(final String email)
 	{
		if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
 			Toast.makeText(this, "Invalid email address", Toast.LENGTH_LONG).show();
 			return false;
 		}
 		return true;
 	}
 }

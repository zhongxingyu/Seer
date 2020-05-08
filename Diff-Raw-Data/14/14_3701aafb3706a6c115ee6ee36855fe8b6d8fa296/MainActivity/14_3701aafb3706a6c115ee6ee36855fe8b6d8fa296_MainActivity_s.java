 package com.bluetext.nextapp;
 
 import java.net.Socket;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import bigsky.BlueTextRequest;
 import bigsky.BlueTextResponse;
 import bigsky.Contact;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	
 	@SuppressWarnings("unused")
 	private final static String TAG = "AGG";
 	private static String phoneNumber;
 	static AsyncTask<String, Void, Socket> task;
 	static AsyncTask<String, String, String> sqlTask;
	static AsyncTask<Object, Void, ConcurrentLinkedQueue<Contact>> getAllContacts = null;
 	protected static Context ctx;
 	public static Contact userContact;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		SQLLoginActivity.ma = this;
 		sqlTask = null;
 		
 		ctx = this.getApplicationContext();		
 		getAllContacts = new GetAllContactsActivity().execute();
 		phoneNumber = ((TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
 		if(phoneNumber.length() == 11){
 			phoneNumber = phoneNumber.substring(1);
 		}
 		((EditText) findViewById(R.id.phoneNumberField)).setFocusable(false);
 		((EditText) findViewById(R.id.phoneNumberField)).setText(phoneNumber);
 		
 		// TODO get the actual name of the user
 		userContact = new Contact("Jonathan", "Mielke", phoneNumber, null);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 		
 	public void getLogin(View view)
 	{				
 		// Prevents user from spamming login button
 		if(sqlTask != null){
 			return;
 		}
 		
 		// Pull the username and password out of the login and password field
 		String password    = ((EditText) findViewById(R.id.passwordField)).getText().toString();
 		
 		// Remove any wildcard chars that were entered as input
 		password = password.replace('%', ' ');
 		password = password.replace('_', ' ');
 		
 		// Wipe out password on login attempt
 		((EditText) findViewById(R.id.passwordField)).setText("");
 				
 		/*
 		 * Starts an AsyncTask to verify the phone number and password for the 
 		 * data given.  If the number and password are correct, then the AsyncTask
 		 * will grab the most recent IP address for the account that has logged in
 		 * from the PC side client.
 		 * After this task completes, we kick off the checkLogin() method, which 
 		 * actually starts the ServerListener thread on the IP Address found earlier.
 		 */
 		sqlTask = new SQLLoginActivity().execute(phoneNumber, password);
 	}
 	
 	public void checkLogin(String loginResult)
 	{
 		if(loginResult.contains("Login Successful!")){
 			Toast.makeText(ctx, "Login Successful!", Toast.LENGTH_LONG).show();
 			
 			// Get the IP address of the PC client from the return string and open
 			// a socket in a new thread through this AsyncTask
 			String IPaddr = loginResult.substring(loginResult.indexOf('!') + 2);
 			task = new ServerListener().execute(IPaddr, Integer.toString(1300));
 			
 			Intent i = new Intent(getApplicationContext(), PostLoginActivity.class);
 			startActivity(i);
 		} 
 		else {
 			// Let user retry the login proccess
 			Toast.makeText(ctx, loginResult, Toast.LENGTH_LONG).show();
 			sqlTask = null;
 		}
 	}	
 }

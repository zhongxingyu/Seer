 package com.example.boilerbanker;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 
 public class MainActivity extends Activity {
 	
 	private static Client client;
 	
 	public void openOfflineView(View view) {
 		// Checks to see if OfflineView is enabled
 				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
 				boolean checked = sp.getBoolean("OFFLINE_CHECKBOX", false);
 				
 				/*
 				 * If checked is true, begins the new intent and opens the offline
 				 * view. If false, informs the user
 				 */
 				if (checked) {
 					Intent offlineIntent = new Intent(this, DisplayOfflineViewActivity.class);
 					startActivity(offlineIntent);
 				} else {
 					AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
 					alertDialog.setTitle("UH OH!");
 					alertDialog.setMessage("Offline View is not set!");
 					alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							return;
 						}
 					});
 					alertDialog.show();
 				}
 	}
 	
 	public void openWelcome(View view) throws IOException  {
 		// Alert message set up
 		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
 		alertDialog.setTitle("UH OH!");
 		alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				return;
 			}
 		});
 		
 		
 		// Saves username and password to strings
 		EditText userText = (EditText) findViewById(R.id.username_message);
 		EditText passText = (EditText) findViewById(R.id.password_message);
 		final String user = userText.getText().toString();
 		final String pass = passText.getText().toString();
 		
 		if (user.equals("") && pass.equals("")) {
 			alertDialog.setMessage("Please enter a username and password");
 			alertDialog.show();
 		} else if (user.equals("")) {
 			alertDialog.setMessage("Please enter a username");
 			alertDialog.show();
 		} else if (pass.equals("")) {
 			alertDialog.setMessage("Please enter a password");
 			alertDialog.show();
 		} else {
 			Intent welcomeIntent = new Intent(this, DisplayWelcomeActivity.class);
 			startActivity(welcomeIntent);
 			Thread thread = new Thread() {
 				public void run() {
 					getClient().sendUserCredentials(user, pass);
 				}
 			};
 			thread.start();
 			
 			
 		}
 	}
 	
 	public static Client makeClient() {
 		
 		return client;
 	}
 	
 	public static Client getClient() {
 		if (client == null) {
 			try {
				client = new Client("sslab07.cs.purdue.edu", 5003);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return client;
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }

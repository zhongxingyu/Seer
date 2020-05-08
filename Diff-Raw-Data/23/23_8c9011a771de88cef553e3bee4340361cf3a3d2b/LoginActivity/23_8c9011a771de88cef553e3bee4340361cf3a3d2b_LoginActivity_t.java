 package com.tim.stullich.drawerapp;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class LoginActivity extends FragmentActivity {
 
 	private Button loginButton;
 	private Spinner serverList;
 	private ImageButton addServerButton;
 	private Activity act;
 	private ArrayList<String> serverNames;
 	private ArrayAdapter<String> serverNamesAdapter;
 	private EditText userNameField;
 	private static final String USER_PREFS = "UserPrefs";
 	private static final String SERVER_NAMES = "serverNames";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 		
 		userNameField = (EditText) findViewById(R.id.username_edit_text);
 		
 		addServerButton = (ImageButton) findViewById(R.id.add_server_button);
 		addServerButton.setOnClickListener(new ServerAddOnClickListener(this));
 		
 		loginButton = (Button) findViewById(R.id.login_button);
 		loginButton.setOnClickListener(new LoginOnClickListener());
 		
 		serverList = (Spinner) findViewById(R.id.servers_spinner);
 		
 		act = this;
 		
 		addServersToSpinner();
 	}
 	
 	@Override
 	protected void onStop() {
 		super.onStop();
 		if (saveServerPreferences()) {
 			Log.i("OPAM", "Preferences saved successfully");
 		}
 		else {
 			Log.i("OPAM", "Preferences did not save");
 		}
 	}
 	
 	private void addServersToSpinner() {
 		loadServerPreferences();
 		serverNamesAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item,
 				serverNames);
 		serverNamesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		serverList.setAdapter(serverNamesAdapter);
 		serverList.setOnItemSelectedListener(new SpinnerItemSelectedListener());
 	}
 	
 	//TODO Fix this shit
 	private ArrayList<String> loadServerPreferences() {
 		SharedPreferences prefs = getSharedPreferences(USER_PREFS, 0);
 		int size = prefs.getInt(SERVER_NAMES + "_SIZE", 1);
 		if (size == 1) {
 			serverNames = new ArrayList<String>(size);
 			Log.i("OPAM", "Resource size " + size);
			serverNames.add(0, "No Server Selected...");
 			Log.i("OPAM", serverNames.toString());
 			return serverNames;
 		}
 		for (int i = 0; i < size; i++) {
 			serverNames.add(i, prefs.getString(SERVER_NAMES + "_" + i, null));
 		}
 		return serverNames;
 	}
 	
 	private boolean saveServerPreferences() {
 		SharedPreferences prefs = getSharedPreferences(USER_PREFS, 0);
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.putInt(SERVER_NAMES + "_SIZE", serverNames.size());
 		
 		for (int i = serverNames.size() == 1 ? 2 : serverNames.size(); i < i - 1; i++) {
 			editor.putString(SERVER_NAMES + "_" + i, serverNames.get(i));
 		}
 		
 		return editor.commit();
 	}
 	
 		private class LoginOnClickListener implements Button.OnClickListener {
 			
 			@Override
 			public void onClick(View v) {
 				switch (v.getId()) {
 				//TODO Implement actual login logic	
 				case R.id.login_button :
 						Intent i = new Intent(LoginActivity.this, MainActivity.class);
 						i.putExtra("userName", userNameField.getText().toString());
 						LoginActivity.this.startActivity(i);
 				}
 			}
 		}
 		
 		private class ServerAddOnClickListener implements ImageButton.OnClickListener{
 
 			private Context ctx;
 			private EditText urlField, portField;
 			private View view;
 			
 			public ServerAddOnClickListener(Context ctx) {
 				this.ctx = ctx;				
 			}
 
 			@Override
 			public void onClick(View v) {
 				switch (v.getId()) {
 				case R.id.add_server_button :
 					displayAddServerDialog();
 				}	
 			}
 
 			private void displayAddServerDialog() {
 				AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
 				
 				builder.setTitle(R.string.add_server_alert_title);
 				
 				view = act.getLayoutInflater().inflate(R.layout.add_server_dialog, null);
 				
 				urlField = (EditText) view.findViewById(R.id.alert_dialog_address_edit_text);
 				portField = (EditText) view.findViewById(R.id.alert_dialog_port_edit_text);
 				
 				builder.setView(view);
 								
 				builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						serverNames.add(urlField.getText().toString() 
 								+ ":" 
 								+ portField.getText().toString());
 						
 						serverNamesAdapter.notifyDataSetChanged();
 						
 						Toast.makeText(ctx, urlField.getText().toString() 
 								+ " "
 								+ portField.getText().toString(), 
 								Toast.LENGTH_SHORT).show();
 					}
 				});
 				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			               // User cancelled the dialog
 			           }
 			       });
 				
 				
 				builder.create().show();
 			}
 		}
 		
 		private class SpinnerItemSelectedListener implements OnItemSelectedListener {
 			
 			//TODO Also this
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view, int pos ,long id) {
 				Log.i("OPAM", "Item selected");
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				Log.i("OPAM", "Nothing selected");	
 			}
 		}
 }

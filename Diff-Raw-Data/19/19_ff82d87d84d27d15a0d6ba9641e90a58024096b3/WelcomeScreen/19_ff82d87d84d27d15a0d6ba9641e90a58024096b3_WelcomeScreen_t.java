 package no.group09.ucsoftwarestore;
 
 /*
  * Licensed to UbiCollab.org under one or more contributor
  * license agreements.  See the NOTICE file distributed 
  * with this work for additional information regarding
  * copyright ownership. UbiCollab.org licenses this file
  * to you under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import no.group09.utils.Devices;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 public class WelcomeScreen extends Activity {
 
 	private SharedPreferences sharedPref;
 	private ProgressDialog progressDialog;
 
 	private Button device, browse;
 	private CheckBox reconnectCheckBox;
 	private Context ctxt;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.intro_screen);
 		sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		ctxt = getBaseContext();
 
 		//Progressdialog used to indicate that the program is connecting to a BT device
 		progressDialog = new ProgressDialog(this);
 		progressDialog.setCancelable(false);
 
 		//Check preferences if we should reconnect to last connected device
 		if(sharedPref.getBoolean("reconnect_check_box", true)){
 			//Try to reconnect
 			reconnect();
 		}
 
 		device = (Button)findViewById(R.id.intro_device_list_button);
 		browse = (Button)findViewById(R.id.intro_browse_shop_button);
 		addButtonFunctionality();
 
 		reconnectCheckBox = (CheckBox) findViewById(R.id.reconnectbox);
 		reconnectCheckBox.setChecked(sharedPref.getBoolean("reconnect_check_box", true));
 		reconnectCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				Editor edit = sharedPref.edit();
 				edit.putBoolean("reconnect_check_box", isChecked);
 				edit.commit();
 			}
 		});
 	}
 
	/**
	 * Adds functionality to the buttons on the screen
	 */
 	private void addButtonFunctionality(){
 		device.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				startActivity(new Intent(ctxt, Devices.class));
 			}
 		});
 
 		browse.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				startActivity(new Intent(ctxt, MainActivity.class));
 			}
 		});
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		setActivityTitle();
 	}
 
 	/** Try to connect to the previous connected device if there is one */
 	public void reconnect(){
 		if(!Devices.isConnected()){
 			String deviceName = sharedPref.getString("connected_device_name", "null");
 			String deviceMac = sharedPref.getString("connected_device_mac", "null");
 
 			//If there is a last device in the preferences try to connect to it
 			if(!deviceName.equals("null") && !deviceMac.equals("null")){
 
 				Intent serviceIntent = new Intent(getApplicationContext(), no.group09.utils.BtArduinoService.class);
 				serviceIntent.putExtra(Devices.MAC_ADDRESS, deviceMac);
 
 				startService(serviceIntent);
 				new Reconnect().execute();
 			}
 		}
 	}
 	
 	/** Add the connected device name to the title if connected */
 	public void setActivityTitle(){
 		String appName = sharedPref.getString("connected_device_name", "null");
 
 		if(Devices.isConnected() && !appName.equals("null")){
 			setTitle("µC Software Store" + " - " + appName);
 		}
 		else{
 			setTitle("µC Software Store");
 		}
 	}
 
 	/** Help class for connection to last device */
 	public class Reconnect extends AsyncTask<Void, Void, Boolean> {
 
 		private long timeout;
 
 		@Override
 		protected void onPreExecute() {
 			timeout = System.currentTimeMillis() + 30000;
 			progressDialog.setMessage("Connecting, please wait... This might take " +
 					"up to 30 seconds.");
 			progressDialog.setCancelable(false);
 			progressDialog.show();
 		}
 
 		@Override
 		protected Boolean doInBackground(Void... params) {
 			while(true) {
 				if(Devices.isConnected()){
 					return true;
 				}
 
 				if (System.currentTimeMillis() > timeout) {
 					return false;
 				}
 				
 				else if (Devices.connectionHasFailed()) return false;
 
 				try { Thread.sleep(1);
 				} catch (InterruptedException e) {}
 			}
 		}      
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			String message = "";
 			if (result && Devices.isConnected()) {
 				String deviceName = sharedPref.getString("connected_device_name", "null");
 				
 				message = "Successfully connected to device: " + deviceName;
 			}
 			else{
 				message = "The connection was not successfull. Go to Device List to try again.";
 			}
 			setActivityTitle();
 
 			//Close the "trying to connect" dialog
 			progressDialog.dismiss();
 
 			//Open the connection status dialog
 			createDialog(message);
 
 		}
 	}
 
 	/** Dialog box that is used to show connection status after attempting to connect to a device */
 	public Dialog createDialog(String message) {
 		AlertDialog.Builder responseDialog = new AlertDialog.Builder(this);
 
 		responseDialog.setMessage(message)
 		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				setActivityTitle();
 			}
 		}).setCancelable(false);
 
 		return responseDialog.show();
 	}
 }

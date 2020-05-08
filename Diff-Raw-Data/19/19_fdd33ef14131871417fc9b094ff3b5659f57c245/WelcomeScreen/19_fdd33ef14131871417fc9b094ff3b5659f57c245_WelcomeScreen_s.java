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
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.widget.Toast;
 
 public class WelcomeScreen extends Activity {
 
 	private SharedPreferences sharedPref;
 	private ProgressDialog progressDialog;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.intro_screen);	//change this to wilhelms xml
 		sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		
 		//Progressdialog used to indicate that the program is connecting to a BT device
 		progressDialog = new ProgressDialog(this);
 		progressDialog.setCancelable(false);
 		
 		//Check preferences if we should reconnect to last connected device
		if(sharedPref.getBoolean("", true)){
 			//Try to reconnect
 			reconnect();
 		}
 		
 	}
 
 	/** Try to connect to the previous connected device if there was one */
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
 			else{
 				startActivity(new Intent(getBaseContext(), Devices.class));
 			}
 		}
 	}
 
 	/** Help class for connection to last device */
 	public class Reconnect extends AsyncTask<Void, Void, Boolean> {
 
 		private long timeout;
 
 		@Override
 		protected void onPreExecute() {
 			timeout = System.currentTimeMillis() + 3000;
 			progressDialog.setMessage("Connecting, please wait...");
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
 
 				try { Thread.sleep(1);
 				} catch (InterruptedException e) {}
 			}
 		}      
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			String message = "";
 			if (result && Devices.isConnected()) {
 				message = "Connected to " + sharedPref.getString("connected_device_name", "null");
 			}
 			else{
 				message = "Not connected to any device";
 			}
 //			setActivityTitle();
 			
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
 				startActivity(new Intent(getBaseContext(), Devices.class));
 			}
 		}).setCancelable(false);
 
 		return responseDialog.show();
 	}
 }

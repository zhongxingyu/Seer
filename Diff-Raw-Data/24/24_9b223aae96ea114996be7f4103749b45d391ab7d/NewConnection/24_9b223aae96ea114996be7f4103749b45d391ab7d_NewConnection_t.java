 /* Copyright 2009 Sean Hodges <seanhodges@bluebottle.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package tesla.app;
 
 import tesla.app.command.provider.AppConfigProvider;
 import tesla.app.connect.ConnectionOptions;
 import tesla.app.service.CommandService;
 import tesla.app.service.business.ICommandController;
 import tesla.app.task.ConnectToServerTask;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.DialogInterface.OnCancelListener;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 
 public class NewConnection extends Activity implements OnClickListener, ConnectToServerTask.OnConnectionListener {
 	
 	ConnectionOptions config;
 	private ProgressDialog progressDialog;
 	
 	private ICommandController commandService;
 	protected ConnectToServerTask connectTask;
 	
 	EditText hostText;
 	EditText portText;
 	EditText userText;
 	EditText passText;
 	RadioGroup appSelection;
 	
 	private ServiceConnection connection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			commandService = ICommandController.Stub.asInterface(service);
 			
 			// Connect to the server in the background
 			connectTask = new ConnectToServerTask();
 			connectTask.registerConnectionListener(NewConnection.this);
 			connectTask.execute(commandService);
 		}
 		
 		public void onServiceDisconnected(ComponentName name) {
 			commandService = null;
 		}
 	};
 	
     /* This is the new connection screen. */
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         setContentView(R.layout.new_connection);
         
         // Attach the button listeners
         View cancelButton = this.findViewById(R.id.cancel);
         cancelButton.setOnClickListener(this);
         View connectButton = this.findViewById(R.id.connect);
         connectButton.setOnClickListener(this);
         
         // Load the last saved settings
         config = new ConnectionOptions(this);
 		hostText = (EditText)this.findViewById(R.id.host);
 		portText = (EditText)this.findViewById(R.id.port);
 		userText = (EditText)this.findViewById(R.id.user);
 		passText = (EditText)this.findViewById(R.id.pass);
 		appSelection = (RadioGroup)this.findViewById(R.id.initial_app);
 		hostText.setText(config.hostname);
 		portText.setText(String.valueOf(config.port));
 		userText.setText(config.username);
 		passText.setText(config.password);
 		if (!config.appSelection.equals("")) appSelection.check(findAppMatchingName(config.appSelection));
 		
 		// Stop any existing command service
 		stopService(new Intent(NewConnection.this, CommandService.class));
     }
     
 	private int findAppMatchingName(String appSelectionText) {
 		for (int i = 0; i < appSelection.getChildCount(); i++) {
 			RadioButton item = (RadioButton)appSelection.getChildAt(i);
 			String itemText = item.getText().toString();
 			if (itemText.equalsIgnoreCase(appSelectionText)) {
 				return item.getId();
 			}
 		}
 		return -1;
 	}
 
 	protected void onDestroy() {
 		super.onDestroy();
 		stopService(new Intent(NewConnection.this, CommandService.class));
 	}
 	
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.connect:
 			startConnection();
 			break;
 		case R.id.cancel:
 			finish();
 			break;
 		}
 	}
 	
 	private void setConnectionConfig() {
 		config.hostname = hostText.getText().toString();
 		config.port = Integer.parseInt(portText.getText().toString());
 		config.username = userText.getText().toString();
 		config.password = passText.getText().toString();
 		
 		// Determine which application was selected
 		RadioButton currentSelection = (RadioButton)findViewById(appSelection.getCheckedRadioButtonId());
 		String selectionText = currentSelection.getText().toString();
 		if (selectionText.equalsIgnoreCase(AppConfigProvider.APP_RHYTHMBOX)) {
 			config.appSelection = AppConfigProvider.APP_RHYTHMBOX;
 		}
 		else if (selectionText.equalsIgnoreCase(AppConfigProvider.APP_AMAROK)) {
 			config.appSelection = AppConfigProvider.APP_AMAROK;
 		}
 		else if (selectionText.equalsIgnoreCase(AppConfigProvider.APP_VLC)) {
 			config.appSelection = AppConfigProvider.APP_VLC;
 		}
 		
 		// Check the input
 		if (config.port == 0) config.port = 22;
 		
 		// Save the settings for next time
 		config.saveSettings();
 	}
 	
 	private void startConnection() {
 		setConnectionConfig();
 		
 		OnCancelListener cancelListener = new OnCancelListener() {
 			public void onCancel(DialogInterface dialog) {
 				// Connection was cancelled by user
 				onConnectionCancelled();
 			}
 		};
 		progressDialog = ProgressDialog.show(
 			this, 
 			getString(R.string.connection_progress_title), 
 			getString(R.string.connection_progress_body) + " " + config.hostname, 
 			true, true, cancelListener);
 		
 		// Start the CommandService, and attempt to connect it
 		startService(new Intent(NewConnection.this, CommandService.class));
 		bindService(new Intent(NewConnection.this, CommandService.class), connection, Context.BIND_AUTO_CREATE);
 	}
 	
 	public void onConnectionComplete() {
 		unbindService(connection);
 		progressDialog.dismiss();
 		startActivity(new Intent(NewConnection.this, Playback.class));
 	}
 
 	public void onConnectionCancelled() {
 		connectTask.cancel(true);
 		unbindService(connection);
 		stopService(new Intent(NewConnection.this, CommandService.class));
 		progressDialog.dismiss();
 	}
 
 	public void onConnectionFailed(String title, String message) {
 		onConnectionCancelled();
 		new AlertDialog.Builder(NewConnection.this)
 			.setTitle(title)
 			.setMessage(message)
 			.show();
 	}
 }

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
 
 package tesla.app.ui;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import tesla.app.R;
 import tesla.app.command.provider.AppConfigProvider;
 import tesla.app.service.CommandService;
 import tesla.app.service.business.ICommandController;
 import tesla.app.service.connect.ConnectionOptions;
 import tesla.app.ui.task.ConnectToServerTask;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.DialogInterface.OnCancelListener;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class NewConnection extends Activity implements OnClickListener, ConnectToServerTask.OnConnectionListener {
 	
 	public static final int APP_SELECTOR_RESULT = 1;
 
 	private ConnectionOptions config;
 	private ProgressDialog progressDialog;
 	private List<Map<String, String>> providerList;
 	
 	private ICommandController commandService;
 	protected ConnectToServerTask connectTask;
 	
 	private EditText hostText;
 	private EditText userText;
 	private EditText passText;
 	
 	private ServiceConnection connection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			commandService = ICommandController.Stub.asInterface(service);
 			
 			// Connect to the server in the background
 			connectTask = new ConnectToServerTask();
 			connectTask.registerListener(NewConnection.this);
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
         
         providerList = AppConfigProvider.getAppDirectory();
         
         // Attach the button listeners
         View cancelButton = this.findViewById(R.id.cancel);
         cancelButton.setOnClickListener(this);
         View connectButton = this.findViewById(R.id.connect);
         connectButton.setOnClickListener(this);
         View changeAppButton = this.findViewById(R.id.change_app);
         changeAppButton.setOnClickListener(this);
         
         // Load the last saved settings
         config = new ConnectionOptions(this);
 		hostText = (EditText)this.findViewById(R.id.host);
 		userText = (EditText)this.findViewById(R.id.user);
 		passText = (EditText)this.findViewById(R.id.pass);
 		hostText.setText(config.hostname);
 		userText.setText(config.username);
 		passText.setText(config.password);
 		
 		if (config.appSelection.equals("")) {
 			// Default to Rhythmbox
 			config.appSelection = AppConfigProvider.APP_RHYTHMBOX;
 		}
 		setAppButtonData(config.appSelection);
 		
 		// Stop any existing command service
 		stopService(new Intent(NewConnection.this, CommandService.class));
     }
     
 	private void setAppButtonData(String appSelectionItem) {
 		Map<String, String> app = findAppMatchingName(appSelectionItem);
 		
 		TextView appName = (TextView)this.findViewById(R.id.app_current_name);
 		appName.setText(app.get("name"));
 		
 		int resId = Integer.parseInt(app.get("icon"));
 		Drawable icon = getResources().getDrawable(resId);
 		appName.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
 	}
 	
 	private Map<String, String> findAppMatchingName(String appSelection) {
 		Iterator<Map<String, String>> it = providerList.iterator();
 		boolean found = false;
 		Map<String, String>  out = null;
 		while (it.hasNext() && !found) {
 			out = it.next();
 			if (out.get("ref").equalsIgnoreCase(appSelection)) {
 				found = true;
 			}
 		}
 		if (found == false) out = null;
 		return out;
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
 		case (R.id.change_app):
 			startActivityForResult(new Intent(NewConnection.this, AppSelector.class), APP_SELECTOR_RESULT);
 			break;
 		}
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == Activity.RESULT_OK) {
 			switch (requestCode) {
 			case APP_SELECTOR_RESULT:
 				// Update the app selection
 				config.reloadSettings();
 				setAppButtonData(config.appSelection);
 				break;
 			}
 		}
 	}
 	
 	private void setConnectionConfig() {
 		config.hostname = hostText.getText().toString();
 		config.username = userText.getText().toString();
 		config.password = passText.getText().toString();
 		
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
 
 	public void onServiceError(String title, String message) {
 		onConnectionCancelled();
 		new AlertDialog.Builder(NewConnection.this)
 			.setTitle(title)
 			.setMessage(message)
 			.show();
 	}
 }

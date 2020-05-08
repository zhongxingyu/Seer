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
 
 import tesla.app.command.Command;
 import tesla.app.command.provider.AppConfigProvider;
 import tesla.app.service.CommandService;
 import tesla.app.service.business.ICommandController;
 import tesla.app.service.business.IErrorHandler;
 import tesla.app.ui.task.GetMediaInfoTask;
 import tesla.app.ui.task.GetVolumeLevelTask;
 import tesla.app.ui.task.IsPlayingTask;
 import tesla.app.R;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.os.RemoteException;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.view.KeyEvent;
 
 public abstract class AbstractTeslaActivity extends Activity {
 	
 	protected ICommandController commandService;
 	private PowerManager.WakeLock wakeLock;
 	private boolean phoneIsBusy = false;
 	
 	protected ServiceConnection connection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			commandService = ICommandController.Stub.asInterface(service);
 			
 			// Detect phone calls
 			TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
 			tm.listen(callStateHandler, PhoneStateListener.LISTEN_CALL_STATE);
 			
 			onTeslaServiceConnected();
 		}
 
 		public void onServiceDisconnected(ComponentName name) {
 			commandService = null;
 			onTeslaServiceDisconnected();
 		}
 	};
 	
 	protected IErrorHandler errorHandler = new IErrorHandler.Stub() {
 		public void onServiceError(String title, String message, boolean fatal) throws RemoteException {
 			showErrorMessage(AbstractTeslaActivity.this.getClass(), title, message, null);
 		}
 	};
 	
 	private PhoneStateListener callStateHandler = new PhoneStateListener() {
 	        public void onCallStateChanged(int state, String incomingNumber) {
                 
 	        	switch (state) {
 				case TelephonyManager.CALL_STATE_RINGING:
 				case TelephonyManager.CALL_STATE_OFFHOOK:
 					// Pause playback during a call
 					if (!phoneIsBusy) {
 						phoneIsBusy = true;
 						try {
 							commandService.registerErrorHandler(errorHandler);
 							Command command = commandService.queryForCommand(Command.PAUSE);
 							if (command != null) {
 								commandService.sendCommand(command);
 								onPhoneIsBusy();
 							}
 			        	
 							commandService.unregisterErrorHandler(errorHandler);
 						
 						} catch (RemoteException e) {
 			    			// Failed to send command
 			    			e.printStackTrace();
 			    		}
 					}
 					break;
 
 				case TelephonyManager.CALL_STATE_IDLE:
 					// Resume playback when call ends
 					if (phoneIsBusy) {
 						phoneIsBusy = false;
 						try {
 							commandService.registerErrorHandler(errorHandler);
 							Command command = commandService.queryForCommand(Command.PLAY);
 							if (command != null) {
 								commandService.sendCommand(command);
 								onPhoneIsIdle();
 							}
 							commandService.unregisterErrorHandler(errorHandler);
 						} catch (RemoteException e) {
 			    			// Failed to send command
 			    			e.printStackTrace();
 			    		}
 					}
 					
 				default:
 					// Do nothing
 				}
 	        }
 	};
 	
 	public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         
         // Used to keep the Wifi available as long as the activity is running
         PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
         wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Tesla SSH session");
     }
 	
 	protected void onPause() {
 		super.onPause();
 		if (connection != null) unbindService(connection);
 		wakeLock.release();
 	}
 
 	protected void onResume() {
 		super.onResume();
 		wakeLock.acquire();
 		bindService(new Intent(AbstractTeslaActivity.this, CommandService.class), connection, Context.BIND_AUTO_CREATE);
 	}
 	
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		switch (keyCode) {
 		case KeyEvent.KEYCODE_HOME:
 			// If the HOME button is pressed, the application is shutting down.
 			// Therefore, stop the service...
 			stopService(new Intent(AbstractTeslaActivity.this, CommandService.class));
 		}
 		return super.onKeyDown(keyCode, event); 
 	}
 	
 	protected abstract void onTeslaServiceConnected();
 	protected abstract void onTeslaServiceDisconnected();
 	
 	protected abstract void onPhoneIsBusy();
 	protected abstract void onPhoneIsIdle();
 
 	protected void showErrorMessage(Class<? extends Object> invoker, String title, String message, Command command) {
 		if (!isFinishing()) {
 			
 			// Digest message for user-friendly display
 			String errorCode = generateErrorCode(invoker, title, command);
 			
 			title = getResources().getString(R.string.user_error_title);
 			message = getResources().getString(R.string.user_error_body);
 			message = message.replaceAll("%errorcode%", errorCode);
 			
 			// Show error message
 			new AlertDialog.Builder(AbstractTeslaActivity.this)
 				.setTitle(title)
 				.setMessage(message)
 				.setNegativeButton("OK", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.dismiss();
 						// Close the activity to avoid more of these
 						finish();
 					}
 				})
 				.show();
 		}
 	}
 
 	private String generateErrorCode(Class<? extends Object> invoker, String title, Command command) {
 		
 		// This is a little hacky, but should suffice for the time being
 		
 		// Get the invoker ID
 		int idInvoker = 0;
 		if (invoker.equals(Playback.class)) { idInvoker = 1; }
 		else if (invoker.equals(VolumeControl.class)) { idInvoker = 2; }
 		else if (invoker.equals(GetMediaInfoTask.class)) { idInvoker = 3; }
 		else if (invoker.equals(GetVolumeLevelTask.class)) { idInvoker = 4; }
 		else if (invoker.equals(IsPlayingTask.class)) { idInvoker = 5; }
 		
 		// Get the service function ID
 		int idServiceCall = 0;
 		if (title.equals("Failed to connect to remote machine")) { idServiceCall = 1; } // Failed to connect
 		if (title.equals("Failed to send command to remote machine")) { idServiceCall = 2; } // Failed to send command
 		if (title.equals("Failed to send query to remote machine")) { idServiceCall = 3; } // Failed to perform query
 		
 		int idApp = 0;
 		int idCommand = 0;
 		
 		if (command != null) {
 			// Get the player ID
 			String app = command.getTargetApp();
 			if (app.equals(AppConfigProvider.APP_AMAROK)) { idApp = 1; }
 			else if (app.equals(AppConfigProvider.APP_BANSHEE)) { idApp = 2; }
 			else if (app.equals(AppConfigProvider.APP_DRAGONPLAYER)) { idApp = 3; }
 			else if (app.equals(AppConfigProvider.APP_RHYTHMBOX)) { idApp = 4; }
 			else if (app.equals(AppConfigProvider.APP_TOTEM)) { idApp = 5; }
 			else if (app.equals(AppConfigProvider.APP_VLC)) { idApp = 6; }
 			else if (app.equals(AppConfigProvider.APP_EXAILE)) { idApp = 7; }
 			
 			// Get the command ID
 			String commandKey = command.getKey();
 			if (commandKey.equals(Command.GET_MEDIA_INFO)) { idCommand = 1; }
 			else if (commandKey.equals(Command.INIT)) { idCommand = 2; }
 			else if (commandKey.equals(Command.IS_PLAYING)) { idCommand = 3; }
 			else if (commandKey.equals(Command.NEXT)) { idCommand = 4; }
 			else if (commandKey.equals(Command.PAUSE)) { idCommand = 5; }
 			else if (commandKey.equals(Command.PLAY)) { idCommand = 6; }
 			else if (commandKey.equals(Command.POWER)) { idCommand = 7; }
 			else if (commandKey.equals(Command.PREV)) { idCommand = 8; }
 			else if (commandKey.equals(Command.VOL_CHANGE)) { idCommand = 9; }
 			else if (commandKey.equals(Command.VOL_CURRENT)) { idCommand = 10; }
 			else if (commandKey.equals(Command.VOL_MUTE)) { idCommand = 11; }
 		}
 		
 		// Build the code
 		StringBuilder builder = new StringBuilder();
 		builder.append("8"); // Sanity number
 		builder.append(idInvoker);
 		builder.append(idServiceCall);
 		builder.append(idApp);
 		builder.append(idCommand);
 		return builder.toString();
 	}
 }

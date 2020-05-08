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
 
 import tesla.app.R;
 import tesla.app.command.Command;
 import tesla.app.service.CommandService;
 import tesla.app.service.business.ICommandController;
 import tesla.app.service.business.IErrorHandler;
 import tesla.app.ui.task.GetVolumeLevelTask;
 import tesla.app.ui.widget.VolumeSlider;
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
import android.widget.Toast;
 
 public class VolumeControl extends Activity implements VolumeSlider.OnVolumeLevelChangeListener, GetVolumeLevelTask.OnGetVolumeLevelListener {
 	
 	private VolumeSlider volumeSlider;
 	private ICommandController commandService;
 	private PowerManager.WakeLock wakeLock;
 	
 	private ServiceConnection connection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			commandService = ICommandController.Stub.asInterface(service);
 			setInitialVolume();
 		}
 		
 		public void onServiceDisconnected(ComponentName name) {
 			commandService = null;
 		}
 	};
 	
 	private IErrorHandler errorHandler = new IErrorHandler.Stub() {
 		public void onServiceError(String title, String message, boolean fatal) throws RemoteException {
 			showErrorMessage(title, message);
 		}
 	};
 	
 	
     /* This is the volume control. */
 	
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         setContentView(R.layout.volume_control);
 
         // Used to keep the Wifi available as long as the activity is running
         PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
         wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Tesla SSH session");
         
         volumeSlider = (VolumeSlider)this.findViewById(R.id.volume);
         volumeSlider.setOnVolumeLevelChangeListener(this);
     }
 
 	private void updateVolume(VolumeSlider volumeSlider, float level) {
 		Command command = null;
 		
 		try {
 			commandService.registerErrorHandler(errorHandler);
 			
 	    	if (level == 0) {
 	    		command = commandService.queryForCommand(Command.VOL_MUTE); 
 	    	}
 	    	else {
 				command = commandService.queryForCommand(Command.VOL_CHANGE);
 		    	Object levelArg = null;
 		    	if (level > 1) {
 		    		// Values above a fraction should be absolute integers
 		    		levelArg = new Integer((int)level);
 		    	}
 		    	else {
 		    		levelArg = new Float(level);
 		    	}
 				command.addArg(levelArg);
 	    	}
 			commandService.sendCommand(command);
 			
 			commandService.unregisterErrorHandler(errorHandler);
 		} catch (RemoteException e) {
 			// Failed to send command
 			e.printStackTrace();
 		}
     }
     
 	protected void onPause() {
 		super.onPause();
 		if (connection != null) unbindService(connection);
 		wakeLock.release();
 	}
 
 	protected void onResume() {
 		super.onResume();
 		wakeLock.acquire();
 		bindService(new Intent(VolumeControl.this, CommandService.class), connection, Context.BIND_AUTO_CREATE);
 	}
 
 	public void onLevelChanged(VolumeSlider volumeSlider, float level) {
 		updateVolume(volumeSlider, level);
 	}
 	
 	private void setInitialVolume() {
         volumeSlider.setLevel(0.0f);
 		GetVolumeLevelTask getVolumeTask = new GetVolumeLevelTask(commandService);
 		getVolumeTask.registerListener(VolumeControl.this);
 		getVolumeTask.execute();
 	}
 
 	public void onGetVolumeExtents(float min, float max) {
 		volumeSlider.setMinVolume(min);
 		volumeSlider.setMaxVolume(max);
 	}
 
 	public void onGetVolumeComplete(Float result) {
 		volumeSlider.setLevel(result);
 		volumeSlider.refresh();
 	}
 
 	public void onServiceError(String title, String message) {
 		showErrorMessage(title, message);
 	}
 
 	protected void showErrorMessage(String title, String message) {
 		new AlertDialog.Builder(VolumeControl.this)
 			.setTitle(title)
 			.setMessage(message)
 			.setNegativeButton("OK", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 				}
 			})
 			.show();
 	}
 
 	public void onChangeFinished(VolumeSlider volumeSlider) {
		String message = getResources().getText(R.string.volume_update) + " " + volumeSlider.getLevelPercent() + "%";
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
 		// Close the activity when the user stops touching the slider
 		finish();
 	}
 }

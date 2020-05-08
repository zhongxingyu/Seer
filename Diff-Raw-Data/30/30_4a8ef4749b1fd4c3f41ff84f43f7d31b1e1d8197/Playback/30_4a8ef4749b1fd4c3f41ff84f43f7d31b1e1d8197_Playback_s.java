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
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.Map;
 import java.util.concurrent.RejectedExecutionException;
 
 import tesla.app.R;
 import tesla.app.command.Command;
import tesla.app.command.helper.DBusHelper;
 import tesla.app.command.provider.AppConfigProvider;
 import tesla.app.mediainfo.MediaInfo;
 import tesla.app.service.CommandService;
 import tesla.app.service.business.ICommandController;
 import tesla.app.service.business.IErrorHandler;
 import tesla.app.service.connect.ConnectionOptions;
 import tesla.app.ui.task.GetMediaInfoTask;
 import tesla.app.ui.task.IsPlayingTask;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.os.RemoteException;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class Playback extends Activity implements OnClickListener, IsPlayingTask.OnIsPlayingListener, GetMediaInfoTask.OnGetMediaInfoListener {
 
 	private static final long SONG_INFO_UPDATE_PERIOD = 1000;
 	private static final int APP_SELECTOR_RESULT = 1;
 	
 	private ICommandController commandService;
 	private boolean stopSongInfoPolling = false;
 	private boolean appReportingIfPlaying = false;
 	private PowerManager.WakeLock wakeLock;
 	
 	private Handler updateSongInfoHandler = new Handler();
 	private Runnable updateSongInfoRunnable = new Runnable() {
 		public void run() {
 			updateSongInfo(false);
 		}
 	};
 	
 	private ServiceConnection connection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			commandService = ICommandController.Stub.asInterface(service);
 	        
 	        // If the application is reporting whether it is playing, we don't want to toggle the play button manually
 	        try {
 				commandService.registerErrorHandler(errorHandler);
 				Command command = commandService.queryForCommand(Command.IS_PLAYING);
 				
 				Map<String, String> settings = command.getSettings();
 				if (settings.containsKey("ENABLED")) {
 					appReportingIfPlaying = Boolean.parseBoolean(settings.get("ENABLED"));
 				}
 				commandService.unregisterErrorHandler(errorHandler);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 			
 			// Update the song info now, and start the polling update 
 			updateSongInfo(false);
 		}
 		
 		public void onServiceDisconnected(ComponentName name) {
 			commandService = null;
 		}
 	};
 	
 	private IErrorHandler errorHandler = new IErrorHandler.Stub() {
 		public void onServiceError(String title, String message, boolean fatal) throws RemoteException {
 			onServiceErrorAction(title, message);
 		}
 	};
 	
     /* This is the main screen, providing the playback controls. */
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         setContentView(R.layout.playback);
         
         // Used to keep the Wifi available as long as the activity is running
         PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
         wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Tesla SSH session");
 		 
         // Attach the button listeners for playback controls
         View targetButton;
         targetButton = this.findViewById(R.id.pc_power);
         targetButton.setOnClickListener(this);
         targetButton = this.findViewById(R.id.play_pause);
         targetButton.setOnClickListener(this);
         targetButton.setSelected(true);
         targetButton = this.findViewById(R.id.last_song);
         targetButton.setOnClickListener(this);
         targetButton = this.findViewById(R.id.next_song);
         targetButton.setOnClickListener(this);
         targetButton = this.findViewById(R.id.playlist);
         targetButton.setOnClickListener(this);
         targetButton = this.findViewById(R.id.volume);
         targetButton.setOnClickListener(this);
 		
         setAppIcon();
     }
 	
     public void onClick(View v) {
 		Command command = null;
 		
 		try {
 			commandService.registerErrorHandler(errorHandler);
 			switch (v.getId()) {
 			case R.id.pc_power:
 				confirmPowerButton();
 				break;
 			case R.id.play_pause: 
 				togglePlayPauseButtonMode();
 				command = commandService.queryForCommand(Command.PLAY);
 				break;
 			case R.id.last_song:
 				command = commandService.queryForCommand(Command.PREV);
 				updateSongInfo(true);
 				break;
 			case R.id.next_song:
 				command = commandService.queryForCommand(Command.NEXT);
 				updateSongInfo(true);
 				break;
 			case R.id.playlist:
 				new AlertDialog.Builder(Playback.this)
 					.setTitle("Not implemented")
 					.setMessage("Playlist support is not yet available.")
 					.setNegativeButton("OK", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.dismiss();
 						}
 					})
 					.show();
 				break;
 			case R.id.volume:
 				// Start the volume control activity
 				Intent intent = new Intent(Playback.this, VolumeControl.class);
 				startActivity(intent);
 			}
 			
 			if (command != null) {
 				commandService.sendCommand(command);
 			}
 			commandService.unregisterErrorHandler(errorHandler);
 		} catch (RemoteException e) {
 			// Failed to send command
 			e.printStackTrace();
 		}
 	}
 
 	private void confirmPowerButton() {
 		// Ask the user before executing this action
 		new AlertDialog.Builder(Playback.this)
 		.setTitle("Confirm shutdown")
 		.setMessage("Are you sure you want to turn off the computer?")
 		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		})
 		.setPositiveButton("Turn Off", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				try {
 					commandService.registerErrorHandler(errorHandler);
 					Command command = commandService.queryForCommand(Command.POWER);
 					if (command != null) {
 						commandService.sendCommand(command);
 					}
 					commandService.unregisterErrorHandler(errorHandler);
 				} catch (RemoteException e) {
 					// Failed to send command
 					e.printStackTrace();
 				}
 			}
 		})
 		.show();
 	}
 
 	private void togglePlayPauseButtonMode() {
 		if (!appReportingIfPlaying) {
 			ImageButton button = (ImageButton)this.findViewById(R.id.play_pause);
 			button.setSelected(!button.isSelected());
 			button.refreshDrawableState();
 		}
 	}
 
 	private void updateSongInfo(boolean isOverride) {
 		if (commandService != null && stopSongInfoPolling == false) {
 			if (appReportingIfPlaying) {
 				try {
 					IsPlayingTask isPlayingTask = new IsPlayingTask();
 					isPlayingTask.registerListener(this);
 					isPlayingTask.execute(commandService);
 				}
 				catch (RejectedExecutionException e) {
 					// Ignore failed executions
 				}
 			}
 			
 			try {
 				GetMediaInfoTask getSongInfoTask = new GetMediaInfoTask();
 				getSongInfoTask.registerListener(this);
 				getSongInfoTask.execute(commandService);
 			}
 			catch (RejectedExecutionException e) {
 				// Ignore failed executions
 			}
 			
 			if (!isOverride) {
 				updateSongInfoHandler.removeCallbacks(updateSongInfoRunnable);
 				updateSongInfoHandler.postDelayed(updateSongInfoRunnable, SONG_INFO_UPDATE_PERIOD);
 			}
 		}
 	}
 
 	protected void onPause() {
 		super.onPause();
 		stopSongInfoPolling = true;
 		if (connection != null) unbindService(connection);
 		wakeLock.release();
 	}
 
 	protected void onResume() {
 		super.onResume();
 		wakeLock.acquire();
 		stopSongInfoPolling = false;
 		bindService(new Intent(Playback.this, CommandService.class), connection, Context.BIND_AUTO_CREATE);
 	}
 
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		switch (keyCode) {
 		case KeyEvent.KEYCODE_HOME:
 			// If the HOME button is pressed, the application is shutting down.
 			// Therefore, stop the service...
 			stopService(new Intent(Playback.this, CommandService.class));
 		}
 		return super.onKeyDown(keyCode, event); 
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(R.string.menu_application_change);
 		return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Currently only one item to select
 		startActivityForResult(new Intent(Playback.this, AppSelector.class), APP_SELECTOR_RESULT);
 		return true;
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (resultCode == Activity.RESULT_OK) {
 			switch (requestCode) {
 			case APP_SELECTOR_RESULT:
 				// Target media player has been changed
 				try {
 					commandService.reloadCommandFactory();
 					setAppIcon();
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 				break;
 			}
 		}
 	}
 
 	private void setAppIcon() {
 		ConnectionOptions config = new ConnectionOptions(this);
 		Map<String, String> appInfo = AppConfigProvider.findAppMatchingName(config.appSelection);
 		if (appInfo.containsKey("icon")) {
 			int iconRef = Integer.valueOf(appInfo.get("icon"));
 			if (iconRef > 0) {
 				ImageView appIcon = (ImageView)findViewById(R.id.app_icon);
 				appIcon.setImageResource(iconRef);
 			}
 		}
 	}
 
 	public void onServiceErrorAction(String title, String message) {
 		onServiceError(title, message);
 	}
 
 	public void onServiceError(String title, String message) {
 		stopSongInfoPolling = true;
 		if (!isFinishing()) {
 			new AlertDialog.Builder(Playback.this)
 				.setTitle(title)
 				.setMessage(message)
 				.setNegativeButton("OK", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.dismiss();
 					}
 				})
 				.show();
 		}
 	}
 
 	public void onMediaInfoChanged(MediaInfo info) {
 		TextView labelTitle = (TextView)this.findViewById(R.id.song_title);
 		TextView labelArtist = (TextView)this.findViewById(R.id.song_artist);
 		TextView labelAlbum = (TextView)this.findViewById(R.id.song_album);
 		ImageView artwork = (ImageView)this.findViewById(R.id.album_cover);
 		
 		if (info.title != null && info.artist != null && info.album != null) {
 			String newTitle = info.track + " - " + info.title;
 			setLabelTextIfChanged(labelTitle, newTitle);
 			setLabelTextIfChanged(labelArtist, info.artist);
 			setLabelTextIfChanged(labelAlbum, info.album);
 			
 			// Load the artwork from the cache store
 			boolean failed = false;
 			if (info.artwork != null && new File(info.artwork).exists()) {
 				FileInputStream fis;
 				try {
 					fis = new FileInputStream(info.artwork);
 					Bitmap bitmap = BitmapFactory.decodeStream((InputStream)fis);
 					fis.close();
 					Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 80, 80, true);
 					artwork.setImageBitmap(scaled);
 				} catch (Exception e) {
 					// Failed to load image from cache store
 					failed = true;
 				}
 			}
 			else failed = true;
 			if (failed == true) {
 				// Revert to the generic CD cover image
 				artwork.setImageResource(R.drawable.album_cover);
 			}
 		}
 		else {
 			// Revert to the generic media info text
 			setLabelTextIfChanged(labelTitle, getResources().getText(R.string.info_title).toString());
 			setLabelTextIfChanged(labelArtist, getResources().getText(R.string.info_artist).toString());
 			setLabelTextIfChanged(labelAlbum, getResources().getText(R.string.info_album).toString());
 			artwork.setImageResource(R.drawable.album_cover);
 		}
 	}
 	
 	private void setLabelTextIfChanged(TextView label, String newString) {
 		if (!label.getText().equals(newString)) {
 			label.setText(newString);
 		}
 	}
 
 	public void onPlayingChanged(boolean isPlaying) {
         ImageButton playPauseButton = (ImageButton)this.findViewById(R.id.play_pause);
         playPauseButton.setSelected(isPlaying);
 	}
 }

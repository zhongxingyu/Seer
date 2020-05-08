 package uk.sean;
 
 import uk.sean.connect.ConnectionOptions;
 import uk.sean.connect.IConnection;
 import uk.sean.connect.SSHConnection;
 import uk.sean.dbus.InitScriptProvider;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class Tesla extends Activity implements OnClickListener {
 	
 	private IConnection connection;
 	
     /* This is the main screen, providing the playback controls. */
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         setContentView(R.layout.main);
         
         // Attach the button listeners for playback controls
         View playPauseButton = this.findViewById(R.id.play_pause);
         playPauseButton.setOnClickListener(this);
         View prevSongButton = this.findViewById(R.id.last_song);
         prevSongButton.setOnClickListener(this);
         View nextSongButton = this.findViewById(R.id.next_song);
         nextSongButton.setOnClickListener(this);
         
         // Assume an SSH connection for now
         connection = new SSHConnection();
         try {
 			connection.connect(new ConnectionOptions(this));
 			// Initialise the DBUS connection
 			String response = connection.sendCommand(InitScriptProvider.getInitScript());
 			if (!response.equals("success\n")) {
 				throw new Exception("Init script failed with output: " + response);
 			}
 		} catch (Exception e) {
 			// Show errors in a dialog
			new AlertDialog.Builder(Tesla.this)
 	        	.setTitle("Failed to connect to remote machine")
 	        	.setMessage(e.getMessage())
 	        	.show();
			connection.disconnect();
 		}
     }
     
 	protected void onDestroy() {
 		super.onDestroy();
 		
 		// Disconnect from temp connection
		connection.disconnect();
 	}
 	
 	public void onClick(View v) {
 		String command = "";
 		
 		switch (v.getId()) {
 		case R.id.play_pause: 
 			command = "qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player playPause false";
 			break;
 		case R.id.last_song: 
 			command = "qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player previous";
 			break;
 		case R.id.next_song: 
 			command = "qdbus org.gnome.Rhythmbox /org/gnome/Rhythmbox/Player next";
 			break;
 		}
 		
 		if (command.length() > 0) {
 			try {
 				connection.sendCommand(command);
 			} catch (Exception e) {
 				// Show errors in a dialog
 				new AlertDialog.Builder(Tesla.this)
 		        	.setTitle("Failed to send command to remote machine")
 		        	.setMessage(e.getMessage())
 		        	.show();
 			}
 		}
 	}
 }

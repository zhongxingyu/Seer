 package com.wasser.spotify.remote.activities;
 
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.wasser.spotify.remote.R;
 import com.wasser.spotify.remote.connection.SocketConnection;
 import com.wasser.spotify.remote.player.Commands;
 import com.wasser.spotify.remote.player.PlayerProperties;
 import com.wasser.spotify.remote.player.Status;
 import com.wasser.spotify.remote.player.Track;
 
 public class MainActivity extends PlayerActivity implements OnClickListener {
 
 	// Fields with Track info
 	private TextView artistField;
 	private TextView albumField;
 	private TextView titleField;
 
 	// Controls
 	private ImageView prevButton;
 	private ImageView nextButton;
 	private ImageView playPauseButton;
 
 	private Status currentStatus = Status.STOPPED;
 	private SocketConnection socket = null;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.player);
 
 		// Selecting fonts
 		Typeface tfFixedText = Typeface.createFromAsset(getAssets(),
 				"fonts/GOODTIME.TTF");
 		Typeface tfText = Typeface.createFromAsset(getAssets(),
 				"fonts/Roboto-Regular.ttf");
 
 		artistField = (TextView) findViewById(R.id.artist_value);
 		albumField = (TextView) findViewById(R.id.album_value);
 		titleField = (TextView) findViewById(R.id.title_value);
 
 		artistField.setTypeface(tfText);
 		albumField.setTypeface(tfText);
 		titleField.setTypeface(tfText);
 
 		// Labels
 		TextView artistLabel = (TextView) findViewById(R.id.artist);
 		TextView albumLabel = (TextView) findViewById(R.id.album);
 		TextView titleLabel = (TextView) findViewById(R.id.title);
 
 		artistLabel.setTypeface(tfFixedText);
 		albumLabel.setTypeface(tfFixedText);
 		titleLabel.setTypeface(tfFixedText);
 
 		// Button Configuration
 		prevButton = (ImageView) findViewById(R.id.rewind);
 		nextButton = (ImageView) findViewById(R.id.fast_forward);
 		playPauseButton = (ImageView) findViewById(R.id.play_pause);
 
 		prevButton.setOnClickListener(this);
 		nextButton.setOnClickListener(this);
 		playPauseButton.setOnClickListener(this);
 
 		// Server Connection
 		socket = SocketConnection.getClient();
		new StateReceiver().execute(); // Receives data from server
										// asynchronously
 		socket.sendCommand(Commands.GET_TRACK);
 	}
 
 	@Override
 	public void changeState(PlayerProperties state) {
		setMetadata(state.getTrack());
		setStatus(state.getStatus());
 	}
 
 	public void setMetadata(Track track) {
 		if (track == null) {
 			track = new Track();
 		}
 		artistField.setText(track.getArtist());
 		albumField.setText(track.getAlbum());
 		titleField.setText(track.getTitle());
 	}
 
 	public void setStatus(Status newStatus) {
 		if (newStatus == null)
 			newStatus = Status.STOPPED;
 		if (newStatus == Status.PLAYING) {
 			playPauseButton.setImageResource(R.drawable.pause);
 		} else {
 			playPauseButton.setImageResource(R.drawable.play);
 		}
 		this.currentStatus = newStatus;
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.play_pause:
 			if (currentStatus == Status.PLAYING)
 				socket.sendCommand(Commands.PAUSE);
 			else
 				socket.sendCommand(Commands.PLAY);
 			break;
 		case R.id.rewind:
 			socket.sendCommand(Commands.PREV);
 			break;
 		case R.id.fast_forward:
 			socket.sendCommand(Commands.NEXT);
 			break;
 		default:
 			System.out.println("Command not recognized");
 		}
 	}
 
 	/**
 	 * Gets States from the server asynchronously during the whole duration of
 	 * the activity.
 	 * 
 	 * @author wasser
 	 * 
 	 */
 	private class StateReceiver extends AsyncTask<Void, PlayerProperties, Void> {
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			while (!this.isCancelled() && !socket.isClosed()) {
 				PlayerProperties receivedState = socket.receiveState();
 				publishProgress(new PlayerProperties[] { receivedState });
 			}
 			return null;
 		}
 
 		@Override
 		protected void onProgressUpdate(PlayerProperties... values) {
 			changeState(values[0]);
 		}
 
 	}
 }

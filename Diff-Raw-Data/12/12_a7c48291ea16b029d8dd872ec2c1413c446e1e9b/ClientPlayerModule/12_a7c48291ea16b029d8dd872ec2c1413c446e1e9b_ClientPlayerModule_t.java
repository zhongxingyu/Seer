 package com.ricex.rpi.server;
 
 import com.ricex.rpi.common.Playlist;
 import com.ricex.rpi.common.RPIStatus;
 import com.ricex.rpi.common.message.IMessage;
 import com.ricex.rpi.common.message.MovieMessage;
 import com.ricex.rpi.common.video.Video;
 import com.ricex.rpi.server.client.ClientChangeEvent;
 import com.ricex.rpi.server.client.ClientChangeListener;
 import com.ricex.rpi.server.client.RPIClient;
 
 /** Interface between the GUI and the Server
  * 
  * @author Mitchell
  *
  */
 
 public class ClientPlayerModule implements ClientChangeListener<RPIClient> {
 
 	/** The client to send the commands to */
 	private RPIClient client;
 
 	/** The currently playing playlist */
 	private Playlist playlist;
 
 	/** Creates a new ServerPlayerModule with the given client */
 	public ClientPlayerModule(RPIClient client) {
 		this.client = client;
 		client.addChangeListener(this);
 	}
 
 
 
 	/** Starts playing the given play list
 	 * 
 	 * @param videoPath Path to the movie to play
 	 */
 
 	public void play(Playlist playlist) {
 		this.playlist = playlist;
 		playVideo(playlist.getNextItem());
 	}
 
 	/** Plays the given video from the playlist, if the video passed in it will return false.
 	 * 
 	 * @param video THe video to play
 	 * @return True if the video started to play, false if there was no video to play
 	 */
 
 	private boolean playVideo(Video video) {
 		if (video == null) {
 			return false;
 		}
 		sendMessage(new MovieMessage( video.getVideoFile(), MovieMessage.Command.PLAY));
 		return true;
 	}
 
 	/** Stops the currently playing movie
 	 * 
 	 * TODO: Figure out if stop should stop playlist or just the currently playing movie, more opt towards Playlist.
 	 */
 	
 	public void stop() {
 		sendMessage(new MovieMessage(MovieMessage.Command.STOP));
 	}
 
 	/** Pauses / resumes the currently playing movie
 	 * 
 	 */
 
 	public void pause() {
		System.out.println("PlayerModule pause...");
 		sendMessage(new MovieMessage(MovieMessage.Command.PAUSE));
 	}
 
 	/** Move to the next chapter */
 
 	public void nextChapter() {
 		sendMessage(new MovieMessage(MovieMessage.Command.NEXT_CHAPER));
 	}
 
 	/** Move the previous chapter */
 
 	public void previousChapter() {
 		sendMessage(new MovieMessage(MovieMessage.Command.PREVIOUS_CHAPTER));
 	}
 
 	/** Turn the volume up */
 
 	public void volumeUp() {
 		sendMessage(new MovieMessage(MovieMessage.Command.VOLUME_UP));
 	}
 
 	/** Turn the volume down */
 
 	public void volumeDown() {
 		sendMessage(new MovieMessage(MovieMessage.Command.VOLUME_DOWN));
 	}
 
 	/** Seek to the right 30 seconds */
 
 	public void seekForwardSlow() {
 		sendMessage(new MovieMessage(MovieMessage.Command.SEEK_FORWARD_SLOW));
 	}
 
 	/** Seek to the right 600 seconds */
 
 	public void seekForwardFast() {
 		sendMessage(new MovieMessage(MovieMessage.Command.SEEK_FORWARD_FAST));
 	}
 
 	/** Seek to the left 30 seconds */
 
 	public void seekBackwardSlow() {
 		sendMessage(new MovieMessage(MovieMessage.Command.SEEK_BACKWARD_SLOW));
 	}
 
 	/** Seek to the right 600 seconds */
 
 	public void seekBackwardFast() {
 		sendMessage(new MovieMessage(MovieMessage.Command.SEEK_BACKWARD_FAST));
 	}
 
 	/** Sends a message to this server play modules client
 	 * @param message The message to send
 	 */
 
 	private void sendMessage(IMessage message) {
 		client.sendMessage(message);
 	}
 
 
 
 	@Override
 	public void clientChanged(ClientChangeEvent<RPIClient> changeEvent) {
 		if (changeEvent.getEventType() == ClientChangeEvent.EVENT_STATUS_CHANGE) {
 			if (client.getStatus().getStatus() == RPIStatus.IDLE) {
				playVideo(playlist.getNextItem());
 			}
 		}
 
 	}
 
 }
 

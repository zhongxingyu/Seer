 package plt.playlist;
 
 
 import android.media.MediaPlayer;
 import android.media.AudioManager;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.app.Activity;
 
 import java.io.IOException;
 import java.util.List;
 

 
 
 
 
 
 // Given a playlist, plays music.
 
 // Needs a handler to queue its stuff.  MediaPlayer is supposed to be
 // finicky about running in the UI thread, so all of our operations
 // must run from the handler.
 
 public class PlaylistPlayer {
     private MediaPlayer mediaPlayer;
     private Handler handler;
     private Activity activity;
 
 
     private int volume;
     private int currentSongIndex;
     private int delayBetweenSongs;
     private List<Uri> songs;
     private boolean isPlaying;
 
 
     public PlaylistPlayer(final Activity activity,
 			  Handler handler,
 			  final PlaylistRecord record) {
	System.out.println("Constructing player: ");
 
 	final PlaylistPlayer that = this;
 	this.activity = activity;
 	this.handler = handler;
 	this.handler.post(new Runnable() { public void run() { 
 	    that.volume = 100;
 	    that.songs = record.getSongUris(activity);
 	    that.currentSongIndex = 0;
 	    that.isPlaying = false;
 	    that.delayBetweenSongs = 2000;
 	}});
 
     }
 
     // The following methods will queue up a sequence of songs to play.
 
     public void play() {
	System.out.println("play: isPlaying = " + isPlaying);
 	final PlaylistPlayer that = this;
 	this.handler.post(new Runnable() {
 		public void run() {
 		    try {
 			if (that.mediaPlayer == null) {
 			    that.mediaPlayer = new MediaPlayer();
 			    that.mediaPlayer.setLooping(false);
 			    that.mediaPlayer.setOnPreparedListener
 				(new OnPreparedListener() {
 					public void onPrepared(MediaPlayer mp) {
 					    that.handler.post(new Runnable() {
 						    public void run() {
 							that.mediaPlayer.start();
 						    }
 						});
 					}
 				    });
 			    
 			    that.mediaPlayer.setOnCompletionListener
 				(new OnCompletionListener() {
 					public void onCompletion(MediaPlayer mp) {
 					    that.handler.postAtTime(new Runnable() {
 						    public void run() {
 							that.mediaPlayer.release();
 							that.mediaPlayer = null;
 							that.currentSongIndex = 
 							    (that.currentSongIndex + 1) %
 							    that.songs.size();
 							that.play();
 						    }
 						}, SystemClock.uptimeMillis() + that.delayBetweenSongs);
 					}
 					
 				    });
 
 			    that.mediaPlayer.setDataSource
 				(that.activity,
 				 that.songs.get(that.currentSongIndex));
 			    that.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 			    that.mediaPlayer.prepareAsync();
 			    that.isPlaying = true;
 			} else if (that.isPlaying) {
 			    return;
 			} else {
 			    that.mediaPlayer.start();
 			}
 		    } catch (IOException e) {
 			e.printStackTrace();
 		    }
 		}
 	    });
     }
 
 
 
     public void pause() {
 	final PlaylistPlayer that = this;
 	this.handler.post(new Runnable() {
 		public void run() {
 		    if (that.mediaPlayer == null) {
 		        return;
 		    } else if (that.isPlaying) {
 			that.mediaPlayer.pause();
 			that.isPlaying = false;
 		    }
 		}
 	    });
     }
 
 
     public void stop() {
 	final PlaylistPlayer that = this;
 	this.handler.post(new Runnable() {
 		public void run() {
 		    if (that.mediaPlayer == null) {
 		        return;
 		    } else if (that.isPlaying) {
 			that.mediaPlayer.seekTo(0);
 			that.mediaPlayer.pause();
 			that.isPlaying = false;
 		    }
 		}
 	    });
     }
 
 
 
     public int getVolume() {
 	return this.volume;
     }
 
 
     public void setVolume(final int percent) {
 	final PlaylistPlayer that = this;
 	this.handler.post(new Runnable() {
 		public void run() {
 		    that.volume = percent;
 		}
 	    });
     }
     
 
 
 }

 package com.gradugation;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Handler;
 
 public class SongPlayer {
	private static SongPlayer instance;
 	private Intent backgroundMusicServiceIntent;
 	private Context context;
 	private boolean isPlaying;
 	private long lastTimePlayed;
 	
 	public SongPlayer(Context context) {
 		backgroundMusicServiceIntent = new Intent(context, BackgroundMusicService.class);
         backgroundMusicServiceIntent.setAction(BackgroundMusicService.ACTION_PLAY);
         this.context = context;
         isPlaying = false;
         lastTimePlayed = 0;
 	}
 	
 	public static void initializePlayer(Context context) {
 		instance = new SongPlayer(context);
 	}
 	
 	public static void playSong() {
 		instance.play();
 	}
 	
 	public void play() {
 		if (!isPlaying) {
 			context.startService(backgroundMusicServiceIntent);
 			isPlaying = true;
 		}
 		lastTimePlayed = 0;
 	}
 	
 	public static void stopSong() {
 		instance.stop();
 	}
 	
 	public void stop() {
 		if (isPlaying) {
 			context.stopService(backgroundMusicServiceIntent);
 			isPlaying = false;
 		}
 	}
 	
 	public static void stopSongDelayed() {
 		instance.stopDelayed();
 	}
 
 	public void stopDelayed() {
 		lastTimePlayed = System.currentTimeMillis();
 		
 		final Handler handler = new Handler();
 		Timer timer = new Timer(false);
 		TimerTask timerTask = new TimerTask() {
 		    @Override
 		    public void run() {
 		        handler.post(new Runnable() {
 		            public void run() {
 		            	if (lastTimePlayed == 0) {
 		            		return;
 		            	}
 		                if ((System.currentTimeMillis() - lastTimePlayed) > 1000) {
 		                	context.stopService(backgroundMusicServiceIntent);
 		        			isPlaying = false;
 		                }
 		            }
 		        });
 		    }
 		};
 		timer.schedule(timerTask, 1000);
 	}
 }

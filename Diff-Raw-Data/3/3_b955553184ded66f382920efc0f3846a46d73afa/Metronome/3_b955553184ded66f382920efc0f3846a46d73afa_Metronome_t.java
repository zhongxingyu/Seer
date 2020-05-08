 package com.jpapps.badmetronome;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import android.graphics.Canvas;
 import android.media.MediaPlayer;
 import android.os.Handler;
 import android.util.Log;
 
 public class Metronome {
 	
 	public static final int DEFAULT_SPEED = 50;
 	public static final int MAX_ACCURACY = 100;
 	
 	private int speed, accuracy;
 	private MediaPlayer player;
 	
 	protected long delay;
 	protected boolean playing;
 	protected Thread playbackThread;
 	protected Runnable playbackRunnable;
 	protected long lastTime;
 	
 	public Metronome(MediaPlayer player) {
 		this(DEFAULT_SPEED, player);
 	}
 	
 	public Metronome(int speed, MediaPlayer player) {
 		this(speed, MAX_ACCURACY, player);
 	}
 	
 	public Metronome(int speed, int accuracy, MediaPlayer player) {
 		playing = false;
 		lastTime = 0;
 		this.setAccuracy(accuracy);
 		this.setPlayer(player);
 		this.setSpeed(speed);
 	}
 
 	public int getSpeed() {
 		return speed;
 	}
 
 	public void setSpeed(int speed) {
 		this.speed = speed;
 		delay = calculateDelay();
 	}
 
 	public int getAccuracy() {
 		return accuracy;
 	}
 
 	public void setAccuracy(int accuracy) {
 		this.accuracy = accuracy;
 	}
 	
 	private int calculateDelay() {
 		int delay = 0;
 		int duration = 0;
 		if(player!=null)
 			 duration = player.getDuration();
 		double beatLength = 60000.0 / speed;
 		delay = (int)Math.round(beatLength) - duration;
		
		Log.w("BadMetronome", "Delay = " + delay);
		
 		return Math.max(delay, 0);
 	}
 	
 	public void start() {
 		
 		playbackRunnable = new Runnable() {
 			@Override
 			public void run() {		
 				while (playing) {
 		        	long beforeTime = System.nanoTime();
 		            
 		        	if(player.isPlaying()) {
 		        		player.seekTo(0);
 					} else {
 						player.start();
 					}
 		        	
 		            long adjustedSleepTime = delay - ((System.nanoTime()-beforeTime)/1000000L);
 		            
 		            try {
 		            	if(adjustedSleepTime > 0)
 		            		Thread.sleep(adjustedSleepTime);
 		            } catch (InterruptedException e) {
 		            	Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
 		            }
 		        }
 			}
 		};
 		
 		playing = true;
 		
 		playbackThread = new Thread(playbackRunnable);
 		playbackThread.start();
 	}
 	
 	public void stop() {
 		if(player.isPlaying()){
 			player.pause();
 		}
 		playing = false;
 	}
 	
 	public void togglePlayback() {
 		if(playing) {
 			this.stop();
 		}
 		else {
 			this.start();
 		}
 	}
 
 	public MediaPlayer getPlayer() {
 		return player;
 	}
 
 	public void setPlayer(MediaPlayer player) {
 		this.player = player;
 	}
 	
 }

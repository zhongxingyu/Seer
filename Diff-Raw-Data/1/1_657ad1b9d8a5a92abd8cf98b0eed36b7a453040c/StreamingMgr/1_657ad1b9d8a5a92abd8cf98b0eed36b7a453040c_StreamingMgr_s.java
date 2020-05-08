 package com.moupress.app.media;
 
 import java.io.File;
 import java.io.IOException;
 import android.content.Context;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.CountDownTimer;
 
 import com.moupress.app.Const;
 import com.spoledge.aacplayer.AACPlayer;
 
 
 public class StreamingMgr {
 	
 	private StreamingMediaPlayer audioStreamer;
 	private AACPlayer aacPlayer;
 	private MediaPlayer defaultAlarmPlayer;
 	private StreamingNotifier notifier;
 	
 	private Context context;
 	
 	private CountDownTimer streamDownCountTimer;
 	private static final long TIMERSTREAMINGDELAY = 60 * 2 * 1000;
 	
 	public boolean bIsPlaying;
 	
 	public StreamingMgr(Context context) {
 		this.context = context;
 		//nc = new NetworkConnection(Const.BBC_WORLD_SERVICE,context);
 	}
 
 	public void startStreaming(String mediaURL, int mediaLengthInKb, int mediaLengthInSeconds) {
 		
 		try {
 			if (mediaURL.equals(Const.DEFAULT_RIGNTONE)) {
 				playDefaultAlarmSound();
 			} else {
 				streamDownCountTimer = new CountDownTimer(TIMERSTREAMINGDELAY, TIMERSTREAMINGDELAY) {
 
 					@Override
 					public void onFinish() {
 						System.out.println("Time Up!");
 						//streaming too slow, switch to default ringtone
 						boolean isStreamingDelayed = false;
 						if ((audioStreamer != null && !audioStreamer.isPlaying())
 								|| (aacPlayer != null && !aacPlayer.isPlaying())) {
 							isStreamingDelayed = true;
 						}
 						if (isStreamingDelayed){
 							playDefaultAlarmSound();
 						}
 						
 					}
 
 					@Override
 					public void onTick(long arg0) {
 						System.out.println("1 minutes");
 					}
 					
 				}.start();
 				
 				
 				if (mediaURL.startsWith("mms://")) {
 		    		aacPlayer = new AACPlayer();
 		    		aacPlayer.playAsync(mediaURL );
 				} else {
 					audioStreamer = new StreamingMediaPlayer(context);
 					audioStreamer.startStreaming(mediaURL, mediaLengthInKb, mediaLengthInSeconds);
 					//audioStreamer.interrupt();
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void interrupt() {
 		if (audioStreamer != null) audioStreamer.interrupt();
 		if (aacPlayer != null) aacPlayer.stop();
 		if (defaultAlarmPlayer != null) defaultAlarmPlayer.stop();
 	}
 	
 	public void exitApp() {
 		if (audioStreamer != null) {
 			audioStreamer.interrupt();
 			audioStreamer.release();
 			File dir = context.getCacheDir();
 			if (dir != null && dir.isDirectory()) {
 				String[] children = dir.list();
 				for (int i = 0; i < children.length; i++) {
 		            new File(dir, children[i]).delete();
 		        }
 			}
 		}
 		if (aacPlayer != null) {
 			aacPlayer.stop();
 		}
 		if (defaultAlarmPlayer != null) {
 			defaultAlarmPlayer.stop();
 			defaultAlarmPlayer.release();
 		}
 	}
 	
 	private void playDefaultAlarmSound() {
 		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); 
 		if(alert == null){
 	        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
 	        if(alert == null){
 	            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);               
 	        }
 	    }
 		defaultAlarmPlayer = new MediaPlayer();
 		try {
 			defaultAlarmPlayer.setDataSource(context, alert);
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		final AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
 		if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
 			defaultAlarmPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
 			defaultAlarmPlayer.setLooping(true);
 			try {
 				defaultAlarmPlayer.prepare();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			defaultAlarmPlayer.start();
 		}
 	}
 
 }

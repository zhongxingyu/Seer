 package app.util;
 
 import org.apache.log4j.Logger;
 import org.primefaces.push.PushContext;
 import org.primefaces.push.PushContextFactory;
 
 
 public class ThreadMonitorWinamp extends Thread {
 	private Logger log = LogUtils.getLogger(ThreadMonitorWinamp.class);
 	
 	private String playingMusic;
 
 	@Override
 	public void run() {
		boolean run = true;
		while (run) {
 			try {
 				playingMusic = WinampUtils.getFileNamePlaying();
 				PushContext pushContext = PushContextFactory.getDefault().getPushContext();
 				pushContext.push(Constants.CHANNEL_PLAYING_MUSIC, playingMusic);
 				
 				sleep(3000);
 			} catch (InterruptedException e) {
 				log.error("Error in run",e);
 				break;
 			} 
 		}
 	}
 	
     public String getPlayingMusic() {
         return playingMusic;
     }
 }

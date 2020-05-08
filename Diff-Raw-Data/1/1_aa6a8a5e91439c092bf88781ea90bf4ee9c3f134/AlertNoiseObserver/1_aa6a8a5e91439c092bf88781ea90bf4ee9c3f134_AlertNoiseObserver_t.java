 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 import com.sun.syndication.feed.synd.SyndFeedImpl;
 
 
 public class AlertNoiseObserver extends FeedObserver {
 
 	/**
 	 * Code adapted from http://stackoverflow.com/tags/javasound/info.
 	 */
 	@Override
 	public void notify(ArrayList<SyndFeedImpl> changedFeeds, Date lastRunDate) {
 		File alertFile = new File("data/Alert.wav");
 		Clip clip;
 		
 		try {
 			clip = AudioSystem.getClip();
 			AudioInputStream ais;
 			ais = AudioSystem.getAudioInputStream( alertFile );
 			clip.open(ais);
			clip.start();
 		} catch (UnsupportedAudioFileException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (LineUnavailableException e) {
 			e.printStackTrace();
 		}
 
 	}
 }

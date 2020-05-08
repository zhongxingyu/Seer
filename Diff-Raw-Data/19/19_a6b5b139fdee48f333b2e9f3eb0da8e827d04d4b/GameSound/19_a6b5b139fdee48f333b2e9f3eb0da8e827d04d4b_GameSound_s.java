 
 /**
  * Routines to handle sound for Awesome Alphabet.
  */
 
 package edu.bu.cs673.AwesomeAlphabet.model;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 import org.apache.log4j.Logger;
 
 import edu.bu.cs673.AwesomeAlphabet.main.AAConfig;
 
 
 public class GameSound {
 	static Logger log = Logger.getLogger(GameSound.class);
 
 	private String soundName;
 	private Clip curr_clip = null;
 	
 	/**
 	 * Constructor. This prepends the directory to the sound's filename
 	 * @param soundName sound's filename
 	 */
 	public GameSound(String soundName) {
 		// TODO: Error handling. File not being present.
 		this.soundName = soundName;
 		// log.info("Adding sound file " + this.soundName);	
 	}
 	
 	/**
 	 * Plays the sound for this object. Does not return until the sound has finished playing.
 	 * This is limited to short-duration sounds only (2 seconds or less).
 	 */
 	public void PlaySound() {
 		try {
 			InputStream is = AAConfig.getSoundResource(soundName);
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(is);
 			curr_clip = AudioSystem.getClip();
 			
 			curr_clip.open(audioIn);
 			curr_clip.start();
 		} catch (UnsupportedAudioFileException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (LineUnavailableException e) {
 			log.error("Sound file '" + soundName + "' is too large to play.");
 		}
 	}
 	public void StopSound() {
 		if (curr_clip == null)
 			return;
 		curr_clip.stop();
 		curr_clip.close();
 		curr_clip = null;
 	}
 }

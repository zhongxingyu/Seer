 package bitcity;
 
import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineListener;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import java.io.IOException;
 import java.io.File;
 
 public enum Sound {
 	CAR_HONK("data/beepbeep.wav"),
 	FIREFIGHTER_SIREN("data/Firetruck Siren.wav");
 
 	private Clip clip;
 	
 	private Sound (String filename) {
 		File soundFile = new File(filename);
		
 		try {
 			AudioInputStream soundStream = AudioSystem.getAudioInputStream(soundFile);
			AudioFormat format = soundStream.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			this.clip = (Clip)AudioSystem.getLine(info);
 			this.clip.open(soundStream);
 		} catch (UnsupportedAudioFileException e) {
 			System.out.println("Caught exception: " + e.getMessage());
 		} catch (LineUnavailableException e) {
 			System.out.println("Caught exception: " + e.getMessage());
 		} catch (IOException e) {
 			System.out.println("Caught exception: " + e.getMessage());
 		}
 	}
 
 	public void addListener(LineListener listener) {
 		this.clip.addLineListener(listener);
 	}
 	
 	public void play() {
 		this.clip.setFramePosition(0);
 		this.clip.start();
 	}
 	
 	public void loop() {
 		this.clip.setFramePosition(0);
 		this.clip.loop(Clip.LOOP_CONTINUOUSLY);
 	}
 	
 	public void stop() {
 		this.clip.stop();
 	}
 	
 	static void init() {
 		values();
 	}
 }

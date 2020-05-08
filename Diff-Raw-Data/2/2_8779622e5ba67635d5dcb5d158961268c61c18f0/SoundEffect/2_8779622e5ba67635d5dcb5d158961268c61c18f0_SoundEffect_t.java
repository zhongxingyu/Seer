package edu.mines.csci598.recycler.bettyCrocker;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.FloatControl;
 import javax.sound.sampled.LineEvent;
 import javax.sound.sampled.LineListener;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 /**
  * A SoundEffect is used to play a short sound clip typically short enough
  * to store completely in memory. A SoundEffect can be played at any time
  * by using the 'play' method. 
  * 
  * To create a SoundEffect, simply use the constructor that takes the
  * name of the sound file (using a relative path).
  * 
  * In addition, the volume and looping attributes
  * can be set by the user.
  * 
  * @author John
  *
  */
 public class SoundEffect {
 	
 	private File soundFile;
 	private Clip clip;
 	private AudioInputStream audioStream;
 	
 	public SoundEffect(String fileName) {
 		soundFile = new File(fileName);
 	}
 	
 	public void play() {
 		//new PlayThread().start();
 		
 		try {
 			audioStream = AudioSystem.getAudioInputStream(soundFile);
 			
 			clip = AudioSystem.getClip();
 			clip.open(audioStream);
 		
 			clip.addLineListener( new LineListener() {
 				@Override
 				public void update(LineEvent evt) {
 					if (evt.getType() == LineEvent.Type.STOP) {
 				    evt.getLine().close();
 				  }
 				}
 			});
 			
 			clip.start();
 		} catch (UnsupportedAudioFileException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (LineUnavailableException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void play(int times) {
 		clip.loop(times - 1); //The Clip method for looping states 0 will play it once)
 	}
 	
 	public void loop() {
 		clip.loop(Clip.LOOP_CONTINUOUSLY);
 	}
 	
 	public void stop() {
 		if (clip.isRunning()) {
 			clip.stop();
 		}
 	}
 	
 	public File getSoundFile() {
 		return soundFile;
 	}
 	
 	public void setVolume(float newVolume) {
 		FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
 		volume.setValue(newVolume);
 	}
 	
 	
 	/*
 	 * This class is used to play the SoundEffect in a separate
 	 * thread so that the main application isn't interrupted.
 	 */
 	private class PlayThread extends Thread {
 		
 		public void run() {
 			try {
 				audioStream = AudioSystem.getAudioInputStream(soundFile);
 				
 				clip = AudioSystem.getClip();
 				clip.open(audioStream);
 			
 				clip.addLineListener( new LineListener() {
 					@Override
 					public void update(LineEvent evt) {
 						 if (evt.getType() == LineEvent.Type.STOP) {
 					     evt.getLine().close();
 					   }
 					}
 				});
 				
 				clip.start();
 			} catch (UnsupportedAudioFileException e) {
 				System.out.println("Unsupported audio file: " + soundFile.getName());
 				e.printStackTrace();
 			} catch (IOException e) {
 				System.out.println("Error reading the file: " + soundFile.getName());
 				e.printStackTrace();
 			} catch (LineUnavailableException e) {
 				System.out.println("Problem playing sound file: " + soundFile.getName() + ". Line was unavailable for playback.");
 				e.printStackTrace();
 			}
 			
 		}
 
 	}
 	
 }

 package com.github.a2g.core.platforms.java;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.Line;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 import com.github.a2g.core.interfaces.ISound;
 
 public class SoundForJava
 implements ISound
 {
 
 	double durationInSeconds;
 	String location;
 	Clip clip;
 
 	SoundForJava(String location)
 	{
 		this.location = location;
 		File file = new File(location);
 		long audioFileLength = file.length();
 		AudioInputStream audioInputStream;
 		try {
 			audioInputStream = AudioSystem.getAudioInputStream(file);
 			AudioFormat format = audioInputStream.getFormat();
 
 			int frameSize = format.getFrameSize();
 			float frameRate = format.getFrameRate();
 			durationInSeconds = (audioFileLength / (frameSize * frameRate));
 
 		} catch (UnsupportedAudioFileException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void play() {
 	
 		//can use this to do mp3
 		//http://stackoverflow.com/questions/3046669/how-do-i-get-a-mp3-files-total-time-in-java
 
 		// can use this if want to ship in jar file:
 		// -get the sound file as a resource out of my jar file;
 		// -the sound file must be in the same directory as this class file.
 		// -the input stream portion of this recipe comes from a javaworld.com article.
 		File file = new File(location);
 		long audioFileLength = file.length();
 		AudioInputStream audioInputStream;
 		try {
 			audioInputStream = AudioSystem.getAudioInputStream(file);
 
 			AudioFormat format = audioInputStream.getFormat();
 
 			int frameSize = format.getFrameSize();
 			float frameRate = format.getFrameRate();
 			durationInSeconds = (audioFileLength / (frameSize * frameRate));
 
 
 			Line.Info linfo = new Line.Info(Clip.class);
 			Line line = AudioSystem.getLine(linfo);
 			clip = (Clip) line;
 			//clip.addLineListener(this);
 			clip.open(audioInputStream);
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
 
 	@Override
 	public double getDuration() 
 	{
 		return durationInSeconds;
 	}
 
 	@Override
 	public void stop() {
		clip.stop();
 	}
 
 }

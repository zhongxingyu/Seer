 package com.maspain.chitchat;
 
 import java.applet.Applet;
 import java.applet.AudioClip;
import java.io.File;
import java.net.MalformedURLException;
 
 
 public class Sound {
 	
	private static String path = "res/sounds/";
	private File wavFile;
 	private AudioClip clip;
 	
 	public static Sound sound_beeps = new Sound(path + "sound_beeps.wav");
 	public static Sound sound_chirp = new Sound(path + "sound_chirp.wav");
 	public static Sound sound_click = new Sound(path + "sound_click.wav");
 	public static Sound sound_ding = new Sound(path + "sound_ding.wav");
 	public static Sound sound_glitch = new Sound(path + "sound_glitch.wav");
 	public static Sound sound_pop = new Sound(path + "sound_pop.wav");
 	public static Sound sound_snap = new Sound(path + "sound_snap.wav");
 	
 	public Sound(String fileName) {
		wavFile = new File(fileName); 
		try {
			clip = Applet.newAudioClip(wavFile.toURI().toURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
 	}
 	
 	public void play() {
 		clip.play();
 	}
 	
 	public void loop() {
 		clip.loop();
 	}
 	
 	public void stop() {
 		clip.stop();
 	}
 }

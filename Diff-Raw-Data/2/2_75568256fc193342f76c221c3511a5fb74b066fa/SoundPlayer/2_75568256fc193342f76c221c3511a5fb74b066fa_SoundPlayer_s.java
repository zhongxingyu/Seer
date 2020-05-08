 package com.forrestpruitt.texas;
 
 import java.applet.Applet;
 import java.applet.AudioClip;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 
 
 public class SoundPlayer
 {
 	
 	//Set up the audio files to be used
 	//Loads them into memory ready to be played by SoundPlayer.playSound();
	public static final File sound_betting = new File("sounds/betting");
 	public static final File sound_bonusEnd = new File("sounds/bonusEnd.wav");
 	public static final File sound_shuffle = new File("sounds/shuffle.wav");
 	public static final File sound_cardFanning = new File("sounds/cardFanning.wav");
 	public static final File sound_flop = new File("sounds/flop.wav");
 	public static final File sound_turn = new File("sounds/turn.wav");
 	public static final File sound_fold = new File("sounds/fold.wav");
 	public static final File sound_river = sound_turn;
 	public static final File sound_win = new File("sounds/win.wav");
 	public static final File sound_lose = new File("sounds/lose.wav");
 
 	public static synchronized void playSound(File sound) 
 	{
 	    	  AudioClip clip;
 	    	  File fileClip = sound;
 	    	  URL url = null;
 	    	  try
 	    	  {
 	    		  URI uri = fileClip.toURI();
 	    		  url = uri.toURL();
 	    	      clip = Applet.newAudioClip(url);
 	    	      clip.play();
 	    	  }
 	    	  catch (MalformedURLException e){e.printStackTrace();}
 	    	  
 	}
 }

 package propinquity;
 
 import processing.core.PApplet;
 import ddf.minim.*;
 
 /**
  * Handles sound content initialization and usage.
  * 
  * @author Stephane Beniak
  */
 public class Sounds {
 
 	public static final String SONG_FOLDER = "songs/";
 	public static final int BUFFER_SIZE = 2048;
 	public static final boolean MUTE = false;
 
 	AudioPlayer complete;
 	AudioPlayer negativeCoop;
 	AudioPlayer negativeP1;
 	AudioPlayer negativeP2;
 	AudioPlayer song;
 
 	Minim minim;
 
 	/**
 	 * Setup the Minim audio manager.
 	 * 
	 * @param parent
 	 */
 	public Sounds(PApplet parent) {
 		minim = new Minim(parent);
 	}
 
 	/**
 	 * Load common sound content.
 	 */
 	public void loadCommonContent() {
 
 		complete = minim.loadFile("sounds/comp.mp3", BUFFER_SIZE);
 		complete.setGain(5);
 
 		negativeCoop = minim.loadFile("sounds/neg.mp3", BUFFER_SIZE);
 		negativeP1 = minim.loadFile("sounds/neg1.mp3", BUFFER_SIZE);
 		negativeP2 = minim.loadFile("sounds/neg2.mp3", BUFFER_SIZE);
 	}
 
 	/**
 	 * Load the song for the current level.
 	 */
 	public void loadSong(String file) {
 
 		song = minim.loadFile(SONG_FOLDER + file, BUFFER_SIZE);
 	}
 
 }

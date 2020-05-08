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
 
 	Minim minim;
 
 	AudioSample bubbleLow, bubbleHigh;
 	AudioSample whoosh;
 	AudioSample whooshBubble;
 	AudioSample gong;
 	AudioSample crash;
 	AudioSample dingding;
 	AudioSample orbwon;
 	AudioSample monsterswon;
 
 	AudioSample heartBeat;
 	AudioSample ekgStart;
 	AudioSample flatline;
 	AudioSample inhale;
 	AudioSample exhale;
 
 	/**
 	 * Setup the Minim audio manager.
 	 * 
 	 * @param parent
 	 */
 	public Sounds(PApplet parent) {
 		minim = new Minim(parent);
 
 		bubbleLow = minim.loadSample("sounds/bubble350Hz.mp3", BUFFER_SIZE);
 		bubbleLow.setGain(-2);
 		bubbleHigh = minim.loadSample("sounds/bubble600Hz.mp3", BUFFER_SIZE);
 		bubbleHigh.setGain(-2);
 
 		whoosh = minim.loadSample("sounds/whoosh.mp3", BUFFER_SIZE);
 
 		whooshBubble = minim.loadSample("sounds/whooshbubble.mp3", BUFFER_SIZE);
 		gong = minim.loadSample("sounds/gong.mp3", BUFFER_SIZE);
		crash = minim.loadSample("sounds/crash.mp3", BUFFER_SIZE);
 		orbwon = minim.loadSample("sounds/victory_humans.mp3", BUFFER_SIZE);
 		monsterswon = minim.loadSample("sounds/victory_monsters.mp3", BUFFER_SIZE);
 		dingding = minim.loadSample("sounds/dingding.mp3", BUFFER_SIZE);
 
 		heartBeat = minim.loadSample("sounds/beat.mp3", BUFFER_SIZE);
 
 		ekgStart = minim.loadSample("sounds/ecg_slow.mp3", BUFFER_SIZE);
 		flatline = minim.loadSample("sounds/flatline.mp3", BUFFER_SIZE);
 
 		inhale = minim.loadSample("sounds/inhale.mp3", BUFFER_SIZE);
 		exhale = minim.loadSample("sounds/exhale.mp3", BUFFER_SIZE);
 	}
 
 	/**
 	 * Load the song for the current level.
 	 */
 	public AudioPlayer loadSong(String file) {
 		return minim.loadFile(SONG_FOLDER + file, BUFFER_SIZE);
 	}
 
 	public AudioSample getBubbleLow() {
 		return bubbleLow;
 	}
 
 	public AudioSample getBubbleHigh() {
 		return bubbleHigh;
 	}
 
 	public AudioSample getWhoosh() {
 		return whoosh;
 	}
 
 	public AudioSample getWhooshBubble() {
 		return whooshBubble;
 	}
 
 	public AudioSample getGong() {
 		return gong;
 	}
 
 	public AudioSample getCrash() {
 		return crash;
 	}
 
 	public AudioSample getOrbWon() {
 		return orbwon;
 	}
 
 	public AudioSample getMonstersWon() {
 		return monsterswon;
 	}
 
 	public AudioSample getDingDing() {
 		return dingding;
 	}
 
 	public AudioSample getHeartBeat() {
 		return heartBeat;
 	}
 
 	public AudioSample getFlatline() {
 		return flatline;
 	}
 
 	public AudioSample getEKGStart() {
 		return ekgStart;
 	}
 
 	public AudioSample getInhale() {
 		return inhale;
 	}
 
 	public AudioSample getExhale() {
 		return exhale;
 	}
 
 }

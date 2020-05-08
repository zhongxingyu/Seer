 package com.github.joakimpersson.tda367.audio;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.lwjgl.openal.AL;
 import org.newdawn.slick.SlickException;
 
 import com.github.joakimpersson.tda367.model.constants.EventType;
 import com.github.joakimpersson.tda367.model.constants.Parameters;
 
 /**
  * 
  * @author Viktor Anderling
  *
  * A Singelton class for handling all the sounds in the game, witch includes
  * storing them and the volume-options.
  *
  */
 public class SoundHandler {
 	
 	/**
 	 * The volume of the background music in the game, witch is a value from 0 to 1;
 	 */
 	private float bgmVolume;
 	
 	/**
 	 * The volume of the sound effects in the game, witch is a value from 0 to 1;
 	 */
 	private float sfxVolume;
 	
 	private SoundEffect bombExplode;
 	private SoundEffect bombPlaced;
 	private BackgroundMusic titleTheme;
 	private BackgroundMusic battleSong1;
 	private BackgroundMusic preInterlude;
 	private BackgroundMusic interlude;
 	private SoundEffect menuNavigate;
 	private SoundEffect menuAction;
 	private SoundEffect errorSound;
 	private SoundEffect countDown;
 	
 	/**
 	 * The music that is currently playing.
 	 */
 	private BackgroundMusic playingMusic;
 	
 	/**
 	 * The timer that consecutively ticks through the playlist.
 	 */
 	private Timer playlistTimer;
 	
 	/**
 	 * The instance of this class.
 	 */
 	private static SoundHandler instance = null;
 	
 	/**
 	 * This method returns the instance of this class.
 	 * 
 	 * @return This instance.
 	 */
 	public static SoundHandler getInstance() {
 		if(instance == null) {
 			instance = new SoundHandler();
 		}
 		return instance;
 	}
 	
 	/**
 	 * This constructor initiates the sounds and sets 
 	 * the SFX and BGM to the initial volume.
 	 */
 	private SoundHandler() {
 		initiateSounds();
 		Parameters par = Parameters.INSTANCE;
 		setSFXVolume(par.getInitSFXVolume());
 		setBGMVolume(par.getInitBGMVolume());
 	}
 	
 	/**
 	 * This class initiate and saves all the sounds from the resources folder.
 	 */
 	public void initiateSounds() {
 		try {
 			bombPlaced = new SoundEffect("res/sounds/PlaceBomb1.wav", 0.7f);
 			bombExplode = new SoundEffect("res/sounds/Bomb1.wav", 0.55f);
 			titleTheme = new BackgroundMusic("res/sounds/bg1.wav", 0.7f);
 			battleSong1 = new BackgroundMusic("res/sounds/BattleSong1.wav", 1.0f);
 			menuNavigate = new SoundEffect("res/sounds/MenuNavigate.wav", 0.75f);
 			menuAction = new SoundEffect("res/sounds/MenuAction.wav", 0.75f);
 			errorSound = new SoundEffect("res/sounds/ErrorSound.wav", 0.7f);
 			preInterlude = new BackgroundMusic("res/sounds/PreInterlude.wav", 0.5f);
 			interlude = new BackgroundMusic("res/sounds/Interlude1.wav", 0.35f);
 			countDown = new SoundEffect("res/sounds/CountDown.wav", 0.25f);
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * This method plays the sound that corresponds to the given SoundType.
 	 * If a BackgroundMusic is previously playing, it will stop playing
 	 * before the next will start.
 	 * If a the same backgroundMusic is playing 
 	 * 
 	 * @param soundType The given SoundType.
 	 */
 	public void playSound(EventType soundType) {
 		GameSound sound = chooseSound(soundType);
 		playSound(sound);
 		
 	}
 	
 	/**
 	 * See playSound(EventType soundType).
 	 */
 	private void playSound(GameSound sound) {
 		if(sound instanceof SoundEffect) {
 			sound.play(sfxVolume);
 		} else {
 			if(sound != playingMusic) {
 				if(playlistTimer != null) {
 					playlistTimer.cancel();
 					playlistTimer = null;
 				}
 				if(playingMusic != null) {
 					playingMusic.stop();
 				}
 				sound.play(bgmVolume);
 				playingMusic = (BackgroundMusic) sound;
 			}
 		}	
 	}
 	
 	/**
 	 * Plays the the music that corresponds to the eventTypes in the given list.
 	 * When one sound has stopped playing, the next one will start.
 	 * The last song in the list will loop, so it is possible to use this method to
 	 * play one or several pre-songs before the main song. 
 	 * 
 	 * @param playlist
 	 * 					The given playlist.
 	 * @precon
 	 * 			The list contains only EventTypes that corresponds to a BackgroundMusic.
 	 */
 	public void playList(List<EventType> playlist) {
 
 		LinkedList<BackgroundMusic> soundList = new LinkedList<BackgroundMusic>();
 		for(EventType et : playlist) {
 			if(chooseSound(et) instanceof BackgroundMusic) {
 				soundList.add((BackgroundMusic) chooseSound(et));
 			}
 		}
 		if(soundList.size() > 1) {
 			stopCurrentlyPlayingMusic();
 			playlistTimer = new Timer();
 			playlistTimer.schedule(new PlaylistTask(soundList), soundList.get(0).getDuration());
 			playingMusic = soundList.removeFirst();
 			playingMusic.play(bgmVolume);
 		} else {
 			playSound(soundList.getFirst());
 		}
 	}
 	
 	/**
 	 * This method plays the sound that corresponds to the given SoundType.
 	 * 
 	 * @param soundType The given SoundType.
 	 */
 	public void stopSound(EventType soundType) {
 		chooseSound(soundType).stop();
 	}
 	
 	/**
 	 * This method stops all currently playing background-music.
 	 */
 	public void stopCurrentlyPlayingMusic() {
 		if(playingMusic != null) {
 			playingMusic.stop();
 			playingMusic = null;
 		}
 	}
 	
 	/**
 	 * This method checks if the sound that corresponds to the given SoundType
 	 * is playing.
 	 * 
 	 * @param soundType The given SoundType.
 	 * @return True if the sound is playing, else false.
 	 */
 	public boolean isSoundPlaying(EventType soundType) {
 		return chooseSound(soundType).isPlaying();	
 	}
 	
 	/**
 	 * This method is used internally to choose between different IGameSounds.
 	 * 
 	 * @param soundType
 	 * @return The IGameSound that corresponds to the given SoundType.
 	 */
 	private GameSound chooseSound(EventType soundType) {
 		switch(soundType) {
 		case BOMB_PLACED:
 			return bombPlaced;
 		case BOMB_EXPLODED:
 			return bombExplode;
 		case MENU_NAVIGATE:
 			return menuNavigate;
 		case MENU_ACTION:
 			return menuAction;
 		case ERROR:
 			return errorSound;
 		case TITLE_SCREEN:
 			return titleTheme;
 		case BATTLE_SCREEN:
 			return battleSong1;
 		case ROUND_ENDED:
 			return preInterlude;
 		case UPGRADE_SCREEN:
 			return interlude;
 		case COUNT_DOWN:
 			return countDown;
 		default:
 			return null;
 		}
 	}
 	
	// TODO fix input for setVolume.
	
 	/**
 	 * This method sets the volume for all the background music.
 	 * 
 	 * @param volume A float number between 0 and 1.
 	 */
 	public void setBGMVolume(float volume) {
		this.bgmVolume = volume;
 	}
 	
 	/**
 	 * This method sets the volume for all the sound-effect music.
 	 * 
 	 * @param volume A float number between 0 and 1.
 	 */
 	public void setSFXVolume(float volume) {
		this.sfxVolume = volume;
 	}
 	
 	
 	/**
 	 * This method destroys the openAL. Use before closing the application.
 	 */
 	public void destroy() {
 		AL.destroy();
 	}
 	
 	/**
 	 * The timer task that is scheduled for the playlistTimer.
 	 * Its run() method calls on a new timer task until all songs have played.
 	 */
 	private class PlaylistTask extends TimerTask {
 		private LinkedList<BackgroundMusic> playlist;
 		
 		public PlaylistTask(LinkedList<BackgroundMusic> playlist) {
 			this.playlist = playlist;
 		}
 
 		@Override
 		public void run() {
 			if(!playlist.isEmpty()) {
 				stopCurrentlyPlayingMusic();
 				BackgroundMusic sound = playlist.removeFirst();
 				sound.play(bgmVolume);
 				playingMusic = sound;
 				playlistTimer = new Timer();
 				playlistTimer.schedule(new PlaylistTask(playlist), sound.getDuration());
 			} else {
 				playlistTimer = null;
 			}
 		}
 	}
 }

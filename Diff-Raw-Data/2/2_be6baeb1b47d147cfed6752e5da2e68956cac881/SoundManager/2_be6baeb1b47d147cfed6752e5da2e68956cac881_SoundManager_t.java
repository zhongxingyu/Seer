 package cs309.a1.shared;
 
 import static cs309.a1.shared.Constants.LANGUAGE;
 import static cs309.a1.shared.Constants.LANGUAGE_CANADA;
 import static cs309.a1.shared.Constants.LANGUAGE_FRANCE;
 import static cs309.a1.shared.Constants.LANGUAGE_GERMAN;
 import static cs309.a1.shared.Constants.LANGUAGE_UK;
 import static cs309.a1.shared.Constants.LANGUAGE_US;
 import static cs309.a1.shared.Constants.PREFERENCES;
 import static cs309.a1.shared.Constants.SOUND_EFFECTS;
 import static cs309.a1.shared.Constants.SPEECH_VOLUME;
 
 import java.util.Locale;
 import java.util.Random;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.res.TypedArray;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.SoundPool;
 import android.speech.tts.TextToSpeech;
 import android.util.Log;
 import cs309.a1.R;
 
 /**
  * This class is used to control the all of the sound in the game. It has
  * several methods for playing specific sounds and for using the Text To Speech
  * feature of android
  */
 public class SoundManager {
 
 	/**
 	 * The Logcat Debug tag
 	 */
 	private static final String TAG = SoundManager.class.getName();
 
 	/**
 	 * This has a bunch of sounds that can be played
 	 */
 	private static SoundPool soundpool;
 
 	/**
 	 * This will play music or other sounds
 	 */
 	private static MediaPlayer mediaplayer;
 
 	/**
 	 * This is how we will read the names of the players out loud
 	 */
 	private static TextToSpeech tts;
 
 	/**
 	 * This is how we can tell if the TTS has been initialized
 	 */
 	boolean isTTSInitialized;
 
 	/**
 	 * This will be obtained from the shared preference to see if the player wants sound fx
 	 */
 	private boolean isSoundFXOn = true;
 
 	/**
 	 * This will be obtained from the shared preference to see if the player wants to have TTS
 	 */
 	private boolean isTTSOn = true;
 
 	/**
 	 * This is how we will get the settings that the user has set.
 	 */
 	private SharedPreferences sharedPreferences;
 
 	/**
 	 * Strings to be spoken when it is a player's turn
 	 */
 	private final String[] playerTurnPrompt;
 
 	/**
 	 * array to store the soundpool IDs for the draw card sounds
 	 */
 	private int[] drawCardSounds;
 
 	/**
 	 * array to store the soundpool IDs for the play card sounds
 	 */
 	private int[] playCardSounds;
 
 	/**
 	 * array to store the soundpool IDs for the shuffle card sounds
 	 */
 	private int[] shuffleCardSounds;
 
 	/**
 	 * this will initialize the SoundManager by initializing all the sound FX
 	 * and the TTS object and obtaining user sound preferences
 	 * 
 	 * @param context The context of the class to use the SoundManager
 	 */
 	public SoundManager(Context context) {
 		sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_WORLD_WRITEABLE);
 		isSoundFXOn = sharedPreferences.getBoolean(SOUND_EFFECTS, true);
 		isTTSOn = sharedPreferences.getBoolean(SPEECH_VOLUME, true);
 
 		soundpool = new SoundPool(5, AudioManager.STREAM_MUSIC, 100);
 
 		// Initialize the strings to speak when it is a users turn
 		playerTurnPrompt = context.getResources().getStringArray(R.array.phrases);
 
 		// draw card sounds
 		TypedArray drawSounds = context.getResources().obtainTypedArray(R.array.drawCard);
 		drawCardSounds = new int[drawSounds.length()];
 		for (int i = 0; i < drawCardSounds.length; i++) {
 			drawCardSounds[i] = soundpool.load(context, drawSounds.getResourceId(i, 0), 1);
 		}
 
 		// card playing sounds
 		TypedArray playSounds = context.getResources().obtainTypedArray(R.array.playCard);
 		playCardSounds = new int[playSounds.length()];
 		for (int i = 0; i < playCardSounds.length; i++) {
 			playCardSounds[i] = soundpool.load(context, playSounds.getResourceId(i, 0), 1);
 		}
 
 		// card shuffling sounds
 		TypedArray shuffleSounds = context.getResources().obtainTypedArray(R.array.shuffle);
 		shuffleCardSounds = new int[shuffleSounds.length()];
 		for (int i = 0; i < shuffleCardSounds.length; i++) {
 			shuffleCardSounds[i] = soundpool.load(context, shuffleSounds.getResourceId(i, 0), 1);
 		}
 
 		mediaplayer = new MediaPlayer();
 		MyInitListener mil = new MyInitListener();
 		tts = new TextToSpeech(context.getApplicationContext(), mil);
 		isTTSInitialized = false;
 		mediaplayer = MediaPlayer.create(context, R.raw.sound_test);
 	}
 
 	/**
 	 * plays the sound of a card being drawn. plays various sounds.
 	 */
 	public void drawCardSound() {
 		if (isSoundFXOn) {
 			Random rand = new Random();
 			int i = Math.abs(rand.nextInt() % drawCardSounds.length);
 			soundpool.play(drawCardSounds[i], 1, 1, 1, 0, 1);
 		}
 	}
 
 	/**
 	 * plays the sound of a card being played. plays various sounds.
 	 */
 	public void playCardSound() {
 		if (isSoundFXOn) {
 			Random rand = new Random();
 			int i = Math.abs(rand.nextInt() % playCardSounds.length);
 			soundpool.play(playCardSounds[i], 1, 1, 1, 0, 1);
 		}
 	}
 
 	/**
 	 * plays the sound of a card being played. plays various sounds.
 	 */
 	public void shuffleCardsSound() {
 		if (isSoundFXOn) {
 			Random rand = new Random();
 			int i = Math.abs(rand.nextInt() % shuffleCardSounds.length);
 			soundpool.play(shuffleCardSounds[i], 1, 1, 1, 0, 1);
 		}
 	}
 
 	/**
 	 * This will use TextToSpeech to say the string out loud
 	 *
 	 * @param words this string will be read aloud
 	 */
 	public void speak(String words) {
 		if (isTTSInitialized && isTTSOn) {
 			tts.speak(words, TextToSpeech.QUEUE_FLUSH, null);
 		}
 	}
 
 	/**
 	 * This function will tell a player it is their turn using various strings
 	 * 
 	 * @param name the name of the player
 	 */
 	public void sayTurn(String name) {
 		Random rand = new Random();
 		int i = Math.abs(rand.nextInt() % playerTurnPrompt.length);
		speak(playerTurnPrompt[i].replace("%s", name) );
 	}
 
 	/**
 	 * This will play the theme music
 	 */
 	public void playMusic() {
 		if (isSoundFXOn) {
 			mediaplayer.seekTo(0);
 			mediaplayer.start();
 		}
 	}
 
 	/**
 	 * This will stop the music from playing
 	 */
 	public void stopMusic() {
 		if (mediaplayer.isPlaying()) {
 			mediaplayer.stop();
 		}
 	}
 
 	/**
 	 * This should make all sounds stop playing
 	 */
 	public void stopAllSound() {
 		if (mediaplayer.isPlaying()) {
 			mediaplayer.stop();
 		}
 		soundpool.autoPause();
 		tts.stop();
 	}
 
 	/**
 	 * this class will have the onInit method called when the TTS has been
 	 * initialized then this method will finish the setup of TTS including
 	 * getting the "dialect" or "Locale" of the voice
 	 */
 	private class MyInitListener implements TextToSpeech.OnInitListener {
 
 		@Override
 		public void onInit(int status) {
 			if (status == TextToSpeech.SUCCESS) {
 				// get the user preference
 				String lang = sharedPreferences.getString(LANGUAGE, LANGUAGE_US);
 				int langResult = -1;
 
 				if (lang.equals(LANGUAGE_US)) { // default
 					langResult = tts.setLanguage(Locale.US);
 				} else if (lang.equals(LANGUAGE_GERMAN)) {
 					langResult = tts.setLanguage(Locale.GERMAN);
 				} else if (lang.equals(LANGUAGE_FRANCE)) {
 					langResult = tts.setLanguage(Locale.FRANCE);
 				} else if (lang.equals(LANGUAGE_CANADA)) {
 					langResult = tts.setLanguage(Locale.CANADA);
 				} else if (lang.equals(LANGUAGE_UK)) {
 					langResult = tts.setLanguage(Locale.UK);
 				}
 
 				if (langResult == TextToSpeech.LANG_MISSING_DATA
 						|| langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
 					if (Util.isDebugBuild()) {
 						Log.d(TAG, "Language not available");
 					}
 				} else {
 					// let us know that it is safe to use it now
 					isTTSInitialized = true;
 				}
 			} else if (Util.isDebugBuild()) {
 				Log.d(TAG, "Text To Speech did not initialize correctly");
 			}
 		}
 	}
 }

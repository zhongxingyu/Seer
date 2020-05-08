 package org.techbridgeworld.bwt.student;
 
 import java.util.Date;
 import java.util.Locale;
 import java.util.Random;
 
 import javaEventing.interfaces.Event;
 import javaEventing.interfaces.GenericEventListener;
 
 import org.techbridgeworld.bwt.api.BWT;
 import org.techbridgeworld.bwt.api.events.BoardEvent;
 import org.techbridgeworld.bwt.api.events.ChangeCellEvent;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.speech.tts.TextToSpeech;
 import android.support.v4.view.GestureDetectorCompat;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.widget.TextView;
 
 public class AnimalGame extends Activity implements TextToSpeech.OnInitListener {
 
 	private TextToSpeech tts;
 
 	private static final int SWIPE_MIN_DISTANCE = 120;
 	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
 	private GestureDetectorCompat detector;
 	private Random generator = new Random(new Date().getTime());
 
 	private TextView animal_game;
 
 	private String[] options;
 	private int numOptions = 2;
 	private int currentOption = 0;
 
 	private final BWT bwt = new BWT(this, AnimalGame.this);
 	private GenericEventListener AnimalListener, ChangeListener;
 
 	private String currAnimal = "";
 	private String[] animals = { "bee", "camel", "cat", "cow", "dog", "horse",
 			"pig", "rooster", "sheep", "zebra" };
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.animal_game);
 
 		options = new String[numOptions];
 		options[0] = getResources().getString(R.string.replay);
 		options[1] = getResources().getString(R.string.delete);
 
 		animal_game = (TextView) findViewById(R.id.animal_game);
 
 		tts = new TextToSpeech(this, this);
 		detector = new GestureDetectorCompat(this, new MyGestureListener());
 
 		bwt.init();
 	}
 	
     @Override
     public void onDestroy() {
     	// Stop text-to-speech
         if (tts != null) {
             tts.stop();
             tts.shutdown();
         }
         super.onDestroy();
     }
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		this.detector.onTouchEvent(event);
 		return super.onTouchEvent(event);
 	}
 
 	private String getCurr() {
 		return currAnimal;
 	}
 
 	private void regenerate() {
 		currAnimal = animals[generator.nextInt(animals.length)];
 	}
 
 	@Override
 	public void onInit(int status) {
 		bwt.start();
 		if (status == TextToSpeech.SUCCESS) {
 			int result = tts.setLanguage(Locale.US);
 			if (result == TextToSpeech.LANG_MISSING_DATA
 					|| result == TextToSpeech.LANG_NOT_SUPPORTED)
 				Log.e("TTS", "This language is not supported");
 			else{
 				bwt.initializeEventListeners();
 				bwt.startTracking();
 				
 				runGame();
 			}
 		} else
 			Log.e("TTS", "Initilization Failed!");
 	}
 
 	private void runGame() {
 		regenerate();
 		speakOutQueue("Spell the word " + getCurr() + ".");
 
 		createListeners();
 	}
 
 	private void createListeners() {
 		// Say last typed character if available
 		ChangeListener = new GenericEventListener() {
 			public void eventTriggered(Object arg0, Event arg1) {
 				bwt.defaultChangeCellHandler(arg0, arg1);
 				ChangeCellEvent e = (ChangeCellEvent) arg1;
 				if (e.getOldCell() == -1)
 					return;
 
 				char last = e.getOldCellGlyph();
 				int cellState = e.getOldCellBits();
 				Log.i("Animal Game", "Just typed character " + last + " ("
 						+ Integer.toBinaryString(cellState) + ") to cellInd "
 						+ e.getOldCell());
 				speakOutQueue(last + ".");
 			}
 		};
 		
 		// Handles the checking and comparing of the expected word vs user input
 		AnimalListener = new GenericEventListener() {
 			@Override
 			public void eventTriggered(Object arg0, Event arg1) {
 				String goal = getCurr();
 				
 
 				/** FOR DEBUGGING **/
 				bwt.defaultBoardHandler(arg0, arg1);
 				BoardEvent e = (BoardEvent) arg1;
 
 				String trial = bwt.viewTrackingAsString();
 				
 				Log.d("Animal Game", "Trial viewing: " + trial + "; Goal: "
 						+ goal);
 
 				int cellstate = e.getCellState();
 				Log.i("Animal Game", "Current cell (" + e.getCellInd()
 						+ ") bits: " + Integer.toBinaryString(cellstate));
 				/*********************/
 				
 				// Matches
 				if (bwt.currentMatchesString(goal)) {
 					bwt.clearAllTracking();
 					regenerate();
 					speakOutReplace("Good. Spell the word " + getCurr() + ".");
 				}
 				// Goes off track of goal
 				else if (bwt.offTrackFromString(goal)) {
 					bwt.clearAllTracking();
 					speakOutReplace("No. Try again.");
 				}
 			}
 		};
 
 		bwt.replaceListener("onBoardEvent", AnimalListener);
 		bwt.replaceListener("onChangeCellEvent", ChangeListener);
 	}
 
 	// Add a string to the text-to-speech queue.
 	private void speakOutQueue(String text) {
 		tts.speak(text, TextToSpeech.QUEUE_ADD, null);
 	}
 
 	// Replace the text-to-speech queue with the given string.
 	private void speakOutReplace(String text) {
 		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
 	}
 
 	// Listen for swipes, and enact the appropriate menu item if necessary.
 	class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
 
 		@Override
 		public boolean onFling(MotionEvent event1, MotionEvent event2,
 				float velocityX, float velocityY) {
 			// Swipe up
 			if (event1.getY() - event2.getY() > SWIPE_MIN_DISTANCE
 					&& Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
 				Intent intent = new Intent(AnimalGame.this, GameActivity.class);
 				bwt.stopTracking();
 				bwt.removeEventListeners();
 		        bwt.stop();
 				startActivity(intent);
 			}
 
 			// Swipe left (Rotate left through menu items)
 			else if (event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE
 					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
 				currentOption = (currentOption - 1) % numOptions;
 				if (currentOption == -1)
 					currentOption += numOptions;
 				animal_game.setText(options[currentOption]);
 				animal_game.setContentDescription(options[currentOption]);
 			}
 
 			// Swipe right (Rotate left through menu items)
 			else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE
 					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
 				currentOption = (currentOption + 1) % numOptions;
 				animal_game.setText(options[currentOption]);
 				animal_game.setContentDescription(options[currentOption]);
 			}
 
 			return true;
 		}
 	}
 }

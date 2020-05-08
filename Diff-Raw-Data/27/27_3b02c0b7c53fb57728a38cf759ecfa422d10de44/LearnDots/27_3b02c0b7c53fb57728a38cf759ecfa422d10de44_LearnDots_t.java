 package org.techbridgeworld.bwt.student;
 
 import java.util.Random;
 import javaEventing.EventManager;
 import javaEventing.interfaces.Event;
 import javaEventing.interfaces.GenericEventListener;
 
 import org.techbridgeworld.bwt.api.BWT;
 import org.techbridgeworld.bwt.api.events.BoardEvent;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.speech.tts.TextToSpeech;
 import android.util.Log;
 import android.view.KeyEvent;
 
 public class LearnDots extends Activity {
 
 	private String[] numbers = {"one", "two", "three", "four", "five", "six"};
 
 	private MyApplication application;
 	private TextToSpeech tts;
 	
 	private Random generator = new Random(15239);
 
 	private final BWT bwt = new BWT(this, LearnDots.this);
 	private GenericEventListener DotListener;
 
 	private int currentDot = -1;
 
 	@Override
 	/**
 	 * Runs on creation of the new activity.
 	 */
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.list);
 		
 		application = ((MyApplication) getApplicationContext());
 		tts = application.myTTS;
 		
 		application.currentFile = 0;
 		application.filenames.clear();
 
 		// Initialize the BWT connection.
 		bwt.init();
 		
 		// Start the BWT
 		bwt.start();
 		
 		// Start tracking the state of the BWT
 		bwt.initializeEventListeners();
 		bwt.startTracking();
 		runGame();
 	}
 	
 	@Override
 	public void onPause() {
 		application.clearAudio();
 		bwt.stopTracking();
 		bwt.removeEventListeners();
 		bwt.stop();
 		super.onPause();
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
 
 	/**
 	 * Get a new random dot, and trigger the associated audio.
 	 */
 	private void regenerate(){
 		currentDot = generator.nextInt(6) + 1;
 		//application.clearAudio();
 		// "Good."
 		application.queueAudio(R.string.good);
 		// "Find dot"
 		application.queueAudio(R.string.find_dot);
 		// "[Dot Number]."
 		application.queueAudio(numbers[currentDot-1]);
 	}
 
 	/**
 	 * A private getter for currentDot, so the listener can access it.
 	 * @return curentDot.
 	 */
 	private int getCurrent(){
 		return currentDot;
 	}
 
 	private void runGame(){
 		// Generate the first dot.
 		currentDot = generator.nextInt(6) + 1;
 		application.queueAudio(R.string.find_dot);
		application.queueAudio(numbers[getCurrent() - 1]);
 		application.playAudio();
 		
 		createListener();
 	}
 	
 	private void createListener() {
 		// Listener to detect board input.
 		DotListener = new GenericEventListener(){
 
 			@Override
 			public void eventTriggered(Object arg0, Event event) {
 				Log.d("Learn Dots", "Triggered Board Event");
 				
 				//Do nothing in regards to AltBtn being pressed
 				if(((BoardEvent)event).getCellInd() == -1) {
 					return;
 				}
 				
 				// Cast the given event as a BoardEvent, and get the relevant dot information.
 				int trial = ((BoardEvent) event).getDot();
 				int goal = getCurrent();
 				Log.i("Dot Game", "Just pressed dot " + trial + ". We want dot " + goal + ".");

				bwt.clearTouchedCells();
 				
 				// If they pressed the dot, then pick another dot.
 				if(trial == goal){
 					regenerate();
 				}
 				
 				// Otherwise, tell the user that they are incorrect and repeat the prompt. 
 				else{
 					//application.clearAudio();
 					application.queueAudio(R.string.no);
 					application.queueAudio(R.string.find_dot);
 					application.queueAudio(numbers[currentDot-1]);
 				}
 				application.playAudio();
 			}
 		};
 		
 		// Start the listener. 
 		EventManager.registerEventListener(DotListener, BoardEvent.class);
 	}
 	
 	// If the user presses back, go back properly
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			// If we've started the dot listener, remove it since we're done with it.
 			if(DotListener != null){
 				EventManager.unregisterEventListener(DotListener, BoardEvent.class);
 			}
 			
 			Intent intent = new Intent(LearnDots.this, GameActivity.class);
 			startActivity(intent);
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 }

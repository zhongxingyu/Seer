 package org.techbridgeworld.bwt.student;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Random;
 import javaEventing.interfaces.Event;
 import javaEventing.interfaces.GenericEventListener;
 
 import org.techbridgeworld.bwt.api.BWT;
 import org.techbridgeworld.bwt.api.events.SubmitEvent;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.speech.tts.TextToSpeech;
 import android.util.Log;
 import android.view.KeyEvent;
 
 public class Hangman extends Activity {
 
 	private MyApplication application;
 	private TextToSpeech tts;
 	
 	private Random generator = new Random(new Date().getTime());
 
 	private final BWT bwt = new BWT(this, Hangman.this);
 	private GenericEventListener HangmanListener;
 
 	private String[] numbers = {"one", "two", "three", "four", "five", "six", "seven", "eight"};
 	private String[] wordBank = {};
 	private final int MAX_MISTAKES = 8;
 	
 	private String currWord = "";
 	private int wordBankInd;
 	private int numCorrectLetters;
 	private char[] wordStatus;
 	private ArrayList<Character> guessedBank;
 	private int numMistakes;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.list);
 
 		application = ((MyApplication) getApplicationContext());
 		tts = application.myTTS;
 		
 		application.currentFile = 0;
 		application.filenames.clear();
 		
 		ArrayList<String> arr = application.hangmanWords;
 		if(arr != null) {
 			wordBank = new String[arr.size()];
 			for (int i = 0; i < arr.size(); i++)
 				wordBank[i] = arr.get(i);
 		}
 		
 		bwt.init();
 		bwt.start();
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
 	
 	private void runGame() {
 		wordBankInd = -1;
 		regenerate();
 		createListeners();
 		application.playAudio();
 	}
 
 	private void regenerate() {
 		wordBankInd++;
 		numMistakes = 0;
 		guessedBank = new ArrayList<Character>();
 		
 		if(wordBankInd >= wordBank.length) {
 			//start over if they beat the game
 			wordBankInd = -1;
 			return;
 		}
		
 		//Won't repeat words already done
 		int nextWordInd = generator.nextInt(wordBank.length - wordBankInd) + wordBankInd;
 		currWord = wordBank[nextWordInd];
 		numCorrectLetters = 0;
 		
 		//Tell student: "The new word has N letters"
 		int numLetters = currWord.length();
 		application.queueAudio(R.string.the_new_word);
 		application.queueAudio(numbers[numLetters-1]);
 		application.queueAudio(R.string.letters);

 		//Speak out the dashes
 		wordStatus = new char[numLetters];
 		for (int i = 0; i < numLetters; i++) {
 			wordStatus[i] = '-';
 			application.queueAudio(R.string.dash);
 		}
 		application.queueAudio(R.string.guess_a_letter);
 		
 		
 		//swap strings in array; everything before wordBankInd have been done
 		wordBank[nextWordInd] = wordBank[wordBankInd];
 		wordBank[wordBankInd] = currWord;
 	}
 	
 	/**
 	 * Spells to the user the status of the word they've been guessing,
 	 * where "dash" indicates that they still have yet to guess that character
 	 */
 	private void spellWordStatus() {
 		for (int i = 0; i < currWord.length(); i++) {
 			Character c = wordStatus[i];
 			if (c == '-') {
 				application.queueAudio(R.string.dash);
 			}
 			else {
 				application.queueAudio(c.toString());
 			}
 		}
 	}
 	
 	/**
 	 * Called when game is ready for next letter to be guessed.
 	 * Alerts user of current word status, and number of mistakes made.
 	 */
 	private void promptGuess() {
 		//Update user on status of word
 		application.queueAudio(R.string.so_far);
 		spellWordStatus();
 		
 		//Update user on how many mistakes have been made
 		application.queueAudio(R.string.youve_made);
 		if(numMistakes == 1) {
 			application.queueAudio(R.string.one);
 			application.queueAudio(R.string.mistake);
 		}
 		else {
 			application.queueAudio(((Integer)numMistakes).toString());
 			application.queueAudio(R.string.mistakes);
 		}
 		
 		//Warning of last chance
 		if(numMistakes == MAX_MISTAKES - 1) {
 			application.queueAudio(R.string.but_you_have);
 		}
 			
 		//Prompt another guess
 		application.queueAudio(R.string.guess_a_letter);
 	}
 	
 	/**
 	 * If guessed letter is correct:
 	 * - update wordStatus
 	 * - if word is now complete, move onto next word
 	 * - otherwise, re-prompt
 	 */
 	private void correctGuessHandler(char guessedLetter) {
 		guessedBank.add(guessedLetter);
 		
 		//update wordStatus
 		int start = 0;
 		while (start >= 0 && start < currWord.length()) {
 			int index = currWord.indexOf(guessedLetter, start);
 			if(index < 0) break;
 
 			numCorrectLetters++;
 			wordStatus[index] = guessedLetter;
 			start = index + 1;
 		}
 		
 		//Move onto next word if all the letters are there
 		if(numCorrectLetters == currWord.length()) {
 			application.queueAudio(R.string.good);
 			
 			revealCurrWord();
 			regenerate();
 		}
 		else {
 			promptGuess();
 		}		
 	}
 	
 	/**
 	 * Spell out the correct answer for the user
 	 */
 	private void revealCurrWord() {
 		application.queueAudio(R.string.the_correct_answer_was);
 		application.queueAudio(currWord);
 		for (int i = 0; i < currWord.length(); i++) {
 			Character ch = currWord.charAt(i);
 			application.queueAudio(ch.toString());
 		}
 				
 	}
 	
 	/**
 	 * Handle wrong guesses accordingly, whether they've met the max
 	 * mistakes or not.
 	 */
 	private void wrongGuessHandler() {
 		numMistakes++;
 		application.queueAudio(R.string.no);
 		
 		//Reached max number of mistakes, move onto new word
 		if(numMistakes == MAX_MISTAKES) {
 			revealCurrWord();
 			regenerate();
 		}
 		//Can still guess another letter
 		else {
 			promptGuess();	
 		}
 	}
 
 	private void createListeners() {
 		// Handles the checking and comparing of the expected word vs user input
 		HangmanListener = new GenericEventListener() {
 			@Override
 			public void eventTriggered(Object arg0, Event arg1) {
 				bwt.defaultSubmitHandler(arg0, arg1);
 				SubmitEvent e = (SubmitEvent) arg1;
 				Log.d("Hangman", "Triggered Submit Event");
 
 				//Grab submitted character and clear board and audio				
 				int cellInd = e.getCellInd();
 				char glyphAtCell = bwt.getGlyphAtCell(cellInd);
 				bwt.clearTouchedCells();
 				//application.clearAudio();
 				
 				//Input wasn't a Braille character --> invalid input
 				if(glyphAtCell == '-') {
 					application.queueAudio(R.string.invalid_input);
 					wrongGuessHandler();
 					application.playAudio();
 					return;
 				}
 				
 				//Speak out character inputed
 				application.queueAudio(((Character)glyphAtCell).toString());
 				
 				//Check for already-guessed letters
 				if(guessedBank.contains(glyphAtCell)) {
 					application.queueAudio(R.string.youve_already);
 					promptGuess();
 					application.playAudio();
 					return;
 				}
 				else guessedBank.add(glyphAtCell);
 				
 				// Guesses incorrectly
 				if (currWord.indexOf(glyphAtCell) < 0) {
 					wrongGuessHandler();
 				}
 				// Guesses correctly
 				else {
 					correctGuessHandler(glyphAtCell);
 				}
 				application.playAudio();
 			}
 		};
 		
 		bwt.replaceListener("onSubmitEvent", HangmanListener);
 	}
 
 	// If the user presses back, go to the home screen
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 	        Intent intent = new Intent(Hangman.this, GameActivity.class);
 			startActivity(intent);
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 }

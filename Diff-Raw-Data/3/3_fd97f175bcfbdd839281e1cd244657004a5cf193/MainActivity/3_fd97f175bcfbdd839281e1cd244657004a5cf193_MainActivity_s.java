 package com.cssdpu.hangman;
 
 import java.util.Random;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	
 	private EditText mEditText;
 	private TextView mGuessedTextView, mCorrectlyGuessedTextView;
 	private String mGuessedString = "";
 	
 	private String mCurrentWord;
 
 	private static final String[] WORDS = new String[] {
 		"Belly", "Runner", "Chicago", "DePaul", "Fake"
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		resetGame();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	    case R.id.action_restart:
 	    	resetGame();
 	        return true;
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	private void resetGame() {
 		mCurrentWord = null;
 		mGuessedString = "";
 		getEditText().setText("");
 		getGuessedTextView().setText("");
 		getCorrectlyGuessedTextView().setText("");
 		//reset other stuff here
 		
 		//Generate new word
 		mCurrentWord = generateRandomWord();
 		
 		String underscoreWord = getCurrentWord().replaceAll("[A-Za-z]", "_");
 		getCorrectlyGuessedTextView().setText(underscoreWord);
 	}
 	
 	private void showToast(String message) {
 		Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
 	}
 	
 	//checks if 'text' has already been guessed
 	//returns true if already guessed
 	private boolean didGuess(String text) {
 		if (mGuessedString.contains(text))
 			return true;
 		return false;
 	}
 	
 	private boolean isCorrectGuess(String text) {
 		if (getCurrentWord().contains(text))
 			return true;
 		return false;
 	}
 	
 	public void onSubmitPress(View view) {
 		final String text = getEditText().getText().toString().toUpperCase();
 		getEditText().setText(""); //erase text
 		
 		if (didGuess(text)) {
 			showToast(text+" was already guessed!");
 			return; //don't penalize - just have them guess again
 		}
 		
 		//add to guess string (doesn't matter if wrong or right guess)
 		addToGuessedString(text);
 		
 		if (isCorrectGuess(text)) {
 			showToast("Correct!");
 			
 			StringBuilder currentCorrectText = new StringBuilder(getCorrectlyGuessedTextView().getText().toString());
 			final String currWord = getCurrentWord();
 			int index = currWord.indexOf(text);
 			while (index != -1) {
 				currentCorrectText.setCharAt(index, text.charAt(0));
 				index = currWord.indexOf(text, index+1);
 			}
 			getCorrectlyGuessedTextView().setText(currentCorrectText);
 		}
 		
 		
 	}
 	
 	private void addToGuessedString(String text) {
 		if (mGuessedString.length() == 0)
 			mGuessedString = "Guessed: ";
 		
 		mGuessedString += text;
 		getGuessedTextView().setText(mGuessedString);
 	}
 	
 	private String generateRandomWord() {
 		int rand = new Random().nextInt(WORDS.length);
 		return WORDS[rand].toUpperCase();
 	}
 	
 	
 	/*
 	 * Getter methods below
 	 */
 	public String getCurrentWord() {
 		return mCurrentWord;
 	}
 	
 	public TextView getGuessedTextView() {
 		if (mGuessedTextView == null)
 			mGuessedTextView = (TextView) findViewById(R.id.guessedTextView);
 		return mGuessedTextView;
 	}
 	
 	public TextView getCorrectlyGuessedTextView() {
 		if (mCorrectlyGuessedTextView == null)
 			mCorrectlyGuessedTextView = (TextView) findViewById(R.id.correctlyGuessedTextView);
 		return mCorrectlyGuessedTextView;
 	}
 	
 	public EditText getEditText() {
 		if (mEditText == null)
 			mEditText = (EditText) findViewById(R.id.editText);
 		return mEditText;
 	}
 
 }

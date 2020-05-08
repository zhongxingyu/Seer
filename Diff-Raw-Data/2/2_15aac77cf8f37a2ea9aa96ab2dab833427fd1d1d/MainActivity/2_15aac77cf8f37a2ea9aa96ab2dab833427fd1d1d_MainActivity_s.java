 package com.fun.midworx;
 
 import android.os.Bundle;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.view.View;
 import android.widget.TextView;
 
 import com.fun.midworx.com.fun.midworx.views.LetterOrganizer;
 import com.fun.midworx.com.fun.midworx.views.BoxesContainer;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class MainActivity extends MidWorxActivity {
    private static final int MAX_GAME_SECONDS = 20g;
     private BoxesContainer mBoxesContainer;
     private TextView mScoreText;
     private int mSessionScore = 0;
     private int mLeftSecs;
     private TextView mTimeText;
 
 	private LetterOrganizer letterOrganizer;
 
     private Words mWords;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         try {
             mWords = new Words(getApplicationContext());
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         setContentView(R.layout.activity_main);
 
         setGuessButton();
         setNextButton();
 
         letterOrganizer = new LetterOrganizer(findViewById(R.id.letters_organizer));
 
         mBoxesContainer = (BoxesContainer) findViewById(R.id.words_boxes_layout);
         mScoreText = (TextView) findViewById(R.id.score_txt);
         mTimeText = (TextView) findViewById(R.id.time_txt);
 
         startNewGame();
     }
 
     private void startNewGame() {
         mBoxesContainer.clear();
 
         //dummy data
         List<String> words = null;
         try {
             words = mWords.getWord();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         ArrayList<ArrayList<String>> wordsByLength = new ArrayList<ArrayList<String>>();
         for(int i = 0; i < 6; ++i) {
             wordsByLength.add(new ArrayList<String>());
         }
 
         for (String word: words) {
             wordsByLength.get(word.length()-1).add(word);
         }
 
         for (int i = 2; i < 6; ++i) {
             mBoxesContainer.addBox(wordsByLength.get(i));
         }
 
         startTimer();
 //		letterOrganizerContainer.addView(letterOrganizer);
 
 		List<String> letters = new ArrayList<String>();
         String lettersWord = wordsByLength.get(5).get(0);
         for (int i = 0; i < lettersWord.length(); ++i) {
             letters.add(String.valueOf(lettersWord.charAt(i)));
         }
 
 		letterOrganizer.setLettersPool(letters);
 
         letterOrganizer.show();
         findViewById(R.id.next_btn).setVisibility(View.GONE);
         findViewById(R.id.guess_btn).setVisibility(View.VISIBLE);
 
     }
 
     private enum EndGameReason {TIMEOUT,GUESS_ALL_WORDS}
 
     private void startTimer() {
         mLeftSecs = MAX_GAME_SECONDS;
         mTimeText.post(new Runnable() {
             @Override
             public void run() {
                 mTimeText.setText("Time: " + mLeftSecs);
                 mLeftSecs--;
                 if (mLeftSecs >= 0)
                     mTimeText.postDelayed(this,1000);
                 else
                     endGame(EndGameReason.TIMEOUT);
             }
         });
     }
 
     private void endGame(EndGameReason reason) {
         if (reason == EndGameReason.TIMEOUT) {
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
             builder.setMessage("Your score is " + mSessionScore).setTitle("Game Timeout!");
             letterOrganizer.hide();
             findViewById(R.id.next_btn).setVisibility(View.VISIBLE);
             findViewById(R.id.guess_btn).setVisibility(View.INVISIBLE);
             builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     mBoxesContainer.showUnguessed();
                 }
             });
             builder.setCancelable(false);
             AlertDialog dialog = builder.create();
             dialog.show();
         }
         mBoxesContainer.permitDefinitions();
     }
 
     private void setGuessButton() {
         findViewById(R.id.guess_btn).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 guessWord(getCurrentGuess());
             }
         });
     }
 
     private void setNextButton() {
         findViewById(R.id.next_btn).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startNewGame();
             }
         });
     }
 
     private void guessWord(String word) {
         if (mBoxesContainer.guessWord(word))
             addToScore(word);
     }
 
     private void addToScore(String word) {
         mSessionScore += calculateWordScore(word);
         mScoreText.setText("Score: " + mSessionScore);
     }
 
     private int calculateWordScore(String word) {
         return word.length();
     }
 
     private String getCurrentGuess() {
 		return letterOrganizer.getCurrentGuessAndReset();
     }
 
 
 }

 package edu.rit.cs.distrivia;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.SystemClock;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import edu.rit.cs.distrivia.api.DistriviaAPI;
 import edu.rit.cs.distrivia.model.GameData;
 import edu.rit.cs.distrivia.model.Question;
 
 /**
  * Activity for players to answer questions during a round.
  */
 public class RoundActivity extends GameActivityBase {
 
     private long startTime = 0;
     private long stopTime = 0;
     private String selection = null;
     private TextView question;
     private TextView score;
     private Button answerA;
     private Button answerB;
     private Button answerC;
     private Button answerD;
     private ProgressBar pbar;
 
     private final Handler loadHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             if (gameData().isDone()) {
             	GameData gd = gameData();
             	gd.setLoadLocal(true);
             	setGameData(gd);
                 startActivity(LEADERBOARD_ACTIVITY);
                finish();
             } else {
                 setupButtons(gameData().getQuestion());
             }
         }
     };
 
     private final Handler pbarHandler = new Handler();
 
     private final Runnable updateProgress = new Runnable() {
         @Override
         public void run() {
             pbar.setProgress((int) (SystemClock.elapsedRealtime() - startTime));
             pbarHandler.postDelayed(updateProgress, 500);
         }
     };
 
     OnClickListener answerListener = new OnClickListener() {
         @Override
         public void onClick(final View v) {
             stopTime = SystemClock.elapsedRealtime();
             updateProgress.run();
             pbarHandler.removeCallbacks(updateProgress);
 
             deselectAll();
 
             final Button answer = (Button) v;
             answer.setBackgroundResource(R.drawable.red);
 
             if (answer == answerA)
                 selection = "a";
             else if (answer == answerB)
                 selection = "b";
             else if (answer == answerC)
                 selection = "c";
             else if (answer == answerD)
                 selection = "d";
 
         }
     };
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.round);
 
         pbar = (ProgressBar) findViewById(R.id.seekBar);
         pbar.setMax(10000);
         question = (TextView) findViewById(R.id.question);
         score = (TextView) findViewById(R.id.score_label);
         answerA = (Button) findViewById(R.id.answer1_button);
         answerB = (Button) findViewById(R.id.answer2_button);
         answerC = (Button) findViewById(R.id.answer3_button);
         answerD = (Button) findViewById(R.id.answer4_button);
 
         Button submit = (Button) findViewById(R.id.answer_submit_button);
         submit.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (selection != null) {
                     submitAndLoad();
                 } else {
                     Toast.makeText(v.getContext(), "Please select an answer",
                             10).show();
                 }
             }
         });
 
         answerA.setOnClickListener(answerListener);
         answerB.setOnClickListener(answerListener);
         answerC.setOnClickListener(answerListener);
         answerD.setOnClickListener(answerListener);
 
         setupButtons(gameData().getQuestion());
 
     }
 
     private void submitAndLoad() {
         final long time = stopTime - startTime;
         new Thread() {
             @Override
             public void run() {
                 // Submit answer/get next question
                 try {
                     GameData gd = gameData();
                     gd = DistriviaAPI.answer(gd, selection, time);
                     setGameData(gd);
                 } catch (final Exception e) {
                     e.printStackTrace();
                     return;
                 }
                 // Signal UI thread to reload
                 loadHandler.sendEmptyMessage(0);
             }
         }.start();
     }
 
     /**
      * Fill out the button's with question data
      */
     private void setupButtons(final Question q) {
         question.setText(q.getQuestion());
         score.setText("Score: " + gameData().getScore());
         answerA.setText("A: " + q.getChoiceA());
         answerB.setText("B: " + q.getChoiceB());
         answerC.setText("C: " + q.getChoiceC());
         answerD.setText("D: " + q.getChoiceD());
 
         deselectAll();
         // Store time which the screen stopped rendering.
         startTime = SystemClock.elapsedRealtime();
 
         pbar.setProgress(0);
         pbarHandler.removeCallbacks(updateProgress);
         pbarHandler.postDelayed(updateProgress, 1000);
     }
 
     /**
      * Reset all buttons to normal state.
      */
     private void deselectAll() {
         answerA.setBackgroundResource(R.drawable.green);
         answerB.setBackgroundResource(R.drawable.green);
         answerC.setBackgroundResource(R.drawable.green);
         answerD.setBackgroundResource(R.drawable.green);
         selection = null;
     }
 
 }

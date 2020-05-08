 package com.WordFinder;
 
 import android.view.KeyEvent;
 import android.content.DialogInterface;
 import android.app.AlertDialog;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Timer;
 import java.util.TimerTask;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.widget.TextView;
 
 // -------------------------------------------------------------------------
 /**
  * Main application activity.
  *
  * @author John Mooring (jmooring)
 * @author Christopher Buck (cmbuck)
  * @author Bryan Malyn (bmalyn)
  * @version 2011.10.29
  */
 
 public class WordFinderActivity
     extends Activity
 {
 
     private static final int          TIME      = 180;
     private static WordFinderActivity instance;
     private static TextView           points;
     private static TextView           timerText;
     private static int                time      = TIME;
     private LetterGrid                grid;
     private LetterGridView            board;
     private final int                 GRID_SIZE = 4;
     private static Timer              timer;
     private final Handler             handler   = new WordFinderHandler();
 
     /**
      *
      *  Handles timer events. Necessary so actions are performed in main thread
      *
      *  @author John Mooring (jmooring)
      *  @author Bryan Malyn (bmalyn)
      *  @author Christopher Buck (cmbuck)
      *  @version 2011.12.4
      */
     private class WordFinderHandler
         extends Handler
     {
         public void handleMessage(Message m)
         {
             switch (m.what)
             {
                 case 0:
                     timerText.setText("Time: " + time / 60 + ":"
                         + (time % 60 < 10 ? "0" + time % 60 : time % 60));
                     break;
                 case 1:
                     WordFinderActivity.getInstance().gotoResults();
             }
         }
     }
 
 
     /**
      * Called when the activity is first created.
      *
      * @param savedInstanceState
      *            unused
      */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         instance = this;
         setContentView(R.layout.main);
         board = (LetterGridView)findViewById(R.id.letterGrid);
         points = (TextView)findViewById(R.id.points);
         timerText = (TextView)findViewById(R.id.timer);
         grid = new LetterGrid();
         grid.addObserver(new Observer() {
             public void update(Observable observable, Object data)
             {
                 points.setText("Points: " + grid.getPoints() + "/"
                     + grid.getPossiblePoints());
             }
         });
 
         if (timer == null)
         {
             timer = new Timer();
             timer.scheduleAtFixedRate(new TimerTask() {
                 public void run()
                 {
                     time--;
                     handler.sendEmptyMessage(0);
                     if (time == 0)
                     {
                         handler.sendEmptyMessage(1);
                     }
                 }
             }, 0, 1000);
         }
         board.setModel(grid);
         getWindow().setBackgroundDrawableResource(R.drawable.background);
         newBoard();
     }
 
 
     /**
      * Makes sure the user cannot access an old board if they come back to this
      * activity by creating a new board
      */
     public void onResume()
     {
         newBoard();
         super.onResume();
     }
 
 
     /**
      * Launches a confirmation dialogue. Depending on input, calls newBoard
      *
      * @param v
      *            unused
      */
     public void newBoard(View v)
     {
         new AlertDialog.Builder(this)
             .setIcon(android.R.drawable.ic_dialog_alert).setTitle("New Game")
             .setMessage("Are you sure you want to start a new game?")
             .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 
                 public void onClick(DialogInterface dialog, int which)
                 {
                     newBoard();
                 }
 
             }).setNegativeButton("No", null).show();
 
     }
 
 
     /**
      * Intercepts the back button and prompts the user for confirmation
      *
      * @param keyCode
      *            the key pressed
      * @param event
      *            the key event
      */
     public boolean onKeyDown(int keyCode, KeyEvent event)
     {
         if (keyCode == KeyEvent.KEYCODE_BACK)
         {
             new AlertDialog.Builder(this)
                 .setIcon(android.R.drawable.ic_dialog_alert)
                 .setTitle("Quit")
                 .setMessage("Are you sure you would like to quit?")
                 .setPositiveButton(
                     "Yes",
                     new DialogInterface.OnClickListener() {
 
                         public void onClick(DialogInterface dialog, int which)
                         {
 
                             WordFinderActivity.this.finish();
                         }
 
                     }).setNegativeButton("No", null).show();
 
             return true;
         }
         else
         {
             return super.onKeyDown(keyCode, event);
         }
 
     }
 
 
     /**
      * Creates a new game board board
      */
     public void newBoard()
     {
         do
         {
             grid.loadRandom(getIntent().getIntExtra("size", GRID_SIZE));
         }
         while (grid.getPossiblePoints() < 150);
         time = TIME;
         handler.sendEmptyMessage(0);
     }
 
 
     /**
      * Shows an are you sure dialogue, depending on the result calls gotoResults
      *
      * @param v
      *            unused
      */
     public void giveUp(View v)
     {
         new AlertDialog.Builder(this)
             .setIcon(android.R.drawable.ic_dialog_alert).setTitle("Give Up")
             .setMessage("Are you sure you want to give up?")
             .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 
                 public void onClick(DialogInterface dialog, int which)
                 {
                     gotoResults();
                 }
 
             }).setNegativeButton("No", null).show();
 
     }
 
 
     /**
      * Packages game state into an intent and launches the results activity.
      */
     public void gotoResults()
     {
         time = -1;
         Intent intent = new Intent(getInstance(), ResultsActivity.class);
         intent.putExtra("userPoints", grid.getPoints());
         intent.putExtra("solvedPoints", grid.getPossiblePoints());
         intent.putExtra("size", getIntent().getIntExtra("size", GRID_SIZE));
         String[] userWords = {};
         userWords = grid.getFoundWords().toArray(userWords);
         intent.putExtra("userWords", userWords);
         String[] solvedWords = {};
         solvedWords = WordSolver.getInstance().getWords().toArray(solvedWords);
         intent.putExtra("solvedWords", solvedWords);
         startActivity(intent);
     }
 
 
     /**
      * Returns an instance of this activity so classes can get context
      *
      * @return instance of this activity
      */
     public static WordFinderActivity getInstance()
     {
         return instance;
     }
 }

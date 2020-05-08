 package com.WordFinder;
 
import android.view.View;
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /**
  *  Activity for the Results Screen
  *
  *  @author cmbuck
  *  @version Nov 28, 2011
  */
 public class ResultsActivity extends Activity
 {
     //Data model
     private WordSolver wordSolver;
 
     //GUI components
     TextView points;
     ListView left;  //found words
     ListView right; //all possible words
     Button titleScreen;
     Button playAgain;
 
     /**
      * Called when the activity is first created.
      *
      * @param savedInstanceState previous state saved by the last run of the
      *     activity
      */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         points = (TextView)findViewById(R.id.pointsTextView);
         left = (ListView)findViewById(R.id.listViewLeft);
         right = (ListView)findViewById(R.id.listViewRight);
         titleScreen = (Button)findViewById(R.id.titleScreenButton);
         playAgain = (Button)findViewById(R.id.playAgainButton);
 
         setContentView(R.layout.results);
     }
 
     /**
      * Makes the ListViews populate with words
      */
     public void populateListViews()
     {
         //set left ListView to have the list of found words
         //set right ListView to have the list of all possible words
     }
 
     /**
      * Sets the Model reference for the Activity
      * @param lg The data model
      */
     public void setWordSolver(WordSolver ws)
     {
         wordSolver = ws;
     }
 
     /**
      * Does something when the titleScreen button is clicked
      */
     public void onTitleScreenClicked(View v)
     {
         //TODO: something
     }
 
     /**
      * Does something when the playAgain button is clicked
      */
     public void onPlayAgainClicked(View v)
     {
         //TODO: something
     }
 
     /**
      * Sets the string to be displayed by the points TextView
      * @param str The string to be displayed
      */
     public void setPoints(String str)
     {
         points.setText(str);
     }
 
 }

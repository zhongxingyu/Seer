 package com.example.zootypers;
 
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 /**
  *
  * UI / Activity for post-game screen.
  * @author cdallas
  *
  */
 public class PostGameScreen extends Activity {
 
   @SuppressLint("NewApi")
   @Override
   protected final void onCreate(final Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     this.requestWindowFeature(Window.FEATURE_NO_TITLE);
     
     // get & display background
     setContentView(R.layout.activity_pregame_selection);
     Drawable background = ((ImageButton) findViewById(getIntent().getIntExtra("bg", 0))).getDrawable();
     
     setContentView(R.layout.activity_postgame_screen);
     findViewById(R.id.postgame_layout).setBackground(background);
         
     // get and display score
     String score = getIntent().getStringExtra("score");
     TextView finalScore = (TextView) findViewById(R.id.final_score);
     finalScore.setText(score);
   }
 
   @Override
   public final boolean onCreateOptionsMenu(final Menu menu) {
     // Inflate the menu; this adds items to the action bar if it is present.
     getMenuInflater().inflate(R.menu.postgame_screen, menu);
     return true;
   }
 
   /**
    * Called when the user clicks the "Main Menu" button.
    * @param view The button clicked
    */
   public final void goToTitlePage(final View view) {
     Intent intent = new Intent(this, TitlePage.class);
     startActivity(intent);
   }
 
   /**
   * Called when the user clicks the "New Game" button.
   * @param view The button clicked
   */
   public final void goToPreGameSelection(final View view) {
     Intent intent = new Intent(this, PreGameSelection.class);
     startActivity(intent);
   }
 }

 package com.example.zootypers;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 
 /**
  * UI / controller from pre-game selection screen.
  * @author cdallas
  */
 public class PreGameSelection extends Activity {
 
  // TODO either set buttons to background:@android:color/transparent in layout
  // or change default to actually current bg
  private int[] buttonColors = new int[] { Color.parseColor("#00CC33"), Color.parseColor("#0066FF") };
   private final int DEFAULT_BUTTON_BG = 0; //#00CC33
  private final int HIGHLIGH_BUTTON_BG = android.R.drawable.btn_default;
 
   private View diff;
   private View animal;
   private View background;
 
   @Override
   protected void onCreate(final Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.activity_pregame_selection);
 
     // TODO change so initial values are gotten from storage
    diff = findViewById(R.id.easy_difficulty_button);
     setDiff(diff);
     animal = findViewById(R.id.elephant_button);
     setAnimal(animal);
     background = findViewById(R.id.BG1_button);
     setBackground(background);
   }
 
   @Override
   public boolean onCreateOptionsMenu(final Menu menu) {
     // Inflate the menu; this adds items to the action bar if it is present.
     getMenuInflater().inflate(R.menu.pregame_selection, menu);
     return true;
   }
 
   /**
    * When the user clicks a button to select a difficulty, highlights that
    * button and un-highlights the last selected button.
    * @param view The button clicked
    */
   public void setDiff(View view) {
     if (diff != null) {
      diff.setBackgroundColor(buttonColors[0]); //DEFAULT_BUTTON_BG
     }
    view.setBackgroundColor(buttonColors[1]); //HIGHLIGH_BUTTON_BG
     diff = view;
   }
 
   /**
    * When the user clicks a button to select an animal, highlights that
    * button and un-highlights the last selected button.
    * @param view The button clicked
    */
   public void setAnimal(View view) {
     if (animal != null) {
       animal.setBackgroundResource(DEFAULT_BUTTON_BG);
     }
    view.setBackgroundResource(HIGHLIGH_BUTTON_BG);
     animal = view;
   }
 
   /**
    * When the user clicks a button to select a background, highlights that
    * button and un-highlights the last selected button.
    * @param view The button clicked
    */
   public void setBackground(View view) {
     if (background != null) {
       background.setBackgroundResource(DEFAULT_BUTTON_BG);
     }
    view.setBackgroundResource(HIGHLIGH_BUTTON_BG);
     background = view;
   }
 
   /**
    * When continue is clicked, goes to the game play screen.
    * @param view The button clicked.
    */
   public void goToSinglePlayer(View view) {
    // TODO write these diff/animal/bg to storage
    //STORED animal = animal;
    //STORED background = background;
    //STORED difficulty = diff;
     
     Intent intent = new Intent(this, SinglePlayerGame.class);
     // pass animal and background
     intent.putExtra("anm", animal.getId());
     intent.putExtra("bg", background.getId());
     startActivity(intent);
   }
 
 }

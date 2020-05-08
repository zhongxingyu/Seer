 package com.example.zootypers;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.PorterDuff.Mode;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 
 /**
  *
  * UI / Activity for pre-game selection screen for a single player game.
  * @author cdallas
  *
  */
 public class PreGameSelection extends Activity {
 
     private final int HIGHTLIGHT_COLOR = 0xFF000000; // black
     private View diff;
     private View animal;
     private View background;
 
     @Override
     protected final void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
     
         setContentView(R.layout.activity_pregame_selection);
 
         diff = findViewById(R.id.medium_difficulty_button);
         setDiff(diff);
         animal = findViewById(R.id.elephant_button);
         setAnimal(animal);
         background = findViewById(R.id.BG1_button);
         setBackground(background);
     }
 
     @Override
     public final boolean onCreateOptionsMenu(final Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.pregame_selection, menu);
         return true;
     }
 
     /**
     * When the user clicks a button to select a difficulty, highlights that
     * button and un-highlights the last selected button.
     * @param view The button clicked
     */
     public final void setDiff(final View view) {
         if (diff != null) {
             diff.getBackground().clearColorFilter();
         }
         view.getBackground().setColorFilter(HIGHTLIGHT_COLOR, Mode.MULTIPLY);
         diff = view;
     }
 
     /**
     * When the user clicks a button to select an animal, highlights that
     * button and un-highlights the last selected button.
     * @param view The button clicked
     */
     public final void setAnimal(final View view) {
         if (animal != null) {
             animal.getBackground().clearColorFilter();
         }
         view.getBackground().setColorFilter(HIGHTLIGHT_COLOR, Mode.MULTIPLY);
         animal = view;
     }
 
     /**
     * When the user clicks a button to select a background, highlights that
     * button and un-highlights the last selected button.
     * @param view The button clicked
     */
     public final void setBackground(final View view) {
         if (background != null) {
             background.getBackground().clearColorFilter();
         }
         view.getBackground().setColorFilter(HIGHTLIGHT_COLOR, Mode.MULTIPLY);
         background = view;
     }
 
     /**
     * When continue is clicked, goes to the game play screen.
     * Passes id of animal & background button selected and an int coding of difficulty,
     * where 1 is easy, 2 is medium, and 3 is hard.
     * @param view The button clicked.
     */
     public final void goToSinglePlayer(final View view) {
         Intent intent = new Intent(this, SinglePlayer.class);
     
         // pass difficulty
         if (diff == findViewById(R.id.easy_difficulty_button)) {
             intent.putExtra("diff", 1);      
         } else if (diff == findViewById(R.id.hard_difficulty_button)) {
             intent.putExtra("diff", 3);      
         } else {
             intent.putExtra("diff", 2);      
         }
     
         // pass animal and background
         intent.putExtra("anm", animal.getId());
         intent.putExtra("bg", background.getId());
     
         startActivity(intent);
     }
 
     /**
     * Called when the user clicks the "Main Menu" button.
     * @param view The button clicked
     */
     public final void goToTitlePage(final View view) {
         Intent intent = new Intent(this, TitlePage.class);
         startActivity(intent);
     }
    
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, TitlePage.class);
        startActivity(intent);
    }
 }

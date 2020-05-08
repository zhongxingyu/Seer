 package com.example.zootypers;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 
 /**
 *
 * UI / Activity for title screen.
 * @author cdallas
 *
 */
 public class TitlePage extends Activity {
 
     @Override
     protected final void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.activity_title_page);
     }
 
     @Override
     public final boolean onCreateOptionsMenu(final Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.title_page, menu);
         return true;
     }
 
     /**
     * Called when the user clicks the "Single Player" button.
     * @param view The button clicked
     */
     public final void goToPreGameSelection(final View view) {
         Intent intent = new Intent(this, PreGameSelection.class);
         startActivity(intent);
     }
 
     /**
     * Called when the user clicks the "Multiplayer" button.
     * @param view The button clicked
     */
     public final void goToPreGameSelectionMulti(final View view) {
         Intent intent = new Intent(this, PreGameSelectionMulti.class);
         startActivity(intent);
     }
 
     /**
     * Called when the user clicks the "Leaderboard" button.
     * @param view The button clicked
     */
     public final void goToLeaderboard(final View view) {
         Intent intent = new Intent(this, Leaderboard.class);
         startActivity(intent);
     }
 
     /**
     * Called when the user clicks the "Options" button.
     * @param view The button clicked
     */
     public final void goToOptions(final View view) {
         Intent intent = new Intent(this, Options.class);
         startActivity(intent);
     }
 }

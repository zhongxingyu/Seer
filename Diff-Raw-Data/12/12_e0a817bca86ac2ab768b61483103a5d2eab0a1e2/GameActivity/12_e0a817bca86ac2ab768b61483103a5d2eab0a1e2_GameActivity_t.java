 package com.threewisedroids.superc4;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TextView;
 
 import com.threewisedroids.superc4.backend.GameState;
 
 @SuppressWarnings("deprecation")
 public class GameActivity extends Activity {
 
    static final boolean hasActionBar = android.os.Build.VERSION.SDK_INT >= 11;

    GameState            state;
    boolean              useAI;
    boolean              fillCorners;
 
     @SuppressLint("NewApi")
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_game);
        if (hasActionBar)
            getActionBar().setDisplayHomeAsUpEnabled(true);
 
         final int gridSize = getIntent().getIntExtra("gridSize", 8);
         useAI = getIntent().getBooleanExtra("useAI", true);
         fillCorners = getIntent().getBooleanExtra("fillCorners", false);
 
         state = new GameState(gridSize, fillCorners);
 
         state.setInfoUpdate(new Runnable() {
 
             public void run() {
                 runOnUiThread(new Runnable() {
 
                     public void run() {
                         updateInfoBox();
                     }
                 });
             }
         });
 
         ((GameView) findViewById(R.id.gameView1)).setGameState(state);
         ((GameView) findViewById(R.id.gameView1)).setUseAI(useAI);
 
         if (savedInstanceState != null) {
             state.load(savedInstanceState.getString("state"));
             updateInfoBox();
 
             if (useAI && state.getNextPlayer() != 1)
                 ((GameView) findViewById(R.id.gameView1)).playAI();
         }
 
         findViewById(R.id.button_renew).setOnClickListener(
                 new View.OnClickListener() {
                     public void onClick(View v) {
                         ((GameView) findViewById(R.id.gameView1)).stopThread();
                         state.clear();
                     }
                 });
 
         findViewById(R.id.button_undo).setOnClickListener(
                 new View.OnClickListener() {
 
                     public void onClick(View v) {
                         ((GameView) findViewById(R.id.gameView1)).stopThread();
                         state.undo();
                         if (useAI && state.getNextPlayer() != 1)
                             state.undo();
                     }
                 });
 
     }
 
     @Override
     @Deprecated
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case 1:
                 Dialog dialog = new Dialog(this);
                 dialog.setContentView(R.layout.dialog_rules);
                 dialog.setTitle(R.string.rules);
                 return dialog;
             default:
                 return null;
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.game_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
             case R.id.menu_see_rules:
                 // Show Rules Dialog
                 showDialog(1);
                 return true;
             case R.id.menu_settings:
                 // Start Setting Activity
                 Intent intent = new Intent(this, SettingsActivity.class);
                 startActivity(intent);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 
         outState.putString("state", state.save());
     }
 
     public void setInfoBoxPlayer(int player) {
         int drawable;
         if (player == 1)
             drawable = R.drawable.blue_disc;
         else
             drawable = R.drawable.red_disc;
 
         ((TextView) findViewById(R.id.infoBox))
                 .setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0);
     }
 
     public void updateInfoBox() {
         if (state.hasVictory()) {
             ((TextView) findViewById(R.id.infoBox)).setText(R.string.victory);
             setInfoBoxPlayer(3 - state.getNextPlayer());
         } else {
             if (useAI && state.getNextPlayer() != 1)
                 ((TextView) findViewById(R.id.infoBox))
                         .setText(R.string.next_ai);
             else
                 ((TextView) findViewById(R.id.infoBox)).setText(R.string.next);
             setInfoBoxPlayer(state.getNextPlayer());
         }
     }
 
 }

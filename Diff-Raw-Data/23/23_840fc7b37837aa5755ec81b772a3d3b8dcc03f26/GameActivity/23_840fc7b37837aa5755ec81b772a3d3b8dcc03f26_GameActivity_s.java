 /**
  * Copyright (C) 2012 Jesse Wilson
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.publicobject.rounds;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.text.SpannableStringBuilder;
 import android.text.style.RelativeSizeSpan;
 import android.text.style.StyleSpan;
 import android.util.TypedValue;
 import android.view.View;
 import android.widget.HorizontalScrollView;
 import android.widget.ImageButton;
 import android.widget.TableLayout;
 import android.widget.TextView;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import static android.app.ActionBar.LayoutParams;
 
 public final class GameActivity extends SherlockActivity {
     private String gameId;
     private Game game;
     private PowerManager.WakeLock wakeLock;
     private GameSaver gameSaver;
 
     private View layout;
     private JogWheel jogWheel;
     private ScoreHistoryTable scoreHistoryTable;
     private TextView labelTextView;
     private TextView valueTextView;
     private TextView playersTextView;
 
     private ActionBarBackground actionBarBackground;
     private ActionBar actionBar;
     private ImageButton nextRound;
     private TextView roundTextView;
     private ImageButton previousRound;
 
     /** True for landscape layout. */
     private boolean tablet;
 
     @Override public void onCreate(Bundle savedState) {
         super.onCreate(savedState);
 
         tablet = Device.isTablet(this);
         setRequestedOrientation(tablet
                 ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                 : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
         Intent intent = getIntent();
         gameId = savedState != null
                 ? savedState.getString(IntentExtras.GAME_ID)
                 : intent.getStringExtra(IntentExtras.GAME_ID);
 
         PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
         wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, getPackageName());
 
         layout = getLayoutInflater().inflate(R.layout.game, null);
         setContentView(layout);
 
         labelTextView = (TextView) layout.findViewById(R.id.label);
         valueTextView = (TextView) layout.findViewById(R.id.value);
         playersTextView = (TextView) layout.findViewById(R.id.players);
 
         scoreHistoryTable = new ScoreHistoryTable(getApplicationContext(),
                 (TableLayout) layout.findViewById(R.id.runningScores),
                 (TableLayout) layout.findViewById(R.id.currentScores),
                 (HorizontalScrollView) layout.findViewById(R.id.runningScoresScroller));
 
         jogWheel = (JogWheel) layout.findViewById(R.id.jogWheel);
         jogWheel.setListener(new JogWheel.Listener() {
             @Override public void selecting(int player, int value) {
                 int selectingFrom = game.playerScore(player, game.round());
 
                 labelTextView.setText(game.playerName(player));
                 labelTextView.setTextColor(game.playerColor(player));
                 labelTextView.setVisibility(View.VISIBLE);
 
                 SpannableStringBuilder ssb = new SpannableStringBuilder();
                 if (selectingFrom != 0 && selectingFrom != value) {
                     ssb.append(Integer.toString(selectingFrom));
                     if (value > selectingFrom) {
                         ssb.append(" + ").append(Integer.toString(value - selectingFrom));
                     } else {
                         ssb.append(" - ").append(Integer.toString(selectingFrom - value));
                     }
                     ssb.append(" = ");
                     ssb.setSpan(new RelativeSizeSpan(0.75f), 0, ssb.length(), 0);
                 }
                 String valueString = (value > 0 ? "+" : "") + Integer.toString(value);
                 ssb.append(valueString);
                 ssb.setSpan(new StyleSpan(Typeface.BOLD),
                         ssb.length() - valueString.length(), ssb.length(), 0);
 
                 valueTextView.setText(ssb);
                 valueTextView.setVisibility(View.VISIBLE);
                 if (!tablet) {
                     actionBar.hide();
                 } else {
                     playersTextView.setVisibility(View.INVISIBLE);
                 }
             }
             @Override public void selected(int player, int value) {
                 int round = game.round();
                 game.setPlayerScore(player, round, value);
                 scoreHistoryTable.scoreChanged(player, round);
                 updateActionBarBackground();
                 roundChanged();
                 gameSaver.saveLater();
             }
             @Override public void cancelled() {
                 roundChanged();
             }
         });
 
         actionBar = getSupportActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         actionBarBackground = new ActionBarBackground(getResources());
         actionBar.setBackgroundDrawable(actionBarBackground);
 
         View roundPicker;
         if (tablet) {
             roundPicker = layout.findViewById(R.id.roundPicker);
         } else {
             roundPicker = getLayoutInflater().inflate(R.layout.round_picker, null);
             actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                     ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
             actionBar.setCustomView(roundPicker, new ActionBar.LayoutParams(
                     LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
         }
 
         nextRound = (ImageButton) roundPicker.findViewById(R.id.nextRound);
         roundTextView = (TextView) roundPicker.findViewById(R.id.roundNumber);
         previousRound = (ImageButton) roundPicker.findViewById(R.id.previousRound);
         nextRound.setOnClickListener(new View.OnClickListener() {
             @Override public void onClick(View view) {
                 game.setRound(game.round() + 1);
                 roundChanged();
             }
         });
         previousRound.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 game.setRound(game.round() - 1);
                 roundChanged();
             }
         });
     }
 
     @Override public boolean onCreateOptionsMenu(Menu menu) {
         getSupportMenuInflater().inflate(R.menu.game, menu);
        menu.findItem(R.id.randomPlayer).setEnabled(game.playerCount() > 1);
         return true;
     }
 
     @Override protected void onSaveInstanceState(Bundle savedState) {
         super.onSaveInstanceState(savedState);
         savedState.putString(IntentExtras.GAME_ID, gameId);
     }
 
     private void roundChanged() {
         scoreHistoryTable.roundCountChanged();
         roundTextView.setText("Round " + Integer.toString(game.round() + 1));
         labelTextView.setVisibility(View.INVISIBLE);
         previousRound.setEnabled(game.round() > 0);
         nextRound.setEnabled(game.round() < game.roundCount() - 1
                 || game.hasNonZeroScore(game.round()));
         valueTextView.setVisibility(View.INVISIBLE);
         if (tablet) {
             int textSize = (game.playerCount() > 4) ? 40 : 48;
             playersTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
             playersTextView.setText(Names.styleScores(game));
             playersTextView.setVisibility(View.VISIBLE);
         } else {
             actionBar.show();
         }
         jogWheel.invalidate();
     }
 
     private void updateActionBarBackground() {
         int color = Color.BLACK; // BLACK indicates the winning color hasn't yet been found.
         int winningTotal = game.winningTotal();
         for (int p = 0; p < game.playerCount(); p++) {
             if (game.playerTotal(p) != winningTotal) {
                 continue;
             }
             if (color == Color.BLACK) {
                 color = game.playerColor(p);
             } else {
                 color = Color.WHITE; // WHITE indicates a tie.
             }
         }
         actionBarBackground.setColor(color);
     }
 
     @Override protected void onPause() {
         super.onPause();
 
         if (wakeLock.isHeld()) {
             wakeLock.release();
         }
 
         gameSaver.onPause();
     }
 
     @Override protected void onResume() {
         super.onResume();
 
         boolean useWakeLock = true; // TODO: make this a preference?
         if (useWakeLock && !wakeLock.isHeld()) {
             wakeLock.acquire();
         }
 
         GameDatabase database = GameDatabase.getInstance(getApplicationContext());
         game = database.get(gameId);
         game.setRound(game.roundCount() - 1);
         gameSaver = new GameSaver(database, game);
         scoreHistoryTable.setGame(game);
         jogWheel.setModel(game);
         updateActionBarBackground();
         roundChanged();
     }
 
     @Override public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.editPlayers:
             Intent intent = new Intent(this, SetUpActivity.class);
             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             intent.putExtra(IntentExtras.GAME_ID, gameId);
             intent.putExtra(IntentExtras.IS_NEW_GAME, false);
             startActivity(intent);
             overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
             return true;
 
         case R.id.randomPlayer:
             if (game.playerCount() < 2) {
                 throw new IllegalStateException();
             }
             List<Integer> playersToEliminate = new ArrayList<Integer>();
             for (int p = 0; p < game.playerCount(); p++) {
                 playersToEliminate.add(p);
             }
             Collections.shuffle(playersToEliminate);
             jogWheel.selectPlayer(playersToEliminate);
             return true;
 
         case R.id.share:
             intent = new Intent(this, ShareActivity.class);
             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             intent.putExtra(IntentExtras.GAME_ID, gameId);
             startActivity(intent);
             overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
             return true;
 
         case android.R.id.home:
             intent = new Intent(this, HomeActivity.class);
             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(intent);
             overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
             return true;
 
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 }

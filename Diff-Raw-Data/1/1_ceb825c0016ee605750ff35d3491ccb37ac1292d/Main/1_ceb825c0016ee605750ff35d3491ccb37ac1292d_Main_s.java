 /*
  * Copyright 2010 Andrew Prunicki
  * 
  * This file is part of Twinkle.
  * 
  * Twinkle is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Twinkle is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Twinkle.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.prunicki.suzuki.twinkle;
 
 import static com.prunicki.suzuki.twinkle.model.Score.DIFFICULTY_LEVEL_EASY;
 import static com.prunicki.suzuki.twinkle.model.Score.DIFFICULTY_LEVEL_HARD;
 import static com.prunicki.suzuki.twinkle.model.Score.PROP_CHG_LAST_SCORE;
 import static com.prunicki.suzuki.twinkle.Constants.PLAYER_ID_KEY;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnDismissListener;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 import com.prunicki.suzuki.twinkle.db.ScoreDAO;
 import com.prunicki.suzuki.twinkle.model.ModelHelper;
 import com.prunicki.suzuki.twinkle.model.Player;
 
 public class Main extends TwinkleActivity {
     public static final String TAG = "SuzukiTwinkle";
     private static final int SELECT_PLAYER_FOR_PLAY = 1;
     private static final int SALUTATION_WIDGET = 1;
     private static final int HISCORE_WIDGET = 2;
     private static final int DIFFICULTY_WIDGET = 4;
     private static final int ALL_WIDGETS = SALUTATION_WIDGET | HISCORE_WIDGET | DIFFICULTY_WIDGET;
     
     private TwinkleApplication mApp;
     private ScoreDAO mDao;
     private Player mPlayer;
     private TextView mSalutation;
     private TextView mHiScore;
     private View mPlayerInfo;
     private Button mPlayButton;
     private Button mPracticeButton;
     private Button mSwitchPlayerButton;
     private ToggleButton mDifficultyButton;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         mApp = (TwinkleApplication) getApplication();
         mApp.addPropertyChangeListener(mPropChgListener);
         mDao = mApp.getDAO();
         
         mSalutation = (TextView) findViewById(R.id.Salutation);
         mHiScore = (TextView) findViewById(R.id.HiScore);
         mPlayerInfo = findViewById(R.id.PlayerInfo);
         mPlayButton = (Button) findViewById(R.id.MainPlay);
         mSwitchPlayerButton = (Button) findViewById(R.id.SwitchPlayer);
         mPracticeButton = (Button) findViewById(R.id.MainPractice);
         mDifficultyButton = (ToggleButton) findViewById(R.id.MainDifficultyLevel);
         
         mPlayerInfo.setOnClickListener(mPlayerInfoListener);
         mPlayButton.setOnClickListener(mPlayListener);
         mSwitchPlayerButton.setOnClickListener(mSwitchPlayerListener);
         mPracticeButton.setOnClickListener(mPracticeListener);
         mDifficultyButton.setOnCheckedChangeListener(mDifficultyListener);
     }
     
     @Override
     protected void onDestroy() {
         super.onDestroy();
         
         mApp.removePropertyChangeListener(mPropChgListener);
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         
         if (requestCode == SELECT_PLAYER_FOR_PLAY) {
             startGame();
         }
     }
     
     @Override
     protected void onResume() {
         super.onResume();
         
         Player player = mApp.getCurrentPlayer();
         setPlayerWidgetValues(player, ALL_WIDGETS);
         if (player != null) {
             player.addPropertyChangeListener(mPropChgListener);
         }
         mPlayer = player;
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         
         if (mPlayer != null) {
             mPlayer.removePropertyChangeListener(mPropChgListener);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Dialog d = null;
         LinearLayout layout = null;
         switch(item.getItemId()) {
             case R.id.about:
                 d  = new Dialog(this);
                 d.setContentView(R.layout.about);
                 d.setCanceledOnTouchOutside(true);
                 
                 layout  = (LinearLayout) d.findViewById(R.id.AboutLayout);
                 layout.setOnClickListener(new AboutListener(d));
                 
                 String appName = Main.this.getResources().getString(R.string.app_name);
                 d.setTitle("About " + appName);
                 d.show();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
     
     void setPlayerWidgetValues(Player player, int flags) {
         if (player == null) {
             mPlayerInfo.setVisibility(View.INVISIBLE);
         } else {
             mPlayerInfo.setVisibility(View.VISIBLE);
             if ((flags & SALUTATION_WIDGET) > 0) {
                 mSalutation.setText(player.getName());
             }
             if ((flags & HISCORE_WIDGET) > 0) {
                 //TODO Move text to strings.xml
                 mHiScore.setText("Hi Score: " + player.getHiScore());
             }
             if ((flags & DIFFICULTY_WIDGET) > 0) {
                 boolean hard = player.getDifficulty() == DIFFICULTY_LEVEL_HARD ? true : false;
                 mDifficultyButton.setChecked(hard);
             }
         }
     }
 
     void startGame() {
         if (mPlayer != null) {
             boolean hard = mDifficultyButton.isChecked();
             int level = hard ? DIFFICULTY_LEVEL_HARD : DIFFICULTY_LEVEL_EASY;
             
             Intent intent = new Intent(Main.this, GameScreen.class);
             intent.putExtra(GameScreen.DIFFICULTY_LEVEL_KEY, level);
             startActivity(intent);
         }
     }
 
     private OnClickListener mPlayListener = new OnClickListener() {
         public void onClick(View v) {
             if (mPlayer == null) {
                 int cnt = mDao.playerCount();
                 if (cnt > 0) {
                     Intent intent = new Intent(Main.this, ChangePlayerScreen.class);
                     startActivityForResult(intent, SELECT_PLAYER_FOR_PLAY);
                 } else {
                     Dialog dlg = null;
                     dlg = new NewPlayerDialog(Main.this);
                     dlg.setOnDismissListener(mDismissListener);
                     dlg.show();
                 }
             } else {
                 startGame();
             }
         }
     };
     
     private OnClickListener mPracticeListener = new OnClickListener() {
         @Override
         public void onClick(View v) {
             Intent intent = new Intent(Main.this, PracticeStartScreen.class);
             startActivity(intent);
         }
     };
     
     private OnClickListener mSwitchPlayerListener = new OnClickListener() {
         @Override
         public void onClick(View v) {
             Intent intent = new Intent(Main.this, ChangePlayerScreen.class);
             startActivity(intent);
         }
     };
     
     private OnClickListener mPlayerInfoListener = new OnClickListener() {
         @Override
         public void onClick(View v) {
             Intent intent = new Intent(Main.this, PlayerInfoScreen.class);
             intent.putExtra(PLAYER_ID_KEY, mPlayer.getId());
             startActivity(intent);
         }
     };
     
     private OnDismissListener mDismissListener = new OnDismissListener() {
         @Override
         public void onDismiss(DialogInterface dialog) {
             startGame();
         }
     };
     
     private OnCheckedChangeListener mDifficultyListener = new OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
             int difficulty = isChecked ? DIFFICULTY_LEVEL_HARD : DIFFICULTY_LEVEL_EASY;
             
             Player player = mPlayer;
             if (player != null) {
                 player.setDifficulty(difficulty);
                 ModelHelper.savePlayer(player, mDao);
             }
         }
     };
     
     private PropertyChangeListener mPropChgListener = new PropertyChangeListener() {
         @Override
         public void propertyChange(PropertyChangeEvent event) {
             if (TwinkleApplication.PROP_CHG_PLAYER.equals(event.getPropertyName())) {
                 Player player = mPlayer;
                 if (player != null) {
                     player.removePropertyChangeListener(this);
                 }
                 
                 Player newPlayer = (Player) event.getNewValue();
                 mPlayer = null;
                 setPlayerWidgetValues(newPlayer, ALL_WIDGETS);
                 mPlayer = newPlayer;
                 
                 if (newPlayer != null) {
                     newPlayer.addPropertyChangeListener(this);
                 }
             } else if (PROP_CHG_LAST_SCORE.equals(event.getPropertyName())) {
                 Player player = (Player) event.getSource();
                 if (player == mPlayer) {
                     setPlayerWidgetValues(player, HISCORE_WIDGET);
                 }
             }
         }
     };
 
     private class AboutListener implements OnClickListener {
         private Dialog mDialog;
         
         public AboutListener(Dialog dialog) {
             mDialog = dialog;
         }
         
         public void onClick(View v) {
             mDialog.cancel();
         }
     };
 }

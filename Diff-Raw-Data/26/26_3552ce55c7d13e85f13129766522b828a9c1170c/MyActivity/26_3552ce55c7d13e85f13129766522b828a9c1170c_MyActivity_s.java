 package com.Norvan.LockPick;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.hardware.*;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.*;
 
 import java.util.List;
 
 public class MyActivity extends Activity
 
 {
 
 
     VibrationHandler vibrationHandler;
     GameHandler gameHandler;
     TextView textPicksLeft, textLevel, textGameOver, textHighScore;
     ImageButton imgbutToggleVolume;
     Button butNextLevel;
     Chronometer chronoTimer;
     AnnouncementHandler announcementHandler;
     SharedPreferencesHandler prefs;
     Context context;
     AudioManager audioManager;
     long gamePausedChronoProgress;
     GraphView graphView;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         context = this;
         vibrationHandler = new VibrationHandler(this);
         if (!vibrationHandler.hasVibrator()) {
             showUnsuportedDialog();
             return;
         }
         gameHandler = new GameHandler(this, gameStatusInterface, vibrationHandler);
         audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
         textPicksLeft = (TextView) findViewById(R.id.textPicksLeft);
         textLevel = (TextView) findViewById(R.id.textLevelNumber);
         textGameOver = (TextView) findViewById(R.id.textGameOver);
         textHighScore = (TextView) findViewById(R.id.textHighScore);
 
         imgbutToggleVolume = (ImageButton) findViewById(R.id.imgbutToggleVolume);
         imgbutToggleVolume.setOnClickListener(onClick);
         butNextLevel = (Button) findViewById(R.id.butNextLevel);
         butNextLevel.setOnClickListener(onClick);
         chronoTimer = (Chronometer) findViewById(R.id.chronoTimer);
         if (SharedPreferencesHandler.isFirstRun(this)) {
             Intent i = new Intent(this, FirstRunActivity.class);
             startActivityForResult(i, 1);
         } else {
             announcementHandler = new AnnouncementHandler(this, vibrationHandler);
             announcementHandler.newLaunch();
         }
         chronoTimer.setKeepScreenOn(true);
         prefs = new SharedPreferencesHandler(this);
         setHighScore(prefs.getHighScore() + 1);
         setVolumeToggleImage(isVolumeMuted());
 
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(0, 0, 0, "Reset User Type");
 
 
         return true;
 
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
         AlertDialog.Builder adb = new AlertDialog.Builder(context);
         adb.setTitle("Alert");
         adb.setMessage("Please restart the app to continue with user type reset.");
         adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                 //To change body of implemented methods use File | Settings | File Templates.
                 SharedPreferencesHandler.clearUserType(context);
                 finish();
             }
         });
         adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
 
 
             }
         });
         adb.create().show();
 
         return super.onMenuItemSelected(featureId, item);    //To change body of overridden methods use File | Settings | File Templates.
     }
 
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
             if (!event.isLongPress() && event.getRepeatCount() == 0) {
                 if (gameHandler.getGameState() == GameHandler.STATE_INGAME) {
                     gameHandler.gotKeyDown();
                 } else {
                     gameHandler.playCurrentLevel();
                 }
             }
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }
 
     @Override
     public boolean onKeyUp(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
             gameHandler.gotKeyUp();
             return true;
         }
         return super.onKeyUp(keyCode, event);
     }
 
     View.OnClickListener onClick = new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             if (butNextLevel.equals(view)) {
                 gameHandler.playCurrentLevel();
             } else if (imgbutToggleVolume.equals(view)) {
                 toggleVolume();
             }
         }
     };
 
     private boolean toggleVolume() {
         if (isVolumeMuted()) {
             audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
             setVolumeToggleImage(false);
             return false;
         } else {
             audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
             setVolumeToggleImage(true);
             return true;
         }
     }
 
     private boolean isVolumeMuted() {
         if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
             return true;
         }
         return false;
     }
 
     private void setVolumeToggleImage(boolean isMute) {
         imgbutToggleVolume.setImageResource(isMute ? R.drawable.ic_audio_vol : R.drawable.ic_audio_vol_mute);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == 1) {
             if (resultCode == RESULT_CANCELED) {
                 Intent i = new Intent(this, FirstRunActivity.class);
                 startActivityForResult(i, 1);
             } else if (resultCode == RESULT_OK) {
                 announcementHandler = new AnnouncementHandler(this, vibrationHandler);
                 announcementHandler.newLaunch();
 
             }
         }
         super.onActivityResult(requestCode, resultCode, data);    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     public GameHandler.GameStatusInterface gameStatusInterface = new GameHandler.GameStatusInterface() {
         @Override
         public void newGameStart() {
             setHighScore(prefs.getHighScore() + 1);
         }
 
         @Override
         public void levelStart(int level, int picksLeft) {
             butNextLevel.setVisibility(View.GONE);
             textGameOver.setVisibility(View.GONE);
 
             chronoTimer.setBase(SystemClock.elapsedRealtime());
             chronoTimer.start();
             setPicksLeft(picksLeft);
            textLevel.setText("Level " + String.valueOf(level + 1)+" "+String.valueOf(gameHandler.getTargetLocation()));
             announcementHandler.levelStart(level, picksLeft);
 //            boolean needsToAdd = false;
 //            if (graphView == null) {
 //                needsToAdd = true;
 //            }
 //            graphView = new GraphView(context, gameHandler.getLevelData(), GraphView.LINE);
 //            if (needsToAdd) {
 //                ((LinearLayout) findViewById(R.id.linearMain)).addView(graphView);
 //            }
 //            graphView.setVisibility(View.VISIBLE);
 
         }
 
         @Override
         public void levelWon(int levelWon, int picksLeft) {
             butNextLevel.setText("Next Level");
             chronoTimer.stop();
             float levelTime = SystemClock.elapsedRealtime() - chronoTimer.getBase();
             butNextLevel.setVisibility(View.VISIBLE);
            graphView.setVisibility(View.GONE);
 
             setPicksLeft(picksLeft);
             announcementHandler.levelWon(levelTime, levelWon);
 
         }
 
         @Override
         public void levelLost(int level, int picksLeft) {
             butNextLevel.setText("Try Again");
             chronoTimer.stop();
             butNextLevel.setVisibility(View.VISIBLE);
 //            graphView.setVisibility(View.GONE);
 
             setPicksLeft(picksLeft);
             announcementHandler.levelLost(level, picksLeft);
         }
 
         @Override
         public void gameOver(int maxLevel) {
             butNextLevel.setText("New Game");
             if (prefs.getHighScore() < maxLevel) {
                 textGameOver.setText("GAME OVER\nHIGH SCORE!");
                 prefs.setHighScore(maxLevel);
                 setHighScore(maxLevel + 1);
             } else {
                 textGameOver.setText("GAME OVER");
             }
 
             textGameOver.setVisibility(View.VISIBLE);
 
             chronoTimer.stop();
             butNextLevel.setVisibility(View.VISIBLE);
 //            graphView.setVisibility(View.GONE);
 
             announcementHandler.gameOver(maxLevel);
 
         }
     };
 
     private void showUnsuportedDialog() {
         AlertDialog.Builder adb = new AlertDialog.Builder(this);
         adb.setTitle("Error!");
         adb.setMessage("Your device does not have a vibrator, which is required for the game.");
         adb.setCancelable(false);
         adb.setPositiveButton("Quit", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 finish();
             }
         });
         adb.create().show();
     }
 
 
     void setPicksLeft(int picks) {
         textPicksLeft.setText("Picks Left: " + String.valueOf(picks));
     }
 
     void setHighScore(int highScore) {
         textHighScore.setText("High Score: " + String.valueOf(highScore));
     }
 
 
     @Override
     protected void onResume() {
         if (gameHandler.getGameState() == GameHandler.STATE_INGAME) {
             gameHandler.setSensorPollingState(true);
             chronoTimer.setBase(getCurrentTime() - gamePausedChronoProgress);
             chronoTimer.start();
         }
         super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     @Override
     protected void onPause() {
         gameHandler.setSensorPollingState(false);
         if (gameHandler.getGameState() == GameHandler.STATE_INGAME) {
             gamePausedChronoProgress = getCurrentTime() - chronoTimer.getBase();
             chronoTimer.stop();
         }
         vibrationHandler.stopVibrate();
         super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     @Override
     protected void onDestroy() {
         announcementHandler.shutDown();
         vibrationHandler.stopVibrate();
         announcementHandler = null;
         vibrationHandler = null;
         gameHandler = null;
         prefs =  null;
 
         super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
     }
 
 
     private long getCurrentTime() {
         return SystemClock.elapsedRealtime();
     }
 }
 

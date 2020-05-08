 package com.Norvan.LockPick;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import com.Norvan.LockPick.Helpers.AnalyticsHelper;
 import com.Norvan.LockPick.Helpers.VolumeToggleHelper;
 import com.Norvan.LockPick.SurvivalMode.SurvivalGameActivity;
 import com.Norvan.LockPick.TimeTrialMode.TimeTrialGameActivity;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ngorgi
  * Date: 2/8/12
  * Time: 9:20 PM
  * To change this template use File | Settings | File Templates.
  */
 public class MainActivity extends Activity {
     private static final int REQ_FIRSTRUNACTIVITY = 1;
     private static final int REQ_SURVIVALGAMEACTIVITY = 2;
     private static final int REQ_TIMETRIALGAMEACTIVITY = 4;
     private static final int REQ_INSTRUCTIONS = 3;
 
     int userType = 0;
 
     Button butNewSurvivalGame, butNewTimeTrialGame, butHelp, butSettings;
     VolumeToggleHelper volumeToggleHelper;
     ImageButton imgbutToggleVolume;
     Context context;
     VibrationHandler vibrationHandler;
     AnnouncementHandler announcementHandler;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.mainlayout);
         context = this;
         vibrationHandler = new VibrationHandler(context);
         if (!vibrationHandler.hasVibrator()) {
             showUnsuportedDialog();
             return;
         }
         butNewSurvivalGame = (Button) findViewById(R.id.butMainNewSurvivalGame);
         butNewTimeTrialGame = (Button) findViewById(R.id.butMainNewTimeTrialGame);
         butHelp = (Button) findViewById(R.id.butMainHelp);
         butSettings = (Button) findViewById(R.id.butMainSettings);
         butNewSurvivalGame.setOnClickListener(onClickListener);
         butNewTimeTrialGame.setOnClickListener(onClickListener);
         butHelp.setOnClickListener(onClickListener);
         butSettings.setOnClickListener(onClickListener);
         butSettings.setText("Reset User Type");
         imgbutToggleVolume = (ImageButton) findViewById(R.id.imgbutMainVolume);
         imgbutToggleVolume.setOnClickListener(onClickListener);
         volumeToggleHelper = new VolumeToggleHelper(this, imgbutToggleVolume);
         if (SharedPreferencesHandler.isFirstRun(this)) {
             startFirstRunActivity();
             return;
         } else {
             announcementHandler = new AnnouncementHandler(this, vibrationHandler);
 
         }
         setVolumeControlStream(AudioManager.STREAM_MUSIC);
         userType = SharedPreferencesHandler.getUserType(context);
         announcementHandler.mainActivityLaunch();
         AnalyticsHelper analyticsHelper = new AnalyticsHelper(this);
         analyticsHelper.startApp(userType);
         analyticsHelper = null;
     }
 
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
             case REQ_FIRSTRUNACTIVITY: {
                 if (resultCode == RESULT_OK) {
                     userType = SharedPreferencesHandler.getUserType(context);
                     announcementHandler = new AnnouncementHandler(context, vibrationHandler);
                     Log.i("AMP", "goonnnaaadoittt");
                     announcementHandler.mainActivityLaunch();
                 } else {
                     startFirstRunActivity();
                 }
             }
             break;
             case REQ_SURVIVALGAMEACTIVITY: {
 
                 if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
                     finish();
                 }
 
             }
             break;
             case REQ_TIMETRIALGAMEACTIVITY: {
                 if (userType == SharedPreferencesHandler.USER_DEAFBLIND) {
                     finish();
                 }
 
             }
         }
 
         super.onActivityResult(requestCode, resultCode, data);    //To change body of overridden methods use File | Settings | File Templates.
     }
 
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
             if (event.getRepeatCount() == 0 && !event.isLongPress()) {
                 if (userType == SharedPreferencesHandler.USER_BLIND || userType == SharedPreferencesHandler.USER_DEAFBLIND) {
 
                     if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                         startSurvivalGameActivity();
                     } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                         startTimeTrialGameActivity();
                     }
                 }
 
             }
             return true;
         }
         return super.onKeyDown(keyCode, event);    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean onKeyUp(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
             return true;
         }
         return super.onKeyUp(keyCode, event);    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
         vibrationHandler.stopVibrate();
         announcementHandler.shutDown();
     }
 
     View.OnClickListener onClickListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
 
             if (imgbutToggleVolume.equals(v)) {
                 volumeToggleHelper.toggleMute();
             } else if (butNewSurvivalGame.equals(v)) {
                 startSurvivalGameActivity();
             } else if (butNewTimeTrialGame.equals(v)) {
                 startTimeTrialGameActivity();
             } else if (butHelp.equals(v)) {
                 startInstructionsActivity();
             } else if (butSettings.equals(v)) {
                 showResetUserTypeDialog();
             }
         }
     };
 
 
     private void startSurvivalGameActivity() {
         announcementHandler.shutUp();
         startActivityForResult(new Intent(context, SurvivalGameActivity.class), REQ_SURVIVALGAMEACTIVITY);
     }
 
     private void startTimeTrialGameActivity() {
         announcementHandler.shutUp();
         startActivityForResult(new Intent(context, TimeTrialGameActivity.class), REQ_TIMETRIALGAMEACTIVITY);
     }
 
     private void startFirstRunActivity() {
        announcementHandler.shutUp();
         startActivityForResult(new Intent(context, FirstRunActivity.class), REQ_FIRSTRUNACTIVITY);
     }
 
     private void startInstructionsActivity() {
         announcementHandler.shutUp();
         startActivityForResult(new Intent(context, Instructions.class), REQ_INSTRUCTIONS);
     }
 
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
 
     private void showResetUserTypeDialog() {
 
         AlertDialog.Builder adb = new AlertDialog.Builder(context);
         adb.setTitle("Alert");
         adb.setMessage("Please restart the app to continue with user type reset.");
         adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
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
 
 
     }
 }

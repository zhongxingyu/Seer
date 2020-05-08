 package com.Norvan.LockPick;
 
 import android.content.Context;
 import android.util.Log;
 import android.widget.Toast;
 import com.Norvan.LockPick.Helpers.ResponseHelper;
 import com.Norvan.LockPick.Helpers.UserType;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ngorgi
  * Date: 2/5/12
  * Time: 2:10 AM
  * To change this template use File | Settings | File Templates.
  */
 public class AnnouncementHandler {
     int userType;
     VibrationHandler vibrationHandler;
     TTSHandler tts;
     Context context;
     ResponseHelper responseHelper;
 
     public AnnouncementHandler(Context context, VibrationHandler vibrationHandler) {
         userType = SharedPreferencesHandler.getUserType(context);
         this.vibrationHandler = vibrationHandler;
         this.context = context;
         tts = new TTSHandler(context);
         responseHelper = new ResponseHelper(context);
     }
 
 
     public void shutDown() {
 //        tts.shutDownTTS(false);
     }
 
 
     public void masterShutDown() {
         tts.shutDownTTS();
     }
 
     public void shutUp() {
         tts.shutUp();
         vibrationHandler.stopVibrate();
     }
 
 
     public void mainActivityLaunch() {
         userType = SharedPreferencesHandler.getUserType(context);
         if (userType == UserType.USER_BLIND) {
             //tts
             tts.speakPhrase(context.getResources().getString(R.string.mainactivityBlind));
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.stopVibrate();
             vibrationHandler.playString(context.getResources().getString(R.string.mainactivityDeafBlind));
         }
     }
 
     public void playPuzzleDescription() {
         if (userType == UserType.USER_BLIND) {
             //tts
             tts.speakPhrase(context.getResources().getString(R.string.puzzleBlind));
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.stopVibrate();
             vibrationHandler.playString(context.getResources().getString(R.string.puzzleDeafBlind));
         }
     }
 
     public void playTimeAttackDescription() {
         if (userType == UserType.USER_BLIND) {
             //tts
             tts.speakPhrase(context.getResources().getString(R.string.timeattackBlind));
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.stopVibrate();
             vibrationHandler.playString(context.getResources().getString(R.string.timeattackDeafBlind));
         }
     }
 
 
     public void puzzlelevelStart(int level, int picksLeft) {
         if (userType == UserType.USER_BLIND) {
             //tts
             tts.speakPhrase("Level " + String.valueOf(level + 1) + ", " + String.valueOf(picksLeft) + " picks left.");
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.playString("lvl " + String.valueOf(level + 1));
         } else {
             tts.speakPhrase("Level " + String.valueOf(level + 1));
         }
     }
 
 
     public void puzzlelevelWon(int wonLevel) {
 
         if (userType == UserType.USER_BLIND) {
             //tts
             if (wonLevel > 8) {
                 tts.speakPhrase(responseHelper.getLevelWin10plus() + " Press either volume key to continue.");
             } else {
                 tts.speakPhrase(responseHelper.getLevelWin0to10() + " Press either volume key to continue.");
             }
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.playString("won lvl " + String.valueOf(wonLevel + 1));
         } else {
             if (wonLevel > 8) {
                 tts.speakPhrase(responseHelper.getLevelWin10plus());
             } else {
                 tts.speakPhrase(responseHelper.getLevelWin0to10());
             }
         }
     }
 
     public void puzzlelevelLost(int level, int picksLeft) {
 
 
         if (userType == UserType.USER_BLIND) {
             //tts
             if (level > 3) {
                 tts.speakPhrase(responseHelper.getLevelLose5plus() + " Press either volume key to try again.");
             } else {
                 tts.speakPhrase("You lost. Press volume to try again");
             }
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.playString("lvl lost " + String.valueOf(picksLeft) + " lives");
         } else {
             if (level > 3) {
                 tts.speakPhrase(responseHelper.getLevelLose5plus());
             } else {
                 tts.speakPhrase("You lost.");
             }
         }
     }
 
     public void gameOver(int score, boolean isHighScore) {
 
         if (userType == UserType.USER_BLIND) {
             //tts
             if (isHighScore) {
                 tts.speakPhrase("Game Over. Your scored " + String.valueOf((score)) + " points. A new high score! Press volume for a new game.");
             } else {
                 tts.speakPhrase("Game Over. Your scored " + String.valueOf((score)) + " points. Press volume for a new game.");
             }
 
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             if (isHighScore) {
                 vibrationHandler.playString("Game over high score " + String.valueOf(score) + " points");
 
             } else {
                 vibrationHandler.playString("Game over score " + String.valueOf(score) + " points");
             }
         } else {
             tts.speakPhrase("Game Over");
         }
 
     }
 
 
     public void userTakingTooLong() {
         if (userType != UserType.USER_DEAFBLIND) {
             tts.speakPhrase(responseHelper.getTakingTooLong());
         }
     }
 
     private String getTimeString(float time) {
         int seconds = (int) time / 1000;
         return String.valueOf(seconds) + " seconds";
 
     }
 
     public void announceTimeLeft(int secondsLeft) {
         if (secondsLeft <= 60 && secondsLeft > 25) {
             if (secondsLeft % 10 == 0) { //Every 10 seconds between 60 and 25 seconds
                 tts.speakFast(String.valueOf(secondsLeft) + " seconds remaining");
             }
         } else if (secondsLeft <= 25 && secondsLeft > 10) {
             if (secondsLeft % 5 == 0) { //Every 5 seconds between 25 and 10 seconds 
                 tts.speakPhrase(String.valueOf(secondsLeft) + " seconds");
             }
        } else if (secondsLeft <= 10 && secondsLeft > 1) {
             tts.speakPhrase(String.valueOf(secondsLeft));
         }
     }
 
 
     public void confirmBackButton() {
         if (userType == UserType.USER_BLIND) {
             tts.speakPhrase("Press back again to exit");
         }
         Toast.makeText(context, "Press back again to quit", Toast.LENGTH_SHORT).show();
 
     }
 
     public void speakQuadrant(int quadrant) {
         if (userType == UserType.USER_BLIND) {
             //tts
 
             switch (quadrant) {
                 case 3:
                     tts.speakPhrase("Game Instructions. Hold to select.");
                     break;
                 case 2:
                     tts.speakPhrase("Puzzle Mode. Hold to select.");
                     break;
                 case 4:
                     tts.speakPhrase("Reset User Type. Hold to select.");
                     break;
                 case 1:
                     tts.speakPhrase("Time Attack Mode. Hold to select.");
                     break;
             }
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.stopVibrate();
             switch (quadrant) {
                 case 3:
                     vibrationHandler.playString("Game Instructions. Hold to select.");
                     break;
                 case 2:
                     vibrationHandler.playString("Puzzle Mode. Hold to select.");
                     break;
                 case 4:
                     vibrationHandler.playString("Reset User Type. Hold to select.");
                     break;
                 case 1:
                     vibrationHandler.playString("Time Attack Mode. Hold to select.");
                     break;
             }
         }
     }
 
     public void gameStartFreshGame() {
         if (userType == UserType.USER_BLIND) {
             //tts
             tts.speakPhrase("Start Game. Hold to select.");
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.stopVibrate();
             vibrationHandler.playString("Start Game. Hold to select.");
         }
     }
 
     public void gameStartNewGame() {
         if (userType == UserType.USER_BLIND) {
             //tts
             tts.speakPhrase("Start New Game. Hold to select.");
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.stopVibrate();
             vibrationHandler.playString("Start New Game. Hold to select.");
         }
     }
 
     public void gameNextLevel(boolean wonLevel) {
         String speakPhrase = "";
         if (wonLevel) {
             speakPhrase = "Next level. Hold to select.";
         } else {
             speakPhrase = "Restart level. Hold to select.";
         }
         String vibePhrase = speakPhrase;
         if (userType == UserType.USER_BLIND) {
             //tts
             tts.speakPhrase(speakPhrase);
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.stopVibrate();
             vibrationHandler.playString(vibePhrase);
         }
     }
 
     public void gamePauseGame() {
         if (userType == UserType.USER_BLIND) {
             //tts
             tts.speakPhrase("Pause Game. Hold to select.");
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.stopVibrate();
             vibrationHandler.playString("Pause Game. Hold to select.");
         }
     }
 
     public void gameResumeGame() {
         if (userType == UserType.USER_BLIND) {
             //tts
             tts.speakPhrase("Resume Game. Hold to select.");
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.stopVibrate();
             vibrationHandler.playString("Resume Game. Hold to select.");
         }
     }
 
     public void readScores(int score, int HighScore) {
         if (userType == UserType.USER_BLIND) {
             //tts
             tts.speakPhrase("Your Score is " + String.valueOf(score) + ". The high score is " + String.valueOf(HighScore) + ".");
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.stopVibrate();
             vibrationHandler.playString("Your Score " + String.valueOf(score) + " high score " + String.valueOf(HighScore));
         }
     }
 
     public void readLevelLabel(int level, boolean isGameOver) {
         String speakPhrase = "Level " + String.valueOf(level);
         if (isGameOver) {
             speakPhrase = "Final level reached was " + String.valueOf(level);
         }
         String vibePhrase = speakPhrase;
         if (userType == UserType.USER_BLIND) {
             //tts
             tts.speakPhrase(speakPhrase);
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.stopVibrate();
             vibrationHandler.playString(vibePhrase);
         }
     }
 
     public void readGameOver() {
         String speakPhrase = "Game Over";
         String vibePhrase = speakPhrase;
         if (userType == UserType.USER_BLIND) {
             //tts
             tts.speakPhrase(speakPhrase);
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.stopVibrate();
             vibrationHandler.playString(vibePhrase);
         }
     }
 
 
     public void readPicksLeft(int picksLeft) {
         String speakPhrase = String.valueOf(picksLeft) + " picks left";
         String vibePhrase = speakPhrase;
         if (userType == UserType.USER_BLIND) {
             //tts
             tts.speakPhrase(speakPhrase);
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.stopVibrate();
             vibrationHandler.playString(vibePhrase);
         }
     }
 
     public void confirmGamePause() {
         if (userType == UserType.USER_BLIND) {
             //tts
             tts.speakPhrase("Game Paused. Hold to resume.");
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.stopVibrate();
             vibrationHandler.playString("Game Paused. Hold to resume.");
         }
     }
 
     public void confirmGameResume() {
         if (userType == UserType.USER_BLIND) {
             //tts
             tts.speakPhrase("Game Resumed");
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.stopVibrate();
             vibrationHandler.playString("Game Resumed");
         }
     }
 
     public void readLevelResult(boolean lastWon, int curLevel) {
 
         String speakPhrase = "";
         if (lastWon) {
             speakPhrase = "You beat level " + String.valueOf(curLevel);
         } else {
             speakPhrase = "Level " + String.valueOf(curLevel + 1) + " failed";
         }
         String vibePhrase = speakPhrase;
         if (userType == UserType.USER_BLIND) {
             //tts
             tts.speakPhrase(speakPhrase);
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.stopVibrate();
             vibrationHandler.playString(vibePhrase);
         }
     }
 
     public void pressBottomLeft() {
         String speakPhrase = "Tap bottom left corner to begin";
         String vibePhrase = speakPhrase;
         if (userType == UserType.USER_BLIND) {
             //tts
             tts.speakPhrase(speakPhrase);
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.stopVibrate();
             vibrationHandler.playString(vibePhrase);
         }
     }
 
     public void readSecondsLeft(int seconds) {
         String speakPhrase = String.valueOf(seconds) + " seconds left";
         String vibePhrase = speakPhrase;
         if (userType == UserType.USER_BLIND) {
             //tts
             tts.speakPhrase(speakPhrase);
         } else if (userType == UserType.USER_DEAFBLIND) {
             //morse
             vibrationHandler.stopVibrate();
             vibrationHandler.playString(vibePhrase);
         }
     }
 
     public void timeTrialWin() {
         if (userType == UserType.USER_BLIND || userType == UserType.USER_NORMAL) {
 
             tts.speakFast(responseHelper.getLevelWin0to10());
 
         }
     }
 
     public void timeTrialLose() {
         if (userType == UserType.USER_BLIND || userType == UserType.USER_NORMAL) {
 
             tts.speakFast(responseHelper.getLevelLoseFast());
 
         }
     }
 }

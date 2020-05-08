 package com.netwokz.tiktaktoe;
 
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import basegameutils.BaseGameActivity;
 
 /**
  * Created by Stephen on 8/29/13.
  */
 public class TikTakToeActivity extends BaseGameActivity implements MainMenuScreen.MainMenu, AfterGameDialog.ButtonListener {
 
     private TikTakToeGame mGame;
 
     private Button mBoardButtons[];
 
     private TextView mInfo, mPlayerOneCount, mTieCount, mAndroidCount, mPlayerOneCountHeader, mAndroidCountHeader;
 
     private int mPlayerOneCounter = 0;
     private int mPlayerTwoCounter = 0;
     private int mTieCounter = 0;
     private int mAndroidCounter = 0;
 
     private boolean mPlayerOneFirst = true;
     private boolean mIsOnePlayer = false;
     private boolean mPlayerOneTurn = true;
     private boolean mGameOver = false;
 
     AfterGameDialog mDialog = null;
 
     SharedPreferences mPrefs;
     SharedPreferences.Editor mPrefsEditor;
 
    long mGameStartTime;
 
     String PLAYER_ONE = "Player 1:";
     String PLAYER_TWO = "Player 2:";
     String ANDROID_PLAYER = "Android:";
     private String MAIN_MENU_TAG = "main_menu_fragment";
     private String GAME_IN_PROGRESS_PREF = "game_in_progress";
     private String GAME_MOVE_STATE_PREF = "game_move_state";
     private String PLAYER_ONE_FIRST_PREF = "player_one_first";
     private String IS_ONE_PLAYER_PREF = "is_one_player";
     private String STAT_ALL_TIME_GAMES_PLAYED = "all_time_games_played";
     private String STAT_ALL_TIME_GAMES_WON = "all_time_games_won";
     private String STAT_ALL_TIME_GAMES_LOST = "all_time_games_lost";
     private String STAT_ALL_TIME_TIME_PLAYED = "all_time_time_played";
     private String STAT_ALL_TIME_GAMES_TIE = "all_time_games_tie";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.tik_tak_toe_game);
 
         mBoardButtons = new Button[TikTakToeGame.getBoardSize()];
         mGame = new TikTakToeGame();
 
         mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
         mPrefsEditor = mPrefs.edit();
 
         initializeViews();
         if (!isGameInProgress()) {
             initiateStartScreen();
         } else {
             mGame.clearBoard();
             loadGame();
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         if (isGameInProgress())
             saveGame();
     }
 
     private void initiateStartScreen() {
         MainMenuScreen mainMenuScreen = new MainMenuScreen();
         FragmentManager fm = getFragmentManager();
         FragmentTransaction ft = fm.beginTransaction();
         ft.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
         ft.add(android.R.id.content, mainMenuScreen, MAIN_MENU_TAG);
         ft.addToBackStack(null);
         ft.commit();
     }
 
     private void initializeViews() {
 
         mBoardButtons[0] = (Button) findViewById(R.id.btn_1);
         mBoardButtons[1] = (Button) findViewById(R.id.btn_2);
         mBoardButtons[2] = (Button) findViewById(R.id.btn_3);
         mBoardButtons[3] = (Button) findViewById(R.id.btn_4);
         mBoardButtons[4] = (Button) findViewById(R.id.btn_5);
         mBoardButtons[5] = (Button) findViewById(R.id.btn_6);
         mBoardButtons[6] = (Button) findViewById(R.id.btn_7);
         mBoardButtons[7] = (Button) findViewById(R.id.btn_8);
         mBoardButtons[8] = (Button) findViewById(R.id.btn_9);
 
         mInfo = (TextView) findViewById(R.id.tv_info_header);
         mPlayerOneCount = (TextView) findViewById(R.id.tv_human_value);
         mTieCount = (TextView) findViewById(R.id.tv_ties_value);
         mAndroidCount = (TextView) findViewById(R.id.tv_android_value);
         mPlayerOneCountHeader = (TextView) findViewById(R.id.tv_human_header);
         mAndroidCountHeader = (TextView) findViewById(R.id.tv_android_header);
 
         mPlayerOneCount.setText(Integer.toString(mPlayerOneCounter));
         mTieCount.setText(Integer.toString(mTieCounter));
         mAndroidCount.setText(Integer.toString(mAndroidCounter));
     }
 
     public void setUpOnePlayerGame() {
         mPlayerOneCountHeader.setText(PLAYER_ONE);
         mAndroidCountHeader.setText(ANDROID_PLAYER);
     }
 
     public void setUpTwoPlayerGame() {
         mPlayerOneCountHeader.setText(PLAYER_ONE);
         mAndroidCountHeader.setText(PLAYER_TWO);
     }
 
     public void setGameInProgress(String state) {
         mPrefsEditor.putString(GAME_IN_PROGRESS_PREF, state);
         mPrefsEditor.commit();
     }
 
     public boolean isGameInProgress() {
         if (mPrefs.getString(GAME_IN_PROGRESS_PREF, "false").equals("true")) {
             return true;
         }
         return false;
     }
 
     private void startNewGame(boolean onePlayer) {
         setGameInProgress("true");
         this.mIsOnePlayer = onePlayer;
         mGame.clearBoard();
 
         for (int i = 0; i < mBoardButtons.length; i++) {
             mBoardButtons[i].setText("");
             mBoardButtons[i].setEnabled(true);
             mBoardButtons[i].setOnClickListener(new ButtonClickListener(i));
         }
 
         if (mIsOnePlayer) {
             setUpOnePlayerGame();
             if (mPlayerOneFirst) {
                 mInfo.setText(R.string.first_human);
                 mPlayerOneFirst = false;
             } else {
                 mInfo.setText(R.string.turn_android);
                 int move = mGame.getAndroidMove();
                 setMove(TikTakToeGame.ANDROID_PLAYER, move);
                 mPlayerOneFirst = true;
                 mInfo.setText(R.string.turn_human);
             }
         } else {
             setUpTwoPlayerGame();
             if (mPlayerOneFirst) {
                 mInfo.setText(R.string.turn_player_one);
                 mPlayerOneFirst = false;
             } else {
                 mInfo.setText(R.string.turn_player_two);
                 mPlayerOneFirst = true;
                 mInfo.setText(R.string.turn_player_one);
             }
         }
 
         mGameStartTime = System.currentTimeMillis();
 
         mGameOver = false;
     }
 
     private void updateStats(String stat, Long value) {
         if (stat.equals(STAT_ALL_TIME_GAMES_PLAYED)) {
             long tmp = mPrefs.getLong(STAT_ALL_TIME_GAMES_PLAYED, 0);
             if (tmp > 0) {
                 tmp += value;
                 mPrefsEditor.putLong(STAT_ALL_TIME_GAMES_PLAYED, tmp);
                 mPrefsEditor.commit();
             }
         } else if (stat.equals(STAT_ALL_TIME_TIME_PLAYED)) {
             long tmp = mPrefs.getLong(STAT_ALL_TIME_TIME_PLAYED, 0);
             if (tmp > 0) {
                 tmp += value;
                 mPrefsEditor.putLong(STAT_ALL_TIME_TIME_PLAYED, tmp);
                 mPrefsEditor.commit();
             }
         } else if (stat.equals(STAT_ALL_TIME_GAMES_WON)) {
             long tmp = mPrefs.getLong(STAT_ALL_TIME_GAMES_WON, 0);
             if (tmp > 0) {
                 tmp += value;
                 mPrefsEditor.putLong(STAT_ALL_TIME_GAMES_WON, tmp);
                 mPrefsEditor.commit();
             }
         } else if (stat.equals(STAT_ALL_TIME_GAMES_LOST)) {
             long tmp = mPrefs.getLong(STAT_ALL_TIME_GAMES_LOST, 0);
             if (tmp > 0) {
                 tmp += value;
                 mPrefsEditor.putLong(STAT_ALL_TIME_GAMES_LOST, tmp);
                 mPrefsEditor.commit();
             }
         }
     }
 
     public void saveGame() {
         char[] gameMoves = mGame.getGameState();
         String moves = new String(gameMoves);
         mPrefsEditor.putBoolean(PLAYER_ONE_FIRST_PREF, mPlayerOneFirst);
         mPrefsEditor.putString(GAME_MOVE_STATE_PREF, moves);
         mPrefsEditor.commit();
     }
 
     public void loadGame() {
         String moves = mPrefs.getString(GAME_MOVE_STATE_PREF, null);
         mPlayerOneFirst = mPrefs.getBoolean(PLAYER_ONE_FIRST_PREF, true);
         mIsOnePlayer = mPrefs.getBoolean(IS_ONE_PLAYER_PREF, true);
         if (moves != null) {
             startGame(mIsOnePlayer);
             char[] gameMoves = moves.toCharArray();
             for (int i = 0; i < mBoardButtons.length; i++) {
                 if (gameMoves[i] == 'X') {
                     setMove(gameMoves[i], i);
                 } else if (gameMoves[i] == 'O') {
                     setMove(gameMoves[i], i);
                 }
             }
         }
     }
 
     public void setMove(char player, int location) {
         mGame.setMove(player, location);
         mBoardButtons[location].setEnabled(false);
         mBoardButtons[location].setText(String.valueOf(player));
 
         if (player == TikTakToeGame.PLAYER_ONE) {
             mBoardButtons[location].setTextColor(getResources().getColor(R.color.BLUE));
         } else {
             mBoardButtons[location].setTextColor(getResources().getColor(R.color.RED));
         }
 
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         MenuItem signIn = menu.findItem(R.id.action_sign_in);
         MenuItem signOut = menu.findItem(R.id.action_sign_out);
 
         if (!isSignedIn()) {
             signIn.setVisible(true);
             signOut.setVisible(false);
         } else {
             signIn.setVisible(false);
             signOut.setVisible(true);
         }
 
         return true;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_sign_in:
                 if (!isSignedIn()) {
                     beginUserInitiatedSignIn();
                 }
                 break;
             case R.id.action_sign_out:
                 if (isSignedIn())
                     signOut();
                 break;
         }
         return true;
     }
 
     @Override
     public void onSignInFailed() {
         invalidateOptionsMenu();
     }
 
     @Override
     public void onSignInSucceeded() {
         invalidateOptionsMenu();
     }
 
     @Override
     public void startGame(boolean gameType) {
         startNewGame(gameType);
     }
 
     @Override
     public void exitGame() {
         this.finish();
     }
 
     @Override
     public void dismissMainMenu() {
         FragmentManager fm = getFragmentManager();
         FragmentTransaction ft = fm.beginTransaction();
         MainMenuScreen mainMenuScreen = (MainMenuScreen) fm.findFragmentByTag(MAIN_MENU_TAG);
         ft.remove(mainMenuScreen);
         ft.commit();
     }
 
     @Override
     public void newGame() {
         if (mDialog != null) {
             startGame(mIsOnePlayer);
             mDialog.dismiss();
             mDialog = null;
         }
     }
 
     @Override
     public void returnToMainMenu() {
         if (mDialog != null) {
             initiateStartScreen();
             mDialog.dismiss();
             mDialog = null;
         }
     }
 
     private class ButtonClickListener implements View.OnClickListener {
         int location;
 
         public ButtonClickListener(int location) {
             this.location = location;
         }
 
         public void onClick(View view) {
             if (!mGameOver) {
                 if (mBoardButtons[location].isEnabled()) {
                     if (mIsOnePlayer) {
                         // One Player Game
                         setMove(TikTakToeGame.PLAYER_ONE, location);
                         int winner = mGame.checkForWinner();
                         if (winner == 0) {
                             mInfo.setText(R.string.turn_android);
                             int move = mGame.getAndroidMove();
                             setMove(TikTakToeGame.ANDROID_PLAYER, move);
                             winner = mGame.checkForWinner();
                         }
 
                         if (winner == 0)
                             mInfo.setText(R.string.turn_human);
                         else if (winner == 1) {
                             mInfo.setText(R.string.result_tie);
                             mTieCounter++;
                             mTieCount.setText(Integer.toString(mTieCounter));
                             setGameInProgress("false");
                             mGameOver = true;
                             updateStats(STAT_ALL_TIME_TIME_PLAYED, System.currentTimeMillis() - mGameStartTime);
                             updateStats(STAT_ALL_TIME_GAMES_PLAYED, 1l);
                             updateStats(STAT_ALL_TIME_GAMES_TIE, 1l);
                             mDialog = AfterGameDialog.newInstance();
                             mDialog.show(getFragmentManager(), "Dialog");
                         } else if (winner == 2) {
                             mInfo.setText(R.string.result_human_win);
                             mPlayerOneCounter++;
                             mPlayerOneCount.setText(Integer.toString(mPlayerOneCounter));
                             setGameInProgress("false");
                             mGameOver = true;
                             updateStats(STAT_ALL_TIME_TIME_PLAYED, System.currentTimeMillis() - mGameStartTime);
                             updateStats(STAT_ALL_TIME_GAMES_PLAYED, 1l);
                             updateStats(STAT_ALL_TIME_GAMES_WON, 1l);
                             mDialog = AfterGameDialog.newInstance();
                             mDialog.show(getFragmentManager(), "Dialog");
                         } else {
                             mInfo.setText(R.string.result_android_win);
                             mAndroidCounter++;
                             mAndroidCount.setText(Integer.toString(mAndroidCounter));
                             setGameInProgress("false");
                             mGameOver = true;
                             updateStats(STAT_ALL_TIME_TIME_PLAYED, System.currentTimeMillis() - mGameStartTime);
                             updateStats(STAT_ALL_TIME_GAMES_PLAYED, 1l);
                             updateStats(STAT_ALL_TIME_GAMES_LOST, 1l);
                             mDialog = AfterGameDialog.newInstance();
                             mDialog.show(getFragmentManager(), "Dialog");
                         }
                     } else {
                         // Two Player Game
                         if (mPlayerOneTurn) {
                             setMove(TikTakToeGame.PLAYER_ONE, location);
                         } else {
                             setMove(TikTakToeGame.PLAYER_TWO, location);
                         }
                         int winner = mGame.checkForWinner();
                         if (winner == 0) {
                             if (mPlayerOneTurn) {
                                 mInfo.setText(R.string.turn_player_two);
                                 mPlayerOneTurn = false;
                             } else {
                                 mInfo.setText(R.string.turn_player_one);
                                 mPlayerOneTurn = true;
                             }
 
                         } else if (winner == 1) {
                             mInfo.setText(R.string.result_tie);
                             mTieCounter++;
                             mTieCount.setText(Integer.toString(mTieCounter));
                             setGameInProgress("false");
                             mGameOver = true;
                             updateStats(STAT_ALL_TIME_TIME_PLAYED, System.currentTimeMillis() - mGameStartTime);
                             updateStats(STAT_ALL_TIME_GAMES_PLAYED, 1l);
                             updateStats(STAT_ALL_TIME_GAMES_TIE, 1l);
                             mDialog = AfterGameDialog.newInstance();
                             mDialog.show(getFragmentManager(), "Dialog");
                         } else if (winner == 2) {
                             mInfo.setText(R.string.result_player_one_wins);
                             mPlayerOneCounter++;
                             mPlayerOneCount.setText(Integer.toString(mPlayerOneCounter));
                             setGameInProgress("false");
                             mGameOver = true;
                             updateStats(STAT_ALL_TIME_TIME_PLAYED, System.currentTimeMillis() - mGameStartTime);
                             updateStats(STAT_ALL_TIME_GAMES_PLAYED, 1l);
                             updateStats(STAT_ALL_TIME_GAMES_WON, 1l);
                             mDialog = AfterGameDialog.newInstance();
                             mDialog.show(getFragmentManager(), "Dialog");
                         } else {
                             mInfo.setText(R.string.result_player_two_wins);
                             mPlayerTwoCounter++;
                             mAndroidCount.setText(Integer.toString(mPlayerTwoCounter));
                             setGameInProgress("false");
                             mGameOver = true;
                             updateStats(STAT_ALL_TIME_TIME_PLAYED, System.currentTimeMillis() - mGameStartTime);
                             updateStats(STAT_ALL_TIME_GAMES_PLAYED, 1l);
                             updateStats(STAT_ALL_TIME_GAMES_LOST, 1l);
                             mDialog = AfterGameDialog.newInstance();
                             mDialog.show(getFragmentManager(), "Dialog");
                         }
                     }
                 }
             }
         }
     }
 }

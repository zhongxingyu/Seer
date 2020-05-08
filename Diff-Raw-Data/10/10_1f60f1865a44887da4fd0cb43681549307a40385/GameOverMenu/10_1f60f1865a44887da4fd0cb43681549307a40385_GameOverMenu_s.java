 package com.slauson.asteroid_dasher.menu;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.slauson.asteroid_dasher.game.Game;
 import com.slauson.asteroid_dasher.game.GameActivity;
 import com.slauson.asteroid_dasher.other.Util;
 import com.slauson.asteroid_dasher.status.Achievements;
 import com.slauson.asteroid_dasher.status.GlobalStatistics;
 import com.slauson.asteroid_dasher.status.HighScores;
 import com.slauson.asteroid_dasher.status.LocalStatistics;
 import com.slauson.asteroid_dasher.status.Options;
 import com.slauson.asteroid_dasher.status.Points;
 import com.slauson.asteroid_dasher.status.Statistics;
 import com.slauson.asteroid_dasher.R;
 
 
 public class GameOverMenu extends PaidDialogBaseMenu {
 	
 	/** Length of toast notification **/
 	private static final int TOAST_LENGTH_SHORT = 2000;
 	
 	/** Time the back button has to be hit again by to quit **/
 	private long backButtonQuitEndTime;
 	
 	/** Game mode that we just ended **/
 	private int gameMode;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.game_over_menu);
 		
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			gameMode = extras.getInt(GameActivity.BUNDLE_FLAG_GAME_MODE);
 		} else {
 			gameMode = Game.GAME_MODE_NORMAL;
 		}
 
 		setup();
 	
 		backButtonQuitEndTime = 0;
 	}
 	
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		// override back button so that user cannot go back to game
 		switch(keyCode) {
 		case KeyEvent.KEYCODE_BACK:
 			if (System.currentTimeMillis() < backButtonQuitEndTime) {
 				Intent intent = new Intent(GameOverMenu.this, MainMenu.class);
 				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(intent);
 			} else {
 				Toast.makeText(this, "Press again to quit", Toast.LENGTH_SHORT).show();
 				backButtonQuitEndTime = System.currentTimeMillis() + TOAST_LENGTH_SHORT;
 			}
 			return true;
 		}
 		
 		// let other buttons go through
 		super.onKeyUp(keyCode, event);
 		return true;
 	}
 	
 	/**
 	 * Sets up game over menu
 	 */
 	private void setup() {
 
 		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
 		SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
 		
 		// calculate points
 		Statistics localStatistics = LocalStatistics.getInstance();
 		int points = 0;
 		points += Points.POINTS_TIME_PLAYED*localStatistics.timePlayed;
 		points += Points.POINTS_ASTEROIDS_DESTROYED*localStatistics.getTotalNumAsteroidsDestroyed();
 		
 		// update statistics
 		GlobalStatistics.update();
 
 		// update high scores
 		updateHighScores(sharedPreferencesEditor);
 				
 		// check/update achievements if paid version
 		if (!Options.demoVersion) {
 			updateAchievements(sharedPreferencesEditor);
 			
 			// update points after checking achievements
 			points += Points.POINTS_ACHIEVEMENT*Achievements.localAchievements.size();
 			
 			// points achievements
 			if (points > Achievements.LOCAL_OTHER_POINTS_NUM_1) {
 				if (Achievements.unlockLocalAchievement(Achievements.localOtherPoints1)) {
 					points += Points.POINTS_ACHIEVEMENT;
 				}
 			}
 			if (points > Achievements.LOCAL_OTHER_POINTS_NUM_2) {
 				if (Achievements.unlockLocalAchievement(Achievements.localOtherPoints2)) {
 					points += Points.POINTS_ACHIEVEMENT;
 				}
 			}
 			if (points > Achievements.LOCAL_OTHER_POINTS_NUM_3) {
 				if (Achievements.unlockLocalAchievement(Achievements.localOtherPoints3)) {
 					points += Points.POINTS_ACHIEVEMENT;
 				}
 			}
 		}
 				
 		// update points
 		updatePoints(sharedPreferencesEditor, points);
 		
 		// update points in statistics and save result
 		GlobalStatistics.getInstance().pointsEarned += points;
 		GlobalStatistics.save(sharedPreferencesEditor);
 
 		// commit everything
 		sharedPreferencesEditor.commit();
 
 		// calculate number of achievements
 		int numAchievements = Achievements.localAchievements.size();
 		
 		
 		// survival time
 		TextView gameOverSummaryTime = (TextView)findViewById(R.id.gameOverSummaryTime);
 		gameOverSummaryTime.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(GameOverMenu.this, HighScoresMenu.class);
 				startActivity(intent);
 			}
 		});
		gameOverSummaryTime.setText(localStatistics.timePlayed + "");
 		
 		// points earned
 		TextView gameOverSummaryPoints = (TextView)findViewById(R.id.gameOverSummaryPoints);
 		gameOverSummaryPoints.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(GameOverMenu.this, PointDetailsMenu.class);
 				startActivity(intent);
 			}
 		});
 		gameOverSummaryPoints.setText(Util.getPointsString(points) + " points earned");
 		
 		// achievements unlocked
 		TextView gameOverSummaryAchievements = (TextView)findViewById(R.id.gameOverSummaryAchievements);
 		gameOverSummaryAchievements.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				// check if free version
 				if (Options.demoVersion) {
 					Bundle bundle = new Bundle();
 					bundle.putInt(DIALOG_EXTRA_PAID_FEATURE, R.string.menu_achievements);
 					showDialog(DIALOG_PAID_VERSION, bundle);
 				} else {
 					Intent intent = new Intent(GameOverMenu.this, LocalAchievementsMenu.class);
 					startActivity(intent);
 				}
 			}
 		});
 		
 		if (numAchievements == 1) {
 			gameOverSummaryAchievements.setText(numAchievements + " achievement unlocked");
 		} else {
 			gameOverSummaryAchievements.setText(numAchievements + " achievements unlocked");			
 		}
 		
 		// retry button
 		Button gameOverMenuRetryButton = (Button)findViewById(R.id.gameOverMenuRetryButton);
 		gameOverMenuRetryButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				Intent intent = new Intent(GameOverMenu.this, GameActivity.class);
 				intent.putExtra(GameActivity.BUNDLE_FLAG_GAME_MODE, gameMode);
 				startActivity(intent);
 			}
 		});
 		
 		// statistics button
 		Button gameOverMenuStatisticsButton = (Button)findViewById(R.id.gameOverMenuStatisticsButton);
 		gameOverMenuStatisticsButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				Intent intent = new Intent(GameOverMenu.this, LocalStatisticsMenu.class);
 				startActivity(intent);
 			}
 		});
 		
 		// upgrades button
 		Button gameOverMenuUpgradesButton = (Button)findViewById(R.id.gameOverMenuUpgradesButton);
 		gameOverMenuUpgradesButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				// check if free version
 				if (Options.demoVersion) {
 					Bundle bundle = new Bundle();
 					bundle.putInt(DIALOG_EXTRA_PAID_FEATURE, R.string.menu_upgrades);
 					showDialog(DIALOG_PAID_VERSION, bundle);
 				} else {
 					Intent intent = new Intent(GameOverMenu.this, UpgradesMenu.class);
 					startActivity(intent);
 				}
 			}
 		});
 
 		// quit button
 		Button gameOverMenuQuitButton = (Button)findViewById(R.id.gameOverMenuQuitButton);
 		gameOverMenuQuitButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				Intent intent = new Intent(GameOverMenu.this, MainMenu.class);
 				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(intent);
 			}
 		});
 	}
 	/**
 	 * Updates achievements
 	 */
 	private void updateAchievements(SharedPreferences.Editor sharedPreferencesEditor) {
 		
 		Statistics localStatistics = LocalStatistics.getInstance();
 		
 		// playtime
 		if (localStatistics.timePlayed > Achievements.LOCAL_PLAYTIME_1) {
 			Achievements.unlockLocalAchievement(Achievements.localPlaytime1);
 		}
 		if (localStatistics.timePlayed > Achievements.LOCAL_PLAYTIME_2) {
 			Achievements.unlockLocalAchievement(Achievements.localPlaytime2);
 		}
 		if (localStatistics.timePlayed > Achievements.LOCAL_PLAYTIME_3) {
 			Achievements.unlockLocalAchievement(Achievements.localPlaytime3);
 		}
 		
 		Achievements.checkGlobalAchievements();
 		
 		Achievements.save(sharedPreferencesEditor);
 	}
 	
 	/**
 	 * Updates points
 	 */
 	private void updatePoints(SharedPreferences.Editor sharedPreferencesEditor, int points) {
 		Points.update(points);
 		Points.save(sharedPreferencesEditor);
 	}
 
 	/**
 	 * Updates high scores
 	 */
 	private void updateHighScores(SharedPreferences.Editor sharedPreferencesEditor) {
 		HighScores.update(LocalStatistics.getInstance().timePlayed);
 		HighScores.save(sharedPreferencesEditor);
 	}
 }

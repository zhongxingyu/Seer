 package edu.hm.androidsweeper.test.activities;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.test.ActivityInstrumentationTestCase2;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.SeekBar;
 
 import com.jayway.android.robotium.solo.Solo;
 
 import edu.hm.androidsweeper.R;
 import edu.hm.androidsweeper.activities.GameActivity;
 import edu.hm.androidsweeper.activities.HighscoreActivity;
 import edu.hm.androidsweeper.activities.MainMenuActivity;
 import edu.hm.androidsweeper.activities.MyPreferenceActivity;
 import edu.hm.androidsweeper.dialogs.GameFinishedDialog;
 import edu.hm.androidsweeper.difficulties.CustomizedDifficulty;
 import edu.hm.androidsweeper.difficulties.EasyDifficulty;
 import edu.hm.androidsweeper.difficulties.IDifficulty;
 import edu.hm.androidsweeper.gamelogic.Game;
 import edu.hm.androidsweeper.gamelogic.GameState;
 
 public class RobotiumTest extends
 		ActivityInstrumentationTestCase2<MainMenuActivity> {
 
 	private Solo solo;
 
 	private final String sampleString1 = "string1";
 	private final String sampleString2 = "string2";
 
 	public RobotiumTest() {
 		super(MainMenuActivity.class);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 
 	public void testNavToSettings() throws Exception {
 		solo.clickOnMenuItem(solo.getString(R.string.menu_settings));
 		solo.waitForActivity("MyPreferenceActivity", 500);
 		solo.assertCurrentActivity("MyPreferenceActivity expected",
 				MyPreferenceActivity.class);
 	}
 
 	public void testCreateNewEasyGame() throws Exception {
 		solo.clickOnView(solo.getView(R.id.textView_newGame));
 		solo.waitForActivity("DifficultyActivity", 500);
 		solo.clickOnRadioButton(0);
 		solo.clickOnView(solo.getView(R.id.button_start));
 		solo.waitForActivity("GameActivity", 500);
 		solo.assertCurrentActivity("GameActivity expected", GameActivity.class);
 	}
 
 	public void testHighscores() throws Exception {
 		solo.clickOnView(solo.getView(R.id.textView_highscore));
 		solo.waitForActivity("HighscoreActivity", 500);
 		solo.clickOnView(solo.getView(R.id.textViewEasy));
 		solo.clickOnView(solo.getView(R.id.textViewMedium));
 		solo.clickOnView(solo.getView(R.id.textViewHard));
 		solo.assertCurrentActivity("HighscoreActivity expected",
 				HighscoreActivity.class);
 	}
 
 	public void testChangeDefaultPlayerName() throws Exception {
 		solo.clickOnMenuItem(solo.getString(R.string.menu_settings));
 		solo.waitForActivity("MyPreferenceActivity", 500);
 		solo.clickOnText(solo.getString(R.string.prefs_default_player_name));
 		EditText playerNameInput = solo.getEditText(0);
 		String currentPlayerName = playerNameInput.getText().toString();
 		solo.clearEditText(playerNameInput);
 		String expected = null;
 		if (!currentPlayerName.equals(sampleString1)) {
 			solo.enterText(playerNameInput, sampleString1);
 			expected = sampleString1;
 		} else {
 			solo.enterText(playerNameInput, sampleString2);
 			expected = sampleString2;
 		}
 		solo.clickOnView(solo.getView(android.R.id.button1));
 		solo.goBackToActivity("MainMenuActivity");
 		Game game = new Game(new EasyDifficulty());
 		game.setCurrentPlaytime(10);
 		game.setState(GameState.WON);
 		Activity current = solo.getCurrentActivity();
 
 		final Dialog gameFinishedDialog = new GameFinishedDialog(current, game);
 		current.runOnUiThread(new Runnable() {
 
 			public void run() {
 				gameFinishedDialog.show();
 			}
 		});
 		solo.waitForText(solo.getString(R.string.dialog_finished_won_title));
 		EditText playerName = solo.getEditText(0);
 		String player = playerName.getText().toString();
 		assertEquals(expected, player);
 	}
 
 	public void testCustomizeDifficulty() throws Exception {
 		solo.clickOnView(solo.getView(R.id.textView_newGame));
 		solo.waitForActivity("DifficultyActivity", 500);
 		solo.clickOnRadioButton(3);
 		SeekBar bombs = (SeekBar) solo.getView(R.id.seekBar_bombs);
 		SeekBar height = (SeekBar) solo.getView(R.id.seekBar_height);
 		SeekBar width = (SeekBar) solo.getView(R.id.seekBar_width);
 
 		bombs.setProgress(10);
 		height.setProgress(10);
 		width.setProgress(10);
 
 		solo.clickOnView(solo.getView(R.id.button_start));
		solo.waitForActivity("GameActivity", 500);
 		Bundle extras = solo.getCurrentActivity().getIntent().getExtras();
 		IDifficulty difficulty = (IDifficulty) extras.get(IDifficulty.EXTRA_NAME);
 		assertEquals(10 + CustomizedDifficulty.MIN_BOMBS, difficulty.getBombs());
 		assertEquals(10 + CustomizedDifficulty.MIN_SIZE, difficulty.getWidth());
 		assertEquals(10 + CustomizedDifficulty.MIN_SIZE, difficulty.getHeight());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.test.ActivityInstrumentationTestCase2#tearDown()
 	 */
 	@Override
 	protected void tearDown() throws Exception {
 		solo.finishOpenedActivities();
 	}
 
 }

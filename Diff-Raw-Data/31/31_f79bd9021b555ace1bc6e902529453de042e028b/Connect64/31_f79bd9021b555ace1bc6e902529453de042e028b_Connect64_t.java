 package edu.uwg.jamestwyford.connect64;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Arrays;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.util.Base64;
 import android.util.Log;
 import android.util.SparseIntArray;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SubMenu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Chronometer;
 import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import edu.uwg.jamestwyford.connect64.db.ScoresContentProviderDB;
 import edu.uwg.jamestwyford.connect64.db.ScoresContract.Scores;
 
 /**
  * Game logic and GUI handling for the 8x8 board.
  * 
  * @author jtwyford
  * @version assignment3
  */
 public class Connect64 extends Activity implements
 		AdapterView.OnItemSelectedListener, OnSharedPreferenceChangeListener {
 
 	private static final int BAD_VALUE = -1;
 	private static final int BOARD_MAX = 64;
 	private static final int COLUMN_DELTA = 1;
 	private static final int COL_SIZE = 8;
 	private static final int NUM_INPUT_BUTTONS = 16;
 	private static final int MENU_PUZZLE_OFFSET = 100;
 	private static final int ROW_DELTA = 10;
 	private static final int ROW_SIZE = 8;
 	private static final int STARTING_PUZZLE = 1;
 	private static final String CURRENT_INPUT = "currentInput";
 	private static final String CURRENT_PUZZLE = "currentPuzzle";
 	private static final String CURRENT_RANGE = "currentRange";
 	private static final String ELAPSED_TIME = "elapsedTime";
 	private static final String GAME_STATE_PREFS = "gameStatePrefs";
 	private static final String LOG_TAG = "C64";
 	private static final String MAX_PUZZLE_ATTEMPTED = "maxPuzzleAttempted";
 	private static final String STATE_POSITIONS = "statePositions";
 	private static final String STATE_VALUES = "stateValues";
 	private static final String TIMER_RUNNING = "timerRunning";
 	private static final String PLAYER_NAME = "Player";
 
 	// views
	private LinearLayout gameBoard;
 	private Button[] inputButtons;
 	private ImageButton pauseResume;
 	private TextView puzzleLabel;
 	private Spinner rangeSpinner;
 	private Chronometer timer;
 
 	// game state
 	private SparseIntArray boardState;
 	private int currentInput;
 	private int currentPuzzle;
 	private int currentRange;
 	private long elapsedTime;
 	private int maxPuzzleAttempted;
 	private boolean timerRunning;
 
 	// prefs
 	private SharedPreferences preferences;
 	private boolean autoFillIn;
 	private int prefNumberColor;
 
 	/**
 	 * Input handler for the clear button. Reloads the current puzzle and resets
 	 * the timer.
 	 * 
 	 * @param view
 	 *            ignored
 	 */
 	public final void clearButtonClick(final View view) {
 		loadPuzzle(this.currentPuzzle, true);
 	}
 
 	/**
 	 * Input handler for the 64 game board Buttons. If the current input is
 	 * valid, sets the clicked Button to that value. Clears the Button
 	 * otherwise. If the board is full, checks if the player has won and moves
 	 * to the next Puzzle if so.
 	 * 
 	 * @param view
 	 *            the Button clicked
 	 */
 	public final void gameButtonClick(final View view) {
 		setGameButtonText((Button) view);
 		if (this.boardState.size() == BOARD_MAX) {
 			checkWinCondition();
 		}
 	}
 
 	/**
 	 * Input handler for the 16 input buttons. Sets the current input to the
 	 * value of the clicked Button.
 	 * 
 	 * @param view
 	 *            the Button clicked
 	 */
 	public final void inputButtonClick(final View view) {
 		this.currentInput = Integer.parseInt(((Button) view).getText()
 				.toString());
 	}
 
 	@Override
 	public final boolean onContextItemSelected(final MenuItem item) {
 		if (this.autoFillIn) {
 			this.currentInput = Integer.parseInt(item.getTitle().toString());
 			final Button button = getGameButton(String
 					.valueOf(item.getItemId()));
 			setGameButtonText(button);
 			return true;
 		}
 		return super.onContextItemSelected(item);
 	}
 
 	@Override
 	public final void onCreateContextMenu(final ContextMenu menu,
 			final View view, final ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, view, menuInfo);
 		if (this.autoFillIn) {
 			final int position = Integer.parseInt(view.getTag().toString());
 			final int[] options = getValidOptions(position);
 			menu.clear();
 			for (final int value : options) {
 				menu.add(Menu.NONE, position, Menu.NONE, String.valueOf(value));
 			}
 		}
 	}
 
 	@Override
 	public final boolean onCreateOptionsMenu(final Menu menu) {
 		getMenuInflater().inflate(R.menu.connect64, menu);
 		return true;
 	}
 
 	@Override
 	public final void onItemSelected(final AdapterView<?> parent,
 			final View view, final int pos, final long id) {
 		if (parent.getId() == R.id.rangeSpinner) {
 			this.currentRange = pos;
 			setupInputButtons();
 		}
 	}
 
 	@Override
 	public final void onNothingSelected(final AdapterView<?> parent) {
 		// no-op
 	}
 
 	@Override
 	public final boolean onOptionsItemSelected(final MenuItem item) {
 		final boolean retVal = true;
 		final int id = item.getItemId();
 		if (id == R.id.topScores) {
 			final Intent intent = new Intent(this, TopScores.class);
 			startActivity(intent);
 		} else if (id == R.id.action_settings) {
 			final Intent intent = new Intent(this, SettingsActivity.class);
 			startActivity(intent);
 		} else if (id >= MENU_PUZZLE_OFFSET
 				&& id <= MENU_PUZZLE_OFFSET + PuzzleFactory.numPuzzles()) {
 			final int puzzle = id - MENU_PUZZLE_OFFSET;
 			final String outString = String.format(Locale.US, getResources()
 					.getString(R.string.loading_puzzle_s), puzzle);
 			final Toast toast = Toast.makeText(this, outString,
 					Toast.LENGTH_SHORT);
 			toast.show();
 			loadPuzzle(puzzle, true);
 		}
 		return retVal;
 	}
 
 	@Override
 	public final boolean onPrepareOptionsMenu(final Menu menu) {
 		final SubMenu submenu = menu.findItem(R.id.loadPuzzle).getSubMenu();
 		submenu.clear();
 
 		for (int i = STARTING_PUZZLE; i <= this.maxPuzzleAttempted; i++) {
 			submenu.add(
 					Menu.NONE,
 					MENU_PUZZLE_OFFSET + i,
 					Menu.NONE,
 					String.format(Locale.US,
 							getResources().getString(R.string.puzzle_s), i));
 		}
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public final void onSharedPreferenceChanged(
 			final SharedPreferences sharedPreferences, final String key) {
 		if (key.equals(SettingsActivity.KEY_PREF_NUMBER_COLOR)) {
 			this.prefNumberColor = sharedPreferences.getInt(
 					SettingsActivity.KEY_PREF_NUMBER_COLOR,
 					SettingsActivity.PREF_NUMBER_COLOR_DEFAULT);
 			changeButtonTextColor();
 		} else if (key.equals(SettingsActivity.KEY_PREF_CELL_COLOR)) {
 			this.gameBoard.setBackgroundColor(sharedPreferences.getInt(
 					SettingsActivity.KEY_PREF_CELL_COLOR,
 					SettingsActivity.PREF_CELL_COLOR_DEFAULT));
 		} else if (key.equals(SettingsActivity.KEY_PREF_AUTO_FILLIN)) {
 			this.autoFillIn = sharedPreferences.getBoolean(
 					SettingsActivity.KEY_PREF_AUTO_FILLIN,
 					SettingsActivity.PREF_AUTO_FILLIN_DEFAULT);
 		}
 	}
 
 	@Override
 	public final void onWindowFocusChanged(final boolean hasFocus) {
 		super.onWindowFocusChanged(hasFocus);
 		if (hasFocus && this.timerRunning) {
 			resumeTimer();
 		} else if (hasFocus && !this.timerRunning) {
 			this.timer.setText(formatTime(this.elapsedTime));
 		} else if (!hasFocus && this.timerRunning) {
 			pauseTimer();
 			this.timerRunning = true;
 		}
 	}
 
 	/**
 	 * Input handler for the pause/resume button.
 	 * 
 	 * @param view
 	 *            ignored
 	 */
 	public final void pauseResumeClick(final View view) {
 		if (this.timerRunning) {
 			pauseTimer();
 		} else {
 			resumeTimer();
 		}
 	}
 
 	@Override
 	protected final void onCreate(final Bundle savedState) {
 		super.onCreate(savedState);
 		setContentView(R.layout.activity_connect64);
 		setupViews();
 		setupPreferences();
 	}
 
 	@Override
 	protected final void onPause() {
 		super.onPause();
 		final SharedPreferences statePrefs = getSharedPreferences(
 				GAME_STATE_PREFS, MODE_PRIVATE);
 		final SharedPreferences.Editor stateEditor = statePrefs.edit();
 		stateEditor.putBoolean(TIMER_RUNNING, this.timerRunning);
 		if (this.timerRunning) {
 			setElapsedTime();
 			this.timerRunning = false;
 		}
 		stateEditor.putLong(ELAPSED_TIME, this.elapsedTime);
 		stateEditor.putInt(CURRENT_INPUT, this.currentInput);
 		stateEditor.putInt(CURRENT_PUZZLE, this.currentPuzzle);
 		stateEditor.putInt(CURRENT_RANGE, this.currentRange);
 		stateEditor.putInt(MAX_PUZZLE_ATTEMPTED, this.maxPuzzleAttempted);
 
 		final SparseIntArray state = this.boardState;
 		final int size = state.size();
 		final int[] positions = new int[size];
 		final int[] values = new int[size];
 		for (int i = 0; i < size; i++) {
 			positions[i] = state.keyAt(i);
 			values[i] = state.valueAt(i);
 		}
 		stateEditor.putString(STATE_POSITIONS, serializeIntArray(positions));
 		stateEditor.putString(STATE_VALUES, serializeIntArray(values));
 		stateEditor.apply();
 	}
 
 	@Override
 	protected final void onResume() {
 		super.onResume();
 		final SharedPreferences statePrefs = getSharedPreferences(
 				GAME_STATE_PREFS, MODE_PRIVATE);
 		this.currentInput = statePrefs.getInt(CURRENT_INPUT, BAD_VALUE);
 		this.currentPuzzle = statePrefs.getInt(CURRENT_PUZZLE, STARTING_PUZZLE);
 		this.maxPuzzleAttempted = statePrefs.getInt(MAX_PUZZLE_ATTEMPTED,
 				STARTING_PUZZLE);
 
 		this.timerRunning = statePrefs.getBoolean(TIMER_RUNNING, true);
 		loadPuzzle(this.currentPuzzle, false); // to disable initial buttons
 		this.currentRange = statePrefs.getInt(CURRENT_RANGE, 0);
 		this.rangeSpinner.setSelection(this.currentRange);
 
 		final String posString = statePrefs.getString(STATE_POSITIONS, null);
 		final String valString = statePrefs.getString(STATE_VALUES, null);
 
 		this.elapsedTime = statePrefs.getLong(ELAPSED_TIME, 0L);
 		if (posString != null && valString != null) {
 			final int[] positions = deserializeIntArray(posString);
 			final int[] values = deserializeIntArray(valString);
 			final SparseIntArray state = this.boardState;
 			for (int i = 0; i < positions.length; i++) {
 				state.put(positions[i], values[i]);
 				getGameButton(String.valueOf(positions[i])).setText(
 						String.valueOf(values[i]));
 			}
 		} else {
 			resumeTimer();
 		}
 	}
 
 	private void addScoretoDB() {
 		final ContentValues newScore = new ContentValues(3);
 		newScore.put(Scores.PLAYER, PLAYER_NAME);
 		newScore.put(Scores.PUZZLE, this.currentPuzzle);
 		newScore.put(Scores.COMPLETION_TIME, formatTime(this.elapsedTime));
 		getContentResolver().insert(ScoresContentProviderDB.CONTENT_URI,
 				newScore);
 	}
 
 	private void changeButtonTextColor() {
 		final int color = this.prefNumberColor;
 		for (int i = 1; i <= ROW_SIZE; i++) {
 			for (int j = 1; j <= COL_SIZE; j++) {
 				getGameButton("" + i + j).setTextColor(color);
 			}
 		}
 	}
 
 	/**
 	 * Overall board win condition check. Board must be full and all positions
 	 * must have a valid higher and lower neighbor to fulfill the win condition.
 	 * If the player has won, load the next Puzzle if one exists.
 	 */
 	private void checkWinCondition() {
 		Toast toast = null;
 		final boolean boardCorrect = isBoardCorrect();
 		final boolean onLastPuzzle = this.currentPuzzle == PuzzleFactory
 				.numPuzzles();
 		if (boardCorrect) {
 			setElapsedTime(); // so checkstyle is ok with no "this." here...
 			this.addScoretoDB(); // but it wants one here?
 			performWinFeedback();
 		}
 		if (boardCorrect && onLastPuzzle) {
 			toast = Toast.makeText(this, R.string.all_puzzles_complete,
 					Toast.LENGTH_SHORT);
 			this.timerRunning = false;
 			this.timer.stop();
 		} else if (boardCorrect && !onLastPuzzle) {
 			final String outString = String.format(Locale.US, getResources()
 					.getString(R.string.you_won_in_s),
 					formatTime(this.elapsedTime));
 			toast = Toast.makeText(this, outString, Toast.LENGTH_SHORT);
 			final int nextPuzzle = this.currentPuzzle + 1;
 			loadPuzzle(nextPuzzle, true);
 		} else if (!boardCorrect) {
 			toast = Toast
 					.makeText(this, R.string.try_again, Toast.LENGTH_SHORT);
 			performLoseFeedback();
 		} else {
 			return;
 		}
 		toast.show();
 	}
 
 	private int[] deserializeIntArray(final String string) {
 		try {
 			final byte[] bytes = Base64.decode(string, Base64.DEFAULT);
 			final ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
 			final ObjectInputStream in = new ObjectInputStream(bin);
 			return (int[]) in.readObject();
 		} catch (final Exception ex) {
 			return null;
 		}
 	}
 
 	private String formatTime(final long millis) {
 		// if there's a built-in version of this, I'd much rather use it.
 		final int millisToSec = 1000;
 		final int secToMin = 60;
 		final long elapsed = millis / millisToSec;
 		return String.format(Locale.US, "%02d:%02d", elapsed / secToMin,
 				elapsed % secToMin);
 	}
 
 	private Button getGameButton(final String tag) {
 		final Button button = (Button) this.gameBoard.findViewWithTag(tag);
 		if (button == null) {
 			Log.e(LOG_TAG, "invalid tag passed: " + tag);
 		}
 		return button;
 	}
 
 	/**
 	 * Given a position on the board, calculate all valid values that can go in
 	 * that position.
 	 * 
 	 * @param position
 	 *            the position to check against
 	 * @return an array with valid unique values. May be empty.
 	 */
 	private int[] getValidOptions(final int position) {
 		final int maxOptions = 8;
 		final int[] values = { getValue(position - COLUMN_DELTA),
 				getValue(position + COLUMN_DELTA),
 				getValue(position - ROW_DELTA), getValue(position + ROW_DELTA) };
 		int[] out = new int[maxOptions];
 		int count = 0;
 
 		for (final int value : values) {
 			if (value > 1 && !isOnBoard(value - 1)
 					&& !isInArray(value - 1, out)) {
 				out[count++] = value - 1;
 			}
 			if (value > 0 && value < BOARD_MAX && !isOnBoard(value + 1)
 					&& !isInArray(value + 1, out)) {
 				out[count++] = value + 1;
 			}
 		}
 		// trim the array before sorting it so we don't sort the 0s
 		out = Arrays.copyOf(out, count);
 		Arrays.sort(out);
 		return out;
 	}
 
 	private int getValue(final int position) {
 		return this.boardState.get(position, BAD_VALUE);
 	}
 
 	/**
 	 * Win-condition checking at the position level. Looks for a neighbor whose
 	 * value is 1 higher than this position's value, and another neighbor whose
 	 * value is 1 lower.
 	 * 
 	 * @param pos
 	 *            the position to check against
 	 * @return true if one neighbor has value+1 AND another neighbor has value-1
 	 */
 	private boolean hasValidNeighbors(final int pos) {
 		final int value = this.getValue(pos);
 		if (value == BAD_VALUE) {
 			return false;
 		}
 
 		final int left = this.getValue(pos - COLUMN_DELTA);
 		final int right = this.getValue(pos + COLUMN_DELTA);
 		final int up = this.getValue(pos - ROW_DELTA);
 		final int down = this.getValue(pos + ROW_DELTA);
 
 		final boolean hasNext = (value == BOARD_MAX) || isNext(value, left)
 				|| isNext(value, right) || isNext(value, up)
 				|| isNext(value, down);
 		final boolean hasPrev = (value == 1) || isPrev(value, left)
 				|| isPrev(value, right) || isPrev(value, up)
 				|| isPrev(value, down);
 
 		return hasNext && hasPrev;
 	}
 
 	/**
 	 * Win-condition checking at the board level. Iterates through each position
 	 * to check that it has valid neighbors.
 	 * 
 	 * @return true if no position has all invalid neighbors.
 	 */
 	private boolean isBoardCorrect() {
 		for (int i = 1; i <= COL_SIZE; i++) {
 			for (int j = 1; j <= ROW_SIZE; j++) {
 				if (!this.hasValidNeighbors(i * ROW_DELTA + j)) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	private boolean isInArray(final int needle, final int[] haystack) {
 		for (final int i : haystack) {
 			if (needle == i) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean isNext(final int thisValue, final int otherValue) {
 		return thisValue == otherValue - 1;
 	}
 
 	private boolean isOnBoard(final int value) {
 		return this.boardState.indexOfValue(value) >= 0;
 	}
 
 	private boolean isPrev(final int thisValue, final int otherValue) {
 		return thisValue == otherValue + 1;
 	}
 
 	/**
 	 * Loads the specified Puzzle. Will reset the timer if restartTimer is true
 	 * 
 	 * @param newPuzzle
 	 *            the puzzle to load
 	 * @param restartTimer
 	 *            whether or not to restart the timer
 	 */
 	private void loadPuzzle(final int newPuzzle, final boolean restartTimer) {
 		this.currentPuzzle = newPuzzle;
 		this.puzzleLabel.setText(String.format(Locale.US, getResources()
 				.getString(R.string.puzzle_s), newPuzzle));
 		if (newPuzzle > this.maxPuzzleAttempted) {
 			this.maxPuzzleAttempted = newPuzzle;
 			invalidateOptionsMenu(); // to add the new puzzle to the menu
 		}
 		resetAndInitializeBoard();
 		if (restartTimer) {
 			this.elapsedTime = 0;
 			resumeTimer();
 		}
 		if (!this.timerRunning) {
 			pauseTimer();
 		}
 	}
 
 	/**
 	 * Stores the elapsed time if the timer is running, hides the game board,
 	 * and disables all the input buttons.
 	 */
 	private void pauseTimer() {
 		if (this.timerRunning) {
 			setElapsedTime();
 		}
 		if (!this.isBoardCorrect()) {
 			// hide the board only if we're not done with it
 			this.gameBoard.setVisibility(View.INVISIBLE);
 		}
 		this.timerRunning = false;
 		this.timer.stop();
 		this.pauseResume.setImageResource(android.R.drawable.ic_media_play);
 		setupInputButtons();
 	}
 
 	/** If feedback is on, play the chosen "lose" feedback, aural or haptic. */
 	private void performLoseFeedback() {
 		final int prefFeedback = Integer.parseInt(this.preferences.getString(
 				SettingsActivity.KEY_PREF_FEEDBACK, "0"));
 		switch (prefFeedback) {
 		case SettingsActivity.FEEDBACK_AURAL:
 			Log.d(LOG_TAG, "music play goes here");
 			/*// @formatter:off
 			final MediaPlayer mp = MediaPlayer.create(this, R.raw.lose);
 			mp.start();
 			*/// @formatter:on
 			break;
 		case SettingsActivity.FEEDBACK_HAPTIC:
 			Log.d(LOG_TAG, "BZZZTT!!1!");
			final int longLength = 500;
 			final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 			vibe.vibrate(longLength);
 			break;
 		default:
 		}
 	}
 
 	/** If feedback is on, play the chosen "win" feedback, aural or haptic. */
 	private void performWinFeedback() {
 		final int prefFeedback = Integer.parseInt(this.preferences.getString(
 				SettingsActivity.KEY_PREF_FEEDBACK, "0"));
 		switch (prefFeedback) {
 		case SettingsActivity.FEEDBACK_AURAL:
 			Log.d(LOG_TAG, "music play goes here");
 			/*// @formatter:off
 			final MediaPlayer mp = MediaPlayer.create(this, R.raw.win);
 			mp.start();
 			*/// @formatter:on
 			break;
 		case SettingsActivity.FEEDBACK_HAPTIC:
 			Log.d(LOG_TAG, "BZZZTT!!1!");
			final int shortLength = 250;
 			final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 			vibe.vibrate(shortLength);
 			break;
 		default:
 		}
 	}
 
 	/**
 	 * Clears/reenables all board buttons, loads in the current puzzle to the
 	 * board, and disables the buttons at the starting positions.
 	 */
 	private void resetAndInitializeBoard() {
 		for (int i = 1; i <= COL_SIZE; i++) {
 			for (int j = 1; j <= ROW_SIZE; j++) {
 				final Button button = this.getGameButton("" + i + j);
 				button.setText("");
 				button.setEnabled(true);
 			}
 		}
 		this.boardState.clear();
 		this.rangeSpinner.setSelection(0);
 		this.currentInput = BAD_VALUE;
 
 		final Puzzle puzzle = PuzzleFactory.getPuzzle(this.currentPuzzle);
 		final int[] pos = puzzle.getPositions();
 		final int[] vals = puzzle.getValues();
 
 		for (int i = 0; i < pos.length; i++) {
 			final Button button = this.getGameButton(String.valueOf(pos[i]));
 			button.setText(String.valueOf(vals[i]));
 			button.setEnabled(false);
 			this.boardState.put(pos[i], vals[i]);
 		}
 
 		setupInputButtons();
 	}
 
 	/** Rebases the timer, starts it, updates the input buttons. */
 	private void resumeTimer() {
 		this.timer.setBase(SystemClock.elapsedRealtime() - this.elapsedTime);
 		this.timer.start();
 		this.timerRunning = true;
 		this.pauseResume.setImageResource(android.R.drawable.ic_media_pause);
 		this.gameBoard.setVisibility(View.VISIBLE);
 		setupInputButtons();
 	}
 
 	private String serializeIntArray(final int[] array) {
 		try {
 			final ByteArrayOutputStream bo = new ByteArrayOutputStream();
 			final ObjectOutputStream out = new ObjectOutputStream(bo);
 			out.writeObject(array);
 			out.flush();
 			return Base64.encodeToString(bo.toByteArray(), Base64.DEFAULT);
 		} catch (final Exception ex) {
 			return null;
 		}
 	}
 
 	private void setElapsedTime() {
 		this.elapsedTime = SystemClock.elapsedRealtime() - this.timer.getBase();
 	}
 
 	/**
 	 * Sets the text of the specified Button to the value of the current input
 	 * if valid, clearing the Button otherwise. If a value already exists at the
 	 * specified Button, sets the current input to that value. Also updates the
 	 * internal state as appropriate.
 	 * 
 	 * @param button
 	 *            the Button to set the text for.
 	 */
 	private void setGameButtonText(final Button button) {
 		final int position = Integer.parseInt(button.getTag().toString());
 		final int oldValue = this.getValue(position);
 
 		if (this.autoFillIn && this.currentInput == BAD_VALUE
 				&& oldValue == BAD_VALUE) {
 			final int[] options = this.getValidOptions(position);
 			// how else do I do this? don't want to call gVO unnecessarily.
 			if (options.length == 1) {
 				this.currentInput = options[0];
 			}
 		}
 
 		if (this.currentInput == BAD_VALUE) {
 			button.setText("");
 			this.boardState.delete(position);
 		} else {
 			button.setText(String.valueOf(this.currentInput));
 			this.boardState.put(position, this.currentInput);
 		}
 		this.currentInput = oldValue;
 		setupInputButtons();
 	}
 
 	private void setupContextClicks() {
 		for (int i = 1; i <= COL_SIZE; i++) {
 			for (int j = 1; j <= ROW_SIZE; j++) {
 				registerForContextMenu(this.getGameButton("" + i + j));
 			}
 		}
 	}
 
 	/**
 	 * Sets the text on the input Buttons based on the current range. Buttons
 	 * whose freshly-set text appears on the game board will be disabled. If the
 	 * timer is paused, disables all Buttons.
 	 */
 	private void setupInputButtons() {
 		final int range = this.currentRange;
 		final Button[] inputs = this.inputButtons;
 		final boolean timerActive = this.timerRunning;
 
 		for (int i = 0; i < NUM_INPUT_BUTTONS; i++) {
 			final int value = NUM_INPUT_BUTTONS * range + i + 1;
 			final Button inputButton = inputs[i];
 			inputButton.setText(String.valueOf(value));
 			inputButton.setEnabled(timerActive && !this.isOnBoard(value));
 		}
 	}
 
	/** Loads prefs and sets the grid bg/fg colors as appropriate. */
 	private void setupPreferences() {
 		this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		this.preferences.registerOnSharedPreferenceChangeListener(this);
 		this.gameBoard.setBackgroundColor(this.preferences.getInt(
 				SettingsActivity.KEY_PREF_CELL_COLOR,
 				SettingsActivity.PREF_CELL_COLOR_DEFAULT));
 		this.prefNumberColor = this.preferences.getInt(
 				SettingsActivity.KEY_PREF_NUMBER_COLOR,
 				SettingsActivity.PREF_NUMBER_COLOR_DEFAULT);
 		this.changeButtonTextColor();
 		this.autoFillIn = this.preferences.getBoolean(
 				SettingsActivity.KEY_PREF_AUTO_FILLIN,
 				SettingsActivity.PREF_AUTO_FILLIN_DEFAULT);
 	}
 
	/** Gets references to views, hooks up the conext listeners. */
 	private void setupViews() {
 		this.boardState = new SparseIntArray(BOARD_MAX);
		this.gameBoard = (LinearLayout) findViewById(R.id.connect64);
 		this.setupContextClicks();
 		this.puzzleLabel = (TextView) findViewById(R.id.puzzleLabel);
 		this.rangeSpinner = (Spinner) findViewById(R.id.rangeSpinner);
 		this.pauseResume = (ImageButton) findViewById(R.id.pauseResumeButton);
 		this.timer = (Chronometer) findViewById(R.id.chronometer);
 		final ArrayAdapter<CharSequence> adapter = ArrayAdapter
 				.createFromResource(this, R.array.ranges,
 						android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		this.rangeSpinner.setAdapter(adapter);
 		this.rangeSpinner.setOnItemSelectedListener(this);
 
		final RelativeLayout inputs = (RelativeLayout) findViewById(R.id.inputButtons);
 		final Button[] buttons = new Button[NUM_INPUT_BUTTONS];
 		for (int i = 0; i < NUM_INPUT_BUTTONS; i++) {
 			buttons[i] = (Button) inputs.findViewWithTag("in" + (i + 1));
 		}
 		this.inputButtons = buttons;
 	}
 }

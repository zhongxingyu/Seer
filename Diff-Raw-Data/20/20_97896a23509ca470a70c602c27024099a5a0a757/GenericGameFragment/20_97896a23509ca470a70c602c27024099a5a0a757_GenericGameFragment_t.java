 package com.charlesmadere.android.classygames;
 
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewTreeObserver;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.charlesmadere.android.classygames.games.Coordinate;
 import com.charlesmadere.android.classygames.games.GenericBoard;
 import com.charlesmadere.android.classygames.games.Position;
 import com.charlesmadere.android.classygames.models.Game;
 import com.charlesmadere.android.classygames.models.Person;
 import com.charlesmadere.android.classygames.server.ServerApi;
 import com.charlesmadere.android.classygames.server.ServerApiForfeitGame;
 import com.charlesmadere.android.classygames.server.ServerApiSendMove;
 import com.charlesmadere.android.classygames.server.ServerApiSkipMove;
 import com.charlesmadere.android.classygames.utilities.ServerUtilities;
 import com.charlesmadere.android.classygames.utilities.Utilities;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 
 public abstract class GenericGameFragment extends SherlockFragment
 {
 
 
 	private final static String LOG_TAG = Utilities.LOG_TAG + " - GenericGameFragment";
 
 
 	private final static String BUNDLE_BOARD_JSON = "BUNDLE_BOARD_JSON";
 
 
 	public final static String KEY_GAME_ID = "KEY_GAME_ID";
 	public final static String KEY_WHICH_GAME = "BUNDLE_WHICH_GAME";
 	public final static String KEY_PERSON_ID = "KEY_PERSON_ID";
 	public final static String KEY_PERSON_NAME = "KEY_PERSON_NAME";
 
 
 
 
 	/**
 	 * This game's Game object. This contains a whole bunch of necessary data
 	 * such as the ID of the game as well as the Person object that the current
 	 * Android user is playing against. <strong>DO NOT MODIFY THIS OBJECT ONCE
 	 * IT HAS BEEN SET.</strong>
 	 */
 	protected Game game;
 
 
 	/**
 	 * JSONObject downloaded from the server that represents the board.
 	 */
 	private JSONObject boardJSON;
 
 
 	/**
 	 * The actual logical representation of the board.
 	 */
 	protected GenericBoard board;
 
 
 	/**
 	 * Holds a handle to the currently running (if it's currently running)
 	 * AsyncGetGame AsyncTask. This could be null.
 	 */
 	private AsyncGetGame asyncGetGame;
 
 
 	/**
 	 * Holds a handle to a currently running (if it's currently running)
 	 * ServerApi object.
 	 */
 	private ServerApi serverApiTask;
 
 
 	/**
 	 * Callback interface for the ServerApi class.
 	 */
 	private ServerApi.ServerApiListeners serverApiListeners;
 
 
 	/**
 	 * The position on the game board that the user just now selected.
 	 */
 	private ImageButton positionSelectedCurrent;
 
 
 	/**
 	 * The position on the game board that the user selected last time.
 	 */
 	private ImageButton positionSelectedPrevious;
 
 
 	/**
 	 * A bright position's default background.
 	 */
 	private BitmapDrawable backgroundBoardBright;
 
 
 	/**
 	 * When a position is selected that has a bright background then this
 	 * BitmapDrawable should be applied as its background.
 	 */
 	private BitmapDrawable backgroundBoardBrightSelected;
 
 
 	/**
 	 * A dark position's defeault background.
 	 */
 	private BitmapDrawable backgroundBoardDark;
 
 
 	/**
 	 * When a position is selected that has a dark background then this
 	 * BitmapDrawable should be applied as its background.
 	 */
 	private BitmapDrawable backgroundBoardDarkSelected;
 
 
 	/**
 	 * Checks to see which position on the board was clicked and then moves
 	 * pieces and / or performs actions accordingly.
 	 */
 	private View.OnClickListener onBoardClick;
 
 
 
 
 	/**
 	 * Object that allows us to run any of the methods that are defined in the
 	 * GenericGameFragmentListeners interface.
 	 */
 	private GenericGameFragmentListeners listeners;
 
 
 	/**
 	 * A bunch of listener methods for this Fragment.
 	 */
 	public interface GenericGameFragmentListeners
 	{
 
 
 		/**
 		 * This is fired during this Fragment's onCreateOptionsMenu() method.
 		 * Checks to see if the current device is considered by Android to be
 		 * small. This be basically every phone.
 		 * 
 		 * @return
 		 * Returns true if the current device is small.
 		 */
 		public boolean isDeviceSmall();
 
 
 		/**
 		 * This is fired in the event that an error was detected with some of
 		 * the data needed to instantiate a game.
 		 */
 		public void onDataError();
 
 
 		/**
 		 * This is fired if the AsyncGetGame AsyncTask gets cancelled.
 		 */
 		public void onGetGameCancelled();
 
 
 		/**
 		 * This is fired when a move (or new game) is finished being sent to
 		 * the server.
 		 */
 		public void onServerApiTaskFinished();
 
 
 	}
 
 
 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 	}
 
 
 	@Override
 	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
 	{
 		onCreateView();
 		return inflater.inflate(getGameView(), container, false);
 	}
 
 
 	@Override
 	public void onActivityCreated(final Bundle savedInstanceState)
 	{
 		super.onActivityCreated(savedInstanceState);
 
 		final Bundle arguments = getArguments();
 
 		if (arguments == null || arguments.isEmpty())
 		// Check the arguments given to this Fragment. This Fragment requires
 		// information regarding the game that it is going to load. If no data
 		// is found, then this Fragment has encountered an error and must
 		// terminate itself.
 		{
 			listeners.onDataError();
 		}
 		else
 		{
 			// Grabs data that was given to this Fragment. This data will then
 			// be checked for validity.
 			final String gameId = arguments.getString(KEY_GAME_ID);
 			final byte whichGame = arguments.getByte(KEY_WHICH_GAME);
 			final long personId = arguments.getLong(KEY_PERSON_ID);
 			final String personName = arguments.getString(KEY_PERSON_NAME);
 
 			if (Game.isWhichGameValid(whichGame) && Person.isIdAndNameValid(personId, personName))
 			// Check the data for validity. Note that we are not checking the
 			// gameId String for validity. This is because it is possible for a
 			// game to not have an ID. Brand new games do not have a game ID.
 			{
 				serverApiListeners = new ServerApi.ServerApiListeners()
 				{
 					@Override
 					public void onCancel()
 					{
 						serverApiTask = null;
 					}
 
 
 					@Override
 					public void onComplete()
 					{
 						serverApiTask = null;
 						listeners.onServerApiTaskFinished();
 					}
 
 
 					@Override
 					public void onDismiss()
 					{
 						serverApiTask = null;
 					}
 				};
 
 				onBoardClick = new View.OnClickListener()
 				{
 					@Override
 					public void onClick(final View v)
 					{
 						if (positionSelectedCurrent == null)
 						{
 							positionSelectedCurrent = (ImageButton) v;
 							onBoardClick(positionSelectedCurrent);
 						}
 						else
 						{
 							positionSelectedPrevious = positionSelectedCurrent;
 							positionSelectedCurrent = (ImageButton) v;
 
 							if (positionSelectedPrevious == positionSelectedCurrent)
 							// The player has clicked the same position on
 							// the board twice in a row. This is the
 							// deselect action.
 							{
 								clearSelectedPositions();
 							}
 							else
 							{
 								onBoardClick(positionSelectedPrevious, positionSelectedCurrent);
 							}
 						}
 					}
 				};
 
 				final Person person = new Person(personId, personName);
 				loadBackgroundResources();
 
 				if (Game.isIdValid(gameId))
 				// Check to see if we were given a valid game ID. We will only
 				// have a valid game ID if we are trying to recreate an already
 				// existing game. A brand new game will not have a game ID.
 				{
 					game = new Game(person, whichGame, gameId);
 
 					if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_BOARD_JSON))
 					{
 						try
 						{
 							boardJSON = new JSONObject(savedInstanceState.getString(BUNDLE_BOARD_JSON));
 
 							initViewsAndLoadPieces();
 							resumeOldBoard();
 							flush();
 						}
 						catch (final JSONException e)
 						{
 							getGame();
 						}
 					}
 					else
 					{
 						getGame();
 					}
 				}
 				else
 				// We were not given a valid game ID. That means that we are
 				// going to instantiate a brand new game.
 				{
 					game = new Game(person);
 
 					try
 					{
 						initNewBoard();
 						initViewsAndLoadPieces();
 						flush();
 					}
 					catch (final JSONException e)
 					{
 						listeners.onDataError();
 					}
 				}
 			}
 			else
 			{
 				listeners.onDataError();
 			}
 		}
 	}
 
 
 	@Override
 	public void onAttach(final Activity activity)
 	// This makes sure that the Activity containing this Fragment has
 	// implemented the callback interface. If the callback interface has not
 	// been implemented, an exception is thrown.
 	{
 		super.onAttach(activity);
 
 		try
 		{
 			listeners = (GenericGameFragmentListeners) activity;
 		}
 		catch (final ClassCastException e)
 		{
 			throw new ClassCastException(activity.toString() + " must implement listeners!");
 		}
 	}
 
 
 	@Override
 	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
 	{
 		menu.removeItem(R.id.generic_refresh_menu_refresh);
 
 		if (listeners.isDeviceSmall())
 		{
 			menu.removeItem(R.id.game_fragment_activity_menu_new_game);
 		}
 
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && asyncGetGame != null)
 		{
 			inflater.inflate(R.menu.generic_cancel, menu);
 		}
 		else
 		{
 			inflater.inflate(R.menu.generic_game_fragment, menu);
 		}
 
 		final Bundle arguments = getArguments();
 
 		if (arguments != null && !Utilities.verifyValidString(arguments.getString(KEY_GAME_ID)))
 		{
 			menu.removeItem(R.id.generic_game_fragment_menu_skip_move);
 			menu.removeItem(R.id.generic_game_fragment_menu_forfeit_game);
 		}
 
 		// load any menu items as added by the classes that extend this one
 		createOptionsMenu(menu, inflater);
 
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 
 	@Override
	public void onDestroyView()
	{
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
		{
//			destroyBitmapDrawables();
		}

		super.onDestroyView();
	}


	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case R.id.generic_cancel_menu_cancel:
 				if (asyncGetGame != null)
 				{
 					asyncGetGame.cancel(true);
 				}
 				break;
 
 			case R.id.generic_game_fragment_menu_forfeit_game:
 				forfeitGame();
 				break;
 
 			case R.id.generic_game_fragment_menu_send_move:
 				sendMove();
 				break;
 
 			case R.id.generic_game_fragment_menu_skip_move:
 				skipMove();
 				break;
 
 			case R.id.generic_game_fragment_menu_undo_move:
 				undoMove();
 				break;
 
 			default:
 			// If we hit this point then that means that the options item that
 			// was selected is not any of the above choices. That means that it
 			// was probably a menu item created by one of the classes that
 			// extend this one. So this will check that class's menu items.
 				return optionsItemSelected(item);
 		}
 
 		return true;
 	}
 
 
 	@Override
 	public void onPrepareOptionsMenu(final Menu menu)
 	{
 		if (asyncGetGame == null)
 		{
 			MenuItem menuItem = menu.findItem(R.id.generic_game_fragment_menu_send_move);
 			if (menuItem != null)
 			{
 				if (board == null)
 				{
 					menuItem.setEnabled(false);
 				}
 				else
 				{
 					menuItem.setEnabled(board.hasMoveBeenMade());
 				}
 			}
 
 			menuItem = menu.findItem(R.id.generic_game_fragment_menu_undo_move);
 			if (menuItem != null)
 			{
 				if (board == null)
 				{
 					menuItem.setEnabled(false);
 				}
 				else
 				{
 					menuItem.setEnabled(board.hasMoveBeenMade());
 				}
 			}
 		}
 
 		// Allow the classes that extend this one to run menu preparation
 		// operations.
 		prepareOptionsMenu(menu);
 	}
 
 
 	@Override
 	public void onSaveInstanceState(final Bundle outState)
 	{
 		if (Game.isIdValid(game.getId()) && boardJSON != null)
 		{
 			final String boardJSONString = boardJSON.toString();
 
 			if (Utilities.verifyValidString(boardJSONString))
 			{
 				outState.putString(BUNDLE_BOARD_JSON, boardJSONString);
 			}
 		}
 
 		super.onSaveInstanceState(outState);
 	}
 
 
 
 
 	/**
 	 * Attempts to cancel the currently running AsyncGetGame AsyncTask.
 	 * 
 	 * @return
 	 * Returns true if the AsyncTask was cancelled.
 	 */
 	private boolean cancelRunningAsyncGetGame()
 	{
 		boolean cancelled = false;
 
 		if (asyncGetGame != null)
 		{
 			asyncGetGame.cancel(true);
 			cancelled = true;
 		}
 
 		return cancelled;
 	}
 
 
 	/**
 	 * Attempts to cancel the currently running ServerApiTask.
 	 * 
 	 * @return
 	 * Returns true if the AsyncTask was cancelled.
 	 */
 	private boolean cancelRunningServerApiTask()
 	{
 		boolean cancelled = false;
 
 		if (serverApiTask != null)
 		{
 			serverApiTask.cancel();
 			cancelled = true;
 		}
 
 		return cancelled;
 	}
 
 
 	/**
 	 * Cancels the currently running AsyncTask (if any).
 	 */
 	public void cancelRunningAnyAsyncTask()
 	{
 		if (!cancelRunningAsyncGetGame())
 		{
 			cancelRunningServerApiTask();
 		}
 	}
 
 
 	/**
 	 * Clears both selected positions variables. (They are both set to null.)
 	 * The background image on both of them is also reset.
 	 */
 	protected void clearSelectedPositions()
 	{
 		if (positionSelectedPrevious != null)
 		{
 			final String tag = (String) positionSelectedPrevious.getTag();
 			final Coordinate previous = new Coordinate(tag);
 			setPositionBackground(positionSelectedPrevious, false, previous);
 			positionSelectedPrevious = null;
 		}
 
 		if (positionSelectedCurrent != null)
 		{
 			final String tag = (String) positionSelectedCurrent.getTag();
 			final Coordinate current = new Coordinate(tag);
 			setPositionBackground(positionSelectedCurrent, false, current);
 			positionSelectedCurrent = null;
 		}
 	}
 
 
 	/**
 	 * Creates a tag to be used in a findViewWithTag() operation.
 	 * 
 	 * <p><strong>Examples</strong><br />
 	 * createTag(3, 5) <strong>returns</strong> "x3y5"<br />
 	 * createTag(0, 0) <strong>returns</strong> "x0y0"<br /></p>
 	 * 
 	 * @param x
 	 * The <strong>X</strong> coordinate to create the tag from.
 	 * 
 	 * @param y
 	 * The <strong>Y</strong> coordinate to create the tag from.
 	 * 
 	 * @return
 	 * Returns a tag made from the input arguments.
 	 */
 	protected String createTag(final byte x, final byte y)
 	{
 		return "x" + x + "y" + y;
 	}
 
 
 	/**
 	 * Creates a tag to be used in a findViewWithTag() operation.
 	 * 
 	 * <p><strong>Examples</strong><br />
 	 * Coordinate c1 = new Coordinate(3, 5);<br />
 	 * createTag(c1) <strong>returns</strong> "x3y5"<br />
 	 * Coordinate c2 = new Coordinate(0, 0);<br />
 	 * createTag(c2) <strong>returns</strong> "x0y0"<br /></p>
 	 * 
 	 * @param coordinate
 	 * The Coordinate object to create the tag from.
 	 * 
 	 * @return
 	 * Returns a tag made from the input Coordinate.
 	 */
 	protected String createTag(final Coordinate coordinate)
 	{
 		return createTag(coordinate.getX(), coordinate.getY());
 	}
 
 
 	/**
 	 * Recycles all of the BitmapDrawable objects passed in to this method.
 	 * After all of those objects have been recycled, this method will then
 	 * recycle all of the board's BitmapDrawable background objects. <strong>
 	 * This method should only be run on versions of Android below Honeycomb!
 	 * So that means Gingerbread and below.</strong>
 	 *
 	 * @param drawables
 	 * All of the BitmapDrawable objects that you want recycled. This should be
 	 * every single BitmapDrawable that is used in the classes that extend this
 	 * one.
 	 */
 	protected void destroyBitmapDrawables(final BitmapDrawable... drawables)
 	{
 		for (final BitmapDrawable drawable : drawables)
 		{
 			drawable.getBitmap().recycle();
 		}
 
 		backgroundBoardBright.getBitmap().recycle();
 		backgroundBoardBrightSelected.getBitmap().recycle();
 		backgroundBoardDark.getBitmap().recycle();
 		backgroundBoardDarkSelected.getBitmap().recycle();
 	}
 
 
 	/**
 	 * Renders all of the game's pieces on the board by first clearing all of
 	 * the existing pieces from it and then placing all of the current pieces.
 	 */
 	protected void flush()
 	{
 		// clear all of the existing pieces from the board
 		for (byte x = 0; x < board.getLengthHorizontal(); ++x)
 		{
 			for (byte y = 0; y < board.getLengthVertical(); ++y)
 			{
 				final String tag = createTag(x, y);
 
 				// setting the ImageDrawable to null erases the current image
 				// (if there is any) from this ImageDrawable
 				((ImageButton) getView().findViewWithTag(tag)).setImageDrawable(null);
 			}
 		}
 
 		// place all of the pieces back onto the board
 		for (byte x = 0; x < board.getLengthHorizontal(); ++x)
 		{
 			for (byte y = 0; y < board.getLengthVertical(); ++y)
 			{
 				final Position position = board.getPosition(x, y);
 
 				if (position.hasPiece())
 				{
 					// let each GameFragment class that extends from this class
 					// handle it from here
 					flush(position);
 				}
 			}
 		}
 	}
 
 
 	/**
 	 * Forfeits this game by asking the user if they're sure that they want to
 	 * through a dialog box.
 	 */
 	private void forfeitGame()
 	{
 		if (Utilities.verifyValidString(game.getId()) && !isAnAsyncTaskRunning())
 		{
 			serverApiTask = new ServerApiForfeitGame(getSherlockActivity(), serverApiListeners, game);
 			serverApiTask.execute();
 		}
 	}
 
 
 	/**
 	 * If the AsyncGetGame AsyncTask is not already running, then this will
 	 * execute it.
 	 */
 	private void getGame()
 	{
 		if (asyncGetGame == null)
 		{
 			asyncGetGame = new AsyncGetGame(getSherlockActivity(), getLayoutInflater(getArguments()), (ViewGroup) getView());
 			asyncGetGame.execute();
 		}
 	}
 
 
 	/**
 	 * Executes the initViews() method and then the loadPieces() method. That's
 	 * all.
 	 */
 	private void initViewsAndLoadPieces()
 	{
 		initViews();
 		loadPieces();
 	}
 
 
 	/**
 	 * @return
 	 * Returns true if either the AsyncGetGame AsyncTask is running or if the
 	 * AsyncSendMove AsyncTask is running.
 	 */
 	public boolean isAnAsyncTaskRunning()
 	{
 		return asyncGetGame != null || serverApiTask != null;
 	}
 
 
 	/**
 	 * Loads BitmapDrawables that are needed for applying to ImageButtons as
 	 * their background image.
 	 */
 	private void loadBackgroundResources()
 	{
 		final Resources resources = getResources();
 
 		backgroundBoardBright = (BitmapDrawable) resources.getDrawable(R.drawable.bg_board_bright);
 		backgroundBoardBrightSelected = (BitmapDrawable) resources.getDrawable(R.drawable.bg_board_bright_selected);
 		backgroundBoardDark = (BitmapDrawable) resources.getDrawable(R.drawable.bg_board_dark);
 		backgroundBoardDarkSelected = (BitmapDrawable) resources.getDrawable(R.drawable.bg_board_dark_selected);
 	}
 
 
 	/**
 	 * Reads in a JSON response String as received from the web server and
 	 * pulls the needed information out of it. If there is an error during this
 	 * process, null is returned.
 	 * 
 	 * @param serverResponse
 	 * The JSON response String as received from the web server. This method
 	 * <strong>does</strong> check to see if this passed in String is either
 	 * null or empty. In that case, the method will immediately log that error
 	 * and then return null.
 	 * 
 	 * @return
 	 * Returns a JSONObject containing only the necessary game information.
 	 * But, if there is an error in the parsing process, this method will log
 	 * some stuff and then return null.
 	 */
 	private JSONObject parseServerResponse(final String serverResponse)
 	{
 		JSONObject parsedServerResponse = null;
 
 		if (Utilities.verifyValidString(serverResponse))
 		{
 			try
 			{
 				final JSONObject jsonData = new JSONObject(serverResponse);
 				final JSONObject jsonResult = jsonData.getJSONObject(ServerUtilities.POST_DATA_RESULT);
 
 				try
 				{
 					final String successMessage = jsonResult.getString(ServerUtilities.POST_DATA_SUCCESS);
 					Log.i(LOG_TAG, "Server returned success message: " + successMessage);
 
 					parsedServerResponse = new JSONObject(successMessage);
 				}
 				catch (final JSONException e)
 				{
 					try
 					{
 						final String errorMessage = jsonResult.getString(ServerUtilities.POST_DATA_ERROR);
 						Log.e(LOG_TAG, "Server returned error message: " + errorMessage);
 					}
 					catch (final JSONException e1)
 					{
 						Log.e(LOG_TAG, "Server response is nothing much.");
 					}
 				}
 			}
 			catch (final JSONException e)
 			{
 				Log.e(LOG_TAG, "Server response is massively malformed.");
 			}
 		}
 		else
 		{
 			Log.e(LOG_TAG, "Either null or empty String received from server on send move!");
 		}
 
 		return parsedServerResponse;
 	}
 
 
 	/**
 	 * Puts the board in a state so that the move is now ready to be sent.
 	 * 
 	 * @param force
 	 * If you want to force a pre honeycomb device to refresh it's menu then
 	 * set this to true.
 	 */
 	protected void readyToSendMove(final boolean force)
 	{
 		Utilities.compatInvalidateOptionsMenu(getSherlockActivity(), force);
 	}
 
 
 	/**
 	 * Puts the board in a state so that the move is now ready to be sent.
 	 */
 	protected void readyToSendMove()
 	{
 		readyToSendMove(false);
 	}
 
 
 	/**
 	 * This method will initialize the game board as if this is an in progress
 	 * game. This <strong>resumes</strong> an old game. Do not use this for a
 	 * brand new game.
 	 */
 	private void resumeOldBoard()
 	{
 		if (boardJSON == null)
 		{
 			Log.e(LOG_TAG, "Tried to build the board from a null JSONObject!");
 			listeners.onDataError();
 		}
 		else
 		{
 			try
 			{
 				resumeOldBoard(boardJSON);
 			}
 			catch (final JSONException e)
 			{
 				Log.e(LOG_TAG, "resumeOldBoard(): boardJSON is massively malformed.", e);
 				listeners.onDataError();
 			}
 		}
 	}
 
 
 	/**
 	 * If the AsyncSendMove AsyncTask is not already running, then this will
 	 * execute it.
 	 */
 	private void sendMove()
 	{
 		if (!isAnAsyncTaskRunning())
 		{
 			final SharedPreferences sPreferences = Utilities.getPreferences(getSherlockActivity());
 			final boolean askUserToExecute = sPreferences.getBoolean(getString(R.string.settings_key_ask_before_sending_move), true);
 
 			serverApiTask = new ServerApiSendMove(getSherlockActivity(), serverApiListeners, game, board);
 			serverApiTask.execute(askUserToExecute);
 		}
 	}
 
 
 	/**
 	 * Applies the onBoardClick OnClickListener to all of the given View
 	 * objects.
 	 *
 	 * @param views
 	 * The set of View objects to apply the OnClickListener to.
 	 */
 	protected void setBoardOnClickListeners(final View... views)
 	{
 		for (final View view : views)
 		{
 			view.setOnClickListener(onBoardClick);
 		}
 	}
 
 
 	/**
 	 * Sets all of the positions on the game board to equal height and width.
 	 * This is needed because by default Android will not size the game board's
 	 * height and width values to the same thing, and thus the game board will
 	 * look like a misshapen rectangle crazy thing. So yeah, this method fixes
 	 * that situation and makes the board pretty instead of ugly. <b>This
 	 * method should only be called from one of the fragments that extend this
 	 * abstract class.</b>
 	 *
 	 * @param view
 	 * The View object as received from the getView() method.
 	 *
 	 * @param modelBoardPosition
 	 * The board position with height and / or width values that will be used
 	 * when resizing the rest of the game board. For checkers and chess, this
 	 * should probably be x0y7.
 	 *
 	 * @param xPositions
 	 * An int array of handles to all of the board's rows.
 	 *
 	 * @param yPositions
 	 * An int array of handles to all of the board's columns.
 	 */
 	protected void setAllBoardPositionsToEqualHeightAndWidth(final View view, final int modelBoardPosition,
 		final int [] xPositions, final int [] yPositions)
 	{
 		final Resources resources = getResources();
 		final ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
 
 		viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
 		{
 			@SuppressLint("NewApi")
 			@SuppressWarnings("deprecation")
 			@Override
 			public void onGlobalLayout()
 			{
 				final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams
 					(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
 				final View view = getView();
 
 				if (view != null)
 				{
 					final View boardPosition = view.findViewById(modelBoardPosition);
 
 					if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
 					{
 						final int width = boardPosition.getWidth();
 						layoutParams.height = width;
 
 						for (final int yPosition : yPositions)
 						{
 							view.findViewById(yPosition).setLayoutParams(layoutParams);
 						}
 					}
 					else
 					{
 						final int height = boardPosition.getHeight();
 						layoutParams.width = height;
 
 						for (final int xPosition : xPositions)
 						{
 							view.findViewById(xPosition).setLayoutParams(layoutParams);
 						}
 					}
 
 					if (viewTreeObserver.isAlive())
 					{
 						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
 						{
 							viewTreeObserver.removeOnGlobalLayoutListener(this);
 						}
 						else
 						{
 							viewTreeObserver.removeGlobalOnLayoutListener(this);
 						}
 					}
 				}
 			}
 		});
 	}
 
 
 	/**
 	 * Sets the background of the given ImageButton to what it should be. If
 	 * the ImageButton was just now selected, it will be given a highlighted
 	 * background that signifies that fact. If the ImageButton was just now
 	 * deselected then it will have its background set back to its default.
 	 * 
 	 * @param position
 	 * The ImageButton to change the background of.
 	 * 
 	 * @param newlySelected
 	 * True if this ImageButton was just now selected.
 	 * 
 	 * @param coordinate
 	 * The coordinate for the given ImageButton.
 	 */
 	@SuppressWarnings("deprecation")
 	protected void setPositionBackground(final ImageButton position, final boolean newlySelected, final Coordinate coordinate)
 	{
 		if (coordinate.areBothEitherEvenOrOdd())
 		{
 			if (newlySelected)
 			{
 				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
 				{
 					position.setBackground(backgroundBoardDarkSelected);
 				}
 				else
 				{
 					position.setBackgroundDrawable(backgroundBoardDarkSelected);
 				}
 			}
 			else
 			{
 				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
 				{
 					position.setBackground(backgroundBoardDark);
 				}
 				else
 				{
 					position.setBackgroundDrawable(backgroundBoardDark);
 				}
 			}
 		}
 		else
 		{
 			if (newlySelected)
 			{
 				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
 				{
 					position.setBackground(backgroundBoardBrightSelected);
 				}
 				else
 				{
 					position.setBackgroundDrawable(backgroundBoardBrightSelected);
 				}
 			}
 			else
 			{
 				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
 				{
 					position.setBackground(backgroundBoardBright);
 				}
 				else
 				{
 					position.setBackgroundDrawable(backgroundBoardBright);
 				}
 			}
 		}
 	}
 
 
 	/**
 	 * Skips the user's turn by asking the user if they're sure that they want
 	 * to using a dialog box.
 	 */
 	private void skipMove()
 	{
 		if (Utilities.verifyValidString(game.getId()) && !isAnAsyncTaskRunning())
 		{
 			serverApiTask = new ServerApiSkipMove(getSherlockActivity(), serverApiListeners, game);
 			serverApiTask.execute();
 		}
 	}
 
 
 	/**
 	 * Undoes the user's last move on the board. Unlocks the board, allowing
 	 * the user to make a different move on the board.
 	 */
 	private void undoMove()
 	{
 		if (board.isBoardLocked() || board.hasMoveBeenMade())
 		{
 			clearSelectedPositions();
 
 			try
 			{
 				board.reset();
 			}
 			catch (final JSONException e)
 			{
 				listeners.onDataError();
 			}
 
 			flush();
 			Utilities.compatInvalidateOptionsMenu(getSherlockActivity(), true);
 		}
 	}
 
 
 
 
 	/**
 	 * An AsyncTask that will download the current state of the game board from
 	 * the server.
 	 */
 	private final class AsyncGetGame extends AsyncTask<Void, Void, String>
 	{
 
 
 		private SherlockFragmentActivity fragmentActivity;
 		private LayoutInflater inflater;
 		private ViewGroup viewGroup;
 
 
 		private AsyncGetGame(final SherlockFragmentActivity fragmentActivity, final LayoutInflater inflater, final ViewGroup viewGroup)
 		{
 			this.fragmentActivity = fragmentActivity;
 			this.inflater = inflater;
 			this.viewGroup = viewGroup;
 		}
 
 
 		@Override
 		protected String doInBackground(final Void... params)
 		{
 			String serverResponse = null;
 
 			if (!isCancelled())
 			{
 				final ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 				nameValuePairs.add(new BasicNameValuePair(ServerUtilities.POST_DATA_ID, game.getId()));
 
 				try
 				{
 					serverResponse = ServerUtilities.postToServer(ServerUtilities.ADDRESS_GET_GAME, nameValuePairs);
 				}
 				catch (final IOException e)
 				{
 					Log.e(LOG_TAG, "IOException error in AsyncGetGame - doInBackground()!", e);
 				}
 			}
 
 			return serverResponse;
 		}
 
 
 		private void cancelled()
 		{
 			setRunningState(false);
 			listeners.onGetGameCancelled();
 		}
 
 
 		@Override
 		protected void onCancelled()
 		{
 			cancelled();
 		}
 
 
 		@Override
 		protected void onCancelled(final String serverResponse)
 		{
 			cancelled();
 		}
 
 
 		@Override
 		protected void onPostExecute(final String serverResponse)
 		{
 			boardJSON = parseServerResponse(serverResponse);
 
 			viewGroup.removeAllViews();
 			inflater.inflate(getGameView(), viewGroup);
 
 			initViewsAndLoadPieces();
 			resumeOldBoard();
 			flush();
 
 			setRunningState(false);
 		}
 
 
 		@Override
 		protected void onPreExecute()
 		{
 			setRunningState(true);
 
 			viewGroup.removeAllViews();
 			inflater.inflate(R.layout.generic_game_fragment_loading, viewGroup);
 
 			final TextView textView = (TextView) viewGroup.findViewById(R.id.generic_game_fragment_loading_textview);
 			textView.setText(getString(getLoadingText(), game.getPerson().getName()));
 		}
 
 
 		/**
 		 * Use this method to reset the options menu. This should only be used when
 		 * an AsyncTask is running (or has just finished).
 		 * 
 		 * @param isRunning
 		 * True if the AsyncTask is just starting to run, false if it's just
 		 * finished.
 		 */
 		private void setRunningState(final boolean isRunning)
 		{
 			if (!isRunning)
 			{
 				asyncGetGame = null;
 			}
 
 			Utilities.compatInvalidateOptionsMenu(fragmentActivity);
 		}
 
 
 	}
 
 
 
 
 	/**
 	 * Can be looked at as a main method for classes that extend this one. This
 	 * method is very important because it must returns which layout this
 	 * fragment will need to inflate. Returning the needed layout is really the
 	 * only thing that this method needs to do. In fact, it's probably the only
 	 * thing that this method should do. Stuff that's typically in an
 	 * Activity's onCreate() method should instead be, for fragments, placed in
 	 * the onActivityCreated() method.
 	 */
 	protected abstract void onCreateView();
 
 
 	/**
 	 * This method is run at the end of the Android onCreateOptionsMenu method.
 	 * Use this method to add menu items to this Fragment's menu.
 	 *
 	 * @param menu
 	 * The Menu object as given by Android.
 	 *
 	 * @param inflater
 	 * The MenuInflater object as given by Android. Use this to inflate a menu
 	 * XML file.
 	 */
 	protected abstract void createOptionsMenu(final Menu menu, final MenuInflater inflater);
 
 
 	/**
 	 * Versions of Android below Honeycomb (v3.0) do not properly garbage
 	 * collect Drawable objects. Use this method to perform the proper
 	 * releasing and removal of any Drawable objects from memory. <strong>This
 	 * method only runs when the version of Android running this app is
 	 * Gingerbread and below.</strong>
 	 */
 	protected abstract void destroyBitmapDrawables();
 
 
 	/**
 	 * This method is run at the end of the Android onOptionsItemSelected
 	 * method if the selected MenuItem was not already found. Use this to check
 	 * to see which MenuItem was selected by the user.
 	 *
 	 * @param item
 	 * The selected MenuItem as given by Android.
 	 *
 	 * @return
 	 * Returns true if the selected MenuItem was found. Returns
 	 * super.onOptionsItemSelected(item) if the MenuItem was not found.
 	 */
 	protected abstract boolean optionsItemSelected(final MenuItem item);
 
 
 	/**
 	 * This method is run at the end of the Android onPrepareOptionsMenu
 	 * method. Use this to make alterations to the currently alive menu.
 	 *
 	 * @param menu
 	 * The Menu object as given by Android. This is the object you'll want to
 	 * edit if you want to make changes to the currently alive menu.
 	 */
 	protected abstract void prepareOptionsMenu(final Menu menu);
 
 
 	/**
 	 * A Game specific implementation that looks at the given Position
 	 * parameter and draws a piece on that position if / as necessary.
 	 * 
 	 * @param position
 	 * The current Position object in the flush() loop.
 	 */
 	protected abstract void flush(final Position position);
 
 
 	/**
 	 * @return
 	 * Returns the int value for the XML layout to use as the standard game
 	 * board layout. For checkers, this method will return
 	 * R.layout.checkers_game_layout. For chess, this method will return
 	 * R.layout.chess_game_layout.
 	 */
 	protected abstract int getGameView();
 
 
 	/**
 	 * @return
 	 * Returns the int value for the XML String to use as the text to display
 	 * as a loading message when the AsyncGetGame AsyncTask is running.
 	 */
 	protected abstract int getLoadingText();
 
 
 	/**
 	 * Initialize the game board as if it's a <strong>brand new game</strong>.
 	 * 
 	 * @throws JSONException
 	 * This should never happen. But still, I guess it still possibly could. So
 	 * definitely prepare for that situation in some way. If this Exception is
 	 * thrown then it can be assumed that the game we were trying to initialize
 	 * is corrupt. A corrupt game should <b>never, ever</b> be played. So this
 	 * should trigger the GenericGameFragment to destroy itself to prevent
 	 * some sort of malformity from hitting the Classy Games server.
 	 */
 	protected abstract void initNewBoard() throws JSONException;
 
 
 	/**
 	 * Initializes some of this Fragment's view data such as layout
 	 * configurations (width and height of the board's positions) and
 	 * onClickListeners.
 	 */
 	protected abstract void initViews();
 
 
 	/**
 	 * Loads in the images to be used for the game pieces on the actual game
 	 * board. If the game has the potential for custom colored pieces, then
 	 * this code should read the Android shared preferences information and
 	 * load in the respective colors.
 	 */
 	protected abstract void loadPieces();
 
 
 	/**
 	 * Checks to see which position on the board was clicked and then moves
 	 * pieces and / or performs actions accordingly. This method will only be
 	 * called if there is no previous position on the board that the user
 	 * clicked on.
 	 *
 	 * @param positionCurrent
 	 * The ImageButton object that was just now clicked on.
 	 */
 	protected abstract void onBoardClick(final ImageButton positionCurrent);
 
 
 	/**
 	 * Checks to see which position on the board was clicked and then moves
 	 * pieces and / or performs actions accordingly.
 	 * 
 	 * @param positionPrevious
 	 * The ImageButton object that was previously clicked on.
 	 *
 	 * @param positionCurrent
 	 * The ImageButton object that was just now clicked on.
 	 */
 	protected abstract void onBoardClick(final ImageButton positionPrevious, final ImageButton positionCurrent);
 
 
 	/**
 	 * Creates a Board object out of the given JSON String.
 	 * 
 	 * @param boardJSON
 	 * The JSONObject that represents the game board as was received from the
 	 * Classy Games server.
 	 */
 	protected abstract void resumeOldBoard(final JSONObject boardJSON) throws JSONException;
 
 
 
 
 }

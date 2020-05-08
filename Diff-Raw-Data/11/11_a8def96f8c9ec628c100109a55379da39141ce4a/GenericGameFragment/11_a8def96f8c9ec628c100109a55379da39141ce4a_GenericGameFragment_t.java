 package edu.selu.android.classygames;
 
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.res.Resources;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import edu.selu.android.classygames.games.Coordinate;
 import edu.selu.android.classygames.games.GenericBoard;
 import edu.selu.android.classygames.games.Position;
 import edu.selu.android.classygames.models.Game;
 import edu.selu.android.classygames.models.Person;
 import edu.selu.android.classygames.utilities.ServerUtilities;
 import edu.selu.android.classygames.utilities.Utilities;
 
 
 public abstract class GenericGameFragment extends SherlockFragment
 {
 
 
 	private final static String LOG_TAG = Utilities.LOG_TAG + " - GenericGameFragment";
 
 
 	public final static String KEY_GAME_ID = "KEY_GAME_ID";
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
 	 * Stores the arguments given to this Fragment.
 	 */
 	private Bundle arguments;
 
 
 	/**
 	 * Variable that holds whether or not the asyncGetGame AsyncTask is
 	 * currently running.
 	 */
 	private boolean isAsyncGetGameRunning = false;
 
 
 	/**
 	 * Holds a handle to the currently running (if it's currently running)
 	 * AsyncGetGame AsyncTask. This could be null.
 	 */
 	private AsyncGetGame asyncGetGame;
 
 
 	/**
 	 * Variable that holds whether or not the asyncSendMove AsyncTask is
 	 * currently running.
 	 */
 	private boolean isAsyncSendMoveRunning = false;
 
 
 	/**
 	 * Holds a handle to the currently running (if it's currently running)
 	 * AsyncSendMove AsncTask. This could be null.
 	 */
 	private AsyncSendMove asyncSendMove;
 
 
 	/**
 	 * Variable that holds whether or not the asyncSkipMove AsyncTask is
 	 * currently running.
 	 */
 	private boolean isAsyncSkipMoveRunning = false;
 
 
 	/**
 	 * Holds a handle to the currently running (if it's currently running)
 	 * AsyncSkipMove AsyncTask. This could be null.
 	 */
 	private AsyncSkipMove asyncSkipMove;
 
 
 	/**
 	 * Boolean indicating if the board is in a state that would allow it to be
 	 * sent to the server.
 	 */
 	private boolean isReadyToSendMove = false;
 
 
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
 	protected OnClickListener onBoardClick;
 
 
 
 
 	/**
 	 * One of this class's callback methods. This is fired during this
 	 * fragment's onCreateOptionsMenu.
 	 */
 	private GenericGameFragmentIsDeviceLargeListener genericGameFragmentIsDeviceSmallListener;
 
 	public interface GenericGameFragmentIsDeviceLargeListener
 	{
 		public boolean genericGameFragmentIsDeviceSmall();
 	}
 
 
 	/**
 	 * One of this class's callback methods. This is fired in the event that
 	 * the user cancels the AsyncGetGame AsyncTask.
 	 */
 	private GenericGameFragmentOnAsyncGetGameOnCancelledListener genericGameFragmentOnAsyncGetGameOnCancelledListener;
 
 	public interface GenericGameFragmentOnAsyncGetGameOnCancelledListener
 	{
 		public void genericGameFragmentOnAsyncGetGameOnCancelled();
 	}
 
 
 	/**
 	 * One of this class's callback methods. This is fired in the event that
 	 * a move has finished being sent to the server.
 	 */
 	private GenericGameFragmentOnAsyncSendMoveFinishedListener genericGameFragmentOnAsyncSendOrSkipMoveFinishedListener;
 
 	public interface GenericGameFragmentOnAsyncSendMoveFinishedListener
 	{
 		public void genericGameFragmentOnAsyncSendOrSkipMoveFinished();
 	}
 
 
 	/**
 	 * One of this class's callback methods. This is fired in the event that an
 	 * error was detected in some of the data needed to instantiate a game.
 	 */
 	private GenericGameFragmentOnDataErrorListener genericGameFragmentOnDataErrorListener;
 
 	public interface GenericGameFragmentOnDataErrorListener
 	{
 		public void genericGameFragmentOnDataError();
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
 
		if (arguments == null || arguments.isEmpty())
		{
			arguments = getArguments();
		}
 
 		if (arguments == null || arguments.isEmpty())
 		{
 			genericGameFragmentOnDataErrorListener.genericGameFragmentOnDataError();
 		}
 		else
 		{
 			final String gameId = arguments.getString(KEY_GAME_ID);
 			final long personId = arguments.getLong(KEY_PERSON_ID);
 			final String personName = arguments.getString(KEY_PERSON_NAME);
 
 			if (Person.isIdValid(personId) && Person.isNameValid(personName))
 			{
 				onBoardClick = new OnClickListener()
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
 							// deselect action
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
 				{
 					game = new Game(person, gameId);
 					getGame();
 				}
 				else
 				{
 					game = new Game(person);
 
 					try
 					{
 						initNewBoard();
 						initViews();
 						flush();
 					}
 					catch (final JSONException e)
 					{
 						genericGameFragmentOnDataErrorListener.genericGameFragmentOnDataError();
 					}
 				}
 			}
 			else
 			{
 				genericGameFragmentOnDataErrorListener.genericGameFragmentOnDataError();
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
 			genericGameFragmentIsDeviceSmallListener = (GenericGameFragmentIsDeviceLargeListener) activity;
 			genericGameFragmentOnAsyncGetGameOnCancelledListener = (GenericGameFragmentOnAsyncGetGameOnCancelledListener) activity;
 			genericGameFragmentOnAsyncSendOrSkipMoveFinishedListener = (GenericGameFragmentOnAsyncSendMoveFinishedListener) activity;
 			genericGameFragmentOnDataErrorListener = (GenericGameFragmentOnDataErrorListener) activity;
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
 
 		if (genericGameFragmentIsDeviceSmallListener.genericGameFragmentIsDeviceSmall())
 		{
 			menu.removeItem(R.id.game_fragment_activity_menu_new_game);
 		}
 
		if (arguments == null || arguments.isEmpty())
		{
			arguments = getArguments();
		}

 		if (!Utilities.verifyValidString(arguments.getString(KEY_GAME_ID)))
 		{
 			menu.removeItem(R.id.generic_game_fragment_menu_skip_move);
 		}
 
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && isAsyncGetGameRunning)
 		{
 			inflater.inflate(R.menu.generic_cancel, menu);
 		}
 		else
 		{
 			inflater.inflate(R.menu.generic_game_fragment, menu);
 		}
 
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case R.id.generic_cancel_menu_cancel:
 				if (isAsyncGetGameRunning)
 				{
 					asyncGetGame.cancel(true);
 				}
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
 				return super.onOptionsItemSelected(item);
 		}
 
 		return true;
 	}
 
 
 	@Override
 	public void onPrepareOptionsMenu(final Menu menu)
 	{
 		if (!isAsyncGetGameRunning)
 		{
 			MenuItem menuItem = menu.findItem(R.id.generic_game_fragment_menu_send_move);
 			if (menuItem != null)
 			{
 				menuItem.setEnabled(isReadyToSendMove);
 			}
 
 			menuItem = menu.findItem(R.id.generic_game_fragment_menu_undo_move);
 			if (menuItem != null)
 			{
 				menuItem.setEnabled(isReadyToSendMove);
 			}
 		}
 	}
 
 
 
 
 	/**
 	 * Attemps to cancel the currently running AsyncGetGame AsyncTask.
 	 * 
 	 * @return
 	 * Returns true if the AsyncTask was cancelled.
 	 */
 	private boolean cancelRunningAsyncGetGame()
 	{
 		boolean cancelled = false;
 
 		if (isAsyncGetGameRunning)
 		{
 			asyncGetGame.cancel(true);
 			cancelled = true;
 		}
 
 		return cancelled;
 	}
 
 
 	/**
 	 * Attempts to cancel the currently running AsyncSendMove AsyncTask.
 	 * 
 	 * @return
 	 * Returns true if the AsyncTask was cancelled.
 	 */
 	private boolean cancelRunningAsyncSendMove()
 	{
 		boolean cancelled = false;
 
 		if (isAsyncSendMoveRunning)
 		{
 			asyncSendMove.cancel(true);
 			cancelled = true;
 		}
 
 		return cancelled;
 	}
 
 
 	/**
 	 * Attempts to cancel the currently running AsyncSkipMove AsyncTask.
 	 * 
 	 * @return
 	 * Returns true if the AsyncTask was cancelled.
 	 */
 	private boolean cancelRunningAsyncSkipMove()
 	{
 		boolean cancelled = false;
 
 		if (isAsyncSkipMoveRunning)
 		{
 			asyncSkipMove.cancel(true);
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
 			if (!cancelRunningAsyncSendMove())
 			{
 				cancelRunningAsyncSkipMove();
 			}
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
 			final Coordinate previous = new Coordinate((String) positionSelectedPrevious.getTag());
 			setPositionBackground(positionSelectedPrevious, false, previous);
 			positionSelectedPrevious = null;
 		}
 
 		if (positionSelectedCurrent != null)
 		{
 			final Coordinate current = new Coordinate((String) positionSelectedCurrent.getTag());
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
 	 * If the AsyncGetGame AsyncTask is not already running, then this will
 	 * execute it.
 	 */
 	private void getGame()
 	{
 		if (!isAsyncGetGameRunning)
 		{
 			asyncGetGame = new AsyncGetGame(getSherlockActivity(), getLayoutInflater(getArguments()), (ViewGroup) getView());
 			asyncGetGame.execute();
 		}
 	}
 
 
 	/**
 	 * @return
 	 * Returns true if either the AsyncGetGame AsyncTask is running or if the
 	 * AsyncSendMove AsyncTask is running.
 	 */
 	public boolean isAnAsyncTaskRunning()
 	{
 		return isAsyncGetGameRunning || isAsyncSendMoveRunning || isAsyncSkipMoveRunning;
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
 		isReadyToSendMove = true;
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
 	 * If the AsyncSendMove AsyncTask is not already running, then this will
 	 * execute it.
 	 */
 	private void sendMove()
 	{
 		if (!isAsyncSendMoveRunning && isReadyToSendMove)
 		{
 			asyncSendMove = new AsyncSendMove(getSherlockActivity());
 			asyncSendMove.execute();
 		}
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
 	 * Skips the user's turn.
 	 */
 	private void skipMove()
 	{
 		if (Utilities.verifyValidString(game.getId()) && !isAsyncSkipMoveRunning)
 		{
 			final AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity())
 				.setMessage(R.string.generic_game_fragment_skipmove_dialog_message)
 				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
 				{
 					@Override
 					public void onClick(final DialogInterface dialog, final int which)
 					{
 						dialog.dismiss();
 					}
 				})
 				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
 				{
 					@Override
 					public void onClick(final DialogInterface dialog, final int which)
 					{
 						if (Utilities.verifyValidString(game.getId()) && !isAsyncSkipMoveRunning)
 						{
 							asyncSkipMove = new AsyncSkipMove(getSherlockActivity());
 							asyncSkipMove.execute();
 						}
 
 						dialog.dismiss();
 					}
 				})
 				.setTitle(R.string.generic_game_fragment_skipmove_dialog_title);
 
 			builder.show();
 		}
 	}
 
 
 	/**
 	 * Undoes the user's last move on the board. Unlocks the board, allowing
 	 * the user to make a different move on the board.
 	 */
 	private void undoMove()
 	{
 		if (board.getIsBoardLocked() || isReadyToSendMove)
 		{
 			clearSelectedPositions();
 
 			try
 			{
 				board.reset();
 			}
 			catch (final JSONException e)
 			{
 				genericGameFragmentOnDataErrorListener.genericGameFragmentOnDataError();
 			}
 
 			flush();
 			isReadyToSendMove = false;
 
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
 
 
 		AsyncGetGame(final SherlockFragmentActivity fragmentActivity, final LayoutInflater inflater, final ViewGroup viewGroup)
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
 			genericGameFragmentOnAsyncGetGameOnCancelledListener.genericGameFragmentOnAsyncGetGameOnCancelled();
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
 
 			initViews();
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
 		 * This method will initialize the game board as if this is an in progress
 		 * game. This <strong>resumes</strong> an old game. Do not use this for a
 		 * brand new game.
 		 */
 		private void resumeOldBoard()
 		{
 			if (boardJSON == null)
 			{
 				Log.e(LOG_TAG, "Tried to build the board from either a null or empty JSON String!");
 				genericGameFragmentOnDataErrorListener.genericGameFragmentOnDataError();
 			}
 			else
 			{
 				try
 				{
 					GenericGameFragment.this.resumeOldBoard(boardJSON);
 				}
 				catch (final JSONException e)
 				{
 					Log.e(LOG_TAG, "resumeOldBoard(): boardJSON is massively malformed.", e);
 					genericGameFragmentOnDataErrorListener.genericGameFragmentOnDataError();
 				}
 			}
 		}
 
 
 		/**
 		 * Use this method to reset the options menu. This should only be used when
 		 * an AsyncTask is running.
 		 * 
 		 * @param isRunning
 		 * True if the AsyncTask is just starting to run, false if it's just
 		 * finished.
 		 */
 		private void setRunningState(final boolean isRunning)
 		{
 			isAsyncGetGameRunning = isRunning;
 			Utilities.compatInvalidateOptionsMenu(fragmentActivity);
 		}
 
 
 	}
 
 
 
 
 	/**
 	 * An AsyncTask that will send the user's move on the game board to the
 	 * server.
 	 */
 	private final class AsyncSendMove extends AsyncTask<Void, Void, String>
 	{
 
 
 		private Context context;
 		private ProgressDialog progressDialog;
 
 
 		AsyncSendMove(final Context context)
 		{
 			this.context = context;
 		}
 
 
 		@Override
 		protected String doInBackground(final Void... params)
 		{
 			String serverResponse = null;
 
 			if (!isCancelled())
 			{
 				try
 				{
 					final Person whoAmI = Utilities.getWhoAmI(context);
 
 					final JSONObject boardJSON = board.makeJSON();
 					final String boardJSONString = boardJSON.toString();
 
 					final ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 					nameValuePairs.add(new BasicNameValuePair(ServerUtilities.POST_DATA_USER_CREATOR, whoAmI.getIdAsString()));
 					nameValuePairs.add(new BasicNameValuePair(ServerUtilities.POST_DATA_BOARD, boardJSONString));
 
 					if (!isCancelled())
 					{
 						if (Utilities.verifyValidString(game.getId()))
 						{
 							nameValuePairs.add(new BasicNameValuePair(ServerUtilities.POST_DATA_USER_CHALLENGED, game.getPerson().getIdAsString()));
 							nameValuePairs.add(new BasicNameValuePair(ServerUtilities.POST_DATA_NAME, game.getPerson().getName()));
 							nameValuePairs.add(new BasicNameValuePair(ServerUtilities.POST_DATA_GAME_ID, game.getId()));
 
 							serverResponse = ServerUtilities.postToServer(ServerUtilities.ADDRESS_NEW_MOVE, nameValuePairs);
 						}
 						else
 						{
 							nameValuePairs.add(new BasicNameValuePair(ServerUtilities.POST_DATA_USER_CHALLENGED, game.getPerson().getIdAsString()));
 							nameValuePairs.add(new BasicNameValuePair(ServerUtilities.POST_DATA_NAME, game.getPerson().getName()));
 
 							serverResponse = ServerUtilities.postToServer(ServerUtilities.ADDRESS_NEW_GAME, nameValuePairs);
 						}
 					}
 				}
 				catch (final IOException e)
 				{
 					Log.e(LOG_TAG, "IOException error in AsyncSendMove - doInBackground()!", e);
 				}
 				catch (final JSONException e)
 				{
 					Log.e(LOG_TAG, "JSONException error in AsyncSendMove - doInBackground()!", e);
 				}
 			}
 
 			return serverResponse;
 		}
 
 
 		private void cancelled()
 		{
 			if (progressDialog.isShowing())
 			{
 				progressDialog.dismiss();
 			}
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
 			parseServerResponse(serverResponse);
 
 			if (progressDialog.isShowing())
 			{
 				progressDialog.dismiss();
 			}
 
 			genericGameFragmentOnAsyncSendOrSkipMoveFinishedListener.genericGameFragmentOnAsyncSendOrSkipMoveFinished();
 		}
 
 
 		@Override
 		protected void onPreExecute()
 		{
 			progressDialog = new ProgressDialog(context);
 			progressDialog.setCancelable(true);
 			progressDialog.setCanceledOnTouchOutside(true);
 			progressDialog.setMessage(context.getString(R.string.generic_game_fragment_sendmove_progressdialog_message));
 
 			progressDialog.setOnCancelListener(new OnCancelListener()
 			{
 				@Override
 				public void onCancel(final DialogInterface dialog)
 				{
 					AsyncSendMove.this.cancel(true);
 				}
 			});
 
 			progressDialog.setTitle(R.string.generic_game_fragment_progressdialog_title);
 			progressDialog.show();
 		}
 
 
 	}
 
 
 
 
 	/**
 	 * An AsyncTask that will skip the user's turn.
 	 */
 	private final class AsyncSkipMove extends AsyncTask<Void, Void, String>
 	{
 
 
 		private Context context;
 		private ProgressDialog progressDialog;
 
 
 		AsyncSkipMove(final Context context)
 		{
 			this.context = context;
 		}
 
 
 		@Override
 		protected String doInBackground(final Void... params)
 		{
 			String serverResponse = null;
 
 			if (!isCancelled() && Utilities.verifyValidString(game.getId()))
 			{
 				try
 				{
 					final Person whoAmI = Utilities.getWhoAmI(context);
 
 					final JSONObject boardJSON = board.makeJSON();
 					final String boardJSONString = boardJSON.toString();
 
 					final ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 					nameValuePairs.add(new BasicNameValuePair(ServerUtilities.POST_DATA_USER_CREATOR, whoAmI.getIdAsString()));
 					nameValuePairs.add(new BasicNameValuePair(ServerUtilities.POST_DATA_BOARD, boardJSONString));
 
 					if (!isCancelled())
 					{
 						nameValuePairs.add(new BasicNameValuePair(ServerUtilities.POST_DATA_USER_CHALLENGED, game.getPerson().getIdAsString()));
 						nameValuePairs.add(new BasicNameValuePair(ServerUtilities.POST_DATA_NAME, game.getPerson().getName()));
 						nameValuePairs.add(new BasicNameValuePair(ServerUtilities.POST_DATA_GAME_ID, game.getId()));
 
 						serverResponse = ServerUtilities.postToServer(ServerUtilities.ADDRESS_SKIP_MOVE, nameValuePairs);
 					}
 				}
 				catch (final IOException e)
 				{
 					Log.e(LOG_TAG, "IOException error in AsyncSkipMove - doInBackground()!", e);
 				}
 				catch (final JSONException e)
 				{
 					Log.e(LOG_TAG, "JSONException error in AsyncSkipMove - doInBackground()!", e);
 				}
 			}
 
 			return serverResponse;
 		}
 
 
 		private void cancelled()
 		{
 			if (progressDialog.isShowing())
 			{
 				progressDialog.dismiss();
 			}
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
 			if (progressDialog.isShowing())
 			{
 				progressDialog.dismiss();
 			}
 
 			genericGameFragmentOnAsyncSendOrSkipMoveFinishedListener.genericGameFragmentOnAsyncSendOrSkipMoveFinished();
 		}
 
 
 		@Override
 		protected void onPreExecute()
 		{
 			progressDialog = new ProgressDialog(context);
 			progressDialog.setCancelable(true);
 			progressDialog.setCanceledOnTouchOutside(true);
 			progressDialog.setMessage(context.getString(R.string.generic_game_fragment_skipmove_progressdialog_message));
 
 			progressDialog.setOnCancelListener(new OnCancelListener()
 			{
 				@Override
 				public void onCancel(final DialogInterface dialog)
 				{
 					AsyncSkipMove.this.cancel(true);
 				}
 			});
 
 			progressDialog.setTitle(R.string.generic_game_fragment_progressdialog_title);
 			progressDialog.show();
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
 	 * 
 	 * @return
 	 * This method must return the Android int representation of its layout.
 	 * For checkers, this method will return R.layout.checkers_game_layout. For
 	 * chess, this method will return R.layout.chess_game_layout.
 	 */
 	protected abstract void onCreateView();
 
 
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
 	 * This should never happen. 
 	 */
 	protected abstract void initNewBoard() throws JSONException;
 
 
 	/**
 	 * Initializes some of this Fragment's view data, such as the actionbar's
 	 * title, layout configurations, and onClickListeners.
 	 */
 	protected abstract void initViews();
 
 
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

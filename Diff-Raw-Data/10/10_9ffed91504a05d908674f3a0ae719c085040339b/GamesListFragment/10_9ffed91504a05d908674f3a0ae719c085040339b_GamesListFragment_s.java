 package com.charlesmadere.android.classygames;
 
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.res.Resources;
 import android.graphics.Shader;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.charlesmadere.android.classygames.gcm.GCMIntentService;
 import com.charlesmadere.android.classygames.models.Game;
 import com.charlesmadere.android.classygames.models.ListItem;
 import com.charlesmadere.android.classygames.models.Person;
 import com.charlesmadere.android.classygames.server.*;
 import com.charlesmadere.android.classygames.utilities.FacebookUtilities;
 import com.charlesmadere.android.classygames.utilities.Utilities;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 
 
 public final class GamesListFragment extends SherlockListFragment implements
 	AdapterView.OnItemClickListener,
 	AdapterView.OnItemLongClickListener
 {
 
 
 	private final static String LOG_TAG = Utilities.LOG_TAG + " - GamesListFragment";
 	private final static String KEY_GAMES_LIST_JSON = "KEY_GAMES_LIST_JSON";
 
 
 	private ListView list;
 	private TextView empty;
 	private LinearLayout loading;
 	private TextView noInternetConnection;
 
 
 	/**
 	 * JSONObject downloaded from the server that represents the games list.
 	 */
 	private JSONObject gamesListJSON;
 
 
 	/**
 	 * Boolean that marks if this is the first time that the onResume() method
 	 * was hit.
 	 */
 	private boolean isFirstOnResume = true;
 
 
 	/**
 	 * Used to perform a refresh of the Games List.
 	 */
 	private AsyncRefreshGamesList asyncRefreshGamesList;
 
 
 	/**
 	 * If the user has selected a game in their games list, then this will be a
 	 * handle to that Game object. If no game is currently selected, then this
 	 * will be null.
 	 */
 	private ListItem<Game> selectedGame;
 
 
 	/**
 	 * Holds a handle to a currently running (if it's currently running)
 	 * ServerApi object.
 	 */
 	private ServerApi serverApiTask;
 
 
 	/**
 	 * Callback interface for the ServerApi class.
 	 */
 	private ServerApi.Listeners serverApiListeners;
 
 
 
 
 	/**
 	 * Object that allows us to run any of the methods that are defined in the
 	 * Listeners interface.
 	 */
 	private Listeners listeners;
 
 
 	/**
 	 * A bunch of listener methods for this Fragment.
 	 */
 	public interface Listeners
 	{
 
 
 		/**
 		 * This is fired when the user selects a game in their games list.
 		 * 
 		 * @param game
 		 * The Game object that the user selected.
 		 */
 		public void onGameSelected(final Game game);
 
 
 		/**
 		 * This is fired whenever the user has selected the Refresh button on
 		 * the action bar.
 		 */
 		public void onRefreshSelected();
 
 
 	}
 
 
 
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 	}
 
 
 	@Override
 	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
 	{
 		return inflater.inflate(R.layout.games_list_fragment, container, false);
 	}
 
 
 	@Override
 	@SuppressWarnings("deprecation")
 	public void onActivityCreated(final Bundle savedInstanceState)
 	{
 		super.onActivityCreated(savedInstanceState);
 		final View view = getView();
 
 		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
 		{
 			final BitmapDrawable background = (BitmapDrawable) getResources().getDrawable(R.drawable.bg_bright);
 			background.setAntiAlias(true);
 			background.setDither(true);
 			background.setFilterBitmap(true);
 			background.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
 			view.setBackgroundDrawable(background);
 		}
 
 		serverApiListeners = new ServerApi.Listeners()
 		{
 			@Override
 			public void onCancel()
 			{
 				serverApiTask = null;
 			}
 
 
 			@Override
 			public void onComplete(final String serverResponse)
 			{
 				serverApiTask = null;
 				listeners.onRefreshSelected();
 			}
 
 
 			@Override
 			public void onDismiss()
 			{
 				serverApiTask = null;
 			}
 		};
 
 		list = getListView();
 		list.setOnItemClickListener(this);
 		list.setOnItemLongClickListener(this);
 		empty = (TextView) view.findViewById(android.R.id.empty);
 		loading = (LinearLayout) view.findViewById(R.id.games_list_fragment_loading);
 		noInternetConnection = (TextView) view.findViewById(R.id.fragment_no_internet_connection);
 
 		if (savedInstanceState != null && savedInstanceState.containsKey(KEY_GAMES_LIST_JSON))
 		{
 			final String gamesListJSONString = savedInstanceState.getString(KEY_GAMES_LIST_JSON);
 
 			if (Utilities.validString(gamesListJSONString))
 			{
 				try
 				{
 					gamesListJSON = new JSONObject(gamesListJSONString);
 				}
 				catch (final JSONException e)
 				{
 					Log.e(LOG_TAG, "JSONException in onActivityCreated()!", e);
 				}
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
 		listeners = (Listeners) activity;
 	}
 
 
 	@Override
 	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
 	{
 		inflater.inflate(R.menu.games_list_fragment, menu);
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 
 	@Override
 	public void onDestroyView()
 	{
 		cancelRunningAnyAsyncTask();
 		super.onDestroyView();
 	}
 
 
 	@Override
 	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
 	{
 		@SuppressWarnings("unchecked")
 		final ListItem<Game> game = (ListItem<Game>) parent.getItemAtPosition(position);
 
 		if (!game.isSelected() && game.get().isTypeGame() && game.get().isTurnYours())
 		{
 			listeners.onGameSelected(game.get());
 			selectedGame = game;
 			selectedGame.select();
 
 			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
 			{
 				view.setActivated(true);
 			}
 		}
 	}
 
 
 	@Override
 	public boolean onItemLongClick(final AdapterView<?> parent, final View view, int position, final long id)
 	{
 		if (!isAnAsyncTaskRunning())
 		{
 			@SuppressWarnings("unchecked")
 			final ListItem<Game> game = (ListItem<Game>) parent.getItemAtPosition(position);
 
 			if (game.get().isTypeGame())
 			{
 				final Context context = getSherlockActivity();
 				String [] items;
 
 				if (game.get().isTurnYours())
 				{
 					items = getResources().getStringArray(R.array.games_list_fragment_context_menu_entries_turn_yours);
 				}
 				else
 				{
 					items = getResources().getStringArray(R.array.games_list_fragment_context_menu_entries_turn_theirs);
 				}
 
 				new AlertDialog.Builder(context)
 					.setItems(items, new DialogInterface.OnClickListener()
 					{
 						@Override
 						public void onClick(final DialogInterface dialog, final int which)
 						{
 							dialog.dismiss();
 
 							switch (which)
 							{
 								case 0:
 									serverApiTask = new ServerApiForfeitGame(context, serverApiListeners, game.get());
 									break;
 
 								case 1:
 									serverApiTask = new ServerApiSkipMove(context, serverApiListeners, game.get());
 									break;
 							}
 
 							if (serverApiTask != null)
 							{
 								serverApiTask.execute();
 							}
 						}
 					})
 					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
 					{
 						@Override
 						public void onClick(final DialogInterface dialog, final int which)
 						{
 							dialog.dismiss();
 						}
 					})
 					.setTitle(getString(R.string.select_an_action_for_this_game_against_x, game.get().getPerson().getName()))
 					.show();
 
 				return true;
 			}
 
 			return false;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case R.id.games_list_fragment_menu_refresh:
 				if (!isAnAsyncTaskRunning())
 				{
 					listeners.onRefreshSelected();
 				}
 				break;
 
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 
 		return true;
 	}
 
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 
 		if (isFirstOnResume)
 		{
 			isFirstOnResume = false;
 
 			final boolean restoreExistingList = gamesListJSON != null;
 			refreshGamesList(restoreExistingList);
 		}
 	}
 
 
 	@Override
 	public void onSaveInstanceState(final Bundle outState)
 	{
 		if (gamesListJSON != null)
 		{
 			final String gamesListJSONString = gamesListJSON.toString();
 
 			if (Utilities.validString(gamesListJSONString))
 			{
 				outState.putString(KEY_GAMES_LIST_JSON, gamesListJSONString);
 			}
 		}
 
 		super.onSaveInstanceState(outState);
 	}
 
 
 
 
 	/**
 	 * Cancels the AsyncRefreshGamesList AsyncTask if it is currently
 	 * running.
 	 */
 	private void cancelRunningAnyAsyncTask()
 	{
 		if (isAsyncRefreshGamesListRunning())
 		{
 			asyncRefreshGamesList.cancel(true);
 		}
 		else if (serverApiTask != null)
 		{
 			serverApiTask.cancel();
 		}
 	}
 
 
 	private void deselectGame()
 	{
 		if (selectedGame != null)
 		{
 			selectedGame.unselect();
 		}
 
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
 		{
 			for (int i = 0; i < list.getChildCount(); ++i)
 			{
 				final View view = list.getChildAt(i);
 				view.setActivated(false);
 			}
 		}
 	}
 
 
 	/**
 	 * @return
 	 * Returns true if the asyncRefreshGamesList AsyncTask is currently
 	 * running.
 	 */
 	private boolean isAsyncRefreshGamesListRunning()
 	{
 		return asyncRefreshGamesList != null;
 	}
 
 
 	/**
 	 * @return
 	 * Returns true if an AsyncTask is running.
 	 */
 	private boolean isAnAsyncTaskRunning()
 	{
 		return isAsyncRefreshGamesListRunning() || serverApiTask != null;
 	}
 
 
 	public boolean onBackPressed()
 	{
 		if (isAnAsyncTaskRunning())
 		{
 			cancelRunningAnyAsyncTask();
 		}
 		else
 		{
 			deselectGame();
 		}
 
 		return false;
 	}
 
 
 	public void refreshGamesList()
 	{
 		refreshGamesList(false);
 	}
 
 
 	/**
 	 * Refreshes the Games List if a refresh is not already running.
 	 * 
 	 * @param restoreExistingList
 	 * Set this to true if you want to restore the games list from the existing
 	 * stored games list. Set this to false to force the app to download a new
 	 * games list from the server.
 	 */
 	private void refreshGamesList(final boolean restoreExistingList)
 	{
 		if (!isAnAsyncTaskRunning())
 		{
 			asyncRefreshGamesList = new AsyncRefreshGamesList(restoreExistingList);
 			asyncRefreshGamesList.execute();
 		}
 	}
 
 
 
 
 	private final class AsyncRefreshGamesList extends AsyncTask<Void, Void, LinkedList<ListItem<Game>>>
 		implements Comparator<ListItem<Game>>
 	{
 
 
 		private byte runStatus;
 		private final static byte RUN_STATUS_NORMAL = 1;
 		private final static byte RUN_STATUS_IOEXCEPTION = 2;
 		private final static byte RUN_STATUS_NO_NETWORK_CONNECTION = 3;
 
 
 		private boolean restoreExistingList;
 		private SherlockFragmentActivity fragmentActivity;
 
 
 		private AsyncRefreshGamesList(final boolean restoreExistingList)
 		{
 			this.restoreExistingList = restoreExistingList;
 			fragmentActivity = getSherlockActivity();
 			runStatus = RUN_STATUS_NORMAL;
 		}
 
 
 		@Override
 		protected LinkedList<ListItem<Game>> doInBackground(final Void... params)
 		{
 			LinkedList<ListItem<Game>> games = null;
 
 			if (restoreExistingList && gamesListJSON != null)
 			{
 				games = parseServerResponse(null);
 			}
 			else if (!isCancelled())
 			{
 				restoreExistingList = false;
 				final Person whoAmI = Utilities.getWhoAmI(fragmentActivity);
 
 				GCMIntentService.clearNotifications(fragmentActivity);
 				GenericGameFragment.clearCachedBoards(fragmentActivity);
 				MyStatsDialogFragment.clearCachedStats(fragmentActivity);
 
 				if (!isCancelled() && Utilities.checkForNetworkConnectivity(fragmentActivity))
 				{
 					try
 					{
 						Thread.sleep(Server.WAIT_FOR_SERVER_DELAY);
 					}
 					catch (final InterruptedException e)
 					{
 						Log.w(LOG_TAG, "AsyncRefreshGamesList thread sleep interrupted!", e);
 					}
 
 					try
 					{
 						// create the data that will be posted to the server
 						final ApiData data = new ApiData()
 							.addKeyValuePair(Server.POST_DATA_ID, whoAmI.getId());
 
 						// Make a call to the Classy Games server API and store
 						// the JSON response. Note that we're also sending it
 						// the nameValuePairs variable that we just created.
 						// The server requires we send it some information in
 						// order for us to get a meaningful response back.
 						final String serverResponse = Server.postToServerGetGames(data);
 
 						// This line does a lot. Check the parseServerResponse()
 						// method below to get detailed information. This will
 						// parse the JSON response that we got from the server
 						// and create a bunch of individual Game objects out of
 						// that data.
 						games = parseServerResponse(serverResponse);
 					}
 					catch (final IOException e)
 					{
 						Log.e(LOG_TAG, "IOException error in AsyncPopulateGamesList - doInBackground()!", e);
 					}
 				}
 				else
 				{
 					runStatus = RUN_STATUS_NO_NETWORK_CONNECTION;
 				}
 			}
 
 			return games;
 		}
 
 
 		private void cancelled()
 		{
 			setRunningState(false);
 		}
 
 
 		@Override
 		public int compare(final ListItem<Game> gameOne, final ListItem<Game> gameTwo)
 		{
 			return (int) (gameTwo.get().getTimestamp() - gameOne.get().getTimestamp());
 		}
 
 
 		@Override
 		protected void onCancelled()
 		{
 			cancelled();
 		}
 
 
 		@Override
 		protected void onCancelled(final LinkedList<ListItem<Game>> games)
 		{
 			cancelled();
 		}
 
 
 		@Override
 		protected void onPostExecute(final LinkedList<ListItem<Game>> games)
 		{
 			if (runStatus == RUN_STATUS_NORMAL && games != null && !games.isEmpty())
 			{
 				final GamesListAdapter gamesListAdapter = new GamesListAdapter(games);
 				list.setAdapter(gamesListAdapter);
 
 				list.setVisibility(View.VISIBLE);
 				empty.setVisibility(View.GONE);
 				loading.setVisibility(View.GONE);
 				noInternetConnection.setVisibility(View.GONE);
 			}
 			else if (runStatus == RUN_STATUS_IOEXCEPTION || runStatus == RUN_STATUS_NO_NETWORK_CONNECTION)
 			{
 				list.setVisibility(View.GONE);
 				empty.setVisibility(View.GONE);
 				loading.setVisibility(View.GONE);
 				noInternetConnection.setVisibility(View.VISIBLE);
 			}
 			else
 			{
 				list.setVisibility(View.GONE);
 				empty.setVisibility(View.VISIBLE);
 				loading.setVisibility(View.GONE);
 				noInternetConnection.setVisibility(View.GONE);
 			}
 
 			setRunningState(false);
 		}
 
 
 		@Override
 		protected void onPreExecute()
 		{
 			setRunningState(true);
 
 			list.setVisibility(View.GONE);
 			empty.setVisibility(View.GONE);
 			loading.setVisibility(View.VISIBLE);
 			noInternetConnection.setVisibility(View.GONE);
 		}
 
 
 		/**
 		 * Parses the JSON response from the server and makes a bunch of Game
 		 * objects about it. 
 		 * 
 		 * @param serverResponse
 		 * The JSON response acquired from the Classy Games server. This method
 		 * <strong>does</strong> check to make sure that this String is both
 		 * not null and not empty. If that scenario happens then this method
 		 * will return an empty LinkedList of Game objects.
 		 * 
 		 * @return
 		 * Returns an LinkedList of Game objects. This LinkedList has a
 		 * possibility of being empty.
 		 */
 		private LinkedList<ListItem<Game>> parseServerResponse(final String serverResponse)
 		{
 			final LinkedList<ListItem<Game>> games = new LinkedList<ListItem<Game>>();
 
 			if (!isCancelled())
 			{
 				if (restoreExistingList || Utilities.validString(serverResponse))
 				{
 					try
 					{
 						if (!restoreExistingList)
 						// Check to see if this boolean is set to false. If it
 						// is set to false, then we're restoring an existing
 						// games list.
 						{
 							gamesListJSON = new JSONObject(serverResponse);
 						}
 
 						final JSONObject jsonResult = gamesListJSON.getJSONObject(Server.POST_DATA_RESULT);
 						final JSONObject jsonGameData = jsonResult.optJSONObject(Server.POST_DATA_SUCCESS);
 
 						if (jsonGameData == null)
 						{
 							final String successMessage = jsonResult.optString(Server.POST_DATA_SUCCESS);
 
 							if (Utilities.validString(successMessage))
 							{
 								Log.d(LOG_TAG, "Server returned success message: " + successMessage);
 							}
 							else
 							{
 								final String errorMessage = jsonResult.getString(Server.POST_DATA_ERROR);
 								Log.e(LOG_TAG, "Server returned error message: " + errorMessage);
 							}
 						}
 						else
 						{
 							LinkedList<ListItem<Game>> turn = parseTurn(jsonGameData, Server.POST_DATA_TURN_YOURS, Game.TURN_YOURS);
 							if (turn != null && !turn.isEmpty())
 							{
 								games.addAll(turn);
 							}
 
 							turn = parseTurn(jsonGameData, Server.POST_DATA_TURN_THEIRS, Game.TURN_THEIRS);
 							if (turn != null && !turn.isEmpty())
 							{
 								games.addAll(turn);
 							}
 						}
 					}
 					catch (final JSONException e)
 					{
 						Log.e(LOG_TAG, "JSON String is massively malformed.", e);
 					}
 				}
 				else
 				{
 					Log.e(LOG_TAG, "Empty or null String received from server on get games!");
 				}
 			}
 
 			return games;
 		}
 
 
 		/**
 		 * Creates and returns a LinkedList of Game objects that are of the
 		 * turn as specified in the whichTurn parameter.
 		 * 
 		 * @param jsonGameData
 		 * The JSON game data as received from the server.
 		 * 
 		 * @param postDataTurn
 		 * Which turn to pull from the JSON game data. This variable's value
 		 * should be one of the Server.POST_DATA_TURN_* variables.
 		 * 
 		 * @param whichTurn
 		 * Who's turn is this? This variable's value should be one of the
 		 * GAME.TURN_* variables.
 		 * 
 		 * @return
 		 * Returns all of the games of the specified turn. Has the possibility
 		 * of being null. Check for that!
 		 */
 		private LinkedList<ListItem<Game>> parseTurn(final JSONObject jsonGameData, final String postDataTurn, final boolean whichTurn)
 		{
 			LinkedList<ListItem<Game>> games = null;
 
 			try
 			{
 				final JSONArray turn = jsonGameData.getJSONArray(postDataTurn);
 				final int turnLength = turn.length();
 
 				if (turnLength >= 1)
 				// ensure that we have at least one element in the JSONArray
 				{
 					games = new LinkedList<ListItem<Game>>();
 					final Game separator = new Game(whichTurn);
 					games.add(new ListItem<Game>(separator));
 
 					for (int i = 0; i < turnLength && !isCancelled(); ++i)
 					// loop through all of the games in this turn
 					{
 						try
 						{
 							final JSONObject jsonGame = turn.getJSONObject(i);
 
 							final Game game = new Game(jsonGame, whichTurn);
 							games.add(new ListItem<Game>(game));
 						}
 						catch (final JSONException e)
 						{
 							Log.e(LOG_TAG, "Error parsing a turn's game data! (" + i + ") whichTurn: " + whichTurn);
 						}
 					}
 
 					Collections.sort(games, this);
 				}
 				else
 				{
 					throw new JSONException("Player has no games for this turn.");
 				}
 			}
 			catch (final JSONException e)
 			{
 				if (whichTurn == Game.TURN_YOURS)
 				{
 					Log.d(LOG_TAG, "Player has no games that are his own turn.");
 				}
 				else
 				{
 					Log.d(LOG_TAG, "Player has no games that are the other people's turn.");
 				}
 			}
 
 			return games;
 		}
 
 
 		/**
 		 * Use this method to reset the options menu. This should only be used when
 		 * an AsyncTask has either just begun or has just ended.
 		 * 
 		 * @param isRunning
 		 * True if the AsyncTask is just starting to run, false if it's just
 		 * finished.
 		 */
 		private void setRunningState(final boolean isRunning)
 		{
 			if (!isRunning)
 			{
 				asyncRefreshGamesList = null;
 			}
 		}
 
 
 	}
 
 
 
 
 	private final class GamesListAdapter extends BaseAdapter
 	{
 
 
 		private Activity activity;
 		private Drawable checkersIcon;
 		private Drawable chessIcon;
 		private Drawable emptyProfilePicture;
 		private LayoutInflater inflater;
 		private LinkedList<ListItem<Game>> games;
 		private Resources resources;
 
 
 		private GamesListAdapter(final LinkedList<ListItem<Game>> games)
 		{
 			this.games = games;
 			activity = getSherlockActivity();
 			inflater = activity.getLayoutInflater();
 			resources = getResources();
 			emptyProfilePicture = resources.getDrawable(R.drawable.empty_profile_picture_small);
 			checkersIcon = resources.getDrawable(R.drawable.game_icon_checkers_small);
 			chessIcon = resources.getDrawable(R.drawable.game_icon_chess_small);
 		}
 
 
 		@Override
 		public int getCount()
 		{
 			return games.size();
 		}
 
 
 		@Override
 		public ListItem<Game> getItem(final int position)
 		{
 			return games.get(position);
 		}
 
 
 		@Override
 		public long getItemId(final int position)
 		{
 			return position;
 		}
 
 
 		@Override
 		public View getView(final int position, View convertView, final ViewGroup parent)
 		{
 			final ListItem<Game> listItem = games.get(position);
 			final Game game = listItem.get();
 
 			if (game.isTypeGame())
 			{
				if (convertView == null)
 				{
 					convertView = inflater.inflate(R.layout.games_list_fragment_listview_item, null);
 					final ViewHolder viewHolder = new ViewHolder(convertView);
 					convertView.setTag(viewHolder);
 				}
 
 				final ViewHolder viewHolder = (ViewHolder) convertView.getTag();
 				viewHolder.picture.setImageDrawable(emptyProfilePicture);
 				final String friendsPictureURL = FacebookUtilities.getFriendsPictureSquare(activity, game.getPerson().getId());
 				Utilities.getImageLoader().displayImage(friendsPictureURL, viewHolder.picture);
 
 				viewHolder.name.setText(game.getPerson().getName());
 				viewHolder.time.setText(game.getTimestampFormatted(resources));
 
 				if (game.isGameCheckers())
 				{
 					viewHolder.gameIcon.setImageDrawable(checkersIcon);
 				}
 				else if (game.isGameChess())
 				{
 					viewHolder.gameIcon.setImageDrawable(chessIcon);
 				}
 				else
 				{
 					viewHolder.gameIcon.setImageDrawable(null);
 				}
 
 				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
 				{
 					if (listItem.isSelected())
 					{
 						convertView.setActivated(true);
 					}
 					else
 					{
 						convertView.setActivated(false);
 					}
 				}
 			}
 			else
 			{
 				if (game.isTurnYours())
 				{
 					convertView = inflater.inflate(R.layout.games_list_fragment_listview_turn_yours, null);
 				}
 				else
 				{
 					convertView = inflater.inflate(R.layout.games_list_fragment_listview_turn_theirs, null);
 				}
 
 				convertView.setOnClickListener(null);
 				convertView.setOnLongClickListener(null);
 			}
 
 			return convertView;
 		}
 
 
 	}
 
 
 
 
 	private final class ViewHolder
 	{
 
 
 		private ImageView gameIcon;
 		private ImageView picture;
 		private TextView name;
 		private TextView time;
 
 
 		private ViewHolder(final View view)
 		{
 			gameIcon = (ImageView) view.findViewById(R.id.games_list_fragment_listview_item_game_icon);
 			picture = (ImageView) view.findViewById(R.id.games_list_fragment_listview_item_picture);
 			name = (TextView) view.findViewById(R.id.games_list_fragment_listview_item_name);
 			time = (TextView) view.findViewById(R.id.games_list_fragment_listview_item_time);
 		}
 
 
 	}
 
 
 
 
 }

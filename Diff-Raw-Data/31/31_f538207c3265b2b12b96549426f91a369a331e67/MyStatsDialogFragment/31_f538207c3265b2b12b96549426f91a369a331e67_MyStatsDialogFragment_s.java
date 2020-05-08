 package com.charlesmadere.android.classygames;
 
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockDialogFragment;
 import com.charlesmadere.android.classygames.server.Server;
 import com.charlesmadere.android.classygames.server.ServerApi;
 import com.charlesmadere.android.classygames.server.ServerApiGetStats;
 import com.charlesmadere.android.classygames.utilities.Utilities;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 /**
  * This Fragment shows the user their stats and scores and stuff.
  */
 public class MyStatsDialogFragment extends SherlockDialogFragment
 {
 
 
 	private final static String LOG_TAG = Utilities.LOG_TAG + " - MyStatsDialogFragment";
 	public final static String PREFERENCES_NAME = "MyProfileFragment_Preferences";
 	private final static String KEY_CHECKERS_LOSES = "KEY_CHECKERS_LOSES";
 	private final static String KEY_CHECKERS_WINS = "KEY_CHECKERS_WINS";
 	private final static String KEY_CHESS_LOSES = "KEY_CHESS_LOSES";
 	private final static String KEY_CHESS_WINS = "KEY_CHESS_WINS";
 
 
 	private ServerApi serverTask;
 	private SharedPreferences sPreferences;
 	private Editor sPreferencesEditor;
 	private LinearLayout stats;
 	private TextView checkersLoses;
 	private TextView checkersWins;
 	private TextView chessLoses;
 	private TextView chessWins;
 	private TextView gamesPlayed;
 	private LinearLayout loading;
 	private View view;
 
 
 
 	private Listeners listeners;
 
 
 	public interface  Listeners
 	{
 		/**
 		 * This method is fired whenever there was an error when trying to
 		 * parse data as received from the server when performing the GetStats
 		 * server api call.
 		 */
		public void onGetStatsDataError();
 	}
 
 
 
 
 	@Override
 	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
 	{
 		getDialog().requestWindowFeature(STYLE_NO_TITLE);
 		return inflater.inflate(R.layout.my_stats_dialog_fragment, container, false);
 	}
 
 
 	@Override
 	public void onActivityCreated(final Bundle savedInstanceState)
 	{
 		super.onActivityCreated(savedInstanceState);
 
 		findViews();
 		getPreferences();
 
 		final int checkersLosses = sPreferences.getInt(KEY_CHECKERS_LOSES, -1);
 		final int checkersWins = sPreferences.getInt(KEY_CHECKERS_WINS, -1);
 		final int chessLosses = sPreferences.getInt(KEY_CHESS_LOSES, -1);
 		final int chessWins = sPreferences.getInt(KEY_CHESS_WINS, -1);
 
 		if (checkersLosses >= 0 && checkersWins >= 0 && chessLosses >= 0 && chessWins >= 0)
 		{
 			flipViews();
 			flushViews(checkersLosses, checkersWins, chessLosses, chessWins);
 		}
 		else
 		{
 			getPreferencesEditor();
 			sPreferencesEditor.clear();
 			sPreferencesEditor.commit();
 
 			serverTask = new ServerApiGetStats(getSherlockActivity(), new ServerApi.Listeners()
 			{
 				@Override
 				public void onCancel()
 				{
 					serverTask = null;
 				}
 
 
 				@Override
 				public void onComplete(final String serverResponse)
 				{
 					parseServerResponse(serverResponse);
 					serverTask = null;
 				}
 
 
 				@Override
 				public void onDismiss()
 				{
 					serverTask = null;
 				}
 			});
 
 			serverTask.execute(false);
 		}
 	}
 
 
 	@Override
 	public void onAttach(final Activity activity)
 	{
 		super.onAttach(activity);
 
 		try
 		{
 			listeners = (Listeners) activity;
 		}
 		catch (final ClassCastException e)
 		{
 			throw new ClassCastException(activity.toString() + " must implement listeners!");
 		}
 	}
 
 
 	/**
 	 * Cancels the currently running AsyncTask (if any).
 	 */
 	public void cancelRunningServerTask()
 	{
 		if (isServerTaskRunning())
 		{
 			serverTask.cancel();
 		}
 	}
 
 
 	private void findViews()
 	{
 		if (view == null || checkersLoses == null || checkersWins == null || chessLoses == null
 			|| chessWins == null || gamesPlayed == null || loading == null)
 		{
 			view = getView();
 			stats = (LinearLayout) view.findViewById(R.id.my_stats_dialog_fragment_linearlayout_stats);
 			checkersLoses = (TextView) view.findViewById(R.id.my_stats_dialog_fragment_textview_checkers_loses);
 			checkersWins = (TextView) view.findViewById(R.id.my_stats_dialog_fragment_textview_checkers_wins);
 			chessLoses = (TextView) view.findViewById(R.id.my_stats_dialog_fragment_textview_chess_loses);
 			chessWins = (TextView) view.findViewById(R.id.my_stats_dialog_fragment_textview_chess_wins);
 			gamesPlayed = (TextView) view.findViewById(R.id.my_stats_dialog_fragment_textview_games_played);
 			loading = (LinearLayout) view.findViewById(R.id.my_stats_dialog_fragment_textview_loading);
 		}
 	}
 
 
 	private void flipViews()
 	{
 		findViews();
 
 		if (stats.getVisibility() == View.GONE || gamesPlayed.getVisibility() == View.GONE
 			|| loading.getVisibility() == View.VISIBLE)
 		{
 			stats.setVisibility(View.VISIBLE);
 			gamesPlayed.setVisibility(View.VISIBLE);
 			loading.setVisibility(View.GONE);
 		}
 		else
 		{
 			stats.setVisibility(View.GONE);
 			gamesPlayed.setVisibility(View.GONE);
 			loading.setVisibility(View.VISIBLE);
 		}
 	}
 
 
 	private void flushViews(final int[] checkers, final int[] chess)
 	{
 		flushViews(checkers[0], checkers[1], chess[0], chess[1]);
 	}
 
 
 	private void flushViews(final int checkersLoses, final int checkersWins, final int chessLoses,
 		final int chessWins)
 	{
 		if (checkersLoses == 1)
 		{
 			this.checkersLoses.setText(getString(R.string.one_loss));
 		}
 		else
 		{
 			this.checkersLoses.setText(getString(R.string.x_loses, checkersLoses));
 		}
 
 		if (checkersWins == 1)
 		{
 			this.checkersWins.setText(getString(R.string.one_win));
 		}
 		else
 		{
 			this.checkersWins.setText(getString(R.string.x_wins, checkersWins));
 		}
 
 		if (chessLoses == 1)
 		{
 			this.chessLoses.setText(getString(R.string.one_loss));
 		}
 		else
 		{
 			this.chessLoses.setText(getString(R.string.x_loses, chessLoses));
 		}
 
 		if (chessWins == 1)
 		{
 			this.chessWins.setText(getString(R.string.one_win));
 		}
 		else
 		{
 			this.chessWins.setText(getString(R.string.x_wins, chessWins));
 		}
 
 		final int gamesPlayed = checkersLoses + checkersWins + chessLoses + chessWins;
 
 		if (gamesPlayed == 1)
 		{
 			this.gamesPlayed.setText(getString(R.string.one_game_played));
 		}
 		else
 		{
 			this.gamesPlayed.setText(getString(R.string.x_games_played, gamesPlayed));
 		}
 	}
 
 
 	private SharedPreferences getPreferences()
 	{
 		if (sPreferences == null)
 		{
 			sPreferences = getSherlockActivity().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
 		}
 
 		return sPreferences;
 	}
 
 
 	private Editor getPreferencesEditor()
 	{
 		if (sPreferencesEditor == null)
 		{
 			sPreferencesEditor = getPreferences().edit();
 		}
 
 		return sPreferencesEditor;
 	}
 
 
 	/**
 	 * @return
 	 * Returns true if a ServerApi Task is running.
 	 */
 	public boolean isServerTaskRunning()
 	{
 		return serverTask != null;
 	}
 
 
 	/**
 	 * Reads in the server response and gets the user's win and lose data out
 	 * of it.
 	 *
 	 * @param serverResponse
 	 * The raw String data as received from the Classy Games server.
 	 */
 	private void parseServerResponse(final String serverResponse)
 	{
 		if (Utilities.verifyValidString(serverResponse))
 		{
 			try
 			{
 				final JSONObject jsonResult = new JSONObject(serverResponse);
 				final JSONObject jsonStatsData = jsonResult.optJSONObject(Server.POST_DATA_SUCCESS);
 
 				if (jsonStatsData == null)
 				{
 					final String successMessage = jsonResult.optString(Server.POST_DATA_SUCCESS);
 
 					if (Utilities.verifyValidString(successMessage))
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
 					final int[] checkers = parseLosesAndWins(jsonStatsData, Server.POST_DATA_CHECKERS);
 					final int[] chess = parseLosesAndWins(jsonStatsData, Server.POST_DATA_CHESS);
 					flushViews(checkers, chess);
 				}
 			}
 			catch (final JSONException e)
 			{
				Log.e(LOG_TAG, "JSON String is massively malformed.", e);
				listeners.onGetStatsDataError();
 			}
 		}
 		else
 		{
			listeners.onGetStatsDataError();
 		}
 	}
 
 
 	private int[] parseLosesAndWins(final JSONObject statsData, final String game)
 		throws JSONException
 	{
 		final JSONObject gameStats = statsData.getJSONObject(game);
 		final int loses = gameStats.getInt(Server.POST_DATA_LOSES);
 		final int wins = gameStats.getInt(Server.POST_DATA_WINS);
 		return new int[] { loses, wins };
 	}
 
 
 }

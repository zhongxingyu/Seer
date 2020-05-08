 package org.cniska.foosball.android;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 
 public class StatisticsActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {
 
 	private static final String TAG = "StatisticsActivity";
 
 	// Enumerables
 	// ----------------------------------------
 
 	public enum SortColumn {
 		PLAYER,
 		WINS,
 		LOSSES,
 		GAMES_PLAYED,
 		WIN_LOSS_RATIO,
 		GOALS_FOR,
 		GOALS_AGAINST,
 		RATING
 	}
 
 	public enum SortDirection {
 		ASCENDING,
 		DESCENDING
 	}
 
 	// Inner classes
 	// ----------------------------------------
 
 	private class PlayerComparator implements Comparator<Player> {
 
 		private SortColumn mColumn;
 		private SortDirection mDirection;
 
 		@Override
 		public int compare(Player p1, Player p2) {
 			if (mColumn != null) {
 				switch (mColumn) {
 					case WINS:
 						return compareInt(p1.getWins(), p2.getWins());
 					case LOSSES:
 						return compareInt(p1.getLosses(), p2.getLosses());
 					case GAMES_PLAYED:
 						return compareInt(p1.gamesPlayed(), p2.gamesPlayed());
 					case WIN_LOSS_RATIO:
 						return compareFloat(p1.winLossRatio(), p2.winLossRatio());
 					case GOALS_FOR:
 						return compareInt(p1.getGoalsFor(), p2.getGoalsFor());
 					case GOALS_AGAINST:
 						return compareInt(p1.getGoalsAgainst(), p2.getGoalsAgainst());
 					case RATING:
 						return compareInt(p1.getRating(), p2.getRating());
 					case PLAYER:
 					default:
 						return compareString(p1.getName(), p2.getName());
 				}
 			} else {
 				return 0;
 			}
 		}
 
 		/**
 		 * Compares two strings and returns the result.
 		 * @param value1 String 1.
 		 * @param value2 String 2.
 		 * @return The result.
 		 */
 		private int compareString(String value1, String value2) {
 			if (mDirection == SortDirection.ASCENDING) {
 				return value1.compareTo(value2);
 			} else {
 				return value2.compareTo(value1);
 			}
 		}
 
 		/**
 		 * Compares two integers and returns the result.
 		 * @param value1 Integer 1.
 		 * @param value2 Integer 2.
 		 * @return The result.
 		 */
 		private int compareInt(int value1, int value2) {
 			return compareFloat(value1, value2);
 		}
 
 		/**
 		 * Compares two floats and returns the result.
 		 * @param value1 Float 1.
 		 * @param value2 Float 2.
 		 * @return The result.
 		 */
 		private int compareFloat(float value1, float value2) {
 			if (mDirection == SortDirection.ASCENDING && (value1 < value2)
 					|| mDirection == SortDirection.DESCENDING && (value2 < value1)) {
 				return 1;
 			} else if (mDirection == SortDirection.ASCENDING && (value1 > value2)
 					|| mDirection == SortDirection.DESCENDING && (value2 > value1)) {
 				return -1;
 			} else {
 				return 0;
 			}
 		}
 
 		/**
 		 * Changes the sorting order for the comparator.
 		 * @param column Sort column type.
 		 */
 		public void sortColumn(SortColumn column) {
 			if (mDirection != null && column == this.mColumn) {
 				mDirection = oppositeDirection();
 			} else {
 				mDirection = defaultDirection();
 			}
 
 			this.mColumn = column;
 		}
 
 		/**
 		 * Returns the opposite sorting direction to the current.
 		 * @return The direction.
 		 */
 		private SortDirection oppositeDirection() {
 			return mDirection == SortDirection.ASCENDING ? SortDirection.DESCENDING : SortDirection.ASCENDING;
 		}
 
 		/**
 		 * Returns the default sorting direction.
 		 * @return The direction.
 		 */
 		public SortDirection defaultDirection() {
 			return SortDirection.ASCENDING;
 		}
 	}
 
 	private static class PlayerAdapter extends SimpleAdapter {
 
 		/**
 		 * Creates a new adapter.
 		 * @param context The context.
 		 * @param data A list of maps.
 		 */
 		public PlayerAdapter(Context context, ArrayList<HashMap<String, String>> data) {
 			super(context, data, R.layout.statistics_item, new String[] {
 				Player.NAME,
 				Player.WINS,
 				Player.LOSSES,
 				Player.GAMES_PLAYED,
 				Player.GOALS_FOR,
 				Player.GOALS_AGAINST,
 				Player.RATING
 			}, new int[] {
 				R.id.column_name,
 				R.id.column_wins,
 				R.id.column_losses,
 				R.id.column_games_played,
 				R.id.column_goals_for,
 				R.id.column_goals_against,
 				R.id.column_rating
 			});
 		}
 	}
 
 	// Static variables
 	// ----------------------------------------
 
 	private static final String[] PROJECTION = {
 		Player._ID,
 		Player.NAME,
 		Player.WINS,
 		Player.LOSSES,
 		Player.GOALS_FOR,
 		Player.GOALS_AGAINST,
 		Player.RATING
 	};
 
 	// Member variables
 	// ----------------------------------------
 
 	private PlayerAdapter mAdapter;
 	private ListView mListView;
 	private ArrayList<Player> mPlayers = new ArrayList<Player>();
 	private PlayerComparator mComparator = new PlayerComparator();
 
 	// Methods
 	// ----------------------------------------
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setTitle(getString(R.string.title_statistics));
 		setContentView(R.layout.statistics);
 		mListView = (ListView) findViewById(R.id.list);
 		setHeaderClickListeners();
 		getSupportLoaderManager().initLoader(0, null, this);
 		Logger.info(TAG, "Activity created.");
 	}
 
 	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.statistics, menu);
		return true;
	}

	@Override
 	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
 		return new CursorLoader(getApplicationContext(), Player.CONTENT_URI, PROJECTION, null, null, null);
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
 		if (cursor.moveToFirst()) {
 			while (!cursor.isAfterLast()) {
 				mPlayers.add(cursorToPlayer(cursor));
 				cursor.moveToNext();
 			}
 		}
 
 		updateListView();
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> cursorLoader) {
 	}
 
 	/**
 	 * Updates the list view by rebuilding the data for the adapter.
 	 */
 	private void updateListView() {
 		ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
 
 		for (int i = 0, len = mPlayers.size(); i < len; i++) {
 			Player player = mPlayers.get(i);
 			HashMap<String, String> map = new HashMap<String, String>();
 			map.put(Player.NAME, player.getName());
 			map.put(Player.WINS, String.valueOf(player.getWins()));
 			map.put(Player.LOSSES, String.valueOf(player.getLosses()));
 			map.put(Player.GAMES_PLAYED, String.valueOf(player.gamesPlayed()));
 			map.put(Player.GOALS_FOR, String.valueOf(player.getGoalsFor()));
 			map.put(Player.GOALS_AGAINST, String.valueOf(player.getGoalsAgainst()));
 			map.put(Player.RATING, String.valueOf(player.getRating()));
 			data.add(map);
 		}
 
 		mAdapter = new PlayerAdapter(this, data);
 		mListView.setAdapter(mAdapter);
 	}
 
 	/**
 	 * Sets click listeners for the table header cells.
 	 */
 	private void setHeaderClickListeners() {
 		TextView headerPlayer = (TextView) findViewById(R.id.heading_name);
 		headerPlayer.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				sortByColumn(SortColumn.PLAYER);
 			}
 		});
 
 		TextView headerWins = (TextView) findViewById(R.id.heading_wins);
 		headerWins.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				sortByColumn(SortColumn.WINS);
 			}
 		});
 
 		TextView headerLosses = (TextView) findViewById(R.id.heading_losses);
 		headerLosses.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				sortByColumn(SortColumn.LOSSES);
 			}
 		});
 
 		TextView headerGamesPlayed = (TextView) findViewById(R.id.heading_games_played);
 		headerGamesPlayed.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				sortByColumn(SortColumn.GAMES_PLAYED);
 			}
 		});
 
 		TextView headerGoalsFor = (TextView) findViewById(R.id.heading_goals_for);
 		headerGoalsFor.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				sortByColumn(SortColumn.GOALS_FOR);
 			}
 		});
 
 		TextView headerGoalsAgainst = (TextView) findViewById(R.id.heading_goals_against);
 		headerGoalsAgainst.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				sortByColumn(SortColumn.GOALS_AGAINST);
 			}
 		});
 
 		TextView headerRating = (TextView) findViewById(R.id.heading_rating);
 		headerRating.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				sortByColumn(SortColumn.RATING);
 			}
 		});
 	}
 
 	/**
 	 * Sorts the list view by the given column.
 	 * @param column Sort column type.
 	 */
 	private void sortByColumn(SortColumn column) {
 		mComparator.sortColumn(column);
 		Collections.sort(mPlayers, mComparator);
 		updateListView();
 	}
 
 	/**
 	 * Returns a player object with data from the given cursor.
 	 * @param cursor The cursor.
 	 * @return The player.
 	 */
 	private Player cursorToPlayer(Cursor cursor) {
 		Player player = new Player();
 		player.setName(cursor.getString(cursor.getColumnIndex(Player.NAME)));
 		player.setWins(cursor.getInt(cursor.getColumnIndex(Player.WINS)));
 		player.setLosses(cursor.getInt(cursor.getColumnIndex(Player.LOSSES)));
 		player.setGoalsFor(cursor.getInt(cursor.getColumnIndex(Player.GOALS_FOR)));
 		player.setGoalsAgainst(cursor.getInt(cursor.getColumnIndex(Player.GOALS_AGAINST)));
 		player.setRating(cursor.getInt(cursor.getColumnIndex(Player.RATING)));
 		return player;
 	}
 }

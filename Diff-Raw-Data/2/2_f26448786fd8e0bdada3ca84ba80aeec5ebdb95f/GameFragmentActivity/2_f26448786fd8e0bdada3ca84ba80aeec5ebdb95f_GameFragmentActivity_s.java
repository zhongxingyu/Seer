 package com.charlesmadere.android.classygames;
 
 
 import android.app.NotificationManager;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.charlesmadere.android.classygames.models.Game;
 import com.charlesmadere.android.classygames.models.Person;
 import com.charlesmadere.android.classygames.settings.SettingsActivity;
 import com.charlesmadere.android.classygames.utilities.Utilities;
 
 
 public class GameFragmentActivity extends SherlockFragmentActivity implements
 	GamesListFragment.GamesListFragmentListeners,
 	GenericGameFragment.GenericGameFragmentListeners
 {
 
 
 	private final static String LOG_TAG = Utilities.LOG_TAG + " - GameFragmentActivity";
 
 
 	public final static int RESULT_CODE_FINISH = MainActivity.GAME_FRAGMENT_ACTIVITY_REQUEST_CODE_FINISH;
 	public final static int NEW_GAME_FRAGMENT_ACTIVITY_REQUEST_CODE_FRIEND_SELECTED = 16;
 
 
 	public final static String BUNDLE_DATA_GAME_ID = "BUNDLE_DATA_GAME_ID";
 	public final static String BUNDLE_DATA_GAME_TYPE = "BUNDLE_DATA_GAME_TYPE";
 	public final static String BUNDLE_DATA_PERSON_OPPONENT_ID = "BUNDLE_DATA_PERSON_OPPONENT_ID";
 	public final static String BUNDLE_DATA_PERSON_OPPONENT_NAME = "BUNDLE_DATA_PERSON_OPPONENT_NAME";
 
 	private final static String KEY_ACTION_BAR_TITLE = "KEY_ACTION_BAR_TITLE";
 
 
 
 
 	private EmptyGameFragment emptyGameFragment;
 	private GamesListFragment gamesListFragment;
 	private GenericGameFragment genericGameFragment;
 
 
 
 
 	@Override
 	protected void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.game_fragment_activity);
 		setResult(RESULT_CODE_FINISH);
 		Utilities.styleActionBar(getResources(), getSupportActionBar(), false);
 
 		final FragmentManager fManager = getSupportFragmentManager();
 
 		if (savedInstanceState == null)
 		{
 			final FragmentTransaction fTransaction = fManager.beginTransaction();
 
 			if (isDeviceLarge())
 			{
 				emptyGameFragment = new EmptyGameFragment();
 				fTransaction.add(R.id.game_fragment_activity_fragment_game, emptyGameFragment);
 
 				gamesListFragment = (GamesListFragment) fManager.findFragmentById(R.id.game_fragment_activity_fragment_games_list_fragment);
 			}
 			else
 			{
 				gamesListFragment = new GamesListFragment();
 				fTransaction.add(R.id.game_fragment_activity_container, gamesListFragment);
 			}
 
 			fTransaction.commit();
 		}
 		else
 		{
 			if (isDeviceLarge())
 			{
 				try
 				{
 					emptyGameFragment = (EmptyGameFragment) fManager.findFragmentById(R.id.game_fragment_activity_fragment_game);
 				}
 				catch (final ClassCastException e)
 				{
 					genericGameFragment = (GenericGameFragment) fManager.findFragmentById(R.id.game_fragment_activity_fragment_game);
 
 					final ActionBar actionBar = getSupportActionBar();
 					actionBar.setDisplayHomeAsUpEnabled(true);
 					actionBar.setTitle(savedInstanceState.getCharSequence(KEY_ACTION_BAR_TITLE));
 				}
 			}
 			else
 			{
 				try
 				{
 					gamesListFragment = (GamesListFragment) fManager.findFragmentById(R.id.game_fragment_activity_container);
 				}
 				catch (final ClassCastException e)
 				{
 					genericGameFragment = (GenericGameFragment) fManager.findFragmentById(R.id.game_fragment_activity_container);
 
 					final ActionBar actionBar = getSupportActionBar();
 					actionBar.setDisplayHomeAsUpEnabled(true);
 					actionBar.setTitle(savedInstanceState.getCharSequence(KEY_ACTION_BAR_TITLE));
 				}
 			}
 		}
 
 		final Intent intent = getIntent();
 
 		if (intent != null)
 		{
 			if (intent.hasExtra(BUNDLE_DATA_GAME_ID) && intent.hasExtra(BUNDLE_DATA_GAME_TYPE)
 				&& intent.hasExtra(BUNDLE_DATA_PERSON_OPPONENT_ID) && intent.hasExtra(BUNDLE_DATA_PERSON_OPPONENT_NAME))
 			{
 				final String gameId = intent.getStringExtra(BUNDLE_DATA_GAME_ID);
				final byte gameType = intent.getByteExtra(BUNDLE_DATA_GAME_TYPE, (byte) 0);
 				final long personId = intent.getLongExtra(BUNDLE_DATA_PERSON_OPPONENT_ID, 0);
 				final String personName = intent.getStringExtra(BUNDLE_DATA_PERSON_OPPONENT_NAME);
 
 				if (Game.isIdValid(gameId) && Game.isWhichGameValid(gameType)
 					&& Person.isIdValid(personId) && Person.isNameValid(personName))
 				{
 					final Game game = new Game(new Person(personId, personName), gameId);
 					onGameSelected(game);
 				}
 			}
 		}
 	}
 
 
 	@Override
 	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (resultCode == NewGameFragmentActivity.RESULT_CODE_FRIEND_SELECTED)
 		{
 			final Bundle extras = data.getExtras();
 
 			if (extras != null && !extras.isEmpty())
 			{
 				final long id = extras.getLong(NewGameFragmentActivity.KEY_FRIEND_ID);
 				final String name = extras.getString(NewGameFragmentActivity.KEY_FRIEND_NAME);
 				final byte type = extras.getByte(NewGameFragmentActivity.KEY_GAME_TYPE);
 
 				if (Person.isIdValid(id) && Person.isNameValid(name) && Game.isWhichGameValid(type))
 				{
 					final Person friend = new Person(id, name);
 					final Game game = new Game(friend, type);
 					onGameSelected(game);
 				}
 				else
 				{
 					Log.e(LOG_TAG, "onActivityResult() received game data that was malformed.");
 				}
 			}
 		}
 	}
 
 
 	@Override
 	public void onBackPressed()
 	{
 		if (gamesListFragment != null && gamesListFragment.isAnAsyncTaskRunning())
 		{
 			gamesListFragment.cancelRunningAnyAsyncTask();
 		}
 		else
 		{
 			final ActionBar actionBar = getSupportActionBar();
 			actionBar.setDisplayHomeAsUpEnabled(false);
 			actionBar.setTitle(R.string.games_list);
 
 			super.onBackPressed();
 		}
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(final Menu menu)
 	{
 		final MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.game_fragment_activity, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 
 	@Override
 	protected void onDestroy()
 	{
 		if (gamesListFragment != null)
 		{
 			gamesListFragment.cancelRunningAnyAsyncTask();
 		}
 
 		if (genericGameFragment != null)
 		{
 			genericGameFragment.cancelRunningAnyAsyncTask();
 		}
 
 		super.onDestroy();
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case android.R.id.home:
 				onBackPressed();
 				break;
 
 			case R.id.game_fragment_activity_menu_about:
 				startActivity(new Intent(this, AboutActivity.class));
 				break;
 
 			case R.id.game_fragment_activity_menu_new_game:
 				if (isDeviceLarge() && genericGameFragment != null && genericGameFragment.isVisible())
 				{
 					onBackPressed();
 				}
 
 				startActivityForResult(new Intent(this, NewGameFragmentActivity.class), NEW_GAME_FRAGMENT_ACTIVITY_REQUEST_CODE_FRIEND_SELECTED);
 				break;
 
 			case R.id.game_fragment_activity_menu_settings:
 				startActivity(new Intent(this, SettingsActivity.class));
 				break;
 
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 
 		return true;
 	}
 
 
 	@Override
 	protected void onSaveInstanceState(final Bundle outState)
 	{
 		final CharSequence actionBarTitle = getSupportActionBar().getTitle();
 		outState.putCharSequence(KEY_ACTION_BAR_TITLE, actionBarTitle);
 
 		super.onSaveInstanceState(outState);
 	}
 
 
 
 
 	/**
 	 * Ensures that the gamesListFragment Fragment is not null.
 	 */
 	private void getGamesListFragment()
 	{
 		if (gamesListFragment == null)
 		{
 			final FragmentManager fManager = getSupportFragmentManager();
 
 			if (isDeviceLarge())
 			{
 				gamesListFragment = (GamesListFragment) fManager.findFragmentById(R.id.game_fragment_activity_fragment_games_list_fragment);
 			}
 			else
 			{
 				try
 				{
 					gamesListFragment = (GamesListFragment) fManager.findFragmentById(R.id.game_fragment_activity_container);
 				}
 				catch (final ClassCastException e)
 				{
 
 				}
 			}
 		}
 	}
 
 
 	/**
 	 * Checks to see what size screen this device has. This method will return
 	 * true if the device has a large screen, and as such said device should be
 	 * using the dual pane, fragment based, layout stuff.
 	 * 
 	 * @return
 	 * Returns true if this device has a large screen.
 	 */
 	private boolean isDeviceLarge()
 	{
 		return findViewById(R.id.game_fragment_activity_container) == null;
 	}
 
 
 
 
 	@Override
 	public void onGameSelected(final Game game)
 	{
 		if (isDeviceLarge() || (genericGameFragment == null || !genericGameFragment.isVisible()))
 		{
 			if (genericGameFragment != null && genericGameFragment.isVisible())
 			{
 				onBackPressed();
 			}
 
 			final Bundle arguments = new Bundle();
 			arguments.putString(GenericGameFragment.KEY_GAME_ID, game.getId());
 			arguments.putLong(GenericGameFragment.KEY_PERSON_ID, game.getPerson().getId());
 			arguments.putString(GenericGameFragment.KEY_PERSON_NAME, game.getPerson().getName());
 
 			if (game.isGameCheckers())
 			{
 				genericGameFragment = new CheckersGameFragment();
 			}
 			else if (game.isGameChess())
 			{
 				genericGameFragment = new ChessGameFragment();
 			}
 
 			genericGameFragment.setArguments(arguments);
 
 			final FragmentTransaction fTransaction = getSupportFragmentManager().beginTransaction();
 			fTransaction.addToBackStack(null);
 
 			if (isDeviceLarge())
 			{
 				fTransaction.replace(R.id.game_fragment_activity_fragment_game, genericGameFragment);
 			}
 			else
 			{
 				fTransaction.add(R.id.game_fragment_activity_container, genericGameFragment);
 			}
 
 			fTransaction.commit();
 
 			final ActionBar actionBar = getSupportActionBar();
 			actionBar.setDisplayHomeAsUpEnabled(true);
 
 			if (game.isGameCheckers())
 			{
 				actionBar.setTitle(getString(R.string.checkers_with_x, game.getPerson().getName()));
 			}
 			else if (game.isGameChess())
 			{
 				actionBar.setTitle(getString(R.string.chess_with_x, game.getPerson().getName()));
 			}
 		}
 	}
 
 
 	@Override
 	public void onRefreshSelected()
 	{
 		if (isDeviceLarge() && genericGameFragment != null && genericGameFragment.isVisible())
 		{
 			onBackPressed();
 		}
 
 		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
 
 		getGamesListFragment();
 		gamesListFragment.refreshGamesList();
 	}
 
 
 	@Override
 	public boolean isDeviceSmall()
 	{
 		return !isDeviceLarge();
 	}
 
 
 	@Override
 	public void onDataError()
 	{
 		Utilities.easyToastAndLogError(this, R.string.couldnt_create_the_game_as_malformed_data_was_detected);
 		onBackPressed();
 	}
 
 
 	@Override
 	public void onGetGameCancelled()
 	{
 		onBackPressed();
 	}
 
 
 	@Override
 	public void onServerApiTaskFinished()
 	{
 		final FragmentManager fManager = getSupportFragmentManager();
 		fManager.popBackStack();
 		final FragmentTransaction fTransaction = fManager.beginTransaction();
 
 		if (isDeviceLarge())
 		{
 			if (emptyGameFragment == null)
 			{
 				emptyGameFragment = new EmptyGameFragment();
 			}
 
 			fTransaction.replace(R.id.game_fragment_activity_fragment_game, emptyGameFragment);
 		}
 		else
 		{
 			fTransaction.remove(genericGameFragment);
 		}
 
 		fTransaction.commit();
 		fManager.executePendingTransactions();
 
 		final ActionBar actionBar = getSupportActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(false);
 		actionBar.setTitle(R.string.games_list);
 
 		onRefreshSelected();
 	}
 
 
 }

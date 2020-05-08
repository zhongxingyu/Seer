 package local.quidstats;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import local.quidstats.helper.DatabaseHelper;
 import local.quidstats.model.GameDb;
 import local.quidstats.model.PlayerDb;
 import local.quidstats.util.Action;
 import local.quidstats.util.CursorAdapterOnFieldList;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Button;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class FragmentWorkPlayers extends ListFragment
 {
 
 	DatabaseHelper db;
 	String TAG = "FragmentPlayerList";
 
 	ListView currentPlayers;
 	Button awaySnitch;
 	Button homeSnitch;
 	Button undoButton;
 	Button redoButton;
 	TextView homeScore;
 	TextView awayScore;
 	TextView time;
 
 	Timer timer = new Timer();
 	ListAdapter listAdapter;
 	FragmentHolderWork fragmentHolder;
 	View rootView;
 
 	Cursor c = null;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState)
 	{
 		View rootView = inflater.inflate(R.layout.fragment_work_player, null);
 
 		homeScore = (TextView) rootView.findViewById(R.id.home_score);
 		awayScore = (TextView) rootView.findViewById(R.id.away_score);
 		time = (TextView) rootView.findViewById(R.id.time);
 		awaySnitch = (Button) rootView.findViewById(R.id.away_snitch);
 		homeSnitch = (Button) rootView.findViewById(R.id.home_snitch);
 		undoButton = (Button) rootView.findViewById(R.id.undo_button);
 		redoButton = (Button) rootView.findViewById(R.id.redo_button);
 
 		return rootView;
 	}
 
 	public void onActivityCreated(Bundle savedInstanceState)
 	{
 		super.onActivityCreated(savedInstanceState);
 
 		fragmentHolder = (FragmentHolderWork) getActivity();
 		db = new DatabaseHelper(fragmentHolder.mContext);
 		fragmentHolder.playerFrag = this;
 
 		homeScore.setText(fragmentHolder.gInfo.getHomeScore() + "");
 		awayScore.setText(fragmentHolder.gInfo.getAwayScore() + "");
 		int totalSeconds = fragmentHolder.gInfo.getGameTimeSeconds();
 
 		for (int i = 0; i < 7; i++)
 		{
 			fragmentHolder.timeSubbedIn[i] = totalSeconds;
 			fragmentHolder.sinceRefresh[i] = totalSeconds;
 		}
 		displayTime(totalSeconds, time);
 
 		fragmentHolder.running = false;
 		time.setOnClickListener(new OnClickListener()
 		{
 
 			@Override
 			public void onClick(View v)
 			{
 				switchTime(v);
 			}
 
 		});
 
 		populateList();
 
 		getListView().setOnItemLongClickListener(new OnItemLongClickListener()
 		{
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3)
 			{
 				if (c != null)
 				{
 					c.moveToPosition(arg2);
 					AlertDialog.Builder subBuilder = subDialog(fragmentHolder.gId, c
 							.getString(c.getColumnIndex(DatabaseHelper.COL_ID)));
 					subBuilder.show();
 				}
 				return true;
 			}
 
 		});
 		
 		awayScore.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				opponentScored();
 			}
 		});
 
 		awaySnitch.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				opponentSnitch();
 			}
 		});
 
 		homeSnitch.setOnClickListener(new OnClickListener()
 		{
 
 			@Override
 			public void onClick(View v)
 			{
 				homeSnitch();
 			}
 
 		});
 
 		undoButton.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				if (fragmentHolder.undoStack.size() == 0)
 				{
 					Toast toast = Toast.makeText(getActivity(),
 							"Nothing to Undo", Toast.LENGTH_SHORT);
 					toast.show();
 				}
 				else
 				{
 					Action a = fragmentHolder.undoStack.pop();
 					String col = a.getDatabaseColumn();
 					// for opponents
 					if (a.getPlayerId().equals("AWAY"))
 					{
 						// do away team shit
 						if (a.getDatabaseColumn().equals(
 								DatabaseHelper.COL_AWAY_SCORE))
 						{
 							updateScore("opponent", -10);
 							// get rid of the minuses
 							List<PlayerDb> onFieldPlayers = db
 									.getOnFieldPlayersFromGame(fragmentHolder.gId);
 							for (int i = 0; i < onFieldPlayers.size(); i++)
 							{
 								db.updateStat(fragmentHolder.gId, onFieldPlayers.get(i)
 										.getPlayerId(), -1,
 										DatabaseHelper.COL_MINUSES);
 							}
 						}
 						else
 						{
 							updateScore("opponent", -(a.getValueAdded()));
 						}
 					}
 					// for home team...
 					else
 					{
 						if (a.getDatabaseColumn().equals(
 								DatabaseHelper.COL_TOTAL_TIME))
 						{
 							int curTime = db.getGameTime(fragmentHolder.gId);
 							int timeChange = curTime - a.getTimeSwitched();
 							// actually change who is in the game
 							db.subOut(fragmentHolder.gId, a.getPlayerId(),
 									a.getPlayerSubbedOut(), a.getValueAdded());
 							db.updateStat(fragmentHolder.gId, a.getPlayerId(), -timeChange,
 									DatabaseHelper.COL_TOTAL_TIME);
 							db.updateStat(fragmentHolder.gId, a.getPlayerSubbedOut(),
 									timeChange, DatabaseHelper.COL_TOTAL_TIME);
 							populateList();
 						}
 						else
 						{
 							db.updateStat(fragmentHolder.gId, a.getPlayerId(),
 									-(a.getValueAdded()), col);
 							if (col.equals(DatabaseHelper.COL_GOALS))
 							{
 								db.updateStat(fragmentHolder.gId, a.getPlayerId(), -1,
 										DatabaseHelper.COL_SHOTS);
 								List<PlayerDb> onFieldPlayers = db
 										.getOnFieldPlayersFromGame(fragmentHolder.gId);
 								for (int i = 0; i < onFieldPlayers.size(); i++)
 								{
 									db.updateStat(fragmentHolder.gId, onFieldPlayers.get(i)
 											.getPlayerId(),
 											-(a.getValueAdded()),
 											DatabaseHelper.COL_PLUSSES);
 								}
 								updateScore("homeTeam", -10);
 							}
 							else if (col.equals(DatabaseHelper.COL_SNITCHES))
 							{
 								updateScore("homeTeam", -30);
 							}
 						}
 					}
 					fragmentHolder.redoStack.push(a);
 					// Clear the stack on certain undos
 				}
 			}
 		});
 
 		redoButton.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				if (fragmentHolder.redoStack.size() == 0)
 				{
 					Toast toast = Toast.makeText(getActivity(),
 							"Nothing to Redo", Toast.LENGTH_SHORT);
 					toast.show();
 				}
 				else
 				{
 					redoAction();
 				}
 			}
 		});
 
 	}
 
 	public void opponentScored()
 	{
 		List<PlayerDb> onFieldPlayers = db.getOnFieldPlayersFromGame(fragmentHolder.gId);
 		for (int i = 0; i < onFieldPlayers.size(); i++)
 		{
 			db.updateStat(fragmentHolder.gId, onFieldPlayers.get(i).getPlayerId(), 1,
 					DatabaseHelper.COL_MINUSES);
 		}
 		Action toAdd = new Action();
 		toAdd.setDatabaseColumn(DatabaseHelper.COL_AWAY_SCORE);
 		toAdd.setGameId(fragmentHolder.gId);
 		toAdd.setPlayerId("AWAY");
 		toAdd.setValueAdded(10);// because I'll want to subtract this
 		fragmentHolder.undoStack.add(toAdd);
 
 		updateScore("opponent", 10);
 	}
 
 	public void opponentSnitch()
 	{
 		updateScore("opponent", 30);
 		Action toAdd = new Action();
 		toAdd.setDatabaseColumn("away_snitch");
 		toAdd.setGameId(fragmentHolder.gId);
 		toAdd.setPlayerId("AWAY");
 		toAdd.setValueAdded(30);// because I'll want to subtract this
 		fragmentHolder.undoStack.add(toAdd);
 	}
 
 	public void homeSnitch()
 	{
 		// assign to a player
 		// launch a list of players
 		AlertDialog.Builder snitchBuilder = snitchDialog(fragmentHolder.gId);
 		snitchBuilder.show();
 	}
 
 	public void homeStat(String playerId, String statColumn)
 	{
 		db.updateStat(fragmentHolder.gId, playerId, 1, statColumn);
 		Action toAdd = new Action();
		toAdd.setDatabaseColumn(statColumn);
 		toAdd.setGameId(fragmentHolder.gId);
 		toAdd.setPlayerId(playerId);
 		toAdd.setValueAdded(1);// because I'll want to subtract this
 		fragmentHolder.undoStack.add(toAdd);
 		if (statColumn.equals(DatabaseHelper.COL_GOALS))
 		{
 			db.updateStat(fragmentHolder.gId, playerId, 1, DatabaseHelper.COL_SHOTS);
 			List<PlayerDb> onFieldPlayers = db
 					.getOnFieldPlayersFromGame(fragmentHolder.gId);
 			for (int i = 0; i < onFieldPlayers.size(); i++)
 			{
 				db.updateStat(fragmentHolder.gId, onFieldPlayers.get(i).getPlayerId(), 1,
 						DatabaseHelper.COL_PLUSSES);
 			}
 			updateScore("homeTeam", 10);
 		}
 		else if (statColumn.equals(DatabaseHelper.COL_SNITCHES))
 		{
 			db.updateScore(fragmentHolder.gId, "homeTeam", 30);
 		}
 	}
 
 	public void redoAction()
 	{
 		Action a = fragmentHolder.redoStack.pop();
 		// for a substitution
 		if (a.getDatabaseColumn().equals(DatabaseHelper.COL_TOTAL_TIME))
 		{
 			int curTime = db.getGameTime(fragmentHolder.gId);
 			int timeChange = curTime - a.getTimeSwitched();
 			// actually change who is in the game
 			db.subOut(fragmentHolder.gId, a.getPlayerSubbedOut(), a.getPlayerId(),
 					a.getValueAdded());
 			db.updateStat(fragmentHolder.gId, a.getPlayerId(), timeChange,
 					DatabaseHelper.COL_TOTAL_TIME);
 			db.updateStat(fragmentHolder.gId, a.getPlayerSubbedOut(), -timeChange,
 					DatabaseHelper.COL_TOTAL_TIME);
 			populateList();
 			fragmentHolder.undoStack.push(a);
 		}
 		if (a.getPlayerId().equals("AWAY"))
 		{
 			if (a.getDatabaseColumn().equals(DatabaseHelper.COL_AWAY_SCORE))
 			{
 				opponentScored();
 				return;
 			}
 			else
 				// caught snitch
 			{
 				opponentSnitch();
 				return;
 			}
 		}
 		// Something we did...
 		if (a.getDatabaseColumn().equals("home_snitch"))
 		{
 			homeSnitch();
 			return;
 		}
 		homeStat(a.getPlayerId(), a.getDatabaseColumn());
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id)
 	{
 		if (c != null)
 		{
 			c.moveToPosition(position);
 			AlertDialog.Builder statsBuilder = statsDialog(fragmentHolder.gId,
 					c.getString(c.getColumnIndex(DatabaseHelper.COL_ID)));
 			statsBuilder.show();
 		}
 	}
 
 	Builder statsDialog(final String gId, final String pId)
 	{
 		// setup the dialog
 		LayoutInflater dialogFactory = LayoutInflater.from(getActivity());
 		final View addStatView = dialogFactory.inflate(
 				R.layout.stat_choice_dialog, null);
 		final AlertDialog.Builder statsBuilder = new AlertDialog.Builder(
 				getActivity());
 
 		statsBuilder.setView(addStatView);
 		statsBuilder.setTitle("Action");
 		statsBuilder.setCancelable(true);
 
 		CharSequence[] items = { "Shot", "Goal", "Assist", "Steal", "Turnover",
 				"Save"};
 
 		statsBuilder.setItems(items, new DialogInterface.OnClickListener()
 		{
 
 			@Override
 			public void onClick(DialogInterface dialog, int which)
 			{
 				if (which == 0)
 				{
 					homeStat(pId, DatabaseHelper.COL_SHOTS);
 
 				}
 				else if (which == 1)
 				{
 					homeStat(pId, DatabaseHelper.COL_GOALS);
 				}
 				else if (which == 2)
 				{
 					homeStat(pId, DatabaseHelper.COL_ASSISTS);
 				}
 				else if (which == 3)
 				{
 					homeStat(pId, DatabaseHelper.COL_STEALS);
 				}
 				else if (which == 4)
 				{
 					homeStat(pId, DatabaseHelper.COL_TURNOVERS);
 				}
 				else if (which == 5)
 				{
 					homeStat(pId, DatabaseHelper.COL_SAVES);
 				}
 				else if (which == 6)
 				{
 					homeStat(pId, DatabaseHelper.COL_SNITCHES);
 				}
 				else
 				{
 					// do nothing
 					Log.e(TAG, "Fell through the stats tree");
 				}
 			}
 		});
 
 		return statsBuilder;
 	}
 
 	Builder subDialog(final String gId, final String playerId)
 	{
 		// setup the dialog
 		LayoutInflater dialogFactory = LayoutInflater.from(getActivity());
 		final View addSubView = dialogFactory.inflate(
 				R.layout.stat_choice_dialog, null);
 		final AlertDialog.Builder subBuilder = new AlertDialog.Builder(
 				getActivity());
 		subBuilder.setView(addSubView);
 		subBuilder.setTitle("Sub in:");
 		subBuilder.setCancelable(true);
 
 		final List<PlayerDb> list = db.getOffFieldPlayersFromGame(gId);
 		Collections.sort(list, new PlayerDb.OrderByFirstName());
 		CharSequence[] items = new String[list.size()];
 		for (int i = 0; i < list.size(); i++)
 		{
 			items[i] = list.get(i).toString();
 		}
 
 		subBuilder.setItems(items, new DialogInterface.OnClickListener()
 		{
 
 			@Override
 			public void onClick(DialogInterface dialog, int which)
 			{
 				Action subOutAct = new Action();
 				subOutAct.setDatabaseColumn(DatabaseHelper.COL_TOTAL_TIME);
 				subOutAct.setGameId(fragmentHolder.gId);
 				subOutAct.setPlayerId(list.get(which).getPlayerId());
 				subOutAct.setPlayerSubbedOut(playerId);
 				subOutAct.setTimeSwitched(db.getGameTime(fragmentHolder.gId));
 				int spotOnList = db.getListLocation(fragmentHolder.gId, playerId);
 				subOutAct.setValueAdded(spotOnList); // location WRONG!
 				if (spotOnList > 0)
 				{
 					db.subOut(fragmentHolder.gId, playerId, list.get(which).getPlayerId(),
 							spotOnList);
 				}
 				else
 				{
 					showMessage("An error has occured", "The Guardians");
 				}
 
 				fragmentHolder.undoStack.add(subOutAct);
 
 				populateList();
 
 			}
 		});
 		return subBuilder;
 	}
 
 	Builder snitchDialog(final String gId)
 	{
 		// setup the dialog
 		LayoutInflater dialogFactory = LayoutInflater.from(getActivity());
 		final View addSubView = dialogFactory.inflate(
 				R.layout.stat_choice_dialog, null);
 		final AlertDialog.Builder snitchBuilder = new AlertDialog.Builder(
 				getActivity());
 		snitchBuilder.setView(addSubView);
 		snitchBuilder.setTitle("Who caught the snitch?");
 		snitchBuilder.setCancelable(true);
 
 		final List<PlayerDb> list = db.getOffFieldPlayersFromGame(gId);
 		Collections.sort(list, new PlayerDb.OrderByFirstName());
 		CharSequence[] items = new String[list.size()];
 		for (int i = 0; i < list.size(); i++)
 		{
 			items[i] = list.get(i).toString();
 		}
 
 		snitchBuilder.setItems(items, new DialogInterface.OnClickListener()
 		{
 
 			@Override
 			public void onClick(DialogInterface dialog, int which)
 			{
 				updateScore("homeTeam", 30);
 				Action toAdd = new Action();
 				toAdd.setDatabaseColumn(DatabaseHelper.COL_SNITCHES);
 				toAdd.setGameId(fragmentHolder.gId);
 				toAdd.setPlayerId(list.get(which).getPlayerId());
 				toAdd.setValueAdded(1);
 				db.updateStat(fragmentHolder.gId, list.get(which).getPlayerId(), 1,
 						DatabaseHelper.COL_SNITCHES);
 				fragmentHolder.undoStack.add(toAdd);
 
 			}
 		});
 		return snitchBuilder;
 	}
 
 	public void populateList()
 	{
 		c = db.getOnFieldPlayersFromGameCursor(fragmentHolder.gId);
 		final CursorAdapterOnFieldList onFieldAdapter = new CursorAdapterOnFieldList(
 				fragmentHolder.mContext, c, 0);
 		if (onFieldAdapter.getCount() == 0)
 		{
 			displayPopup("You should activiate some players");
 		}
 		setListAdapter(onFieldAdapter);
 	}
 
 	public void showMessage(String name, String action)
 	{
 		String str = action + " by " + name;
 		Toast toast = Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT);
 		toast.show();
 	}
 
 	public void switchTime(final View v)
 	{
 		if (fragmentHolder.running == false)
 		{
 			timer = new Timer();
 			timer.scheduleAtFixedRate(new TimerTask()
 			{
 				@Override
 				public void run()
 				{
 					// update the game time, and the view
 					final TextView clock = (TextView) v;
 					//Countdown latch on this, I think?
 					db.updateTime(fragmentHolder.gId, 1); // update every second
 					//Unlock?
 					int totalSeconds = db.getGameTime(fragmentHolder.gId);
 					int minutes = totalSeconds / 60;
 					int seconds = totalSeconds % 60;
 					String gameTime = minutes + ":" + seconds;
 					if (seconds < 10)
 					{
 						gameTime = minutes + ":0" + seconds;
 					}
 					// add one second to every player on the field
 					List<PlayerDb> players = db
 							.getOnFieldPlayersFromGame(fragmentHolder.gId);
 					for (int i = 0; i < players.size(); i++)
 					{
 						db.updateStat(fragmentHolder.gId, players.get(i).getPlayerId(), 1,
 								DatabaseHelper.COL_TOTAL_TIME);
 					}
 					final String gT = gameTime; // cause it needs to be final
 					getActivity().runOnUiThread(new Runnable()
 					{
 						@Override
 						public void run()
 						{
 							clock.setText(gT);
 						}
 					});
 				}
 
 			}, 0, 1000);
 			fragmentHolder.running = true;
 		}
 		else
 		{
 			timer.cancel();
 			fragmentHolder.running = false;
 		}
 	}
 
 	public void updateTime(int location)
 	{
 		int totalSeconds = db.getGameTime(fragmentHolder.gId);
 		int timeOnField = totalSeconds - fragmentHolder.timeSubbedIn[location];
 	}
 
 	public void onPause()
 	{
 		super.onPause();
 		// stop clock
 		// updateTimeStat
 	}
 
 	public void displayTime(int time, TextView timeTV)
 	{
 		int minutes = time / 60;
 		int seconds = time % 60;
 		String gameTime = minutes + ":" + seconds;
 		if (seconds < 10)
 		{
 			gameTime = minutes + ":0" + seconds;
 		}
 		if (minutes != 0 || seconds != 0)
 		{
 			timeTV.setText(gameTime);
 		}
 	}
 
 	public void updateScore(String who, int howMany)
 	{
 		GameDb curGame = db.getGameInfo(fragmentHolder.gId);
 		if (who.equals("opponent"))
 		{
 			db.updateScore(fragmentHolder.gId, "opponent", curGame.getAwayScore() + howMany);
 			awayScore.setText((curGame.getAwayScore() + howMany) + "");
 		}
 		else
 		{
 			db.updateScore(fragmentHolder.gId, "homeTeam", curGame.getHomeScore() + howMany);
 			homeScore.setText((curGame.getHomeScore() + howMany) + "");
 		}
 	}
 
 	public void showSubDialog(int pos)
 	{
 		if (c != null)
 		{
 			c.moveToPosition(pos);
 			AlertDialog.Builder subBuilder = subDialog(fragmentHolder.gId,
 					c.getString(c.getColumnIndex(DatabaseHelper.COL_ID)));
 			subBuilder.show();
 		}
 	}
 
 	public void displayPopup(String textToShow)
 	{
 		Toast.makeText(fragmentHolder.mContext, textToShow, Toast.LENGTH_LONG).show();
 	}
 }

 /*
         Keep Score: keep track of player scores during a card game.
         Copyright (C) 2009 Michael Elsdörfer <http://elsdoerfer.name>
 
         This program is free software: you can redistribute it and/or modify
         it under the terms of the GNU General Public License as published by
         the Free Software Foundation, either version 3 of the License, or
         (at your option) any later version.
 
         This program is distributed in the hope that it will be useful,
         but WITHOUT ANY WARRANTY; without even the implied warranty of
         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
         GNU General Public License for more details.
 
         You should have received a copy of the GNU General Public License
         along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.elsdoerfer.keepscore;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.ContextMenu;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.SimpleCursorAdapter.ViewBinder;
 
 public class Setup extends Activity {
 
 	// views
 	protected ListView mExistingPlayersList;
 	protected EditText mNewPlayerNameText;
 	protected Button mAddNewPlayerOrStartButton;
 	protected LinearLayout mExistingSessionsPanel;
 	protected ListView mExistingSessionsList;
 
 	// menu items
 	public static final int CLEAR_PLAYERS_ID = Menu.FIRST;
 	public static final int CONTINUE_GAME_ID = Menu.FIRST + 1;
 	public static final int DELETE_GAME_ID = Menu.FIRST + 2;
 	public static final int CLEAR_GAMES_ID = Menu.FIRST + 3;
 	protected MenuItem mClearPlayersItem;
 	protected MenuItem mDeleteGameItem;
 	protected MenuItem mClearGamesItem;
 
 	DbAdapter mDb = new DbAdapter(this);
 
 	// list objects/data
 	protected ArrayList<String> mListOfPlayersArray;
 	protected ArrayAdapter<String> mListOfPlayersAdapter;
 	protected SimpleCursorAdapter mExistingSessionsAdapter;
 
 	// storage keys
 	public static final String LIST_OF_PLAYERS_KEY = "players";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.setup);
 
 		// open database
 		mDb = new DbAdapter(this);
 		mDb.open();
 
 		// get views
 		mExistingPlayersList = (ListView)findViewById(R.id.existing_players);
 		mNewPlayerNameText = (EditText)findViewById(R.id.new_player_name);
 		mAddNewPlayerOrStartButton = (Button)findViewById(R.id.add_new_player_or_start);
 		mExistingSessionsPanel = (LinearLayout)findViewById(R.id.existing_sessions);
 		mExistingSessionsList = (ListView)findViewById(R.id.existing_sessions_list);
 
 		// prepare the list of players for a new session
 		mListOfPlayersArray = savedInstanceState != null
 		? savedInstanceState.getStringArrayList(LIST_OF_PLAYERS_KEY)
 				: new ArrayList<String>();
 		mListOfPlayersAdapter = new ArrayAdapter<String>(
 				this, R.layout.player_list_item, mListOfPlayersArray);
 		mExistingPlayersList.setAdapter(mListOfPlayersAdapter);
 
 		// prepare the list of existing sessions
 		final Cursor existingSessionListCursor = mDb.fetchAllSessions();
 		startManagingCursor(existingSessionListCursor);
 		mExistingSessionsAdapter =
 			new SimpleCursorAdapter(
 					this, R.layout.session_list_item,
 					existingSessionListCursor,
 					new String[] { DbAdapter.SESSION_LABEL_VKEY, DbAdapter.SESSION_LAST_PLAYED_AT_KEY },
 					new int[] { android.R.id.text1, android.R.id.text2 });
 		mExistingSessionsAdapter.setViewBinder(new ViewBinder() {
 			@Override
 			public boolean setViewValue(View view, Cursor cursor, int columnIndex)  {
 				int lastPlayedIndex = cursor.getColumnIndex(DbAdapter.SESSION_LAST_PLAYED_AT_KEY);
 				if (columnIndex == lastPlayedIndex) {
 					long now = new Date().getTime() / 1000;
 					long lastPlayed = cursor.getLong(lastPlayedIndex) / 1000;
 
 					// This code was adapted from http://code.google.com/p/connectbot/
 					String nice = getString(R.string.never);
 					if (lastPlayed > 0) {
 						int minutes = (int)((now - lastPlayed) / 60);
 						if (minutes >= 60) {
 							int hours = (minutes / 60);
 							if (hours >= 24) {
 								int days = (hours / 24);
 								if (days > 30) {
 									nice = new SimpleDateFormat("dd. MMM yyyy, HH:mm").
 									format(new Date(lastPlayed));
 								}
 								else
 									nice = getString(R.string.bind_days, days);
 							}
 							else
 								nice = getString(R.string.bind_hours, hours);
 						}
 						else if (minutes == 0)
 							nice = getString(R.string.just_now);
 						else
 							nice = getString(R.string.bind_minutes, minutes);
 					}
 
 					((TextView)view).setText(nice);
 					return true;
 				}
 				return false;
 			}
 		});
 		mExistingSessionsList.setAdapter(mExistingSessionsAdapter);
 
 		// setup event handlers - we need to refer to the context in some of them
 		final Context context = this;
 
 		this.registerForContextMenu(mExistingSessionsList);
 
 		mNewPlayerNameText.setOnKeyListener(new View.OnKeyListener() {
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if (keyCode == KeyEvent.KEYCODE_ENTER) {
 					// For now, ENTER cannot start a game, only add a new
 					// player. First, we don't want it to happen by
 					// accident. Second, for now for some reason pressing
 					// ENTER would always start a new game, if we were to
 					// simulate a click on the addNewPlayerOrStartButton
 					// here (even with a onScreen keyboard). Would need to
 					// be investigated.
 					newPlayerNameSubmit();
 					return true;
 				}
 				return false;
 			}
 		});
 		mNewPlayerNameText.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void afterTextChanged(Editable s) {
 				updateAddPlayerOrStartButton();
 			}
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {}
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {}
 		});
 
 		mAddNewPlayerOrStartButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// "add a new player" mode
 				if (!addPlayerOrStartButtonIsStartMode()) {
 					newPlayerNameSubmit();
 				}
 
 				// "start the game" mode
 				else {
 					long newId = mDb.createSession((String[]) mListOfPlayersArray.toArray(new String[0]));
 					existingSessionListCursor.requery();
 					continueSession(newId);
 
 					// TODO: The user will still see how the interface resets,
 					// while the new activity is being loaded - not particularly
 					// nice. Do something about it.
 					mNewPlayerNameText.setText("");
 					mListOfPlayersAdapter.clear();
 					updateUI();
 				}
 			}
 		});
 
 		mExistingPlayersList.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
 				final String selectedPlayer = mListOfPlayersAdapter.getItem(position);
 				new AlertDialog.Builder(context)
 				.setIcon(android.R.drawable.ic_dialog_alert)
 				.setTitle(getResources().getString(R.string.confirm_rm_player, selectedPlayer))
 				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						mListOfPlayersAdapter.remove(selectedPlayer);
 						updateUI();
 					}
 				})
 				.setNegativeButton(R.string.no, null)
 				.create().show();
 			}
 		});
 
 		mExistingSessionsList.setOnItemSelectedListener(new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view,
 					int position, long id) {
 				sessionListSelectionChanged();
 			}
 			@Override
 			public void onNothingSelected(AdapterView<?> parent) {
 				sessionListSelectionChanged();
 			}
 		});
 
 		mExistingSessionsList.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				continueSession(id);
 			}
 		});
 
 		// initial update
 		updateUI();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		mDb.close();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putStringArrayList(LIST_OF_PLAYERS_KEY, mListOfPlayersArray);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		mClearPlayersItem = menu.add(0, CLEAR_PLAYERS_ID, 0, R.string.clear_players);
 		mClearPlayersItem.setIcon(R.drawable.ic_menu_close_clear_cancel);
 		mDeleteGameItem = menu.add(0, DELETE_GAME_ID, 0, R.string.delete_session);
 		mDeleteGameItem.setIcon(R.drawable.ic_menu_delete);
 		mClearGamesItem = menu.add(0, CLEAR_GAMES_ID, 0, R.string.clear_sessions);
 		mClearGamesItem.setIcon(R.drawable.ic_menu_close_clear_cancel);
 		// setup initial visibilities
 		updateUI();
 		sessionListSelectionChanged();
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case DELETE_GAME_ID:
 			deleteSession(mExistingSessionsList.getSelectedItemId());
 			return true;
 		case CLEAR_GAMES_ID:
 			new AlertDialog.Builder(this)
 			.setIcon(android.R.drawable.ic_dialog_alert)
 			.setTitle(R.string.clear_sessions)
 			.setMessage(R.string.confirm_rm_all_sessions)
 			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 					mDb.clearSessions();
 					mExistingSessionsAdapter.getCursor().requery();
 					updateUI();
 				}
 			})
 			.setNegativeButton("No", null)
 			.create().show();
 			return true;
 		case CLEAR_PLAYERS_ID:
 			mListOfPlayersAdapter.clear();
 			updateUI();
 			return true;
 		}
 		return false;
 	}
 
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
 		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
 		menu.setHeaderTitle(((TextView)info.targetView.findViewById(android.R.id.text1)).getText());
 		menu.add(R.string.continue_session).setOnMenuItemClickListener(new OnMenuItemClickListener() {
 			@Override
 			public boolean onMenuItemClick(MenuItem item) {
 				continueSession(info.id);
 				return true;
 			}
 		});
 		menu.add(R.string.delete_session).setOnMenuItemClickListener(new OnMenuItemClickListener() {
 			@Override
 			public boolean onMenuItemClick(MenuItem item) {
 				deleteSession(info.id);
 				return true;
 			}
 		});
 	}
 
 	private void newPlayerNameSubmit() {
 		String playerName = mNewPlayerNameText.getText().toString().trim();
 		if (playerName.length()==0)
 			return;
 		addPlayerToNewGame(playerName);
 		// clear field for new player
 		mNewPlayerNameText.setText("");
 		mNewPlayerNameText.requestFocus();
 	}
 
 	protected void addPlayerToNewGame(String playerName) {
 		mListOfPlayersAdapter.add(playerName);
 		updateUI();
 	}
 
 	protected void continueSession(long id) {
 		Intent intent = new Intent(this, Game.class);
 		intent.putExtra(DbAdapter.SESSION_ID_KEY, id);
 		startActivity(intent);
 	}
 
 	protected void deleteSession(long id) {
 		mDb.deleteSession(id);
 		mExistingSessionsAdapter.getCursor().requery();
 		updateUI();
 	}
 
 	protected void sessionListSelectionChanged() {
 		if (mDeleteGameItem!=null)
 			mDeleteGameItem.setEnabled(mExistingSessionsList.getSelectedItem() != null);
 	}
 
 	/**
 	 * Returns true if the button is in start mode, false otherwise.
 	 */
 	protected boolean addPlayerOrStartButtonIsStartMode() {
 		// allow to start a new game only if min. 2 players
 		return (mNewPlayerNameText.getText().toString().length() == 0 &&
 				mListOfPlayersAdapter.getCount() >= 2);
 	}
 
 	protected void updateAddPlayerOrStartButton() {
 		Drawable drawable = null;
 		if (addPlayerOrStartButtonIsStartMode())
 			drawable = getResources().getDrawable(R.drawable.ic_menu_play_clip_small);
 		else
 			drawable = getResources().getDrawable(R.drawable.ic_menu_add_small);
 		mAddNewPlayerOrStartButton.setCompoundDrawablesWithIntrinsicBounds(
 				drawable, null, null, null);
 	}
 
 	protected void updateUI() {
 		// Hide "existing session" list once the user starts to add
 		// players for a new game. This is mostly for layout reasons,
 		// because we apparently can't really have two lists in the
 		// same screen unless both are fixed height (the first
 		// list would push elements below it out of the screen) (*).
 		//
 		// So we basically hide the session list when the player
 		// starts to use the player list.
 		//
 		// (*) We could possible work with a parent ScrollView and
 		// making both lists wrap_content, i.e. the whole screen
 		// would scroll, through both lists and the controls in
 		// between. This wouldn't make for very good user interface
 		// though, since the user would be responsible to scrolling
 		// the "player name" TextEdit into view when he wants to use it.
 		if (!mListOfPlayersAdapter.isEmpty()) {
 			LinearLayout.LayoutParams params;
 			params = (LinearLayout.LayoutParams) mExistingPlayersList.getLayoutParams();
 			params.weight = 1;
 			mExistingPlayersList.setLayoutParams(params);
 
 			mExistingSessionsPanel.setVisibility(View.GONE);
 		} else {
 			LinearLayout.LayoutParams params;
 			params = (LinearLayout.LayoutParams) mExistingPlayersList.getLayoutParams();
 			params.weight = 0;
 			mExistingPlayersList.setLayoutParams(params);
 
 			// Also hide the whole sessions panel if there aren't any sessions
 			if (mExistingSessionsAdapter.isEmpty())
 				mExistingSessionsPanel.setVisibility(View.GONE);
 			else
 				mExistingSessionsPanel.setVisibility(View.VISIBLE);
 		}
 
 		// Show/hide/enable menu items depending on the features
 		// and controls  currently visible.
 		if (mDeleteGameItem != null) {   // Menu might not have been created yet
 			boolean editingPlayers = !mListOfPlayersAdapter.isEmpty();
 			boolean sessionsExist = !mExistingSessionsAdapter.isEmpty();
 			mDeleteGameItem.setVisible(!editingPlayers);
 			mClearGamesItem.setVisible(!editingPlayers);
 			mClearGamesItem.setEnabled(sessionsExist);
 			mClearPlayersItem.setVisible(editingPlayers);
 		}
 
 		updateAddPlayerOrStartButton();
 	}
 }

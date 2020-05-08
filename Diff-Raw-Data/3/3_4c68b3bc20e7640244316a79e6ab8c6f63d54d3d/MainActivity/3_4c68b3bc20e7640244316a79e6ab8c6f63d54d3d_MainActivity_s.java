 package edu.mines.alterego;
 
 import java.util.ArrayList;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 /**
  * Alter Ego
  * 
  * @author: Matt Buland, Maria Deslis, Eric Young
  * 
  *          --------------------------------------------------------------------
  *          -------- The MIT License (MIT)
  * 
  *          Copyright (c) 2013 Matt Buland, Maria Deslis, Eric Young
  * 
  *          Permission is hereby granted, free of charge, to any person
  *          obtaining a copy of this software and associated documentation files
  *          (the "Software"), to deal in the Software without restriction,
  *          including without limitation the rights to use, copy, modify, merge,
  *          publish, distribute, sublicense, and/or sell copies of the Software,
  *          and to permit persons to whom the Software is furnished to do so,
  *          subject to the following conditions:
  * 
  *          The above copyright notice and this permission notice shall be
  *          included in all copies or substantial portions of the Software.
  * 
  *          THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  *          EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  *          MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  *          NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
  *          BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
  *          ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  *          CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  *          SOFTWARE.
  *          ------------------------------------------------------------
  *          ----------------
  * 
  * @version: 0.1
  * 
  *           Release Notes:
  * 
  *           0.1: The basic functionality is *basically* there. Games and
  *           characters can be created. The remaining components will follow the
  *           same flow: have a display; click button to add things to "display"
  *           (from database); use a dialog to get input-parameters.
  */
 
 /**
  * 
  * @author mdeslis GROUP POINT DISTRIBUTION, as discussed and agreed upon by the
  *         group
  * 
  * 
  *         Matt: 1/3 Maria: 1/3 Eric: 1/3
  * 
  */
 public class MainActivity extends Activity implements View.OnClickListener,
 		ListView.OnItemClickListener {
 
 	ArrayAdapter<GameData> mGameDbAdapter;
 	ListView listView;
 	CharacterDBHelper mDbHelper;
 	Button newGameB;
 
 	// Host Game Checkbox
 	final CharSequence[] host = { "Host Game?" };
 
 	@SuppressLint("CutPasteId")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		mDbHelper = new CharacterDBHelper(this);
 		ArrayList<GameData> gamePairList = mDbHelper.getGames();
 
 		mGameDbAdapter = new ArrayAdapter<GameData>(this,
 				android.R.layout.simple_list_item_1, gamePairList);
 		ListView gameListView = (ListView) findViewById(R.id.main_game_list_view);
 		gameListView.setAdapter(mGameDbAdapter);
 		gameListView.setOnItemClickListener(this);
 
 		// Create New Game Button
 		newGameB = (Button) findViewById(R.id.main_new_game);
 
 		// Set On Click Listener for Create New Game Button
 		newGameB.setOnClickListener(this);
 
 		// Create context menu
 		listView = (ListView) findViewById(R.id.main_game_list_view);
 		registerForContextMenu(listView);
 		
 		if (!gamePairList.isEmpty()) {
 			hideCreateNewGameButton();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Main Menu Items
 		switch (item.getItemId()) {
 		case R.id.action_new_game:
 			newGameDialogue();
 			break;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v == newGameB) {
 			newGameDialogue();
 		}
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 			long id) {
 		GameData selectedGame = mGameDbAdapter.getItem(position);
 
 		Log.i("AlterEgos::MainAct::SelectGame",
 				"The game with an id " + selectedGame.getGameId()
 						+ " and a name of " + selectedGame.getGameName()
 						+ " was selected.");
 
 		Intent launchGame = new Intent(view.getContext(), GameActivity.class);
 		launchGame.putExtra((String) getResources().getText(R.string.gameid),
 				selectedGame.getGameId());
 
 		MainActivity.this.startActivity(launchGame);
 	}
 
 	// Opens up dialogue for user to input new game
 	public void newGameDialogue() {
 		AlertDialog.Builder newGameDialog = new AlertDialog.Builder(this);
 		LayoutInflater inflater = getLayoutInflater();
 		// Inflate the view
 		newGameDialog
 				.setTitle("Create New Game")
 				.setView(inflater.inflate(R.layout.new_game_dialog, null))
 				.setPositiveButton(R.string.create,
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int id) {
 								// Perceive this dialog as an AlertDialog
 								AlertDialog thisDialog = (AlertDialog) dialog;
 
 								EditText nameInput = (EditText) thisDialog
 										.findViewById(R.id.game_name);
 								String gameName = nameInput.getText()
 										.toString();
 								// Create a new game
 								Log.i("AlterEgos::MainAct::NewGame",
 										"Creating a game with the name "
 												+ gameName);
 
 								CheckBox hostingCheck = (CheckBox) thisDialog
 										.findViewById(R.id.hosting);
 								int hosting = hostingCheck.isChecked() ? 1 : 0;
 
 								// CharacterDBHelper mDbHelper = new
 								// CharacterDBHelper(this);
 								if (gameName.equals("")) {
 									Toast createGame = Toast.makeText(MainActivity.this, "Required: Game Name", Toast.LENGTH_SHORT);
 									createGame.show();
 								} else {
 									GameData newGame = mDbHelper.addGame(gameName,
 											hosting);
 									mGameDbAdapter.add(newGame);
 									hideCreateNewGameButton();
									attentionDialogue();
 								}
 							}
 						})
 				.setNegativeButton(R.string.cancel,
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int id) {
 								// Cancel: Just close the dialog
 								dialog.dismiss();
 							}
 						});
 
 		newGameDialog.create().show();
 	}
 
 	public void editGameDialogue(final int game_id) {
 		AlertDialog.Builder editGameDialog = new AlertDialog.Builder(this);
 		LayoutInflater inflater = getLayoutInflater();
 
 		// Inflate the view
 		editGameDialog
 				.setTitle("Edit Game")
 				.setView(inflater.inflate(R.layout.edit_game_dialog, null))
 				.setPositiveButton(R.string.new_edit,
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int id) {
 								// Perceive this dialog as an AlertDialog
 								AlertDialog thisDialog = (AlertDialog) dialog;
 
 								EditText nameInput = (EditText) thisDialog
 										.findViewById(R.id.new_game_name);
 								String name = nameInput.getText().toString();
 								if (name.equals("")) {
 									Toast editGame = Toast.makeText(MainActivity.this, "Required: Game Name", Toast.LENGTH_SHORT);
 									editGame.show();
 								} else {
 									mDbHelper.updateGame(game_id, name);
 									mGameDbAdapter.clear();
 									mGameDbAdapter.addAll(mDbHelper.getGames());
 									Toast editGame = Toast.makeText(MainActivity.this, "Game Edited", Toast.LENGTH_SHORT);
 									editGame.show();
 								}
 							}
 						})
 				.setNegativeButton(R.string.new_cancel,
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int id) {
 								// Cancel: Just close the dialog
 								dialog.dismiss();
 							}
 						});
 
 		editGameDialog.create().show();
 	}
 
 	public void hideCreateNewGameButton() {
 		newGameB.setVisibility(View.GONE);
 	}
 
 	// Long Press Menu
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		if (v.getId() == R.id.main_game_list_view) {
 			MenuInflater inflater = getMenuInflater();
 			inflater.inflate(R.menu.context_menu, menu);
 		}
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		switch (item.getItemId()) {
 			case R.id.context_edit:
 				editGameDialogue(mGameDbAdapter.getItem(info.position).getGameId());
 				return true;
 			case R.id.context_delete:
 				mDbHelper.deleteGame(mGameDbAdapter.getItem(info.position)
 						.getGameId());
 				mGameDbAdapter.remove(mGameDbAdapter.getItem(info.position));
 				showToast("Game Deleted");
 				return true;
 			default:
 				return super.onContextItemSelected(item);
 		}
 	}
 
 	public void attentionDialogue() {
 		LayoutInflater attLI = LayoutInflater.from(this);
 		final View aV = attLI.inflate(R.layout.dialog_use_settings, null);
 		AlertDialog.Builder useSettings = new AlertDialog.Builder(this);
 		useSettings.setView(aV);
 		useSettings.setTitle("REMEMBER");
 		useSettings.setCancelable(false);
 		useSettings.setNegativeButton("Got it, thanks!",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();
 
 					}
 				});
 
 		AlertDialog alert = useSettings.create();
 		alert.show();
 	}
 
 	public void showToast(String message) {
 		Toast toast = Toast.makeText(getApplicationContext(), message,
 				Toast.LENGTH_SHORT);
 		toast.show();
 	}
 }

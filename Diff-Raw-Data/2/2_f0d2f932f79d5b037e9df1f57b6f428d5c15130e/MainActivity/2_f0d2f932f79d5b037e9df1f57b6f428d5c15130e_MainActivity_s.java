 package com.zuehlke.jhp.bucamp.android.jass;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import ch.mbaumeler.jass.core.Game;
 import ch.mbaumeler.jass.core.JassEngine;
 import ch.mbaumeler.jass.core.game.PlayerToken;
 import ch.mbaumeler.jass.extended.ui.ObservableGame;
 
 import com.zuehlke.jhp.bucamp.android.jass.controller.GameController;
 import com.zuehlke.jhp.bucamp.android.jass.settings.model.JassSettings;
 import com.zuehlke.jhp.bucamp.android.jass.settings.model.Player;
 import com.zuehlke.jhp.bucamp.android.jass.settings.model.SettingsCreator;
 
 public class MainActivity extends Activity {
 
 	private static Game game;
 	private ObservableGame observableGame;
 	private GameController gameController;
 
 	private Map<PlayerToken, Player> players = new HashMap<PlayerToken, Player>();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		SharedPreferences sharedPrefs = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		JassSettings settings = SettingsCreator
 				.createFromPreferences(sharedPrefs);
 
 		if (savedInstanceState == null || game == null) {
 			game = new JassEngine().createJassGame();
 		}
 		observableGame = new ObservableGame(game);
 
 		players = new HashMap<PlayerToken, Player>();
 		List<PlayerToken> all = observableGame.getPlayerRepository().getAll();
 		players.put(all.get(0), settings.getTeam1().getPlayer1());
 		players.put(all.get(1), settings.getTeam2().getPlayer1());
 		players.put(all.get(2), settings.getTeam1().getPlayer2());
 		players.put(all.get(3), settings.getTeam2().getPlayer2());
 
 		gameController = new GameController(observableGame, players, settings);
 		observableGame.addObserver(gameController);
		gameController = new GameController(observableGame, players, settings);
		observableGame.addObserver(gameController);
 		observableGame.addObserver(new AnsageObserver(gameController
 				.getHumanPlayerToken(), this));
 	}
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		observableGame.notifyObservers();
 	}
 
 	public String getName(PlayerToken token) {
 		return players.get(token).getName();
 	}
 
 	public ObservableGame getGame() {
 		return observableGame;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		MenuItem restartMenuItem = menu.findItem(R.id.menu_item_restart);
 		if (restartMenuItem == null) {
 			return true;
 		}
 		restartMenuItem
 				.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
 
 					public boolean onMenuItemClick(MenuItem item) {
 						restartGame(item);
 						return true;
 					}
 				});
 		return true;
 	}
 
 	public void displaySettingsActivity() {
 		startActivity(new Intent(this, SetupActivity.class));
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public GameController getGameController() {
 		return gameController;
 	}
 
 	public void restartGame(MenuItem item) {
 		startActivity(new Intent(this, MainActivity.class));
 	}
 }

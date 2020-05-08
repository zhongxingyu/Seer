 package com.zuehlke.jhp.bucamp.android.jass.controller;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.os.Handler;
 import ch.mbaumeler.jass.core.card.Card;
 import ch.mbaumeler.jass.core.game.PlayerToken;
 import ch.mbaumeler.jass.extended.ai.PlayStrategy;
 import ch.mbaumeler.jass.extended.ai.simple.SimpleStrategyEngine;
 import ch.mbaumeler.jass.extended.ui.JassModelObserver;
 import ch.mbaumeler.jass.extended.ui.ObservableGame;
 import ch.mbaumeler.jass.extended.ui.ObserverableMatch.Event;
 
 import com.zuehlke.jhp.bucamp.android.jass.MainActivity;
 import com.zuehlke.jhp.bucamp.android.jass.settings.model.JassSettings;
 import com.zuehlke.jhp.bucamp.android.jass.settings.model.Player;
 
 public class GameController implements JassModelObserver {
 
 	private Timer timer = new Timer();
 	private final Handler handler = new Handler();
 	private ObservableGame game;
 	private JassSettings settings;
 	private MainActivity mainActivity;
	private Map<PlayerToken, Player> players = new HashMap<PlayerToken, Player>();
 	private Map<String, PlayStrategy> strategies = new HashMap<String, PlayStrategy>();
 
 	public GameController(ObservableGame game, MainActivity mainActivity,
 			JassSettings settings) {
 		this.game = game;
 		this.mainActivity = mainActivity;
 		this.settings = settings;
 		initPlayersMap(settings);
 	}
 
 	private void initPlayersMap(JassSettings settings) {
 		List<PlayerToken> all = this.game.getPlayerRepository().getAll();
 		players.put(all.get(0), settings.getTeam1().getPlayer1());
 		players.put(all.get(1), settings.getTeam2().getPlayer1());
 		players.put(all.get(2), settings.getTeam1().getPlayer2());
 		players.put(all.get(3), settings.getTeam2().getPlayer2());
 	}
 
 	public PlayerToken getHumanPlayerToken() {
 		return this.game.getPlayerRepository().getAll().get(0);
 	}
 
 	public void updated(Event arg0, PlayerToken arg1, Object arg2) {
 		if (this.game.getCurrentMatch().getCardsOnTable().size() == 4) {
 			if (isGameFinished()) {
 				this.mainActivity.showGameFinishedDialog();
 				return;
 			}
 		}
 		if (!this.game.getCurrentMatch().getActivePlayer()
 				.equals(getHumanPlayerToken())) {
 			this.timer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					handler.post(new Runnable() {
 						public void run() {
 							playCard();
 						}
 					});
 
 				}
 			}, settings.getPlayDelay());
 		}
 
 	}
 
 	private boolean isGameFinished() {
 		return this.game.getCurrentMatch().getScore()
 				.getPlayerScore(getHumanPlayerToken()) >= this.settings
 				.getTargetPoints()
 				|| this.game.getCurrentMatch().getScore()
 						.getOppositeScore(getHumanPlayerToken()) >= this.settings
 						.getTargetPoints();
 	}
 
 	public void playCard() {
 		PlayerToken token = this.game.getCurrentMatch().getActivePlayer();
 		PlayStrategy strategy = getStrategyForPlayerToken(token);
 		Card cardToPlay = strategy.getCardToPlay(this.game.getCurrentMatch());
 		this.game.getCurrentMatch().playCard(cardToPlay);
 	}
 
 	private PlayStrategy getStrategyForPlayerToken(PlayerToken token) {
 		String className = players.get(token).getStrategy();
 
 		if (strategies.containsKey(className)) {
 			return strategies.get(className);
 		} else {
 			PlayStrategy s = null;
 			if (className
 					.equals("ch.mbaumeler.jass.extended.ai.simple.SimpleStrategy")) {
 				s = new SimpleStrategyEngine().create();
 			} else if (className
 					.equals("ch.mbaumeler.jass.extended.ai.dummy.DummyStrategy")) {
 				s = null;
 			}
 
 			if (s == null) {
 				s = new SimpleStrategyEngine().create();
 			}
 			strategies.put(className, s);
 
 			return s;
 		}
 	}
 
 	public String getPlayerName(PlayerToken token) {
 		return players.get(token).getName();
 	}
 
 }

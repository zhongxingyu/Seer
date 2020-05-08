 package uk.co.thomasc.wordmaster.objects;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 
 import uk.co.thomasc.wordmaster.BaseGame;
 import uk.co.thomasc.wordmaster.api.ServerAPI;
 import uk.co.thomasc.wordmaster.api.UpdateAlphaRequestListener;
 import uk.co.thomasc.wordmaster.objects.callbacks.TurnAddedListener;
 
 public class Game implements UpdateAlphaRequestListener {
 
 	public static String keySegment = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApAOqKoj3zH7ADRMM9zHZkUegL8xRAoD8Qb7tl7Xz94T99y7qFiphoZ";
 
 	public static HashMap<String, Game> games = new HashMap<String, Game>();
 
 	public static Game getGame(String id) {
 		if (Game.games.containsKey(id)) {
 			return Game.games.get(id);
 		}
 		return null;
 	}
 
 	public static Game getGame(String id, User player, User opponent) {
 		if (Game.games.containsKey(id)) {
 			Game game = Game.games.get(id);
 			game.player = player;
 			game.opponent = opponent;
 			return game;
 		} else {
 			Game newGame = new Game(id, player, opponent);
 			Game.games.put(id, newGame);
 			return newGame;
 		}
 	}
 
 	public static Game getGame(String playerID, String opponentID) {
 		for (Game g : Game.games.values()) {
 			if (g.getPlayer().getPlusID().equals(playerID) && g.getOpponent() != null && g.getOpponent().getPlusID().equals(opponentID)) {
 				return g;
 			}
 		}
 		return null;
 	}
 
 	/* Properties */
 	private String gameID;
 	private User player, opponent;
 	private ArrayList<Turn> turns = new ArrayList<Turn>();
 	private int latestTurnId = -1;
 	private int oldestTurnId = Integer.MAX_VALUE;
 	private int playerScore = 0, opponentScore = 0, turnNumber = 1;
 	private String playerWord = "", opponentWord = "";
 	private boolean needsWord = true, playersTurn = false;
 	private ArrayList<TurnAddedListener> turnListeners = new ArrayList<TurnAddedListener>();
 	private long lastUpdated = 0;
 	private int alpha = 0;
 	private byte alphaStatus = 0;
 
 	/* Constructors */
 	private Game(String id, User player, User opponent) {
 		gameID = id;
 		this.player = player;
 		this.opponent = opponent;
 	}
 
 	/* Getters */
 	public String getID() {
 		return gameID;
 	}
 
 	public int getPlayerScore() {
 		return playerScore;
 	}
 
 	public int getOpponentScore() {
 		return opponentScore;
 	}
 
 	public int getTurnNumber() {
 		return turnNumber;
 	}
 
 	public String getPlayerWord() {
 		return playerWord;
 	}
 
 	public String getOpponentWord() {
 		return opponentWord;
 	}
 
 	public ArrayList<Turn> getTurns() {
 		return turns;
 	}
 
 	public boolean needsWord() {
 		return needsWord;
 	}
 
 	public boolean isPlayersTurn() {
 		return playersTurn;
 	}
 
 	public User getPlayer() {
 		return player;
 	}
 
 	public User getOpponent() {
 		return opponent;
 	}
 
 	public long getLastUpdateTimestamp() {
 		return lastUpdated;
 	}
 
 	/* Setters */
 	public void setPlayerWord(String word) {
 		playerWord = word;
 	}
 
 	public void setOpponentWord(String word) {
 		opponentWord = word;
 	}
 
 	public void setScore(int playerScore, int opponentScore) {
 		this.playerScore = playerScore;
 		this.opponentScore = opponentScore;
 	}
 
 	public void setTurnNumber(int turnNumber) {
 		this.turnNumber = turnNumber;
 	}
 
 	public void setNeedsWord(boolean needsWord) {
 		this.needsWord = needsWord;
 	}
 
 	public void setPlayersTurn(boolean isPlayersTurn) {
 		playersTurn = isPlayersTurn;
 	}
 
 	public void setLastUpdateTimestamp(long timestamp) {
 		lastUpdated = timestamp;
 	}
 
 	/* Other Methods */
 	public void addTurn(Turn turn) {
 		turns.add(turn);
 		boolean newerTurn = false;
 		if (turn.getID() > latestTurnId) {
 			latestTurnId = turn.getID();
 			turnNumber = (turn.getTurnNum() / 2) + 1;
 			setLastUpdateTimestamp(turn.getUnixTimestamp());
 			newerTurn = true;
 
 			if (turn.getTurnNum() > 0) {
				playersTurn = turn.getUser().equals(opponent);
 				setNeedsWord(turn.getCorrectLetters() == 4);
 			}
 		}
 		if (turn.getID() < oldestTurnId) {
 			oldestTurnId = turn.getID();
 		}
 		for (TurnAddedListener l : turnListeners) {
 			l.onTurnAdded(turn, newerTurn);
 		}
 	}
 
 	public void addTurnListener(TurnAddedListener listener) {
 		turnListeners.add(listener);
 	}
 
 	public void removeTurnListener(TurnAddedListener listener) {
 		turnListeners.remove(listener);
 	}
 
 	public int getPivotLatest() {
 		return latestTurnId;
 	}
 
 	public int getPivotOldest() {
 		return oldestTurnId;
 	}
 
 	public static void saveState(Context context) {
 		for (String gameid : Game.games.keySet()) {
 			SharedPreferences prefs = context.getSharedPreferences("wordmaster.game." + gameid, Context.MODE_PRIVATE);
 			SharedPreferences.Editor editor = prefs.edit();
 			Game.games.get(gameid).updatePreferences(editor);
 			editor.commit();
 		}
 	}
 
 	public static void saveState(Bundle outState) {
 		for (String gameid : Game.games.keySet()) {
 			outState.putBundle(gameid, Game.games.get(gameid).toBundle());
 		}
 		outState.putStringArray("games", Game.games.keySet().toArray(new String[Game.games.size()]));
 	}
 
 	public static void restoreState(Bundle inState, BaseGame activityReference) {
 		String[] gameids = inState.getStringArray("games");
 		for (String gameid : gameids) {
 			Bundle gameData = inState.getBundle(gameid);
 			Game game = Game.getGame(gameid, User.getUser(gameData.getString("playerid"), activityReference), User.getUser(gameData.getString("opponentid"), activityReference));
 			game.setPlayersTurn(gameData.getBoolean("playersturn"));
 			game.setNeedsWord(gameData.getBoolean("needsword"));
 			game.setScore(gameData.getInt("playerscore"), gameData.getInt("opponentscore"));
 		}
 	}
 
 	public Bundle toBundle() {
 		Bundle bundle = new Bundle();
 		bundle.putString("playerid", player.getPlusID());
 		bundle.putString("opponentid", opponent.getPlusID());
 		bundle.putBoolean("playersturn", playersTurn);
 		bundle.putBoolean("needsword", needsWord);
 		bundle.putInt("playerscore", playerScore);
 		bundle.putInt("opponentscore", opponentScore);
 		return bundle;
 	}
 
 	private void updatePreferences(Editor editor) {
 		editor.putLong("time", getLastUpdateTimestamp());
 		editor.putString("oppname", opponent.getName());
 	}
 
 	public void setAlpha(int alpha) {
 		this.alpha = alpha;
 	}
 
 	public boolean getAlpha(int j) {
 		return ((alpha >> j) & 1) == 1;
 	}
 
 	public void updateAlpha(int id, boolean strike, BaseGame activityReference) {
 		alpha ^= 1 << id;
 		if ((alphaStatus & 1) == 0) {
 			alphaStatus |= 1;
 			ServerAPI.updateAlpha(gameID, alpha, activityReference, this);
 		} else {
 			alphaStatus |= 2;
 		}
 	}
 
 	@Override
 	public void onRequestComplete(int errorCode, BaseGame activityReference) {
 		if (errorCode != 0) {
 			alphaStatus |= 2;
 		}
 		
 		if ((alphaStatus & 2) == 2) {
 			alphaStatus = 1;
 			ServerAPI.updateAlpha(gameID, alpha, activityReference, this);
 		} else {
 			alphaStatus = 0;
 		}
 	}
 
 	public void clearTurns() {
 		turns.clear();
 		latestTurnId = -1;
 		oldestTurnId = Integer.MAX_VALUE;
 	}
 
 }

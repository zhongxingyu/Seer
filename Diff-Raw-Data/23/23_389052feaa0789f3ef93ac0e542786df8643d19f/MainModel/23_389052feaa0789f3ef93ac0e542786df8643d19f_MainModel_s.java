 package model;
 
 import java.util.HashMap;
 import java.util.Observable;
 
 import command.CommandFactory;
 
 import model.core.Difficulty;
 import model.core.PlayerIndex;
 
 /**
  * The top-level game model which holds overall game state
  */
 public class MainModel extends Observable {
 
 	public static final int SCREEN_WIDTH = 1180;
 	public static final int SCREEN_HEIGHT = 700;
 	
 	private int highScore;
 	private HashMap<PlayerIndex, String> playerNames;
 	private GameState activeGameState;
 	private GameConfiguration gameConfig;
 	private Difficulty activeDifficulty;
 
 	public MainModel() {
 		this.setGameConfig(new GameConfiguration());
 		this.playerNames = new HashMap<PlayerIndex, String>();
 		this.setHighScore(0);
 	}
 	
 	public void setPlayerName(PlayerIndex idx, String name) {
 		this.playerNames.put(idx, name);
 	}
 	
 	public String getPlayerName(PlayerIndex idx) {
 		return this.playerNames.get(idx);
 	}
 
 	public int getHighScore() {
 		return highScore;
 	}
 
 	public void setHighScore(int highScore) {
 		this.highScore = highScore;
 		
 		this.notifyObservers();
 	}
 
 	public GameState getActiveGameState() {
 		return activeGameState;
 	}
 
 	public void setActiveGameState(GameState activeGameState) {
 		this.activeGameState = activeGameState;
 
 		this.notifyObservers();
 	}
 
 	public GameConfiguration getGameConfig() {
 		return gameConfig;
 	}
 
 	public void setGameConfig(GameConfiguration gameConfig) {
 		this.gameConfig = gameConfig;
 	}
 
 	public Difficulty getActiveDifficulty() {
 		return activeDifficulty;
 	}
 
 	public void setActiveDifficulty(Difficulty activeDifficulty) {
 		this.activeDifficulty = activeDifficulty;
 	}
 
 }

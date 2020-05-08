 package blueMaggot;
 
 import java.awt.Dimension;
 import java.io.File;
 import java.util.ArrayList;
 
 import entity.Tank;
 
 public class GameState {
 
 	private boolean paused = false;
 	private boolean gameOver = false;
 	private boolean running = false;
 	
 	private int playerNumber;
 
	private String player1Nick = "Player1";
 	private String player2Nick = "Player2";
 
 	private boolean isHost = false;
 	private final String defaultTerrainPath = "./lvl/Cityscape_terrain.png";
 	private final String defaultBackgroundPath = "./lvl/Cityscape_background.png";
 	
 	private File selectedLevelTerrain = new File(defaultTerrainPath);
 	private File selectedLevelBackground = new File(defaultBackgroundPath);
 
 	
 
 	private int width = 1280;
 	private int height = 720;
 	public Dimension dimension = new Dimension(width, height);
 
 	// customizable player variables!
 	ArrayList<Tank> players;
 
 	public String hostIp;
 
 	private static GameState instance = null;
 
 	public static GameState getInstance() {
 		
 		if (instance == null) {
 			instance = new GameState();
 		}
 		return instance;
 	}
 
 	private GameState() {
 	}
 	
 	public void init(){
 		players = new ArrayList<Tank>();
 		gameOver = false;
 	}
 	
 	public boolean isHost() {
 		return isHost;
 	}
 
 	public void setHost(boolean isHost) {
 		this.isHost = isHost;
 	}
 
 	public File getSelectedLevelTerrain() {
 		return selectedLevelTerrain;
 	}
 
 	public void setSelectedLevelTerrain(File selectedLevelTerrain) {
 		this.selectedLevelTerrain = selectedLevelTerrain;
 	}
 
 	public File getSelectedLevelBackground() {
 		return selectedLevelBackground;
 	}
 
 	public void setSelectedLevelBackground(File selectedLevelBackground) {
 		this.selectedLevelBackground = selectedLevelBackground;
 	}
 
 	public String getHostIp() {
 		return hostIp;
 	}
 
 	public void setHostIp(String hostIp) {
 		this.hostIp = hostIp;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public Dimension getDimension() {
 		return dimension;
 	}
 
 	public void setGameOver(boolean gameOver) {
 		this.gameOver = gameOver;
 	}
 
 	public boolean isGameOver() {
 		return gameOver;
 	}
 
 	public boolean isPaused() {
 		return paused;
 	}
 
 	public void setPaused(boolean paused) {
 		this.paused = paused;
 	}
 
 	public boolean isRunning() {
 		return running;
 	}
 
 	public void setRunning(boolean running) {
 		this.running = running;
 	}
 
 	public int getPlayerNumber() {
 		return playerNumber;
 	}
 
 	public void setPlayerNumber(int playerNumber) {
 		this.playerNumber = playerNumber;
 	}
 
 	public ArrayList<Tank> getPlayers() {
 		return players;
 	}
 
 	public void setPlayers(ArrayList<Tank> players) {
 		this.players = players;
 	}
 	public String getPlayer2Nick() {
		return player1Nick;
 	}
 
 	public void setPlayer1Nick(String player1Nick) {
		this.player1Nick = player1Nick;
 	}
 
 	public String getPlayer1Nick() {
		return player2Nick;
 	}
 
 	public void setPlayer2Nick(String player2Nick) {
		this.player2Nick = player2Nick;
 	}
 	public String getDefaultTerrainPath() {
 		return defaultTerrainPath;
 	}
 
 	public String getDefaultBackgroundPath() {
 		return defaultBackgroundPath;
 	}
 }

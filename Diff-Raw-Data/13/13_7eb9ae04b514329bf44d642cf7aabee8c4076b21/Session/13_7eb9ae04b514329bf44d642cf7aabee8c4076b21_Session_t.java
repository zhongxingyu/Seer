 package game;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Comparator;
 
 import ui.render.Render;
 
 public interface Session 
 {
 	public ArrayList<String> createPlayers(int n);
 	public ArrayList<String> getPlayerIds();
 	public String getCurrentPlayerId();
 	public void setCurrentPlayer(String id);
 	public void removePlayerFollower(String id);
 	public void setPlayerFollower(String id, Follower follower);
 	public void sortPlayers(Comparator<Player> comp);
 	public boolean advancePlayer();
 	public void updatePlayer(String id);
 	public void applyForceToPlayer(String id, int fx, int fy);
 	public void addPlotToPlayer(String id, Plot plot);
 	public boolean isPlotOwnedByPlayer(String id, Plot plot);
 	public void playerSellResource(String id, String resource, int quantity, int price);
 	public void playerBuyResource(String id, String resource, int quantity, int price);
 	
 	@Deprecated
 	public Follower getPlayerFollower(String id);
 	
 	public Render getPlayerRender(String id);
 	public int getPlayerMoney(String id);
 	public void setPlayerMoney(String id, int amount);
 	public Color getPlayerColor(String id);
 	public void setPlayerColor(String id, Color color);
 	public String getPlayerName(String id);
 	public void setPlayerName(String id, String name);
 	public PlayerType getPlayerType(String id);
 	public void setPlayerType(String id, PlayerType type);
 	public int getPlayerOre(String id);
 	public int getPlayerFood(String id);
 	public int getPlayerEnergy(String id);
 	public int getPlayerCrystite(String id);
 	
 	public int getPlayerX(String id);
 	public void setPlayerX(String id, int x);
 	
 	public int getPlayerY(String id);
 	public void setPlayerY(String id, int y);
 	
 	public int getCurrentRound();
 	public void incrementRound();
 	
 	public void setMap(Map map);
 	public Plot getPlot(int x, int y);
 	
 	public void setTimer(int n);
 	public int getTimer();
 	public void incrementTimer();
 	public void decrementTimer();
 	
 	public Session getSaveCopy();
 	public void forceCopy();
 }

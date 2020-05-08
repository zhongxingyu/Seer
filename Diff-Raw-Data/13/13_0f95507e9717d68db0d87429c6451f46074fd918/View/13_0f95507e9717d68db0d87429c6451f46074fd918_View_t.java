 package riskyspace.view;
 
 import java.util.List;
 import java.util.Map;
 
 import riskyspace.logic.SpriteMapData;
 import riskyspace.model.BuildAble;
 import riskyspace.model.Colony;
 import riskyspace.model.Fleet;
 import riskyspace.model.Planet;
 import riskyspace.model.Player;
 import riskyspace.model.PlayerStats;
 import riskyspace.model.Territory;
 
 public interface View {
 	
 	public void draw();
 	public void setViewer(Player player);
 	public void setActivePlayer(Player player);
 	public void setVisible(boolean visible);
 	public boolean isVisible();
 	
 	public void updateData(SpriteMapData data);
 	public void setPlayerStats(PlayerStats stats);
 	public void setQueue(Map<Colony, List<BuildAble>> colonyQueues);
 	
 	public void showPlanet(Territory selection);
 	public void showColony(Colony selection);
 	public void showFleet(Fleet selection);
 	public void hideMenus();

	public void showGameOver(Player loser);
 }


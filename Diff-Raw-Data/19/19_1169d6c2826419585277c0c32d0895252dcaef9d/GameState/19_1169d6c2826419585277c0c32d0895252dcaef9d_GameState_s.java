 package zombiehouse;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import zombiehouse.objects.*;
 
 public class GameState {
 
	private List<Zombie>   zombies;
 	private List<Obstacle> obstacles;
 	private List<Firetrap> firetraps;
	private Player         player;
	private House          house;
	private boolean        isNextLevel;
 	
 	public GameState()
 	{
 		this.zombies = new ArrayList<Zombie>(0);
 		this.obstacles = new ArrayList<Obstacle>(0);
 		this.firetraps = new ArrayList<Firetrap>(0);
 		
 	}
 
 	
 	public List<Zombie> getZombies() {
 		return zombies;
 	}
 	public List<Obstacle> getObstacles() {
 		return obstacles;
 	}
 	public List<Firetrap> getFiretraps() {
 		return firetraps;
 	}
 	public Player getPlayer() {
 		return player;
 	}
 	public House getHouse() {
 		return house;
 	}
	public boolean isNextLevel() {
		return isNextLevel;
 	}
 
 	public void setPlayer(Player player) {
 		this.player = player;
 	}
 	public void setHouse(House house) {
 		this.house = house;
 	}
	public void setIsNextLevel(boolean isNextLevel) {
		this.isNextLevel = isNextLevel;
 	}
 }

 package infrastructure;
 
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.LinkedList;
 
import org.jbox2d.dynamics.Fixture;

 import javafx.scene.paint.Color;
 
 import entities.BouncyBall;
 import entities.Creature;
 import entities.Entity;
 import entities.Player;
 
 public class GameWorld implements Serializable {
 	private LinkedList<GameMap> maps;
 	private GameMap currentMap;
 	private Entity player;
 	private Boolean isAtDoor;
 
 	public GameWorld() {
 		player = new Player(10, 20);
 		currentMap = new GameMap(new BackGround("maps/menu.jpg"));
 		player.addToMap(currentMap);
 		maps = new LinkedList<GameMap>();
 		isAtDoor = false;
 		maps.add(currentMap);
 		currentMap.startTime();
 		currentMap.setVisible(true);
 	}
 
 	public GameMap getCurrentMap() {
 		return currentMap;
 	}
 
 	public void setPlayer(Entity entity) {
 		this.player = entity;
 	}
 
 	public Entity getPlayer() {
 		return player;
 	}
 
 	public LinkedList<GameMap> getMaps() {
 		return maps;
 	}
 
 	public void addMap(GameMap game) {
 		maps.add(game);
 	}
 
 	public void changeMap(GameMap Map) {
 		if (currentMap != null) {
 			//currentMap.reset();
 			currentMap.killTime();
 			currentMap.setVisible(false);
 		}
 		currentMap = Map;
 		App.camera.reset();
 		player.addToMap(Map);
 		currentMap.setVisible(true);
 		currentMap.startTime();
 	}
 
 	@Override
 	public String toString() {
 		String str = "";
 		Iterator iter = maps.iterator();
 		while (iter.hasNext()) {
 			str += iter.next().toString() + Parse.delim;
 		}
 		return str;
 	}
 
 	public void isAtDoor(boolean b) {
 		this.isAtDoor = b;
 	}
 	public boolean getIsAtDoor(){
 		return isAtDoor;
 	}
 }

 package infrastructure;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import entities.Creature;
 import entities.Entity;
 
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.scene.control.Button;
 
 public class GameWorld {
 	public LinkedList<GameMap> maps;
 	public GameMap currentMap;
 	public Entity player;
 	public Camera camera;
 
 	public GameWorld() {
 		player = new Creature(30, 80);
 		currentMap = new GameMap("maps/menu.jpg");
 		player.addToWorld(currentMap);
 		maps = new LinkedList<GameMap>();
 		camera = new Camera();
		//changeMap(currentMap);
 		maps.add(currentMap);
		currentMap.addElementsToGUI();
		currentMap.time.startTime();
 	}
 	public void setPlayer(Entity entity) {
 		this.player = entity;
 	}
 
 	public void changeMap(GameMap Map) {
 		if(currentMap != null){
 			currentMap.stopAll();
 		}
 		currentMap = Map;
 		player.addToWorld(Map);
 		camera.reset();
 		App.root.getChildren().removeAll(App.root.getChildren());
 		currentMap.addElementsToGUI();
 		currentMap.time.startTime();
 	}
 	public static GameWorld parse(String raw) {
 		String[] MapList = Parse.fragment(raw);
 		GameWorld game = new GameWorld();
 		for (String a : MapList) {
 			try {
 				game.addMap(GameMap.parse(Parse.readFromFile(a)));
 			} catch (IOException e) {
 				System.out
 						.println("Error: attempted to add a Map that doesn't exist");
 				e.printStackTrace();
 			}
 		}
 		return game;
 	}
 
 	public String toString() {
 		String str = "";
 		Iterator iter = maps.iterator();
 		//skip adding menu to toString()
 		iter.next();
 		while(iter.hasNext()){
 			str += iter.next().toString() + Parse.delim;
 		}
 		return str;
 	}
 
 	public void addMap(GameMap game) {
 		maps.add(game);
 	}
 }

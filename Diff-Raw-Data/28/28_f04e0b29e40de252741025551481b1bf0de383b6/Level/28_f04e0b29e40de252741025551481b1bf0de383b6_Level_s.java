 package com.vulcastudios.actors;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 
 import com.vulcastudios.TestGame;
 import com.vulcastudios.util.ResourceManager;
 
 
 public class Level {
 	private TiledMap map;
 	private String par;
 	private ResourceManager resourceManager;
 	private Player player;
 	private ArrayList<Zombie> zombies = new ArrayList<Zombie>();
 	private long startTime;
 	private long finalTime = 0;
 	private HashMap<String, Button> buttons = new HashMap<String, Button>();
 	private HashMap<String, Door> doors = new HashMap<String, Door>();
 	private ArrayList<Collidable> collidables = new ArrayList<Collidable>();
 	private End end;
 	private Start startingPoint;
 	
 	public Level(String mapName, ResourceManager resourceManager){
 		this.resourceManager = resourceManager;
 		
 		System.out.println(this.resourceManager.maps.size());
 		map = this.resourceManager.maps.get(mapName);
 		par = map.getMapProperty("par", "3");
 		
 		for(int i = 0; i < map.getObjectGroupCount(); i++){
 			for(int j = 0; j < map.getObjectCount(i); j++){
 				String name = map.getObjectName(i, j);
 				String type = map.getObjectType(i, j);
 				System.out.println(name+":"+type);
 				if(type.equals("door")){
 					String initialState = map.getObjectProperty(i, j, "initialState", "closed");
					doors.put(name, new Door(this, name, map.getObjectX(i, j), map.getObjectY(i, j), map.getObjectWidth(i, j), map.getObjectHeight(i, j), initialState));
 				} else if(type.equals("button")){
 					String activates = map.getObjectProperty(i, j, "activates", "door"+name.substring(6));
 					buttons.put(name, new Button(this, name, map.getObjectX(i, j), map.getObjectY(i, j), map.getObjectWidth(i, j), map.getObjectHeight(i, j), activates));
 				} else if (type.equals("end")) {
 					System.out.println("end");
 					end = new End(this, name, map.getObjectX(i, j), map.getObjectY(i, j), map.getObjectWidth(i, j), map.getObjectHeight(i, j));
 				}
 				else if(type.equals("collidable")){
 					collidables.add(new Collidable(name, map.getObjectX(i, j), map.getObjectY(i, j), map.getObjectWidth(i, j), map.getObjectHeight(i, j)));
 				}
 				else if(type.equals("start")){
 					startingPoint = new Start(name, map.getObjectX(i, j), map.getObjectY(i, j));
 				}
 			}
 		}
 	}
 	
 	
 	
 	public void initLevel(){
 		this.createPlayer();
 		startTime = System.currentTimeMillis();
 	}
 	
 	public TiledMap getMap(){
 		return this.map;
 	}
 	
 	public String getPar() {
 		return this.par;
 	}
 	
 	public long getStartTime() {
 		return this.startTime;
 	}
 	
 	public void render(GameContainer container, StateBasedGame game, Graphics g){
 		map.render(0, 0, 0, 0, 100, 100);
 		
 		for (Zombie zombie : zombies) {
 			zombie.render(container, g);
 		}
 
 		Set<Entry<String, Button>> buttonEntries = buttons.entrySet();
 		for(Entry<String, Button> entry : buttonEntries){
 			entry.getValue().render(container, game, g);
 		}
 		
 		Set<Entry<String, Door>> doorsEntries = doors.entrySet();
 		for(Entry<String, Door> entry : doorsEntries){
 			entry.getValue().render(container, game, g);
 		}
 		
 		player.render(container, game, g);
 		
 	}
 	
 	public void createPlayer() {
 		player = new Player(this.resourceManager, startingPoint.getX(), startingPoint.getY());
 	}
 	
 	public void initLevelWithNewZombie() {
 		for(Zombie z: zombies){
 			z.restartZombie();
 		}
 		zombies.add(new Zombie(this.resourceManager, startingPoint.getX(), startingPoint.getY(), player.getMovementMap()));
 		createPlayer();
 	}
 	
 	public void update(GameContainer container, StateBasedGame game, int delta){
 		((TestGame)game).checkEndPoint(player);
 		
 		for(Entry<String, Door> entry : doors.entrySet()){
 			entry.getValue().setOpen(entry.getValue().isInitialOpen());
 		}
 		
 		((TestGame)game).checkObjects(player);
 		for (Zombie zombie : zombies) {
 			((TestGame)game).checkObjects(zombie);
 		}
 		
 		player.update(container, game, delta);
 		
 		for (Zombie zombie : zombies) {
 			zombie.update(container, game, delta);
 		}
 		
 		if (container.getInput().isKeyPressed(Input.KEY_SPACE) || !player.isAlive()) {
 			initLevelWithNewZombie();
 		} else if (container.getInput().isKeyPressed(Input.KEY_ENTER)) {
 			restartLevel();
 		}
 	}
 	
 	public void end() {
 		
 	}
 	
 	public void restartLevel(){
 		startTime = System.currentTimeMillis();
 		createPlayer();
 		zombies.clear();
 	}
 	
 	public void setFinalTime(long finalTime) {
 		this.finalTime = finalTime;
 	}
 	
 	public long getFinalTime() {
 		return this.finalTime;
 	}
 
 	public int getNumberOfZombies(){
 		return zombies.size();
 	}
 	
 	public HashMap<String, Button> getButtons(){
 		return this.buttons;
 	}
 	
 	public HashMap<String, Door> getDoors(){
 		return this.doors;
 	}
 	
 	public ArrayList<Collidable> getCollidables(){
 		return this.collidables;
 	}
 	
 	public End getEnd() {
 		return this.end;
 	}
 	
 }

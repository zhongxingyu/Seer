 package edu.calpoly.csc.pulseman;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 
 import edu.calpoly.csc.pulseman.gameobject.Collidable;
 import edu.calpoly.csc.pulseman.gameobject.Enemy;
 import edu.calpoly.csc.pulseman.gameobject.GameObject;
 import edu.calpoly.csc.pulseman.gameobject.Goal;
 import edu.calpoly.csc.pulseman.gameobject.KillingObstacle;
 import edu.calpoly.csc.pulseman.gameobject.MovingTile;
 import edu.calpoly.csc.pulseman.gameobject.OscillateBehavior;
 import edu.calpoly.csc.pulseman.gameobject.Player;
 import edu.calpoly.csc.pulseman.gameobject.Tile;
 
 public class World {
 	public static int kPixelsPerTile = 48;
 	public static int kVectorCenter = 50;
 	
 	private static World world = new World();
 	private static String lastLevel;
 	private int lvlWidth, lvlHeight;
 	private List<Collidable> collidables = new ArrayList<Collidable>();
 	private List<GameObject> nonCollidables = new ArrayList<GameObject>();
 	private List<Enemy> enemies = new ArrayList<Enemy>();
 	private Player player;
 	private Vector2f playerSpawn = new Vector2f(0.0f, 0.0f);
 	
 	private static enum TileType {kNothing, kTile, kPlayerSpawn, kMovingTile, kEnemy,
 		kSpike, kGoal, kTimeMovingTile, kTimeEnemy, kTimeSpike};
 	private static Map<Integer, TileType> ColorMap = new HashMap<Integer, TileType>();
 	
 	static {
 		ColorMap.put(new Integer(255), TileType.kNothing);
 		ColorMap.put(new Integer(254), TileType.kPlayerSpawn);
 		ColorMap.put(new Integer(253), TileType.kTile);
 		ColorMap.put(new Integer(252), TileType.kEnemy);
 		ColorMap.put(new Integer(250), TileType.kMovingTile);
 		ColorMap.put(new Integer(249), TileType.kSpike);
 		ColorMap.put(new Integer(248), TileType.kGoal);
 		ColorMap.put(new Integer(11), TileType.kTimeMovingTile);
 		ColorMap.put(new Integer(12), TileType.kTimeEnemy);
 		ColorMap.put(new Integer(13), TileType.kTimeSpike);
 	}
 	
 	private World() {}
 	
 	public static World getWorld() {
 		return world;
 	}
 	
 	public List<Collidable> getCollidables() {
 		return collidables;
 	}
 	
 	public List<Enemy> getEnemies()
 	{
 		return enemies;
 	}
 	
 	public void loadLastLevel() throws SlickException {
 		loadLevel(lastLevel);
 	}
 	
 	public void nextLevel() {
 		try {
 			loadLevel(Main.nextLevel());
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void loadLevel(String fileName) throws SlickException {
 		lastLevel = fileName;
 		Image level = new Image(fileName);
 		int width = level.getWidth(), height = level.getHeight();
 		
 		collidables.clear();
 		nonCollidables.clear();
 		enemies.clear();
 		lvlWidth = width * kPixelsPerTile;
 		lvlHeight = height * kPixelsPerTile;
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				PixelToObject(level.getColor(x, y), x * kPixelsPerTile, y * kPixelsPerTile);
 			}
 		}
 	}
 	
 	public void render(GameContainer gc, Graphics g) {
 		player.render(gc, g);
 		for (Enemy enemy : enemies)
 		{
 			enemy.render(gc, g);
 		}
 		for (Collidable obj: collidables) {
 			obj.render(gc, g);
 		}
 		for (GameObject obj: nonCollidables) {
 			obj.render(gc, g);
 		}
 	}
 	
 	public void update(GameContainer gc, int dt, int affectedDt) {
 		for (Collidable obj: collidables) {
 			if (obj.isAffectedByPulse())
 				obj.update(gc, affectedDt);
 			else
 				obj.update(gc, dt);
 		}
 		for (GameObject obj: nonCollidables) {
 			if (obj.isAffectedByPulse())
 				obj.update(gc, affectedDt);
 			else
 				obj.update(gc, dt);
 		}
 		for (Enemy enemy : enemies)
 		{
 			if (enemy.isAffectedByPulse())
 				enemy.update(gc, affectedDt);
 			else
 				enemy.update(gc, dt);
 		}
 		player.update(gc, dt);
 	}
 	
 	private void PixelToObject(Color color, int xPos, int yPos) throws SlickException {
 		TileType type = ColorMap.get(color.getRed()); 
 		if (type == null) {
 			throw new RuntimeException("Color not found in color map, red: " + color.getRed());
 		}
 		switch(type) {
 		case kNothing:
 			break;
 		case kPlayerSpawn:
 			player = new Player(xPos, yPos);
 			break;
 		case kTile:
 			collidables.add(new Tile(xPos, yPos));
 			break;
 		case kMovingTile:
			collidables.add(new MovingTile(xPos, yPos, new OscillateBehavior(xPos, yPos, .5f, 
 					new Vector2f(kPixelsPerTile * (color.getGreen() - kVectorCenter), 
 							kPixelsPerTile * (color.getBlue() - kVectorCenter))), false));
 			break;
 		case kEnemy:
 			enemies.add(new Enemy(xPos, yPos, false));
 			break;
 		case kSpike:
 			collidables.add(new KillingObstacle("res/spike.png", xPos, yPos, 
 					new OscillateBehavior(xPos, yPos, .3f, 
 					new Vector2f(kPixelsPerTile * (color.getGreen() - kVectorCenter), 
 							kPixelsPerTile * (color.getBlue() - kVectorCenter))), false));
 			break;
 		case kGoal:
 			collidables.add(new Goal(xPos, yPos));
 			break;
 		case kTimeMovingTile:
 			collidables.add(new MovingTile(xPos, yPos, new OscillateBehavior(xPos, yPos, .3f, 
 					new Vector2f(kPixelsPerTile * (color.getGreen() - kVectorCenter), 
 							kPixelsPerTile * (color.getBlue() - kVectorCenter))), true));
 			break;
 		case kTimeEnemy:
 			enemies.add(new Enemy(xPos, yPos, false));
 			break;
 		case kTimeSpike:
 			collidables.add(new KillingObstacle("res/spike.png", xPos, yPos, 
 					new OscillateBehavior(xPos, yPos, .3f, 
 					new Vector2f(kPixelsPerTile * (color.getGreen() - kVectorCenter), 
 							kPixelsPerTile * (color.getBlue() - kVectorCenter))), true));
 			break;
 		}
 
 	}
 	
 	public Player getPlayer() {
 		return player;
 	}
 	
 	public int getLevelWidth() {
 		return lvlWidth;
 	}
 	
 	public int getLevelHeight() {
 		return lvlHeight;
 	}
 	
 	public Vector2f getPlayerSpawn() {
 		return playerSpawn;
 	}
 	
 }

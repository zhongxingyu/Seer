 package edu.calpoly.csc.pulseman;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 
 import edu.calpoly.csc.pulseman.gameobject.BackgroundObject;
 import edu.calpoly.csc.pulseman.gameobject.Collidable;
 import edu.calpoly.csc.pulseman.gameobject.Enemy;
 import edu.calpoly.csc.pulseman.gameobject.GameObject;
 import edu.calpoly.csc.pulseman.gameobject.Goal;
 import edu.calpoly.csc.pulseman.gameobject.KillingObstacle;
 import edu.calpoly.csc.pulseman.gameobject.KillingObstacle.Orientation;
 import edu.calpoly.csc.pulseman.gameobject.MovingTile;
 import edu.calpoly.csc.pulseman.gameobject.OscillateBehavior;
 import edu.calpoly.csc.pulseman.gameobject.Player;
 import edu.calpoly.csc.pulseman.gameobject.Tile;
 import edu.calpoly.csc.pulseman.gameobject.Tile.TileType;
 
 public class World
 {
 	public static int kPixelsPerTile = 48;
 	public static int kVectorCenter = 50;
 
 	private static final float kAlphaToSpeed = 1.0f;
 
 	private static World world = new World();
 	private static Camera cam;
 	private static String lastLevel;
 	private int lvlWidth, lvlHeight;
 	private List<Collidable> collidables = new ArrayList<Collidable>();
 	private List<GameObject> nonCollidables = new ArrayList<GameObject>();
 	private List<Enemy> enemies = new ArrayList<Enemy>();
 	private Player player;
 	private Vector2f playerSpawn = new Vector2f(0.0f, 0.0f);
 	private ArrayList<LevelLoadListener> levelListeners = new ArrayList<LevelLoadListener>();
 
 	private static enum BlockType
 	{
 		kNothing, kTile, kPlayerSpawn, kMovingTile, kEnemy, kSpike, kGoal, kTimeMovingTile, kTimeEnemy, kTimeSpike
 	};
 
 	private static Map<Integer, BlockType> ColorMap = new HashMap<Integer, BlockType>();
 
 	static
 	{
 		ColorMap.put(new Integer(255), BlockType.kNothing);
 		ColorMap.put(new Integer(254), BlockType.kPlayerSpawn);
 		ColorMap.put(new Integer(253), BlockType.kTile);
 		ColorMap.put(new Integer(252), BlockType.kEnemy);
 		ColorMap.put(new Integer(250), BlockType.kMovingTile);
 		ColorMap.put(new Integer(249), BlockType.kSpike);
 		ColorMap.put(new Integer(248), BlockType.kGoal);
 		ColorMap.put(new Integer(11), BlockType.kTimeMovingTile);
 		ColorMap.put(new Integer(12), BlockType.kTimeEnemy);
 		ColorMap.put(new Integer(13), BlockType.kTimeSpike);
 	}
 
 	private World()
 	{
 	}
 
 	public static World getWorld()
 	{
 		return world;
 	}
 
 	public boolean isTile(int x, int y, Image map)
 	{
 		System.out.println(x / kPixelsPerTile + " " + y / kPixelsPerTile + "\n");
 		BlockType tt = ColorMap.get(map.getColor(x / kPixelsPerTile, y / kPixelsPerTile).getRed());
 		System.out.println(tt);
 		return tt == BlockType.kTile || tt == BlockType.kMovingTile;
 	}
 
 	public TileType getTileType(int x, int y, Image map)
 	{
 		if(y - kPixelsPerTile > 0 && ColorMap.get(map.getColor(x / kPixelsPerTile, (y - kPixelsPerTile) / kPixelsPerTile).getRed()) == BlockType.kNothing)
 		{
 			return TileType.SURFACE;
 		}
 		else
 		{
 			return TileType.NORMAL;
 		}
 	}
 
 	public Orientation calcSpikeOrientation(int x, int y, Image map)
 	{
 		if(x - kPixelsPerTile > 0 && isTile(x - kPixelsPerTile, y, map))
 			return Orientation.RIGHT;
 		else if(x + kPixelsPerTile < lvlWidth && isTile(x + kPixelsPerTile, y, map))
 			return Orientation.LEFT;
 		else if(y + kPixelsPerTile < lvlHeight && isTile(x, y + kPixelsPerTile, map))
 			return Orientation.UP;
 		else if(y - kPixelsPerTile > 0 && isTile(x, y - kPixelsPerTile, map))
 			return Orientation.DOWN;
 		return Orientation.UP;
 	}
 
 	public List<Collidable> getCollidables()
 	{
 		return collidables;
 	}
 
 	public List<Enemy> getEnemies()
 	{
 		return enemies;
 	}
 
 	public void loadLastLevel() throws SlickException
 	{
 		loadLevel(lastLevel);
 	}
 
 	public void nextLevel()
 	{
 		SchemeLoader.loadScheme(GameScreen.levelToScheme[Main.getCurrentLevel() + 1]);
 		try
 		{
 			loadLevel(Main.nextLevel());
 		}
 		catch(SlickException e)
 		{
 			e.printStackTrace();
 		}
 
 	}
 
 	public void loadLevel(String fileName) throws SlickException
 	{
 		lastLevel = fileName;
 		// If the fileName is empty, then the game has been beated
 		if (fileName.length() == 0) {
 			return;
 		}
 		Image level = new Image(fileName);
 		Image[] bgs = SchemeLoader.getBackgrounds();
 		cam = new Camera(bgs);
 		int width = level.getWidth(), height = level.getHeight();
 
 		collidables.clear();
 		nonCollidables.clear();
 		enemies.clear();
 		lvlWidth = width * kPixelsPerTile;
 		lvlHeight = height * kPixelsPerTile;
 		for(int x = 0; x < width; x++)
 		{
 			for(int y = 0; y < height; y++)
 			{
 				PixelToObject(level.getColor(x, y), x * kPixelsPerTile, y * kPixelsPerTile, level);
 			}
 		}
 
 		onLevelLoaded();
 	}
 
 	public void render(GameContainer gc, Graphics g)
 	{
 		cam.render(gc, g, player);
 		player.render(gc, g);
 
 		for(GameObject obj : nonCollidables)
 		{
 			obj.render(gc, g);
 		}
 		for(Enemy enemy : enemies)
 		{
 			enemy.render(gc, g);
 		}
 		for(Collidable obj : collidables)
 		{
 			obj.render(gc, g);
 		}
 
 	}
 
 	public void update(GameContainer gc, int dt, int affectedDt)
 	{
 		for(Collidable obj : collidables)
 		{
 			if(obj.isAffectedByPulse())
 				obj.update(gc, affectedDt);
 			else
 				obj.update(gc, dt);
 		}
 		for(GameObject obj : nonCollidables)
 		{
 			if(obj.isAffectedByPulse())
 				obj.update(gc, affectedDt);
 			else
 				obj.update(gc, dt);
 		}
 		for(Enemy enemy : enemies)
 		{
 			if(enemy.isAffectedByPulse())
 				enemy.update(gc, affectedDt);
 			else
 				enemy.update(gc, dt);
 		}
 		player.update(gc, dt);
 	}
 
 	private void PixelToObject(Color color, int xPos, int yPos, Image map) throws SlickException
 	{
 		BlockType type = ColorMap.get(color.getRed());
 		Orientation orient;
 		if(type == null)
 		{
 			throw new RuntimeException("Color not found in color map, red: " + color.getRed());
 		}
 		switch(type)
 		{
 		case kNothing:
 			break;
 		case kPlayerSpawn:
 			player = new Player(xPos, yPos);
 			break;
 		case kTile:
 			collidables.add(new Tile(xPos, yPos));
 			if(getTileType(xPos, yPos, map) == TileType.SURFACE && new Random().nextBoolean())
 			{
 				Animation prop = SchemeLoader.getProp();
 				int newObjYPos = yPos - prop.getHeight();
 				nonCollidables.add(new BackgroundObject(prop, xPos, newObjYPos));
 
 			}
 			break;
 		case kMovingTile:
 			collidables.add(new MovingTile(xPos, yPos, new OscillateBehavior(xPos, yPos, kAlphaToSpeed * color.getAlpha() / 255.0f, new Vector2f(kPixelsPerTile * (color.getGreen() - kVectorCenter), kPixelsPerTile * (color.getBlue() - kVectorCenter))), false));
 			break;
 		case kEnemy:
 			enemies.add(new Enemy(xPos, yPos, false));
 			break;
 		case kSpike:
 			orient = calcSpikeOrientation(xPos, yPos, map);
 			collidables.add(new KillingObstacle("res/spike.png", xPos, yPos, new OscillateBehavior(xPos, yPos, kAlphaToSpeed * color.getAlpha() / 255.0f, new Vector2f(kPixelsPerTile * (color.getGreen() - kVectorCenter), kPixelsPerTile * (color.getBlue() - kVectorCenter))), false, orient));
 			break;
 		case kGoal:
 			collidables.add(new Goal(xPos, yPos - Goal.portalImage.getHeight() + kPixelsPerTile));
 			break;
 		case kTimeMovingTile:
 			collidables.add(new MovingTile(xPos, yPos, new OscillateBehavior(xPos, yPos, kAlphaToSpeed * color.getAlpha() / 255.0f, new Vector2f(kPixelsPerTile * (color.getGreen() - kVectorCenter), kPixelsPerTile * (color.getBlue() - kVectorCenter))), true));
 			break;
 		case kTimeEnemy:
			enemies.add(new Enemy(xPos, yPos, true));
 			break;
 		case kTimeSpike:
 			orient = calcSpikeOrientation(xPos, yPos, map);
 			collidables.add(new KillingObstacle("res/spike.png", xPos, yPos, new OscillateBehavior(xPos, yPos, kAlphaToSpeed * color.getAlpha() / 255.0f, new Vector2f(kPixelsPerTile * (color.getGreen() - kVectorCenter), kPixelsPerTile * (color.getBlue() - kVectorCenter))), true, orient));
 			break;
 		}
 
 	}
 
 	public Player getPlayer()
 	{
 		return player;
 	}
 
 	public int getLevelWidth()
 	{
 		return lvlWidth;
 	}
 
 	public int getLevelHeight()
 	{
 		return lvlHeight;
 	}
 
 	public Vector2f getPlayerSpawn()
 	{
 		return playerSpawn;
 	}
 
 	public void onLevelLoaded()
 	{
 		synchronized(levelListeners)
 		{
 			for(int i = 0; i < levelListeners.size(); ++i)
 			{
 				levelListeners.get(i).onLevelLoad();
 			}
 		}
 	}
 
 	public void addLevelLoadListener(LevelLoadListener listener)
 	{
 		synchronized(levelListeners)
 		{
 			levelListeners.add(listener);
 		}
 	}
 
 	public void removeLevelLoadListener(LevelLoadListener listener)
 	{
 		synchronized(levelListeners)
 		{
 			levelListeners.remove(listener);
 		}
 	}
 
 	interface LevelLoadListener
 	{
 		public void onLevelLoad();
 	}
 
 }

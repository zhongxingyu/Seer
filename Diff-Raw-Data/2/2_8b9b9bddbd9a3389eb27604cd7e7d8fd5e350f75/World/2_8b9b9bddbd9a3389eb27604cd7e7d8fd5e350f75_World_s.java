 package state;
 
 import java.io.FileReader;
 import java.util.ArrayDeque;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Queue;
 import java.util.Random;
 import java.util.Set;
 
 import logic.GameUpdate;
 
 /**
  * Stores everything in the game.
  */
 public class World {
 
 	private int woodResource = 200;
 	private int plantResource = 200;
 	private int crystalResource = 200;
 
 	private boolean showHealth = true;
 
 	public Queue<Task> tasks = new ArrayDeque<Task>();
 
 	private Tile[][] worldTile;
 
 	long seed = System.currentTimeMillis();
 	private Random random = new Random(seed);
 
 	private GameUpdate gameUpdate; // the current game update object to send
 									// changes to
 
 	private Set<Dude> allDudes = new HashSet<Dude>();
 	private Set<Structure> structures = new HashSet<Structure>();
 	private Set<Resource> resources;
 
 	/**
 	 * Returns a random tile name.
 	 */
 	public String generateRandomTile() {
 		int rand = random.nextInt(5);
 		if (rand == 1)
 			return "tile";
 		else if (rand == 2)
 			return "dark-sand";
 		else if (rand == 3)
 			return "barren-grass";
 		else if (rand == 4)
 			return "dark-sand";
 		else
 			return "Grass";
 	}
 
 	/**
 	 * Creates a 100x100 world with random tiles.
 	 */
 	public World() {
 		worldTile = new Tile[100][100];
 		for (int x = 0; x < 100; x++)
 			for (int y = 0; y < 100; y++) {
 				if (random.nextInt(2) == 1)
 					worldTile[x][y] = new Tile(generateRandomTile(), 0, x, y);
 				else
 					worldTile[x][y] = new Tile(generateRandomTile(), 1, x, y);
 			}
 
 		for (int x = 3; x < 100; x += 1) {
 			for (int y = 3; y < 100; y += 1) {
 				if (random.nextInt(20) == 1)
 					addStructure(new Structure(x, y, 3, 3,
 							"Assets/EnvironmentObjects/DarkTree.png"));
 			}
 		}
 		addDude(new Dude(this, 7, 7, 1, 1, "Assets/Characters/Man.png"));
 		addDude(new Dude(this, 8, 8, 1, 1, "Assets/Characters/Man.png"));
 	}
 
 	/**
 	 * Creates a world from a tile array.
 	 */
 	public World(Tile[][] tiles) {
 		worldTile = tiles;
 		resources = new HashSet<Resource>();
 		addDude(new Dude(this, 7, 7, 1, 1, "Assets/Characters/Man.png"));
 		addDude(new Dude(this, 8, 8, 1, 1, "Assets/Characters/Man.png"));
 
 	}
 
 	/**
 	 * Adds a structure to the world and returns true. If the structure can't be
 	 * placed, returns false without changing anything.
 	 */
 	public boolean addStructure(Structure s) {
 		int x = s.getX(), y = s.getY(), w = s.getWidth(), h = s.getHeight();
 
 		if (x - w < -1 || y - h < -1 || x >= getXSize() || y >= getYSize())
 			return false;
 
 		// check for overlap
 		for (int X = 0; X < w; X++)
 			for (int Y = 0; Y < h; Y++)
 				if (worldTile[x - X][y - Y].getStructure() != null) {
 					System.out.println("Cannot add structure: overlap");
 					return false; // can't have two structures on one tile
 				}
 
 		s.setWorld(this);
 
 		if(s instanceof Resource)
 			resources.add((Resource)s);
 		structures.add(s);
 
 		// place the structure
 		for (int X = 0; X < w; X++)
 			for (int Y = 0; Y < h; Y++)
 				worldTile[x - X][y - Y].setStructure(s, false);
 
 		return true;
 	}
 
 
 	public void toggleShowHealth() {
 		showHealth = !showHealth;
 	}
 
 	public boolean showHealth() {
 		return showHealth;
 	}
 
 	public void removeStructure(Structure s) {
 		int x = s.getX(), y = s.getY(), w = s.getWidth(), h = s.getHeight();
 
 		for(int X = 0; X < w; X++)
 			for(int Y = 0; Y < h; Y++)
 				worldTile[x-X][y-Y].setStructure(null, false);
 
 		if(s instanceof Resource)
 			resources.remove(s);
 		structures.remove(s);
 	}
 
 	/**
 	 * Adds a dude to the world and returns true. If the dude can't be placed,
 	 * returns false without changing anything.
 	 */
 	public boolean addDude(Dude s) {
 		int x = s.getX(), y = s.getY(), w = s.getWidth(), h = s.getHeight();
 
 		if (x - w < -1 || y - h < -1 || x >= getXSize() || y >= getYSize())
 			return false;
 
 		// check for overlap
 		for (int X = 0; X < w; X++)
 			for (int Y = 0; Y < h; Y++)
 				if (worldTile[x - X][y - Y].getDude() != null)
 					return false; // can't have two structures on one tile
 									// <--The best comment! =)
 
 		// place the structure
 		for (int X = 0; X < w; X++)
 			for (int Y = 0; Y < h; Y++)
 				worldTile[x - X][y - Y].setDude(s);
 
 		allDudes.add(s);
 
 		return true;
 	}
 
 	/**
 	 * Returns a tile at given coordinates. Throws an exception if coordinates
 	 * are invalid.
 	 */
 	public Tile getTile(int x, int y) {
 		if (x < 0 || y < 0 || x >= worldTile.length || y >= worldTile[0].length)
 			return null;
 		return worldTile[x][y];
 	}
 
 	/**
 	 * Sets a tile at given coordinates. Throws an exception if coordinates are
 	 * invalid.
 	 */
 	public void setTile(int x, int y, Tile t) {
 		worldTile[x][y] = t;// TODO add bounds checking
 		gameUpdate.changedTileColour(t);
 	}
 
 	/**
 	 * Returns the size of the world in the X direction.
 	 */
 	public int getXSize() {
 		return worldTile.length;
 	}
 
 	/**
 	 * Returns the size of the world in the Y direction.
 	 */
 	public int getYSize() {
 		return worldTile[0].length;
 	}
 
 	/**
 	 * Updates everything in the world.
 	 */
 	public void update() {
 		for (Dude d : allDudes)
 			d.update();
 	}
 
 	public void setGameUpdate(GameUpdate g) {
 		gameUpdate = g;
 	}
 
 	/**
 	 * Returns all stored Dudes
 	 *
 	 * @return
 	 */
 	public Set<Dude> getDudes() {
 		return allDudes;
 	}
 
 	/**
 	 * Finds the nearest resource structure of the given type.
 	 * resType is the type to look for, or null if any type is ok.
 	 */
 	public Resource getNearestResource(Tile tile, ResourceType resType) {
 		int x = tile.getX();
 		int y = tile.getY();
 		int bestSquaredDistance = Integer.MAX_VALUE;
 		Resource bestResource = null;
 
 		for(Resource r : resources) {
 			if(resType != null && r.getResType() != resType)
 				continue;
 			if(r.getResType() == null)
 				continue;
 			int squaredDistance = (r.getX()-x)*(r.getX()-x) + (r.getY()-y)*(r.getY()-y);
 			if(squaredDistance < bestSquaredDistance) {
 				bestSquaredDistance = squaredDistance;
 				bestResource = r;
 			}
 		}
 		return bestResource;
 	}
 
 
 
 	public Structure getNearestStructure(Class<?> class1, Tile tile) {
 		int x = tile.getX();
 		int y = tile.getY();
 		int bestSquaredDistance = Integer.MAX_VALUE;
 		Structure bestStructure = null;
 
 		for(Structure r : structures) {
 			if(!class1.isInstance(r))
 				continue;
 			int squaredDistance = (r.getX()-x)*(r.getX()-x) + (r.getY()-y)*(r.getY()-y);
 			if(squaredDistance < bestSquaredDistance) {
 				bestSquaredDistance = squaredDistance;
 				bestStructure = r;
 			}
 		}
 		return bestStructure;
 	}
 
 	public int getCrystalResource() {
 		return crystalResource;
 	}
 
 	public void setCrystalResource(int crystalResource) {
 		this.crystalResource = crystalResource;
 	}
 
 	public int getPlantResource() {
 		return plantResource;
 	}
 
 	public void setPlantResource(int plantResource) {
 		this.plantResource = plantResource;
 	}
 
 	public int getWoodResource() {
 		return woodResource;
 	}
 
 	public void setWoodResource(int woodResource) {
 		this.woodResource = woodResource;
 	}
 
 
 
 
 
 
 
 
 
 	public void build(Tile t, String type) {
 		// TODO Auto-generated method stub
 
 	}
 }

 package state;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Queue;
 import java.util.Random;
 import java.util.Set;
 import java.util.Stack;
 
 import sound.AudioPlayer;
 import sound.MixingDesk;
 
 import logic.GameUpdate;
 import logic.Logic;
 
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
 
 	private Logic logic;
 	private boolean dudeSpawningEnabled = true;
 	private boolean slugBalancingEnabled = true;
 	private AudioPlayer audioPlayer;
 
 	private String currentBuild = "BarrenGrass";
 
 	MixingDesk mixingDesk;
 
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
 	 * Creates a world from a tile array.
 	 */
 	public World(Tile[][] tiles, GameUpdate initialUpdate) {
 		gameUpdate = initialUpdate;
 		worldTile = tiles;
 		resources = new HashSet<Resource>();
 		logic = new Logic(this);
 		for(Tile[] row : tiles)
 			for(Tile t : row)
 				t.setWorld(this);
 		start();
 	}
 
 	/**
 	 * If we put this stuff in the constructor the game will break so put it
 	 * here and it's called from inside UpdateThread
 	 */
 	private void start() {
 		addDude(new Dude(this, 7, 7, 1, 1, "Assets/Characters/Man.png"));
 		addDude(new Dude(this, 8, 8, 1, 1, "Assets/Characters/Man.png"));
 		addDude(new Octodude(this, 2, 2, 1, 1,"Assets/Characters/Enemies/AlienOctopus/EyeFrontRight.png"));
 		addDude(new Slugdude(this, 3, 3, 1, 1,"Assets/Characters/Enemies/AlienSlug/SlugFrontRight.png"));
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
 
 		if (s instanceof Resource)
 			resources.add((Resource) s);
 		structures.add(s);
 
 		// place the structure
 		for (int X = 0; X < w; X++)
 			for (int Y = 0; Y < h; Y++)
 				worldTile[x - X][y - Y].setStructure(s, false);
 		gameUpdate.structureAdded(s); // Send change to the network class
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
 
 		for (int X = 0; X < w; X++)
 			for (int Y = 0; Y < h; Y++)
 				worldTile[x - X][y - Y].setStructure(null, false);
 
 		if (s instanceof Resource)
 			resources.remove(s);
 		structures.remove(s);
 		gameUpdate.structureRemoved(s); // Let the network know about the change
 	}
 
 	public void removeDude(Dude s) {
 		int x = s.getX(), y = s.getY(), w = s.getWidth(), h = s.getHeight();
 		int ox = s.getOldX(), oy = s.getOldY();
 
 		for (int X = 0; X < w; X++)
 			for (int Y = 0; Y < h; Y++) {
 				worldTile[x - X][y - Y].setDude(null);
 				if (worldTile[ox - X][oy - Y].getDude() == s)
 					worldTile[ox - X][oy - Y].setDude(null);
 			}
 
 		allDudes.remove(s);
 
 		gameUpdate.dudeRemoved(s); //Let the network know about the change
 		s.setDeleted();
 	}
 
 	/**
 	 * Adds a dude to the world and returns true. If the dude can't be placed,
 	 * returns false without changing anything.
 	 */
 	public boolean addDude(Dude s) {
 		if (crystalResource > 50) {
 			crystalResource = crystalResource - 50;//TODO Change amount if needed
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
 			s.setWorld(this);
 			allDudes.add(s);
 			// plays the sound
 
 			if (mixingDesk != null) {
 				this.mixingDesk.addAudioPlayer("NewDudeBorn.wav", true);
 			}
 
 			gameUpdate.dudeAdded(s);
 
 			return true;
 		}
 		return false;
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
 		logic.mapChanged(x, y);
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
 
 	// Update counter for below.
 	int counter;
 
 	/**
 	 * Updates everything in the world.
 	 */
 	public void update() {
 		for (Dude d : new ArrayList<Dude>(allDudes))
 			d.update();
 		for (Structure s : new ArrayList<Structure>(structures))
 			s.update();
 
 		if(counter == 60 && dudeSpawningEnabled){
 			int rand = (int) Math.random()*100 + 1;
 			if( rand > 0 && rand <= 50)
 				addDude(new Octodude(this, /*((int)(Math.random() * getXSize()) + 1)*/2,/*(int) ((Math.random() * getYSize()) + 1)*/2, 1, 1, "Assets/Characters/Enemies/AlienOctopus/EyeFrontRight.png"));
 			else if ( rand > 50 && rand <= 100)
 				addDude(new Slugdude(this, /*((int)(Math.random() * getXSize()) + 1)*/2,/*(int) ((Math.random() * getYSize()) + 1)*/2, 1, 1, "Assets/Characters/Enemies/AlienSlug/SlugFrontRight.png"));
 			counter = 0;
 		} else if(!dudeSpawningEnabled && counter == 150){
 			int rand = (int) Math.random()*100 + 1;
 			if(rand > 0 && rand <= 50)
 				addDude(new Octodude(this, /*((int)(Math.random() * getXSize()) + 1)*/2,/*(int) ((Math.random() * getYSize()) + 1)*/2, 1, 1, "Assets/Characters/Enemies/AlienOctopus/EyeFrontRight.png"));
 			else if (rand > 50 && rand <= 100)
 				addDude(new Slugdude(this, /*((int)(Math.random() * getXSize()) + 1)*/2,/*(int) ((Math.random() * getYSize()) + 1)*/2, 1, 1, "Assets/Characters/Enemies/AlienSlug/SlugFrontRight.png"));
 
 			counter = 0;
 		} else {
 			counter++;
 		}
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
 
 	public Resource getNearestResource(Tile tile, Dude dude) {
 
 		int x = tile.getX();
 		int y = tile.getY();
 		int bestSquaredDistance = Integer.MAX_VALUE;
 		Resource bestResource = null;
 
 		for (Resource r : resources) {
 			if (!dude.canMine(r))
 				continue;
 			Tile restile = getTile(r.getX(), r.getY());
 			if (restile.getDude() != null && restile.getDude() != dude)
 				continue;
 			int squaredDistance = (r.getX() - x) * (r.getX() - x)
 					+ (r.getY() - y) * (r.getY() - y);
 
 			if (squaredDistance < bestSquaredDistance) {
 				if (!getLogic().findRoute(tile, getTile(r.getX(), r.getY()),
 						dude).isEmpty() || getTile(dude.getX(), dude.getY()) == tile) {
 					bestSquaredDistance = squaredDistance;
 					bestResource = r;
 				}
 			}
 		}
 		return bestResource;
 	}
 
 	public Structure getNearestStructure(Class<?> class1, Tile tile, Dude dude) {
 		int x = tile.getX();
 		int y = tile.getY();
 		int bestSquaredDistance = Integer.MAX_VALUE;
 		Structure bestStructure = null;
 
 		for (Structure r : structures) {
 			if (!class1.isInstance(r))
 				continue;
 			Tile td = getTile(r.getX(), r.getY());
 			if (td.getDude() != null && td.getDude() != dude)
 				continue;
 			int squaredDistance = (r.getX() - x) * (r.getX() - x)
 					+ (r.getY() - y) * (r.getY() - y);
 			if (squaredDistance < bestSquaredDistance) {
 				if (!getLogic().findRoute(tile, getTile(r.getX(), r.getY()),
 						dude).isEmpty() || getTile(dude.getX(), dude.getY()) == tile) {
 					bestSquaredDistance = squaredDistance;
 					bestStructure = r;
 				}
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
 
 	public boolean build(Tile t, String type, Dude dude) {
 			if (dude.isAt(t.getX(), t.getY())) {
 				// finish building tile
 				if (t.getStructure() != null) {
 					removeStructure(t.getStructure());
 				}
 
 				t.setImage(currentBuild);
 				t.setHeight(t.getHeight() + 1);
 
 				// set tile non transparent
 				// reassign dude to new task
 				return true;
 		} else {
 			// otherwise reassign dude and repush task
 			tasks.add(new Task(t, "build", type));
 			return true;
 		}
 	}
 
 	public boolean hasResources(String type) {
 		if( type.equals("BarrenWall"))
 			return true;
 		if(type.equals("BarrenGrass"))
 			return true;
 		if(type.equals("DarkSand"))
 			return true;
		if(type.equals("grass"))
 			return true;
 		else {return false;}
 	}
 
 	public boolean isDudeSpawningEnabled() {
 		return dudeSpawningEnabled;
 	}
 
 	public void toggleDudeSpawning() {
 		dudeSpawningEnabled = !dudeSpawningEnabled;
 	}
 
 	/**
 	 * sets game music player to
 	 * @param mixingDesk
 	 */
 	public void setAudioPlayer(MixingDesk mixingDesk) {
 		this.mixingDesk = mixingDesk;
 	}
 
 	public boolean isSlugBalancingEnabled() {
 		return slugBalancingEnabled;
 	}
 
 	public void toggleSlugBalancing() {
 		slugBalancingEnabled = !slugBalancingEnabled;
 	}
 
 
 	/**
 	 * Returns the current audio system for playing sounds
 	 * returns null if nothing assigned yet.
 	 * @return
 	 */
 	public MixingDesk getAudioPlayer() {
 		return this.mixingDesk;
 	}
 
 	public Logic getLogic() {
 		return this.logic;
 	}
 
 	public String getCurrentBuild() {
 		return currentBuild;
 	}
 
 	public void setCurrentBuild(String currentBuild) {
 		this.currentBuild = currentBuild;
 	}
 }

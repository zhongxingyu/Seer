 package state;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Queue;
 import java.util.Random;
 import java.util.Set;
 
 import logic.GameUpdate;
 import logic.Logic;
 import sound.AudioPlayer;
 import sound.MixingDesk;
 import util.TileImageStorage;
 
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
 	//false = building structures true = building tiles
 	private boolean buildingStructures = false;
 
 	private Set<Dude> allDudes = new HashSet<Dude>();
 	private Set<Structure> structures = new HashSet<Structure>();
 	private Set<Resource> resources;
 
 	private Logic logic;
 	private boolean dudeSpawningEnabled = true;
 	private boolean slugBalancingEnabled = true;
 	private AudioPlayer audioPlayer;
 
 	private String currentBuild = "BarrenGrass";
 	private String currentStruct = "Ramp";
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
 		for (Tile[] row : tiles)
 			for (Tile t : row)
 				t.setWorld(this);
 		start();
 	}
 
 	/**
 	 * If we put this stuff in the constructor the game will break so put it
 	 * here and it's called from inside UpdateThread
 	 */
 	private void start() {
 		//WHAT STARTS WHERE
 		addStructure(new Crate(34, 34));
 		addStructure(new DudeSpawnBuilding(30,30));
 
 		Random r = new Random();
 		for(int k = 0; k < 50; k++) {
 			int x = r.nextInt(getXSize()), y = r.nextInt(getYSize());
 
 			while(getTile(x,y) != null && getTile(x,y).getImageName().equals("Water")){
 				x = r.nextInt(getXSize());
 				y = r.nextInt(getYSize());
 			}
 
 			addStructure(new Crystal(x, y));
 		}
 
 		for(int k = 0; k < 50; k++) {
 			int x = r.nextInt(getXSize()), y = r.nextInt(getYSize());
 
 			while(getTile(x,y) != null && getTile(x,y).getImageName().equals("Water")){
 				x = r.nextInt(getXSize());
 				y = r.nextInt(getYSize());
 			}
 
 			int rad = 10;
 			for(int i = 0; i < 30; i++) {
 				int x2 = x + r.nextInt(rad), y2 = y + r.nextInt(rad);
 
 				while(getTile(x2,y2) != null && getTile(x2,y2).getImageName().equals("Water")){
 					x2 = r.nextInt(getXSize());
 					y2 = r.nextInt(getYSize());
 				}
 
 				Tile t = getTile(x2, y2);
 				if(t != null && t.getImageName().equals("DarkSand"))
 					addStructure(new Tree(x2, y2));
 			}
 		}
 
 		for(int k = 0; k < 10; k++) {
 			int x = r.nextInt(getXSize()), y = r.nextInt(getYSize());
 			int length = 7 + r.nextInt(3);
 			if(r.nextBoolean()) {
 				for(int i = 0; i < length; i++) {
 					x++;
					if(getTile(x,y) == null || getTile(x, y).getImageName().equals("Water")){ continue; }
 					addStructure(new Plant(x, y));
 				}
 			} else {
 				for(int i = 0; i < length; i++) {
 					y++;
					if(getTile(x,y) == null || getTile(x, y).getImageName().equals("Water")){ continue; }
 					addStructure(new Plant(x, y));
 				}
 			}
 		}
 
 		addDude(new Dude(this, 30, 30, 1, 1, "Assets/Characters/Man.png"));
 		addDude(new Dude(this, 32, 34, 1, 1, "Assets/Characters/Man.png"));
 		addDude(new Dude(this, 34, 31, 1, 1, "Assets/Characters/Man.png"));
 		addDude(new Dude(this, 31, 35, 1, 1, "Assets/Characters/Man.png"));
 		addDude(new Dude(this, 33, 33, 1, 1, "Assets/Characters/Man.png"));
 
 
 
 //		addDude(new Octodude(this, 2, 2, 1, 1,
 //				"Assets/Characters/Enemies/AlienOctopus/EyeFrontRight.png"));
 //		addDude(new Slugdude(this, 3, 3, 1, 1,
 //				"Assets/Characters/Enemies/AlienSlug/SlugFrontRight.png"));
 //		addDude(new Slugdude(this, 10, 10, 1, 1,
 //				"Assets/Characters/Enemies/AlienSlug/SlugFrontRight.png"));
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
 
 		gameUpdate.dudeRemoved(s); // Let the network know about the change
 		s.setDeleted();
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
 		s.setWorld(this);
 		allDudes.add(s);
 		// plays the sound
 
 		if (mixingDesk != null) {
 			this.mixingDesk.addAudioPlayer("NewDudeBorn.wav", true);
 		}
 
 		gameUpdate.dudeAdded(s);
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
 		Tile.getImagesCache().update();
 
 
 		List<Resource> spawnResources = getEatableResources();
 		Resource toSpawnNear = null;
 		if(spawnResources.size() > 0){
 			toSpawnNear = spawnResources.get(new Random().nextInt(spawnResources.size()));
 		}
 
 		if(counter > 60 && dudeSpawningEnabled){
 			int rand = (int) (Math.random()*100) + 1;
 			if( rand > 0 && rand <= 50){
 				if(toSpawnNear != null){
 					addDude(new Octodude(this, Math.min(Math.max(0,new Random().nextInt(40) - 20), worldTile.length-1) + toSpawnNear.getX(),Math.min(Math.max(0,new Random().nextInt(40) - 20), worldTile[0].length-1) + toSpawnNear.getY()));
 				}else{
 					addDude(new Octodude(this, new Random().nextInt(this.getXSize()), new Random().nextInt(this.getYSize())));
 				}
 			}else if ( rand > 50 && rand <= 100){
 				if(toSpawnNear != null){
 					addDude(new Slugdude(this, Math.min(Math.max(0,new Random().nextInt(40) - 20), worldTile.length-1) + toSpawnNear.getX(),Math.min(Math.max(0,new Random().nextInt(40) - 20), worldTile[0].length-1) + toSpawnNear.getY(), 1, 1, "Assets/Characters/Enemies/AlienSlug/SlugFrontRight.png"));
 				}else{
 					addDude(new Octodude(this, new Random().nextInt(this.getXSize()), new Random().nextInt(this.getYSize())));
 				}
 			}
 			counter = 0;
 		} else if(!dudeSpawningEnabled && counter > 150){
 			int rand = (int) (Math.random()*100) + 1;
 			if(rand > 0 && rand <= 50)
 				if(toSpawnNear != null){
 					addDude(new Octodude(this, Math.min(Math.max(0,new Random().nextInt(40) - 20), worldTile.length-1) + toSpawnNear.getX(),Math.min(Math.max(0,new Random().nextInt(40) - 20), worldTile[0].length-1) + toSpawnNear.getY()));
 				}else{
 					addDude(new Octodude(this, new Random().nextInt(this.getXSize()), new Random().nextInt(this.getYSize())));
 				}
 			else if (rand > 50 && rand <= 100)
 				if(toSpawnNear != null){
 					addDude(new Slugdude(this, Math.min(Math.max(0,new Random().nextInt(40) - 20), worldTile.length-1) + toSpawnNear.getX(),Math.min(Math.max(0,new Random().nextInt(40) - 20), worldTile[0].length-1) + toSpawnNear.getY(), 1, 1, "Assets/Characters/Enemies/AlienSlug/SlugFrontRight.png"));
 				}else{
 					addDude(new Octodude(this, new Random().nextInt(this.getXSize()), new Random().nextInt(this.getYSize())));
 				}
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
 						dude).isEmpty()
 						|| getTile(dude.getX(), dude.getY()) == tile) {
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
 						dude).isEmpty()
 						|| getTile(dude.getX(), dude.getY()) == tile) {
 					bestSquaredDistance = squaredDistance;
 					bestStructure = r;
 				}
 			}
 
 		}
 		return bestStructure;
 	}
 
 	public List<Resource> getEatableResources(){
 		List<Resource> highResources = new ArrayList<Resource>();
 		for(Resource r : resources){
 			if(r instanceof Crystal && ((Crystal)r).shouldOctoMine()){
 				highResources.add(r);
 			}
 		}
 		return highResources;
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
 
 	public boolean build(Tile t, String type, Dude dude)
 	{
 		if (playerHasEnoughResource()) {
 			if (dude.getTask().getTask().equals("buildTile")) {
 				if (dude.isAt(t.getX(), t.getY())) {
 					// finish building tile
 					if (t.getStructure() != null) {
 						removeStructure(t.getStructure());
 					}
 
 					t.setImage(dude.getTask().getType());
 					t.setHeight(t.getHeight() + 1);
 					// set tile non transparent
 					// reassign dude to new task
 					return true;
 				}
 			}
 		}
 		else if (dude.getTask().getTask().equals("buildStructure"))
 		{
 			if (dude.isAt(t.getX(), t.getY()))
 			{
 				// finish building tile
 				if (t.getStructure() != null)
 				{
 					removeStructure(t.getStructure());
 				}
 				this.addStructure(new Structure(t.getX(), t.getY(), 1, 1,
 						"Assets/EnvironmentObjects/"+type+".png"));
 
 			// plays audio
 			if (mixingDesk != null) {
 				mixingDesk.addAudioPlayer("PlaceItem.wav", true);
 			}
 			// set tile non transparent
 			// reassign dude to new task
 			return true;
 			}
 		} else
 		{
 			// otherwise reassign dude and repush task
 //			tasks.add(new Task(t, "build", type));
 			return true;
 		}
 		return false;
 	}
 
 	private boolean playerHasEnoughResource() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean hasResources(String type) {
 		if (type.equals("BarrenWall"))
 			return true;
 		else if(type.equals("BarrenGrass"))
 			return true;
 		else if(type.equals("DarkSand"))
 			return true;
 		else if(type.equals("Grass"))
 			return true;
 		else if (type.equals("DarkTree"))
 			return true;
 		else {
 			return false;
 		}
 	}
 
 	public boolean isDudeSpawningEnabled() {
 		return dudeSpawningEnabled;
 	}
 
 	public void toggleDudeSpawning() {
 		dudeSpawningEnabled = !dudeSpawningEnabled;
 	}
 
 	/**
 	 * sets game music player to
 	 *
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
 	 * Returns the current audio system for playing sounds returns null if
 	 * nothing assigned yet.
 	 *
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
 
 
 	public boolean getBuildType(){
 		return buildingStructures;
 	}
 
 	public void setBuildType(){
 		buildingStructures = !buildingStructures;
 	}
 
 	public boolean dig(Tile t, Dude dude) {
 		if (dude.isAt(t.getX() - 1, t.getY())
 				|| dude.isAt(t.getX() + 1, t.getY())
 				|| dude.isAt(t.getX(), t.getY() + 1)
 				|| dude.isAt(t.getX() + 1, t.getY() - 1)) {
 			// finish building tile
 			if (t.getStructure() != null) {
 				removeStructure(t.getStructure());
 			}
 
 			t.setHeight(t.getHeight() - 1);
 
 			// plays audio
 			if (mixingDesk != null) {
 				mixingDesk.addAudioPlayer("PlaceItem.wav", true);
 			}
 
 			// set tile non transparent
 			// reassign dude to new task
 			return true;
 		} else {
 			// otherwise reassign dude and repush task
 //			tasks.add(new Task(t, "dig"));
 			return false;
 		}
 	}
 
 	public String getCurrentStruct() {
 		return currentStruct;
 	}
 
 	public void setCurrentStruct(String currentStruct) {
 		this.currentStruct = currentStruct;
 	}
 }

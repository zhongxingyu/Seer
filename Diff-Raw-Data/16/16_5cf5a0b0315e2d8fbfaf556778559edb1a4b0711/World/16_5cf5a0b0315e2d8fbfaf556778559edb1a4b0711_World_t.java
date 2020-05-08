 package state;
 
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 
 /**
  * Stores everything in the game.
  */
 public class World {
 	private Tile[][] worldTile;
 
 	long seed = System.currentTimeMillis();
 	private Random random = new Random(seed);
 
 	private Set<Dude> allDudes = new HashSet<Dude>();
 
 	/**
 	 * Returns a random tile name.
 	 */
 	public String generateRandomTile(){
 		int rand = random.nextInt(5);
 		if(rand==1)
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
 	public World(){
 		worldTile = new Tile[100][100];
 		for(int x = 0; x < 100; x++)
 			for(int y = 0; y < 100; y++) {
 				if (random.nextInt(2) == 1)
 					worldTile[x][y] = new Tile(generateRandomTile(),0,x,y);
 				else
 					worldTile[x][y] = new Tile(generateRandomTile(),1,x,y);
 			}
 
 		for(int x = 3; x < 100; x += 1){
 			for(int y = 3; y < 100; y += 1) {
 				if(random.nextInt(20)==1)
					addStructure(new Structure(x, y, 3, 3, "Assets/EnvironmentObjects/dark-tree.png"));
 				}
 			}
 		addDude(new Dude(this, 7, 7, 1, 1, "Assets/Man.png"));
 	}
 
 	/**
 	 * Creates a world from a tile array.
 	 */
 	public World(Tile[][] tiles) {
 		worldTile = tiles;
 		addDude(new Dude(this, 7, 7, 1, 1, "Assets/Man.png"));
 	}
 
 	/**
 	 * Adds a structure to the world and returns true.
 	 * If the structure can't be placed, returns false without changing anything.
 	 */
 	public boolean addStructure(Structure s) {
 		int x = s.getX(), y = s.getY(), w = s.getWidth(), h = s.getHeight();
 
 		if(x-w < -1 || y-h < -1 || x >= getXSize() || y >= getYSize())
 			return false;
 
 		// check for overlap
 		for(int X = 0; X < w; X++)
 			for(int Y = 0; Y < h; Y++)
 				if(worldTile[x-X][y-Y].getStructure() != null){
 					System.out.println("Cannot add structure: overlap");
 					return false; // can't have two structures on one tile
 				}
 
 		// place the structure
 		for(int X = 0; X < w; X++)
 			for(int Y = 0; Y < h; Y++)
 				worldTile[x-X][y-Y].setStructure(s);
 
 		return true;
 	}
 
 
 	/**
 	 * Adds a dude to the world and returns true.
 	 * If the dude can't be placed, returns false without changing anything.
 	 */
 	public boolean addDude(Dude s) {
 		int x = s.getX(), y = s.getY(), w = s.getWidth(), h = s.getHeight();
 
 		if(x-w < -1 || y-h < -1 || x >= getXSize() || y >= getYSize())
 			return false;
 
 		// check for overlap
 		for(int X = 0; X < w; X++)
 			for(int Y = 0; Y < h; Y++)
 				if(worldTile[x-X][y-Y].getDude() != null)
 					return false; // can't have two structures on one tile <--The best comment! =)
 
 		// place the structure
 		for(int X = 0; X < w; X++)
 			for(int Y = 0; Y < h; Y++)
 				worldTile[x-X][y-Y].setDude(s);
 
 		allDudes.add(s);
 
 		return true;
 	}
 
 	/**
 	 * Returns a tile at given coordinates.
 	 * Throws an exception if coordinates are invalid.
 	 */
 	public Tile getTile(int x, int y) {
 		return worldTile[x][y];
 	}
 
 	/**
 	 * Sets a tile at given coordinates.
 	 * Throws an exception if coordinates are invalid.
 	 */
 	public void setTile(int x, int y, Tile t) {
 		worldTile[x][y] = t;
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
 		for(Dude d : allDudes)
 			d.update();
 	}
 }

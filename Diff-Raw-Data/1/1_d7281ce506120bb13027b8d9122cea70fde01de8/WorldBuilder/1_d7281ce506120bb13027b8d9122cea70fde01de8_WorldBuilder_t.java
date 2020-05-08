 package model.world;
 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Random;
 import model.geometrical.Position;
 import model.world.generator.MapGenerator;
 
 /**
  * This will build new worlds by using specific seeds which always will produce the
  * same map every time.
  * 
  * @author Calleberg
  *
  */
 public class WorldBuilder {
 
 	private long seed;
 	private Random random;
 		
 	/*Paths to where the different files are located*/
 	private final String BASE_SHORE = "shoreline/10x10_shore_";
 	private final String BASE_ROAD = "road/10x10_road_";
 	private final String BASE_RESIDENTIAL = "residential/";
 	private Properties attributes;
 	
 	private List<Tile> spawnPoints;
 	
 	/**
 	 * Creates a new default world builder.
 	 */
 	public WorldBuilder() {
 		this(Calendar.getInstance().getTimeInMillis());
 	}
 	
 	/**
 	 * Creates a new world builder with the specified seed.
 	 * @param seed the seed to use.
 	 */
 	public WorldBuilder(long seed) {
 		this.seed = seed;
 		this.random = new Random(seed);
 		this.resetSpawnPoints();
 		this.initAttributes();
 	}
 	
 	/*
 	 * Initialises the map where a specified lot size is the key for the amount of 
 	 * lots of that size.
 	 */
 	private void initAttributes() {
 		attributes = new Properties();
 		String path = BASE_RESIDENTIAL + "lot";
 		this.addAttributesFrom(attributes, path, "10x20");
 		this.addAttributesFrom(attributes, path, "20x20");
 		this.addAttributesFrom(attributes, path, "20x10");
 	}
 
 	private void addAttributesFrom(Map<Object, Object> attributes, String pathBase, String key) {
 		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 		String path = pathBase + key + "/info.txt";
 		
 		try {
 			InputStream is = classLoader.getResourceAsStream(path);
 			BufferedReader reader;
 			if(is == null) {
 				reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)), "ISO-8859-1"));
 			}else{
 				reader = new BufferedReader(new InputStreamReader(is));
 			}
 			reader.readLine();
 			attributes.put(pathBase + key + "/" + key + "_N", reader.readLine());		
 			reader.readLine();
 			attributes.put(pathBase + key + "/" + key + "_E", reader.readLine());	
 			reader.readLine();
 			attributes.put(pathBase + key + "/" + key + "_S", reader.readLine());	
 			reader.readLine();
 			attributes.put(pathBase + key + "/" + key + "_W", reader.readLine());	
 										
 			reader.close();
 		} catch (IOException exc) {
 			System.out.println("Could not find " + path);
 		}
 	}
 	
 	/**
 	 * Gives the seed used when generating a world.
 	 * @return the seed used when generating a world.
 	 */
 	public long getSeed() {
 		return this.seed;
 	}
 	
 	/**
 	 * Gives an empty world.
 	 * @param width the width of the world.
 	 * @param height the height of the world.
 	 * @return an empty world.
 	 */
 	public static Tile[][] getEmptyWorld(int width, int height) {
 		Tile[][] tiles = new Tile[width][height];
 		for(int x = 0; x < tiles.length; x++) {
 			for(int y = 0; y < tiles[0].length; y++) {
 				tiles[x][y] = new Tile(new Position(x, y), 0);
 			}
 		}
 		return tiles;
 	}
 	
 	/**
 	 * Resets the spawnpoints.
 	 * This means the list of spawnpoints will be cleared.
 	 */
 	public void resetSpawnPoints() {
 		this.spawnPoints = new ArrayList<Tile>();
 	}
 	
 	/**
 	 * Creates a new world with the specified size.
 	 * @param width the width of the world to create.
 	 * @param height the height of the world to create.
 	 * @return a new world.
 	 */
 	public Tile[][] getNewWorld(int width, int height) {
 		MapGenerator g = new MapGenerator(seed);
 		int[][] mapData = g.generateWorld(width/10, height/10);
 		width = mapData.length * 10;
 		height = mapData[0].length * 10;
 		
 		//Resets the spawn points.
 		this.resetSpawnPoints();
 		
 		//Create grass everywhere.
 		Tile[][] tiles = new Tile[width][height];
 		for(int x = 0; x < tiles.length; x++) {
 			for(int y = 0; y < tiles[0].length; y++) {
 				tiles[x][y] = new Tile(new Position(x, y), 0);
 			}
 		}
 				
 		//Loops through every tileset and adds the right tiles.
 		for(int x = 0; x < mapData.length; x++) {
 			for(int y = 0; y < mapData[0].length; y++) {
 				//Adds water.
 				if(mapData[x][y] == MapGenerator.WATER) {
 					addTiles(tiles, x*10, y*10, "lots/water.lot");
 				}
 				//Adds shorelines.
 				else if(mapData[x][y] == MapGenerator.SHORE) {
 					this.buildDynamicTile(tiles, mapData, x, y, BASE_SHORE, MapGenerator.WATER, false);
 				}
 				//Adds the right road part to the world.
 				if(mapData[x][y] == MapGenerator.ROAD) {
 					this.buildDynamicTile(tiles, mapData, x, y, BASE_ROAD, MapGenerator.ROAD, false);
 				}
 				else if(mapData[x][y] == MapGenerator.HOUSE) {
 					this.tryPlaceBuilding(tiles, mapData, x, y, BASE_RESIDENTIAL, MapGenerator.HOUSE);
 				}
 			}
 		}
 				
 		return tiles;
 	}
 	
 	/***
 	 * Tries to place a building at the specified position.
 	 * @param tiles the tiles to add to.
 	 * @param data the data to read from.
 	 * @param x X coordinate.
 	 * @param y Y coordinate.
 	 * @param base the base path to the files.
 	 * @param type the type to add.
 	 */
 	private void tryPlaceBuilding(Tile[][] tiles, int[][] data, int x, int y, String base, int type) {
 		int w = 0, h = 0;
 		//Checks if the building can be 20 in width
 		if(data[x+1][y] == type) {
 			//Checks if a 20x20 building is possible
 			if(data[x][y+1] == type && data[x+1][y+1] == type && random.nextInt(3) == 0) {
 				w = 2;
 				h = 2;
 			//Then the building is 20x10
 			}else{
 				w = 2;
 				h = 1;
 			}
 		//Checks if a 10x20 building can be used
 		}else if(data[x][y+1] == type) {
 			w = 1;
 			h = 2;
 		}
 		
 		//Checks if a building was found.
 		if(w != 0 && h != 0) {
 			StringBuilder sb = new StringBuilder();
 			sb.append("lot" + w + "0x" + h + "0" + "/" + w + "0x" + h + "0_");
 			//Adds the appropriate direction value
 			int oldLength = sb.length();
 			if(data[x-1][y] == MapGenerator.ROAD) {
 				sb.append("W");
 			}else if(data[x+w][y] == MapGenerator.ROAD) {
 				sb.append("E");
 			}else if(data[x][y-1] == MapGenerator.ROAD) {
 				sb.append("N");
 			}else if(data[x][y+h] == MapGenerator.ROAD) {
 				sb.append("S");
 			}
 			if(sb.length() != oldLength) {
 				int n = Integer.parseInt(attributes.get(base + sb.toString()).toString());
 				if(n != 0) {
 					//Loops through all the tiles used to build a building so they
 					//cannot be used by another building.
 					for(int xLoop = 0; xLoop < w; xLoop++) {
 						for(int yLoop = 0; yLoop < h; yLoop++) {
 							data[x + xLoop][y + yLoop] = MapGenerator.USED;
 						}
 					}
 					sb.append((1 + random.nextInt(n)));
 					addTiles(tiles, x*10, y*10, base + sb.toString() + ".lot");
 				}
 			}
 		}
 	}
 
 	/**
 	 * Builds a tile which can connect to other tiles of the same type around it.
 	 * @param tiles the tiles to add to.
 	 * @param data the data to read from.
 	 * @param x X coordinate.
 	 * @param y Y coordinate.
 	 * @param base the base of the path to the files.
 	 * @param type the type to build and connect to.
 	 * @param once set to true if the tile can only be used once and then be used.
 	 */
 	private void buildDynamicTile(Tile[][] tiles, int[][] data, int x, int y, String base, int type, boolean once) {
 		StringBuilder sb = new StringBuilder();
 
 		//Note: 1 is N, 2 is NE, 3 is E, 4 is SE ...
 		//This way complex connections can be made which cannot be expressed in simple directions (N, NE, W etc.)
 		if(data[x][y-1] == type) {
 			sb.append('1');
 		}
 		if(data[x+1][y] == type) {
 			sb.append('3');
 		}
 		if(data[x][y+1] == type) {
 			sb.append('5');
 		}
 		if(data[x-1][y] == type) {
 			sb.append('7');
 		}
 
 		//If none of the simple connections could be made, look for more complex ones
 		if(sb.toString().length() == 0) {
 			if(data[x+1][y-1] == type) {
 				sb.append('2');
 			}
 			if(data[x+1][y+1] == type) {
 				sb.append('4');
 			}
 			if(data[x-1][y+1] == type) {
 				sb.append('6');
 			}
 			if(data[x-1][y-1] == type) {
 				sb.append('8');
 			}
 		}
 
 		if(sb.toString().length() > 0) {
 			addTiles(tiles, x*10, y*10, base + sb.toString() + ".lot");
 		}
 	}
 	
 	/**
 	 * Gives a list of all the spawn points in the last built world.
 	 * @return a list of all the spawn points in the last built world.
 	 */
 	public List<Tile> getSpawnPoints() {
 		return this.spawnPoints;
 	}
 	
 	/**
 	 * Adds the specified tileset to the world.
 	 * @param tiles the whole map.
 	 * @param startX the X coordinate to add the tileset to.
 	 * @param startY the Y coordinate to add the tileset to.
 	 * @param path the path to where the data of the tileset is stored.
 	 */
 	public void addTiles(Tile[][] tiles, int startX, int startY, String path) {
 		WorldBuilderIO.addTiles(tiles, startX, startY, path, this.spawnPoints);
 	}
 	
 }

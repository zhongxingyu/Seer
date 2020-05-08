 /* 
  * File Name: Map.java
  * Contributors:	Jonathan Bradley 	- 7/18/2013
  * 					Ryan Meier			- 
  * 					Ben Emrick			-
  * 
  * Purpose: This class creates a map grid to keep track of each game tile
  * 
  * Future Goals: change constructor to read-in a text file for different
  * 		levels once we have the different types of tiles developed
  */
 package td.map;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 
 import td.Game;
 
 
 public class Map {
 
 	// Set up needed variables
 	private int mapWidth, mapHeight;
 	private Tile[][] mapGrid;
 	private int TileID;
 	private int mapWidthPixels, mapHeightPixels;
 	private Boolean hasChanged;
 	private BufferedImage mapImage;
 
 	// Constructor - this will need to be modified once we have a config file set up for maps
 	//		currently only creates tiles of grass
 	public Map(int width, int height) {
		this.mapGrid = new Tile[width][height]; // Map is nothing more than a 2D array of Tiles
 		TileID = 0;
 		mapWidth = width;
 		mapHeight = height;
 		for (int i = 0; i < width; i++) {
 			for (int k = 0; k < height; k++) {
 				this.mapGrid[i][k] = new Tile("artAssets/grass.png", TileID++);
 			}
 		}
 		mapWidthPixels = mapGrid[0][0].getWidth() * mapWidth * Game.SCALE; // Sprite width * Tile width * scale
 		mapHeightPixels = mapGrid[0][0].getHeight() * mapHeight * Game.SCALE; // Sprite height * Tile height * scale
 		generateMapImage();
 	}
 
 	// Returns the Map height (in tiles)
 	public int getHeight() {
 		return mapHeight;
 	}
 
 	// Returns the Map Width (in tiles)
 	public int getWidth() {
 		return mapWidth;
 	}
 
 	// Returns the Map Height (in pixels)
 	public int getHeightPixels() {
 		return mapHeightPixels;
 	}
 
 	// Returns the Map Width (in pixels)
 	public int getWidthPixels() {
 		return mapWidthPixels;
 	}
 
 	// I don't think this is needed - might remove later?
 	public Tile[][] getMap() {
 		return mapGrid;
 	}
 
 	// Returns a specific Tile, by x and y coordinates
 	public Tile getTile(int x, int y) {
 		return mapGrid[x][y];
 	}
 
 	// returns the BufferedImage of the map
 	public BufferedImage getMapImage() {
 		return mapImage;
 	}
 
 	// Creates a fresh BufferedImage if the map layout were to change
 	private void generateMapImage() {
 		mapImage = new BufferedImage(mapWidthPixels, mapHeightPixels,
 				BufferedImage.TYPE_INT_ARGB);
 		Graphics g = mapImage.createGraphics();
 		for (int i = 0; i < mapWidth; i++) {
 			for (int k = 0; k < mapHeight; k++) {
 				Tile temp = mapGrid[i][k];
 				int tempx = i * (temp.getWidth() * Game.SCALE);
 				int tempy = k * (temp.getHeight() * Game.SCALE);
 				g.drawImage(temp.getImage(), tempx, tempy, 
 						temp.getWidth() * Game.SCALE, 
 						temp.getHeight() * Game.SCALE,
 						null);
 			}
 		}
 		hasChanged = false;
 	}
 
 	// Changes a specific Tile in the map by Tile ID
 	public void changeTile(String img, int ID) {
 		// use black magic to find the location in the grid
 		mapGrid[(int) (Math.floor(ID / mapWidth))][(ID % mapWidth)] = new Tile(
 				img, ID);
 		hasChanged = true;
 	}
 	
 	// Changes a specific Tile in the map by x and y coordinate
 	public void changeTile(String img, int i, int k) {
 		mapGrid[i][k] = new Tile(img, ((i * mapHeight) + k));
 		hasChanged = true;
 	}
 
 	// WIP
 	public void tick() {
 
 	}
 
 	// If the map has changed, it renders a new map, else it simply ends
 	// 	Screen.java will call the Buffered map image later in the render loop
 	public void render() {
 		if (hasChanged)  {
 			generateMapImage();
 		}
 	}
 }

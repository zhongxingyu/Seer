 package ss.linearlogic.quizquest;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Scanner;
 
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.util.ResourceLoader;
 
 import ss.linearlogic.quizquest.entity.Door;
 import ss.linearlogic.quizquest.entity.Enemy;
 import ss.linearlogic.quizquest.entity.Entity;
 import ss.linearlogic.quizquest.entity.Floor;
 import ss.linearlogic.quizquest.entity.Grass;
 import ss.linearlogic.quizquest.entity.Wall;
 import ss.linearlogic.quizquest.item.Item;
 import ss.linearlogic.quizquest.item.Key;
 import ss.linearlogic.quizquest.item.Potion;
 import ss.linearlogic.quizquest.item.Spell;
 
 public class Map {
 	
 	/**
 	 * 2D array containing the type ID of the entity at each tile location in the map
 	 */
 	private static int map[][];
 	
 	/**
 	 * 2D array containing the Entity subclass object of the entity at each tile location in the map
 	 */
 	private static Entity entityMap[][];
 	
 	/**
 	 * An array list which holds all of the enemies
 	 */
 	private static ArrayList<Enemy> enemies = new ArrayList<Enemy>();
 	
 	/**
 	 * The width, in tiles, of the full map
 	 */
 	private static int width;
 	
 	/**
 	 * Height, in tiles, of the full map
 	 */
 	private static int height;
 	
 	/**
 	 * Width and height, in quadrants, of the full map
 	 */
 	private static int quadrant_count_width = 3;
 	
 	/**
 	 * X-coordinate of the quadrant the player and camera view are currently in
 	 */
 	private static int current_quadrant_x = 0;
 	
 	/**
 	 * Y-coordinate of the quadrant the player and camera view are currently in
 	 */
 	private static int current_quadrant_y = 0;
 	
 	/**
 	 * HashMap containing corresponding Texture ID and object
 	 */
 	private static HashMap<Integer, Texture> textures = new HashMap<Integer, Texture>();
 	
 	/**
 	 * Size, in pixels, of a map tile
 	 */
 	private static int tileSize;
 	
 	/**
 	 * Unused for the time being (stub of a constructor that enables multiple maps)
 	 * 
 	 * @param filename The .txt file from which to load Map specifications and entities
 	 */
 	public Map(String filename) {}
 	
 	/**
 	 * Loads the loads the Map with the specifications and entity types and locations provided in the Map file provided
 	 * @param filename The .txt file from which to load Map specifications and entities
 	 */
 	public static void initialize(String filename, String entityFilename) {
  		loadMapFile(filename);
  		loadEnemyEntityFile(entityFilename);
 	}
 
 	
 	/**
 	 * Renders the current map segment (renders all entity tiles as textured rectangles), adjusting the camera view as necessary
 	 */
 	public static void render() {
 		//Get the players position
 		int start_coordinate_x = getCurrentQuadrantX() * 15;
 		int start_coordinate_y = getCurrentQuadrantY() * 15;
 		
 		//Get the coordinate shift to keep rendering inside the camera
 		int coordinate_shift_x = getCoordinateShiftX();
 		int coordinate_shift_y = getCoordinateShiftY();
 				
 		for (int x = start_coordinate_x; x < (start_coordinate_x + 15); ++x) {
 			for (int y = start_coordinate_y; y < (start_coordinate_y + 15); ++y) {
 				//2 Layers of rendering, the base layer being the grass and the top layer being the objects like doors and walls, etc.
 				Renderer.renderTexturedRectangle((x * tileSize) - coordinate_shift_x, (y * tileSize) - coordinate_shift_y, tileSize, tileSize, textures.get(0));
 				
 				if (map[x][y] == 0)
 					continue;
 				
 				//Render the top layer such as walls and doors
 				Renderer.renderTexturedRectangle((x * tileSize) - coordinate_shift_x, (y * tileSize) - coordinate_shift_y, tileSize, tileSize, textures.get(map[x][y]));
 			}
 		}
 		
 		/*for (Enemy enemy: enemies) {
 			Renderer.renderColoredRectangle((enemy.getX() * 32) - coordinate_shift_x, (enemy.getY() * 32) - coordinate_shift_y, 32, 32, 1.0, 0.0, 0.0);
 		}*/
 	}
 	
 	/**
 	 * @return The width, in entity tiles, of the full map
 	 */
 	public static int getWidth() { return width; }
 	
 	/**
 	 * @return The height, in entity tiles, of the full map
 	 */
 	public static int getHeight() { return height; }
 	
 	/**
 	 * Sets x-coordinate ({@link #current_quadrant_x}) of current quadrant of the camera
 	 * @param quadrantX
 	 */
 	public static void setCurrentQuadrantX(int quadrantX) {
 		int current_temp = current_quadrant_x;
 		current_quadrant_x = quadrantX;
 		
 		if ((getCurrentQuadrantX() < 0) || (getCurrentQuadrantX() > getQuadrantWidth())) {
 			current_quadrant_x = current_temp;
 			return;
 		}
 	}
 	
 	/**
 	 * Sets y-coordinate ({@link #current_quadrant_y}) of current quadrant of the camera
 	 * @param quadrantY
 	 */
 	public static void setCurrentQuadrantY(int quadrantY) {
 		int current_temp = current_quadrant_y;
 		current_quadrant_y = quadrantY;
 		
 		if ((getCurrentQuadrantY() < 0) || getCurrentQuadrantY() > getQuadrantWidth()) {
 			current_quadrant_y = current_temp;
 			return;
 		}
 	}
 	
 	/**
 	 * @return The x-coordinate ({@link #current_quadrant_x}) of current quadrant of the camera
 	 */
 	public static int getCurrentQuadrantX() {
 		return (int) current_quadrant_x;
 	}
 	
 	/**
 	 * @return The y-coordinate ({@link #current_quadrant_y}) of current quadrant of the camera
 	 */
 	public static int getCurrentQuadrantY() {
 		return (int) current_quadrant_y;
 	}
 	
 	/**
 	 * @return The width and height, in quadrants, of the full map
 	 */
 	public static int getQuadrantWidth() {
 		return quadrant_count_width;
 	}
 	
 	/**
 	 * @return The horizontal camera shift to the right, in pixels
 	 */
 	public static int getCoordinateShiftX() {
 		return (int)(getCurrentQuadrantX() * 480);
 	}
 	
 	/**
 	 * @return The vertical camera shift downward, in pixels
 	 */
 	public static int getCoordinateShiftY() {
 		return (int)(getCurrentQuadrantY() * 480);
 	}
 	
 	/**
 	 * @return The width (and height, as tiles are square) of an entity tile
 	 */
 	public static int getTileSize() { return tileSize; }
 	
 	/**
 	 * Add a corresponding Texture object and ID to the {@link #textures} HashMap
 	 * 
 	 * @param filename
 	 * @param GID
 	 */
 	public static void addTexture(String filename, int GID) {
 		Texture texture = null;
 		
 		//Read the texture object from the string
 		try {
 			texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream(filename));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		//Put the tile into the textures HashMap
 		if (texture != null) textures.put(GID, texture);
 		else System.out.println("Texture is null, unable to add to array");
 	}
 	
 	/**
 	 * Load the Map specifications and initializes the maplill and entityMap 2D arrays using the entity IDs in the provided map .txt file
 	 * @param filename The .txt file from which to load Map specifications and entities
 	 */
 	private static void loadMapFile(String filename) { 
 		Scanner textReader = null;
 		
 		//Read the file from the filesystem
 		try {
 			textReader = new Scanner(new File(filename));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		width = textReader.nextInt();
 		height = textReader.nextInt();
 		
 		//The next two ints are only required by the map editor
 		quadrant_count_width = textReader.nextInt();
 		textReader.nextInt();
 				
 		tileSize = textReader.nextInt();
 		
 		map = new int[width][height];
 		entityMap = new Entity[width][height];
 		
 		for (int x = 0; x < width; ++x) {
 			for (int y = 0; y < height; ++y) {
 				int typeID = textReader.nextInt();
 				switch(typeID) {
 					default:
 					case 0: //Grass
 						addEntity(new Grass(x, y));
 						break;
 					case 1: //Door
 						addEntity(new Floor(x, y));
 						break;
 					case 2: //Wall
 						addEntity(new Wall(x, y));
 						break;
 					case 3: //Door
 						addEntity(new Door(1, x, y));
 						break;
 					case 4: //Enemy
 						addEntity(new Enemy(4, 20, x, y));
 						break;
 				}
 			}
 		}
 		textReader.close();
 	}
 	
 	/**
 	 * Adds the supplied Entity subclass to the {@link #entityMap} 2D array (and its type ID to the {@link #map} 2D array).
 	 * @param entity
 	 */
 	public static void addEntity(Entity entity) {
 		int x = entity.getX();
 		int y = entity.getY();
 		map[x][y] = entity.getTypeID();
 		entityMap[x][y] = entity;
 	}
 	
 	/**
 	 * Remove the Entity subclass at the specified location
 	 * @param x
 	 * @param y
 	 */
 	public static void removeEntity(int x, int y) {
 		if (map[x][y] != 0)
 			map[x][y] = 0;
 		if (entityMap[x][y] != null)
 			entityMap[x][y] = new Grass(x, y);
 	}
 	
 	public static void removeEnemy(Enemy enemy) {
 		removeEntity(enemy.getX(), enemy.getY());
 		enemies.remove(enemy);
 	}
 	
 	/**
 	 * Gets an entity in the map
 	 * @param x The x-coordinate of the Entity
 	 * @param y The y-coordinate of the Entity
 	 * @return The Entity subclass at the supplied location
 	 */
 	public static Entity getEntity(int x, int y) {
 		return entityMap[x][y];
 	}
 	
 	//Do not touch this for the time being
 	private static void loadEnemyEntityFile(String filename) {
 		Scanner textReader = null;
 		
 		try {
 			textReader = new Scanner(new File(filename));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		int enemyEntityCount = textReader.nextInt();
 		
 		for (int i = 0; i < enemyEntityCount; ++i) {
 			int maxHP = textReader.nextInt();
 			int damage = textReader.nextInt();
 			int itemNo = textReader.nextInt();
 			
 			int itemAttribute = textReader.nextInt();
 			
 			Item item = null;
 			
 			switch (itemNo) {
 				case 1:
 					item = new Key(itemAttribute, 1);
 					break;
 				case 2:
 					item = new Potion(itemAttribute, 1);
 					break;
 				case 3:
 					item = new Spell(itemAttribute, 1);
 					break;
 				default:
 					item = new Item(itemNo, 1);
 					break;
 			}
 			
 			int x = textReader.nextInt();
 			int y = textReader.nextInt();
 			
 			int qID = textReader.nextInt();
 									
 			Enemy enemy = new Enemy(damage, maxHP, item, x, y, qID);
 			addEntity(enemy);
 			
 			enemies.add(enemy);
 		}
 		
 		textReader.close();
 	}
 }

 package fi.ringofsnake.entities;
 
 import java.util.HashMap;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import fi.ringofsnake.io.ResourceManager;
 
 public class SnakeMap implements IGameObject {
 		
 	private Player player;
 	
 	public float tunnelHorizontalOffset = 0;	
 	public float tunnelSpeed = 0.5f;
 	
 	public static enum Tileset { 
 		SPACE, 
 		SNAKE_LEFT, SNAKE_UPPER, SNAKE_RIGHT, SNAKE_LOWER, SNAKE, 
 		CORNER_UL, CORNER_UR, CORNER_LR, CORNER_LL,
 		NARROW_CORNER_UL, NARROW_CORNER_UR, NARROW_CORNER_LR, NARROW_CORNER_LL,
 		STRAIGHT,
 		NARROW,
 		STRAIGHT_NARROW_LR, STRAIGHT_NARROW_UL, STRAIGHT_NARROW_RL, STRAIGHT_NARROW_LU
 	}
 	
 	private Tileset[][] map;
 	
 	private int width, height;
 	
 	private static int DEFAULT_MAP_SIZE = 3;
 	
 	private HashMap<Tileset, Tile> tiles = new HashMap<Tileset, Tile>();
 	
 	private HashMap<Tileset, Tile> createTileSet()
 	{
 
 		tiles.put(Tileset.SPACE, new Tile(ResourceManager.fetchImage("TILE_SPACE")));
 		tiles.put(Tileset.STRAIGHT, new Tile(ResourceManager.fetchImage("STRAIGHT")));
 		//tiles.put(Tileset.STRAIGHT_NARROW_LR, new Tile(ResourceManager.fetchImage("STRAIGHT")));	//TODO
 		tiles.put(Tileset.CORNER_UL, new Tile(ResourceManager.fetchImage("CORNER"), true, true));
 		tiles.put(Tileset.CORNER_UR, new Tile(ResourceManager.fetchImage("CORNER"), false, true));
 		tiles.put(Tileset.CORNER_LR, new Tile(ResourceManager.fetchImage("CORNER"), false, false));
 		tiles.put(Tileset.CORNER_LL, new Tile(ResourceManager.fetchImage("CORNER"), true, false));
 
 		return tiles;
 	}
 		
 	public SnakeMap(int w, int h, Player player)
 	{
 		this.player = player;		
 		width = w; height = h;
 		map = new Tileset[w][h];
 	
 		for (int i = 0; i < width; i++) {				
 			for (int j = 0; j < height; j++) {
 				map[i][j] = Tileset.SPACE;
 			}
 		}
 		
 		for (int i = 0; i < width; i++) {
 			map[i][1] = Tileset.STRAIGHT;
 		}
 			
 		createTileSet();
 	}	
 
 	public SnakeMap()
 	{
 		this(DEFAULT_MAP_SIZE, DEFAULT_MAP_SIZE, null ); 
 		createTileSet();
 	}
 	
 	public Tile getTile(int x, int y) {
 		assert(x >= 0 && x < width
 			&& y >= 0 && y < height);
 		return tiles.get(map[x][y]);
 	}
 	
 	public Tile getTile(Tileset c) {
 		return tiles.get(c);
 	}
 	
 	@Override
 	public void update(GameContainer cont, int delta) throws SlickException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void render(GameContainer cont, Graphics g) throws SlickException {		
 		for( int x = 0; x < width; x++ ) {
 			for( int y = 0; y < height; y++ ) {				
				// TODO: y-axis
				if( tunnelHorizontalOffset - cont.getWidth() - Tile.TILE_WIDTH <= x * Tile.TILE_WIDTH 
					&& tunnelHorizontalOffset + cont.getWidth() * 2 + Tile.TILE_WIDTH >= x * Tile.TILE_WIDTH   
					&& map[x][y] != Tileset.SPACE ) {
 					tiles.get(map[x][y]).render(g, x * Tile.TILE_WIDTH, y * Tile.TILE_HEIGHT);
				}
			}
		}
 	}
 	
 	public int getWidth() {
 		return width;
 	}
 	
 	public int getHeight() {
 		return height;
 	}
 }

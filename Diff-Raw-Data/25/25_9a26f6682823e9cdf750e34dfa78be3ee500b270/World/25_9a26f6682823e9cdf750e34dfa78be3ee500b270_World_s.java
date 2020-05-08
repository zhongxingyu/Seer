 package ld27jam;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import ld27jam.entities.Entity;
 import ld27jam.entities.Hourglass;
 import ld27jam.entities.Inventory;
 import ld27jam.entities.Item;
 import ld27jam.entities.ItemType;
 import ld27jam.entities.Tile;
 import ld27jam.entities.TileType;
 import ld27jam.res.ImageSheet;
 import ld27jam.spatialData.AABB;
 import ld27jam.spatialData.Region;
 import ld27jam.spatialData.SpatialMap;
 import ld27jam.entities.Character;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class World
 {
 	public GameDirector gd;
 	
 	public static final Vector2f SCREEN_TILE_SIZE = new Vector2f(32, 16);
 	public static final Vector2f SCREEN_HALF_TILE = new Vector2f(16, 8);
 	public static final int WALL_HEIGHT = 80;
 	
 	private Set<Entity> entities = new HashSet<Entity>();
 	public SpatialMap<Entity> spatialMap = new SpatialMap<Entity>();
 	private Tile[][] grid;
 	private ImageSheet tileSheet, wallSheet;
 	private Character character;
 	private Inventory inventory = new Inventory();
 	private Hourglass hourglass = new Hourglass();
 	
 	public void add(Entity entity)
 	{
 		entities.add(entity);
 		spatialMap.put(entity);
 	}
 	public void remove(Entity entity)
 	{
 		entities.remove(entity);
 		spatialMap.remove(entity);
 	}
 	
 	public Set<Entity> getEntitiesInRegion(AABB region)
 	{
 		return spatialMap.get(region);
 	}
 	public Set<Tile> getTilesInRegion(AABB region)
 	{
 		Set<Tile> tiles = new HashSet<Tile>();
 		
 		int minX = (int) Math.floor(region.getPosition().x),
 		    maxX = (int) Math.ceil(region.getRightX()),
 		    minY = (int) Math.floor(region.getPosition().y),
 		    maxY = (int) Math.ceil(region.getBottomY());
 		for (int x = minX; x < maxX; x++)
 			for (int y = minY; y < maxY; y++)
 				tiles.add(grid[x][y]);
 		return tiles;
 	}
 	
 	public static Vector2f getScreenCoordinates(Vector2f mapCoordinates)
 	{
 		return new Vector2f((mapCoordinates.x - mapCoordinates.y) * SCREEN_HALF_TILE.x,
 							(mapCoordinates.x + mapCoordinates.y) * SCREEN_HALF_TILE.y);
 	}
 	public Vector2f getMapCoordinates(Vector2f pScreenCoordinates)
 	{
 		return null;
 	}
 	
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException 
 	{
 		int xsize = gd.level.getWidth();
 		int ysize = gd.level.getHeight();
 		grid = new Tile[xsize][ysize];
 		for	(int x = 0; x < xsize; x++)
 		{
 			for (int y = 0; y < ysize; y++) 
 			{
 				TileType tileType = gd.level.getCell(x, y);
 				grid[x][y] = new Tile(tileType, x, y);
 			}
 		}
 		character = new Character(gd.level.dungeon.getClosestFreeCell(xsize / 2, xsize / 2));
 		add(character);
 		character.init(this);
 		inventory.items.add(new Item(ItemType.Key));
 		inventory.items.add(new Item(ItemType.Key));
 		inventory.items.add(new Item(ItemType.Key));
 		inventory.items.add(new Item(ItemType.Key));
 	}
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException
 	{
 		AABB region = new Region(new Vector2f(), new Vector2f(1, 1));
 		
 		Vector2f halfScreen = new Vector2f(gc.getWidth() / 2, gc.getHeight() / 2);
 		
 		Vector2f camera = getScreenCoordinates(new Vector2f(character.getPosition().x, character.getPosition().y)).add(SCREEN_HALF_TILE).sub(halfScreen);
 		camera.x = Math.round(camera.x);
 		camera.y = Math.round(camera.y);
 		
 		Vector2f characterPosUnaltered = World.getScreenCoordinates(character.getPosition()).sub(camera);
 		Vector2f characterPos = characterPosUnaltered.copy().add(new Vector2f(0, SCREEN_TILE_SIZE.y));
 		
 		AABB characterSquare = new Region(characterPos, new Vector2f(SCREEN_TILE_SIZE.x, WALL_HEIGHT - SCREEN_HALF_TILE.y));
 		
 		int projectionHeight = (int)Math.ceil((camera.y + WALL_HEIGHT + gc.getHeight()) / SCREEN_HALF_TILE.y) + 1;
 		for (int projection = (int)Math.floor(camera.y / SCREEN_HALF_TILE.y) - 1; 
 			 projection < grid.length + grid[0].length && projection < projectionHeight; 
 			 projection++)
 		{
 			for (int x = Math.min(projection, grid.length - 1), y = Math.max(0, projection - (grid.length - 1));
 				 x >= 0 && y < grid[x].length; 
 				 y++, x--)
 			{
 				// Do not draw stuff outside the camera
 				Vector2f tilePos = getScreenCoordinates(new Vector2f(x, y)).sub(camera);
 				if (tilePos.x < - SCREEN_TILE_SIZE.x)
 					break;
 				if (tilePos.x > gc.getWidth() + SCREEN_TILE_SIZE.x)
 					continue;
 
				
 				// Tile
 				Tile tile = grid[x][y];
 				if (tile != null && tile.type != TileType.None)
 				{
					float blackness = tilePos.distance(characterPosUnaltered) / character.lightBase + character.lightVariation;
 					if (tile.type.isFloor)
 					{
 						tileSheet.setFrameX(tile.type.tileId);
 						tileSheet.getColor().r = Math.max(20f / 255f, character.lightColor.r - blackness);
 						tileSheet.getColor().g = Math.max(20f / 255f, character.lightColor.g - blackness);
 						tileSheet.getColor().b = Math.max(30f / 255f, character.lightColor.b - blackness);
 						tileSheet.getColor().a = 1.2f - blackness * blackness * blackness;
 						tileSheet.render(tilePos);
 					}
 					else
 					{
 						wallSheet.setFrameX(tile.type.tileId);
 						wallSheet.getColor().r = Math.max(20f / 255f, character.lightColor.r - blackness);
 						wallSheet.getColor().g = Math.max(20f / 255f, character.lightColor.g - blackness);
 						wallSheet.getColor().b = Math.max(30f / 255f, character.lightColor.b - blackness);
 						if (characterSquare.containsRegion(new Region(tilePos, SCREEN_TILE_SIZE)))
 							wallSheet.getColor().a = tilePos.distance(characterPosUnaltered) / 100f;
 						else
 							wallSheet.getColor().a = 1.2f - blackness * blackness * blackness;
 						tilePos.y -= WALL_HEIGHT - SCREEN_TILE_SIZE.y;
 						wallSheet.render(tilePos);
 						
 						
 					}
 				}
 				
 				region.getPosition().x = x;
 				region.getPosition().y = y;
 
 				// Entities
 				for (Entity entity : spatialMap.get(region))
 				{
 					Vector2f bottomRight = entity.getBottomRightPoint();
 					if (region.getPosition().x < bottomRight.x &&
 						region.getRightX() >= bottomRight.x &&
 						region.getPosition().y < bottomRight.y &&
 						region.getBottomY() >= bottomRight.y)
						entity.render(gc, sbg, g, camera);
 				}
 			}
 		}
 		
 		hourglass.render(gc, character.sanity / character.maxSanity);
 		inventory.render();
 	}
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException 
 	{
 		for (Entity entity : entities)
 			entity.update(gc, sbg, delta, this);
 	}
 	
 	public void load(int level)
 	{
 		gd.level = new GameLevel(level);
 	}
 	
 	public World(GameDirector gd) throws SlickException
 	{
 		this.gd = gd;
 		tileSheet = new ImageSheet(gd.spriteSheetLocation, (int)SCREEN_TILE_SIZE.x, (int)SCREEN_TILE_SIZE.y);
 		wallSheet = new ImageSheet(gd.wallSheetLocation, (int)SCREEN_TILE_SIZE.x, WALL_HEIGHT);
 		
 	}
 }

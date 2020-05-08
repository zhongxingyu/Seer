 package Level;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 
 import Entity.Entity;
 import Entity.Player;
 import Entity.TestMob;
 import Item.Item;
 
 public class LevelHandler {
 
 	private Player player;
 	private TestMob testMob;
 	
 	private TiledMap map;
 	private int mapId;
	private String mapString = "/res/maps/Map_" + mapId;
	private int mapOffsetX = 0;
	private int mapOffsetY = 0;
	
	public int getMapOffsetX() {
		return mapOffsetX;
	}

	public int getMapOffsetY() {
		return mapOffsetY;
	}
 
 	private List<Entity> entities = new ArrayList<Entity>();
 	private List<Item> items = new ArrayList<Item>();
 	private List<Rectangle> tileHitBoxes = new ArrayList<Rectangle>();
 	
 	public LevelHandler() {
 		
 	}
 	
 	public synchronized List<Rectangle> getTileHitBoxes() {
 		return tileHitBoxes;
 	}
 	
 	public synchronized List<Entity> getEntities() {
 		return entities;
 	}
 	
 	public synchronized List<Item> getItems() {
 		return items;
 	}
 	
 	public TiledMap getMap() {
 		return map;
 	}
 	
 	public int getMapId() {
 		return mapId;
 	}
 	
 	public Entity getPlayer() {
 		return player;
 	}
 	
 	public void init(GameContainer container, StateBasedGame game) throws SlickException {
 		player = new Player(10, 10);
 		testMob = new TestMob(100, 100, 5);
 		entities.add(testMob);
 		entities.add(player);
 		try {
 			map = new TiledMap("/res/maps/DesertMap.tmx");
 		} catch(SlickException e) {
 			e.printStackTrace();
 		}
 		//map = new TiledMap(mapString);
 		for(Item item : items) {
 			item.init(container, game);
 		}
 		
 		for(Entity entity : entities) {
 			entity.init(container, game);
 		}
 	}
 
 	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
 		map.render(0, 0);
 		
 		drawHitBoxes(g);
 		
 		for(Item item : items) {
 			item.render(container, game, g);
 		}
 		
 		for(Entity entity : entities) {
 			entity.render(container, game, g);
 		}
 	}
 
 	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
 		for(Item item : items) {
 			item.update(container, game, delta);
 		}
 		
 		for(Entity entity : entities) {
 			entity.update(container, game, delta);
 		}
 	}
 	
 	public void drawHitBoxes(Graphics g) {
 		if(!tileHitBoxes.isEmpty()) {
 			for(int i = 0; i < tileHitBoxes.size(); i++) {
 				tileHitBoxes.remove(i);
 			}
 		}
 		int collisionLayer = map.getLayerIndex("Collision");
 		
 		for(int i = 0; i < map.getWidth(); i++) {
 			for(int j = 0; j < map.getHeight(); j++) {
 				int tileId = map.getTileId(i, j, collisionLayer);
 				
 				if("true".equals(map.getTileProperty(tileId, "wall", "a"))) {
					tileHitBoxes.add(new Rectangle((i * 32) + mapOffsetX, (j * 32) + mapOffsetY, 32, 32));
 				}
 			}
 		}
 		
		/*g.setColor(Color.green);
 		for(Rectangle rec : tileHitBoxes) {
 			g.draw(rec);
		}*/
	}
	
	public void translateMap(float x, float y) {
		mapOffsetX += (int)x;
		mapOffsetY += (int)y;
 	}
 }

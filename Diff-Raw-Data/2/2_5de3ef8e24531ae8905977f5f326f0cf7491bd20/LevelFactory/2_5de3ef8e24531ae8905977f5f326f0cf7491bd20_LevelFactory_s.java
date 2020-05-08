 package rsmg.levelfactory;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jdom.Document;
 import org.jdom.Element;
 
 import rsmg.io.Levels;
 import rsmg.model.Level;
 import rsmg.model.TileGrid;
 import rsmg.model.ai.Ai;
 import rsmg.model.ai.BallBotAi;
 import rsmg.model.ai.BossBotAi;
 import rsmg.model.ai.BucketBotAi;
 import rsmg.model.ai.EmptyAi;
 import rsmg.model.ai.RocketBotAi;
 import rsmg.model.ai.TankBotAi;
 import rsmg.model.object.bullet.Bullet;
 import rsmg.model.object.item.HealthPack;
 import rsmg.model.object.item.Item;
 import rsmg.model.object.item.Pistol;
 import rsmg.model.object.item.RocketLauncher;
 import rsmg.model.object.item.Shotgun;
 import rsmg.model.object.item.UpgradePoints;
 import rsmg.model.object.unit.BallBot;
 import rsmg.model.object.unit.BossBotHead;
 import rsmg.model.object.unit.BucketBot;
 import rsmg.model.object.unit.PCharacter;
 import rsmg.model.object.unit.RocketBot;
 import rsmg.model.object.unit.Spikes;
 import rsmg.model.object.unit.TankBot;
 import rsmg.model.tile.AirTile;
 import rsmg.model.tile.EndTile;
 import rsmg.model.tile.GroundTile;
 import rsmg.model.tile.SpawnTile;
 import rsmg.model.tile.Tile;
 import rsmg.model.variables.Constants;
 import rsmg.model.variables.ObjectName;
 
 /**
  * This class is responsible for creating the levels. It asks the class Levels i
  * the io package for a level XML file, and then it converts it into a working
  * Level.
  * 
  * @author Johan Rigns, Daniel Jonsson
  * 
  */
 public final class LevelFactory {
 
 	/*
 	 * These maps are needed to bridge a String to an Enum. The XML document
 	 * does of course contain Strings, and we want to use switch statements when
 	 * walking through those objects. However, we also want the project to be
 	 * compatible with Java 1.6, and in 1.6 is switch statements with Strings
 	 * not supported.
 	 */
 	@SuppressWarnings("serial")
 	private static Map<String, ObjectName> tiles = new HashMap<String, ObjectName>() {{
 		put("AirTile", ObjectName.AIR_TILE);
 		put("GroundTile", ObjectName.BOX_TILE1);
 		put("GroundTile2", ObjectName.BOX_TILE2);
 		put("GroundTile3", ObjectName.BOX_TILE3);
 		put("GroundTile4", ObjectName.BOX_TILE4);
 		put("SpawnTile", ObjectName.SPAWN_TILE);
 		put("EndTile", ObjectName.END_TILE);
 	}};
 	
 	@SuppressWarnings("serial")
 	private static Map<String, ObjectName> items = new HashMap<String, ObjectName>() {{
 		put("healthPack", ObjectName.HEALTH_PACK);
 		put("upgradePoint", ObjectName.UPGRADE_POINT);
		put("laserPistol", ObjectName.LASER_BULLET);
 		put("shotgun", ObjectName.SHOTGUN);
 		put("rocketLauncher", ObjectName.ROCKET_LAUNCHER);
 	}};
 	
 	@SuppressWarnings("serial")
 	private static Map<String, ObjectName> enemies = new HashMap<String, ObjectName>() {{
 		put("tankbot", ObjectName.TANKBOT);
 		put("rocketbot", ObjectName.ROCKETBOT);
 		put("ballbot", ObjectName.BALLBOT);
 		put("bucketbot", ObjectName.BUCKETBOT);
 		put("bossbot", ObjectName.BOSSBOT);
 		put("spikes", ObjectName.SPIKES);
 	}};
 	
 	/**
 	 * Get a working, playable Level.
 	 * 
 	 * @param levelNumber
 	 *            The level's number.
 	 * @return A Level.
 	 */
 	public static Level getLevel(int levelNumber) {
 
 		// Prepare some variables that Level's constructor will need.
 		Tile[][] grid;
 		List<Item> itemList = new ArrayList<Item>();
 		List<Ai> aiList = new ArrayList<Ai>();
 		List<Bullet> enemyBulletList = new ArrayList<Bullet>();
 		List<Bullet> alliedBulletList = new ArrayList<Bullet>();
 		PCharacter character = new PCharacter(alliedBulletList);
 		
 		// Get the document from the io package.
 		Document document = Levels.getLevel(levelNumber);
 		
 		// Get the root node.
 		Element rootNode = document.getRootElement();
 
 		// Set the size of tile tile grid.
 		Element sizeElem = rootNode.getChild("size");
 		int height = Integer.parseInt(sizeElem.getChildText("height"));
 		int width = Integer.parseInt(sizeElem.getChildText("width"));
 		grid = new Tile[height][width];
 
 		// Walk through all row nodes in the document. These row nodes defines
 		// how the tile grid should be constructed.
 		@SuppressWarnings("unchecked")
 		List<Element> rows = rootNode.getChildren("row");
 		for (int y = 0; y < rows.size(); y++) {
 			
 			// Walk through all cells on the current row.
 			Element row = rows.get(y);
 			@SuppressWarnings("unchecked")
 			List<Element> cells = row.getChildren("cell");
 			for (int x = 0; x < cells.size(); x++) {
 
 				// Retrieve tile
 				Element cell = (Element) cells.get(x);
 				String cellValue = cell.getText();
 				Tile tile;
 				switch (tiles.get(cellValue)) {
 					case AIR_TILE :
 						tile = new AirTile();
 						break;
 					case BOX_TILE1 :
 						tile = new GroundTile(ObjectName.BOX_TILE1);
 						break;
 					case BOX_TILE2 :
 						tile = new GroundTile(ObjectName.BOX_TILE2);
 						break;
 					case BOX_TILE3 :
 						tile = new GroundTile(ObjectName.BOX_TILE3);
 						break;
 					case BOX_TILE4 :
 						tile = new GroundTile(ObjectName.BOX_TILE4);
 						break;
 					case SPAWN_TILE :
 						tile = new SpawnTile();
 						break;
 					case END_TILE :
 						tile = new EndTile();
 						break;
 					default :
 						tile = new AirTile();
 						break;
 				}
 				grid[y][x] = tile;
 				
 				int scale = Constants.TILESIZE; // Used to place items and enemies on appropriate positions.
 				// Retrieve eventual item
 				String itemValue = cell.getAttributeValue("item");
 				if (itemValue != null) {
 					Item item;
 					switch (items.get(itemValue)) {
 						case HEALTH_PACK :
 							item = new HealthPack(x*scale,y*scale);
 							break;
 						case UPGRADE_POINT :
 							item = new UpgradePoints(x*scale,y*scale);
 							break;
 						case LASER_PISTOL :
 							item = new Pistol(x*scale,y*scale, 15, 15);
 							break;
 						case SHOTGUN :
 							item = new Shotgun(x*scale,y*scale, 15, 15);
 							break;
 						default :
 							item = new RocketLauncher(x*scale,y*scale, 28, 15);
 							break;
 					}
 					itemList.add(item);
 				}
 				
 				// Retrieve Enemies and assign AI to them
 				String enemyValue = cell.getAttributeValue("enemy");
 				if (enemyValue != null) {
 					Ai ai;
 					switch (enemies.get(enemyValue)) {
 						case TANKBOT :
 							ai = new TankBotAi(new TankBot(x*scale, y*scale, enemyBulletList), character);
 							break;
 						case ROCKETBOT :
 							ai = new RocketBotAi(new RocketBot(x*scale, y*scale), character);
 							break;
 						case BALLBOT :
 							ai = new BallBotAi(new BallBot(x*scale, y*scale), aiList, character);
 							break;
 						case BUCKETBOT :
 							ai = new BucketBotAi(new BucketBot(x*scale, y*scale, enemyBulletList), character);
 							break;
 						case BOSSBOT :
 							ai = new BossBotAi(new BossBotHead(x*scale, y*scale, enemyBulletList), character);
 							break;
 						default :
 							ai = new EmptyAi(new Spikes(x*scale, y*scale));
 							break;
 					}
 					aiList.add(ai);
 				}
 			}
 		}
 		
 		return new Level(new TileGrid(grid), character, itemList, aiList, enemyBulletList, alliedBulletList);
 	}
 }

 package rsmg.io;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 import rsmg.model.Constants;
 import rsmg.model.ObjectName;
 import rsmg.model.object.item.HealthPack;
 import rsmg.model.object.item.Item;
 import rsmg.model.object.item.UpgradePoints;
 import rsmg.model.object.item.Weapon;
 import rsmg.model.object.unit.Enemy;
 import rsmg.model.object.unit.Tankbot;
 import rsmg.model.tile.AirTile;
 import rsmg.model.tile.EndTile;
 import rsmg.model.tile.GroundTile;
 import rsmg.model.tile.SpawnTile;
 import rsmg.model.tile.Tile;
 
 /**
  * Converts to/from XML to/from Tile[][]
  */
 public class XmlConverter {
 
 	private List<Item> itemList = new ArrayList<Item>();
 	private List<Enemy> enemyList = new ArrayList<Enemy>();
 	/**
 	 * Converts the XML file to Tile matrix by setting appropriate Tiles in the
 	 * matrix according to the XML file
 	 * 
 	 * @param file
 	 *            The XML file that are going to be converted to Tiles
 	 * @return A Tile matrix. If error: null
 	 */
 	public Tile[][] xmlToTiles(File xmlFile) {
 		SAXBuilder builder = new SAXBuilder();
 		Tile[][] grid;
 		try {
 
 			Document document = (Document) builder.build(xmlFile);
 			Element rootNode = document.getRootElement();
 
			List sizeList = rootNode.getChildren("size");
			Element sizeElem = (Element) sizeList.get(0);
 			int height = Integer.parseInt(sizeElem.getChildText("height"));
 			int width = Integer.parseInt(sizeElem.getChildText("width"));
 			grid = new Tile[height][width];
 
			List rows = rootNode.getChildren("row");
 			for (int y = 0; y < rows.size(); y++) {
 				Element cellsElem = (Element) rows.get(y);
				List cells = cellsElem.getChildren("cell");
 
 				for (int x = 0; x < cells.size(); x++) {
 
 					// Retrieve Tiles
 					Element cell = (Element) cells.get(x);
 					String cellValue = cell.getText();
 					Tile tile;
 					if (cellValue.equals("AirTile"))
 						tile = new AirTile();
 					else if (cellValue.equals("GroundTile"))
 						tile = new GroundTile();
 					else if (cellValue.equals("SpawnTile"))
 						tile = new SpawnTile();
 					else if (cellValue.equals("EndTile"))
 						tile = new EndTile();
 					else
 						tile = new AirTile();
 					grid[y][x] = tile;
 					
 					int scale = Constants.TILESIZE;
 					// Retrieve Items
 					String itemValue = cell.getAttributeValue("item");
 					if(itemValue != null){
 						Item item;
 						if(itemValue.equals(ObjectName.HEALTH_PACK))
 							item = new HealthPack(x*scale,y*scale);
 						else if(itemValue.equals(ObjectName.UPGRADE_POINT))
 							item = new UpgradePoints(x*scale,y*scale);
 						else if(itemValue.equals(ObjectName.LASER_PISTOL))
 							item = new Weapon(x*scale,y*scale, 15, 15, ObjectName.LASER_PISTOL);
 						else
 							item = new Weapon(x*scale,y*scale, 20, 15, ObjectName.ROCKET_LAUNCHER);
 						itemList.add(item);
 					}
 					
 					// Retrieve Enemies(ONLY TANKBOT IMPLEMENTED)
 					String enemyValue = cell.getAttributeValue("enemy");
 					if(enemyValue != null){
 						Enemy enemy;
 						if(enemyValue.equals("ballbot"))
 							enemy = new Tankbot(x*scale,y*scale);
 						else if(enemyValue.equals(ObjectName.TANKBOT))
 							enemy = new Tankbot(x*scale,y*scale);
 						else
 							enemy = new Tankbot(x*scale,y*scale);
 						enemyList.add(enemy);
 					}
 				}
 			}
 			return grid;
 		} catch (IOException io) {
 			System.out.println(io.getMessage());
 		} catch (JDOMException jdomex) {
 			System.out.println(jdomex.getMessage());
 		}
 		System.out.println("error!!");
 		return null;
 	}
 			
 	public List<Item> getItemList(){
 		return itemList;
 	}
 	
 	public List<Enemy> getEnemyList(){
 		return enemyList;
 	}
 }

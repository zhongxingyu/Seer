 package se.chalmers.kangaroo.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.kangaroo.constants.Constants;
 import se.chalmers.kangaroo.io.FileToMap;
 
 /**
  * This is a class for representing a map. It consists of a matrix of Tiles. The
  * map has tiles but also other things; creatures, items and iteractiveobjects
  * 
  * @author alburgh
  * 
  */
 public class GameMap {
 
 	/* The map represented by a matrix of tiles */
 	private Tile[][] map;
 	private List<InteractiveObject> iObjects = new ArrayList<InteractiveObject>();
 	private List<Item> items = new ArrayList<Item>();
 	private List<Creature> creatures = new ArrayList<Creature>();
 	
 	private Item[] sItems;
 	private Creature[] sCreatures;
 
 	/**
 	 * Creates a GameMap with the level name. The level name should be the path
 	 * to the file. Creates all tiles along with items and creatures.
 	 */
 	public GameMap(String level) {
 		super();
 		int[][] tiles = FileToMap.readTmxFileToMap(level);
 		String itemList = Constants.ITEM_IDS;
 		String creatureList = Constants.CREATURE_IDS;
 		String iObjectsList = Constants.IOBJECTS_IDS;
 		if (tiles != null)
 			map = new Tile[tiles.length][tiles[0].length];
 		else
 			System.out.println("HEJ");
 		Factory tf = new Factory();
 		for (int i = 0; i < map.length; i++)
 			for (int j = 0; j < map[0].length; j++) {
 				map[i][j] = tf.createTile((tiles[i][j]), i, j);
 				if (itemList.contains(" " + tiles[i][j]+" "))
 					items.add(tf.createItem(tiles[i][j], i, j));
 				if (creatureList.contains(" "+tiles[i][j]+" "))
					creatures.add(tf.createCreature(tiles[i][j], new Position(
 							i * 32, j * 32)));
 				if (iObjectsList.contains(" "+tiles[i][j]+" ")){
 					iObjects.add(tf.createIObjects(tiles[i][j], i, j, this));
 				}
 					
 			}
 		sItems = new Item[items.size()];
 		for(int i = 0; i < sItems.length;i++)
 			sItems[i] = items.get(i);
 		sCreatures = new Creature[creatures.size()];
 		for(int i = 0; i < sCreatures.length; i++)
 			sCreatures[i] = creatures.get(i);
 	}
 
 	/**
 	 * Returns the i:th interactive object Will cast IndexOutOfBoundsException
 	 * if i > getIObjectSize()
 	 * 
 	 * @return the list of InteractiveObjects
 	 */
 	public InteractiveObject getIObjectAt(int x, int y) {
 		Position p = new Position(x, y);
 		for(int i = 0; i < iObjects.size(); i++){
 			if (iObjects.get(i).getPosition().equals(p))
 				return iObjects.get(i);
 		}
 			
 			
 				
 		return null;
 	}
 
 	/**
 	 * Return the amount of iObjects currently on the map.
 	 * 
 	 * @return the amount
 	 */
 	public int getIObjectSize() {
 		return iObjects.size();
 	}
 	
 	public InteractiveObject getIObject(int i){
 		return iObjects.get(i);
 	}
 
 	/**
 	 * Will return an item if there is one at the given position. Else it will
 	 * return null.
 	 * 
 	 * @return item at the position, or null
 	 */
 	public Item getItemAt(int x, int y) {
 		Position p = new Position(x, y);
 		for(int i = 0; i < items.size(); i++){
 			if (items.get(i).getPosition().equals(p)){
 				return items.remove(i);
 			}
 			
 		}
 			
 		return null;
 	}
 	/**
 	 * Return the item at which they are drawn. 
 	 * @param i
 	 * @return
 	 */
 	public Item getItem(int i){
 		return items.get(i);
 	}
 	/**
 	 * The number of items currently on the map. 
 	 * @return
 	 */
 	public int amountOfItems(){
 		return items.size();
 	}
 	/**
 	 * Will reset the items to the first state. 
 	 */
 	public void resetItems(){
 		items.clear();
 		for(Item i : sItems)
 			items.add(i);
 	}
 
 	/**
 	 * Return the i:th creature. Will cast IndexOutOfBoundsException if i >
 	 * getCreatureSize()
 	 * 
 	 * @return a list of creatures
 	 */
 	public Creature getCreatureAt(int i) {
 		return creatures.get(i);
 	}
 
 	/**
 	 * Return the amount of creatures currently on the map.
 	 * 
 	 * @return the number of creatures
 	 */
 	public int getCreatureSize() {
 		return creatures.size();
 	}
 	/**
 	 * Will reset the creatures to the first state. 
 	 */
 	public void resetCreatures(){
 		creatures.clear();
 		for(Creature c : sCreatures)
 			creatures.add(c);
 	}
 
 	/**
 	 * Returns the tile at the given position
 	 * 
 	 * @param x
 	 *            , position in x-axis, x < getTileWidth()
 	 * @param y
 	 *            , position in y,axis, y < getTileHieght()
 	 * @throws IndexOutOfBoundsException
 	 *             if -^
 	 * @return the tile at the given position
 	 */
 	public Tile getTile(int x, int y) {
 		return map[x][y];
 	}
 
 	/**
 	 * Returns the width of the map in tiles.
 	 * 
 	 * @return
 	 */
 	public int getTileWidth() {
 		return map.length;
 	}
 
 	/**
 	 * Returns the height of the map in tiles
 	 * 
 	 * @return
 	 */
 	public int getTileHeight() {
 		return map[0].length;
 	}
 }

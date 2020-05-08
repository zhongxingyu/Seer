 import java.util.ArrayList;
 
 /**
  * A class for randomly generating coherent dungeons.
  * @since 5/26/13
  * @version 1
  * @author alex
  */
 public class DungeonGenerator {
 	private static TileDictionary tileDictionary;
 	private static ItemDictionary itemDictionary;
 	private GameMap map;
 	
 	public static void initTileDictionary(TileDictionary tileDictionary) {
 		DungeonGenerator.tileDictionary = tileDictionary;
 	}
 	
 	public static void initItemDictionary(ItemDictionary itemDictionary) {
 		DungeonGenerator.itemDictionary = itemDictionary;
 	}
 	
 	public static void generateRoom(Tile[][] tiles, int x, int y, int width, int height) {
 		//if (height > tiles.size()) { return; }
 		if (x + width > tiles.length || y + height > tiles[0].length) { return; }
 		for (int x1 = x; x1 < width; x1++) {
 			for (int y1 = y; y1 < height; y1++) {
 				if (x1 == 0 || y1 == 0 || x1 == width - 1 || y1 == height - 1) {
 					DungeonGenerator.placeTile(tiles, new Tile(tileDictionary, tileDictionary.getRandomWall(), x1, y1));
 				} else {
 					DungeonGenerator.placeTile(tiles, new Tile(tileDictionary, tileDictionary.getRandomNonwall(), x1, y1));
 				}
 			}
 		}
 	}
 
 	public static void generateHallway(Tile[][] tiles, int x1, int y1, int x2, int y2) {
 			ArrayList<PathfindingTile> openList = new ArrayList<>();
 			ArrayList<PathfindingTile> closedList = new ArrayList<>();
 			PathfindingTile start = new PathfindingTile(null, x1, y1, 0, 0, 0);
 			openList.add(start);
 			PathfindingTile curLookingTile = start;
 			outerloop: // Loop label.
 			while (true) {
 				innerloop: // Loop label.
 				for (Direction dir : Direction.values()) { // Find adj tiles to starting point.
 					Vector vec = Misc.getLocFromDir(curLookingTile.x, curLookingTile.y, dir);
 					System.out.print("Dir: " + dir + ", x: " + vec.getX() + ", y: " + vec.getY() + "| ");
 					
 					for (int i = 0; i < closedList.size(); i++) {
 						System.out.println("Closed check: " + i + ", size: " + closedList.size());
 						if (vec.getX() == closedList.get(i).x && vec.getY() == openList.get(i).y) {
 							System.out.println("Tile was already on closed list, skipping!");
 							continue innerloop;
 						}
 					}
 					
 					for (int i = 0; i < openList.size(); i++) {
 						System.out.println("Opened check: " + i + ", size: " + openList.size());
 						if (vec.getX() == openList.get(i).x && vec.getY() == openList.get(i).y) {
 							System.out.println("Tile was already on open list, skipping?"); // Since we are only doing straight adjacents, skipping is find, since g is never needed to be recalculated.
 							continue innerloop; // Possibly, might want to remove?
 						}
 					}
 					
 					try {
 						if (vec.getX() < 0 || vec.getX() > tiles.length || vec.getY() < 0 || vec.getY() > tiles[0].length || (tiles[vec.getX()][vec.getY()].isWall() && !tiles[vec.getX()][vec.getY()].getName().equalsIgnoreCase("Empty"))) { System.out.println("Tile was invalid! skipping!"); continue; }
 					} catch (IndexOutOfBoundsException ex) { }
 					
 					//calculate f, g & h here.
 					int g = 10;
 					if (dir == Direction.LEFT_DOWN || dir == Direction.LEFT_UP || dir == Direction.RIGHT_UP || dir == Direction.RIGHT_DOWN) { g = 20; }
 					int h = 10 * (Math.abs(vec.getX() - x2) + Math.abs(vec.getY() - y2));
 					System.out.println("g: "+  g + ", h: " + h);
 					openList.add(new PathfindingTile(curLookingTile, vec.getX(), vec.getY(), g + h, g, h));
 					if (vec.getX() == x2 && vec.getY() == y2) { closedList.add(new PathfindingTile(curLookingTile, x2, y2, 0, 0, 0)); System.out.println("Got to last tile!"); break outerloop; }
 				}
 				if (openList.isEmpty()) { System.out.println("openList emptied, no possible path!"); break; }
 				
 				openList.remove(curLookingTile); // Add starting tile to closed list.
 				closedList.add(curLookingTile);
 				
 				int lowest = 0;
 				for (int i = 0; i < openList.size(); i++) { // Find lowest cost tile. (Will pick last tile if some are the same.)
 					if (openList.get(i).f < openList.get(lowest).f) {
 						lowest = i;
 					}
 				}
 				
 				curLookingTile = openList.get(lowest); // Set new tile to starting point, remove from open, add to closed.
 				closedList.add(curLookingTile);
 				openList.remove(lowest);
             }
 			System.out.println("O: " + openList);
 			System.out.println("C: " + closedList);
 			
 			PathfindingTile tile = closedList.get(closedList.size() - 1);
 			while (true) {
 	//			tiles.get(tile.x).add(tile.y, new Tile(tileDictionary, "Wood", tile.x, tile.y));
 				DungeonGenerator.placeTile(tiles, new Tile(tileDictionary, "Wood", tile.x, tile.y));
 				if (tile.parent == null) { break; }
 				tile = tile.parent;
 			}
 	}
 	
 	public static void placePlayerInFeasibleLocation(Tile[][] tiles, Player ply) {
 		for (int x = 0; x < tiles.length; x++) {
 			for (int y = 0; y < tiles[0].length; y++) {
 				if (tiles[x][y] == null || tiles[x][y].getName().equalsIgnoreCase("") || tiles[x][y].isWall() || (int) (Math.random() * 20) - 1 <= 10) { continue; }
 				System.out.println("Placing player at: (" + x + ", " + y + ")");
 				ply.setTile(tiles[x][y]);
 				return;
 			}
 		}
 	}
 	
 	public static void placeItems(Tile[][] tiles, int w, int h) {
 		for (int x = 0; x < w; x++) {
 			for (int y = 0; y < h; y++) {
 				//calculate random shit and if equals other random shit place random item
				if((int) (Math.random() * 100) + 1 <= 10) {
 					Item item = itemDictionary.getRandomItem();
 					if (item == null) { continue; }
 					item.setTile(tiles[x][y]);
 				}
 			}
 		}
 	}
 	
 	public static void placeTile(Tile[][] tiles, Tile tile) {
 			if (tile.getX() >= tiles.length || tile.getY() >= tiles[0].length) { return; }
 			tiles[tile.getX()][tile.getY()] = tile;
 	}
 
 	public static GameMap generateDungeon(int w, int h, TileDictionary tileDictionary, ItemDictionary itemDictionary) {
 		DungeonGenerator.initTileDictionary(tileDictionary);
 		DungeonGenerator.initItemDictionary(itemDictionary);
 		GameMap empty = new GameMap(w, h, tileDictionary);
 		
 		Tile[][] tiles = new Tile[w][h];
 		for (int x = 0; x < w; x++) {
 			for (int y = 0; y < h; y++) {
 				tiles[x][y] = new Tile(tileDictionary, "Empty", x, y);
 			}
 		}
 		
 		generateRoom(tiles, 0, 0, 10, 10);
 		placeItems(tiles, w, h);
 		System.out.println(tiles);
 		//generateHallway(tiles, 0, 11, 5, 0);
 		/*String[] temp = new String[tileDictionary.size()];
 		temp[0] = "Stone";
 		temp[1] = "Wood";
 		temp[2] = "Glass";
 		temp[3] = "Obsidian";
 		for(int x = 0; x < w; x++) {
 			ArrayList<Tile> tileY = new ArrayList<>();
 			for(int y = 0; y < h; y++) {
 				//tileY.add(new Tile(tileDictionary, "Wood", x, y).setSeen(true));
 				tileY.add(new Tile(tileDictionary, temp[(int) (Math.random() * temp.length - 1)], x, y).setSeen(true).setInLos(Misc.randomBool()));
 			}
 			tiles.add(tileY);
 		}*/
 		empty.setTiles(tiles);
 		return empty;
 	}
 }

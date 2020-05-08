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
 		for (int x1 = x; x1 < (x + width); x1++) {
 			for (int y1 = y; y1 < (y + height); y1++) {
 				if (x1 == x || y1 == y || x1 == (x + width) - 1 || y1 == (y + height) - 1) {
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
 			
 			PathfindingTile curLookingTile = new PathfindingTile(null, x1, y1, 0, 0, 0);
 			openList.add(curLookingTile);
 			
 			int added = 0; // May not be needed?
 			
 			outerloop: // Loop label.
 			while (true) {
 				innerloop: // Loop label.
 				for (Direction dir : Direction.values()) { // Find adj tiles to starting point.
 					Vector vec = Misc.getLocFromDir(curLookingTile.x, curLookingTile.y, dir);
 					System.out.print("Dir: " + dir + ", x: " + vec.getX() + ", y: " + vec.getY() + "| ");
 					
 					for (int i = 0; i < closedList.size(); i++) {
 						if (vec.getX() == closedList.get(i).x && vec.getY() == closedList.get(i).y) {
 							System.out.println("Tile was already on closed list, skipping!");
 							continue innerloop;
 						}
 					}
 					
 					for (int i = 0; i < openList.size(); i++) {
 						if (vec.getX() == openList.get(i).x && vec.getY() == openList.get(i).y) {
 							System.out.println("Tile was already on open list, skipping?"); // Since we are only doing straight adjacents, skipping is find, since g is never needed to be recalculated.
 							continue innerloop; // Possibly, might want to remove?
 						}
 					}
 					
 					try {
 						if (vec.getX() < 0 || vec.getX() > tiles.length || vec.getY() < 0 || vec.getY() > tiles[0].length || (tiles[vec.getX()][vec.getY()].isWall() && !tiles[vec.getX()][vec.getY()].getName().equalsIgnoreCase("Empty"))) { System.out.println("Tile was invalid! skipping!"); continue; }
 					} catch (IndexOutOfBoundsException ex) { }
 					
 					//calculate f, g & h here.
 					double g = 10.0;
 					double h = Double.MAX_VALUE;
 					
 					double xDistance = Math.abs(vec.getX() - x2); // Diagonol Shortcut Huerisitic
 					double yDistance = Math.abs(vec.getY() - y2);
 					if (xDistance > yDistance) {
 						h = 14.0 * yDistance + 10.0 * (xDistance-yDistance);
 					} else {
 						h = 14.0 * xDistance + 10.0 * (yDistance-xDistance);
 					}
 					
 					//double h = 10 * (Math.abs(vec.getX() - x2) + Math.abs(vec.getY() - y2)); // Manhattan Huerisitic
 					
 					System.out.println("g: "+  g + ", h: " + h);
 					openList.add(new PathfindingTile(curLookingTile, vec.getX(), vec.getY(), g + h, g, h));
 					added++;
 					if (vec.getX() == x2 && vec.getY() == y2) { closedList.add(new PathfindingTile(curLookingTile, x2, y2, 0, 0, 0)); System.out.println("Got to last tile!"); break outerloop; }
 				}
 				if (added == 0) { System.out.println("Target unreachable!"); return; }
 				if (openList.isEmpty()) { System.out.println("openList emptied, no possible path!"); return; }
 				
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
 				
 			//	System.out.println("O: " + openList);
 			//	System.out.println("C: " + closedList);
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
				if((int) (Math.random() * 100) + 1 <= 3) {
 					Item item = itemDictionary.getRandomItem();
 					if (item == null) { continue; }
					if (tiles[x][y].getName().equalsIgnoreCase("Empty") || tiles[x][y].getName().equalsIgnoreCase("Wall")) { continue; }
 					item.setTile(tiles[x][y]);
 				}
 			}
 		}
 	}
 	
 	public static void placeTile(Tile[][] tiles, Tile tile) {
 			if (tile.getX() >= tiles.length || tile.getY() >= tiles[0].length) { return; }
 			System.out.println("DungeonGenerator.placeTile: Placing tile at " + tile.getX() + ", " + tile.getY());
 			tiles[tile.getX()][tile.getY()] = tile;
 	}
 
 	public static GameMap generateDungeon(int w, int h, TileDictionary tileDictionary, ItemDictionary itemDictionary) {
 		DungeonGenerator.initTileDictionary(tileDictionary);
 		DungeonGenerator.initItemDictionary(itemDictionary);
 		GameMap empty = new GameMap(w, h, tileDictionary);
 		
 		Tile[][] tiles = new Tile[w][h];
 		for (int x = 0; x < w; x++) {
 			for (int y = 0; y < h; y++) {
 				tiles[x][y] = new Tile(tileDictionary, "Empty", x, y); // Layout an entire dungeon full of "Empty" tiles.
 			}
 		}
 		
 		ArrayList<ArrayList<Tile>> rooms = new ArrayList<>();
 		
 		generateRoom(tiles, 0, 0, 10, 10);
 		generateRoom(tiles, 11, 1, 5, 5);
 		
 		generateHallway(tiles, 0, 10, 13, 0);
 		
 		placeItems(tiles, w, h);
 		
 		empty.setTiles(tiles);
 		return empty;
 	}
 }

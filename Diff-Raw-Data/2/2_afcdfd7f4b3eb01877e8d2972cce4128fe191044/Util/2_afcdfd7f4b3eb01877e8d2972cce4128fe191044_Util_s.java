 import java.util.Set;
 
 
 public class Util {
 	
 	public static Tile getClosestTile(Tile myPos, Set<Tile> tiles) {
 		
 		int distance = Integer.MAX_VALUE;
 		Tile closest = null;
 		
 
 		for (Tile currentTile : tiles) {
 			int d = MyBot.ants.getDistance(currentTile, myPos);
 			
 			if (d < distance) {
 				distance = d;
 				closest = currentTile;
 			}
 		}
 		
 		return closest;
 	}
 	
 	public static boolean samePosition(Tile a, Tile b) {
		return (a.getCol() == b.getCol() && a.getRow() == b.getRow())
 	}
 	
 	public static Tile getClosestUnseenTile(Tile myPos) {
 		Tile unseen = null;
 		
 		int distance = Integer.MAX_VALUE;
 		for (int row = 0; row < MyBot.ants.getRows(); row++) {
 			for (int col = 0; col < MyBot.ants.getCols(); col++) {
 				if (!MyBot.seenTiles[row][col]) {
 					Tile unseenTile = new Tile(row, col);
 					int d = MyBot.ants.getDistance(unseenTile, myPos);
 					if (d < distance) {
 						distance = d;
 						unseen = unseenTile;
 					}
 				}
 			}
 		}
 		
 		return unseen;
 	}
 }

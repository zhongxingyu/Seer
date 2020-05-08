 /**
  *
  * @author jonathan
  */
 public class Structure {
 	private static int getX(Location loc) {
 		return (int)Math.floor(loc.getZ());
 	}
 	private static int getY(Location loc) {
 		return (int)Math.floor(loc.getX());
 	}
 	private static int getZ(Location loc) {
 		// Subtract one, as we always use the block beneath the player's feet.
 		return (int)Math.floor(loc.getY())-1;
 	}
 	public static BlockType nonEvalBlock = BlockType.BEDROCK;
 
 	public interface Actor {
 		public boolean doBlockAction(BlockType structureBlockType,
 						Block worldBlock);
 	}
 
 	public static class Validator implements Actor {
 		public int invalidBlockCount;
 		public Validator() {
 			invalidBlockCount = 0;
 		}
 		public boolean doBlockAction(BlockType structureBlockType,
 							Block worldBlock) {
 			if( worldBlock.getBlockType() != structureBlockType &&
 					structureBlockType != Structure.nonEvalBlock ) {
 				invalidBlockCount += 1;
 				return false;
 			}
 			return true;
 		}
 	}
 	
 	/**
 	 * NOTE:
 	 * Altitude -> Y
 	 * Latitude -> X
 	 * Longitude -> Z
 	 * 
 	 *
 	 * @param pattern
 	 * @param startPosition
 	 * @param playerLocation
 	 * @return
 	 */
 	public static void parse(BlockType[][][] pattern,
 					int[] startPosition,
 					Location playerLocation, Actor callBack) {
 		int thisZ, thisX, thisY;
 		Location thisLocation = new Location(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
 
		Waypoints.loadChunkAtLocation(thisLocation);

 		/**
 		 * This ugly loop iterates over every value of the pattern.
 		 * We'll use the startPosition to calculate it's relative location from the
 		 * player's location. We grab the block at that location, and compare it to
 		 * the block we're expecting in the pattern, if it's false, we return out
 		 * early, otherwise we keep looping.
 		 *
 		 * If we make it through the loop, we've got a valid pattern.
 		 *
 		 * There probably is a WAY smarter way to do this, I'm just too dumb to
 		 * figure it out.
 		 */
 		for( int z = 0; z < pattern.length; z++ ){
 			for( int y = 0; y < pattern[z].length; y++ ) {
 				for( int x = 0; x < pattern[z][y].length; x++ ) {
 					thisZ = Structure.getZ(playerLocation)+(z-startPosition[0]);
 					thisY = Structure.getY(playerLocation)+(y-startPosition[1]);
 					thisX = Structure.getX(playerLocation)+(x-startPosition[2]);
 					thisLocation.setX(thisY);
 					thisLocation.setY(thisZ);
 					thisLocation.setZ(thisX);
 
 					boolean continueLoop = (null == callBack) ? true :
 						callBack.doBlockAction(pattern[z][y][x], World.getBlock(thisLocation));
 					
 					if( !continueLoop ) return;
 				}
 			}
 		}
 	}
 
 }

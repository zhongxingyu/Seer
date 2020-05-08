 package mods.natureoverhaul;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 /**
  * 
  * @author Clinton Alexander
  *
  */
 public class Utils {
 
 	/**
 	* Calculate an optimal distance coefficient. This is for cases
 	* where a larger number is desired the further you get from
 	* the optimal value. The minimum is 1, so multiplying any number "r"
 	* by the result of this operation will result in "r" if r is equal to "opt".
 	* If r is extremely far from opt, the coefficient will be extremely large
 	*
 	* @param	rain		Current value
 	* @param	opt		Optimal value
 	* @param	tol		tolerance (lower = Higher probability)
 	* @return The modifier. Output always >= 1, where 1 is "just as likely" and
 	* 		higher is "less likely"
 	*/
 	public static float getOptValueMult(float rain, float opt, float tol) {	
 		return tol * (float) Math.pow(opt - rain, 2) + 1;
 	}
 
 	public static NOType getType(int id){
 		return NatureOverhaul.instance.getIDToTypeMapping().get(Integer.valueOf(id));
 	}
 	
 	public static int getLeafFromLog(int id){
 		try{
 			return NatureOverhaul.instance.getLogToLeafMapping().get(Integer.valueOf(id));
 		}catch(NullPointerException n){
 			System.err.println("NatureOverhaul failed to find corresponding leaf to log block with id "+id+" in config file");
 			return 0;
 		}
 	}
 	/**
 	* Check if we have at least a nearby block of corresponding id within radius
 	*
 	* @param	radius	Radius to check in
 	* @param	ignoreSelf If center block should be ignored
 	* @return	True if at least one block is nearby
 	*/
 	public static boolean hasNearbyBlock(World world, int i, int j, int k, int id, int radius, boolean ignoreSelf) {
 		for(int y = -radius; y <= radius; y++) {
 			for(int x = -radius; x <= radius; x++) {
 				for(int z = -radius; z <= radius; z++) {
 					if(ignoreSelf && x==0 && y==0 && z==0){
 						z++;
 					}
 					if( world.getBlockId(i + x, j + y, k + z) == id) {
 						return true;
 					}
 				}
 			}
 		}	
 		return false;
 	}
 	/**
 	 * Randomize given coordinates within range
 	 * 
 	 * @param	i first coordinate
 	 * @param	j second coordinate
 	 * @param	k third coordinate
 	 * @param  range The limiting value for the distance in each direction
 	 * @return An array of randomized coordinates
 	 */
 	public static int[] findRandomNeighbour(int i, int j, int k, int range){
 		Random rand=new Random();
 		int[] coord=new int[]{i,j,k};
 		int dist;
		for(int index=0;index<coord.length;index++){
				dist=rand.nextInt(range);
 			if (rand.nextBoolean())
				coord[index]+=dist;
 			else
				coord[index]-=dist;	
 		}
 		return coord;
 	}
 	/**
 	* Gets the j location of the lowest block of the type specified
 	*  below the block from given coordinate
 	* @param type 
 	* @return	lowest block j location
 	*/
 	public static int getLowestTypeJ(World world, int i, int j, int k, NOType type) {
 		int low=j;
 		while(getType(world.getBlockId(i, low - 1, k)) == type) {
 			low--;
 		}	
 		return low;
 	}
 	/**
 	* Emit a specific item
 	*
 	* @param item	Item to emit
 	*/
 	public static void emitItem(World world, int i, int j, int k, ItemStack item) {
 		if(!world.isRemote)
 		{
 			EntityItem entityitem = new EntityItem(world, i, j, k, item);
 			world.spawnEntityInWorld(entityitem);
 		}
 	}
 	
 	public String toString(){
 		return "Nature Overhaul Utility Class";
 	}
 }

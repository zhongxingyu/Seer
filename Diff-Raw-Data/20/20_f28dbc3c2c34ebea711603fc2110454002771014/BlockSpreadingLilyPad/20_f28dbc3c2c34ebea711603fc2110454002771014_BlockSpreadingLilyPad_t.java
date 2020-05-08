 package eggdropsoap.spreadinglilypads;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockLilyPad;
 import net.minecraft.world.World;
 
 public class BlockSpreadingLilyPad extends BlockLilyPad
 {	
 	public static final int growthDelay = 10;
 	
 	public static final Integer[] SOIL_BLOCK_IDS = new Integer[] {
 		Block.dirt.blockID,
 		Block.blockClay.blockID,
 		Block.sand.blockID,
 		Block.grass.blockID
 		};
 	public static final Set<Integer> SOILS = new HashSet<Integer>(Arrays.asList(SOIL_BLOCK_IDS));
 			
 	protected BlockSpreadingLilyPad(int id)
 	{
 		super(id);
 	}
 	
 	// based on reading the code in net.minecraft.block.BlockMushroom
     public void updateTick(World world, int blockX, int blockY, int blockZ, Random rng)
     {
 //    	System.out.printf("Received update tick at %d %d %d\n", blockX, blockY, blockZ);
     	if (rng.nextInt(growthDelay) == 0)
     	{
 //    		System.out.println("Entering spreading attempt");
     		int checkDistance = 1;
    		int lilyCap = 6;
     		int i;
     		int j;
     		
     		// First count the number of Lily Pads in range and return if too many
     		for (i = blockX - checkDistance; i <= blockX + checkDistance; ++i)
     		{
         		for (j = blockZ - checkDistance; j <= blockZ + checkDistance; ++j)
         		{
         			if (world.getBlockId(i, blockY, j) == this.blockID)
         			{
         				--lilyCap;
         				if (lilyCap <= 0)
         				{
 //        					System.out.printf("%d %d %d: Too many lilies in range\n", blockX, blockY, blockZ);
         					return;
         				}
         			}
         		}
     			
     		}
     		
     		int newX; 
     		int newZ;
     		    		
     		for (int attempts = 0; attempts < 4; ++attempts)	// try four times to spread
     		{
         		newX = blockX + rng.nextInt(3) - 1;	// pick one of [-1, 0, 1] 
         		newZ = blockZ + rng.nextInt(3) - 1;	// ditto
 
 //        		System.out.printf("Trying to spread to %d %d %d\n", newX, blockY, newZ);
 
         		if (world.isAirBlock(newX, blockY, newZ) &&
         				this.canBlockStay(world, newX, blockY, newZ) &&
         				this.hasSoilAndShallowWater(world, newX, blockY, newZ)
         				)
     			{
 //    				System.out.println("This location is a valid target");
 
     				blockX = newX;
     				blockZ = newZ;
     				
 //    				System.out.printf("%d %d %d: Spreading!\n", newX, blockY, newZ);
     				
     				// set the block location to a new Lily Pad with no metadata and a client update
         			world.setBlock(blockX, blockY, blockZ, this.blockID, 0, 2);
 
         			break;	// pre-emptively terminate for loop once a spread target found
     			}    			
     		}    			
     	}
     }
 	
     // checks if the blocks underneath are still water and one "soil"
     public boolean hasSoilAndShallowWater(World world, int x, int y, int z)
     {
     	int maxDepth = 2;	// up to how many blocks of water counts as "shallow"
     	boolean shallow = false;	// assume false
     	boolean hasSoil = false;
     	int waterDepth;		// actual water depth
 
     	for (waterDepth = 1; waterDepth <= maxDepth; waterDepth++)
     	{
     		if (! ( world.getBlockId(x, y - waterDepth, z) == Block.waterStill.blockID ) )
     		{	
     			// we've found a non-water block:
     			break;
     		}
     	}
		// back up one block to where water was
		waterDepth = waterDepth - 1;

		System.out.printf("%d %d %d: Water depth %d\n", x, y, z, waterDepth);
     	
     	shallow = ( waterDepth <= maxDepth );
     	
    	hasSoil = SOILS.contains(world.getBlockId(x, y - (waterDepth + 1), z));
     	
    	System.out.printf("%d %d %d: Shallow: %b; hasSoil: %b\n", x, y, z, shallow, hasSoil);
     	
     	return ( shallow && hasSoil );    	
     }
 
    public int idDropped(int par1, Random random, int par2) {
        return Block.waterlily.blockID;
    }
 }

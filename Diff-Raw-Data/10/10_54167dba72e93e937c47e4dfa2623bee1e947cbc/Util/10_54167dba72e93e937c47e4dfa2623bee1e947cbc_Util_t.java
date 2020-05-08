 package me.tobi.FloatingIslands;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.bukkit.Chunk;
 import org.bukkit.Material;
 import org.bukkit.TreeType;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 
 public class Util {
 	/**
 	 * 
 	 * @param spawnBlock The block, the player spawns "inside"
 	 * @return
 	 */
 	public static boolean isValidSpawn(Block spawnBlock){
 		//ensure that the block below the player is grass
 		if(spawnBlock.getRelative(BlockFace.DOWN).getType()==Material.GRASS){
 			//ensure that the player can spawn inside the spawn block
 			if(spawnBlock.getType()==Material.AIR
 					|| spawnBlock.getType()==Material.GRASS
 					|| spawnBlock.getType()==Material.SUGAR_CANE_BLOCK
 					|| spawnBlock.getType()==Material.RED_MUSHROOM
 					|| spawnBlock.getType()==Material.BROWN_MUSHROOM
 					|| spawnBlock.getType()==Material.RED_ROSE
 					|| spawnBlock.getType()==Material.YELLOW_FLOWER
 					|| spawnBlock.getType()==Material.SNOW
 			){
 				//ensure that the two blocks above them are air
 				if(spawnBlock.getRelative(0, 2, 0).getType()==Material.AIR
 					&& spawnBlock.getRelative(0, 3, 0).getType()==Material.AIR){
 					return true;
 				}
 				else return false;
 			}
 			else return false;
 		}
 		else return false;
 	}
 	
 	public static Block getHighestBlockOfType(World world, int x, int z,
 			int minHeight, int maxHeight, Material type){
 		Block block=world.getBlockAt(x, maxHeight, z);
 		while(block.getType()!=type && block.getY()>minHeight){
 			block=block.getRelative(BlockFace.DOWN);
 		}
 		return block;
 	}
 	
 	public static Block getFirstSolidBlockInChunk(Chunk chunk,
 			int maxHeight, int minHeight){
 		Block retBlock=null;
 		for(int x=0; x<16; x++){
 			for(int z=0; z<16; z++){
 				retBlock=chunk.getBlock(x, maxHeight, z);
 				while(retBlock.getType()==Material.AIR
 						&& retBlock.getY()>minHeight){
 					retBlock=retBlock.getRelative(BlockFace.DOWN);
 				}
 				if(retBlock.getType()!=Material.AIR) return retBlock;
 			}
 		}
 		return retBlock;
 	}
 	
 	/**
 	 * Ensures a tree on an island
 	 * @param startBlock The first block of this island
 	 */
 	public static void ensureTreeAtIsland(Block startBlock){
 		boolean treeFound=false;
 		for(int x=0; x<3; x++){
 			for(int z=0; z<3; z++){
 				if(startBlock.getRelative(x, 1, z).getType()==Material.LOG){
 					treeFound=true;
 					return;
 				}
 			}
 		}
 		if(!treeFound){
 			startBlock.getRelative(2, 0, 2).setType(Material.DIRT); //base of tree
 			startBlock.getRelative(2, 1, 2).setType(Material.AIR); //free this block
 			startBlock.getWorld().generateTree(
 					startBlock.getRelative(2, 1, 2).getLocation(),
 					TreeType.TREE
 			);
 		}
 	}
 	
 	public static void saveSpawnToFile(String path, Block spawnBlock){
		System.out.println("saving spawn to file \""+path+"\"");
 		BufferedWriter out=null;
 		try {
 			out=new BufferedWriter(new FileWriter(path));
 			out.write(spawnBlock.getX()+" "+spawnBlock.getY()+" "
 					+spawnBlock.getZ()+"\n");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}finally{
 			if(out!=null){
 				try{
 					out.close();
 				}catch(IOException e){
 					e.printStackTrace();
 				}
 			}
 		}
		System.out.println("spawn saved");
 	}
 }

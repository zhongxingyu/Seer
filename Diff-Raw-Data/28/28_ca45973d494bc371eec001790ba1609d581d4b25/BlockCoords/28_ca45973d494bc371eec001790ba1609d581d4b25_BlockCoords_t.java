 package minemail;
 
 import org.bukkit.block.Block;
 
 public class BlockCoords {
 	private int blockx;
 	private int blocky;
 	private int blockz;
 	
 	public BlockCoords(Block block) {
 		blockx = block.getX();
 		blocky = block.getY();
 		blockz = block.getZ();
 	}
 	
 	public String getCoords() {
		return(blockx+"_"+blocky+"_"+blockz);
 	}
 }

 package com.herpingdo.infinitedispenser;
 
 import java.util.ArrayList;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 
 public class Utils {
 
 	/** Gets the Block object of a sign adjacent to another block, containing a set text.
 	 * 
 	 * @param bb - The block to fid an adjacent sign
 	 * @param search - The text on the sign to find
 	 * @param line - The line number to find the text on
 	 * @return The block object of the sign, or null if not found.
 	 */
 	public static Block getAdjacentSign(Block bb, String search, int line)
 	{
		for (Block b : getAdjacentSigns(bb))
 		{
			String[] lines = ((Sign)bb).getLines();
 			if (lines.length < line + 1) continue;
			if (lines[line].equalsIgnoreCase(search)) return b;
 		}
 		return null;
 	}
 	
 	/** Gets a Block[] Array of adjacent signs to a specified Block.
 	 * 
 	 * @param bl - The block to get adjacent signs of.
 	 * @return Block[] array of signs.
 	 */
	public static Block[] getAdjacentSigns(Block bl)
 	{
 		ArrayList<Block> tmp2 = new ArrayList<Block>();
 		Block[] tmp = {
 			bl.getRelative(BlockFace.UP),
 			bl.getRelative(BlockFace.DOWN),
 			bl.getRelative(BlockFace.NORTH),
 			bl.getRelative(BlockFace.SOUTH),
 			bl.getRelative(BlockFace.EAST),
 			bl.getRelative(BlockFace.WEST)
 		};
 		//int i = tmp.length;
 		for (Block b : tmp)
 		{
 			if (b.getType().equals(Material.WALL_SIGN) || b.getType().equals(Material.SIGN_POST))
 			{
 				tmp2.add(b);
 			}
 		}
 		if (tmp2.size() <= 0) return null;
		return (Block[]) tmp2.toArray();
 	}
 }

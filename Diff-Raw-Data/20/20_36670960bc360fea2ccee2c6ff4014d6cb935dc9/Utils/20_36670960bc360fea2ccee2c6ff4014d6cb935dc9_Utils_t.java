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
 		Object[] oo = getAdjacentSigns(bb);
 		/* Fixes NullPointerException when placing dispensers */
 		if (oo == null) return null;
 		for (Object b : oo)
 		{
 			String[] lines = ((Sign) ((Block)b).getState()).getLines();
 			if (lines.length < line + 1) continue;
 			if (lines[line].equalsIgnoreCase(search)) return ((Block)b);
 		}
 		return null;
 	}
 	
 	/** Gets a Block[] Array of adjacent signs to a specified Block.
 	 * 
 	 * @param bl - The block to get adjacent signs of.
 	 * @return Block[] array of signs.
 	 */
 	public static Object[] getAdjacentSigns(Block bl)
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
 		return tmp2.toArray();
 	}
 	
 	/** Returns true if the specified Block is next to a dispenser.
 	 * 
 	 * @param bl - The block.
 	 * @return True or false, see above.
 	 */
 	public static boolean isDispenserAdjacent(Block bl)
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
 		for (Block b : tmp)
 		{
 			if (b.getType().equals(Material.DISPENSER))
 			{
 				tmp2.add(b);
 			}
 		}
 		if (tmp2.size() <= 0) return false;
 		return true;
 	}
 	public static boolean isInfiniteDispenser(Block block)
 	{
		try
		{
			if (!block.getType().equals(Material.DISPENSER)) return false;
			return (getAdjacentSign(block, "[Infinite]", 0) != null);
		}
		catch (Exception e)
		{
			return false;
		}
 	}
 }

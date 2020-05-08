 package com.xdrapor.safeguard.utilities;
 import net.minecraft.server.v1_5_R2.IBlockAccess;
 import net.minecraft.server.v1_5_R2.Item;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.xdrapor.safeguard.core.ICore;
 import com.xdrapor.safeguard.player.SGPlayer;
 
 /**
  * This is a static class that will not be instanced ever. The 'data' is already instanced in the Event we are passed, no reason to create it again. <-- Rename once understood.
  * 
  * @author xDrapor
  * @author IchBinMude
  * @author Richard
  * 
  */
 public class SGBlockUtil implements ICore
 {
 	/** Returns the time between the last block break and last block hit times of the specified SGPlayer. */
 	public static double getPlayerBreakDuration(Player sgPlayer) {
 		return Math.round(safeGuard.sgPlayerManager.getPlayer(sgPlayer.getName()).getLastBlockBreakTime() - safeGuard.sgPlayerManager.getPlayer(sgPlayer.getName()).getLastBlockHitTime());
 	}
 
 	/** Returns a block hardness value. */
 	public static double getHardness(Block block) {
 		if(block.getTypeId() == 0) return 0;
 		return net.minecraft.server.v1_5_R2.Block.byId[block.getTypeId()].l(((CraftWorld)block.getWorld()).getHandle(), block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
 	}
 
 	/** Returns how long it should take to break the block with the specified tool. */
 	public static double getDurationVSTool(SGPlayer player, ItemStack item, Block block) {
 		float currentToolMultiplier = item.getTypeId() == 0 ? 1.0F : net.minecraft.server.v1_5_R2.Item.byId[item.getTypeId()].getDestroySpeed(new net.minecraft.server.v1_5_R2.ItemStack(Item.byId[item.getTypeId()]), net.minecraft.server.v1_5_R2.Block.byId[block.getTypeId()]);
 		double currentBlockHardness = getHardness(block);
 		return Math.round((1000f * 5f * currentBlockHardness) / (currentToolMultiplier * 3.33f));
 	}
 
 	/** Returns the closest ground Location relative to the specified Location. */
 	public static Location findClosestGroundToLocation(Player sgPlayer) {
 		Location highestBlock = sgPlayer.getWorld().getHighestBlockAt(sgPlayer.getLocation()).getLocation();
 
 		// Before you had an infinite loop until condition produced a result, what about 'The End' where your condition could possibly never return a result.
 		if (highestBlock == null) {	return sgPlayer.getWorld().getSpawnLocation(); }
 
 		// Return highest block if player is above highest block.
 		if (sgPlayer.getLocation().getY() >= highestBlock.getY()) { return highestBlock; }
 
 		// Return location under player if below highest block.
 		for (int i = 0; i < sgPlayer.getLocation().getY(); i++) {
 			if (!sgPlayer.getWorld().getBlockAt(sgPlayer.getLocation().subtract(0, i, 0)).isEmpty()) {
 				return sgPlayer.getLocation().subtract(0, i - 1, 0);
 			}
 		}
 
 		return sgPlayer.getWorld().getSpawnLocation();
 	}
 
 	/** Returns true if the specified Block is a fence. */
 	public static boolean isFence(final Block block) {
 		return block.getType() == Material.NETHER_FENCE || block.getType() == Material.FENCE;
 	}
 
 	/** Returns true if the specified Block is a stair. */
 	public static boolean isStair(final Block block) {
 		return block.getType() == Material.WOOD_STEP
 				|| block.getType() == Material.STEP
 				|| block.getType() == Material.BRICK_STAIRS
 				|| block.getType() == Material.COBBLESTONE_STAIRS
 				|| block.getType() == Material.WOOD_STAIRS
 				|| block.getType() == Material.BIRCH_WOOD_STAIRS
 				|| block.getType() == Material.JUNGLE_WOOD_STAIRS
 				|| block.getType() == Material.SPRUCE_WOOD_STAIRS
 				|| block.getType() == Material.NETHER_BRICK_STAIRS
 				|| block.getType() == Material.SANDSTONE_STAIRS
 				|| block.getType() == Material.SMOOTH_STAIRS
 				|| block.getType() == Material.WOOD_DOUBLE_STEP;
 	}
 
 	/** Returns true if the specified Block is snow. */
 	public static boolean isSnow(final Block block) {
 		return block.getType() == Material.SNOW;
 	}
 
 	/** Returns true if the block is a ladder **/
 	public static boolean isLadder(Block block) {
 		return block.getType() == Material.LADDER;
 	}
 	
 	/** Checks to see if the specified block is a vine. */
	public boolean isClimbable(final Block block){
 		return block.getType() == Material.VINE || block.getType() == Material.LADDER;
 	}
 	
 	/** Checks to see if the specified block is a lily. */
	public boolean isLily(final Block block){
 		return block.getType() == Material.WATER_LILY;
 	}
 	
 	/** Returns whether a block is passable or not **/
 	public static boolean isPassable(final SGPlayer player, final IBlockAccess blockAccess, final double x, final double y, final double z, final int id) {
 		final int bx = Location.locToBlock(x);
 		final int by = Location.locToBlock(y);
 		final int bz = Location.locToBlock(z);
 		final net.minecraft.server.v1_5_R2.Block block = net.minecraft.server.v1_5_R2.Block.byId[id];
 		final Block craftBlock = player.getPlayer().getWorld().getBlockAt((int)x, (int)y, (int)z);
 		if(block == null || craftBlock == null)return true;
 		final double fx = x - bx;
 		final double fy = y - by;
 		final double fz = z - bz;
 		if (craftBlock.isLiquid() || craftBlock.isEmpty() || player.isOnLily() || player.isClimbing() || fx < block.u() || fx >= block.v() || fy < block.w() || fy >= block.x() || fz < block.y() || fz >= block.z()) return true;
 		else {
 			if (SGMovementUtil.isAboveStairs(player.getPlayer()))if ((blockAccess.getData(bx, by, bz) & 0x4) != 0)if (fy < 0.5) return true;else if (fy >= 0.5) return true;
 			else if (id == Material.WOODEN_DOOR.getId()) return true;
 			else if (id == Material.IRON_DOOR_BLOCK.getId()) return true;
 			else if (player.getPlayer().getLocation().getBlock().getRelative(BlockFace.NORTH).getType() == Material.WOODEN_DOOR) return false;
 			else if (player.getPlayer().getLocation().getBlock().getRelative(BlockFace.NORTH).getType() == Material.IRON_DOOR_BLOCK) return false;
 			else if (id == Material.SOUL_SAND.getId() && fy >= 0.875) return true; // 0.125
 			else if (id == Material.SAND.getId() && fy >= 0.975) return true; // 0.025
 			else if (id == Material.IRON_FENCE.getId() || id == Material.THIN_GLASS.getId())if (Math.abs(0.5 - fx) > 0.05 && Math.abs(0.5 - fz) > 0.05) return true;
 			else if (id == Material.FENCE.getId() || id == Material.NETHER_FENCE.getId())if (Math.abs(0.2 - fx) > 0.02 && Math.abs(0.2 - fz) > 0.02) return true;
 			else if (id == Material.FENCE_GATE.getId() && (blockAccess.getData(bx, by, bz) & 0x4)!= 0) return true;
 			else if (id == Material.CAKE_BLOCK.getId() && fy >= 0.4375) return true; 
 			else if (id == Material.CAULDRON.getId()){if (Math.abs(0.5 - fx) < 0.1 && Math.abs(0.5 - fz) < 0.1 && fy > 0.1) return true;}
 			else if (id == Material.WATER.getId())return true;
 			else if (id == Material.LADDER.getId())return true;
 			else if (id == Material.VINE.getId())return true;
 			else if (id == Material.WATER_LILY.getId())return true;
 			else if (id == Material.SNOW.getId())return true;
 			else if (id == Material.AIR.getId())return true;
 			else if (id == Material.CACTUS.getId() && fy >= 0.9375) return true;
 			return false;
 		}
 	}
 }

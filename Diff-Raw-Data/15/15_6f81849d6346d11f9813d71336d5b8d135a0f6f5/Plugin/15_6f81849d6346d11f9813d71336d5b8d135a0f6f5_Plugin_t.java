 package org.bonsaimind.bukkitplugins.redsponge;
 
 import java.util.logging.Level;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.Material;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * RedSponge for Bukkit
  * 
  * @author admalledd
  */
 public class Plugin extends JavaPlugin implements Listener {
 	@Override
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(this, this);
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.getLogger().log(
 				Level.INFO,
 				pdfFile.getName() + " version " + pdfFile.getVersion()
 						+ " is enabled!");
 	}
 
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 
 		this.getLogger().log(
 				Level.INFO,
 				pdfFile.getName() + " version " + pdfFile.getVersion()
 						+ " is disabled!");
 	}
 
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent event) {
 		Block blockPlaced = event.getBlock();
 		spongeEvent(blockPlaced);
 
 	}
 
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event) {
 		Block block = event.getBlock();
 		if (block.getType() == Material.SPONGE) {
 			updateBlocks(block);
 		}
 	}
 
 	@EventHandler
 	public void onBlockFlow(BlockFromToEvent event) {
 		Block blockFrom = event.getBlock();
 		Block blockTo = event.getToBlock();
 		//TODO: use isliquid()
 		boolean isLava = blockFrom.getType() == Material.LAVA
 				|| blockFrom.getType() == Material.STATIONARY_LAVA;
 		boolean isWater = blockFrom.getType() == Material.WATER
 				|| blockFrom.getType() == Material.STATIONARY_WATER;
 		
 		boolean waterSponge = getConfig().getBoolean("options.water");
 		boolean lavaSponge = getConfig().getBoolean("options.lava");
 
 		// is the flow water or lava and are the sponges enabled
 		if ((lavaSponge && isLava) || (waterSponge && isWater)) {
 			cancelFlow(blockTo, event);
 		}
 	}
 
 	@EventHandler
 	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
 
 		if (!getConfig().getBoolean("options.redstone")) {
 			return; // return early to bypass redstone logics
 		}
 		Block north = event.getBlock().getRelative(BlockFace.NORTH);
 		Block east = event.getBlock().getRelative(BlockFace.EAST);
 		Block south = event.getBlock().getRelative(BlockFace.SOUTH);
 		Block west = event.getBlock().getRelative(BlockFace.WEST);
 		Block up = event.getBlock().getRelative(BlockFace.UP);
 		Block down = event.getBlock().getRelative(BlockFace.DOWN);
 
 		int newCurrent = event.getNewCurrent();
 		int oldCurrent = event.getOldCurrent();
 		Block sponge = null;
 		if (north.getType() == Material.SPONGE) {
 			sponge = north;
 		} else if (east.getType() == Material.SPONGE) {
 			sponge = east;
 		} else if (south.getType() == Material.SPONGE) {
 			sponge = south;
 		} else if (west.getType() == Material.SPONGE) {
 			sponge = west;
 		} else if (up.getType() == Material.SPONGE) {
 			sponge = up;
 		} else if (down.getType() == Material.SPONGE) {
 			sponge = down;
 		}
 		if (sponge != null) {
 
 			// redstone changes from low to high
 			if (oldCurrent == 0 && newCurrent >= 1)
 			{
 				getLogger().info("low->high");
 				updateBlocks(sponge);
 			}
 			// redstone changes from high to low
 			else if (oldCurrent >= 1 && newCurrent == 0)
 			{
 				getLogger().info("high->low");
 				clearArea(sponge);
 			}
 		}
 	}
 
 	public void spongeEvent(Block blockPlaced) {
 		boolean blockWhenPowered = getConfig().getBoolean("options.invert");
 
 		if (blockPlaced.getType() == Material.SPONGE) {
 			// blockWhenPowered == false -> default, a sponge works like a
 			// sponge without redstone current
 			// check here as well in case on block place we are powered
 			if (blockWhenPowered == false) {
 				if (!ispowered(blockPlaced)) {
 					clearArea(blockPlaced);
 
 				}
 			}
 			// blockWhenPowered == true -> sponge works like sponge WITH
 			// redstone current
 			else {
 				if (ispowered(blockPlaced)) {
 					clearArea(blockPlaced);
 				}
 			}
 		}
 	}
 
 	public void clearArea(Block center) {
 		getLogger().info(
 				String.format("clearing area around: %d,%d,%d", center.getX(),
 						center.getY(), center.getZ()));
		int radius = getConfig().getInt("options.radius");
 		for (int radX = 0 - radius; radX <= radius; radX++) {
 			for (int radY = 0 - radius; radY <= radius; radY++) {
 				for (int radZ = 0 - radius; radZ <= radius; radZ++) {
 					Block b = center.getRelative(radX, radY, radZ);
 
 					if (isLiquid(b)) {
 						b.setType(Material.AIR);
 					}
 				}
 			}
 		}
 
 		updateBlocks(center);
 	}
 
 	public void cancelFlow(Block blockTo, BlockFromToEvent event) {
 		boolean blockWhenPowered = getConfig().getBoolean("options.invert");
		int radius = getConfig().getInt("options.radius");
 
 		for (int radX = 0 - radius; radX <= radius; radX++) {
 			for (int radY = 0 - radius; radY <= radius; radY++) {
 				for (int radZ = 0 - radius; radZ <= radius; radZ++) {
 
 					Block isThisSponge = blockTo.getRelative(radX, radY, radZ);
 					Material id = isThisSponge.getType();
 					if (blockWhenPowered == false) {
 						if (id == Material.SPONGE && !ispowered(isThisSponge)) {
 							// getLogger().info(String.format("power off, flow cancelled sponge/from/to: %d,%d,%d / %d,%d,%d / %d,%d,%d",
 							// isThisSponge.getX(),isThisSponge.getY(),isThisSponge.getZ(),
 							// event.getBlock().getX(),event.getBlock().getY(),event.getBlock().getZ(),
 							// blockTo.getX(),blockTo.getY(),blockTo.getZ()));
 							event.setCancelled(true);
 							return;
 						}
 					} else {
 						if (id == Material.SPONGE && ispowered(isThisSponge)) {
 							// getLogger().info("power on, flow cancelled");
 							event.setCancelled(true);
 							return;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	// is it a liquid that we care to remove?
 	public boolean isLiquid(Block block) {
 		boolean waterSponge = getConfig().getBoolean("options.water");
 		boolean lavaSponge = getConfig().getBoolean("options.lava");
 
 		if (lavaSponge
 				&& (block.getType() == Material.STATIONARY_LAVA || block
 						.getType() == Material.LAVA)) {
 			return true;
 		}
 		if (waterSponge
 				&& (block.getType() == Material.STATIONARY_WATER || block
 						.getType() == Material.WATER)) {
 			return true;
 		}
 		// default false :(
 		return false;
 
 	}
 
 	public void updateBlocks(Block center) {
		int radius = getConfig().getInt("options.radius");
 		// terrible debug code left in:
 		//getLogger().info(
 		//		String.format("updating area around: %d,%d,%d", center.getX(),
 		//				center.getY(), center.getZ()));
		for (int x = -(radius + 1); x <= (radius + 1); x++) {
			for (int y = -(radius + 1); y <= (radius + 1); y++) {
				for (int z = -(radius + 1); z <= (radius + 1); z++) {
 
 					final Block block = center.getRelative(x, y, z);
 					// only update the block if it is water/lava ect
 					if (isLiquid(block)) {
 						// use the original id if all else fails (no need to update)
 						int tempid = block.getTypeId();
 						if (tempid == Material.LAVA.getId()
 								|| tempid == Material.STATIONARY_LAVA.getId()) {
 							//use "moving" lava so that the server knows to update it
 							tempid = Material.LAVA.getId();
 							}
 						if (tempid == Material.WATER.getId()
 								|| tempid == Material.STATIONARY_WATER.getId()) {
 							//use "moving" water so that the server knows to update it
 							tempid = Material.WATER.getId();
 						}
 
 						byte tempdata = block.getData();
 						// TODO: for some reason when it is the last block of
 						// water only it fails to update?
 						
 						// terrible debug code left in:
 						// getLogger().info(String.format("updating block / around: %s,%s / %d,%d,%d",
 						// tempid,tempdata,block.getX(),block.getY(),block.getZ()));
 						
 						//removeing the block, so that there is a block change to update with
 						block.setType(Material.AIR);
 
 						//this now sets it to what it was, causing a update
 						block.setTypeIdAndData(tempid, tempdata, true);
 					}
 				}
 			}
 		}
 	}
 
 	public boolean ispowered(Block tocheck) {
 		return (tocheck.isBlockIndirectlyPowered() || tocheck.isBlockPowered());
 		// return tocheck.isBlockPowered();
 	}
 
 }

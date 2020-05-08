 package org.bonsaimind.bukkitplugins.redsponge;
 
 import java.util.logging.Level;
 
 import org.bukkit.block.Block;
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
  * Big list of TODO's:
  * 
  * Update blocks on removal
  * 
  * move around the getConfig() stuff
  *	anything that gets from the config with the same settings 
  *	likely has need of refactoring into sub-functions (see clearwater vs clearlava, too much code dupe)
  * 
  * 
  * 
  * @author admalledd
  */
 public class Plugin extends JavaPlugin implements Listener
 {
 	@Override
 	public void onEnable()
 	{
 		getServer().getPluginManager().registerEvents(this,this);
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.getLogger().log(Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
 	}
 	
 	@Override
 	public void onDisable()
 	{
 		PluginDescriptionFile pdfFile = this.getDescription();
 		
 		this.getLogger().log(Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled!");
 	}
 
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent event)
 	{
 		Block blockPlaced = event.getBlock();
 		spongeEvent(blockPlaced);
 		
 	}
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event) 
 	{
 		Block block = event.getBlock();
 		if (block.getType() == Material.SPONGE)
 		{
 			updateBlocks(block);
 		}
 	}
 	
 	
 	@EventHandler
 	public void onBlockFlow(BlockFromToEvent event)
 	{
 		Block blockFrom = event.getBlock();
 		Block blockTo = event.getToBlock();
 		boolean isLava = blockFrom.getType() == Material.LAVA || blockFrom.getType() == Material.STATIONARY_LAVA;
 		boolean isWater = blockFrom.getType() == Material.WATER || blockFrom.getType() == Material.STATIONARY_WATER;
 		boolean waterSponge = getConfig().getBoolean("options.water");
 		boolean lavaSponge = getConfig().getBoolean("options.lava");
 
 		// is the flow water or lava and are the sponges enabled
 		if ((lavaSponge && isLava) || (waterSponge && isWater))
 		{
 			cancelFlow(blockTo, event);
 		}
 	}
 	@EventHandler
 	public void onBlockRedstoneChange(BlockRedstoneEvent event)
 	{
 		if (!getConfig().getBoolean("options.redstone"))			
 		{
 			return; //return early to bypass redstone logics
 		}
 		Block blockTo = event.getBlock();
 		boolean blockWhenPowered = getConfig().getBoolean("invert");
 
 		//check around block to find if any are our sponge
 		for (int radX = -1; radX <= 1; radX++)
 		{
 			for (int radY = -1; radY <= 1; radY++)
 			{
 				for (int radZ = -1; radZ <= 1; radZ++)
 				{
 
 					
 					Block isThisSponge = blockTo.getRelative(radX, radY, radZ);
 					
 					if (isThisSponge.getType() == Material.SPONGE)
 					{
 						getLogger().info(String.format("power change, blockcheck: %d,%d,%d",
 								isThisSponge.getX(),isThisSponge.getY(),isThisSponge.getZ()));
 						if (blockWhenPowered == false)//normal sponge (deactivates when powered)
 						{
 							if (!ispowered(isThisSponge))
 							{
 								//now powered, let the water flow!
 								getLogger().info("power off, updated");
 								updateBlocks(isThisSponge);
 	
 							}
 							else if (ispowered(isThisSponge))
 							{
 								getLogger().info("power on, cleared");
 								//no longer powered, clear it all again
 								spongeEvent(isThisSponge);
 							}
 						}
 	
 						else//inverted sponge (clears when powered only)
 						{
 							if (!ispowered(isThisSponge))
 							{
 								//not powered, let the water flow!
 								getLogger().info("power off, updated");
 								updateBlocks(isThisSponge);
 							}
 							else if (ispowered(isThisSponge))
 							{
 								//powered, clear it all again
 								getLogger().info("power on, cleared");
 								spongeEvent(isThisSponge);
 							}
 						}
 					}
 				}
 			}
 		}
 	}	
 	
 
 	public void spongeEvent(Block blockPlaced)
 	{
 		boolean blockWhenPowered = getConfig().getBoolean("invert");
 		
 		if (blockPlaced.getType() == Material.SPONGE)
 		{
 			// blockWhenPowered == false -> default, a sponge works like a
 			// sponge without redstone current
 			//check here as well in case on block place we are powered
 			if (blockWhenPowered == false)
 			{
 				if (!ispowered(blockPlaced))
 				{
 					clearArea(blockPlaced);
 					
 				}
 			}
 			// blockWhenPowered == true -> sponge works like sponge WITH
 			// redstone current
 			else
 			{
 				if (ispowered(blockPlaced))
 				{
 					clearArea(blockPlaced);
 				}
 			}
 		}
 	}
 	public void clearArea(Block center)
 	{
 		getLogger().info(String.format("clearing area around: %d,%d,%d", center.getX(),center.getY(),center.getZ()));
 		int radius = getConfig().getInt("options.raduis");
 		for (int radX = 0 - radius; radX <= radius; radX++)
 		{
 			for (int radY = 0 - radius; radY <= radius; radY++)
 			{
 				for (int radZ = 0 - radius; radZ <= radius; radZ++)
 				{
 					Block b = center.getRelative(radX, radY, radZ);
 
 					if (isLiquid(b))
 					{
 						b.setType(Material.AIR);
 					}
 				}
 			}
 		}
 		
 		updateBlocks(center);
 	}
 
 
 	public void cancelFlow(Block blockTo, BlockFromToEvent event)
 	{
 		boolean blockWhenPowered = getConfig().getBoolean("invert");
 		int radius = getConfig().getInt("options.raduis");
 
 		for (int radX = 0 - radius; radX <= radius; radX++)
 		{
 			for (int radY = 0 - radius; radY <= radius; radY++)
 			{
 				for (int radZ = 0 - radius; radZ <= radius; radZ++)
 				{
 					
 					Block isThisSponge = blockTo.getRelative(radX, radY, radZ);
 					Material id = isThisSponge.getType();
 					if (blockWhenPowered == false)
 					{
 						if (id == Material.SPONGE && !ispowered(isThisSponge))
 						{
 							getLogger().info(String.format("power off, flow cancelled sponge/from/to: %d,%d,%d / %d,%d,%d / %d,%d,%d",
 									isThisSponge.getX(),isThisSponge.getY(),isThisSponge.getZ(),
 									event.getBlock().getX(),event.getBlock().getY(),event.getBlock().getZ(),
 									blockTo.getX(),blockTo.getY(),blockTo.getZ()));
 							event.setCancelled(true);
 							return;
 						}
 					}
 					else
 					{
 						if (id == Material.SPONGE && ispowered(isThisSponge))
 						{
 							//getLogger().info("power on, flow cancelled");
 							event.setCancelled(true);
 							return;
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	//is it a liquid that we care to remove?
 	public boolean isLiquid(Block block)
 	{
 		boolean waterSponge = getConfig().getBoolean("options.water");
 		boolean lavaSponge = getConfig().getBoolean("options.lava");
 		
 		if (lavaSponge && (block.getType() == Material.STATIONARY_LAVA || block.getType() == Material.LAVA))
 		{
 			return true;
 		}
 		if (waterSponge && (block.getType() == Material.STATIONARY_WATER || block.getType() == Material.WATER))
 		{
 			return true;
 		}
 		//default false :(
 		return false;
 		
 	}
 	public void updateBlocks(Block center)
 	{
 		int raduis = getConfig().getInt("options.raduis");
 		getLogger().info(String.format("updating area around: %d,%d,%d", center.getX(),center.getY(),center.getZ()));
 		for (int x = -(raduis+1); x <= (raduis+1); x++){
 			for (int y = -(raduis+1); y <= (raduis+1); y++){
 				for (int z = -(raduis+1); z <= (raduis+1); z++) {
 					
 					final Block block = center.getRelative(x,y,z);
 					if (isLiquid(block)){//only update the block if it is water/lava ect
 						int tempid = block.getTypeId();
						if (tempid==Material.LAVA.getId() || tempid== Material.STATIONARY_LAVA.getId())
						{ 
							tempid = Material.LAVA.getId();
						}
						if (tempid==Material.WATER.getId() ||tempid == Material.STATIONARY_WATER.getId())
						{ 
							tempid = Material.WATER.getId();
						}
						
						
 						byte tempdata = block.getData();
 						//TODO: for some reason when it is the last block of water only it fails to update?
						getLogger().info(String.format("updating block / around: %s,%s / %d,%d,%d", tempid,tempdata,block.getX(),block.getY(),block.getZ()));
 						block.setType(Material.AIR);//force block update by changing it!
 						block.setTypeIdAndData(tempid, tempdata, true);//this now sets it to what it was, updates
 					}
 				}
 			}
 		}
 	}
 	public boolean ispowered(Block tocheck)
 	{
 		return (tocheck.isBlockIndirectlyPowered()||tocheck.isBlockPowered());
 	}
 
 }

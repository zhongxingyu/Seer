 package com.icomeinpieces.UndroppableDroppables;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.inventory.ItemStack;
 
 public class UDBlockListener extends BlockListener
 {
 	public static UndroppableDroppables UDP;
 	public UDBlockListener(UndroppableDroppables instance)
 	{
 	    UDP = instance;
 	}
 	public void onBlockBreak(BlockBreakEvent event) {
 		if (!event.isCancelled())
 		{
 			Player player = event.getPlayer();
 			Block block = event.getBlock();
 			if (checkWG(player, block) && checkPB(player))
 			{
 				if (block.getTypeId() == 47 && (UDP).permissionHandler.has(player, "ud.drop.bookcase")) //bookcases drop bookcases not nothing
 				{
 			        Location locale = new Location(event.getBlock().getWorld(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), 0.0F, 0.0F);
 			        switch (UDP.bookshelfDrop)
 			        {
 				        case 1:
 				        {
 				        	event.getBlock().getWorld().dropItem(locale, new ItemStack(47, 1, (short) 1));
 					        event.setCancelled(true);
 					        event.getBlock().setType(Material.AIR);
 				        	break;
 				        }
 				        case 2:
 				        {
 				        	event.getBlock().getWorld().dropItem(locale, new ItemStack(340, 3));
 				        	event.getBlock().getWorld().dropItem(locale, new ItemStack(5, 6));
 					        event.setCancelled(true);
 					        event.getBlock().setType(Material.AIR);
 				        	break;
 				        }
 			        }
 				}
 				if (block.getTypeId() == 53 && (UDP).permissionHandler.has(player, "ud.drop.woodenstairs")) //wooden stairs drops wooden stairs not wood
 				{
 			        Location locale = new Location(event.getBlock().getWorld(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), 0.0F, 0.0F);
 			        switch (UDP.woodenstairsDrop)
 			        {
 				        case 1:
 				        {
 				        	event.getBlock().getWorld().dropItem(locale, new ItemStack(53, 1, (short) 1));
 					        event.setCancelled(true);
 					        event.getBlock().setType(Material.AIR);
 				        	break;
 				        }
 				        case 2:
 				        {
 				        	event.getBlock().getWorld().dropItem(locale, new ItemStack(5, 6));
 					        event.setCancelled(true);
 					        event.getBlock().setType(Material.AIR);
 				        	break;
 				        }
 			        }
 				}
 				if (block.getTypeId() == 67 && (UDP).permissionHandler.has(player, "ud.drop.cobblestonestairs")) // stone stairs drops stone stairs not cobblestone
 				{
 			        Location locale = new Location(event.getBlock().getWorld(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), 0.0F, 0.0F);
 			        switch (UDP.cobblestonestairsDrop)
 			        {
 				        case 1:
 				        {
 				        	event.getBlock().getWorld().dropItem(locale, new ItemStack(67, 1, (short) 1));
 					        event.setCancelled(true);
 					        event.getBlock().setType(Material.AIR);
 				        	break;
 				        }
 				        case 2:
 				        {
 				        	event.getBlock().getWorld().dropItem(locale, new ItemStack(4, 6));
 					        event.setCancelled(true);
 					        event.getBlock().setType(Material.AIR);
 				        	break;
 				        }
 			        }
 				}
 				if (block.getTypeId() == 89 && (UDP).permissionHandler.has(player, "ud.drop.glowstone")) //glowstone drops glowstone not dust
 				{
 			        Location locale = new Location(event.getBlock().getWorld(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), 0.0F, 0.0F);
 			        switch (UDP.glowstoneDrop)
 			        {
 				        case 1:
 				        {
 				        	event.getBlock().getWorld().dropItem(locale, new ItemStack(89, 1, (short) 1));
 					        event.setCancelled(true);
 					        event.getBlock().setType(Material.AIR);
 				        	break;
 				        }
 				        case 2:
 				        {
 				        	event.getBlock().getWorld().dropItem(locale, new ItemStack(348, 9));
 					        event.setCancelled(true);
 					        event.getBlock().setType(Material.AIR);
 				        	break;
 				        }
 			        }
 				}
 				if (block.getTypeId() == 20 && (UDP).permissionHandler.has(player, "ud.drop.glass")) //glass drops glass not nothing
 				{
 			        Location locale = new Location(event.getBlock().getWorld(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), 0.0F, 0.0F);
 			        switch (UDP.glassDrop)
 			        {
 				        case 1:
 				        {
 				        	event.getBlock().getWorld().dropItem(locale, new ItemStack(20, 1));
 					        event.setCancelled(true);
 					        event.getBlock().setType(Material.AIR);
 				        	break;
 				        }
 				        case 2:
 				        {
 				        	event.getBlock().getWorld().dropItem(locale, new ItemStack(12, 1));
 					        event.setCancelled(true);
 					        event.getBlock().setType(Material.AIR);
 				        	break;
 				        }
 			        }
 				}
 				if (block.getTypeId() == 79 && (UDP).permissionHandler.has(player, "ud.drop.ice")) //ice drops ice not water in it's place
 				{
 			        Location locale = new Location(event.getBlock().getWorld(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), 0.0F, 0.0F);
 			        switch (UDP.iceDrop)
 			        {
 				        case 1:
 				        {
 				        	event.getBlock().getWorld().dropItem(locale, new ItemStack(79, 1));
 					        event.setCancelled(true);
 					        event.getBlock().setType(Material.AIR);
 				        	break;
 				        }
 				        case 2:
 				        {
 				        	event.getBlock().getWorld().dropItem(locale, new ItemStack(79, 1));
 					        event.setCancelled(true);
 					        event.getBlock().setType(Material.AIR);
 				        	break;
 				        }
 			        }
 				}
 				if (block.getTypeId() == 2 && (UDP).permissionHandler.has(player, "ud.drop.grass")) //grass drops grass not dirt
 				{
 			        Location locale = new Location(event.getBlock().getWorld(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), 0.0F, 0.0F);
 			        switch (UDP.grassDrop)
 			        {
 				        case 1:
 				        {
 				        	event.getBlock().getWorld().dropItem(locale, new ItemStack(2, 1));
 					        event.setCancelled(true);
 					        event.getBlock().setType(Material.AIR);
 				        	break;
 				        }
 				        case 2:
 				        {
 				        	event.getBlock().getWorld().dropItem(locale, new ItemStack(295, 1));
 					        event.setCancelled(true);
 					        event.getBlock().setType(Material.AIR);
 				        	break;
 				        }
 			        }
 				}
 //				if (block.getTypeId() == 52 && (UDP).permissionHandler.has(player, "ud.drop.creaturespawner"))
 //				{
 //					Location locale = new Location(event.getBlock().getWorld(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), 0.0F, 0.0F);
 //					CreatureSpawner spawner = null;
 //					if (event.getBlock() instanceof CreatureSpawner) spawner = (CreatureSpawner) event.getBlock();
 //					switch (UDP.spawnerDrop)
 //					{
 //					case 1:
 //					{
 //			        	event.getBlock().getWorld().dropItem(locale, (ItemStack) spawner);
 //				        event.setCancelled(true);
 //				        event.getBlock().setType(Material.AIR);
 //					}
 //					}
 //					
 //				}
 				if (block.getTypeId() == 8 && UDP.iceDrop == 1) //handles the water that generates after an ice block is broken
 				{
 					event.getBlock().setType(Material.AIR);
 				}
 			}
			else
			{
				UDP.log.info("Something went wrong with WorldGuard or Permissions detection, please advise author");
			}
 		}
 	}
 	private boolean checkPB(Player player)
 	{
 		World world = player.getWorld();
 		if (UDP.permissionHandler != null)
 		{
 			String playerGroup = UDP.permissionHandler.getGroup(world.getName(), player.getName());
 			if (UDP.permissionHandler.canGroupBuild(world.getName(), playerGroup))
 			{
 				return true;
 			}
 			else
 			{
 				return false;
 			}
 		}
 		else
 		{
 			return true;
 		}
 	}
 	private boolean checkWG(Player player, Block block)
 	{
 		if (UndroppableDroppables.WGP != null)
 		{
 			if (UndroppableDroppables.WGP.canBuild(player, block))
 			{
 				return true;
 			}
 			else
 			{
 				return false;
 			}
 		}
 		else
 		{
 			return true;
 		}
 	}
 }

 package aor.SimplePlugin.Spells;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import java.lang.Math;
 
 import aor.SimplePlugin.Runnables.RunnableDestroyCactus;
 
 import aor.SimplePlugin.SimplePlugin;
 import aor.SimplePlugin.Spell;
 import aor.SimplePlugin.Runnables.RunnableShootArrow;
 
 public class SpikeSpell extends Spell {
 	public SpikeSpell(SimplePlugin instance) // Constructor.
 	{
 		plugin = instance;
 		spellName = "Spikes";
 		spellDescription = "Summons a cactus on command. Needs 4 cacti, 1 sand.";
 		shortName = "Spikes";
 
 		setRequiredItems(new ItemStack(Material.CACTUS, 4), new ItemStack(Material.SAND, 1)); // 1 cactus, 1 sandblock.
 	}
 
 	public boolean canPlaceCactus(Block targetBlock)
 	{
 		if (targetBlock.getType() == Material.CACTUS)
 		{
 			return false; // Cannot spawn cactus on cactus.
 		}
 		
 		for (int i = 1; i <= 3; i++) // For each space above the block...
 		{
 			if (targetBlock.getRelative(0, i, 0).getType() == Material.AIR) { } // If it's air do nothing
 			else { return false; } // Otherwise you can't place a cactus.
 		}
 		
 		for (int i = 1; i <= 3; i++) // For each space left of the block...
 		{
 			if (targetBlock.getRelative(1, i, 0).getType() == Material.AIR) { } // If it's air do nothing
 			else { return false; } // Otherwise you can't place a cactus.
 		}
 		
 		for (int i = 1; i <= 3; i++) // For each space right of the block...
 		{
 			if (targetBlock.getRelative(-1, i, 0).getType() == Material.AIR) { } // If it's air do nothing
 			else { return false; } // Otherwise you can't place a cactus.
 		}
 		
 		for (int i = 1; i <= 3; i++) // For each space in front of the block...
 		{
 			if (targetBlock.getRelative(0, i, 1).getType() == Material.AIR) { } // If it's air do nothing
 			else { return false; } // Otherwise you can't place a cactus.
 		}
 		
 		for (int i = 1; i <= 3; i++) // For each space behind the block...
 		{
 			if (targetBlock.getRelative(0, i, -1).getType() == Material.AIR) { } // If it's air do nothing
 			else { return false; } // Otherwise you can't place a cactus.
 		}
 		
 		return true; // If nothing turned up.
 	}
 	
 	public void castSpell(Player player)
 	{
 		if (checkInventoryRequirements(player.getInventory())) // They have the required items.
 		{
 
 
 			Block targetBlock = player.getTargetBlock(null, 101);
 
 			if ((targetBlock.getType() != Material.AIR) && (targetBlock.getType() != Material.BEDROCK)) // Can't do it to air or bedrock.
 			{
 
 				Location loc = targetBlock.getLocation();
 				Location playerLoc = player.getLocation();
 
 				// Distance formula.
 				double xdiff = loc.getX() - playerLoc.getX();
 				double ydiff = loc.getZ() - playerLoc.getZ();
 				double xdiffsq = xdiff * xdiff;
 				double ydiffsq = ydiff * ydiff;
 				double xyadd = xdiffsq + ydiffsq;
 				double distance = Math.sqrt(xyadd);
 				// Distance formula.
 
 				if (distance < 30) // Maximum distance is 31.
 				{
 					
 					
 					if (canPlaceCactus(targetBlock)) // If the space is compatable with a cactus
 					{
						removeRequiredItemsFromInventory(player.getInventory()); // Remove required items here.
 						Material originalTargetMaterial = targetBlock.getType();
 						boolean sandstoneSupport = false;
 						
 						if (targetBlock.getRelative(0, -1, 0).getType() == Material.AIR) // if said cactus would fall apart
 						{
 							targetBlock.getRelative(0, -1, 0).setType(Material.SANDSTONE); // Place a sandstone support
 							sandstoneSupport = true; // Set the sandstonesupport variable.
 						}
 						
 						targetBlock.setType(Material.SAND); // Set the target block to sand.
 						
 						for (int i = 1; i <= 3; i++) // For each space above it
 						{
 							targetBlock.getRelative(0, i, 0).setType(Material.CACTUS); // Make cactus. Go forth and make cactus.
 							
 							
 						}
 						
 						player.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RunnableDestroyCactus(targetBlock, originalTargetMaterial, sandstoneSupport), 300);
 						
 						player.sendMessage("SPIKES SPIKES SPIKES BABY");
 
 					}
 					else
 					{
 						player.sendMessage("Cannot cast! A cactus wouldn't fit there!");
 					}
 				}
 				
 				
 				else
 				{
 					player.sendMessage("Could not cast! Target is out of range!");
 				}
 
 
 			}
 		}
 
 		else
 		{
 
 			player.sendMessage("Could not cast! Requires 4 cacti and 1 sand.");
 
 		}
 
 		/*
 		Block targetBlock = player.getTargetBlock(null, 30); // Select the target block.
 		if (targetBlock.getType() != Material.AIR) // No placing bedrock midair!
 		{
 
 			Location loc = targetBlock.getLocation();
 			(player.getWorld().getBlockAt(loc)).setType(Material.SAND);
 
 			for (int i = 0; i < 3;i++)
 			{
 				loc.setY(loc.getY()+1);
 				(player.getWorld().getBlockAt(loc)).setType(Material.CACTUS);
 			}
 			player.getWorld().getBlockAt(loc);
 
 		}
 		 */	
 	}
 
 }

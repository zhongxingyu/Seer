 package aor.SimplePlugin.Spells;
 
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.entity.Player;
 
 import aor.SimplePlugin.SimplePlugin;
 import aor.SimplePlugin.Spell;
 import aor.SimplePlugin.Runnables.RunnableBuildFortCactus;
 
 public class SpikeFortSpell extends Spell {
 
 	public SpikeFortSpell(SimplePlugin instance) // Constructor.
 	{
 		plugin = instance;
 		spellName = "Spike Fort";
 		spellDescription = "Summons a fortification of cacti on command.  Needs 64 cacti, 25 sand, 20 sandstone.";
		shortName = "SpikeFort";
 
 		setRequiredItems(new ItemStack(Material.CACTUS, 64), new ItemStack(Material.SAND, 25), new ItemStack(Material.SANDSTONE, 20)); // 64 cactus, 25 sandblock, 20 sandstone.
 	}
 
 	public double distanceBetween(Location locA, Location locB)
 	{
 		// Distance formula.
 		double xdiff = locA.getX() - locB.getX();
 		double ydiff = locA.getZ() - locB.getZ();
 		double xdiffsq = xdiff * xdiff;
 		double ydiffsq = ydiff * ydiff;
 		double xyadd = xdiffsq + ydiffsq;
 		return Math.sqrt(xyadd);
 		// Distance formula.
 	}
 
 
 	public Block[] blockSquare(Location center)
 	{
 		Block[] blocks = new Block[20];
 
 		World world = center.getWorld();
 
 		blocks[1] = world.getBlockAt(center.getBlockX(), center.getBlockY(), center.getBlockZ() + 2);
 		blocks[2] = world.getBlockAt(blocks[1].getLocation().getBlockX() + 2, blocks[1].getLocation().getBlockY(), blocks[1].getLocation().getBlockZ());
 		blocks[3] = world.getBlockAt(blocks[1].getLocation().getBlockX() - 2, blocks[1].getLocation().getBlockY(), blocks[1].getLocation().getBlockZ());
 		blocks[4] = world.getBlockAt(center.getBlockX() + 2, center.getBlockY(), center.getBlockZ());
 		blocks[5] = world.getBlockAt(blocks[4].getLocation().getBlockX(), blocks[4].getLocation().getBlockY(), blocks[4].getLocation().getBlockZ() - 2);
 		blocks[0] = world.getBlockAt(center.getBlockX(), center.getBlockY(), center.getBlockZ() - 2);
 		blocks[6] = world.getBlockAt(blocks[0].getLocation().getBlockX() - 2, blocks[0].getLocation().getBlockY(), blocks[0].getLocation().getBlockZ());
 		blocks[7] = world.getBlockAt(center.getBlockX() - 2, center.getBlockY(), center.getBlockZ());
 		
 		blocks[8] = world.getBlockAt(center.getBlockX() - 1, center.getBlockY(), center.getBlockZ() + 3);
 		blocks[9] = world.getBlockAt(center.getBlockX() - 3, center.getBlockY(), center.getBlockZ() + 3);
 		blocks[10] = world.getBlockAt(center.getBlockX() + 1, center.getBlockY(), center.getBlockZ() + 3);
 		blocks[11] = world.getBlockAt(center.getBlockX() + 3, center.getBlockY(), center.getBlockZ() + 3);
 		blocks[12] = world.getBlockAt(center.getBlockX() - 3, center.getBlockY(), center.getBlockZ() + 1);
 		blocks[13] = world.getBlockAt(center.getBlockX() - 3, center.getBlockY(), center.getBlockZ() - 1);
 		blocks[14] = world.getBlockAt(center.getBlockX() - 3, center.getBlockY(), center.getBlockZ() - 3);
 		blocks[15] = world.getBlockAt(center.getBlockX() - 1, center.getBlockY(), center.getBlockZ() - 3);
 		blocks[16] = world.getBlockAt(center.getBlockX() + 1, center.getBlockY(), center.getBlockZ() - 3);
 		blocks[17] = world.getBlockAt(center.getBlockX() + 3, center.getBlockY(), center.getBlockZ() - 3);
 		blocks[18] = world.getBlockAt(center.getBlockX() + 3, center.getBlockY(), center.getBlockZ() - 1);
 		blocks[19] = world.getBlockAt(center.getBlockX() + 3, center.getBlockY(), center.getBlockZ() + 1);
 
 		return blocks;
 
 	}
 
 
 
 	public void castSpell(Player player)
 	{
 		if (checkInventoryRequirements(player.getInventory()))
 		{
 			
 			removeFromInventory(player.getInventory(), new ItemStack(Material.SAND, 5)); // Take out the extra items.
 			removeFromInventory(player.getInventory(), new ItemStack(Material.CACTUS, 4)); // Take out the extra items.
 			
 			Block[] blocks = blockSquare(new Location(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ())); // Get the block square.
 
 			int b = 0; // The first cactus goes up immediately.
 
 			for (int i = 0; i < blocks.length; i++)
 			{
 				player.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RunnableBuildFortCactus(blocks[i], player, plugin), b);
 				b = b + 1; // Once every server tick.
 			}
 		}
 
 		else { player.sendMessage("Could not cast! Requires 64 cacti, 25 sand, 20 sandstone."); } // They don't have the required items.
 	}
 	
 	
 
 }

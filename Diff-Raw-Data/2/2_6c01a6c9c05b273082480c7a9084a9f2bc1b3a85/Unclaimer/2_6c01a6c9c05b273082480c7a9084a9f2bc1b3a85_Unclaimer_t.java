 package net.betterverse.unclaimed;
 
 import java.util.Random;
 import java.util.logging.Level;
 import net.betterverse.unclaimed.Unclaimed;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 
 /**
  * Class to find any unclaimed chunks in the background. Any unclaimed chunks
  * found will be added to a list. The /unclaimed teleport command will grab a
  * random chunk from that list to teleport to.
  *
  * @author Ryan
  */
 public class Unclaimer {
 	private final Unclaimed plugin;
 
 	private Random random = new Random();
 
 	public Unclaimer(Unclaimed plugin) {
 		this.plugin = plugin;
 	}
 
 	public Location generateUnclaimedLocation() {
 		int x;
 		int z;
 		Chunk chunk;
 		Location tpLoc = null;
 		do {
 			x = random.nextInt(plugin.getConfiguration().getMaxX() * 2) - plugin.getConfiguration().getMaxX();
 			z = random.nextInt(plugin.getConfiguration().getMaxZ() * 2) - plugin.getConfiguration().getMaxZ();
			chunk = plugin.getServer().getWorld("world").getChunkAt(x >> 4, z >> 4);
 		} while ((tpLoc = getLocationFor(chunk)) == null);
 		return tpLoc;
 	}
 
 	private Location getLocationFor(Chunk c) {
 		// First we want to try and provide a random result.
 		Location location = getRandomLocationFor(c, plugin.getConfiguration().getMinTeleportationY(), plugin.getConfiguration().getMaxTeleportationY());
 		if (location != null) {
 			return location;
 		}
 		// Since our random attempts have failed, now we systematically go through the chunk looking for a valid place to set the player.
 		for (int x = 0; x < 16; x++) {
 			for (int z = 0; z < 16; z++) {
 				location = getLocationFor(c, x, z, plugin.getConfiguration().getMinTeleportationY(), plugin.getConfiguration().getMaxTeleportationY());
 				if (location != null) {
 					return location;
 				}
 			}
 		}
 		plugin.getLogger().log(Level.INFO, "Valid dropoff could not be found for a chunk!");
 		return null;
 	}
 
 	private Location getRandomLocationFor(Chunk c, int minY, int maxY) {
 		Random rand = new Random();
 		// Make five attempts to place them randomly.  If all five of these fail, then we return null.
 		// Perhaps in the future this should/could/needs to be added to the config.
 		for (int attempts = 0; attempts < 5; attempts++) {
 			int x = rand.nextInt(16);
 			int z = rand.nextInt(16);
 			Location loc = getLocationFor(c, x, z, minY, maxY);
 			if (loc != null) {
 				return loc;
 			}
 		}
 		return null;
 	}
 
 	private Location getLocationFor(Chunk c, int x, int z, int minY, int maxY) {
 		// y is decremented by three because we need two blocks worth of space for the player to stand on.
 		// ^ this makes no sense ~albireox
 		for (int y = maxY; y >= minY; y--) {
 			Block b = c.getBlock(x, y - 1, z); // The location is the one above this block
 			if (!b.getType().isSolid() || b.getType().equals(Material.CACTUS)) {
 				continue;
 			}
 
 			Block air1 = b.getLocation().add(0, 1, 0).getBlock();
 			Block air2 = b.getLocation().add(0, 2, 0).getBlock();
 			if ((air1.getType().equals(Material.AIR) && air2.getType().equals(Material.AIR)) || b.getY() == b.getWorld().getMaxHeight() - 1) { // Check for max block
 				return b.getLocation().add(0.5, 1, 0.5); // Teleport to the center of the block
 			}
 		}
 		return null;
 	}
 }

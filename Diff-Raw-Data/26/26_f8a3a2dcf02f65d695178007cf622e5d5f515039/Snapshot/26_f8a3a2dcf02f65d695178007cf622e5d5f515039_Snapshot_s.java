 package us.fitzpatricksr.cownet.commands;
 
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import us.fitzpatricksr.cownet.CowNetThingy;
 import us.fitzpatricksr.cownet.utils.SchematicUtils;
 import us.fitzpatricksr.cownet.utils.StringUtils;
 import us.fitzpatricksr.cownet.utils.TimeUtils;
 
 import java.io.File;
 
 public class Snapshot extends CowNetThingy {
 	@Setting
 	private long resetIntervalSeconds = 60 * 60;  // 1 hour in seconds
 	@Setting
 	private int maxBlocks = 10000000;
 	@Setting
 	private boolean resetAir = true;
 
 	@Override
 	protected void onEnable() throws Exception {
 		// start the countdown
 		debugInfo("String countdown timer");
 		performCountdown(0);
 	}
 
 	@Override
 	protected String getHelpString(CommandSender sender) {
		return "usage: snapshot [ 'delete' | 'reset' | 'create > ]";
 	}
 
 	@CowCommand(permission = "release")
 	private boolean doDelete(Player player) {
 		return false;
 	}
 
 	@CowCommand
 	private boolean doUnsnapshot(Player player) {
 		World world = player.getWorld();
 		RegionManager regionManager = getWorldGuard().getRegionManager(world);
 		ApplicableRegionSet regions = regionManager.getApplicableRegions(player.getLocation());
 		if (regions.size() == 0) {
 			player.sendMessage("You are not in a WorldGuard region.  Nothing to snapshot.");
 		} else if (regions.size() > 1) {
 			player.sendMessage("You are multiple regions:");
 			for (ProtectedRegion region : regions) {
 				player.sendMessage("  " + region.getId());
 			}
 		} else {
 			ProtectedRegion region = regions.iterator().next();
 			String regionName = region.getId();
 			if (!region.isOwner(player.getName())) {
 				player.sendMessage("You do not own this region: " + regionName);
 			} else {
 				logInfo("Taking snapshot of " + regionName);
 				File schematicFile = new File(getSchematicsFolder(), regionName + ".schematic");
 				if (schematicFile.exists()) {
 					schematicFile.delete();
 				}
 				debugInfo("  file: " + schematicFile);
 				logInfo("Snapshot removed.");
 			}
 		}
 		return true;
 	}
 
 	@CowCommand
 	private boolean doSnapshot(Player player) {
 		World world = player.getWorld();
 		RegionManager regionManager = getWorldGuard().getRegionManager(world);
 		ApplicableRegionSet regions = regionManager.getApplicableRegions(player.getLocation());
 		if (regions.size() == 0) {
 			player.sendMessage("You are not in a WorldGuard region.  Nothing to snapshot.");
 		} else if (regions.size() > 1) {
 			player.sendMessage("You are multiple regions:");
 			for (ProtectedRegion region : regions) {
 				player.sendMessage("  " + region.getId());
 			}
 		} else {
 			ProtectedRegion region = regions.iterator().next();
 			String regionName = region.getId();
 			logInfo("Taking snapshot of " + regionName);
 			File schematicFile = new File(getSchematicsFolder(), regionName + ".schematic");
 			if (schematicFile.exists()) {
 				schematicFile.delete();
 			}
 			Vector min = region.getMinimumPoint();
 			Vector max = region.getMaximumPoint();
 			Location loc1 = new Location(world, min.getX(), min.getY(), min.getZ());
 			Location loc2 = new Location(world, max.getX(), max.getY(), max.getZ());
 			debugInfo("  file: " + schematicFile);
 			debugInfo("  loc1: " + loc1);
 			debugInfo("  loc2: " + loc2);
 			SchematicUtils.saveSchematic(schematicFile, loc1, loc2, maxBlocks);
 			logInfo("Saved.");
 		}
 		return true;
 	}
 
 	@CowCommand
 	private boolean doReset(Player player) {
 		return resetWorldRegions(player.getWorld());
 	}
 
 	private boolean resetWorldRegions(World w) {
 		RegionManager regionManager = getWorldGuard().getRegionManager(w);
 		for (ProtectedRegion region : regionManager.getRegions().values()) {
 			String regionName = region.getId();
 			File schematicFile = getRegionSaveFile(region);
 			if (schematicFile != null) {
 				logInfo("Resetting " + regionName);
 				Vector min = region.getMinimumPoint();
 				Location where = new Location(w, min.getX(), min.getY(), min.getZ());
 				debugInfo("  file: " + schematicFile);
 				debugInfo("  location: " + where);
 				SchematicUtils.placeSchematic(schematicFile, where, resetAir, maxBlocks);
 			} else {
 				debugInfo("Skipping region: " + regionName);
 			}
 		}
 		return true;
 	}
 
 	private void performCountdown(final long timeWhenDone) {
 		long timeLeft = timeWhenDone - System.currentTimeMillis();
 		long timeUntilNext = timeUntilNextWake(timeLeft);
 		long ticksUntilNext = TimeUtils.millisToTicks(timeUntilNext);
 		if (ticksUntilNext > 0) {
 			broadcast("Regenerating jails in " + StringUtils.durationString(timeLeft / 1000));
 			debugInfo("Regenerating jails in " + StringUtils.durationString(timeLeft / 1000));
 			getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
 				public void run() {
 					performCountdown(timeWhenDone);
 				}
 			}, ticksUntilNext);
 		} else {
 			// OK, it's regen time
 			for (World w : getPlugin().getServer().getWorlds()) {
 				resetWorldRegions(w);
 			}
 			performCountdown(System.currentTimeMillis() + resetIntervalSeconds * 1000);
 		}
 	}
 
 	private long timeUntilNextWake(long timeLeft) {
 		if (timeLeft <= 0) {
 			// OK, time to regenerate
 			return 0;
 		} else if (timeLeft <= 10 * 1000) {
 			// final 10 seconds
 			return 1 * 1000;
 		} else if (timeLeft <= 60 * 1000) {
 			// final minute
 			return 10 * 1000;
 		} else if (timeLeft <= 5 * 60 * 1000) {
 			// final 5 minutes
 			return 60 * 1000;
 		} else if (timeLeft <= 10 * 60 * 1000) {
 			// final 5 minutes
 			return 5 * 60 * 1000;
 		} else {
			return 10 * 60 * 1000;
 		}
 	}
 
 	private File getRegionSaveFile(ProtectedRegion region) {
 		String regionName = region.getId();
 		File schematicFile = new File(getSchematicsFolder(), regionName + ".schematic");
 		if (schematicFile.exists()) {
 			return schematicFile;
 		} else {
 			return null;
 		}
 	}
 
 	protected File getSchematicsFolder() {
 		try {
			File folder = getPlugin().getDataFolder();
			if (!folder.exists()) {
 				return null;
 			}
 			return folder;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	private void broadcast(String msg) {
 		for (Player player : getPlugin().getServer().getOnlinePlayers()) {
 			player.sendMessage(msg);
 		}
 	}
 
 	private WorldGuardPlugin getWorldGuard() {
 		Plugin worldPlugin = getPlugin().getServer().getPluginManager().getPlugin("WorldGuard");
 		if (worldPlugin == null || !(worldPlugin instanceof WorldGuardPlugin)) {
 			debugInfo("WorldGuard must be loaded first");
 			worldPlugin = null;
 		}
 		return (WorldGuardPlugin) worldPlugin;
 	}
 }
 

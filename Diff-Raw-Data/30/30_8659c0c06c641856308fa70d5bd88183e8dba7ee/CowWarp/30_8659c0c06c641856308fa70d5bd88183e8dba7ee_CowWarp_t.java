 package us.fitzpatricksr.cownet.commands;
 
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import us.fitzpatricksr.cownet.CowNetThingy;
 import us.fitzpatricksr.cownet.utils.CowNetConfig;
 import us.fitzpatricksr.cownet.utils.StringUtils;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 public class CowWarp extends CowNetThingy {
 	@Setting
 	private String publicWarpKey = "public";
 	@Setting
 	private String defaultOwner = "public";
 	@Setting
 	private String defaultWorld = "guestWorld";
 	@Setting
 	private double defaultPitch = 0.0;
 	@Setting
 	private double defaultYaw = 0.0;
 	@Setting
 	private double defaultX = 0.0;
 	@Setting
 	private double defaultY = 0.0;
 	@Setting
 	private double defaultZ = 0.0;
 
 	private Map<String, WarpData> warps; // playerName -> warpData
 
 	@Override
 	protected void onEnable() throws Exception {
 	}
 
 	@Override
 	protected void onDisable() {
 	}
 
 	@Override
 	protected void reloadManualSettings() throws Exception {
 		// clear the cache and force it to be reloaded from disk
 		warps = new HashMap<String, WarpData>();
 	}
 
 	@CowCommand
 	protected boolean doCowwarp(Player player, String warpName) {
 		Location loc = getWarp(player.getName(), warpName);
 		if (loc != null) {
 			player.teleport(loc);
 		} else {
 			player.sendMessage("Warp not found.");
 		}
 		return true;
 	}
 
 	@CowCommand
 	protected boolean doSet(Player player, String warpName) {
 		try {
 			WarpData warps = getWarpsFor(player.getName());
 			warps.setWarp(warpName, player.getLocation());
			player.sendMessage("Warp set.");
 		} catch (IOException e) {
 			player.sendMessage("There was a problem and the warp could not be set.");
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			player.sendMessage(e.getMessage());
 		}
 		return true;
 	}
 
 	@CowCommand
	protected boolean doDelete(Player player, String warpName) {
		return doRemove(player, warpName);
	}

	@CowCommand
 	protected boolean doRemove(Player player, String warpName) {
 		WarpData warps = getWarpsFor(player.getName());
 		try {
			if (warps.warpExists(warpName)) {
				warps.removeWarp(warpName);
				player.sendMessage("Warp removed.");
			} else {
				player.sendMessage("Warp not found, public, or not owned by you.");
			}
 		} catch (IOException e) {
 			player.sendMessage("There was a problem and the warp could not be removed.");
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			player.sendMessage(e.getMessage());
 		}
 		return true;
 	}
 
 	@CowCommand
 	protected boolean doList(Player player) {
 		player.sendMessage("Public Warps:");
 		doList(player, publicWarpKey);
 		player.sendMessage("");
 		player.sendMessage("Private Warps:");
 		doList(player, player.getName());
 		return true;
 	}
 
 	@CowCommand(opOnly = true)
 	protected boolean doList(CommandSender sender, String playerName) {
 		WarpData data = getWarpsFor(playerName);
 		sender.sendMessage(StringUtils.flatten(data.getWarpNames(), ","));
 		return true;
 	}
 
 	@CowCommand
 	protected boolean doInfo(Player player, String warpName) {
 		return doInfo(player, player.getName(), warpName);
 	}
 
 	@CowCommand(opOnly = true)
 	protected boolean doInfo(CommandSender sender, String playerName, String warpName) {
 		Location loc = getWarp(playerName, warpName);
 		if (loc != null) {
 			sender.sendMessage(loc.getWorld().getName() + " - " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
 		} else {
 			sender.sendMessage("Warp not found.");
 		}
 		return true;
 	}
 
 	@CowCommand
 	protected boolean doShare(Player player, String warpName) {
 		try {
 			WarpData warps = getWarpsFor(player.getName());
 			Location loc = warps.getWarpLocation(warpName);
 			if (loc == null) {
 				player.sendMessage("You don't have a warp with that name");
 			} else {
 				WarpData publicWarps = getWarpsFor(publicWarpKey);
 				warps.removeWarp(warpName);
 				publicWarps.setWarp(player.getName(), warpName, loc);
				player.sendMessage("Warp shared.");
 			}
 		} catch (IOException e) {
 			player.sendMessage("There was a problem and the warp could not be set.");
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			player.sendMessage(e.getMessage());
 		}
 		return true;
 	}
 
 	@CowCommand
 	protected boolean doUnshare(Player player, String warpName) {
 		try {
 			WarpData publicWarps = getWarpsFor(publicWarpKey);
 			Location loc = publicWarps.getWarpLocation(warpName);
 			if (loc == null) {
 				player.sendMessage("There is no shared warp with that name.");
 			} else {
 				WarpData privateWarps = getWarpsFor(player.getName());
 				publicWarps.removeWarp(player.getName(), warpName);
 				privateWarps.setWarp(warpName, loc);
				player.sendMessage("Warp unshared.");
 			}
 		} catch (IOException e) {
 			player.sendMessage("There was a problem and the warp could not be set.");
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			player.sendMessage(e.getMessage());
 		}
 		return true;
 	}
 
 	// get a warp for the specified play or return
 	// a public warp if the player doesn't have one
 	private Location getWarp(String playerName, String warpName) {
 		WarpData warps = getWarpsFor(playerName);
		Location result = warps.getWarpLocation(warpName);
		if (result == null) {
 			warps = getWarpsFor(publicWarpKey);
			result = warps.getWarpLocation(warpName);
 		}
		return result;
 	}
 
 	// return a map of warp points for a particular player
 	private WarpData getWarpsFor(String playerName) {
 		playerName = playerName.toLowerCase();
 		WarpData result = warps.get(playerName);
 		if (result == null) {
 			try {
 				result = new WarpData(getPlugin(), playerName);
 				warps.put(playerName, result);
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (InvalidConfigurationException e) {
 				e.printStackTrace();
 			}
 		}
 		return result;
 	}
 
 	private class WarpData extends CowNetConfig {
 		// The structure of a warp point is as follows:
 		// warps.warpname.invitees: List<String> (optional)
 		// warps.warpname.world: string
 		// warps.warpname.pitch: double
 		// warps.warpname.yaw: double
 		// warps.warpname.x: double
 		// warps.warpname.y: double
 		// warps.warpname.z: double
 		private String playerName;
 
 		public WarpData(JavaPlugin plugin, String playerName) throws IOException, InvalidConfigurationException {
 			super(plugin, "Warps-" + playerName.toLowerCase() + ".yml");
 			this.playerName = playerName.toLowerCase();
 			loadConfig();
 		}
 
 		public Location getWarpLocation(String warpName) {
 			warpName = warpName.toLowerCase();
 			if (warpExists(warpName)) {
 				String world = getConfigValue("warps." + warpName + ".world", defaultWorld);
 				double pitch = getConfigValue("warps." + warpName + ".pitch", defaultPitch);
 				double yaw = getConfigValue("warps." + warpName + ".yaw", defaultYaw);
 				double x = getConfigValue("warps." + warpName + ".x", defaultX);
 				double y = getConfigValue("warps." + warpName + ".y", defaultY);
 				double z = getConfigValue("warps." + warpName + ".z", defaultZ);
 				return new Location(getPlugin().getServer().getWorld(world), x, y, z, (float) yaw, (float) pitch);
 			} else {
 				return null;
 			}
 		}
 
 		public void setWarp(String warpName, Location loc) throws IOException, IllegalAccessException {
 			setWarp(playerName, warpName, loc);
 		}
 
 		/* this version of the method is used to set warps in the public space
 		   while preserving the previous owner.
 		 */
 		public void setWarp(String owner, String warpName, Location loc) throws IOException, IllegalAccessException {
 			warpName = warpName.toLowerCase();
 			owner = owner.toLowerCase();
 			if (warpExists(warpName)) {
 				//we're replacing an existing warp, so check the owner first
 				String oldOwner = getConfigValue("warps." + warpName + ".owner", playerName);
 				if (!oldOwner.equalsIgnoreCase(owner)) {
 					throw new IllegalAccessException("You can't delete a warp owned by " + oldOwner);
 				}
 			}
 			updateConfigValue("warps." + warpName + ".owner", owner);
 			updateConfigValue("warps." + warpName + ".world", loc.getWorld().getName());
 			updateConfigValue("warps." + warpName + ".pitch", loc.getPitch());
 			updateConfigValue("warps." + warpName + ".yaw", loc.getYaw());
 			updateConfigValue("warps." + warpName + ".x", loc.getX());
 			updateConfigValue("warps." + warpName + ".y", loc.getY());
 			updateConfigValue("warps." + warpName + ".z", loc.getZ());
 			saveConfig();
 		}
 
 		public void removeWarp(String warpName) throws IOException, IllegalAccessException {
 			removeWarp(playerName, warpName);
 		}
 
 		public void removeWarp(String remover, String warpName) throws IOException, IllegalAccessException {
 			warpName = warpName.toLowerCase();
 			if (warpExists(warpName)) {
 				String owner = getConfigValue("warps." + warpName + ".owner", playerName);
 				if (remover.equalsIgnoreCase(owner)) {
 					removeConfigValue("warps." + warpName + ".owner");
 					removeConfigValue("warps." + warpName + ".world");
 					removeConfigValue("warps." + warpName + ".pitch");
 					removeConfigValue("warps." + warpName + ".yaw");
 					removeConfigValue("warps." + warpName + ".x");
 					removeConfigValue("warps." + warpName + ".y");
 					removeConfigValue("warps." + warpName + ".z");
					removeConfigValue("warps." + warpName);
 					saveConfig();
 				} else {
 					throw new IllegalAccessException("You can't delete a warp owned by " + owner);
 				}
 			} else {
 				// hey jf - do we want to do anything if someone tries to remove a warp that doesn't exist?
 			}
 		}
 
 		public boolean warpExists(String warpName) {
 			return hasConfigValue("warps." + warpName.toLowerCase() + ".world");
 		}
 
 		public Set<String> getWarpNames() {
 			if (getNode("warps") != null) {
 				Set<String> temp = getNode("warps").getKeys(false);
 				// this bit is to filter out warps that are invalid in old OpenWarp configs
 				Set<String> result = new HashSet<String>();
 				for (String name : temp) {
 					if (warpExists(name)) {
 						result.add(name);
 					}
 				}
 				return result;
 			} else {
 				return Collections.emptySet();
 			}
 		}
 	}
 }

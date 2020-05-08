 package com.em.fan;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.yaml.snakeyaml.Yaml;
 
 public class Fan extends JavaPlugin {
 
 	FanListener thelistener = new FanListener(this);
 
 	// Properties
 	Map<Location, FanBlock> fanMap = new HashMap<Location, FanBlock>();
 
 	// Constant
 	Material BLOCK_TYPE = Material.PUMPKIN;
 
 	public void onDisable() {
 		// reload the configuration
 		reloadConfig();
 
 		// Construct the data
 		Set<String> fanSetS = new HashSet<String>();
 		for (Location l : this.fanMap.keySet()) {
 			fanSetS.add(convertLocation(l));
 		}
 		String fans = new Yaml().dump(fanSetS);
 
 		// if data change, update and save
 		if (!fans.equals(getConfig().getString("data", "{}"))) {
 			getConfig().set("data", fans);
 
 			saveConfig();
 		}
 	}
 
 	public void onEnable() {
 
 		getServer().getPluginManager().registerEvents(this.thelistener, this);
 
 		@SuppressWarnings("unchecked")
 		Set<String> fanSetS = (HashSet<String>) new Yaml().loadAs(getConfig().getString("data", "{}"), HashSet.class);
 		for (String s : fanSetS) {
 			Location l = convertString(s);
 
 			if (l.getBlock().getType().equals(BLOCK_TYPE)) {
 				this.fanMap.put(l, new FanBlock(l.getBlock(), this));
 			}
 		}
 
 		getLogger().info(fanMap.size() + " fans");
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (!sender.hasPermission("fan.create")) {
 			return false;
 		}
 		try {
 			Player player = (Player) sender;
 			Block block = getPlayerTargetBlock(player);
 
 			if (!block.getType().equals(BLOCK_TYPE)) {
 				sender.sendMessage(ChatColor.RED + "This " + block.getType().toString().toLowerCase() + "is not a " + BLOCK_TYPE.toString().toLowerCase());
 				return true;
 			}
 			if (this.fanMap.containsKey(block.getLocation())) {
 				sender.sendMessage(ChatColor.RED + "That " + block.getType().toString().toLowerCase() + " is already a fan ");
 				return true;
 			}
 			
 			this.fanMap.put(block.getLocation(), new FanBlock(block, this));
 			sender.sendMessage(ChatColor.GREEN + "Fan created");
 		} catch (ClassCastException e) {
 			sender.sendMessage("You can only use this command as a player!");
 		}
 		return true;
 	}
 
 	String convertLocation(Location l) {
 		String out = "";
 		out = out + l.getWorld().getName() + ",";
 		out = out + l.getBlockX() + ",";
 		out = out + l.getBlockY() + ",";
 		out = out + l.getBlockZ();
 		return out;
 	}
 
 	Location convertString(String s) {
 		String[] parts = s.split(",");
 		return new Location(getServer().getWorld(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
 	}
 
 	/**
 	 * Get the targeted Block
 	 * 
 	 * @param player
 	 * @return
 	 */
 	private Block getPlayerTargetBlock(Player player) {
 		Block block = player.getTargetBlock(TRANSPARENT, 5);
 		return block;
 	}
 
 	// define transparent blocks id
 	private static final HashSet<Byte> TRANSPARENT = new HashSet<Byte>();
 	static {
 		TRANSPARENT.add((byte) Material.AIR.getId());
 		TRANSPARENT.add((byte) Material.FENCE.getId());
 		TRANSPARENT.add((byte) Material.FENCE_GATE.getId());
 		TRANSPARENT.add((byte) Material.DETECTOR_RAIL.getId());
 		TRANSPARENT.add((byte) Material.POWERED_RAIL.getId());
 		TRANSPARENT.add((byte) Material.RAILS.getId());
 		TRANSPARENT.add((byte) Material.REDSTONE_WIRE.getId());
 		TRANSPARENT.add((byte) Material.TORCH.getId());
 	}
 
 }

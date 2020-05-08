 package com.fernferret.lightlevel;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class LightLevel extends JavaPlugin {
 	
 	public static final Logger log = Logger.getLogger("Minecraft");
 	public static final String logPrefix = "[LightLevel]";
 	
 	private PermissionHandler permissions;
 	private boolean usePermissions;
 	private String chatPrefixError = ChatColor.RED.toString();
 	
 	@Override
 	public void onDisable() {
 		log.info(logPrefix + " - Disabled");
 	}
 	
 	@Override
 	public void onEnable() {
 		checkPermissions();
 		log.info(logPrefix + " - Version " + this.getDescription().getVersion() + " Enabled");
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (command.getName().equalsIgnoreCase("lightlevel")) {
 			if (sender instanceof Player && hasPermission((Player)sender, "lightlevel.use")) {
 				Player p = (Player) sender;
 				ArrayList<Block> target = (ArrayList<Block>) p.getLastTwoTargetBlocks(null, 50);
 				// If the block isn't air, continue, otherwise show error
 				if (!target.get(1).getType().equals(Material.matchMaterial("AIR")) && target.size() >= 2) {
 					String numbercolor = getColorFromLightLevel(target.get(0).getLightLevel()).toString();
 					p.sendMessage(target.get(1).getType().name().toUpperCase() + ": " + numbercolor + target.get(0).getLightLevel());
 				} else {
 					p.sendMessage(ChatColor.RED + "Get closer!");
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Grab the Permissions plugin from the Plugin Manager.
 	 */
 	private void checkPermissions() {
 		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
 		if (test != null) {
 			log.info(logPrefix + " using Permissions " + test.getDescription().getVersion());
 			permissions = ((Permissions) test).getHandler();
 			usePermissions = true;
 		}
 	}
 	/**
 	 * Check to see if Player p has the permission given
 	 * @param p The Player to check
 	 * @param permission The permission to check
 	 * @return True if the player has permission, false if not
 	 */
 	public boolean hasPermission(Player p, String permission) {
 		if (!usePermissions || p.isOp()) {
 			return true;
 		}
 		if (!permissions.has(p, permission)) {
 			p.sendMessage(chatPrefixError  + "You don't have permission (" + permission + ") to do this!");
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Returns a chatcolor based on what the light level is. Allows visual change between good and bad levels
 	 * @param lightLevel
	 * @return
 	 */
 	private ChatColor getColorFromLightLevel(byte lightLevel) {
 		// The level at which hostile mobs can spawn
 		if (lightLevel <= 7) {
 			return ChatColor.DARK_RED;
 		}
 		// The level at which NO mobs can spawn
 		if (lightLevel < 9) {
 			return ChatColor.GOLD;
 		}
 		// Anything else, friendly mobs can spawn
 		return ChatColor.GREEN;
 	}
 	
 }

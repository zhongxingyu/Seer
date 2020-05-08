 package com.fernferret.lightlevel;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class LightLevel extends JavaPlugin {
 	
 	public static final Logger log = Logger.getLogger("Minecraft");
 	public static final String logPrefix = "[LightLevel]";
 	private static final String LIGHT_LEVEL_CONFIG = "LightLevel.yml";
 	public static final String WAND_KEY = "wand.item";
 	public static final Integer TORCH_ITEM = 50;
 	public static final String WAND_ENABLE_KEY = "wand.enable";
 	public static final String WAND_ENABLE_DEFAULT_KEY = "wand.default";
 	
 	private PermissionHandler permissions;
 	private boolean usePermissions;
 	private String chatPrefixError = ChatColor.RED.toString();
 	protected Configuration configLL;
 	private LLBlockListener blockListener;
 	
 	private ArrayList<Player> wandEnabled;
 	
 	@Override
 	public void onDisable() {
 		// TODO: Save users to file
 		log.info(logPrefix + " - Disabled");
 	}
 	
 	@Override
 	public void onEnable() {
 		// TODO: Load users from file
 		loadConfiguration();
 		checkPermissions();
 		wandEnabled = new ArrayList<Player>();
 		PluginManager pm = getServer().getPluginManager();
 		blockListener = new LLBlockListener(this);
 		pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Priority.Normal, this);
 		log.info(logPrefix + " - Version " + this.getDescription().getVersion() + " Enabled");
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (command.getName().equalsIgnoreCase("lightlevel") || command.getName().equalsIgnoreCase("ll")) {
 			if(sender instanceof Player) {
 				getLightLevel((Player)sender);
 			}
 			return true;
 		}
 		if (command.getName().equalsIgnoreCase("llwand")) {
 			if(sender instanceof Player) {
 				togglePlayerWand((Player)sender);
 			}
 			return true;
 		}
 		return false;
 	}
 	/**
 	 * Toggle whether or not a player wants to use the wand
 	 * @param p
 	 */
 	protected void togglePlayerWand(Player p) {
 		if(wandEnabled.contains(p)) {
 			wandEnabled.remove(p);
 		} else {
 			wandEnabled.add(p);
 		}
		if(playerHasWandEnabled(p)) {
			p.sendMessage(ChatColor.GREEN + "Wand Enabled!");
		} else {
			p.sendMessage(ChatColor.RED + "Wand DISABLED!");
		}
 	}
 	protected boolean playerHasWandEnabled(Player p) {
		if(this.configLL.getBoolean(WAND_ENABLE_DEFAULT_KEY, true)) {
			return !wandEnabled.contains(p);
		}
 		return wandEnabled.contains(p);
		
 	}
 
 	protected void getLightLevel(Player p) {
 		if (hasPermission((Player) p, "lightlevel.use")) {
 			ArrayList<Block> target = (ArrayList<Block>) p.getLastTwoTargetBlocks(null, 50);
 			// If the block isn't air, continue, otherwise show error
			if (target.size() >= 2 &&!target.get(1).getType().equals(Material.matchMaterial("AIR"))) {
 				String numbercolor = getColorFromLightLevel(target.get(0).getLightLevel()).toString();
 				p.sendMessage(target.get(1).getType().name().toUpperCase() + ": " + numbercolor + target.get(0).getLightLevel());
 			} else {
 				p.sendMessage(ChatColor.RED + "Get closer!");
 			}
 		}
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
 	 * 
 	 * @param p The Player to check
 	 * @param permission The permission to check
 	 * @return True if the player has permission, false if not
 	 */
 	public boolean hasPermission(Player p, String permission) {
 		if (!usePermissions || p.isOp()) {
 			return true;
 		}
 		if (!permissions.has(p, permission)) {
 			p.sendMessage(chatPrefixError + "You don't have permission (" + permission + ") to do this!");
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Returns a chatcolor based on what the light level is. Allows visual change between good and bad levels
 	 * 
 	 * @param lightLevel
 	 * @return The chatcolor that the number should be
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
 	
 	private void loadConfiguration() {
 		getDataFolder().mkdirs();
 		configLL = new Configuration(new File(this.getDataFolder(), LIGHT_LEVEL_CONFIG));
 		configLL.load();
		if ((configLL.getProperty(WAND_ENABLE_KEY) == null) || !(configLL.getProperty(WAND_ENABLE_KEY) instanceof Boolean)) {
 			configLL.setProperty(WAND_ENABLE_KEY, true);
 			configLL.save();
 		}
 		if ((configLL.getProperty(WAND_KEY) == null) || !(configLL.getProperty(WAND_KEY) instanceof Integer)) {
 			configLL.setProperty(WAND_KEY, TORCH_ITEM);
 			configLL.save();
 		}
 		if ((configLL.getProperty(WAND_ENABLE_DEFAULT_KEY) == null) || !(configLL.getProperty(WAND_ENABLE_DEFAULT_KEY) instanceof Boolean)) {
 			configLL.setProperty(WAND_ENABLE_DEFAULT_KEY, true);
 			configLL.save();
 		}
 	}
 	
 }

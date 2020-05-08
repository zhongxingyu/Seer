 package me.puzzler995.MorningAnnounce;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.util.config.Configuration;
 
 public class MorningAnnounce extends JavaPlugin {
 	Logger log = Logger.getLogger("Minecraft");
 	public Configuration config;
 	public static boolean usePermissions;
 	public static boolean usePermissionsBukkit;
 	public static boolean useOP;
 	public static String configDayString;
 	public static String configNightString;
 	public static Map<Player, Boolean> pluginEnabled = new HashMap<Player, Boolean>();
 	public static PermissionHandler permissionHandler;
 	public void onEnable(){
 		if (!new File(getDataFolder(), "config.yml").exists()) {
 			try {
 				new File(getDataFolder(), "config.yml").createNewFile();
 			}
 			catch(Exception e) {
 			}
 		}
 		config = this.getConfiguration();
 		if (config.getKeys().isEmpty()) {
 			log.info(prefix()+" config.yml not found, making with default values");
 			config.setProperty("Day Announcement", "It is now Daytime!");
 			config.setProperty("Night Announcement", "It is now nighttime! Hide!");
 			config.setHeader("#Choose a Permissions System.");
 			config.setProperty("Permissions", true);
 			config.setProperty("PermissionsBukkit", false);
 			config.setProperty("OP", false);
 			config.save();
 		}
 		usePermissions = config.getBoolean("Permissions", false);
 		usePermissionsBukkit = config.getBoolean("PermissionsBukkit", false);
 		useOP = config.getBoolean("OP", false);
 		configDayString = config.getString("Day Announcement");
 		configNightString = config.getString("Night Announcement");
 		if(usePermissions==true) {
 			setupPermissions();
 		}
 		log.info( prefix() + " has been enabled!");
 	}
 	
 	public void onDisable(){
 		log.info(prefix()+" has been disabled.");
 	}
 	
 	public String prefix(){
 		PluginDescriptionFile desc = this.getDescription();
 		String prefix = "[" + desc.getName() + " v" + desc.getVersion() + "]";
 		return prefix;
 	}
 
 	
 	private void setupPermissions() {
 		if (permissionHandler != null) {
 			return;
 		}
 		
 		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 		
 		if (permissionsPlugin == null) {
 			log.warning(prefix()+" Permissions plugin not detected! Please change the config.yml!");
 
 			return;
 		}
 		
 		permissionHandler = ((Permissions) permissionsPlugin).getHandler();
 		log.info(prefix()+" Found and will use plugin "+((Permissions)permissionsPlugin).getDescription().getFullName());
 	}
 	
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		String cmd = command.getName();
 		Player player = (Player)sender;
 		if (usePermissions==true) {
 			Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 			permissionHandler = ((Permissions) permissionsPlugin).getHandler();
 			if (!permissionHandler.has(player, "MorningAnnounce.use")) {
 				player.sendMessage(prefix() + " You don't have permissions to do that!");
 				return false;
 			}
 		}
		
		if(usePermissionsBukkit && !player.isOp()){
			if(!player.hasPermission("MorningAnnounce.use")) {
 				player.sendMessage(prefix() + " You don't have the permissions to do that!");
 				return true;
 			}
 		}
		if(!usePermissionsBukkit && !player.isOp()) {
			player.sendMessage(prefix() + " You don't have the permissions to do that!");
			return true;
		}
 		if (cmd.equalsIgnoreCase("MorningAnnounce")){
 			togglePluginState(player);
 			return announceOn(player);
 
 		} else if (cmd.equalsIgnoreCase("ma")){
 			togglePluginState(player);
 			return announceOn(player);
 		}
 		return false;
 		
 	}
 	public boolean announceOn(Player player){
 		long curtime = player.getWorld().getTime();
 		long intdaydel = (24000-curtime);
 		long intnightdel = (14000-curtime);
 		long intdel = 0;
 		if (intdaydel < intnightdel) {
 			intdel = intdaydel;
 		}
 		if (intdaydel >= intnightdel) {
 			intdel = intnightdel;
 		}
 		final Player playert = player;
 		try{
 			Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
 				@Override
 				public void run(){
 					long curtime = playert.getWorld().getTime();
					if (curtime == 0) {
 						playert.sendMessage(configDayString);
 					}
					if (curtime == 14000) {
 						playert.sendMessage(configNightString);
 					}
 				}
 			}, intdel, 1000);
 			return true;
 		} catch(Exception e) {
 			log.info(prefix()+ e.getStackTrace());
 			return false;
 		}
 	}
 	public void togglePluginState(Player player){
 		if(pluginEnabled.containsKey(player)){
 			if(pluginEnabled.get(player)){
 				pluginEnabled.put(player, false);
 				player.sendMessage("MorningAnnounce disabled");
 			} else {
 				pluginEnabled.put(player, true);
 				player.sendMessage("MorningAnnounce enabled");
 			}
 		} else {
 			pluginEnabled.put(player, true);
 			player.sendMessage("MorningAnnounce enabled");
 		}
 	}
 
 }

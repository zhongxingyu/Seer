 package ru.cronfire.cfBlazeProtect;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.CreatureSpawner;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class cfBlazeProtect extends JavaPlugin implements Listener {
   public static Logger log = Logger.getLogger("Minecraft");
   public static PluginDescriptionFile pdfFile;
 
 public void onEnable() {
 	try {
 		if (!getDataFolder().exists()) {
 		    log.info("[cfBlazeProtect] DataFolder not found, creating a new one.");
 		    getDataFolder().mkdir();
 		    }
 		    } catch (Exception ex) {
 				ex.printStackTrace();
 		    }
 
 	loadConfig();
 	reloadConfig();
 
 	PluginManager pm = getServer().getPluginManager();
	pm.registerEvents(new this, this);
 
 	pdfFile = getDescription();
 	PluginDescriptionFile pdfFile = getDescription();
 
 	log.info("[" + pdfFile.getName() + "]" + " version " + pdfFile.getVersion() + " is enabled!");
 }
 
 public void onDisable() {
 	log.info("[" + pdfFile.getName() + "]" + " version " + pdfFile.getVersion() + " is disabled!");
 }
 		  
 public void loadConfig() { 
 	getConfig().addDefault("Message", "You are not allowed to do it!");
 
 	getConfig().options().copyDefaults(true);
 	saveConfig();
 	log.info("[cfBlazeProtect] Successfully loaded configuration file.");
 }
 
 @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 public void onBlockBreak(BlockBreakEvent event) {
 	String worldname = event.getPlayer().getWorld().getName();
 	Player player = event.getPlayer();
 	
 	if(worldname.equalsIgnoreCase("world_nether") || worldname.contains("nether")) {  //for bukkit users
 		if (event.getBlock().getType() == Material.MOB_SPAWNER) {
         	CreatureSpawner spawner = (CreatureSpawner) event.getBlock().getState();
         	
         	if ((spawner.getSpawnedType() == EntityType.BLAZE) && (!player.hasPermission("blazeprotect.exempt"))) {
             	player.sendMessage(ChatColor.RED + getConfig().getString("Message"));
             	event.setCancelled(true);
         	}
     	}
 	}
     
 }
 		
 }

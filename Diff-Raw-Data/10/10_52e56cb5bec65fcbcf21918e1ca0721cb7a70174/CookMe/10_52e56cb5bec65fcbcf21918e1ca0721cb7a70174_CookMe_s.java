 package de.xghostkillerx.cookme;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.logging.Logger;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.configuration.file.*;

 //Stats
 import com.randomappdev.pluginstats.Ping;
 

 /**
  * CookeMe for CraftBukkit/Bukkit
  * Handles some general stuff!
  * 
  * Refer to the forum thread:
  * http://bit.ly/cookmebukkit
  * Refer to the dev.bukkit.org page:
  * http://bit.ly/cookmebukkitdev
  *
  * @author xGhOsTkiLLeRx
  * @thanks nisovin
  * 
  */
 
 public class CookMe extends JavaPlugin {
 	
 	public static final Logger log = Logger.getLogger("Minecraft");
 	private final CookMePlayerListener playerListener = new CookMePlayerListener(this);
 	public FileConfiguration config;
 	public File configFile;
 	
 	// Shutdown
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info(pdfFile.getName() + " " + pdfFile.getVersion()	+ " has been disabled!");
 	}
 
 	// Start
 	public void onEnable() {
 		// Events
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
 		
 		// Config
 		configFile = new File(getDataFolder(), "config.yml");
 		if(!configFile.exists()){
 	        configFile.getParentFile().mkdirs();
 	        copy(getResource("config.yml"), configFile);
 	    }
 		config = this.getConfig();
 		loadConfig();
 		
 		// Message
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info(pdfFile.getName() + " " + pdfFile.getVersion() + " is enabled!");
 		
 		// Stats
 		Ping.init(this);
 	}
 	
 	// Loads the config at start
 	public void loadConfig() {
 		config.options().header("For help please refer to http://bit.ly/cookmebukkitdev or http://bit.ly/cookmebukkit");
 		config.addDefault("configuration.permissions", true);
 		config.addDefault("configuration.messages", true);
 		config.addDefault("effects.damage", true);
 		config.addDefault("effects.death", true);
 		config.addDefault("effects.venom", true);
 		config.addDefault("effects.hungervenom", true);
 		config.addDefault("effects.hungerdecrease", true);
 		config.addDefault("effects.confusion", true);
 		config.addDefault("effects.blindness", true);
 		config.addDefault("effects.weakness", true);
 		config.addDefault("effects.venom", true);
 		config.addDefault("effects.slowness", true);
 		config.addDefault("effects.slowness_blocks", true);
 		config.options().copyDefaults(true);
 		saveConfig();
 	}
 
 	// Reloads the config via command /cookme reload
 	public void loadConfigAgain() {
 		try {
 			config.load(configFile);
 			saveConfig();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	// If no config is found, copy the default one!
 	private void copy(InputStream in, File file) {
 		try {
 			OutputStream out = new FileOutputStream(file);
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len=in.read(buf)) >0) {
 				out.write(buf,0,len);
 			}
 			out.close();
 			in.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	// Refer to CookMeCommands
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		CookMeCommands cmd = new CookMeCommands(this);
 		return cmd.CookMeCommand(sender, command, commandLabel, args);
 	}
 }

 /*
  *  Copyright:
  *  2013 Darius Mewes
  */
 
 package de.dariusmewes.TimoliaCustom;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.text.SimpleDateFormat;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 
 import de.dariusmewes.TimoliaCustom.commands.CommandHandler;
import de.dariusmewes.TimoliaCustom.commands.addlink;
 import de.dariusmewes.TimoliaCustom.commands.protnpc;
 import de.dariusmewes.TimoliaCustom.commands.sapopvp;
 import de.dariusmewes.TimoliaCustom.commands.west;
 import de.dariusmewes.TimoliaCustom.events.EntityListener;
 import de.dariusmewes.TimoliaCustom.events.InventoryListener;
 import de.dariusmewes.TimoliaCustom.events.PlayerListener;
 import de.dariusmewes.TimoliaCustom.events.ProjectileListener;
 
 public class TimoliaCustom extends JavaPlugin {
 
 	public static final String PREFIX = ChatColor.GREEN + "[TCustom] " + ChatColor.WHITE;
 	public static File dataFolder;
 	public static boolean coding = false;
 
 	public void onEnable() {
 		CommandHandler.init(this);
 		initEventHandlers();
 		initConfig();
 		dataFolder = getDataFolder();
 
 		sapopvp.updateArena();
 		protnpc.load();
 		west.loadWatchedPlayers();
 		west.repeatScanning();
 		// potion damage 64
 	}
 
 	public void onDisable() {
 		west.saveWatchedPlayers();
 		protnpc.save();
 	}
 
 	private void initEventHandlers() {
 		Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
 		Bukkit.getPluginManager().registerEvents(new EntityListener(), this);
 		Bukkit.getPluginManager().registerEvents(new InventoryListener(), this);
 		Bukkit.getPluginManager().registerEvents(new ProjectileListener(), this);
 	}
 
 	private void initConfig() {
 		FileConfiguration conf = getConfig();
 		conf.addDefault("sapopvp.pos1", "");
 		conf.addDefault("sapopvp.pos2", "");
 		conf.addDefault("sapopvp.warpgold", "");
 		conf.addDefault("sapopvp.warpeisen", "");
 		conf.addDefault("westwatchwelt", "timolia");
 		conf.addDefault("sgamesdeathmsg", true);
 		conf.addDefault("linkShortening", true);
 		conf.addDefault("linkURL", "http://www.timolia.de/s/");
 		conf.options().copyDefaults(true);
 		saveConfig();
		
		loadConfig();
	}
	
	public void loadConfig(){
		addlink.coreURL = getConfig().getString("linkURL");
		addlink.shorterCore = addlink.coreURL.substring(7);
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
 		CommandHandler.handleCommand(sender, cmd, args);
 		return true;
 	}
 
 	public static WorldGuardPlugin getWorldGuard() {
 		Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
 
 		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
 			return null;
 		}
 
 		return (WorldGuardPlugin) plugin;
 	}
 
 	public static void logError(String err) {
 		try {
 			File file = new File(TimoliaCustom.dataFolder + File.separator + "errors.txt");
 			BufferedWriter output = new BufferedWriter(new FileWriter(file, true));
 			String date = new SimpleDateFormat("dd. MMM. yyyy, HH:mm").format(System.currentTimeMillis());
 			output.append(date + "\t" + err + "\n");
 			output.close();
 			Message.console("Error logged!");
 		} catch (Exception e) {
 
 		}
 	}
 
 }

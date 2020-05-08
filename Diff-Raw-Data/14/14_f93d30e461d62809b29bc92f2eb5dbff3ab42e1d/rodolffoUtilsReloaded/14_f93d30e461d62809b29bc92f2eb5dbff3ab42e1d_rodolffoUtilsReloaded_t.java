 package com.zolli.rodolffoutilsreloaded;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.Location;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.zolli.rodolffoutilsreloaded.listeners.commandExecutor;
 import com.zolli.rodolffoutilsreloaded.listeners.playerListener;
 
 public class rodolffoUtilsReloaded extends JavaPlugin {
 	
 	private PluginManager pm;
 	public Economy econ = null;
 	public Permission perm = null;
 	
 	private File configFile;
 	private File messagesFile;
 	private File buttonFile;
 	
 	public FileConfiguration config;
 	public FileConfiguration messages;
 	public FileConfiguration button;
 	
 	public Logger log;
 	private PluginDescriptionFile pdfile;
 	private CommandExecutor commandExec;
 	
	public String SelectorPlayer = null;
 	public String selectType;
 	public String selectName;
 	public Location selectLoc;
 	
 	private boolean setupPermissions() {
         RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
         perm = rsp.getProvider();
         return perm != null;
     }
 	
 	private boolean setupEconomy() {
         RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
         econ = rsp.getProvider();
         return econ != null;
     }
 	
 	private void copy(InputStream in, File file) {
         try {
             OutputStream out = new FileOutputStream(file);
             byte[] buf = new byte[1024];
             int len;
             while((len=in.read(buf))>0){
                 out.write(buf,0,len);
             }
             out.close();
             in.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 	
 	private void firstRun() {
 		if(!configFile.exists()) {
 			configFile.getParentFile().mkdirs();
 			copy(getResource("config.yml"), configFile);
 		}
 		if(!messagesFile.exists()) {
 			messagesFile.getParentFile().mkdirs();
 			copy(getResource("messages.yml"), messagesFile);
 		}
 		if(!buttonFile.exists()) {
 			buttonFile.getParentFile().mkdirs();
 			copy(getResource("buttons.yml"), buttonFile);
 		}
 	}
 	
 	public void loadConfiguration() {
 		try {
 			config.load(configFile);
 			messages.load(messagesFile);
 			button.load(buttonFile);
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void saveConfiguration() {
 		try {
 			config.save(configFile);
 			messages.save(messagesFile);
 			button.save(buttonFile);
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void onLoad() {
 		
 		config = new YamlConfiguration();
 		messages = new YamlConfiguration();
 		button = new YamlConfiguration();
 		configFile = new File(getDataFolder(), "config.yml");
 		messagesFile = new File(getDataFolder(), "messages.yml");
 		buttonFile = new File(getDataFolder(), "buttons.yml");
 		
 		try {
             this.firstRun();
         } catch (Exception e) {
             e.printStackTrace();
         }
 		
 		this.loadConfiguration();
 		
 		log = Logger.getLogger("Minecraft");
 		pdfile = getDescription();
 		pm = getServer().getPluginManager();
 		commandExec = new commandExecutor(this);
 		
 		
 	}
 	
 	public void onEnable() {
 		
 		final playerListener playerListener = new playerListener(this);
 		pm.registerEvents(playerListener, this);
 		
 		getCommand("achat").setExecutor(commandExec);
 		getCommand("fakechat").setExecutor(commandExec);
 		getCommand("definebutton").setExecutor(commandExec);
 		
 		if(pm.isPluginEnabled("Vault")) {
 			setupPermissions();
 			log.info("[" + pdfile.getName() + "] Hooked with " + perm.getName() + "!");
 			setupEconomy();
 			log.info("[" + pdfile.getName() + "] Hooked with " + econ.getName() + "!");
 		} else {
 			log.warning("[" + pdfile.getName() + "] Vault not found! Disabling plugin...");
 			pm.disablePlugin(this);
 		}
 		
 		log.info("[" + pdfile.getName() + "] Version: " + pdfile.getVersion() + " Sucessfully enabled!");
 		
 	}
 	
 	public void onDisable() {
 		
 		this.saveConfiguration();
 		log.info("[" + pdfile.getName() + "] Version: " + pdfile.getVersion() + " Sucessfully disabled!");
 		
 	}
 	
 }

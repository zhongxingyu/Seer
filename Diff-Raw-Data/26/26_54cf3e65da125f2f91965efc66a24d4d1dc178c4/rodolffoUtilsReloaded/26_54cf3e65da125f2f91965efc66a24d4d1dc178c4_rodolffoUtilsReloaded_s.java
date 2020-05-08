 package com.zolli.rodolffoutilsreloaded;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.miykeal.showCaseStandalone.ShowCaseStandalone;
 import com.zolli.rodolffoutilsreloaded.listeners.blockListener;
 import com.zolli.rodolffoutilsreloaded.listeners.commandExecutor;
 import com.zolli.rodolffoutilsreloaded.listeners.entityListener;
 import com.zolli.rodolffoutilsreloaded.listeners.inventoryListener;
 import com.zolli.rodolffoutilsreloaded.listeners.playerListener;
 import com.zolli.rodolffoutilsreloaded.listeners.tamedMobHelperListener;
 import com.zolli.rodolffoutilsreloaded.utils.recipes;
 
 /**
  * 
  * @author Zolli
  * @version 1.2
  * @since 2012-05-09
  */
 
 public class rodolffoUtilsReloaded extends JavaPlugin {
 	
 	private PluginManager pm;
 	public Economy econ = null;
 	public Permission perm = null;
 	public ShowCaseStandalone scs = null;
 	
 	private File configFile;
 	private File messagesFile;
 	private File buttonFile;
 	
 	public FileConfiguration config;
 	public FileConfiguration messages;
 	public FileConfiguration button;
 	
 	public Logger log;
	private PluginDescriptionFile pdfile;
 	private CommandExecutor commandExec;
 	private recipes recipes;
 	
 	public String SelectorPlayer = null;
 	public String selectValue = null;
 	public String selectType;
 	public String selectName;
 	public Location selectLoc;
 	
 	public Block pistonBugBlock;
 	public int pistonBugId;
 	public byte pistonBugData;
 	
 	private void setupScs() {	
 		for(Plugin p : getServer().getPluginManager().getPlugins()) {
 			
 			String cName = p.getClass().getName();
 			
 			if(cName.equals("com.miykeal.showCaseStandalone.ShowCaseStandalone")) {
 				
 				scs = (ShowCaseStandalone) p;
 				
 			}
 			
 		}
 	}
 	
 	
 	/**
 	 * Get the most valuable permission system
 	 * @return perm null if service not found
 	 */
 	private boolean setupPermissions() {
         RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
         perm = rsp.getProvider();
         return perm != null;
     }
 	
 	/**
 	 * Get the most valuable economy system
 	 * @return econ null if valubale service not found
 	 */
 	private boolean setupEconomy() {
         RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
         econ = rsp.getProvider();
         return econ != null;
     }
 	
 	/**
 	 * Copy a file to another location
 	 * @param in an inputStream object
 	 * @param file file to copy
 	 */
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
 	
 	/**
 	 * If configuration files dont exist this function is copy all the resource
 	 */
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
 	
 	/**
 	 * Load all configuration to object
 	 */
 	public void loadConfiguration() {
 		try {
 			config.load(configFile);
 			messages.load(messagesFile);
 			button.load(buttonFile);
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Saving configuration object to disk. THis only save the buttons resource
 	 */
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
 		recipes = new recipes();
 		
 		recipes.createRecipes(this);
 		
 	}
 	
 	public void onEnable() {
 		
 		final playerListener playerListener = new playerListener(this);
 		final entityListener entityListener = new entityListener(this);
 		final blockListener blockListener = new blockListener(this);
 		final inventoryListener inventoryListener = new inventoryListener(this);
 		final tamedMobHelperListener tamedMobHelperListener = new tamedMobHelperListener(this);
 		
 		pm.registerEvents(playerListener, this);
 		pm.registerEvents(entityListener, this);
 		pm.registerEvents(blockListener, this);
 		pm.registerEvents(inventoryListener, this);
 		pm.registerEvents(tamedMobHelperListener, this);
 		
 		getCommand("achat").setExecutor(commandExec);
 		getCommand("fakechat").setExecutor(commandExec);
 		getCommand("definebutton").setExecutor(commandExec);
 		getCommand("idban").setExecutor(commandExec);
 		getCommand("idunban").setExecutor(commandExec);
 		getCommand("napos").setExecutor(commandExec);
 		getCommand("nappal").setExecutor(commandExec);
 		getCommand("rur").setExecutor(commandExec);
 		
 		if(pm.isPluginEnabled("Vault")) {
 			setupPermissions();
 			log.info("[" + pdfile.getName() + "] Hooked with " + perm.getName() + "!");
 			setupEconomy();
 			log.info("[" + pdfile.getName() + "] Hooked with " + econ.getName() + "!");
 			setupScs();
 		} else {
 			log.warning("[" + pdfile.getName() + "] Vault not found! Disabling plugin...");
 			pm.disablePlugin(this);
 		}
 		
 		if(this.config.getInt("savealldelay")>0)
 		{
 			log.info("[" + pdfile.getName() + "] Auto save enabled for "+this.config.getInt("savealldelay")+" minutes!");
 			getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() 
 	    	{
 	    	    public void run() 
 	    	    {
 	    	    	log.info("[" + pdfile.getName() + "] Auto save start...");
 	    	    	getServer().dispatchCommand(getServer().getConsoleSender(), "save-all");
 	    	    }
 	    	}, 200L, this.config.getLong("savealldelay")*60*20L);
 		}
 		
 		
 		log.info("[" + pdfile.getName() + "] Version: " + pdfile.getVersion() + " Sucessfully enabled!");
 		
 	}
 	
 	public void onDisable() {
 		
 		this.saveConfiguration();
 		log.info("[" + pdfile.getName() + "] Version: " + pdfile.getVersion() + " Sucessfully disabled!");
 		
 	}
 	
 }

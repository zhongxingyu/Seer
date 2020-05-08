 package me.lenis0012.pvp;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin
 {
 	public Economy economy;
 	public Logger log = Logger.getLogger("Minecraft");
 	private FileConfiguration customConfig = null;
     private File customConfigFile = null;
     public List<Integer> levels = new ArrayList<Integer>();
     private DataHandler data = null;
 	
 	@Override
 	public void onEnable()
 	{
 		File file = new File(this.getDataFolder(), "config.yml");
 		boolean fr = !file.exists();
 		
 		//create variables
 		PluginManager pm = this.getServer().getPluginManager();
 		FileConfiguration config =  this.getConfig();
 		
 		//create config if not exists
 		config.addDefault("settings.useFancyConsole", true);
 		config.addDefault("settings.max lvl", 100);
 		config.addDefault("settings.MySQL.use", false);
 		config.addDefault("settings.MySQL.host", "localhost");
 		config.addDefault("settings.MySQL.port", 3306);
 		config.addDefault("settings.MySQL.database", "craftbukkit");
 		config.addDefault("settings.MySQL.user", "root");
 		config.addDefault("settings.MySQL.password", "password");
 		config.addDefault("settings.reward.item.use", true);
 		config.addDefault("settings.reward.item.id", 264);
 		config.addDefault("settings.reward.item.amount", 1);
 		config.addDefault("settings.reward.money.use", false);
 		config.addDefault("settings.reward.money.amount", 120);
 		config.addDefault("settings.reward.command.use", false);
 		config.addDefault("settings.reward.command.command", "say {Player} has reached a new level");
 		if(fr)
 			config.addDefault("settings.reward.lvl.50.command", "pex user {Player} group add PvpGod");
 		config.options().copyDefaults(true);
 		saveConfig();
 		
 		//setup data management
 		data = new DataHandler();
 		data.setup(this);
 		
 		//register events
 		pm.registerEvents(new MainListener(this), this);
 		
 		//enable economy if Vault is enabled
 		Plugin vault = pm.getPlugin("Vault");
 		if(vault != null)
 		{
 			if(setupEconomy())
 			{
 				log("Hooked with "+economy.getName()+" using Vault");
 			}
 		}
 		
 		//generate level list
 		int i = 0;
 		int j = 0;
 		int k = 0;
 		int l = 0;
 		while(i <= this.getConfig().getInt("settings.max lvl"))
 		{
 			if(l >= 2)
 			{
 				l = 0;
 				k = k + 1;
 			}
			j = j + 2 + k;
 			levels.add(j);
 			i++;
 		}
 		
 		for(OfflinePlayer p : Bukkit.getServer().getOfflinePlayers())
 		{
 			String user = p.getName();
 			if(this.getConfig().getBoolean("settings.MySQL.use") == false)
 			{
 				int lvl = this.getCustomConfig().getInt("lvl."+user, 0);
 				int kills = this.getCustomConfig().getInt("kills."+user, 0);
 				if(kills >= 2 && lvl == 0)
 				{
 					this.getCustomConfig().set("kills."+user, 0);
 					this.saveCustomConfig();
 				}
 			}
 		}
 		for(Player p : Bukkit.getServer().getOnlinePlayers())
 		{
 			if(!data.isset(p.getName()))
 			{
 				data.setValue(p.getName(), 0, 0, 0);
 			}
 		}
 	}
 	
 	public DataHandler getDB()
 	{
 		return this.data;
 	}
 	
 	//reload data.yml
 	public void reloadCustomConfig()
 	{
 	    if (customConfigFile == null)
 	    {
 	    	customConfigFile = new File(getDataFolder(), "data.yml");
 	    }
 	    customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
 	    java.io.InputStream defConfigStream = this.getResource("data.yml");
 	    if (defConfigStream != null) {
 	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 	        customConfig.setDefaults(defConfig);
 	    }
 	}
 	
 	//get data.yml file
 	public FileConfiguration getCustomConfig()
 	{
 	    if (customConfig == null)
 	    {
 	        this.reloadCustomConfig();
 	    }
 	    return customConfig;
 	}
 
 	//save data.yml file
 	public void saveCustomConfig()
 	{
 	    if (customConfig == null || customConfigFile == null)
 	    {
 	    	return;
 	    }
 	    try {
 	        getCustomConfig().save(customConfigFile);
 	    } catch (IOException ex) {
 	        this.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);}
 	}
 	
 	//log a message in console
 	public void log(String msg)
 	{
 		if(this.getConfig().getBoolean("settings.useFancyConsole"))
 		{
 			ConsoleCommandSender console = this.getServer().getConsoleSender();
 			String name = "["+ChatColor.LIGHT_PURPLE+this.getDescription().getName()
 					+" v"+this.getDescription().getVersion()+ChatColor.GRAY+"] ";
 			console.sendMessage(name+msg);
 		}else
 			this.log.info(msg);
 	}
 	
 	//log a warning in console
 	public void warn(String msg)
 	{
 		if(this.getConfig().getBoolean("settings.useFancyConsole"))
 		{
 			ConsoleCommandSender console = this.getServer().getConsoleSender();
 			String name = ChatColor.DARK_RED+"[Warning]"+ChatColor.GRAY+
 					"["+ChatColor.LIGHT_PURPLE+this.getDescription().getName()+ChatColor.GRAY+"] ";
 			console.sendMessage(name+msg);
 		}else
 			this.log.warning(msg);
 	}
 
 	//check for available economy providers and set them up using Vault
     private boolean setupEconomy()
     {
         RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
         if (economyProvider != null) {
             economy = economyProvider.getProvider();
         }
 
         return (economy != null);
     }
 }

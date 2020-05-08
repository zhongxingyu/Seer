 package me.shock.shockpvp;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin
 {
 
 	/*
 	 * Variables for config
 	 */
 	double deathexplodesize;
 	double deathexpdrop;
 	double deathmoneyloss;
 	double noarmordamagemultiplier;
 	
 	//private Config config = new Config(this);
 	public FileConfiguration config;
 	private Logger log = Bukkit.getLogger();
  	public static Economy econ = null;
 	
 	public Main plugin;
 	
 	
 	public void onEnable()
 	{
 		PluginManager pm = getServer().getPluginManager();
 		
 		loadConfig();
 		
 		pm.registerEvents(new DropListener(this), this);
 		pm.registerEvents(new InteractListener(this), this);
 		pm.registerEvents(new LaunchListener(this), this);
 		pm.registerEvents(new DeathListener(this), this);
 		pm.registerEvents(new DamageListener(this), this);
 		pm.registerEvents(new ArmorListener(this), this);
 
 		
 		try 
 		{
 			setupEconomy();
 		}
 		catch(Exception e)
 		{
 			log.info("[ShockPvP] can't find vault :(");
 			log.info("[ShockPvP] not using economy");
 		}
 		
 	}
 	
 	public void onDisable()
 	{
 		
 	}
 	
 	private boolean setupEconomy() 
 	{
 		if (getServer().getPluginManager().getPlugin("Vault") == null) 
 		{
 			return false;
 		}
 		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
 		if (rsp == null) 
 		{
 			return false;
 		}
 		econ = rsp.getProvider();
 		return econ != null;
 	}
 	
 	private void loadConfig() 
 	{
 		try 
 		{
 			reloadConfig();
 			this.config = getConfig();
 			this.config.options().copyDefaults(true);
 	
 			this.deathexplodesize = Double.valueOf(this.config.getDouble("deathexplodesize"));
 			this.deathexpdrop = Double.valueOf(this.config.getDouble("deathexpdrop"));
 			this.deathmoneyloss = Double.valueOf(this.config.getDouble("deathmoneyloss"));
 			this.noarmordamagemultiplier = Double.valueOf(this.config.getDouble("noarmordamagemultiplier"));
 			saveConfig();
 			this.log.info("[ShockPvP] config loaded");
 		}
 		catch (Exception e) 
 		{
 			this.log.log(Level.SEVERE, "[ShockPvP] Failed to load config", e);
 			e.printStackTrace();
 			// Hopefully never happens :o
 		}
 	}
 	
 }

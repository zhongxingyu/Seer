 package me.wizzledonker.plugins.oblicomcore.world;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 import me.wizzledonker.plugins.oblicomcore.exceptions.OblicomWorldException;
 
 import me.wizzledonker.plugins.oblicomranks.OblicomRankScore;
 import me.wizzledonker.plugins.oblicomranks.OblicomRanks;
 
 import net.milkbowl.vault.economy.Economy;
 
 /**
  *
  * @author nagib.kanaan
  */
 public class OblicomWorld {
     
     private static OblicomWorld world = null;
     private Economy economy = null;
     private OblicomRankScore ranking = null;
     
     private Jail jail;
     private Wanted wanted;
     private Stealer stealer;
     private Tracker tracker;
     private Lockpicker lockpicker;
     
     /**
      * The world configuration file
      */
     private final YamlConfiguration data;
 
     /**
      * The world configuration file
      */
     private final File dataFile;    
     
     /**
      * The plugin this metrics submits for
      */
     private final Plugin plugin;
     
     private OblicomWorld(Plugin plugin) throws OblicomWorldException {
         
         this.plugin = plugin;
         
         // load dependecies
         _setupDependencies();
         
         // load the config
         dataFile = getDataFile();
         data = YamlConfiguration.loadConfiguration(dataFile);
         
         if (!dataFile.exists()) {            
             _setDefaults();
             
             data.options().copyHeader(true);
             data.options().copyDefaults(true);
             
             try {
                 data.save(dataFile);
             } catch(IOException error) {
                 throw new OblicomWorldException("Error to save the data file of the world", error);
             }
         }
         
         // Setup the features
         wanted = new Wanted();
         jail = new Jail();
         stealer = new Stealer();
         tracker = new Tracker();
         lockpicker = new Lockpicker();
     }
     
     public static OblicomWorld getWorld() {
        return OblicomWorld.world;
     }    
     
     public static OblicomWorld getWorld(Plugin plugin) throws OblicomWorldException {
        if (OblicomWorld.world == null) {
            OblicomWorld.world = new OblicomWorld(plugin);
         }
         
        return OblicomWorld.world;
     }
     
     /**
      * Gets the data from the world.
      *
      * @return the ranking
      */        
     
     public YamlConfiguration getData() {
         return data;
     }
 
     /**
      * Gets the plugin
      *
      * @return the plugin
      */        
     
     public Plugin getPlugin() {
         return plugin;
     }
     
     /**
      * Gets the economy service.
      *
      * @return the economy
      */        
     
     public Economy getEconomy() {
         return economy;
     }
 
     /**
      * Gets the ranking service.
      *
      * @return the ranking
      */        
     public OblicomRankScore getRanking() {
         return ranking;
     }
 
     /**
      * Gets the jail service.
      *
      * @return the jail
      */        
     public Jail getJail() {
         return jail;
     }
 
     /**
      * Gets the wanted service.
      *
      * @return the wanted
      */        
     public Wanted getWanted() {
         return wanted;
     }
     
     /**
      * Gets the stealer service.
      *
      * @return the stealer
      */        
     public Stealer getStealer() {
         return stealer;
     }
     
     /**
      * Gets the tracker service.
      *
      * @return the tracker
      */        
     public Tracker getTracker() {
         return tracker;
     }
     
     /**
      * Gets the lockpicker service.
      *
      * @return the lockpicker
      */        
     public Lockpicker getLockpicker() {
         return lockpicker;
     }
     
     /**
      * Gets the ranking service.
      *
      * @return the ranking
      */        
     public void save() throws OblicomWorldException {
         data.options().copyHeader(true);
         data.options().copyDefaults(true);
 
         try {
             data.save(dataFile);
         } catch(IOException error) {
             throw new OblicomWorldException("Error to save the data file of the world", error);
         }
     }
     
     /**
      * Gets the File object of the world file that should be used to store data.
      *
      * @return the File object for the world file
      */    
     private File getDataFile() {
         File pluginsFolder = plugin.getDataFolder().getParentFile();
 
         return new File(new File(pluginsFolder, "OblicomCore"), "world.yml");
     }
     
     /**
      * Set default values.
      */    
     private void _setDefaults() {
 
         data.options().header("OblicomWrold.");
         
         data.addDefault("criminal.stealers", "");
         data.addDefault("police.wanted.citizens", "");
         
         data.addDefault("police.jail.prisoners", "");
         data.addDefault("police.jail.location.x", 10);
         data.addDefault("police.jail.location.y", 10);
         data.addDefault("police.jail.location.z", 10);
         data.addDefault("police.jail.location.world", "world");
     }
     
     private void _setupDependencies() throws OblicomWorldException {
         _setupEconomy();
         _setupRanks();
     }
     
     private boolean _setupEconomy() throws OblicomWorldException {
         Plugin vault = plugin.getServer().getPluginManager().getPlugin("Vault");
         
         if (vault == null) {
             throw new OblicomWorldException("Error finding the Vault plugin.");
         }
         
         RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
         
         if (economyProvider == null) {
             throw new OblicomWorldException("Error to load economy.");
         }
         
         economy = economyProvider.getProvider();
         return true;
     }
     
     private boolean _setupRanks() throws OblicomWorldException {
         Plugin ranks = plugin.getServer().getPluginManager().getPlugin("oblicomRanks");
                  
         if (ranks == null) {
             throw new OblicomWorldException("Error to load the Oblicom Ranks");
         }
 
         OblicomRanks oblicomRanks = (OblicomRanks) ranks;
         
         if (oblicomRanks.score == null) {
             throw new OblicomWorldException("Error to get the Oblicom Ranks scores.");
         }
         
         ranking = oblicomRanks.score;
         return true;
     }   
 }

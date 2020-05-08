 package com.github.CubieX.TimedRanks;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class TimedRanksConfigHandler
 {
    private final TimedRanks plugin;
    private FileConfiguration config = null;
    private FileConfiguration promoPlayersCfg = null;
    private File promotedPlayersConfigFile = null;
    private final String promotedPlayersFileName = "promotedPlayers.yml";
 
    //Constructor
    public TimedRanksConfigHandler(TimedRanks plugin)
    {        
       this.plugin = plugin;
 
       initConfig();
    }
 
    private void initConfig()
    {
       // plugin config
       plugin.saveDefaultConfig(); //creates a copy of the provided config.yml in the plugins data folder, if it does not exist
       config = plugin.getConfig(); //re-reads config out of memory. (Reads the config from file only, when invoked the first time!)
 
       // promoted players list
      reloadPromotedPlayersFile(); // load file from disk and create objects
       savePromotedPlayersDefaultFile(); // creates a copy of the provided promotedPlayers.yml
      reloadPromotedPlayersFile(); // load config again, now when file is actually present on disk (fix for "first load" error)
    }
    
    public FileConfiguration getConfig()
    {
       return (config);
    }
 
    //reloads the config from disc (used if user made manual changes to the config.yml file)
    public void reloadConfig(CommandSender sender)
    {
      // reload config.yml
       plugin.reloadConfig();
       config = plugin.getConfig(); // new assignment necessary when returned value is assigned to a variable or static field(!)

      // reload promotedPlayers.yml
       reloadPromotedPlayersFile();
       
       plugin.readConfigValues();
 
       sender.sendMessage(ChatColor.GREEN + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion() + " reloaded!");       
    }
 
    // =====================================
    // promotedPlayers config handling
    // =====================================
 
    // reload from disk
    public void reloadPromotedPlayersFile()
    {
       if (promotedPlayersConfigFile == null)
       {
          promotedPlayersConfigFile = new File(plugin.getDataFolder(), promotedPlayersFileName);
       }
       promoPlayersCfg = YamlConfiguration.loadConfiguration(promotedPlayersConfigFile);
 
       // Look for defaults in the jar
       InputStream defConfigStream = plugin.getResource(promotedPlayersFileName);
       if (defConfigStream != null)
       {
          YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
          promoPlayersCfg.setDefaults(defConfig);
       }
    }
    
    public FileConfiguration getPromotedPlayersFile()
    {
       if(null == promoPlayersCfg)
       {
          this.reloadPromotedPlayersFile();
       }
       return promoPlayersCfg;
    }
 
    //save config
    public void savePromotedPlayersFile()
    {
       if (promoPlayersCfg == null || promotedPlayersConfigFile == null)
       {
          return;
       }
      else
       {
         try
         {
            getPromotedPlayersFile().save(promotedPlayersConfigFile);
         }
         catch (IOException ex)
         {
            TimedRanks.log.severe(TimedRanks.logPrefix + "Could not save data to " + promotedPlayersConfigFile.getName());
            TimedRanks.log.severe(ex.getMessage());
         }
       }
    }
 
    // safe a default config if there is no file present
    public void savePromotedPlayersDefaultFile()
    {
       if (!promotedPlayersConfigFile.exists())
       {            
          plugin.saveResource(promotedPlayersFileName, false);
       }
    }   
 }

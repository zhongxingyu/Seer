 package de.codeinfection.quickwango.ItemRepair;
 
 import de.codeinfection.quickwango.ItemRepair.RepairBlocks.CheapRepair;
 import de.codeinfection.quickwango.ItemRepair.RepairBlocks.CompleteRepair;
 import de.codeinfection.quickwango.ItemRepair.RepairBlocks.SingleRepair;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.milkbowl.vault.economy.Economy;
 import org.bukkit.Server;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ItemRepair extends JavaPlugin
 {
     private static ItemRepair instance = null;
     private static Logger logger = null;
     public static boolean debugMode = false;
 
     public final static List<Player> addBlockChoiceRequests = new ArrayList<Player>();
     public final static List<Player> removeBlockChoiceRequests = new ArrayList<Player>();
     
     private Server server;
     private PluginManager pm;
     private ItemRepairConfiguration config;
     private File dataFolder;
     private Economy economy = null;
 
     public ItemRepair()
     {
         instance = this;
     }
 
     public static ItemRepair getInstance()
     {
         return instance;
     }
 
     @Override
     public void onEnable()
     {
         logger = this.getLogger();
         this.server = this.getServer();
         this.pm = this.server.getPluginManager();
         
         this.dataFolder = this.getDataFolder();
 
         this.dataFolder.mkdirs();
        
         Configuration configuration = this.getConfig();
         configuration.options().copyDefaults(true);
         this.config = new ItemRepairConfiguration(configuration);
         debugMode = configuration.getBoolean("debug");
         this.saveConfig();
 
         this.economy = this.setupEconomy();
 
         RepairBlockManager rbm = RepairBlockManager.initialize(this);
                 rbm.setPersister(new RepairBlockPersister(new File(dataFolder, "blocks.yml")))
                 .addRepairBlock(new SingleRepair(
                         this.config.repairBlocks_singleRepair_block,
                         this.config
                 ))
                 .addRepairBlock(new CompleteRepair(
                         this.config.repairBlocks_completeRepair_block,
                         this.config
                 ))
                 .addRepairBlock(new CheapRepair(
                         this.config.repairBlocks_cheapRepair_block,
                         this.config
                 ))
                 .loadBlocks();
 
         this.pm.registerEvents(new ItemRepairListener(), this);
 
         this.getCommand("itemrepair").setExecutor(new ItemrepairCommand(this));
 
         log("Version " + this.getDescription().getVersion() + " enabled");
     }
 
     @Override
     public void onDisable()
     {
         addBlockChoiceRequests.clear();
         removeBlockChoiceRequests.clear();
         log("Version " + this.getDescription().getVersion() + " disabled");
     }
 
     private Economy setupEconomy()
     {
         if (this.pm.getPlugin("Vault") != null)
         {
             RegisteredServiceProvider<Economy> rsp = this.server.getServicesManager().getRegistration(Economy.class);
             if (rsp != null)
             {
                 Economy eco = rsp.getProvider();
                 if (eco != null)
                 {
                     return eco;
                 }
             }
         }
         throw new IllegalStateException("Failed to initialize with Vault!");
     }
 
     /**
      * Returns the economy API
      *
      * @return the economy API
      */
     public Economy getEconomy()
     {
         return this.economy;
     }
 
     public ItemRepairConfiguration getConfiguration()
     {
         return this.config;
     }
 
     public static void log(String msg)
     {
         logger.log(Level.INFO, msg);
     }
 
     public static void error(String msg)
     {
         logger.log(Level.SEVERE, msg);
     }
 
     public static void error(String msg, Throwable t)
     {
         logger.log(Level.SEVERE, msg, t);
     }
 
     public static void debug(String msg)
     {
         if (debugMode)
         {
             log("[debug] " + msg);
         }
     }
 }

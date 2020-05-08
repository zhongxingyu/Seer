 package com.mrmag518.ChestShopUtil;
 
 import com.mrmag518.ChestShopUtil.Files.Local;
 import com.mrmag518.ChestShopUtil.Files.Config;
 import com.mrmag518.ChestShopUtil.Files.ShopDB;
 import com.mrmag518.ChestShopUtil.Util.Cooldown;
 import com.mrmag518.ChestShopUtil.Util.EcoHandler;
 import com.mrmag518.ChestShopUtil.Util.Log;
 import com.mrmag518.ChestShopUtil.Util.UpdateThread;
 import com.mrmag518.ChestShopUtil.Util.Updater;
 
 import java.io.IOException;
 
 import javax.annotation.Nullable;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitTask;
 
 public class CSU extends JavaPlugin {
     public boolean updateFound = false;
     public String verionFound = "";
     private Plugin chestshop = null;
     public BukkitTask timeChecker = null;
     
     /**
      * Self reminder:
      * - Make daily events such as the max buy & sell feature reset at midnight.
      * - World variable support for most lists.
      * - Max sell and buy per day for specific items.
      */
     
     @Override
     public void onDisable() {
         timeChecker = null;
         Log.info("Version " + getVersion() + " disabled.");
     }
     
     @Override
     public void onEnable() {
         if(!getDataFolder().exists()) {
             getDataFolder().mkdir();
         }
         checkPlugins();
         Config.properLoad();
         Local.properLoad();
         ShopDB.properLoad();
         
         if(Config.checkUpdates) {
             if(Config.useMultiThread) {
                 Thread t = new Thread(new UpdateThread(this, null));
                 t.start();
             } else {
                 updateCheck(null);
             }
         }
         
         try {
             Metrics metrics = new Metrics(this);
             metrics.start();
         } catch (IOException e) {
         }
         
         if(ShopDB.use()) {
             startTimeCheck();
         }
         
         getCommand("chestshoputil").setExecutor(new Commands(this));
         getCommand("shopedit").setExecutor(new Commands(this));
         Log.info("Version " + getVersion() + " enabled.");
     }
     
     public void startTimeCheck() {
         if(timeChecker == null) {
             Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
                 @Override
                 public void run() {
                     ShopDB.properLoad();
                 }
             }, 864000, 864000); // Run every 12th hour.
         }
     }
     
     public String getVersion() {
         PluginDescriptionFile pdffile = getDescription();
         return pdffile.getVersion().replace("v", "");
     }
     
     public void updateCheck(@Nullable CommandSender s) {    
         Log.info("Checking for updates ..");
         
         try {
             Updater updater = new Updater(this, "chestshoputil", this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
 
             Updater.UpdateResult result = updater.getResult();
             switch(result) {
                 case NO_UPDATE:
                     Log.info("No update was found.");
                     if(s != null) {
                         s.sendMessage(ChatColor.BLUE + "No update was found.");
                     }
                     break;
                 case FAIL_DBO:
                     Log.warning("Failed to contact dev.bukkkit.org!");
                     if(s != null) {
                         s.sendMessage(ChatColor.RED + "Failed to contact dev.bukkit.org!");
                     }
                     break;
                 case UPDATE_AVAILABLE:
                     updateFound = true;
                     verionFound = updater.getLatestVersionString().replace("v", "").replace("ChestShopUtil", "");
                     Log.info("A new version of ChestShopUtil was found!");
                     Log.info("Version found: " + updater.getLatestVersionString());
                     Log.info("Version running: ChestShopUtil v" + getVersion());
                     if(s != null) {
                         s.sendMessage(ChatColor.BLUE + "An update was found!");
                         s.sendMessage(ChatColor.BLUE + "Version found: " + updater.getLatestVersionString());
                         s.sendMessage(ChatColor.BLUE + "Version running: " + getVersion());
                     }
                     break;
             }
         } catch(RuntimeException re) {
             Log.warning("Failed to establish a connection to dev.bukkit.org!");
             if(s != null) {
                 s.sendMessage(ChatColor.RED + "Failed to establish a connection to dev.bukkit.org!");
             }
         }
     }
     
     private void checkPlugins() {
         PluginManager pm = getServer().getPluginManager();
         chestshop = pm.getPlugin("ChestShop");
         Plugin vault = pm.getPlugin("Vault");
         
         if(chestshop != null && pm.isPluginEnabled(chestshop)) {
             String version = chestshop.getDescription().getVersion().replace("v", "");
             Log.info("ChestShop version " + version + " found!");
             EventListener listener = new EventListener(this);
         } else {
             Log.severe("ChestShop was not found!");
             Log.severe("Please make sure ChestShop is installed, and is being enabled without errors!");
             Log.severe("Disabling ..");
             pm.disablePlugin(this);
         }
         
         if(vault != null && pm.isPluginEnabled(vault)) {
             EcoHandler.setupEconomy();
             Log.info("Hooked to " + EcoHandler.getEconomy().getName() + "!");
         } else {
             Log.severe("Vault was not found!");
             Log.severe("Please make sure Vault is installed, and is being enabled without errors!");
             Log.severe("Disabling ..");
             pm.disablePlugin(this);
         }
     }
 }

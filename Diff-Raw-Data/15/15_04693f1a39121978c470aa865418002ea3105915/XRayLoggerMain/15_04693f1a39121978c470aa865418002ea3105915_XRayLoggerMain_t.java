 package de.FlatCrafter.XRayLogger;
 
import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import net.gravitydevelopment.updater.Updater;
 import net.milkbowl.vault.economy.Economy;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.configuration.MemorySection;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.Metrics;
 import org.mcstats.Metrics.Graph;
 
 
 public class XRayLoggerMain extends JavaPlugin
 {
   BlockBreakLoggerListener bbll;
   CommandExecutor exe;
   private Economy economy;
 
   @Override
   public void onEnable() {
     if ((getConfig().getBoolean("fine.enabled")) && (setupEconomy())) {
       setupEconomy();
       getLogger().info("Fining is enabled");
     } else {
       System.out.println("Vault hooked economy plugin: " + vaultHookedPlugin());
       System.out.println("Enabled in config: " + isFineable());
     }
     this.bbll = new BlockBreakLoggerListener(this);
     this.exe = new XLCmdExec(this);
     if (isBannable()) {
           System.out.println("[XRayLogger]: Banning is enabled");
     }
     else if (!isBannable()) {
       System.out.println("[XRayLogger]: Banning is disabled.");
     }
 
     getCommand("logs.check").setExecutor(this.exe);
     getCommand("logs.clear").setExecutor(this.exe);
     getCommand("me.hide").setExecutor(this.exe);
     getCommand("me.remove").setExecutor(this.exe);
     getCommand("logs.look").setExecutor(this.exe);
     getCommand("logs.hidden").setExecutor(this.exe);
     getCommand("player.hide").setExecutor(this.exe);
     getCommand("player.remove").setExecutor(this.exe);
     getCommand("config.reload").setExecutor(this.exe);
     getCommand("config.purge").setExecutor(exe);
     getServer().getPluginManager().registerEvents(this.bbll, this);
     if(getConfig().getStringList("hidden") != null || getConfig().getStringList("logs") != null) {
         bbll.hideXray = getConfig().getStringList("hidden");
         bbll.loggedXray = getConfig().getStringList("logs");
     }
     if (!getConfig().contains("fine.enabled")) {
         HashMap<String, Object> defaults = new HashMap();
                         defaults.put("fine.enabled", true);
                         defaults.put("fine.amount", 10.0);
                         defaults.put("fine.fine-item-amount.lapis", 40);
                         defaults.put("fine.fine-item-amount.diamond", 20);
                         defaults.put("fine.fine-item-amount.gold", 20);
                         defaults.put("ban.enabled", false);
                         defaults.put("ban.message", "&cYou have been banned for surpassing the item &4BAN &climit!");
                         defaults.put("ban.ban-amount.lapis", 196);
                         defaults.put("ban.ban-amount.diamond", 128);
                         defaults.put("ban.ban-amount.gold", 128);
                         defaults.put("item-amount.lapis", 32);
                         defaults.put("item-amount.diamond", 16);
                         defaults.put("item-amount.gold", 16);
 
                         Set<String> keys = defaults.keySet();
                         for(String path : keys) {
                             getConfig().set(path, defaults.get(path));
                         }
                         this.saveDefaultConfig();
                         saveConfig();
                         reloadConfig();
     }
     if(getConfig().getBoolean("autoUpdate.enabled")) {
         Updater update = new Updater(this, 49531, this.getFile(), Updater.UpdateType.DEFAULT, getConfig().get("autoUpdate.consoleInfo") instanceof MemorySection || getConfig().get("autoUpdate.consoleInfo")== null ? false : getConfig().getBoolean("autoUpdate.consoleInfo"));
         Updater.UpdateResult result = update.getResult();
         switch(result)
         {
             case SUCCESS:
                 getLogger().log(Level.INFO, "Successfully downloaded newest version of {0}", update.getLatestName());
                 break;
             case NO_UPDATE:
                 getLogger().info("No update found!");
                 break;
             case DISABLED:
                 getLogger().info("I won't update because you don't want me to. Look in the update file for a configuration file.");
                 break;
             case FAIL_DOWNLOAD:
                 getLogger().info("Found and update but was unable to download.");
                 break;
             case FAIL_DBO:
                 getLogger().info("Update found. Communication errors with BukkitDev");
                 break;
             default:
                 getLogger().severe("Unknown issue found with the updater. Not updating. Continuing");
                 break;
         }
     } else if (!getConfig().contains("autoUpdate.enabled")){
         getConfig().set("autoUpdate.enabled", true);
         getConfig().set("autoUpdate.consoleInfo", false);
     }
     try {
       Metrics metrics = new Metrics(this);

      Graph peopleUsingXRay = metrics.createGraph("Players Using XRay");
       peopleUsingXRay.addPlotter(new Metrics.Plotter("People") {
         @Override
         public int getValue() {
          return bbll.loggedXray.size();
         }
       });

       metrics.start();
     } catch (IOException ioe) {

     }
   }
 
   @Override
   public void onDisable()
   {
     String[] loggedArray = new String[this.bbll.loggedXray.size()];
     int i = 0;
     for (String string : this.bbll.loggedXray) {
       loggedArray[i] = string;
       i++;
     }
     List loggedXray = Arrays.asList(loggedArray);
     i = 0;
     String[] hiddenArray = new String[this.bbll.hideXray.size()];
     for (String string : this.bbll.hideXray) {
       hiddenArray[i] = string;
       i++;
     }
     List hiddenXray = Arrays.asList(hiddenArray);
     getConfig().set("logs", loggedXray);
     getConfig().set("hidden", hiddenXray);
     saveConfig();
   }
 
   public boolean isBannable() {
     return getConfig().getBoolean("ban.enabled");
   }
 
   public boolean isFineable() {
     return getConfig().getBoolean("fine.enabled");
   }
 
   public double getFine() {
     return getConfig().getDouble("fine.amount");
   }
 
   public int getDiamond() {
     return getConfig().getInt("item-amount.diamond");
   }
 
   public int getGold() {
     return getConfig().getInt("item-amount.gold");
   }
 
   public int getLapis() {
     return getConfig().getInt("item-amount.lapis");
   }
 
   public int getDiamondBan() {
     return getConfig().getInt("ban.ban-amount.diamond");
   }
 
   public int getGoldBan() {
     return getConfig().getInt("ban.ban-amount.gold");
   }
 
   public int getLapisBan() {
     return getConfig().getInt("ban.ban-amount.lapis");
   }
 
   public int getDiamondFine() {
     return getConfig().getInt("fine.fine-item-amount.diamond");
   }
 
   public int getGoldFine() {
     return getConfig().getInt("fine.fine-item-amount.gold");
   }
 
   public int getLapisFine() {
     return getConfig().getInt("fine.fine-item-amount.lapis");
   }
 
   private boolean setupEconomy() {
     RegisteredServiceProvider economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
     if (economyProvider != null) {
       this.economy = ((Economy)economyProvider.getProvider());
       rsp = economyProvider;
     }
     return this.economy != null;
   }
 
   public Economy getEcon() {
     return this.economy;
   }
  RegisteredServiceProvider rsp;
     private String vaultHookedPlugin() {
         for(Plugin p : getServer().getPluginManager().getPlugins()) {
             if(p.getName().equalsIgnoreCase("Vault") && setupEconomy() && rsp != null) {
                 return "Vault found " + rsp.getProvider().getClass().getSimpleName();
             } else if (!setupEconomy() && p.getName().equalsIgnoreCase("Vault")){
                 return "Vault is enabled but you do not have an economy plugin!";
             } else {
                 continue;
             }
         }
         return "Vault is not enabled";
     }
 }

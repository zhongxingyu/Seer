 package net.daboross.bukkitdev.playerdata;
 
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import net.daboross.bukkitdev.playerdata.metrics.Metrics;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.Bukkit;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * PlayerData Plugin Made By DaboRoss
  *
  * @author daboross
  */
 public final class PlayerData extends JavaPlugin {
 
     private static PlayerData currentInstance;
     private static boolean isVaultLoaded;
     private static Permission permissionHandler;
     private PDataHandler playerDataHandler;
     private PlayerDataHandler handler;
     private PlayerDataEventListener eventListener;
     private Metrics metrics;
     private PlayerDataCustomMetrics pdcm;
 
     /**
      *
      */
     @Override
     public void onEnable() {
         currentInstance = this;
         try {
             metrics = new Metrics(this);
         } catch (IOException ioe) {
             getLogger().warning("Unable to create Metrics");
         }
         if (metrics != null) {
             metrics.start();
             pdcm = new PlayerDataCustomMetrics(this, metrics);
         }
         PluginManager pm = this.getServer().getPluginManager();
         setupVault(pm);
         playerDataHandler = new PDataHandler(this);
         PluginCommand playerdata = getCommand("playerdata:playerdata");
         if (playerdata != null) {
             playerdata.setExecutor(new PlayerDataCommandExecutor(this));
         } else {
             getLogger().log(Level.WARNING, "Command /playerdata:playerdata not found! Is another plugin using it?");
         }
         PluginCommand getusername = getCommand("playerdata:getusername");
         if (getusername != null) {
             getusername.setExecutor(new PossibleUserNames(this));
         } else {
             getLogger().log(Level.WARNING, "Command /playerdata:getusername not found! Is another plugin using it?");
         }
         eventListener = new PlayerDataEventListener(this);
         pm.registerEvents(eventListener, this);
         handler = new PlayerDataHandler(this);
         playerDataHandler.init();
         if (pdcm != null) {
             pdcm.addCustom();
         }
         getLogger().info("PlayerData Load Completed");
     }
 
     /**
      *
      */
     @Override
     public void onDisable() {
         playerDataHandler.endServer();
         playerDataHandler.saveAllData(false, null);
         currentInstance = null;
         permissionHandler = null;
         isVaultLoaded = false;
         getLogger().info("PlayerData Unload Completed");
     }
 
     /**
      * This is the internal PDataHandler. Use getHandler() instead if you are
      * outside of the PlayerData project.
      *
      * @return
      */
     public PDataHandler getPDataHandler() {
         return playerDataHandler;
     }
 
     /**
      *
      * @return
      */
     public static PlayerData getCurrentInstance() {
         return currentInstance;
     }
 
     public PlayerDataHandler getHandler() {
         return handler;
     }
 
     /**
      * Get a visually nice date from a timestamp. Acts like: 4 years, 2 months,
      * 1 day, 10 hours, 30 minutes, and 9 seconds (That is just a random string
      * of numbers I came up with, but that is what the formating is like) Will
      * emit any terms that are 0, eg, if 0 days, then it would be 4 years, 2
      * months, 10 hours, 30 minutes, and 9 seconds Will put a , between all
      * terms and also a , and between the last term and the second to last term.
      * would do 4 years, 2 months and 10 hours returns now if
      *
      * @param millis the millisecond value to turn into a date string
      * @return A visually nice date. "Not That Long" if millis == 0;
      */
    public static String getFormattedDate(long millis) {
         if (millis == 0) {
             return "Not That Long";
         }
         long years;
         long days;
         long hours;
         long minutes;
         long seconds;
         years = 0;
         days = TimeUnit.MILLISECONDS.toDays(millis);
         hours = TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.DAYS.toHours(days);
         minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hours) - TimeUnit.DAYS.toMinutes(days);
         seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.DAYS.toSeconds(days);
         while (days > 365) {
             years += 1;
             days -= 365;
         }
         StringBuilder resultBuilder = new StringBuilder();
         if (years > 0) {
             resultBuilder.append(years).append(years == 1 ? " year" : " years");
             if (days > 0) {
                 resultBuilder.append(", and ");
             }
         }
         if (days > 0) {
             resultBuilder.append(years).append(days == 1 ? " day" : " days");
             if (hours > 0) {
                 resultBuilder.append(", and ");
             }
         }
         if (hours > 0 && years <= 0) {
             resultBuilder.append(hours).append(hours == 1 ? " hour" : " hours");
             if (minutes > 0 && days <= 0) {
                 resultBuilder.append(", and ");
             }
         }
         if (minutes > 0 && days <= 0 && years <= 0) {
             resultBuilder.append(minutes).append(minutes == 1 ? " minute" : " minutes");
             if (seconds > 0 && hours <= 0) {
                 resultBuilder.append(", and ");
             }
         }
         if (seconds > 0 && hours <= 0 && days <= 0 && years <= 0) {
             resultBuilder.append(seconds).append(seconds == 1 ? " second" : " seconds");
         }
         return resultBuilder.toString();
     }
 
     public static String formatList(String[] str) {
         String returnS = "";
         for (int i = 0; i < str.length; i++) {
             if (!returnS.equals("")) {
                 returnS += ", ";
             }
             returnS += str[i];
         }
         return returnS;
     }
 
     public static String getCombinedString(String[] array, int start) {
         if (array == null || start >= array.length || start < 0) {
             throw new IllegalArgumentException();
         } else if (start + 1 == array.length) {
             return array[start];
         } else {
             StringBuilder sb = new StringBuilder(array[start]);
             for (int i = start + 1; i < array.length; i++) {
                 sb.append(" ").append(array[i]);
             }
             return sb.toString();
         }
     }
 
     private void setupVault(PluginManager pm) {
         isVaultLoaded = pm.isPluginEnabled("Vault");
         if (isVaultLoaded) {
             RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
             permissionHandler = rsp.getProvider();
             if (permissionHandler == null) {
                 isVaultLoaded = false;
                 getLogger().log(Level.INFO, "Vault found, but Permission handler not found.");
             } else {
                 getLogger().log(Level.INFO, "Vault and Permission handler found.");
             }
         } else {
             getLogger().log(Level.INFO, "Vault not found.");
         }
     }
 
     public static boolean isVaultLoaded() {
         return isVaultLoaded;
     }
 
     public static Permission getPermissionHandler() {
         return permissionHandler;
     }
 
     public PlayerDataEventListener getEventListener() {
         return eventListener;
     }
 }

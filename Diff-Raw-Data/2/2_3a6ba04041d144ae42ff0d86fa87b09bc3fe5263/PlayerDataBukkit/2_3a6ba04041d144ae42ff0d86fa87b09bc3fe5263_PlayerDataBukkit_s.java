 /*
  * Author: Dabo Ross
  * Website: www.daboross.net
  * Email: daboross@daboross.net
  */
 package net.daboross.bukkitdev.playerdata;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import net.daboross.bukkitdev.playerdata.api.PlayerHandler;
 import net.daboross.bukkitdev.playerdata.libraries.metrics.MetricsLite;
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
 public final class PlayerDataBukkit extends JavaPlugin {
 
     private PlayerHandlerImpl playerHandler;
     private boolean permissionLoaded = false;
     private Permission permissionHandler;
     private boolean enabledSuccessfully = true;
 
     @Override
     public void onEnable() {
         PlayerDataStatic.setPlayerDataBukkit(this);
         MetricsLite metrics = null;
         try {
             metrics = new MetricsLite(this);
         } catch (IOException ioe) {
             getLogger().warning("Unable to create Metrics");
         }
         if (metrics != null) {
             metrics.start();
         }
         PluginManager pm = this.getServer().getPluginManager();
         if (pm.isPluginEnabled("Vault")) {
             RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
             permissionHandler = rsp.getProvider();
             if (permissionHandler == null) {
                 getLogger().log(Level.INFO, "Vault found. Permission not found.");
             } else {
                 permissionLoaded = true;
                getLogger().log(Level.INFO, "Vault found. Permission not found.");
             }
         } else {
             getLogger().log(Level.INFO, "Vault not found.");
         }
         playerHandler = new PlayerHandlerImpl(this);
         enabledSuccessfully = playerHandler.init();
         if (!enabledSuccessfully) {
             pm.disablePlugin(this);
             PlayerDataStatic.setPlayerDataBukkit(null);
             permissionHandler = null;
             playerHandler = null;
             permissionLoaded = false;
             return;
         }
         PluginCommand playerdata = getCommand("pd");
         if (playerdata != null) {
             new PlayerDataCommandExecutor(this, playerHandler).registerCommand(playerdata);
         }
         PluginCommand getusername = getCommand("gu");
         if (getusername != null) {
             getusername.setExecutor(new GetUsernameCommand(playerHandler));
         }
         pm.registerEvents(new PlayerDataEventListener(playerHandler), this);
         playerHandler.init();
         getLogger().info("PlayerData Load Sucessful");
     }
 
     @Override
     public void onDisable() {
         if (enabledSuccessfully) {
             playerHandler.endServer();
             playerHandler.saveAllData();
         }
         PlayerDataStatic.setPlayerDataBukkit(null);
         permissionHandler = null;
         playerHandler = null;
         permissionLoaded = false;
     }
 
     public PlayerHandler getHandler() {
         return playerHandler;
     }
 
     public boolean isPermissionLoaded() {
         return permissionLoaded;
     }
 
     public Permission getPermissionHandler() {
         return permissionHandler;
     }
 
     public int getAPIVersion() {
         return enabledSuccessfully == true ? 1 : -1;
     }
 
     public boolean enabledSuccessfully() {
         return enabledSuccessfully;
     }
 }

 package redcastlemedia.multitallented.bukkit.heromatchmaking.managers;
 
 /**
  *
  * @author Multitallented
  */
 import java.io.File;
import java.io.IOException;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.parts.redcastlemedia.multitallented.controllers.Controller;
 import org.parts.redcastlemedia.multitallented.controllers.Manager;
 import org.parts.redcastlemedia.multitallented.controllers.Order;
 import org.parts.redcastlemedia.multitallented.listeners.BukkitEventListener;
 import org.parts.redcastlemedia.multitallented.models.YMLProxy;
 import redcastlemedia.multitallented.bukkit.heromatchmaking.builders.ProxyBuilder;
 
 /**
  *
  * @author Multitallented
  */
 public class InitManager implements Manager {
     
     @Override
     public Order build() {
         return new Order("init", false) {
 
             @Override
             public void exec() {
                 //Setup HeroStronghold dependency
                 PluginManager pm = Bukkit.getServer().getPluginManager();
                 if (pm.isPluginEnabled("Heroes")) {
                     Controller.addInstance("heroes", pm.getPlugin("Heroes"));
                     System.out.println("[HeroMatchMaking] Hooked into Heroes");
                 } else {
                      new BukkitEventListener() {
                         @EventHandler
                         public void onPluginEnable(PluginEnableEvent event) {
                             if (event.getPlugin().getDescription().getName().equals("Heroes")) {
                                 Controller.addInstance("heroes", event.getPlugin());
                                 System.out.println("[HeroMatchMaking] Hooked into Heroes");
                             }
                         }
                     };
                 }
                 
                 if (pm.isPluginEnabled("Vault")) {
                     //Setup econ and perm providers
                     RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
                     Economy econ = (Economy) Controller.getInstance("econ");
                     if (rsp != null) {
                         econ = rsp.getProvider();
                         if (econ != null)
                             System.out.println("[HeroMatchMaking] Hooked into " + econ.getName());
                     }
 
                     Permission perm = (Permission) Controller.getInstance("perm");
                     RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
                     if (permissionProvider != null) {
                         perm = permissionProvider.getProvider();
                         if (perm != null)
                             System.out.println("[HeroMatchMaking] Hooked into " + perm.getName());
                     }
                 } else {
                     new BukkitEventListener() {
                         @EventHandler
                         public void onPluginEnable(PluginEnableEvent event) {
                             if (event.getPlugin().getDescription().getName().equals("Vault")) {
                                 RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
                                 Economy econ = (Economy) Controller.getInstance("econ");
                                 if (rsp != null) {
                                     econ = rsp.getProvider();
                                     if (econ != null)
                                         System.out.println("[HeroMatchMaking] Hooked into " + econ.getName());
                                 }
 
                                 Permission perm = (Permission) Controller.getInstance("perm");
                                 RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
                                 if (permissionProvider != null) {
                                     perm = permissionProvider.getProvider();
                                     if (perm != null)
                                         System.out.println("[HeroMatchMaking] Hooked into " + perm.getName());
                                 }
                             }
                         }
                     };
                 }
                 
                 //Load data
                 YMLProxy config = ProxyBuilder.buildDefaultConfigProxy();
                 JavaPlugin plugin = (JavaPlugin) Controller.getInstance("plugin");
                 File file = new File(plugin.getDataFolder(), "config.yml");
                 File folder = new File(plugin.getDataFolder() + "");
                folder.mkdirs();
                 config.createIfNotExists(file);
                 Controller.addInstance("config", config);
                 
                 Controller.addInstance("playermanager", new PlayerManager());
                 Controller.addInstance("arenamanager", new ArenaManager());
                 
                 //Setup listeners
             }
             
         };
     }
     
 }

 package com.chaseoes.forcerespawn;
 
 import com.chaseoes.forcerespawn.api.NMS;
 import com.chaseoes.forcerespawn.exception.ForceRespawnException;
 import com.chaseoes.forcerespawn.listener.PlayerDeathListener;
 import org.apache.commons.lang.Validate;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class ForceRespawn extends JavaPlugin {
 
     private static ForceRespawn instance;
     private static Thread thread;
 
     private NMS nms;
 
     public static void sendRespawnPacket(Player player) {
         Validate.notNull(player, "player can not be null");
         threadCheck();
         ForceRespawn.instance.nms.sendRespawnPacket(player);
     }
 
     public void onEnable() {
         ForceRespawn.instance = this;
         ForceRespawn.thread = Thread.currentThread();
         String version = getPackageVersion();
         if (version.equals("craftbukkit"))
            version = "pre";
         try {
             Class<?> clazz = Class.forName("com.chaseoes.forcerespawn.nms." + version + ".NMSHandler");
             if (NMS.class.isAssignableFrom(clazz)) {
                 nms = (NMS) clazz.newInstance();
             }
         } catch (Exception ex) {
             getLogger().severe("Could not find support for version: " + version);
             getLogger().severe("Check for an update here: " + "http://dev.bukkit.org/bukkit-plugins/force-respawn/");
             getLogger().severe("If there is no update, bug psycowithespn");
             this.setEnabled(false);
             return;
         }
         getLogger().info("Loaded support for " + version);
         registerListeners();
     }
 
     public void onDisable() {
         ForceRespawn.instance = null;
         ForceRespawn.thread = null;
     }
 
     private String getPackageVersion() {
         String pkg = getServer().getClass().getPackage().getName();
         return pkg.substring(pkg.lastIndexOf('.') + 1);
     }
 
     private void registerListeners() {
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvents(new PlayerDeathListener(this), this);
     }
 
     private static void threadCheck() {
         if (ForceRespawn.instance == null) {
             throw new ForceRespawnException("Api call when ForceRespawn is disabled!");
         }
         if (!Thread.currentThread().equals(thread)) {
             throw new ForceRespawnException("Api call from a different thread! Expected: " + thread.getName() + ", Recieved: " + Thread.currentThread().getName());
         }
     }
 }

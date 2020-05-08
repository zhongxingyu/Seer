 /*
  * ************************************* Coded by Ultimate + ShadowLordAlpha *********************************
  *          Any use of this not on the server Coalition Events/Mini-Games is strictly PROHIBITED!  
  * ***********************************************************************************************************           
  */
 package me.ultimate.E;
 
 import java.util.HashMap;
 
 import me.ultimate.E.ChatManager.ChatListener;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitTask;
 
 public class Events extends JavaPlugin {
 
     Location loc;
     String eventName;
     BukkitTask Scoreboard;
     HashMap<Player, String> playerEvent = new HashMap<Player, String>();
     String currentEvent;
 
     @Override
     public void onEnable() {
         Scoreboard = new Scoreboard(this).runTaskTimer(this, 20, 20);
         getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getConfig().options().copyDefaults(true);
        saveConfig();
     }
 
     @Override
     public void onDisable() {
         Scoreboard.cancel();
     }
 }

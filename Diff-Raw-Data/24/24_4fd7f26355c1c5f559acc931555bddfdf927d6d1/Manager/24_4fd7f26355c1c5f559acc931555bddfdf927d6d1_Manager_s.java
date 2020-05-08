 package net.krinsoft.deathcounter;
 
 import org.bukkit.entity.Player;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author krinsdeath
  */
 public class Manager {
     private DeathCounter plugin;
     private Map<String, Killer> killers = new HashMap<String, Killer>();
     
     public Manager(DeathCounter plugin) {
         this.plugin = plugin;
         for (Player p : plugin.getServer().getOnlinePlayers()) {
             killers.put(p.getName(), plugin.getTracker().fetch(p.getName()));
         }
     }
     
     public void register(Killer player) {
         killers.put(player.getName(), player);
     }
     
     public void register(String player) {
         killers.put(player, plugin.getTracker().fetch(player));
     }
     
     public void unregister(String player) {
         killers.remove(player);
     }
     
     public Killer getKiller(String player) {
         return killers.get(player);
     }
     
 }

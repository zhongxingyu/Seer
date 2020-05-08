 package me.jophestus.JOPHWarn;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 
 
 public class JOPHWarnListener
   implements Listener
 {
   public static JOPHWarn plugin;
 
   public JOPHWarnListener(JOPHWarn instance)
   {
     plugin = instance;
   }
  @EventHandler
   public void onPlayerJoin(PlayerJoinEvent e) {
 	Player p = e.getPlayer();
 	List<String> warnings = plugin.getCustomConfig().getStringList(
 			p.getName() + "offline");
 	int size = warnings.size();
 	if (size > 0){
 		p.sendMessage(ChatColor.GREEN + "While you were away " + p.getName() + " You were warned " + size + " times. For:");
 		for (String s : warnings) {
 
 			p.sendMessage(ChatColor.GOLD + s);
 plugin.getCustomConfig().set(p.getName() + "offline", null);
 plugin.saveCustomConfig();
 plugin.reloadCustomConfig();
 		}
 	}
 	 
     	}
   
    
     
     
   }
 
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   

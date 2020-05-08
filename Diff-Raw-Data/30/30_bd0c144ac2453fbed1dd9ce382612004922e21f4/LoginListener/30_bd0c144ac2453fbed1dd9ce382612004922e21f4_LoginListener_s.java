 package me.groot314.AdvancedM;
 
 import java.util.ArrayList;
 
 import org.bukkit.event.Listener;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 
 public class LoginListener implements Listener{
 	
 	private AdvancedM plugin; //pointer to methods in main class
 	public LoginListener(AdvancedM plugin) {
 		this.plugin = plugin;
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
  
     @EventHandler(priority = EventPriority.HIGH)
     public void Login(PlayerLoginEvent event) {
        String pip = event.getPlayer().getAddress().getAddress().getHostAddress();
        pip.replace("/", "");
         if(!(ipPresent(plugin.iplist, pip))){
         	plugin.iplist.add(pip);
         }
     }
     @EventHandler(priority = EventPriority.HIGH)
     public void Logout(PlayerQuitEvent event) {
         // Some code here
     }
     
     public static boolean ipPresent(ArrayList<String> ipl,String pip){
     	for (int i = 0; i < ipl.size(); i++) {
 			if(ipl.get(i) == pip){
 				return true;
 			}
 		}
 		return false;
     }
 }

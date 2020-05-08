 package com.github.winneonsword.JM;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 
 import org.kitteh.vanish.VanishPerms;
 import org.kitteh.vanish.event.VanishStatusChangeEvent;
 
 import static com.github.winneonsword.CMAPI.API.ChatAPI.*;
 
 public class VanishJM implements Listener {
 	
 	private MainJM plugin;
 	
 	public VanishJM(MainJM plugin){
 		this.plugin = plugin;
 	}
 	
 	@EventHandler(priority = EventPriority.LOW)
 	public void onVanishEvent(VanishStatusChangeEvent e){
 		Player p = e.getPlayer();
 		
 		if (e.isVanishing() == true){
 			String introMessage = plugin.getConfig().getString("introMessage");
 			
 			plugin.getConfig().set("Users." + e.getPlayer().getName() + ".vanished", true);
 			plugin.saveConfig();
 			if (VanishPerms.joinWithoutAnnounce(p) || VanishPerms.joinVanished(p)){
				p.sendMessage(rA(introMessage + " &7You are now omit from sending join and leave messages."));
 				return;
 			}
			p.sendMessage(rA(introMessage + " &7You are now omit from sending leave messages."));
 			return;
 		}
 		String introMessage = plugin.getConfig().getString("introMessage");
 		plugin.getConfig().set("Users", null);
 		plugin.saveConfig();
 		if (VanishPerms.joinWithoutAnnounce(p) || VanishPerms.joinVanished(p)){
			p.sendMessage(rA(introMessage + " &7You are no longer omit from sending join and leave messages."));
 			return;
 		}
		p.sendMessage(rA(introMessage + " &7You are no longer omit from sending leave messages."));
 	}
 }

 package com.fsscripts.bentzilla;
 
import org.bukkit.Location;
 import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 /**
  * Handle events for all Player related events
  * @author bentzilla
  */
 public class HPlayerListener extends PlayerListener {
    private final Health plugin;
     private Permissions HPermissions = null;
 
     public HPlayerListener(Health instance) {
         plugin = instance;
     }
     
     @Override
     public void onPlayerTeleport(PlayerMoveEvent event) {
     	Player player = event.getPlayer();
     	
     	boolean hasUsePermission = this.HPermissions == null ? false : Permissions.Security.permission(player, "hp.use");
     	boolean hasChatPermission = this.HPermissions == null ? false : Permissions.Security.permission(player, "hp.chathealth");
     	boolean hasHealthPermission = this.HPermissions == null ? false : Permissions.Security.permission(player, "hp.nohealth");
     	
     	if (!hasUsePermission) {
 	    	if(Health.hpChatEnabled(player)) {
 	    		Health.chatHealthUsers.remove(player);
 	    		player.sendMessage("Chat in Health DISABLED");
 	    	}
 	    	if(Health.damageHealthEnabled(player)) {
 	    		Health.damageHealthUsers.remove(player);
 	    		player.sendMessage("Health ENABLED");
 	    	}
     	}else {
 	    		if (!hasChatPermission) {
 	    	    	if(Health.hpChatEnabled(player)) {
 	    	    		Health.chatHealthUsers.remove(player);
 	    	    		player.sendMessage("Chat in Health DISABLED");
 	    	    	}
 	    	    if (!hasHealthPermission){
 	    	    	if(Health.damageHealthEnabled(player)) {
 	    	    		Health.damageHealthUsers.remove(player);
 	    	    		player.sendMessage("Health ENABLED");
 	    	    	}
 	    	    }
 	    	}
     	}
     }   
 }

 package com.github.roburrito.bukkit.godly.listeners;
 
 import java.util.List;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import com.github.roburrito.bukkit.godly.Godly;
 import com.github.roburrito.bukkit.util.FavorMetadataMethods;
 
 public class GodlyPlayerListener implements Listener {
 	
 	private final Godly plugin;
 	private FavorMetadataMethods favorMethods;
 	
 	public GodlyPlayerListener (Godly instance) {
 		plugin = instance;
 	}
 
 	@EventHandler
 	public void onPlayerPreLogin(PlayerPreLoginEvent event) {
 		/*For Banning*/
 	}
 	
 	@EventHandler
 	public void onPlayerLogin(PlayerLoginEvent event){
 		/*TODO: Validate the Player's login and apply permissions to the Player.*/
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player player = event.getPlayer();
 		
 		if(plugin.isError()) {
 			player.sendMessage("[Godly Error] Locked in safe mode!");
 			return;
 		}
 		
 		ensureFavorsExist(player);
 	}
 	
 	public void ensureFavorsExist(Player player) {
 		String Godly_Favors[] = favorMethods.godlyFavors;
 		for(String favor : Godly_Favors){
 			if(favorMethods.getMetadata(player, favor) == Integer.MIN_VALUE)
 				favorMethods.setMetadata(player, favor, 0);
 		}
 	}
 	
 }

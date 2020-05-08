 package me.shock.uberhearts;
 
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 public class DeathListener implements Listener 
 {
 
 	public HashMap<String, Long> lives = new HashMap<String, Long>();
	String uber = ChatColor.RED + "[" + ChatColor.WHITE + "UberHearts" + ChatColor.RED + "]" + ChatColor.WHITE + ": ";
 	
     public Main plugin;
 	
 	public DeathListener(Main instance)
 	{
 		this.plugin = instance;
 	}
 	
 	@EventHandler
 	public void onRespawn(PlayerRespawnEvent event)
 	{
 		Player player = event.getPlayer();
 		long maxLives = plugin.getConfig().getLong("max-lives");
 		long currentLives = lives.get(player.getName());
 
 		/**
 		 * Handles when a player has already respawned once.
 		 * Handling it in reverse order I guess.
 		 */
		if(lives.containsKey(player))
 		{
			player.setMaxHealth((int) currentLives - 1);
 		}
 		else
 		{
 			lives.put(player.getName(), maxLives - 1);
 		}
 		
 		player.sendMessage(uber + "You have " + ChatColor.RED + lives.get(player.getName()) + ChatColor.WHITE + " lives left.");
 
 	}
 	
 	@EventHandler
 	public void onJoin(PlayerJoinEvent event)
 	{
 		long maxLives = plugin.getConfig().getLong("max-lives");
 
 		Player player = event.getPlayer();
		lives.put(player.getName(), maxLives);
 		long currentLives = lives.get(player.getName());
		player.sendMessage(uber + "You have " + currentLives + " left.");
 	}
 }

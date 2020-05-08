 package com.lenis0012.bukkit.pvp.listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import com.lenis0012.bukkit.pvp.PvpLevels;
 import com.lenis0012.bukkit.pvp.PvpPlayer;
 
 public class PlayerListener implements Listener {
 	private PvpLevels plugin;
 	
 	public PlayerListener(PvpLevels plugin) {
 		this.plugin = plugin;
 	}
 	
	@EventHandler (priority = EventPriority.MONITOR)
 	public void onPlayerChat(AsyncPlayerChatEvent event) {
 		Player player = event.getPlayer();
 		String name = player.getName();
 		String format = event.getFormat();
 		PvpPlayer pp = new PvpPlayer(name);
 		
 		String lvl = String.valueOf(pp.get("level"));
 		String toReplace = plugin.getConfig().getString("replace-string", "[LEVEL]");
 		if(format.contains(toReplace))
 			format = format.replace(toReplace, lvl);
 		else
 			format = "["+ChatColor.GREEN+"Lvl "+lvl+ChatColor.WHITE+"] "+format;
 		
 		event.setFormat(format);
 	}
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player player = event.getPlayer();
 		String name = player.getName();
 		PvpPlayer pp = new PvpPlayer(name);
 		
 		if(!pp.isCreated())
 			pp.create();
 		else
 			pp.update();
 	}
 	
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		Player player = event.getPlayer();
 		String name = player.getName();
 		PvpPlayer pp = new PvpPlayer(name);
 		
 		if(pp.isCreated())
 			pp.update();
 	}
 }

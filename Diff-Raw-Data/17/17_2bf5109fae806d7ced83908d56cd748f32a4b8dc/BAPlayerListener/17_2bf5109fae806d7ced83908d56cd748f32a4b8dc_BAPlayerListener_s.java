 package com.dechiridas.bitauth.listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.*;
 
 import com.dechiridas.bitauth.BitAuth;
 import com.dechiridas.bitauth.player.BAPlayer;
 import com.dechiridas.bitauth.player.BAState;
 
 public class BAPlayerListener implements Listener {
 	private BitAuth plugin;
 	
 	public BAPlayerListener(BitAuth instance) {
 		this.plugin = instance;
 	}
 	
 	@EventHandler
 	public void onPlayerLogin(PlayerLoginEvent event) {
 		boolean whitelisted = plugin.database.isWhitelisted(event.getPlayer());
 		
 		if (whitelisted == false && plugin.database.whitelistEnabled() == true)
 			event.disallow(null, "You are not whitelisted.");
 		
 		else
 			plugin.database.tryLogin(event.getPlayer(), event);
 	}
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player player = event.getPlayer();
 		BAPlayer ba = plugin.pman.getBAPlayerByName(player.getName());
 		BAState state = ba.getState();
 		boolean pwreset = false;
 		
 		if (state == BAState.LOGGEDIN) {
 			player.sendMessage(ChatColor.GREEN + 
 					"Welcome back, " + event.getPlayer().getName());
 		} else if (state == BAState.LOGGEDOUT) {
 			player.sendMessage(new String[] { 
 					ChatColor.GREEN + "Please log in by typing:",
 					ChatColor.GREEN + "/login <password>"
 			});
 		} else if (state == BAState.REGISTERED) {
 			
 		} else if (state == BAState.UNREGISTERED) {
 			player.sendMessage(new String[] { 
 					ChatColor.GREEN + "Oh no! It appears you haven't registered for our server.", 
 					ChatColor.GREEN + "Not to fret though, you can register by simply typing:",
 					ChatColor.GREEN + "/register <password>" 
 			});
 		} else if (state == BAState.PWRESET) {
 			player.sendMessage(new String[] { 
 					ChatColor.RED + "You need to reset your password! You can do this by typing:",
 					ChatColor.RED + "/login <temp password> <new password>"
 			});
 			pwreset = true;
 		}
 		
 		if (pwreset == true)
 			player.sendMessage(new String[] { 
 					ChatColor.GREEN + "Please log in by typing:",
 					ChatColor.GREEN + "/login <password>"
 			});
 	}
 	
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent event) {
 		Player player = event.getPlayer();
 		BAPlayer ba = plugin.pman.getBAPlayerByName(player.getName());
 		BAState state = ba.getState();
 		
 		if (state == BAState.UNREGISTERED || state == BAState.LOGGEDOUT)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		Player player = event.getPlayer();
 		BAPlayer ba = plugin.pman.getBAPlayerByName(player.getName());
 
 		plugin.pman.removePlayer(ba);
 	}
 	
 	@EventHandler
 	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
 		Player player = event.getPlayer();
 		BAPlayer ba = plugin.pman.getBAPlayerByName(player.getName());
 		BAState state = ba.getState();
 
 		if (state == BAState.UNREGISTERED || state == BAState.LOGGEDOUT)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
 		Player player = event.getPlayer();
 		BAPlayer ba = plugin.pman.getBAPlayerByName(player.getName());
 		BAState state = ba.getState();
 		
 		String[] split = event.getMessage().split(" ");
 		boolean allow = false;
 		
 		if (split[0].equals("/register"))
 			allow = true;
 		if (split[0].equals("/login"))
 			allow = true;
 		
 		if (allow == false)
 			if (state == BAState.UNREGISTERED || state == BAState.LOGGEDOUT)
 				event.setCancelled(true);
 	}
 	
	/*@EventHandler
	public void onPlayerChat(PlayerChatEvent event) {
		Player player = event.getPlayer();
		BAPlayer ba = plugin.pman.getBAPlayerByName(player.getName());
		BAState state = ba.getState();

		if (state == BAState.UNREGISTERED || state == BAState.LOGGEDOUT)
			event.setCancelled(true);
	}*/
	
 	// PlayerChatEvent deprecated, replaced with AsyncPlayerChatEvent
 	@EventHandler
 	public void onPlayerChat(AsyncPlayerChatEvent event) {
 		Player player = event.getPlayer();
 		BAPlayer ba = plugin.pman.getBAPlayerByName(player.getName());
 		BAState state = ba.getState();
 		
 		if (state == BAState.UNREGISTERED || state == BAState.LOGGEDOUT)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void onPlayerInteractEvent(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		BAPlayer ba = plugin.pman.getBAPlayerByName(player.getName());
 		BAState state = ba.getState();
 
 		if (state == BAState.UNREGISTERED || state == BAState.LOGGEDOUT)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
 		Player player = event.getPlayer();
 		BAPlayer ba = plugin.pman.getBAPlayerByName(player.getName());
 		BAState state = ba.getState();
 
 		if (state == BAState.UNREGISTERED || state == BAState.LOGGEDOUT)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void onPlayerExpChangeEvent(PlayerExpChangeEvent event) {
 		Player player = event.getPlayer();
 		BAPlayer ba = plugin.pman.getBAPlayerByName(player.getName());
 		BAState state = ba.getState();
 
 		if (state == BAState.UNREGISTERED || state == BAState.LOGGEDOUT)
 			event.setAmount(0);
 	}
 	
 	@EventHandler
 	public void onPlayerToggleSneakEvent(PlayerToggleSneakEvent event) {
 		Player player = event.getPlayer();
 		BAPlayer ba = plugin.pman.getBAPlayerByName(player.getName());
 		BAState state = ba.getState();
 		
 
 		if (state == BAState.UNREGISTERED || state == BAState.LOGGEDOUT)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void onPlayerToggleSprintEvent(PlayerToggleSprintEvent event) {
 		Player player = event.getPlayer();
 		BAPlayer ba = plugin.pman.getBAPlayerByName(player.getName());
 		BAState state = ba.getState();
 		
 
 		if (state == BAState.UNREGISTERED || state == BAState.LOGGEDOUT)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
 		Player player = event.getPlayer();
 		BAPlayer ba = plugin.pman.getBAPlayerByName(player.getName());
 		BAState state = ba.getState();
 		
 
 		if (state == BAState.UNREGISTERED || state == BAState.LOGGEDOUT)
 			event.setCancelled(true);
 	}
 }

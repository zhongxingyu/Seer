 package eu.icecraft.iceauth;
 
 import org.bukkit.entity.*;
 import org.bukkit.event.player.*;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 
 public class IceAuthPlayerListener extends PlayerListener {
 
 	private IceAuth plugin;
 
 	public IceAuthPlayerListener(IceAuth instance) {
 		plugin = instance;
 	}
 
 	@Override
 	public void onPlayerLogin(PlayerLoginEvent event) {
 
 		if(event.getResult() != Result.ALLOWED || event.getPlayer() == null) {
 			return;
 		}
 
 		Player player = event.getPlayer();
 		String playername = player.getName();
 
 		for(Player p : plugin.getServer().getOnlinePlayers()) {
 
 			if(p.getName().toLowerCase().equals(event.getPlayer().getName().toLowerCase()) && plugin.checkAuth(p)) {
 				event.disallow(Result.KICK_OTHER, "There's an user logged in with that name!");
 			}
 
 		}
 
 		if((!playername.matches("[a-zA-Z0-9_?]*"))
 				|| (playername.length() > 16)
 				|| (playername.length() < 3)
 				|| (playername.equalsIgnoreCase("Notch")) // Enough Notch'es already!
 				|| (playername.equalsIgnoreCase("Player"))) {
 			event.disallow(Result.KICK_OTHER, "Name contained disallowed characters or was Player");
 		}
 	}
 
 	@Override
 	public void onPlayerKick(PlayerKickEvent event) {
 		if(event.isCancelled() || event.getPlayer() == null) {
 			return;
 		}
 		Player player = event.getPlayer();
 
 		if(event.getReason().contains("logged in from another location")) {
 			if(plugin.checkAuth(player)) {
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@Override
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		if(event.getPlayer() == null) {
 			return;
 		}
 		Player player = event.getPlayer();
 
 		boolean regged = plugin.isRegistered(player.getName());

 		if(player.getHealth() <= 0) {
 			player.teleport(player.getWorld().getSpawnLocation());
		} else {
			if(plugin.checkInvEmpty(player.getInventory().getContents()) && !plugin.isInvCacheEmpty(player.getName())) {
				plugin.restoreInv(player, true);
			}
 		}

 		plugin.addPlayerNotLoggedIn(player, player.getLocation(), regged);

 		plugin.msgPlayerLogin(player);
 
 		player.getInventory().clear();
 		player.getInventory().setHelmet(null);
 		player.getInventory().setChestplate(null);
 		player.getInventory().setLeggings(null);
 		player.getInventory().setBoots(null);
 
 	}

 	/*
 	@Override
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		if(event.getPlayer() == null) {
 			return;
 		}
 		Player player = event.getPlayer();

 		if(!plugin.checkAuth(player)) {
 			plugin.restoreInv(player);
 		}
 	}
	 */

 	@Override
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		if(event.getPlayer() == null) {
 			return;
 		}
 		Player player = event.getPlayer();

 		plugin.removePlayerCache(player);
 
 	}
 
 	@Override
 	public void onPlayerChat(PlayerChatEvent event) {
 		if(event.isCancelled() || event.getPlayer() == null) {
 			return;
 		}
 		Player player = event.getPlayer();
 
 		if(!plugin.checkAuth(player)) {
 			plugin.msgPlayerLogin(player);
 			event.setMessage("");
 			event.setCancelled(true);
 		}
 	}
 
 	@Override
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
 		if(event.isCancelled() || event.getPlayer() == null) {
 			return;
 		}
 		Player player = event.getPlayer();
 		String commandLabel = event.getMessage().split(" ")[0];
 
 		if(plugin.checkAuth(player)) {
 			return;
 		}
 
 		if(commandLabel.equalsIgnoreCase("/register")) {
 			return;
 		}
 
 		if(commandLabel.equalsIgnoreCase("/login")) {
 			return;
 		}
 
 		if(commandLabel.equalsIgnoreCase("/l")) {
 			return;
 		}
 		// Not sure this is needed
 		//if(event.getMessage().equals("/0.1.3") || event.getMessage().equals(
 		//		"/0.1.6") || event.getMessage().equals("/0.1.7")) {
 		//	return;
 		//}
 
 		plugin.msgPlayerLogin(player);
 
 		event.setMessage("/notloggedin");
 		event.setCancelled(true);
 	}
 
 	@Override
 	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
 		if(event.isCancelled() || event.getPlayer() == null) {
 			return;
 		}
 		Player player = event.getPlayer();
 		if(!plugin.checkAuth(player)) {
 			event.setCancelled(true);
 		}
 	}
 
 	@Override
 	public void onPlayerDropItem(PlayerDropItemEvent event) {
 		if(event.isCancelled() || event.getPlayer() == null) {
 			return;
 		}
 		Player player = event.getPlayer();
 		if(!plugin.checkAuth(player)) {
 			event.setCancelled(true);
 		}
 	}
 
 	@Override
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if(event.isCancelled() || event.getPlayer() == null) {
 			return;
 		}
 		Player player = event.getPlayer();
 		if(!plugin.checkAuth(player)) {
 			event.setCancelled(true);
 		}
 	}
 
 }

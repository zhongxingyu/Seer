 package eu.icecraft.iceauth;
 
 import org.bukkit.GameMode;
 import org.bukkit.entity.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.player.*;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 
 public class IceAuthPlayerListener implements Listener {
 	private IceAuth plugin;
 
 	public IceAuthPlayerListener(IceAuth instance) {
 		plugin = instance;
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerLoginEarly(PlayerLoginEvent event) {
 		if((event.getResult() != Result.ALLOWED && event.getResult() != Result.KICK_FULL) || event.getPlayer() == null) return;
 		Player player = event.getPlayer();
 		String playername = player.getName();
 
 		for(Player p : plugin.getServer().getOnlinePlayers()) {
 			if(p.getName().equalsIgnoreCase(playername)) {
 				if(!plugin.checkAuth(p)) {
 					p.kickPlayer("You logged in from another location.");
 					System.out.println("[IceAuth] Duplicate player name for " + event.getPlayer().getName().toLowerCase() + ", kicked not logged in player.");
 				} else {
 					event.disallow(Result.KICK_OTHER, "There's an user logged in with that name!");
 					System.out.println("[IceAuth] Cancelled early login event, duplicate name for " + event.getPlayer().getName().toLowerCase());	
 				}
 			}
 		}
 
 		if((!playername.matches("[a-zA-Z0-9_?]*"))
 				|| (playername.length() > 16)
 				|| (playername.length() < 3)
 				|| (playername.equalsIgnoreCase("Notch")) // Enough Notch'es already!
 				|| (playername.equalsIgnoreCase("Player"))) {
 			event.disallow(Result.KICK_OTHER, "Name contained disallowed characters or was Player/Notch");
 			System.out.println("[IceAuth] Cancelled early login event, bad name for " + playername);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerKick(PlayerKickEvent event) {
 		if(event.isCancelled() || event.getPlayer() == null) return;
 		Player player = event.getPlayer();
 
 		if(event.getReason().contains("logged in from another location")) {
 			if(plugin.checkAuth(player)) {
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		if(event.getPlayer() == null) return;
 		Player player = event.getPlayer();
 
 		boolean regged = plugin.isRegistered(player.getName());
 
 		if(player.getHealth() <= 0) {
 			player.teleport(player.getWorld().getSpawnLocation());
 		} else {
 			if(plugin.checkInvEmpty(player.getInventory().getContents()) && !plugin.isInvCacheEmpty(player.getName())) {
 				plugin.restoreInv(player, true);
 			}
 		}
 
 		if(!regged) {
 			if(plugin.firstSpawn != null) player.teleport(plugin.firstSpawn);
 			if(plugin.giveKits) plugin.giveKits(player);
 		}
 
 		plugin.addPlayerNotLoggedIn(player, player.getLocation(), regged);
 
 		plugin.msgPlayerLogin(player, true);
 
 		player.setGameMode(GameMode.SURVIVAL);
 
 		player.getInventory().clear();
 		player.getInventory().setHelmet(null);
 		player.getInventory().setChestplate(null);
 		player.getInventory().setLeggings(null);
 		player.getInventory().setBoots(null);
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		if(event.getPlayer() == null) {
 			return;
 		}
 		Player player = event.getPlayer();
 
 		plugin.removePlayerCache(player);
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerChat(PlayerChatEvent event) {
 		if(event.isCancelled() || event.getPlayer() == null) return;
 		Player player = event.getPlayer();
 
 		if(!plugin.checkAuth(player)) {
 			plugin.msgPlayerLogin(player);
 			event.setMessage("");
 			event.setCancelled(true);
 			return;
 		}
 
 		if(plugin.hideChatNonLogged) {
			Player[] recipients = event.getRecipients().toArray(new Player[0]);
			for(int r = 0; r < recipients.length; r++) {
				Player p = recipients[r];
 				if(!plugin.checkAuth(p)) event.getRecipients().remove(p);
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
 		if(event.isCancelled() || event.getPlayer() == null) return;
 		Player player = event.getPlayer();
 		String commandLabel = event.getMessage().split(" ")[0];
 
 		if(plugin.checkAuth(player)) {
 			return;
 		}
		if(commandLabel.equalsIgnoreCase("/register") ||
				commandLabel.equalsIgnoreCase("/login") ||
				commandLabel.equalsIgnoreCase("/l")) {
 			return;
 		}
 
 		plugin.msgPlayerLogin(player);
 
 		event.setMessage("/notloggedin");
 		event.setCancelled(true);
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
 		if(event.isCancelled() || event.getPlayer() == null) return;
 		Player player = event.getPlayer();
 		if(!plugin.checkAuth(player)) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerDropItem(PlayerDropItemEvent event) {
 		if(event.isCancelled() || event.getPlayer() == null) return;
 
 		Player player = event.getPlayer();
 		if(!plugin.checkAuth(player)) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if(event.isCancelled() || event.getPlayer() == null) return;
 		Player player = event.getPlayer();
 		if(!plugin.checkAuth(player)) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
 		if (event.isCancelled() || event.getPlayer() == null) return;
 		Player player = event.getPlayer();
 		if (!plugin.checkAuth(player)) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onFoodLevelChange(FoodLevelChangeEvent event) {
 		if (event.isCancelled() || !(event.getEntity() instanceof Player)) return;
 		Player player = (Player) event.getEntity();
 		if (!plugin.checkAuth(player)) {
 			event.setCancelled(true);
 		}
 	}
 }

 package net.kiwz.ThePlugin.listeners;
 
 import java.util.logging.Logger;
 
 import net.kiwz.ThePlugin.ThePlugin;
 import net.kiwz.ThePlugin.utils.HandlePlaces;
 import net.kiwz.ThePlugin.utils.HandlePlayers;
 import net.kiwz.ThePlugin.utils.HandleWorlds;
 import net.kiwz.ThePlugin.utils.MsgToOthers;
 import net.kiwz.ThePlugin.utils.OfflinePlayer;
 import net.kiwz.ThePlugin.utils.Permissions;
 import net.kiwz.ThePlugin.utils.Tablist;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.help.HelpTopic;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 
 public class PlayerListener implements Listener {
 	private HandlePlaces places = new HandlePlaces();
 	private HandlePlayers players = new HandlePlayers();
 	private HandleWorlds worlds = new HandleWorlds();
     private MsgToOthers msg = new MsgToOthers();
     private Permissions perm = new Permissions();
 	private String denyString = ThePlugin.c2 + "Du har ingen tilgang her";
 	
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
 
 		if (!worlds.isTrample(block.getWorld())) {
 			if ((block.getType() == Material.SOIL) || (block.getType() == Material.CROPS)) {
 				event.setCancelled(true);
 				return;
 			}
 		}
 		
 		if (event.getClickedBlock() != null) {
 			if (!places.hasAccess(player, event.getClickedBlock().getLocation())) {
 				event.setCancelled(true);
 				player.sendMessage(denyString);
 				return;
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
 		Player player = event.getPlayer();
 		
 		if (event.getRightClicked() != null) {
 			if (!places.hasAccess(player, event.getRightClicked().getLocation())) {
 				event.setCancelled(true);
 				player.sendMessage(denyString);
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
     public void onInventoryClose(InventoryCloseEvent event) {
 		Inventory inv = event.getView().getTopInventory();
 		String invType = inv.getType().toString();
 		if (invType == "PLAYER") {
 			Player holder = (Player) inv.getHolder();
 			if (!holder.isOnline()) {
 				ItemStack[] content = inv.getHolder().getInventory().getContents();
 				OfflinePlayer offlinePlayer = new OfflinePlayer();
 				Player player = offlinePlayer.getPlayer(holder.getName());
 				player.getInventory().setContents(content);
 				player.saveData();
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
 		if (!event.isCancelled()) {
 			Player player = event.getPlayer();
 			String cmd = event.getMessage().split(" ")[0];
 	    	HelpTopic topic = Bukkit.getServer().getHelpMap().getHelpTopic(cmd);
 	    	if (topic == null) {
 				player.sendMessage(ThePlugin.c2 + cmd + " finnes ikke, skriv \"/hjelp\" for hjelp");
 				event.setCancelled(true);
 	    	}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerChat(AsyncPlayerChatEvent event) {
 		//event.setFormat("%s: %s");
 		Player player = event.getPlayer();
 		String msg = player.getName() + ": " + event.getMessage();
 		
 		if (players.isMuted(player.getName())) {
 			player.sendMessage(ThePlugin.c2 + "En admin har bestemt at du ikke fr snakke av gode grunner");
 			event.setCancelled(true);
 			return;
 		}
 		if (player.isOp() || perm.isAdmin(player)) {
 			//event.setFormat(ChatColor.RED + "%s: " + ChatColor.WHITE + "%s");
 			msg = ChatColor.RED + player.getName() + ": " + ChatColor.WHITE + event.getMessage();
 		}
 		for (Player thisPlayer : Bukkit.getServer().getOnlinePlayers()) {
 			thisPlayer.sendMessage(msg);
 		}
 		event.setCancelled(true);
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		event.setKeepLevel(true);
 		event.setDroppedExp(0);
 		event.setDeathMessage("");
 		Location loc = event.getEntity().getLocation();
 		String world = loc.getWorld().getName();
 		String x = Double.toString(loc.getX()).replaceAll("\\..*","");
 		String y = Double.toString(loc.getY()).replaceAll("\\..*","");
 		String z = Double.toString(loc.getZ()).replaceAll("\\..*","");
 		event.getEntity().getPlayer().sendMessage(ThePlugin.c2 + "Du dde i " + world
 				+ " X: " + x + " Y: " + y + " Z: " + z);
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		Plugin pl = Bukkit.getServer().getPluginManager().getPlugin("ThePlugin");
 		final Player player = event.getPlayer();
 		final Location loc = worlds.getSpawn(player, player.getWorld().getName());
 		if (player.getBedSpawnLocation() == null) {
 			Bukkit.getScheduler().scheduleSyncDelayedTask(pl, new Runnable() {
 					@Override
 	                public void run() {
 					player.teleport(loc);
 				}
 			}, 1);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerJoin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
         String playerName = player.getName();
         String ip = player.getAddress().toString();
         String worldName = player.getWorld().getName();
         String coords = player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ();
         String log = "Spiller logget inn";
         String loginMsg = "Noen logget inn";
         event.setJoinMessage("");
         new Tablist().setColor(player);
         
     	if (perm.isAdmin(player)) {
     		perm.setPermissions(player);
     	}
     	
         if (!player.isOp()) {
         	player.setGameMode(GameMode.SURVIVAL);
         	player.setAllowFlight(false);
         }
         
         if (players.hasPlayedBefore(playerName)) {
 	        players.setLastLogin(playerName);
 	        players.setIP(playerName);
 	        log = playerName + " [" + ip + "] logget inn ([" + worldName + "] " + coords + ")";
 			loginMsg = ThePlugin.c3 + playerName + " logget inn";
         }
         
         else {
         	players.addPlayer(playerName);
 	        log = playerName + " [" + ip + "] logget inn for frste gang ([" + worldName + "] " + coords + ")";
 			loginMsg = ThePlugin.c3 + playerName + " logget inn for frste gang";
 			Location loc = worlds.getSpawn(player, player.getWorld().getName());
 			player.teleport(loc);
         }
         
         Logger.getLogger("Minecraft-Server").info(log);
 		msg.sendMessage(player, loginMsg);
         player.sendMessage(ThePlugin.c1 + "Velkommen til LarvikGaming");
         player.sendMessage(ThePlugin.c1 + "Besk vr hjemmeside: http://larvikgaming.net");
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void OnPlayerQuit(PlayerQuitEvent event) {
         players.setTimePlayed(event.getPlayer().getName());
 		event.setQuitMessage(ThePlugin.c2 + event.getPlayer().getName() + " logget ut");
		/*for (Player players : Bukkit.getServer().getOnlinePlayers()) {
 			players.getLocation().getWorld().strikeLightningEffect(players.getLocation());
		}*/
 	}
 }

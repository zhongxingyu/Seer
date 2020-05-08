 package me.coolblinger.swordsgame.listeners;
 
 import me.coolblinger.swordsgame.SwordsGame;
 import me.coolblinger.swordsgame.SwordsGameCommand;
 import me.coolblinger.swordsgame.classes.SwordsGameLobbyClass;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.*;
 import org.bukkit.scheduler.BukkitScheduler;
 import org.bukkit.util.Vector;
 
 import java.util.Iterator;
 import java.util.List;
 
 public class SwordsGamePlayerListener extends PlayerListener {
 	SwordsGame plugin;
 
 	public SwordsGamePlayerListener(SwordsGame instance) {
 		plugin = instance;
 	}
 
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		if (plugin.players.containsKey(event.getPlayer())) {
 			plugin.players.get(event.getPlayer()).restore();
			plugin.updateLobbySigns();
 		}
 	}
 
 	public void onPlayerMove(PlayerMoveEvent event) {
 		if (event.getFrom() != event.getTo()) {
 			Player player = event.getPlayer();
 			if (plugin.players.containsKey(player)) {
 				if (plugin.players.get(player).noMovement) {
 					event.setCancelled(true);
 					return;
 				}
 			}
 			if (plugin.getArena(event.getTo().subtract(0.5, 0, 0.5).toVector()) == null && plugin.players.containsKey(player)) {
 				plugin.games.get(plugin.players.get(player).arena).toSpawn(player, true);
 				plugin.players.get(player).noMovement = true;
 				BukkitScheduler bScheduler = plugin.getServer().getScheduler();
 				final Player finalPlayer = player;
 				bScheduler.scheduleAsyncDelayedTask(plugin, new Runnable() {
 					@Override
 					public void run() {
 						plugin.players.get(finalPlayer).noMovement = false;
 					}
 				}, 2);
 			}
 			for (SwordsGameLobbyClass lobby : plugin.lobbies.values()) { // I'm not sure how laggy this will be or how to do this more efficient.
 				Vector vector = new Vector(lobby.portX, lobby.portY, lobby.portZ);
 				if (event.getTo().subtract(0.5, 0, 0.5).getBlock() == vector.toLocation(plugin.toWorld(lobby.world)).subtract(0.5, 0, 0.5).getBlock()) {
 					SwordsGameCommand command = new SwordsGameCommand(plugin);
 					String[] args = new String[2];
 					args[0] = "game";
 					args[1] = lobby.arena;
 					command.game(player, args);
 					Vector teleportBack = new Vector(lobby.cornerX[0] + 1.5, lobby.cornerY[0] + 1, lobby.cornerZ[0] - 0.5);
 					if (plugin.players.containsKey(player)) {
 						plugin.players.get(player).location = teleportBack.toLocation(plugin.toWorld(lobby.world));
 					}
 					plugin.players.get(player).noMovement = true;
 					BukkitScheduler bScheduler = plugin.getServer().getScheduler();
 					final Player finalPlayer = player;
 					bScheduler.scheduleAsyncDelayedTask(plugin, new Runnable() {
 						@Override
 						public void run() {
 							plugin.players.get(finalPlayer).noMovement = false;
 						}
 					}, 2);
 				}
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
 		String[] split = event.getMessage().split(" ");
 		if (plugin.players.containsKey(event.getPlayer())) {
 			List<String> allowedCommands = plugin.config.readList("allowCommands");
 			allowedCommands.add("/sg");
 			allowedCommands.add("/swordsgame");
 			if (containsIgnoreCase(allowedCommands, split[0])) {
 
 			} else {
 				event.getPlayer().sendMessage(ChatColor.RED + plugin.local("errors.command.notAllowedInGame"));
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		if (plugin.define.containsKey(player)) {
 			if (plugin.define.get(player).mode == "define") {
 				if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 					if (event.getClickedBlock() != null) {
 						Vector vector = event.getClickedBlock().getLocation().toVector();
 						if (plugin.define.get(player).setCorner(vector, player.getWorld(), player)) {
 							player.sendMessage(ChatColor.GREEN + plugin.local("defining.defining.secondCorner") + ChatColor.WHITE + vector.toString() + ChatColor.GREEN + ".");
 						} else {
 							player.sendMessage(ChatColor.GREEN + plugin.local("defining.defining.firstCorner") + ChatColor.WHITE + vector.toString() + ChatColor.GREEN + ".");
 						}
 					}
 				} else {
 					plugin.define.remove(player);
 					player.sendMessage(ChatColor.RED + plugin.local("defining.defining.canceled"));
 				}
 			} else if (plugin.define.get(player).mode == "setspawns") {
 				if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 					if (event.getClickedBlock() != null) {
 						Vector vector = event.getClickedBlock().getLocation().toVector();
 						String arenaName = plugin.getArena(vector);
 						if (arenaName != null) {
 							int setSpawns = plugin.arenas.get(arenaName).setSpawns(vector);
 							if (setSpawns != 0) {
 								player.sendMessage(ChatColor.GREEN + plugin.local("defining.settingSpawns.set") + ChatColor.WHITE + setSpawns + ChatColor.GREEN + plugin.local("defining.list.outOf") + ChatColor.WHITE + "4" + ChatColor.GREEN + plugin.local("defining.settingSpawns.for") + "arena '" + ChatColor.WHITE + arenaName + ChatColor.GREEN + "'.");
 							} else {
 								player.sendMessage(ChatColor.RED + plugin.local("errors.settingSpawns.alreadyFourSpawns"));
 							}
 						} else {
 							player.sendMessage(plugin.local("errors.settingSpawns.inValid"));
 						}
 					}
 				} else {
 					plugin.define.remove(player);
 					player.sendMessage(ChatColor.RED + plugin.local("defining.settingSpawns.canceled"));
 				}
 			}
 		}
 	}
 
 	public boolean containsIgnoreCase(List<String> list, String string) {
 		Iterator<String> it = list.iterator();
 		while (it.hasNext()) {
 			if (it.next().equalsIgnoreCase(string))
 				return true;
 		}
 		return false;
 	}
 }

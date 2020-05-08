 package me.limebyte.battlenight.core.Listeners;
 
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.Other.Util;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 
 public class DeathListener implements Listener {
 	
 	// Get Main Class
 	public static BattleNight plugin;
 	public DeathListener(BattleNight instance) {
 		plugin = instance;
 	}
 	
 	private Util util;
 	
 	// Called when player dies
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityDeath(EntityDeathEvent event) {
 		Entity e = event.getEntity();
 		if(e instanceof Player) {
 			Player player = (Player)e;
 			String name = player.getName();
 			if (plugin.BattleUsersTeam.containsKey(name)) {
 				event.getDrops().clear();
 				((PlayerDeathEvent) event).setDeathMessage("");
 				if(!plugin.playersInLounge){
 					try {
 						Player killer = player.getKiller();
 						String playerName;
 						String killerName;
 						
 						// Colour Names
 						if (util.isInTeam(killer, "blue")) 		killerName = ChatColor.BLUE + killer.getName();
 						else if (util.isInTeam(killer, "red"))  killerName = ChatColor.RED + killer.getName();
 						else 									killerName = ChatColor.BLACK + killer.getName();
 						
 						if (util.isInTeam(player, "blue")) 		playerName = ChatColor.BLUE + player.getName();
 						else if (util.isInTeam(player, "red"))  playerName = ChatColor.RED + player.getName();
 						else 									playerName = ChatColor.BLACK + player.getName();
 						// ------------
 						
 						plugin.killFeed(killerName + ChatColor.GRAY + " killed " + playerName + ".");
 						plugin.BattleUsersRespawn.put(name, "true");
						plugin.removePlayer(player, null, "You have been removed from the Battle because you were killed.");
 					} catch (NullPointerException error) {
 						plugin.killFeed(ChatColor.RED + name + ChatColor.GRAY + " was killed.");
 						plugin.BattleUsersRespawn.put(name, "true");
						plugin.removePlayer(player, null, "You have been removed from the Battle because you were killed.");
 						if(plugin.configDebug){
 							BattleNight.log.info("[BattleNight] Could not find killer for player: " + name);
 						}
 					}
 				}
 				else if(plugin.playersInLounge){
 					plugin.removePlayer(player, "has been removed from the Battle because they were killed in a lounge.", "You have been removed from the Battle because you were killed in a lounge.");
 					plugin.BattleUsersRespawn.put(name, "true");
 				}
 			}
 		}
 	}
 }

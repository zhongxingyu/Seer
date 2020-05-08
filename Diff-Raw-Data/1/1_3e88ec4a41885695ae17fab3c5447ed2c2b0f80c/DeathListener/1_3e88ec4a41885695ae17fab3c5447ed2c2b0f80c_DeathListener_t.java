 package me.limebyte.battlenight.core.Listeners;
 
 import me.limebyte.battlenight.core.BattleNight;
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
 						if (isInTeam(killer, "blue")) 		killerName = ChatColor.BLUE + killer.getName();
 						else if (isInTeam(killer, "red"))  killerName = ChatColor.RED + killer.getName();
 						else 									killerName = ChatColor.BLACK + killer.getName();
 						
 						if (isInTeam(player, "blue")) 		playerName = ChatColor.BLUE + player.getName();
 						else if (isInTeam(player, "red"))  playerName = ChatColor.RED + player.getName();
 						else 									playerName = ChatColor.BLACK + player.getName();
 						// ------------
 						
 						plugin.killFeed(killerName + ChatColor.GRAY + " killed " + playerName + ".");
 						plugin.BattleUsersRespawn.put(name, "true");
 					} catch (NullPointerException error) {
 						plugin.killFeed(ChatColor.RED + name + ChatColor.GRAY + " was killed.");
 						plugin.BattleUsersRespawn.put(name, "true");
 						if(plugin.configDebug){
 							BattleNight.log.info("[BattleNight] Could not find killer for player: " + name);
 						}
 					}
 				}
 				else if(plugin.playersInLounge){
 					plugin.BattleUsersRespawn.put(name, "true");
 				}
 			}
 		}
 	}
 	
 	private boolean isInTeam(Player player, String team) {
 		if (plugin.BattleUsersTeam.containsKey(player.getName())) {
 			if ((plugin.BattleUsersTeam.get(player.getName()) == team)) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 		else {
 			return false;
 		}
 	}
 }

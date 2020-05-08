 package me.hunterboerner.war;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 public class WarListener implements Listener {
 
 	private War plugin;
 	private Map<Player,Set<Player>> movers	=	Collections.synchronizedMap(new HashMap<Player,Set<Player>>());
 	private int warSight					=	64;
 	
 	public WarListener(War plugin){
 		this.plugin	=	plugin;
 	}
 	
 	@EventHandler
 	public void onPlayerDeath(EntityDeathEvent event){
 		if(event instanceof PlayerDeathEvent){
 			Player victim 	= 	(Player) event.getEntity();
 			Player killer	=	victim.getKiller();
 			if(plugin.areAtWar(victim, killer)){
 				((PlayerDeathEvent) event).setDeathMessage(ChatColor.DARK_PURPLE + "War has claimed another life. "+ChatColor.WHITE+victim.getName()+ChatColor.DARK_PURPLE + " was slain by "+ChatColor.WHITE + killer.getName()+ChatColor.DARK_PURPLE + ".");
				plugin.removeWar(victim, killer);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent event){
 		Iterator<Entity> itr	=	event.getPlayer().getNearbyEntities(warSight, warSight, warSight).iterator();
 		while(itr.hasNext()){
 			Entity entity	=	itr.next();
 			if(entity instanceof Player){
 				
 				if(plugin.isAtWarWith(event.getPlayer(), (Player) entity)){
 					//The moving player is within range of someone with whom they have declared war
 					if(!isAround(event.getPlayer(),(Player) entity)){
 						//The moving player was not already around the entity
 						addNearby(event.getPlayer(),(Player) entity);
 						event.getPlayer().sendMessage(ChatColor.DARK_PURPLE+"[WAR] "+ChatColor.WHITE+" You are within range of an enemy! "+((Player) entity).getName());
 					}
 					
 				}
 				
 				if(plugin.isAtWarWith((Player) entity, event.getPlayer())){
 					//The moving player is within range of someone whom has declared war upon them
 					if(!isAround((Player) entity, event.getPlayer())){
 						//The entity was not already around the moving player
 						addNearby((Player) entity,event.getPlayer());
 						((Player) entity).sendMessage(ChatColor.DARK_PURPLE+"[WAR] "+ChatColor.WHITE+" An enemy has come within range! "+event.getPlayer().getName());
 					}
 					
 				}
 			}
 		}
 		if (movers.get(event.getPlayer()) != null){
 			Iterator<Player> pitr	=	movers.get(event.getPlayer()).iterator();
 			while(pitr.hasNext()){
 				Player wasNearby	=	pitr.next();
 				if(!event.getPlayer().getNearbyEntities(warSight, warSight, warSight).contains(wasNearby) && plugin.isAtWarWith(event.getPlayer(), wasNearby)){
 					event.getPlayer().sendMessage(ChatColor.DARK_PURPLE+"[WAR] "+ChatColor.WHITE+" You are no longer in range of an enemy! "+wasNearby.getName());
 					removeNearby(event.getPlayer(),wasNearby);
 				}else if(!plugin.isAtWarWith(event.getPlayer(),wasNearby)){
 					removeNearby(event.getPlayer(),wasNearby);
 				}
 			}
 		}
 	}
 	private boolean isAround(Player player, Player target){
 		boolean hasTarget	=	false;
 		if(movers.containsKey(player)){
 			hasTarget	=	movers.get(player).contains(target);
 		}
 		return hasTarget;
 	}
 	
 	private void addNearby(Player player,Player target){
 		if(!movers.containsKey(player)){
 			movers.put(player, Collections.synchronizedSet(new HashSet<Player>()));
 		}
 		movers.get(player).add(target);
 	}
 	
 	private void removeNearby(Player player, Player target){
 		if(movers.containsKey(player)){
 			movers.get(player).remove(target);
 		}
 	}
 }

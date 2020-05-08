 package me.ellbristow.greylistVote;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 
 public class greyPlayerListener implements Listener {
 	
 	public static greylistVote plugin;
 	
 	public greyPlayerListener (greylistVote instance) {
 		plugin = instance;
 	}
 	
 	@EventHandler (priority = EventPriority.NORMAL)
 	public void onPlayerLogin (PlayerLoginEvent event) {
 		Player player = event.getPlayer();
 		String voteList = plugin.usersConfig.getString(player.getName().toLowerCase() + ".votes", null);
 		String griefList = plugin.usersConfig.getString(player.getName().toLowerCase() + ".griefer", null);
 		int rep = 0;
 		boolean forceApprove = false;
 		String[] voteArray;
 		String[] griefArray;
 		if (voteList != null) {
 			voteArray = voteList.split(",");
 			for (String vote : voteArray) {
 				rep++;
 				if (vote.equals("Server") || player.hasPermission("greylistvote.approved")) {
 					rep = plugin.approvalVotes;
 					forceApprove = true;
 				}
 			}
 		}
 		if (griefList != null && !forceApprove) {
 			griefArray = griefList.split(",");
 			for (String vote : griefArray) {
 				rep--;
 				if (vote.equals("Server")) {
 					rep = -1;
 				}
 			}
 		}
 		if (rep >= plugin.approvalVotes || player.hasPermission("greylistvote.approved")) {
 			player.addAttachment(plugin, "greylistvote.build", true);
 		}
 		else if (rep < plugin.approvalVotes && !player.hasPermission("greylistvote.approved")) {
 			player.addAttachment(plugin, "greylistvote.build", false);
 		}
 	}
 	
 	@EventHandler (priority = EventPriority.NORMAL)
 	public void onPlayerPvP (EntityDamageEvent event) {
 		if (!event.isCancelled() && plugin.noPVP) {
 			// Stop PvP for guests
 			if (event instanceof EntityDamageByEntityEvent) {
 				EntityDamageByEntityEvent pvpEvent = (EntityDamageByEntityEvent)event;
 				Entity entity = pvpEvent.getEntity();
 				Entity damager = pvpEvent.getDamager();
 				if (entity instanceof Player && damager instanceof Player) {
 					// Player is damaged by player
 					Player player = (Player) damager;
                                         Player target = (Player) entity;
 					if (!player.hasPermission("greylistvote.approved") || !target.hasPermission("greylistvote.approved")) {
 						// Player not allowed to PvP, Cancel event
 						event.setCancelled(true);
 					}
				} else if (damager instanceof Projectile) {
                                     LivingEntity shooter = ((Projectile)damager).getShooter();
                                     if (shooter instanceof Player) {
                                         Player player = (Player)entity;
                                         if (!player.hasPermission("greylistvote.approved")) {
                                             // Player not approved, protect from Projectile
                                             ((Projectile)damager).setBounce(true);
                                             event.setCancelled(true);
                                         }
                                     }
                                 }
 			}
 		}
 	}
 }

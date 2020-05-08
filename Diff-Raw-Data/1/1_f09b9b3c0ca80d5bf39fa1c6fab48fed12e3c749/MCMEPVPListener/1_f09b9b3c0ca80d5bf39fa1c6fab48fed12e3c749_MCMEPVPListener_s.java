 package at.doebi;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.inventory.InventoryType.SlotType;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class MCMEPVPListener implements Listener{
 	
 	public MCMEPVPListener(MCMEPVP instance) {
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	void onPlayerJoin(final PlayerLoginEvent event){
 		MCMEPVP.addTeam(event.getPlayer(),"spectator");
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	void onPlayerLeave(final PlayerQuitEvent event){
 		MCMEPVP.removeTeam(event.getPlayer());
 		//TODO check if every online player is participating and start if true
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	void onPlayerChat(final PlayerChatEvent event){
 		if(event.isCancelled()){
 			
 		}else{
 			MCMEPVP.sendToTeam(event.getMessage(), event.getPlayer());
 			event.setCancelled(true);	
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	void onPlayerDeath(final PlayerDeathEvent event){
 		event.getDrops().clear();
 		if(MCMEPVP.GameStatus == 1){
 			Player player = event.getEntity();
 			String Team = MCMEPVP.PlayerTeams.get(player.getName());
 			if(Team == "spectator"){
 				event.setDeathMessage(ChatColor.YELLOW + "Spectator " + player.getName() + " was tired watching this fight!");
 			}
 			if(Team == "red"){
 				event.setDeathMessage(ChatColor.RED + "Team Red " + ChatColor.YELLOW + "lost " + player.getName());
 				event.getDrops().add(new ItemStack(364, 1));
 			}
 			if(Team =="blue"){
 				event.setDeathMessage(ChatColor.BLUE + "Team Blue " + ChatColor.YELLOW + "lost " + player.getName());
 				event.getDrops().add(new ItemStack(364, 1));
 			}
 			MCMEPVP.removeTeam(event.getEntity());
 			if(MCMEPVP.BlueMates == 0){
 				Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "Team " + ChatColor.RED + "Red" + ChatColor.GREEN + " wins!");
 				MCMEPVP.resetGame();
 			}else if(MCMEPVP.RedMates == 0){
 				Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "Team " + ChatColor.BLUE + "Blue" + ChatColor.GREEN + " wins!");
 				MCMEPVP.resetGame();
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	void onPlayerDamage(final EntityDamageByEntityEvent event){
 		if(event.getDamager().getType().equals(EntityType.PLAYER) && event.getEntity().getType().equals(EntityType.PLAYER)){
 			Player Attacker = (Player) event.getDamager();
 			Player Victim = (Player) event.getEntity();
 		    String AttackerTeam = MCMEPVP.PlayerTeams.get(Attacker.getName());
 		    String VictimTeam = MCMEPVP.PlayerTeams.get(Victim.getName());
 		    if(AttackerTeam != "spectator" && VictimTeam != "spectator" && AttackerTeam != "participant" && VictimTeam != "participant" && AttackerTeam != VictimTeam){
 		    	//Victim got attacked by Attacker and both are in rivaling Teams
 		    }else{
 		    	//Either friendly fire or Spectator or Participant involved in fight
 		    	Attacker.sendMessage(ChatColor.DARK_RED + "You can't attack " + Victim.getName() + "!");
 		    	event.setCancelled(true);
 		    }
 		}
 		//hopefully this prevents players from shooting allies
 		if(event.getCause() == DamageCause.PROJECTILE) {
 		    Arrow a = (Arrow) event.getDamager();
 		    if(a.getShooter() instanceof Player) {
 		        Player Attacker = (Player) a.getShooter();
 				if(event.getDamager().getType().equals(EntityType.PLAYER) && event.getEntity().getType().equals(EntityType.PLAYER)){
 					Player Victim = (Player) event.getEntity();
 				    String AttackerTeam = MCMEPVP.PlayerTeams.get(Attacker.getName());
 				    String VictimTeam = MCMEPVP.PlayerTeams.get(Victim.getName());
 				    if(AttackerTeam != "spectator" && VictimTeam != "spectator" && AttackerTeam != "participant" && VictimTeam != "participant" && AttackerTeam != VictimTeam){
 				    	//Victim got attacked by Attacker and both are in rivaling Teams
 				    }else{
 				    	//Either friendly fire or Spectator or Participant involved in fight
 				    	Attacker.sendMessage(ChatColor.DARK_RED + "You can't attack " + Victim.getName() + "!");
 				    	event.setCancelled(true);
 				    }
 				}
 		    }
 		}
 	}
 	//TODO prevent player from taking off head
 	public void onInventoryClick (InventoryClickEvent event) {
 	    HumanEntity player = event.getWhoClicked();
 	    if (event.getSlotType() == SlotType.ARMOR) {
 	        if (player.getInventory().getHelmet().getType() != Material.AIR) {
 	        	//TODO send message to player on armor remove attempt
 	            event.setCancelled(true);
 	        }
 	    }
 	}
 }

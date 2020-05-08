 package com.CC.Listeners;
 
 import com.CC.Arenas.Game;
 import com.CC.Arenas.GameManager;
 import com.CC.Arenas.Team;
 import com.CC.General.User;
 import com.CC.General.UserManager;
 import com.CC.General.onStartup;
 import static org.bukkit.ChatColor.*;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class GameMechanicsListener implements Listener{
 	
 private GameManager gamemanager;
 private onStartup plugin;
 private Game playergame;
 private UserManager usermanager;
     public GameMechanicsListener(onStartup instance) {
     	plugin = instance;
     	gamemanager = plugin.getGameManager();
     	usermanager = plugin.getUserManager();
 	}
 	
     //Updates player's death
     @EventHandler
     public void onDeath(EntityDamageByEntityEvent event){
     	if(event.getEntity() instanceof Player){
     		if(event.getDamager() instanceof Player){
     			User killer = usermanager.getUser((Player)event.getDamager());
     			killer.addKill();
     			usermanager.updatePlayer((Player)event.getEntity(), "stats");
     		}
     	}
     }
 	//DeathMessages, updates player's stats, and spectates dead player
 	@EventHandler
 	public void spectateOnDeath(PlayerDeathEvent event){
 			Player peter = event.getEntity();
 			User player = usermanager.getUser(peter);
 		if(gamemanager.isInGame(peter.getName())){
 			player.addDeath();
 			usermanager.updatePlayer(peter, "stats");
 			if(peter.getBedSpawnLocation() != null){
 					playergame = gamemanager.getGameByPlayer(peter);
 					Team playersteam = playergame.getTeam(peter);
 					if(playersteam.equals(Team.BLUE)){
 						for(Player p :playergame.getBlueTeamPlayers()){
 							p.sendMessage(new StringBuilder(GRAY.toString()).append(peter.getName()).append(GREEN).append(" has died fighting for your team").toString());
 						}
 						for(Player p :playergame.getRedTeamPlayers()){
 							p.sendMessage(new StringBuilder(GRAY.toString()).append(peter.getName()).append(GREEN).append(" has been killed").toString());
 						}
 					}else{
 						for(Player p :playergame.getRedTeamPlayers()){
 							p.sendMessage(new StringBuilder(GRAY.toString()).append(peter.getName()).append(GREEN).append(" has died fighting for your team").toString());
 						}
 						for(Player p :playergame.getBlueTeamPlayers()){
 							p.sendMessage(new StringBuilder(GRAY.toString()).append(peter.getName()).append(GREEN.toString()).append(" has been killed").toString());
 						}
 				
 					}
 			}else{
 				playergame = gamemanager.getGameByPlayer(peter);
 				playergame.removePlayer(peter);
 				//Something to teleport to dead box of the game :D 
 				
 				
 			}
 		}
 	}
 	//Stops crafting of beds
 	@EventHandler
 	public void noCraftBed(CraftItemEvent event){
 		ItemStack result = event.getCurrentItem();
 		Player player = (Player) event.getWhoClicked();
 		if(gamemanager.isInGame(player)){
 			if(result.getType() == Material.BED || result.getType() == Material.BED_BLOCK){
 				event.setCancelled(true);
 			}
 		}
 	}
 	//Stops building of Nether portals
 	@EventHandler(priority=EventPriority.LOWEST)
 	  public void onPlayerInteract(PlayerInteractEvent event)
 	  {
 	    if (event.isCancelled()) return;
 
 	    Player player = event.getPlayer();
 	    if ((event.getClickedBlock().getType() == Material.OBSIDIAN) && (event.getMaterial() == Material.FLINT_AND_STEEL))
 	    {
 	    	event.setCancelled(true);
 	    	player.sendMessage(new StringBuilder(RED.toString()).append("You may not create Nether portals !").toString());
 	    }
 	           
 	    
 	  }
 	
 	
 		//Stops building of Ender portals
 	  @EventHandler(priority=EventPriority.LOWEST)
 	  public void enderBlockInteract(PlayerInteractEvent event)
 	  {
 	    if (event.isCancelled()) return;
 
 	   
 	    Player player = event.getPlayer();
 	    if ((event.getClickedBlock().getType() == Material.ENDER_PORTAL_FRAME) && (event.getMaterial() == Material.EYE_OF_ENDER))
 	    {
 	      
 	        event.setCancelled(true);
 	        player.sendMessage(new StringBuilder(RED.toString()).append("You may not create End portals !").toString());
 	      
 	    }
 	  }
 	  //Stops glass enclosed structures from being accessed 
 	  @EventHandler
 	  public void onBlockBreak(BlockBreakEvent event){
 		  Player peter = event.getPlayer();
 		  if(gamemanager.isInGame(peter)){
 			  if(event.getBlock().getType() == Material.GLASS){
 				  peter.sendMessage(new StringBuilder(RED.toString()).append("Glass inclosed structures are off limits!").toString());
 				  event.setCancelled(true);
 			  }
 		  }else{
 			  event.setCancelled(true);
 		  }
 	  }
 	  
 	  
 	  //Calculates the killer and killed's score
 	public int calculateScoreOnKill(Player killer, Player killed){
 		User killedcurrent = usermanager.getUser(killed);
 		User killercurrent = usermanager.getUser(killer);
 		int killedcurrentpoints = killedcurrent.getPoints();
 		int killercurrentpoints = killercurrent.getPoints();
 		if(killedcurrentpoints > 4){
 			killedcurrent.changePoints(killedcurrentpoints - killedcurrentpoints/killercurrentpoints);
 			usermanager.updatePlayer(killed, "stats");
 		}
 		
 		int plusscore = (killedcurrentpoints/killercurrentpoints + 1)*killercurrentpoints/killedcurrentpoints + 3;
 		killercurrent.changePoints(killercurrentpoints + plusscore);
 		usermanager.updatePlayer(killer, "stats");
 		return 0;
 	}
 	
 	
 	//Updates player profile when said player joins
 	@EventHandler
 	public void onJoin(PlayerJoinEvent event){
 		Player player = event.getPlayer();
 		usermanager.createUser(player);
 		User user = new User(player);
 		usermanager.updatePlayer(player, "stats");
 	}
 	
 	
 	//Stop block breaking when the game is not started 
 	@EventHandler
 	public void blockBreak(BlockBreakEvent event){
 		if(gamemanager.isInGame(event.getPlayer())){
 			if(!gamemanager.getGameByPlayer(event.getPlayer()).started){
 				event.setCancelled(true);
 			}
 		}
 	}
 	
 	
 	//Stops friendly fire and pvp before the game starts
 	@EventHandler
 	public void playerHit(EntityDamageByEntityEvent event){
 		if(event.getEntity() instanceof Player){
 			if(event.getDamager() instanceof Player){
 				Player player = (Player)event.getEntity();
 				Player damager = (Player)event.getDamager();
 				if(gamemanager.isInGame(player) && gamemanager.isInGame(damager)){
 					if(gamemanager.getGameByPlayer(player).started){
 						if(gamemanager.getGameByPlayer(player).getTeam(player).equals(gamemanager.getGameByPlayer(damager).getTeam(damager))){
 							damager.sendMessage("You may not hurt your own teammate");
 							event.setCancelled(true);
 						}
 					}else{
 						damager.sendMessage(" You may not hurt other players until the game starts");
 						event.setCancelled(true);
 					}
 				}
 			}
 		}
 	}
 	
	@EventHandler
	public void onLeave(PlayerQuitEvent event){
		if(gamemanager.isInGame(event.getPlayer())){
			gamemanager.getGameByPlayer(event.getPlayer()).removePlayer(event.getPlayer());
		}
		
	}
	
 	
 	
 	
 	
 	
 	
 
 }

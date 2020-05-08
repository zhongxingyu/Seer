 package com.ftwinston.Killer.GameModes;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import com.ftwinston.Killer.GameMode;
 import com.ftwinston.Killer.PlayerManager;
 import com.ftwinston.Killer.PlayerManager.Info;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.Material;
 
 public class ContractKiller extends GameMode
 {
 	@Override
 	public String getName() { return "Contract Killer"; }
 	
 	@Override
 	public int getModeNumber() { return 5; }
 
 	@Override
 	public int absMinPlayers() { return 4; }
 
 	@Override
 	public boolean killersCompassPointsAtFriendlies() { return false; }
 
 	@Override
 	public boolean friendliesCompassPointsAtKiller() { return false; }
 
 	@Override
 	public boolean compassPointsAtTarget() { return true; }
 	
 	@Override
 	public boolean discreteDeathMessages() { return false; }
 
 	@Override
 	public boolean usesPlinth() { return false; }
 
 	@Override
 	public int determineNumberOfKillersToAdd(int numAlive, int numKillers, int numAliveKillers)
 	{
 		return numAlive - numAliveKillers;
 	}
 	
 	public boolean assignKillers(int numKillers, CommandSender sender, PlayerManager pm)
 	{
 		List<Map.Entry<String, Info>> players = new LinkedList<Map.Entry<String, Info>>();
 		for ( Map.Entry<String, Info> entry : pm.getPlayerInfo() )
 			if ( entry.getValue().isAlive() )
 				players.add(entry);
 		
 		if ( players.size() < absMinPlayers() )
 		{
 			String message = "Insufficient players to assign a killer. A minimum of " + absMinPlayers() + " players are required.";
 			if ( sender != null )
 				sender.sendMessage(message);
 			if ( informOfKillerAssignment(pm) )
 				plugin.getServer().broadcastMessage(message);
 			return false;
 		}
 		
 		
 		if ( !plugin.statsManager.isTracking )
 			plugin.statsManager.gameStarted(pm.numSurvivors());
 		
 		Map.Entry<String, Info> firstOne = players.remove(r.nextInt(players.size()));
 		Map.Entry<String, Info> prevOne = firstOne;
 		
 		while ( players.size() > 0 )
 		{
 			Map.Entry<String, Info> current = players.remove(r.nextInt(players.size())); 
 			prevOne.getValue().target = current.getKey();
 			
 			Player hunterPlayer = plugin.getServer().getPlayerExact(prevOne.getKey());
 			if ( hunterPlayer != null && hunterPlayer.isOnline() )
 				hunterPlayer.sendMessage("Your target is: " +  ChatColor.YELLOW + current.getKey() + ChatColor.RESET + "!");
 			prevOne = current;
 			
 			plugin.statsManager.killerAdded();
 			if ( sender != null )
 				plugin.statsManager.killerAddedByAdmin();
 		}
 		
 		prevOne.getValue().target = firstOne.getKey();
 		
 		Player hunterPlayer = plugin.getServer().getPlayerExact(prevOne.getKey());
 		if ( hunterPlayer != null && hunterPlayer.isOnline() )
 			hunterPlayer.sendMessage("Your target is: " +  ChatColor.YELLOW + firstOne.getKey() + ChatColor.RESET + "!");
 		
 		plugin.statsManager.killerAdded();
 		if ( sender != null )
 			plugin.statsManager.killerAddedByAdmin();
 		
 
 		plugin.getServer().broadcastMessage("All players have been allocated a target to kill");
 
 		return true;
 	}
 	
 	@Override
 	public String describePlayer(boolean killer, boolean plural)
 	{
 		return plural ? "players" : "player";
 	}
 	
 	@Override
 	public boolean informOfKillerAssignment(PlayerManager pm) { return false; }
 	
 	@Override
 	public boolean informOfKillerIdentity() { return false; }
 	
 	@Override
 	public boolean immediateKillerAssignment() { return true; }
 	
 	@Override
 	public void explainGameMode(Player player, PlayerManager pm)
 	{
 		String message = getName() + "\n";
 		message += "Everyone " + (pm.numKillersAssigned() > 0 ? "has been" : "will soon be") + " assigned a target, and they must try and kill this player without being seen doing so by anybody else. Your compass will point towards your victim, and if anyone sees you kill them, you will die instead of them. Remember that someone else is hunting you! If you kill anyone other than your target or your hunter, you will die instead of them.\nWhen you kill your target, you are assigned their target, and the game continues until only one player remains alive.";
 		player.sendMessage(message);
 	}
 	
 	@Override
 	public void playerJoined(Player player, PlayerManager pm, boolean isNewPlayer, PlayerManager.Info info)
 	{
 		if ( isNewPlayer ) // this is a new player, and the game's started ... so fit them into the victim list
 		{
 			player.sendMessage("Welcome to Killer Minecraft!");
 			
 			if ( pm.numKillersAssigned() == 0 )
 				return;
 				
 			// pick a player to be this player's hunter. This player's victim will be the hunter's victim.
 			int numCandidates = 0;
 			for ( Map.Entry<String, Info> entry : pm.getPlayerInfo() )
 				if ( entry.getValue().isAlive() && entry.getValue().isKiller() )
 					numCandidates ++;
 			
 			int hunterIndex = r.nextInt(numCandidates), i = 0;
 			for ( Map.Entry<String, Info> entry : pm.getPlayerInfo() )
 				if ( entry.getValue().isAlive() && entry.getValue().isKiller() )
 					if ( i == hunterIndex )
 					{
 						info.target = entry.getValue().target;
 						entry.getValue().target = player.getName();
 						
 						Player hunterPlayer = plugin.getServer().getPlayerExact(entry.getKey());
 						if ( hunterPlayer != null && hunterPlayer.isOnline() )
 							hunterPlayer.sendMessage("Your target has changed, and is now: " +  ChatColor.YELLOW + info.target + ChatColor.RESET + "!");
 						break;
 					}
 					else
 						i++;
 			
 			// tell them who their target is
 			player.sendMessage("Your target is: " +  ChatColor.YELLOW + info.target + ChatColor.RESET + "!");
 			info.setKiller(true); // everyone's a killer in their own way
 		}
 		else
 		{
 			String message = "Welcome back.";
 			
 			if ( info != null && info.target != null )
 				message += " Your target is: " +  ChatColor.YELLOW + info.target + ChatColor.RESET + "!";
 				
 			player.sendMessage(message);
 		}
 	}
 	
 	private static final double maxObservationRangeSq = 60 * 60;
 	
 	@Override
 	public void playerDamaged(EntityDamageEvent event)
 	{		
 		if ( !(event instanceof EntityDamageByEntityEvent) || plugin.playerManager.numKillersAssigned() == 0 )
 			return;
 
 		EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent)event;
 		if ( event.getEntity() instanceof Player )
 		{
 			if ( !(edbee.getDamager() instanceof Player) )
 				return;
 			
 			Player victim = (Player)event.getEntity();
 			Player attacker = (Player)edbee.getDamager();
 					
 			// armour is a problem. looks like its handled in EntityHuman.b(DamageSource damagesource, int i) - can replicate the code ... technically also account for enchantments
 			if ( event.getDamage() >= victim.getHealth() )
 				if ( plugin.playerManager.getInfo(attacker.getName()).target.equals(plugin.playerManager.getInfo(victim.getName())) ||
 					 plugin.playerManager.getInfo(victim.getName()).target.equals(plugin.playerManager.getInfo(attacker.getName())) )
 				{// this interaction was allowed ... should still check if they were observed!
 					 for ( Player observer : plugin.getServer().getOnlinePlayers() )					
 					 {
 						 if ( observer == victim || observer == attacker || !plugin.playerManager.isAlive(observer.getName()) )
 							 continue;
 						 
 						 if ( plugin.playerManager.canSee(observer, attacker, maxObservationRangeSq) )
 						 {
 							 event.setCancelled(true);
 							 attacker.damage(50);
 							 
 							 attacker.sendMessage("You were observed trying to kill " + victim.getName() + " by " + observer.getName() + ", so you've been killed instead.");
 							 victim.sendMessage(attacker.getName() + " tried to kill you, but was observed doing so by " + observer.getName() + " - so " + attacker.getName() + " has been killed instead.");
 							 observer.sendMessage("You observed " + attacker.getName() + " trying to kill " + victim.getName() + ", so " + attacker.getName() + " was killed instead.");
 							 break;
 						 }
 					 }
 				}
 				else
 				{
 					// this wasn't a valid kill target, and was a killing blow. Cancel, and kill the attacker.
 					event.setCancelled(true);
 					attacker.damage(50);
 					
 					attacker.sendMessage(victim.getName() + " was neither your target nor your hunter, so you've been killed for trying to kill them!");
 					victim.sendMessage(attacker.getName() + " tried to kill you - they've been killed instead.");
 				}
 		}
 	}
 	
 	@Override
 	public void playerKilled(Player player, PlayerManager pm, PlayerManager.Info info)
 	{
 		if ( pm.numSurvivors() > 1 ) 
 			// find this player's hunter ... change their target to this player's target
 			for ( Map.Entry<String, Info> entry : pm.getPlayerInfo() )
 				if ( player.getName().equals(entry.getValue().target) )
 				{
 					entry.getValue().target = info.target;
 					Player hunterPlayer = plugin.getServer().getPlayerExact(entry.getKey());
 					if ( hunterPlayer != null && hunterPlayer.isOnline() )
 						hunterPlayer.sendMessage("Your target has changed, and is now: " +  ChatColor.YELLOW + info.target + ChatColor.RESET + "!");
 					break;
 				}
 			
 		info.target = null;
 	}
 		
 	@Override
 	public void prepareKiller(Player player, PlayerManager pm, boolean isNewPlayer)
 	{
 		if ( !isNewPlayer )
 			return; // don't give items when rejoining
 		
 		PlayerInventory inv = player.getInventory();
 		inv.addItem(new ItemStack(Material.COMPASS, 1));
 	}
 	
 	@Override
 	public void prepareFriendly(Player player, PlayerManager pm, boolean isNewPlayer)
 	{
 	}
 	
 	@Override
 	public void checkForEndOfGame(PlayerManager pm, Player playerOnPlinth, Material itemOnPlinth)
 	{
 		// if there's no one alive at all, game was drawn
 		if (pm.numSurvivors() < 2 )
 		{
 			pm.gameFinished(pm.numSurvivors() == 1, false, null, null);
 			return;
 		}
 	}
 }

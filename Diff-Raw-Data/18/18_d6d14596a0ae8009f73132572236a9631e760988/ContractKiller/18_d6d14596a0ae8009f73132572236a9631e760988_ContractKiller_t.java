 package com.ftwinston.Killer.GameModes;
 
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import com.ftwinston.Killer.GameMode;
 import com.ftwinston.Killer.PlayerManager;
 import com.ftwinston.Killer.PlayerManager.Info;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
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
 			prevOne.getValue().setKiller(true);
 			
 			Player hunterPlayer = plugin.getServer().getPlayerExact(prevOne.getKey());
 			if ( hunterPlayer != null && hunterPlayer.isOnline() )
 			{
 				hunterPlayer.sendMessage("Your target is: " +  ChatColor.YELLOW + current.getKey() + ChatColor.RESET + "!");
 				prepareKiller(hunterPlayer, pm, true);
 			}
 			prevOne = current;
 			
 			plugin.statsManager.killerAdded();
 			if ( sender != null )
 				plugin.statsManager.killerAddedByAdmin();
 		}
 		
 		prevOne.getValue().target = firstOne.getKey();
 		prevOne.getValue().setKiller(true);
 		
 		Player hunterPlayer = plugin.getServer().getPlayerExact(prevOne.getKey());
 		if ( hunterPlayer != null && hunterPlayer.isOnline() )
 		{
 			hunterPlayer.sendMessage("Your target is: " +  ChatColor.YELLOW + firstOne.getKey() + ChatColor.RESET + "!");
 			prepareKiller(hunterPlayer, pm, true);
 		}
 		
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
 	public void gameFinished()
 	{
 		victimWarningTimes.clear();
 	}
 	
 	@Override
 	public boolean informOfKillerAssignment(PlayerManager pm) { return false; }
 	
 	@Override
 	public boolean informOfKillerIdentity() { return false; }
 	
 	@Override
 	public boolean revealKillersIdentityAtEnd() { return false; }
 	
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
 					
 			if ( !info.isAlive() || pm.numKillersAssigned() == 0 )
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
 			
 			if ( info != null && info.isAlive() && info.target != null )
 				message += " Your target is: " +  ChatColor.YELLOW + info.target + ChatColor.RESET + "!";
 				
 			player.sendMessage(message);
 		}
 	}
 	
 	private static final double maxObservationRangeSq = 60 * 60;
 	
 	@Override
 	public boolean playerDamaged(Player victim, Entity attacker, DamageCause cause, int amount)
 	{
 		PlayerManager pm = plugin.playerManager;
 
 		if ( !(attacker instanceof Player) )
 			return true; // we only care about damage by players
 		
 		Player attackerPlayer = (Player)attacker;
 		
 		String victimTarget = pm.getInfo(victim.getName()).target; 
 		String attackerTarget = pm.getInfo(attackerPlayer.getName()).target; 
 		
 		// armour is a problem. looks like its handled in EntityHuman.b(DamageSource damagesource, int i) - can replicate the code ... technically also account for enchantments
 		if ( amount >= victim.getHealth() )
 			if ( attackerTarget.equals(victim.getName()) || victimTarget.equals(attackerPlayer.getName()) )
 			{// this interaction was allowed ... should still check if they were observed!
 				for ( Player observer : plugin.getServer().getOnlinePlayers() )					
 				{
 					 if ( observer == victim || observer == attacker || !pm.isAlive(observer.getName()) )
 						 continue;
 					 
 					 if ( pm.canSee(observer, attackerPlayer, maxObservationRangeSq) )
 					 {
 						 attackerPlayer.damage(50);
 						 
 						 attackerPlayer.sendMessage("You were observed trying to kill " + victim.getName() + " by " + observer.getName() + ", so you've been killed instead.");
 						 victim.sendMessage(attackerPlayer.getName() + " tried to kill you, but was observed doing so by " + observer.getName() + " - so " + attackerPlayer.getName() + " has been killed instead.");
 						 observer.sendMessage("You observed " + attackerPlayer.getName() + " trying to kill " + victim.getName() + ", so " + attackerPlayer.getName() + " was killed instead.");
 						 
 						 return false;
 					 }
 				}
 				
 				if ( attackerTarget.equals(victim.getName()) && pm.numSurvivors() > 1)
 					victim.sendMessage("You killed your hunter - but someone else is hunting you now!");
 			}
 			else
 			{
 				// this wasn't a valid kill target, and was a killing blow
 				attackerPlayer.damage(50);
 				
 				attackerPlayer.sendMessage(victim.getName() + " was neither your target nor your hunter, so you've been killed for trying to kill them!");
 				victim.sendMessage(attackerPlayer.getName() + " tried to kill you - they've been killed instead.");
 				
 				return false; // cancel the damage
 			}
 		else if ( attackerTarget.equals(victim.getName()) )
 		{
			if ( shouldSendVictimMessage(victim.getName(), attackerPlayer.getName(), "H") )
 				victim.sendMessage(attackerPlayer.getName() + " is your hunter - " + ChatColor.RED + "they can kill you!");
 		}
 		else if ( victimTarget.equals(attackerPlayer.getName()) )
 		{
			if ( shouldSendVictimMessage(victim.getName(), attackerPlayer.getName(), "V") )
 				victim.sendMessage(attackerPlayer.getName() + " is your victim - " + ChatColor.RED + "they can kill you!");
 		}
 		else
 		{
			if ( shouldSendVictimMessage(victim.getName(), attackerPlayer.getName(), "-") )
 				victim.sendMessage(attackerPlayer.getName() + " is neither your hunter nor your victim - they cannot kill you, and will die if they try!");
 		}
 		return true;
 	}
 	
 	private static final long victimWarningRepeatInterval = 200;
	private Map<String, Long> victimWarningTimes = new LinkedHashMap<String, Long>();
 	private boolean shouldSendVictimMessage(String victim, String attacker, String relationship)
 	{
 		// if there's a value saved for this player pair/relationship, see if it was saved within the last 10 secs - if so, don't send.
 		String key = victim + "|" + attacker + "|" + relationship;
 		long currentTime = plugin.getServer().getWorlds().get(0).getTime();
 		
 		if ( victimWarningTimes.containsKey(key) )
 		{
 			long warnedTime = victimWarningTimes.get(key);
 			if ( currentTime - warnedTime <= victimWarningRepeatInterval )
 				return false; // they were warned about this attacker IN THIS RELATIONSHIP within the last 10 secs. Don't warn again.
 		}
 		
 		// save this off, so they don't get this same message again in the next 10 secs
 		victimWarningTimes.put(key, currentTime);
 		return true;
 	}
 	
 	@Override
 	public void playerKilled(Player player, PlayerManager pm, PlayerManager.Info info)
 	{
 		if ( pm.numSurvivors() > 1 ) 
 		{
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
 	public void prepareFriendly(Player player, PlayerManager pm, boolean isNewPlayer) { }
 	
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

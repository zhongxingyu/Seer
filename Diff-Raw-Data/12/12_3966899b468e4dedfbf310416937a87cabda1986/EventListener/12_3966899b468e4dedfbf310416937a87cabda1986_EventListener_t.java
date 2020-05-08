 package net.amoebaman.gamemaster;
 
 import net.amoebaman.gamemaster.api.TeamAutoGame;
 import net.amoebaman.gamemaster.enums.MasterStatus;
 import net.amoebaman.gamemaster.enums.PlayerStatus;
 import net.amoebaman.gamemaster.modules.MessagerModule;
 import net.amoebaman.gamemaster.modules.RespawnModule;
 import net.amoebaman.gamemaster.modules.SafeSpawnModule;
 import net.amoebaman.kitmaster.enums.GiveKitContext;
 import net.amoebaman.kitmaster.utilities.ClearKitsEvent;
 import net.amoebaman.kitmaster.utilities.GiveKitEvent;
 import net.amoebaman.statmaster.StatMaster;
 import net.amoebaman.utils.ChatUtils;
 import net.amoebaman.utils.ChatUtils.ColorScheme;
 import net.amoebaman.utils.GenUtil;
 import net.amoebaman.utils.maps.PlayerMap;
 import net.amoebaman.utils.nms.StatusBar;
 import net.minecraft.util.com.google.common.collect.Lists;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Tameable;
 import org.bukkit.entity.ThrownPotion;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.PotionSplashEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.server.ServerListPingEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import com.vexsoftware.votifier.model.Vote;
 import com.vexsoftware.votifier.model.VotifierEvent;
 
 @SuppressWarnings("deprecation")
 public class EventListener implements Listener {
 	
 	@EventHandler
 	public void blockPlace(BlockPlaceEvent event){
 		if(GameMaster.getStatus(event.getPlayer()) != PlayerStatus.ADMIN)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void blockBreak(BlockBreakEvent event){
 		if(GameMaster.getStatus(event.getPlayer()) != PlayerStatus.ADMIN)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void entityDamageModify(final EntityDamageEvent event){
 		/*
 		 * Don't even bother if no event is running
 		 */
 		if(GameMaster.status != MasterStatus.RUNNING){
 			event.setCancelled(true);
 			return;
 		}
 		/*
 		 * Determine the victim
 		 */
 		Player victim = null;
 		if(event.getEntity() instanceof Player)
 			victim = (Player) event.getEntity();
 		if(event.getEntity() instanceof Tameable && ((Tameable) event.getEntity()).getOwner() instanceof Player)
 			victim = (Player) ((Tameable) event.getEntity()).getOwner();
 		if(victim == null)
 			return;
 		/*
 		 * Cancel if the victim isn't playing, or is respawning
 		 */
 		if(GameMaster.getStatus(victim) != PlayerStatus.PLAYING || GameMaster.respawning.contains(victim)){
 			event.setCancelled(true);
 			return;
 		}
 		/*
 		 * We're only really concerned about players damaged by entities here...
 		 */
 		if(event instanceof EntityDamageByEntityEvent){
 			/*
 			 * Determine who did the damage
 			 */
 			Player damager = null;
 			try{
 				damager = (Player) GenUtil.getTrueCulprit((EntityDamageByEntityEvent) event);
 			}
 			catch(ClassCastException cce){}
 			/*
 			 * Teammates can't hurt each other
 			 */
 			if(GameMaster.activeGame instanceof TeamAutoGame){
 				TeamAutoGame game = (TeamAutoGame) GameMaster.activeGame;
 				if(game.getTeam(victim) == game.getTeam(damager))
 					event.setCancelled(true);
 			}
 			/*
 			 * Safe spawn games
 			 */
 			if(GameMaster.activeGame instanceof SafeSpawnModule){
 				SafeSpawnModule game = (SafeSpawnModule) GameMaster.activeGame;
 				if(victim.getLocation().distance(game.getSafeLoc(victim)) < game.getSpawnRadius(victim)){
 					event.setCancelled(true);
 					damager.sendMessage(ChatUtils.format("You can't damage enemies that are in their spawn", ColorScheme.ERROR));
 				}
 				if(damager.getLocation().distance(game.getSafeLoc(damager)) < game.getSpawnRadius(damager) && victim.getLocation().distance(game.getSafeLoc(damager)) > game.getSpawnRadius(damager)){
 					event.setCancelled(true);
 					damager.sendMessage(ChatUtils.format("You can't attack enemies from the safety of your spawn", ColorScheme.ERROR));
 				}
 			}
 			/*
 			 * Schedule to modify the damage so that the real damager (wolf owner, arrow shooter) gets the credit
 			 */
 			final Player fVictim = victim, fDamager = damager;
 			Bukkit.getScheduler().scheduleSyncDelayedTask(GameMaster.plugin(), new Runnable(){ public void run(){
 				fVictim.setLastDamageCause(new EntityDamageByEntityEvent(fVictim, fDamager, DamageCause.ENTITY_ATTACK, event.getDamage()));
 			}});
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
 	public void entityDamageMonitor(EntityDamageEvent event){
 		if(!(event.getEntity() instanceof Player))
 			return;
 		if(GameMaster.status.active && GameMaster.activeGame instanceof SafeSpawnModule){
 			final Player player = (Player) event.getEntity();
 			if(GameMaster.getStatus(player) == PlayerStatus.PLAYING && (event.getCause() == DamageCause.ENTITY_ATTACK || event.getCause() == DamageCause.PROJECTILE || event.getCause() == DamageCause.MAGIC || event.getCause() == DamageCause.POISON))
 				GameMaster.lastDamage.put(player, System.currentTimeMillis());
 		}
 	}
 	
 	@EventHandler
 	public void foodLevelChange(FoodLevelChangeEvent event){
 		if(GameMaster.status != MasterStatus.RUNNING)
 			event.setCancelled(true);
 	}
 	
	@EventHandler
 	public void playerJoin(final PlayerJoinEvent event){
 		final Player player = event.getPlayer();
 		Bukkit.getScheduler().scheduleSyncDelayedTask(GameMaster.plugin(), new Runnable(){ public void run(){
 			switch(GameMaster.status){
 				case PREP:
 					player.sendMessage(ChatUtils.format("The server is currently preparing to launch [[" + GameMaster.activeGame.getGameName() + "]]", ColorScheme.HIGHLIGHT));
 					player.sendMessage(ChatUtils.format("Type [['/help games']] for more info", ColorScheme.HIGHLIGHT));
 					break;
 				case INTERMISSION:
 					player.sendMessage(ChatUtils.format("The server is waiting to begin the next event", ColorScheme.HIGHLIGHT));
 				default: }
 		} }, 20);
 		/*
 		 * If the player is an admin, leave them be
 		 */
 		if(player.hasPermission("gamemaster.admin"))
 			GameMaster.changeStatus(player, PlayerStatus.ADMIN);
 		/*
 		 * If the player is new, welcome them and send them to the newbie initiation room
 		 */
 		else if(!player.hasPlayedBefore()){
 			GameMaster.changeStatus(player, PlayerStatus.EXTERIOR);
 			//TODO Welcome player to server
 		}
 		/*
 		 * Otherwise, just shove them headfirst into the games
 		 */
 		else{
 			GameMaster.changeStatus(player, PlayerStatus.PLAYING);
 			if(GameMaster.status.active)
 				GameMaster.activeGame.addPlayer(player);
 			else
 				player.teleport(GameMaster.mainLobby);
 		}
 	}
 	
 	@EventHandler
 	public void playerQuit(PlayerQuitEvent event){
 		Player player = event.getPlayer();
 		StatusBar.removeStatusBar(player);
 		if(GameMaster.status.active && GameMaster.getStatus(player) == PlayerStatus.PLAYING){
 			player.teleport(GameMaster.mainLobby);
 			GameMaster.resetPlayer(player);
 			GameMaster.activeGame.removePlayer(player);
 			player.setPlayerListName(player.getName());
 		}
 	}
 	
 	@EventHandler
 	public void playerKick(PlayerKickEvent event){
 		playerQuit(new PlayerQuitEvent(event.getPlayer(), "simulation"));
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void playerRespawn(final PlayerRespawnEvent event){
 		final Player player = event.getPlayer();
 		player.removePotionEffect(PotionEffectType.INVISIBILITY);
 		if(!GameMaster.status.active)
 			Bukkit.getScheduler().scheduleSyncDelayedTask(GameMaster.plugin(), new Runnable(){ public void run(){
 				player.teleport(GameMaster.mainLobby);
 			}});
 		else if(GameMaster.activeGame instanceof RespawnModule){
 			final RespawnModule game = (RespawnModule) GameMaster.activeGame;
 			/*
 			 * If and only if the player is playing...
 			 */
 			if(GameMaster.getStatus(player) == PlayerStatus.PLAYING){
 				/*
 				 * First send them to the waiting location
 				 */
 				Bukkit.getScheduler().scheduleSyncDelayedTask(GameMaster.plugin(), new Runnable(){ public void run(){
 					player.teleport(game.getWaitingLoc(player));
 					GameMaster.respawning.add(player);
 				}});
 				/*
 				 * After the delay has passed...
 				 */
 				Bukkit.getScheduler().scheduleSyncDelayedTask(GameMaster.plugin(), new Runnable(){ public void run(){
 					/*
 					 * If they're still active, respawn them
 					 */
 					if(GameMaster.status.active && GameMaster.getStatus(player) == PlayerStatus.PLAYING && player.isOnline()){
 						player.teleport(game.getRespawnLoc(player));
 						player.setNoDamageTicks(20 * game.getRespawnInvulnSeconds(player));
 						/*
 						 * If the game also happens to be MessagerCompatible, send messages as well
 						 */
 						if(game instanceof MessagerModule){
 							player.sendMessage(ChatUtils.spacerLine());
 							for(String line : ((MessagerModule) game).getSpawnMessage(player))
 								player.sendMessage(ChatUtils.centerAlign(ChatUtils.format(line, ColorScheme.HIGHLIGHT)));
 							player.sendMessage(ChatUtils.spacerLine());
 						}
 						GameMaster.respawning.remove(player);
 					}
 				}}, 20 * game.getRespawnSeconds(player));
 				return;
 			}
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void playerDeath(EntityDeathEvent event){
 		event.getDrops().clear();
 		GameMaster.lastDamage.remove(event.getEntity());
 		if(event instanceof PlayerDeathEvent){
 			Player player = (Player) event.getEntity();
 			StatusBar.removeStatusBar(player);
 			if(splashHarms.containsKey(player) && (player.getLastDamageCause().getCause() == DamageCause.MAGIC || player.getLastDamageCause().getCause() == DamageCause.WITHER)){
 				Player thrower = Bukkit.getPlayer(splashHarms.get(player));
 				if(thrower != null){
 					player.setLastDamageCause(new EntityDamageByEntityEvent(thrower, player, DamageCause.ENTITY_ATTACK, 10.0));
 					splashHarms.remove(player);
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void forbidChestStorage(InventoryClickEvent event){
 		Player player = (Player) event.getWhoClicked();
 		if(GameMaster.getStatus(player) != PlayerStatus.ADMIN && event.getView().getTopInventory() != null)
			switch(event.getView().getTopInventory().getType()){
				case CRAFTING: case CREATIVE: case PLAYER:
					event.setCancelled(true);
				default:
			}
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void teamChat(AsyncPlayerChatEvent event){
 		Player player = event.getPlayer();
 		if(GameMaster.teamChatters.contains(player) && GameMaster.getStatus(player) == PlayerStatus.PLAYING && GameMaster.status.active && GameMaster.activeGame instanceof TeamAutoGame){
 			TeamAutoGame game = (TeamAutoGame) GameMaster.activeGame;
 			for(Player other : Bukkit.getOnlinePlayers())
 				if(game.getTeam(player) != game.getTeam(other))
 					event.getRecipients().remove(other);
 			event.setMessage("(" + game.getTeam(player).chat + "TEAM" + ChatColor.WHITE + ") " + event.getMessage());
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void votifier(VotifierEvent event){
 		Vote vote = event.getVote();
 		if(vote == null){
 			GameMaster.logger().severe("VotifierEvent returned null vote");
 			return;
 		}
 		GameMaster.logger().info("Received vote from " + vote.getServiceName() + " by " + vote.getUsername() + " from " + vote.getAddress() + " at " + vote.getTimeStamp());
 		OfflinePlayer player = Bukkit.getPlayer(vote.getUsername());
 		if(player == null)
 			player = Bukkit.getOfflinePlayer(vote.getUsername());
 		if(player.hasPlayedBefore() || player.isOnline()){
 			StatMaster.getHandler().incrementStat(player, "charges");
 			StatMaster.getHandler().incrementCommunityStat("votes");
 			Bukkit.broadcastMessage(ChatUtils.format("[[" + player.getName() + "]] voted for the server, and now has [[" + StatMaster.getHandler().getStat(player, "charges") + "]] charges", ColorScheme.HIGHLIGHT));
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void restrictCommandKitsToSpawns(GiveKitEvent event){
 		final Player player = event.getPlayer();
 		if(GameMaster.getStatus(player) == PlayerStatus.PLAYING && GameMaster.status == MasterStatus.RUNNING)
 			if(GameMaster.activeGame instanceof RespawnModule && player.getLocation().distance(((RespawnModule) GameMaster.activeGame).getRespawnLoc(player)) > 10)
 				if(!event.getContext().overrides && event.getContext() != GiveKitContext.SIGN_TAKEN && !player.hasPermission("gamemaster.globalkit")){
 					player.sendMessage(ChatUtils.format("You must be within [[10 blocks]] of your spawn to choose a kit.", ColorScheme.ERROR));
 					event.setCancelled(true);
 					return;
 				}
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void killProjectilesOnKitChange(ClearKitsEvent event){
 		Player player = event.getPlayer();
 		for(World world : Bukkit.getWorlds())
 			for(Entity e : world.getEntities())
 				if(e instanceof Projectile){
 					Projectile proj = (Projectile) e;
 					if(player.equals(proj.getShooter()))
 						proj.remove();
 				}
 	}
 	
 	private static PlayerMap<String> splashHarms = new PlayerMap<String>();
	@EventHandler
 	public void managePotionSplashes(PotionSplashEvent event){
 		ThrownPotion potion = event.getPotion();
 		Player thrower = null;
 		if(potion.getShooter() instanceof Player)
 			thrower = (Player) potion.getShooter();
 		if(thrower == null || GameMaster.status != MasterStatus.RUNNING)
 			return;
 		for(LivingEntity entity : event.getAffectedEntities()){
 			if(entity instanceof Player){
 				Player victim = (Player) entity;
 				if(GameMaster.activeGame instanceof TeamAutoGame){
 					TeamAutoGame game = (TeamAutoGame) GameMaster.activeGame;
 					if(game.getTeam(thrower) != game.getTeam(victim)){
 						if(victim.getLocation().distance(game.getSafeLoc(victim)) < game.getSpawnRadius(victim)){
 							event.setIntensity(victim, 0);
 							thrower.sendMessage(ChatUtils.format("You can't damage enemies that are in their spawn", ColorScheme.ERROR));
 						}
 						if(thrower.getLocation().distance(game.getSafeLoc(thrower)) < game.getSpawnRadius(thrower) && victim.getLocation().distance(game.getSafeLoc(thrower)) >= game.getSpawnRadius(thrower)){
 							event.setIntensity(victim, 0);
 							thrower.sendMessage(ChatUtils.format("You can't damage enemies out of your spawn", ColorScheme.ERROR));
 						}
 					}
 					else if(game.getTeam(thrower) == game.getTeam(victim))
 						for(PotionEffect effect : potion.getEffects())
 							//List of all negative potion effect IDs (this is for friendly-fire)
 							if(Lists.newArrayList(2,4,7,9,15,17,18,19,20).contains(effect.getType().getId()))
 								event.setIntensity(victim, 0);
 				}
 				for(PotionEffect effect : potion.getEffects())
 					if(effect.getType().equals(PotionEffectType.HARM) || effect.getType().equals(PotionEffectType.WITHER))
 						splashHarms.put(victim, thrower.getName());
 			}
 		}
 	}
 	
 	@EventHandler
 	public void updateServerList(ServerListPingEvent event){
 		switch(GameMaster.status){
 			case INTERMISSION:
 				event.setMotd(ChatUtils.format("Voting on the next game", ColorScheme.HIGHLIGHT));
 				break;
 			case PREP:
 				event.setMotd(ChatUtils.format("Voting on a map for [[" + GameMaster.activeGame + "]]", ColorScheme.HIGHLIGHT));
 				break;
 			case RUNNING:
 			case SUSPENDED:
 				event.setMotd(ChatUtils.format("Playing [[" + GameMaster.activeGame + "]] on [[" + GameMaster.activeMap + "]]", ColorScheme.HIGHLIGHT));
 				break;
 		}
 	}
 	
 }

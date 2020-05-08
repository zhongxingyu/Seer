 package net.amoebaman.gamemasterv3;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.*;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.hanging.HangingBreakByEntityEvent;
 import org.bukkit.event.hanging.HangingPlaceEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.*;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.event.server.ServerListPingEvent;
 import org.bukkit.potion.PotionEffect;
 
 import com.vexsoftware.votifier.model.Vote;
 import com.vexsoftware.votifier.model.VotifierEvent;
 
 import net.amoebaman.gamemasterv3.api.AutoGame;
 import net.amoebaman.gamemasterv3.api.TeamAutoGame;
 import net.amoebaman.gamemasterv3.enums.GameState;
 import net.amoebaman.gamemasterv3.enums.PlayerState;
 import net.amoebaman.gamemasterv3.modules.RespawnModule;
 import net.amoebaman.gamemasterv3.modules.SafeSpawnModule;
 import net.amoebaman.kitmaster.enums.GiveKitContext;
 import net.amoebaman.kitmaster.utilities.ClearKitsEvent;
 import net.amoebaman.kitmaster.utilities.GiveKitEvent;
 import net.amoebaman.statmaster.StatMaster;
 import net.amoebaman.utils.chat.Chat;
 import net.amoebaman.utils.chat.Message;
 import net.amoebaman.utils.chat.Scheme;
 import net.amoebaman.utils.nms.StatusBar;
 
 import net.minecraft.util.com.google.common.collect.Lists;
 
 public class EventListener implements Listener{
 	
 	private Set<Player> teamChatting = new HashSet<Player>();
 	
 	private GameMaster master;
 	
 	protected EventListener(GameMaster master){
 		this.master = master;
 	}
 	
 	public boolean toggleTeamChat(Player player){
 		if(!teamChatting.remove(player)){
 			teamChatting.add(player);
 			return true;
 		}
 		return false;
 	}
 	
 	@EventHandler
 	public void deregisterUnloadedGames(PluginDisableEvent event){
 		if(event.getPlugin() instanceof AutoGame)
 			master.deregisterGame((AutoGame) event.getPlugin());
 	}
 	
 	@EventHandler
 	public void forbidBlockPlacing(BlockPlaceEvent event){
 		if(master.getState(event.getPlayer()) != PlayerState.EXTERIOR)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void forbidBlockBreaking(BlockBreakEvent event){
 		if(master.getState(event.getPlayer()) != PlayerState.EXTERIOR)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void protectHangersFromPlacing(HangingPlaceEvent event){
 		if(master.getState(event.getPlayer()) != PlayerState.EXTERIOR)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void protectHangersFromBreaking(HangingBreakByEntityEvent event){
 		Entity culprit = event.getRemover();
 		if(culprit instanceof Projectile && ((Projectile) culprit).getShooter() instanceof Entity)
 			culprit = (Entity) ((Projectile) culprit).getShooter();
 		if(culprit instanceof Player && master.getState((Player) culprit) != PlayerState.EXTERIOR)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void protectFramesFromExtraction(EntityDamageByEntityEvent event){
 		Entity culprit = event.getDamager();
 		if(culprit instanceof Projectile && ((Projectile) culprit).getShooter() instanceof Entity)
 			culprit = (Entity) ((Projectile) culprit).getShooter();
 		if(event.getEntity() instanceof Hanging && culprit instanceof Player && master.getState((Player) culprit) != PlayerState.EXTERIOR)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void protectHangersFromMeddling(PlayerInteractEntityEvent event){
 		if(master.getState(event.getPlayer()) != PlayerState.EXTERIOR && event.getRightClicked() instanceof Hanging)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void entityDamageModify(final EntityDamageEvent event){
 		/*
 		 * Determine the victim (we consider wolves part of their owners)
 		 */
 		Player victim = null;
 		if(event.getEntity() instanceof Player)
 			victim = (Player) event.getEntity();
 		if(event.getEntity() instanceof Tameable && ((Tameable) event.getEntity()).getOwner() instanceof Player)
 			victim = (Player) ((Tameable) event.getEntity()).getOwner();
 		if(victim == null)
 			return;
 		/*
 		 * Cancel if they're a spectator
 		 */
 		if(master.getState(victim) == PlayerState.WATCHING)
 			event.setCancelled(true);
 		/*
 		 * Do nothing further if they're not playing
 		 */
 		if(master.getState(victim) != PlayerState.PLAYING)
 			return;
 		/*
 		 * Cancel and return if a game isn't running, or if they're respawning
 		 */
 		if(master.getState() != GameState.RUNNING || master.getPlayerManager().isRespawning(victim)){
 			event.setCancelled(true);
 			return;
 		}
 		/*
 		 * We're really only interested in EDBE events
 		 */
 		if(event instanceof EntityDamageByEntityEvent){
 			Player culprit = null;
 			Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
 			/*
 			 * Get the culprit (trace arrows and wolves to their source)
 			 */
 			if(damager instanceof Player)
 				culprit = (Player) damager;
 			if(damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Player)
 				culprit = (Player) ((Projectile) damager).getShooter();
 			if(damager instanceof Tameable && ((Tameable) damager).getOwner() instanceof Player)
 				culprit = (Player) ((Tameable) damager).getOwner();
 			if(culprit == null)
 				return;
 			/*
 			 * Remember kids, friendly fire isn't!
 			 */
 			if(master.getActiveGame() instanceof TeamAutoGame){
 				TeamAutoGame game = (TeamAutoGame) master.getActiveGame();
 				if(game.getTeam(victim) == game.getTeam(culprit))
 					event.setCancelled(true);
 			}
 			/*
 			 * Spawn protection for safe spawning games
 			 */
 			if(master.getActiveGame() instanceof SafeSpawnModule){
 				SafeSpawnModule game = (SafeSpawnModule) master.getActiveGame();
 				if(victim.getLocation().distance(game.getSafeLoc(victim)) < game.getSafeRadius(victim)){
 					event.setCancelled(true);
 					new Message(Scheme.WARNING).t(victim.getName()).s().t(" is under spawn protection").send(culprit);
 				}
 				if(culprit.getLocation().distance(game.getSafeLoc(culprit)) < game.getSafeRadius(culprit) && victim.getLocation().distance(game.getSafeLoc(culprit)) > game.getSafeRadius(culprit)){
 					event.setCancelled(true);
 					new Message(Scheme.WARNING).t("You can't attack enemies while under spawn protection").send(culprit);
 				}
 			}
 			/*
 			 * Stamp the damage
 			 */
 			if(!event.isCancelled())
 				master.getPlayerManager().stampDamage(victim, culprit);
 			/*
 			 * Modify the damage to put the true source on record
 			 */
 			final Player fVictim = victim, fCulprit = culprit;
 			Bukkit.getScheduler().scheduleSyncDelayedTask(master, new Runnable(){
 				
 				public void run(){
 					fVictim.setLastDamageCause(new EntityDamageByEntityEvent(fVictim, fCulprit, DamageCause.ENTITY_ATTACK, event.getDamage()));
 				}
 			});
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void stampOtherDamager(EntityDamageEvent event){
 		if(!(event.getEntity() instanceof Player))
 			return;
 		Player player = (Player) event.getEntity();
 		if(event.getCause() == DamageCause.FIRE_TICK || event.getCause() == DamageCause.MAGIC || event.getCause() == DamageCause.WITHER || event.getCause() == DamageCause.POISON)
 			master.getPlayerManager().stampDamage(player, master.getPlayerManager().getLastDamager(player));
 	}
 	
 	@EventHandler
 	public void foodLevelChange(FoodLevelChangeEvent event){
 		if(master.getState() == GameState.INTERMISSION && master.getState((Player) event.getEntity()) == PlayerState.PLAYING || master.getState((Player) event.getEntity()) == PlayerState.WATCHING)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void playerJoin(final PlayerJoinEvent event){
 		if(master.getConfig().getBoolean("wrap-server", false)){
 			final Player player = event.getPlayer();
 			Bukkit.getScheduler().scheduleSyncDelayedTask(master, new Runnable(){ public void run(){
 				if(master.getState() == GameState.INTERMISSION){
 					if(master.getActiveGame() == null)
 						Chat.send(player,
 							new Message(Scheme.HIGHLIGHT).then("We're voting on the next game"),
 							new Message(Scheme.HIGHLIGHT).then("Click here").strong().style(ChatColor.BOLD).command("/vote").then(" to vote")
 							);
 					else if(master.getActiveMap() == null)
 						Chat.send(player,
 							new Message(Scheme.HIGHLIGHT).then("We're voting on the map for ").t(master.getActiveGame()).s(),
 							new Message(Scheme.HIGHLIGHT).then("Click here").strong().style(ChatColor.BOLD).command("/vote").then(" to vote")
 							);
 					else
 						Chat.send(player,
 							new Message(Scheme.HIGHLIGHT)
 						.then("We're waiting for")
 						.t(master.getActiveGame()).s()
 						.t(" on ")
 						.t(master.getActiveMap()).s()
 						.t(" to start")
 							);
 				}
 			} }, 20);
 			/*
 			 * If the player is an admin, leave them be
 			 */
 			if(player.hasPermission("gamemaster.admin"))
 				master.setState(player, PlayerState.EXTERIOR);
 			/*
 			 * If the player is new, welcome them and send them to the newbie initiation room
 			 */
 			else if(!player.hasPlayedBefore()){
 				master.setState(player, PlayerState.EXTERIOR);
 				player.teleport(master.getWelcome());
 			}
 			/*
 			 * Otherwise, just shove them headfirst into the games
 			 */
 			else{
 				master.setState(player, PlayerState.PLAYING);
 				if(master.getState() != GameState.INTERMISSION)
 					master.getActiveGame().join(player);
 				else
 					player.teleport(master.getLobby());
 			}
 		}
 	}
 	
 	@EventHandler
 	public void playerQuit(PlayerQuitEvent event){
 		if(master.getConfig().getBoolean("wrap-server", false)){
 			Player player = event.getPlayer();
 			StatusBar.removeStatusBar(player);
 			if(master.getState() != GameState.INTERMISSION && master.getState(player) == PlayerState.PLAYING){
 				player.teleport(master.getLobby());
 				master.getActiveGame().leave(player);
 				master.getPlayerManager().resetPlayer(player);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void playerRespawn(final PlayerRespawnEvent event){
 		final Player player = event.getPlayer();
 		/*
 		 * If and only if the player is playing
 		 */
 		if(master.getState(player) == PlayerState.PLAYING){
 			/*
 			 * Destamp the player
 			 */
 			master.getPlayerManager().destamp(player);
 			/*
 			 * If it's the intermission, send them to the lobby
 			 */
 			if(master.getState() == GameState.INTERMISSION)
 				Bukkit.getScheduler().scheduleSyncDelayedTask(master, new Runnable(){ public void run(){
 					player.teleport(master.getLobby());
 				}});
 			/*
 			 * Otherwise, if it's an autorespawn game
 			 */
 			else if(master.getActiveGame() instanceof RespawnModule){
 				final RespawnModule game = (RespawnModule) master.getActiveGame();
 				/*
 				 * First send them to the waiting location
 				 */
 				Bukkit.getScheduler().scheduleSyncDelayedTask(master, new Runnable(){ public void run(){
 					player.teleport(game.getWaitingLoc(player));
 					master.getPlayerManager().toggleRespawning(player);
 				}});
 				/*
 				 * After the delay has passed...
 				 */
 				Bukkit.getScheduler().scheduleSyncDelayedTask(master, new Runnable(){ public void run(){
 					/*
 					 * If they're still active, respawn them
 					 */
 					if(master.getState() != GameState.INTERMISSION && master.getState(player) == PlayerState.PLAYING && player.isOnline()){
 						player.teleport(game.getRespawnLoc(player));
 						player.setNoDamageTicks(20 * game.getRespawnInvuln(player));
 						master.getPlayerManager().toggleRespawning(player);
 					}
 				}}, 20 * game.getRespawnDelay(player));
 				return;
 			}
 		}
 	}
 	
 	@EventHandler
 	public void updateServerList(ServerListPingEvent event){
 		if(master.getConfig().getBoolean("wrap-server", false)){
 			if(master.getState() == GameState.INTERMISSION){
 				if(master.getActiveGame() == null)
 					event.setMotd(new Message(Scheme.HIGHLIGHT).t("Voting on the next game").toString());
 				else if(master.getActiveMap() == null)
 					event.setMotd(new Message(Scheme.HIGHLIGHT).t("Voting on a map for ").t(master.getActiveGame()).s().toString());
 				else
					event.setMotd(new Message(Scheme.HIGHLIGHT).t("Waiting for ").t(master.getActiveGame()).s().t(" on ").t(master.getActiveMap()).s().t(" to start").toString());
 			}
 			else
 				event.setMotd(new Message(Scheme.HIGHLIGHT).t("Playing ").t(master.getActiveGame()).s().t(" on ").t(master.getActiveMap()).s().toString());
 		}
 	}
 	
 	@EventHandler
 	public void killProjectilesOnKitChange(ClearKitsEvent event){
 		Player player = event.getPlayer();
 		if(master.getState(player) != PlayerState.EXTERIOR)
 			for(Projectile proj : player.getWorld().getEntitiesByClass(Projectile.class))
 				if(player.equals(proj.getShooter()))
 					proj.remove();
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void restrictCommandKitsToSpawns(GiveKitEvent event){
 		final Player player = event.getPlayer();
 		if(master.getState(player) == PlayerState.PLAYING && master.getState() == GameState.RUNNING)
 			if(master.getActiveGame() instanceof SafeSpawnModule){
 				SafeSpawnModule game = (SafeSpawnModule) master.getActiveGame();
 				if(player.getLocation().distance(game.getSafeLoc(player)) > game.getSafeRadius(player))
 				if(!event.getContext().overrides && event.getContext() != GiveKitContext.SIGN_TAKEN && !player.hasPermission("gamemaster.globalkit")){
 					new Message(Scheme.WARNING)
 						.then("You must be in your spawn to take kits via command")
 						.send(player);
 					event.setCancelled(true);
 				}
 			}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void forbidChestStorage(InventoryClickEvent event){
 		Player player = (Player) event.getWhoClicked();
 		if(master.getState(player) != PlayerState.EXTERIOR && event.getView().getTopInventory() != null)
 			switch(event.getView().getTopInventory().getType()){
 				case CRAFTING: case CREATIVE: case PLAYER:
 					event.setCancelled(false);
 					break;
 				default:
 					event.setCancelled(true);
 					break;
 			}
 	}
 	
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void teamChat(AsyncPlayerChatEvent event){
 		Player player = event.getPlayer();
 		if(teamChatting.contains(player) && master.getState(player) == PlayerState.PLAYING && master.getState() != GameState.INTERMISSION && master.getActiveGame() instanceof TeamAutoGame){
 			TeamAutoGame game = (TeamAutoGame) master.getActiveGame();
 			for(Player other : Bukkit.getOnlinePlayers())
 				if(game.getTeam(player) != game.getTeam(other))
 					event.getRecipients().remove(other);
 			event.setMessage("(" + game.getTeam(player).chat + "TEAM" + ChatColor.WHITE + ") " + event.getMessage());
 		}
 	}
 
 	@EventHandler
 	public void votifier(VotifierEvent event){
 		Vote vote = event.getVote();
 		if(vote == null){
 			master.log("VotifierEvent returned null vote");
 			return;
 		}
 		master.log("Received vote from " + vote.getServiceName() + " by " + vote.getUsername() + " from " + vote.getAddress() + " at " + vote.getTimeStamp());
 		OfflinePlayer player = Bukkit.getPlayer(vote.getUsername());
 		if(player == null)
 			player = Bukkit.getOfflinePlayer(vote.getUsername());
 		if(player.hasPlayedBefore() || player.isOnline()){
 			StatMaster.getHandler().incrementStat(player, "charges");
 			StatMaster.getHandler().incrementCommunityStat("votes");
 			new Message(Scheme.HIGHLIGHT)
 				.t(player.getName()).s()
 				.t(" voted for the server, and now has ")
 				.t(StatMaster.getHandler().getStat(player, "charges")).s()
 				.t(" charges")
 				.broadcast();
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
     @EventHandler
 	public void managePotionSplashes(PotionSplashEvent event){
 		ThrownPotion potion = event.getPotion();
 		Player thrower = null;
 		if(potion.getShooter() instanceof Player)
 			thrower = (Player) potion.getShooter();
 		if(thrower == null || master.getState() != GameState.RUNNING)
 			return;
 		/*
 		 * Check to see if this potion is harmful
 		 */
 		boolean harmful = false;
 		List<Integer> ids = Lists.newArrayList(2,4,7,9,15,17,18,19,20);
 		for(PotionEffect effect : potion.getEffects())
 			if(ids.contains(effect.getType().getId()))
 				harmful = true;
 		/*
 		 * We don't actually care if it's not harmful
 		 */
 		if(harmful)
 			for(LivingEntity entity : event.getAffectedEntities())
 				if(entity instanceof Player){
 					/*
 					 * Run a mock normal damage event to see if this is allowed
 					 */
 					Player victim = (Player) entity;
 					EntityDamageByEntityEvent tester = new EntityDamageByEntityEvent(thrower, victim, DamageCause.ENTITY_ATTACK, 0.0);
 					entityDamageModify(tester);
 					if(tester.isCancelled())
 						event.setIntensity(victim, 0);
 				}
 	}
 	
 	@EventHandler
 	public void logPlayerMoves(PlayerMoveEvent event){
 		master.getPlayerManager().stampMovement(event.getPlayer());
 	}
 	
 	@EventHandler
 	public void dontDropItems(PlayerDeathEvent event){
 		if(master.getState(event.getEntity()) == PlayerState.PLAYING)
 			event.getDrops().clear();
 	}
 	
 }

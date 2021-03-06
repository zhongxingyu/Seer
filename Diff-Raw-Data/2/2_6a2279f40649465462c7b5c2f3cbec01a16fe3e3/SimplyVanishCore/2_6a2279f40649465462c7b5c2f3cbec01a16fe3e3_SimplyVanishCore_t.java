 package asofold.simplyvanish;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.ExperienceOrb;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.entity.PotionSplashEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.util.Vector;
 
 import asofold.simplyvanish.config.Settings;
 import asofold.simplyvanish.config.VanishConfig;
 import asofold.simplyvanish.hooks.Hook;
 import asofold.simplyvanish.util.HookUtil;
 import asofold.simplyvanish.util.Utils;
 
 /**
  * Core methods for vanish/reappear.
  * @author mc_dev
  *
  */
 public class SimplyVanishCore implements Listener{
 	
 	/**
 	 * Vanished players.
 	 */
 	private final Map<String, VanishConfig> vanishConfigs = new HashMap<String, VanishConfig>();
 	
 	private final HookUtil hookUtil = new HookUtil();
 	
 	/**
 	 * Flag for if the plugin is enabled.
 	 */
 	boolean enabled = false;
 	
 	Settings settings = new Settings();
 	
 	/*
 	 * File to save vanished players to.
 	 */
 	private File vanishedFile = null;
 	
 	
 
 	@EventHandler(priority=EventPriority.HIGHEST)
 	void onPlayerJoin( PlayerJoinEvent event){
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 		VanishConfig cfg = vanishConfigs.get(playerName.toLowerCase());
 		boolean auto = false;
 		if ( settings.autoVanishUse && (cfg == null || cfg.auto.state) ) auto = true;
 		else  auto = false;
 		if (auto){
 			if (Utils.hasPermission(player, settings.autoVanishPerm)){
 				addVanishedName(playerName);
 				if (cfg == null) cfg = getVanishConfig(playerName);
 			}
 		}
 		
 		boolean doVanish = false;
 		if (cfg != null){
 			doVanish = cfg.vanished.state;
 			if (doVanish){
				if (!hookUtil.callBeforeVanish(playerName)){
 					cfg.vanished.state = false;
 					cfg.changed = true;
 				}
 				
 			}
 		}
 		updateVanishState(event.getPlayer()); // called in any case
 		if (doVanish) hookUtil.callAfterVanish(playerName);	
 		
 		if ( settings.suppressJoinMessage && cfg!=null && cfg.vanished.state){
 			event.setJoinMessage(null);
 		}
 		else if (!cfg.needsSave()) removeVanishedName(playerName);
 	}
 
 	/**
 	 * Save vanished names to file, does NOT update states (!).
 	 */
 	public void saveVanished(){
 		long ns = System.nanoTime();
 		File file = getVanishedFile();
 		if ( file==null){
 			Utils.warn("Can not save vanished players: File is not set.");
 			return;
 		}
 		if (!createFile(file, "vanished players")) return;
 		BufferedWriter writer = null;
 		try {
 			writer = new BufferedWriter( new FileWriter(file));
 			writer.write("\n"); // to write something at least.
 			for (Entry<String, VanishConfig> entry : vanishConfigs.entrySet()){
 				String n = entry.getKey();
 				VanishConfig cfg = entry.getValue();
 				if (cfg.needsSave()){
 					writer.write(n);
 					writer.write(cfg.toLine());
 					writer.write("\n");
 				}
 				cfg.changed = false;
 			}
 		} 
 		catch (IOException e) {
 			Utils.warn("Can not save vanished players: "+e.getMessage());
 		}
 		finally{
 			if ( writer != null)
 				try {
 					writer.close();
 				} catch (IOException e) {
 				}
 		}
 		SimplyVanish.stats.addStats(SimplyVanish.statsSave, System.nanoTime()-ns);
 	}
 	
 	/**
 	 * Load vanished names from file.<br>
 	 *  This does not update vanished states!<br>
 	 *  Assumes each involved VanishConfig to be changed by loading.
 	 */
 	public void loadVanished(){
 		File file = getVanishedFile();
 		if ( file == null){
 			Utils.warn("Can not load vanished players: File is not set.");
 			return;
 		}
 		if (!createFile(file, "vanished players")) return;
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader( new FileReader(file));
 			String line = "";
 			while ( line != null){
 				String n = line.trim().toLowerCase();
 				if (!n.isEmpty()){
 					if (n.startsWith("nosee:") && n.length()>6){
 						// kept for compatibility:
 						n = n.substring(7).trim();
 						if (n.isEmpty()) continue;
 						VanishConfig cfg = getVanishConfig(n);
 						cfg.see.state = false;
 						cfg.changed = true;
 					}
 					else{
 						String[] split = n.split(" ");
 						n = split[0].trim().toLowerCase();
 						if (n.isEmpty()) continue;
 						VanishConfig cfg = getVanishConfig(n);
 						cfg.readFromArray(split, 1, true);
 					}
 				}
 				line = reader.readLine();
 			}
 		} 
 		catch (IOException e) {
 			Utils.warn("Can not load vanished players: "+e.getMessage());
 		}
 		finally{
 			if ( reader != null)
 				try {
 					reader.close();
 				} catch (IOException e) {
 				}
 		}
 	}
 	
 	/**
 	 * Create if not exists.
 	 * @param file
 	 * @param tag
 	 * @return if exists now.
 	 */
 	public boolean createFile(File file, String tag){
 		if ( !file.exists() ){
 			try {
 				if ( file.createNewFile()) return true;
 				else{
 					Utils.warn("Could not create "+tag+" file.");
 				}
 			} catch (IOException e) {
 				Utils.warn("Could not create "+tag+" file: "+e.getMessage());
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public File getVanishedFile(){
 		return vanishedFile;
 	}
 	
 	public void setVanishedFile(File file) {
 		vanishedFile = file;
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST)
 	void onPlayerQuit(PlayerQuitEvent event){
 		Player player = event.getPlayer();
 		String name = player.getName();
 		if ( settings.suppressQuitMessage && isVanished(name)){
 			event.setQuitMessage(null);
 			if (settings.notifyState){
 				String msg = SimplyVanish.msgLabel+ChatColor.GREEN+name+ChatColor.GRAY+" quit.";
 				for (Player other : Bukkit.getServer().getOnlinePlayers()){
 					if ( Utils.hasPermission(other, settings.notifyStatePerm)) other.sendMessage(msg);
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST)
 	void onPlayerKick(PlayerKickEvent event){
 		// (still set if cancelled)
 		Player player = event.getPlayer();
 		String name = player.getName();
 		if ( settings.suppressQuitMessage && isVanished(name)){
 			event.setLeaveMessage(null);
 			if (settings.notifyState && !event.isCancelled()){
 				String msg = SimplyVanish.msgLabel+ChatColor.GREEN+name+ChatColor.GRAY+" was kicked.";
 				for (Player other : Bukkit.getServer().getOnlinePlayers()){
 					if ( Utils.hasPermission(other, settings.notifyStatePerm)) other.sendMessage(msg);
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	final void onEntityTarget(final EntityTargetEvent event){
 		if ( event.isCancelled() ) return;
 		final Entity target = event.getTarget();
 		if (!(target instanceof Player)) return;
 		final String playerName = ((Player) target).getName();
 		final VanishConfig cfg = vanishConfigs.get(playerName.toLowerCase());
 		if (cfg == null) return;
 		if (cfg.vanished.state){
 			if (settings.expEnabled && !cfg.pickup.state){
 				Entity entity = event.getEntity();
 				if ( entity instanceof ExperienceOrb){
 					repellExpOrb((Player) target, (ExperienceOrb) entity);
 					event.setCancelled(true);
 					event.setTarget(null);
 					return;
 				}
 			}
 			if (!cfg.target.state) event.setTarget(null);
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST)
 	void onPotionSplash(PotionSplashEvent event){
 		try{
 			final List<Entity> rem = new LinkedList<Entity>();
 			final Collection<LivingEntity> affected = event.getAffectedEntities();
 			for ( LivingEntity entity : affected){
 				if (entity instanceof Player ){
 					String playerName = ((Player) entity).getName();
 					VanishConfig cfg = vanishConfigs.get(playerName.toLowerCase());
 					if (cfg == null) continue;
 					if (cfg.vanished.state){
 						if (!cfg.damage.state) rem.add(entity);
 					}
 				}
 			}
 			if (!rem.isEmpty()) affected.removeAll(rem);
 		} catch(Throwable t){
 			// ignore (fast addition.)
 		}
 	}
 	
 //	@EventHandler(priority=EventPriority.HIGHEST)
 //	void onServerListPing(ServerListPingEvent event){
 //		// TODO: try reflection ??
 //	}
 	
 	/**
 	 * Attempt some workaround for experience orbs:
 	 * prevent it getting near the player.
 	 * @param target
 	 * @param entity
 	 */
 	void repellExpOrb(Player player, ExperienceOrb orb) {
 		Location pLoc = player.getLocation();
 		Location oLoc = orb.getLocation();
 		Vector dir = oLoc.toVector().subtract(pLoc.toVector());
 		double dx = Math.abs(dir.getX());
 		double dz = Math.abs(dir.getZ());
 		if ( (dx == 0.0) && (dz == 0.0)){
 			// Special case probably never happens
 			dir.setX(0.001);
 		}
 		if ((dx < settings.expThreshold) && (dz < settings.expThreshold)){
 			Vector nDir = dir.normalize();
 			Vector newV = nDir.clone().multiply(settings.expVelocity);
 			newV.setY(0);
 			orb.setVelocity(newV);
 			if ((dx < settings.expTeleDist) && (dz < settings.expTeleDist)){
 				// maybe oLoc
 				orb.teleport(oLoc.clone().add(nDir.multiply(settings.expTeleDist)), TeleportCause.PLUGIN);
 			} 
 			if ((dx < settings.expKillDist) && (dz < settings.expKillDist)){
 				orb.remove();
 			} 
 		} 
 	}
 
 	@EventHandler(priority=EventPriority.LOW)
 	final void onEntityDamage(final EntityDamageEvent event){
 		if ( event.isCancelled() ) return;
 		final Entity entity = event.getEntity();
 		if (!(entity instanceof Player)) return;
 		final String playerName = ((Player) entity).getName();
 		final VanishConfig cfg = vanishConfigs.get(playerName.toLowerCase());
 		if (cfg == null) return;
 		if (!cfg.vanished.state || cfg.damage.state) return;
 		event.setCancelled(true);
 		if ( entity.getFireTicks()>0) entity.setFireTicks(0);
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	void onItemPickUp(PlayerPickupItemEvent event){
 		if ( event.isCancelled() ) return;
 		Player player = event.getPlayer();
 		VanishConfig cfg = vanishConfigs.get(player.getName().toLowerCase());
 		if (cfg == null) return;
 		if (!cfg.vanished.state) return;
 		if (!cfg.pickup.state) event.setCancelled(true);
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	void onItemDrop(PlayerDropItemEvent event){
 		if ( event.isCancelled() ) return;
 		Player player = event.getPlayer();
 		VanishConfig cfg = vanishConfigs.get(player.getName().toLowerCase());
 		if (cfg == null) return;
 		if (!cfg.vanished.state) return;
 		if (!cfg.drop.state) event.setCancelled(true);
 	}
 
 	/**
 	 * Only has relevance for static access by Plugin.
 	 * @param enabled
 	 */
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
 	}
 	
 	/**
 	 * Only for static access by plugin.
 	 * @return
 	 */
 	public boolean isEnabled(){
 		return enabled;
 	}
 	
 	public void setSettings(Settings settings){
 		this.settings = settings;
 	}
 	
 	/**
 	 * Adjust state of player to vanished, message player.
 	 * @param player
 	 */
 	public void doVanish(Player player) {
 		doVanish(player, true);
 	}
 	
 	/**
 	 * Adjust state of player to vanished.
 	 * @param player
 	 * @param message If to message players.
 	 */
 	public void doVanish(Player player, boolean message) {
 		long ns = System.nanoTime();
 		String name = player.getName();
 		boolean was = !addVanishedName(name);
 		String fakeQuit = null;
 		if (settings.sendFakeMessages && !settings.fakeQuitMessage.isEmpty()){
 			fakeQuit = settings.fakeQuitMessage.replaceAll("%name", name);
 			fakeQuit = fakeQuit.replaceAll("%displayname", player.getDisplayName());
 		}
 		final String msgNotify = SimplyVanish.msgLabel+ChatColor.GREEN+name+ChatColor.GRAY+" vanished.";
 		for ( Player other : Bukkit.getServer().getOnlinePlayers()){
 			if (other.getName().equals(name)) continue;
 			boolean shouldSee = shouldSeeVanished(other);
 			boolean notify = settings.notifyState && Utils.hasPermission(other, settings.notifyStatePerm);
 			if ( other.canSee(player)){
 				if (!shouldSee) hidePlayer(player, other); 
 				if (notify){
 					if (!was){
 						if (message) other.sendMessage(msgNotify);
 					}
 				} else if (!shouldSee){
 					if (fakeQuit != null) other.sendMessage(fakeQuit);
 				}
 			} else{
 				if (shouldSee) showPlayer(player, other); // added as consistency check
 				if (!was && notify){
 					if (message) other.sendMessage(msgNotify);
 				}
 			}
 		}
 		if (message) player.sendMessage(was?SimplyVanish.msgStillInvisible:SimplyVanish.msgNowInvisible);
 		SimplyVanish.stats.addStats(SimplyVanish.statsVanish, System.nanoTime()-ns);
 	}
 
 	/**
 	 * Central access point for checking if player has permission and wants to see vanished players.
 	 * @param player
 	 * @return
 	 */
 	public final boolean shouldSeeVanished(final Player player) {
 		final VanishConfig cfg = vanishConfigs.get(player.getName().toLowerCase());
 		if(cfg!=null){
 			if (!cfg.see.state) return false;
 		}
 		return Utils.hasPermission(player, "simplyvanish.see-all"); 
 	}
 
 	/**
 	 * Adjust state of player to not vanished.
 	 * @param player
 	 *  @param message If to send messages.
 	 */
 	public void doReappear(Player player, boolean message) {
 		long ns = System.nanoTime();
 		String name = player.getName();
 		boolean was = removeVanishedName(name);
 		String fakeJoin = null;
 		if (settings.sendFakeMessages && !settings.fakeJoinMessage.isEmpty()){
 			fakeJoin = settings.fakeJoinMessage.replaceAll("%name", name);
 			fakeJoin = fakeJoin.replaceAll("%displayname", player.getDisplayName());
 		}
 		final String msgNotify = SimplyVanish.msgLabel+ChatColor.RED+name+ChatColor.GRAY+" reappeared.";
 		for ( Player other : Bukkit.getServer().getOnlinePlayers()){
 			if (other.getName().equals(name)) continue;
 			boolean notify = settings.notifyState && Utils.hasPermission(other, settings.notifyStatePerm);
 			if (!other.canSee(player)){
 				showPlayer(player, other);
 				if (notify){
 					if (message) other.sendMessage(msgNotify);
 				} else if (!shouldSeeVanished(other)){
 					if (fakeJoin != null) other.sendMessage(fakeJoin);
 				}
 			} 
 			else{
 				// No need to adjust visibility.
 				if (was && notify){
 					if (message) other.sendMessage(msgNotify);
 				}
 			}
 		}
 		if (message) player.sendMessage(SimplyVanish.msgLabel+ChatColor.GRAY+"You are "+(was?"now":"still")+" "+ChatColor.RED+"visible"+ChatColor.GRAY+" to everyone!");
 		SimplyVanish.stats.addStats(SimplyVanish.statsReappear, System.nanoTime()-ns);
 	}
 	
 	/**
 	 * Heavy update for who can see this player and whom this player can see.
 	 * @param player
 	 */
 	public void updateVanishState(Player player){
 		updateVanishState(player, true);
 	}
 	
 	/**
 	 * Heavy update for who can see this player and whom this player can see and other way round.
 	 * @param player
 	 * @param message If to message the player.
 	 */
 	public void updateVanishState(Player player, boolean message){
 		long ns = System.nanoTime();
 		String playerName = player.getName();
 		Server server = Bukkit.getServer();
 		Player[] players = server.getOnlinePlayers();
 		boolean shouldSee = shouldSeeVanished(player);
 		boolean was = isVanished(playerName);
 		// Show or hide other players to player:
 		for (Player other : players){
 			if (shouldSee||!isVanished(other.getName())){
 				if (!player.canSee(other)) showPlayer(other, player);
 			} 
 			else if (player.canSee(other)) hidePlayer(other, player);
 			if (!was && !other.canSee(player)) showPlayer(player, other);   
 			
 		}
 		if (was) doVanish(player, message); // remove: a) do not save 2x b) people will get notified.	
 		SimplyVanish.stats.addStats(SimplyVanish.statsUpdateVanishState, System.nanoTime()-ns);
 	}
 	
 	/**
 	 * Unlikely that sorted is needed, but anyway.
 	 * @return
 	 */
 	public List<String> getSortedVanished(){
 		Collection<String> vanished = getVanishedPlayers();
 		List<String> sorted = new ArrayList<String>(vanished.size());
 		sorted.addAll(vanished);
 		Collections.sort(sorted);
 		return sorted;
 	}
 	
 	/**
 	 * Show player to canSee.
 	 * Delegating method, for the case of other things to be checked.
 	 * @param player The player to show.
 	 * @param canSee 
 	 */
 	void showPlayer(Player player, Player canSee){
 		if (!checkInvolved(player, canSee, "showPlayer")) return;
 		try{
 			canSee.showPlayer(player);
 		} catch(Throwable t){
 			Utils.severe("showPlayer failed (show "+player.getName()+" to "+canSee.getName()+"): "+t.getMessage());
 			t.printStackTrace();
 			onPanic(new Player[]{player, canSee});
 		}
 	}
 	
 	/**
 	 * Hide player from canNotSee.
 	 * Delegating method, for the case of other things to be checked.
 	 * @param player The player to hide.
 	 * @param canNotSee
 	 */
 	void hidePlayer(Player player, Player canNotSee){
 		if (!checkInvolved(player, canNotSee, "hidePlayer")) return;
 		try{
 			canNotSee.hidePlayer(player);
 		} catch ( Throwable t){
 			Utils.severe("hidePlayer failed (hide "+player.getName()+" from "+canNotSee.getName()+"): "+t.getMessage());
 			t.printStackTrace();
 			onPanic(new Player[]{player, canNotSee});
 		}
 	}
 	
 	/**
 	 * Do online checking and also check settings if to continue.
 	 * @param player1 The player to be shown or hidden.
 	 * @param player2
 	 * @param tag
 	 * @return true if to continue false if to abort.
 	 */
 	boolean checkInvolved(Player player1, Player player2, String tag){
 		boolean inconsistent = false;
 		if (!Utils.checkOnline(player1, tag)) inconsistent = true;
 		if (!Utils.checkOnline(player2, tag)) inconsistent = true;
 		if (settings.noAbort){
 			return true;
 		} else if (inconsistent){
 			try{
 				player1.sendMessage(SimplyVanish.msgLabel+ChatColor.RED+"Warning: Could not use "+tag+" to player: "+player2.getName());
 			} catch (Throwable t){	
 			}
 		}
 		return !inconsistent; // "true = continue = not inconsistent"
 	}
 	
 	void onPanic(Player[] involved){
 		Server server = Bukkit.getServer();
 		if ( settings.panicKickAll){
 			for ( Player player :  server.getOnlinePlayers()){
 				try{
 					player.kickPlayer(settings.panicKickMessage);
 				} catch (Throwable t){
 					// ignore
 				}
 			}
 		} 
 		else if (settings.panicKickInvolved){
 			for ( Player player : involved){
 				try{
 					player.kickPlayer(settings.panicKickMessage);
 				} catch (Throwable t){
 					// ignore
 				}
 			}
 		}
 		try{
 			Utils.sendToTargets(settings.panicMessage, settings.panicMessageTargets);
 		} catch ( Throwable t){
 			Utils.warn("[Panic] Failed to send to: "+settings.panicMessageTargets+" ("+t.getMessage()+")");
 			t.printStackTrace();
 		}
 		if (settings.panicRunCommand && !"".equals(settings.panicCommand)){
 			try{
 				server.dispatchCommand(server.getConsoleSender(), settings.panicCommand);
 			} catch (Throwable t){
 				Utils.warn("[Panic] Failed to dispathc command: "+settings.panicCommand+" ("+t.getMessage()+")");
 				t.printStackTrace();
 			}
 		}
 	}
 
 	public boolean addVanishedName(String name) {
 		VanishConfig cfg = getVanishConfig(name);
 		boolean res = false;
 		if (!cfg.vanished.state){
 			cfg.vanished.state = true;
 			cfg.changed = true;
 			res = true;
 		}
 		if (cfg.changed && settings.saveVanishedAlways) saveVanished();
 		return res;
 	}
 
 	/**
 	 * 
 	 * @param name
 	 * @return If the player was vanished.
 	 */
 	public boolean removeVanishedName(String name) {
 		VanishConfig cfg = vanishConfigs.get(name.toLowerCase());
 		if (cfg==null) return false;
 		boolean res = false;
 		if (cfg.vanished.state){
 			cfg.vanished.state = false;
 			if (!cfg.needsSave()) vanishConfigs.remove(name.toLowerCase());
 			cfg.changed = true;
 			res = true;
 		}
 		if (cfg.changed && settings.saveVanishedAlways) saveVanished();
 		return res;
 	}
 
 	public String getVanishedMessage() {
 		List<String> sorted = getSortedVanished();
 		StringBuilder builder = new StringBuilder();
 		builder.append(ChatColor.GOLD+"[VANISHED]");
 		Server server = Bukkit.getServer();
 		boolean found = false;
 		for ( String n : sorted){
 			Player player = server.getPlayerExact(n);
 			VanishConfig cfg = vanishConfigs.get(n);
 			if (!cfg.vanished.state) continue;
 			found = true;
 			boolean isNosee = !cfg.see.state; // is lower case
 			if ( player == null ){
 				builder.append(" "+ChatColor.GRAY+"("+n+")");
 				if (isNosee) builder.append(ChatColor.DARK_RED+"[NOSEE]");
 			}
 			else{
 				builder.append(" "+ChatColor.GREEN+player.getName());
 				if (!Utils.hasPermission(player, "simplyvanish.see-all")) builder.append(ChatColor.DARK_RED+"[CANTSEE]");
 				else if (isNosee) builder.append(ChatColor.RED+"[NOSEE]");
 			}
 		}
 		if (!found) builder.append(" "+ChatColor.DARK_GRAY+"<none>");
 		return builder.toString();
 	}
 	
 	/**
 	 * Get a VanishConfig, create it if necessary.<br>
 	 * (Might be from vanished, parked, or new thus put to parked).
 	 * @param playerName
 	 * @return
 	 */
 	public final VanishConfig getVanishConfig(String playerName){
 		playerName = playerName.toLowerCase();
 		VanishConfig cfg = vanishConfigs.get(playerName);
 		if (cfg != null) return cfg;
 		cfg = new VanishConfig();
 		vanishConfigs.put(playerName, cfg);
 		return cfg;
 	}
 
 	public final boolean isVanished(final String playerName) {
 		final VanishConfig cfg = vanishConfigs.get(playerName.toLowerCase());
 		if (cfg == null) return false;
 		else return cfg.vanished.state;
 	}
 
 	/**
 	 * Public access method for vanish/reappear.<br>
 	 * This will call hooks.<br>
 	 * This can also be used to force a state update, though updateVanishState should be sufficient.
 	 * 
 	 * @param playerName
 	 * @param vanished
 	 * @return 
 	 */
 	public boolean setVanished(String playerName, boolean vanished) {
 		playerName = playerName.toLowerCase();
 		// check hooks
 		boolean allow;
 		if (vanished) allow = hookUtil.callBeforeVanish(playerName);
 		else allow = hookUtil.callBeforeReappear(playerName);
 		if (!allow) return false;
 		// Do vanish or reappear:
 		Player player = Bukkit.getServer().getPlayerExact(playerName);
 		if (player != null){
 			// The simple but costly part.
 			if ( vanished) doVanish(player, true);
 			else doReappear(player, true);
 		}
 		else{
 			// Now very simple (lower-case names).
 			if (vanished) addVanishedName(playerName);
 			else removeVanishedName(playerName); 
 		}
 		// call further hooks:
 		if (vanished) hookUtil.callAfterVanish(playerName);
 		else hookUtil.callAfterReappear(playerName);
 		return true;
 	}
 
 	/**
 	 * Lower case names.<br>
 	 * Currently iterates over all VanishConfig entries.
 	 * @return
 	 */
 	public Set<String> getVanishedPlayers() {
 		Set<String> out = new HashSet<String>();
 		for (Entry<String, VanishConfig> entry : vanishConfigs.entrySet()){
 			if (entry.getValue().vanished.state) out.add(entry.getKey());
 		}
 		return out;
 	}
 
 	/**
 	 * Only set the flags, no save.
 	 * TODO: probably needs a basic mix-in permission to avoid abuse (though that would need command spam).
 	 * @param playerName
 	 * @param args
 	 * @param startIndex Start parsing flags from that index on.
 	 * @param sender For performing permission checks.
 	 * @param hasBypass Has some bypass permission (results in no checks)
 	 * @param other If is sender is other than name
 	 * @param save If to save state.
 	 */
 	public void setFlags(String playerName, String[] args, int startIndex, CommandSender sender, boolean hasBypass, boolean other, boolean save) {
 		long ns = System.nanoTime();
 		playerName = playerName.trim().toLowerCase();
 		if (playerName.isEmpty()) return;
 		final String permBase =  "simplyvanish.flags.set."+(other?"other":"self"); // bypass permission
 		if (!hasBypass) hasBypass = Utils.hasPermission(sender, permBase);
 		VanishConfig cfg = vanishConfigs.get(playerName);
 		boolean hasSomePerm = hasBypass; // indicates that the player has any permission at all.
 		if (cfg == null) cfg = new VanishConfig();
 		boolean hasClearFlag = false;
 		for ( int i = startIndex; i<args.length; i++){
 			String name = VanishConfig.getMappedFlagName(args[i].trim().toLowerCase());
 			if ( name.equals("clear")){
 				hasClearFlag = true;
 				break;
 			} 
 		}
 		VanishConfig newCfg;
 		if (hasClearFlag){
 			newCfg = new VanishConfig();
 			newCfg.set("vanished", cfg.get("vanished"));
 		}
 		else newCfg = cfg.clone();
 		
 		newCfg.readFromArray(args, startIndex, false);
 		
 		List<String> changes = cfg.getChanges(newCfg);
 		
 		// Determine permissions and apply valid changes:
 		Set<String> missing = new HashSet<String>();
 			
 		Set<String> ok = new HashSet<String>();
 		for ( String fn : changes){
 			String name = fn.substring(1);
 			if (!hasBypass && !Utils.hasPermission(sender, permBase+"."+name)) missing.add(name);
 			else{
 				hasSomePerm = true;
 				ok.add(name);
 			}
 		}
 		
 		if (!missing.isEmpty()) Utils.send(sender, SimplyVanish.msgLabel+ChatColor.RED+"Missing permission for flags: "+Utils.join(missing, ", "));
 		if (!hasSomePerm){
 			// Difficult: might be a player without ANY permission.
 			// TODO: maybe check permissions for all flags
 			Utils.send(sender, SimplyVanish.msgLabel+ChatColor.DARK_RED+"You can not set these flags.");
 			SimplyVanish.stats.addStats(SimplyVanish.statsSetFlags, System.nanoTime()-ns);
 			return;
 		}
 		// if pass:
 		vanishConfigs.put(playerName, cfg); // just to ensure it is there.
 		if (!hookUtil.callBeforeSetFlags(playerName, cfg.clone(), newCfg.clone() )){
 			if (!cfg.needsSave()) removeVanishedName(playerName);
 			Utils.send(sender, ChatColor.RED+"Action was prevented by hooks.");
 			return;
 		}
 		// Now actually apply changes to he vcfg.
 		for (String name : ok){
 			cfg.set(name, newCfg.get(name));
 		}
 		if ( save && cfg.changed && settings.saveVanishedAlways) saveVanished();
 		Player player = Bukkit.getServer().getPlayerExact(playerName);
 		if (player != null) updateVanishState(player, false);
 		if (!cfg.needsSave()) removeVanishedName(playerName);
 		hookUtil.callAfterSetFlags(playerName);
 		SimplyVanish.stats.addStats(SimplyVanish.statsSetFlags, System.nanoTime()-ns);
 	}
 
 	public void onShowFlags(CommandSender sender, String name) {
 		if ( name == null) name = sender.getName();
 		name = name.toLowerCase();
 		VanishConfig cfg = vanishConfigs.get(name);
 		if (cfg != null) sender.sendMessage(SimplyVanish.msgLabel+ChatColor.GRAY+"Flags("+name+"): "+cfg.toLine());
 		else sender.sendMessage(SimplyVanish.msgNoFlags);
 	}
 
 	public void onNotifyPing() {
 		if (!settings.pingEnabled) return;
 		for ( final Entry<String, VanishConfig> entry : vanishConfigs.entrySet()){
 			final Player player = Bukkit.getPlayerExact(entry.getKey());
 			if (player==null) continue;
 			final VanishConfig cfg = entry.getValue();
 			if (!cfg.vanished.state) continue;
 			if (!cfg.ping.state) continue;
 			player.sendMessage(SimplyVanish.msgNotifyPing);
 		}
 	}
 
 	public boolean addHook(Hook hook) {
 		return hookUtil.addHook(hook);
 	}
 
 	public boolean removeHook(Hook hook) {
 		return hookUtil.removeHook(hook);
 	}
 
 	public boolean removeHook(String hookName) {
 		return hookUtil.removeHook(hookName);
 	}
 
 	public void removeAllHooks() {
 		hookUtil.removeAllHooks();
 	}
 
 	/**
 	 * This does not put a newly generate config into vanishConfigs.
 	 * @param playerName
 	 * @param create if to create if not present.
 	 * @return
 	 */
 	public VanishConfig getVanishConfig(String playerName, boolean create) {
 		VanishConfig cfg = vanishConfigs.get(playerName.toLowerCase());
 		if (cfg != null) return cfg;
 		else if (create) return new VanishConfig();
 		else return null;
 	}
 
 	public void setVanishedConfig(String playerName, VanishConfig cfg,
 			boolean update, boolean message) {
 		VanishConfig newCfg = new VanishConfig();
 		newCfg.setAll(cfg);
 		vanishConfigs.put(playerName.toLowerCase(), newCfg);
 		if (update){
 			Player player = Bukkit.getServer().getPlayerExact(playerName);
 			if (player != null) updateVanishState(player, message);
 		}
 	}
 }

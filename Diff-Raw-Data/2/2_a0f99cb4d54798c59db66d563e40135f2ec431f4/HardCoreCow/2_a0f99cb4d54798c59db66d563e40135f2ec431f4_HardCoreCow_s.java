 package us.fitzpatricksr.cownet;
 
 import com.onarandombox.MultiverseCore.MultiverseCore;
 import com.onarandombox.MultiverseCore.api.MVDestination;
 import com.onarandombox.MultiverseCore.api.MVWorldManager;
 import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
 import com.onarandombox.MultiverseCore.destination.DestinationFactory;
 import com.onarandombox.MultiverseCore.enums.TeleportResult;
 import org.bukkit.*;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.MemorySection;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.player.*;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import us.fitzpatricksr.cownet.hardcore.HardCoreLog;
 import us.fitzpatricksr.cownet.hardcore.PlayerState;
 import us.fitzpatricksr.cownet.utils.CowNetConfig;
 import us.fitzpatricksr.cownet.utils.CowNetThingy;
 import us.fitzpatricksr.cownet.utils.CowZombeControl;
 import us.fitzpatricksr.cownet.utils.StringUtils;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /*
     The world is generated.  Player can visit and look all they want until they touch
     something, then they are in the game.  As soon as the world has at least one player
     in the game it needs to maintain at least one live player.  If at any time the number
     of live players goes to 0, the world regenerates.
 
     Timeouts.  Urg.
     If player is dead past timeout, remove them from dead queue.  If they queues are now
     empty, regen the world.  So, if they're a ghost and they try to do something, they
     will be removed from dead because of the timeout and re-added to because of the interaction.
     If they were just dead for a long time, we just remove them.  It's passive and only happens
     when the config is touched.  It's a reaper process, not something that's active.
  */
 public class HardCoreCow extends CowNetThingy implements Listener {
     private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     private static final int REAPER_FREQUENCY = 20 * 30; // 30 seconds
     private HardCoreState config;
     private HardCoreLog logFile;
     private String hardCoreWorldNames = "HardCore";
     private int safeDistance = 10;
     private MultiverseCore mvPlugin;
     private Difficulty difficulty = Difficulty.HARD;
     private double monsterBoost = 1.0d;
     private boolean allowFly = false;
     private boolean allowXRay = false;
 
     public HardCoreCow(JavaPlugin plugin, String permissionRoot, String trigger) {
         super(plugin, permissionRoot, trigger);
         if (isEnabled()) {
             reload();
             PluginManager pm = plugin.getServer().getPluginManager();
             pm.registerEvents(this, plugin);
             getPlugin().getServer().getScheduler().scheduleAsyncRepeatingTask(
                     getPlugin(),
                     new Runnable() {
                         public void run() {
                             config.reapDeadPlayers();
                         }
                     },
                     REAPER_FREQUENCY,
                     REAPER_FREQUENCY);
         }
     }
 
     @Override
     protected void reload() {
         if (mvPlugin != null) mvPlugin.decrementPluginCount();
         try {
             hardCoreWorldNames = getConfigString("worldname", hardCoreWorldNames);
             safeDistance = getConfigInt("safedistance", safeDistance);
             PlayerState.liveTimeout = getConfigLong("livetimeout", PlayerState.liveTimeout);
             PlayerState.deathDuration = getConfigLong("deathduration", PlayerState.deathDuration);
             PlayerState.timeOutGrowth = getConfigDouble("timeoutgrowth", PlayerState.timeOutGrowth);
             difficulty = Difficulty.valueOf(getConfigString("dificulty", difficulty.toString()));
             monsterBoost = Double.valueOf(getConfigString("monsterBoost", Double.toString(monsterBoost)));
             allowFly = getConfigBoolean("allowFly", allowFly);
             allowXRay = getConfigBoolean("allowXRay", allowXRay);
             config = new HardCoreState(getPlugin(), getTrigger() + ".yml");
             config.loadConfig();
             mvPlugin = (MultiverseCore) getPlugin().getServer().getPluginManager().getPlugin("Multiverse-Core");
             if (mvPlugin == null) {
                 logInfo("Could not find Multiverse-Core plugin.  Disabling self");
                 disable();
             } else {
                 mvPlugin.incrementPluginCount();
             }
             logInfo("" + getHardCoreWorldNames().length + " hardcore worlds found");
             for (String wn : getHardCoreWorldNames()) {
                 logInfo("  worldname:" + wn);
             }
             logInfo("safeDistance:" + safeDistance);
             logInfo("liveTimeout:" + PlayerState.liveTimeout);
             logInfo("deathDuration:" + PlayerState.deathDuration);
             logInfo("timeOutGrowth:" + PlayerState.timeOutGrowth);
             logInfo("difficulty:" + difficulty);
             logInfo("monsterBoost:" + monsterBoost);
             logFile = new HardCoreLog(getPlugin(), "eventlog");
         } catch (IOException e) {
             e.printStackTrace();
             disable();
         } catch (InvalidConfigurationException e) {
             e.printStackTrace();
             disable();
         }
     }
 
     @EventHandler
     protected void handlePluginDisabled(PluginDisableEvent event) {
         if (event.getPlugin() == getPlugin()) {
             mvPlugin.decrementPluginCount();
             config.saveConfig();
         }
     }
 
     @Override
     protected String getHelpString(CommandSender player) {
         PlayerState ps = config.getPlayerState(player.getName());
         long timeOut = (ps != null) ? ps.getSecondsTillTimeout() : PlayerState.deathDuration;
         return "usage: hardcore (<worldname> | info | stats | revive <player> | regen | twiddle <params>)  " +
                 "HardCore is played with no mods.  You're on your own.  " +
                 "Type /HardCore (or /hc) to enter and exit HardCore world.  " +
                 "The leave, you must be close to the spawn point.  " +
                 "You're officially in the game once you interact with something.  " +
                 "Until then you are an observer.  After you die,  " +
                 "you are a ghost for a " + StringUtils.durationString(timeOut) + " and can only observer.  " +
                 "The time you are a ghost increases with each death.  " +
                 "If everyone is dead at the same time, the world regens.  If you don't " +
                 "play for " + StringUtils.durationString(timeOut) + ", you're no longer considered in the game.";
     }
 
     @Override
     protected boolean handleCommand(CommandSender sender, Command cmd, String[] args) {
         // subcommands
         //  regen
         //  revive <player>
         //  info
         //  go
         //  --- empty takes you to the hardcore world
         if (args.length == 1) {
             if ("info".equalsIgnoreCase(args[0])
                     || "list".equalsIgnoreCase(args[0])
                     || "stats".equalsIgnoreCase(args[0])) {
                 return goInfo(sender);
             } else if ("regen".equalsIgnoreCase(args[0])) {
                 return goRegen(sender);
             }
         } else if (args.length == 2) {
             if ("revive".equalsIgnoreCase(args[0])) {
                 return goRevive(sender, args[1]);
             }
         } else if (args.length == 3) {
             if ("twiddle".equalsIgnoreCase(args[0])) {
                 return goTwiddle(sender, args[1], args[2]);
             }
         }
         return super.handleCommand(sender, cmd, args);
     }
 
     @Override
     protected boolean handleCommand(Player player, Command cmd, String[] args) {
         // handle commands that operate on the player who issued the command
         if (args.length == 0) {
             return goHardCore(player, null);
         } else if ((args.length == 1) && (isHardCoreWorld(args[0]))) {
             return goHardCore(player, args[0]);
         }
         return false;
     }
 
     private boolean goHardCore(Player player, String worldName) {
         if (isHardCoreWorld(player.getWorld()) && (worldName == null)) {
             //Player is on HARD CORE world already and wants to leave.
             //if they are close to spawn we will rescue them
             World world = player.getWorld();
             Location spawn = world.getSpawnLocation();
             Location loc = player.getLocation();
             double diff = spawn.distance(loc);
             if ((diff < safeDistance) || config.isDead(player.getName()) || hasPermissions(player, "canalwaysescape")) {
                 World exitPlace = mvPlugin.getMVWorldManager().getSpawnWorld().getCBWorld();
                 SafeTTeleporter teleporter = mvPlugin.getSafeTTeleporter();
                 teleporter.safelyTeleport(null, player, exitPlace.getSpawnLocation(), true);
                 player.sendMessage("You must logoff and log into the server to reenable FlyMod.");
             } else {
                 player.sendMessage("You need to make a HARD CORE effort to get closer to the spawn point.");
             }
         } else {
             // player is in some other world and wants to go HARD CORE
             if (worldName == null) worldName = getHardCoreWorldNames()[0];
             MVWorldManager mgr = mvPlugin.getMVWorldManager();
             if (!mgr.isMVWorld(worldName) && !config.generateNewWorld(worldName)) {
                 //this is an error.  Error message sent to console already.
                 player.sendMessage("Something is wrong with HARD CORE.  You can't be transported at the moment.");
                 return true;
             }
             String playerName = player.getName();
             if (config.isDead(playerName)) {
                 player.sendMessage("You're dead, so you will roam the world as a ghost for the next " +
                         StringUtils.durationString(config.getSecondsTillTimeout(playerName)) + ".");
             }
             SafeTTeleporter teleporter = mvPlugin.getSafeTTeleporter();
             DestinationFactory destFactory = mvPlugin.getDestFactory();
             MVDestination destination = destFactory.getDestination(worldName);
             TeleportResult result = teleporter.safelyTeleport(player, player, destination);
             switch (result) {
                 case FAIL_PERMISSION:
                     player.sendMessage("You don't have permissions to go to " + worldName);
                     break;
                 case FAIL_UNSAFE:
                     player.sendMessage("Can't find a safe spawn location for you.");
                     break;
                 case FAIL_TOO_POOR:
                     player.sendMessage("You don't have enough money.");
                     break;
                 case FAIL_INVALID:
                     player.sendMessage(worldName + " is temporarily out of service.");
                     break;
                 case SUCCESS:
                     player.sendMessage("Good luck.");
                     logFile.log(player.getName() + " became hardcore");
                     break;
                 case FAIL_OTHER:
                 default:
                     player.sendMessage("Something went wrong.  Something.  Stuff.");
                     break;
             }
         }
         return true;
     }
 
     private boolean goInfo(CommandSender player) {
         if (!hasPermissionsOrOp(player, "info")) {
             player.sendMessage("Sorry, you don't have permission.");
             return true;
         }
         String playerName = player.getName();
         String duration = StringUtils.durationString(config.getSecondsTillTimeout(playerName));
         if (config.isDead(playerName)) {
             player.sendMessage("You're dead for " + duration + ".  Not very HARD CORE.");
         } else if (config.isLive(playerName)) {
             player.sendMessage("You've been very HARD CORE up 'till now.");
             player.sendMessage("You need to do something in " + duration + " to stay in the game.");
         } else {
             player.sendMessage("You are not in the HARD CORE game.  Type /hc and do something to enter.");
         }
         player.sendMessage("HARE CORE worlds: " + hardCoreWorldNames);
         player.sendMessage("  --- HARD CORE Rankings ---");
         player.sendMessage("  Name  - Deaths TimeOn Placed Broken Kills LastActive");
         for (PlayerState p : config.getRankedPlayers()) {
             player.sendMessage("    " + p.name
                     + ((p.isLive) ? "" : "(dead)")
                     + " -  D:" + p.deathCount
                     + "  T:" + StringUtils.durationString(p.getSecondsInHardcore())
                     + "  P:" + p.blocksPlaced
                     + "  B:" + p.blocksBroken
                     + "  K:" + p.mobsKilled
                     + "  L:" + StringUtils.durationString((System.currentTimeMillis() - p.lastActivity) / 1000));
         }
 
         return true;
     }
 
     private boolean goRegen(CommandSender player) {
         if (!hasPermissionsOrOp(player, "regen")) {
             player.sendMessage("Sorry, you're not HARD CORE enough.  Come back when you're more HARD CORE.");
             return true;
         }
         config.generateNewWorlds();
         player.sendMessage("The HARD CORE worlds have been regenerated HARDer and more CORE than ever.");
         logFile.log(hardCoreWorldNames + " regenerated");
         return true;
     }
 
     private boolean goRevive(CommandSender player, String arg) {
         if (!hasPermissionsOrOp(player, "revive")) {
             player.sendMessage("Sorry, you're not HARD CORE enough to revive other players.");
         } else if (!config.isDead(arg)) {
             player.sendMessage(arg + " is still going at it HARD CORE and isn't dead.");
         } else {
             config.markPlayerUndead(arg);
             player.sendMessage(arg + " has been revived, be is still not as HARD CORE as you.");
             logFile.log(arg + " revived by " + player.getName());
         }
         return true;
     }
 
     private boolean goTwiddle(CommandSender player, String playerName, String param) {
        if (!hasPermissionsOrOp(player, "twiddle")) {
             PlayerState ps = config.getPlayerState(playerName);
             if (ps == null) {
                 player.sendMessage(playerName + " is not in the game.");
             } else {
                 String option = param.substring(0, 2).toLowerCase();
                 long arg = Long.parseLong(param.substring(2).toLowerCase());
                 if ("d:".equals(option)) {
                     ps.deathCount = (int) arg;
                 } else if ("t:".equals(option)) {
                     ps.timeInGame = arg;
                 } else if ("p:".equals(option)) {
                     ps.blocksPlaced = arg;
                 } else if ("b:".equals(option)) {
                     ps.blocksBroken = arg;
                 } else if ("k:".equals(option)) {
                     ps.mobsKilled = arg;
                 } else if ("l:".equals(option)) {
                     ps.lastActivity = arg;
                 } else {
                     player.sendMessage("Could not set property " + option + " on player " + playerName);
                 }
             }
         } else {
             player.sendMessage("You don't have permissions to set player properties");
         }
         return true;
     }
 
     // ---- Event handlers
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
         debugInfo("onPlayerChangedWorld");
         handleWorldChange(event.getPlayer(), event.getFrom());
     }
 
     @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
     public void onPlayerTeleport(PlayerTeleportEvent event) {
         if (event.isCancelled()) return;
         handleWorldChange(event.getPlayer(), event.getFrom().getWorld());
     }
 
     private void handleWorldChange(Player player, World fromWorld) {
         // set up the proper player settings for HARD CORE
         if (isHardCoreWorld(player.getWorld())) {
             // teleported to hardcore
             config.setWasOp(player.getName(), player.isOp());
             if (!hasPermissions(player, "keepop")) {
                 player.setOp(false);
                 player.setAllowFlight(false);
                 player.setGameMode(GameMode.SURVIVAL);
                 CowZombeControl.setAllowFly(player, allowFly);
                 CowZombeControl.setAllowMap(player, allowXRay);
                 CowZombeControl.setAllowXray(player, allowXRay);
                 CowZombeControl.setAllowNoClip(player, allowXRay);
                 CowZombeControl.setAllowCheat(player, allowXRay);
             }
             config.playerEnteredHardCore(player.getName());
             logFile.log(player.getName() + " entered " + player.getWorld().getName() + "  op = " + player.isOp());
         } else if (isHardCoreWorld(fromWorld)) {
             // teleported from hardcore
             player.setAllowFlight(true);
             CowZombeControl.setAllowMods(player, true);
             if (hasPermissions(player, "keepop")) {
                 player.setOp(true);
             } else {
                 player.setOp(config.wasOp(player.getName()));
             }
             config.playerLeftHardCore(player.getName());
             logFile.log(player.getName() + " left " + fromWorld);
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerInteract(PlayerInteractEvent event) {
         // --- Stop Ghosts from doing things
         if (event.isCancelled()) return;
         if (!isHardCoreWorld(event.getPlayer().getWorld())) return;
         if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR) {
             return;
         }
         String playerName = event.getPlayer().getName();
         if (config.isDead(playerName)) {
             debugInfo("Ghost event");
             event.getPlayer().sendMessage("You're dead for " + StringUtils.durationString(config.getSecondsTillTimeout(playerName)));
             event.setCancelled(true);
         } else {
             config.playerActivity(playerName);
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onBlockPlace(BlockPlaceEvent event) {
         if (event.isCancelled()) return;
         if (!isHardCoreWorld(event.getPlayer().getWorld())) return;
         String playerName = event.getPlayer().getName();
         if (config.isDead(playerName)) {
             debugInfo("Ghost event");
             event.getPlayer().sendMessage("You're dead for " + StringUtils.durationString(config.getSecondsTillTimeout(playerName)));
             event.setCancelled(true);
         } else {
             config.accrueBlockPlaced(playerName);
             config.playerActivity(playerName);
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onBlockBreak(BlockBreakEvent event) {
         if (event.isCancelled()) return;
         if (!isHardCoreWorld(event.getPlayer().getWorld())) return;
         String playerName = event.getPlayer().getName();
         if (config.isDead(playerName)) {
             debugInfo("Ghost event");
             event.getPlayer().sendMessage("You're dead for " + StringUtils.durationString(config.getSecondsTillTimeout(playerName)));
             event.setCancelled(true);
         } else {
             config.accrueBlockBroken(playerName);
             config.playerActivity(playerName);
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPickupItem(PlayerPickupItemEvent event) {
         if (event.isCancelled()) return;
         if (!isHardCoreWorld(event.getPlayer().getWorld())) return;
         String playerName = event.getPlayer().getName();
         if (config.isDead(playerName)) {
             debugInfo("Ghost event");
             event.getPlayer().sendMessage("You're dead for " + StringUtils.durationString(config.getSecondsTillTimeout(playerName)));
             event.setCancelled(true);
         } else {
             config.playerActivity(playerName);
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onGameModeChange(PlayerGameModeChangeEvent event) {
         if (event.isCancelled()) return;
         World world = event.getPlayer().getWorld();
         if (isHardCoreWorld(world)) {
             event.setCancelled(true);
             event.getPlayer().sendMessage("Can't change game modes.  " + world.getName() + " is HARD CORE.");
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onEntityDamage(EntityDamageEvent event) {
         if (event.isCancelled()) return;
         if (!isHardCoreWorld(event.getEntity().getWorld())) return;
         Entity entity = event.getEntity();
         if (entity instanceof Player) {
             // ghosts can't be hurt
             String playerName = ((Player) entity).getName();
             if (config.isDead(playerName)) {
                 event.setCancelled(true);
             }
         }
         if (event instanceof EntityDamageByEntityEvent) {
             final EntityDamageByEntityEvent target = (EntityDamageByEntityEvent) event;
             final Entity damager = target.getDamager();
             if (damager instanceof Player) {
                 String playerName = ((Player) damager).getName();
                 if (config.isDead(playerName)) {
                     event.setCancelled(true);
                 }
             }
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerDeath(EntityDeathEvent event) {
         if (!isHardCoreWorld(event.getEntity().getWorld())) return;
         Entity entity = event.getEntity();
         if (entity instanceof Player) {
             Player player = (Player) entity;
             String playerName = player.getName();
             config.markPlayerDead(playerName);
             logFile.log(player.getName() + " died ");
         } else {
             // OK, something else was killed.  Not a player.
             EntityDamageEvent lastDamageEvent = entity.getLastDamageCause();
             if (lastDamageEvent != null && lastDamageEvent instanceof EntityDamageByEntityEvent) {
                 Entity damager = ((EntityDamageByEntityEvent) lastDamageEvent).getDamager();
                 Player killer = null;
                 if (damager instanceof Arrow) {
                     Arrow arrow = (Arrow) damager;
                     if (arrow.getShooter() instanceof Player) {
                         killer = (Player) arrow.getShooter();
                     }
                 } else if (damager instanceof Player) {
                     killer = (Player) damager;
                 }
                 if (killer != null) {
                     config.accrueMobKilled(killer.getName());
                 }
             }
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onEntityTarget(EntityTargetEvent event) {
         if (event.isCancelled() || event.getTarget() == null) return;
         if (!isHardCoreWorld(event.getTarget().getWorld())) return;
         if (event.getTarget() instanceof Player) {
             String playerName = ((Player) event.getTarget()).getName();
             if (config.isDead(playerName)) {
                 event.setCancelled(true);
             }
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
         if (event.isCancelled()) return;
         if (!isHardCoreWorld(event.getEntity().getWorld())) return;
         if (event.getEntity() instanceof Player) {
             String playerName = ((Player) event.getEntity()).getName();
             if (config.isDead(playerName)) {
                 event.setCancelled(true);
             }
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerLogin(PlayerLoginEvent event) {
     }
 
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onPlayerJoin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
         debugInfo("onPlayerJoin: " + player.getName());
         if (isHardCoreWorld(player.getWorld())) {
             config.playerEnteredHardCore(player.getName());
             debugInfo("  hardcore");
         } else {
             debugInfo("  softy (" + player.getWorld().getName() + ")");
         }
         // just dump the info through the command handler even though it's a hack
         goInfo(player);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerQuit(PlayerQuitEvent event) {
         debugInfo("onPlayerQuit");
         Player player = event.getPlayer();
         if (isHardCoreWorld(player.getWorld())) {
             config.playerLeftHardCore(player.getName());
         }
     }
 
     private boolean isHardCoreWorld(World w) {
         return isHardCoreWorld(w.getName());
     }
 
     private boolean isHardCoreWorld(String worldName) {
         return hardCoreWorldNames.toLowerCase().contains(worldName.toLowerCase());
     }
 
     private String[] getHardCoreWorldNames() {
         return hardCoreWorldNames.split(",");
     }
 
     //
     // Persistent state methods (ex. live vs. dead)
     //
 
     /*
        Configuration looks like this:
        hardcorecow.creationDate: creationDate
        hardcorecow.liveplayers: player1,player2,player3
        hardcorecow.deadplayers: player1,player2,player3
     */
     private class HardCoreState extends CowNetConfig {
         private boolean regenIsAlreadyScheduled = false;
         private Map<String, PlayerState> allPlayers = new HashMap<String, PlayerState>();
         private String creationDate = "unknown";
         private String name;
 
         public HardCoreState(JavaPlugin plugin, String name) {
             super(plugin);
             this.name = name;
         }
 
         protected String getFileName() {
             return name;
         }
 
         public void loadConfig() throws IOException, InvalidConfigurationException {
             super.loadConfig();
             creationDate = getString("creationdate", creationDate);
 
             allPlayers.clear();
 
             if (get("players") instanceof MemorySection) {
                 MemorySection section = (MemorySection) get("players");
                 for (String key : section.getKeys(false)) {
                     PlayerState ps = (PlayerState) section.get(key);
                     allPlayers.put(key.toLowerCase(), ps);
                 }
             }
         }
 
         public void saveConfig() {
             debugInfo("saveConfig()");
             set("creationdate", creationDate);
             set("players", allPlayers);
             try {
                 super.saveConfig();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
         /*
         Mark as player as dead and return true if that was the last
         active player.
          */
         public void markPlayerDead(String player) {
             if (isLive(player)) {
                 // this player was live in the game, so mark them dead.
                 getPlayerState(player).setIsDead();
                 saveConfig();
                 debug("markPlayerDead " + player);
                 logFile.log(player + " died ");
             }
         }
 
         /*
         This method moves someone from the dead queue to the live
         queue ONLY.  It doesn't add them to the live queue
         if they are not already there.
          */
         public void markPlayerUndead(String player) {
             if (!isDead(player)) return;
             getPlayerState(player).setIsLive();
             saveConfig();
             debug("markPlayerUndead " + player);
             logFile.log(player + " revived ");
         }
 
         public boolean isDead(String player) {
             PlayerState ps = getPlayerState(player);
             return (ps != null) && !ps.isLive;
         }
 
         public boolean isLive(String player) {
             PlayerState ps = getPlayerState(player);
             return (ps != null) && ps.isLive;
         }
 
         public boolean isUnknown(String player) {
             return getPlayerState(player) == null;
         }
 
         public boolean wasOp(String player) {
             PlayerState state = getPlayerState(player);
             return ((state != null) && state.wasOp);
         }
 
         public void setWasOp(String player, boolean isOp) {
             if (wasOp(player) != isOp) {
                 getOrCreatePlayerState(player).setWasOp(isOp);
                 saveConfig();
             }
         }
 
         /*
         This is the primary way to put a player into the game and to
         keep them there.
          */
         public void playerActivity(String player) {
             if (isDead(player)) return;
             getOrCreatePlayerState(player).noteActivity();
         }
 
         private PlayerState getPlayerState(String name) {
             return allPlayers.get(name.toLowerCase());
         }
 
         private PlayerState getOrCreatePlayerState(String name) {
             PlayerState result = allPlayers.get(name.toLowerCase());
             if (result == null) {
                 result = new PlayerState(name);
                 allPlayers.put(name.toLowerCase(), result);
             }
             return result;
         }
 
         public Set<PlayerState> getLivePlayers() {
             Set<PlayerState> result = new HashSet<PlayerState>();
             for (PlayerState player : allPlayers.values()) {
                 if (isLive(player.name)) {
                     result.add(player);
                 }
             }
             return result;
         }
 
         public Set<PlayerState> getDeadPlayers() {
             Set<PlayerState> result = new HashSet<PlayerState>();
             for (PlayerState player : allPlayers.values()) {
                 if (isDead(player.name)) {
                     result.add(player);
                 }
             }
             return result;
         }
 
         public List<PlayerState> getRankedPlayers() {
             List<PlayerState> result = new LinkedList<PlayerState>(allPlayers.values());
             Collections.sort(result);
             return result;
         }
 
         public long getSecondsTillTimeout(String name) {
             PlayerState ps = getPlayerState(name);
             if (ps == null) return 0;
             return ps.getSecondsTillTimeout();
         }
 
         public String getCreationDate() {
             return creationDate;
         }
 
         public void resetWorldState() {
             creationDate = dateFormat.format(new Date());
             allPlayers.clear();
             saveConfig();
             debug("resetWorldState");
         }
 
         public void playerEnteredHardCore(String name) {
             PlayerState ps = getOrCreatePlayerState(name);
             ps.playerEnteredHardCore();
             debugInfo("playerEnteredHardCore(" + name + ") -> " + ps.getSecondsInHardcore());
         }
 
         public void playerLeftHardCore(String name) {
             PlayerState ps = getPlayerState(name);
             if (ps != null) {
                 ps.playerLeftHardCore();
                 debugInfo("playerLeftHardCore(" + name + ") -> " + ps.getSecondsInHardcore());
                 saveConfig();
             }
         }
 
         public void accrueBlockPlaced(String name) {
             PlayerState ps = getPlayerState(name);
             if (ps != null) ps.accrueBlockPlaced();
         }
 
         public void accrueBlockBroken(String name) {
             PlayerState ps = getPlayerState(name);
             if (ps != null) ps.accrueBlockBroken();
         }
 
         public void accrueMobKilled(String name) {
             PlayerState ps = getPlayerState(name);
             if (ps != null) ps.accrueMobKilled();
         }
 
         private void reapDeadPlayers() {
             if (allPlayers.size() == 0) return;
             LinkedList<String> playersToStomp = new LinkedList<String>();
             for (PlayerState player : allPlayers.values()) {
                 if (player.getSecondsTillTimeout() == 0) {
                     // It's been so long since we've seen this player we don't
                     // count them as live anymore.
                     playersToStomp.add(player.name.toLowerCase());
                     debugInfo("Stomping on " + player);
                 } else {
                     debugInfo("Timeremaining: " + player.getSecondsTillTimeout() + "  " + player);
                 }
             }
             for (String playerName : playersToStomp) {
                 PlayerState ps = allPlayers.get(playerName);
                 if (ps.isLive) {
                     allPlayers.remove(playerName);
                     logFile.log(playerName + " removed because of inactivity");
                 } else {
                     ps.setIsLive();
                     logFile.log(playerName + " death penalty complete.  Now alive.");
                     Player player = getPlugin().getServer().getPlayer(ps.name);
                     if (player != null) {
                         player.sendMessage("You are now alive again in HARD CORE.  Got to it soldier!");
                     }
                 }
             }
             config.saveConfig();
             debug("Reaping complete");
             if (getLivePlayers().size() == 0) {
                 // if the last live player was removed, regen the world
                 getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
                     public void run() {
                         generateNewWorlds();
                         resetWorldState();
                         getPlugin().getServer().broadcastMessage("Seems like things were too HARD CORE.  " +
                                 "The HARD CORE worlds have been regenerated to be a bit more fluffy for you softies.");
                     }// end of run
                 }, 40);
             }
         }
 
         private void generateNewWorlds() {
             for (String worldName : getHardCoreWorldNames()) {
                 generateNewWorld(worldName);
             }
         }
 
         private boolean generateNewWorld(String worldName) {
             logInfo("generateNewWorld " + worldName);
             if (regenIsAlreadyScheduled) {
                 logInfo("generateNewWorld " + worldName + " aborted.  Regen already in progress.");
                 return true;
             }
             try {
                 regenIsAlreadyScheduled = true;
                 MVWorldManager mgr = mvPlugin.getMVWorldManager();
                 if (mgr.isMVWorld(worldName)) {
                     mgr.removePlayersFromWorld(worldName);
                     if (!mgr.deleteWorld(worldName)) {
                         logInfo("Agh!  Can't regen " + worldName);
                         return false;
                     }
                 }
                 //TODO hey jf - can we create this world in another thread?
                 if (mgr.addWorld(worldName,
                         World.Environment.NORMAL,
                         "" + (new Random().nextLong()),
                         WorldType.NORMAL,
                         true,
                         null,
                         true)) {
                     config.resetWorldState();
                     World w = mgr.getMVWorld(worldName).getCBWorld();
                     w.setDifficulty(difficulty);
                     w.setTicksPerMonsterSpawns((int) (w.getTicksPerMonsterSpawns() * monsterBoost) + 1);
                     logInfo(worldName + " has been regenerated.");
                     return true;
                 } else {
                     logInfo("Oh No's! " + worldName + " don wurk.");
                     return false;
                 }
             } finally {
                 regenIsAlreadyScheduled = false;
             }
         }
 
         private void debug(String msg) {
             debugInfo(msg);
             debugInfo("  World: " + hardCoreWorldNames);
             debugInfo("  Created: " + creationDate);
             debugInfo("  Players: ");
             for (String name : allPlayers.keySet()) {
                 PlayerState ps = allPlayers.get(name);
                 debugInfo("    " + ps.toString());
             }
         }
     }
 
 
     // --- Hachky test
     public static void main(String[] args) {
         PlayerState test = new PlayerState("Player1");
         Map<String, Object> map = test.serialize();
         for (String key : map.keySet()) {
             System.out.println(key);
             System.out.println("  " + map.get(key));
         }
         test = new PlayerState(map);
         System.out.println(test);
 
         test.setIsLive();
         test.getSecondsTillTimeout();
         test.noteActivity();
         test.getSecondsTillTimeout();
         test.setIsDead();
         test.getSecondsTillTimeout();
     }
 }
 
 

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
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.configuration.serialization.ConfigurationSerialization;
 import org.bukkit.configuration.serialization.SerializableAs;
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
 import us.fitzpatricksr.cownet.utils.CowNetConfig;
 import us.fitzpatricksr.cownet.utils.CowNetThingy;
 import us.fitzpatricksr.cownet.utils.CowZombeControl;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
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
     private String worldName = "HardCoreCow";
     private int safeDistance = 10;
     private MultiverseCore mvPlugin;
     private static long liveTimeout = 7 * 24 * 60 * 60;  //live players keep things going for 7 days.
     private static long deathDuration = 60;  //default time before you are removed from game in seconds.
     private static double timeOutGrowth = 2.0; //the rate at which timeout increases.
     private Difficulty difficulty = Difficulty.HARD;
     private double monsterBoost = 1.0d;
 
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
             worldName = getConfigString("worldname", worldName);
             safeDistance = getConfigInt("safedistance", safeDistance);
             liveTimeout = getConfigLong("livetimeout", liveTimeout);
             deathDuration = getConfigLong("deathduration", deathDuration);
             timeOutGrowth = getConfigDouble("timeoutgrowth", timeOutGrowth);
             difficulty = Difficulty.valueOf(getConfigString("dificulty", difficulty.toString()));
             monsterBoost = Double.valueOf(getConfigString("monsterBoost", Double.toString(monsterBoost)));
             config = new HardCoreState(getPlugin(), getTrigger() + ".yml");
             config.loadConfig();
             mvPlugin = (MultiverseCore) getPlugin().getServer().getPluginManager().getPlugin("Multiverse-Core");
             if (mvPlugin == null) {
                 logInfo("Could not find Multiverse-Core plugin.  Disabling self");
                 disable();
             } else {
                 //TODO: hey jf - there needs to be a way to do a teardown on disable.
                 mvPlugin.incrementPluginCount();
             }
             logInfo("worldname:" + worldName);
             logInfo("safeDistance:" + safeDistance);
             logInfo("liveTimeout:" + liveTimeout);
             logInfo("deathDuration:" + deathDuration);
             logInfo("timeOutGrowth:" + timeOutGrowth);
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
         long timeOut = (ps != null) ? ps.getSecondsTillTimeout() : deathDuration;
         return "usage: hardcore (go | info | stats | revive <player> | regen)  " +
                 "HardCore is played with no mods.  You're on your own.  " +
                 "Type /HardCore (or /hc) to enter and exit HardCore world.  " +
                 "The leave, you must be close to the spawn point.  " +
                 "You're officially in the game once you interact with something.  " +
                 "Until then you are an observer.  After you die,  " +
                 "you are a ghost for a " + durationString(timeOut) + " and can only observer.  " +
                 "The time you are a ghost increases with each death.  " +
                 "If everyone is dead at the same time, the world regens.  If you don't " +
                 "play for " + durationString(timeOut) + ", you're no longer considered in the game.";
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
         }
         return super.handleCommand(sender, cmd, args);
     }
 
     @Override
     protected boolean handleCommand(Player player, Command cmd, String[] args) {
         // handle commands that operate on the player who issued the command
         if (args.length == 0) {
             return goHardCore(player);
         } else if ((args.length == 1) && ("go".equalsIgnoreCase((args[0])))) {
             return goHardCore(player);
         }
         return false;
     }
 
     private boolean goHardCore(Player player) {
         if (isHardCoreWorld(player.getWorld())) {
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
             } else {
                 player.sendMessage("You need to make a HARD CORE effort to get closer to the spawn point.");
             }
         } else {
             // player is in some other world and wants to go HARD CORE
             MVWorldManager mgr = mvPlugin.getMVWorldManager();
             if (!mgr.isMVWorld(worldName) && !config.generateNewWorld()) {
                 //this is an error.  Error message sent to console already.
                 player.sendMessage("Something is wrong with HARD CORE.  You can't be transported at the moment.");
                 return true;
             }
             String playerName = player.getName();
             if (config.isDead(playerName)) {
                 player.sendMessage("You're dead, so you will roam the world as a ghost for the next " +
                         durationString(config.getSecondsTillTimeout(playerName)) + ".");
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
         if (!hasPermissions(player, "info")) {
             player.sendMessage("Sorry, you don't have permission.");
             return true;
         }
         String playerName = player.getName();
         String duration = durationString(config.getSecondsTillTimeout(playerName));
         if (config.isDead(playerName)) {
             player.sendMessage("You're dead for " + duration + ".  Not very HARD CORE.");
         } else if (config.isLive(playerName)) {
             player.sendMessage("You've been very HARD CORE up 'till now.");
             player.sendMessage("You need to do something in " + duration + " to stay in the game.");
         } else {
             player.sendMessage("You are not in the HARD CORE game.  Type /hc and do something to enter.");
         }
         player.sendMessage("  World: " + worldName);
         player.sendMessage("  Created: " + config.getCreationDate());
         player.sendMessage("  Dead players: ");
         for (PlayerState p : config.getDeadPlayers()) {
             player.sendMessage("    " + p.name
                     + "  Deaths:" + p.deathCount
                     + "  Time: " + durationString(p.getSecondsInHardcore())
                     + "  Placed: " + p.blocksPlaced
                     + "  Broken: " + p.blocksBroken
                     + "  Kills: " + p.mobsKilled
                     + "  Last on: " + dateFormat.format(new Date(p.lastActivity))
             );
         }
         player.sendMessage("  Live players: ");
         for (PlayerState p : config.getLivePlayers()) {
             player.sendMessage("    " + p.name
                     + "  Deaths:" + p.deathCount
                     + "  Time: " + durationString(p.getSecondsInHardcore())
                     + "  Placed: " + p.blocksPlaced
                     + "  Broken: " + p.blocksBroken
                     + "  Kills: " + p.mobsKilled
                     + "  Last on: " + dateFormat.format(new Date(p.lastActivity))
             );
         }
         return true;
     }
 
     private boolean goRegen(CommandSender player) {
         if (!hasPermissions(player, "regen")) {
             player.sendMessage("Sorry, you're not HARD CORE enough.  Come back when you're more HARD CORE.");
             return true;
         }
         if (config.generateNewWorld()) {
             player.sendMessage(worldName + " has been regenerated HARDer and more CORE than ever.");
             logFile.log(worldName + " regenerated");
         } else {
             player.sendMessage(worldName + " is too HARD CORE to be regenerated.");
         }
         return true;
     }
 
     private boolean goRevive(CommandSender player, String arg) {
         if (!hasPermissions(player, "revive")) {
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
 
     // ---- Event handlers
 
     @EventHandler
     public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
         debugInfo("onPlayerChangedWorld");
         handleWorldChange(event.getPlayer(), event.getFrom());
     }
 
     @EventHandler(priority = EventPriority.LOW)
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
                 CowZombeControl.setAllowMods(player, false);
             }
             config.playerEnteredHardCore(player.getName());
             logFile.log(player.getName() + " entered " + worldName + "  op = ");
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
             logFile.log(player.getName() + " left " + worldName);
         }
     }
 
     @EventHandler
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
             event.getPlayer().sendMessage("Your're dead for " + durationString(config.getSecondsTillTimeout(playerName)));
             event.setCancelled(true);
         } else {
             config.playerActivity(playerName);
         }
     }
 
     @EventHandler
     public void onBlockPlace(BlockPlaceEvent event) {
         if (event.isCancelled()) return;
         if (!isHardCoreWorld(event.getPlayer().getWorld())) return;
         String playerName = event.getPlayer().getName();
         if (config.isDead(playerName)) {
             debugInfo("Ghost event");
             event.getPlayer().sendMessage("Your're dead for " + durationString(config.getSecondsTillTimeout(playerName)));
             event.setCancelled(true);
         } else {
             config.accrueBlockPlaced(playerName);
             config.playerActivity(playerName);
         }
     }
 
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
         if (event.isCancelled()) return;
         if (!isHardCoreWorld(event.getPlayer().getWorld())) return;
         String playerName = event.getPlayer().getName();
         if (config.isDead(playerName)) {
             debugInfo("Ghost event");
             event.getPlayer().sendMessage("Your're dead for " + durationString(config.getSecondsTillTimeout(playerName)));
             event.setCancelled(true);
         } else {
             config.accrueBlockBroken(playerName);
             config.playerActivity(playerName);
         }
     }
 
     @EventHandler
     public void onPickupItem(PlayerPickupItemEvent event) {
         if (event.isCancelled()) return;
         if (!isHardCoreWorld(event.getPlayer().getWorld())) return;
         String playerName = event.getPlayer().getName();
         if (config.isDead(playerName)) {
             debugInfo("Ghost event");
             event.getPlayer().sendMessage("Your're dead for " + durationString(config.getSecondsTillTimeout(playerName)));
             event.setCancelled(true);
         } else {
             config.playerActivity(playerName);
         }
     }
 
     @EventHandler
     public void onGameModeChange(PlayerGameModeChangeEvent event) {
         if (event.isCancelled()) return;
         if (isHardCoreWorld(event.getPlayer().getWorld())) {
             event.setCancelled(true);
             event.getPlayer().sendMessage("Can't change game modes.  " + worldName + " is HARD CORE.");
         }
     }
 
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
                 String playerName = ((Player) entity).getName();
                 if (config.isDead(playerName)) {
                     event.setCancelled(true);
                 }
             }
         }
     }
 
     @EventHandler
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
 
     @EventHandler
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
 
     @EventHandler
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
 
     @EventHandler
     public void onPlayerLogin(PlayerLoginEvent event) {
         Player player = event.getPlayer();
         debugInfo("onPlayerLogin: " + player.getName());
         if (isHardCoreWorld(player.getWorld())) {
             config.playerEnteredHardCore(player.getName());
             debugInfo("  hardcore");
         } else {
             debugInfo("  softy (" + player.getWorld().getName() + ")");
         }
     }
 
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent event) {
         debugInfo("onPlayerQuit");
         Player player = event.getPlayer();
         if (isHardCoreWorld(player.getWorld())) {
             config.playerLeftHardCore(player.getName());
         }
     }
 
     private boolean isHardCoreWorld(World w) {
         return worldName.equalsIgnoreCase(w.getName());
     }
 
     private static String durationString(long duration) {
         if (duration <= 0) {
             return "just a few seconds";
         } else {
             return String.format("%02d:%02d:%02d", duration / 3600, (duration % 3600) / 60, (duration % 60));
         }
     }
 
     //
     // Persistent state methods (ex. live vs. dead)
     //
 
     private class HardCoreLog extends CowNetConfig {
         private PrintWriter log;
         private String nameRoot;
 
         public HardCoreLog(JavaPlugin plugin, String name) throws IOException {
             super(plugin);
             nameRoot = name;
         }
 
         protected String getFileName() {
             return nameRoot + "-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".txt";
         }
 
         public void log(String message) {
             try {
                 log = new PrintWriter(new BufferedWriter(new FileWriter(getConfigFile(), true)));
                 String timeStamp = dateFormat.format(new Date());
                 log.println("[" + timeStamp + "] " + message);
                 log.flush();
                 log.close();
             } catch (Exception e) {
                 //if we can't write log file, whatever
                 e.printStackTrace();
             }
         }
     }
 
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
                         generateNewWorld();
                         resetWorldState();
                         getPlugin().getServer().broadcastMessage("Seems like " + worldName + " was too HARD CORE.  " +
                                 "It's been regenerated to be a bit more fluffy for you softies.");
                     }// end of run
                 }, 40);
             }
         }
 
         private boolean generateNewWorld() {
             if (regenIsAlreadyScheduled) return true;
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
             debugInfo("  World: " + worldName);
             debugInfo("  Created: " + creationDate);
             debugInfo("  Players: ");
             for (String name : allPlayers.keySet()) {
                 PlayerState ps = allPlayers.get(name);
                 debugInfo("    " + ps.toString());
             }
         }
     }
 
     static {
         ConfigurationSerialization.registerClass(PlayerState.class);
     }
 
     @SerializableAs("PlayerState")
     public static class PlayerState implements ConfigurationSerializable {
         public String name;         // come si chiama
         public long lastActivity;   // either time when player should be removed from allPlayers list
         public boolean isLive;      // hey?  You still there?
         public boolean wasOp;       // was this player an op outside hardcore
 
         public int deathCount;      // you died how many times?
         public long timeInGame;
         public long blocksPlaced;
         public long blocksBroken;
         public long mobsKilled;
        private volatile long lastEnteredWorld; //if in hardcore, then non-zero.   Used to accumulate total time.
 
         public void setIsLive() {
             if (!isLive) {
                 isLive = true;
                 // start accumulating game time again
                 playerEnteredHardCore();
             }
         }
 
         public void setIsDead() {
             playerLeftHardCore();
             isLive = false;
             deathCount++;
             noteActivity();
         }
 
         public void setWasOp(boolean wasOp) {
             this.wasOp = wasOp;
         }
 
         public void noteActivity() {
             lastActivity = System.currentTimeMillis();
         }
 
         public long getSecondsTillTimeout() {
             long secondsElapsed = (System.currentTimeMillis() - lastActivity) / 1000;
             if (isLive) {
                 long timeRequired = liveTimeout / (deathCount + 1);
                 long timeLeft = timeRequired - secondsElapsed;
                 return Math.max(0, timeLeft);
             } else {
                 long timeRequired = (long) (deathDuration * Math.pow(timeOutGrowth, deathCount - 1));
                 long timeLeft = timeRequired - secondsElapsed;
                 return Math.max(0, timeLeft);
             }
         }
 
         public long getSecondsInHardcore() {
             if (lastEnteredWorld == 0) {
                 return timeInGame / 1000;
             } else {
                 return (timeInGame + (System.currentTimeMillis() - lastEnteredWorld)) / 1000;
             }
         }
 
         public void playerEnteredHardCore() {
             if (isLive) {
                 // only accumulate time stats unless the player is alive
                 lastEnteredWorld = System.currentTimeMillis();
             } else {
                 lastEnteredWorld = 0;
             }
         }
 
         public void playerLeftHardCore() {
             if (isLive) {
                 // only accumulate time stats unless the player is alive
                 timeInGame = timeInGame + (System.currentTimeMillis() - lastEnteredWorld);
             } else {
                 lastEnteredWorld = 0;
             }
         }
 
         public void accrueBlockPlaced() {
             blocksPlaced++;
         }
 
         public void accrueBlockBroken() {
             blocksBroken++;
         }
 
         public void accrueMobKilled() {
             mobsKilled++;
         }
 
         public PlayerState(String name) {
             this.name = name;
             this.isLive = true;
             noteActivity();
         }
 
         public String toString() {
             StringBuilder builder = new StringBuilder();
             builder.append(name);
             builder.append("  ");
             builder.append((isLive) ? "live  " : "dead  ");
             builder.append("deathCount:");
             builder.append(deathCount);
             builder.append("  activity: ");
             builder.append(dateFormat.format(new Date(lastActivity)));
             builder.append("  timeInGame: ");
             builder.append(durationString(timeInGame / 1000));
             builder.append("  lastEnteredWorld: ");
             builder.append(dateFormat.format(new Date(lastEnteredWorld)));
             return builder.toString();
         }
 
         // --- serialize/deserialize support
         public static PlayerState deserialize(Map<String, Object> args) {
             return new PlayerState(args);
         }
 
         public static PlayerState valueOf(Map<String, Object> map) {
             return new PlayerState(map);
         }
 
         public PlayerState(Map<String, Object> map) {
             CowNetConfig.deserialize(this, map);
         }
 
         @Override
         public Map<String, Object> serialize() {
             return CowNetConfig.serialize(this);
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
 

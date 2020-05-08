 package us.fitzpatricksr.cownet;
 
 import com.onarandombox.MultiverseCore.MultiverseCore;
 import com.onarandombox.MultiverseCore.api.MVDestination;
 import com.onarandombox.MultiverseCore.api.MVWorldManager;
 import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
 import com.onarandombox.MultiverseCore.destination.DestinationFactory;
 import com.onarandombox.MultiverseCore.enums.TeleportResult;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.WorldType;
 import org.bukkit.command.Command;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import us.fitzpatricksr.cownet.utils.CowNetConfig;
 import us.fitzpatricksr.cownet.utils.CowNetThingy;
 import us.fitzpatricksr.cownet.utils.StringUtils;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class HardCoreCow extends CowNetThingy implements Listener {
     private HardCoreState config;
     private String worldName = "HardCoreCow";
     private int safeDistance = 5;
     private MultiverseCore mvPlugin;
 
     private boolean regenIsAlreadyScheduled = false;
 
     public HardCoreCow(JavaPlugin plugin, String permissionRoot, String trigger) {
         super(plugin, permissionRoot, trigger);
         if (isEnabled()) {
             reload();
             PluginManager pm = plugin.getServer().getPluginManager();
             pm.registerEvents(this, plugin);
         }
     }
 
     @Override
     protected void reload() {
         if (mvPlugin != null) mvPlugin.decrementPluginCount();
         try {
             worldName = getConfigString("worldname", worldName);
             safeDistance = getConfigInt("safedistance", safeDistance);
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
         } catch (IOException e) {
             e.printStackTrace();
             disable();
         } catch (InvalidConfigurationException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             disable();
         }
     }
 
     @Override
     protected String getHelpString(Player player) {
         return "usage: hardcore (go | info | revive <player> | regen)";
     }
 
     @Override
     protected boolean handleCommand(Player player, Command cmd, String[] args) {
         // subcommands
         //  regen
         //  revive <player>
         //  info
         //  go
         //  --- empty takes you to the hardcore world
         if (args.length == 0) {
             return goHardCore(player);
         } else if (args.length == 1) {
             if ("info".equalsIgnoreCase(args[0])) {
                 return goInfo(player);
             } else if ("regen".equalsIgnoreCase(args[0])) {
                 return goRegen(player);
             } else if ("go".equalsIgnoreCase((args[0]))) {
                 return goHardCore(player);
             }
         } else if (args.length == 2) {
             if ("revive".equalsIgnoreCase(args[0])) {
                 return goRevive(player, args[1]);
             }
         }
 
         return false;
     }
 
     private boolean goHardCore(Player player) {
         if (!hasPermissions(player, "go")) {
             player.sendMessage("Sorry, you don't have permission.");
             return true;
         }
         if (player.getWorld().getName().equalsIgnoreCase(worldName)) {
             //Player is on HARD CORE world aleady.
             //if they are close to spawn we will rescue them
             World world = player.getWorld();
             Location spawn = world.getSpawnLocation();
             Location loc = player.getLocation();
             double diff = spawn.distance(loc);
             if ((diff > safeDistance) && config.playerIsLive(player)) {
                 player.sendMessage("You need to make a HARD CORE effort to get closer to the spawn point.");
             } else {
                 for (World w : mvPlugin.getServer().getWorlds()) {
                     if (!w.getName().equalsIgnoreCase(worldName)) {
                         SafeTTeleporter teleporter = mvPlugin.getSafeTTeleporter();
                         teleporter.safelyTeleport(null, player, w.getSpawnLocation(), true);
                         break;
                     }
                 }
             }
         } else {
             // player is in some other world and wants to go HARD CORE
             MVWorldManager mgr = mvPlugin.getMVWorldManager();
             if (!mgr.isMVWorld(worldName) && !generateNewWorld()) {
                 return true;
             }
             if (config.playerIsDead(player.getName())) {
                 player.sendMessage("You're dead, so you will roam the world as a ghost.");
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
                     break;
                 case FAIL_OTHER:
                 default:
                     player.sendMessage("Something went wrong.  Something.  Stuff.");
                     break;
             }
         }
         return true;
     }
 
     private boolean goInfo(Player player) {
         if (!hasPermissions(player, "info")) {
             player.sendMessage("Sorry, you don't have permission.");
             return true;
         }
         if (config.playerIsDead(player.getName())) {
             player.sendMessage("You're dead.  Not very HARD CORE.");
         } else {
             player.sendMessage("You've been very HARD CORE up 'till now.");
         }
         player.sendMessage("  World: " + worldName);
         player.sendMessage("  Created: " + config.getCreationDate());
         player.sendMessage("  Dead players: " + StringUtils.flatten(config.getDeadPlayers()));
         player.sendMessage("  Live players: " + StringUtils.flatten(config.getLivePlayers()));
         return true;
     }
 
     private boolean goRegen(Player player) {
         if (!hasPermissions(player, "regen")) {
             player.sendMessage("Sorry, you're not HARD CORE enough.  Come back when you're more HARD CORE.");
             return true;
         }
         if (generateNewWorld()) {
             player.sendMessage(worldName + " has been regenerated HARDer and more CORE than ever.");
         } else {
             player.sendMessage(worldName + " is too HARD CORE to be regenerated.");
         }
         return true;
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
                 try {
                     config.resetWorldState();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
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
 
     private boolean goRevive(Player player, String arg) {
         if (!hasPermissions(player, "revive")) {
             player.sendMessage("Sorry, you're not HARD CORE enough.");
         } else if (!config.playerIsDead(arg)) {
             player.sendMessage(arg + " is still going at it HARD CORE and isn't dead.");
         } else if (config.markPlayerLive(arg)) {
             player.sendMessage("We'll give " + arg + " a second chance to be HARD CORE.");
         } else {
             player.sendMessage(arg + " just isn't HARD CORE enough to be given another chance right now.");
         }
         return true;
     }
 
 
     //
     // Persistent state methods (ex. live vs. dead)
     //
     private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
     /*
        Configuration looks like this:
        hardcorecow.creationDate: creationDate
        hardcorecow.liveplayers: player1,player2,player3
        hardcorecow.deadplayers: player1,player2,player3
     */
     private class HardCoreState extends CowNetConfig {
         private HashSet<String> livePlayers = new HashSet<String>();
         private HashSet<String> deadPlayers = new HashSet<String>();
         private String creationDate = "unknown";
 
         //TODO hey jf - it would be nice if live players expired after a while so they
         // don't keep the hardcore world around forever.
 
         public HardCoreState(JavaPlugin plugin, String name) {
             super(plugin, name);
         }
 
         public void loadConfig() throws IOException, InvalidConfigurationException {
             super.loadConfig();
             creationDate = getString("creationdate", creationDate);
 
             String liveString = getString("liveplayers", "");
             livePlayers.clear();
             livePlayers.addAll(Arrays.asList(StringUtils.unflatten(liveString)));
 
             String deadString = getString("deadplayers", "");
             deadPlayers.clear();
             deadPlayers.addAll(Arrays.asList(StringUtils.unflatten(deadString)));
 
             debug("Restored:");
         }
 
         public void saveConfig() throws IOException {
             set("creationdate", creationDate);
             set("liveplayers", StringUtils.flatten(livePlayers));
             set("deadplayers", StringUtils.flatten(deadPlayers));
             super.saveConfig();
             debug("Saving...");
         }
 
         public boolean playerIsDead(Player player) {
             return playerIsDead(player.getName());
         }
 
         public boolean playerIsDead(String player) {
             player = player.toLowerCase();
             return deadPlayers.contains(player);
         }
 
         public boolean playerIsLive(Player player) {
             return playerIsLive(player.getName());
         }
 
         public boolean playerIsLive(String player) {
             player = player.toLowerCase();
             return livePlayers.contains(player);
         }
 
         public Set<String> getLivePlayers() {
             return livePlayers;
         }
 
         public Set<String> getDeadPlayers() {
             return deadPlayers;
         }
 
         public boolean markPlayerDead(Player p) {
             return markPlayerDead(p.getName());
         }
 
         public boolean markPlayerDead(String p) {
             p = p.toLowerCase();
             if (deadPlayers.contains(p)) return true;
             deadPlayers.add(p);
             livePlayers.remove(p);
             try {
                 saveConfig();
                 return true;
             } catch (IOException e) {
                 e.printStackTrace();
                 deadPlayers.remove(p);
                 livePlayers.add(p);
                 return false;
             }
         }
 
         public boolean markPlayerLive(Player p) {
             return markPlayerLive(p.getName());
         }
 
         private boolean markPlayerLive(String p) {
             p = p.toLowerCase();
             if (livePlayers.contains(p)) return true;
             deadPlayers.remove(p);
             livePlayers.add(p);
             try {
                 saveConfig();
                 return true;
             } catch (IOException e) {
                 e.printStackTrace();
                 deadPlayers.add(p);
                 livePlayers.remove(p);
                 return false;
             }
         }
 
         public String getCreationDate() {
             return creationDate;
         }
 
         public void resetWorldState() throws IOException {
             livePlayers.clear();
             deadPlayers.clear();
             creationDate = dateFormat.format(new Date());
             //worldName = worldName;
             saveConfig();
         }
 
         private void debug(String msg) {
             logInfo(msg);
             logInfo("  World: " + worldName);
             logInfo("  Created: " + creationDate);
             logInfo("  Dead players: " + StringUtils.flatten(deadPlayers));
             logInfo("  Live players: " + StringUtils.flatten(livePlayers));
         }
     }
 
 
     // --- Stop Ghosts from doing things
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.isCancelled()) return;
         if (!event.getPlayer().getWorld().getName().equalsIgnoreCase(worldName)) return;
         if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR) {
             return;
         }
         if (config.playerIsDead(event.getPlayer())) {
             logInfo("Ghost event");
             event.setCancelled(true);
         } else {
             config.markPlayerLive(event.getPlayer());
         }
     }
 
     @EventHandler
     public void onBlockPlace(BlockPlaceEvent event) {
         if (event.isCancelled()) return;
         if (!event.getPlayer().getWorld().getName().equalsIgnoreCase(worldName)) return;
         if (config.playerIsDead(event.getPlayer())) {
             logInfo("Ghost event");
             event.setCancelled(true);
         } else {
             config.markPlayerLive(event.getPlayer());
         }
     }
 
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
         if (event.isCancelled()) return;
         if (!event.getPlayer().getWorld().getName().equalsIgnoreCase(worldName)) return;
         if (config.playerIsDead(event.getPlayer())) {
             logInfo("Ghost event");
             event.setCancelled(true);
         } else {
             config.markPlayerLive(event.getPlayer());
         }
     }
 
     @EventHandler
     public void onPlayerDeath(EntityDeathEvent event) {
         if (event.getEntity() instanceof Player) {
             Player player = (Player) event.getEntity();
             if (!player.getWorld().getName().equalsIgnoreCase(worldName)) return;
             config.markPlayerDead(player);
         }
     }
 
     @EventHandler
     public void onPlayerRespawn(PlayerRespawnEvent event) {
         if (regenIsAlreadyScheduled) return;
         if (config.getDeadPlayers().size() > 0 && config.getLivePlayers().size() == 0) {
             logInfo("Everyone's dead.  This world was too HARD CORE.  Generating a new world in 2 seconds.  Please wait...");
             getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
                 public void run() {
                     generateNewWorld();
                     getPlugin().getServer().broadcastMessage("Seems like " + worldName + " was too HARD CORE.  " +
                             "It's been regenerated to be a bit more fluffy for you softies.");
                 }// end of run
             }, 40);
         }
     }
 
 }
 

 package us.fitzpatricksr.cownet;
 
 import com.onarandombox.MultiverseCore.MultiverseCore;
 import com.onarandombox.MultiverseCore.api.MVWorldManager;
 import org.bukkit.*;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.*;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import us.fitzpatricksr.cownet.hungergames.GameInstance;
 import us.fitzpatricksr.cownet.hungergames.PlayerInfo;
 import us.fitzpatricksr.cownet.utils.BlockUtils;
 import us.fitzpatricksr.cownet.utils.CowNetThingy;
 
 import java.util.Random;
 
 /*
     the game world is off limits until a game starts
     the first person to enter starts the gathering
     gathering continues for a specified period of time
     all players who entered after that time are teleported in and their inventory cleared.
     after the last player dies the world is regenerated.
 
     games have 3 states
        ended - nothing in progress
        gathering - someone is waiting for the games to start
        inprogress - the games are underway
 
     for each person
        enum { player, deadPlayer, sponsor } gameState
        boolean lastTribute
  */
 public class HungerGames extends CowNetThingy implements Listener {
     private static final int GAME_WATCHER_FREQUENCY = 20 * 1; // 1 second
     private static final Material[] gifts = {
             Material.TNT,
             Material.TORCH,
             Material.IRON_SPADE,
             Material.IRON_PICKAXE,
             Material.IRON_AXE,
             Material.FLINT_AND_STEEL,
             Material.APPLE,
             Material.BOW,
             Material.ARROW,
             Material.COAL,
             Material.DIAMOND,
             Material.IRON_SWORD,
             Material.WOOD_SWORD,
             Material.WOOD_SPADE,
             Material.WOOD_PICKAXE,
             Material.WOOD_AXE,
             Material.STONE_SWORD,
             Material.STONE_SPADE,
             Material.STONE_PICKAXE,
             Material.STONE_AXE,
 //            Material.DIAMOND_SWORD,
 //            Material.DIAMOND_SPADE,
 //            Material.DIAMOND_PICKAXE,
 //            Material.DIAMOND_AXE,
             Material.STICK,
             Material.BOWL,
             Material.MUSHROOM_SOUP,
             Material.GOLD_SWORD,
             Material.GOLD_SPADE,
             Material.GOLD_PICKAXE,
             Material.GOLD_AXE,
             Material.STRING,
             Material.FEATHER,
             Material.WOOD_HOE,
             Material.STONE_HOE,
             Material.IRON_HOE,
             Material.DIAMOND_HOE,
             Material.GOLD_HOE,
             Material.SEEDS,
             Material.WHEAT,
             Material.BREAD,
             Material.LEATHER_HELMET,
             Material.LEATHER_CHESTPLATE,
             Material.LEATHER_LEGGINGS,
             Material.LEATHER_BOOTS,
             Material.CHAINMAIL_HELMET,
             Material.CHAINMAIL_CHESTPLATE,
             Material.CHAINMAIL_LEGGINGS,
             Material.CHAINMAIL_BOOTS,
             Material.IRON_HELMET,
             Material.IRON_CHESTPLATE,
             Material.IRON_LEGGINGS,
             Material.IRON_BOOTS,
 //            Material.DIAMOND_HELMET,
 //            Material.DIAMOND_CHESTPLATE,
 //            Material.DIAMOND_LEGGINGS,
 //            Material.DIAMOND_BOOTS,
             Material.GOLD_HELMET,
             Material.GOLD_CHESTPLATE,
             Material.GOLD_LEGGINGS,
             Material.GOLD_BOOTS,
             Material.FLINT,
             Material.PORK,
             Material.GRILLED_PORK,
             Material.GOLDEN_APPLE,
             Material.BUCKET,
             Material.FISHING_ROD,
             Material.CAKE,
             Material.MAP,
             Material.SHEARS,
             Material.COOKED_BEEF,
             Material.COOKED_CHICKEN
     };
 
     private final Random rand = new Random();
 
     private String gameWorldName = "HungerGames";
     private int arenaSize = 500;
     private boolean allowFly = false;
     private boolean allowXRay = false;
     private double monsterBoost = 1.0d;
     private int teleportJiggle = 5;
 
     private MultiverseCore mvPlugin;
     private GameInstance gameInstance = new GameInstance();
     private int arenaSizeThisGame = 0;
     private boolean regenIsAlreadyScheduled = false;
 
 
     private HungerGames() {
         // for testing only
     }
 
     public HungerGames(JavaPlugin plugin, String permissionRoot, String trigger) {
         super(plugin, permissionRoot, trigger);
         if (isEnabled()) {
             reload();
             PluginManager pm = plugin.getServer().getPluginManager();
             pm.registerEvents(this, plugin);
             getPlugin().getServer().getScheduler().scheduleAsyncRepeatingTask(
                     getPlugin(),
                     new Runnable() {
                         public void run() {
                             goGameWatcher();
                         }
                     },
                     GAME_WATCHER_FREQUENCY,
                     GAME_WATCHER_FREQUENCY);
         }
     }
 
     @Override
     protected void reload() {
         if (mvPlugin != null) mvPlugin.decrementPluginCount();
         gameWorldName = getConfigString("worldName", gameWorldName);
         arenaSize = getConfigInt("arenaSize", arenaSize);
         allowFly = getConfigBoolean("allowFly", allowFly);
         allowXRay = getConfigBoolean("allowXRay", allowXRay);
         GameInstance.timeToGather = getConfigLong("timeToGather", GameInstance.timeToGather);
        GameInstance.timeToAcclimate = getConfigLong("timeToGather", GameInstance.timeToAcclimate);
         PlayerInfo.timeBetweenGifts = getConfigLong("timeBetweenGifts", PlayerInfo.timeBetweenGifts);
         GameInstance.minTributes = getConfigInt("minTributes", GameInstance.minTributes);
         teleportJiggle = getConfigInt("teleportJiggle", teleportJiggle);
         monsterBoost = getConfigDouble("monsterBoost", monsterBoost);
         mvPlugin = (MultiverseCore) getPlugin().getServer().getPluginManager().getPlugin("Multiverse-Core");
         if (mvPlugin == null) {
             logInfo("Could not find Multiverse-Core plugin.  Disabling self");
             disable();
         } else {
             mvPlugin.incrementPluginCount();
         }
         logInfo("worldName:" + gameWorldName);
         logInfo("arenaSize:" + arenaSize);
         logInfo("allowFly:" + allowFly);
         logInfo("timeToGather:" + GameInstance.timeToGather);
         logInfo("timeToAcclimate:" + GameInstance.timeToAcclimate);
         logInfo("timeBetweenGifts:" + PlayerInfo.timeBetweenGifts);
         logInfo("minTributes:" + GameInstance.minTributes);
         logInfo("teleportJiggle:" + teleportJiggle);
         logInfo("monsterBoost:" + monsterBoost);
     }
 
     @EventHandler
     protected void handlePluginDisabled(PluginDisableEvent event) {
         if (event.getPlugin() == getPlugin()) {
             mvPlugin.decrementPluginCount();
         }
     }
 
     @Override
     protected String getHelpString(CommandSender player) {
         return "usage: /hungergames or /hg   join | info | quit";
     }
 
     @Override
     protected boolean handleCommand(CommandSender sender, Command cmd, String[] args) {
         if (args.length == 1 && (
                 "info".equalsIgnoreCase(args[0])
                         || "list".equalsIgnoreCase(args[0])
                         || "stats".equalsIgnoreCase(args[0]))) {
             return goInfo(sender);
         }
         return false;
     }
 
     protected boolean handleCommand(Player sender, Command cmd, String[] args) {
         if (args.length == 0) {
             return doJoin(sender);
         } else if (args.length == 1) {
             if ("quit".equalsIgnoreCase(args[0])) {
                 return goQuit(sender);
             } else if ("join".equalsIgnoreCase(args[0])) {
                 return doJoin(sender);
             }
         }
         return false;
     }
 
     private boolean doJoin(Player player) {
         if (!hasPermissions(player, "join")) {
             player.sendMessage("You don't have permission.");
         } else {
             if (!gameInstance.isGameOn()) {
                 gameInstance.addPlayerToGame(player);
                 player.sendMessage("You've joined the game as a tribute.");
                 goInfo(player);
             } else {
                 player.sendMessage("You can't join a game in progress.  You can only sponsor tributes.");
             }
         }
         return true;
     }
 
     private boolean goInfo(CommandSender sender) {
         if (!hasPermissions(sender, "info")) {
             sender.sendMessage("You don't have permission.");
         } else {
             sender.sendMessage(gameInstance.getGameStatusMessage());
             for (Player player : getPlugin().getServer().getOnlinePlayers()) {
                 PlayerInfo info = gameInstance.getPlayerInfo(player);
                 sender.sendMessage("  " + info);
             }
         }
         return true;
     }
 
     private boolean goQuit(Player player) {
         if (!hasPermissions(player, "quit")) {
             player.sendMessage("You don't have permission.");
         } else {
             if (playerIsInGame(player)) {
                 gameInstance.removePlayerFromGame(player);
                 playPlayerDeathSound(player);
                 player.sendMessage("You've left the game.");
             }
         }
         return true;
     }
 
     // --------------------------------------------------------------
     // ---- Game watcher moves the game forward through different stages
 
     private void goGameWatcher() {
         debugInfo(gameInstance.getGameStatusMessage());
         if (gameInstance.isUnstarted()) {
             //don't do anything until the games start
         } else if (gameInstance.isEnded()) {
             for (PlayerInfo info : gameInstance.getPlayersInGame()) {
                 broadcast("The winner of the games is: " + info.getPlayer().getDisplayName());
             }
             removeAllPlayersFromArena(gameWorldName);
             gameInstance = new GameInstance();
             //TODO hey jf - you should generateNewWorld() here
         } else if (gameInstance.isFailed()) {
             broadcast("The games have been canceled due to lack of tributes.");
             removeAllPlayersFromArena(gameWorldName);
             gameInstance = new GameInstance();
             //TODO hey jf - you should generateNewWorld() here
         } else if (gameInstance.isGathering()) {
             long timeToWait = gameInstance.getTimeToGather() / 1000;
             if (timeToWait % 10 == 0 || timeToWait < 10) {
                 broadcast("Gathering for the games ends in " + timeToWait + " seconds");
             }
         } else {
             if (arenaSizeThisGame == 0) {
                 // the basic 1 on 1 arena is arenaSize.  3 people is 3/2 * arenaSize.  4 is 4/2 arenasize
                 arenaSizeThisGame = gameInstance.getPlayersInGame().size() / 2 * arenaSize;
             }
             if (gameInstance.isAcclimating()) {
                 long timeToWait = gameInstance.getTimeToAcclimate() / 1000;
                 broadcast("The games start in " + timeToWait + " seconds");
             }
             // if acclimating or in progress...
             for (PlayerInfo playerInfo : gameInstance.getPlayersInGame()) {
 //                if (!isInArena(playerInfo.getPlayer())) {
                 if (!isGameWorld(playerInfo.getPlayer().getLocation().getWorld())) {
                     teleportPlayerToArena(playerInfo.getPlayer(), gameWorldName);
                 }
             }
         }
     }
 
     // --------------------------------------------------------------
     // ---- Event handlers
 
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onPlayerTeleport(PlayerTeleportEvent event) {
         // nobody can go to the game world unless a game is underway
         if (isGameWorld(event.getTo().getWorld()) && !gameInstance.isGameOn()) {
             event.setCancelled(true);
             event.getPlayer().sendMessage("You can't teleport to the arena until the game starts.");
             removeAllPlayersFromArena(gameWorldName);
             debugInfo("canceled PlayerTeleportEvent");
         } else if (gameInstance.isGameOn()) {
             Player player = event.getPlayer();
             if (playerIsInGame(player)) {
                 // tributes can only be teleported from outside the arena to inside the arena.
                 Location to = event.getTo();
                 Location from = event.getFrom();
                 if (isInArena(from) || !isInArena(to)) {
                     event.setCancelled(true);
                 }
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onPlayerMoved(PlayerMoveEvent event) {
         if (!gameInstance.isGameOn()) return;
         if (!playerIsInGame(event.getPlayer())) return;
         // OK, we have a player
         if (gameInstance.isAcclimating()) {
             // don't let them move while acclimating
             Location to = event.getTo().getBlock().getLocation();
             Location from = event.getFrom().getBlock().getLocation();
             if (to.getX() != from.getX() ||
                     to.getY() != from.getY() ||
                     to.getZ() != from.getZ()) {
                 // if they do anything but spin or move their head, cancel.
                 event.setCancelled(true);
                 event.getPlayer().teleport(from);
             }
         } else if (!isInArena(event.getTo())) {
             // don't let them leave the arena ever.
             event.setCancelled(true);
             debugInfo("canceled PlayerMovedEvent");
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (!gameInstance.isGameOn()) return;
         // if sponsor, you can't do anything
         Player player = event.getPlayer();
         if (!playerIsInGame(player) && isInArena(player.getLocation())) {
             event.setCancelled(true);
             player.sendMessage("You can't interfere with the game directly.");
             debugInfo("canceled PlayerInteractEvent");
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onBlockPlace(BlockPlaceEvent event) {
         if (!gameInstance.isGameOn()) return;
         // if sponsor, you can't do anything
         Player player = event.getPlayer();
         if (!playerIsInGame(player) && isInArena(player.getLocation())) {
             event.setCancelled(true);
             player.sendMessage("You can't interfere with the game directly.");
             debugInfo("canceled BlockPlaceEvent");
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onBlockBreak(BlockBreakEvent event) {
         if (!gameInstance.isGameOn()) return;
         // if sponsor, you can't do anything
         Player player = event.getPlayer();
         if (!playerIsInGame(player) && isInArena(player.getLocation())) {
             event.setCancelled(true);
             player.sendMessage("You can't interfere with the game directly.");
             debugInfo("canceled BlockBreakEvent");
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPickupItem(PlayerPickupItemEvent event) {
         if (!gameInstance.isGameOn()) return;
         Player player = event.getPlayer();
         if (!playerIsInGame(player) && isInArena(player.getLocation())) {
             event.setCancelled(true);
             player.sendMessage("You can't interfere with the game directly.");
             debugInfo("canceled PlayerPickupItemEvent");
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerDropItem(PlayerDropItemEvent event) {
         if (!gameInstance.isGameOn()) return;
         Player player = event.getPlayer();
         if (playerIsOutOfGame(player) && isInArena(player)) {
             event.setCancelled(true);
             player.sendMessage("You are out of the game and can't sponsor other players.");
         } else if (playerIsSponsor(player) && isInArena(player)) {
             if (!dropGiftToTribute(player)) {
                 event.setCancelled(true);
                 player.sendMessage("You can only give gifts once every " + PlayerInfo.timeBetweenGifts / 1000 / 60 + " minutes.");
                 debugInfo("canceled PlayerDropItemEvent");
             }
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onGameModeChange(PlayerGameModeChangeEvent event) {
         if (!gameInstance.isGameOn()) return;
         Player player = event.getPlayer();
         if (playerIsInGame(player)) {
             event.setCancelled(true);
             player.sendMessage("You can't change the game mode of players in the game.");
             debugInfo("canceled PlayerGameModeChangeEvent");
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onEntityDamage(EntityDamageEvent event) {
         if (!gameInstance.isGameOn()) return;
         // if sponsor, you can't do anything
         Entity entity = event.getEntity();
         if (entity instanceof Player) {
             // make sure sponsors can't be damaged
             Player victim = (Player) entity;
             if (!playerIsInGame(victim) && isInArena(victim)) {
                 event.setCancelled(true);
                 debugInfo("canceled EntityDamageEvent");
             }
         }
         if (event instanceof EntityDamageByEntityEvent) {
             // make user non-players can't damage things in the arena
             final EntityDamageByEntityEvent target = (EntityDamageByEntityEvent) event;
             final Entity damager = target.getDamager();
             if (damager instanceof Player) {
                 Player player = ((Player) damager);
                 if (!playerIsInGame(player) && isInArena(player)) {
                     event.setCancelled(true);
                     debugInfo("canceled EntityDamageEvent");
                 }
             }
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerDeath(PlayerDeathEvent event) {
         if (!gameInstance.isGameOn()) return;
         if (playerIsInGame(event.getEntity())) {
             gameInstance.removePlayerFromGame(event.getEntity());
             playPlayerDeathSound(event.getEntity());
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onEntityTarget(EntityTargetEvent event) {
         if (!gameInstance.isGameOn()) return;
         // if sponsor, they can't be targeted
         if (event.getTarget() instanceof Player) {
             Player player = (Player) event.getTarget();
             if (!playerIsInGame(player) && isInArena(player)) {
                 event.setCancelled(true);
                 debugInfo("canceled EntityTargetEvent");
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onPlayerJoin(PlayerJoinEvent event) {
         if (!gameInstance.isGameOn()) return;
         goInfo(event.getPlayer());
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerQuit(PlayerQuitEvent event) {
         if (!gameInstance.isGameOn()) return;
         if (playerIsInGame(event.getPlayer())) {
             gameInstance.removePlayerFromGame(event.getPlayer());
             playPlayerDeathSound(event.getPlayer());
         }
     }
 
     private void playPlayerDeathSound(Player player) {
         player.getWorld().strikeLightningEffect(player.getLocation());
         player.getWorld().strikeLightningEffect(player.getLocation());
         broadcast(player.getDisplayName() + " has left the games.");
     }
 
     // ------------------------------------------------
     // ----- Arena world utilities
 
     private boolean isGameWorld(World w) {
         return isGameWorld(w.getName());
     }
 
     private boolean isGameWorld(String worldName) {
         return gameWorldName.equalsIgnoreCase(worldName);
     }
 
     private boolean isInArena(Player player) {
         return isInArena(player.getLocation());
     }
 
     private boolean isInArena(Location loc) {
         World w = loc.getWorld();
         if (isGameWorld(w)) {
             Location spawnLoc = w.getSpawnLocation();
             double distance = spawnLoc.distance(loc);
             debugInfo("Distance: " + distance);
             return distance < arenaSizeThisGame;
         } else {
             return false;
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
                 World w = mgr.getMVWorld(worldName).getCBWorld();
                 w.setDifficulty(Difficulty.HARD);
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
 
     private void teleportPlayerToArena(Player player, String worldName) {
         if (regenIsAlreadyScheduled) return;
         World gameWorld = getPlugin().getServer().getWorld(worldName);
         Location spawn = gameWorld.getSpawnLocation().clone();
         Location location = spawn.clone();
         location = jiggleLocation(location);
         location = BlockUtils.getHighestLandLocation(location);
         location.add(0, 1, 0);
         player.setGameMode(GameMode.SURVIVAL);
         player.teleport(location);
         player.sendMessage("Good luck.");
         //place 3 random gifts per player
         for (int i = 0; i < 3; i++) {
             Location giftLoc = spawn.clone();
             giftLoc = jiggleLocation(giftLoc);
             giftLoc = BlockUtils.getHighestLandLocation(giftLoc);
             giftLoc.add(0, 1, 0);
             Material gift = gifts[rand.nextInt(gifts.length)];
             gameWorld.dropItemNaturally(giftLoc, new ItemStack(gift, 1));
         }
         debugInfo("Teleported player " + player.getDisplayName() + " to " + worldName);
     }
 
     private void removeAllPlayersFromArena(String worldName) {
         MVWorldManager mgr = mvPlugin.getMVWorldManager();
         if (mgr.isMVWorld(worldName)) {
             mgr.removePlayersFromWorld(worldName);
             debugInfo("Removed all players from arena");
         }
     }
 
     private Location jiggleLocation(Location loc) {
         //the more players, the bigger the jiggle
         int jiggle = teleportJiggle * (gameInstance.getPlayersInGame().size() - 1);
         Location result = loc.clone();
         result.add(rand.nextInt(jiggle * 2) - jiggle, 0, rand.nextInt(jiggle * 2) - jiggle);
         return result;
     }
 
     private void broadcast(String msg) {
         for (Player player : getPlugin().getServer().getOnlinePlayers()) {
             player.sendMessage(msg);
         }
     }
 
     // --------------------------------------------------------------
     // --- per player state while the games are in progress.
 
     private boolean dropGiftToTribute(Player player) {
         return gameInstance.getPlayerInfo(player).dropGiftToTribute();
     }
 
     private boolean playerIsInGame(Player p) {
         return gameInstance.getPlayerInfo(p).isInGame();
     }
 
     private boolean playerIsSponsor(Player p) {
         return gameInstance.getPlayerInfo(p).isSponsor();
     }
 
     private boolean playerIsOutOfGame(Player p) {
         return gameInstance.getPlayerInfo(p).isOutOfGame();
     }
 }
 
 

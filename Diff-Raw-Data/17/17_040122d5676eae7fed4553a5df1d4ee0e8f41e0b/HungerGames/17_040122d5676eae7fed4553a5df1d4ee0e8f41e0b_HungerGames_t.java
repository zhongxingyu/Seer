 package us.fitzpatricksr.cownet;
 
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
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
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import us.fitzpatricksr.cownet.hungergames.GameHistory;
 import us.fitzpatricksr.cownet.hungergames.GameInstance;
 import us.fitzpatricksr.cownet.hungergames.PlayerInfo;
 import us.fitzpatricksr.cownet.utils.BlockUtils;
 import us.fitzpatricksr.cownet.utils.CowNetThingy;
 import us.fitzpatricksr.cownet.utils.StringUtils;
 
 import java.io.IOException;
 import java.util.List;
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
     private boolean allowFly = false;
     private boolean allowXRay = false;
     private double monsterBoost = 1.0d;
     private int arenaSize = 500;
     private int landingPadSize = 5;
     private int giftsPerPlayer = 3;
 
     private GameInstance gameInstance = new GameInstance();
     private GameHistory gameHistory;
     private int arenaSizeThisGame = 0;
 
     private HungerGames() {
         // for testing only
     }
 
     public HungerGames(JavaPlugin plugin, String permissionRoot, String trigger) {
         super(plugin, permissionRoot, trigger);
         if (isEnabled()) {
             reload();
             PluginManager pm = plugin.getServer().getPluginManager();
             pm.registerEvents(this, plugin);
             getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(
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
         gameWorldName = getConfigString("worldName", gameWorldName);
         arenaSize = getConfigInt("arenaSize", arenaSize);
         allowFly = getConfigBoolean("allowFly", allowFly);
         allowXRay = getConfigBoolean("allowXRay", allowXRay);
         GameInstance.timeToGather = getConfigLong("timeToGather", GameInstance.timeToGather);
         GameInstance.timeToAcclimate = getConfigLong("timeToAcclimate", GameInstance.timeToAcclimate);
         PlayerInfo.timeBetweenGifts = getConfigLong("timeBetweenGifts", PlayerInfo.timeBetweenGifts);
         GameInstance.minTributes = getConfigInt("minTributes", GameInstance.minTributes);
         landingPadSize = getConfigInt("landingPadSize", landingPadSize);
         monsterBoost = getConfigDouble("monsterBoost", monsterBoost);
         giftsPerPlayer = getConfigInt("giftsPerPlayer", giftsPerPlayer);
         gameHistory = new GameHistory(getPlugin(), getTrigger() + ".yml");
         try {
             gameHistory.loadConfig();
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (InvalidConfigurationException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         logInfo("worldName:" + gameWorldName);
         logInfo("arenaSize:" + arenaSize);
         logInfo("allowFly:" + allowFly);
         logInfo("timeToGather:" + GameInstance.timeToGather);
         logInfo("timeToAcclimate:" + GameInstance.timeToAcclimate);
         logInfo("timeBetweenGifts:" + PlayerInfo.timeBetweenGifts);
         logInfo("giftsPerPlayer:" + giftsPerPlayer);
         logInfo("minTributes:" + GameInstance.minTributes);
         logInfo("landingPadSize:" + landingPadSize);
         logInfo("monsterBoost:" + monsterBoost);
     }
 
     @Override
     protected String[] getHelpText(CommandSender player) {
         return new String[]{
                 "usage: /hungergames or /hg   join | info | quit | tp <player> | start",
                 "   join - join the games as a tribute",
                 "   info - what's the state of the current game?",
                 "   quit - chicken out and just watch",
                 "   tp <player> - transport to a player, if you're a sponsor",
                 "   start - just get things started already!",
                 "   stats - see how you stack up against others.",
                 "Note: sponsors can drop items once a minute"
         };
     }
 
     @Override
     protected boolean handleCommand(CommandSender sender, Command cmd, String[] args) {
         if (args.length == 1) {
             if ("info".equalsIgnoreCase(args[0]) || "list".equalsIgnoreCase(args[0])) {
                 return goInfo(sender);
             } else if ("stats".equalsIgnoreCase(args[0])) {
                 return goStats(sender);
             }
         }
         return false;
     }
 
     protected boolean handleCommand(Player sender, Command cmd, String[] args) {
         if (args.length == 0) {
             return doJoin(sender);
         } else if (args.length == 1) {
             if ("quit".equalsIgnoreCase(args[0])) {
                 return goQuit(sender);
             } else if ("start".equalsIgnoreCase(args[0])) {
                 return doStart(sender);
             } else if ("join".equalsIgnoreCase(args[0])) {
                 return doJoin(sender);
             }
         } else if (args.length == 2) {
             if ("tp".equalsIgnoreCase(args[0])) {
                 return doTeleport(sender, args[1]);
             }
         }
         return false;
     }
 
     private boolean goStats(CommandSender sender) {
         if (sender instanceof Player) {
             Player player = (Player) sender;
             player.sendMessage("Your wins: " + gameHistory.getPlayerWins(player) + "   "
                     + StringUtils.fitToColumnSize(Double.toString(gameHistory.getPlayerAverage(player) * 100), 5) + "%");
         }
         gameHistory.dumpRecentHistory(sender);
         gameHistory.dumpLeaderBoard(sender);
         return true;
     }
 
     private boolean doJoin(Player player) {
         if (!gameInstance.isGameOn()) {
             gameInstance.addPlayerToGame(player);
             player.sendMessage("You've joined the game as a tribute.");
             broadcast("" + gameInstance.getPlayerInfo(player));
             goInfo(player);
         } else {
             player.sendMessage("You can't join a game in progress.  You can only sponsor tributes.");
             goInfo(player);
         }
         return true;
     }
 
     private boolean doStart(Player player) {
         if (!gameInstance.isGameOn()) {
             gameInstance.startNow();
         }
         return true;
     }
 
     private boolean goInfo(CommandSender sender) {
         sender.sendMessage(gameInstance.getGameStatusMessage());
         for (Player player : getPlugin().getServer().getOnlinePlayers()) {
             PlayerInfo info = gameInstance.getPlayerInfo(player);
             sender.sendMessage("  " + info);
         }
         return true;
     }
 
     private boolean goQuit(Player player) {
         if (playerIsInGame(player)) {
             gameInstance.removePlayerFromGame(player);
             gameHistory.registerLossFor(player);
             playPlayerDeathSound(player);
             player.sendMessage("You've left the game.");
         }
         return true;
     }
 
     private boolean doTeleport(Player sender, String destName) {
         PlayerInfo source = gameInstance.getPlayerInfo(sender);
         if (source.isInGame()) {
             sender.sendMessage("Tributes are not allowed to teleport.");
         } else if (source.isSponsor()) {
             Player dest = getPlugin().getServer().getPlayerExact(destName);
             PlayerInfo destInfo = gameInstance.getPlayerInfo(dest);
             if (destInfo.isInGame()) {
                 dest.teleport(dest.getLocation());
                 return true;
             } else {
                 sender.sendMessage("Sponsors can only teleport to tributes");
             }
         } else {
             sender.sendMessage("Only Sponsors can teleport and only to active tributes.");
         }
         return false;
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
                 gameHistory.registerWinFor(info.getPlayer());
             }
             for (Player player : getPlugin().getServer().getOnlinePlayers()) {
                 goStats(player);
             }
             removeAllPlayersFromArena(gameWorldName);
             setNewSpawnLocation();
             gameInstance = new GameInstance();
         } else if (gameInstance.isFailed()) {
             broadcast("The games have been canceled due to lack of tributes.");
             removeAllPlayersFromArena(gameWorldName);
             gameInstance = new GameInstance();
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
                 for (PlayerInfo playerInfo : gameInstance.getPlayersInGame()) {
                     Player player = playerInfo.getPlayer();
                     clearPlayerInventory(player);
                     player.setGameMode(GameMode.SURVIVAL);
                 }
             }
             // if acclimating or in progress...
             int ndx = 0;
             List<PlayerInfo> playersInGame = gameInstance.getPlayersInGame();
             for (PlayerInfo playerInfo : playersInGame) {
 //                if (!isInArena(playerInfo.getPlayer())) {
                 if (!isGameWorld(playerInfo.getPlayer().getLocation().getWorld())) {
                     teleportPlayerToArena(playerInfo.getPlayer(), gameWorldName, ndx++, playersInGame.size());
                 }
             }
         }
     }
 
     private void clearPlayerInventory(Player player) {
         debugInfo("Clearing inventory for " + player.getName());
         player.setItemInHand(null);
         Inventory inventory = player.getInventory();
         inventory.setContents(new ItemStack[inventory.getSize()]);
     }
 
     // --------------------------------------------------------------
     // ---- Event handlers
 
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onPlayerTeleport(PlayerTeleportEvent event) {
         debugInfo("PlayerTeleportEvent");
         // nobody can go to the game world unless a game is underway
         if (isGameWorld(event.getTo().getWorld()) && !gameInstance.isGameOn()) {
             if (!hasPermissions(event.getPlayer(), "gamemaker")) {
                 event.setCancelled(true);
                 event.getPlayer().sendMessage("You can't teleport to the arena until the game starts.");
                 removeAllPlayersFromArena(gameWorldName);
                 debugInfo("canceled PlayerTeleportEvent1");
             }
         } else if (gameInstance.isGameOn()) {
             Player player = event.getPlayer();
             if (playerIsInGame(player)) {
                 // tributes can only be teleported from outside the arena to inside the arena.
                 Location to = event.getTo();
                 Location from = event.getFrom();
                 if (!isInArena(from) && isInArena(to)) {
                     // from somewhere to the arena.  clear player inventory
                 } else {
                     // teleport to/from anywhere else is not allowed for players.
                     event.setCancelled(true);
                     debugInfo("canceled PlayerTeleportEvent2");
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
             Player player = event.getPlayer();
             if (to.getX() != from.getX() ||
                     to.getY() != from.getY() ||
                     to.getZ() != from.getZ()) {
                 // if they do anything but spin or move their head, strike with lightning.
 //                player.getWorld().strikeLightning(to);
 //                player.damage(2);
                 event.setCancelled(true);
                 debugInfo("Player " + event.getPlayer().getName() + " is moving during acclimation");
             }
         } else if (!isInArena(event.getTo())) {
             // don't let them leave the arena ever.
             event.setCancelled(true);
             debugInfo("canceled PlayerMovedEvent");
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerInteract(PlayerInteractEvent event) {
         debugInfo("PlayerInteractEvent");
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
         debugInfo("BlockPlaceEvent");
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
         debugInfo("BlockBreakEvent");
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
         debugInfo("PlayerPickupItemEvent");
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
         debugInfo("PlayerDropItemEvent");
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
         debugInfo("PlayerGameModeChangeEvent");
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
         debugInfo("PlayerDeathEvent");
         if (!gameInstance.isGameOn()) return;
         if (playerIsInGame(event.getEntity())) {
             gameInstance.removePlayerFromGame(event.getEntity());
             gameHistory.registerLossFor(event.getEntity());
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
         debugInfo("PlayerJoinEvent");
         if (!gameInstance.isGameOn()) return;
         goInfo(event.getPlayer());
         goStats(event.getPlayer());
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerQuit(PlayerQuitEvent event) {
         debugInfo("PlayerQuitEvent");
         if (!gameInstance.isGameOn()) return;
         if (playerIsInGame(event.getPlayer())) {
             gameInstance.removePlayerFromGame(event.getPlayer());
             gameHistory.registerLossFor(event.getPlayer());
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
             return distance < arenaSizeThisGame;
         } else {
             return false;
         }
     }
 
     private void setNewSpawnLocation() {
         logInfo("Setting new spawn in " + gameWorldName);
 
         // regenerating the world is VERY slow, so let's just move spawn around.
         World w = getPlugin().getServer().getWorld(gameWorldName);
         Location spawn = w.getSpawnLocation().getBlock().getLocation();
 
         if (spawn.getBlock().getType().equals(Material.GOLD_BLOCK)) {
             logInfo("Not moving hungergames spawn because spawn is a gold block");
             return;
         }
 
         debugInfo("    CurrentSpawn:" + spawn);
         spawn = new Location(spawn.getWorld(), spawn.getX() + 0.5, 250, spawn.getZ() + 0.5);
         debugInfo("    PreJiggleSpawn:" + spawn);
 //        spawn = jiggleLocation(spawn, arenaSizeThisGame);
         spawn = placeRandomlyWithRadius(spawn, arenaSizeThisGame * landingPadSize);
         debugInfo("    PostJiggleSpawn:" + spawn);
         spawn = BlockUtils.getHighestLandLocation(spawn);
         debugInfo("    NewSpawn:" + spawn);
        w.setSpawnLocation(spawn.getBlockX() % 2000, spawn.getBlockY() + 1, spawn.getBlockZ() % 2000);
 
         // clear the spawn area
         spawn = w.getSpawnLocation();
         int originX = spawn.getBlockX();
         int originY = spawn.getBlockY();
         int originZ = spawn.getBlockZ();
         int radius = landingPadSize + 3;
         int radiusSquared = radius * radius;
 
         for (int z = -radius; z <= radius; z++) {
             for (int x = -radius; x <= radius; x++) {
                 if (x * x + z * z <= radiusSquared) {
                     int actualX = originX + x;
                     int actualZ = originZ + z;
                     for (int y = originY + 15; y >= originY; y--) {
                         w.getBlockAt(actualX, y, actualZ).setType(Material.AIR);
                     }
                     w.getBlockAt(actualX, originY - 1, actualZ).setType(Material.GRASS);
                 }
             }
         }
     }
 
     private void teleportPlayerToArena(Player player, String worldName, int index, int totalPlayers) {
         debugInfo("Teleported player " + player.getDisplayName() + " to " + worldName);
         World gameWorld = getPlugin().getServer().getWorld(worldName);
         Location spawn = gameWorld.getSpawnLocation().clone();
         Location location = spawn.getBlock().getLocation().clone();
 
         double angle = 2 * Math.PI / totalPlayers * index;
 
         location.setX(location.getX() + landingPadSize * Math.cos(angle));
         location.setZ(location.getZ() + landingPadSize * Math.sin(angle));
 
         location = BlockUtils.getHighestLandLocation(location);
         //TODO hey jf - set the block at this location to be something recognizable
         location.getBlock().setType(Material.DIAMOND_BLOCK);
        location.add(0.5, 1, 0.5);
         player.teleport(location);
         player.sendMessage("Good luck.");
         //place 3 random gifts per player
         for (int i = 0; i < giftsPerPlayer; i++) {
             Location giftLoc = spawn.clone();
 //            giftLoc = jiggleLocation(giftLoc, gameInstance.getNumberOfGamePlayers() - 1);
             giftLoc = placeRandomlyWithRadius(giftLoc, arenaSizeThisGame / 2);
             giftLoc = BlockUtils.getHighestLandLocation(giftLoc);
             giftLoc.add(0, 1, 0);
             Material gift = gifts[rand.nextInt(gifts.length)];
             gameWorld.dropItemNaturally(giftLoc, new ItemStack(gift, 1));
         }
         debugInfo("  Teleported player " + player.getDisplayName() + " to " + worldName + " complete");
     }
 
     private void removeAllPlayersFromArena(String worldName) {
         debugInfo("Removed all players from arena");
         World w = getPlugin().getServer().getWorld(worldName);
         World safeWorld = getPlugin().getServer().getWorlds().get(0);
         for (Player p : w.getPlayers()) {
             p.teleport(safeWorld.getSpawnLocation(), null);
         }
     }
 
     private Location placeRandomlyWithRadius(Location loc, int radius) {
         Random rand = new Random();
         int radians = rand.nextInt();
        int distance = (int) (rand.nextDouble() * radius);
         Location result = loc.clone();
         result.setX(result.getX() + distance * Math.cos(radians));
         result.setY(result.getY() + distance * Math.sin(radians));
         return result;
     }
 
 /*    private Location jiggleLocation(Location loc, int jiggleFactor) {
         //the more players, the bigger the jiggle
         int jiggle = landingPadSize * jiggleFactor;
         Location result = loc.clone();
         result.add(rand.nextInt(jiggle * 2) - jiggle, 0, rand.nextInt(jiggle * 2) - jiggle);
         return result;
     }*/
 
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
 
 

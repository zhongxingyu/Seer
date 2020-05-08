 package com.entrocorp.linearlogic.oneinthegun.game;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Score;
 import org.bukkit.scoreboard.Scoreboard;
 
 import com.entrocorp.linearlogic.oneinthegun.OITG;
 import com.entrocorp.linearlogic.oneinthegun.util.HLComparablePair;
 import com.entrocorp.linearlogic.oneinthegun.util.Pair;
 import com.entrocorp.linearlogic.oneinthegun.util.TriMap;
 
 public class Arena implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     private String name;
 
     private boolean closed;
     private boolean allowBlockPlace;
     private boolean allowBlockBreak;
     private boolean allowHealthRegen;
     private boolean allowHunger;
     private boolean allowItemDrop;
     private boolean allowItemPickup;
     private boolean allowMobCombat;
 
     private int startCount;
     private int playerLimit;
     private int timeLimit;
     private int killLimit;
 
     private SerializableLocation lobby;
     private ArrayList<SerializableLocation> spawns;
     private ArrayList<SerializableLocation> signLocations;
 
     private transient Scoreboard board;
     private transient Objective objective;
 
     private transient int stage;
     private transient int timer;
 
     private transient Set<Pair<Player, HLComparablePair<Integer, Integer>>> leaderboard;
     private transient Set<Player> godmodePlayers;
     private transient HashMap<Player, Integer> killstreaks;
     private transient TriMap<Player, Integer, Integer> playerData;
 
     public Arena(String name) {
         this.name = name;
         init();
     }
 
     public void init() {
         if (startCount < 2)
             startCount = 10;
         if (playerLimit < 2 && playerLimit != -1)
             playerLimit = 20;
         if (timeLimit < 10 && timeLimit != -1)
             timeLimit = 300;
         if (killLimit < 1 && killLimit != -1)
             killLimit = 10;
         if (spawns == null)
             spawns = new ArrayList<SerializableLocation>();
         if (signLocations == null)
             signLocations = new ArrayList<SerializableLocation>();
         playerData = new TriMap<Player, Integer, Integer>();
         leaderboard = playerData.sortedEntrySet();
         godmodePlayers = new HashSet<Player>();
         killstreaks = new HashMap<Player, Integer>();
         board = OITG.instance.getServer().getScoreboardManager().getNewScoreboard();
         objective = board.registerNewObjective("kills", "dummy");
         objective.setDisplayName("" + ChatColor.DARK_RED + ChatColor.BOLD + "\u00AB Kills \u00BB");
         objective.setDisplaySlot(DisplaySlot.SIDEBAR);
         setStage(0);
     }
 
     public boolean save(boolean logSuccess) {
         try {
             File arenaDir = new File(OITG.instance.getDataFolder() + File.separator + "arenas");
             arenaDir.mkdirs();
             ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(arenaDir, name.toLowerCase() + ".arena")));
             oos.writeObject(this);
             oos.close();
             if (logSuccess)
                 OITG.instance.logInfo("Saved arena \"" + name + "\"");
             return true;
         } catch (IOException e) {
             OITG.instance.logSevere("Failed to save arena \"" + name + "\"");
             e.printStackTrace();
             return false;
         }
     }
 
     public void delete() {
         wipeSigns();
         new File(OITG.instance.getDataFolder() + File.separator + "arenas", name.toLowerCase() + ".arena").delete();
         OITG.instance.logInfo("Deleted arena \"" + name + "\"");
     }
 
     public void broadcast(String message) {
         for (Player player : playerData.keySet())
             player.sendMessage(OITG.prefix + "<" + ChatColor.YELLOW + name + ChatColor.GRAY + "> " + message);
     }
 
     public void startRound() {
         if (closed || playerData.size() < 2 || stage == 2)
             return;
         Location[] spawns = getSpawns();
         int index = -1;
         for (Player player : playerData.keySet()) {
             player.setGameMode(GameMode.SURVIVAL);
             armPlayer(player);
             player.teleport(spawns[++index % spawns.length]);
         }
         setAllPlayersInGodmode(true);
         OITG.instance.getServer().getScheduler().scheduleSyncDelayedTask(OITG.instance, new Runnable() {
             public void run() {
                 setAllPlayersInGodmode(false);
             }
         }, OITG.spawnShieldDuration * 20L);
         setStage(2);
         broadcast("" + ChatColor.RED + ChatColor.BOLD + "Game on!");
     }
 
     public void endRound() {
         if (stage != 2)
             return;
         Player victor = leaderboard.iterator().next().getX();
         OITG.instance.getServer().broadcastMessage(OITG.prefix + ChatColor.YELLOW + ChatColor.BOLD + victor.getName() +
                 ChatColor.GRAY + " emerges victorious from arena " + ChatColor.YELLOW + name + ChatColor.GRAY + "!");
         OITG.instance.getListenerManager().fireVictoryEvent(victor);
         clearPlayers();
     }
 
     public String toString() {
         return name;
     }
 
     public String getName() {
         return name;
     }
 
     public boolean setName(String name) {
         if (this.name.equalsIgnoreCase(name))
             return false;
         delete();
         this.name = name;
         save(true);
         populateSigns();
         return true;
     }
 
     public boolean isClosed() {
         return closed;
     }
 
     public void setClosed(boolean closed) {
         this.closed = closed;
         if (closed) {
             broadcast(ChatColor.DARK_RED + "The arena has been closed by an administrator");
             clearPlayers();
             return;
         }
         populateSigns();
     }
 
     public boolean isBlockPlacingAllowed() {
         return allowBlockPlace;
     }
 
     public void allowBlockPlacing(boolean allowed) {
         if (allowBlockPlace == allowed)
             return;
         allowBlockPlace = allowed;
         if (OITG.saveOnEdit)
             save(false);
     }
 
     public boolean isBlockBreakingAllowed() {
         return allowBlockBreak;
     }
 
     public void allowBlockBreaking(boolean allowed) {
         if (allowBlockBreak == allowed)
             return;
         allowBlockBreak = allowed;
         if (OITG.saveOnEdit)
             save(false);
     }
 
     public boolean isHealthRegenAllowed() {
         return allowHealthRegen;
     }
 
     public void allowHealthRegen(boolean allowed) {
         if (allowHealthRegen == allowed)
             return;
         allowHealthRegen = allowed;
         if (OITG.saveOnEdit)
             save(false);
     }
 
     public boolean isHungerAllowed() {
         return allowHunger;
     }
 
     public void allowHunger(boolean allowed) {
         if (allowHunger == allowed)
             return;
         allowHunger = allowed;
         if (OITG.saveOnEdit)
             save(false);
     }
 
     public boolean isItemDroppingAllowed() {
         return allowItemDrop;
     }
 
     public void allowItemDropping(boolean allowed) {
         if (allowItemDrop == allowed)
             return;
         allowItemDrop = allowed;
         if (OITG.saveOnEdit)
             save(false);
     }
 
     public boolean isItemPickupAllowed() {
         return allowItemPickup;
     }
 
     public void allowItemPickup(boolean allowed) {
         if (allowItemPickup == allowed)
             return;
         allowItemPickup = allowed;
         if (OITG.saveOnEdit)
             save(false);
     }
 
     public boolean isMobCombatAllowed() {
         return allowMobCombat;
     }
 
     public void allowMobCombat(boolean allowed) {
         if (allowMobCombat == allowed)
             return;
         allowMobCombat = allowed;
         if (OITG.saveOnEdit)
             save(false);
     }
 
     public boolean isWaiting() {
         return stage == 0;
     }
 
     public boolean isStarting() {
         return stage == 1;
     }
 
     public boolean isIngame() {
         return stage == 2;
     }
 
     public int getStage() {
         return stage;
     }
 
     public void setStage(int stage) {
         if (this.stage == stage)
             return;
         switch(stage) {
             default:
             case 0:
                 this.stage = 0;
                 timer = -1;
                 if (!OITG.instance.getArenaManager().areAnyArenasIngame())
                     OITG.instance.getArenaManager().stopTimers();
                 break;
             case 1:
                 this.stage = 1;
                 timer = OITG.pregameDuration;
                 if (timer < 1)
                     timer = 1;
                 OITG.instance.getArenaManager().startTimers();
                 break;
             case 2:
                 this.stage = 2;
                 timer = timeLimit;
                 OITG.instance.getArenaManager().startTimers();
                 updateLeaderboard();
                 updateScoreboard();
                 break;
         }
         populateSigns();
     }
 
     public int getTimer() {
         return timer;
     }
 
     public String getTimerFormatted() {
         return timer == -1 ? "infinity" : String.format("%02d:%02d", timer / 60, timer % 60);
     }
 
     public void setTimer(int seconds) {
         if (stage == 0)
             return;
         timer = seconds < 1 ? 1 : seconds; // Add a one second delay if < 1 to ensure that start- and end-game handling occurs
     }
 
     public void decrementTimer() {
         if (stage == 0 || timer == -1)
             return;
         switch(--timer) {
             case 0:
                 if (stage == 2)
                     endRound();
                 else
                     startRound();
                 break;
             case 10:
             case 30:
                 broadcast(ChatColor.RED + "The round " + (stage == 1 ? "starts" : "ends") + " in " + ChatColor.LIGHT_PURPLE +
                         timer + " seconds" + ChatColor.GRAY + "!");
                 break;
             default:
                 if (timer % 60 == 0)
                     broadcast(ChatColor.GRAY + "The round " + (stage == 1 ? "starts" : "ends") + " in " + ChatColor.LIGHT_PURPLE + (timer == 60 ? "1 minute" :
                             timer / 60 + " minutes") + ChatColor.GRAY + "!");
                 break;
         }
     }
 
     public int getStartCount() {
         return startCount;
     }
 
     public void setStartCount(int count) {
         if (startCount == count)
             return;
         startCount = count;
         if (OITG.saveOnEdit)
             save(false);
         populateSigns();
     }
 
     public int getPlayerLimit() {
         return playerLimit;
     }
 
     public void setPlayerLimit(int limit) {
         if (playerLimit == limit)
             return;
         playerLimit = limit;
         if (OITG.saveOnEdit)
             save(false);
         populateSigns();
     }
 
     public int getTimeLimit() {
         return timeLimit;
     }
 
     public String getTimeLimitFormatted() {
         return timeLimit == -1 ? "none" : String.format("%02d:%02d", timeLimit / 60, timeLimit % 60);
     }
 
     public void setTimeLimit(int limit) {
         if (timeLimit == limit)
             return;
         timeLimit = limit;
         if (OITG.saveOnEdit)
             save(false);
     }
 
     public int getKillLimit() {
         return killLimit;
     }
 
     public void setKillLimit(int limit) {
         if (killLimit == limit)
             return;
         killLimit = limit;
         if (OITG.saveOnEdit)
             save(false);
     }
 
     public Location getLobby() {
         return lobby == null ? null : lobby.asBukkitLocation();
     }
 
     public void setLobby(Location loc) {
         lobby = new SerializableLocation(loc);
         if (OITG.saveOnEdit)
             save(false);
     }
 
     public Location[] getSpawns() {
         Location[] output = new Location[spawns.size()];
         for (int i = 0; i < output.length; i++)
             output[i] = spawns.get(i).asBukkitLocation();
         return output;
     }
 
     public Location getRandomSpawn() {
         return spawns.get(new Random().nextInt(spawns.size())).asBukkitLocation();
     }
 
     public boolean isSpawn(Location loc) {
         return spawns.contains(new SerializableLocation(loc));
     }
 
     public void addSpawn(Location loc) {
         spawns.add(new SerializableLocation(loc));
         if (OITG.saveOnEdit)
             save(false);
     }
 
     public void clearSpawns() {
         spawns.clear();
         if (OITG.saveOnEdit)
             save(false);
     }
 
     public Location[] getSignLocations() {
         Location[] output = new Location[signLocations.size()];
         for (int i = 0; i < output.length; i++)
             output[i] = signLocations.get(i).asBukkitLocation();
         return output;
     }
 
     public boolean isSignLocation(Location loc) {
         return signLocations.contains(new SerializableLocation(loc));
     }
 
     public boolean addSignLocation(Location loc) {
         SerializableLocation sloc = new SerializableLocation(loc);
         if (signLocations.contains(sloc))
             return false;
         signLocations.add(sloc);
         populateSign(loc);
         if (OITG.saveOnEdit)
             save(false);
         return true;
     }
 
     public void removeSignLocation(Location loc) {
         wipeSign(loc);
         signLocations.remove(new SerializableLocation(loc));
         if (OITG.saveOnEdit)
             save(false);
     }
 
     public void clearSignLocations() {
         wipeSigns();
         signLocations.clear();
     }
 
     public boolean populateSign(Location loc) {
         if (!signLocations.contains(new SerializableLocation(loc)))
             return false;
         Block block = loc.getBlock();
         if (!block.getType().equals(Material.SIGN_POST) && !block.getType().equals(Material.WALL_SIGN))
             return false;
         Sign sign = (Sign) block.getState();
         sign.setLine(0, name);
        sign.setLine(1, getState());
        sign.setLine(2, playerData.size() + "/" + (playerLimit == -1 ? "infinity" : playerLimit));
        sign.setLine(3, playerData.size() < startCount ? startCount - playerData.size() + " to start" : null);
         return sign.update();
     }
 
     public void populateSigns() {
         String[] lines = {name, getState(), playerData.size() + "/" + (playerLimit == -1 ? "infinity" : playerLimit),
                 playerData.size() < startCount ? startCount - playerData.size() + " to start" : null};
         for (SerializableLocation sloc : signLocations) {
             Block block = sloc.asBukkitLocation().getBlock();
             if (!block.getType().equals(Material.SIGN_POST) && !block.getType().equals(Material.WALL_SIGN))
                 continue;
             Sign sign = (Sign) block.getState();
             for (int i = 0; i < 4; i++)
                 sign.setLine(i, lines[i]);
             sign.update();
         }
     }
 
     public boolean wipeSign(Location loc) {
         if (!signLocations.contains(new SerializableLocation(loc)))
             return false;
         Block block = loc.getBlock();
         if (!block.getType().equals(Material.SIGN_POST) && !block.getType().equals(Material.WALL_SIGN))
             return false;
         Sign sign = (Sign) block.getState();
         sign.setLine(0, null);
         sign.setLine(1, null);
         sign.setLine(2, null);
         sign.setLine(3, null);
         return sign.update();
     }
 
     public void wipeSigns() {
         for (SerializableLocation sloc : signLocations) {
             Block block = sloc.asBukkitLocation().getBlock();
             if (!block.getType().equals(Material.SIGN_POST) && !block.getType().equals(Material.WALL_SIGN))
                 continue;
             Sign sign = (Sign) block.getState();
             sign.setLine(0, null);
             sign.setLine(1, null);
             sign.setLine(2, null);
             sign.setLine(3, null);
         }
     }
 
     public Player[] getPlayers() {
         return playerData.keySet().toArray(new Player[playerData.size()]);
     }
 
     public Player getPlayerWithMostKills() {
         return leaderboard.iterator().next().getX();
     }
 
     public int getPlayerCount() {
         return playerData.size();
     }
 
     public boolean containsPlayer(Player player) {
         return playerData.containsKey(player);
     }
 
     public boolean addPlayer(Player player) {
         if (playerData.containsKey(player))
             return false;
         playerData.put(player, 0, 0);
         populateSigns();
         OITG.instance.getListenerManager().getGameListener().setRegistered(true);
         if (stage == 0 && playerData.size() == startCount) {
             broadcast(player.getName() + " has joined the arena. The round will start shortly.");
             setStage(1);
             return true;
         }
         broadcast(player.getName() + " has joined the arena.");
         return true;
     }
 
     public boolean removePlayer(Player player, boolean broadcast) {
         if (playerData.remove(player) == null)
             return false;
         godmodePlayers.remove(player);
         killstreaks.remove(player);
         player.setScoreboard(OITG.instance.getServer().getScoreboardManager().getNewScoreboard());
         player.getInventory().clear();
         player.teleport(OITG.instance.getArenaManager().getGlobalLobby());
         populateSigns();
         if (broadcast)
             broadcast(player.getName() + " has " + (stage == 2 ? "fled" : "left") + " the arena!");
         switch(stage) {
             case 0:
                 if (playerData.size() == 0 && OITG.instance.getArenaManager().areAllArenasEmpty())
                     OITG.instance.getListenerManager().getGameListener().setRegistered(false);
                 break;
             case 1:
                 if (playerData.size() <= 1) {
                     broadcast(ChatColor.RED + "There must be at least two players to start the round. Cancelling the countdown...");
                     setStage(0);
                 }
                 break;
             case 2:
                 if (playerData.size() == 1) {
                     endRound();
                     return true;
                 }
                 updateLeaderboard();
                 Iterator<Pair<Player, HLComparablePair<Integer, Integer>>> iter = leaderboard.iterator();
                 for (int i = 0; i < 10; i++) {
                     if (!iter.hasNext())
                         break;
                     if (iter.next().getX().equals(player)) { // Player was in the top 10 and thus was listed on the scoreboard
                         updateScoreboard();
                         break;
                     }
                 }
                 break;
         }
         return true;
     }
 
     public void clearPlayers() {
         closeScoreboard();
         for (Player player : playerData.keySet()) {
             player.getInventory().clear();
             player.teleport(OITG.instance.getArenaManager().getGlobalLobby());
         }
         playerData.clear();
         godmodePlayers.clear();
         killstreaks.clear();
         setStage(0);
         populateSigns();
         updateLeaderboard();
         if (OITG.instance.getArenaManager().areAllArenasEmpty())
             OITG.instance.getListenerManager().getGameListener().setRegistered(false);
     }
 
     public Set<Pair<Player, HLComparablePair<Integer, Integer>>> getLeaderboard() {
         return leaderboard;
     }
 
     public void updateLeaderboard() {
         leaderboard = playerData.sortedEntrySet();
     }
 
     public int getKills(Player player) {
         Integer kills = playerData.getX(player);
         return kills == null ? 0 : kills;
     }
 
     public boolean setKills(Player player, int kills) {
         if (!playerData.setX(player, kills))
             return false;
         if (killLimit != -1 && kills >= killLimit) {
             endRound();
             return true;
         }
         updateLeaderboard();
         Iterator<Pair<Player, HLComparablePair<Integer, Integer>>> iter = leaderboard.iterator();
         for (int i = 0; i < 10; i++) {
             if (!iter.hasNext())
                 break;
             if (iter.next().getX().equals(player)) {
                 updateScoreboard();
                 break;
             }
         }
         return true;
     }
 
     public boolean incrementKills(Player player) {
         int kills = getKills(player);
         if (!setKills(player, kills + 1))
             return false;
         if (OITG.killstreaks) {
             killstreaks.put(player, getCurrentKillstreak(player) + 1);
             OITG.instance.getListenerManager().fireKillstreakChangeEvent(player, kills, kills + 1);
         }
         return true;
     }
 
     public int getDeaths(Player player) {
         Integer deaths = playerData.getY(player);
         return deaths == null ? 0 : deaths;
     }
 
     public boolean setDeaths(Player player, int deaths) {
         if (!playerData.setY(player, deaths))
             return false;
         updateLeaderboard();
         return true;
     }
 
     public boolean incrementDeaths(Player player) {
         if (!setDeaths(player, getDeaths(player) + 1))
             return false;
         if (OITG.killstreaks) {
             int previousKills = getCurrentKillstreak(player);
             if (previousKills != 0) {
                 killstreaks.put(player, 0);
                 OITG.instance.getListenerManager().fireKillstreakChangeEvent(player, previousKills, 0);
             }
         }
         return true;
     }
 
     public void killPlayer(final Player player) {
         if (!incrementDeaths(player))
             return;
         player.setHealth(player.getMaxHealth());
         player.teleport(getRandomSpawn());
         armPlayer(player);
         godmodePlayers.add(player);
         OITG.instance.getServer().getScheduler().scheduleSyncDelayedTask(OITG.instance, new Runnable() {
             public void run() {
                 godmodePlayers.remove(player);
             }
         }, OITG.spawnShieldDuration * 20L);
     }
 
     public double getKDR(Player player) {
         if (!playerData.containsKey(player))
             return -1.0;
         return getDeaths(player) == 0 ? getKills(player) : getKills(player) / (double) getDeaths(player);
     }
 
     public boolean setPlayerData(Player player, int kills, int deaths) {
         if (!playerData.containsKey(player))
             return false;
         playerData.put(player, kills, deaths);
         return true;
     }
 
     public int getCurrentKillstreak(Player player) {
         return killstreaks.containsKey(player) ? killstreaks.get(player) : 0;
     }
 
     public Player getPlayerWithLongestKillstreak() {
         Player longest = null;
         int streak = 0;
         for (Entry<Player, Integer> entry : killstreaks.entrySet()) {
             if (entry.getValue() >= streak) {
                 longest = entry.getKey();
                 streak = entry.getValue();
             }
         }
         return longest;
     }
 
     public Player[] getPlayersWithKillstreak(int streak) {
         if (streak < 0)
             return new Player[0];
         List<Player> players = new ArrayList<Player>();
         for (Entry<Player, Integer> entry : killstreaks.entrySet())
             if (entry.getValue() == streak)
                 players.add(entry.getKey());
         return players.toArray(new Player[players.size()]);
     }
 
     public Player[] getPlayersInGodmode() {
         return godmodePlayers.toArray(new Player[godmodePlayers.size()]);
     }
 
     public boolean isPlayerInGodmode(Player player) {
         return godmodePlayers.contains(player);
     }
 
     public void setPlayerInGodmode(Player player, boolean godmode) {
         if (godmode)
             godmodePlayers.add(player);
         else
             godmodePlayers.remove(player);
     }
 
     public void setAllPlayersInGodmode(boolean godmode) {
         if (godmode)
             godmodePlayers.addAll(playerData.keySet());
         else
             godmodePlayers.clear();
     }
 
     public void armPlayer(Player player) {
         player.getInventory().clear();
         player.getInventory().setArmorContents(new ItemStack[4]);
         player.getInventory().addItem(new ItemStack(Material.BOW));
         player.getInventory().addItem(new ItemStack(Material.WOOD_SWORD));
         player.getInventory().addItem(new ItemStack(Material.ARROW));
     }
 
     public void updateScoreboard() {
         Iterator<Pair<Player, HLComparablePair<Integer, Integer>>> iter = leaderboard.iterator();
         for (int i = 0; i < 10; i++) {
             if (!iter.hasNext())
                 break;
             Pair<Player, HLComparablePair<Integer, Integer>> entry = iter.next();
             Score score = objective.getScore(entry.getX());
             if (entry.getY().getX() == 0) {
                 score.setScore(1);
                 score.setScore(0);
             } else {
                 score.setScore(entry.getY().getX());
             }
         }
         for (Player player : playerData.keySet())
             player.setScoreboard(board);
     }
 
     public void closeScoreboard() {
         for (Player player : playerData.keySet()) {
             board.resetScores(player);
             player.setScoreboard(OITG.instance.getServer().getScoreboardManager().getNewScoreboard());
         }
     }
 
     public String getState() {
         if (closed)
             return ChatColor.DARK_RED + "Closed";
         switch(stage) {
             default:
             case 0:
                 return ChatColor.GREEN + "Waiting";
             case 1:
                 return ChatColor.YELLOW + "Starting";
             case 2:
                 return ChatColor.RED + "In game";
         }
     }
 }

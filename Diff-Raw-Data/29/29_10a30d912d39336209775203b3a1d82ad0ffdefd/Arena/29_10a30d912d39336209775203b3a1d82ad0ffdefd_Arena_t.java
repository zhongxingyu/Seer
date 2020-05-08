 package com.entrocorp.linearlogic.oneinthegun.game;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
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
 
     private int playerLimit;
     private int timeLimit;
     private int killLimit;
 
     private SerializableLocation lobby;
     private ArrayList<SerializableLocation> spawns;
     private ArrayList<SerializableLocation> signLocations;
 
     private transient Scoreboard board;
     private transient Objective objective;
 
     private transient boolean ingame;
     private transient int timer;
 
     private transient Set<Pair<Player, HLComparablePair<Integer, Integer>>> leaderboard;
     private transient TriMap<Player, Integer, Integer> playerData;
 
 
     public Arena(String name) {
         this.name = name;
         init();
     }
 
     public void init() {
         if (playerLimit < 2 && playerLimit != -1)
             playerLimit = 10;
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
         board = OITG.instance.getServer().getScoreboardManager().getNewScoreboard();
         objective = board.registerNewObjective("kills", "totalKillCount");
         objective.setDisplayName("" + ChatColor.DARK_RED + ChatColor.BOLD + "\\u0171 Kills \\u0187");
         objective.setDisplaySlot(DisplaySlot.SIDEBAR);
         setIngame(false);
     }
 
     public boolean save() {
         try {
             File arenaDir = new File(OITG.instance.getDataFolder() + File.separator + "arenas");
             arenaDir.mkdirs();
             ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(arenaDir, name.toLowerCase() + ".arena")));
             oos.writeObject(this);
             oos.close();
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
 
     public void start() {
         Location[] spawns = getSpawns();
         int index = -1;
         for (Player player : playerData.keySet()) {
             player.setGameMode(GameMode.SURVIVAL);
             armPlayer(player);
             player.teleport(spawns[++index % spawns.length]);
         }
         setIngame(true);
         broadcast("" + ChatColor.RED + ChatColor.BOLD + "Game on!");
     }
 
     private void endgame() {
         OITG.instance.getServer().broadcastMessage(OITG.prefix + ChatColor.YELLOW + ChatColor.BOLD +
                 leaderboard.iterator().next().getX().getName() + ChatColor.GRAY + " emerges victorious from arena " +
                 ChatColor.YELLOW + name + ChatColor.GRAY + "!");
         clearPlayers();
     }
 
     public String toString() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
         delete();
         save();
         populateSigns();
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
             save();
     }
 
     public boolean isBlockBreakingAllowed() {
         return allowBlockBreak;
     }
 
     public void allowBlockBreaking(boolean allowed) {
         if (allowBlockBreak == allowed)
             return;
         allowBlockBreak = allowed;
         if (OITG.saveOnEdit)
             save();
     }
 
     public boolean isHealthRegenAllowed() {
         return allowHealthRegen;
     }
 
     public void allowHealthRegen(boolean allowed) {
         if (allowHealthRegen == allowed)
             return;
         allowHealthRegen = allowed;
         if (OITG.saveOnEdit)
             save();
     }
 
     public boolean isHungerAllowed() {
         return allowHunger;
     }
 
     public void allowHunger(boolean allowed) {
         if (allowHunger == allowed)
             return;
         allowHunger = allowed;
         if (OITG.saveOnEdit)
             save();
     }
 
     public boolean isItemDroppingAllowed() {
         return allowItemDrop;
     }
 
     public void allowItemDropping(boolean allowed) {
         if (allowItemDrop == allowed)
             return;
         allowItemDrop = allowed;
         if (OITG.saveOnEdit)
             save();
     }
 
     public boolean isItemPickupAllowed() {
         return allowItemPickup;
     }
 
     public void allowItemPickup(boolean allowed) {
         if (allowItemPickup == allowed)
             return;
         allowItemPickup = allowed;
         if (OITG.saveOnEdit)
             save();
     }
 
     public boolean isMobCombatAllowed() {
         return allowMobCombat;
     }
 
     public void allowMobCombat(boolean allowed) {
         if (allowMobCombat == allowed)
             return;
         allowMobCombat = allowed;
         if (OITG.saveOnEdit)
             save();
     }
 
     public boolean isIngame() {
         return ingame;
     }
 
     public void setIngame(boolean ingame) {
         if (this.ingame == ingame)
             return;
         this.ingame = ingame;
        populateSigns();
         if (ingame) {
             updateLeaderboard();
             timer = timeLimit;
             OITG.instance.getArenaManager().startTimers();
            return;
         }
         timer = -1;
         if (!OITG.instance.getArenaManager().areAnyArenasIngame())
             OITG.instance.getArenaManager().stopTimers();
     }
 
     public int getTimerRemaining() {
         return timer;
     }
 
     public String getTimeRemainingFormatted() {
         return timer == -1 ? "N/A" : String.format("%02d:%02d", timer / 60, timer % 60);
     }
 
     public void setTimeRemaining(int seconds) {
         if (!ingame)
             return;
         timer = seconds < 1 ? 1 : seconds; // Add a one second delay if < 1 to ensure that endgame handling occurs
     }
 
     public void decrementTimer() {
         if (!ingame || timer == -1)
             return;
         switch(--timer) {
             case 0:
                 endgame();
                 break;
             case 10:
             case 30:
                 broadcast(ChatColor.RED + "The round ends in " + ChatColor.LIGHT_PURPLE + timer + " seconds" + ChatColor.GRAY + "!");
                 break;
             default:
                 if (timer % 60 == 0)
                     broadcast(ChatColor.GRAY + "The round ends in " + ChatColor.LIGHT_PURPLE + (timer == 60 ? "1 minute" :
                             timer / 60 + " minutes") + ChatColor.GRAY + "!");
                 break;
         }
     }
 
     public int getPlayerLimit() {
         return playerLimit;
     }
 
     public void setPlayerLimit(int limit) {
         if (playerLimit == limit)
             return;
         playerLimit = limit;
         if (OITG.saveOnEdit)
             save();
         populateSigns();
     }
 
     public int getTimeLimit() {
         return timeLimit;
     }
 
     public String getTimeLimitFormatted() {
         return timeLimit == -1 ? "none" : String.format("%02d:%02d", timeLimit / 60, timeLimit % 60);
     }
 
     public void setTimeLimit(int limit) {
         timeLimit = limit;
         if (OITG.saveOnEdit)
             save();
     }
 
     public int getKillLimit() {
         return killLimit;
     }
 
     public void setKillLimit(int limit) {
         killLimit = limit;
         if (OITG.saveOnEdit)
             save();
     }
 
     public Location getLobby() {
         return lobby == null ? null : lobby.asBukkitLocation();
     }
 
     public void setLobby(Location loc) {
         lobby = new SerializableLocation(loc);
         if (OITG.saveOnEdit)
             save();
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
             save();
     }
 
     public void clearSpawns() {
         spawns.clear();
         if (OITG.saveOnEdit)
             save();
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
             save();
         return true;
     }
 
     public void removeSignLocation(Location loc) {
         wipeSign(loc);
         signLocations.remove(new SerializableLocation(loc));
         if (OITG.saveOnEdit)
             save();
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
         sign.setLine(1, null);
         sign.setLine(2, getState());
         sign.setLine(3, playerData.size() + "/" + playerLimit);
         return sign.update();
     }
 
     public void populateSigns() {
         for (SerializableLocation sloc : signLocations) {
             Block block = sloc.asBukkitLocation().getBlock();
             if (!block.getType().equals(Material.SIGN_POST) && !block.getType().equals(Material.WALL_SIGN))
                 continue;
             Sign sign = (Sign) block.getState();
             sign.setLine(0, name);
             sign.setLine(1, null);
             sign.setLine(2, getState());
             sign.setLine(3, playerData.size() + "/" + playerLimit);
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
         OITG.instance.getGameListener().setRegistered(true);
         broadcast(player.getName() + " has joined the arena.");
         return true;
     }
 
     public boolean removePlayer(Player player, boolean broadcast) {
         if (playerData.remove(player) == null)
             return false;
         board.resetScores(player);
         player.setScoreboard(OITG.instance.getServer().getScoreboardManager().getNewScoreboard());
         populateSigns();
         player.teleport(OITG.instance.getArenaManager().getGlobalLobby());
         if (broadcast)
             broadcast(player.getName() + " has fled the arena!");
         if (ingame) {
             if (playerData.size() == 1) {
                 endgame();
                 return true;
             }
             updateLeaderboard();
             return true;
         }
         if (playerData.size() == 0 && OITG.instance.getArenaManager().areAllArenasEmpty())
             OITG.instance.getGameListener().setRegistered(false);
         return true;
     }
 
     public void clearPlayers() {
         closeScoreboard();
         for (Player player : playerData.keySet())
             player.teleport(OITG.instance.getArenaManager().getGlobalLobby());
         playerData.clear();
         setIngame(false);
         populateSigns();
         updateLeaderboard();
         if (OITG.instance.getArenaManager().areAllArenasEmpty())
             OITG.instance.getGameListener().setRegistered(false);
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
        if (killLimit != -1 && kills >= killLimit)
            endgame();
         updateLeaderboard();
         return true;
     }
 
     public boolean incrementKills(Player player) {
         return setKills(player, getKills(player) + 1);
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
         return setDeaths(player, getDeaths(player) + 1);
     }
 
     public void killPlayer(Player player) {
         if (!incrementDeaths(player))
             return;
         player.setHealth(player.getMaxHealth());
         player.teleport(getRandomSpawn());
         armPlayer(player);
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
 
     public void armPlayer(Player player) {
         player.getInventory().clear();
         player.getInventory().setArmorContents(new ItemStack[4]);
         player.getInventory().setItem(0, new ItemStack(Material.BOW));
         player.getInventory().setItem(1, new ItemStack(Material.WOOD_SWORD));
         player.getInventory().setItem(8, new ItemStack(Material.ARROW));
     }
 
     public void loadScoreboard(boolean reset) {
         for (Player player : playerData.keySet()) {
             if (reset)
                 objective.getScore(player).setScore(0);
             player.setScoreboard(board);
         }
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
         if (ingame)
             return ChatColor.RED + "In game";
         return ChatColor.GREEN + "Waiting";
     }
 }

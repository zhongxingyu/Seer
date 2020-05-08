 package com.entrocorp.linearlogic.oneinthegun.game;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Map.Entry;
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Scoreboard;
 
 import com.entrocorp.linearlogic.oneinthegun.OITG;
 import com.entrocorp.linearlogic.oneinthegun.util.Pair;
 import com.entrocorp.linearlogic.oneinthegun.util.TriMap;
 
 public class Arena implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     private String name;
     private boolean closed;
 
     private int playerLimit;
     private int timeLimit;
     private int killLimit;
 
     private SerializableLocation lobby;
     private ArrayList<SerializableLocation> spawns;
     private ArrayList<SerializableLocation> signLocations;
 
     private transient Scoreboard board;
     private transient Objective objective;
 
     private transient boolean ingame;
     private transient TriMap<Player, Integer, Integer> playerData;
 
     public Arena(String name) {
         this.name = name;
         init();
     }
 
     public void init() {
         if (playerLimit < 2)
             playerLimit = 10;
         if (timeLimit < 2) // Ten seconds
             timeLimit = 120;
         if (killLimit != -1 && killLimit < 1)
             killLimit = 10;
         if (spawns == null)
             spawns = new ArrayList<SerializableLocation>();
         if (signLocations == null)
             signLocations = new ArrayList<SerializableLocation>();
         playerData = new TriMap<Player, Integer, Integer>();
         board = OITG.instance.getServer().getScoreboardManager().getNewScoreboard();
         objective = board.registerNewObjective("kills", "totalKillCount");
         objective.setDisplayName("" + ChatColor.DARK_RED + ChatColor.BOLD + " Kills ");
         objective.setDisplaySlot(DisplaySlot.SIDEBAR);
         ingame = false;
     }
 
     public void save() {
         try {
             File arenaDir = new File(OITG.instance.getDataFolder() + File.separator + "arenas");
             arenaDir.mkdirs();
             ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(arenaDir, name.toLowerCase() + ".arena")));
             oos.writeObject(this);
             oos.close();
             OITG.instance.logInfo("Saved arena \"" + name + "\"");
         } catch (IOException e) {
             OITG.instance.logSevere("Failed to save arena \"" + name + "\"");
             e.printStackTrace();
         }
     }
 
     public void delete() {
         new File(OITG.instance.getDataFolder() + File.separator + "arenas", name.toLowerCase() + ".arena").delete();
         OITG.instance.logInfo("Deleted arena \"" + name + "\"");
     }
 
     public void broadcast(String message) {
         for (Player player : playerData.keySet())
             player.sendMessage(OITG.prefix + "<" + ChatColor.YELLOW + name + ChatColor.GRAY + "> " + message);
     }
 
     public String toString() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public boolean isClosed() {
         return closed;
     }
 
     public void setClosed(boolean closed) {
         this.closed = closed;
         if (closed) {
             broadcast(ChatColor.DARK_RED + "The arena has been closed by an administrator");
             clearPlayers();
         }
     }
 
     public boolean isIngame() {
         return ingame;
     }
 
     public void setIngame(boolean ingame) {
         this.ingame = ingame;
     }
 
     public int getPlayerLimit() {
         return playerLimit;
     }
 
     public void setPlayerLimit(int limit) {
         this.playerLimit = limit;
     }
 
     public int getTimeLimit() {
         return timeLimit;
     }
 
     public void setTimeLimit(int limit) {
         timeLimit = limit;
     }
 
     public int getKillLimit() {
         return killLimit;
     }
 
     public void setKillLimit(int limit) {
         killLimit = limit;
     }
 
     public Location getLobby() {
        return lobby == null ? null : lobby.asBukkitLocation();
     }
 
     public void setLobby(Location loc) {
         lobby = new SerializableLocation(loc);
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
     }
 
     public void clearSpawns() {
         spawns.clear();
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
         return true;
     }
 
     public boolean removeSignLocation(Location loc) {
         wipeSign(loc);
         return signLocations.remove(new SerializableLocation(loc));
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
 
     public int getPlayerCount() {
         return playerData.size();
     }
 
     public boolean containsPlayer(Player player) {
         return playerData.keySet().contains(player);
     }
 
     public boolean addPlayer(Player player) {
         if (playerData.containsKey(player))
             return false;
         playerData.put(player, 0, 0);
         populateSigns();
         return true;
     }
 
     public boolean removePlayer(Player player) {
         if (playerData.remove(player) == null)
             return false;
         board.resetScores(player);
         player.setScoreboard(OITG.instance.getServer().getScoreboardManager().getNewScoreboard());
         populateSigns();
         return true;
     }
 
     public void clearPlayers() {
         closeScoreboard();
         playerData.clear();
         ingame = false;
         populateSigns();
     }
 
     public int getKills(Player player) {
         Integer kills = playerData.getX(player);
         return kills == null ? 0 : kills;
     }
 
     public boolean setKills(Player player, int kills) {
         return playerData.setX(player, kills);
     }
 
     public int getDeaths(Player player) {
         Integer deaths = playerData.getY(player);
         return deaths == null ? 0 : deaths;
     }
 
     public boolean setDeaths(Player player, int deaths) {
         return playerData.setY(player, deaths);
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
 
     public Player getPlayerWithMostKills() {
         int mostKills = 0;
         Player killer = null;
         for (Entry<Player, Pair<Integer, Integer>> entry : playerData.entrySet()) {
             int kills = entry.getValue().getX();
             if (kills > mostKills) {
                 mostKills = kills;
                 killer = entry.getKey();
             }
         }
         return killer;
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

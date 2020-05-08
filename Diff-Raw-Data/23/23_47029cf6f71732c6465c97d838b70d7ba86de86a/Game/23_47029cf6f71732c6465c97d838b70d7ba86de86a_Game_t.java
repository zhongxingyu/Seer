 package me.cmastudios.ironball;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import me.cmastudios.ironball.task.GameStartTimer;
 import org.apache.commons.lang.Validate;
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Sound;
 import org.bukkit.entity.IronGolem;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.scheduler.BukkitTask;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Scoreboard;
 
 /**
  * Represents an active game of IronBall. This controls players, teams, and the
  * arena itself.
  *
  * @author Connor
  */
 public class Game {
 
     private IronBall plugin;
     private final Arena arena;
     private Map<String, TeamType> players;
     private Map<String, ItemStack[]> inventories;
     private Map<String, ItemStack[]> armor;
     private Map<TeamType, Integer> points;
     private IronGolem golem;
     /**
      * Storage for repeating tasks.
      */
     private Map<Runnable, BukkitTask> tasks;
     private Scoreboard scoreboard;
 
     public Game(Arena arena) {
         this.arena = arena;
         this.players = new HashMap();
         this.inventories = new HashMap();
         this.armor = new HashMap();
         this.tasks = new HashMap();
         this.points = new HashMap();
         this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
         scoreboard.registerNewObjective("Points", "dummy");
         Objective objective = scoreboard.getObjective("Points");
         objective.setDisplayName("Points");
         objective.setDisplaySlot(DisplaySlot.SIDEBAR);
         objective.getScore(Bukkit.getOfflinePlayer(TeamType.RED.getDisplayName())).setScore(0);
         objective.getScore(Bukkit.getOfflinePlayer(TeamType.BLUE.getDisplayName())).setScore(0);
     }
 
     /**
      * Schedule starting tasks in order to launch the game.
      *
      * @param plugin Plugin to schedule as.
      */
     public void startGame(IronBall plugin) {
         this.plugin = plugin;
         GameStartTimer gameStartTimer = new GameStartTimer(this, plugin); 
         plugin.getServer().getScheduler().runTaskLater(plugin, gameStartTimer, 600L);
     }
 
     /**
      * Cancel a game. This removes all players from the game. It is the caller's
      * responsibility to remove this game from the
      * {@link me.cmastudios.ironball.IronBall#activeGames} list.
      */
     public void endGame() {
         for (BukkitTask task : tasks.values()) {
             task.cancel();
         }
         for (Iterator<String> it = this.getPlayerMap().keySet().iterator(); it.hasNext();) {
             String str = it.next();
             it.remove();
             Player player = Bukkit.getPlayerExact(str);
             this.restoreState(player);
         }
         if (golem != null) {
             golem.remove();
         }
     }
 
     /**
      * Get the winning team.
      *
      * @return the winning team or null if neither have won yet.
      */
     public TeamType getWinningTeam() {
         int redScore = scoreboard.getObjective("Points").getScore(Bukkit.getOfflinePlayer(TeamType.RED.getDisplayName())).getScore();
         int blueScore = scoreboard.getObjective("Points").getScore(Bukkit.getOfflinePlayer(TeamType.BLUE.getDisplayName())).getScore();
         if (redScore >= arena.getMaxScore()) {
             return TeamType.RED;
         } else if (blueScore >= arena.getMaxScore()) {
             return TeamType.BLUE;
         }
         return null;
     }
 
     /**
      * Send a message if a team won.
      */
     public void checkWin() {
         TeamType winningTeam = this.getWinningTeam();
         if (winningTeam != null) {
             this.broadcastMessage(IronBall.getPrefix() + IronBall.getString("GAME.WIN", new Object[]{winningTeam.getDisplayName(), arena.getMaxScore()}));
         }
     }
 
     /**
      * Checks if teams are unbalanced. Unbalanced means that one team has 2 or
      * more players than the other team.
      *
      * @return true if teams are unbalanced.
      */
     public boolean areTeamsUnbalanced() {
         return Math.abs(this.getTeam(TeamType.RED).size() - this.getTeam(TeamType.BLUE).size()) >= 2;
     }
 
     /**
     * Checks if adding a player to a team would cause an imbalance. This does
     * not actually add a player to a team, it only tests it.
      *
      * @param team Team to add a player to.
     * @return true if adding a player to this team would cause an imbalance.
      */
     public boolean addPlayerWouldCauseUnbalance(TeamType team) {
         int redPlayers = this.getTeam(TeamType.RED).size() + (team == TeamType.RED ? 1 : 0);
         int bluePlayers = this.getTeam(TeamType.BLUE).size() + (team == TeamType.BLUE ? 1 : 0);
         return Math.abs(redPlayers - bluePlayers) >= 2;
     }
 
     /**
      * Get players in a team. This scans the entire player map for players on
      * the indicated team.
      *
      * @param team Team color.
      * @return list of players on the team.
      */
     public List<Player> getTeam(TeamType team) {
         List<Player> teamPlayers = new ArrayList();
         for (String str : getPlayerMap().keySet()) {
             if (getPlayerMap().get(str) == team) {
                 teamPlayers.add(Bukkit.getPlayerExact(str));
             }
         }
         return teamPlayers;
     }
 
     /**
      * Reorganizes players in the teams to make them fairly balanced.
      */
     public void balanceTeams() {
         if (!this.areTeamsUnbalanced()) {
             return;
         }
         this.broadcastMessage(IronBall.getPrefix() + IronBall.getString("GAME.REBALANCE"));
         List<Player> redTeam = this.getTeam(TeamType.RED);
         List<Player> blueTeam = this.getTeam(TeamType.BLUE);
         int overflowAmount = Math.abs(redTeam.size() - blueTeam.size());
         TeamType overflowTeam = redTeam.size() > blueTeam.size() ? TeamType.RED : TeamType.BLUE;
         int playersToGrab = (int) Math.floor(overflowAmount / 2);
         Random rng = new Random();
         for (int x = 0; x < playersToGrab; x++) {
             int random = rng.nextInt(this.getTeam(overflowTeam).size());
             Player player = redTeam.get(random);
             this.removePlayer(player);
             this.addPlayerToTeam(player, overflowTeam == TeamType.RED ? TeamType.BLUE : TeamType.RED);
         }
     }
 
     /**
      * Find a team for a player. Using this will always prevent unbalanced
      * teams.
      *
      * @return team.
      */
     public TeamType autoAssign() {
         if (this.areTeamsUnbalanced()) {
             this.balanceTeams();
         }
         if (this.addPlayerWouldCauseUnbalance(TeamType.RED)) {
             return TeamType.BLUE;
         } else {
             return TeamType.RED;
         }
     }
 
     /**
      * Adds a player to a team. This stores a player's inventory, gives him the
      * proper items and teleports him to his team.
      *
      * @param player Player to add.
      * @param team The player's new team.
      */
     public void addPlayerToTeam(Player player, TeamType team) {
         Validate.isTrue(!isPlaying(player), "Player is already playing in this game.");
         getInventories().put(player.getName(), player.getInventory().getContents());
         this.armor.put(player.getName(), player.getInventory().getArmorContents());
         player.getInventory().clear();
         player.getInventory().setArmorContents(new ItemStack[0]);
         player.setHealth(20);
         player.setFoodLevel(20);
         player.setExhaustion(0);
         player.setSaturation(20);
         team.addItems(player);
         player.setGameMode(GameMode.ADVENTURE);
         Location teleport = team.getTeamSpawn(getArena()).add(0, 2, 0);
         teleport.setPitch(90);
         getPlayerMap().put(player.getName(), team);
         player.setScoreboard(scoreboard);
         player.teleport(teleport);
         player.getWorld().playSound(teleport, Sound.ENDERMAN_TELEPORT, 8, 1);
         this.broadcastMessage(IronBall.getString("GAME.PLAYERJOIN", new Object[]{player.getDisplayName(), team.getDisplayName()}));
     }
 
     /**
      * Remove a player from the game. This restores their inventory and
      * teleports them back to the spectator point.
      *
      * @param player Player to remove.
      */
     public void removePlayer(Player player) {
         if (!this.getPlayers().contains(player)) {
             return;
         }
         this.getPlayerMap().remove(player.getName());
        this.restoreState(player);
         this.broadcastMessage(IronBall.getString("GAME.PLAYERQUIT", new Object[]{player.getDisplayName()}));
        if (this.getPlayerMap().size() < MIN_PLAYERS) {
             this.endGame();
         }
     }
 
     private void restoreState(Player player) {
         player.getInventory().clear();
         player.getInventory().setArmorContents(new ItemStack[0]);
         ItemStack[] items = getInventories().remove(player.getName());
         ItemStack[] armor = this.armor.remove(player.getName());
         if (items != null) {
             player.getInventory().setContents(items);
         }
         if (armor != null) {
             player.getInventory().setArmorContents(armor);
         }
         player.setHealth(20);
         player.setFoodLevel(20);
         player.setExhaustion(0);
         player.setSaturation(20);
         player.setGameMode(GameMode.SURVIVAL);
         player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
         player.teleport(Arena.deserializeLocation(getArena().getSpectatorSpawn()));
         player.getWorld().playSound(Arena.deserializeLocation(getArena().getSpectatorSpawn()), Sound.ENDERMAN_TELEPORT, 8, 1);
     }
 
     /**
      * Add a point to a team.
      *
      * @param team Team to receive the point
      * @param amount Amount of points to add
      */
     public boolean addPoint(TeamType team, int amount) {
         Validate.notNull(team);
         this.points.put(team, (points.containsKey(team) == true ? points.get(team) : 0) + amount);
         this.scoreboard.getObjective("Points").getScore(Bukkit.getOfflinePlayer(team.getDisplayName())).setScore(points.get(team));
         this.broadcastMessage(IronBall.getPrefix() + IronBall.getString("GAME.POINT", new Object[]{team.getDisplayName(), amount}));
         if (this.points.get(team) >= arena.getMaxScore()) {
             this.broadcastMessage(IronBall.getPrefix() + IronBall.getString("GAME.WIN", new Object[] {team.getDisplayName(), arena.getMaxScore()}));
             this.endGame();
             this.plugin.activeGames.remove(this);
             return true;
         }
         return false;
     }
     public static final int MIN_PLAYERS = 1; // For testing
 
     /**
     * Checks if a game can be started. A game can be started if the minimum
     * player count is satisfied.
      *
     * @return true if there are enough players registered.
     * @see #MIN_PLAYERS
      */
     public boolean canStartGame() {
         return this.getPlayerMap().size() >= MIN_PLAYERS;
     }
 
     /**
      * Check if a player is playing in this game.
      *
      * @param player Player to check.
      * @return true if the player is in this game.
      */
     public boolean isPlaying(Player player) {
         return this.players.containsKey(player.getName());
     }
 
     /**
      * Check if the game has 0 players left.
      *
      * @return true if empty.
      */
     public boolean isEmpty() {
         return this.players.isEmpty();
     }
 
     public Arena getArena() {
         return arena;
     }
 
     public Map<String, TeamType> getPlayerMap() {
         return players;
     }
 
     public List<Player> getPlayers() {
         List<Player> ret = new ArrayList();
         for (String str : getPlayerMap().keySet()) {
             ret.add(Bukkit.getPlayerExact(str));
         }
         return ret;
     }
 
     public Map<String, ItemStack[]> getInventories() {
         return inventories;
     }
 
     public TeamType getTeam(Player player) {
         return this.players.get(player.getName());
     }
 
     public void broadcastMessage(String message) {
         for (Player player : this.getPlayers()) {
             player.sendMessage(message);
         }
     }
 
     public IronGolem getGolem() {
         return golem;
     }
 
     public void setGolem(IronGolem golem) {
         this.golem = golem;
     }
 
     @Override
     public String toString() {
         return String.format("%s{arena = %s}", getClass().getName(), getArena().getName());
     }
 
     public Map<Runnable, BukkitTask> getTasks() {
         return tasks;
     }
 }

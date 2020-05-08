 package me.limebyte.battlenight.api.battle;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.logging.Level;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.event.BattleDeathEvent;
 import me.limebyte.battlenight.api.util.PlayerData;
 import me.limebyte.battlenight.core.listeners.SignListener;
 import me.limebyte.battlenight.core.util.Messenger;
 import me.limebyte.battlenight.core.util.Messenger.Message;
 import me.limebyte.battlenight.core.util.Metadata;
 import me.limebyte.battlenight.core.util.SafeTeleporter;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public abstract class Battle {
 
     public BattleNightAPI api;
     public static final int INFINITE_LIVES = -1;
 
     private Arena arena;
     private boolean inProgress = false;
     private int minPlayers = 2;
     private int maxPlayers = Integer.MAX_VALUE;
     private int lives = INFINITE_LIVES;
 
     private Set<String> players = new HashSet<String>();
     private Set<String> spectators = new HashSet<String>();
 
     Battle() {
 
     }
 
     /* --------------- */
     /* General Methods */
     /* --------------- */
 
     public boolean start() {
         if (isInProgress()) return false;
         if (getPlayers().size() < getMinPlayers()) return false;
         if (getPlayers().size() > getMaxPlayers()) return false;
         if (getArena() == null || !getArena().isSetup(1) || !getArena().isEnabled()) return false;
         if (!onStart()) return false;
 
         Iterator<String> it = getPlayers().iterator();
         while (it.hasNext()) {
             Player player = toPlayer(it.next());
             if (player == null) {
                 it.remove();
                 continue;
             }
 
             Metadata.remove(player, "ready");
             Metadata.set(player, "lives", getLives());
             Metadata.set(player, "kills", 0);
             Metadata.set(player, "deaths", 0);
         }
 
         teleportAllToSpawn();
         SignListener.cleanSigns();
         inProgress = true;
         return true;
     }
 
     public boolean stop() {
         if (!isInProgress()) return false;
         if (!onStop()) return false;
 
         Iterator<String> pIt = getPlayers().iterator();
         while (pIt.hasNext()) {
             Player player = toPlayer(pIt.next());
             if (player == null) {
                 pIt.remove();
                 continue;
             }
 
             PlayerData.reset(player);
             PlayerData.restore(player, true, false);
             api.setPlayerClass(player, null);
             Metadata.remove(player, "lives");
             Metadata.remove(player, "kills");
             Metadata.remove(player, "deaths");
             pIt.remove();
         }
 
         Iterator<String> sIt = getSpectators().iterator();
         while (sIt.hasNext()) {
             Player player = toPlayer(sIt.next());
             if (player == null) {
                 sIt.remove();
                 continue;
             }
 
             PlayerData.reset(player);
             PlayerData.restore(player, true, false);
             api.setPlayerClass(player, null);
             Metadata.remove(player, "lives");
             Metadata.remove(player, "kills");
             Metadata.remove(player, "deaths");
             sIt.remove();
         }
 
         inProgress = false;
         return true;
     }
 
     /**
      * Adds the specified {@link Player} to the battle. This will return false
      * if it is unsuccessful.
      * 
      * @param player the Player to add
      * @return true if successful
      */
     public boolean addPlayer(Player player) {
         if (getArena() == null) {
             if (api.getArenas().isEmpty()) {
                 Messenger.tell(player, "No Arenas.");
                 return false;
             }
             setArena(api.getRandomArena());
         }
 
         if (!getArena().isSetup(1)) {
             Messenger.tell(player, "No Spawn Points.");
             return false;
         }
 
         if (!api.getLoungeWaypoint().isSet()) {
             Messenger.tell(player, Message.WAYPOINTS_UNSET);
             return false;
         }
 
         PlayerData.store(player);
         PlayerData.reset(player);
         getPlayers().add(player.getName());
         SafeTeleporter.tp(player, api.getLoungeWaypoint().getLocation());
         return true;
     }
 
     /**
      * Removes the specified {@link Player} to the battle. This will return
      * false if it is unsuccessful.
      * 
      * @param player the Player to remove
      * @return true if successful
      */
     public boolean removePlayer(Player player) {
         if (!containsPlayer(player)) return false;
         PlayerData.reset(player);
         PlayerData.restore(player, true, false);
         api.setPlayerClass(player, null);
         getPlayers().remove(player.getName());
         Metadata.remove(player, "lives");
         Metadata.remove(player, "kills");
         Metadata.remove(player, "deaths");
 
         if (shouldEnd()) stop();
         return true;
     }
 
     public boolean addSpectator(Player player) {
         PlayerData.store(player);
         PlayerData.reset(player);
         getSpectators().add(player.getName());
         player.setGameMode(GameMode.ADVENTURE);
         player.setAllowFlight(true);
         for (String n : getPlayers()) {
             if (Bukkit.getPlayerExact(n) != null) {
                 Bukkit.getPlayerExact(n).hidePlayer(player);
             }
         }
         SafeTeleporter.tp(player, Bukkit.getPlayerExact((String) getPlayers().toArray()[0]).getLocation());
         return true;
     }
 
     public boolean removeSpectator(Player player) {
         PlayerData.reset(player);
         PlayerData.restore(player, true, false);
         getSpectators().remove(player.getName());
         return true;
     }
 
     public void respawn(Player player) {
         if (!containsPlayer(player)) return;
         Messenger.debug(Level.INFO, "Respawning " + player.getName() + "...");
         PlayerData.reset(player);
         api.getPlayerClass(player).equip(player);
         SafeTeleporter.tp(player, getArena().getRandomSpawnPoint().getLocation());
     }
 
     public abstract Location toSpectator(Player player, boolean death);
 
     /* --------------- */
     /* Utility Methods */
     /* --------------- */
 
     private Player toPlayer(String name) {
         Player player = Bukkit.getPlayerExact(name);
         return player;
     }
 
     protected void teleportAllToSpawn() {
         List<Waypoint> waypoints = getArena().getSpawnPoints();
         List<Waypoint> free = waypoints;
         Random random = new Random();
 
         for (String name : getPlayers()) {
             Player player = Bukkit.getPlayerExact(name);
             if (player == null || !player.isOnline()) continue;
 
            if (free.isEmpty()) free = waypoints;
 
             int id = random.nextInt(free.size());
             SafeTeleporter.tp(player, free.get(id).getLocation());
             free.remove(id);
         }
     }
 
     public boolean shouldEnd() {
         return isInProgress() && getPlayers().size() < getMinPlayers();
     }
 
     /* ------------------- */
     /* Getters and Setters */
     /* ------------------- */
 
     /**
      * Returns the {@link Arena} that is set for this battle.
      * 
      * @return the arena
      * @see Arena
      */
     public Arena getArena() {
         return arena;
     }
 
     /**
      * Sets the {@link Arena} that will be used for this battle. The arena will
      * not be set if this battle is in progress.
      * 
      * @param arena the arena to set
      * @see Arena
      */
     public void setArena(Arena arena) {
         if (isInProgress()) return;
         this.arena = arena;
     }
 
     /**
      * Returns the minimum amount of players the battle requires before it can
      * be started.
      * 
      * @return the minPlayers
      */
     public int getMinPlayers() {
         return minPlayers;
     }
 
     /**
      * Sets the minimum amount of players the battle requires before it can be
      * started. This cannot be set below one.
      * 
      * @param minPlayers the minPlayers to set
      */
     public void setMinPlayers(int minPlayers) {
         if (getMinPlayers() < 1) return;
         this.minPlayers = minPlayers;
     }
 
     /**
      * Returns the maximum amount of players the battle can have. By default
      * this is set to {@link Integer.MAX_VALUE}.
      * 
      * @return the maxPlayers
      */
     public int getMaxPlayers() {
         return maxPlayers;
     }
 
     /**
      * Sets the maximum amount of players the battle can have. Setting this
      * value will prevent players from joining if the battle is full. This
      * cannot be set to a value that is less than the minimum.
      * 
      * @param maxPlayers the maxPlayers to set
      */
     public void setMaxPlayers(int maxPlayers) {
         if (maxPlayers < getMinPlayers()) return;
         this.maxPlayers = maxPlayers;
     }
 
     /**
      * @return the lives
      */
     public int getLives() {
         return lives;
     }
 
     /**
      * @param lives the lives to set
      */
     public void setLives(int lives) {
         this.lives = lives;
     }
 
     /**
      * @return the inProgress
      */
     public boolean isInProgress() {
         return inProgress;
     }
 
     /**
      * @return the players
      */
     public Set<String> getPlayers() {
         return players;
     }
 
     public boolean containsPlayer(Player player) {
         return getPlayers().contains(player.getName());
     }
 
     public List<String> getLeadingPlayers() {
         if (getPlayers().size() == 0) return null;
 
         List<String> leading = new ArrayList<String>();
         Iterator<String> it = getPlayers().iterator();
         while (it.hasNext()) {
             String name = it.next();
             Player player = toPlayer(name);
             if (player == null) {
                 it.remove();
                 continue;
             }
 
             if (leading.isEmpty()) {
                 leading.add(name);
                 continue;
             }
 
             int kills = Metadata.getInt(player, "kills");
             int leadingKills = Metadata.getInt(toPlayer(leading.get(0)), "kills");
 
             if (leadingKills == kills) {
                 leading.add(name);
                 continue;
             }
 
             if (leadingKills < kills) {
                 leading.clear();
                 leading.add(name);
                 continue;
             }
         }
 
         return leading;
     }
 
     public String getWinMessage() {
         String message;
         List<String> leading = getLeadingPlayers();
 
         if (leading.isEmpty() || leading.size() == players.size()) {
             message = "Draw!";
         } else if (leading.size() == 1) {
             message = leading.get(0) + " won the battle!";
         } else {
             message = leading.toString().replaceAll("\\[|\\]", "").replaceAll("[,]([^,]*)$", " and$1") + " won the battle!";
         }
 
         return message;
     }
 
     /**
      * @return the spectators
      */
     public Set<String> getSpectators() {
         return spectators;
     }
 
     public boolean containsSpectator(Player player) {
         return getSpectators().contains(player.getName());
     }
 
     /* ------ */
     /* Events */
     /* ------ */
 
     public abstract boolean onStart();
 
     public abstract boolean onStop();
 
     public void onPlayerDeath(BattleDeathEvent event) {
         Player player = event.getPlayer();
         Player killer = player.getKiller();
 
         if (killer != null) {
             Metadata.set(player, "kills", Metadata.getInt(killer, "kills") + 1);
             Messenger.tell(player, "You were killed by " + ChatColor.RED + killer.getName() + ChatColor.RESET + "!");
         } else {
             Messenger.tell(player, "You were killed!");
         }
 
         int deaths = Metadata.getInt(player, "deaths");
         int lives = Metadata.getInt(player, "lives");
 
         Metadata.set(player, "deaths", ++deaths);
         Metadata.set(player, "lives", --lives);
 
         if (lives > 0) {
             if (lives == 1) {
                 Messenger.tell(player, ChatColor.RED + "Last life!");
             } else {
                 Messenger.tell(player, "You have " + lives + " lives remaining.");
             }
             event.setCancelled(true);
         }
     }
 
 }

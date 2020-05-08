 package me.limebyte.battlenight.api.battle;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.util.PlayerData;
 import me.limebyte.battlenight.core.util.Messenger;
 import me.limebyte.battlenight.core.util.Messenger.Message;
 import me.limebyte.battlenight.core.util.SafeTeleporter;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 public abstract class Battle {
 
     public BattleNightAPI api;
     private Arena arena;
     private boolean inProgress = false;
 
     private Set<String> players = new HashSet<String>();
     private Set<String> spectators = new HashSet<String>();
 
     public boolean start() {
         if (isInProgress()) return false;
         if (getArena() == null) {
             if (api.getArenas().isEmpty()) return false;
             arena = api.getRandomArena();
         }
         inProgress = true;
         onStart();
         return true;
     }
 
     public boolean stop() {
         if (!isInProgress()) return false;
         inProgress = false;
         onEnd();
         return true;
     }
 
     public abstract void onStart();
 
     public abstract void onEnd();
 
     public boolean isInProgress() {
         return inProgress;
     }
 
     /**
      * Adds the specified {@link Player} to the battle. This will return false
      * if it is unsuccessful.
      * 
      * @param player the Player to add
      * @return true if successful
      */
     public boolean addPlayer(Player player) {
         if (!api.getLoungeWaypoint().isSet() || arena == null || !arena.isSetup(1)) {
             Messenger.tell(player, Message.WAYPOINTS_UNSET);
             return false;
         }
 
         PlayerData.store(player);
         PlayerData.reset(player);
         players.add(player.getName());
        SafeTeleporter.tp(player, api.getExitWaypoint().getLocation());
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
         players.remove(player.getName());
         return true;
     }
 
     public boolean containsPlayer(Player player) {
         return players.contains(player.getName());
     }
 
     public Set<String> getPlayers() {
         return players;
     }
 
     public boolean addSpectator(Player player) {
         spectators.add(player.getName());
         return true;
     }
 
     public boolean removeSpectator(Player player) {
         spectators.remove(player.getName());
         return true;
     }
 
     public boolean containsSpectator(Player player) {
         return spectators.contains(player.getName());
     }
 
     public Set<String> getSpectators() {
         return spectators;
     }
 
     public Player getLeadingPlayer() {
         return null;
     }
 
     public Arena getArena() {
         return arena;
     }
 
     public boolean setArena(Arena arena) {
         if (isInProgress()) return false;
         this.arena = arena;
         return true;
     }
 
     public void onPlayerDeath(PlayerDeathEvent event) {
         // TODO Auto-generated method stub
 
     }
 
     public void onPlayerRespawn(PlayerRespawnEvent event) {
         // TODO Auto-generated method stub
 
     }
 
 }

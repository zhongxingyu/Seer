 package me.limebyte.battlenight.core.battle;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.api.BattleEndEvent;
 import me.limebyte.battlenight.core.other.Tracks.Track;
 import me.limebyte.battlenight.core.util.Messaging;
 import me.limebyte.battlenight.core.util.Messaging.Message;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.kitteh.tag.TagAPI;
 
 public class Battle {
 
     private BattleNight plugin;
     private int redTeam = 0;
     private int blueTeam = 0;
     private boolean inProgress = false;
 
     public final Map<String, Team> usersTeam = new HashMap<String, Team>();
     public final Map<String, String> usersClass = new HashMap<String, String>();
     public final Set<String> spectators = new HashSet<String>();
 
     public Battle() {
         this.plugin = BattleNight.instance;
     }
 
     public void addPlayer(Player player) {
         if (plugin.preparePlayer(player)) {
             String name = player.getName();
             Team team;
 
             if (blueTeam > redTeam) {
                 team = Team.RED;
                 redTeam++;
                 BattleNight.goToWaypoint(player, Waypoint.RED_LOUNGE);
             } else {
                 team = Team.BLUE;
                 blueTeam++;
                 BattleNight.goToWaypoint(player, Waypoint.BLUE_LOUNGE);
             }
 
             usersTeam.put(name, team);
             Messaging.tell(player, "Welcome! You are on team " + team.getColour() + team.getName() + ChatColor.WHITE + ".");
            Messaging.tellEveryoneExcept(player, name + " has joined team" + team.getColour() + team.getName() + ChatColor.WHITE + ".");
 
             plugin.setNames(player);
             BattleNight.playersInLounge = true;
         } else {
             Messaging.tell(player, Track.MUST_HAVE_EMPTY.msg);
         }
     }
 
     public void removePlayer(Player player, boolean death, String msg1, String msg2) {
         final String name = player.getName();
 
         if (usersTeam.containsKey(name)) {
             final Team team = usersTeam.get(name);
             final boolean sendMsg1 = msg1 != null;
 
             if (team.equals(Team.RED)) {
                 redTeam--;
             } else if (team.equals(Team.BLUE)) {
                 blueTeam--;
             }
 
             if (sendMsg1) Messaging.tellEveryoneExcept(player, team.getColour() + name + ChatColor.WHITE + " " + msg1);
 
             if (msg2 != null) {
                 Messaging.tell(player, msg2);
             }
 
             // If red or blue won
             if (redTeam == 0 || blueTeam == 0) {
 
                 // If the battle started
                 if (!BattleNight.playersInLounge) {
                     // If red won
                     if (redTeam > 0) {
                         Messaging.tellEveryone(Message.TEAM_WON, Team.RED.getColour() + Team.RED.getName());
                         Bukkit.getServer().getPluginManager().callEvent(new BattleEndEvent("red", "blue", usersTeam));
                         // If blue won
                     } else if (blueTeam > 0) {
                         Messaging.tellEveryone(Message.TEAM_WON, Team.BLUE.getColour() + Team.BLUE.getName());
                         Bukkit.getServer().getPluginManager().callEvent(new BattleEndEvent("blue", "red", usersTeam));
                         // If neither team won
                     } else {
                         Messaging.tellEveryone(Message.DRAW);
                         Bukkit.getServer().getPluginManager().callEvent(new BattleEndEvent("draw", "draw", null));
                     }
                 }
 
                 for (String currentName : usersTeam.keySet()) {
                     if (Bukkit.getPlayer(currentName) != null) {
                         Player currentPlayer = Bukkit.getPlayer(currentName);
                         if (!(death && currentPlayer == player)) {
                             resetPlayer(currentPlayer, true, false);
                         }
                     }
                 }
 
                 resetBattle();
             }
 
             if (!death) resetPlayer(player, true, true);
         } else {
             BattleNight.log.warning("Failed to remove player '" + name + "' from the Battle as they are not in it.");
         }
     }
 
     public void resetPlayer(Player player, boolean teleport, boolean removeHash) {
         player.getInventory().clear();
         plugin.restorePlayer(player);
         if (teleport) BattleNight.goToWaypoint(player, Waypoint.EXIT);
         plugin.cleanSigns(player);
 
         if (removeHash) {
             usersTeam.remove(player.getName());
             usersClass.remove(player.getName());
             try {
                 TagAPI.refreshPlayer(player);
             } catch (final Exception e) {
             }
         }
     }
 
     private void resetBattle() {
         Set<String> toRefresh = usersTeam.keySet();
 
         plugin.removeAllSpectators();
         plugin.cleanSigns();
         plugin.BattleSigns.clear();
         inProgress = false;
         plugin.redTeamIronClicked = false;
         plugin.blueTeamIronClicked = false;
         usersTeam.clear();
         usersClass.clear();
         redTeam = 0;
         blueTeam = 0;
 
         for (String name : toRefresh) {
             if (Bukkit.getPlayer(name) != null) {
                 try {
                     TagAPI.refreshPlayer(Bukkit.getPlayer(name));
                 } catch (final Exception e) {
                 }
             }
         }
     }
 
     public void stop() {
         if (blueTeam > redTeam) {
             Messaging.tellEveryone(Message.TEAM_WON, Team.BLUE.getColour() + Team.BLUE.getName());
             Bukkit.getServer().getPluginManager().callEvent(new BattleEndEvent("blue", "red", usersTeam));
         } else if (redTeam > blueTeam) {
             Messaging.tellEveryone(Message.TEAM_WON, Team.RED.getColour() + Team.RED.getName());
             Bukkit.getServer().getPluginManager().callEvent(new BattleEndEvent("red", "blue", usersTeam));
         } else {
             Messaging.tellEveryone(Message.DRAW);
             Bukkit.getServer().getPluginManager().callEvent(new BattleEndEvent("draw", "draw", null));
         }
 
         for (String currentName : usersTeam.keySet()) {
             if (Bukkit.getPlayer(currentName) != null) {
                 Player currentPlayer = Bukkit.getPlayer(currentName);
                 resetPlayer(currentPlayer, true, false);
             }
         }
 
         resetBattle();
 
         plugin.removeAllSpectators();
     }
 
     public boolean isInProgress() {
         return inProgress;
     }
 
     public void start() {
         inProgress = true;
         Messaging.tellEveryone(Message.BATTLE_STARTED);
         plugin.teleportAllToSpawn();
     }
 }

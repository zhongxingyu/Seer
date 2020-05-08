 package me.limebyte.battlenight.core;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import me.limebyte.battlenight.core.API.BattleEndEvent;
 import me.limebyte.battlenight.core.Other.Tracks.Track;
 import me.limebyte.battlenight.core.Other.Waypoint;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.kitteh.tag.TagAPI;
 
 public class Battle {
 
     BattleNight plugin;
     int redTeam = 0;
     int blueTeam = 0;
 
     public final Map<String, Team> usersTeam = new HashMap<String, Team>();
     public final Map<String, String> usersClass = new HashMap<String, String>();
     public final Set<String> spectators = new HashSet<String>();
 
     public Battle() {
         this.plugin = BattleNight.instance;
     }
 
     public void addPlayer(Player player) {
         if (plugin.preparePlayer(player)) {
             String name = player.getName();
 
             if (blueTeam > redTeam) {
                 BattleNight.goToWaypoint(player, Waypoint.RED_LOUNGE);
                 usersTeam.put(name, Team.RED);
                 BattleNight.tellPlayer(player, "Welcome! You are on team " + ChatColor.RED + "<Red>");
                plugin.tellEveryoneExcept(player, name + " has joined team " + ChatColor.RED + "<Red>");
                 redTeam++;
             } else {
                 BattleNight.goToWaypoint(player, Waypoint.BLUE_LOUNGE);
                 usersTeam.put(name, Team.BLUE);
                 BattleNight.tellPlayer(player, "Welcome! You are on team " + ChatColor.BLUE + "<Blue>");
                plugin.tellEveryoneExcept(player, name + " has joined team " + ChatColor.BLUE + "<Blue>");
                 blueTeam++;
             }
 
             plugin.setNames(player);
             plugin.playersInLounge = true;
         } else {
             BattleNight.tellPlayer(player, Track.MUST_HAVE_EMPTY);
         }
     }
 
     public void removePlayer(Player player, boolean death, String msg1, String msg2) {
         final String name = player.getName();
 
         if (usersTeam.containsKey(name)) {
             final Team team = usersTeam.get(name);
             final boolean sendMsg1 = msg1 != null;
 
             if (team.equals(Team.RED)) {
                 redTeam--;
                if (sendMsg1) plugin.tellEveryoneExcept(player, ChatColor.RED + name + ChatColor.WHITE + " " + msg1);
             }
             if (team.equals(Team.BLUE)) {
                 blueTeam--;
                if (sendMsg1) plugin.tellEveryoneExcept(player, ChatColor.BLUE + name + ChatColor.WHITE + " " + msg1);
             }
 
             if (msg2 != null) {
                 BattleNight.tellPlayer(player, msg2);
             }
 
             // If red or blue won
             if (redTeam == 0 || blueTeam == 0) {
 
                 // If the battle started
                 if (!plugin.playersInLounge) {
                     // If red won
                     if (redTeam > 0) {
                         plugin.tellEveryone(Track.RED_WON);
                         Bukkit.getServer().getPluginManager().callEvent(new BattleEndEvent("red", "blue", usersTeam));
                         // If blue won
                     } else if (blueTeam > 0) {
                         plugin.tellEveryone(Track.BLUE_WON);
                         Bukkit.getServer().getPluginManager().callEvent(new BattleEndEvent("blue", "red", usersTeam));
                         // If neither team won
                     } else {
                         plugin.tellEveryone(Track.DRAW);
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
         BattleNight.battleInProgress = false;
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
 
     public void end() {
         if (blueTeam > redTeam) {
             plugin.tellEveryone(Track.BLUE_WON);
             Bukkit.getServer().getPluginManager().callEvent(new BattleEndEvent("blue", "red", usersTeam));
         } else if (redTeam > blueTeam) {
             plugin.tellEveryone(Track.RED_WON);
             Bukkit.getServer().getPluginManager().callEvent(new BattleEndEvent("red", "blue", usersTeam));
         } else {
             plugin.tellEveryone(Track.DRAW);
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
 }

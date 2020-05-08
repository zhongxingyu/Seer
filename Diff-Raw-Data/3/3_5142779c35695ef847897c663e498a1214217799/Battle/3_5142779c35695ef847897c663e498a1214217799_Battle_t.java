 package me.limebyte.battlenight.core.battle;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.PlayerData;
 import me.limebyte.battlenight.core.SimpleUtil;
 import me.limebyte.battlenight.core.listeners.SignListener;
 import me.limebyte.battlenight.core.util.Metadata;
 import me.limebyte.battlenight.core.util.SafeTeleporter;
 import me.limebyte.battlenight.core.util.chat.Messaging;
 import me.limebyte.battlenight.core.util.chat.Messaging.Message;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.kitteh.tag.TagAPI;
 
 public class Battle {
 
     private int redTeam = 0;
     private int blueTeam = 0;
     private boolean inLounge = false;
     private boolean inProgress = false;
     private boolean ending = false;
 
     public final Map<String, Team> usersTeam = new HashMap<String, Team>();
     public final Set<String> spectators = new HashSet<String>();
 
     public void addPlayer(Player player) {
         if (BattleNight.preparePlayer(player)) {
             String name = player.getName();
             Team team;
 
             if (blueTeam > redTeam) {
                 team = Team.RED;
                 redTeam++;
                 SafeTeleporter.tp(player, Waypoint.RED_LOUNGE);
             } else {
                 team = Team.BLUE;
                 blueTeam++;
                 SafeTeleporter.tp(player, Waypoint.BLUE_LOUNGE);
             }
 
             usersTeam.put(name, team);
             Messaging.tell(player, Message.JOINED_TEAMED_BATTLE, team);
             Messaging.tellEveryoneExcept(player, true, Message.PLAYER_JOINED_TEAMED_BATTLE, player, team);
 
             BattleNight.setNames(player);
             inLounge = true;
         } else {
             Messaging.tell(player, Message.INVENTORY_NOT_EMPTY);
         }
     }
 
     public void removePlayer(Player player, boolean death, String msg1, String msg2) {
         String name = player.getName();
 
         if (usersTeam.containsKey(name)) {
             Team team = usersTeam.get(name);
             boolean sendMsg1 = msg1 != null;
 
             if (team.equals(Team.RED)) {
                 redTeam--;
             } else if (team.equals(Team.BLUE)) {
                 blueTeam--;
             }
 
             if (sendMsg1) Messaging.tellEveryoneExcept(player, team.getColour() + name + ChatColor.WHITE + " " + msg1, true);
 
             if (msg2 != null) {
                 Messaging.tell(player, msg2);
             }
 
             // If red or blue won
             if (redTeam == 0 || blueTeam == 0) {
 
                 ending = true;
 
                 // If the battle started
                 if (!inLounge) {
                     // If red won
                     if (redTeam > 0) {
                         Messaging.tellEveryone(true, Message.TEAM_WON, Team.RED.getColour() + Team.RED.getName());
                         // If blue won
                     } else if (blueTeam > 0) {
                         Messaging.tellEveryone(true, Message.TEAM_WON, Team.BLUE.getColour() + Team.BLUE.getName());
                         // If neither team won
                     } else {
                         Messaging.tellEveryone(true, Message.DRAW);
                     }
                 }
 
                 Iterator<String> it = usersTeam.keySet().iterator();
                 while (it.hasNext()) {
                     String currentName = it.next();
                     if (Bukkit.getPlayerExact(currentName) != null) {
                         Player currentPlayer = Bukkit.getPlayerExact(currentName);
                         if (currentPlayer != player) {
                             resetPlayer(currentPlayer, true, it, false);
                         }
                     }
                 }
 
                 resetBattle();
             }
 
             if (!death) resetPlayer(player, true, null, false);
         } else {
             Messaging.log(Level.WARNING, "Failed to remove player '" + name + "' from the Battle as they are not in it.");
         }
     }
 
     public void resetPlayer(Player player, boolean teleport, Iterator<String> it, boolean keepData) {
         SimpleUtil.reset(player);
         PlayerData.restore(player, teleport, keepData);
         SignListener.cleanSigns(player);
         Metadata.remove(player, "class");
 
         if (it != null) {
             it.remove();
         } else {
             usersTeam.remove(player.getName());
         }
 
         try {
             TagAPI.refreshPlayer(player);
         } catch (Exception e) {
         }
     }
 
     private void resetBattle() {
         removeAllSpectators();
         SignListener.cleanSigns();
         inProgress = false;
         inLounge = false;
         ending = false;
         BattleNight.redTeamIronClicked = false;
         BattleNight.blueTeamIronClicked = false;
         spectators.clear();
         usersTeam.clear();
         redTeam = 0;
         blueTeam = 0;
     }
 
     public void start() {
         inProgress = true;
         inLounge = false;
         Messaging.tellEveryone(true, Message.BATTLE_STARTED);
         BattleNight.teleportAllToSpawn();
         SignListener.cleanSigns();
     }
 
     public void stop() {
         if (!inLounge) {
             if (blueTeam > redTeam) {
                 Messaging.tellEveryone(true, Message.TEAM_WON, Team.BLUE.getColour() + Team.BLUE.getName());
             } else if (redTeam > blueTeam) {
                 Messaging.tellEveryone(true, Message.TEAM_WON, Team.RED.getColour() + Team.RED.getName());
             } else {
                 Messaging.tellEveryone(true, Message.DRAW);
             }
         }
 
         Iterator<String> it = usersTeam.keySet().iterator();
         while (it.hasNext()) {
             String currentName = it.next();
             if (Bukkit.getPlayerExact(currentName) != null) {
                 Player currentPlayer = Bukkit.getPlayerExact(currentName);
                 resetPlayer(currentPlayer, true, it, false);
             }
         }
 
         resetBattle();
     }
 
     public boolean isInLounge() {
         return inLounge;
     }
 
     public boolean isInProgress() {
         return inProgress;
     }
 
     public boolean isEnding() {
         return ending;
     }
 
     public void addSpectator(Player player, String type) {
         if (type.equals("death")) {
             Messaging.tell(player, Message.WELCOME_SPECTATOR_DEATH);
         } else {
             SafeTeleporter.tp(player, Waypoint.SPECTATOR);
             Messaging.tell(player, Message.WELCOME_SPECTATOR);
         }
 
         if (!PlayerData.storageContains(player)) {
             PlayerData.store(player);
         }
 
        SimpleUtil.reset(player);
         player.setAllowFlight(true);
 
         for (String n : usersTeam.keySet()) {
             if (Bukkit.getPlayerExact(n) != null) {
                 Bukkit.getPlayerExact(n).hidePlayer(player);
             }
         }
 
         spectators.add(player.getName());
     }
 
     public void removeSpectator(Player player, Iterator<String> it) {
         SimpleUtil.reset(player);
         PlayerData.restore(player, true, false);
         Messaging.tell(player, Message.GOODBYE_SPECTATOR);
 
         if (it != null) {
             it.remove();
         } else {
             spectators.remove(player.getName());
         }
     }
 
     public void removeAllSpectators() {
         Iterator<String> it = spectators.iterator();
         while (it.hasNext()) {
             String currentName = it.next();
             if (Bukkit.getPlayerExact(currentName) != null) {
                 Player currentPlayer = Bukkit.getPlayerExact(currentName);
                 removeSpectator(currentPlayer, it);
             }
         }
     }
 }

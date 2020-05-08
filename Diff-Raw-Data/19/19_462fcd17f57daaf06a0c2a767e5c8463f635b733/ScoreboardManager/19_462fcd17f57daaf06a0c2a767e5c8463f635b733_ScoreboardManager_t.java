 package com.hotmail.shinyclef.shinybridge;
 
 import com.comphenix.protocol.Packets;
 import com.comphenix.protocol.ProtocolManager;
 import com.comphenix.protocol.events.PacketContainer;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.Team;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.nio.charset.Charset;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.util.*;
 
 /**
  * User: Shinyclef
  * Date: 19/09/13
  * Time: 2:58 AM
  */
 
 public class ScoreboardManager
 {
     private static boolean scoreboardEnabled = true;
     private static ShinyBridge p;
     private static Server s;
     private static ProtocolManager protocolManager;
     private static Scoreboard scoreboard;
     private static File teamsFile;
     private static Charset encoding = StandardCharsets.UTF_8;
 
     private static Map<String, PacketContainer> onlineTeamAndPacketMap;
     //(String fullName, PacketContainer onlinePacket)
 
     public static void initialise(ShinyBridge plugin, ProtocolManager protocolManager,
                                   File teamsFile, boolean scoreboardEnabled)
     {
         p = plugin;
         s = plugin.getServer();
         ScoreboardManager.protocolManager = protocolManager;
         onlineTeamAndPacketMap = new LinkedHashMap<>();
         ScoreboardManager.scoreboardEnabled = scoreboardEnabled;
         ScoreboardManager.teamsFile = teamsFile;
         scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
         removeTeamsOnTeamsFile();
     }
 
     public static void processServerPlayerJoin(Player player)
     {
         //ensure any potential scoreboard presence is removed
         removeFromScoreboard(player.getName());
 
         //send r+ scoreboard online packets to player, delay to make sure they are added to end of list
         sendScoreboardListToNewPlayer(player);
     }
 
     public static void processServerPlayerQuit(Player player)
     {
         //check if play is logged into r+. If so, add to scoreboard.
         if (Account.getLoggedInClientUsernamesSet().contains(player.getName()))
         {
             addToScoreboard(player.getName());
         }
     }
 
     public static void addToScoreboard(String playerName)
     {
         if (!scoreboardEnabled)
         {
            if (ShinyBridge.DEV_BUILD)
            {
                MCServer.pluginLog("Add to scoreboard triggered while scoreboard not enabled.");
            }
             return;
         }
 
         //make sure player is not online on server to prevent duplicate names
         if (s.getOfflinePlayer(playerName).isOnline())
         {
             if (ShinyBridge.DEV_BUILD)
             {
                 MCServer.pluginLog("Add to scoreboard triggered while player is online.");
             }
             return;
         }
 
        if (ShinyBridge.DEV_BUILD)
        {
            MCServer.pluginLog("Add to scoreboard running with name: " + playerName);
        }

         //split first character off the name for prefix
         String firstLetter = playerName.substring(0, 1);
         String nameRemainder = playerName.substring(1);
         String rankColour = MCServer.getRankColour(Account.getAccountMap().get(playerName).getRank());
 
         //create prefix consisting of rankColour and firstLetter, and r+ suffix
         String prefix = rankColour + ChatColor.ITALIC + firstLetter;
         String suffix = (ChatColor.GRAY + "" + ChatColor.ITALIC + "(r+)");
 
         //prepare the packet and the team
         PacketContainer packet = createScoreboardAddPacket(nameRemainder);
         Team team = scoreboard.getTeam(nameRemainder);
         if (team == null)
         {
             team = scoreboard.registerNewTeam(nameRemainder);
         }
         team.setPrefix(prefix);
         team.setSuffix(suffix);
 
         //add player to the team, then send the packet to each online player
         team.addPlayer(s.getOfflinePlayer(nameRemainder));
         for (Player player : s.getOnlinePlayers())
         {
             sendPacketToPlayer(player.getName(), packet);
         }
 
         //add the fake player to the onlineTeamAndPacketMap
         onlineTeamAndPacketMap.put(playerName, packet);
 
         //update teams.txt
         updateTeamsListFile();
     }
 
     public static void removeFromScoreboard(String playerName)
     {
         if (!scoreboardEnabled)
         {
             return;
         }
 
         //check if player is on scoreboard
         if (!onlineTeamAndPacketMap.containsKey(playerName))
         {
             return;
         }
 
         //get the nameRemainder that's registered with scoreboard
         String nameRemainder = playerName.substring(1);
 
         //get the packet
         PacketContainer packet = createScoreboardRemovePacket(nameRemainder);
 
         //send the packet to all online users
         for (Player player : s.getOnlinePlayers())
         {
             sendPacketToPlayer(player.getName(), packet);
         }
 
         //remove this player from the onlineTeamAndPacketMap
         onlineTeamAndPacketMap.remove(playerName);
 
         //remove the team from scoreboard
         scoreboard.getTeam(nameRemainder).unregister();
 
         //update teams.txt
         updateTeamsListFile();
     }
 
     public static void sendScoreboardListToNewPlayer(final Player player)
     {
         if (!scoreboardEnabled)
         {
             return;
         }
 
         Bukkit.getScheduler().scheduleSyncDelayedTask(p, new Runnable() {
             @Override
             public void run()
             {
                 for (PacketContainer packet : onlineTeamAndPacketMap.values())
                 {
 
                     sendPacketToPlayer(player.getName(), packet);
                 }
             }
         }, 4);
 
 
     }
 
     private static PacketContainer createScoreboardAddPacket(String packetPlayerName)
     {
         PacketContainer playerInfo = protocolManager.createPacket(Packets.Server.PLAYER_INFO);
         playerInfo.getStrings().write(0, packetPlayerName);
         playerInfo.getBooleans().write(0, true);
         playerInfo.getIntegers().write(0, 0);
         return playerInfo;
     }
 
     private static PacketContainer createScoreboardRemovePacket(String packetPlayerName)
     {
         PacketContainer playerInfo = protocolManager.createPacket(Packets.Server.PLAYER_INFO);
         playerInfo.getStrings().write(0, packetPlayerName);
         playerInfo.getBooleans().write(0, false);
         playerInfo.getIntegers().write(0, 9999);
         return playerInfo;
     }
 
     private static void sendPacketToPlayer(String recipientPlayerName, PacketContainer packetContainer)
     {
         try
         {
             protocolManager.sendServerPacket(s.getPlayer(recipientPlayerName), packetContainer);
         }
         catch (InvocationTargetException e)
         {
             MCServer.pluginLog("WARNING! Could not send scoreboard packet to player in MCServer.addToScoreboard: " +
                     e.getMessage());
         }
     }
 
     private static void removeTeamsOnTeamsFile()
     {
         //get teamsList from file
         List<String> teamsList;
         try
         {
             teamsList = Files.readAllLines(Paths.get(teamsFile.getPath()), encoding);
         }
         catch (IOException e)
         {
             MCServer.pluginLog("WARNING! IOException in ScoreboardManager.removeTeamsOnTeamsFile. " +
                     "Scoreboard functionality disabled! Exception: " + e.getMessage());
             ScoreboardManager.scoreboardEnabled = false;
             return;
         }
 
         //remove all scoreboard teams on the list if they exist
         for (String teamName : teamsList)
         {
             Team team = scoreboard.getTeam(teamName);
             if (team != null)
             {
                 scoreboard.getTeam(teamName).unregister();
             }
         }
 
         //clear file contents
         updateTeamsListFile();
     }
 
     private static void updateTeamsListFile()
     {
         List<String> teamsList = new ArrayList<>();
         for (String name : onlineTeamAndPacketMap.keySet())
         {
             teamsList.add(name.substring(1));
         }
 
         try
         {
             Files.write(Paths.get(teamsFile.getPath()), teamsList, encoding);
         }
         catch (IOException e)
         {
             MCServer.pluginLog("WARNING! IOException in ScoreboardManager.updateTeamsListFile: " + e.getMessage());
         }
     }
 
     public static void scoreboardDebug(CommandSender sender)
     {
         String teamsString = "";
         Set<Team> teams = scoreboard.getTeams();
         for (Team team : teams)
         {
             teamsString = teamsString + team.getName() + ", ";
         }
 
         //remove last ", "
         teamsString = teamsString.substring(0, teamsString.length() - 2);
 
         sender.sendMessage(ChatColor.AQUA + "Active Scoreboard Teams: " + ChatColor.YELLOW + teamsString);
     }
 }

 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.stutiguias.tasks;
 
 import java.util.Map;
 import me.stutiguias.mcpk.MCPlayer;
 import me.stutiguias.mcpk.Mcpk;
 import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Score;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.ScoreboardManager;
 
 /**
  *
  * @author Stutiguias
  */
 public class AlertPkTask implements Runnable {
    
     private final Mcpk plugin;
 
     public AlertPkTask(Mcpk plugin) {
             this.plugin = plugin;
     }
     
     @Override
     public void run() {
       try{
             Player[] playerList = plugin.getServer().getOnlinePlayers();
             if(plugin.getServer().getOnlinePlayers().length == 0) return;
             for (Map.Entry m : plugin.MCPlayers.entrySet()) {
                 MCPlayer McpkPlayer =(MCPlayer)m.getValue();
                 Player pkPlayer = plugin.getServer().getPlayer((String)m.getKey());
                 
                 if(pkPlayer == null || !McpkPlayer.getIsPK()) continue;
                 
                 if(plugin.AlertMsg) WarningPlayer(pkPlayer, playerList);
                 
                 Integer timeleft = Integer.parseInt(String.valueOf(plugin.MCPlayers.get(pkPlayer.getName()).getPKTime() - plugin.GetCurrentMilli()));
                 
                 if(McpkPlayer.getIsPK() && McpkPlayer.getPKMsg()) {
                     timeleft = timeleft / 1000;
                     if(plugin.UseScoreBoard){
                         WarningPK(pkPlayer, timeleft);
                     }else{
                         pkPlayer.sendMessage(plugin.translate.TimeLeftOnPK + " " + timeleft);
                     }
                 }
 
                 if(plugin.GetCurrentMilli() > McpkPlayer.getPKTime()) {
                     String[] playersgroups = McpkPlayer.getPkOldGroups();
                     if(playersgroups != null && plugin.RemoveAllOtherGroup) {
                         for (int i = 0; i < playersgroups.length; i++) {
                              plugin.permission.playerAddGroup(pkPlayer, playersgroups[i]);
                         }
                     }
                     plugin.permission.playerRemoveGroup(pkPlayer, plugin.GroupPk);
                     plugin.MCPlayers.get(pkPlayer.getName()).setIsPK(Boolean.FALSE);
                     plugin.MCPlayers.get(pkPlayer.getName()).setKills(0);
                     plugin.DB.UpdateKill(pkPlayer, 0);
                     ScoreboardManager manager = Bukkit.getScoreboardManager();
                     pkPlayer.setScoreboard(manager.getNewScoreboard());
                 }
             }
       } catch (IllegalArgumentException | IllegalStateException ex) {
          ex.printStackTrace();
       }        
 
     }
     
     public void WarningPK(Player player,int timeLeft) {
         ScoreboardManager manager = Bukkit.getScoreboardManager();
         Scoreboard board = manager.getNewScoreboard();
         Objective objective = board.registerNewObjective("mcpk","dummy");
         objective.setDisplaySlot(DisplaySlot.SIDEBAR);
         objective.setDisplayName(plugin.translate.TimeLeftOnPK);
 
         Score score = objective.getScore(Bukkit.getOfflinePlayer(player.getName()));
         score.setScore(timeLeft); 
 
         player.setScoreboard(board);
     }
     
     
     public void WarningPlayer(Player pkPlayer,Player[] playerList) {
         Double PkPlayerX = pkPlayer.getLocation().getX();
         Double PkPlayerZ = pkPlayer.getLocation().getZ();
         for (Player player : playerList) {
                 Double playerX = player.getLocation().getX();
                 Double playerZ = player.getLocation().getZ();
                 if ((playerX < PkPlayerX + (double)plugin.radius) &&
                     (playerX > PkPlayerX - (double)plugin.radius) &&
                     (playerZ < PkPlayerZ + (double)plugin.radius) &&
                     (playerZ > PkPlayerZ - (double)plugin.radius) &&
                      plugin.MCPlayers.get(player.getName()).getAlertMsg() && (player.getName().equals(pkPlayer.getName()) == false) 
                 ) {
                     player.sendMessage(plugin.parseColor(plugin.translate.AlertMsg.replace("%player%", pkPlayer.getName())));
                 }
         }
     }
     
 }

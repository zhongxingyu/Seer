 package com.github.pabook.commandkepper;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 public class getCommand implements Listener {
    CommandKepper cmdKepper;
 
     public getCommand(CommandKepper cmdkepper){
         this.cmdKepper = cmdkepper;
     }
 
         @EventHandler
         public void getPlayerCommand(PlayerCommandPreprocessEvent event){
             Player senderPlayer = event.getPlayer();
             String getCmd = event.getMessage();
 
             if(senderPlayer.hasPermission("commandkepper.bypass") || senderPlayer.isOp()){
                     return;
             }
 
             for(int i = 0 ; i < cmdKepper.kickCmd.size() ; i++){
                     if(cmdKepper.kickCmd.get(i).equalsIgnoreCase(getCmd)){
                         Player[] onlinePlayers = cmdKepper.getServer().getOnlinePlayers();
                         for(int ii = 0 ; ii < onlinePlayers.length ; ii++){
                            if(onlinePlayers[ii].hasPermission("commandkepper.staff") || onlinePlayers[i].isOp()){
                                 onlinePlayers[ii].sendMessage("[CommandKepper]" + cmdKepper.noticeStaffMsg.replaceAll("%Player%", ChatColor.GREEN + senderPlayer.getName() + ChatColor.WHITE).replaceAll("%Command%", ChatColor.GRAY + getCmd + ChatColor.WHITE));
                             }
                         }
                        cmdKepper.getServer().broadcastMessage("[CommandKepper]" + cmdKepper.noticeStaffMsg.replaceAll("%Player%", ChatColor.GREEN + senderPlayer.getName() + ChatColor.WHITE).replaceAll("%Command%", ChatColor.GRAY + getCmd + ChatColor.WHITE));
                         senderPlayer.kickPlayer(cmdKepper.kickMsg);
                     }
             }
         }
 }
 

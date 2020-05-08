 package com.github.pabook.commandkepper;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 public class getCommand implements Listener {
    CommandKepper cmdKepper = null;
 
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
            getCmd = getCmd.substring(1,getCmd.length());

            if(getCmd.substring(0,1).equals("/")){
                getCmd.substring(1,getCmd.length());
            }
 
             for(int i = 0 ; i < cmdKepper.kickCmd.size() ; i++){
                     if(cmdKepper.kickCmd.get(i).equalsIgnoreCase(getCmd)){
                         Player[] onlinePlayers = cmdKepper.getServer().getOnlinePlayers();
                         for(int ii = 0 ; ii < onlinePlayers.length ; ii++){
                            if(onlinePlayers[ii].hasPermission("commandkepper.staff") || onlinePlayers[ii].isOp()){
                                 onlinePlayers[ii].sendMessage("[CommandKepper]" + cmdKepper.noticeStaffMsg.replaceAll("%Player%", ChatColor.GREEN + senderPlayer.getName() + ChatColor.WHITE).replaceAll("%Command%", ChatColor.GRAY + getCmd + ChatColor.WHITE));
                             }
                         }
                        cmdKepper.getServer().broadcastMessage("[CommandKepper]" + cmdKepper.noticeMsg.replaceAll("%Player%", ChatColor.GREEN + senderPlayer.getName() + ChatColor.WHITE).replaceAll("%Command%", ChatColor.GRAY + getCmd + ChatColor.WHITE));
                         senderPlayer.kickPlayer(cmdKepper.kickMsg);
                     }
             }
         }
 }
 

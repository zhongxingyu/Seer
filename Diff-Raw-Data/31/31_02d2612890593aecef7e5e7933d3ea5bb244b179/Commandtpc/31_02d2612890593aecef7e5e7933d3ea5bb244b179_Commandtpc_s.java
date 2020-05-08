 package com.ementalo.tcl.Commands;
 
 import com.ementalo.tcl.*;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: devhome
  * Date: 20/12/11
  * Time: 15:49
  * To change this template use File | Settings | File Templates.
  */
 public class Commandtpc implements ITclCommand {
 
     public void execute(Player player, Command command, String commandLabel, String[] args, TeleConfirmLite parent) {
         TpAction req = null;
         Player other = null;
 
         if (args.length != 1) {
             help(player, commandLabel);
             return;
         } else {
             List<Player> targets = parent.getServer().matchPlayer(args[0]);
             if (targets.size() >= 1) {
                 other = targets.get(0);
             } else {
                 player.sendMessage(Config.playerNotFound.replace("%p", args[0]));
                 return;
             }
         }
         if (!Config.isDebug) {
             if (other.getName().equalsIgnoreCase((player.getDisplayName()))) {
                 player.sendMessage(Config.selfTp);
                 return;
             }
         }
         if (parent.tclUserHandler.playerHasPendingRequest(other)) {
             player.sendMessage(Config.playerHasPendingReq);
             return;
         }
 
         if (parent.tclUserHandler.hasToggled(other)) {
             player.sendMessage(Config.toggledMsg.replace("%p",other.getDisplayName()));
             return;
         }
 
         req = new TpAction(player.getDisplayName(), other.getDisplayName(), player.getDisplayName(), TpAction.Actions.TELEPORT_TO_PLAYER, parent);
         parent.tclUserHandler.processRequest(player, req);
         return;
     }
 
     public void execute(CommandSender sender, Command command, String commandLabel, String[] args, TeleConfirmLite parent) {
 
         sender.sendMessage(Config.playerCommand);
     }
 
     public void help(CommandSender sender, String commandLabel) {
 
         sender.sendMessage(ChatColor.RED + "Usage: /" + commandLabel + " playername");
     }
 }

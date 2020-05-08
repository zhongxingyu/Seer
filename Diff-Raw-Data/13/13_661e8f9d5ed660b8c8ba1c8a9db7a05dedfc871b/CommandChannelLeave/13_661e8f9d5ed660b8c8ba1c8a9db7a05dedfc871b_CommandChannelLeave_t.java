 package main.java.net.simplicite_mc.roblikescake.IdealChat.Commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class CommandChannelLeave implements CommandExecutor {
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (CommandChannelJoin.channelHandler.removePlayer(sender)) {
             sender.sendMessage(ChatColor.GRAY + "You have left the channel!");
         } else {
             sender.sendMessage(ChatColor.GRAY + "You are not in this channel!");
         }
         return true;
     }
 }

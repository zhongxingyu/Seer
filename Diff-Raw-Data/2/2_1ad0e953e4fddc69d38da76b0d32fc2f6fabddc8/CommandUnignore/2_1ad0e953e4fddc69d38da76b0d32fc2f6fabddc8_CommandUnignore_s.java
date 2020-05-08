 package com.minecarts.barrenschat.command;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.util.List;
 
 import com.minecarts.barrenschat.BarrensChat;
 import com.minecarts.barrenschat.ChatChannel;
 import com.minecarts.barrenschat.event.ChatChannelJoinEvent;
 import com.minecarts.barrenschat.helpers.StringHelper;
 
 
 public class CommandUnignore extends CommandHandler{
     
     public CommandUnignore (BarrensChat plugin){
         super(plugin);
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if(sender instanceof Player){
             Player player = (Player) sender;
             List<String> ignoreList = plugin.dbHelper.getIgnoreList(player);
             List<Player> ignoreMatches = server.matchPlayer(args[0]);
             boolean didUnignore = false;
             if (ignoreMatches.size() > 0) {
               for (Player unignore : ignoreMatches) {
                if (ignoreList.contains(unignore.toString())) {
                   player.sendMessage("You have unignored " + unignore.getName() + ".");
                   plugin.dbHelper.removeIgnore(player, unignore);
                   didUnignore = true;
                 }
               }
               if (didUnignore) return true;
               player.sendMessage("You are not ignoring anyone by that name.");
             } else {
               player.sendMessage("Could not find that player to unignore. They must be online.");
             }
             return true;
         }
         return false;
     }
 }

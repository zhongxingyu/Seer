 package me.limebyte.endercraftessentials;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class FormationCommand implements CommandExecutor {
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage("This command can only be run by a player.");
         } else {
             Player player = (Player) sender;
             if (args != null && args[0] != null) {
                 try {
                     int radius = Integer.parseInt(args[0]);

                    if (radius > 100) {
                        sender.sendMessage("Calm down mate!  Not too big!");
                        return true;
                    }

                     WitherFormationFinder.searchArea(player, radius);
                 } catch (NumberFormatException e) {
                     sender.sendMessage("Invalid radius.");
                     sender.sendMessage("Usage: " + cmd.getUsage());
                 }
             } else {
                 sender.sendMessage("Please specify a radius.");
                 sender.sendMessage("Usage: " + cmd.getUsage());
             }
         }
         return true;
     }
 
 }

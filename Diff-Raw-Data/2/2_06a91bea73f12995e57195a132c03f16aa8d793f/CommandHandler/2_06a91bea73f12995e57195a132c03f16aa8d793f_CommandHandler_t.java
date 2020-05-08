 package cc.thedudeguy.xpinthejar;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 /**
  * @author bendem
  */
 public class CommandHandler implements CommandExecutor {
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!command.getName().equalsIgnoreCase("xpinthejar")) {
             return false;
         }
         if(!sender.hasPermission("xpjar.command.debug")) {
             sender.sendMessage(ChatColor.RED + "You don't have the permission to use this command!");
             return true;
         }
         if(args.length == 2 && "debug".equalsIgnoreCase(args[0])) {
             if("true".equalsIgnoreCase(args[1]) || "false".equalsIgnoreCase(args[1])) {
                 XPInTheJar.instance.getConfig().set("debug", "true".equalsIgnoreCase(args[1]));
                 XPInTheJar.instance.saveConfig();
                 sender.sendMessage("Debug " + (XPInTheJar.instance.getConfig().getBoolean("debug") ? "" : "de") + "activated");
             } else {
                 sender.sendMessage("Debug value can only be 'true' or 'false'");
             }
             return true;
         }
 
         return false;
     }
 
 }

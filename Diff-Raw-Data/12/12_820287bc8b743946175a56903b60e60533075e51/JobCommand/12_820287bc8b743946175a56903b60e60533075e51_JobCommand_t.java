 package net.krinsoft.jobsuite.commands;
 
 import com.pneumaticraft.commandhandler.Command;
 import net.krinsoft.jobsuite.JobCore;
 import net.krinsoft.jobsuite.JobManager;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * @author krinsdeath
  */
 public abstract class JobCommand extends Command {
     protected JobCore plugin;
     protected JobManager manager;
 
     public JobCommand(JobCore instance) {
         super(instance);
         plugin = instance;
         manager = plugin.getJobManager();
     }
 
     @Override
     public void showHelp(CommandSender sender) {
         sender.sendMessage(ChatColor.DARK_GREEN + "=== " + ChatColor.AQUA + getCommandName() + ChatColor.DARK_GREEN + " ===");
         sender.sendMessage(ChatColor.GREEN + "Usage: " + ChatColor.DARK_RED + getCommandUsage());
         sender.sendMessage(ChatColor.GREEN + "Description: " + ChatColor.DARK_AQUA + getCommandDesc());
         sender.sendMessage(ChatColor.GREEN + "Permission: " + ChatColor.DARK_PURPLE + getPermissionString());
        if (getCommandExamples().size() > 0) {
            sender.sendMessage(ChatColor.GREEN + "Examples:");
            int i = 0;
            for (String example : getCommandExamples()) {
                sender.sendMessage(example);
                if (i++ > 4 && sender instanceof Player) { break; }
            }
         }
     }
 
     public boolean hasPermission(CommandSender sender, String node) {
         return sender instanceof ConsoleCommandSender || sender.hasPermission(node);
     }
 
     public void message(CommandSender sender, String message) {
         sender.sendMessage(ChatColor.GREEN + "[Job] " + ChatColor.WHITE + message);
     }
 
     public void error(CommandSender sender, String message) {
         sender.sendMessage(ChatColor.RED + "[Job] " + message);
     }
 
 }

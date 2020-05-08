 package haveric.woolDyer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class Commands implements CommandExecutor {
 
     private WoolDyer plugin;
 
     private static String cmdMain = "wooldyer";
     private static String cmdMainAlt = "wd";
     private static String cmdHelp = "help";
 
     private static String cmdPerms = "perms";
     private static String cmdPermsAlt = "perm";
 
     public Commands(WoolDyer wd) {
         plugin = wd;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         ChatColor msgColor = ChatColor.DARK_AQUA;
 
         String title = msgColor + "[" + ChatColor.GRAY + plugin.getDescription().getName() + msgColor + "] ";
 
         boolean op = false;
         if (sender.isOp()) {
             op = true;
         }
 
         if (commandLabel.equalsIgnoreCase(cmdMain) || commandLabel.equalsIgnoreCase(cmdMainAlt)) {
             if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase(cmdHelp))) {
                 sender.sendMessage(title + "github.com/haveric/WoolDyer - v" + plugin.getDescription().getVersion());
             } else if (args.length == 1 && (args[0].equalsIgnoreCase(cmdPerms) || args[0].equalsIgnoreCase(cmdPermsAlt))) {
                 if (op) {
                     sender.sendMessage(title + "Permission nodes:");
                     sender.sendMessage(Perms.getPermDye() + " - " + msgColor + "Allows user to dye wool");
                 } else {
                    sender.sendMessage(title + ChatColor.RED + "You must be an op to see permission nodes.");
                 }
             }
         }
         return false;
     }
 
     public String getMain() {
         return cmdMain;
     }
 }

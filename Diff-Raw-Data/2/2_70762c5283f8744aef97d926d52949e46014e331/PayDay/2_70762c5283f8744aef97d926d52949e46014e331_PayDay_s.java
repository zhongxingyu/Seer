 package tzer0.PayDay;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.nijiko.coelho.iConomy.iConomy;
 import com.nijiko.coelho.iConomy.system.Bank;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class PayDay extends JavaPlugin {
     public PermissionHandler permissions;
     PluginDescriptionFile pdfFile;
     private Configuration conf;
     @Override
     public void onDisable() {
 
     }
 
     @Override
     public void onEnable() {
         pdfFile = this.getDescription();
         conf = getConfiguration();
         setupPermissions();
         getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, new PayDayPlayerListener(this), Priority.Normal, this);
         System.out.println(pdfFile.getName() + " version "
                 + pdfFile.getVersion() + " is enabled!");
     }
 
     public boolean onCommand(CommandSender sender, Command cmd,
             String commandLabel, String[] uargs) {
         // Keeping everything lower-case except for account names.
         String[] args = new String[uargs.length];
         for (int i = 0; i < args.length; i++) {
             uargs[i] = uargs[i].replace(".", "");
             args[i] = uargs[i].toLowerCase();
         }
         if (args.length == 0 || (args.length >= 1 && args[0].equalsIgnoreCase("help"))) {
             int page = 1;
             if (args.length == 2) {                
                 page = toInt(args[1], sender);
             }
             sender.sendMessage(ChatColor.GREEN+"PayDay " + pdfFile.getVersion() + " by TZer0");
             if (page == 1) {
                 sender.sendMessage(ChatColor.YELLOW+"Help (all commands start with /pd):");
                 sender.sendMessage(ChatColor.YELLOW+"checkerrors - checks for errors in the config-file");
                 sender.sendMessage(ChatColor.YELLOW+"players [#] - shows number # page in the list of players");
                 sender.sendMessage(ChatColor.YELLOW+"groups [#] - shows number # page in the list of groups");
                 sender.sendMessage(ChatColor.YELLOW+"payday - pays everyone their money, won't run if checkerrors fails");
                 sender.sendMessage(ChatColor.YELLOW+"payday group/player name - pays a specific group or player");
                 sender.sendMessage(ChatColor.YELLOW+"set group name value - creates a group with earns value per payday");
                 sender.sendMessage(ChatColor.YELLOW+"set player name groupname - assigns a player to a group");
                sender.sendMessage(ChatColor.YELLOW+"mv groupname1 groupname2 - moves all players from one group to another");
                 sender.sendMessage(ChatColor.YELLOW+"delete player/group name - deletes a group/player");
                 sender.sendMessage(ChatColor.YELLOW+"sync [overwrite] - imports players and groups from iConomy and Permissions");
                 sender.sendMessage(ChatColor.RED+"REMEMBER: player-names are CASE-SENSITIVE");
                 sender.sendMessage(ChatColor.YELLOW+"help 2 for aliases (very useful)");
             } else if (page == 2) {
                 sender.sendMessage(ChatColor.YELLOW+"Aliases:");
                 sender.sendMessage(ChatColor.YELLOW+"player = pl, players = pl, groups = gr");
                 sender.sendMessage(ChatColor.YELLOW+"group = gr, checkerrors = ce, payday = pd");
                 sender.sendMessage(ChatColor.YELLOW+"set = s, delete = d, move = mv");
                 sender.sendMessage(ChatColor.YELLOW+"sync = sy, overwrite = ow");
                 sender.sendMessage(ChatColor.YELLOW+"Example usage:");
                 sender.sendMessage(ChatColor.YELLOW+"/pd s gr epicgroup 10000");
                 sender.sendMessage(ChatColor.YELLOW+"/pd s pl TZer0 epicgroup");
                 sender.sendMessage(ChatColor.YELLOW+"/pd pd");
                 sender.sendMessage(ChatColor.YELLOW+"/pd d gr epicgroup");
 
             } else {
                 sender.sendMessage(ChatColor.YELLOW + "No such help-page!");
             }
             return true;
 
         } else if (args.length >= 1 && (args[0].equalsIgnoreCase("sync") || (args[0].equalsIgnoreCase("sy")))) { 
             // Imports data from Permissions and iConomy.
             Bank ic = iConomy.getBank();
             if (permissions == null) {
                 sender.sendMessage(ChatColor.RED + "Permissions unavailable - aborting.");
             } else {
                 boolean overwrite = (args.length == 2 && (args[1].equalsIgnoreCase("overwrite") || args[1].equalsIgnoreCase("ow")));
                 for (String key: ic.getAccounts().keySet()) {
                     if (conf.getString("players."+key) == null || overwrite) {
                         conf.setProperty("players."+key, permissions.getGroup("world", key));
                         if (conf.getString("groups."+permissions.getGroup("world", key)) == null) {
                             conf.setProperty("groups."+permissions.getGroup("world", key), 0);
                         }
                     }
                 }
                 conf.save();
                 sender.sendMessage(ChatColor.GREEN+"Done!");
             }
             return true;
         } else if (args.length >= 1 && (args[0].equalsIgnoreCase("checkerrors") || args[0].equalsIgnoreCase("ce"))) {
             // Utility - checks for errors while not running payday.
             if (!checkErrors(sender, iConomy.getBank())) {
                 sender.sendMessage(ChatColor.GREEN+"No errors found.");
             } else {
                 sender.sendMessage(ChatColor.RED + "Errors found, fix them before running payday");
             }
         } else if (args.length >= 1 && (args[0].replace("players", "player").equalsIgnoreCase("player") || args[0].equalsIgnoreCase("pl"))) {
             // Lists players
             int page = 0;
             if (args.length == 2) {
                 page = toInt(args[2], sender);
             }
             page(page, sender, "Player");
         } else if (args.length >= 1 && (args[0].replace("groups", "group").equalsIgnoreCase("group") || args[0].equalsIgnoreCase("gr"))) {
             // Lists groups
             int page = 0;
             if (args.length == 2) {
                 page = toInt(args[2], sender);
             }
             page(page, sender, "Group");
         } else if (args.length >= 1 && (args[0].equalsIgnoreCase("payday") || args[0].equalsIgnoreCase("pd"))) {
             // Attempts to pay out the predefined amounts of cash, fails before paying out anything if
             // the config is incorrect
             Bank ic = iConomy.getBank();
             if (checkErrors(sender, iConomy.getBank())) {
                 sender.sendMessage(ChatColor.RED + "Errors found, fix them before running payday");
                 return true;
             }
             List<String> pay = null;
             if (args.length == 3) {
                 pay = new LinkedList<String>();
                 List<String> full = conf.getKeys("players.");
                 if (args[1].equalsIgnoreCase("group") || args[1].equalsIgnoreCase("gr")) {
                     for (String name : full) {
                         if (conf.getString("players."+name).equalsIgnoreCase(args[2])) {
                             pay.add(name);
                         }
                     }
                 } else if (args[1].equalsIgnoreCase("player") || args[1].equalsIgnoreCase("pl")) {
                     if (full.contains(args[2])) {
                         pay.add(args[2]);
                     } else {
                         sender.sendMessage(ChatColor.RED + "No such player!");
                     }
                 } else {
                     sender.sendMessage(ChatColor.RED + "Invalid 3rd parameter, must be group or player");
                     return true;
                 }
             } else if (args.length == 1) {
                 pay = conf.getKeys("players.");
                 getServer().broadcastMessage(ChatColor.GOLD+"It is Pay Day!");
             }
 
             if (args.length <= 3 || args.length == 1) {
                 for (String pl : pay) {
                     ic.getAccount(pl).add(conf.getInt("groups."+conf.getString("players."+pl, "none"),0));
                 }
             } else {
                 sender.sendMessage(ChatColor.RED+"Incorrect format, see help");
                 return true;
             }
             sender.sendMessage(ChatColor.GREEN+"Payday complete");
         } else if (args.length >= 1 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("s"))) {
             // sets either a group's income or a player's group
             if (args.length == 4) {
                 if (args[1].equalsIgnoreCase("group") || args[1].equalsIgnoreCase("gr")) {
                     if (checkInt(args[3]) && !args[2].equalsIgnoreCase("none")) {
                         conf.setProperty("groups."+args[2], Integer.parseInt(args[3]));
                         conf.save();
                         sender.sendMessage(ChatColor.GREEN + "Done.");
                     } else {
                         sender.sendMessage("Invalid value.");
                     }
                 } else if (args[1].equalsIgnoreCase("player") || args[1].equalsIgnoreCase("pl")) {
                     if (conf.getString("groups."+args[3]) != null) {
                         conf.setProperty("players."+uargs[2], args[3]);
                         conf.save();
                         sender.sendMessage(ChatColor.GREEN + "Done.");
                     } else {
                         sender.sendMessage(ChatColor.RED + "No such group");
                     }
                 } else {
                     sender.sendMessage(ChatColor.RED+ String.format("Unknown type %s!", uargs[2]));
                     return true;
                 }
             } else {
                 sender.sendMessage(ChatColor.RED+"Invalid format, see help");
             }
 
         } else if (args.length >= 1 && (args[0].equalsIgnoreCase("move") || args[0].equalsIgnoreCase("mv"))) {
             // Moves all players from one group to another - even if the group you're moving from does
             // no longer exist
             if (args.length == 3) {
                 List<String> groups = conf.getKeys("groups.");
                 if (!groups.contains(args[2])) {
                     sender.sendMessage(ChatColor.RED + String.format("No such group %s", args[2]));
                 } else {
                     List<String> players = conf.getKeys("players.");
                     for (String pl : players) {
                         if (conf.getString("players."+pl).equalsIgnoreCase(args[1])) {
                             conf.setProperty("players."+pl, args[2]);
                         }
                         conf.save();
                     }
                 }
             }
         } else if (args.length >= 1 && (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("d"))) {
             // deletes either a player or a group
             if (args.length == 3) {
                 if (args[1].equalsIgnoreCase("group") || args[1].equalsIgnoreCase("gr")) {
                     if (conf.getString("groups."+args[2]) != null) {
                         conf.removeProperty("groups."+args[2]);
                         conf.save();
                     } else {
                         sender.sendMessage(ChatColor.RED+"No such group: " + args[2]);
                         return true;
                     }
                 } else if (args[1].equalsIgnoreCase("player") || args[1].equalsIgnoreCase("pl")) {
                     if (conf.getString("players."+uargs[2]) != null) {
                         conf.removeProperty("players."+uargs[2]);
                         conf.save();
                     } else {
                         sender.sendMessage(ChatColor.RED+"No such player: " + uargs[2]);
                         return true;
                     }
                 } else {
                     sender.sendMessage(ChatColor.RED+ String.format("Unknown type %s!", args[1]));
                     return true;
                 }
                 sender.sendMessage(ChatColor.GREEN + "Done");
             } else {
                 sender.sendMessage(ChatColor.RED+"Incorrect format, see help");
             }
         } else {
             sender.sendMessage(ChatColor.YELLOW + "No such command, see help!");
         }
         return true;
     }
 
     /**
      * Checks the configuration for errors - true if errors are found.
      * 
      * @param sender The one who will receive the error-messages
      * @param ic iConomy-bank
      * @return
      */
     public boolean checkErrors(CommandSender sender, Bank ic) {
         sender.sendMessage(ChatColor.YELLOW+"Checking for errors.");
         boolean failed = false;
         if (conf.getString("failed.") != null) {
             conf.removeProperty("failed.");
         }
         List<String> keys = conf.getKeys("players.");
         List<String> dupefound = new LinkedList<String>();
         List<String> groups = conf.getKeys("groups.");
         if (keys == null || groups == null) {
             sender.sendMessage(ChatColor.RED + "No configuration (groups or players)!");
             return true;
         }
         for (String pl : keys) {
             if (!ic.hasAccount(pl)) {
                 sender.sendMessage(ChatColor.RED+String.format("%s doesn't have an account!", pl));
                 failed = true;
             }
             for (String pl2 : keys) {
                 if (!dupefound.contains(pl2) && pl.equalsIgnoreCase(pl2) && !pl.equals(pl2)) {
                     sender.sendMessage(ChatColor.RED+String.format(ChatColor.RED + "%s may be a duplicate of %s (or vice versa)", pl, pl2));
                     dupefound.add(pl2);
                     dupefound.add(pl);
                     failed = true;
                 }
             }
             if (!groups.contains(conf.getString("players."+pl))) {
                 sender.sendMessage(ChatColor.RED+String.format("%s belongs to an invalid group - %s", pl, conf.getString("players."+pl)));
                 failed = true;
             }
         }
         return failed;
 
     }
     /**
      * Displays information about either groups or players.
      * @param page Page to view
      * @param sender Who gets the output
      * @param node either group or player - decides what is shown.
      */
     public void page(int page, CommandSender sender, String node) {
         List<String> items = conf.getKeys(node.toLowerCase()+"s.");
 
         if (items != null && page*10 < items.size()) {
             sender.sendMessage(String.format("Listing %ss, page %d of %d", node, page, (items.size()-1)/10+1));
             for (int i = page*10; i < Math.min(items.size(), page*10+10); i++) {
                 sender.sendMessage(items.get(i) + " - " + conf.getString(node.toLowerCase()+"s."+items.get(i), "error"));
             }
             if (items.size() > page*10+10) {
                 sender.sendMessage(String.format("/pd %s %d for next page", node, page+1));
             }
         } else {
             sender.sendMessage("No more items.");
         }
     }
     /**
      * Converts to int if valid, if not: returns 0
      * @param in
      * @param sender
      * @return
      */
     public int toInt(String in, CommandSender sender) {
         int out = 0;
         if (checkInt(in)) {
             out = Integer.parseInt(in);
         }
         return out;
     }
     /**
      * Checks if a string is valid as a representation of an unsigned int.
      */
     public boolean checkInt(String in) {
         char chars[] = in.toCharArray();
         for (int i = 0; i < chars.length; i++) {
             if (!Character.isDigit(chars[i])) {
                 return false;
             }
         }
         return true;
     }
     /**
      * Basic Permissions-setup, see more here: https://github.com/TheYeti/Permissions/wiki/API-Reference
      */
     private void setupPermissions() {
         Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
 
         if (this.permissions == null) {
             if (test != null) {
                 this.permissions = ((Permissions) test).getHandler();
             } else {
                 System.out.println(ChatColor.YELLOW
                         + "Permissons not detected - defaulting to OP!");
             }
         }
     }
 }

 package net.bless.ph;
 
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 public class PHCommands implements CommandExecutor {
     PermissionsHealth plugin;
     public Player player;
 
     public PHCommands(PermissionsHealth plugin) {
         this.plugin = plugin;
     }
 
     private enum OBCommand {
         HELP("help", "", "permissionshealth.admin.help"), 
         RELOAD("reload", "r", "permissionshealth.admin.reload"),
         ADD("add", "a", "permissionshealth.admin.add"), 
         UPDATE("update", "u", "permissionshealth.admin.update"), 
         HEAL("heal", "h", "permissionshealth.admin.heal"), 
         HP("hp", "p", "permissionshealth.admin.showhp");
         
         private String cmdName;
         private String cmdShort;
         private String perm;
 
         private OBCommand(String name, String abbr, String perm) {
             cmdName = name;
             cmdShort = abbr;
             this.perm = perm;
         }
 
         public static OBCommand match(String label, String firstArg) {
             boolean arg = false;
             if (label.equalsIgnoreCase("ph"))
                 arg = true;
             for (OBCommand cmd : values()) {
                 if (arg) {
                     for (String item : cmd.cmdName.split(",")) {
                         if (firstArg.equalsIgnoreCase(item))
                             return cmd;
                     }
                 } else if (label.equalsIgnoreCase("ph" + cmd.cmdShort)
                         || label.equalsIgnoreCase("ph" + cmd.cmdName))
                     return cmd;
                 else {
                     for (String shortcut : cmd.cmdShort.split(",")) {
                         if (label.equalsIgnoreCase("ph" + shortcut))
                             return cmd;
 
                         // special case for "o" as a shortcut by itself (eg.
                         // "/o")
                         if (shortcut.equalsIgnoreCase("o")
                                 && label.equalsIgnoreCase(shortcut))
                             return cmd;
 
                     }
                 }
             }
             return null;
         }
 
         public String[] trim(String[] args, StringBuffer name) {
             if (args.length == 0)
                 return args;
             if (!args[0].equalsIgnoreCase(cmdName))
                 return args;
             String[] newArgs = new String[args.length - 1];
             System.arraycopy(args, 1, newArgs, 0, newArgs.length);
             if (name != null)
                 name.append(" " + args[0]);
             return newArgs;
         }
     }
 
     private String getName(CommandSender sender) {
         if (sender instanceof ConsoleCommandSender)
             return "CONSOLE";
         else if (sender instanceof Player)
             return ((Player) sender).getName();
         else
             return "UNKNOWN";
     }
     
     /**
      * @param sender
      * @param args
      * @param cmd
      */
     private boolean checkCommandPermissions(CommandSender sender,
             String[] args, OBCommand cmd) {
         boolean pass = false;
         if (cmd.perm.isEmpty())
             pass = true;
         else if (sender instanceof Player && ((Player)sender).hasPermission(cmd.perm))
             pass = true;
 
         if (!pass)
             sender.sendMessage("You don't have permission for this command.");
         return pass;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command,
             String label, String[] args) {
         OBCommand cmd = OBCommand.match(label, args.length >= 1 ? args[0] : "");
         if (cmd == null)
             return false;
         StringBuffer cmdName = new StringBuffer(label);
         args = cmd.trim(args, cmdName);
 
         if (!checkCommandPermissions(sender, args, cmd))
             return true;
 
         if ((sender instanceof Player)) {
             this.player = ((Player) sender);
         }
 
         switch (cmd) {
         case ADD:
             cmdAdd(sender, args);
             break;
         case HEAL:
             cmdHeal(sender, (args.length > 0 ? args[0] : null));
             break;
         case HELP:
             cmdHelp(sender, args);
             break;
         case HP:
             cmdShowHp(sender, (args.length > 0 ? args[0] : null));
             break;
         case RELOAD:
             cmdReload(sender);
             break;
         case UPDATE:
             cmdUpdate(sender, args);
             break;
         default:
             cmdHelp(sender, args);
             break;
         }
         
         return true;
     }
 
     /**
      * @param sender
      * @param args
      */
     private boolean cmdShowHp(CommandSender sender, String playerName) {
         if (sender instanceof ConsoleCommandSender && playerName == null) { // console and no player name
             player.sendMessage("Please specify a player by doing /pho <player]>");
             return true;
         } else if (playerName == null) { // player and no player name
             this.player.sendMessage(ChatColor.GREEN + "You have " + player.getHealth() + " Health!");
             return true;
         }
 
         Player targetPlayer = Bukkit.getServer().getPlayer(playerName);
         if (targetPlayer != null) {
             player.sendMessage(ChatColor.GREEN
                     + targetPlayer.getDisplayName() + " has "
                     + targetPlayer.getHealth() + " Health!");
         } else {
             player.sendMessage(ChatColor.RED + "That player is not online!");
         }
 
 
         return true;
     }
 
     /**
      * @param sender
      * @param args
      * @return
      */
     private void cmdHeal(CommandSender sender, String playerName) {
         if (player != null && playerName == null) {
             this.player.setHealth(player.getMaxHealth());
             this.player.setFoodLevel(20);
             this.player.setFireTicks(0);
             this.player.sendMessage(ChatColor.GREEN
                     + "You health is full.");
         } else if (playerName != null) {
             Player target = Bukkit.getServer().getPlayer(playerName);
 
             if (target == null) {
                 sender.sendMessage(ChatColor.RED + playerName
                         + " is not online!");
                 return;
             }
             target.setHealth(target.getMaxHealth());
             target.sendMessage(ChatColor.GREEN
                     + "Your health is full.");
             sender.sendMessage(ChatColor.GREEN
                     + playerName + "'s health is full.");
         } else {
             sender.sendMessage("Please write /pheal <name>");
         }
     }    
 
     /**
      * @param sender
      * @param args
      * @return
      */
     private void cmdHelp(CommandSender sender, String[] args) {
         // used a treemap because it's sorted (we want msgs to appear in order)
         Map<String, String> helpInfo = new TreeMap<String, String>();
         helpInfo.put("/php", "used to view your current health");
         helpInfo.put("/phr", "reloads the config");
         helpInfo.put("/phh", "used to to fully heal you");
         helpInfo.put("/phh <player>", "used to to fully heal a player");
         helpInfo.put("/php <player>", "used to view target's current health");
         helpInfo.put("/phl", "displays pages for nodes and health");
         helpInfo.put("/phl page", "used to view your current health");
         helpInfo.put("/ph add <node#> <healthval>", "lets you edit the nodes from ingame changing them");
         helpInfo.put("/ph update <node#> <healthval>", "lets you edit ingame the amount of health the node has");
         
         for (Entry<String, String> entry : helpInfo.entrySet()) {
             this.player.sendMessage("" + ChatColor.GOLD + entry.getKey() + ChatColor.WHITE + " : " + entry.getValue() + ".");
         }
     }
 
 
     /**
      * @return
      */
     private void cmdReload(CommandSender sender) {
         this.plugin.reloadConfig();
         sender.sendMessage(ChatColor.GOLD
                 + "PermissionsHealth has been reloaded");
     }
 
     /**
      * @param sender
      * @param args
      * @param player
      */
     private boolean cmdAdd(CommandSender sender, String[] args) {
         boolean add = true;
         return cmdChange(sender, args, add);
     }
     
     /**
      * @param sender
      * @param args
      * @param player
      */
     private boolean cmdUpdate(CommandSender sender, String[] args) {
         boolean add = false;
         return cmdChange(sender, args, add);
     }
     
     private boolean cmdChange(CommandSender sender, String[] args, boolean add) {
         if (args.length < 2) {
             sender.sendMessage("Usage: /ph <update|add> <node> <value>");
             return false;
         }
 
        String nodeName = args[0];
         String nodeValue = args[1];
 
         if (PermissionsHealth.permissionsMap.containsKey(nodeName)) {
             // key exists, update map and save to config
             setHealthValue(sender, nodeName, nodeValue);
             return true;
         } else {
             // no permission currently, ask player if they want to create?
             // or just create a new node?
             if (add) {
                 // for add command
                 setHealthValue(sender, nodeName, nodeValue);
             } else {
                 // for update command
                 sender.sendMessage(ChatColor.RED + "Error - node not found, use /ph add <node> <value> if you want to add it.");
             }
             return true;
         }
     }
 
     /**
      * @param nodeName
      * @param nodeValue
      */
     private void setHealthValue(CommandSender sender, String nodeName, String nodeValue) {
         int healthValue = 0;
         try {
             healthValue = Integer.parseInt(nodeValue);
         } catch (NumberFormatException nfe) {
 
         }
         plugin.getConfig().set(nodeName, healthValue);
         plugin.saveConfig();
         PermissionsHealth.permissionsMap.put(nodeName, healthValue);
         sender.sendMessage(ChatColor.GREEN + "Permission node '"
                 + nodeName + "' has been updated to " + healthValue);
     }
 }

 package com.hackhalo2.creative;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class PixlCommand implements CommandExecutor {
     private final String[] wool = { "white", "orange", "magenta", "lightblue", "yellow", "lightgreen", "pink",
             "gray", "lightgray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
     private final String name = ChatColor.AQUA + "P" + ChatColor.DARK_AQUA + "i" + ChatColor.BLUE + "x" + ChatColor.DARK_BLUE + "l";
     private final Pixl plugin;
 
     public PixlCommand(Pixl p) {
         this.plugin = p;
     }
 
     public void sendVersionInfo(CommandSender cs) {
         cs.sendMessage(name + ChatColor.AQUA + " Version " + plugin.version);
     }
 
     public void togglePixl(Player player) {
         if (!player.hasPermission("pixl.toggle")) {
             player.sendMessage(ChatColor.RED + "You do not have permission to "
                                + "use this command.");
             return;
         }
 
         plugin.setToggle(player, !plugin.isToggled(player));
         player.sendMessage(ChatColor.AQUA + "PixlToggle " + 
                            (plugin.isToggled(player) ? "Enabled" : "Disabled"));
     }
 
     public boolean onCommand(CommandSender cs, Command c, String l, String[] args) {
         if (!cs.hasPermission("pixl.command")) {
             cs.sendMessage(ChatColor.RED + "You do not have permission to use "
                            + "this command.");
             return true;
         }
 
         Player player;
         if (cs instanceof Player) {
             player = (Player) cs;
         } else {
             // The console can get version info, but can't use Pixl, since it
             // requires a presence in world.
             sendVersionInfo(cs);
             return true;
         }
 
         if (args.length == 0) {
             togglePixl(player);
             return true;
         } else if (args.length == 1) {
             if (args[0].equalsIgnoreCase("version")) {
                 sendVersionInfo(player);
                 return true;
             } else if (args[0].equalsIgnoreCase("toggle")) {
                 togglePixl(player);
                 return true;
             } else if (args[0].equalsIgnoreCase("help")) {
                 sendVersionInfo(player);
                 cs.sendMessage(ChatColor.AQUA + "/pixl help | "
                                + ChatColor.DARK_AQUA + "Displays this menu");
                 cs.sendMessage(ChatColor.AQUA + "/pixl | " + ChatColor.DARK_AQUA
                                + "Toggles Pixl on/off");
                 cs.sendMessage(ChatColor.AQUA + "/pixl set <value> | "
                                + ChatColor.DARK_AQUA
                                + "Set wool color to <value>");
                 cs.sendMessage(ChatColor.AQUA + "/pixl clear | "
                                + ChatColor.DARK_AQUA
                                + "Clears the value set by /pixl set");
                 cs.sendMessage(ChatColor.AQUA + "/pixl break | "
                                + ChatColor.DARK_AQUA
                                + "Toggles PixlBreak on/off");
                 return true;
             } else if (args[0].equalsIgnoreCase("break")) {
                 if (!player.hasPermission("pixl.break")) {
                     cs.sendMessage(ChatColor.RED + "You do not have permission "
                                    + "to use this command.");
                     return true;
                 }
 
                 plugin.setBreak(player,
                                 (plugin.breakMode(player) ? false : true));
                 cs.sendMessage(ChatColor.AQUA + "PixlBreak "
                                + (plugin.breakMode(player)
                                                ? "Enabled" : "Disabled"));
                 if (plugin.shatterMode(player) && !plugin.breakMode(player)) {
                     plugin.setShatter(player, false);
                     cs.sendMessage(ChatColor.AQUA + "PixlShatter was also disabled");
                 }
                 return true;
             } else if (args[0].equalsIgnoreCase("shatter")) {
                 if (!player.hasPermission("pixl.break")) {
                     cs.sendMessage(ChatColor.RED + "You do not have permission "
                                    + "to use this command.");
                     return true;
                 }
 
                plugin.setShatter(player,
                                 (plugin.shatterMode(player) ? false : true));
                 cs.sendMessage(ChatColor.AQUA + "PixlShatter "
                                + (plugin.shatterMode(player)
                                                ? "Enabled" : "Disabled"));
                 return true;
             } else if (args[0].equalsIgnoreCase("clear")) {
                 if (!player.hasPermission("pixl.toggle")) {
                     cs.sendMessage(ChatColor.RED + "You do not have permission "
                                    + "to use this command.");
                     return true;
                 }
 
                 if (plugin.isSet(player) != null) {
                     plugin.removeValue(player);
                     player.sendMessage(ChatColor.AQUA
                                        + "Hard value cleared.");
                 } else {
                     player.sendMessage(ChatColor.AQUA
                                        + "You do not have a hard value set.");
                 }
 
                 return true;
             }
         } else if (args.length == 2) {
             if (args[0].equalsIgnoreCase("set")) {
                 if (!player.hasPermission("pixl.toggle")) {
                     cs.sendMessage(ChatColor.RED + "You do not have permission "
                                    + "to use this command.");
                     return true;
                 }
 
                 try {
                     int raw_value = Integer.parseInt(args[1].trim());
                     int value = Math.max(0,  Math.min(15, raw_value));
                     plugin.setValue(player, value);
                     cs.sendMessage(ChatColor.AQUA + "Hard value set to "
                                    + wool[value] + ".");
                     return true;
                 } catch(NumberFormatException e) {
                     String names = "";
                     String sep = "";
                     for (int i=0; i < wool.length; i++) {
                         if (args[1].equalsIgnoreCase(wool[i])) {
                             plugin.setValue(player, i);
                             cs.sendMessage(ChatColor.AQUA + "Hard value set to "
                                            + wool[i] + ".");
                             return true;
                         }
 
                         names += sep + wool[i];
                         sep = ", ";
                     }
                     cs.sendMessage(ChatColor.RED + "I don't know that color.");
                     cs.sendMessage(ChatColor.AQUA + "Valid color names:");
                     cs.sendMessage(ChatColor.AQUA + names);
                     cs.sendMessage(ChatColor.AQUA + "(You can also specify the "
                                    + "color's data value, if you know it.)");
                     return true;
                 }
             }
         }
         return false;
     }
 }

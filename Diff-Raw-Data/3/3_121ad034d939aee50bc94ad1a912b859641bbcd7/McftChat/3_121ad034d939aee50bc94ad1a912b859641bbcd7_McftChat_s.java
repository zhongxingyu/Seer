 package com.mcftmedia.bukkit.mcftchat;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
 * McftChat plugin for Bukkit
 * Built on Permissions 3
  * 
  * @author Jon la Cour
  * @version 1.3.2
  */
 public class McftChat extends JavaPlugin {
 
     private final McftChatPlayerListener playerListener = new McftChatPlayerListener(this);
     private final ConcurrentHashMap<Player, Boolean> debugees = new ConcurrentHashMap<Player, Boolean>();
     public final ConcurrentHashMap<String, String> settings = new ConcurrentHashMap<String, String>();
     public final ConcurrentHashMap<String, String> colorconfig = new ConcurrentHashMap<String, String>();
     public static PermissionHandler permissionHandler;
     public static final Logger logger = Logger.getLogger("Minecraft.McftChat");
     String baseDir = "plugins/McftChat";
     String configFile = "settings.txt";
     String colorconfigFile = "colors.txt";
 
     @Override
     public void onDisable() {
         PluginDescriptionFile pdfFile = this.getDescription();
         logger.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled!");
     }
 
     @Override
     public void onEnable() {
         checkSettings();
         setupPermissions();
         loadSettings();
 
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Lowest, this);
 
         PluginDescriptionFile pdfFile = this.getDescription();
         logger.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
     }
 
     public boolean isDebugging(final Player player) {
         if (debugees.containsKey(player)) {
             return debugees.get(player);
         } else {
             return false;
         }
     }
 
     public void setDebugging(final Player player, final boolean value) {
         debugees.put(player, value);
     }
 
     public void setupPermissions() {
         if (permissionHandler != null) {
             return;
         }
 
         Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 
         if (permissionsPlugin == null) {
             logger.info("[McftChat] Permissions system not enabled. Disabling plugin.");
             this.getServer().getPluginManager().disablePlugin(this);
         }
 
         permissionHandler = ((Permissions) permissionsPlugin).getHandler();
         logger.info("[McftChat] Found and will use plugin " + ((Permissions) permissionsPlugin).getDescription().getFullName());
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         String cmdname = cmd.getName().toLowerCase();
         Player player = null;
         String pname = ChatColor.DARK_RED + "[Console]";
         if (sender instanceof Player) {
             player = (Player) sender;
             pname = player.getName();
         }
 
         for (String command : settings.keySet()) {
             if (cmdname.equalsIgnoreCase(command)) {
                 args = unshift(cmdname, args);
                 cmdname = "mcftchat";
             }
         }
 
         if (cmdname.equalsIgnoreCase("mcftchat") && args.length >= 2) {
             String message = "";
             for (Integer index = 1; index < args.length; index++) {
                 message = message.concat(" " + args[index]);
             }
             for (String command : settings.keySet()) {
                 if (args[0].equalsIgnoreCase(command)) {
                     if (player == null || permissionHandler.permission(player, "mcftchat." + command + ".send")) {
                         String sendername = pname;
                         if (player != null) {
                             String worldname = player.getLocation().getWorld().getName();
                             String group = permissionHandler.getGroup(worldname, pname);
                             String prefix = groupPrefix(group, worldname);
                             if (!prefix.equals("") || prefix != null) {
                                 String prefixcolor = prefix.replace("&", "");
                                 if (prefixcolor.length() == 1) {
                                     int prefixid = Integer.parseInt(getColor(prefixcolor));
                                     ChatColor usercolor = ChatColor.getByCode(prefixid);
                                     sendername = usercolor + "[" + pname + "]";
                                 }
                             } else {
                                 ChatColor usercolor = ChatColor.WHITE;
                                 sendername = usercolor + "[" + pname + "]";
                             }
                         }
                         String channel = settings.get(command);
                         ChatColor color = ChatColor.valueOf(colorconfig.get(channel));
                         Player[] players = getServer().getOnlinePlayers();
                         for (Player p : players) {
                             if (permissionHandler.permission(p, "mcftchat." + command + ".receive")) {
                                 p.sendMessage(sendername + color + message);
                             }
                         }
                         logger.info(pname + "->" + channel + ":" + message);
                         return true;
                     } else {
                         logger.info("[McftChat] Permission denied for '" + command + "': " + pname);
                     }
                 }
             }
 
         }
         return false;
     }
 
     private void checkSettings() {
         String config = "";
         String colors = "";
         File configfile;
         File colorsfile;
 
         // Creates base directory for config files
         config = baseDir;
         configfile = new File(config);
         if (!configfile.exists()) {
             if (configfile.mkdir()) {
                 logger.info("[McftChat] Created directory '" + config + "'");
             }
         }
 
         // Creates base config file
         config = baseDir + "/" + configFile;
         configfile = new File(config);
         if (!configfile.exists()) {
             BufferedWriter output;
             String newline = System.getProperty("line.separator");
             try {
                 output = new BufferedWriter(new FileWriter(config));
                 output.write("# Command=Channel" + newline);
                 output.write("o=Owners" + newline);
                 output.write("a=Admins" + newline);
                 output.write("d=Donators" + newline);
                 output.close();
                 logger.info("[McftChat] Created config file '" + config + "'");
             } catch (Exception e) {
                 Logger.getLogger(McftChat.class.getName()).log(Level.SEVERE, null, e);
             }
         }
 
         // Creates colors config file
         colors = baseDir + "/" + colorconfigFile;
         colorsfile = new File(colors);
         if (!colorsfile.exists()) {
             BufferedWriter output;
             String newline = System.getProperty("line.separator");
             try {
                 output = new BufferedWriter(new FileWriter(colors));
                 output.write("# Channel=COLOR" + newline);
                 output.write("# Available colors: http://jd.bukkit.org/apidocs/org/bukkit/ChatColor.html" + newline);
                 output.write("Owners=GOLD" + newline);
                 output.write("Admins=LIGHT_PURPLE" + newline);
                 output.write("Donators=DARK_AQUA" + newline);
                 output.close();
                 logger.info("[McftChat] Created colors config file '" + colors + "'");
             } catch (Exception e) {
                 Logger.getLogger(McftChat.class.getName()).log(Level.SEVERE, null, e);
             }
         }
     }
 
     private void loadSettings() {
         String config = baseDir + "/" + configFile;
         String colors = baseDir + "/" + colorconfigFile;
         String line = null;
 
         // Adds default channels to settings map
         settings.put("o", "Owners");
         settings.put("a", "Admins");
         settings.put("d", "Donators");
 
         // Adds default colors to colors map
         colorconfig.put("Owners", "GOLD");
         colorconfig.put("Admins", "LIGHT_PURPLE");
         colorconfig.put("Donators", "DARK_AQUA");
 
         try {
             BufferedReader configuration = new BufferedReader(new FileReader(config));
             while ((line = configuration.readLine()) != null) {
                 line = line.trim();
                 if (!line.startsWith("#") && line.contains("=")) {
                     String[] pair = line.split("=", 2);
                     settings.put(pair[0], pair[1]);
                 }
             }
             BufferedReader colorconfiguration = new BufferedReader(new FileReader(colors));
             while ((line = colorconfiguration.readLine()) != null) {
                 line = line.trim();
                 if (!line.startsWith("#") && line.contains("=")) {
                     String[] pair = line.split("=", 2);
                     colorconfig.put(pair[0], pair[1]);
                 }
             }
         } catch (FileNotFoundException e) {
             // Oh man you're screwed, don't worry I'll save you... using default chat channels.
             logger.warning("[McftChat] Error reading " + e.getLocalizedMessage() + ", using defaults");
         } catch (Exception e) {
             // If you thought you were screwed before, boy have I news for you.
             Logger.getLogger(McftChat.class.getName()).log(Level.SEVERE, null, e);
         }
     }
 
     private String[] unshift(String str, String[] array) {
         String[] newarray = new String[array.length + 1];
         newarray[0] = str;
         for (Integer i = 0; i < array.length; i++) {
             newarray[i + 1] = array[i];
         }
         return newarray;
     }
 
     private String groupPrefix(String groupname, String worldname) {
         String prefix = permissionHandler.getGroupPrefix(worldname, groupname);
         if (prefix == null) {
             prefix = "";
         }
         return prefix;
     }
 
     private String getColor(String color) {
         if (isInt(color)) {
             return color;
         } else {
             if (color.equalsIgnoreCase("a")) {
                 return "10";
             } else if (color.equalsIgnoreCase("b")) {
                 return "11";
             } else if (color.equalsIgnoreCase("c")) {
                 return "12";
             } else if (color.equalsIgnoreCase("d")) {
                 return "13";
             } else if (color.equalsIgnoreCase("e")) {
                 return "14";
             } else if (color.equalsIgnoreCase("f")) {
                 return "15";
             }
             return null;
         }
     }
 
     private boolean isInt(String i) {
         try {
             Integer.parseInt(i);
             return true;
         } catch (NumberFormatException nfe) {
             return false;
         }
     }
 }

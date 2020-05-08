 package com.btbb.figadmin;
 
 import java.util.ArrayList;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 /**
  * Admin plugin for Bukkit.
  * 
  * @author yottabyte
  * @author Serge Humphrey
  */
 
 public class FigAdmin extends JavaPlugin {
 
     public static final Logger log = Logger.getLogger("Minecraft");
 
     Permissions CurrentPermissions = null;
     Database db;
     String maindir = "plugins/FigAdmin/";
     File Settings = new File(maindir + "config.properties");
     ArrayList<EditBan> bannedPlayers;
     private final FigAdminPlayerListener playerListener = new FigAdminPlayerListener(this);
 
     public FileConfiguration config;
     public boolean autoComplete;
     private EditCommand editor;
 
     // NOTE: Event registration should be done in onEnable not here as all
     // events are unregistered when a plugin is disabled
 
     public void setupPermissions() {
         Plugin plugin = this.getServer().getPluginManager().getPlugin("Permissions");
 
         if (CurrentPermissions == null) {
             // Permission plugin already registered
             return;
         }
 
         if (plugin != null) {
             CurrentPermissions = (Permissions) plugin;
         } else {
             log.log(Level.CONFIG, "Permissions plugin is required for this plugin to work. Disabling FigAdmin");
             this.getServer().getPluginManager().disablePlugin(this);
         }
     }
 
     public void onDisable() {
         bannedPlayers = null;
         System.out.println("FigAdmin disabled.");
     }
 
     /**
      * Create a default configuration file from the .jar.
      * 
      * @param name
      */
     public void setupConfig() {
         this.config = getConfig();
         config.options().copyDefaults(true);
         saveConfig();
 
     }
 
     public static boolean validName(String name) {
         return name.length() > 2 && name.length() < 17 && !name.matches("(?i).*[^a-z0-9_].*");
     }
 
     public void onEnable() {
         new File(maindir).mkdir();
 
         setupConfig();
 
         boolean useMysql = getConfig().getBoolean("mysql", false);
         if (useMysql) {
             try {
                 db = new MySQLDatabase(this);
             } catch (Exception e) {
                 log.log(Level.CONFIG, "Ohhh Shit! Can't start MySQL Database!");
                 System.out.println("FigAdmin [Error]: Can't initialize databse.");
                 return;
             }
         } else {
             db = new FlatFileDatabase();
         }
         db.initialize(this);
         bannedPlayers = db.getBannedPlayers();
 
         this.autoComplete = getConfig().getBoolean("auto-complete", true);
         // Register our events
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Highest, this);
         pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
 
         getCommand("editban").setExecutor(editor = new EditCommand(this));
 
         PluginDescriptionFile pdfFile = this.getDescription();
         log.log(Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
     }
 
     public String combineSplit(int startIndex, String[] string, String seperator) {
         StringBuilder builder = new StringBuilder();
 
         for (int i = startIndex; i < string.length; i++) {
             builder.append(string[i]);
             builder.append(seperator);
         }
         builder.deleteCharAt(builder.length() - seperator.length()); // remove
         return builder.toString();
     }
 
     public long parseTimeSpec(String time, String unit) {
         long sec;
         try {
             sec = Integer.parseInt(time) * 60;
         } catch (NumberFormatException ex) {
             return 0;
         }
         if (unit.startsWith("hour"))
             sec *= 60;
         else if (unit.startsWith("day"))
             sec *= (60 * 24);
         else if (unit.startsWith("week"))
             sec *= (7 * 60 * 24);
         else if (unit.startsWith("month"))
             sec *= (30 * 60 * 24);
         else if (unit.startsWith("min"))
             sec *= 1;
         else if (unit.startsWith("sec"))
             sec /= 60;
         return sec;
     }
 
     public String expandName(String Name) {
         if (Name.equals("*"))
             return Name;
         int m = 0;
         String Result = "";
         for (int n = 0; n < getServer().getOnlinePlayers().length; n++) {
             String str = getServer().getOnlinePlayers()[n].getName();
             if (str.matches("(?i).*" + Name + ".*")) {
                 m++;
                 Result = str;
                 if (m == 2) {
                     return null;
                 }
             }
             if (str.equalsIgnoreCase(Name))
                 return str;
         }
         if (m == 1)
             return Result;
         if (m > 1) {
             return null;
         }
         if (m < 1) {
             return Name;
         }
         return Name;
     }
 
     public String formatMessage(String str) {
         String funnyChar = new Character((char) 167).toString();
         str = str.replaceAll("&", funnyChar);
         return str;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
         String commandName = command.getName().toLowerCase();
         String[] trimmedArgs = args;
 
         // sender.sendMessage(ChatColor.GREEN + trimmedArgs[0]);
        if (commandName.equals("reloadka")) {
            return reloadKA(sender);
         }
         if (commandName.equals("unban")) {
             return unBanPlayer(sender, trimmedArgs);
         }
         if (commandName.equals("ban")) {
             return banPlayer(sender, trimmedArgs);
         }
         if (commandName.equals("warn")) {
             return warnPlayer(sender, trimmedArgs);
         }
         if (commandName.equals("kick")) {
             return kickPlayer(sender, trimmedArgs);
         }
         if (commandName.equals("tempban")) {
             return tempbanPlayer(sender, trimmedArgs);
         }
         if (commandName.equals("checkban")) {
             return checkBan(sender, trimmedArgs);
         }
         if (commandName.equals("ipban")) {
             return banPlayer(sender, trimmedArgs, true);
         }
         if (commandName.equals("exportbans")) {
             return exportBans(sender);
         }
 
         return false;
     }
 
     private boolean unBanPlayer(CommandSender sender, String[] args) {
         boolean auth = false;
         Player player = null;
         String kicker = "server";
         if (sender instanceof Player) {
             player = (Player) sender;
             if (Permissions.Security.permission(player, "figadmin.unban"))
                 auth = true;
             kicker = player.getName();
         } else {
             auth = true;
         }
         // Has permission?
         if (!auth)
             return true;
 
         // Has enough arguments?
         if (args.length < 1)
             return false;
 
         String p = args[0];
         if (!validName(p)) {
             sender.sendMessage(formatMessage(getConfig().getString("messages.badPlayerName", "bad player name")));
             return false;
         }
         boolean found = false;
         p = p.toLowerCase();
         for (int i = 0; i < bannedPlayers.size(); i++) {
             EditBan e = bannedPlayers.get(i);
             if (e.name.equals(p)) {
                 found = true;
                 bannedPlayers.remove(i);
                 // Don't break, cycle through all banned players in case player
                 // is banned twice
                 db.removeFromBanlist(e.name);
             }
         }
         if (found) {
             // Log in console
             log.log(Level.INFO, "[FigAdmin] " + kicker + " unbanned player " + p + ".");
 
             String globalMsg = getConfig().getString("messages.unbanMsgGlobal", "test");
             globalMsg = globalMsg.replaceAll("%victim%", p).replaceAll("%player%", kicker);
             // Send a message to unbanner!
             // No point? lol
             // enable when -s is fixed XD
             // sender.sendMessage(formatMessage(globalMsg));
 
             // send a message to everyone!
             this.getServer().broadcastMessage(formatMessage(globalMsg));
         } else {
             // Unban failed
             String kickerMsg = getConfig().getString("messages.unbanMsgFailed", "unban failed");
             kickerMsg = kickerMsg.replaceAll("%victim%", p);
             sender.sendMessage(formatMessage(kickerMsg));
         }
         return true;
     }
 
     private boolean kickPlayer(CommandSender sender, String[] args) {
         boolean auth = false;
         Player player = null;
         String kicker = "server";
         if (sender instanceof Player) {
             player = (Player) sender;
             if (Permissions.Security.permission(player, "figadmin.kick"))
                 auth = true;
             kicker = player.getName();
         } else {
             auth = true;
         }
         // Has permission?
         if (!auth)
             return true;
 
         // Has enough arguments?
         if (args.length < 1) {
             return false;
         }
 
         String p = args[0].toLowerCase();
         // Reason stuff
        String reason = "Global Kick";
         boolean broadcast = true;
 
         if (args.length > 1) {
             /*if(args[1].equalsIgnoreCase("-s")){
             	broadcast = false;
             	reason = combineSplit(2, args, " ");
             }else*/
             reason = combineSplit(1, args, " ");
         }
 
         if (p.equals("*")) {
             if (sender instanceof Player)
                 if (!Permissions.Security.permission(player, "figadmin.kick.all"))
                     return false;
 
             String kickerMsg = getConfig().getString("messages.kickAllMsg");
             kickerMsg = kickerMsg.replaceAll("%player%", kicker);
             kickerMsg = kickerMsg.replaceAll("%reason%", reason);
             log.log(Level.INFO, "[FigAdmin] " + formatMessage(kickerMsg));
 
             // Kick everyone on server
             for (Player pl : this.getServer().getOnlinePlayers()) {
                 pl.kickPlayer(formatMessage(kickerMsg));
                 return true;
             }
         } else if (!validName(p)) {
             sender.sendMessage(formatMessage(getConfig().getString("messages.badPlayerName", "bad player name")));
             return true;
         }
         if (autoComplete)
             p = expandName(p);
         Player victim = this.getServer().getPlayer(p);
         if (victim == null) {
             String kickerMsg = getConfig().getString("messages.kickMsgFailed");
             kickerMsg = kickerMsg.replaceAll("%victim%", p);
             sender.sendMessage(formatMessage(kickerMsg));
             return true;
         }
 
         // Log in console
         log.log(Level.INFO, "[FigAdmin] " + kicker + " kicked player " + p + ". Reason: " + reason);
 
         // Send message to victim
         String kickerMsg = getConfig().getString("messages.kickMsgVictim");
         kickerMsg = kickerMsg.replaceAll("%player%", kicker);
         kickerMsg = kickerMsg.replaceAll("%reason%", reason);
         victim.kickPlayer(formatMessage(kickerMsg));
 
         if (broadcast) {
             // Send message to all players
             String kickerMsgAll = getConfig().getString("messages.kickMsgBroadcast");
             kickerMsgAll = kickerMsgAll.replaceAll("%player%", kicker);
             kickerMsgAll = kickerMsgAll.replaceAll("%reason%", reason);
             kickerMsgAll = kickerMsgAll.replaceAll("%victim%", p);
             this.getServer().broadcastMessage(formatMessage(kickerMsgAll));
         }
         return true;
     }
 
     private boolean banPlayer(CommandSender sender, String[] args) {
         return banPlayer(sender, args, false);
     }
 
     private boolean banPlayer(CommandSender sender, String[] args, boolean ipBan) {
         try {
             boolean auth = false;
             Player player = null;
             String kicker = "server";
             if (sender instanceof Player) {
                 player = (Player) sender;
                 if (Permissions.Security.permission(player, "figadmin.ban"))
                     auth = true;
                 kicker = player.getName();
             } else {
                 auth = true;
             }
             // Has permission?
             if (!auth)
                 return true;
 
             // Has enough arguments?
             if (args.length < 1)
                 return false;
             String p = args[0]; // Get the victim's name
             if (!validName(p)) {
                 sender.sendMessage(formatMessage(getConfig().getString("messages.badPlayerName", "bad player name")));
                 return true;
             }
             if (autoComplete)
                 p = expandName(p); // If the admin has chosen to do so,
                                    // autocomplete the name!
             Player victim = this.getServer().getPlayer(p); // What player is
                                                            // really the victim?
             // Reason stuff
             String reason = "Ban Hammer has Spoken!";
             boolean broadcast = true;
             if (args.length > 1) {
                 /*if(args[1].equalsIgnoreCase("-s")){
                 	broadcast = false;
                 	reason = combineSplit(2, args, " ");
                 }else*/
                 reason = combineSplit(1, args, " ");
             }
             if (isBanned(p)) {
                 String kickerMsg = getConfig().getString("messages.banMsgFailed");
                 kickerMsg = kickerMsg.replaceAll("%victim%", p);
                 sender.sendMessage(formatMessage(kickerMsg));
                 return true;
             }
             EditBan ban = null;
 
             String ip = null;
             if (victim != null) {
                 ip = victim.getAddress().getAddress().getHostAddress();
             }
             if (ipBan) {
                 ban = new EditBan(p, reason, kicker, ip, EditBan.IPBAN);
             } else {
                 ban = new EditBan(p, reason, kicker, ip, EditBan.BAN);
             }
 
             bannedPlayers.add(ban); // Add name to RAM
 
             // Add player to database
             db.addPlayer(ban);
 
             // Log in console
             log.log(Level.INFO, "[FigAdmin] " + kicker + " banned player " + p + ".");
 
             if (victim != null) { // If he is online, kick him with a nice
                                   // message :)
 
                 // Send message to victim
                 String kickerMsg = getConfig().getString("messages.banMsgVictim");
                 kickerMsg = kickerMsg.replaceAll("%player%", kicker);
                 kickerMsg = kickerMsg.replaceAll("%reason%", reason);
                 victim.kickPlayer(formatMessage(kickerMsg));
             }
             // Send message to all players
             if (broadcast) {
                 String kickerMsgAll = getConfig().getString("messages.banMsgBroadcast");
                 kickerMsgAll = kickerMsgAll.replaceAll("%player%", kicker);
                 kickerMsgAll = kickerMsgAll.replaceAll("%reason%", reason);
                 kickerMsgAll = kickerMsgAll.replaceAll("%victim%", p);
                 this.getServer().broadcastMessage(formatMessage(kickerMsgAll));
             }
         } catch (Exception exc) {
             exc.printStackTrace();
         }
         return true;
     }
 
     private boolean tempbanPlayer(CommandSender sender, String[] args) {
         boolean auth = false;
         Player player = null;
         String kicker = "server";
         if (sender instanceof Player) {
             player = (Player) sender;
             if (Permissions.Security.permission(player, "figadmin.tempban"))
                 auth = true;
             kicker = player.getName();
         } else {
             auth = true;
         }
         if (!auth)
             return true;
 
         if (args.length < 3)
             return false;
 
         String p = args[0]; // Get the victim's name
         if (!validName(p)) {
             sender.sendMessage(formatMessage(getConfig().getString("messages.badPlayerName", "bad player name")));
             return true;
         }
         if (autoComplete)
             p = expandName(p); // If the admin has chosen to do so, autocomplete
                                // the name!
         Player victim = this.getServer().getPlayer(p); // What player is really
                                                        // the victim?
         // Reason stuff
         String reason = "Ban Hammer has Spoken!";
         boolean broadcast = true;
 
         if (args.length > 3) {
             /*if(args[1].equalsIgnoreCase("-s")){
             	broadcast = false;
             	reason = combineSplit(2, args, " ");
             }else*/
             reason = combineSplit(3, args, " ");
         }
 
         if (isBanned(p)) {
             String kickerMsg = getConfig().getString("messages.banMsgFailed");
             kickerMsg = kickerMsg.replaceAll("%victim%", p);
             sender.sendMessage(formatMessage(kickerMsg));
             return true;
         }
 
         long tempTime = parseTimeSpec(args[1], args[2]); // parse the time and
                                                          // do other crap below
         if (tempTime == 0)
             return false;
         tempTime = System.currentTimeMillis() / 1000 + tempTime;
         EditBan ban = new EditBan(p, reason, kicker, tempTime, EditBan.BAN);
         bannedPlayers.add(ban); // Add name to RAM
 
         // Add to database
         db.addPlayer(ban);
 
         // Log in console
         log.log(Level.INFO, "[FigAdmin] " + kicker + " tempbanned player " + p + ".");
 
         if (victim != null) { // If he is online, kick him with a nice message
                               // :)
 
             // Send message to victim
             String kickerMsg = getConfig().getString("messages.tempbanMsgVictim");
             kickerMsg = kickerMsg.replaceAll("%player%", kicker);
             kickerMsg = kickerMsg.replaceAll("%reason%", reason);
             victim.kickPlayer(formatMessage(kickerMsg));
         }
         if (broadcast) {
             // Send message to all players
             String kickerMsgAll = getConfig().getString("messages.tempbanMsgBroadcast");
             kickerMsgAll = kickerMsgAll.replaceAll("%player%", kicker);
             kickerMsgAll = kickerMsgAll.replaceAll("%reason%", reason);
             kickerMsgAll = kickerMsgAll.replaceAll("%victim%", p);
             this.getServer().broadcastMessage(formatMessage(kickerMsgAll));
         }
         return true;
     }
 
     private boolean checkBan(CommandSender sender, String[] args) {
         if (args.length == 0) {
             sender.sendMessage("Please specify a player");
             return true;
         }
         String p = args[0];
         if (!validName(p)) {
             sender.sendMessage(formatMessage(getConfig().getString("messages.badPlayerName", "bad player name")));
             return true;
         }
         if (isBanned(p))
             sender.sendMessage(ChatColor.RED + "Player " + p + " is banned.");
         else
             sender.sendMessage(ChatColor.GREEN + "Player " + p + " is not banned.");
         return true;
     }
 
     private boolean warnPlayer(CommandSender sender, String[] args) {
         boolean auth = false;
         Player player = null;
         String kicker = "server";
         if (sender instanceof Player) {
             player = (Player) sender;
             if (Permissions.Security.permission(player, "figadmin.warn"))
                 auth = true;
             kicker = player.getName();
         } else {
             auth = true;
         }
         // Has permission?
         if (!auth)
             return true;
 
         // Has enough arguments?
         if (args.length < 1)
             return false;
 
         String p = args[0]; // Get the victim's name
         if (!validName(p)) {
             sender.sendMessage(formatMessage(getConfig().getString("messages.badPlayerName", "bad player name")));
             return true;
         }
         if (autoComplete)
             p = expandName(p); // If the admin has chosen to do so, autocomplete
                                // the name!
         Player victim = this.getServer().getPlayer(p); // What player is really
                                                        // the victim?
         if (victim == null) {
             sender.sendMessage(formatMessage(getConfig().getString("messages.playerNotOnline", "not online")
                     .replaceAll("%player%", p)));
             return true;
         }
         // Reason stuff
         String reason = "undefined";
         boolean broadcast = true;
 
         if (args.length > 1) {
             /*if(args[1].equalsIgnoreCase("-s")){
             	broadcast = false;
             	reason = combineSplit(2, args, " ");
             }else*/
             reason = combineSplit(1, args, " ");
         }
 
         // Add player to database
         EditBan b = new EditBan(p, reason, kicker, EditBan.WARN);
         db.addPlayer(b);
 
         // Log in console
         log.log(Level.INFO, "[FigAdmin] " + kicker + " warned player " + p + ".");
 
         // Send message to all players
         if (broadcast) {
             this.getServer().broadcastMessage(
                     ChatColor.RED + "Player " + p + " recieved a warning from " + kicker + ":");
             this.getServer().broadcastMessage(ChatColor.GRAY + "  " + reason);
         } else {
             if (victim != null) { // If he is online, kick him with a nice
                                   // message :)
                 victim.sendMessage(ChatColor.RED + "You have recieved a warning from " + kicker + ":");
                 victim.sendMessage(ChatColor.GRAY + "  " + reason);
             }
         }
 
         return true;
     }
 
    private boolean reloadKA(CommandSender sender) {
         boolean auth = false;
         Player player = null;
         String kicker = "server";
         if (sender instanceof Player) {
             player = (Player) sender;
             if (Permissions.Security.permission(player, "figadmin.reload"))
                 auth = true;
             kicker = player.getName();
         } else {
             auth = true;
         }
         if (auth) {
 
             bannedPlayers = db.getBannedPlayers();
             editor.ban = null;
             
             log.log(Level.INFO, "[FigAdmin] " + kicker + " Reloaded player banlist.");
             sender.sendMessage("§2Reloaded banlist.");
             return true;
         }
         return false;
     }
 
     private boolean exportBans(CommandSender sender) {
         boolean auth = false;
         Player player = null;
         if (sender instanceof Player) {
             player = (Player) sender;
             if (Permissions.Security.permission(player, "figadmin.export"))
                 auth = true;
         } else {
             auth = true;
         }
         if (auth) {
             try {
                 BufferedWriter banlist = new BufferedWriter(new FileWriter("banned-players.txt", true));
                 for (int n = 0; n < bannedPlayers.size(); n++) {
                     banlist.write(bannedPlayers.get(n).name);
                     banlist.newLine();
                 }
                 banlist.close();
             } catch (IOException e) {
                 FigAdmin.log.log(Level.SEVERE, "FigAdmin: Couldn't write to banned-players.txt");
             }
             sender.sendMessage("§2Exported banlist to banned-players.txt.");
             return true;
         }
         return false;
     }
 
     private boolean isBanned(String name) {
         name = name.toLowerCase();
         for (int i = 0; i < bannedPlayers.size(); i++) {
             EditBan e = bannedPlayers.get(i);
             if (e.name.equals(name)) {
                 return true;
             }
         }
         return false;
     }
 
 }

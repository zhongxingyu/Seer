 package org.landofordos.ordosname;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 //import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.UUID;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.fusesource.jansi.Ansi.Color;
 
 import com.palmergames.bukkit.towny.Towny;
 import com.palmergames.bukkit.towny.TownyFormatter;
 //import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
 import com.palmergames.bukkit.towny.object.TownyUniverse;
 import com.sk89q.worldguard.bukkit.RegionContainer;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 
 public class OrdosName extends JavaPlugin implements Listener {
     enum inputStarted {
         never, started, finished
     };
 
     // Important plugin objects
     private static Server server;
     private static Logger logger;
     // sql vars
     private String URL;
     private String dbType;
     private String dbUser;
     private String dbPass;
     private String dbTable;
     private Connection connection;
     //
     private boolean verbose;
     private long dbcleanuptime;
     //
     private boolean useTowny;
     Towny towny;
     TownyUniverse townyUniverse;
     //
     private boolean useWorldGuard;
     WorldGuardPlugin worldGuard;
 
     public void onDisable() {
         logger.info("Disabled.");
     }
 
     public void onEnable() {
         // static reference to this plugin and the server
         // plugin = this;
         server = getServer();
         // start the logger
         logger = getLogger();
         // save config to default location if not already there
         this.saveDefaultConfig();
         // register events
         server.getPluginManager().registerEvents(this, this);
         // run configuration startup
         loadConfig();
     }
 
     private boolean createSQL() throws SQLException, ClassNotFoundException {
         Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
         try {
             switch (dbType) {
             case "SQL":
                 statement
                         .executeUpdate("CREATE TABLE "
                                 + dbTable
                                 + " (user VARCHAR( 36 )  NOT NULL UNIQUE PRIMARY KEY, first VARCHAR( 32 ), "
                                 + "last VARCHAR( 32 ), title VARCHAR( 32 ), suffix VARCHAR( 32 ), titleoverridesfirst BIT DEFAULT FALSE, "
                                 + "townytitle VARCHAR( 32 ), townysuffix VARCHAR( 32 ), wgsuffix VARCHAR( 32 ), enabled BIT DEFAULT TRUE, displayname VARCHAR( 128 ), "
                                 + "lastseen VARCHAR( 64 ) NOT NULL);");
                 break;
             case "SQLite":
                 statement
                         .executeUpdate("CREATE TABLE "
                                 + dbTable
                                 + " (user TEXT( 36 )  NOT NULL UNIQUE PRIMARY KEY, first TEXT( 32 ), "
                                 + "last TEXT( 32 ), title TEXT( 32 ), suffix TEXT( 32 ), titleoverridesfirst INTEGER DEFAULT 0, "
                                 + "townytitle TEXT( 32 ), townysuffix TEXT( 32 ), wgsuffix TEXT( 32 ), enabled INTEGER DEFAULT 1, displayname TEXT( 128 ), "
                                 + "lastseen TEXT( 64 ) NOT NULL);");
                 break;
             }
         } catch (SQLException e) {
             logger.info(" SQL Exception: " + e);
             return false;
         }
         return true;
     }
 
     private void connectSQL() throws SQLException, ClassNotFoundException {
         Class.forName("com.mysql.jdbc.Driver");
         connection = DriverManager.getConnection("jdbc:" + URL + "?user=" + dbUser + "&password=" + dbPass);
     }
 
     private void connectSQLite() throws SQLException, ClassNotFoundException {
         Class.forName("org.sqlite.JDBC");
         connection = DriverManager.getConnection("jdbc:sqlite:OrdosName.db");
     }
 
     private int getResultSetNumRows(ResultSet res) {
         try {
             // get row at beginning so as to not affect it
             int originalPlace = res.getRow();
             res.last();
             // Get the row number of the last row which is also the row count
             int rowCount = res.getRow();
             // move row back to original position
             res.absolute(originalPlace);
             return rowCount;
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return -1;
     }
 
     private boolean loadConfig() {
         return loadConfig(null);
     }
 
     private boolean loadConfig(CommandSender sender) {
         FileConfiguration config = this.getConfig();
         // check for changes in the verbose logging var
         if (verbose != config.getBoolean("pluginvars.verbose")) {
             verbose = config.getBoolean("pluginvars.verboselogging");
             if (verbose) {
                 logger.info("Verbose logging enabled.");
                 sender.sendMessage(ChatColor.YELLOW + "Verbose logging now enabled.");
             } else {
                 logger.info("Verbose logging disabled.");
                 sender.sendMessage(ChatColor.YELLOW + "Verbose logging now disabled.");
             }
         }
         // retrieve SQL variables from config
         URL = config.getString("dbvars.URL");
         dbUser = config.getString("dbvars.Username");
         dbPass = config.getString("dbvars.Password");
         dbTable = config.getString("dbvars.tablename");
         if (sender != null) {
             logger.info(sender.getName() + " initiated configuration reload.");
             sender.sendMessage(ChatColor.YELLOW + "Reloading config");
         }
         // Check the database type. This cannot be changed while running, for obvious reasons.
         // Check that no connection has yet been started. If so, load variables.
         if (connection == null && dbType == null) {
             dbType = config.getString("dbvars.database");
             // create database connection - at this time, only SQL and SQLite and supported.
             try {
                 switch (dbType) {
                 case "SQLite":
                     connectSQLite();
                     break;
                 case "SQL":
                     connectSQL();
                     break;
                 default:
                     break;
                 }
             } catch (Exception e1) {
                 logger.log(Level.WARNING, "Database connection using " + dbType + " failed. Please check your configuration.");
                 e1.printStackTrace();
                 getServer().getPluginManager().disablePlugin(this);
                 return false;
             }
             if (connection != null) {
                 logger.info("Database connection established using " + dbType + ".");
             }
         }
         // if connection is not available after initialisation attempt, unload plugin
         if (connection == null) {
             getServer().getPluginManager().disablePlugin(this);
             return false;
         }
         // first-run initialisation
         final boolean firstrun = this.getConfig().getBoolean("pluginvars.firstrun");
         if (firstrun) {
             try {
                 boolean SQLsuccess = createSQL();
                 if (verbose && SQLsuccess) {
                     logger.info("Tables created successfully.");
                 }
             } catch (Exception e1) {
                 logger.log(Level.WARNING, "First-run initialisation failed. Please check your configuration.");
                 e1.printStackTrace();
                 getServer().getPluginManager().disablePlugin(this);
                 return false;
             }
             this.getConfig().set("pluginvars.firstrun", false);
             this.saveConfig();
             logger.info("First-run initialisation complete.");
         }
         // retrieve database cleanup threshold from config, if it has changed
         if (dbcleanuptime != config.getLong("pluginvars.dbcleanuptime")) {
             dbcleanuptime = config.getLong("pluginvars.dbcleanuptime");
             // immediately run database cleanup using new threshold
             if (verbose) {
                 logger.info("New database cleanup threshold (" + dbcleanuptime + ") loaded from config.");
             }
             if (sender != null) {
                 sender.sendMessage(ChatColor.YELLOW + "New database cleanup threshold (" + dbcleanuptime + ") loaded from config.");
             }
             if (dbcleanuptime > 0) {
                 dbcleanup();
             }
         }
         // check for Towny integration, and if so open towny plugin object - if not found, disable the feature
         if (useTowny != config.getBoolean("pluginvars.useTowny")) {
             useTowny = config.getBoolean("pluginvars.useTowny");
             if (useTowny) {
                 Plugin plugin = getServer().getPluginManager().getPlugin("Towny");
                 if (plugin == null || !(plugin instanceof Towny)) {
                     logger.severe("Towny integration was enabled, but Towny could not be found!");
                     sender.sendMessage(ChatColor.RED + "Towny integration enabled, but the plugin was not found!");
                     // the feature is nonessential, so disable it and continue running the plugin.
                     useTowny = false;
                 } else {
                     // cast type
                     towny = (Towny) plugin;
                     townyUniverse = towny.getTownyUniverse();
                     logger.info("Towny integration now enabled.");
                 }
             }
 
         }
         // check for WorldGuard integration, and if so open WG plugin object - if not found, disable the feature
         if (useWorldGuard != config.getBoolean("pluginvars.useWorldGuard")) {
             useWorldGuard = config.getBoolean("pluginvars.useWorldGuard");
             if (useWorldGuard) {
                 Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
                 if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
                     logger.severe("WG integration was enabled, but WG could not be found!");
                     sender.sendMessage(ChatColor.RED + "WG integration enabled, but the plugin was not found!");
                     // the feature is nonessential, so disable it and continue running the plugin.
                     useWorldGuard = false;
                 } else {
                     // cast type
                     worldGuard = (WorldGuardPlugin) plugin;
                     logger.info("WorldGuard integration now enabled.");
                 }
             }
         }
         return true;
     }
 
     // Server.getPlayer(name) is deprecated, but is still useful in certain contexts.
     @SuppressWarnings("deprecation")
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         // get timestamp for DB inserts
         long timestamp = new Date().getTime();
         // String timestamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
         // command functionality
         // ------------- ordosname functionality
         if (cmd.getName().equalsIgnoreCase("ordosname")) {
             if ((args.length < 1) || ((args.length == 1) && (args[0].equalsIgnoreCase("help")))) {
                 sender.sendMessage(ChatColor.YELLOW + "/setfirstname [name] " + ChatColor.WHITE + "- " + ChatColor.DARK_GREEN
                         + "Set first name");
                 sender.sendMessage(ChatColor.YELLOW + "/setlastname [name] " + ChatColor.WHITE + "- " + ChatColor.DARK_GREEN
                         + "Set last name");
                 sender.sendMessage(ChatColor.YELLOW + "/settitle [name] " + ChatColor.WHITE + "- " + ChatColor.DARK_GREEN + "Set title");
                 sender.sendMessage(ChatColor.YELLOW + "/setsuffix [name] " + ChatColor.WHITE + "- " + ChatColor.DARK_GREEN + "Set suffix");
                 sender.sendMessage(ChatColor.YELLOW + "/namereload ");
                 return false;
             }
             // code to reload configuration
             if ((args.length == 1) && (args[0].equalsIgnoreCase("reload")) && (sender.hasPermission("ordosname.admin.reloadconfig"))) {
                 loadConfig(sender);
                 return true;
             }
             // code to check people's names
             if ((args.length > 1) && (args[0].equalsIgnoreCase("namecheck")) && (sender.hasPermission("ordosname.admin.namecheck"))) {
                 // pick up spaced parameters held together by speech marks
                 String nameToCheck = "";
                 String target = null;
                 // boolean object - false represents nameToCheck not started, true nameToCheck in progress, null
                 // nameToCheck ended
                 Boolean nameToCheckstarted = false;
                 for (int i = 0; i < args.length; i++) {
                     if (target == null) {
                         if (args[i].startsWith("\"")) {
                             nameToCheckstarted = true;
                         }
                         if (nameToCheckstarted == true) {
                             nameToCheck += " " + args[i];
                             if (args[i].endsWith("\"")) {
                                 nameToCheckstarted = null;
                             }
                         }
                     }
                 }
                 if (nameToCheckstarted == null) {
                     // trim off the start and end speech marks
                     nameToCheck = nameToCheck.substring(2, nameToCheck.length() - 1);
                 } else {
                     // if the nameToCheck never ENDED, that's bad news.
                     // assume all is well, though, and just chop off the start.
                     if (nameToCheckstarted) {
                         nameToCheck = nameToCheck.substring(2, nameToCheck.length());
                     }
                     // if the nameToCheck never started, assume single word nameToCheck
                     if (!nameToCheckstarted) {
                         nameToCheck = args[1];
                     }
                 }
                 if (server.getPlayer(nameToCheck) != null) {
                     // the server returns a player object when queried
                     Player player = server.getPlayer(nameToCheck);
                     UUID playerUUID = player.getUniqueId();
                     Statement statement;
                     try {
                         statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                         ResultSet RS = statement
                                 .executeQuery("SELECT * FROM " + dbTable + " WHERE user = '" + playerUUID.toString() + "';");
                         if (RS.isBeforeFirst()) {
                             if (!(RS.getBoolean("enabled"))) {
                                 if (verbose) {
                                     sender.sendMessage("");
                                 }
                             } else {
                                 // if there's a result, set the player's name appropriately.
                                 // fetch name objects and append appropriate spacing
                                 String title = RS.getString("title");
                                 String last = RS.getString("last");
                                 String suffix = RS.getString("suffix");
                                 String first;
                                 // does the player's title override their first name, and do they have a title?
                                 if ((title != null) && (RS.getBoolean("titleoverridesfirst"))) {
                                     // if so, we won't be needing firstname.
                                     first = null;
                                 } else {
                                     // if not, we'll need to fetch their first name
                                     first = RS.getString("first");
                                 }
                                 // string of final name to be set
                                 String name = "";
                                 if (title != null) {
                                     name += title + " ";
                                 }
                                 if (first != null) {
                                     name += first + " ";
                                 }
                                 if (last != null) {
                                     name += last + " ";
                                 }
                                 if (suffix != null) {
                                     name += suffix;
                                 }
                                 if (name.endsWith(" ")) {
                                     name = name.substring(0, name.length() - 1);
                                 }
                                 if (name.length() > 0) {
                                     sender.sendMessage(ChatColor.DARK_GREEN + "The name of user " + ChatColor.WHITE + nameToCheck
                                             + ChatColor.DARK_GREEN + " is " + ChatColor.WHITE + name);
                                     return true;
                                 }
                             }
                         }
                     } catch (SQLException e) {
                         e.printStackTrace();
                     }
                 } else {
                     // if the server returned null for that player, try searching it as nickname instead.
                     try {
                         Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                         ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE displayname = '" + nameToCheck
                                 + "';");
                         if (tryRS.isBeforeFirst()) {
                             sender.sendMessage(ChatColor.DARK_GREEN + "The username of " + ChatColor.WHITE + nameToCheck
                                     + ChatColor.DARK_GREEN + " is " + ChatColor.WHITE
                                     + server.getPlayer(UUID.fromString(tryRS.getString("user"))));
                             return true;
                         } else {
                             sender.sendMessage(ChatColor.DARK_GREEN + "No results found.");
                             return true;
                         }
                     } catch (SQLException e) {
                         e.printStackTrace();
                     }
                 }
             }
             // command to re-enable a user's towny suffix
             if (useTowny) {
                 if ((args.length > 0) && (args[0].equalsIgnoreCase("townysuffix")) && (sender.hasPermission("ordosname.suffix.others"))) {
                     if (args.length < 2) {
                         sender.sendMessage(ChatColor.RED + "Incorrect number of arguments specified!");
                         return false;
                     } else {
                         if (!(sender instanceof Player)) {
                             sender.sendMessage("You cannot do this since you are not a player.");
                         } else {
                             Player player = (Player) sender;
                             if ((args.length == 1)
                                     || ((sender.getName().equals(args[1])) && (sender.hasPermission("ordosname.suffix.self")))) {
                                 reloadPlayerTownySuffix(player);
                                 sender.sendMessage(ChatColor.RED + "Your suffix has been set to reflect your town.");
                                 return true;
                             }
                         }
                         if ((sender.hasPermission("ordosname.suffix.others")) && (args.length == 2)) {
                             reloadPlayerTownySuffix(server.getPlayer(args[1]));
                             sender.sendMessage(ChatColor.RED + args[1] + "'s suffix has been set to reflect their town.");
                             return true;
                         }
                         return false;
                     }
                 }
             }
         }
         // -------------
         if (cmd.getName().equalsIgnoreCase("namereload")) {
             if (args.length < 1) {
                 sender.sendMessage(ChatColor.RED + "Incorrect number of arguments specified!");
                 return false;
             } else {
                 if (!(sender instanceof Player)) {
                     sender.sendMessage("You cannot do this since you are not a player.");
                 } else {
                     if ((args.length == 0) || ((sender.getName().equals(args[0])) && (sender.hasPermission("ordosname.reload.self")))) {
                         reloadPlayerName((Player) sender);
                         return true;
                     }
                 }
                 if ((sender.hasPermission("ordosname.reload.others")) && (args.length == 1)) {
                     reloadPlayerName(sender, server.getPlayer(args[0]));
                     return true;
                 }
                 return false;
             }
         }
         // -------------
         if (cmd.getName().equalsIgnoreCase("setfirstname")) {
             if (args.length < 1) {
                 if (!(sender instanceof Player)) {
                     // if there are 0 arguments, clear the title of yourself.
                     // this only works if you are a player
                     sender.sendMessage("You cannot do this since you are not a player.");
                     return false;
                 } else {
                     Player player = (Player) sender;
                     // however if it was a player that issued the command, execute it.
                     // permissions check
                     if (sender.hasPermission("ordosname.name.first.self")) {
                         try {
                             Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                             ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '"
                                     + player.getUniqueId().toString() + "';");
                             if (tryRS.isBeforeFirst()) {
                                 // if there's a result, update the table instead of inserting.
                                 statement.executeUpdate("UPDATE " + dbTable + " SET first = NULL, lastseen = '" + timestamp
                                         + "' WHERE user= '" + player.getUniqueId().toString() + "';");
                             } else {
                                 // If no result was returned then the user has not been added before.
                                 // Use INSERT instead of update to create the record
                                 statement.executeUpdate("INSERT INTO " + dbTable
                                         + " (user, first, last, lastseen) VALUES ('"
                                         + player.getUniqueId().toString() + "', NULL, '" + sender.getName() + "', '" + timestamp + "');");
                                 logger.info("Database entry was created for " + sender.getName());
                             }
                             if (statement != null) {
                                 statement.close();
                             }
                         } catch (SQLException e) {
                             e.printStackTrace();
                         }
                         reloadPlayerName((Player) sender);
                         return true;
                     } else {
                         // "You don't have permission!"
                     }
                 }
                 return false;
             } else {
                 if (args.length == 1) {
                     if (!(sender instanceof Player)) {
                         // if only one arg specified take target to be self, but this only works if you are a player
                         sender.sendMessage("You cannot do this since you are not a player.");
                         return false;
                     } else {
                         Player player = (Player) sender;
                         // however if it was a player that issued the command, execute it.
                         // permissions check
                         if (sender.hasPermission("ordosname.name.first.self")) {
                             try {
                                 Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                                 ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '"
                                         + player.getUniqueId().toString() + "';");
                                 if (tryRS.isBeforeFirst()) {
                                     // if there's a result, update the table instead of inserting.
                                     statement.executeUpdate("UPDATE " + dbTable + " SET first = '" + args[0] + "', lastseen = '"
                                             + timestamp + "' WHERE user = '" + player.getUniqueId().toString() + "';");
                                 } else {
                                     // If no result was returned then the user has not been added before.
                                     // Use INSERT instead of update to create the record.
                                     statement.executeUpdate("INSERT INTO " + dbTable
                                             + " (user, first, lastseen) VALUES ('"
                                             + player.getUniqueId().toString() + "', '" + args[0] + "', '" + timestamp + "');");
                                     logger.info("Database entry was created for " + sender.getName());
                                 }
                                 if (statement != null) {
                                     statement.close();
                                 }
                             } catch (SQLException e) {
                                 e.printStackTrace();
                             }
                             reloadPlayerName(player);
                             return true;
                         } else {
                             // "You don't have permission!"
                         }
                     }
                 }
                 if (args.length == 2) {
                     // permission check
                     if (sender.hasPermission("ordosname.name.first.others")) {
                         // this adds the ability to clear people's first names
                         String name;
                         if (args[0].equals("\"\"")) {
                             name = "NULL";
                         } else {
                             name = "'" + args[0] + "'";
                         }
                         String targetUUID = server.getPlayer(args[1]).getUniqueId().toString();
                         String target = server.getPlayer(args[1]).getName();
                         try {
                             Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                             ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '" + targetUUID + "';");
                             if (tryRS.isBeforeFirst()) {
                                 // if there's a result, update the table instead of inserting.
                                 statement.executeUpdate("UPDATE " + dbTable + " SET first = " + name + ", lastseen = '" + timestamp
                                         + "' WHERE user= '" + targetUUID + "';");
                             } else {
                                 // If no result was returned then the user has not been added before.
                                 // Use INSERT instead of update to create the record.
                                 statement.executeUpdate("INSERT INTO " + dbTable
                                         + " (user, first, last, lastseen) VALUES ('" + targetUUID + "', '" + name + 
                                         "', '" + target + "', '" + timestamp + "');");
                                 logger.info("Database entry was created for " + args[1]);
                             }
                             if (statement != null) {
                                 statement.close();
                             }
                         } catch (SQLException e) {
                             e.printStackTrace();
                         }
                         reloadPlayerName(sender, server.getPlayer(args[1]));
                         return true;
                     } else {
                         // "You don't have permission!"
                     }
                 }
             }
         }
         // -------------
         if (cmd.getName().equalsIgnoreCase("setlastname")) {
             if (args.length < 1) {
                 if (!(sender instanceof Player)) {
                     // if there are 0 arguments, clear the title of yourself.
                     // #TODO: Collapse this into the 1-argument code block
                     // this only works if you are a player
                     sender.sendMessage("You cannot do this since you are not a player.");
                     return false;
                 } else {
                     Player player = (Player) sender;
                     // however if it was a player that issued the command, execute it.
                     // permissions check
                     if (sender.hasPermission("ordosname.name.last.self")) {
                         try {
                             Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                             ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '"
                                     + player.getUniqueId().toString() + "';");
                             if (tryRS.isBeforeFirst()) {
                                 // if there's a result, update the table instead of inserting.
                                 // since the last name of a player is never null, we reset it to their actual username
                                 // instead
                                 statement.executeUpdate("UPDATE " + dbTable + " SET last = '" + sender.getName() + "', lastseen = '"
                                         + timestamp + "' WHERE user = '" + player.getUniqueId().toString() + "';");
                             } else {
                                 // If no result was returned then the user has not been added before.
                                 // Use INSERT instead of update to create the record
                                 // instead
                                 statement.executeUpdate("INSERT INTO " + dbTable
                                         + " (user, last, lastseen) VALUES ('" + player.getUniqueId().toString()
                                         + "', '" + sender.getName() + "', '" + timestamp + "');");
                                 logger.info("Database entry was created for " + sender.getName());
                             }
                             if (statement != null) {
                                 statement.close();
                             }
                         } catch (SQLException e) {
                             e.printStackTrace();
                         }
                         reloadPlayerName(player);
                         return true;
                     } else {
                         // "You don't have permission!"
                     }
                 }
                 return false;
             } else {
                 if (args.length == 1) {
                     if (!(sender instanceof Player)) {
                         // if only one arg specified take target to be self, but this only works if you are a player
                         sender.sendMessage("You cannot do this since you are not a player.");
                         return false;
                     } else {
                         Player player = (Player) sender;
                         // however if it was a player that issued the command, execute it.
                         // permissions check
                         if (sender.hasPermission("ordosname.name.last.self")) {
                             try {
                                 Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                                 ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '"
                                         + player.getUniqueId().toString() + "';");
                                 if (tryRS.isBeforeFirst()) {
                                     // if there's a result, update the table instead of inserting.
                                     statement.executeUpdate("UPDATE " + dbTable + " SET last = '" + args[0] + "', lastseen = '" + timestamp
                                             + "' WHERE user = '" + player.getUniqueId().toString() + "';");
                                 } else {
                                     // If no result was returned then the user has not been added before.
                                     // Use INSERT instead of update to create the record.
                                     statement.executeUpdate("INSERT INTO " + dbTable
                                             + " (user, last, lastseen) VALUES ('"
                                             + player.getUniqueId().toString() + "', '" + args[0] + "', '" + timestamp + "');");
                                     logger.info("Database entry was created for " + sender.getName());
                                 }
                                 if (statement != null) {
                                     statement.close();
                                 }
                             } catch (SQLException e) {
                                 e.printStackTrace();
                             }
                             reloadPlayerName((Player) sender);
                             return true;
                         } else {
                             // "You don't have permission!"
                         }
                     }
                 }
                 if (args.length == 2) {
                     // permission check
                     if (sender.hasPermission("ordosname.name.last.others")) {
                         // this adds the ability to clear people's first names
                         String name;
                         if (args[0].equals("\"\"")) {
                             // if trying to clear their last name, reset it to their username.
                             name = "'" + args[1] + "'";
                         } else {
                             name = "'" + args[0] + "'";
                         }
                         if (server.getPlayer(args[1]) != null) {
                             String targetUUID = server.getPlayer(args[1]).getUniqueId().toString();
                             try {
                                 Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                                 ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '" + targetUUID + "';");
                                 if (tryRS.isBeforeFirst()) {
                                     // if there's a result, update the table instead of inserting.
                                     statement.executeUpdate("UPDATE " + dbTable + " SET last = " + name + ", lastseen = '" + timestamp
                                             + "' WHERE user= '" + targetUUID + "';");
                                 } else {
                                     // If no result was returned then the user has not been added before.
                                     // Use INSERT instead of update to create the record.
                                     statement.executeUpdate("INSERT INTO " + dbTable
                                             + " (user, last, lastseen) VALUES ('" + targetUUID + "', " + name + ", '" + timestamp + "');");
                                     logger.info("Database entry was created for " + args[1]);
                                 }
                                 if (statement != null) {
                                     statement.close();
                                 }
                             } catch (SQLException e) {
                                 e.printStackTrace();
                             }
                             reloadPlayerName(sender, server.getPlayer(args[1]));
                             return true;
                         } else {
                             // No player found!
                             sender.sendMessage(Color.RED + "No player found with name " + args[1]);
                         }
                     } else {
                         // "You don't have permission!"
                     }
                 }
             }
         }
         // -------------
         if (cmd.getName().equalsIgnoreCase("settitle")) {
             if (args.length < 1) {
                 if (!(sender instanceof Player)) {
                     // if there are 0 arguments, clear the title of yourself.
                     // this only works if you are a player
                     sender.sendMessage("You cannot do this since you are not a player.");
                     return false;
                 } else {
                     // however if it was a player that issued the command, execute it.
                     // permissions check
                     if (sender.hasPermission("ordosname.name.title.self")) {
                         try {
                             Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                             ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '"
                                     + ((Player) sender).getUniqueId() + "';");
                             if (tryRS.isBeforeFirst()) {
                                 // if there's a result, update the table instead of inserting.
                                 statement.executeUpdate("UPDATE " + dbTable + " SET title = NULL, titleoverridesfirst = 0, "
                                         + "lastseen = '" + timestamp + "' WHERE user= '"
                                         + ((Player) sender).getUniqueId() + "';");
                             } else {
                                 // If no result was returned then the user has not been added before.
                                 // Use INSERT instead of update to create the record
                                 statement.executeUpdate("INSERT INTO " + dbTable
                                         + " (user, title, titleoverridesfirst, last, lastseen) VALUES ('"
                                         + ((Player) sender).getUniqueId() + "', NULL, FALSE, '" + sender.getName() + "', '"
                                         + timestamp + "');");
                                 logger.info("Database entry was created for " + sender.getName());
                             }
                             if (statement != null) {
                                 statement.close();
                             }
                         } catch (SQLException e) {
                             e.printStackTrace();
                         }
                         reloadPlayerName((Player) sender);
                         return true;
                     } else {
                         // "You don't have permission!"
                     }
                 }
                 return false;
             } else {
                 String title = "";
                 String target = null;
                 // now an enum var to avoid NPEs.
                 inputStarted titlestarted = inputStarted.never;
                 for (int i = 0; i < args.length; i++) {
                     if (target == null) {
                         if (args[i].startsWith("\"")) {
                             titlestarted = inputStarted.started;
                         }
                         if (titlestarted == inputStarted.started) {
                             title += " " + args[i];
                             if (args[i].endsWith("\"")) {
                                 titlestarted = inputStarted.finished;
                             }
                         }
                         if ((titlestarted == null) && (i < (args.length - 1))) {
                             target = args[i + 1];
                         }
                     }
                 }
                 if (titlestarted == inputStarted.started) {
                     return false;
                     // if the input never finished, something went wrong.
                 }
                 if ((titlestarted == inputStarted.finished) && (title.length() >= 2)) {
                     // trim off the start and end speech marks
                     title = title.substring(2, title.length() - 1);
                 }
                 // if the title never started, assume single word title and pick a target if it was specified.
                 if (titlestarted == inputStarted.never) {
                     title = args[0];
                     if (args.length > 1) {
                         target = args[1];
                     }
                 }
                 if (target == null) {
                     if (!(sender instanceof Player)) {
                         // if only one arg specified take target to be self, but this only works if you are a player
                         sender.sendMessage("You cannot do this since you are not a player.");
                         return false;
                     } else {
                         // however if it was a player that issued the command, execute it.
                         // permissions check
                         if (sender.hasPermission("ordosname.name.title.self")) {
                             try {
                                 Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                                 ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '"
                                         + ((Player) sender).getUniqueId() + "';");
                                 if (tryRS.isBeforeFirst()) {
                                     // if there's a result, update the table instead of inserting.
                                     statement.executeUpdate("UPDATE " + dbTable + " SET title = '" + title
                                             + "', lastseen = '" + timestamp + "' WHERE user= '"
                                             + ((Player) sender).getUniqueId() + "';");
                                 } else {
                                     // If no result was returned then the user has not been added before.
                                     // Use INSERT instead of update to create the record.
                                     statement.executeUpdate("INSERT INTO " + dbTable
                                             + " (user, title, last, lastseen) VALUES ('" + ((Player) sender).getUniqueId()
                                             + "', '" + title + "', '" + sender.getName() + "', '" + timestamp + "');");
                                     logger.info("Database entry was created for " + sender.getName());
                                 }
                                 if (statement != null) {
                                     statement.close();
                                 }
                             } catch (SQLException e) {
                                 e.printStackTrace();
                             }
                             reloadPlayerName((Player) sender);
                             return true;
                         } else {
                             // "You don't have permission!"
                         }
                     }
                 } else {
                     // permission check
                     if (sender.hasPermission("ordosname.name.title.others")) {
                         try {
                             String targetUUID = server.getPlayer(target).getUniqueId().toString();
                             Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                             ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '" + targetUUID + "';");
                             if (tryRS.isBeforeFirst()) {
                                 // if there's a result, update the table instead of inserting.
                                 statement.executeUpdate("UPDATE " + dbTable + " SET title = '" + title
                                         + "', lastseen = '" + timestamp + "' WHERE user= '" + targetUUID + "';");
                             } else {
                                 // If no result was returned then the user has not been added before.
                                 // Use INSERT instead of update to create the record.
                                 statement.executeUpdate("INSERT INTO " + dbTable + " (user, title, last, lastseen) VALUES ('"
                                         + targetUUID + "', '" + title + "', '" + target + "', '"
                                         + timestamp + "');");
                                 logger.info("Database entry was created for " + target);
                             }
                             if (statement != null) {
                                 statement.close();
                             }
                         } catch (SQLException e) {
                             e.printStackTrace();
                         }
                         reloadPlayerName(server.getPlayer(target));
                         return true;
                     } else {
                         // "You don't have permission!"
                     }
                 }
             }
         }
         // -------------
         if (cmd.getName().equalsIgnoreCase("setsuffix")) {
             if (args.length < 1) {
                 if (!(sender instanceof Player)) {
                     // if there are 0 arguments, clear the title of yourself.
                     // this only works if you are a player
                     sender.sendMessage("You cannot do this since you are not a player.");
                     return false;
                 } else {
                     // however if it was a player that issued the command, execute it.
                     // permissions check
                     if (sender.hasPermission("ordosname.name.suffix.self")) {
                         try {
                             Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                             ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '"
                                     + ((Player) sender).getUniqueId() + "';");
                             if (tryRS.isBeforeFirst()) {
                                 // if there's a result, update the table instead of inserting.
                                 statement.executeUpdate("UPDATE " + dbTable + " SET suffix = NULL, lastseen = '"
                                         + timestamp + "' WHERE user= '" + ((Player) sender).getUniqueId() + "';");
                             } else {
                                 // If no result was returned then the user has not been added before.
                                 // Use INSERT instead of update to create the record
                                 statement.executeUpdate("INSERT INTO " + dbTable
                                         + " (user, last, suffix, lastseen) VALUES ('"
                                         + ((Player) sender).getUniqueId() + "', '" + sender.getName() + "', '" + timestamp
                                         + "');");
                                 logger.info("Database entry was created for " + sender.getName());
                             }
                             if (statement != null) {
                                 statement.close();
                             }
                         } catch (SQLException e) {
                             e.printStackTrace();
                         }
                         reloadPlayerName((Player) sender);
                         return true;
                     } else {
                         // "You don't have permission!"
                     }
                 }
                 return false;
             } else {
                 String suffix = "";
                 String target = null;
                 // now an enum object to avoid NPEs.
                 inputStarted suffixstarted = inputStarted.never;
                 for (int i = 0; i < args.length; i++) {
                     if (target == null) {
                         if (args[i].startsWith("\"")) {
                             suffixstarted = inputStarted.started;
                         }
                         if (suffixstarted == inputStarted.started) {
                             suffix += " " + args[i];
                             if (args[i].endsWith("\"")) {
                                 suffixstarted = inputStarted.finished;
                             }
                         }
                         // if the input has finished and the count var has reached the end of the args, set the title
                         // and end the loop.
                         if ((suffixstarted == inputStarted.finished) && (i < (args.length - 1))) {
                             target = args[i + 1];
                         }
                     }
                 }
                 if (suffixstarted == inputStarted.started) {
                     return false;
                     // if the input never finished, something went wrong.
                 }
                 if ((suffixstarted == inputStarted.finished) && (suffix.length() >= 2)) {
                     // trim off the start and end speech marks
                     suffix = suffix.substring(2, suffix.length() - 1);
                 }
                 // if the suffix never started, assume single word suffix and pick a target if it was specified.
                 if (suffixstarted == inputStarted.never) {
                     suffix = args[0];
                     if (args.length > 1) {
                         target = args[1];
                     }
                 }
                 if (target == null) {
                     if (!(sender instanceof Player)) {
                         // if only one arg specified take target to be self, but this only works if you are a player
                         sender.sendMessage("You cannot do this since you are not a player.");
                         return false;
                     } else {
                         // however if it was a player that issued the command, execute it.
                         // permissions check
                         if (sender.hasPermission("ordosname.name.suffix.self")) {
                             try {
                                 Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                                 ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '"
                                         + ((Player) sender).getUniqueId() + "';");
                                 if (tryRS.isBeforeFirst()) {
                                     // if there's a result, update the table instead of inserting.
                                     statement.executeUpdate("UPDATE " + dbTable + " SET suffix = '" + suffix + "', lastseen = '"
                                             + timestamp + "' WHERE user= '" + ((Player) sender).getUniqueId() + "';");
                                 } else {
                                     // If no result was returned then the user has not been added before.
                                     // Use INSERT instead of update to create the record.
                                     statement.executeUpdate("INSERT INTO " + dbTable
                                             + " (user, suffix, last, lastseen) VALUES ('"
                                             + ((Player) sender).getUniqueId() + "', '" + suffix + "', '" + sender.getName() + "', '"
                                             + timestamp + "');");
                                     logger.info("Database entry was created for " + sender.getName());
                                 }
                                 if (statement != null) {
                                     statement.close();
                                 }
                             } catch (SQLException e) {
                                 e.printStackTrace();
                             }
                             reloadPlayerName((Player) sender);
                             return true;
                         } else {
                             // "You don't have permission!"
                         }
                     }
                 } else {
                     // permission check
                     if (sender.hasPermission("ordosname.name.suffix.others")) {
                         if (server.getPlayer(target) != null) {
                             String targetUUID = server.getPlayer(target).getUniqueId().toString();
                             try {
                                 Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                                 ResultSet tryRS = statement.executeQuery("SELECT user FROM " + dbTable + " WHERE user = '" + targetUUID + "';");
                                 if (tryRS.isBeforeFirst()) {
                                     // if there's a result, update the table instead of inserting.
                                     statement.executeUpdate("UPDATE " + dbTable + " SET suffix = '" + suffix + "', lastseen = '" + timestamp
                                             + "' WHERE user = '" + targetUUID + "';");
                                 } else {
                                     // If no result was returned then the user has not been added before.
                                     // Use INSERT instead of update to create the record.
                                     statement.executeUpdate("INSERT INTO " + dbTable
                                             + " (user, suffix, last, lastseen) VALUES ('" + targetUUID + "', '" + suffix
                                             + "', '" + target + "', '" + timestamp + "');");
                                     logger.info("Database entry was created for " + target);
     
                                 }
                                 if (statement != null) {
                                     statement.close();
                                 }
                             } catch (SQLException e) {
                                 e.printStackTrace();
                             }
                             reloadPlayerName(server.getPlayer(target));
                             return true;
                         } else {
                             sender.sendMessage(Color.RED + "No player found with name " + target);
                         }
                     } else {
                         // "You don't have permission!"
                     }
                 }
             }
         }
 
         return false;
     }
 
     private void dbcleanup() {
         // manual iteration through, checking datediffs
         try {
             Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
             ResultSet RS = statement.executeQuery("SELECT * FROM " + dbTable + ";");
             // check each result for a datestamp exceeding maximum age, and delete if necessary
             if (RS.isBeforeFirst()) {
                 int numDeleted = 0;
                 final long now = new Date().getTime();
                 while (RS.next()) {
                     final long lastSeen = RS.getLong("lastseen");
                     long days = TimeUnit.DAYS.convert(now - lastSeen, TimeUnit.MILLISECONDS);
                     if (days > dbcleanuptime) {
                         statement.executeUpdate("DELETE FROM " + dbTable + " WHERE user = " + RS.getString("user") + ");");
                         numDeleted++;
                     }
                 }
                 logger.info("Deleted " + numDeleted + " expired records.");
             } else {
                 logger.info("Deleted 0 expired records.");
             }
             logger.info("Database cleanup complete.");
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     public void reloadPlayerName(CommandSender sender, Player player) {
         Statement statement;
         try {
             statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
             ResultSet RS = statement.executeQuery("SELECT * FROM " + dbTable + " WHERE user = '" + player.getUniqueId().toString() + "';");
             if (RS.isBeforeFirst()) {
                 if (!(RS.getBoolean("enabled"))) {
                     sender.sendMessage(ChatColor.RED + "Data was found, but ENABLED was flagged FALSE");
                 } else {
                     if (player != null) {
                         // fetch name objects and append appropriate spacing
                         String title = RS.getString("title");
                         String first = RS.getString("first");
                         String last = RS.getString("last");
                         String suffix = RS.getString("suffix");
                         
                         //plugin fields
                         String townytitle = null;
                         String townysuffix = null;
                         String wgsuffix = null;
                         if (useTowny) {
                             townytitle = RS.getString("townytitle");
                             townysuffix = RS.getString("townysuffix");                            
                         }
                         if (useWorldGuard) {
                             wgsuffix = RS.getString("wgsuffix");                            
                         }
                         // does the player's title override their first name, and do they have a title?
                         if ((title != null) && (RS.getBoolean("titleoverridesfirst"))) {
                             // if so, we won't be needing firstname.
                             first = null;
                         }
                         // string of final name to be set
                         String name = "";
                         // having a title set overrides towny title
                         if (title != null) {
                             name += title + " ";
                         } else if (townytitle != null) {
                             name += townytitle + " ";                            
                         }
                         
                         if (first != null) {
                             name += first + " ";
                         }
                         
                         if (last != null) {
                             name += last + " ";
                         }
                         
                         // having a suffix set overrides towny suffix
                         if (suffix != null) {
                             name += suffix;
                         // having a towny suffix overrides WG suffix
                         } else if (townysuffix != null) {
                             name += townysuffix;
                         } else if (wgsuffix != null) {
                             name += wgsuffix;
                         }
                         
                         name = name.trim();
                         if (name.length() < 1) {
                             sender.sendMessage(ChatColor.RED + "Data was found, but all fields were NULL");
                         } else {
                             sender.sendMessage(ChatColor.RED + "Player " + player.getName() + "'s name set to " + name);
                             recordDisplayName(player.getUniqueId().toString(), name);
                             player.setDisplayName(name);
                         }
                     }
                 }
             } else {
                 // If no result was returned then the user has no record. Return an error.
                 sender.sendMessage(ChatColor.RED + "No data found for player " + player.getName());
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     public void reloadPlayerName(Player player) {
         reloadPlayerName(player, player);
     }
 
     public void reloadPlayerTownySuffix(Player player) {
         // this method generates and applies a suffix to a player based on what town they are in.
         // it assumes that useTowny is TRUE and thus that towny is a valid instance of the Towny plugin
         // try and fetch the name of the town this player is in
         // -----------
         // get timestamp for DB inserts
         Object timestamp = new java.sql.Timestamp((new Date()).getTime());
         String townname = null;
         try {
             // check whether the player is in a town - this avoids Towny throwing a NotRegisteredException when I try to
             // get a non-existent town name
             if (TownyUniverse.getDataSource().getResident(player.getName()).hasTown()) {
                 // if yes, set townname to whatever it should be
                 townname = TownyUniverse.getDataSource().getResident(player.getName()).getTown().getName();
             } else {
                 // if they aren't registered to a town, reset their suffix
                 if (verbose) {
                     logger.info("Player " + player.getName() + " does not belong to a town. Resetting suffix.");
                     try {
                         Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                         ResultSet RS = statement.executeQuery("SELECT townysuffix FROM " + dbTable + " WHERE user = '"
                                 + player.getUniqueId().toString() + "';");
                         if (RS.isBeforeFirst()) {
                             // if the user is already in the database, update their record
                             statement.executeUpdate("UPDATE " + dbTable + " SET townysuffix = NULL WHERE user= '" + player.getUniqueId() + "';");
                         } else {
                             // if the user is not already in the database, insert a new record
                             statement.executeQuery("INSERT INTO " + dbTable
                                     + " (user, last, townysuffix, lastseen) VALUES ('"
                                     + player.getUniqueId().toString() + "', '" + player.getName() + "', NULL"
                                     + ", '" + timestamp + "');");
                             logger.info("Database entry was created for " + player.getName());
                         }
                     } catch (SQLException e1) {
                         e1.printStackTrace();
                     }
                     return;
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         // if the previous code returns a result, apply appropriate formatting.
         if (townname != null) {
             townname = "of " + townname;
             // then query for their previous suffix - if it is different, then update the record
             try {
                 Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                 ResultSet RS = statement.executeQuery("SELECT townysuffix FROM " + dbTable + " WHERE user = '" + player.getUniqueId().toString()
                         + "';");
                 // check to see if data has changed
                 if (RS.isBeforeFirst()) {
                     // if the user is already in the database, update their suffix if it differs.
                     String recordedSuffix = RS.getString("townysuffix");
                     if (RS.wasNull()) {
                         recordedSuffix = "";
                     }
                     if (!(recordedSuffix.equals(townname))) {
                         if (verbose) {
                             logger.info("Assigning new suffix '" + townname + "' to player " + player.getName());
                         }
                         statement.executeUpdate("UPDATE " + dbTable + " SET townysuffix = '" + townname + "' WHERE user= '"
                                 + player.getUniqueId() + "';");
                         return;
                     } else {
                         // if they don't differ then nothing else is required.
                         if (verbose) {
                             logger.info("Attempted to add suffix to player " + player.getName() + " but their suffix is already correct.");
                             return;
                         }
                     }
                 } else {
                     // if the user is not already in the database, insert a new record with their username (so that the
                     // suffix doesn't look stupid)
                     statement.executeQuery("INSERT INTO " + dbTable + " (user, last, townysuffix, lastseen) VALUES ('"
                             + player.getUniqueId() + "', '" + player.getName() + "', "
                             + townname + ", '" + timestamp + "');");
                     logger.info("Database entry was created for " + player.getName());
                 }
             } catch (SQLException e) {
                 e.printStackTrace();
             }
         } else {
             // if the townname returned was null (or if townname was never set, for whatever reason) go ahead and assume
             // the player is not in a town
             if (verbose) {
                 logger.info("Player " + player.getName() + " does not belong to a town. Resetting suffix.");
                 try {
                     Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                     ResultSet RS = statement.executeQuery("SELECT townysuffix FROM " + dbTable + " WHERE user = '"
                             + player.getUniqueId().toString() + "';");
                     if (RS.isBeforeFirst()) {
                         // if the user is already in the database, update their record
                         statement.executeUpdate("UPDATE " + dbTable + " SET townysuffix = NULL WHERE user= '" + player.getUniqueId() + "';");
                     } else {
                         // if the user is not already in the database, insert a new record
                         statement.executeQuery("INSERT INTO " + dbTable
                                 + " (user, last, townysuffix, lastseen) VALUES ('" + player.getUniqueId().toString()
                                 + "', '" + player.getName() + "', NULL"
                                 + ", '" + timestamp + "');");
                         logger.info("Database entry was created for " + player.getName());
                     }
                 } catch (SQLException e1) {
                     e1.printStackTrace();
                 }
                 return;
             }
         }
     }
 
     public void reloadPlayerTownyTitle(Player player) {
         // this method generates and applies a title to a player based on what town they are in.
         // it assumes that useTowny is TRUE and thus that towny is a valid instance of the Towny plugin
         // try and fetch the name of the town this player is in
         // -----------
         // get timestamp for DB inserts
         Object timestamp = new java.sql.Timestamp((new Date()).getTime());
         String towntitle = null;
         try {
             // check whether the player has a town - avoids NPEs
             if (TownyUniverse.getDataSource().getResident(player.getName()).hasTown()) {
                 // if yes, set title to whatever it should be
                 // NOTE: DO NOT USE Resident.getTitle() - that's not right
                 towntitle = TownyFormatter.getNamePrefix(TownyUniverse.getDataSource().getResident(player.getName()));
                 if (towntitle.trim().length() < 1) {
                     towntitle = null;
                 }
             } else {
                 // if they aren't registered to a town, reset their suffix
                 if (verbose) {
                     logger.info("Player " + player.getName() + " has no title. Resetting title.");
                     try {
                         Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                         ResultSet RS = statement.executeQuery("SELECT townytitle FROM " + dbTable + " WHERE user = '"
                                 + player.getUniqueId().toString() + "';");
                         if (RS.isBeforeFirst()) {
                             // if the user is already in the database, update their record
                             statement.executeUpdate("UPDATE " + dbTable + " SET townytitle = NULL WHERE user= '" + player.getUniqueId() + "';");
                         } else {
                             // if the user is not already in the database, insert a new record
                             statement.executeQuery("INSERT INTO " + dbTable
                                     + " (user, last, townytitle, lastseen) VALUES ('" + player.getUniqueId().toString()
                                     + "', '" + player.getName() + "', NULL, '" + timestamp + "');");
                             logger.info("Database entry was created for " + player.getName());
                         }
                     } catch (SQLException e1) {
                         e1.printStackTrace();
                     }
                     return;
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         // if the previous code returns a result, apply appropriate formatting.
         if (towntitle != null) {
             towntitle = towntitle.trim();
             // then query for their previous suffix - if it is different, then update the record
             try {
                 Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                 ResultSet RS = statement.executeQuery("SELECT suffix FROM " + dbTable + " WHERE user = '" + player.getUniqueId().toString()
                         + "';");
                 // check to see if data has changed
                 if (RS.isBeforeFirst()) {
                     // if the user is already in the database, update their suffix if it differs.
                     String recordedSuffix = RS.getString("townytitle");
                     if (RS.wasNull()) {
                         recordedSuffix = "";
                     }
                     if ((recordedSuffix.equals("")) || (!(recordedSuffix.equals(towntitle)))) {
                         if (verbose) {
                             logger.info("Assigning new title '" + towntitle + "' to player " + player.getName());
                         }
                         statement.executeUpdate("UPDATE " + dbTable + " SET townytitle = '" + towntitle + "' WHERE user = '"
                                 + player.getUniqueId() + "';");
                         return;
                     } else {
                         // if they don't differ then nothing else is required.
                         if (verbose) {
                             logger.info("Attempted to add title to player " + player.getName() + " but their title is already correct.");
                             return;
                         }
                     }
                 } else {
                     // if the user is not already in the database, insert a new record with their username (so that the
                     // suffix doesn't look stupid)
                     statement.executeQuery("INSERT INTO " + dbTable + " (user, last, townytitle, lastseen) VALUES ('"
                             + player.getUniqueId() + "', '" + player.getName() + "', '" + towntitle + "', '" + timestamp + "');");
                     logger.info("Database entry was created for " + player.getName());
                 }
             } catch (SQLException e) {
                 e.printStackTrace();
             }
         } else {
             // if the townname returned was null (or if townname was never set, for whatever reason) go ahead and assume
             // the player is not in a town
             if (verbose) {
                 logger.info("Player " + player.getName() + "'s title returned NULL. Resetting title.");
                 try {
                     Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                     ResultSet RS = statement.executeQuery("SELECT townytitle FROM " + dbTable + " WHERE user = '"
                             + player.getUniqueId().toString() + "';");
                     if (RS.isBeforeFirst()) {
                         // if the user is already in the database, update their record
                         statement.executeUpdate("UPDATE " + dbTable + " SET townytitle = NULL WHERE user = '" + player.getUniqueId() + "';");
                     } else {
                         // if the user is not already in the database, insert a new record
                         statement.executeQuery("INSERT INTO " + dbTable
                                 + " (user, last, townytitle, lastseen) VALUES ('" + player.getUniqueId().toString()
                                 + "', '" + player.getName() + "', '" + towntitle + "', '" + timestamp + "');");
                         logger.info("Database entry was created for " + player.getName());
                     }
                 } catch (SQLException e1) {
                     e1.printStackTrace();
                 }
                 return;
             }
         }
     }
 
     public void reloadPlayerWGSuffix(Player player) {
         // this method generates and applies a suffix to a player based on the regions they own
         // the top-level (no parent) region under which they own the most child regions is their master region.
         //
         // it assumes that useTowny is TRUE and thus that towny is a valid instance of the Towny plugin, and that the
         // player has townysuffix TRUE.
         // try and fetch the name of the town this player is in
         // -----------
         // get timestamp for DB inserts
         Object timestamp = new java.sql.Timestamp((new Date()).getTime());
         String regionname = null;
 
         // Map associating top-level regions to the number of plots owned in that region
         Map<ProtectedRegion, Integer> parentCount = new LinkedHashMap<ProtectedRegion, Integer>();
 
         RegionContainer container = worldGuard.getRegionContainer();
         // iterate through all current RegionManagers - one for each world WG is enabled on
         for (RegionManager currRegionManager : container.getLoaded()) {
             // iterate through all regions held by such regionmanagers, looking for regions owned by this player
             for (ProtectedRegion currRegion : currRegionManager.getRegions().values()) {
                 if (currRegion.getOwners().contains(worldGuard.wrapPlayer(player))) {
                     ProtectedRegion currTopLevelRegion = getTopLevelRegion(currRegion);
                     if (parentCount.get(currTopLevelRegion) == null) {
                         // initialise at one
                         parentCount.put(currTopLevelRegion, 1);
                     } else {
                         // increment count
                         parentCount.put(currTopLevelRegion, parentCount.get(currTopLevelRegion) + 1);
                     }
                 }
             }
         }
 
         if (parentCount.size() < 1) {
             // it is possible that the player owns no regions, and therefore should have no WG suffix.
             if (verbose) {
                 logger.info("Player " + player.getName() + " does not own any WG regions. Resetting suffix.");
                 try {
                     Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                     ResultSet RS = statement.executeQuery("SELECT wgsuffix FROM " + dbTable + " WHERE user = '"
                             + player.getUniqueId().toString() + "';");
                     if (RS.isBeforeFirst()) {
                         // if the user is already in the database, update their record
                         statement.executeUpdate("UPDATE " + dbTable + " SET wgsuffix = NULL WHERE user= '" + player.getUniqueId() + "';");
                     } else {
                         // if the user is not already in the database, insert a new record
                         statement.executeQuery("INSERT INTO " + dbTable
                                 + " (user, last, wgsuffix, lastseen) VALUES ('"
                                 + player.getUniqueId().toString() + "', '" + player.getName() + "', NULL"
                                 + ", '" + timestamp + "');");
                         logger.info("Database entry was created for " + player.getName());
                     }
                 } catch (SQLException e1) {
                     e1.printStackTrace();
                 }
                 return;
             }
         }
 
         // Now run a second pass to find the most common top-level region
         // Ties are resolved by WorldGuard internal priorities, implemented through compareTo
         Entry<ProtectedRegion, Integer> mostCommonRegion = null;
         for (Entry<ProtectedRegion, Integer> currEntry : parentCount.entrySet()) {
             if (mostCommonRegion == null || mostCommonRegion.getValue() < currEntry.getValue()
                     || mostCommonRegion.getKey().compareTo(currEntry.getKey()) < 0) {
                 mostCommonRegion = currEntry;
             }
         }
 
         regionname = mostCommonRegion.getKey().getId();
         
         // apply appropriate formatting
         // capitalise first letter
         regionname = regionname.substring(0, 1).toUpperCase() + regionname.substring(1);        
         regionname = "of " + regionname;
         // then query for their previous suffix - if it is different, then update the record
         try {
             Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
             ResultSet RS = statement.executeQuery("SELECT wgsuffix FROM " + dbTable + " WHERE user = '" + player.getUniqueId().toString()
                     + "';");
             // check to see if data has changed
             if (RS.isBeforeFirst()) {
                 // if the user is already in the database, update their suffix if it differs.
                 String recordedSuffix = RS.getString("wgsuffix");
                 if (RS.wasNull()) {
                     recordedSuffix = "";
                 }
                 if (!(recordedSuffix.equals(regionname))) {
                     if (verbose) {
                         logger.info("Assigning new suffix '" + regionname + "' to player " + player.getName());
                     }
                     statement.executeUpdate("UPDATE " + dbTable + " SET wgsuffix = '" + regionname + "' WHERE user= '"
                             + player.getUniqueId() + "';");
                     return;
                 } else {
                     // if they don't differ then nothing else is required.
                     if (verbose) {
                         logger.info("Attempted to add suffix to player " + player.getName() + " but their suffix is already correct.");
                         return;
                     }
                 }
             } else {
                 // if the user is not already in the database, insert a new record with their username (so that the
                 // suffix doesn't look stupid)
                 statement.executeQuery("INSERT INTO " + dbTable + " (user, last, wgsuffix, lastseen) VALUES ('"
                         + player.getUniqueId() + "', '" + player.getName() + "', "
                         + regionname + ", '" + timestamp + "');");
                 logger.info("Database entry was created for " + player.getName());
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     public void recordDisplayName(String userUUID, String name) {
         try {
             Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
             statement.executeUpdate("UPDATE " + dbTable + " SET displayname = '" + name + "' WHERE user= '" + userUUID + "';");
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     // EventPriority.NORMAL by default
     public void onPlayerLogin(PlayerLoginEvent event) {
         // get timestamp for DB inserts
         long timestamp = new Date().getTime();
 
         Player player = event.getPlayer();
 
         try {
             Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
             ResultSet tryRS = statement.executeQuery("SELECT townysuffix FROM " + dbTable + " WHERE user = '"
                     + player.getUniqueId().toString() + "';");
             if (tryRS.isBeforeFirst()) {
                 // if the user is already in the database, just update their record with the new login time.
                 statement.executeUpdate("UPDATE " + dbTable + " SET lastseen = '" + timestamp + "' WHERE user= '" + player.getUniqueId()
                         + "';");
             } else {
                 // if the user is not already in the database, insert a new record with their username (so that suffixes
                 // and titles don't look stupid)
                 statement.executeUpdate("INSERT INTO " + dbTable + " (user, last, lastseen) VALUES ('"
                         + player.getUniqueId() + "', '" + player.getName() + "', '" + timestamp + "');");
                 logger.info("Database entry was created for " + player.getName());
             }
             // use WG integration
             if (useWorldGuard) {
                 reloadPlayerWGSuffix(event.getPlayer());
             }
             // use Towny integration
             if (useTowny) {
                 reloadPlayerTownySuffix(event.getPlayer());
                 reloadPlayerTownyTitle(event.getPlayer());
             }
             reloadPlayerName(server.getConsoleSender(), player);
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     public static ProtectedRegion getTopLevelRegion(ProtectedRegion region) {
         // traverses up to find the region with no parents
        if (region.getParent() == null) {
             return region;
         } else {
             return getTopLevelRegion(region.getParent());
         }
     }
 }

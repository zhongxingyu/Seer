 package de.fgtech.pomo4ka.AuthMe;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.fgtech.pomo4ka.AuthMe.DataSource.DataSource;
 import de.fgtech.pomo4ka.AuthMe.DataSource.DataCache;
 import de.fgtech.pomo4ka.AuthMe.DataSource.FlatfileData;
 import de.fgtech.pomo4ka.AuthMe.DataSource.MySQLData;
 import de.fgtech.pomo4ka.AuthMe.InventoryCache.FlatfileCache;
 import de.fgtech.pomo4ka.AuthMe.InventoryCache.InventoryArmour;
 import de.fgtech.pomo4ka.AuthMe.Listener.AuthMeBlockListener;
 import de.fgtech.pomo4ka.AuthMe.Listener.AuthMeEntityListener;
 import de.fgtech.pomo4ka.AuthMe.Listener.AuthMePlayerListener;
 import de.fgtech.pomo4ka.AuthMe.LoginTimeout.LoginTimer;
 import de.fgtech.pomo4ka.AuthMe.MessageHandler.MessageHandler;
 import de.fgtech.pomo4ka.AuthMe.Parameters.Messages;
 import de.fgtech.pomo4ka.AuthMe.Parameters.Settings;
 import de.fgtech.pomo4ka.AuthMe.PlayerCache.PlayerCache;
 import de.fgtech.pomo4ka.AuthMe.Sessions.SessionHandler;
 import de.fgtech.pomo4ka.AuthMe.security.PasswordSecurity;
 import de.fgtech.pomo4ka.AuthMe.util.Utility;
 
 public class AuthMe extends JavaPlugin {
 
     private PasswordSecurity pws;
     private Settings settings;
     private Messages messages;
     private PlayerCache playercache;
     private FlatfileCache invcache;
     private SessionHandler sessionhandler;
     private DataSource data;
 
     @Override
     public void onEnable() {
         // Creating dir, if it doesn't exist
         final File folder = new File(Settings.PLUGIN_FOLDER);
         if(!folder.exists()) {
             folder.mkdirs();
         }
 
         // Loading config
         File configFile = new File(Settings.PLUGIN_FOLDER, "config.yml");
         settings = new Settings(configFile);
 
         // Loading messages
         String lang = settings.Language().toLowerCase();
         if(lang.equals("en")) {
             File messagesFile = new File(Settings.PLUGIN_FOLDER, "messages.yml");
             if(!messagesFile.exists()) {
                 messagesFile = new File(Settings.PLUGIN_FOLDER, "messages.yml");
             }
             messages = new Messages(messagesFile);
         } else {
             String filename = "messages_" + lang + ".yml";
             messages = new Messages(Utility.loadFileFromJar(filename), false);
         }
 
         // Create a new cache for player stuff
         playercache = new PlayerCache();
 
         // Create the cache that's needed for inventory backups
         invcache = new FlatfileCache();
 
         // Create password hasher
         pws = new PasswordSecurity();
 
         // Create a session handler, that manages player sessions
         int maxTimePeriod = settings.MaximalTimePeriod();
         boolean IPCheck = settings.SessionIPCheckEnabled();
         sessionhandler = new SessionHandler(maxTimePeriod, IPCheck);
 
         // Create the wished DataSource
         if(settings.DataSource().equals("mysql")) {
             MessageHandler.showInfo("Using MySQL as datasource!");
             data = new DataCache(new MySQLData(settings), settings.
                     CachingEnabled());
         } else {
             MessageHandler.showInfo("Using flatfile as datasource!");
             data = new DataCache(new FlatfileData(), settings.CachingEnabled());
         }
 
         if(settings.CachingEnabled()) {
             MessageHandler.showInfo("Cache for registrations is enabled!");
         }
 
         MessageHandler.showInfo("There are " + data.getRegisteredPlayerAmount()
                                 + " registered players in database!");
         MessageHandler.showInfo("Version " + this.getDescription().getVersion()
                                 + " is enabled!");
 
         // Check if the plugin was loaded under runtime or was reloaded
         if(getServer().getOnlinePlayers().length > 0) {
             onAuthMeReload();
         }
 
         // Setting up the listeners
         AuthMePlayerListener playerListener = new AuthMePlayerListener(this);
         AuthMeBlockListener blockListener = new AuthMeBlockListener(this);
         AuthMeEntityListener entityListener = new AuthMeEntityListener(this);
 
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener,
                 Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener,
                 Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener,
                 Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener,
                 Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener,
                 Priority.Low, this);
         pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener,
                 Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLAYER_KICK, playerListener,
                 Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, playerListener,
                 Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener,
                 Priority.Monitor, this);
         pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener,
                 Priority.Monitor, this);
         pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener,
                 Priority.Monitor, this);
         pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener,
                 Priority.Monitor, this);
         pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener,
                 Priority.Monitor, this);
     }
 
     public void onAuthMeReload() {
         for(Player player : getServer().getOnlinePlayers()) {
             // Is player really registered?
             boolean regged = data.isPlayerRegistered(player.getName());
 
             // Create PlayerCache
             playercache.createCache(player, regged, false, player.getLocation());
             player.sendMessage(messages.getMessage("Alert.PluginReloaded"));
         }
 
         MessageHandler.showInfo("AuthMe restored the player cache!");
     }
 
     @Override
     public void onDisable() {
         MessageHandler.showInfo("Version " + this.getDescription().getVersion()
                                 + " is disabled!");
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd,
             String commandLabel, String[] args) {
 
         if(commandLabel.equalsIgnoreCase("register")) {
             if(!(sender instanceof Player)) {
                 return false;
             }
 
             Player player = (Player) sender;
 
             if(!settings.RegisterEnabled()) {
                 player.sendMessage("Registrations are currently disabled!");
                 return false;
             }
 
             if(playercache.isPlayerRegistered(player)) {
                 player.sendMessage(
                         messages.getMessage("Error.AlreadyRegistered"));
                 return false;
             }
 
             Map<String, String> customFields = settings.getCustomInformationFields();
             Map<String, String> customInformation = new HashMap<String, String>();
 
             // Do we have custom tables and mysql as datasource?
             if(customFields.size() > 0
                && settings.DataSource().equals("mysql")) {
 
                 // Check if the player typed the right amount of fields
                 if(args.length != customFields.size() + 1) {
                     String usageCustomFields = "";
                     for(String key : customFields.keySet()) {
                         usageCustomFields = usageCustomFields + "<" + key
                                             + "> ";
                     }
                     player.sendMessage("Usage: /register <password> "
                                        + usageCustomFields);
                     return false;
                 }
 
                 // Check the custom fields, if they comply to the RegEx'es and
                 // extract the informations and save them in a hashtable
                 int counter = 1;
                 for(String key : customFields.keySet()) {
                     if(!args[counter].matches(customFields.get(key))) {
                         player.sendMessage(messages.getMessage(
                                 "Command.RegisterExtraInfoCheckFailed", key));
                         return false;
                     }
                     customInformation.put(key, args[counter]);
                     counter++;
                 }
 
             } else {
                 // Check if the player typed the right amount of fields
                 if(args.length != 1) {
                     player.sendMessage("Usage: /register <password>");
                     return false;
                 }
             }
 
             String password = args[0];
 
             boolean executed;
             if(settings.Hash().equals("MD5")) {
                 executed = data.saveAuth(player.getName(),
                         pws.getMD5(password),
                         customInformation);
             } else if(settings.Hash().equals("SHA256")) {
                 String salt = Long.toHexString(Double.doubleToLongBits(Math.
                         random()));
                 executed = data.saveAuth(player.getName(),
                         pws.getSaltedHash(password, salt),
                         customInformation);
             } else if(settings.Hash().equals("SHA1")) {
                 executed = data.saveAuth(player.getName(),
                         pws.getSHA1(password),
                         customInformation);
             } else {
                 executed = false;
             }
 
             if(!executed) {
                 player.sendMessage(messages.getMessage("Error.DatasourceError"));
                 MessageHandler.showError(
                         "Failed to save an auth due to an error in the datasource!");
                 return false;
             }
 
             playercache.setPlayerAuthenticated(player, true);
             playercache.setPlayerRegistered(player, true);
             if(!settings.WalkAroundSpawnEnabled() || settings.ForceRegistration()) {
                 player.teleport(playercache.getPlayerData(player).getSpawn());
             }
 
             player.sendMessage(messages.getMessage("Command.RegisterResponse",
                     password));
             MessageHandler.showInfo("Player " + player.getName()
                                     + " is now registered!");
 
             return true;
         }
 
         if(commandLabel.equalsIgnoreCase("login")
            || commandLabel.equalsIgnoreCase("l")) {
             if(!(sender instanceof Player)) {
                 return false;
             }
             Player player = (Player) sender;
 
             if(!settings.LoginEnabled()) {
                 player.sendMessage("Logins are currently disabled!");
                 return false;
             }
 
             if(args.length != 1) {
                 player.sendMessage("Usage: /login <password>");
                 return false;
             }
 
             String playername = player.getName();
             String password = args[0];
 
             if(!playercache.isPlayerRegistered(player)) {
                 player.sendMessage(messages.getMessage("Error.NotRegistered"));
                 return false;
             }
 
             if(playercache.isPlayerAuthenticated(player)) {
                 player.sendMessage(messages.getMessage("Error.AlreadyLoggedIn"));
                 return false;
             }
 
             final String realPassword = data.loadHash(playername);
 
             if(!comparePassword(password, realPassword)) {
                 if(settings.KickOnWrongPassword()) {
                     player.kickPlayer(messages.getMessage(
                             "Error.InvalidPassword"));
                 } else {
                     player.sendMessage(messages.getMessage(
                             "Error.InvalidPassword"));
                 }
                 MessageHandler.showInfo(
                         "Player " + player.getName()
                         + " tried to login with a wrong password!");
                 return false;
             }
 
             LoginTimer.unscheduleLoginTimer(this, player);
             performPlayerLogin(player);
             if(!settings.WalkAroundSpawnEnabled()) {
                 player.teleport(playercache.getPlayerData(player).getSpawn());
             }
 
             player.sendMessage(messages.getMessage("Command.LoginResponse"));
             MessageHandler.showInfo("Player " + player.getName()
                                     + " logged in!");
 
             return true;
         }
 
         if(commandLabel.equalsIgnoreCase("changepassword")) {
             if(!(sender instanceof Player)) {
                 return false;
             }
             Player player = (Player) sender;
 
             if(!settings.ChangePasswordEnabled()) {
                 player.sendMessage("Changing passwords is currently disabled!");
                 return false;
             }
             if(!playercache.isPlayerRegistered(player)) {
                 player.sendMessage(messages.getMessage("Error.NotRegistered"));
                 return false;
             }
             if(!playercache.isPlayerAuthenticated(player)) {
                 player.sendMessage(messages.getMessage("Error.NotLogged"));
                 return false;
             }
             if(args.length != 2) {
                 player.sendMessage(
                         "Usage: /changepassword <oldpassword> <newpassword>");
                 return false;
             }
             if(!comparePassword(args[0],
                     data.loadHash(player.getName()))) {
                 player.sendMessage(messages.getMessage("Error.WrongPassword"));
                 return false;
             }
 
             String salt = Long.toHexString(
                     Double.doubleToLongBits(Math.random()));
             boolean executed = data.updateAuth(player.getName(), pws.
                     getSaltedHash(args[1], salt));
 
             if(!executed) {
                 player.sendMessage(messages.getMessage("Error.DatasourceError"));
                 MessageHandler.showError(
                         "Failed to update an auth due to an error in the datasource!");
                 return false;
             }
 
             player.sendMessage(messages.getMessage(
                     "Command.ChangePasswordResponse"));
             MessageHandler.showInfo("Player " + player.getName()
                                     + " changed his password!");
         }
 
         if(commandLabel.equalsIgnoreCase("logout")) {
             if(!(sender instanceof Player)) {
                 return false;
             }
             Player player = (Player) sender;
 
             if(!settings.LogoutEnabled()) {
                 player.sendMessage("Logging out is currently disabled!");
                 return false;
             }
 
             if(!playercache.isPlayerAuthenticated(player)) {
                 player.sendMessage(messages.getMessage("Error.NotLogged"));
                 return false;
             }
 
             playercache.setPlayerAuthenticated(player, false);
 
             player.sendMessage(messages.getMessage("Command.LogoutResponse"));
             MessageHandler.showInfo("Player " + player.getName()
                                     + " logged out!");
         }
 
         if(commandLabel.equalsIgnoreCase("unregister")) {
             if(!(sender instanceof Player)) {
                 return false;
             }
             Player player = (Player) sender;
 
             if(!settings.UnregisterEnabled()) {
                 player.sendMessage("Unregistering is currently disabled!");
                 return false;
             }
             if(!playercache.isPlayerRegistered(player)) {
                 player.sendMessage(messages.getMessage("Error.NotRegistered"));
                 return false;
             }
             if(!playercache.isPlayerAuthenticated(player)) {
                 player.sendMessage(messages.getMessage("Error.NotLogged"));
                 return false;
             }
             if(args.length != 1) {
                 player.sendMessage("Usage: /unregister <password>");
                 return false;
             }
             if(!comparePassword(args[0],
                     data.loadHash(player.getName()))) {
                 player.sendMessage(messages.getMessage("Error.WrongPassword"));
                 return false;
             }
 
             boolean executed = data.removeAuth(player.getName());
 
             if(!executed) {
                 player.sendMessage(messages.getMessage("Error.DatasourceError"));
                 MessageHandler.showError(
                         "Failed to remove an auth due to an error in the datasource!");
                 return false;
             }
 
             playercache.recreateCache(player,player.getLocation());
 
             player.sendMessage(messages.getMessage("Command.UnregisterResponse"));
             MessageHandler.showInfo("Player " + player.getName()
                                     + " is now unregistered!");
         }
 
         if(commandLabel.equalsIgnoreCase("authme")) {
 
             String pre = "";
 
             if(sender instanceof Player) {
                 Player player = (Player) sender;
 
                 if(!player.isOp()) {
                     player.sendMessage("You dont have permission to do this!");
                     return false;
                 }
                 if(!playercache.isPlayerAuthenticated(player)) {
                     player.sendMessage(messages.getMessage("Error.NotLogged"));
                     return false;
                 }
 
                 pre = "/";
             }
 
             if(args.length == 0) {
                 sender.sendMessage(
                         "Usage: "
                         + pre
                         + "authme <reloadconfig | reloadcache | toggleregs | deleteauth>");
                 return false;
             }
 
             if(args[0].equals("deleteauth")) {
                 if(args.length != 2) {
                     sender.sendMessage("Usage: " + pre
                                        + "authme deleteauth <playername>");
                     return false;
                 }
             }
 
             // /authme reload cache Command
             if(args[0].equals("reloadcache")) {
                 if(!settings.ReloadEnabled()) {
                     sender.sendMessage(
                             "Reloading authentications is currently disabled!");
                     return false;
                 }
                 if(!settings.CachingEnabled()) {
                     sender.sendMessage(
                             ChatColor.RED
                             + "Error: There is no need to reload the authentication cache. Caching is disabled in config anyway!");
                     return false;
                 }
 
                 data.loadAllAuths();
 
                 sender.sendMessage(
                         ChatColor.GREEN
                         + "AuthMe has successfully reloaded all authentications!");
 
                 MessageHandler.showInfo(
                         "Authentication cache reloaded by command!");
             }
 
             // /authme reload config Command
             if(args[0].equals("reloadconfig")) {
                 File configFile = new File(Settings.PLUGIN_FOLDER, "config.yml");
                 settings = new Settings(configFile);
 
                 sender.sendMessage(
                         ChatColor.GREEN
                         + "AuthMe has successfully reloaded it's config file!");
 
                 MessageHandler.showInfo("Config file reloaded by command!");
             }
 
             // /authme toggle regs Command
             if(args[0].equals("toggleregs")) {
                 String key = "Commands.Users.RegisterEnabled";
                 if(settings.getBoolean(key, true)) {
                     settings.setProperty(key, false);
                     sender.sendMessage(
                             ChatColor.GREEN
                             + "AuthMe has successfully disabled registrations!");
                 } else {
                     settings.setProperty(key, true);
                     sender.sendMessage(
                             ChatColor.GREEN
                             + "AuthMe has successfully enabled registrations!");
                 }
             }
 
             // /authme delete auth Command
             if(args[0].equals("deleteauth")) {
                 if(!settings.ResetEnabled()) {
                     sender.sendMessage(
                             "Reseting a authentication is currently disabled!");
                     return false;
                 }
                 if(!data.isPlayerRegistered(args[1])) {
                     sender.sendMessage(messages.getMessage(
                             "Error.PlayerNotRegistered"));
                     return false;
                 }
 
                 boolean executed = data.removeAuth(args[1]);
 
                 if(!executed) {
                     sender.sendMessage(messages.getMessage(
                             "Error.DatasourceError"));
                     MessageHandler.showError(
                             "Failed to remove an auth due to an error in the datasource!");
                     return false;
                 }
 
                 // If the player is online, recreate his cache
                 Player delPlayer = getServer().getPlayer(args[1]);
                 if(delPlayer != null) {
                     playercache.recreateCache(delPlayer, delPlayer.getLocation());
                 }
 
                 sender.sendMessage(ChatColor.GREEN
                                    + "This player is now unregistered!");
                 MessageHandler.showInfo("Account of " + args[1]
                                         + " got deleted by command!");
             }
 
         }
 
         return false;
     }
 
     public boolean checkAuth(Player player) {
         if(playercache.isPlayerAuthenticated(player)) {
             return true;
         }
         return !settings.ForceRegistration();
     }
 
     public void performPlayerLogin(Player player) {
         playercache.setPlayerAuthenticated(player, true);
 
         if(invcache.doesCacheExist(player.getName())) {
             InventoryArmour invarm = invcache.readCache(player.getName());
 
             ItemStack[] invstackbackup = invarm.getInventory();
             player.getInventory().setContents(invstackbackup);
 
             ItemStack[] armStackBackup = invarm.getArmour();
 
             if(armStackBackup[3] != null) {
                 if(armStackBackup[3].getAmount() != 0) {
                     player.getInventory().setHelmet(armStackBackup[3]);
                 }
             }
             if(armStackBackup[2] != null) {
                 if(armStackBackup[2].getAmount() != 0) {
                     player.getInventory().setChestplate(armStackBackup[2]);
                 }
             }
             if(armStackBackup[1] != null) {
                 if(armStackBackup[1].getAmount() != 0) {
                     player.getInventory().setLeggings(armStackBackup[1]);
                 }
             }
             if(armStackBackup[0] != null) {
                 if(armStackBackup[0].getAmount() != 0) {
                     player.getInventory().setBoots(armStackBackup[0]);
                 }
             }
 
             invcache.removeCache(player.getName());
         }
     }
 
     private boolean comparePassword(String password, String hash) {
         //MD5
         if(hash.length() == 32) {
             return hash.equals(pws.getMD5(password));
         }
 
         //SHA1
         if(hash.length() == 40) {
             return hash.equals(pws.getSHA1(password));
         }
 
         //SHA256 with salt
         if(hash.contains("$")) {
             String[] line = hash.split("\\$");
             if(line.length > 3 && line[1].equals("SHA")) {
                 return hash.equals(pws.getSaltedHash(password, line[2]));
             } else {
                 return false;
             }
         }
         return false;
     }
 
     public DataSource getData() {
         return data;
     }
 
     public FlatfileCache getInvcache() {
         return invcache;
     }
 
     public Messages getMessages() {
         return messages;
     }
 
     public PlayerCache getPlayercache() {
         return playercache;
     }
 
     public SessionHandler getSessionhandler() {
         return sessionhandler;
     }
 
     public Settings getSettings() {
         return settings;
     }
}

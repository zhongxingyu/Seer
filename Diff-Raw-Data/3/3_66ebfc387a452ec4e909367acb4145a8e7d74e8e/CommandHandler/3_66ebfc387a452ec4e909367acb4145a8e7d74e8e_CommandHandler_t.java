 package net.tgxn.bukkit.backup.events;
 
 import java.io.File;
 import net.tgxn.bukkit.backup.config.Settings;
 import net.tgxn.bukkit.backup.config.Strings;
 import net.tgxn.bukkit.backup.threading.PrepareBackup;
 import net.tgxn.bukkit.backup.utils.LogUtils;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.Plugin;
 
 public class CommandHandler implements Listener, CommandExecutor {
 
     private PrepareBackup prepareBackup = null;
     private final Plugin plugin;
     private Settings settings;
     private Strings strings;
 
     /**
      * The main constructor to initalize listening for commands.
      *
      * @param prepareBackup The instance of prepareBackup.
      * @param plugin The plugin object itself
      * @param settings Load settings for the plugin.
      * @param strings The strings configuration for th plugin.
      */
     public CommandHandler(PrepareBackup prepareBackup, Plugin plugin, Settings settings, Strings strings) {
         this.prepareBackup = prepareBackup;
         this.plugin = plugin;
         this.settings = settings;
         this.strings = strings;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 
         // Initalize variables.
         Player player = null;
 
         // Process in-game commands.
         if ((sender instanceof Player)) {
 
             // Get player object
             player = (Player) sender;
         }
 
         // Do the actual processing.
         return processCommand(label, args, player);
     }
 
     /**
      * Method to process every command.
      *
      * @param command The command (Usually "backup")
      * @param args Arguments passed along with the command.
      * @param player The player that requested the command.
      * @return True is success, False if fail.
      */
     public boolean processCommand(String command, String[] args, Player player) {
 
         if (player == null) {
             if (command.equalsIgnoreCase("backup")) {
 
                 if (args.length > 0) {
                     if (args[0].equals("updateconf")) {
                         updateConfig(null);
                     } else if (args[0].equals("reload")) {
                         reloadPlugin(plugin, player);
                     }
 
 
                 } else {
                     doManualBackup();
                 }
             }
         } else {
             // For all playercommands.
 
 
 
             // For everything under the backup command.
             if (command.equalsIgnoreCase("backup")) {
 
 
                 // Contains auguments.
                 if (args.length > 0) {
                     String argument = args[0];
                     if (argument.equals("help")) {
                         if (checkPerms(player, "backup.help")) {
                             sendHelp(player);
                         }
                     } else if (argument.equals("reload")) {
                         if (checkPerms(player, "backup.reload")) {
                             reloadPlugin(plugin, player);
                         }
                     } else if (argument.equals("list")) {
                         if (checkPerms(player, "backup.list")) {
                             listBackups(player);
                         }
                     } else if (argument.equals("config")) {
                         if (checkPerms(player, "backup.config")) {
                             showConfig(player);
                         }
                     } else if (argument.equals("log")) {
                         if (checkPerms(player, "backup.log")) {
                             showLog(player);
                         }
                     } else if (argument.equals("upgradeconf")) {
                         if (checkPerms(player, "backup.upgradeconf")) {
                             updateConfig(player);
                         }
                     }
 
                 } else {
                     if (checkPerms(player, "backup.backup")) {
                         doManualBackup();
                     }
                 }
             }
 
         }
         return true;
     }
 
     public void reloadPlugin(Plugin plugin, Player player) {
         plugin.onDisable();
         plugin.onLoad();
         plugin.onEnable();
        if (player != null)
            player.sendMessage(strings.getString("reloadedok", plugin.getDescription().getVersion()));
     }
 
     /**
      * Start a manual backup.
      */
     private void doManualBackup() {
         prepareBackup.setAsManualBackup();
         plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, prepareBackup);
     }
 
     /**
      * Send the plugin help to the player.
      *
      * @param player The player who requested the help.
      */
     private void sendHelp(Player player) {
         player.sendMessage("Backup v" + plugin.getDescription().getVersion() + " Help Menu");
         player.sendMessage("Commands:");
         player.sendMessage("/backup");
         player.sendMessage("- Performs a backup.");
         player.sendMessage("/backup upgradeconf");
         player.sendMessage("- Re-Installs config file.");
         player.sendMessage(".");
         player.sendMessage(".");
         player.sendMessage("Coming Soon :)");
         player.sendMessage(".");
     }
 
     /**
      * Checks if the player has permissions. Also sends a message if the player
      * does not have permissions.
      *
      * @param player The player's object.
      * @param permission The name of the permission
      * @return True if they have permission, false if no permission
      */
     private boolean checkPerms(Player player, String permission) {
 
         // We hooked a perms system.
         if (player.isPermissionSet(permission)) {
             if (!player.hasPermission(permission)) {
                 player.sendMessage(strings.getString("norights"));
                 return false;
             } else {
                 return true;
             }
 
         } else {
 
             // Check what to do in case of no permissions.
             if (settings.getBooleanProperty("onlyops") && !player.isOp()) {
                 player.sendMessage(strings.getString("norights"));
                 return false;
             } else {
                 return true;
             }
         }
     }
 
     /**
      * For listing all the backups for a user. Lists to a maximum of 8 so that
      * it doesn't flow off screen.
      *
      * @param player The player that requested the list.
      */
     private void listBackups(Player player) {
 
         // Get the backups path.
         String backupDir = settings.getStringProperty("backuppath");
 
         // Make a list.
         String[] filesList = new File(backupDir).list();
 
         // Inform what is happenning.
         player.sendMessage("Listing backup directory: \"" + backupDir + "\".");
 
         // Check if the directory exists.
         if (filesList == null) {
 
             // Error message.
             player.sendMessage("Error listing directory!");
         } else {
 
             // How many files in array.
             int amountoffiles = filesList.length;
 
             // Limit listings, so it doesnt flow off screen.
             if (amountoffiles > 8) {
                 amountoffiles = 8;
             }
 
             // Send informal message.
             player.sendMessage("" + amountoffiles + " backups found, listing...");
 
             // Loop through files, and list them.
             for (int i = 0; i < amountoffiles; i++) {
 
                 // Get filename of file.
                 String filename = filesList[i];
 
                 // Send messages for each file.
                 int number = i + 1;
                 player.sendMessage(number + "). " + filename);
             }
         }
     }
 
     /**
      * To show the plugins configuration.
      *
      * @param player The player that requested the configuration.
      */
     private void showConfig(Player player) {
 
         player.sendMessage("Backup Configuration");
 
         int interval = settings.getIntervalInMinutes("backupinterval");
         if (interval != 0) {
             player.sendMessage("Scheduled Backups: Enabled, " + interval + " mins between backups.");
         } else {
             player.sendMessage("Scheduled backups: Disabled, Manual backups only.");
         }
 
         boolean hasToZIP = settings.getBooleanProperty("zipbackup");
         if (hasToZIP) {
             player.sendMessage("Backup compression is Enabled.");
         } else {
             player.sendMessage("Backup compression is Disabled.");
         }
     }
 
     private void showLog(Player player) {
         player.sendMessage("Coming Soon :)");
     }
 
     private void updateConfig(Player player) {
         if (settings.checkConfigVersion(false)) {
             if (player != null) {
                 player.sendMessage(strings.getString("updatingconf"));
             }
             settings.doConfigurationUpgrade();
         } else {
             if (player != null) {
                 player.sendMessage(strings.getString("confuptodate"));
             } else {
                 LogUtils.sendLog(strings.getString("confuptodate"), false);
             }
         }
     }
 }

 package net.tgxn.bukkit.backup.threading;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import net.tgxn.bukkit.backup.config.Settings;
 import net.tgxn.bukkit.backup.config.Strings;
 import net.tgxn.bukkit.backup.utils.LogUtils;
 import net.tgxn.bukkit.backup.utils.SyncSaveAllUtil;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 public class PrepareBackup implements Runnable {
 
     // Public variables.
     public boolean isLastBackup;
     public boolean isManualBackup;
     public LinkedList<String> worldsToBackup;
     // Private variables for this class.
     private final Server server;
     private final Settings settings;
     private Strings strings;
     private Plugin plugin;
 
     public PrepareBackup(Server server, Settings settings, Strings strings) {
         this.server = server;
         this.settings = settings;
         this.plugin = server.getPluginManager().getPlugin("Backup");
         this.strings = strings;
         isLastBackup = false;
     }
 
     @Override
     public void run() {
         checkShouldDoBackup();
     }
 
     /**
      * This method decides whether the doBackup should be run.
      *
      * It checks: - Online players. - Bypass node. - Manual doBackup.
      *
      * It then runs the doBackup if needed.
      */
     private void checkShouldDoBackup() {
 
         // If it is a manual doBackup, start it, otherwise, perform checks.
         if (isManualBackup) {
             prepareBackup();
         } else {
 
             // No player checking.
             if (settings.getBooleanProperty("backupemptyserver")) {
                 prepareBackup();
             } else {
 
                 // Checking online players.
                 if (server.getOnlinePlayers().length == 0) {
                     
                     // Check if last backup
                     if (isLastBackup) {
                         LogUtils.sendLog(strings.getString("lastbackup"));
                         prepareBackup();
                         isLastBackup = false;
                     } else {
                         LogUtils.sendLog(strings.getString("abortedbackup", Integer.toString(settings.getIntervalInMinutes("backupinterval"))), Level.INFO, true);
                     }
                 } else {
 
                     // Default don't do backup.
                     boolean doBackup = false;
 
                     // Get all online players.
                     Player[] players = server.getOnlinePlayers();
 
                     // Loop players.
                     for (int player = 0; player < players.length; player++) {
                         Player currentplayer = players[player];
 
                         // If any players do not have the node, do the doBackup.
                         if (!currentplayer.hasPermission("backup.bypass")) {
                             doBackup = true;
                         }
                     }
 
                     // Final check if we should do the backup.
                     if (doBackup) {
                         prepareBackup();
                     } else {
                         LogUtils.sendLog(strings.getString("skipbackupbypass"));
                     }
                 }
             }
         }
 
         // Check we should do a save-all.
         if (settings.getBooleanProperty("alwayssaveall")) {
             server.getScheduler().scheduleSyncDelayedTask(plugin, new SyncSaveAllUtil(server, 0));
             LogUtils.sendLog(strings.getString("alwayssaveall"));
         }
     }
 
     /**
      * Prepared for, and starts, a doBackup.
      */
     protected void prepareBackup() {
 
         // Notify doBackup has started.
         notifyStarted();
 
         // Perform final world save before backup, then turn off auto-saving.
         server.getScheduler().scheduleSyncDelayedTask(plugin, new SyncSaveAllUtil(server, 1));
 
         // Save all players.
         server.savePlayers();
 
         // Create list of worlds to ignore.
         List<String> ignoredWorldNames = getIgnoredWorldNames();
         worldsToBackup = new LinkedList<String>();
         for (World world : server.getWorlds()) {
             if ((world.getName() != null) && !world.getName().isEmpty() && (!ignoredWorldNames.contains(world.getName()))) {
                 LogUtils.sendLog("Adding world '" + world.getName() + "' to backup list", Level.FINE, true);
                 worldsToBackup.add(world.getName());
             }
         }
 
         // Scedule the doBackup.
         server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            private LinkedList<String> worldsToBackup;
             @Override
             public void run() {
                 server.getScheduler().scheduleAsyncDelayedTask(plugin, new BackupTask(server, settings, strings, worldsToBackup));
             }
         });
         isManualBackup = false;
     }
 
     /**
      * Function to get world names to ignore.
      *
      * @return A List[] of the world names we should not be backing up.
      */
     private List<String> getIgnoredWorldNames() {
 
         // Get skipped worlds form config.
         List<String> worldNames = Arrays.asList(settings.getStringProperty("skipworlds").split(";"));
 
         // Loop all ignored worlds.
         if (worldNames.size() > 0 && !worldNames.get(0).isEmpty()) {
 
             // Log what worlds are disabled.
             LogUtils.sendLog(strings.getString("disabledworlds"));
             LogUtils.sendLog(worldNames.toString());
         }
 
         // Return the world names.
         return worldNames;
     }
 
     /**
      * Notify that the backup has started.
      *
      */
     private void notifyStarted() {
 
         // Get message.
         String startBackupMessage = strings.getString("backupstarted");
 
         // Check the string is set.
         if (startBackupMessage != null && !startBackupMessage.trim().isEmpty()) {
 
             // Notify all players, regardless of the permission node.
             if (settings.getBooleanProperty("notifyallplayers")) {
                 server.broadcastMessage(startBackupMessage);
             } else {
 
                 // Get all players.
                 Player[] players = server.getOnlinePlayers();
                 // Loop through all online players.
                 for (int pos = 0; pos < players.length; pos++) {
                     Player currentplayer = players[pos];
 
                     // If the current player has the right permissions, notify them.
                     if (currentplayer.hasPermission("backup.notify")) {
                         currentplayer.sendMessage(startBackupMessage);
                     }
                 }
             }
         }
     }
 
     /**
      * Set the doBackup as a manual doBackup. IE: Not scheduled.
      */
     public void setAsManualBackup() {
         this.isManualBackup = true;
     }
 
     /**
      * Set the doBackup as a last doBackup.
      */
     public void setAsLastBackup(boolean isLast) {
         this.isLastBackup = isLast;
     }
 }

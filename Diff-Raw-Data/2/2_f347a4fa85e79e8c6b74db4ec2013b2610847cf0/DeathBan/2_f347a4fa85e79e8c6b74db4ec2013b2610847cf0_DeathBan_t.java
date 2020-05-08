 package com.github.sar3th.DeathBan;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.MemorySection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Main class of DeathBan plugin for Bukkit
  */
 public class DeathBan extends JavaPlugin implements Listener {
 
     private FileConfiguration banStorage;
     private HashMap<String, Long> banDatabase;
     private final Integer banDatabaseLock = 31337;
     private boolean suppressDeathEvents = false;
 
     @Override
     public void onEnable() {
         getConfig().options().copyDefaults(true);
         reloadBanDB();
         // Register as event Listener
         getServer().getPluginManager().registerEvents(this, this);
     }
 
     @Override
     public void onDisable() {
         saveBanDB();
         saveConfig();
     }
 
     @EventHandler
     public void onPlayerLogin(PlayerLoginEvent event) {
         if (isEnabled()) {
             Player joiningPlayer = event.getPlayer();
             Long banLiftTime = banDatabase.get(joiningPlayer.getName().toLowerCase());
             if (banLiftTime != null) {
                 // Find out if ban is still in effect
                 long rightNow = (System.currentTimeMillis() / 1000);
                 long remainingBanTime = banLiftTime - rightNow;
 
                 if (remainingBanTime > 0) {
                     // Player is still banned for some time, kick him
                     String readableRemainingBanTime = longToReadableTime(remainingBanTime);
                     String rejoinMessage = getConfig().getString("rejoinmessage").replace("$time", readableRemainingBanTime);
 
                     event.setKickMessage(rejoinMessage);
                     event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
 
                     getLogger().log(Level.INFO, String.format("%s tried to join but is banned for %s.", joiningPlayer.getName(), readableRemainingBanTime));
                 } else {
                     // Player is not banned anymore
                     unbanPlayer(joiningPlayer.getName());
                 }
             } // If player is not banned, do nothing
         }
     }
 
     @EventHandler
     public void onPlayerDeathEvent(PlayerDeathEvent event) {
         if (isEnabled() && !suppressDeathEvents) {
             // Someone died :3
             banPlayer(event.getEntity().getName(), getConfig().getLong("bantime"), event.getDeathMessage());
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (command.getName().equalsIgnoreCase("db-ban")) {
             if (args.length == 2) {
                 Player victim = getServer().getPlayer(args[0]);
                 long banTime = Integer.parseInt(args[1]);
 
                 if ((victim != null) && killPlayer(victim.getName(), sender.getName())) {
                     sender.sendMessage(String.format("Successfully killed %s", victim.getName()));
                 } else {
                     sender.sendMessage(String.format("Unable to kill %s", args[0]));
                 }
 
                 // Add to banlist regardless
                 banPlayer(args[0], banTime, sender.getName(), null);
 
                 return true;
             } else {
                 return false;
             }
         } else if (command.getName().equalsIgnoreCase("db-clear")) {
             if (args.length == 0) {
                 clearBanDB();
 
                 return true;
             } else {
                 return false;
             }
         } else if (command.getName().equalsIgnoreCase("db-list")) {
             if (args.length == 0) {
                 if (banDatabase.isEmpty()) {
                     sender.sendMessage("The banlist is empty! :)");
                     
                     return true;
                 } else {
                     for (String targetPlayer : banDatabase.keySet()) {
                         long remainingTime = banDatabase.get(targetPlayer.toLowerCase()) - (System.currentTimeMillis() / 1000);
 
                         if (remainingTime > 0) {
                             String readableRemainingTime = longToReadableTime(remainingTime);
                             sender.sendMessage(String.format("%s is banned for %s", targetPlayer, readableRemainingTime));
                         } else {
                             // Ban can be lifted
                             unbanPlayer(targetPlayer);
                         }
                     }
 
                     return true;
                 }
             } else if (args.length == 1) {
                 String targetPlayer = args[0];
                 Long banLiftTime = banDatabase.get(targetPlayer.toLowerCase());
 
                 if (banLiftTime != null) {
                     long remainingTime = banLiftTime - (System.currentTimeMillis() / 1000);
 
                     if (remainingTime > 0) {
                         String readableRemainingTime = longToReadableTime(remainingTime);
                         sender.sendMessage(String.format("%s is banned for %s", targetPlayer, readableRemainingTime));
                     } else {
                         // Ban can be lifted
                         unbanPlayer(targetPlayer);
                         sender.sendMessage(String.format("%s is not banned", targetPlayer));
                     }
                 } else {
                     sender.sendMessage(String.format("%s is not banned", targetPlayer));
                 }
 
                 return true;
             } else {
                 return false;
             }
         } else if (command.getName().equalsIgnoreCase("db-reload")) {
             if (args.length == 0) {
                 reloadConfig();
                 reloadBanDB();
 
                 return true;
             } else {
                 return false;
             }
         } else if (command.getName().equalsIgnoreCase("db-settime")) {
             if (args.length == 1) {
                 int banTime = Integer.parseInt(args[0]);
 
                 getConfig().set("bantime", banTime);
                 getLogger().log(Level.INFO, String.format("%s set bantime to %d", sender.getName(), banTime));
                 saveConfig();
 
                 return true;
             } else {
                 return false;
             }
         } else if (command.getName().equalsIgnoreCase("db-unban")) {
             if (args.length == 1) {
                 unbanPlayer(args[0]);
                 sender.sendMessage(String.format("%s has been unbanned.", args[0]));
 
                 return true;
             } else {
                 return false;
             }
         } else {
             return false;
         }
     }
 
     private File getBanDBFile() {
         return new File(getDataFolder(), "bans.yml");
     }
 
     private void reloadBanDB() {
         synchronized (banDatabaseLock) {
             banStorage = YamlConfiguration.loadConfiguration(getBanDBFile());
             banDatabase = new HashMap<String, Long>();
 
             MemorySection storedBanDatabase = (MemorySection) banStorage.get("banlist", null);
             if (storedBanDatabase != null) {
                 Set<String> playerList = storedBanDatabase.getKeys(false);
                 for (String player : playerList) {
                     Object banLiftTime = storedBanDatabase.get(player.toLowerCase());
                     if (banLiftTime instanceof Integer) {
                         banDatabase.put(player.toLowerCase(), ((Integer) banLiftTime).longValue());
                     } else if (banLiftTime instanceof Long) {
                         banDatabase.put(player.toLowerCase(), (Long) banLiftTime);
                     } else {
                         getLogger().log(Level.SEVERE, String.format("Unable to load banLitTime for %s, ignoring!", player));
                     }
                 }
             }
         }
         getLogger().log(Level.INFO, String.format("Loaded %d bans from ban storage.", banDatabase.size()));
     }
 
     private void saveBanDB() {
         synchronized (banDatabaseLock) {
             banStorage.set("banlist", banDatabase);
             File banDBFile = getBanDBFile();
             try {
                 banStorage.save(banDBFile);
                 getLogger().log(Level.INFO, "Banlist saved.");
             } catch (IOException ex) {
                 getLogger().log(Level.SEVERE, String.format("Could not save banlist to %s!", banDBFile.getName()), ex);
             }
         }
     }
 
     private void clearBanDB() {
         synchronized (banDatabaseLock) {
             banDatabase = new HashMap<String, Long>();
             getLogger().log(Level.INFO, "Banlist cleared");
             saveBanDB();
         }
     }
 
     private boolean killPlayer(String victimName, String killerName) {
         Player victim = getServer().getPlayer(victimName);
 
         if (victim != null) {
             if (!victim.isDead()) {
                 // Ensure that we don't accidentally catch this with our listener
                 suppressDeathEvents = true;
 
                 getServer().getPluginManager().callEvent(new EntityDamageEvent(victim, EntityDamageEvent.DamageCause.SUICIDE, victim.getHealth()));
 
                 victim.damage(victim.getHealth());
                 suppressDeathEvents = false;
                 getLogger().log(Level.INFO, String.format("%s killed by %s", victim.getName(), killerName));
             } else {
                 getLogger().log(Level.INFO, String.format("%s wanted to kill %s but player was already dead", killerName, victim.getName()));
             }
 
             return true;
         } else {
             return false;
         }
     }
 
     private void banPlayer(String playerName, long banDuration, String reason) {
         banPlayer(playerName, banDuration, null, reason);
     }
 
     private String longToReadableTime(long time) {
         List<String> timeStrings = new ArrayList<String>(4);
         Long remainingSeconds = time;
 
         // A very dumb way to calculate this - but at least it's readable :)
 
         // Calculate days
         if (remainingSeconds > (60 * 60 * 24)) {
             long days = remainingSeconds / (60 * 60 * 24);
             if (days > 0) {
                 if (days > 1) {
                     timeStrings.add(String.format("%d days", days));
                 } else {
                     timeStrings.add(String.format("%d day", days));
                 }
 
                 remainingSeconds -= days * (60 * 60 * 24);
             }
         }
 
         // Calculate hours
         if (remainingSeconds > (60 * 60)) {
             long hours = remainingSeconds / (60 * 60);
             if (hours > 0) {
                 if (hours > 1) {
                     timeStrings.add(String.format("%d hours", hours));
                 } else {
                     timeStrings.add(String.format("%d hour", hours));
                 }
 
                 remainingSeconds -= hours * (60 * 60);
             }
         }
 
         // Calculate minutes
         if (remainingSeconds > 60) {
             long minutes = remainingSeconds / 60;
             if (minutes > 0) {
                 if (minutes > 1) {
                     timeStrings.add(String.format("%d minutes", minutes));
                 } else {
                     timeStrings.add(String.format("%d minute", minutes));
                 }
 
                 remainingSeconds -= minutes * 60;
             }
         }
 
         // Calculate seconds
         if (remainingSeconds > 0 || (timeStrings.isEmpty())) {
             if (remainingSeconds == 1) {
                 timeStrings.add(String.format("%d second", remainingSeconds));
             } else {
                 timeStrings.add(String.format("%d seconds", remainingSeconds));
             }
         }
 
         // Turn this into a nice, readable String
         // The code itself is not so nice, but the result will be :)
 
         if (timeStrings.size() > 1) {
             StringBuilder sb = new StringBuilder();
 
             sb.append(timeStrings.get(0));
 
             for (int i = 1; i < (timeStrings.size() - 1); i++) {
                 sb.append(String.format(", %s", timeStrings.get(i)));
             }
 
             sb.append(String.format(" and %s", timeStrings.get(timeStrings.size() - 1)));
 
             return sb.toString();
         } else {
             return timeStrings.get(0);
         }
     }
 
     private void unbanPlayer(String playerName) {
         synchronized (banDatabaseLock) {
             banDatabase.remove(playerName);
             saveBanDB();
         }
         getLogger().log(Level.INFO, String.format("Unbanned %s", playerName));
     }
 
     private void banPlayer(String playerName, long banDuration, String senderName, String reason) {
         Player player = getServer().getPlayer(playerName);
         String humanReadableTime = longToReadableTime(banDuration);
         String kickReason = getConfig().getString("kickmessage").replace("$time", humanReadableTime);
         Long banLiftTime = (System.currentTimeMillis() / 1000) + banDuration;
 
         if (player != null) {
             player.getInventory().clear();
             player.kickPlayer(kickReason);
         }
 
         synchronized (banDatabaseLock) {
             banDatabase.put(playerName.toLowerCase(), banLiftTime);
             if (senderName == null) {
                 // Player was banned for death
                 getLogger().log(Level.INFO, String.format("%s and is banned for %s", reason, humanReadableTime));
             } else {
                 getLogger().log(Level.INFO, String.format("%s banned %s for %s", senderName, playerName, humanReadableTime));
 
                 // Notify sender
                 Player sender = getServer().getPlayer(senderName);
                 if (sender != null) {
                    sender.sendMessage(String.format("%s is now banned for %s.", player.getName(), humanReadableTime));
                 }
             }
             saveBanDB();
         }
     }
 }

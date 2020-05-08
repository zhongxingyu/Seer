 package com.github.sar3th.DeathBan;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
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
 
     @Override
     public void onEnable() {
         reloadBanDB();
         // Register as event Listener
         getServer().getPluginManager().registerEvents(this, this);
     }
 
     @EventHandler
     public void onPlayerLogin(PlayerLoginEvent event) {
         if (isEnabled()) {
             // TODO: Add code to kick player if he's banned
         }
     }
 
     @EventHandler
     public void onEntityDeathEvent(EntityDeathEvent event) {
         if (isEnabled()) {
             // TODO: Add code to detect player death and add a ban
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (command.getName().equalsIgnoreCase("db-settime")) {
             if (args.length == 1) {
                 int banTime = Integer.parseInt(args[0]);
 
                 getConfig().set("bantime", banTime);
                 getLogger().log(Level.INFO, String.format("%s set bantime to %d", sender.getName(), banTime));
                 saveConfig();
 
                 return true;
             } else {
                 return false;
             }
         } else if (command.getName().equalsIgnoreCase("db-ban")) {
             if (args.length == 2) {
                 Player victim = getServer().getPlayer(args[0]);
                 long banTime = Integer.parseInt(args[1]);
 
                 if (killPlayer(victim.getName(), sender.getName())) {
                     sender.sendMessage(String.format("Successfully killed %s", victim.getName()));
                 } else {
                     sender.sendMessage(String.format("Unable to kill %s", victim.getName()));
                 }
 
                 // Add to banlist regardless
                 banPlayer(victim.getName(), banTime, sender.getName());
 
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
             banDatabase = (HashMap<String, Long>) banStorage.get("banlist", new HashMap<String, Long>());
         }
        getLogger().log(Level.INFO, String.format("Loaded %d bans from ban storage", banDatabase.size()));
     }
 
     private void saveBanDB() {
         synchronized (banDatabaseLock) {
             banStorage.set("banlist", banDatabase);
             File banDBFile = getBanDBFile();
             try {
                 banStorage.save(banDBFile);
             } catch (IOException ex) {
                getLogger().log(Level.SEVERE, String.format("Could not save banlist to %s", banDBFile.getName()), ex);
             }
         }
     }
 
     private void clearBanDB() {
         synchronized (banDatabaseLock) {
             banDatabase = new HashMap<String, Long>();
             saveBanDB();
         }
     }
 
     private boolean killPlayer(String victimName, String killerName) {
         Player victim = getServer().getPlayer(victimName);
 
         if (victim != null) {
             if (!victim.isDead()) {
                 getServer().getPluginManager().callEvent(new EntityDamageEvent(victim, EntityDamageEvent.DamageCause.SUICIDE, victim.getHealth()));
 
                 victim.damage(victim.getHealth());
                 getLogger().log(Level.INFO, String.format("%s killed by %s", victim.getName(), killerName));
             } else {
                 getLogger().log(Level.INFO, String.format("%s wanted to kill %s but player was already dead", killerName, victim.getName()));
             }
 
             return true;
         } else {
             return false;
         }
     }
 
     private void banPlayer(String playerName, int banDuration) {
         banPlayer(playerName, banDuration, null);
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
        if (remainingSeconds > 0) {
             if (remainingSeconds > 1) {
                 timeStrings.add(String.format("%d seconds", remainingSeconds));
             } else {
                 timeStrings.add(String.format("%d second", remainingSeconds));
             }
         }
 
         // Turn this into a nice, readable String
         // The code itself is not so nice, but the result will be :)
 
         if (timeStrings.size() > 1) {
             StringBuilder sb = new StringBuilder();
 
             sb.append(timeStrings.get(0));
 
             for (int i = 1; i < (timeStrings.size() - 1); i++) {
                 sb.append(String.format(" %s", timeStrings.get(i)));
             }
 
             sb.append(String.format(" and %s", timeStrings.get(timeStrings.size() - 1)));
 
             return sb.toString();
         } else {
             return timeStrings.get(0);
         }
     }
 
     private void banPlayer(String playerName, long banDuration, String senderName) {
         Player player = getServer().getPlayer(playerName);
         String humanReadableTime = longToReadableTime(banDuration);
         String kickReason = getConfig().getString("kickmessage").replace("$time", humanReadableTime);
         Long banLiftTime = (System.currentTimeMillis() / 1000) + banDuration;
 
         player.getInventory().clear();
         player.kickPlayer(kickReason);
         
         synchronized (banDatabaseLock) {
             banDatabase.put(player.getName(), banLiftTime);
         }
 
         if (senderName == null) {
             // Player was banned for death
             getLogger().log(Level.INFO, String.format("%s banned for %s", playerName, humanReadableTime));
         } else {
             getLogger().log(Level.INFO, String.format("%s banned %s for %s", senderName, playerName, humanReadableTime));
             Player sender = getServer().getPlayer(senderName);
             if (sender != null)
                 sender.sendMessage(String.format("%s is now banned for %s.", senderName, humanReadableTime));
         }
     }
 }

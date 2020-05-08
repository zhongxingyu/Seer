 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.cnaude.mutemanager;
 
 import java.text.DecimalFormat;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author cnaude
  */
 public class MuteManager extends JavaPlugin {
 
     // Mute list is stored as playername and milliseconds
     public HashMap<String, Long> mList = new HashMap<String, Long>();
     public HashMap<String, String> mReason = new HashMap<String, String>();
     private final MMListeners mmListeners = new MMListeners(this);
     public boolean configLoaded = false;
     public static MMConfig config;
     public static final String PLUGIN_NAME = "MuteManager";
     public static final String LOG_HEADER = "[" + PLUGIN_NAME + "]";
     static final Logger log = Logger.getLogger("Minecraft");
     private MMFile mFile = new MMFile(this);
     MMLoop mmLoop;
 
     @Override
     public void onEnable() {
         loadConfig();
         mFile.loadMuteList();
         mFile.loadMuteReasonList();
         getCommand("mute").setExecutor(new MMCommandMute(this));
         getCommand("unmute").setExecutor(new MMCommandUnMute(this));
         getCommand("mutelist").setExecutor(new MMCommandMuteList(this));
         getServer().getPluginManager().registerEvents(mmListeners, this);
         mmLoop = new MMLoop(this);
     }
 
     @Override
     public void onDisable() {
         mmLoop.end();
         mFile.saveMuteList();
         mFile.saveMuteReasonList();
         mList.clear();
         mReason.clear();
     }
 
     void loadConfig() {
         if (!this.configLoaded) {
             getConfig().options().copyDefaults(true);
             saveConfig();
             logInfo("Configuration loaded.");
             config = new MMConfig(this);
         } else {
             reloadConfig();
             getConfig().options().copyDefaults(false);
             config = new MMConfig(this);
             logInfo("Configuration reloaded.");
         }
         configLoaded = true;
     }
 
     public void logInfo(String _message) {
         log.log(Level.INFO, String.format("%s %s", LOG_HEADER, _message));
     }
 
     public void logError(String _message) {
         log.log(Level.SEVERE, String.format("%s %s", LOG_HEADER, _message));
     }
     
     public void logDebug(String _message) {
         if (config.debugEnabled()) {
             log.log(Level.INFO, String.format("%s [DEBUG] %s", LOG_HEADER, _message));
         }
     }
 
     public MMConfig getMConfig() {
         return config;
     }
 
     public void mutePlayer(Player player, Long muteTime, CommandSender sender, String reason) {
         long curTime = System.currentTimeMillis();
         long expTime = curTime + (muteTime * 60 * 1000);
         String pName = player.getName();
         mList.put(pName, expTime);
         mReason.put(pName, reason);
         String senderMessage = config.msgPlayerNowMuted();
         senderMessage = senderMessage.replaceAll("%PLAYER%", pName);
         senderMessage = senderMessage.replaceAll("%DURATION%", expireTime(pName));        
         if (!reason.isEmpty()) {
             senderMessage = senderMessage + ChatColor.YELLOW + ". " + config.msgReason() + ": " + ChatColor.RED + reason;
         }
         if (config.shouldNotify()) {
             getServer().broadcastMessage(senderMessage);
         } else {
             sender.sendMessage(senderMessage);            
             if (!config.msgYouHaveBeenMuted().isEmpty()) {
                 player.sendMessage(config.msgYouHaveBeenMuted().replaceAll("%DURATION%", expireTime(pName)));
             }
         }
     }
     
     public void mutePlayer(String pName, Long muteTime, CommandSender sender, String reason) {
         Player player = Bukkit.getServer().getPlayerExact(pName);
         long curTime = System.currentTimeMillis();
         long expTime = curTime + (muteTime * 60 * 1000);
         mList.put(pName, expTime);
         mReason.put(pName, reason);        
         String senderMessage = config.msgPlayerNowMuted();
         senderMessage = senderMessage.replaceAll("%PLAYER%", pName);
         senderMessage = senderMessage.replaceAll("%DURATION%", expireTime(pName));        
         if (!reason.isEmpty()) {
             senderMessage = senderMessage + ChatColor.YELLOW + ". " + config.msgReason() + ": " + ChatColor.RED + reason;            
         }
         if (config.shouldNotify()) {
             getServer().broadcastMessage(senderMessage);
         } else {
             sender.sendMessage(senderMessage);
             if (!config.msgYouHaveBeenMuted().isEmpty()) {
                 if (player != null) {
                     player.sendMessage(config.msgYouHaveBeenMuted().replaceAll("%DURATION%", expireTime(pName)));
                 }
             }
         }
     }
 
     public void unMutePlayer(String pName, CommandSender sender) {
         Player player = Bukkit.getServer().getPlayerExact(pName);
         String senderMessage = config.msgSenderUnMuted().replaceAll("%PLAYER%", pName);
         boolean success = unMutePlayer(pName);
         if (success) {
             if (config.shouldNotify()) {
                 getServer().broadcastMessage(senderMessage);
             } else {
                 logInfo(pName + " has been unmuted!");
                 if (!config.msgYouHaveBeenMuted().isEmpty()) {
                     if (player != null) {
                         player.sendMessage(config.msgYouHaveBeenUnMuted());
                     }
                 }
                 sender.sendMessage(senderMessage);
             }            
         } else {
             sender.sendMessage(config.msgUnableToUnMute().replaceAll("%PLAYER%", pName));
         }
     }
 
    public boolean unMutePlayer(String p) {  
        logDebug("Unmuting: " + p);
         String pName = p;
         for (String s : mList.keySet()) {
            if (s.equalsIgnoreCase(pName)) {
                 pName = s;
             }
         }
         if (mReason.containsKey(pName)) {
             mReason.remove(pName);
         } 
         if (mList.containsKey(pName)) {
             mList.remove(pName);
             return true;
         } else {
             return false;
         }
     }
 
     public boolean isMuted(Player player) {
         String pName = player.getName();
         if (mList.containsKey(pName)) {
             long curTime = System.currentTimeMillis();
             long expTime = mList.get(pName);
             if (expTime > curTime) {
                 return true;
             } else {
                 return false;
             }
         } else {
             return false;
         }
     }
 
     public String expireTime(Player player) {
         String pName = player.getName();
         return expireTime(pName);
     }
     
     public boolean isBlockedCmd(String cmd) {
         return getMConfig().blockedCmds().contains(cmd);
     }
 
     public String expireTime(String pName) {
         DecimalFormat formatter = new DecimalFormat("0.00");
         if (mList.containsKey(pName)) {
             long curTime = System.currentTimeMillis();
             long expTime = mList.get(pName);
             float diffTime = ((expTime - curTime) / 1000f) / 60f;
             if (diffTime > 5256000) {
                 return config.msgForever();
             }
             if (diffTime > 525600) {
                 return (formatter.format(diffTime / 525600f)) + " " + config.msgYears();
             }
             if (diffTime > 1440) {
                 return (formatter.format(diffTime / 1440f)) + " " + config.msgDays();
             }
             if (diffTime > 60) {
                 return (formatter.format(diffTime / 60f)) + " " + config.msgHOurs();
             }
             if (diffTime < 1f) {
                 return (formatter.format(diffTime * 60f)) + " " + config.msgSeconds();
             }
             return (formatter.format(diffTime)) + " " + config.msgMinutes();
         } else {
             return config.msgZeroSeconds();
         }
     }
     
     public Player lookupPlayer(String pName) {
         Player player;
         if (config.reqFullName()) {
             player = Bukkit.getPlayerExact(pName);
         } else {
             // First we attempt to get an exact match before partial match
             player = Bukkit.getPlayerExact(pName);
             if (player == null) {
                 for (Player pl : Bukkit.getOnlinePlayers()) {
                     if (pl.getName().toLowerCase().startsWith(pName.toLowerCase())) {
                         player = Bukkit.getPlayer(pName);
                         break;
                     }
                 }
             }
         }
         return player;
     }
 }

 package com.ementalo.tcl;
 
 import org.bukkit.configuration.Configuration;
 
 import java.util.Set;
 
 public class Config {
 
     public Configuration config = null;
     TeleConfirmLite parent = null;
     public static String accepteeMsg, denyeeMsg, accepterMsg, denierMsg;
     public static String fromMsg, tpToThemMsg, tpThemToYouMsg, acceptDenyPrompt;
     public static String playerHasPendingReq, requestSent, playerNotFound;
     public static String playerCommand, selfTp, toggledMsg, backUsage;
     public static String noBackLocation, removeReq, teleportation, allowed, disabled;
 
     public static boolean isDebug;
     public static String permissionDenied;
    public static boolean clearRequestsOnWorldChange, useBukkitPerms, preventCrossWorldTp;
     public static int requestTimeout;
 
     public Config(TeleConfirmLite parent) {
         this.parent = parent;
         config = parent.getConfig();
     }
 
     public void loadSettings() throws Exception {
         if (!parent.getDataFolder().exists()) {
             parent.getDataFolder().mkdirs();
         }
         final Set<String> keys = config.getKeys(true);
         if (!keys.contains("messages.accepteeMsg"))
             config.set("messages.accepteeMsg", "Accepted request.");
         if (!keys.contains("messages.denyeeMsg"))
             config.set("messages.denyeeMsg", "Denied request.");
         if (!keys.contains("messages.accepterMsg"))
             config.set("messages.accepterMsg", "Request to teleport accepted.");
         if (!keys.contains("messages.denierMsg"))
             config.set("messages.denierMsg", "Request to teleport denied.");
         if (!keys.contains("messages.fromMsg"))
             config.set("messages.fromMsg", "%p would like to %t");
         if (!keys.contains("messages.tpToThemMsg"))
             config.set("messages.tpToThemMsg", "teleport to you");
         if (!keys.contains("messages.tpThemToYouMsg"))
             config.set("messages.tpThemToYouMsg", "teleport you to them");
         if (!keys.contains("messages.acceptDenyPrompt"))
             config.set("messages.acceptDenyPrompt", "To accept this, type %a. To deny, type %d");
         if (!keys.contains("messages.playerHasPendingReq"))
             config.set("messages.playerHasPendingReq", "That player already has a pending request!");
         if (!keys.contains("messages.playerHasPendingReq"))
             config.set("messages.requestSent", "Request sent");
         if (!keys.contains("messages.playerNotFound"))
             config.set("messages.playerNotFound", "cPlayer %p not found");
         if (!keys.contains("messages.playerCommand"))
             config.set("messages.playerCommand", "cThis command is only available in game to players");
         if (!keys.contains("messages.selfTp"))
             config.set("messages.selfTp", "cYou cannot to yourself");
         if (!keys.contains("messages.toggledMsg"))
             config.set("messages.toggledMsg", "c%p 7 is not accepting requests");
         if (!keys.contains("messages.backUsage"))
             config.set("messages.backUsage", "7Teleporting you to your previous location");
         if (!keys.contains("messages.noBackLocation"))
             config.set("messages.noBackLocation", "7No previous location");
         if (!keys.contains("messages.removeReq"))
             config.set("messages.removeReq", "7Requests removed");
         if (!keys.contains("messages.teleportation"))
             config.set("messages.teleportation", "7Teleportation");
         if (!keys.contains("messages.allowed"))
             config.set("messages.allowed", "Allowed");
         if (!keys.contains("messages.disabled"))
             config.set("messages.disabled", "Disabled");
         if (!keys.contains("messages.permissionDenied"))
             config.set("messages.permissionDenied", "permissionDenied");
         if (!keys.contains("settings.clearRequestsOnWorldChange"))
             config.set("settings.clearRequestsOnWorldChange", false);
         if (!keys.contains("settings.useBukkitPerms"))
             config.set("settings.useBukkitPerms", false);
         if (!keys.contains("settings.requestTimeout"))
             config.set("settings.requestTimeout", 30);
        if (!keys.contains("settings.preventCrossWorldTp"))
            config.set("settings.preventCrossWorldTp", false);
 
         parent.saveConfig();
         parent.reloadConfig();
     }
 
     public void AssignSettings() {
         accepteeMsg = config.getString("messages.accepteeMsg", "Accepted request.");
         denyeeMsg = config.getString("messages.denyeeMsg", "Denied request.");
         accepterMsg = config.getString("messages.accepterMsg", "Request to teleport accepted.");
         denierMsg = config.getString("messages.denierMsg", "Request to teleport denied.");
         fromMsg = config.getString("messages.fromMsg", "%p would like to %t");
         tpToThemMsg = config.getString("messages.tpToThemMsg", "teleport to you");
         tpThemToYouMsg = config.getString("messages.tpThemToYouMsg", "teleport you to them");
         acceptDenyPrompt = config.getString("messages.acceptDenyPrompt", "To accept this, type %a. To deny, type %d");
         playerHasPendingReq = config.getString("messages.playerHasPendingReq", "That player already has a pending request!");
         requestSent = config.getString("messages.requestSent", "Request sent");
         playerNotFound = config.getString("messages.playerNotFound", "cPlayer %p not found");
         playerCommand = config.getString("messages.playerCommand", "cThis command is only available in game to players");
         selfTp = config.getString("messages.selfTp", "cYou cannot to yourself");
         toggledMsg = config.getString("messages.toggledMsg", "c%p 7 is not accepting requests");
         backUsage = config.getString("messages.backUsage", "7Teleporting you to your previous location");
         noBackLocation = config.getString("messages.noBackLocation", "7No previous location");
         removeReq = config.getString("messages.removeReq", "7Requests removed");
         teleportation = config.getString("messages.teleportation", "Teleportation");
         allowed = config.getString("messages.allowed", "Allowed");
         disabled = config.getString("messages.disabled", "Disabled");
         permissionDenied = config.getString("messages.permission", "7You do not have permission for that command");
         clearRequestsOnWorldChange = config.getBoolean("settings.clearRequestsOnWorldChange", false);
         useBukkitPerms = config.getBoolean("settings.useBukkitPerms", false);
         requestTimeout = config.getInt("settings.requestTimeout", 30);
         isDebug = config.getBoolean("settings.isDebug", false);
        preventCrossWorldTp = config.getBoolean("settings.preventCrossWorldTp", false);
     }
 }

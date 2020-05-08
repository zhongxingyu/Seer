 package net.daboross.bukkitdev.uberchat;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.metadata.MetadataValue;
 
 /**
  *
  * @author Dabo Ross
  */
 public class UberChatListener implements Listener {
 
     private static final String capsMessage = ChatColor.RED + Colorizor.colorize("I'm sorry, but your chat message contains to many uppercase letters.");
     private static final String chatFormat = ChatColor.YELLOW + "%s " + ChatColor.GRAY + "%s";
    private static final String longNick = Colorizor.colorize(UberChatHelpers.toggleCase("Your Name Is Very Long! use /nick to shorten it!"));
 
     public UberChatListener() {
         mapInit();
     }
 
     @EventHandler(priority = EventPriority.HIGH)
     public void onPlayerChatEvent(AsyncPlayerChatEvent evt) {
         if (evt.isCancelled()) {
             return;
         }
         format(evt);
         andColorCheck(evt);
         if (!whatCheck(evt)) {
             swearCheck(evt);
             toggleCheck(evt);
             capsCheck(evt);
             colorCheck(evt);
             nameCheck(evt);
         }
     }
 
     private void format(AsyncPlayerChatEvent evt) {
         evt.setFormat(chatFormat);
     }
 
     private void colorCheck(AsyncPlayerChatEvent evt) {
         Player p = evt.getPlayer();
         if (p.hasMetadata("isMessageColorOn")) {
             List<MetadataValue> meta = p.getMetadata("isMessageColorOn");
             if (meta.size() >= 1) {
                 if (meta.get(0).asBoolean()) {
                     evt.setMessage(Colorizor.colorize(evt.getMessage()));
                 }
             }
         }
     }
 
     private void capsCheck(AsyncPlayerChatEvent evt) {
         Player p = evt.getPlayer();
         if (p.hasPermission("uberchat.ignorecaps")) {
             return;
         }
         String message = ChatColor.stripColor(evt.getMessage());
         int totalChars = message.length();
         int capChars = 0;
         int lowChars = 0;
         char[] charArray = message.toCharArray();
         for (char c : charArray) {
             if (Character.isUpperCase(c)) {
                 capChars++;
             } else if (Character.isLowerCase(c)) {
                 lowChars++;
             }
         }
         if ((capChars > (lowChars * 2)) && totalChars > 5 || (capChars > 9)) {
             p.sendMessage(capsMessage);
             evt.setMessage(UberChatHelpers.toggleCase(evt.getMessage()));
         }
     }
 
     private void nameCheck(AsyncPlayerChatEvent evt) {
         String name = ChatColor.stripColor(evt.getPlayer().getDisplayName());
         if (name.length() > 22) {
            evt.getPlayer().sendMessage(longNick);
            evt.getPlayer().sendMessage(longNick);
            evt.getPlayer().sendMessage(longNick);
         }
     }
 
     private void toggleCheck(AsyncPlayerChatEvent evt) {
         Player p = evt.getPlayer();
         if (p.hasMetadata("isMessageToggleOn")) {
             List<MetadataValue> meta = p.getMetadata("isMessageToggleOn");
             if (meta.size() >= 1) {
                 if (meta.get(0).asBoolean()) {
                     evt.setMessage(UberChatHelpers.toggleCase(evt.getMessage()));
                 }
             }
         }
     }
 
     private boolean whatCheck(AsyncPlayerChatEvent evt) {
         String msg = ChatColor.stripColor(evt.getMessage()).toLowerCase();
         if (msg.equals("back") || msg.equals("im back") || msg.equals("i'm back")) {
             String fullDisplay = evt.getPlayer().getDisplayName();
             String[] nameSplit = fullDisplay.split(" ");
             String name = nameSplit[nameSplit.length - 1];
             Bukkit.getServer().broadcastMessage(UberChatHelpers.formatName("Announcer") + " " + name + ChatColor.GRAY + " Is Back" + ChatColor.DARK_GRAY + "!");
             evt.setCancelled(true);
             return true;
         } else {
             return false;
         }
     }
     private Map<String, String> swears = new HashMap<String, String>();
     private Map<String, Boolean> swearWord = new HashMap<String, Boolean>();
 
     private void mapInit() {
         swears.put("fuck", "barnacles");
         swearWord.put("fuck", false);
         swears.put("nigger", "happy");
         swearWord.put("nigger", false);
         swears.put("bitch", "lady");
         swearWord.put("bitch", false);
         swears.put("shit", "feces");
         swearWord.put("shit", false);
         swears.put("ass", "donkey");
         swearWord.put("ass", true);
         swears.put("wtf", "wth");
         swearWord.put("wtf", true);
         swears.put("crap", "feces");
         swearWord.put("crap", false);
         swears.put("fag", "apple");
         swearWord.put("fag", false);
         swears.put("pimp", "thief");
         swearWord.put("pimp", true);
         swears.put("dick", "fruit");
         swearWord.put("dick", false);
     }
 
     private void swearCheck(AsyncPlayerChatEvent evt) {
         String rawMessage = evt.getMessage();
         String msg = rawMessage;
         boolean msgNonColor = false;
         for (String rawSwear : swears.keySet()) {
             boolean word = swearWord.get(rawSwear);
             String rawReplacement = swears.get(rawSwear);
             String replacement = word ? (" " + rawReplacement + " ") : rawReplacement;
             String swearRegex = "(?i)" + (word ? (" " + rawSwear + " ") : rawSwear);
             msg = msg.replaceAll(swearRegex, replacement);
            if (word) {
                if (msg.equals(rawSwear)) {
                    msg = rawReplacement;
                } else {
                    if (msg.toLowerCase().endsWith(" " + rawSwear.toLowerCase())) {
                        msg = msg.substring(0, msg.length() - rawSwear.length()).concat(rawReplacement);
                    }
                    if (msg.toLowerCase().startsWith(rawSwear.toLowerCase() + " ")) {
                        msg = rawReplacement.concat(msg.substring(rawSwear.length(), msg.length()));
                    }
                }
            }
             if (!msgNonColor) {
                 String noColorOrig = ChatColor.stripColor(msg);
                 String noColorMsg = noColorOrig;
                 if (noColorMsg.equals(msg)) {
                     msgNonColor = true;
                 } else {
                     noColorMsg = noColorMsg.replaceAll(swearRegex, replacement);
                     if (word) {
                         if (noColorMsg.equals(rawSwear)) {
                             noColorMsg = rawReplacement;
                         } else {
                             if (noColorMsg.toLowerCase().endsWith(" " + rawSwear.toLowerCase())) {
                                 noColorMsg = noColorMsg.substring(0, noColorMsg.length() - rawSwear.length()).concat(rawReplacement);
                             }
                             if (noColorMsg.toLowerCase().startsWith(rawSwear.toLowerCase() + " ")) {
                                 noColorMsg = rawReplacement.concat(noColorMsg.substring(rawSwear.length(), noColorMsg.length()));
                             }
                         }
                     }
                     if (!noColorOrig.equals(noColorMsg)) {
                         msg = noColorMsg;
                         msgNonColor = true;
                     }
                 }
             }
         }
         if (!rawMessage.equals(msg)) {
             evt.setMessage(msg);
         }
     }
 
     private void andColorCheck(AsyncPlayerChatEvent evt) {
         evt.setMessage(ChatColor.translateAlternateColorCodes('&', evt.getMessage()));
     }
 }

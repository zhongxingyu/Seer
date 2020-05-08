 package com.github.calenria.simplechat;
 
 import java.util.StringTokenizer;
 import java.util.logging.Logger;
 
 import me.zford.jobs.container.JobsPlayer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class ChatMessage {
 
     /**
      * Bukkit Logger.
      */
     private static Logger        log = Logger.getLogger("Minecraft");
 
     private Player               sender;
     private World                world;
     private Location             loc;
     private String               playerName;
     private String               server;
     private String               channel;
     private String               playerDisplayName;
     private String               message;
     private String               messageParsed;
     private String               format;
     private String               formatParsed;
     private String               spyFormat;
     private int                  length;
     private boolean              global;
     private boolean              team;
 
     private boolean              help;
 
     private boolean              pm;
     private boolean              lokal;
     private SimpleChat           plugin;
 
     private AsyncPlayerChatEvent event;
 
     public ChatMessage(AsyncPlayerChatEvent event, SimpleChat plugin) {
         this.plugin = plugin;
         this.event = event;
         this.sender = event.getPlayer();
         this.world = sender.getWorld();
         this.loc = sender.getLocation();
         this.message = event.getMessage().trim();
         this.server = plugin.config.getServer();
         this.length = message.length();
         this.global = message.startsWith("!");
         this.team = message.startsWith("#");
         this.help = message.startsWith("?");
         this.pm = message.startsWith("@");
         this.lokal = message.startsWith("~");
 
         this.playerName = event.getPlayer().getName();
         this.playerDisplayName = event.getPlayer().getDisplayName();
 
         if (!global && !help && !pm && !team) {
             lokal = true;
         }
 
         if (lokal && plugin.getChatter(sender.getName()).isConversion() && !message.startsWith("~")) {
             lokal = false;
             pm = true;
             this.message = "@" + plugin.getChatter(sender.getName()).getConversionPartner() + " " + this.message;
         }
 
         if (lokal && message.startsWith("~")) {
             this.message = message.substring(1);
         }
 
         this.channel = setChannel();
         this.format = setFormat();
         this.spyFormat = setSpyFormat();
 
         if (lokal && sender.hasPermission("simplechat." + channel.toLowerCase())) {
             event.setCancelled(true);
             sendLokalMessage();
             sendSpyMessage();
         } else if (global && sender.hasPermission("simplechat." + channel.toLowerCase())) {
             if (!sender.hasPermission("simplechat.gobal.off")) {
                 event.setCancelled(true);
                 sendServerMessage();
             } else {
                 event.setCancelled(true);
                 sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Du musst den globalen Chat erst wieder mit &4/globalchat&6 betreten bevor du etwas schreiben kannst!"));
                 return;
             }
         } else if (team && sender.hasPermission("simplechat." + channel.toLowerCase())) {
             event.setCancelled(true);
             sendServerTeamMessage();
         } else if (help && sender.hasPermission("simplechat." + channel.toLowerCase())) {
             event.setCancelled(true);
             sendServerMessage();
         } else if (pm && sender.hasPermission("simplechat." + channel.toLowerCase())) {
             event.setCancelled(true);
             sendPrivateMessage();
         } else {
             event.setCancelled(true);
             sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4Keine Berechtigung!"));
             log.info(String.format("%s hat keine berechtigung um %s zu schreiben!", this.playerDisplayName, this.channel));
         }
 
     }
 
     private void sendPrivateMessage() {
         if (message.equals("@@")) {
             sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Chatpartner (" + plugin.getChatter(this.sender.getName()).getConversionPartner() + ") gelöscht"));
             plugin.removeConversionPartner(this.sender.getName());
             return;
         }
         if (message.startsWith("@@")) {
             String parsedMessage = message.substring(2).trim();
             StringTokenizer st = new StringTokenizer(parsedMessage, " ");
             String conversionPartner = st.nextToken();
             String cPlayer = plugin.getOnlinePlayer(conversionPartner);
             if (cPlayer == null) {
                 sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4Fehler beim Ermitteln des Chat Partners. Offline oder Verschrieben? (" + conversionPartner + ")"));
                 return;
             } else {
                 plugin.setConversionPartner(this.sender.getName(), cPlayer);
                 if (st.hasMoreElements()) {
                     sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Diese Nachricht und alle weiteren werden an (" + cPlayer + ") gesendet. Zum Beenden &4@@&6 oder &4/w&6 eingeben"));
                 } else {
                     sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Alle weiteren Nachrichten werden an (" + cPlayer + ") gesendet. Zum Beenden &4@@&6 oder &4/w&6 eingeben"));
                     return;
                 }
             }
             message = message.substring(1).trim();
         }
 
         String parsedMessage = message.substring(1).trim();
         StringTokenizer st = new StringTokenizer(parsedMessage, " ");
         String conversionPartner = st.nextToken();
         this.message = this.message.replace("@" + conversionPartner, "").trim().replaceAll("  ", " ");
         this.message = this.message.replace("@ " + conversionPartner, "").trim();
         if (this.message.length() == 0) {
             sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4Fehler beim Ermitteln des Chat Partners. Offline oder Verschrieben? (" + conversionPartner + ")"));
             return;
         }
         String cPlayer = plugin.getOnlinePlayer(conversionPartner);
         if (cPlayer == null) {
             sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4Fehler beim Ermitteln des Chat Partners. Offline oder Verschrieben? (" + conversionPartner + ")"));
             return;
         } else {
             String pMsg = "@#@pm@#@" + server + "@#@" + channel + "@#@" + sender.getName() + "@#@" + cPlayer + "@#@" + parsePrivateMessage(this.message, this.format, cPlayer);
             event.getPlayer().sendPluginMessage(plugin, "SimpleChat", pMsg.getBytes());
 
             pMsg = "@#@pmspy@#@" + server + "@#@pmspy@#@" + sender.getName() + "@#@" + parsePrivateMessage(this.message, this.spyFormat, cPlayer);
             event.getPlayer().sendPluginMessage(plugin, "SimpleChat", pMsg.getBytes());
         }
     }
 
     private String parsePrivateMessage(String message, String format, String to) {
         parseMessage(message, format);
         String parsedMessage = this.formatParsed;
         parsedMessage = parsedMessage.replace("<from>", this.playerName);
         String toFormat = plugin.config.getTo();
         OfflinePlayer toOffP = Bukkit.getOfflinePlayer(to);
         if (plugin.chat != null && toOffP.isOnline()) {
             toFormat = toFormat.replace("<prefix>", plugin.chat.getPlayerPrefix(toOffP.getPlayer()));
             toFormat = toFormat.replace("<suffix>", plugin.chat.getPlayerSuffix(toOffP.getPlayer()));
             toFormat = toFormat.replace("<group>", plugin.chat.getPrimaryGroup(toOffP.getPlayer()));
         } else if (plugin.chat != null && !toOffP.isOnline()) {
             toFormat = toFormat.replace("<prefix>", plugin.chat.getPlayerPrefix(sender.getWorld(), to));
             toFormat = toFormat.replace("<suffix>", plugin.chat.getPlayerSuffix(sender.getWorld(), to));
             toFormat = toFormat.replace("<group>", plugin.chat.getPrimaryGroup(sender.getWorld(), to));
         } else {
             toFormat = toFormat.replace("<prefix>", "");
             toFormat = toFormat.replace("<suffix>", "");
             toFormat = toFormat.replace("<group>", "");
         }
         toFormat = toFormat.replace("<player>", to);
         parsedMessage = parsedMessage.replace("<to>", toFormat);
         parsedMessage = ChatColor.translateAlternateColorCodes('&', parsedMessage);
         log.info(parsedMessage);
         return parsedMessage;
     }
 
     private void sendServerTeamMessage() {
         this.messageParsed = parseMessage(this.message, this.format);
         String pMsg = "@#@message@#@" + server + "@#@" + channel + "@#@" + sender.getName() + "@#@" + this.formatParsed;
         event.getPlayer().sendPluginMessage(plugin, "SimpleChat", pMsg.getBytes());
         Bukkit.broadcast(this.formatParsed, "simplechat." + channel.toLowerCase());
         log.info(this.formatParsed);
     }
 
     private void sendSpyMessage() {
         String pMsg = "@#@spy@#@" + server + "@#@spy@#@" + sender.getName() + "@#@" + parseMessage(this.message, this.spyFormat);
         event.getPlayer().sendPluginMessage(plugin, "SimpleChat", pMsg.getBytes());
     }
 
     private void sendServerMessage() {
         this.messageParsed = parseMessage(this.message, this.format);
         String pMsg = "@#@message@#@" + server + "@#@" + channel + "@#@" + sender.getName() + "@#@" + this.formatParsed;
         event.getPlayer().sendPluginMessage(plugin, "SimpleChat", pMsg.getBytes());
 
         Player[] players = Bukkit.getOnlinePlayers();
         for (Player player : players) {
             if (player.hasPermission("simplechat." + this.channel.toLowerCase()) && !player.hasPermission("simplechat.gobal.off")) {
                 player.sendMessage(this.formatParsed);
             }
         }
         log.info(this.formatParsed);
     }
 
     private String setChannel() {
         if (global) {
             return "Global";
         }
         if (lokal) {
             return "Lokal";
         }
         if (team) {
             return "Admin";
         }
         if (help) {
             return "Hilfe";
         }
         if (pm) {
             return "Privat";
         }
         return "Undefiniert";
     }
 
     /**
      * @return the format
      */
     public String getFormat() {
         return format;
     }
 
     /**
      * @return the length
      */
     public int getLength() {
         return length;
     }
 
     /**
      * @return the message
      */
     public String getMessage() {
         return message;
     }
 
     /**
      * @return the messageParsed
      */
     public String getMessageParsed() {
         return messageParsed;
     }
 
     /**
      * @return the playerDisplayName
      */
     public String getPlayerDisplayName() {
         return playerDisplayName;
     }
 
     /**
      * @return the playerName
      */
     public String getPlayerName() {
         return playerName;
     }
 
     /**
      * @return the plugin
      */
     public SimpleChat getPlugin() {
         return plugin;
     }
 
     /**
      * @return the spyFormat
      */
     public String getSpyFormat() {
         return spyFormat;
     }
 
     /**
      * @return the global
      */
     public boolean isGlobal() {
         return global;
     }
 
     /**
      * @return the help
      */
     public boolean isHelp() {
         return help;
     }
 
     /**
      * @return the lokal
      */
     public boolean isLokal() {
         return lokal;
     }
 
     /**
      * @return the pm
      */
     public boolean isPm() {
         return pm;
     }
 
     /**
      * @return the team
      */
     public boolean isTeam() {
         return team;
     }
 
     private String parseMessage(String message, String format) {
 
         String parsedMessage = message;
         String parsedFormat = format;
         if (global || help || team) {
             parsedMessage = parsedMessage.substring(1);
         }
 
         if (plugin.chat != null) {
             parsedFormat = parsedFormat.replace("<prefix>", plugin.chat.getPlayerPrefix(sender));
             parsedFormat = parsedFormat.replace("<suffix>", plugin.chat.getPlayerSuffix(sender));
             parsedFormat = parsedFormat.replace("<group>", plugin.chat.getPrimaryGroup(sender));
         } else {
             parsedFormat = parsedFormat.replace("<prefix>", "");
             parsedFormat = parsedFormat.replace("<suffix>", "");
             parsedFormat = parsedFormat.replace("<group>", "");
         }
 
         parsedFormat = ChatColor.translateAlternateColorCodes('&', parsedFormat);
         parsedMessage = ChatColor.translateAlternateColorCodes('&', parsedMessage);
         parsedFormat = parsedFormat.replace("<server>", plugin.config.getServer());
 
         if (SimpleChat.jobs) {
             JobsPlayer jPlayer = SimpleChat.jobsPlugin.getPlayerManager().getJobsPlayer(this.playerName);
             String honorific = "";
             if (jPlayer != null)
                 honorific = jPlayer.getDisplayHonorific();
             parsedFormat = parsedFormat.replace("{jobs}", honorific);
         }
 
         if (lokal) {
             parsedFormat = parsedFormat.replace("<player>", this.playerName);
             parsedFormat = parsedFormat.replace("<msg>", parsedMessage);
             parsedFormat = parsedFormat.replace("  ", " ");
             this.event.setFormat(parsedFormat);
             return parsedFormat;
         } else {
             this.formatParsed = parsedFormat.replace("<player>", this.playerName).replace("<msg>", parsedMessage).replace("  ", " ");
             parsedFormat = parsedFormat.replace("<player>", "%1$s");
             parsedFormat = parsedFormat.replace("<msg>", "%2$s");
             parsedMessage = parsedMessage.replace("  ", " ");
             this.event.setFormat(parsedFormat);
             this.event.setMessage(parsedMessage);
             return parsedMessage;
         }
 
     }
 
     private void sendLokalMessage() {
         this.messageParsed = parseMessage(this.message, this.format);
         int cnt = 0;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
             if (!onlinePlayer.equals(sender)) {
                 final Location playerLoc = onlinePlayer.getPlayer().getLocation();
                 boolean abort = false;
                 if (playerLoc.getWorld() != world) {
                     abort = true;
                 }
                 final double delta = playerLoc.distance(loc);
                 if (delta > plugin.config.getRadius()) {
                     abort = true;
                 }
                 if (abort) {
                     continue;
                 }
             }
             onlinePlayer.sendMessage(this.messageParsed);
             cnt++;
         }
 
         if (cnt <= 1) {
             sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Keiner hört dich..."));
         }
         log.info(this.event.getFormat());
     }
 
     private String setFormat() {
         if (global) {
             return plugin.config.getGlobal();
         }
         if (help) {
             return plugin.config.getHilfe();
         }
         if (pm) {
             return plugin.config.getSrvpm();
         }
         if (lokal) {
             return plugin.config.getLokal();
         }
         if (team) {
             return plugin.config.getTeam();
         }
         return ChatColor.translateAlternateColorCodes('&', "&4Es ist ein Fehler aufgetreten!");
     }
 
     private String setSpyFormat() {
         if (pm) {
             return plugin.config.getPmSpy();
         } else {
             return plugin.config.getSpy();
         }
     }
 }

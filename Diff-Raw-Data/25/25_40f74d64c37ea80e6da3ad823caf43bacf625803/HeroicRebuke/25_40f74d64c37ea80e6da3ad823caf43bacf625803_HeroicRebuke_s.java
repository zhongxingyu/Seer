 package com.bitzcraftonline.rebuke;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class HeroicRebuke extends JavaPlugin {
   public static HashMap<String, Warning> warnings;
   public static HashMap<String, ArrayList<String>> lists;
   public int noDatabaseIndex = 1;
 
   private final HeroicRebukeListener listener = new HeroicRebukeListener(this);
   private HeroicRebukeDatasource database;
   public PluginDescriptionFile pdfFile;
   public String name;
   public String version;
   public File dataFolder;
   public static final Logger log = Logger.getLogger("Minecraft");
 
   private static String maindir = "plugins/HeroicRebuke/";
   private final File configfile = new File(new StringBuilder().append(maindir).append("config.yml").toString());
   public RandomString codeGen;
   public String timestampFormat;
   public String consoleSender = "SERVER";
   public String messageColor = ChatColor.RED.toString();
   public String nameColor = ChatColor.DARK_AQUA.toString();
   public String infoColor = ChatColor.GOLD.toString();
   public boolean blockMove;
   public static boolean useDB;
   public boolean useCode;
   public boolean onlyWarnOnline;
   public boolean canAcknowledge;
   public String permissionSystem;
   public List<String> rebukeAdmins;
   public int maxPerPage;
   public int codeLength;
   public String mySqlDir;
   public String mySqlUser;
   public String mySqlPass;
   public String dbType;
   public boolean useBan;
   public int banThreshold;
   public String banMessage;
   public static final Boolean debugging = Boolean.valueOf(false);
 
   public void onEnable()
   {
     new File(maindir).mkdir();
     if (!this.configfile.exists())
       try {
         this.configfile.createNewFile();
       } catch (IOException ex) {
         Logger.getLogger(HeroicRebuke.class.getName()).log(Level.SEVERE, null, ex);
       }
     try
     {
       getConfig().load(this.configfile);
     } catch (FileNotFoundException ex) {
       Logger.getLogger(HeroicRebuke.class.getName()).log(Level.SEVERE, null, ex);
     } catch (IOException ex) {
       Logger.getLogger(HeroicRebuke.class.getName()).log(Level.SEVERE, null, ex);
     } catch (InvalidConfigurationException ex) {
       Logger.getLogger(HeroicRebuke.class.getName()).log(Level.SEVERE, null, ex);
     }
 
     this.blockMove = true;
     warnings = new HashMap();
     lists = new HashMap();
     this.pdfFile = getDescription();
     this.name = this.pdfFile.getName();
     this.version = this.pdfFile.getVersion();
     this.dataFolder = getDataFolder();
     PluginManager pm = getServer().getPluginManager();
     pm.registerEvents(this.listener, this);
 
     this.messageColor = getConfigColor("colors.message", "RED");
     this.nameColor = getConfigColor("colors.name", "DARK_AQUA");
     this.infoColor = getConfigColor("colors.info", "GOLD");
     this.timestampFormat = getConfig().getString("options.timeformat", "MM/dd/yyyy HH:mm:ss z");
     this.permissionSystem = getConfig().getString("options.permissions", "Permissions");
     this.useCode = getConfig().getBoolean("options.code.use", true);
     this.useCode = getConfig().getBoolean("options.code.use", true);
     this.codeLength = getConfig().getInt("options.code.length", 6);
     this.canAcknowledge = getConfig().getBoolean("options.canAcknowledge", true);
     this.consoleSender = getConfig().getString("options.server_name", "SERVER");
     this.blockMove = getConfig().getBoolean("options.block_move", true);
     this.onlyWarnOnline = getConfig().getBoolean("options.only_warn_online", false);
     this.maxPerPage = getConfig().getInt("options.lines_per_page", 5);
     this.mySqlDir = getConfig().getString("options.mysql.location", "localhost:3306/HeroicRebuke");
     this.mySqlUser = getConfig().getString("options.mysql.username", "root");
     this.mySqlPass = getConfig().getString("options.mysql.password", "");
     this.useBan = getConfig().getBoolean("options.ban.enable", false);
     this.banThreshold = getConfig().getInt("options.ban.threshold", 5);
     this.banMessage = getConfig().getString("options.ban.message", "[HeroicRebuke] Banned for cumulative violations!");
 
     this.dbType = getConfig().getString("options.database", "sqlite");
     if ((this.dbType.equalsIgnoreCase("sqlite")) || (this.dbType.equalsIgnoreCase("true"))) {
       useDB = true;
       this.database = new HeroicRebukeSQLite(this);
     } else if (this.dbType.equalsIgnoreCase("mysql")) {
       useDB = true;
       this.database = new HeroicRebukeMySQL(this);
     } else {
       useDB = false;
     }
 
     this.codeGen = new RandomString(this.codeLength);
     if (useDB)
       this.database.initDB();
     else {
       log.log(Level.INFO, MessageFormat.format("[{0}] No database enabled, warnings will not persist.", new Object[] { this.name }));
     }
 
     saveConfig();
     log.log(Level.INFO, MessageFormat.format("[{0}] enabled.", new Object[] { this.name }));
   }
 
   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
   {
     Player player = null;
 
     Warning isWarned = null;
    String senderName;
     if ((sender instanceof Player)) {
       player = (Player)sender;
      String senderName = player.getName();
      isWarned = (Warning)warnings.get(senderName.toLowerCase());
     } else {
       senderName = this.consoleSender;
     }
 
     if (args.length < 1) {
       if (isWarned != null) {
         sendWarning(player, isWarned);
         return true;
       }
       return false;
     }
 
     if (args[0].equalsIgnoreCase("add")) {
       if (!sender.hasPermission("heroicrebuke.add")) {
         return false;
       }
       if (args.length < 3) {
         sender.sendMessage(new StringBuilder().append(this.messageColor).append("Usage: ").append(this.infoColor).append("/warn add <name> <reason>").toString());
         return true;
       }
       if (warnings.containsKey(args[1].toLowerCase())) {
         sender.sendMessage(new StringBuilder().append(this.nameColor).append(args[1]).append(this.messageColor).append(" is already being warned by ").append(this.nameColor).append(((Warning)warnings.get(args[1].toLowerCase())).getSender()).append(this.messageColor).append(".").toString());
         return true;
       }
       StringBuilder result = new StringBuilder();
       result.append(args[2]);
       if (args.length > 3) {
         for (int i = 3; i < args.length; i++) {
           result.append(" ");
           result.append(args[i]);
         }
       }
       String message = result.toString();
       Player p = null;
       String target = args[1];
 
       List pList = getServer().matchPlayer(args[1]);
       if (!pList.isEmpty()) {
         if (pList.size() > 1) {
           String buildMessage = new StringBuilder().append(this.infoColor).append("Error: ").append(this.messageColor).append("Found multiple players matching ").append(this.nameColor).append(args[1]).append(this.messageColor).append(": ").toString();
           Iterator it = pList.iterator();
           while (it.hasNext()) {
             buildMessage = new StringBuilder().append(buildMessage).append(this.nameColor).append(((Player)it.next()).getName()).append(this.messageColor).append(", ").toString();
           }
           buildMessage = new StringBuilder().append(buildMessage).append("please be more specific.").toString();
           sender.sendMessage(buildMessage);
           return true;
         }
         p = (Player)pList.get(0);
         target = p.getName();
       }
 
       int curWarnings = this.database.countWarnings(target);
       if ((this.useBan) && (curWarnings + 1 >= this.banThreshold)) {
         if (p != null) {
           p.setBanned(true);
           if (p.isOnline()) {
             p.kickPlayer(this.banMessage);
           }
         }
         sender.sendMessage(new StringBuilder().append(this.nameColor).append(target).append(this.messageColor).append(" has been banned for cumulative violations.").toString());
         return true;
       }
       if ((p == null) || (!p.isOnline())) {
         if (!this.onlyWarnOnline) {
           makeWarning(target, senderName, message);
           sender.sendMessage(new StringBuilder().append(this.nameColor).append(target).append(this.messageColor).append(" is either offline or not a player, but has been warned.").toString());
         } else {
           sender.sendMessage(new StringBuilder().append(this.infoColor).append("Error: ").append(this.nameColor).append(target).append(this.messageColor).append(" is either offline or not a player!").toString());
         }
         return true;
       }
 
       Warning w = makeWarning(p.getName(), senderName, message);
       sendWarning(p, w);
       this.listener.rootPlayer(p);
       sender.sendMessage(new StringBuilder().append(this.nameColor).append(p.getName()).append(this.messageColor).append(" is online and has been warned.").toString());
 
       return true;
     }
 
     if (args[0].equalsIgnoreCase("clear")) {
       if (!sender.hasPermission("heroicrebuke.clear")) {
         return false;
       }
       if (args.length < 2) {
         sender.sendMessage(new StringBuilder().append(this.messageColor).append("Usage: ").append(this.infoColor).append("/warn clear <name>").toString());
         return true;
       }
       List matchList = new ArrayList();
       String matchName = null;
       for (String warnKey : warnings.keySet()) {
         if (args[1].equalsIgnoreCase(warnKey)) {
           matchList.clear();
           matchList.add(args[1]);
           break;
         }
         if (warnKey.toLowerCase().indexOf(args[1].toLowerCase()) != -1) {
           matchList.add(warnKey);
         }
       }
       if (!matchList.isEmpty()) {
         if (matchList.size() > 1) {
           String buildMessage = new StringBuilder().append(this.infoColor).append("Error: ").append(this.messageColor).append("Found multiple warned players matching ").append(this.nameColor).append(args[1]).append(this.messageColor).append(": ").toString();
           Iterator it = matchList.iterator();
           while (it.hasNext()) {
             buildMessage = new StringBuilder().append(buildMessage).append(this.nameColor).append(((Warning)warnings.get(((String)it.next()).toLowerCase())).getTarget()).append(this.messageColor).append(", ").toString();
           }
           buildMessage = new StringBuilder().append(buildMessage).append("please be more specific.").toString();
           sender.sendMessage(buildMessage);
           return true;
         }
         matchName = (String)matchList.get(0);
       }
 
       if ((matchName == null) || (!warnings.containsKey(matchName.toLowerCase()))) {
         sender.sendMessage(new StringBuilder().append(this.nameColor).append(args[1]).append(this.messageColor).append(" not found or has no active warnings.").toString());
         return true;
       }
 
       if (useDB) {
         this.database.clearWarning(matchName);
       }
 
       sender.sendMessage(new StringBuilder().append(this.messageColor).append("Removed active warning from ").append(this.nameColor).append(((Warning)warnings.get(matchName.toLowerCase())).getTarget()).toString());
       warnRemoval(matchName, senderName);
 
       return true;
     }
 
     if ((args[0].equalsIgnoreCase("del")) || (args[0].equalsIgnoreCase("delete"))) {
       if (!sender.hasPermission("heroicrebuke.delete")) {
         return false;
       }
       if (args.length < 2) {
         sender.sendMessage(new StringBuilder().append(this.messageColor).append("Usage: ").append(this.infoColor).append("/warn delete <index>").toString());
         return true;
       }
       if (!useDB) {
         sender.sendMessage(new StringBuilder().append(this.messageColor).append("The delete command is only available when using a database.").toString());
         return true;
       }
       int index;
       try {
         index = Integer.parseInt(args[1].trim());
       } catch (NumberFormatException e) {
         sender.sendMessage(new StringBuilder().append(this.infoColor).append("Error: ").append(this.messageColor).append("Bad number format. ").append(this.infoColor).append("<index>").append(this.messageColor).append(" must represent a valid index number.").toString());
         return true;
       }
       Warning w = getFromId(index);
       if (w != null) {
         warnRemoval(w.getTarget(), senderName);
       }
 
       String result = this.database.delWarning(Integer.valueOf(index));
       if (result != null)
         sender.sendMessage(new StringBuilder().append(this.messageColor).append("Deleted warning with index [").append(this.infoColor).append(index).append(this.messageColor).append("] on player [").append(this.nameColor).append(result).append(this.messageColor).append("]").toString());
       else {
         sender.sendMessage(new StringBuilder().append(this.messageColor).append("No warning found with index [").append(this.infoColor).append(index).append(this.messageColor).append("]").toString());
       }
       return true;
     }
 
     if ((args[0].equalsIgnoreCase("ack")) || (args[0].equalsIgnoreCase("acknowledge"))) {
       if (!this.canAcknowledge) {
         sender.sendMessage(new StringBuilder().append(this.infoColor).append("Error: ").append(this.messageColor).append("You may not acknowledge this warning").toString());
         return true;
       }
       String code = null;
       if (this.useCode) {
         if (args.length < 2) {
           sender.sendMessage(new StringBuilder().append(this.messageColor).append("Usage: ").append(this.infoColor).append("/warn ack <code>").toString());
           return true;
         }
         code = args[1].trim();
       }
       if (player != null)
         ackWarning(player, code);
       else {
         sender.sendMessage("The server is above the law.");
       }
       return true;
     }
     int i;
     if (args[0].equalsIgnoreCase("list")) {
       if (isWarned != null) {
         sendWarning(player, isWarned);
         return true;
       }
       if (!sender.hasPermission("heroicrebuke.list")) {
         return false;
       }
 
       int page = 1;
       i = 0;
      String target;
       if (sender.hasPermission("heroicrebuke.list.others"))
       {
        String target;
         if (args.length < 2)
          target = senderName;
         else
           try {
             page = Integer.parseInt(args[1].trim());
            target = senderName;
           } catch (NumberFormatException e) {
            String target = args[1].trim();
             if (args.length > 2)
               i = 2;
           }
       }
       else
       {
         target = senderName;
         if (args.length > 1) {
           i = 1;
         }
       }
       if (i > 0)
         try {
           page = Integer.parseInt(args[i].trim());
         } catch (NumberFormatException e) {
           sender.sendMessage(new StringBuilder().append(this.infoColor).append("Error: ").append(this.messageColor).append("Bad number format. Type ").append(this.infoColor).append("/warn list").append(this.messageColor).append(" without a page number to get acceptable range.").toString());
           return true;
         }
       else {
         lists.remove(target.toLowerCase());
       }
       if (!useDB) {
         sender.sendMessage(new StringBuilder().append(this.messageColor).append("The list command is only available when using a database.").toString());
         return true;
       }
 
       sendWarningList(target, sender, senderName, page);
 
       return true;
     }
 
     if (args[0].equalsIgnoreCase("active")) {
       if (!sender.hasPermission("heroicrebuke.active")) {
         return false;
       }
       int page = 1;
       if (args.length > 1)
         try {
           page = Integer.parseInt(args[1].trim());
         } catch (NumberFormatException e) {
           sender.sendMessage(new StringBuilder().append(this.infoColor).append("Error: ").append(this.messageColor).append("Bad number format. Type ").append(this.infoColor).append("/warn active").append(this.messageColor).append(" without a page number to get acceptable range.").toString());
           return true;
         }
       else {
         lists.remove(senderName.toLowerCase());
       }
       sendActiveList(sender, senderName, page);
       return true;
     }
 
     if (args[0].equalsIgnoreCase("info")) {
       if (!sender.hasPermission("heroicrebuke.info")) {
         return false;
       }
       if (args.length < 2) {
         sender.sendMessage(new StringBuilder().append(this.messageColor).append("Usage: ").append(this.infoColor).append("/warn info <index>").toString());
         return true;
       }
       int index = -1;
       try {
         index = Integer.parseInt(args[1].trim());
 
         if (index < 1)
           return false;
       }
       catch (NumberFormatException e)
       {
         sender.sendMessage(new StringBuilder().append(this.infoColor).append("Error: ").append(this.messageColor).append("Bad number format.").toString());
        return 1;
       } finally {
         if (index < 1) {
           return false;
         }
       }
       Warning w = getFromId(index);
       if ((w == null) && (!useDB)) {
         sender.sendMessage(new StringBuilder().append(this.messageColor).append("No warning found with index [").append(this.infoColor).append(index).append(this.messageColor).append("]").toString());
         return true;
       }
       if (useDB) {
         w = this.database.getWarning(index);
       }
       if (w == null) {
         sender.sendMessage(new StringBuilder().append(this.messageColor).append("No warning found with index [").append(this.infoColor).append(index).append(this.messageColor).append("]").toString());
         return true;
       }
       String send_time = getFormatTime(w.getSendTime());
       String ack_time = getFormatTime(w.getAckTime());
       String buildLine = new StringBuilder().append(this.messageColor).append("[").append(this.infoColor).append(w.getId()).append(this.messageColor).append("] ").append(this.infoColor).append(send_time).append(this.messageColor).append(" From: ").append(this.nameColor).append(w.getSender()).append(this.messageColor).append(" To: ").append(this.nameColor).append(w.getTarget()).toString();
 
       if (this.useCode) {
         buildLine = new StringBuilder().append(buildLine).append(this.messageColor).append(" Code: ").append(this.infoColor).append(w.getCode()).toString();
       }
       if (w.isAcknowledged()) {
         buildLine = new StringBuilder().append(buildLine).append(this.infoColor).append(" *ACK* ").append(this.messageColor).append("At: ").append(this.infoColor).append(ack_time).toString();
       }
       sender.sendMessage(buildLine);
       sender.sendMessage(new StringBuilder().append(this.messageColor).append("Message: ").append(w.getMessage()).toString());
       return true;
     }
 
     if (args[0].equalsIgnoreCase("help")) {
       sender.sendMessage(new StringBuilder().append(this.infoColor).append("===HeroicRebuke Commands===").toString());
       if (!sender.isOp()) {
         sender.sendMessage(new StringBuilder().append(this.infoColor).append("/warn ack ").append(this.useCode ? "(code) " : " ").append(this.messageColor).append("- Clears your active warning").append(this.useCode ? new StringBuilder().append(". Requires ").append(this.infoColor).append("(code)").append(this.messageColor).append(" from the warning").toString() : "").toString());
       }
       if (sender.hasPermission("heroicrebuke.add")) {
         sender.sendMessage(new StringBuilder().append(this.infoColor).append("/warn add <name> <reason>").append(this.messageColor).append(" - Warn ").append(this.infoColor).append("<name> ").append(this.messageColor).append("for ").append(this.infoColor).append("<reason>").toString());
       }
       if (sender.hasPermission("heroicrebuke.clear")) {
         sender.sendMessage(new StringBuilder().append(this.infoColor).append("/warn clear <name>").append(this.messageColor).append(" - Clear active warning of ").append(this.infoColor).append("<name>").toString());
       }
       if (sender.hasPermission("heroicrebuke.active")) {
         sender.sendMessage(new StringBuilder().append(this.infoColor).append("/warn active (page)").append(this.messageColor).append(" - Show all unacknowledged warnings").toString());
       }
       if (sender.hasPermission("heroicrebuke.info")) {
         sender.sendMessage(new StringBuilder().append(this.infoColor).append("/warn info <index>").append(this.messageColor).append(" - Display extended information about the given warning").toString());
       }
       if (useDB) {
         if (sender.hasPermission("heroicrebuke.list")) {
           sender.sendMessage(new StringBuilder().append(this.infoColor).append("/warn list ").append(sender.hasPermission("heroicrebuke.list.others") ? "<name> (page)" : " (page)").append(this.messageColor).append(" - List previous warnings").append(sender.hasPermission("heroicrebuke.list.others") ? new StringBuilder().append(" for ").append(this.infoColor).append("<name>").toString() : "").toString());
         }
         if (sender.hasPermission("heroicrebuke.delete")) {
           sender.sendMessage(new StringBuilder().append(this.infoColor).append("/warn delete <index>").append(this.messageColor).append(" - Permanently delete a warning; requires index number displayed by ").append(this.infoColor).append("list").append(this.messageColor).append(" or ").append(this.infoColor).append("active").toString());
         }
       }
       if (isWarned != null) {
         sendWarning(player, isWarned);
         return true;
       }
       return true;
     }
 
     if (isWarned != null) {
       sendWarning(player, isWarned);
       return true;
     }
     return false;
   }
 
   public void warnRemoval(String target, String senderName) {
     warnings.remove(target.toLowerCase());
     Player p = getServer().getPlayer(target);
     if ((p != null) && (p.isOnline())) {
       p.sendMessage(new StringBuilder().append(this.messageColor).append("Your warning was removed by ").append(this.nameColor).append(senderName).toString());
       HeroicRebukeListener.rootLocations.remove(p);
     }
   }
 
   public Warning getFromId(int id) {
     for (Warning w : warnings.values()) {
       if (w.getId().intValue() == id) {
         return w;
       }
     }
     return null;
   }
 
   public void sendWarningList(String target, CommandSender sender, String senderName, int page) {
     ArrayList curList = (ArrayList)lists.get(target.toLowerCase());
     if (curList == null) {
       curList = this.database.listWarnings(target);
       lists.put(target.toLowerCase(), curList);
     }
     if (curList.isEmpty()) {
       sender.sendMessage(new StringBuilder().append(this.nameColor).append(target).append(this.messageColor).append(" has received no warnings.").toString());
       return;
     }
     if (page < 1) {
       page = 1;
     }
     int numPages = (int)Math.ceil(curList.size() / this.maxPerPage);
     if (curList.size() % this.maxPerPage > 0) {
       numPages++;
     }
     if (page > numPages) {
       sender.sendMessage(new StringBuilder().append(this.messageColor).append("Bad page number, please issue ").append(this.infoColor).append("/warn list").append(this.messageColor).append(" command again without a page number to get acceptable range.").toString());
       return;
     }
     int startOfPage = (page - 1) * this.maxPerPage;
     int endOfPage = this.maxPerPage + (page - 1) * this.maxPerPage - 1;
     if (endOfPage >= curList.size()) {
       endOfPage = curList.size() - 1;
     }
     sender.sendMessage(new StringBuilder().append(this.messageColor).append("Warnings Matching [").append(this.nameColor).append(target).append(this.messageColor).append("] (Page ").append(this.infoColor).append(page).append(this.messageColor).append("/").append(this.infoColor).append(numPages).append(this.messageColor).append(") - Type ").append(this.infoColor).append("/warn info #").append(this.messageColor).append(" for details of a given warning.").toString());
     for (int i = startOfPage; i <= endOfPage; i++) {
       String msg = (String)curList.get(i);
       if (msg != null)
         sender.sendMessage(msg);
     }
   }
 
   public void sendWarningListMessages(String target, CommandSender sender, String senderName, int page)
   {
     ArrayList curList = (ArrayList)lists.get(target.toLowerCase());
     if (curList == null) {
       curList = this.database.listWarnings(target);
       lists.put(target.toLowerCase(), curList);
     }
     if (curList.isEmpty()) {
       sender.sendMessage(new StringBuilder().append(this.nameColor).append(target).append(this.messageColor).append(" has received no warnings.").toString());
       return;
     }
     if (page < 1) {
       page = 1;
     }
     int numPages = (int)Math.ceil(curList.size() / this.maxPerPage);
     if (curList.size() % this.maxPerPage > 0) {
       numPages++;
     }
     if (page > numPages) {
       sender.sendMessage(new StringBuilder().append(this.messageColor).append("Bad page number, please issue ").append(this.infoColor).append("/warn list").append(this.messageColor).append(" command again without a page number to get acceptable range.").toString());
       return;
     }
     int startOfPage = (page - 1) * this.maxPerPage;
     int endOfPage = this.maxPerPage + (page - 1) * this.maxPerPage - 1;
     if (endOfPage >= curList.size()) {
       endOfPage = curList.size() - 1;
     }
     sender.sendMessage(new StringBuilder().append(this.messageColor).append("Warnings Matching [").append(this.nameColor).append(target).append(this.messageColor).append("] (Page ").append(this.infoColor).append(page).append(this.messageColor).append("/").append(this.infoColor).append(numPages).append(this.messageColor).append(") - Type ").append(this.infoColor).append("/warn info #").append(this.messageColor).append(" for details of a given warning.").toString());
     for (int i = startOfPage; i <= endOfPage; i++) {
       String msg = (String)curList.get(i);
 
       if (msg != null)
         sender.sendMessage(msg);
     }
   }
 
   public void sendActiveList(CommandSender sender, String senderName, int page)
   {
     ArrayList curList = (ArrayList)lists.get(senderName.toLowerCase());
     if (curList == null) {
       curList = new ArrayList();
       for (Warning w : warnings.values()) {
         String send_time = getFormatTime(w.getSendTime());
         String buildLine = new StringBuilder().append(this.messageColor).append("[").append(this.infoColor).append(w.getId()).append(this.messageColor).append("] ").append(this.infoColor).append(send_time).append(this.messageColor).append(" From: ").append(this.nameColor).append(w.getSender()).append(this.messageColor).append(" To: ").append(this.nameColor).append(w.getTarget()).toString();
 
         curList.add(buildLine);
       }
       lists.put(senderName.toLowerCase(), curList);
     }
     if (curList.isEmpty()) {
       sender.sendMessage(new StringBuilder().append(this.messageColor).append("There are no active warnings!").toString());
       return;
     }
     if (page < 1) {
       page = 1;
     }
     int numPages = (int)Math.ceil(curList.size() / this.maxPerPage);
     if (curList.size() % this.maxPerPage > 0) {
       numPages++;
     }
     debug(new StringBuilder().append("List Size: ").append(curList.size()).append(" Pages: ").append(numPages).append(" Max: ").append(this.maxPerPage).toString());
     if (page > numPages) {
       sender.sendMessage(new StringBuilder().append(this.messageColor).append("Bad page number, please type ").append(this.infoColor).append("/warn active").append(this.messageColor).append(" without a page number to get acceptable range.").toString());
       return;
     }
     int startOfPage = (page - 1) * this.maxPerPage;
     int endOfPage = this.maxPerPage + (page - 1) * this.maxPerPage - 1;
     if (endOfPage >= curList.size()) {
       endOfPage = curList.size() - 1;
     }
     debug(new StringBuilder().append("Start: ").append(startOfPage).append(" End: ").append(endOfPage).toString());
     sender.sendMessage(new StringBuilder().append(this.messageColor).append("Active Warnings (Page ").append(this.infoColor).append(page).append(this.messageColor).append("/").append(this.infoColor).append(numPages).append(this.messageColor).append(") - Type ").append(this.infoColor).append("/warn info #").append(this.messageColor).append(" for details of a given warning.").toString());
     for (int i = startOfPage; i <= endOfPage; i++) {
       String msg = (String)curList.get(i);
       if (msg != null)
         sender.sendMessage(msg);
     }
   }
 
   public Warning makeWarning(String to, String from, String message)
   {
     Warning w = new Warning(to, from, message);
     if (this.useCode) {
       w.setCode(this.codeGen.nextString());
     }
     int index = this.noDatabaseIndex++;
     if (useDB) {
       index = this.database.newWarning(w);
     }
     w.setId(Integer.valueOf(index));
     warnings.put(to.toLowerCase(), w);
     return w;
   }
 
   public void sendWarning(Player p, Warning w) {
     if (w == null) {
       return;
     }
     String warnHeader = new StringBuilder().append(this.messageColor).append("[Warned by: ").append(this.nameColor).append(w.getSender()).append(this.messageColor).append("] ").append(w.getMessage()).toString();
     p.sendMessage(warnHeader);
     if (this.canAcknowledge) {
       String warnFooter = new StringBuilder().append("Type ").append(this.infoColor).append("/warn ack ").append(w.getCode() != null ? w.getCode() : "").append(this.messageColor).append(" to clear it.").toString();
       if (this.blockMove) {
         warnFooter = new StringBuilder().append(this.messageColor).append("Movement disabled; ").append(warnFooter).toString();
       }
       p.sendMessage(warnFooter);
     }
   }
 
   private void ackWarning(Player p, String code) {
     Warning w = (Warning)warnings.get(p.getName().toLowerCase());
     if (w == null) {
       p.sendMessage(new StringBuilder().append(this.messageColor).append("You are not currently warned.").toString());
       return;
     }
     if ((this.useCode) && (w.getCode() != null) && (!w.getCode().equalsIgnoreCase(code))) {
       p.sendMessage(new StringBuilder().append(this.infoColor).append("Error:").append(this.messageColor).append(" You must enter the correct code to acknowledge your warning.").toString());
       return;
     }
     p.sendMessage(new StringBuilder().append(this.messageColor).append("You have acknowledged your warning.").toString());
     String message = new StringBuilder().append(this.nameColor).append(p.getName()).append(this.messageColor).append(" acknowledged your warning.").toString();
     if (!w.getSender().equals(this.consoleSender))
       try {
         Player sender = getServer().getPlayer(w.getSender());
         sender.sendMessage(message);
       }
       catch (Exception e) {
       }
     else System.out.println(message.replaceAll("(?i)[0-F]", ""));
 
     for (Player playertn : Bukkit.getServer().getOnlinePlayers()) {
       if ((playertn.hasPermission("heroicrebuke.notify")) && (!playertn.getName().equalsIgnoreCase(w.getSender()))) {
         playertn.sendMessage(new StringBuilder().append(p.getDisplayName()).append(" acknowledged a warning from ").append(w.getSender()).append(" for ").append(w.getMessage()).toString());
       }
 
     }
 
     warnings.remove(p.getName().toLowerCase());
     HeroicRebukeListener.rootLocations.remove(p);
     if (useDB)
       this.database.ackWarning(p.getName());
   }
 
   public String getFormatTime(Long time)
   {
     if (time == null) {
       time = Long.valueOf(System.currentTimeMillis());
     }
     Date timestamp = new Date(time.longValue());
     try {
       SimpleDateFormat format = new SimpleDateFormat(this.timestampFormat);
       return format.format(timestamp);
     } catch (IllegalArgumentException e) {
       log.log(Level.SEVERE, MessageFormat.format("[{0}] Couldn't use provided timestamp format, using default.", new Object[] { this.name }));
       this.timestampFormat = "MM/dd/yyyy HH:mm:ss z";
       SimpleDateFormat format = new SimpleDateFormat(this.timestampFormat);
       return format.format(timestamp);
     }
   }
 
   public String getConfigColor(String property, String def) {
     String propColor = getConfig().getString(property, def);
     ChatColor returnColor;
     try {
       returnColor = ChatColor.valueOf(propColor);
     } catch (Exception e) {
       log.log(Level.INFO, MessageFormat.format("[{0}] Improper color definition in config.yml, using default.", new Object[] { this.name }));
       returnColor = ChatColor.valueOf(def);
     }
     return returnColor.toString();
   }
 
   public String getColorName(String colorCode) {
     try {
       return ChatColor.getByChar(colorCode.charAt(1)).name();
     } catch (NumberFormatException e) {
       log.log(Level.SEVERE, MessageFormat.format("[{0}] Unexpected error parsing color code: {1}, using default of WHITE", new Object[] { this.name, colorCode }));
     }return "WHITE";
   }
 
   public void saveConfig()
   {
     getConfig().set("colors.message", getColorName(this.messageColor));
     getConfig().set("colors.name", getColorName(this.nameColor));
     getConfig().set("colors.info", getColorName(this.infoColor));
     getConfig().set("options.timeformat", this.timestampFormat);
     getConfig().set("admins", this.rebukeAdmins);
     getConfig().set("options.code.use", Boolean.valueOf(this.useCode));
     getConfig().set("options.code.length", Integer.valueOf(this.codeLength));
     getConfig().set("options.canAcknowledge", Boolean.valueOf(this.canAcknowledge));
     getConfig().set("options.server_name", this.consoleSender);
     getConfig().set("options.block_move", Boolean.valueOf(this.blockMove));
     getConfig().set("options.only_warn_online", Boolean.valueOf(this.onlyWarnOnline));
     getConfig().set("options.lines_per_page", Integer.valueOf(this.maxPerPage));
     getConfig().set("options.mysql.location", this.mySqlDir);
     getConfig().set("options.mysql.username", this.mySqlUser);
     getConfig().set("options.mysql.password", this.mySqlPass);
     getConfig().set("options.database", this.dbType);
     getConfig().set("options.ban.enable", Boolean.valueOf(this.useBan));
     getConfig().set("options.ban.threshold", Integer.valueOf(this.banThreshold));
     getConfig().set("options.ban.message", this.banMessage);
     try {
       getConfig().save(this.configfile);
     } catch (IOException ex) {
       Logger.getLogger(HeroicRebuke.class.getName()).log(Level.SEVERE, null, ex);
     }
   }
 
   public static void debug(String message) {
     if (debugging.booleanValue())
       log.info(message);
   }
 
   public void onDisable()
   {
     if (useDB) {
       try {
         this.database.getConnection().close();
       } catch (SQLException e) {
         log.log(Level.SEVERE, MessageFormat.format("[{0}] Error closing database!", new Object[] { this.name }));
       }
     }
     log.log(Level.INFO, MessageFormat.format("[{0}] disabled", new Object[] { this.name, this.version }));
   }
 }

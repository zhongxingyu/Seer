 package com.minecraftserver.improvedsignlifts;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * SignLift for Bukkit
  */
 public class ImprovedSignLift extends JavaPlugin {
     protected final static Logger        logger         = Logger.getLogger("Minecraft");
     public static final String           name           = "ImprovedSignLift";
 
     private final SignLiftBlockListener  blockListener  = new SignLiftBlockListener(this);
     private final SignLiftPlayerListener playerListener = new SignLiftPlayerListener(this);
 
     private String                       liftString;
     private String                       liftUpString;
     private String                       liftDownString;
     private String                       defaultGoingUpString;
     private String                       defaultGoingDownString;
     private String                       goingUpStringFormat;
     private String                       goingDownStringFormat;
     private String                       normalOpen;
     private String                       normalClose;
     private String                       privateOpen;
     private String                       privateClose;
 
     private String                       deniedLift;
     private String                       deniedCreate;
     private String                       deniedDestroy;
 
     private Boolean                      sanityCheck;
     private HashMap<String, Location>    signEditStatus = new HashMap<String, Location>();
 
     public void loadConfiguration() {
         FileConfiguration cfg = this.getConfig();
 
         normalOpen = cfg.getString("string.normal.open", "[");
         normalClose = cfg.getString("string.normal.close", "]");
         privateOpen = cfg.getString("string.private.open", "{");
         privateClose = cfg.getString("string.private.close", "}");
         liftString = cfg.getString("string.lift", "LIFT");
         liftUpString = cfg.getString("string.up.lift", "LIFT UP");
         liftDownString = cfg.getString("string.down.lift", "LIFT DOWN");
         defaultGoingUpString = ChatColor.BLUE + "Going Up";
         defaultGoingDownString = ChatColor.BLUE + "Going Down";
         goingUpStringFormat = ChatColor.BLUE + "Going to %s";
         goingDownStringFormat = ChatColor.BLUE + "Going to %s";
         deniedLift = ChatColor.RED + "You don't have permission to use this lift";
         deniedCreate = ChatColor.RED + "You don't have permission to create that sign lift";
         deniedDestroy = ChatColor.RED + "You don't have permission to destroy that sign lift";
 
         sanityCheck = cfg.getBoolean("check.destination.paranoid", true);
 
     }
 
     public void saveConfiguration() {
         this.saveConfig();
     }
 
     public boolean getSanityCheck() {
         return sanityCheck;
     }
 
     public String getLiftString() {
         return liftString;
     }
 
     public String getLiftUpString() {
         return liftUpString;
     }
 
     public String getLiftDownString() {
         return liftDownString;
     }
 
     public String getDefaultGoingUpString() {
         return defaultGoingUpString;
     }
 
     public String getDefaultGoingDownString() {
         return defaultGoingDownString;
     }
 
     public String getGoingUpStringFormat() {
         return goingUpStringFormat;
     }
 
     public String getGoingDownStringFormat() {
         return goingDownStringFormat;
     }
 
     public String getDeniedLift() {
         return deniedLift;
     }
 
     public String getDeniedCreate() {
         return deniedCreate;
     }
 
     public String getDeniedDestroy() {
         return deniedDestroy;
     }
 
     public String getNormalOpen() {
         return normalOpen;
     }
 
     public String getNormalClose() {
         return normalClose;
     }
 
     public String getPrivateOpen() {
         return privateOpen;
     }
 
     public String getPrivateClose() {
         return privateClose;
     }
 
     public String shortPlayerName(String playerName) {
         if (playerName.length() > 15) {
             return playerName.substring(0, 15);
         }
         return playerName;
     }
 
     public boolean isBlockSignLift(Block block) {
         if (block.getType() == Material.SIGN || block.getType() == Material.WALL_SIGN) {
             Sign sign = (Sign) block.getState();
             String lineLift = sign.getLine(1).toString();
             String lineOwner = sign.getLine(3).toString();
             if (((lineLift.startsWith(getNormalOpen()) && lineLift.endsWith(getNormalClose())) || (lineLift
                     .startsWith(getPrivateOpen()) && lineLift.endsWith(getPrivateClose())))
                     && lineOwner.length() > 0) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * @param block
      * @return if the lift is private
      */
     public boolean isSignLiftPrivate(Block block) {
         Sign sign = (Sign) block.getState();
         String lineLift = sign.getLine(1).toString();
         if ((lineLift.startsWith(getPrivateOpen()) && lineLift.endsWith(getPrivateClose())))
             return true;
         return false;
     }
 
     public void executeSignLiftAction(Sign sign, Player player) {
         LiftSign ls = new LiftSign(sign, this);
         ls.activate(player);
     }
 
     public void addSignEditStatus(final Player player, final Sign sign) {
        signEditStatus.put(player.getName(), sign.getLocation());
    }

    public void remSignEditStatus(final Player player) {
        if (signEditStatus.containsKey(player.getName())) signEditStatus.remove(player.getName());
     }
 
     public boolean hasPlayerSignEditStatus(Player player) {
         return signEditStatus.containsKey(player.getName());
     }
 
     /*
      * (non-Javadoc)
      * @see
      * org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender
      * , org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
 
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (sender instanceof Player) {
             Player player = (Player) sender;
             if (cmd.getName().equalsIgnoreCase("sl")) {
                 if (hasPlayerSignEditStatus(player)) {
                     Location loc = signEditStatus.get(player.getName());
                     if (args.length != 0) {
                         if (args[0].equals("add")) {
                             if (args.length > 1) {
                                 for (String s : args)
                                     if (!s.equals(args[0])) {
                                         LiftDataManager.addMemberToLift(loc, player.getName(), s);
                                         player.sendMessage(ChatColor.GOLD + s + ChatColor.BLUE
                                                 + " has been added to the lift");
                                     }
                             } else player.sendMessage(ChatColor.AQUA
                                     + "Usage: /sl add <Player1> <Player2> <Player3> ...");
                         } else if (args[0].equals("rem") || args[0].equals("remove")) {
                             if (args.length > 1) {
                                 for (String s : args)
                                     if (!s.equals(args[0])) {
                                         LiftDataManager.remMemberFromLift(loc, player.getName(), s);
                                         player.sendMessage(ChatColor.GOLD + s + ChatColor.BLUE
                                                 + " has been removed from the lift");
                                     }
                             } else player.sendMessage(ChatColor.AQUA
                                     + "Usage: /sl remove <Player1> <Player2> <Player3> ...");
                         }
                     }
                 } else if (args.length > 0 && args[0].equals("version")) {
                     player.sendMessage(ChatColor.BLUE + "Version: " + ChatColor.GOLD
                             + this.getVersion() + " \n" + ChatColor.BLUE + "Made by "
                             + ChatColor.GOLD + "M0P\n" + ChatColor.BLUE
                             + "Based on Bukkit Plugin \"SignLift\" \n" + "Thanks to "
                             + ChatColor.GOLD + "AquaXV" + ChatColor.BLUE
                             + " for helping and testing alot.");
                 } else if (args.length > 0 && args[0].equals("help")) {
                     // TODO
                     player.sendMessage(ChatColor.BLUE
                            + "To create a public SignLift place a sign and add " + ChatColor.GOLD
                            + "[Lift up]" + ChatColor.BLUE + ", " + ChatColor.GOLD + "[Lift down]"
                            + ChatColor.BLUE + " or " + ChatColor.GOLD + "[Lift]" + ChatColor.BLUE
                            + " to the second line of the sign \n" + "Using " + ChatColor.GOLD
                            + "{ }" + ChatColor.BLUE
                            + " brackets instead creates a private SignLift \n"
                            + "You can add members to private SignLifts through " + ChatColor.GOLD
                            + "sneaking and right clicking the sign \n" + ChatColor.BLUE
                            + "You can find more help at the wiki or ask a Deity/Oracle ingame.");
                 } else player.sendMessage(ChatColor.AQUA
                         + "You have to select a sign lift first (sneak and right click it)");
             }
 
         }
         return true;
     }
 
     /*
      * (non-Javadoc)
      * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
      */
 
     private String getVersion() {
         return this.getDescription().getVersion();
     }
 
     @Override
     public void onEnable() {
         PluginManager pm = getServer().getPluginManager();
 
         loadConfiguration();
         saveConfiguration();
 
         pm.registerEvents(playerListener, this);
         pm.registerEvents(blockListener, this);
 
         LiftDataManager.init(this);
 
         log("Improved Sign Lifts - Version " + getVersion() + " is enabled");
 
     }
 
     @Override
     public void onDisable() {
         log("Improved Sign Lifts is Disabled");
     }
 
     public static void log(String txt) {
         logger.log(Level.INFO, String.format("[%s] %s", name, txt));
     }
 
 }

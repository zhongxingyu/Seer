 package com.meggawatts.MeggaChat;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.entity.Player;
 
 public class MeggaChat extends JavaPlugin {
 
     public static final Logger log = Logger.getLogger("Minecraft");
     public static HashMap adminschatting = new HashMap();
 
     File PEX = new File("plugins//PermissionsEx.jar");
     boolean PEXexists = PEX.exists();
     static String channelname;
     static ChatColor channelcolor;
     static ChatColor messagecolor;
     static ChatColor sendercolor;
     static boolean chaton;
     static boolean tabon;
     static boolean enton;
     static boolean dupeon;
     static boolean pipeson;
     static boolean flyon;
     static boolean signon;
 
     @Override
     public void onEnable() {
         setupConfig();
         registerEvents();
         setupBlacklist();
     }
 
     public void setupConfig() {
         getConfig().options().copyDefaults(true);
         saveConfig();
         channelname = getConfig().getString("channelname");
         channelcolor = ChatColor.valueOf(getConfig().getString("channelcolor"));
         messagecolor = ChatColor.valueOf(getConfig().getString("messagecolor"));
         sendercolor = ChatColor.valueOf(getConfig().getString("sendercolor"));
     }
 
     @Override
     public void onDisable() {
         // Clear hash map on disable
         adminschatting.clear();
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         String msg;
         if ((args.length > 0) && ((sender instanceof Player))) {
             Player player = (Player) sender;
             if (player.hasPermission("meggachat.admin") && label.equalsIgnoreCase("a")) {
                 if (args[0].equalsIgnoreCase("!on")) {
                     if (chaton) {
                         adminschatting.put(sender, true);
                         sender.sendMessage(ChatColor.DARK_GREEN + "Admin Chat enabled");
                     }
                 }
                 if (args[0].equalsIgnoreCase("!off")) {
                     if (chaton) {
                         adminschatting.remove(sender);
                         sender.sendMessage(ChatColor.DARK_RED + "Admin Chat disabled");
                     }
                 }
                 if (args[0].equalsIgnoreCase("!?")) {
                     if (chaton) {
                         sender.sendMessage(ChatColor.DARK_RED + "/a !on " + ChatColor.GREEN + "will toggle AdminChat mode on.");
                         sender.sendMessage(ChatColor.DARK_RED + "/a !off " + ChatColor.GREEN + "will toggle AdminChat mode off.");
                         sender.sendMessage(ChatColor.DARK_RED + "/a <message> " + ChatColor.GREEN + "will send the message to all who have access to adminchat.");
                     }
                 }
                 msg = args[0];
                 if (args.length > 1) {
                     for (int i = 1; i < args.length; i++) {
                         msg += " " + args[i];
                     }
                     sendToAdmins(msg, player);
                } else if (args.length == 1 && !(args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off"))) {
                     sendToAdmins(args[0], player);
                 }
             }
         }
         return true;
     }
 
     public static void sendToAdmins(String Message, Player chatter) {
         if (chaton) {
             log.info("[" + channelcolor + channelname + ChatColor.WHITE + "] " + sendercolor + chatter.getName() + ":" + ChatColor.WHITE + " " + messagecolor + Message);
             for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                 if (player.hasPermission("meggachat.admin")) {
                     player.sendMessage("[" + channelcolor + channelname + ChatColor.WHITE + "] " + sendercolor + chatter.getName() + ":" + ChatColor.WHITE + " " + messagecolor + Message);
                 }
             }
         }
 
     }
 
     private void registerEvents() {
         // Register only enabled events.
         chaton = getConfig().getBoolean("features.AdminChat");
         tabon = getConfig().getBoolean("features.TABList");
         enton = getConfig().getBoolean("features.EntityBlocking");
         dupeon = getConfig().getBoolean("features.DupeGravel");
         pipeson = getConfig().getBoolean("features.Pipes");
         flyon = getConfig().getBoolean("features.FlyingPermission");
         signon = getConfig().getBoolean("features.ColoredSigns");
         log.info("[MeggaChat] Enabling Selected Features.");
         if (tabon) {
             // Check for PEX
             if (PEXexists) {
                 getServer().getPluginManager().registerEvents(new ColoredListListener(), this);
                 log.info("[MeggaChat] Found PEX, colored list enabled.");
             } else {
                 log.info("[MeggaChat] PEX not found, colored list disabled.");
             }
         }
         if (chaton) {
             getServer().getPluginManager().registerEvents(new ChatListener(), this);
         }
         if (enton) {
             getServer().getPluginManager().registerEvents(new BlockDropListener(), this);
         }
         if (dupeon) {
             getServer().getPluginManager().registerEvents(new DupeListener(), this);
         }
         if (pipeson) {
             getServer().getPluginManager().registerEvents(new PipeListener(), this);
         }
         if (flyon) {
             getServer().getPluginManager().registerEvents(new PlayerListener(), this);
         }
         if (signon) {
             getServer().getPluginManager().registerEvents(new SignListener(), this);
         }
 
     }
 
     private void setupBlacklist() {
         List blacklist = getConfig().getList("dupeblacklist");
         for (Object o : blacklist) {
             DupeListener.blocked.add(o);
         }
     }
 }

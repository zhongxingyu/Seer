 package com.rylinaux.who;
 
 import java.text.DateFormat;
 import java.util.Date;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class WhoUtils {
 
     public static String getColoredName(Player player, boolean hasPrefix) {
         return getColoredName(player.getName(), hasPrefix);
     }
 
     public static String getColoredName(String player, boolean hasPrefix) {
         if (!hasPrefix)
             player = getNameWithPrefix(player);
         return ChatColor.translateAlternateColorCodes('&', player);
     }
 
     public static String formatPlayerInfo(String key, String value) {
         return String.format(ChatColor.GREEN + "- %s: " + ChatColor.GRAY + "%s", key, value);
     }
 
     public static String getNameWithPrefix(Player player) {
         return getNameWithPrefix(player.getName());
     }
 
     public static String getNameWithPrefix(String player) {
         return Who.getChat().getPlayerPrefix(Bukkit.getServer().getWorlds().get(0), player) + player;
     }
 
     public static boolean isPlayer(String player) {
         if (Bukkit.getServer().getOfflinePlayer(player).hasPlayedBefore() || Bukkit.getServer().getPlayer(player) != null)
             return true;
         return false;
     }
 
     public static boolean isWorld(String world) {
         if (Bukkit.getServer().getWorld(world) != null)
             return true;
         return false;
     }
 
     public static String[] playerInfo(CommandSender sender, Player player) {
 
         String name = WhoUtils.getColoredName(player, false);
         String IP = player.getAddress().getAddress().getHostAddress();
         String host = player.getAddress().getHostName();
 
         String firstSeen = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(player.getFirstPlayed()));
         String lastSeen = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(player.getLastPlayed()));
 
         if (!player.hasPlayedBefore())
             lastSeen = "First join";
 
         int x = player.getLocation().getBlockX();
         int y = player.getLocation().getBlockY();
         int z = player.getLocation().getBlockZ();
 
         String world = player.getWorld().getName();
 
         String location = String.format("%s, %s, %s in %s", x, y, z, world);
 
        String[] info = new String[6];
 
         info[0] = String.format(Who.PREFIX + ChatColor.GREEN + "Player information for %s:", name);
 
         if (sender.hasPermission("who.ip")) {
            info[1] = formatPlayerInfo("IP", IP);
            info[2] = formatPlayerInfo("Host", host);
         }
 
        info[3] = formatPlayerInfo("First Seen", firstSeen);
        info[4] = formatPlayerInfo("Last Seen", lastSeen);
        info[5] = formatPlayerInfo("Location", location);

         return info;
     }
 
     public static String[] playerInfo(OfflinePlayer player) {
 
         String name = WhoUtils.getColoredName(player.getName(), false);
         String firstSeen = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(player.getFirstPlayed()));
         String lastSeen = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(player.getLastPlayed()));
 
         String[] info = new String[3];
 
         info[0] = String.format(Who.PREFIX + ChatColor.GREEN + "Player information for %s:", name);
         info[1] = formatPlayerInfo("First Seen", firstSeen);
         info[2] = formatPlayerInfo("Last Seen", lastSeen);
 
         return info;
     }
 
     public static void sendArray(CommandSender sender, String[] array) {
         for (int i = 0; i < array.length; i++) {
            String message = array[i];
            if (!message.isEmpty())
                sender.sendMessage(array[i]);
         }
     }
 
     public static String who() {
 
         int online = Bukkit.getServer().getOnlinePlayers().length;
         int max = Bukkit.getServer().getMaxPlayers();
 
         StringBuilder players = new StringBuilder();
 
         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
             players.append(WhoUtils.getColoredName(player, false) + " ");
         }
 
         return String.format(Who.PREFIX + "Online Players (%d/%d): ", online, max).concat(players.toString());
 
     }
 
     public static String who(World world) {
 
         int inWorld = world.getPlayers().size();
         int online = Bukkit.getServer().getOnlinePlayers().length;
 
         StringBuilder players = new StringBuilder();
 
         for (Player player : world.getPlayers()) {
             players.append(WhoUtils.getColoredName(player, false) + " ");
         }
 
         return String.format(Who.PREFIX + "Online Players (%d/%d): ", inWorld, online).concat(players.toString());
 
     }
 
 }

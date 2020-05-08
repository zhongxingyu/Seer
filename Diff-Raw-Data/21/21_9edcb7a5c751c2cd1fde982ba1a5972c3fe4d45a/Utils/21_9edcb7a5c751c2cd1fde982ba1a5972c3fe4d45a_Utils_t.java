 package com.craftminecraft.craftchat.utils;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import org.bukkit.entity.Player;
 import org.bukkit.ChatColor;
 
 import net.milkbowl.vault.chat.Chat;
 
 import com.craftminecraft.craftchat.CraftChat;
 import com.craftminecraft.craftchat.conversations.Channel;
 
 public class Utils {
     public static String getPrefix(Player p) {
         String prefix = "";
         if (CraftChat.getInstance().chat != null) {
             Chat chat = CraftChat.getInstance().chat;
             // For all the ignorants out there (including myself) : //
             // http://www.terminally-incoherent.com/blog/2006/04/21/java-operator/ //
             // this explains the following line //
             prefix = (chat.getPlayerPrefix(p) != null) ? chat.getPlayerPrefix(p) : "";
             for (String group : chat.getPlayerGroups(p)) {
                 if (prefix == "") {
                     prefix = chat.getGroupPrefix(p.getWorld(), group);
                 }
             }
         }
         return prefix;
     }
 
     public static String getSuffix(Player p) {
         String suffix = "";
         if (CraftChat.getInstance().chat != null) {
             Chat chat = CraftChat.getInstance().chat;
             // For all the ignorants out there (including myself) : //
             // http://www.terminally-incoherent.com/blog/2006/04/21/java-operator/ //
             // this explains the following line //
             suffix = (chat.getPlayerSuffix(p) != null) ? chat.getPlayerSuffix(p) : "";
             for (String group : chat.getPlayerGroups(p)) {
                 if (suffix == "") {
                     suffix = chat.getGroupSuffix(p.getWorld(), group);
                 }
             }
         }
         return suffix;
     }
 
     public static String formatString(String message, Player player, Channel channel){
         message = message
                   .replaceAll("(?i)" + Pattern.quote("{WORLDNAME}"), player.getWorld().getName())
                   .replaceAll("(?i)" + Pattern.quote("{PREFIX}"), getPrefix(player))
                   .replaceAll("(?i)" + Pattern.quote("{SUFFIX}"), getSuffix(player))
                   .replaceAll("(?i)" + Pattern.quote("{REALNAME}"), player.getName())
                   .replaceAll("(?i)" + Pattern.quote("{DISPNAME}"), "%1\\$s")
                   .replaceAll("(?i)" + Pattern.quote("{CHANNELNAME}"), channel.getName())
                   .replaceAll("(?i)" + Pattern.quote("{CHANNELNICK}"), channel.getNick())
                   .replaceAll("(?i)" + Pattern.quote("{CHANNELCOLOR}"), channel.getColor())
                   .replaceAll("(?i)" + Pattern.quote("{MESSAGE}"), "%2\\$s");
         Matcher matcher = Pattern.compile("(?i)\\{color\\.[a-z_]+\\}").matcher(message);
         while (matcher.find()) {
             String match = matcher.group();
             CraftChat.getInstance().getLogger().info("I have a match : " + match);
             String colorstr = match.substring(7, match.length() - 1);
             message = message.replaceAll(Pattern.quote(match), ChatColor.valueOf(colorstr.toUpperCase()).toString());
         }
         message = message.replaceAll("&([0-9a-fk-or])", "\u00A7$1");
         return message;
    }

    public static String colorise(String message, Player p) {
        if (p.hasPermission("craftchat.colors")) {
            message = message.replaceAll("&([0-9a-fk-or])", "\u00A7$1");
        } else {
            message = message.replaceAll("&([0-9a-fk-or])", "");
            message = message.replaceAll("\u00A7([0-9a-fk-or])", "");
        }
        return message;
    }
 }

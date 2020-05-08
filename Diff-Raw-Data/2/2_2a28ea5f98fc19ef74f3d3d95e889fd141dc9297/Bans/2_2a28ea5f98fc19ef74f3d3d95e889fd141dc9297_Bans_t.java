 package net.robbytu.banjoserver.bungee.bans;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.robbytu.banjoserver.bungee.Main;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 public class Bans {
     public static void banUser(Ban ban) {
         // Insert User ban
         try {
             Connection conn = Main.conn;
             PreparedStatement statement = conn.prepareStatement("INSERT INTO bs_bans (user, admin, reason, date, server, tempban, active) VALUES (?, ?, ?, ?, ?, ?, 1)");
 
             statement.setString(1, ban.username);
             statement.setString(2, ban.mod);
             statement.setString(3, ban.reason);
             statement.setInt(4, ban.date);
             statement.setString(5, ban.server);
             statement.setInt(6, ban.tempban);
 
             statement.executeUpdate();
 
             kickBannedPlayer(ban);
 
             for(ProxiedPlayer player : Main.instance.getProxy().getPlayers()) {
                 if(player.hasPermission("bs.admin")) {
                     player.sendMessage("");
                     player.sendMessage(ChatColor.RED + ban.mod + " heeft " + ban.username + " gebanned:");
                     player.sendMessage(ChatColor.RED + " * " + ban.reason);
                     player.sendMessage("");
                 }
             }
 
             Main.instance.getLogger().info("User " + ban.username + " got banned by " + ban.mod + ": " + ban.reason);
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     public static Ban[] getUserBans(String username, boolean activeOnly) {
         Connection conn = Main.conn;
         ArrayList<Ban> bans = new ArrayList<Ban>();
 
         try {
             // Create a new select statement
             PreparedStatement statement = conn.prepareStatement("SELECT id, user, admin, reason, date, server, tempban, active FROM bs_bans WHERE user LIKE ?" + ((activeOnly) ? " AND active = 1" : ""));
             statement.setString(1, username);
             ResultSet result = statement.executeQuery();
 
             // For each ban ...
             while(result.next()) {
                 // Create a new Ban instance
                 Ban ban = new Ban();
 
                 // Fill in properties
                 ban.id = result.getInt(1);
                 ban.username = result.getString(2);
                 ban.mod = result.getString(3);
                 ban.reason = result.getString(4);
                 ban.date = result.getInt(5);
                 ban.server = result.getString(6);
                 ban.tempban = result.getInt(7);
                 ban.setActive(result.getInt(8));
 
                 // Add ban to return array
                 bans.add(ban);
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
 
         // Return the array of bans
         return bans.toArray(new Ban[bans.size()]);
     }
 
     public static void updateUserBan(Ban ban) {
         // Update User ban
         try {
             Connection conn = Main.conn;
            PreparedStatement statement = conn.prepareStatement("UPDATE bs_bans SET user = ?, admin = ?, reason = ?, date = ?, server = ?, tempban = ?, active = ? WHERE id = ?");
 
             statement.setString(1, ban.username);
             statement.setString(2, ban.mod);
             statement.setString(3, ban.reason);
             statement.setInt(4, ban.date);
             statement.setString(5, ban.server);
             statement.setInt(6, ban.tempban);
             statement.setInt(7, ((ban.active) ? 1 : 0));
             statement.setInt(8, ban.id);
 
             statement.executeUpdate();
 
             if (ban.active) {
                 kickBannedPlayer(ban);
 
                 for(ProxiedPlayer player : Main.instance.getProxy().getPlayers()) {
                     if(player.hasPermission("bs.admin")) {
                         player.sendMessage("");
                         player.sendMessage(ChatColor.RED + ban.mod + " heeft " + ban.username + " gebanned:");
                         player.sendMessage(ChatColor.RED + " * " + ban.reason);
                         player.sendMessage("");
                     }
                 }
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     public static void kickBannedPlayer(Ban ban) {
         if(Main.instance.getProxy().getPlayer(ban.username) != null) {
             if(ban.isTempban()) {
                 Main.instance.getProxy().getPlayer(ban.username).disconnect("Je bent tijdelijk gebanned voor " + ban.tempban + " sec:\n\n" + ChatColor.RED + ban.reason);
             }
             else {
                 Main.instance.getProxy().getPlayer(ban.username).disconnect(ChatColor.RED + "" + ChatColor.BOLD + ban.reason + "\n\n" + ChatColor.RESET + "Je kan een Ban Appeal maken op onze website: www.banjoserver.nl");
             }
         }
     }
 }

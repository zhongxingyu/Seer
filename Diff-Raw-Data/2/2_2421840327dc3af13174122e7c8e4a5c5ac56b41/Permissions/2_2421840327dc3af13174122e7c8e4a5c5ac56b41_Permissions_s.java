 package net.robbytu.banjoserver.bungee.perms;/* vim: set expandtab tabstop=4 shiftwidth=4 softtabstop=4: */
 
 import net.robbytu.banjoserver.bungee.Main;
 import net.robbytu.banjoserver.bungee.mail.Mail;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 public class Permissions {
     public static String getPrefixForUser(String user) {
         Connection conn = Main.conn;
 
         try {
             // Create a new select statement
             PreparedStatement statement = conn.prepareStatement("SELECT `bs_perm_groups`.`prefix` AS `group_name` " +
                                                                 "FROM `bs_perm_groups` " +
                                                                "INNER JOIN `bs_perm_members` " +
                                                                 "WHERE bs_perm_members.user LIKE ? " +
                                                                 "LIMIT 1");
             statement.setString(1, user);
             ResultSet result = statement.executeQuery();
 
             // If this user is a member of a group...
             if(result.next()) return result.getString(1);
         }
         catch (SQLException e) {
             e.printStackTrace();
         }
 
         return "§3Speler";
     }
 }

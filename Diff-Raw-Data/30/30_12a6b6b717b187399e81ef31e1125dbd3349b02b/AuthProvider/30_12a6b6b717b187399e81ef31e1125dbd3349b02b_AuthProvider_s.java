 package net.robbytu.banjoserver.bungee.auth;
 
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.robbytu.banjoserver.bungee.Main;
 
 import java.security.MessageDigest;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.List;
 
 public class AuthProvider {
     private static List<ProxiedPlayer> authenticatedUsers = new ArrayList<ProxiedPlayer>();
 
     public static boolean isAuthenticated(ProxiedPlayer player) {
         return (authenticatedUsers.contains(player));
     }
 
     public static boolean authenticate(ProxiedPlayer player, String providedPassword) {
         Connection conn = Main.conn;
 
         try {
             PreparedStatement statement = conn.prepareStatement("SELECT password FROM bs_auth WHERE username = ?");
             statement.setString(1, player.getName());
             ResultSet result = statement.executeQuery();
 
             if(result.next()) {
                String password = result.getString(0);
 
                String salt = password.split("$")[2];

                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update((providedPassword + salt).getBytes("UTF-8"));
                byte[] digest = md.digest();

                String hashed_password = "$SHA$" + salt + "$" + new String(digest);
                if(password == hashed_password) {
                     authenticatedUsers.add(player);
                     return true;
                 }
             }
         }
         catch(Exception ignored) {}
 
         return false;
     }
 
     public static void unauthenticatePlayer(ProxiedPlayer player) {
         authenticatedUsers.remove(player);
     }
 
     public static boolean isRegistered(String username) {
         Connection conn = Main.conn;
 
         try {
             PreparedStatement statement = conn.prepareStatement("SELECT * FROM bs_auth WHERE username = ?");
             statement.setString(1, username);
             ResultSet result = statement.executeQuery();
 
             return (result.next());
         }
         catch(Exception ignored) {}
 
         return false;
     }
 }

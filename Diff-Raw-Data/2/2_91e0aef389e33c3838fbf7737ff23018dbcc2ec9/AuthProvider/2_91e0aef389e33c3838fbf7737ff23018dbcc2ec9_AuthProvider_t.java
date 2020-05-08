 package net.robbytu.banjoserver.bungee.auth;
 
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.robbytu.banjoserver.bungee.Main;
 import net.robbytu.banjoserver.bungee.PluginMessager;
 
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.List;
 
 public class AuthProvider {
     private static SecureRandom rnd = new SecureRandom();
     private static List<ProxiedPlayer> authenticatedUsers = new ArrayList<ProxiedPlayer>();
 
     public static boolean isAuthenticated(ProxiedPlayer player) {
         return (authenticatedUsers.contains(player));
     }
 
     public static boolean checkPassword(ProxiedPlayer player, String providedPassword) {
         Connection conn = Main.conn;
 
         try {
             PreparedStatement statement = conn.prepareStatement("SELECT password FROM bs_auth WHERE username LIKE ?");
             statement.setString(1, player.getName());
             ResultSet result = statement.executeQuery();
 
             if(result.next()) {
                 String password = result.getString(1);
                 String salt = password.split("\\$")[2];
                 String hashed_password = "$SHA$" + salt + "$" + getSHA256(getSHA256(providedPassword) + salt);
 
                 if(password.equals(hashed_password)) {
                     return true;
                 }
             }
         }
         catch(Exception ignored) {}
 
         return false;
     }
 
     public static boolean authenticate(ProxiedPlayer player, String providedPassword) {
         if(checkPassword(player, providedPassword)) {
             authenticatedUsers.add(player);
             PluginMessager.sendMessage(player.getServer(), "PlayerAuthInfo", player.getName(), "authenticated");
             return true;
         }
         return false;
     }
 
     public static boolean register(ProxiedPlayer player, String password) {
         Connection conn = Main.conn;
 
         try {
             String salt = createSalt(16);
             String hashed_password = "$SHA$" + salt + "$" + getSHA256(getSHA256(password) + salt);
 
            PreparedStatement statement = conn.prepareStatement("INSERT INTO bs_auth (username, password, ip, lastlogin) VALUES (?, ?, ?, 0)");
             statement.setString(1, player.getName());
             statement.setString(2, hashed_password);
             statement.setString(3, player.getAddress().getAddress().toString());
             statement.executeUpdate();
 
             authenticatedUsers.add(player);
             PluginMessager.sendMessage(player.getServer(), "PlayerAuthInfo", player.getName(), "authenticated");
 
             return true;
         }
         catch(Exception ignored) {}
 
         return false;
     }
 
     public static boolean updatePassword(ProxiedPlayer player, String password) {
         Connection conn = Main.conn;
 
         try {
             String salt = createSalt(16);
             String hashed_password = "$SHA$" + salt + "$" + getSHA256(getSHA256(password) + salt);
 
             PreparedStatement statement = conn.prepareStatement("UPDATE bs_auth SET password = ? WHERE username LIKE ?");
             statement.setString(1, player.getName());
             statement.setString(2, hashed_password);
             statement.executeUpdate();
 
             return true;
         }
         catch(Exception ignored) {}
 
         return false;
     }
 
     public static void unauthenticatePlayer(ProxiedPlayer player) {
         authenticatedUsers.remove(player);
         PluginMessager.sendMessage(player.getServer(), "PlayerAuthInfo", player.getName(), "unauthorized");
     }
 
     public static boolean isRegistered(String username) {
         Connection conn = Main.conn;
 
         try {
             PreparedStatement statement = conn.prepareStatement("SELECT * FROM bs_auth WHERE username LIKE ?");
             statement.setString(1, username);
             ResultSet result = statement.executeQuery();
 
             return (result.next());
         }
         catch(Exception ignored) {}
 
         return false;
     }
 
     private static String getSHA256(String message) throws NoSuchAlgorithmException
     {
         MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
 
         sha256.reset();
         sha256.update(message.getBytes());
         byte[] digest = sha256.digest();
 
         return String.format("%0" + (digest.length << 1) + "x", new Object[] { new BigInteger(1, digest) });
     }
 
     private static String createSalt(int length) throws NoSuchAlgorithmException {
         byte[] msg = new byte[40];
         rnd.nextBytes(msg);
 
         MessageDigest sha1 = MessageDigest.getInstance("SHA1");
         sha1.reset();
         byte[] digest = sha1.digest(msg);
         return String.format("%0" + (digest.length << 1) + "x", new Object[] { new BigInteger(1, digest) }).substring(0, length);
     }
 }

 package net.robbytu.banjoserver.bungee.auth;
 
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.robbytu.banjoserver.bungee.Main;
 
import java.math.BigInteger;
 import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
                String password = result.getString(1);
                String salt = password.split("\\$")[2];
                String hashed_password = "$SHA$" + salt + "$" + getSHA256(getSHA256(providedPassword) + salt);
 
                if(password.equals(hashed_password)) {
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

    private static String getSHA256(String message) throws NoSuchAlgorithmException
    {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

        sha256.reset();
        sha256.update(message.getBytes());
        byte[] digest = sha256.digest();

        return String.format("%0" + (digest.length << 1) + "x", new Object[] { new BigInteger(1, digest) });
    }
 }

 package mccarthy.brian.loginpass;
 
 import java.security.MessageDigest;
 
 import net.canarymod.Canary;
 import net.canarymod.Logman;
 import net.canarymod.api.entity.Player;
 
 /**
  * 
  * @author Brian McCarthy
  *
  */
 public class LoginPassActions {
     private static MessageDigest md;
 
     static {
         try {
             Logman.logInfo(LoginPass.SPRE + "Using " + LoginPassSettings.DIGEST_TYPE + " as digest type.");
             md = MessageDigest.getInstance(LoginPassSettings.DIGEST_TYPE);
         } catch (Exception e) {
             Logman.logSevere(LoginPass.SPRE + "Cannot create MessageDigest! Disabling.");
             Canary.loader().disablePlugin(LoginPass.NAME);
         }
     }//target
     public static void sendHelp(Player p) {
         sendMessage(p, "==== LoginPass Help ====");
         sendMessage(p, "/loginpass OR /lp");
         sendMessage(p, "/lp login <pass> - Logs in. You must have created a pass first.");
         sendMessage(p, "/lp create <pass> <pass> - First time login, create a pass.");
         sendMessage(p, "/lp change <old> <new> - Change pass from <old> to <new>");
         sendMessage(p, "");
         sendMessage(p, "This plugin forces you to not be able to start chat with \"loginplugin\" or \"lp\"");
         sendMessage(p, "==== LoginPass Help ====");
     }
 
     public static void sendMessage(Player p, String message) {
         p.sendMessage(LoginPass.PRE + message);
     }
 
     public static byte[] hash(String player, String pass) {
         byte[] b1 = md.digest(player.getBytes());
         byte[] b2 = md.digest(pass.getBytes());
         byte[] b3 = new byte[b1.length + b2.length];
         for (int i = 0; i < b1.length; i++) {
             b3[i] = b1[i];
         }
         for (int i = 0; i < b2.length; i++) {
             b3[b1.length + i] = b2[i];
         }
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < b3.length; i++) {
             sb.append(b3[i]);
         }
         //Logman.logInfo(LoginPass.SPRE + "b3: " + sb.toString());
         return md.digest(b3);
     }
 
     public static String toHex(byte[] bytes){
         StringBuilder sb = new StringBuilder();
         char[] hex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
         for (int i = 0; i < bytes.length; i++) {
             byte b = bytes[i];
             sb.append(hex[(b & 0xf0) >> 4]);
             sb.append(hex[b & 0x0f]);
         }
         return sb.toString();
     }
     
     public static boolean ipMatches(String name, String ip) {
         for (String currIp : LoginPass.ips.getString(name, "").split(",")) {
             if (currIp.trim().equals("")) {
                 continue;
             }
            if (currIp.equals("*")) {
                 return true;
            }
             String currPart = "";
            for (String part : ip.split(".")) {
                 currPart += part;
                 if (currIp.equals(currPart + ".*")) {
                     return true;
                 }
                 currPart += ".";
             }
         }
         return false;
     }
 }

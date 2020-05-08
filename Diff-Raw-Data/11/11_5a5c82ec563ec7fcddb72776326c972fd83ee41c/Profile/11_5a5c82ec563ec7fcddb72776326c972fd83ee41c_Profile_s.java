 package com.beecub.execute;
 
 import java.util.HashMap;
 
 import org.bukkit.entity.Player;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.beecub.glizer.glizer;
 import com.beecub.util.bChat;
 import com.beecub.util.bConnector;
 import com.beecub.util.bMessageManager;
 import com.beecub.util.bPermissions;
 
 public class Profile {
 
     public static boolean profile(String command, Player player, String[] args) {
         if(bPermissions.checkPermission(player, command)) {
             if(args.length == 1) {
                 String recipient = args[0];
                 showProfile(player, recipient);
                 return true;
             }
             bChat.sendMessageToPlayer(player, bMessageManager.messageWrongCommandUsage);
             bChat.sendMessageToPlayer(player, "&6/profile&e [playername]");
             return true;
         }
         return true;
     }
     
     public static boolean editprofile(String command, Player player, String[] args) {
         if(bPermissions.checkPermission(player, command)) {
             if(args.length >= 2) {
                 String field = args[0];
                 String message = "";
                 for(int i = 1; i < args.length; i++) {
                     message += args[i] + " ";
                 }
                 if(editProfile(player, field, message)) {
                     bChat.sendMessageToPlayer(player, "&6Done");
                 }
                 else {
                     return true;
                 }
             }
             bChat.sendMessageToPlayer(player, bMessageManager.messageWrongCommandUsage);
             bChat.sendMessageToPlayer(player, "&6/editprofile&e [field] [value]");
             return true;
         }
         return true;
     }
     
     public static boolean clearprofile(String command, Player player, String[] args) {
         bChat.sendMessageToPlayer(player, "&6Not available in this version of glizer");
         return true;
     }
     
     public static boolean editProfile(Player player, String field, String value) {
         
         String ip = bConnector.getPlayerIPAddress(player);
         
         HashMap<String, String> url_items = new HashMap<String, String>();
         url_items.put("exec", "profile");
         url_items.put("do", "edit");
         url_items.put("account", player.getName());
         url_items.put("ip", ip);
         url_items.put("name", player.getName());
         url_items.put("field", field);
         url_items.put("value", value);
         
         JSONObject result = bConnector.hdl_com(url_items);
         String check = bConnector.checkResult(result);
         
         if(check.equalsIgnoreCase("ok")) {
             if(glizer.D) bChat.log("Profile edit action done");
             return true;
         }
         else if(check.equalsIgnoreCase("not allowed")) {
             if(glizer.D) bChat.log("Profile edit cant be done, its not allowed to edit this profile field");
             bChat.sendMessageToPlayer(player, "&6Error, its not allowed to edit this profile field");
             return false;
         }
         else if(check.equalsIgnoreCase("wrong data type")) {
             if(glizer.D) bChat.log("Profile edit cant be done, wrong data type sent");
             bChat.sendMessageToPlayer(player, "&6Error, wrong data type");
             return false;
         }
         return true;
         
         
         /*
         bChat.log(result.toString());
         
         //{"name":"beecub","realname":"","email":"","age":"0","status":"","mehr":"","lastip":"91.11.230.223",
         //"reputation":"-1","userreputation":"0","lastserver":"1","lastseen":"1306603729","developer":"0"}
         
         try {
             if(result.getString("response").equalsIgnoreCase("ok")) {
                 return "ok";
             }
         } catch (JSONException e) {
             if(glizer.D) e.printStackTrace();
         }        
         return "error";
         */
     }
     
     public static String showProfile(Player player, String recipient) {
         
         String ip = bConnector.getPlayerIPAddress(player);
         
         HashMap<String, String> url_items = new HashMap<String, String>();
         url_items.put("exec", "profile");
         url_items.put("do", "list");
         url_items.put("account", player.getName());
         url_items.put("ip", ip);
         url_items.put("name", recipient);
         
         JSONObject result = bConnector.hdl_com(url_items);
         
         //{"name":"beecub","realname":"","email":"","age":"0","status":"","mehr":"","lastip":"91.11.230.223",
         //"reputation":"-1","userreputation":"0","lastserver":"1","lastseen":"1306603729","developer":"0"}
         
         try {
             bChat.sendMessageToPlayer(player, "&6 --- Profile --- ");
             bChat.sendMessageToPlayer(player, "&6Name: &e" + result.getString("name"));
             bChat.sendMessageToPlayer(player, "&6Realname: &e" + result.getString("realname"));
             bChat.sendMessageToPlayer(player, "&6Age: &e" + result.getString("age"));
             bChat.sendMessageToPlayer(player, "&6Last Server: &e" + result.getString("lastserverurl") + ":" + result.getString("lastserverport"));
             bChat.sendMessageToPlayer(player, "&6Status: &e" + result.getString("status"));
         } catch (JSONException e) {
             if(glizer.D) e.printStackTrace();
         }
         
         return "Ok";
     }
 }

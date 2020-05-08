 package com.codisimus.plugins.textplayer;
 
 import java.io.FileInputStream;
 import java.util.LinkedList;
 import java.util.Properties;
 import org.bukkit.entity.Player;
 
 /**
  * A User holds the contact information for a Player and when they will be contacted
  * 
  * @author Codisimus
  */
 public class User {
     static String world = TextPlayer.server.getWorlds().get(0).getName();
     static Encrypter encrypter = new Encrypter("SeVenTy*7"); 
     
     public String name;
     public String email; //An encrypted version of the User's email address
     public boolean disableWhenLogged = true; //If true, texts will only be sent when the Player is offline
     public int textLimit = -1;
     public int textsSent = 0;
     public int lastText = 0; //The day that the last text was sent to this User
     
     public boolean watchingServer = false;
     public LinkedList<String> players = new LinkedList<String>();
     public LinkedList<String> items = new LinkedList<String>();
     public LinkedList<String> words = new LinkedList<String>();
 
     /**
      * Constructs a new User with the given name
      * 
      * @param name The name of the Player
      */
     public User (String name) {
         this.name = name;
     }
 
     /**
      * Constructs a new User with the given name and email
      * 
      * @param name The name of the Player
      * @param email The encrypted email address
      */
     public User (String name, String email) {
         this.name = name;
         this.email = email;
     }
     
     /**
      * Returns true if this User has the textplayer.admin node
      * 
      * @return True if this User is an Admin
      */
     public boolean isAdmin() {
         return TextPlayer.permission.has(world, name, "textplayer.admin");
     }
 
     /**
      * Formats the given information into a valid email address
      * 
      * @param carrier The cell phone carrier or 'email'
      * @param address The phone number or email address
      * @return The message that will be sent to the Player
      */
     public void setEmail(Player player, String carrier, String address) {
         String old = email;
         
         //Format the given Strings
         address = address.replaceAll("-", "").toLowerCase();
         carrier = carrier.replaceAll("-", "").toLowerCase();
         
         if (carrier.equals("email"))
             //Check the format of the given email address
             if (address.contains("@") && address.contains("."))
                 email = address;
             else {
                 player.sendMessage("Invalid e-mail address");
                 return;
             }
         else {
             //Return if the number is not 10 digits
             switch (address.length()) {
                 case 10: break;
                 case 11: address = address.substring(1); break; //Throw out the included country code
                 default: player.sendMessage("Invalid number format"); return;
             }
                 
             try {
                 Properties p = new Properties();
                 FileInputStream fis = new FileInputStream("plugins/TextPlayer/sms.gateways");
                 p.load(fis);
                 
                 //Return if the carrier is not found
                 String gateway = p.getProperty(carrier);
                 if (gateway == null) {
                     player.sendMessage("Carrier not supported");
                     return;
                 }
                 
                 //Format the number into the correct email address
                 email = gateway.replace("<number>", address);
                 fis.close();
             }
             catch (Exception ex) {
             }
         }
         
         //Encrypt the new email address
         email = encrypter.encrypt(email);
         
         //Return if it is not a new email address
         if (email.equals(old)) {
             player.sendMessage("That is already your current number");
             return;
         }
         
         player.sendMessage("E-mail set to: "+encrypter.decrypt(email));
         
         //Send confirmation text
         player.sendMessage("Sending Confirmation Text...");
         TextPlayerMailer.sendMsg(player, this, "[TextPlayer] Reply 'enable' to link this number to "+name);
 
         //Set the User as not verified
         textLimit = -1;
         TextPlayer.save();
     }
     
     /**
      * Sends a text message (email) to the User
      * 
      * @param msg The message that will be emailed
      */
     public void sendText(String msg) {
         TextPlayerMailer.sendMsg(null, this, msg);
     }
 }

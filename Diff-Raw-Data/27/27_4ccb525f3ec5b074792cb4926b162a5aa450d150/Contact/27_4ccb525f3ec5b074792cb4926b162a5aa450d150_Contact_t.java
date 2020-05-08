 package ch9k.chat;
 
 import java.net.InetAddress;
 
 /**
  * Representation of a contact aka "Buddy".
  * This holds the contacts ip, username, status, and knows whether a contact is online or offline and blocked or not.
  * @author Jens Panneel
  */
 public class Contact {
     private InetAddress ip;
     private String username;
     private String status;
     private boolean online;
     private boolean blocked;
 
     /**
      * Constructor.
      * @param username The username of the new contact.
      * @param ip The ip of the new contact.
     * @param blocked Whether or not the new contact was already blocked.
      */
     public Contact(String username, InetAddress ip, boolean blocked) {
         this.username = username;
         this.ip = ip;
         this.blocked = blocked;
         online = false;
     }
 
     /**
      * Get the contacts ipaddress.
      * @return ip
      */
     public InetAddress getIp() {
         return ip;
     }
 
     /**
      * Set the contacts ipaddress.
      * @param ip
      */
     public void setIp(InetAddress ip) {
         this.ip = ip;
     }
 
     /**
      * Get the contacts username.
      * @return username
      */
     public String getUsername() {
         return username;
     }
 
     /**
      * Get the contacts status.
      * @return status
      */
     public String getStatus() {
         return status;
     }
 
     /**
      * Set the contacts status.
      * @param status
      */
     public void setStatus(String status) {
         this.status = status;
     }
 
     /**
      * Check whether or not the contact is online.
      * @return online
      */
     public boolean isOnline() {
         return online;
     }
 
     /**
      * Set the contact online or offline
      * @param online
      */
     public void setOnline(boolean online) {
         this.online = online;
     }
 
     /**
      * Check whether or not the contact is blocked.
      * @return ip
      */
     public boolean isBlocked() {
         return blocked;
     }
 
     /**
      * Set the contact blocked or not blocked
      * @param blocked
      */
     public void setBlocked(boolean blocked) {
         this.blocked = blocked;
     }
 
     // TODO equals and hashcode
 
 }

 package ch9k.chat;
 
 import ch9k.chat.events.ContactOfflineEvent;
 import ch9k.chat.events.ContactOnlineEvent;
 import ch9k.configuration.Persistable;
 import ch9k.configuration.PersistentDataObject;
 import ch9k.core.Model;
 import ch9k.eventpool.EventPool;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import org.apache.log4j.Logger;
 import org.jdom.Element;
 
 /**
  * Representation of a contact aka "Buddy".
  * This holds the contacts ip, username, status, and knows whether a contact is online or offline and blocked or not.
  * @author Jens Panneel
  */
 public class Contact extends Model implements Comparable<Contact>, Persistable {
     private InetAddress ip;
     private String username;
     private ContactStatus status;
 
 
     /**
      * Constructor.
      * @param username The username of the new contact.
      * @param ip The ip of the new contact.
      * @param blocked Whether or not the new contact was already blocked.
      */
     public Contact(String username, InetAddress ip) {
         this.username = username;
         this.ip = ip;
         this.status = new ContactStatus();
     }
 
     /**
      * Creates a new object, and immediately restores it to a previous state
      *
      * @param data Previously stored state of this object
      */
     public Contact(PersistentDataObject data) {
         load(data);
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
         return status.getText();
     }
 
     /**
      * Set the contacts status.
      * @param status
      */
     public void setStatus(String status) {
         if(!this.status.getText().equals(status)){
             this.status.setText(status);
             fireStateChanged();
         }
     }
 
     /**
      * Check whether or not the contact is online.
      * @return online
      */
     public boolean isOnline() {
         return status.isOnline();
     }
 
     public boolean isOffline() {
         return status.isOffline();
     }
 
     /**
      * Set the contact online or offline
      * @param online
      */
     public void setOnline(boolean online) {
         if(isOnline() != online) {
             Logger.getLogger(getClass()).info("Contact " + username + " is now "
                     + (online ? "online" : "offline") + " from " + ip.toString());
             if(online) {
                 status.setStatus(ContactStatus.Status.ONLINE);
             } else {
                 status.setStatus(ContactStatus.Status.OFFLINE);
             }
             
             fireStateChanged();
         }
     }
 
 
 
     /**
      * Check whether or not the contact is blocked.
      * @return ip
      */
     public boolean isBlocked() {
         return status.isBlocked();
     }
 
     public boolean isRequested() {
         return status.isRequested();
     } 
     
     public boolean isIgnored() {
         return status.isIgnored();
     }
     
     public void setRequested() {
         if(!isRequested()) {
             status.setStatus(ContactStatus.Status.REQUESTED);
             fireStateChanged();
         }
     }
     
     public void setIgnored() {
         if(!isIgnored()) {
             status.setStatus(ContactStatus.Status.IGNORED);
             fireStateChanged();
         }
     }
     
     /**
      * Set the contact blocked or not blocked
      * @param blocked
      */
     public void setBlocked(boolean blocked) {
         if(isBlocked() != blocked) {
             if(blocked) {
                 status.setStatus(ContactStatus.Status.BLOCKED);
                 EventPool.getAppPool().raiseEvent(new ContactOfflineEvent(this));
             } else {
                 status.setStatus(ContactStatus.Status.OFFLINE);
                 EventPool.getAppPool().raiseEvent(new ContactOnlineEvent(this));
             }
             fireStateChanged();
         }
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null || getClass() != obj.getClass()) {
             return false;
         }
         Contact other = (Contact) obj;
         if (this.ip != other.ip && (this.ip == null || !this.ip.equals(other.ip))) {
             return false;
         }
         if (this.username != other.username &&
                 (this.username == null || !this.username.equals(other.username))) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 79 * hash + (ip != null ? ip.hashCode() : 0);
         hash = 79 * hash + (username != null ? username.hashCode() : 0);
         return hash;
     }
 
     @Override
     public int compareTo(Contact contact) {
         if(equals(contact)) {
             return 0;
         }
         
         // order by name
         int compareUsername = username.compareToIgnoreCase(contact.getUsername());
         if (compareUsername != 0){
             return compareUsername;
         } else {
             // if same name, ip cannot be the same, so this will never return 0!
             return this.getIp().toString().compareTo(contact.getIp().toString());
         }
     }
     
     @Override
     public PersistentDataObject persist() {
         Element contact = new Element("contact");
         contact.addContent(new Element("username").addContent(username));
         contact.addContent(new Element("ip").addContent(ip.getHostAddress()));
        contact.addContent(new Element("status").addContent(status.persist().getElement()));
         return new PersistentDataObject(contact);
     }
 
     @Override
     public void load(PersistentDataObject object) {
         Element el = object.getElement();
         username = el.getChildText("username");
         try {
             ip = InetAddress.getByName(el.getChildText("ip"));
         } catch (UnknownHostException ex) {
             Logger.getLogger(Contact.class).warn(ex.toString());
         }
         status = new ContactStatus(new PersistentDataObject(el.getChild("status")));
     }
 }

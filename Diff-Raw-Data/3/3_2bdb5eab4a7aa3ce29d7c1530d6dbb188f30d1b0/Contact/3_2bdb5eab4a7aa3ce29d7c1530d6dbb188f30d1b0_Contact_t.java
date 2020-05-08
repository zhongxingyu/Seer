 package ch9k.chat;
 
 import ch9k.chat.events.ContactBlockedEvent;
 import ch9k.chat.events.ContactOfflineEvent;
 import ch9k.chat.events.ContactOnlineEvent;
 import ch9k.chat.events.ContactStatusChangeEvent;
 import ch9k.chat.events.ContactStatusEventFilter;
 import ch9k.chat.events.ContactUnblockedEvent;
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventListener;
 import ch9k.eventpool.EventPool;
 import ch9k.configuration.Persistable;
 import ch9k.configuration.PersistentDataObject;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jdom.Element;
 
 /**
  * Representation of a contact aka "Buddy".
  * This holds the contacts ip, username, status, and knows whether a contact is online or offline and blocked or not.
  * @author Jens Panneel
  */
 public class Contact implements Comparable<Contact>, EventListener, Persistable {
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
         this.online = false;
         this.status = "";
        EventPool.getAppPool().addListener(this, new ContactStatusEventFilter(this));
     }
 
     /**
      * Creates a new object, and immediately restores it to a previous state
      *
      * @param data Previously stored state of this object
      */
     public Contact(PersistentDataObject data) {
         load(data);
         EventPool.getAppPool().addListener(this, new ContactStatusEventFilter(this));
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
         hash = 79 * hash + (this.ip != null ? this.ip.hashCode() : 0);
         hash = 79 * hash + (this.username != null ? this.username.hashCode() : 0);
         return hash;
     }
 
     @Override
     public int compareTo(Contact contact) {
         if(this.equals(contact)) {
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
     public void handleEvent(Event event) {
         if(event instanceof ContactOnlineEvent) {
             this.setOnline(true);
         }
 
         if(event instanceof ContactOfflineEvent) {
             this.setOnline(false);
         }
 
         if(event instanceof ContactStatusChangeEvent) {
             ContactStatusChangeEvent contactStatusChangeEvent = (ContactStatusChangeEvent)event;
             this.setStatus(contactStatusChangeEvent.getNewStatus());
         }
 
         if(event instanceof ContactBlockedEvent) {
             ContactBlockedEvent contactBlockedChangeEvent = (ContactBlockedEvent)event;
             if(contactBlockedChangeEvent.isExternal()) {
                 this.setOnline(false);
             } else {
                 this.setBlocked(true);
             }
         }
 
         if(event instanceof ContactUnblockedEvent) {
             ContactUnblockedEvent contactUnblockedChangeEvent = (ContactUnblockedEvent)event;
             if(contactUnblockedChangeEvent.isExternal()) {
                 this.setOnline(true);
             } else {
                 this.setBlocked(false);
             }
         }
     }
     
     @Override
     public PersistentDataObject persist() {
         Element contact = new Element("contact");
         contact.addContent(new Element("username").addContent(username));
         contact.addContent(new Element("ip").addContent(ip.getHostAddress()));
         contact.addContent(new Element("online").addContent(Boolean.toString(online)));
         contact.addContent(new Element("blocked").addContent(Boolean.toString(blocked)));
 
         return new PersistentDataObject(contact);
     }
 
     @Override
     public void load(PersistentDataObject object) {
         Element el = object.getElement();
         username = el.getChildText("username");
         try {
             ip = InetAddress.getByName(el.getChildText("ip"));
         } catch (UnknownHostException ex) {
             Logger.getLogger(Contact.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         this.status = "";
         online = Boolean.parseBoolean(el.getChildText("online"));
         blocked = Boolean.parseBoolean(el.getChildText("blocked"));
     }
 }

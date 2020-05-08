 package ch9k.core;
 
 import ch9k.chat.ContactList;
 import ch9k.configuration.Persistable;
 import ch9k.configuration.PersistentDataObject;
 import ch9k.core.event.AccountStatusEvent;
 import ch9k.eventpool.EventPool;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.security.MessageDigest;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.log4j.Logger;
 import org.jdom.Element;
 
 /**
  * Local user info
  * 
  * @author Bruno
  */
 public class Account implements Persistable {
     /**
      * The users contactlist
      */
     private ContactList contactList;
 
     /**
      * The users name within the network
      */
     private String username;
 
     /**
      * Users current status
      */
     private String status;
     
     /**
      * the hash of the password
      */
     private String passwordHash;
 
     /**
      * Create a new account with a password
      * @param username 
      * @param password
      */
     public Account(String username, String password) {
         this.username = username;
         this.passwordHash = hash(password);
         contactList = new ContactList(this);
     }
 
     /**
      * Creates a new object, and immediately restores it to a previous state
      *
      * @param data Previously stored state of this object
      */
     public Account(PersistentDataObject data) {
         load(data);
     }
 
     /**
      * Get username
      * @return Username of the current user
      */
     public String getUsername() {
         return username;
     }
 
     /**
      * will return the password hash
      * NOTE: trying to print this is just stupid
      * @return hash
      */
     public String getPasswordHash() {
         return passwordHash;
     }
     
     /**
      * method to change the password
      * @param password
      */
     public void setPassword(String password) {
         this.passwordHash = hash(password);
     }
 
     /**
      * Get current personal status
      * @return
      */
     public String getStatus() {
         return status;
     }
 
     /**
      * Set a new personal status
      * @param status The new status
      */
     public void setStatus(String status) {
         if(!this.status.equals(status)) {
             this.status = status;
             EventPool.getAppPool().raiseEvent(new AccountStatusEvent(status));
         }
     }
 
     /**
      * Getter for the users current contactlist
      * @return Current ContactList
      */
     public ContactList getContactList() {
         return contactList;
     }
 
     /**
      * Try authenticate a user with a given password
      * @param password 
      * @return result
      */
     public boolean authenticate(String password) {
         return passwordHash != null && passwordHash.equals(hash(password));
     }
 
     private String hash(String password) {
         try {
              byte[] digest = MessageDigest.getInstance("sha1").digest(password.getBytes());
              StringBuffer result = new StringBuffer(digest.length*2);
              for(int i=0;i<digest.length;i++)
              {
                  result.append(Integer.toHexString(digest[i] & 0xFF));
              }
              return new String(result);
         } catch(java.security.NoSuchAlgorithmException e) {
             // throw new VeerleFackException
             return null;
         }
     }
 
     private static final URL IP_LOOKUP_API;
     static {
         URL lookupUrl = null;
         try {
             lookupUrl = new URL("http://zeus.ugent.be/~javache/getIp.php");
         } catch (MalformedURLException ex) {}
         IP_LOOKUP_API = lookupUrl;
     }
 
     private static InetAddress[] inetAddresses;
 
     /**
      * Get the IP-adresses the account is reachable at
      * @return ips
      */
     public static InetAddress[] getInetAddresses() {
         if(inetAddresses == null) {
             List<InetAddress> ipList = new ArrayList<InetAddress>();
             try {
                 // add localhost
                 ipList.add(InetAddress.getLocalHost());
 
                 // perform a small lookup to get the public IP
                 URLConnection connection = IP_LOOKUP_API.openConnection();
                 connection.setReadTimeout(2000);
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(connection.getInputStream()));
                 ipList.add(InetAddress.getByName(reader.readLine()));
                 reader.close();
             } catch (IOException ex) {
                 Logger.getLogger(Account.class).warn(ex.toString());
             }
 
             inetAddresses = ipList.toArray(new InetAddress[0]);
         }
 
         return inetAddresses;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null || getClass() != obj.getClass()) {
             return false;
         }
         Account other = (Account) obj;
         return this.username.equals(other.getUsername())
                 && this.passwordHash.equals(other.getPasswordHash());
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 89 * hash + (this.username != null ? this.username.hashCode() : 0);
         hash = 89 * hash + (this.passwordHash != null ? this.passwordHash.hashCode() : 0);
         return hash;
     }
 
     @Override
     public PersistentDataObject persist() {
         Element pdo = new Element("account");
         pdo.addContent(new Element("username").addContent(username));
         pdo.addContent(new Element("status").addContent(status));
         pdo.addContent(new Element("password").addContent(passwordHash));
         pdo.addContent(contactList.persist().getElement());
 
         return new PersistentDataObject(pdo);
     }
 
     @Override
     public void load(PersistentDataObject object) {
         Element el = object.getElement();
         username = el.getChildText("username");
         status = el.getChildText("status");
         passwordHash = el.getChildText("password");
         contactList = new ContactList(this, new PersistentDataObject(el.getChild("contactlist")));
     }
 
 }

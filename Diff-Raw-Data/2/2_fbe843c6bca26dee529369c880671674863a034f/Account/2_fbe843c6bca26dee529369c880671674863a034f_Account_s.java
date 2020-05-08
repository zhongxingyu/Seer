 package ch9k.core;
 
 import ch9k.configuration.PersistentDataObject;
 import java.security.MessageDigest;
 
 import ch9k.chat.ContactList;
 import ch9k.configuration.Persistable;
 import java.util.Arrays;
 import org.jdom.Element;
 
 /**
  * Local user info
  * 
  * @author Bruno
  */
 public class Account implements Persistable{
 
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
      * creates a new account in the system.
      * this class only stores a hash of the password
      */
     public Account(String username,String password) {
         this.username = username;
         setPassword(password);
         contactList = new ContactList();
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
      * will return the password hash
      * NOTE: trying to print this is just stupid
      */
     public String getPasswordHash() {
         return passwordHash;
     }
     
     /**
      * method to change the password
      */
     public void setPassword(String password) {
         this.passwordHash = hash(password);
     }
     /**
      * Getter for the users current contactlist
      * @return Current ContactList
      */
     public ContactList getContactList() {
         return contactList;
     }
 
     /**
      * Get current personal status
      * @return
      */
     public String getStatus() {
         return status;
     }
 
     /**
      * Get username
      * @return Username of the current user
      */
     public String getUsername() {
         return username;
     }
 
     /**
      * authenticate with the given password
      */
     public boolean authenticate(String pass) {
         return hash(pass).equals(passwordHash);
     }
 
     /**
      * Set a new personal status
      * @param status The new status
      */
     public void setStatus(String status) {
         this.status = status;
     }
 
     private String hash(String password) {
         try {
              byte[] digest = MessageDigest.getInstance("sha1").digest(password.getBytes());
              StringBuffer result = new StringBuffer(digest.length*3);
              for(int i=0;i<digest.length;i++)
              {
                 result.append(" "+Integer.toHexString(digest[i]));
              }
              return new String(result);
         } catch(java.security.NoSuchAlgorithmException e) {
             // throw new VeerleFackException
             return null;
         }
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null || getClass() != obj.getClass()) {
             return false;
         }
         Account other = (Account) obj;
         if (!this.username.equals(other.getUsername())) {
             return false;
         }
         if (this.passwordHash.equals(other.getPasswordHash())) {
             return false;
         }
         return true;
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
         contactList= new ContactList(new PersistentDataObject(el.getChild("contactlist")));
         
     }
 }

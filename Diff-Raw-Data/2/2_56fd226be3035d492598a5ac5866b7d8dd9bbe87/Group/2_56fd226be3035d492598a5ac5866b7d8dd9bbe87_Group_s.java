 package models;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToOne;
 
 import msn.ContactListListener;
 import msn.FileTransferListener;
 import msn.MessageListener;
 import msn.Messenger;
 import msn.MessengerListener;
 import net.sf.jml.Email;
 import net.sf.jml.MsnConnectionType;
 import net.sf.jml.MsnContact;
 import net.sf.jml.MsnMessenger;
 import net.sf.jml.MsnProtocol;
 import net.sf.jml.MsnUserStatus;
 import play.Play;
 import play.db.jpa.Model;
 
 @Entity(name = "IMGroup")
 public class Group extends Model {
 
   private static HashMap<Long, MsnMessenger> messengers = new HashMap<Long, MsnMessenger>();
 
   public String display;
   public String email;
   public String password;
   @OneToOne
   @JoinColumn(name = "owner_id")
   public User owner;
 
   public Boolean online;
 
   public Group(String display, String email, String password, User owner) {
     this.email = email;
     this.password = password;
     this.owner = owner;
 
     this.display = display;
     this.online = false;
   }
 
   public static Group add(String display, String email, String password,
       User owner) {
     Group group = null;
     if (!owner.isOverQuota()) {
       group = Group.find("byEmail", email).first();
       if (group == null) {
         group = new Group(display, email, password, owner);
         group.save();
 
         Quota quota = owner.quota();
         quota.groupCreate++;
         quota.save();
       }
     }
 
     return group;
   }
 
   public void remove() {
     logout();
 
     List<Alias> aliases = Alias.find("byGroup", this).fetch();
     for (Alias alias : aliases) {
       alias.delete();
     }
 
     delete();
 
     Quota quota = owner.quota();
     quota.groupCreate--;
     quota.save();
   }
 
   public void login() {
     MsnMessenger messenger;
     if (!messengers.containsKey(id)) {
       messenger = new Messenger(Email.parseStr(email), password);
       messenger.setSupportedProtocol(new MsnProtocol[] { MsnProtocol.MSNP8,
           MsnProtocol.MSNP9, MsnProtocol.MSNP10, MsnProtocol.MSNP11,
           MsnProtocol.MSNP12 });
       messenger.addMessengerListener(MessengerListener.instance);
       messenger.addContactListListener(ContactListListener.instance);
       messenger.addMessageListener(MessageListener.instance);
       messenger.addFileTransferListener(FileTransferListener.instance);
 
       Boolean debug = Boolean.parseBoolean(Play.configuration
           .getProperty("talks.debug"));
       if (Play.mode == Play.Mode.DEV && debug) {
         messenger.setLogIncoming(true);
         messenger.setLogOutgoing(true);
       }
 
       messengers.put(id, messenger);
     } else {
       messenger = messengers.get(id);
     }
 
     messenger.login();
   }
 
   public void logout() {
     if (messengers.containsKey(id)) {
       MsnMessenger messenger = messengers.get(id);
       messenger.logout();
       
       online = false;
       save();
     }
   }
 
   public void remove(String email) {
     if (messengers.containsKey(id)) {
       MsnMessenger messenger = messengers.get(id);
       messenger.removeFriend(Email.parseStr(email), false);
     }
   }
 
   public void renameFriend(String email, String name) {
     if (messengers.containsKey(id)) {
       Alias alias = Alias.find("byGroupAndEmail", this, email).first();
 
       if (alias == null) {
         alias = new Alias(this, email, name);
      } else if (!alias.name.equals(name)) {
         alias.name = name;
       }
 
       MsnMessenger messenger = messengers.get(id);
       messenger.renameFriend(Email.parseStr(email), name);
 
       alias.save();
     }
   }
 
   public int totalContacts() {
     int total = 0;
 
     if (messengers.containsKey(id)) {
       MsnMessenger messenger = messengers.get(id);
       total = messenger.getContactList().getContacts().length;
     }
 
     return total;
   }
 
   public MsnUserStatus status() {
     MsnUserStatus status = online ? MsnUserStatus.ONLINE
         : MsnUserStatus.OFFLINE;
     return status;
   }
 
   public MsnContact contact(String email) {
     MsnContact contact = null;
 
     if (messengers.containsKey(id)) {
       MsnMessenger messenger = messengers.get(id);
       contact = messenger.getContactList().getContactByEmail(
           Email.parseStr(email));
     }
 
     return contact;
   }
 
   public MsnContact[] contacts() {
     MsnContact[] contacts = new MsnContact[0];
     if (messengers.containsKey(id)) {
       MsnMessenger messenger = messengers.get(id);
       contacts = messenger.getContactList().getContacts();
     }
     return contacts;
   }
 
   public Map<String, List<MessageLog>> log(int page, boolean reverse) {
     List<MessageLog> logs = MessageLog.find(
         "groupEmail like ? order by id desc", this.email).fetch(page, 10);
     if (!reverse) {
       Collections.reverse(logs);
     }
 
     SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
     Map<String, List<MessageLog>> maps;
     if (reverse) {
       maps = new TreeMap<String, List<MessageLog>>().descendingMap();
     } else {
       maps = new TreeMap<String, List<MessageLog>>();
     }
 
     for (MessageLog log : logs) {
       String date = formatter.format(log.created);
       List<MessageLog> inMap = maps.get(date);
       if (inMap == null) {
         inMap = new ArrayList<MessageLog>();
         maps.put(date, inMap);
       }
 
       inMap.add(log);
     }
     return maps;
   }
 
   public long totalLogPage() {
     return MessageLog.count("groupEmail like ?", this.email);
   }
 
   public static Group fromEmail(String email) {
     return Group.find("byEmail", email).first();
   }
 }

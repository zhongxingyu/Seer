 package models;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import play.data.validation.Email;
 import play.data.validation.Phone;
 import play.data.validation.Required;
 import play.data.validation.URL;
 import play.db.jpa.Model;
 import play.libs.Codec;
 import utils.CacheUtil;
 import utils.F.Callable;
 
 @Entity
 public class User extends Model {
     
     @Required
     public String username;
     
     @Required
     public String password;
     
     @Required
     public String name;
     
     @Required
     public String surname;
     
     @Phone
     public String phone;
     
     @Required @Email
     public String mail;
     
     public String address;
     
     @URL
     public String avatarUrl;
     
     public String gravatar;
     
     public Boolean connected;
     
     @ManyToOne
     public OrganizationGroup group;
     
     @ManyToMany
     public List<ChatRoom> connectedRooms;
     
     public static User createUser(String username, String name, String surname,
             String phone, String mail, String address, String avatar,
             String gravatar, String password, String groupId) {
         User u = new User();
         u.username = username;
         u.name = name;
         u.surname = surname;
         u.phone = phone;
         u.mail = mail;
         u.address = address;
         u.avatarUrl = avatar;
         u.gravatar = gravatar;
         u.password = Codec.hexSHA1(password);
         u.connected = false;
         u.connectedRooms = new ArrayList<ChatRoom>();
         u = u.save();
         OrganizationGroup group = OrganizationGroup.findByGroupId(groupId);
         group.users.add(u);
         group.save();
         u.group = group;
         return u.save();
     }
     
     public static User findByGroupAndEmail(final @Required String groupId, 
             final @Required @Email String mail) {
         String key = CacheUtil.key("user.", groupId, mail);
         User user = CacheUtil.get(key, User.class, new Callable<User>() {
             public User apply() {
                 return User.find("group.groupId = ? and mail = ?", groupId, mail).first();
             }
         });
         return user;
     }
     
     public static List<User> findByGroupAndConnected(String groupId) {
         return User.find("connected = true and group.groupId = ?", groupId).fetch();
     }
     
     public static User findByGroupAndMailAndPassword(
             final String groupId, final String mail, final String password) {
         String key = CacheUtil.key("user.", groupId, mail, password);
         User user = CacheUtil.get(key, User.class, new Callable<User>() {
             public User apply() {
                 return User.find("group.groupId = ? and mail = ? and password = ?", 
                     groupId, mail, Codec.hexSHA1(password)).first();
             }
         });
         return user;
     }
     
     public String emailHash() {
         return Codec.hexSHA1(mail);
     }
     
     @Override
     public String toString() {
         return username + " (" + name + ", " + surname 
                 + ", " + mail + ") connected: " + connected; 
     }
     
     public static User disconnect(User user) {
         user.connected = false;
         return user.save();
     } 
     
     public static User connect(User user) {
         user.connected = true;
         return user.save();
     } 
 }

 package models;
 
 import com.google.common.base.Strings;
 import play.db.jpa.GenericModel;
 
 import javax.persistence.*;
 import java.util.List;
 
 @Entity(name = "user")
 public class User extends GenericModel {
 
     @Id
     @GeneratedValue
     public Long userId;
     public String name;
     public String hash;
     @OneToMany(cascade = CascadeType.DETACH, mappedBy = "user")
     public List<Member> members;
 
     public static User get(String name, String pass) {
         return find("name = ?1 and hash = ?2", name, encode(pass)).first();
     }
 
     public static User findByIdString(String userIdString) {
         if (Strings.isNullOrEmpty(userIdString)) return null;
         Long id = Long.valueOf(userIdString);
         if (id <= 0) return null;
         return find("userId = ?1", id).first();
     }
 
     public static User createNewUser(String name, String pass) {
         User u = new User();
         u.name = name;
         u.hash = encode(pass);
         return u.save();
     }
 
     public static User dummy() {
         User u = new User();
         u.name = "dummy";
         u.hash = "dummy";
         return u.save();
     }
 
     public static String encode(String pass) {
         // TODO まともなUtil作れや
         return String.format("%d", pass.hashCode());
     }
 }

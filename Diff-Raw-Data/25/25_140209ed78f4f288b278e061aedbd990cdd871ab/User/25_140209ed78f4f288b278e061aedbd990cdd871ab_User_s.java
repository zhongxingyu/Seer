 package models;
 
 import play.*;
 import play.db.jpa.*;
 import play.modules.objectify.Datastore;
 import play.modules.objectify.ObjectifyModel;
 
 import javax.persistence.*;
 
 import com.googlecode.objectify.Key;
 
 import java.util.*;
 
 @Entity
 public class User extends ObjectifyModel<User> {
 	@Id
 	public Long id;
 
 	public String email;
 	public String name;
 	public Date created;
 	public Date modified;
 
 	public static List<User> getAll() {
 		return Datastore.query(User.class).list();
 	}
 
 	public static User findById(Long id) {
 		return Datastore.find(User.class, id);
 	}
 
 	public static User findByEmail(String email) {
 		return Datastore.query(User.class).filter("email", email).get();
 	}
 	
 	public Key<User> save() {
         return Datastore.put(this);
     }
 
 	public User(String email) {
 		this.email = email;
 	}
 
 	public String toString() {
 		return email;
 	}
 }

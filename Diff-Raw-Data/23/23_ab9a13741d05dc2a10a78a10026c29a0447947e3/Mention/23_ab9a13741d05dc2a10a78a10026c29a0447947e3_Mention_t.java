 package models;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import play.db.ebean.Model;
 
 import com.avaje.ebean.validation.NotNull;
 
 @Entity
 @Table(name="mentions")
 public class Mention extends Model {
 	
	private static final long serialVersionUID = 1130982573023854595L;

 	@Id
 	public long id;
 
 	@NotNull
 	@ManyToOne
 	public User user;
 	
 	@NotNull
 	@ManyToOne
 	public Message message;
 	
 	public static Finder<Long,Mention> find = new Finder<Long,Mention>(
 			Long.class, Mention.class
 	);
 
 }

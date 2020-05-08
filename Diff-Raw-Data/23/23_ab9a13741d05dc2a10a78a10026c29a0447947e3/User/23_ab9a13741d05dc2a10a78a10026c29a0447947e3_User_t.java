 package models;
 
 
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.Lob;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 
 import com.avaje.ebean.validation.NotNull;
 
 @Entity
 @Table(name="users")
 public class User extends Model {
 	
	private static final long serialVersionUID = 5206221463639837118L;

 	@Id
 	public long id;
 	
 	@Constraints.Required
 	@NotNull
 	@Constraints.MaxLength(50)
 	@Column(unique=true, length=50)
 	public String login;
 	
 	@Constraints.Required
 	@NotNull
 	public String password;
 	
 	@Constraints.Email
 	@Constraints.Required
 	@NotNull
 	@Column(unique=true)
 	public String email;
 
 	@Constraints.MaxLength(50)
 	@Column(length=50)
 	public String lastname;
 	
 	@Constraints.MaxLength(50)
 	@Column(length=50)
 	public String firstname;
 	
 	@Lob
 	public String description;
 	
 	public String avatar;
 
 	@NotNull
 	public Date registerDate;
 	
 	@ManyToMany
 	@JoinTable(
 	    name = "following",
 	    joinColumns 		= @JoinColumn(name = "user_id", 	referencedColumnName = "id"),
 	    inverseJoinColumns 	= @JoinColumn(name = "friend_id", 	referencedColumnName = "id")
     )
 	public List<User> following;
 	
 	
 	public static Finder<Long,User> find = new Finder<Long,User>(
 		Long.class, User.class
 	);
 	
 
 }

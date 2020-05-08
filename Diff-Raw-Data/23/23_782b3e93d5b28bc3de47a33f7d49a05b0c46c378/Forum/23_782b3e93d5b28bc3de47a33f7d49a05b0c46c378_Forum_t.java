 package models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.*;
 
 import com.avaje.ebean.Ebean;
 
 //import com.avaje.ebean.*;
 import play.db.ebean.*;
 //import play.data.validation.Constraints.*;
 
 @Entity
 public class Forum extends Model
 {
   public static Finder<Long, Forum> find =
     new Finder<Long, Forum>(Long.class, Forum.class);
 
   @Id
   @GeneratedValue(strategy=GenerationType.IDENTITY)
   public Long forumId;
 
  public Date lastActivity = new Date();
 
   public String name;
   public boolean isCrewOnly;
 
   @ManyToOne
   @JoinColumn(name="email")
   public User owner;
 
   @ManyToMany(cascade = {CascadeType.ALL})
   //@JoinTable(name="forum_user",
   //           joinColumns={@JoinColumn(name="forum_id")},
   //           inverseJoinColumns={@JoinColumn(name="user_email")})
   public Set<User> moderators = new HashSet<User>();
 
   @OneToMany(mappedBy="parent")
   public List<ForumThread> threads = new ArrayList<ForumThread>();
 
   public static Forum get(Long id)
   {
     return find.byId(id);
   }
 
   public static List<Forum> crewForums()
   {
     return Ebean.find(Forum.class).where().eq("isCrewOnly", true).findList();
   }
 
   public static List<Forum> publicForums()
   {
     return Ebean.find(Forum.class).where().eq("isCrewOnly", false).findList();
   }
 }

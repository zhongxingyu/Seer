 package models;
 
 import java.util.*;
 
 import javax.persistence.*;
 
 //import com.avaje.ebean.*;
 import play.db.ebean.*;
 //import play.data.validation.Constraints.*;
 
 @Entity
 public class ForumThread extends Model
 {
   public static Finder<Long, ForumThread> find =
     new Finder<Long, ForumThread>(Long.class, ForumThread.class);
 
   @Id
   @GeneratedValue(strategy=GenerationType.IDENTITY)
   public Long threadId;
 
  public Date lastActivity = new Date();
 
   @ManyToOne
   @JoinColumn(name="forumId")
   public Forum parent;
 
   @OneToMany(mappedBy="parent")
   public List<ForumPost> posts = new ArrayList<ForumPost>();
 
   public String title;
 
   public static ForumThread get(Long id)
   {
     return find.byId(id);
   }
 
   @Override
   public String toString()
   {
     return "ForumThread - id: " + threadId + "; title: " + title + "; parent: " + parent + "; " +
            (posts == null ? "null" : String.valueOf(posts.size()) + " posts");
   }
 }

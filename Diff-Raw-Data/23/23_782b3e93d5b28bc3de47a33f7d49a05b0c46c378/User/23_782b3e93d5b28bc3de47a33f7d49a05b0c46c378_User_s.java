 package models;
 
 import java.util.*;
 
 import javax.persistence.*;
 
 import com.avaje.ebean.*;
 
 import play.db.ebean.*;
 import play.data.validation.Constraints.*;
 
 @Entity
 public class User extends Model
 {
   public static Finder<Long, User> find = new Finder<Long, User>(Long.class, User.class);
 
   @Id
   @GeneratedValue(strategy=GenerationType.IDENTITY)
   public Long userId;
   @Email
   public String email;
   public String password;
   public String givenName;
   public String familyName;
   public String nickName;
 
   public boolean isOpenID;
   public boolean isAdmin;
   public boolean isCrew;
   public boolean isCurator;
   public boolean isActivated;
 
   @OneToMany(mappedBy="owner")
   public Set<Forum> forumsOwned;
 
   @ManyToMany(mappedBy="moderators")
   public Set<Forum> forumsModerated;
 
   @OneToMany(mappedBy="author")
   public Set<ForumPost> forumPosts;
 
   @OneToMany(mappedBy="author")
   public Set<NewsItem> newsItems;
 
   public User(String emailAddr, String passwd, String gName, String fName)
   {
     email = emailAddr;
     password = passwd;
 
     givenName = gName;
     familyName = fName;
     nickName = null;
 
    isOpenID = false;
     isAdmin = false;
     isCrew = false;
     isCurator = false;
     isActivated = true; //TODO: false;
   }
 
   public static List<User> range(Integer start, Integer end)
   {
     return find.all().subList(start, end);
   }
 
   public static int count()
   {
     return find.findRowCount();
   }
 
   public static void create(User user)
   {
     user.save();
   }
 
   public static void update(User item)
   {
     Ebean.save(item);
   }
 
   public static User get(Long userId)
   {
     return find.byId(userId);
   }
   
   public static User get(String email)
   {
     List<User> users = Ebean.find(User.class).where().eq("email", email).findList();
     if (users.size() > 0)
     {
       return users.get(0);
     }
     return null;
   }
 
   public static List<User> getAdmins()
   {
     return Ebean.find(User.class).where().eq("isAdmin", true).findList();
   }
 
   public static User connect(String emailAddr, String passwd)
   {
     try
     {
       List<User> users =
         Ebean.find(User.class)
           .where()
             .eq("email", emailAddr)
             .eq("password", passwd)
           .findList();
       if (users.size() > 0)
       {
         return users.get(0);
       }
     }
     catch (Exception e) { }
     return null;
   }
 
   public static boolean exists(String email)
   {
     try
     {
       User existing = get(email);
       existing.refresh();
     }
     catch (Exception e)
     {
       return false;
     }
     return true;
   }
 
   public static boolean exists(Long userId)
   {
     try
     {
       User existing = get(userId);
       existing.refresh();
     }
     catch (Exception e)
     {
       return false;
     }
     return true;
   }
 
   public String toString()
   {
     StringBuffer sb = new StringBuffer();
     sb.append(email).append(": ");
     sb.append(givenName).append(" ").append(familyName);
     if (isAdmin) sb.append(", admin");
     if (isCrew) sb.append(", crew");
     if (isCurator) sb.append(", curator");
     if (isActivated) sb.append(", activated");
     return sb.toString();
   }
 
   private static boolean emptyString(String s)
   {
     return s == null || s.length() == 0;
   }
 
   public String handle()
   {
     if (!emptyString(nickName))
     {
       return nickName;
     }
     else if (!emptyString(givenName) || !emptyString(familyName))
     {
       return givenName + " " + familyName;
     }
     return email;
   }
 }

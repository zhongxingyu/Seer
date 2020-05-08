 package models;
 
 import java.util.List;
 
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 
 import org.joda.time.DateTime;
 
 import play.db.ebean.Model;
 
 import com.avaje.ebean.ExpressionList;
 
 /**
  * Represents a user of Tutor.me
  */
 public abstract class User extends Model {
 
   // : The name of the field that could contain a reference to this user in a request object
  private static abstract final String requestSelfFieldName;
   
   // : The name of the field that could contain a reference to this user in a session object
  private static abstract final String sessionSelfFieldName;
 
   private static final long serialVersionUID = -2547732540294543775L;
 
   @Id
   @GeneratedValue
   public Long id;
 
   // The username of this user on Tutor.me
   private String username;
 
   // The email address of this user
   private String email;
 
   // The full name of this user
   private String name;
 
   public static Finder<Long, User> find = new Finder<Long, User>(Long.class,
       User.class);
 
   public static List<? extends User> all() {
     return find.all();
   }
 
   public static void create(User user) {
     user.save();
   }
 
   public static void delete(Long id) {
     find.ref(id).delete();
   }
 
   /**
    * @return the username
    */
   public String getUsername() {
     return username;
   }
 
   /**
    * @param username the username to set
    */
   public void setUsername(String username) {
     this.username = username;
   }
 
   /**
    * @return the email
    */
   public String getEmail() {
     return email;
   }
 
   /**
    * @param email the email to set
    */
   public void setEmail(String email) {
     this.email = email;
   }
 
   /**
    * @return the name
    */
   public String getName() {
     return name;
   }
 
   /**
    * @param name the name to set
    */
   public void setName(String name) {
     this.name = name;
   }
 
   /**
    * Gets the requests associated with this user
    * 
    * @return: The list of Requests associated with this user
    */
   public List<Request> getRequests() {
     ExpressionList<Request> requestResults = Request.find.where().eq(
         requestSelfFieldName, this);
     return requestResults.findList();
   }
 
   /**
    * Gets the upcoming tutoring sessions for this user
    * 
    * @return: The list of upcoming Sessions associated with this user
    */
   public List<Session> getUpcomingSessions() {
     ExpressionList<Session> upcomingSessionResults = Session.find.where()
     .eq(sessionSelfFieldName, this).gt("startTime", DateTime.now());
     return upcomingSessionResults.findList();
   }
 
   /**
    * Gets the completed tutoring sessions for this user
    * 
    * @return: The list of completed Sessions associated with this user
    */
   public List<Session> getCurrentSessions(String selfFieldName) {
     ExpressionList<Session> completedSessionResults = Session.find.where()
     .eq(sessionSelfFieldName, this).le("startTime", DateTime.now())
         .ge("endTime", DateTime.now());
     return completedSessionResults.findList();
   }
 
   /**
    * Gets the completed tutoring sessions for this user
    * 
    * @return: The list of completed Sessions associated with this user
    */
   public List<Session> getCompletedSessions(String selfFieldName) {
     ExpressionList<Session> completedSessionResults = Session.find.where()
     .eq(sessionSelfFieldName, this).lt("endTime", DateTime.now());
     return completedSessionResults.findList();
   }
 
   /**
    * Logs this user in
    */
   public void logIn() {
     //TODO
   }
 
   /**
    * Logs this user out
    */
   public void logOut() {
     //TODO
   }
 }

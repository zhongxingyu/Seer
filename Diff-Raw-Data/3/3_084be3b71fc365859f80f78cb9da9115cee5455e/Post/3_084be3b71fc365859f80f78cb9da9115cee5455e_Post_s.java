 package Domain;
 
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 
 import java.sql.Timestamp;
 
 /**
  * Created with IntelliJ IDEA.
  * User: penghaozhang
  * Date: 6/04/13
  * Time: 3:40 PM
  * To change this template use File | Settings | File Templates.
  */
 //do not export the user object when converting to json
 @JsonIgnoreProperties({"user"})
 public class Post {
     private long id;
     private String subject;
     private String content;
     private User user;
     private Timestamp created;
    private String weather;
 
     public Post() {
 
     }
 
     public Post(User user, String subject, String content, String weather) {
         this.user = user;
         this.subject = subject;
         this.content = content;
         this.weather = weather;
         java.util.Date date = new java.util.Date();
         this.created = new Timestamp(date.getTime());
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public String getSubject() {
         return subject;
     }
 
     public void setSubject(String subject) {
         this.subject = subject;
     }
 
     public String getContent() {
         return content;
     }
 
     public void setContent(String content) {
         this.content = content;
     }
 
     public User getUser() {
         return user;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
     public Timestamp getCreated() {
         return created;
     }
 
     public void setCreated(Timestamp created) {
         this.created = created;
     }
 
     public String getWeather() {
         return weather;
     }
 
     public void setWeather(String weather) {
         this.weather = weather;
     }
 }

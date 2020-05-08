 package models;
 
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import org.hibernate.annotations.Type;
 import org.joda.time.DateTime;
 
 import play.db.ebean.Model;
 
 @Entity
@Table(name="Log")
 public class Log extends Model{
     
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = 8324336942908626934L;
 
 	@Id
     @GeneratedValue
     @Column(name="log_id")
     private Long id;
     
     @Column(length=300)
     private String action;
     
     @Temporal(TemporalType.TIMESTAMP)
     @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
     private DateTime time;
     
     @ManyToOne
     @JoinColumn(name="user_id", insertable=true, updatable=true)
     private User user;
     
     @Column
     private String resourceURI;
     
     public static Model.Finder<Long,Log> find = new Model.Finder<Long, Log>(
             Long.class,Log.class
     );
     
     public static List<Log> all(){
         return find.all();
     }
     
     public static void create(Log log){
         log.save();
     }
     
     public static Log createObject(Log log){
         log.save();
         return log;
     }
     
     public static void delete(Long id){
         find.ref(id).delete();
     }
     
     public static Log read(Long id){
         return find.byId(id);
     }
     
     public static List<Log> readByUser(User user) {
     	return find.where().eq("user", user).findList();
     }
     
     
     //Getters & Setters
     
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
     
     public String getAction() {
         return action;
     }
 
     public void setAction(String action) {
         this.action = action;
     }
     
     public DateTime getTime() {
         return time;
     }
 
     public void setTime(DateTime time) {
         this.time = time;
     }
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	public String getResourceURI() {
 		return resourceURI;
 	}
 
 	public void setResourceURI(String resourceURI) {
 		this.resourceURI = resourceURI;
 	}
     
 }

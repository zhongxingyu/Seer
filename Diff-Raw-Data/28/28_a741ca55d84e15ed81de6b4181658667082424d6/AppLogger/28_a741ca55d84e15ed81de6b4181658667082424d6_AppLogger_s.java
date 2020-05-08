 package models;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 import play.db.ebean.Model;
 
 @Entity
 public class AppLogger extends Model{
 
 	private static final long serialVersionUID = 1L;
 	
 	@Id
 	public Long id;
 	
 	public String appName;
 	public String appSize;
 	public String event;
 	public String dateStamp;
 	public String timeStamp;
 	public String content;
 	public String displayID;
 	
 	public static Finder<Long, AppLogger> find = new Finder<Long, AppLogger>(Long.class, AppLogger.class);
 
 	
 	public static List<AppLogger> all() {
 		return find.all();
 	}
 	
 	public static AppLogger addNew(AppLogger dl) {
 		dl.save();
 		return dl;
 	}
 	
 	public static AppLogger get(Long id){
 		return find.ref(id);
 	}
 	
 	public AppLogger(String appName, String appSize, String event, String content, String displayID) {
 		this.appName = appName;
 		this.appSize = appSize;
 		this.event = event;
		this.timeStamp = new SimpleDateFormat("hh:mm:ss").format(new Date());;
		this.dateStamp = new SimpleDateFormat("dd-MM-yyyy").format(new Date());;
 		this.content = content;
 		this.displayID = displayID;
 	}
 	
 	
 }

 /**
  * 
  */
 package bzb.gae.summer.jdo;
 
 import java.util.List;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 import bzb.gae.PMF;
 
 import com.google.appengine.api.datastore.Key;
 
 /**
  * @author bzb
  *
  */
 @PersistenceCapable
 public class Group {
 
 	private static final int TOTAL_CAPACITY = 4;
 	
 	@PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private Key key;
 	
 	@Persistent
 	private String arrivalTime;
 	
 	public Group (String arrivalTime) {
 		setArrivalTime(arrivalTime);
 	}
 
 	public Key getKey() {
         return key;
     }
 	
 	public void setArrivalTime(String arrivalTime) {
 		this.arrivalTime = arrivalTime;
 	}
 
 	public String getArrivalTime() {
 		return arrivalTime;
 	}
 	
 	public int size() {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
			Query q = pm.newQuery("select username from " + User.class.getName() + " where groupKey == " + getKey());
 			return ((List<String>) q.execute()).size();
 		} finally {
 			pm.close();
 		}
 	}
 	
 	public boolean moreRoom () {
 		if (size() < TOTAL_CAPACITY) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 }

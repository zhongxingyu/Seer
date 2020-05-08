 package retweeter;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 import org.apache.commons.lang.ObjectUtils;
 
 import com.google.appengine.api.datastore.Key;
 
 @PersistenceCapable
 public class User implements Serializable {
 	private static final long serialVersionUID = -2890149582037852180L;
 
 	@PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private Key key;
 
 	@Persistent
 	List<String> accountsToWatch;
 	
 	@Persistent
 	List<String> hashTagsToWatch;
 	
 	@Persistent
 	String userOAuthToken;
 	
 	@Persistent
 	String userOAuthSecret;
 	
 	@Persistent
 	String screenName;
 
	@Persistent(serialized="true")
 	Map<String,Long> lastStatusIdMap; // map of search query to tweet ID
 	
 	public User() {
 	}
 	public User(String screenName) {
 		accountsToWatch = new ArrayList<String>();
 		hashTagsToWatch = new ArrayList<String>();
 		lastStatusIdMap = new HashMap<String,Long>();
 		this.screenName = screenName;
 	}
 	
 	public Key getKey() {
 		return key;
 	}
 	public void setKey(Key key) {
 		this.key = key;
 	}
 	public List<String> getAccountsToWatch() {
 		return accountsToWatch;
 	}
 	public void setAccountsToWatch(List<String> accountsToWatch) {
 		this.accountsToWatch = accountsToWatch;
 	}
 	public List<String> getHashTagsToWatch() {
 		return hashTagsToWatch;
 	}
 	public void setHashTagsToWatch(List<String> hashTagsToWatch) {
 		this.hashTagsToWatch = hashTagsToWatch;
 	}
 	public String getUserOAuthToken() {
 		return userOAuthToken;
 	}
 	public void setUserOAuthToken(String consumerToken) {
 		this.userOAuthToken = consumerToken;
 	}
 	public String getUserOAuthSecret() {
 		return userOAuthSecret;
 	}
 	public void setUserOAuthSecret(String userOAuthSecret) {
 		this.userOAuthSecret = userOAuthSecret;
 	}
 	
 	public static User findWithToken(PersistenceManager pm, String userOAuthToken) {
 		Query q = pm.newQuery(User.class);
 		q.declareParameters("String userOAuthToken");
 		q.setFilter("this.userOAuthToken == userOAuthToken");
 		q.setUnique(true);
 		return (User) q.execute(userOAuthToken);
 	}
 
 	public static User findWithScreenName(PersistenceManager pm, String screenName) {
 		Query q = pm.newQuery(User.class);
 		q.declareParameters("String screenName");
 		q.setFilter("this.screenName == screenName");
 		q.setUnique(true);
 		return (User) q.execute(screenName);
 	}
 
 	public static User getOrCreateWithScreenName(PersistenceManager pm, String screenName) {
 		User existing = findWithScreenName(pm, screenName);
 		if(existing != null)
 			return existing;
 		User user = new User(screenName);
 		pm.makePersistent(user);
 		return user;
 	}
 	public void setScreenName(String screenName) {
 		this.screenName = screenName;
 	}
 	public String getScreenName() {
 		return screenName;
 	}
 	public Map<String, Long> getLastStatusIdMap() {
 		return lastStatusIdMap;
 	}
 	public void setLastStatusIdMap(Map<String, Long> lastStatusIdMap) {
 		this.lastStatusIdMap = lastStatusIdMap;
 	}
 	public Long getLastStatusId(String query) {
 		if(lastStatusIdMap == null) return null; 
 			lastStatusIdMap = new HashMap<String, Long>();
 		return lastStatusIdMap.get(query);
 	}
 	public void setLastStatusId(String query, Long statusId) {
 		if(lastStatusIdMap == null)
 			lastStatusIdMap = new HashMap<String, Long>();
 		lastStatusIdMap.put(query, statusId); 		
 	}
 }

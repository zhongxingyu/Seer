 package uk.ac.cam.dashboard.models;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import uk.ac.cam.cl.ldap.LDAPObjectNotFoundException;
 import uk.ac.cam.cl.ldap.LDAPQueryManager;
 import uk.ac.cam.dashboard.queries.DeadlineQuery;
 import uk.ac.cam.dashboard.util.HibernateUtil;
 
 import com.google.common.collect.ImmutableMap;
 
 @Entity
 @Table(name="USERS")
 public class User {
 	@Id
 	private String crsid;
 	
 	private String username;
 	
 	@OneToOne
 	private Settings settings; 
 	
 	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval=true)
 	private Set<DeadlineUser> deadlines = new HashSet<DeadlineUser>();
 
 	@ManyToMany(mappedBy = "users")
 	private Set<Group> subscriptions = new HashSet<Group>();
 	
 	@OneToMany(mappedBy = "owner")
 	private Set<Group> groups = new HashSet<Group>();
 	
 	@OneToMany(mappedBy = "user")
 	private Set<Api> apis = new HashSet<Api>();
 	
 	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval=true)
 	private Set<NotificationUser> notifications = new HashSet<NotificationUser>();
 	
 	public User() {}
 	public User(String crsid) {
 		this.crsid = crsid;
 		this.username = this.retrieveUsername(crsid);
		this.settings = new Settings();
 	}
 	
 	public Settings getSettings() {return settings;}
 	public void setSettings(Settings settings) {this.settings = settings;}
 	
 	public String getCrsid() {return crsid;}
 	public void setCrsid(String crsid) {this.crsid = crsid;}
 	
 	public String getUsername() {return username;}
 	public void setUsername(String username) {this.username = username;}
 	
 	public Set<DeadlineUser> getDeadlines() { return deadlines; }
 	public void clearDeadlines() { deadlines.clear(); }
 	public void addDeadlines(Set<DeadlineUser> deadlines) { this.deadlines.addAll(deadlines); }
 	
 	public Set<Group> getGroups() { return this.groups; }
 	public void addGroups(Set<Group> groups) { this.groups.addAll(groups); }
 
 	public Set<Group> getSubscriptions() { return this.subscriptions; }
 	public void addSubscriptions(Set<Group> subscriptions) { this.subscriptions.addAll(subscriptions); }
 
 	public Set<NotificationUser> getNotifications() { return this.notifications; }
 	
 	public Set<Api> getApis() { return this.apis; }
 	public void addApi(Api api) { this.apis.add(api); }
 	public void addApis(Set<Api> apis) { this.apis.addAll(apis); }
 	
 	public static User registerUser(String crsid){
 
 		Session session = HibernateUtil.getTransactionSession();
 		
 		Query userQuery = session.createQuery("from User where id = :id").setParameter("id", crsid);
 	  	User user = (User) userQuery.uniqueResult();
 	  	
 	  	// If user not in database, check if they exist in LDAP and create them if so
 	  	if(user==null){
 	  		
 	  		try {
 	  			LDAPQueryManager.getUser(crsid);
 	  		} catch(LDAPObjectNotFoundException e){
 	  			//User doesn't exit - return null
 	  			return null;
 	  		}
 	  		
 	  		User newUser = new User(crsid);
 	  		session.save(newUser);
 	  		return newUser;
 	  	}
 		
 		return user;
 	}
 	
 	public String retrieveUsername(String crsid) {
 		
   		try {
   			String name = LDAPQueryManager.getUser(crsid).getcName();
   			return name;
   		} catch(LDAPObjectNotFoundException e){
   			return "Unknown user";
   		}
   		
 	}
 	
 	// Maps
 	public Set<Map<String, ?>> groupsToMap() {
 		HashSet<Map<String, ?>> userGroups = new HashSet<Map<String, ?>>();
 		
 		if(groups==null){
 			return new HashSet<Map<String, ?>>();
 		}
 		
 		for(Group g : groups)  {
 			userGroups.add(g.toMap());
 		}
 		return userGroups;
 	}
 	
 	public Set<Map<String, ?>> subscriptionsToMap() {
 		HashSet<Map<String, ?>> userSubscriptions = new HashSet<Map<String, ?>>();
 		
 		if(subscriptions==null){
 			return new HashSet<Map<String, ?>>();
 		}
 		
 		for(Group g : subscriptions)  {
 			userSubscriptions.add(g.toMap());
 		}
 		return userSubscriptions;
 	}
 	
 	public List<Map<String, ?>> deadlinesToMap() {
 		
 		List<Map<String, ?>> userDeadlines = new ArrayList<Map<String, ?>>();
 		
 		DeadlineQuery dq = DeadlineQuery.set();
 		dq.byUser(this);
 		
 		if(deadlines==null){
 			return new ArrayList<Map<String, ?>>();
 		}
 		
 		List<DeadlineUser> results = dq.setList();
 		
 		for (DeadlineUser d : results) {
 			userDeadlines.add(d.toMap());
 		}
 		
 		return userDeadlines;	
 	}
 	
 	public List<Map<String, ?>> createdDeadlinesToMap() {
 		
 		List<Map<String, ?>> userDeadlines = new ArrayList<Map<String, ?>>();
 		
 		DeadlineQuery dq = DeadlineQuery.created();
 		dq.byOwner(this);
 		
 		if(deadlines==null){
 			return new ArrayList<Map<String, ?>>();
 		}
 		
 		List<Deadline> results = dq.createdList();
 		
 		for (Deadline d : results) {
 			userDeadlines.add(d.toMap());
 		}
 		
 		return userDeadlines;
 	}
 	
 	public List<String> apisToMap(){
 		List<String> userApis = new ArrayList<String>();
 		
 		for(Api a : apis){
 			userApis.add(a.getKey());
 		}
 		
 		return userApis;
 	}
 	
 	// equals
 	@Override
 	public boolean equals(Object object){
 		//check for self-comparison
 		if(this == object) return true;
 		
 		//check that the object is a user
 		if(!(object instanceof User)) return false;
 		
 		//compare crsids
 		return (((User) object).getCrsid().equals(this.crsid));
 	}
 	
 	public ImmutableMap<String, ?> toMap() {
 		return ImmutableMap.of("crsid", crsid);
 	}
 }

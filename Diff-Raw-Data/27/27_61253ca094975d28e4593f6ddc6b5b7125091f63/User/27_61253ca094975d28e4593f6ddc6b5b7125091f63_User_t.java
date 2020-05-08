 package uk.ac.cam.dashboard.models;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
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
 
 import org.hibernate.Session;
 
 import uk.ac.cam.cl.dtg.ldap.LDAPObjectNotFoundException;
 import uk.ac.cam.cl.dtg.ldap.LDAPQueryManager;
 import uk.ac.cam.cl.dtg.ldap.LDAPUser;
 import uk.ac.cam.dashboard.queries.DeadlineQuery;
 import uk.ac.cam.dashboard.queries.GroupQuery;
 import uk.ac.cam.dashboard.queries.UserQuery;
 import uk.ac.cam.dashboard.util.HibernateUtil;
 import uk.ac.cam.dashboard.util.Strings;
 
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
 	public User(String crsid, Settings settings) {
 		this.crsid = crsid;
 		this.settings = settings;
 		this.username = this.getName();
 	}
 	
 	public Settings getSettings() {return settings;}
 	public void setSettings(Settings settings) {this.settings = settings;}
 	
 	public String getCrsid() {return crsid;}
 	public void setCrsid(String crsid) {this.crsid = crsid;}
 	
 	public String getUsername() {return username;}
 	public void setUsername(String username) {this.username = username;}
 	public String getName() {
 		if(this.username==null){
 			try {
 				LDAPUser u = LDAPQueryManager.getUser(crsid);
 				this.username = u.getcName();
 			} catch(LDAPObjectNotFoundException e){
 				this.username = Strings.USER_NOUSERNAME;
 			}			
 		}
 		return this.username;
 	}
 	
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
 
 	  	UserQuery uq = UserQuery.all();
 	  	uq.byCrsid(crsid);
 	  	User user = uq.uniqueResult();
 	  	
 	  	if(user==null){	  		
 			try { LDAPQueryManager.getUser(crsid); } 
 			catch(LDAPObjectNotFoundException e){ return null;}
 			
 	  		Session session = HibernateUtil.getTransactionSession();
 	  		Settings s = new Settings();
 	  		session.save(s);	
 	  		user = new User(crsid, s);
 	  		session.save(user);
 	  	}
 		
 		return user;
 	}
 	
 	public Map<String, Object> getUserDetails(){
 
 		try {
 			HashMap<String, Object> userMap = new HashMap<String, Object>();
 			LDAPUser user = LDAPQueryManager.getUser(crsid);
 			userMap.putAll(user.getAll());
 			userMap.put("supervisor", this.getSettings().getSupervisor());
 			return userMap;
 		} catch (LDAPObjectNotFoundException e){
 			HashMap<String, Object> defaultUser = new HashMap<String, Object>();
 			
 			defaultUser.put("crsid", crsid);
			defaultUser.put("name", Strings.USER_NOUSERNAME);
			defaultUser.put("username", username);
 			defaultUser.put("surname", Strings.USER_NOSURNAME);
 			defaultUser.put("email", Strings.USER_NOEMAIL);
 			defaultUser.put("institution", Strings.USER_NOINST);
 			defaultUser.put("status", Strings.USER_NOSTATUS);
 			defaultUser.put("photo", Strings.USER_NOPHOTO);
 			defaultUser.put("supervisor", this.getSettings().getSupervisor());
 
 			return defaultUser;
 		}
 		
 	}
 	
 	// Maps
 	public List<Map<String, ?>> groupsToMap() {
 		List<Map<String, ?>> userGroups = new ArrayList<Map<String, ?>>();
 		
 		List<Group> groups = GroupQuery.all().byOwner(this).list();
 
 		if(groups==null){ return new ArrayList<Map<String, ?>>();}
 		
 		for(Group g : groups)  {
 			userGroups.add(g.toMap());
 		}
 		
 		return userGroups;
 	}
 	
 	public List<Map<String, ?>> subscriptionsToMap() {
 		List<Map<String, ?>> userSubscriptions = new ArrayList<Map<String, ?>>();
 				
 		List<Group> subscriptions = GroupQuery.all().byMember(this).list();
 		
 		if(subscriptions==null){ return new ArrayList<Map<String, ?>>(); }
 		
 		for(Group g : subscriptions)  {
 			userSubscriptions.add(g.toMap());
 		}
 		
 		return userSubscriptions;
 	}
 	
 	public List<Map<String, ?>> createdDeadlinesToMap() {
 		
 		List<Map<String, ?>> userDeadlines = new ArrayList<Map<String, ?>>();
 		
 		DeadlineQuery dq = DeadlineQuery.created();
 		dq.byOwner(this);
 		dq.afterDate(Calendar.getInstance());
 		
 		if(deadlines==null){ return new ArrayList<Map<String, ?>>();}
 		
 		List<Deadline> results = dq.createdList();
 		
 		for (Deadline d : results) {
 			userDeadlines.add(d.toMap());
 		}
 		
 		return userDeadlines;
 	}
 	
 	public List<String> apisToMap(){
 		List<String> userApis = new ArrayList<String>();
 		
 		if(apis==null){ return new ArrayList<String>();}
 		
 		for(Api a : apis){
 			userApis.add(a.getKey());
 		}
 		
 		return userApis;
 	}
 	
 	public ImmutableMap<String, ?> toMap() {
 		return ImmutableMap.of("crsid", crsid, "name", getName());
 	}
 	
 }

 package uk.ac.cam.signups.models;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import uk.ac.cam.signups.util.HibernateUtil;
 
 import com.google.common.collect.ImmutableMap;
 
 @Entity
 @Table(name="USERS")
 public class User {
 	@Id
 	private String crsid;
 	
 	@ManyToMany(mappedBy = "users")
 	private Set<Deadline> deadlines = new HashSet<Deadline>(0);
 
 	@OneToMany(mappedBy = "owner")
 	private Set<Event> events = new HashSet<Event>(0);
 
 	@ManyToMany(mappedBy = "users")
 	private Set<Group> subscriptions = new HashSet<Group>(0);
 	
 	@OneToMany(mappedBy = "owner")
 	private Set<Group> groups = new HashSet<Group>(0);
 
 	@OneToMany(mappedBy = "owner")
 	private Set<Slot> slots = new HashSet<Slot>(0);
 	
 	public User() {}
 	public User(String crsid, 
 							Set<Deadline> deadlines, 
 							Set<Event> events, 
 							Set<Group> groups,
 							Set<Group> subscriptions,
 							Set<Slot> slots) {
 		this.crsid = crsid;
 		this.events = events;
 		this.deadlines = deadlines;
 		this.groups = groups;
 		this.slots = slots;
 		this.subscriptions = subscriptions;
 	}
 	
 	public String getCrsid() {return crsid;}
 	public void setCrsid(String crsid) {this.crsid = crsid;}
 	
 	public Set<Deadline> getDeadlines() { return deadlines; }
 	public void setDeadlines(Set<Deadline> deadlines) { this.deadlines = deadlines; }
 	
 	public Set<Event> getEvents() { return events; }
 	public void setEvents(Set<Event> events) { this.events = events; }
 	
 	public Set<Slot> getSlots() { return slots; }
 	public void setSlots(Set<Slot> slots) { this.slots = slots; }
 	
 	public Set<Group> getGroups() { return this.groups; }
 	public void setGroups(Set<Group> groups) { this.groups = groups; }
 
 	public Set<Group> getSubscriptions() { return this.subscriptions; }
 	public void setSubscriptions(Set<Group> subscriptions) { this.subscriptions = subscriptions; }
 	
 	// Register user from CRSID
 	public static User registerUser(String crsid){
 		// Add user to database if necessary
 
 		// Begin hibernate session
 		Session session = HibernateUtil.getTransaction();
 		
 		// Does the user already exist?
 		Query userQuery = session.createQuery("from User where id = :id").setParameter("id", crsid);
 	  	User user = (User) userQuery.uniqueResult();
 	  	
 	  	// If no, create them
 	  	if(user==null){
 	  		User newUser = new User(crsid, null, null, null, null, null);
 	  		session.save(newUser);
 			session.getTransaction().commit();
 	  		return newUser;
 	  	}
 
 	  	// Close hibernate session
 		session.getTransaction().commit();
 		
 		return user;
 	}
 	
 	// Soy friendly get methods
	public Set<ImmutableMap<String, ?>> getGroupsMap() {
 		HashSet<ImmutableMap<String, ?>> userGroups = new HashSet<ImmutableMap<String, ?>>(0);
 		
 		if(groups==null){
 			return new HashSet<ImmutableMap<String, ?>>(0);
 		}
 		
 		for(Group g : groups)  {
 			userGroups.add(ImmutableMap.of("id", g.getId(), "name", g.getTitle(), "users", g.getUsersMap(), "owner", g.getOwner().getCrsid()));
 		}
 		return userGroups;
 	}
 }

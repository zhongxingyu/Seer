 package uk.ac.cam.signups.models;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Id;
 import javax.persistence.GeneratedValue;
 import javax.persistence.ManyToMany;
 import javax.persistence.JoinTable;
 import javax.persistence.JoinColumn;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.core.Context;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.annotations.GenericGenerator;
 
 import uk.ac.cam.signups.util.HibernateSessionRequestFilter;
 import uk.ac.cam.signups.util.UserLookupManager;
 
 import com.google.common.collect.ImmutableMap;
 
 @Entity
 @Table(name="GROUPS")
 public class Group {
 
     @Id
 	@GeneratedValue(generator="increment")
 	@GenericGenerator(name="increment", strategy="increment")
 	private int id;
 
 	@FormParam("title") private String title;
 
 	@ManyToMany(cascade = CascadeType.ALL)
 	@JoinTable(name="GROUPS_USERS",
 						joinColumns = {@JoinColumn(name = "GROUP_ID")},
 						inverseJoinColumns = {@JoinColumn(name = "USER_CRSID")})
 	private Set<User> users = new HashSet<User>(0);
 	
 	@ManyToOne
	@JoinTable(name="USER_CRSID")
 	private User owner;
 	
 	public Group() { }
 	
 	public Group(int id, 
 				String title, 
 				Set<User> users,
 				User owner) {
 		this.id = id;
 		this.title = title;
 		this.users = users;
 		this.owner = owner;
 	}
 	
 	public int getId() { return this.id; }
 	public void setId(int id) { this.id = id; }
 	
 	public String getTitle() { return this.title; }
 	public void setTitle(String title) { this.title = title; }
 	
 	public Set<User> getUsers() { return this.users; }
 	public void setUsers(Set<User> users) { this.users = users; }
 	
 	public User getOwner() { return this.owner; }
 	public void setOwner(User owner) { this.owner = owner; }
 	
 	// Create group 
 	
 	// Soy friendly get methods
 	public HashSet getUsersMap() {
 		HashSet<ImmutableMap<String,?>> groupUsers = new HashSet<ImmutableMap<String,?>>();
 		String crsid;
 		for(User u : users){
 			crsid = u.getCrsid();
 			groupUsers.add(ImmutableMap.of("crsid",crsid));
 		}
 		return groupUsers;
 	}
 }

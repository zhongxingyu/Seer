 package uk.ac.cam.signups.models;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 import javax.persistence.GeneratedValue;
 import javax.persistence.JoinColumn;
 
 import org.hibernate.annotations.GenericGenerator;
 
 @Entity
 @Table(name="DEADLINES")
 public class Deadline {
 	@Id
 	@GeneratedValue(generator="increment")
 	@GenericGenerator(name="increment", strategy = "increment")
 	private int id;
 
 	private String title;
 	private String message;
	private String url;
 
 	@ManyToMany(cascade = CascadeType.ALL)
 	@JoinTable(name = "DEADLINES_USERS", 
 						joinColumns = { @JoinColumn(name = "DEADLINE_ID")},
 						inverseJoinColumns = { @JoinColumn(name = "USER_CRSID")})
 	private Set<User> users = new HashSet<User>(0);
 	
 	public Deadline() {}
 	public Deadline(int id, 
 									String title, 
 									String message, 
 									Set<User> users) {
 		this.id = id;
 		this.title = title;
 		this.message = message;
 		this.users = users;
 	}
 	
 	public int getId() { return this.id; }
 	public void setId(int id) { this.id = id; }
 	
 	public String getTitle() { return this.title; }
 	public void setTitle(String title) { this.title = title; }
 
 	public String getMessage() { return this.message; }
 	public void setMessage(String message) { this.message= message; }
 	
 	public Set<User> getUsers() { return this.users; }
 	public void setUsers(Set<User> users) { this.users = users; }
 }

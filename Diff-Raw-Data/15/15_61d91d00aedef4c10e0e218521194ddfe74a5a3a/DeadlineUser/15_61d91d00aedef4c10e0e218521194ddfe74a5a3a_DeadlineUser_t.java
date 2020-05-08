 package uk.ac.cam.dashboard.models;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.GenericGenerator;
 
 import com.google.common.collect.ImmutableMap;
 
 @Entity
@Table(name="DEADLINES_USERS")
 public class DeadlineUser implements Mappable{
 	
 	@Id
 	@GeneratedValue(generator="increment")
 	@GenericGenerator(name="increment", strategy="increment")
 	private int id;
 	
 	@ManyToOne
 	@JoinColumn(name="user_crsid")
 	private User user;
 	
 	@ManyToOne
 	@JoinColumn(name="deadline_id")
 	private Deadline deadline;
 	
 	private boolean complete;
 	
 	private boolean archived;
 	
 	public DeadlineUser() {}
 	public DeadlineUser(User u, Deadline d) {
 		this.user = u;
 		this.deadline = d;
 		this.complete = false;
 		this.archived = false;
 	}
 	
 	public int getId() {return id;}
 	
 	public User getUser() { return user; }
 	public void setUser(User user) { this.user = user; }
 	
 	public Deadline getDeadline() { return deadline; }
 	public void setDeadline(Deadline deadline) { this.deadline = deadline; }
 	
 	public boolean getComplete() { return complete; }
 	public void setComplete(boolean complete) { this.complete = complete; }
 	
 	@Override
 	public ImmutableMap<String, ?> toMap() {
 		
 		ImmutableMap.Builder<String, Object> map = new ImmutableMap.Builder<String, Object>();
 		
 		map.put("id", this.id);
 		map.put("complete", this.complete);
 		map.put("archived", this.archived);
 		map.put("user", this.user.getCrsid());
 		map.put("deadline_id", this.deadline.getId());
 		//TODO
 		
 		return map.build();
 	}
 
 	
 }

 package cz.cvut.fel.bupro.model;
 
 import java.io.Serializable;
 import java.sql.Timestamp;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 
 import cz.cvut.fel.bupro.TimeUtils;
 
 @Entity
 public class Project extends CommentableEntity implements Serializable {
 	private static final long serialVersionUID = -4565743549559354326L;
 
 	@Column(unique = true, nullable = false)
 	private String name;
 	private String description;
 
 	@ManyToOne(optional = false)
 	private User owner;
 	@Column(nullable = false)
 	private Timestamp creationTime;
 
 	@OneToMany
 	private List<Membership> memberships = new LinkedList<Membership>();
 
 	@ManyToMany
 	private Set<Tag> tags = new HashSet<Tag>();
 
	@ManyToOne
 	private Subject subject;
 
 	private int capacity;
 
 	private boolean autoApprove;
 
 	@ManyToOne
 	private Semester startSemester;
 	@ManyToOne
 	private Semester endSemester;
 
 	public Project() {
 		creationTime = TimeUtils.createCurrentTimestamp();
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public User getOwner() {
 		return owner;
 	}
 
 	public void setOwner(User owner) {
 		this.owner = owner;
 	}
 
 	public Timestamp getCreationTime() {
 		return creationTime;
 	}
 
 	public void setCreationTime(Timestamp creationTime) {
 		this.creationTime = creationTime;
 	}
 
 	public List<Membership> getMemberships() {
 		return memberships;
 	}
 
 	public void setMemberships(List<Membership> memberships) {
 		this.memberships = memberships;
 	}
 
 	public Set<Tag> getTags() {
 		return tags;
 	}
 
 	public void setTags(Set<Tag> tags) {
 		this.tags = tags;
 	}
 
 	public Subject getSubject() {
 		return subject;
 	}
 
 	public void setSubject(Subject subject) {
 		this.subject = subject;
 	}
 
 	public int getCapacity() {
 		return capacity;
 	}
 
 	public void setCapacity(int capacity) {
 		this.capacity = capacity;
 	}
 
 	public boolean isAutoApprove() {
 		return autoApprove;
 	}
 
 	public void setAutoApprove(boolean autoApprove) {
 		this.autoApprove = autoApprove;
 	}
 
 	public Semester getStartSemester() {
 		return startSemester;
 	}
 
 	public void setStartSemester(Semester startSemester) {
 		this.startSemester = startSemester;
 	}
 
 	public Semester getEndSemester() {
 		return endSemester;
 	}
 
 	public void setEndSemester(Semester endSemester) {
 		this.endSemester = endSemester;
 	}
 
 }

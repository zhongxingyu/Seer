 package nl.bhit.mtor.model;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 @Entity
 @Table(
 		name = "PROJECT")
 public class Project extends BaseObject implements Serializable {
 	private static final long serialVersionUID = 5444456904374632294L;
 	private Long id;
 	private String name;
 	private Set<MTorMessage> messages;
 	private Company company;
 	private Set<User> users;
	private boolean monitoring;
 	public static final long INTERVAL = 5 * 60 * 1000; // 5 minutes in milliseconds
 
 	public Project() {
 		this.messages = new TreeSet<MTorMessage>();
 	}
 
 	public Project(String name) {
 		this.name = name;
 		this.messages = new TreeSet<MTorMessage>();
 	}
 
 	@ManyToOne(
 			fetch = FetchType.EAGER)
 	@JoinColumn(
 			name = "COMPANY_FK")
 	public Company getCompany() {
 		return company;
 	}
 
 	public void setCompany(Company company) {
 		this.company = company;
 	}
 
 	@Id
 	@GeneratedValue(
 			strategy = GenerationType.AUTO)
 	@Column(
 			name = "ID",
 			unique = true,
 			nullable = false)
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	@Column(
 			name = "NAME",
 			unique = true,
 			nullable = false)
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@OneToMany(
 			fetch = FetchType.EAGER,
 			cascade = CascadeType.ALL)
 	@JoinColumn(
 			name = "PROJECT_FK")
 	public Set<MTorMessage> getMessages() {
 		return messages;
 	}
 
 	public void setMessages(Set<MTorMessage> messages) {
 		this.messages = messages;
 	}
 
 	public void addMessage(MTorMessage message) {
 		messages.add(message);
 	}
 
 	public void removeMessage(String message) {
 		messages.remove(message);
 	}
 
 	/**
 	 * @return will return ERROR if there is an error, WARN if there is a warning else INFO
 	 */
 	public String statusOfProject() {
 		if (!hasHeartBeat()) {
 			return Status.ERROR.toString();
 		}
 		if (hasStatus(Status.ERROR)) {
 			return Status.ERROR.toString();
 		}
 		if (hasStatus(Status.WARN)) {
 			return Status.WARN.toString();
 		}
 		return Status.INFO.toString();
 	}
 
 	public boolean hasStatus(Status status) {
 		if (getMessages() == null) {
 			return false;
 		}
 		for (MTorMessage message : getMessages()) {
 			if (message.getStatus() == status && !message.isResolved()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean hasHeartBeat() {
 		boolean isAlive = false;
 		if (getMessages() != null) {
 			for (MTorMessage message : getMessages()) {
 				long timestamp = message.getTimestamp().getTime();
 				long currentTime = new Date().getTime();
 				long difference = currentTime - timestamp;
 				if (difference <= INTERVAL) {
 					isAlive = true;
 				}
 			}
 		}
 		return isAlive;
 	}
 
 	@ManyToMany(
 			fetch = FetchType.EAGER,
 			cascade = CascadeType.ALL)
 	@JoinTable(
 			name = "project_app_user",
 			joinColumns = { @JoinColumn(
 					name = "PROJECT_ID") },
 			inverseJoinColumns = { @JoinColumn(
 					name = "users_id") })
 	public Set<User> getUsers() {
 		return users;
 	}
 
 	public Set<String> userNames() {
 		Set<String> userNames = new HashSet<String>();
 		if (getUsers() != null) {
 			Set<User> userList = getUsers();
 			for (User user : userList) {
 				userNames.add(user.getFullName());
 			}
 		}
 		return userNames;
 	}
 
 	public void setUsers(Set<User> users) {
 		this.users = users;
 	}
 
 	public void addUser(User user) {
 		if (getUsers() != null) {
 			getUsers().add(user);
 		} else {
 			Set<User> setOfUsers = new HashSet<User>();
 			setOfUsers.add(user);
 			setUsers(setOfUsers);
 		}
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) return true;
 		if (obj == null) return false;
 		if (getClass() != obj.getClass()) return false;
 		Project other = (Project) obj;
 		if (id == null) {
 			if (other.id != null) return false;
 		} else if (!id.equals(other.id)) return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Project [id=" + id + ", name=" + name + ", company=" + company + ", users=" + users + "]";
 	}
 
 	@Column(
			name = "MONITORING",
 			nullable = false)
	public boolean isMonitoring() {
		return monitoring;
 	}
 
	public void setMonitoring(boolean monitoring) {
		this.monitoring = monitoring;
 	}
 }

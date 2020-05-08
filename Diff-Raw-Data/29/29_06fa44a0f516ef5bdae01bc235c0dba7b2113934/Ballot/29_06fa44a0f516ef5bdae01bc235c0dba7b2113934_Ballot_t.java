 package jacs.database.exceed.model;
 
 import java.io.Serializable;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;

 @Entity
 public class Ballot implements Serializable {
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	private int id;
 	private String criteria;
 	
 	@ManyToOne
 	@JoinColumn(name="user_id")
 	private User user;
 	
 	@ManyToOne
 	@JoinColumn(name="project_id")
 	private Project_eXceed project;
 	
 	public Ballot() {
 		super();
 	}
 	
 	public Ballot(String criteria)	{
 		this.criteria  = criteria;
 	}
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((criteria == null) ? 0 : criteria.hashCode());
 		result = prime * result + id;
 		result = prime * result + ((user == null) ? 0 : user.hashCode());
 		return result;
 	}
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof Ballot))
 			return false;
 		Ballot other = (Ballot) obj;
 		if (criteria == null) {
 			if (other.criteria != null)
 				return false;
 		} else if (!criteria.equals(other.criteria))
 			return false;
 		if (id != other.id)
 			return false;
 		if (user == null) {
 			if (other.user != null)
 				return false;
 		} else if (!user.equals(other.user))
 			return false;
 		return true;
 	}
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	public Project_eXceed getProject() {
 		return project;
 	}
 
 	public void setProject(Project_eXceed project) {
 		this.project = project;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	@Override
 	public String toString() {
 		return "Ballot [id=" + id
 				+ ", criteria=" + criteria +": Vote by"+ user.getUsername() + "]";
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public String getCriteria() {
 		return criteria;
 	}
 
 	public void setCriteria(String criteria) {
 		this.criteria = criteria;
 	}
 }

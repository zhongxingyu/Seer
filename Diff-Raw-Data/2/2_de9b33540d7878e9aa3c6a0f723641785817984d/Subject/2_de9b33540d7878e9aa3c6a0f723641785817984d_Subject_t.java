 package cz.cvut.fel.bupro.model;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.OneToMany;
 
 @Entity
 public class Subject extends BaseEntity {
 	private static final long serialVersionUID = -5477454797474559891L;
 
 	@Column(nullable = false, unique = true)
 	private String name;
 	@OneToMany(mappedBy = "subject")
 	private Set<Project> projects = new HashSet<Project>();
	@OneToMany(mappedBy = "subject", cascade = {CascadeType.ALL})
 	private Set<Enrolment> enrolments = new HashSet<Enrolment>();
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Set<Project> getProjects() {
 		return projects;
 	}
 
 	public void setProjects(Set<Project> projects) {
 		this.projects = projects;
 	}
 
 	public Set<Enrolment> getEnrolments() {
 		return enrolments;
 	}
 
 	public void setEnrolments(Set<Enrolment> enrolments) {
 		this.enrolments = enrolments;
 	}
 
 }

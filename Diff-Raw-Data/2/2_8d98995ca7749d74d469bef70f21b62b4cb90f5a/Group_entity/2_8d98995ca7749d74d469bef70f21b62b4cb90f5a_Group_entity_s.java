 package be.betty.gwtp.server.bdd;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 
 @Entity
 public class Group_entity implements Comparable<Group_entity> {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	@Column(name = "id")
 	private int id;
 	private String code;
 
 	@ManyToOne
 	private Project_entity project;
 	private String year;
 	private String section;
 	private String subGroup;
 
 	public Group_entity() {
 	}
 
 	public Group_entity(String year, String section, String subGroup, Project_entity current_project) {
 		this.code = year+section+subGroup;
 		this.year = year;
 		this.section = section;
 		this.setSubGroup(subGroup);
 		this.project = current_project;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getCode() {
 		return code;
 	}
 
 	public void setCode(String code) {
 		this.code = code;
 	}
 
 	public Project_entity getProject() {
 		return project;
 	}
 
 	public void setProject(Project_entity project) {
 		this.project = project;
 	}
 	
 	public String toString() {
 		return code;
 	}
 
 	public String getSubGroup() {
 		return subGroup;
 	}
 
 	public void setSubGroup(String subGroup) {
 		this.subGroup = subGroup;
 	}
 
 	@Override
 	public int compareTo(Group_entity g) {
		return this.getCode().compareToIgnoreCase(getCode());
 	}
 
 }

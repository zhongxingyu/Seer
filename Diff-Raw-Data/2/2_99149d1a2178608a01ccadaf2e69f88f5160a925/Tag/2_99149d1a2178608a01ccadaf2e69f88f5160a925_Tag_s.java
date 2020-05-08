 package cz.cvut.fel.bupro.model;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 
 import org.hibernate.validator.constraints.NotEmpty;
 
 @Entity
 public class Tag extends BaseEntity implements Serializable {
 	private static final long serialVersionUID = -3709930348866558631L;
 
 	@Column(unique = true, nullable = false)
 	@NotEmpty
 	private String name;
 
 	@ManyToMany(mappedBy = "tags")
 	private Set<Project> projects = new HashSet<Project>();
 
 	@ManyToOne(cascade = {CascadeType.MERGE})
 	private TagGroup group;
 
 	public Tag() {
 	}
 
 	public Tag(String name) {
 		this.name = name;
 	}
 
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
 
 	public TagGroup getGroup() {
 		return group;
 	}
 
 	public void setGroup(TagGroup group) {
 		this.group = group;
 	}
 
 	public int getRanking() {
 		return getProjects().size();
 	}
 
 	@Override
 	public String toString() {
 		return getName();
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		return prime + ((getName() == null) ? 0 : getName().hashCode());
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (!(obj instanceof Tag)) {
 			return false;
 		}
 		Tag other = (Tag) obj;
 		if (getName() == null) {
 			if (other.getName() != null) {
 				return false;
 			}
 		} else if (!getName().equals(other.getName())) {
 			return false;
 		}
 		return true;
 	}
 
 	public static void sortByRanking(List<Tag> tags) {
 		Collections.sort(tags, Collections.reverseOrder(new Comparator<Tag>() {
 			public int compare(Tag t1, Tag t2) {
				return Integer.compare(t1.getRanking(), t2.getRanking());
 			}
 		}));
 	}
 
 }

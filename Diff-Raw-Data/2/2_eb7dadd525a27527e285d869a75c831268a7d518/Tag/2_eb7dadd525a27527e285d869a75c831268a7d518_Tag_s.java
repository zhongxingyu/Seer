 package ch.helvetia.jax2011.entity;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 
 import org.hibernate.validator.constraints.NotEmpty;
 
 /**
  * A tag to categorize todo-items.
  */
 @Entity
 @NamedQueries(value = {
 		@NamedQuery(name = "findAllTags", query = "SELECT t FROM Tag t"),
		@NamedQuery(name = "countTags", query = "SELECT tg, count(tg) FROM Todo td LEFT JOIN td.tags tg WHERE td.dueDate > :callDate GROUP BY tg.name") })
 public class Tag implements Comparable<Tag> {
 
 	@GeneratedValue
 	@Id
 	private Long id;
 
 	@NotEmpty
 	private String name;
 
 	private String description;
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
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
 
 	@Override
 	public int compareTo(Tag o) {
 		return name.compareToIgnoreCase(o.name);
 	}
 
 }

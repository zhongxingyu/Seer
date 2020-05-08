 package entities.grid;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 @Entity
 @Table(name = "categories", uniqueConstraints = @UniqueConstraint(columnNames = { "name" }))
 @NamedQuery(name = "findAllCategories", query = "select c from Category c")
 public class Category {
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private int id;
 
 	@Column(unique=true, nullable=false)
 	String name;
 
 	public Category() {
 	}
 
 	public Category(String name) {
 		this.name = name;
 	}
 
 	public int getId() {
 		return id;
 	}
	
 	public String getName() {
 		return name;
 	}
	
	public void setName(String name) {
		this.name = name;
	}
 }

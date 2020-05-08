 package dbs.project.entity;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 @Entity
@javax.persistence.SequenceGenerator(
	    name="SEQ_STORE",
	    sequenceName="my_sequence"
	)
 public class Country {
 	@Id
 	@GeneratedValue
 	long id;
 
 	protected String name;
 
 	public Country() {
 	}
 
 	public Country(String name) {
 		setName(name);
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 }

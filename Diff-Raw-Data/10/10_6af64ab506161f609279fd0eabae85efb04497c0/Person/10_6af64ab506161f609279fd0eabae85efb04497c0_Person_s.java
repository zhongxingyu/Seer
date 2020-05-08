 package pl.agh.enrollme.model;
 
 import java.io.Serializable;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 
 
 @Entity
 public class Person implements Serializable {
 
 	private static final long serialVersionUID = -5777367229609230476L;
 
 	@Id
 	@GeneratedValue
 	private Integer id;
 	
 	private String firstName;
 	
 	private String lastName;
 
 	public Person(int id, String firstName, String lastName) {
 		this.id = id;
 		this.firstName = firstName;
 		this.lastName = lastName;
 	}
 
 	public String getFirstName() {
 		return firstName;
 	}
 
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	public String getLastName() {
 		return lastName;
 	}
 
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 	
 }

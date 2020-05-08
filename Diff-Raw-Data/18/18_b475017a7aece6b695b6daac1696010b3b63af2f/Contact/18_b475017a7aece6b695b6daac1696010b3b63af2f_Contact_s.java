 package br.com.jera.example.domain;
 
 import java.io.Serializable;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 
 import org.jboss.seam.annotations.Name;
 
 @Entity
 @Name("contact")
 public class Contact implements Serializable {
 
 	private static final long serialVersionUID = 7067862717942009603L;
 
 	@Id
 	@GeneratedValue
 	private Integer id;
 
 	private String lastName;
 
 	public Contact() {
 	}
 
 	public Contact(String lastName) {
 		this.lastName = lastName;
 	}
 
 	public String getLastName() {
 		return this.lastName;
 	}
 
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 }

 package org.svfc57.entities;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 import org.apache.tapestry5.beaneditor.NonVisual;
 import org.apache.tapestry5.beaneditor.Validate;
 
 @Entity(name="Person")
 public class Person {
 	
 	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@NonVisual
 	public long id;
 	
 	@Validate("required")
 	public String firstName;
 	
 	@Validate("required")
 	public String lastName;
 	
 	@Validate("required")
 	public String streetOne;
 	
 	public String streetTwo;
 	
 	@Validate("required")
 	public String city;
 	
 	@Validate("required")
 	public String state;
 	
 	@Validate("required,regexp")
 	public String zip;
 	
 	@Validate("required")
 	public String email;
 	
 	@Validate("required")
 	public String phone;
 	
 }

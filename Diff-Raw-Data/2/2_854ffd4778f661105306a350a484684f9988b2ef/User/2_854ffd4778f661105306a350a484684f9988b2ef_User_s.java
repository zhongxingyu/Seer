 package org.jboss.hibernateUniversity.criteria.domain;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 /**
  * @author Emmanuel Bernard
  */
 @Entity
 public class User {
 
 	public User(String firstName, String lastName, Date birthDate, Gender gender, int credits, String username, String password) {
 		this.firstName = firstName;
 		this.lastName = lastName;
 		this.birthDate = birthDate;
 		this.gender = gender;
 		this.credits = credits;
 		this.login = new Login(username, password);
 	}
 
 	@Id @GeneratedValue
 	public Long getId() { return id; }
 	public void setId(Long id) {  this.id = id; }
 	private Long id;
 
 	public String getFirstName() { return firstName; }
 	public void setFirstName(String firstName) {  this.firstName = firstName; }
 	private String firstName;
 
 	public String getLastName() { return lastName; }
 	public void setLastName(String lastName) {  this.lastName = lastName; }
 	private String lastName;
 
 	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = "owner")
 	public Set<Address> getAddresses() { return addresses; }
 	public void setAddresses(Set<Address> addresses) {  this.addresses = addresses; }
 	private Set<Address> addresses = new HashSet<Address>();
 
 	@Temporal(TemporalType.DATE)
 	public Date getBirthDate() { return birthDate; }
 	public void setBirthDate(Date birthDate) {  this.birthDate = birthDate; }
 	private Date birthDate;
 
 	@Enumerated(EnumType.STRING)
 	public Gender getGender() { return gender; }
 	public void setGender(Gender gender) {  this.gender = gender; }
 	private Gender gender;
 
 	public int getCredits() { return credits; }
 	public void setCredits(int credits) {  this.credits = credits; }
 	private int credits;
 
 	public Login getLogin() { return login; }
 	public void setLogin(Login login) {  this.login = login; }
 	private Login login;
 }

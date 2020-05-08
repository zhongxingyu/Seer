 package com.ebook;
 
 import javax.ejb.EJB;
 
import model.User;

 //import javax.persistence.EntityManager;
 //import javax.persistence.PersistenceContext;
 //import model.User;
 import com.ebook.service.RegistrationService;
 
 public class RegistrationBean {
 	
 	@EJB
 	private RegistrationService registrationService;
 	
 	private String firstname;
 	private String lastname;
 	private String address;
 	private String email;
 	private String password;
 	private String creditcard;
 	
 	
 	
 	//@PersistenceContext(unitName="ebookJSF")
 	//EntityManager em;
 	
 	public String getFirstname() {
 		return firstname;
 	}
 
 	public void setFirstname(String firstname) {
 		this.firstname = firstname;
 	}
 
 	public String getLastname() {
 		return lastname;
 	}
 
 	public void setLastname(String lastname) {
 		this.lastname = lastname;
 	}
 
 	public String getAddress() {
 		return address;
 	}
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getCreditcard() {
 		return creditcard;
 	}
 
 	public void setCreditcard(String creditcard) {
 		this.creditcard = creditcard;
 	}
 	
 	public String register(){
		User userEntity = new User();
		
		userEntity.setFirstname(firstname);
		userEntity.setLastname(lastname);
		userEntity.setAddress(address);
		userEntity.setEmail(email);
		userEntity.setPassword(password);
		userEntity.setCreditcard(creditcard);
		return registrationService.register(userEntity);
 	}
 /*
 	public String register(){
 		
 		em.getTransaction().begin();
 		
 		System.exit(0);
 		User userEntity = new User();
 		
 		userEntity.setFirstname(firstname);
 		userEntity.setLastname(lastname);
 		userEntity.setAddress(address);
 		userEntity.setEmail(email);
 		userEntity.setPassword(password);
 		userEntity.setCreditcard(creditcard);
 		
 		
 		userEntity.setFirstname("drew");
 		userEntity.setLastname("valentine");
 		userEntity.setAddress("address");
 		userEntity.setEmail("email");
 		userEntity.setPassword("password");
 		userEntity.setCreditcard("creditcard");
 				
 		em.persist(userEntity);
 		em.getTransaction().commit();
 		
 		return "Registered";
 		
 	}
 */
 
 }

 package com.ebook.service;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import model.User;
 
 @Stateless
 public class RegistrationService {
 	
 	@PersistenceContext(unitName="ebookJSF")
 	EntityManager em;
 
public String register(User userEntity){
 		
 		//em.getTransaction().begin();
 		
		//User userEntity = new User();
 		/*
 		userEntity.setFirstname(registrationbean.firstname);
 		userEntity.setLastname(lastname);
 		userEntity.setAddress(address);
 		userEntity.setEmail(email);
 		userEntity.setPassword(password);
 		userEntity.setCreditcard(creditcard);
 		*/
		/*
 		userEntity.setFirstname("drew");
 		userEntity.setLastname("valentine");
 		userEntity.setAddress("address");
 		userEntity.setEmail("email");
 		userEntity.setPassword("password");
 		userEntity.setCreditcard("creditcard");
		*/
	
 		em.persist(userEntity);
 		//em.getTransaction().commit();
 		
 		return userEntity.getFirstname().toString();
 		
 	}
 
 }

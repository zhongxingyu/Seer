 package com.fafnirx.biketicket.business.boundary;
 
 import com.fafnirx.biketicket.business.entity.Cyclist;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 /**
  *
  * @author mirko
  */
 @Stateless
 public class BikeTicketService {
     @PersistenceContext
     EntityManager em;
    
     public void createCyclist( String userName, String emailAdress){
         System.out.println("Username/emailadress=" + userName + "/" + emailAdress);
         em.persist(new Cyclist(userName,emailAdress));
         
     }
 }

 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mydoppelganger.dao;
 
 import com.mydoppelganger.model.Profile;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author Rob
  */
 @Repository
 @Transactional
 public class ProfileDAO implements ProfileDAOInterface {
 
     
     @PersistenceContext(unitName = "MyDoppPU")
     private EntityManager em;
 
     @Transactional
     @Override
     public void persist(Profile profile) {
        em.persist(profile);
        
     }
     
     @Transactional
     @Override
     public Profile findByUsername(String username) {
         Profile profile = null;
         try {
             profile = (Profile)em.createNamedQuery("Profile.findByUsername").setParameter("username", username).getSingleResult();
         } catch(NoResultException e) {
             
         }
         return profile;
     }
     
 }

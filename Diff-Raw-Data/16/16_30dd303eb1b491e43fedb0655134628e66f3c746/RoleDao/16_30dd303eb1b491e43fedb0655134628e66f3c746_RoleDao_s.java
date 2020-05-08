 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.artivisi.training.dao;
 
 import com.artivisi.training.domain.Role;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author endy
  */
 @Repository @Transactional
 public class RoleDao {
     @PersistenceContext private EntityManager entityManager;
     
     public Role cariById(Integer id){
         Role r = entityManager.find(Role.class, id);
         return r;
     }
 }

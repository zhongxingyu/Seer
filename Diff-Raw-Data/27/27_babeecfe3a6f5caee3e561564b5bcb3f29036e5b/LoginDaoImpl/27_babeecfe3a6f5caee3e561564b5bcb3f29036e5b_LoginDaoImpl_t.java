 package com.abada.cleia.dao.impl;
 
 /*
  * #%L
  * Cleia
  * %%
  * Copyright (C) 2013 Abada Servicios Desarrollo (investigacion@abadasoft.com)
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-3.0.html>.
  * #L%
  */
 import com.abada.cleia.entity.user.Group;
 import com.abada.cleia.entity.user.User;
 import javax.persistence.EntityManager;
 import com.abada.springframework.orm.jpa.support.JpaDaoUtils;
 import com.abada.springframework.security.authentication.dni.DniAuthenticationDao;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import org.apache.commons.lang.RandomStringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jbpm.task.identity.UserGroupCallback;
 import org.springframework.orm.jpa.EntityManagerFactoryInfo;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 
 /**
  *
  * @author katsu
  */
 public class LoginDaoImpl extends JpaDaoUtils implements UserGroupCallback, UserDetailsService, DniAuthenticationDao {
 
     private static final Log logger = LogFactory.getLog(LoginDaoImpl.class);
     private EntityManagerFactory entityManagerFactory;
 
     public void setEntityManagerFactoryBean(EntityManagerFactoryInfo entityManagerFactoryBean) throws Exception {
         entityManagerFactory = entityManagerFactoryBean.getNativeEntityManagerFactory();
     }
 
     public User getUserByDNI(String dni) {
         EntityManager em = entityManagerFactory.createEntityManager();
         try {
             EntityTransaction et = em.getTransaction();
             et.begin();
             User result = (User) em.createQuery("select id.user from Id id where id.value = :dni and id.type.value =  'DNI'").setParameter("dni", dni).getSingleResult();
             if (result != null) {
                 result.getGroups().size();
                result.getRoles().size();
             }
             et.commit();
             return result;
         } catch (RuntimeException e) {
             logger.warn(e.getMessage(), e);
         } finally {
             if (em.isOpen()) {
                 em.close();
             }
         }
         return null;
     }
 
     public User loadUserByUsername(String username) throws UsernameNotFoundException {
        if (logger.isDebugEnabled()) {
            logger.debug("Search for " + username + " username");
        }
        //FIX to jbpm task, with a random user Administrator
        if ("Administrator".equals(username)){            
            User result=new User();
            result.setPassword(RandomStringUtils.randomAlphanumeric(32));
            result.setUsername("Administrator");
            result.setGroups(new ArrayList<Group>());
            return result;
        }
        
         EntityManager em = entityManagerFactory.createEntityManager();
         try {
             EntityTransaction et = em.getTransaction();
             et.begin();
             User result = (User) em.createQuery("select u from User u where u.username=:username").setParameter("username", username).getSingleResult();
             if (result == null) {
                 throw new UsernameNotFoundException(username);
             }
             result.getGroups().size();
             result.getRoles().size();
             et.commit();
             return result;
        } catch (NoResultException e) {
            throw new UsernameNotFoundException(username);
         } finally {
             if (em.isOpen()) {
                 em.close();
             }
         }
     }
 
     public List<String> getGroupByUserName(String username) {
         User user = loadUserByUsername(username);
         if (user != null) {
             List<String> result = new ArrayList<String>();
             for (Group g : user.getGroups()) {
                 result.add(g.getValue());
             }
             return result;
         }
         return null;
     }
 
     public boolean existsUser(String userId) {
         try {
             if ("Administrator".equals(userId)) {
                 return true;//FIXIT
             }
             loadUserByUsername(userId);
             return true;
         } catch (UsernameNotFoundException e) {
             return false;
         }
     }
 
     public boolean existsGroup(String groupId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Search for " + groupId + " group");
        }
         EntityManager em = entityManagerFactory.createEntityManager();
         try {
             EntityTransaction et = em.getTransaction();
             et.begin();
             Group result = em.find(Group.class, groupId);
             et.commit();
             if (result != null) {
                 return true;
             }
         } finally {
             if (em.isOpen()) {
                 em.close();
             }
         }
         return false;
     }
 
     public List<String> getGroupsForUser(String userId, List<String> groupIds, List<String> allExistingGroupIds) {
         return getGroupByUserName(userId);
     }
 }

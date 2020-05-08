 package com.amee.service.environment;
 
 import com.amee.domain.AMEEStatus;
 import com.amee.domain.Pager;
 import com.amee.domain.UidGen;
 import com.amee.domain.auth.User;
 import com.amee.domain.environment.Environment;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.stereotype.Service;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 @Service
 public class SiteService implements Serializable {
 
     private static final String CACHE_REGION = "query.siteService";
 
     @PersistenceContext
     private EntityManager entityManager;
 
     // Users
 
     public User getUserByUid(Environment environment, String uid) {
         User user = null;
         if ((environment != null) && (uid != null)) {
             List<User> users = entityManager.createQuery(
                     "SELECT u FROM User u " +
                             "WHERE u.environment.id = :environmentId " +
                             "AND u.uid = :userUid " +
                             "AND u.status != :trash")
                     .setParameter("environmentId", environment.getId())
                     .setParameter("userUid", uid)
                     .setParameter("trash", AMEEStatus.TRASH)
                     .setHint("org.hibernate.cacheable", true)
                     .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                     .getResultList();
             if (users.size() > 0) {
                 user = users.get(0);
             }
         }
         return user;
     }
 
     public User getUserByUsername(Environment environment, String username) {
         User user = null;
         List<User> users = entityManager.createQuery(
                 "SELECT u FROM User u " +
                         "WHERE u.environment.id = :environmentId " +
                         "AND u.username = :username " +
                         "AND u.status != :trash")
                 .setParameter("environmentId", environment.getId())
                 .setParameter("username", username.trim())
                 .setParameter("trash", AMEEStatus.TRASH)
                 .setHint("org.hibernate.cacheable", true)
                 .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                 .getResultList();
         if (users.size() > 0) {
             user = users.get(0);
         }
         return user;
     }
 
     public List<User> getUsers(Environment environment, Pager pager) {
        return getUsers(environment, pager, null);
     }
 
     public List<User> getUsers(Environment environment, Pager pager, String search) {
         // If search is a list of UIDs then switch to UID search instead.
         Set<String> uids = new HashSet<String>();
         for (String uid : search.split(",")) {
             uid = uid.trim();
             if (UidGen.isValid(uid)) {
                 uids.add(uid);
             }
         }
         if (!uids.isEmpty()) {
             search = null;
         }
         // first count all objects
         String countHql = "SELECT count(u) " +
                 "FROM User u " +
                 "WHERE u.environment.id = :environmentId " +
                 (uids.isEmpty() ? "" : "AND u.uid IN (:uids) ") +
                 (StringUtils.isBlank(search) ? "" : "AND u.username LIKE :search ") +
                 "AND u.status != :trash";
         Query countQuery = entityManager.createQuery(countHql);
         countQuery.setParameter("environmentId", environment.getId());
         if (!uids.isEmpty()) {
             countQuery.setParameter("uids", uids);
         }
         if (!StringUtils.isBlank(search)) {
             countQuery.setParameter("search", "%" + search + "%");
         }
         countQuery.setParameter("trash", AMEEStatus.TRASH);
         countQuery.setHint("org.hibernate.cacheable", true);
         countQuery.setHint("org.hibernate.cacheRegion", CACHE_REGION);
         Long count = (Long) countQuery.getSingleResult();
         // tell pager how many objects there are and give it a chance to select the requested page again
         pager.setItems(count);
         pager.goRequestedPage();
         // now get the objects for the current page
         String hql = "SELECT u " +
                 "FROM User u " +
                 "WHERE u.environment.id = :environmentId " +
                 (uids.isEmpty() ? "" : "AND u.uid IN (:uids) ") +
                 (StringUtils.isBlank(search) ? "" : "AND u.username LIKE :search ") +
                 "AND u.status != :trash " +
                 "ORDER BY u.username";
         Query query = entityManager.createQuery(hql);
         query.setParameter("environmentId", environment.getId());
         if (!uids.isEmpty()) {
             query.setParameter("uids", uids);
         }
         if (!StringUtils.isBlank(search)) {
             query.setParameter("search", "%" + search + "%");
         }
         query.setParameter("trash", AMEEStatus.TRASH);
         query.setHint("org.hibernate.cacheable", true);
         query.setHint("org.hibernate.cacheRegion", CACHE_REGION);
         query.setMaxResults(pager.getItemsPerPage());
         query.setFirstResult((int) pager.getStart());
         List<User> users = query.getResultList();
         // update the pager
         pager.setItemsFound(users.size());
         // all done, return results
         return users;
     }
 
     public List<User> getUsers(Environment environment) {
         if (environment != null) {
             List<User> users = entityManager.createQuery(
                     "SELECT u " +
                             "FROM User u " +
                             "WHERE u.environment.id = :environmentId " +
                             "AND u.status != :trash " +
                             "ORDER BY u.username")
                     .setParameter("environmentId", environment.getId())
                     .setParameter("trash", AMEEStatus.TRASH)
                     .setHint("org.hibernate.cacheable", true)
                     .setHint("org.hibernate.cacheRegion", CACHE_REGION)
                     .getResultList();
             return users;
         } else {
             return null;
         }
     }
 
     public void save(User user) {
         entityManager.persist(user);
     }
 
     public void remove(User user) {
         user.setStatus(AMEEStatus.TRASH);
     }
 }

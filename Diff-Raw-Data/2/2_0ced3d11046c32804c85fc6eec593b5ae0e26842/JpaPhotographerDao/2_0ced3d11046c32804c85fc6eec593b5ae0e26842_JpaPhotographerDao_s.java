 package net.chrissearle.flickrvote.dao;
 
 import net.chrissearle.flickrvote.model.Photographer;
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Repository;
 
 import javax.persistence.*;
 
 @Repository
 
 public class JpaPhotographerDao implements PhotographerDao {
     private Logger log = Logger.getLogger(JpaPhotographerDao.class);
 
     @PersistenceContext(unitName = "FlickrVote")
     private EntityManager em;
 
     public Photographer findById(long id) {
         return em.find(Photographer.class, id);
     }
 
     public Photographer findByUsername(String username) {
         if (log.isDebugEnabled()) {
             log.debug("findByUsername : " + username);
         }
 
         Query query = em.createQuery("select p from Photographer p where p.username = :username");
         query.setParameter("username", username);
 
         try {
             return (Photographer) query.getSingleResult();
         } catch (NoResultException e) {
             if (log.isDebugEnabled()) {
                 log.debug("No matching user found");
             }
 
             // Just means that there is no photographer yet present
             return null;
         }
     }
 
     public Photographer findByToken(String token) {
         Query query = em.createQuery("select p from Photographer p where p.token = :token");
         query.setParameter("token", token);
 
         try {
             return (Photographer) query.getSingleResult();
         } catch (NoResultException e) {
             // Just means that there is no photographer yet validated with flickr
             return null;
         }
     }
 
     public void save(Photographer photographer) {
         Photographer p = findByUsername(photographer.getUsername());
 
         if (p != null) {
             p.setFullname(photographer.getFullname());
             p.setToken(photographer.getToken());
 
             if (photographer.getId() != null) {
                 p.setAdministrator(photographer.isAdministrator());
             }
 
             photographer = p;
         }
 
         em.persist(photographer);
     }
 
     public void delete(Photographer photographer) {
         em.remove(photographer);
     }
 
     public Photographer findByFlickrId(String id) {
        Query query = em.createQuery("select p from Photographer p where p.flickrId = :id");
         query.setParameter("id", id);
 
         try {
             return (Photographer) query.getSingleResult();
         } catch (NoResultException e) {
             // Just means that there is no photographer yet validated with flickr
             return null;
         }
     }
 }

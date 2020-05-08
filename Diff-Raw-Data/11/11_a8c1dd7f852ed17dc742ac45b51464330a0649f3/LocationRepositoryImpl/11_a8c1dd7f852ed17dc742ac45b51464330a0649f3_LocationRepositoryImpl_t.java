 /**
  * 
  */
 package org.nttext.util.jpa;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 import org.nttext.util.Location;
 import org.nttext.util.LocationRepository;
 
 /**
  * @author Neal Audenaert
  */
 public class LocationRepositoryImpl implements LocationRepository {
 
     
     private EntityManagerFactory m_emf = null;
     
     public LocationRepositoryImpl(EntityManagerFactory emf) {
         this.m_emf = emf; 
     }
    
     /**
      * 
      * @param name
      * @return
      */
     @SuppressWarnings({"unchecked"})
     public List<Location> find(String name) {
         List<Location> locations = null;
         EntityManager em = m_emf.createEntityManager();
         EntityTransaction tx = em.getTransaction();
         
         tx.begin();
         Session session = (Session) em.getDelegate();
         try {
             locations = (List<Location>)session.createCriteria(Location.class)
                                 .add(Restrictions.eq("name", name)).list();
            tx.commit();
         } finally {
             if (tx.isActive())
                 tx.rollback();
             em.close();
         }
         
         return (locations != null) ? locations : new ArrayList<Location>();
     }
 
     /**
      * 
      * @param name
      * @return
      */
     public Location findFirst(String name) {
         List<Location> locations = find(name);
         return ((locations != null) && (locations.size() > 0))
                     ? locations.get(0) 
                     : null;
     }
     
     /**
      * 
      * @param name
      * @return
      */
     @SuppressWarnings("unchecked")
     public Location findOrCreate(String name) {
         Location loc = null;
         List<Location> locations = null;
         EntityManager em = m_emf.createEntityManager();
         EntityTransaction tx = em.getTransaction();
         
         tx.begin();
         Session session = (Session) em.getDelegate();
         try {
             locations = (List<Location>)session.createCriteria(Location.class)
                                 .add(Restrictions.eq("name", name)).list();
             
             if ((locations != null) && locations.size() > 0)
                 loc = locations.get(0);
             
             if (loc == null) {
                 loc = new Location(name);
                 em.persist(loc);
             }
             
            tx.commit();
         } finally {
             if (tx.isActive())
                 tx.rollback();
             em.close();
         }
         
         return loc;
     }
     
 
     
 
 }

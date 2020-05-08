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
 import org.nttext.util.Institution;
 import org.nttext.util.InstitutionRepository;
 
 /**
  * @author Neal Audenaert
  */
 public class InstitutionRepositoryImpl implements InstitutionRepository {
     
     private EntityManagerFactory m_emf = null;
     
     public InstitutionRepositoryImpl(EntityManagerFactory emf) {
         this.m_emf = emf; 
     }
    
     /**
      * 
      * @param name
      * @return
      */
     @SuppressWarnings({"unchecked"})
     public List<Institution> find(String name) {
         List<Institution> institutions = null;
         EntityManager em = m_emf.createEntityManager();
         EntityTransaction tx = em.getTransaction();
         
         tx.begin();
         Session session = (Session) em.getDelegate();
         try {
             institutions = (List<Institution>)session.createCriteria(Institution.class)
                                 .add(Restrictions.eq("name", name)).list();
            tx.commit();
         } finally {
             if (tx.isActive())
                 tx.rollback();
             em.close();
         }
         
         return (institutions != null) ? institutions : new ArrayList<Institution>();
     }
 
     /**
      * 
      * @param name
      * @return
      */
     public Institution findFirst(String name) {
         List<Institution> institutions = find(name);
         return ((institutions != null) && (institutions.size() > 0))
                     ? institutions.get(0) 
                     : null;
     }
     
     /**
      * 
      * @param name
      * @return
      */
     @SuppressWarnings("unchecked")
     public Institution findOrCreate(String name) {
         Institution inst = null;
         List<Institution> institutions = null;
         EntityManager em = m_emf.createEntityManager();
         EntityTransaction tx = em.getTransaction();
         
         tx.begin();
         Session session = (Session) em.getDelegate();
         try {
             institutions = (List<Institution>)session.createCriteria(Institution.class)
                                 .add(Restrictions.eq("name", name)).list();
             
             if ((institutions != null) && institutions.size() > 0)
                 inst = institutions.get(0);
             
             if (inst == null) {
                 inst = new Institution(name);
                 em.persist(inst);
             }
             
            tx.commit();
         } finally {
             if (tx.isActive())
                 tx.rollback();
             em.close();
         }
         
         return inst;
     }
     
 }

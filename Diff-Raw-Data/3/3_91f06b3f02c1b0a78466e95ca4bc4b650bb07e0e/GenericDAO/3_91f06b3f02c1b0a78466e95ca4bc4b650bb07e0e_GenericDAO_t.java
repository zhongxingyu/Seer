 package pl.edu.agh.to1.dice.repository;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 import pl.edu.agh.to1.dice.repositoryInterfaces.IGenericDAO;
 
 import javax.persistence.Entity;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.PersistenceContext;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  * @author Michal Partyka
*/
 public class GenericDAO <T> implements IGenericDAO<T> {
 
     private static final Logger LOGGER = Logger.getLogger(GenericDAO.class.getName());
 
     @PersistenceContext
     private EntityManager em;
 
     @Autowired
     private EntityManagerFactory entityManagerFactory;
 
     private Class<T> type;
 
     public GenericDAO(Class<T> type) {
         if(type.getAnnotation(Entity.class) == null) {
             throw new IllegalArgumentException("DAO can be created only with class annotated by Entity. Class " +
                     type.getName() + " unfortunetly has no Entity annotation");
         }
         this.type = type;
     }
 
     @Transactional
     @Override
     public T update(T toUpdate) {
         LOGGER.info("Object provided for update " + toUpdate.toString());
         return em.merge(toUpdate);
     }
 
     @Transactional
     @Override
     public void add(T add) {
         if(em.find(type, entityManagerFactory.getPersistenceUnitUtil().getIdentifier(add)) != null) {
             em.merge(add);
             return;
         }
         em.persist(add);
     }
 
     @Override
     @Transactional
     public void remove(T remove) {
         T removeThis = em.find(type, entityManagerFactory.getPersistenceUnitUtil().getIdentifier(remove));
         if(removeThis != null) {
             em.remove(removeThis);
         } else {
             throw new IllegalStateException("There is no entity you want to remove in database");
         }
     }
 
     //TODO: must be transactional ?? ~ partyks
     @Transactional
     @Override
     public List<T> getList() {
         CriteriaQuery<T> c = em.getCriteriaBuilder().createQuery(type);
         Root<T> from = c.from(type);
         c.select(from);
         return em.createQuery(c).getResultList();
     }
 
     @Transactional
     @Override
     public T getByPK(Object PK) {
         return em.find(type, PK);
     }
 
     @Transactional
     @Override
     public void removeByPK(Object PK) {
         T removeThis = em.find(type, PK);
         if (removeThis != null) {
             em.remove(removeThis);
         }
     }
 
     protected EntityManager getEntityManager() {
         return em;
     }
 
     public Class<T> getType() {
         return type;
     }
 
     public void setType(Class<T> type) {
         this.type = type;
     }
 
 }

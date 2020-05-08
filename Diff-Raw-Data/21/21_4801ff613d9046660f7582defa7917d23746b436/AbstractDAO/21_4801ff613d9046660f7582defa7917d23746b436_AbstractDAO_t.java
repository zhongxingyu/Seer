 package com.nurelm.genericabstracts.DAO;
 
 import com.nurelm.assessormapping.struct.AccessorStruct;
 import com.nurelm.genericabstracts.Entity.AbstractEntity;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @param <T>
  * @author sms
  */
 @Transactional
 public abstract class AbstractDAO<T extends AbstractEntity> implements AbstractDAOIface<T> {
 
   protected ArrayList<AccessorStruct> accessors;
   //protected AccessorProcessor entityProcessor;
   private Class<T> entityClass;
 
   /**
    *
    * @return
    */
   public Class<T> getEntityClass() {
     return entityClass;
   }
 
   /**
    *
    * @param entityClass
    */
   public AbstractDAO(Class<T> entityClass) {
     this.entityClass = entityClass;
     this.em = em;
   }
 
   /**
    *
    * @return
    */
   public Object getParentDAO() {
     try {
       return this.entityClass.getConstructor().newInstance().getParent();
     } catch (Exception ex) {
       Logger.getLogger(AbstractDAO.class.getName()).log(Level.SEVERE, null, ex);
     }
     return null;
   }
   /**
    *
    */
   protected EntityManager em;
 
   public abstract void postConstruct();
 
   public abstract Collection<AccessorStruct> getAccessors();
 
   public abstract Map<String, AccessorStruct> getAccessorMap();
 
   public void setEntityManager(EntityManager em) {
     this.em = em;
   }
 
   /**
    *
    * @return
    */
   public EntityManager getEntityManager() {
     return this.em;
   }
 
   /**
    *
    * @param entity
    */
   public void create(T entity) {
     getEntityManager().persist(entity);
     getEntityManager().flush();
     getEntityManager().refresh(entity);
   }
 
   /**
    * @TODO REVISE
    *
    * @param entity
    * @return
    */
   public T getCreate(T entity) {
     T localentity = null;
     try {
       localentity = entityClass.newInstance();
     } catch (InstantiationException ex) {
       Logger.getLogger(AbstractDAO.class.getName()).log(Level.SEVERE, null, ex);
     } catch (IllegalAccessException ex) {
       Logger.getLogger(AbstractDAO.class.getName()).log(Level.SEVERE, null, ex);
     }
     getEntityManager().persist(localentity);
     getEntityManager().flush();
     T persistedEntity = this.getEntityManager().find(entityClass, localentity.getId());
     entity.setId(localentity.getId());
     getEntityManager().merge(entity);
     return entity;
   }
 
   /**
    *
    * @param entity
    */
   public void edit(T entity) {
     getEntityManager().merge(entity);
     getEntityManager().flush();
   }
 
   /**
    *
    * @param entity
    */
   public void remove(T entity) {
     T localEntity = getEntityManager().merge(entity);
     getEntityManager().remove(localEntity);
    //getEntityManager().detach(entity);
   }
 
   /**
    *
    */
   public void clear() {
     getEntityManager().clear();
   }
 
   /**
    *
    */
   public void flush() {
     getEntityManager().flush();
   }
 
   /**
    *
    * @param id
    * @return
    */
   public T find(Object id) {
     getEntityManager().flush();
     return getEntityManager().find(entityClass, id);
   }
 
   /**
    *
    * @return
    */
   public Collection<Object> findAll() {
     Query query = em.createQuery("SELECT c FROM " + entityClass.getSimpleName() + " c");
     return query.getResultList();
   }
 
   /**
    *
    * @return
    */
   protected Collection<T> findAllInternal() {
     final Query query = em.createQuery("SELECT c FROM " + entityClass.getSimpleName() + " c");
     Collection<T> results = new ArrayList<T>() {
 
       {
 	addAll(query.getResultList());
       }
     };
     return results;
   }
 
   protected Collection<T> findAllInternal(EntityManager localEM) {
     final Query query = localEM.createQuery("SELECT c FROM " + entityClass.getSimpleName() + " c");
     Collection<T> results = new ArrayList<T>(query.getResultList());
     return results;
   }
 
   /**
    *
    * @param range
    * @return
    */
   public List<T> findRange(int[] range) {
     javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
     cq.select(cq.from(entityClass));
     javax.persistence.Query q = getEntityManager().createQuery(cq);
     q.setMaxResults(range[1] - range[0]);
     q.setFirstResult(range[0]);
     return q.getResultList();
   }
 
   /**
    *
    * @return
    */
   public int count() {
     javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
     javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
     cq.select(getEntityManager().getCriteriaBuilder().count(rt));
     javax.persistence.Query q = getEntityManager().createQuery(cq);
     return ((Long) q.getSingleResult()).intValue();
   }
 
   protected T getSingleResult(Query query) {
     List<T> result = query.setMaxResults(1).getResultList();
     if (result != null) {
       if (result.size() > 0) {
 	return result.get(0);
       }
       return null;
     } else {
       return null;
     }
   }
   
   protected Object getSingleResultObject(Query query) {
     List<T> result = query.setMaxResults(1).getResultList();
     if (result != null) {
       if (result.size() > 0) {
 	return result.get(0);
       }
       return null;
     } else {
       return null;
     }
   }
 }

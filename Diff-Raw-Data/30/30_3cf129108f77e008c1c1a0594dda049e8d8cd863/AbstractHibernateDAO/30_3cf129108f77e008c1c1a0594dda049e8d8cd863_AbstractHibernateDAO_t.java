 package com.worthsoln.repository;
 
 import com.worthsoln.patientview.model.BaseModel;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import java.lang.reflect.ParameterizedType;
 import java.util.List;
 
 /**
  *  Abstract DAO class class with support for the common generic operations
  */
 public class AbstractHibernateDAO<T extends BaseModel> {
 
     private Class<T> clazz
             = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
 
     @PersistenceContext
     private EntityManager entityManager;
 
     public T get(final Long id) {
         return entityManager.find(clazz, id);
     }
 
     public List<T> getAll() {
         return entityManager.createQuery("from " + clazz.getName()).getResultList();
     }
 
     public void save(final T entity) {
 
         if (!entity.hasValidId()) {
 
             // apply any baseModel standards
             entityManager.persist(entity);
         } else {
             // apply any baseModel standards
             entityManager.merge(entity);
         }
     }
 
     public void delete(final T entity) {
        entityManager.remove(entity);
     }
 
     public void delete(final Long entityId) {
         final T entity = get(entityId);
         delete(entity);
     }
 
     protected <T> void buildWhereClause(CriteriaQuery<T> criteria, List<Predicate> wherePredicates) {
         if (!wherePredicates.isEmpty()) {
             Predicate[] predicates = new Predicate[wherePredicates.size()];
 
             for (int x = 0; x < wherePredicates.size(); x++) {
                 predicates[x] = wherePredicates.get(x);
             }
 
             criteria.where(entityManager.getCriteriaBuilder().and(predicates));
         }
     }
 
     protected EntityManager getEntityManager() {
         return entityManager;
     }
 }

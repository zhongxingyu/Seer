 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.muni.fi.pa165.dao.commons;
 
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author Aubrey Oosthuizen Abstract class used for implementing CRUD operations on generic types. Used to decouple
  * data access and persistence from business layer. Will be extended by each DAO object class for every entity type
  */
 
 public abstract class GenericJpaDao<T, ID> implements GenericDao<T, ID> {
 
     private Class<T> persistentClass;
     private EntityManager entityManager;
 
     public GenericJpaDao(Class<T> persistenceClass) {
         this.persistentClass = persistenceClass;
     }
 
     protected EntityManager getEntityManager() {
         return entityManager;
     }
 
    @PersistenceContext
     public void setEntityManager(EntityManager entityManager) {
         this.entityManager = entityManager;
     }
 
     public Class<T> getPersistentClass() {
         return persistentClass;
     }
 
     @Override
     @Transactional
     public T save(T entity) {
 
         //not necessary - entityManager.persist(null) throws the same exception
 //        if (entity == null) {
 //            throw new IllegalArgumentException("entity to be created is null");
 //        }
 
         entityManager.persist(entity);       
         return entity;
     }
 
     @Override
     public T update(T entity) {
         T mergedEntity = getEntityManager().merge(entity);
         return mergedEntity;
     }
 
     @Override
     @Transactional
     public void delete(T entity) {
      entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
     }
     
     @Override   
     @Transactional
     public void delete(Long id) {
     
         T entity =  entityManager.find(getPersistentClass(), id);
         entityManager.merge(entity);
        entityManager.remove(entity);
    }
     
        //  entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
    
 
     @Override
     public T findById(ID id) {
         T entity = (T) getEntityManager().find(getPersistentClass(), id);
         return entity;
     }
 
     @Override
     public List<T> findAll() {
         return getEntityManager().createQuery("select e from  " + getPersistentClass().getSimpleName() + " e").getResultList();
     }
 
     @Override
     public void flush() {
         getEntityManager().flush();
     }
 }

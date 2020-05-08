 // Copyright 2013 Structure Eng Inc.
 
 package com.structureeng.persistence.dao.impl;
 
 import com.structureeng.persistence.dao.ActiveDAO;
 import com.structureeng.persistence.dao.DAOErrorCode;
 import com.structureeng.persistence.dao.DAOException;
 import com.structureeng.persistence.model.AbstractActiveModel;
 import com.structureeng.persistence.model.AbstractActiveModel_;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.persistence.PersistenceException;
 
 /**
  * Provides the implementation for {@code ActiveDAO} interface.
  *
  * @author Edgar Rico (edgar.martinez.rico@gmail.com)
  * @param <T> specifies the {@code AbstractActiveModel} of the data access object
  * @param <S> specifies the {@code Serializable} identifier of the {@code AbstractActiveModel}
  */
 public abstract class ActiveDAOImpl<T extends AbstractActiveModel, S extends Serializable> extends
         DAOImpl<T, S> implements ActiveDAO<T, S> {
 
     public ActiveDAOImpl(Class<T> entityClass, Class<S> entityIdClass) {
         super(entityClass, entityIdClass);
     }
 
     @Override
     public void persist(T entity) throws DAOException {
         try {
             validatePersistUniqueKeys(entity);
             super.persist(entity);
         } catch (PersistenceException persistenceException) {
             throw DAOException.Builder.build(DAOErrorCode.PERSISTENCE, persistenceException);
         }
     }
 
     @Override
     public T merge(T entity) throws DAOException {
         try {
             validateMergeUniqueKeys(entity);
             return super.merge(entity);
         } catch (PersistenceException persistenceException) {
             throw DAOException.Builder.build(DAOErrorCode.PERSISTENCE, persistenceException);
         }
     }
 
     @Override
     public void remove(T entity) throws DAOException {
         validateForeignKeys(entity);
         try {
             if (!hasVersionChanged(entity)) {
                 entity.setActive(Boolean.FALSE);
                 merge(entity);
             } else {
                 throw DAOException.Builder.build(DAOErrorCode.OPRIMISTIC_LOCKING);
             }
         } catch (PersistenceException persistenceException) {
             throw DAOException.Builder.build(DAOErrorCode.PERSISTENCE, persistenceException);
         }
     }
 
     @Override
     public T find(S id) {
         T entity = super.find(id);
         if (entity.getActive()) {
             return entity;
         }
         return null;
     }
 
     @Override
     public T find(S id, long version) throws DAOException {
         T entity = super.find(id, version);
         if (entity.getActive()) {
             return entity;
         }
         return null;
     }
 
     /**
      * Count the number of instances in the persistent storage that are active.
      * 
      * @return the number of active entities.
      */
     @Override
     public long count() {
         return countEntities(0, Boolean.TRUE);
     }
     
     @Override
     public long countInactive() {
         return countEntities(0, Boolean.FALSE);
     }
     
     protected Long countEntities(int option, Object value) {
         QueryContainer<Long, T> qc = new QueryContainer<Long, T>(Long.class, getEntityClass());
         qc.getCriteriaQuery().select(qc.getCriteriaBuilder().count(qc.getRoot()));
         countEntities(qc, option, value);        
         return qc.getSingleResult();
     }
     
    protected void countEntities(QueryContainer<Long, T> qc, int option, Object value) {
         switch (option) {
             case 0:
                 qc.getCriteriaQuery().where(qc.getCriteriaBuilder()
                         .equal(qc.getRoot().get(AbstractActiveModel_.active), value));
                 break;
                 
             default:
                 break;
         }
     }
 
     @Override
     public List<T> findRange(int start, int end) {        
         QueryContainer<T, T> qc = newQueryContainer(getEntityClass());
         qc.getCriteriaQuery().where(qc.getCriteriaBuilder()
                         .equal(qc.getRoot().get(AbstractActiveModel_.active), Boolean.TRUE));        
         return qc.getResultList(start, end - start);
     }
 
     /**
      * Validates the unique keys before an entity will be inserted in the persistence storage.
      *
      * @param entity the entity that should be validated.
      * @throws DAOException this exception will be thrown if the validation process failed.
      */
     protected abstract void validatePersistUniqueKeys(final T entity) throws DAOException;
 
     /**
      * Validates the unique keys before an entity will be updated from the persistence storage.
      *
      * @param entity the entity that should be validated.
      * @throws DAOException this exception will be thrown if the validation process failed.
      */
     protected abstract void validateMergeUniqueKeys(final T entity) throws DAOException;
 
     /**
      * Validates the foreign keys before an entity will be deleted from the persistence storage.
      *
      * @param entity the entity that should be validated.
      * @throws DAOException this exception will be thrown if the validation process failed.
      */
     protected abstract void validateForeignKeys(T entity) throws DAOException;
 }

 // Copyright 2013 Structure Eng Inc.

 package com.structureeng.persistence.dao.impl;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import com.structureeng.persistence.dao.ActiveDAO;
import com.structureeng.persistence.dao.DAOErrorCode;
 import com.structureeng.persistence.dao.DAOException;
 import com.structureeng.persistence.dao.QueryContainer;
 import com.structureeng.persistence.model.AbstractActiveModel;
 import com.structureeng.persistence.model.AbstractActiveModel_;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import java.io.Serializable;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.LockModeType;
 import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
 
 /**
  * Provides the implementation for {@code ActiveDAO} interface.
  *
  * @author Edgar Rico (edgar.martinez.rico@gmail.com)
  * @param <T> specifies the {@code AbstractActiveModel} of the data access object
  * @param <S> specifies the {@code Serializable} identifier of the {@code AbstractActiveModel}
  */
 public final class ActiveDAOImpl<T extends AbstractActiveModel, S extends Serializable> implements
         ActiveDAO<T, S> {
 
     private EntityManager entityManager;
     private final Class<T> entityClass;
     private final Class<S> entityIdClass;
     private final DAOImpl<T, S> dao;
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     @Inject
     public ActiveDAOImpl(Class<T> entityClass, Class<S> entityIdClass) {
         this.entityClass = checkNotNull(entityClass);
         this.entityIdClass = checkNotNull(entityIdClass);
         this.dao = new DAOImpl<T, S>(entityClass, entityIdClass);
     }
 
     @Override
     public void persist(T entity) throws DAOException {
         dao.persist(entity);
     }
 
     @Override
     public T merge(T entity) throws DAOException {
         return dao.merge(entity);
     }
 
     @Override
     public final void remove(T entity) throws DAOException {
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
     public void refresh(T entity) {
         dao.refresh(entity);
     }
 
     @Override
     public void refresh(T entity, LockModeType modeType) {
         dao.refresh(entity, modeType);
     }
 
     @Override
     public boolean hasVersionChanged(T entity) throws DAOException {
         return dao.hasVersionChanged(entity);
     }
 
     @Override
     public T find(S id) {
         T entity = dao.find(id);
         if (entity.getActive()) {
             return entity;
         }
         return null;
     }
 
     @Override
     public T find(S id, long version) throws DAOException {
         T entity = dao.find(id, version);
         if (entity.getActive()) {
             return entity;
         }
         return null;
     }
 
     @Override
     public void flush() {
         dao.flush();
     }
 
     /**
      * Count the number of instances in the persistent storage that are active.
      *
      * @return the number of active entities.
      */
     @Override
     public long count() {
         return countEntities(Boolean.TRUE);
     }
 
     @Override
     public long countInactive() {
         return countEntities(Boolean.FALSE);
     }
 
     private long countEntities(Boolean value) {
         QueryContainer<Long, T> qc = QueryContainer.newQueryContainerCount(getEntityManager(),
                 getEntityClass());
         qc.getCriteriaQuery().where(qc.getCriteriaBuilder()
                 .equal(qc.getRoot().get(AbstractActiveModel_.active), value));
         return qc.getSingleResult();
     }
 
     @Override
     public List<T> findRange(int start, int end) {
         QueryContainer<T, T> qc = QueryContainer.newQueryContainer(getEntityManager(),
                 getEntityClass());
         qc.getCriteriaQuery().where(qc.activeEntities(qc.getRoot()));
         return qc.getResultList(start, end - start);
     }
 
     /**
      * Provides the class of this dao.
      *
      * @return - the class of the dao
      */
     public Class<T> getEntityClass() {
         return entityClass;
     }
 
     /**
      * Provides the class of the Id.
      *
      * @return - the class of the Id of an entity.
      */
     public Class<S> getEntityIdClass() {
         return entityIdClass;
     }
 
     @PersistenceContext
     public void setEntityManager(EntityManager entityManager) {
         this.entityManager = checkNotNull(entityManager);
         this.dao.setEntityManager(entityManager);
     }
 
     /**
      * Provides the {@code EntityManager} that is being use by the dao.
      *
      * @return - the instance
      */
     public EntityManager getEntityManager() {
         return entityManager;
     }
 
     /**
      * Provides the {@code Logger} of the concrete class.
      *
      * @return - the logger instance of the class.
      */
     public Logger getLogger() {
         return logger;
     }
 }

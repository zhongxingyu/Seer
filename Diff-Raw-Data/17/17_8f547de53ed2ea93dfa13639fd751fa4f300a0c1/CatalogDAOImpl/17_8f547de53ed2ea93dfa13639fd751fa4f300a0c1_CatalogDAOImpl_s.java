 // Copyright 2013 Structure Eng Inc.
 
 package com.structureeng.persistence.dao.impl;
 
 import com.structureeng.persistence.dao.CatalogDAO;
 import com.structureeng.persistence.dao.DAOErrorCode;
 import com.structureeng.persistence.dao.DAOException;
 import com.structureeng.persistence.model.AbstractActiveModel_;
 import com.structureeng.persistence.model.AbstractCatalogModel;
 import com.structureeng.persistence.model.AbstractCatalogModel_;
 
 import com.google.common.base.Preconditions;
 import java.io.Serializable;
 
 import javax.persistence.NoResultException;
 
 /**
  * Provides the implementation for {@code CatalogDAO} interface.
  *
  * @author Edgar Rico (edgar.martinez.rico@gmail.com)
  * @param <T> specifies the {@code AbstractActiveModel} of the data access object
  * @param <S> specifies the {@code Serializable} identifier of the {@code AbstractActiveModel}
  * @param <RID> specifies the {@code Number} identifier of the {@code AbstractCatalogModel}
  */
 public abstract class CatalogDAOImpl<T extends AbstractCatalogModel, S extends Serializable,
         RID extends Number> extends ActiveDAOImpl<T, S> implements CatalogDAO<T, S, RID> {
 
     private final Class<RID> referenceIdClass;
 
     public CatalogDAOImpl(Class<T> entityClass, Class<S> entityIdClass,
             Class<RID> referenceIdClass) {
         super(entityClass, entityIdClass);
         this.referenceIdClass = Preconditions.checkNotNull(referenceIdClass);
     }
 
     @Override
     public T findByReferenceId(RID referenceId) {
         return findByReferenceId(referenceId, Boolean.TRUE);
     }
 
     @Override
     public T findInactiveByReferenceId(RID referenceId) {
         return findByReferenceId(referenceId, Boolean.FALSE);
     }
 
     @Override
     protected void validatePersistUniqueKeys(final T entity) throws DAOException {
         long uniqueId = countEntitiesWithReferenceId(
                 referenceIdClass.cast(entity.getReferenceId()));
         long uniqueName = countEntitiesWithName(entity.getName());
         if (uniqueId > 0 || uniqueName > 0) {
             DAOException daoException = null;
             if (uniqueId > 0) {
                 daoException = DAOException.Builder.build(getErrorCodeUniqueReferenceId());
             }
             if (uniqueName > 0) {
                 daoException = DAOException.Builder.build(getErrorCodeUniqueName(),
                         daoException);
             }
             throw daoException;
         }
     }
 
     @Override
     protected void validateMergeUniqueKeys(final T entity) throws DAOException {
         long count = countEntitiesWithSameNameButDiffReferenceId(entity);
         if (count > 0) {
             throw DAOException.Builder.build(getErrorCodeUniqueName());
         }
     }
 
     private T findByReferenceId(RID referenceId, Boolean active) {
         try {
             QueryContainer<T, T> qc = newQueryContainer(getEntityClass());
             qc.getCriteriaQuery().where(qc.getCriteriaBuilder()
                     .and(
                     qc.getCriteriaBuilder()
                         .equal(qc.getRoot().get(AbstractActiveModel_.active), active),
                     qc.getCriteriaBuilder()
                         .equal(qc.getRoot().get(AbstractCatalogModel_.referenceId), referenceId)));
             return qc.getSingleResult();
         } catch (NoResultException exc) {
             if (getLogger().isDebugEnabled()) {
                 getLogger().debug(String.format(exc.getMessage().concat(" referenceId [%s]."),
                         referenceId), exc);
             }
             return null;
         }
     }
 
     private Long countEntitiesWithReferenceId(RID referenceId) {
         return countEntities(1, referenceId);
     }
 
     private Long countEntitiesWithName(String name) {
         return countEntities(2, name);
     }
     
     private Long countEntitiesWithSameNameButDiffReferenceId(T entity) {
         return countEntities(3, entity);
     }
     
     @Override
    protected void countEntities(QueryContainer qc, int option, Object value) {
         switch (option) {            
             case 1:
                 qc.getCriteriaQuery().where(qc.getCriteriaBuilder().equal(
                         qc.getRoot().get(AbstractCatalogModel_.referenceId), value));
                 break;
 
             case 2:
                 qc.getCriteriaQuery().where(qc.getCriteriaBuilder().equal(
                         qc.getRoot().get(AbstractCatalogModel_.name), value));
                 break;
                 
             case 3:
                 T entity = getEntityClass().cast(value);
                 qc.getCriteriaQuery().where(
                         qc.getCriteriaBuilder().and(
                             qc.getCriteriaBuilder().equal(qc.getRoot()
                                 .get(AbstractCatalogModel_.name), entity.getName()),
                             qc.getCriteriaBuilder().notEqual(qc.getRoot()
                                 .get(AbstractCatalogModel_.referenceId), entity.getReferenceId())));
                 break;
 
             default:
                 super.countEntities(qc, option, value);
                 break;
         }
     }
 
     public DAOErrorCode getErrorCodeUniqueReferenceId() {
         return DAOErrorCode.CATALOG_REFERENCE_ID_UK;
     }
 
     public DAOErrorCode getErrorCodeUniqueName() {
         return DAOErrorCode.CATALOG_NAME_UK;
     }
 }

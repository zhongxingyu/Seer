 // Copyright 2013 Structure Eng Inc.
 
 package com.structureeng.persistence.dao.impl.product;
 
 import com.structureeng.persistence.dao.DAOException;
 import com.structureeng.persistence.dao.ProductDefinitionDAO;
 import com.structureeng.persistence.dao.impl.CatalogDAOImpl;
 import com.structureeng.persistence.model.product.Product;
 import com.structureeng.persistence.model.product.ProductDefinition;
 import com.structureeng.persistence.model.product.ProductDefinition_;
 import com.structureeng.persistence.model.product.Product_;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import java.math.BigInteger;
 
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.criteria.JoinType;
 
 /**
  * Data Access Object for {@code ProductDefinition}.
  *
  * @author Edgar Rico (edgar.martinez.rico@gmail.com)
  */
 public class ProductDefinitionDAOImpl extends CatalogDAOImpl<ProductDefinition, Long, BigInteger>
     implements ProductDefinitionDAO {
 
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     @PersistenceContext
     private EntityManager entityManager;
 
     @Inject
     public ProductDefinitionDAOImpl() {
         super(ProductDefinition.class, Long.class, BigInteger.class);
     }
 
     @Override
     protected void validatePersistUniqueKeys(ProductDefinition entity) throws DAOException {
         DAOException daoException = null;
         try {
             super.validatePersistUniqueKeys(entity);
         } catch (DAOException exc) {
             daoException = exc;
         }
         Long count = countEntitiesWithKey(entity);
         if (count > 0) {
             daoException = DAOException.Builder.build(ProductErrorCode.PRODUCT_DEFFINITION_KEY_UK,
                     daoException);
         }
         if (daoException != null) {
             throw daoException;
         }
     }
 
     @Override
     protected void validateMergeUniqueKeys(ProductDefinition entity) throws DAOException {
         DAOException daoException = null;
         try {
             super.validateMergeUniqueKeys(entity);
         } catch (DAOException exc) {
             daoException = exc;
         }
         Long count = countEntitiesWithSameKeyButDiffReferenceId(entity);
         if (count > 0) {
             daoException = DAOException.Builder.build(ProductErrorCode.PRODUCT_DEFFINITION_KEY_UK,
                     daoException);
         }
         if (daoException != null) {
             throw daoException;
         }
     }
 
     @Override
     protected void validateForeignKeys(ProductDefinition productDefinition) throws DAOException {
         if (productDefinition.getActive()) {
             long count = countProductWithProductDefinition(productDefinition);
             if (count > 0) {
                 throw DAOException.Builder.build(ProductErrorCode.PRODUCT_DEFFINITION_FK);
             }
         }
     }
 
     private Long countEntitiesWithKey(ProductDefinition productDefinition) {
         return countEntities(4, productDefinition.getKey());
     }
 
     private Long countEntitiesWithSameKeyButDiffReferenceId(ProductDefinition entity) {
         return countEntities(5, entity);
     }
 
     private long countProductWithProductDefinition(final ProductDefinition entity) {
         QueryContainer<Long, Product> qc =
                 new QueryContainer<Long, Product>(Long.class, Product.class);
         qc.getCriteriaQuery().select(qc.getCriteriaBuilder().countDistinct(qc.getRoot()));
         qc.getRoot().join(Product_.productDefinition, JoinType.INNER);
         qc.getCriteriaQuery().where(qc.getCriteriaBuilder().and(
                 qc.getCriteriaBuilder()
                     .equal(qc.getRoot().get(Product_.productDefinition), entity),
                 qc.getCriteriaBuilder()
                     .equal(qc.getRoot().get(ProductDefinition_.active), Boolean.TRUE)));
         return qc.getSingleResult();
     }
 
     @Override
    protected void countEntities(QueryContainer qc, int option, Object value) {
         switch (option) {
             case 4:
                 qc.getCriteriaQuery().where(qc.getCriteriaBuilder().equal(
                         qc.getRoot().get(ProductDefinition_.key), value));
                 break;
 
             case 5:
                 ProductDefinition entity = getEntityClass().cast(value);
                 qc.getCriteriaQuery().where(
                         qc.getCriteriaBuilder().and(
                             qc.getCriteriaBuilder().equal(qc.getRoot()
                                 .get(ProductDefinition_.key), entity.getName()),
                             qc.getCriteriaBuilder().notEqual(qc.getRoot()
                                 .get(ProductDefinition_.referenceId), entity.getReferenceId())));
                 break;
 
             default:
                 super.countEntities(qc, option, value);
                 break;
         }
     }
 
     @Override
     protected EntityManager getEntityManager() {
         return entityManager;
     }
 
     @Override
     public Logger getLogger() {
         return logger;
     }
 }

 // Copyright 2013 Structure Eng Inc.
 
 package com.structureeng.persistence.dao.impl;
 
 import com.structureeng.common.exception.ErrorCode;
 import com.structureeng.persistence.dao.CatalogDAO;
 import com.structureeng.persistence.dao.DAOException;
 import com.structureeng.persistence.model.AbstractCatalogModel;
 import com.structureeng.persistence.model.AbstractCatalogTenantModel;
 import com.structureeng.persistence.model.Model;
 import com.structureeng.persistence.model.history.AbstractHistoryTenantModel;
 
 import org.junit.Assert;
 import org.junit.FixMethodOrder;
 import org.junit.Test;
 import org.junit.runners.MethodSorters;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 
 import java.io.Serializable;
 import java.util.List;
 
import javax.persistence.LockModeType;
 import javax.persistence.PersistenceException;
 
 /**
  * Test for {@code CatalogDAO}.
  *
  * @author Edgar Rico (edgar.martinez.rico@gmail.com)
  */
 @FixMethodOrder(MethodSorters.NAME_ASCENDING)
 @TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
 public abstract class AbstractCatalogDAOImplTest<S extends Serializable, RID extends Number, 
         T extends AbstractCatalogTenantModel<S, RID>, H extends AbstractHistoryTenantModel> 
             extends AbstractTenantModelDAOImplTest<T, H> {
     
     private static AbstractCatalogModel tempCatalogModel;
     private static AbstractCatalogModel versionCatalogModel;
     private final Class<T> clase;
     
     public AbstractCatalogDAOImplTest(Class<T> clase) {
         this.clase = clase;        
     }
     
     @Test
     public void testPersistA() throws DAOException {
         T catalogModel = build(toReferenceId("50"), "test 50");
         getCatalogDAO().merge(clase.cast(catalogModel));
     }
 
     @Test
     public void testPersistB() throws DAOException {
         tempCatalogModel = build(toReferenceId("55"), "test 55");
         getCatalogDAO().persist(clase.cast(tempCatalogModel));
     }
 
     @Test
     public void testPersistCount() throws DAOException {
         Long count = getCatalogDAO().count();
         Assert.assertTrue(count >= 2);
     }
     
     @Test(expected = DAOException.class)
     public void testPersistMerge() throws DAOException {
         tempCatalogModel.setReferenceId(toReferenceId("50"));
         try {
             getCatalogDAO().merge(clase.cast(tempCatalogModel));
             Assert.fail("Should have thrown a DAOException.");
         } catch (DAOException daoException) {
             Assert.assertNotNull(daoException.getErrorCode());
         }
         tempCatalogModel.setReferenceId(toReferenceId("55"));
         tempCatalogModel.setName("test 50");
         try {
             getCatalogDAO().merge(clase.cast(tempCatalogModel));
             Assert.fail("Should have thrown a DAOException.");
         } catch (DAOException daoException) {
             Assert.assertNotNull(daoException.getErrorCode());
             deleteHistory(tempCatalogModel);
             throw daoException;
         }
     }
 
     @Test(expected = PersistenceException.class)
     public void testPersistMergeDirty() throws DAOException {
         T catalogModel = build(toReferenceId("51"), "test 51");
         getCatalogDAO().persist(catalogModel);
         getCatalogDAO().flush();
         catalogModel.setReferenceId(toReferenceId("50"));
         try {
             getCatalogDAO().merge(catalogModel);
             Assert.fail("Should have thrown a DAOException.");
         } catch (PersistenceException daoException) {            
             commit = false;
             throw daoException;
         }
     }
     
     @Test(expected = DAOException.class)
     public void testPersistUK() throws DAOException {
         T catalogModel = build(toReferenceId("50"), "test 50");
         try {
             getCatalogDAO().persist(catalogModel);
             Assert.fail("Should have thrown a DAOException.");
         } catch (DAOException daoException) {
             Assert.assertNotNull(daoException.getErrorCode());
             throw daoException;
         }
     }
 
     @Test(expected = DAOException.class)
     public void testPersistUKName() throws DAOException {
         T catalogModel = build(toReferenceId("99"), "test 50");
         try {
             getCatalogDAO().persist(catalogModel);
             Assert.fail("Should have thrown a DAOException.");
         } catch (DAOException daoException) {
             Assert.assertNotNull(daoException.getErrorCode());
             throw daoException;
         }
     }
 
     @Test(expected = DAOException.class)
     public void testPersistUKReferenceId() throws DAOException {
         T catalogModel = build(toReferenceId("50"), "test 99");
         try {
             getCatalogDAO().persist(catalogModel);
             Assert.fail("Should have thrown a DAOException.");
         } catch (DAOException daoException) {
             ErrorCode errorCode = daoException.getErrorCode();
             Assert.assertNotNull(errorCode);
             throw daoException;
         }
     }
 
     @Test
     public void testRange() {
         List<T> catalogModels = getCatalogDAO().findRange(0, 2);
         Assert.assertNotNull(catalogModels);
         Assert.assertTrue(catalogModels.size() >= 1);
     }
    
    @Test
    public void testRefresh() {
        T catalogModel = getCatalogDAO().findByReferenceId(toReferenceId("50"));
        getCatalogDAO().refresh(catalogModel);
        getCatalogDAO().refresh(catalogModel, LockModeType.READ);
    }
 
     @Test
     public void testUpdate() throws DAOException {
         T catalogModel = getCatalogDAO().findByReferenceId(toReferenceId("50"));
         catalogModel.setReferenceId(toReferenceId("51"));
         catalogModel.setName("test 51");
         tempCatalogModel = getCatalogDAO().merge(catalogModel);
         versionCatalogModel = getCatalogDAO().find((S)tempCatalogModel.getId());
     }
 
     @Test
     public void testUpdateFind() throws DAOException {
         T catalogModel = getCatalogDAO().findByReferenceId(toReferenceId("51"));
         Model tmp = getCatalogDAO().find(catalogModel.getId());
         Assert.assertEquals(catalogModel, tmp);
         tmp = getCatalogDAO().find(catalogModel.getId(), catalogModel.getVersion());
         Assert.assertEquals(catalogModel, tmp);
     }
 
     @Test
     public void testUpdateMerge() throws DAOException {
         tempCatalogModel.setReferenceId(toReferenceId("52"));
         tempCatalogModel.setName("test 52");
         tempCatalogModel = getCatalogDAO().merge(clase.cast(tempCatalogModel));        
     }
 
     @Test
     public void testUpdateMergeHasVersion() throws DAOException {        
         getCatalogDAO().hasVersionChanged(clase.cast(tempCatalogModel));
     }
     
     @Test(expected = DAOException.class)
     public void testUpdateMergeVersion() throws DAOException {
         versionCatalogModel.setReferenceId(toReferenceId("54"));
         versionCatalogModel.setName("test 54");
         try {
             getCatalogDAO().merge(clase.cast(versionCatalogModel));
             Assert.fail("Should have thrown a DAOException.");
         } catch (DAOException exc) {
             Assert.assertNotNull(exc.getErrorCode());
             throw exc;
         }
     }
 
     @Test
     public void testUpdateRemove() throws DAOException {
         T catalogModel = getCatalogDAO().findByReferenceId(toReferenceId("52"));
         getCatalogDAO().remove(catalogModel);
     }
    
    @Test(expected = DAOException.class)
    public void testUpdateRemoveVersion() throws DAOException {       
        try {
            getCatalogDAO().remove(clase.cast(versionCatalogModel));
            Assert.fail("Should have thrown a DAOException.");
        } catch (DAOException exc) {
            Assert.assertNotNull(exc.getErrorCode());
            throw exc;
        }
    }
 
     @Test
     public void testUpdateRemoveCount() {
         Long count = getCatalogDAO().countInactive();
         Assert.assertTrue(count >= 1);
     }
 
     @Test
     public void testUpdateRemoveFind() throws DAOException {
         T catalogModel = getCatalogDAO().findInactiveByReferenceId(toReferenceId("52"));
         T tmp = getCatalogDAO().find(catalogModel.getId());
         Assert.assertNull(tmp);
         tmp = getCatalogDAO().findByReferenceId(catalogModel.getReferenceId());
         Assert.assertNull(tmp);
         tmp = getCatalogDAO().find(catalogModel.getId(), catalogModel.getVersion());
         Assert.assertNull(tmp);
     }
 
     @Test
     public void testValidateRemove() {
         T catalogModel = getCatalogDAO().findInactiveByReferenceId(toReferenceId("52"));
         deleteHistory(catalogModel);
     }
     
     public abstract void deleteHistory(Model model);
     
     public abstract T build(RID referenceId, String name);
     
     public abstract RID toReferenceId(String referenceId);
     
     public abstract CatalogDAO<T,S, RID> getCatalogDAO();
 }

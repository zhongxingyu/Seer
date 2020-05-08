 package gov.nih.nci.cagrid.portal.service;
 
 import gov.nih.nci.cagrid.portal.DBIntegrationTestBase;
 import gov.nih.nci.cagrid.portal.dao.QueryInstanceDao;
 import gov.nih.nci.cagrid.portal.domain.dataservice.CQLQueryInstance;
 import gov.nih.nci.cagrid.portal.domain.dataservice.DCQLQueryInstance;
 import gov.nih.nci.cagrid.portal.domain.dataservice.QueryInstance;
 
 /**
  * User: kherm
  *
  * @author kherm manav.kher@semanticbits.com
  */
 public class QueryInstanceServiceTest extends DBIntegrationTestBase {
 
 
     private QueryInstanceDao queryInstanceDao;
     private QueryInstanceService queryInstanceService;
 
 
     public void testClean() {
         QueryInstance inst = new CQLQueryInstance();
         getQueryInstanceDao().save(inst);
         QueryInstance dcqlInst = new DCQLQueryInstance();
         getQueryInstanceDao().save(dcqlInst);
 
         assertEquals(2, getQueryInstanceDao().getAll().size());
         queryInstanceService.delete(inst.getId());
 
         assertEquals(1, getQueryInstanceDao().getAll().size());
         queryInstanceService.delete(dcqlInst.getId());
         assertEquals(0, getQueryInstanceDao().getAll().size());
 
     }
 
     @Override
     protected String[] getConfigLocations() {
         return new String[]{
                 "classpath:applicationContext-db.xml",
                 "classpath:applicationContext-service.xml"
         };
     }
 
 
     public QueryInstanceDao getQueryInstanceDao() {
         return queryInstanceDao;
     }
 
     public void setQueryInstanceDao(QueryInstanceDao queryInstanceDao) {
         this.queryInstanceDao = queryInstanceDao;
     }
 
     public QueryInstanceService getQueryInstanceService() {
         return queryInstanceService;
     }
 
     public void setQueryInstanceService(QueryInstanceService queryInstanceService) {
         this.queryInstanceService = queryInstanceService;
     }
 }

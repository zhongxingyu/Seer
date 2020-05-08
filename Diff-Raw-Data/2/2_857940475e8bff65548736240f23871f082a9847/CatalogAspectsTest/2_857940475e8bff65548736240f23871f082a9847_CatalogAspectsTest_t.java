 package gov.nih.nci.cagrid.portal.catalog.aspects;
 
 import gov.nih.nci.cagrid.portal.DBIntegrationTestBase;
 import gov.nih.nci.cagrid.portal.util.PortalAggrIntegrationTestBase;
 import gov.nih.nci.cagrid.portal.dao.GridServiceDao;
 import gov.nih.nci.cagrid.portal.dao.ParticipantDao;
 import gov.nih.nci.cagrid.portal.dao.PortalUserDao;
 import gov.nih.nci.cagrid.portal.dao.catalog.GridServiceEndPointCatalogEntryDao;
 import gov.nih.nci.cagrid.portal.dao.catalog.InstitutionCatalogEntryDao;
 import gov.nih.nci.cagrid.portal.dao.catalog.PersonCatalogEntryDao;
 import gov.nih.nci.cagrid.portal.dao.catalog.CatalogEntryDao;
 import gov.nih.nci.cagrid.portal.domain.Participant;
 import gov.nih.nci.cagrid.portal.domain.PortalUser;
 import gov.nih.nci.cagrid.portal.domain.catalog.CatalogEntry;
 import gov.nih.nci.cagrid.portal.domain.catalog.InstitutionCatalogEntry;
 
 /**
  * User: kherm
  *
  * @author kherm manav.kher@semanticbits.com
  */
 public class CatalogAspectsTest extends PortalAggrIntegrationTestBase {
 
     GridServiceEndPointCatalogEntryDao gridServiceEndPointCatalogEntryDao;
     GridServiceDao gridServiceDao;
     InstitutionCatalogEntryDao institutionCatalogEntryDao;
     ParticipantDao participantDao;
     PortalUserDao portalUserDao;
     PersonCatalogEntryDao personCatalogEntryDao;
     CatalogEntryDao catalogEntryDao;
 
       @Override
     protected String[] getConfigLocations() {
         return new String[]{
                 "classpath*:applicationContext-aggr-catalog-aspects.xml",
                 "applicationContext-service.xml",
                 "applicationContext-db.xml"
         };
     }
 
     /**
      * Not valid any more. GridService CE's are being created by ServiceMetadataCatalogEntryBuilderAspect
      */
 //    public void testGSAspect() {
 //        int initCount = gridServiceEndPointCatalogEntryDao.getAll().size();
 //
 //        GridService service = new GridService();
 //
 //        gridServiceDao.save(service);
 //
 //        GridService loadedService = gridServiceDao.getById(1);
 //        // make sure catalog item is created for service
 //        assertNotNull(loadedService.getCatalog());
 ////        assertEquals(gridServiceEndPointCatalogEntryDao.getAll().size() - initCount, 1);
 ////
 ////        for (GridServiceEndPointCatalogEntry entry : gridServiceEndPointCatalogEntryDao.getAll())
 ////            assertNotNull(entry.getAbout());
 //
 //    }
     public void testInstAspect() {
         int initCount = institutionCatalogEntryDao.getAll().size();
 
         Participant p = new Participant();
 
         participantDao.save(p);
 
         Participant loadedP = participantDao.getById(1);
 
         // make sure catalog item is created for service
         assertNotNull(loadedP.getCatalog());
         assertEquals(institutionCatalogEntryDao.getAll().size() - initCount, 1);
 
         for (InstitutionCatalogEntry entry : institutionCatalogEntryDao.getAll())
             assertNotNull(entry.getAbout());
 
     }
 
 
     public void testPersonCatalog() {
         int initCount = personCatalogEntryDao.getAll().size();
 
         PortalUser p = new PortalUser();
 
         portalUserDao.save(p);
 
        PortalUser loadedP = portalUserDao.getById(p.getId());
 
         // make sure catalog item is created for service
         assertNotNull(loadedP.getCatalog());
         assertEquals(personCatalogEntryDao.getAll().size() - initCount, 1);
 
 
 //        for (PersonCatalogEntry entry : personCatalogEntryDao.getAll())
 //            assertNotNull(entry.getAbout());
 
 
     }
 
 
     @Override
     protected void onTearDown() throws Exception {
         for (CatalogEntry catalog : catalogEntryDao.getAll()) {
             assertNotNull("Timestamp is null", catalog.getUpdatedAt());
         }
     }
 
     public CatalogEntryDao getCatalogEntryDao() {
         return catalogEntryDao;
     }
 
     public void setCatalogEntryDao(CatalogEntryDao catalogEntryDao) {
         this.catalogEntryDao = catalogEntryDao;
     }
 
     public PortalUserDao getPortalUserDao() {
         return portalUserDao;
     }
 
     public void setPortalUserDao(PortalUserDao portalUserDao) {
         this.portalUserDao = portalUserDao;
     }
 
     public PersonCatalogEntryDao getPersonCatalogEntryDao() {
         return personCatalogEntryDao;
     }
 
     public void setPersonCatalogEntryDao(PersonCatalogEntryDao personCatalogEntryDao) {
         this.personCatalogEntryDao = personCatalogEntryDao;
     }
 
     public InstitutionCatalogEntryDao getInstitutionCatalogEntryDao() {
         return institutionCatalogEntryDao;
     }
 
     public void setInstitutionCatalogEntryDao(InstitutionCatalogEntryDao institutionCatalogEntryDao) {
         this.institutionCatalogEntryDao = institutionCatalogEntryDao;
     }
 
     public ParticipantDao getParticipantDao() {
         return participantDao;
     }
 
     public void setParticipantDao(ParticipantDao participantDao) {
         this.participantDao = participantDao;
     }
 
     public GridServiceEndPointCatalogEntryDao getGridServiceEndPointCatalogEntryDao() {
         return gridServiceEndPointCatalogEntryDao;
     }
 
     public void setGridServiceEndPointCatalogEntryDao(GridServiceEndPointCatalogEntryDao gridServiceEndPointCatalogEntryDao) {
         this.gridServiceEndPointCatalogEntryDao = gridServiceEndPointCatalogEntryDao;
     }
 
     public GridServiceDao getGridServiceDao() {
         return gridServiceDao;
     }
 
     public void setGridServiceDao(GridServiceDao gridServiceDao) {
         this.gridServiceDao = gridServiceDao;
     }
 }

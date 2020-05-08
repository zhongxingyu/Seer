 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRoleUse;
 import edu.northwestern.bioinformatics.studycalendar.xml.writers.AmendmentApprovalXmlSerializer;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 
 import java.util.Calendar;
 
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
 import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
 import static org.easymock.EasyMock.expect;
 
 /**
  * @author Saurabh Agrawal
  */
 public class AmendmentApprovalsResourceTest extends AuthorizedResourceTestCase<AmendmentApprovalsResource> {
     private static final String STUDY_IDENTIFIER = "EC golf";
     private static final String STUDY_IDENTIFIER_ENCODED = "EC+golf";
     private static final String SITE_IDENTIFIER = "AgU";
 
     private Study study;
     private Site site;
 
     private StudyDao studyDao;
     private SiteDao siteDao;
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
         study = createBasicTemplate();
         study.setAssignedIdentifier(STUDY_IDENTIFIER);
         site = createNamedInstance(SITE_IDENTIFIER, Site.class);
 
         studyDao = registerDaoMockFor(StudyDao.class);
         siteDao = registerDaoMockFor(SiteDao.class);
 
         Amendment amendment = new Amendment();
         amendment.setMandatory(true);
         amendment.setName("Amendment 1");
         amendment.setDate(createDate(2008, Calendar.JANUARY, 2));
         study = createNamedInstance("Cancer Study", Study.class);
         site = createNamedInstance("Northwestern University", Site.class);
         study.setAmendment(amendment);
         Fixtures.createStudySite(study, site);
 
         request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), STUDY_IDENTIFIER_ENCODED);
         request.getAttributes().put(UriTemplateParameters.SITE_IDENTIFIER.attributeName(), SITE_IDENTIFIER);
 
         xmlSerializer = registerMockFor(AmendmentApprovalXmlSerializer.class);
     }
 
     @Override
     @SuppressWarnings({"unchecked"})
     protected AmendmentApprovalsResource createAuthorizedResource() {
         AmendmentApprovalsResource resource = new AmendmentApprovalsResource();
         resource.setStudyDao(studyDao);
         resource.setSiteDao(siteDao);
         resource.setXmlSerializer(xmlSerializer);
         return resource;
     }
 
     @SuppressWarnings({"unchecked"})
     public void testGetXml() throws Exception {
 
         expectResolvedStudyAndSite(study, site);
         expect(xmlSerializer.createDocumentString(study.getStudySite(site).getAmendmentApprovals())).andReturn(MOCK_XML);
 
         doGet();
         assertResponseStatus(Status.SUCCESS_OK);
         assertResponseIsCreatedXml();
     }
 
 
     public void testGet404sOnMissingStudy() throws Exception {
         expectResolvedStudyAndSite(null, site);
         doGet();
         assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
     }
 
     public void testGet404sOnMissingSite() throws Exception {
         expectResolvedStudyAndSite(study, null);
         doGet();
         assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
     }
 
     public void testGet404sOnMissingStudySite() throws Exception {
         study.getStudySites().clear();
         site.getStudySites().clear();
         expectResolvedStudyAndSite(study, site);
         doGet();
         assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
     }
 
     ////// POST
 
     public void testPost404sOnMissingStudySite() throws Exception {
         study.getStudySites().clear();
         site.getStudySites().clear();
         expectResolvedStudyAndSite(study, site);
 
         doPost();
 
         assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
     }
 
     public void testGetWithAuthorizedRoles() {
         assertRolesAllowedForMethod(Method.GET,
                 PscRoleUse.SITE_PARTICIPATION.roles());
     }
 
     public void testPostWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.POST,STUDY_QA_MANAGER);
     }
 
     ////// Helper Methods
 
     private void expectResolvedStudyAndSite(Study expectedStudy, Site expectedSite) {
         expect(studyDao.getByAssignedIdentifier(STUDY_IDENTIFIER)).andReturn(expectedStudy);
         expect(siteDao.getByAssignedIdentifier(SITE_IDENTIFIER)).andReturn(expectedSite);
         ((AmendmentApprovalXmlSerializer) xmlSerializer).setStudy(expectedStudy);
     }
 }

 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 
 import static edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings.createSuiteRoleMembership;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSite;
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.createPscUser;
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
 import static org.easymock.EasyMock.expect;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author Saurabh Agrawal
  */
 public class SitesResourceTest extends AuthorizedResourceTestCase<SitesResource> {
     private SiteDao siteDao;
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
         siteDao = registerDaoMockFor(SiteDao.class);
     }
 
     @Override
     protected SitesResource createAuthorizedResource() {
         SitesResource resource = new SitesResource();
         resource.setSiteDao(siteDao);
         resource.setXmlSerializer(xmlSerializer);
         return resource;
     }
 
     public void testGetAllowed() throws Exception {
         assertAllowedMethods("GET");
     }
 
     public void testGetWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.GET, PscRole.valuesWithSiteScoped());
     }
     
     public void testGetXmlForSites() throws Exception {
         Site site1 = createSite("Site1", "IL01");
         Site site2 = createSite("Site2", "IL02");
         List<Site> sites = Arrays.asList(site1, site2);
         setCurrentUser(createPscUser("jo", createSuiteRoleMembership(DATA_READER).forAllSites()));
         expect(siteDao.getVisibleSites(getCurrentUser().getVisibleSiteParameters())).andReturn(sites);
         expect(xmlSerializer.createDocumentString(sites)).andReturn(MOCK_XML);
 
         doGet();
         assertResponseStatus(Status.SUCCESS_OK);
         assertResponseIsCreatedXml();
     }
 
 }

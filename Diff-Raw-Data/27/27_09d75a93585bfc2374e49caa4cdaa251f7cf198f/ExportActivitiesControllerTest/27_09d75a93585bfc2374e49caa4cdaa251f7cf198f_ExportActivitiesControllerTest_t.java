 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
 import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
 import edu.northwestern.bioinformatics.studycalendar.xml.writers.ActivitySourceXmlSerializer;
 import edu.northwestern.bioinformatics.studycalendar.xml.writers.SourceSerializer;
 import edu.northwestern.bioinformatics.studycalendar.domain.Source;
 import java.util.Collection;
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;
 import static org.easymock.EasyMock.expect;
 
 
 public class ExportActivitiesControllerTest extends ControllerTestCase {
 
     private SourceDao sourceDao;
     private ActivitySourceXmlSerializer activitySourceXmlSerializer;
     private SourceSerializer sourceSerializer;
     private ExportActivitiesController controller;
     private Source source;
    private static final Integer SOURCE_IDENT = 11;
 
     private String headerForCSV ="Name, Type, Code, Description, Source, ";
     private String headerForXLS ="Name\tType\tCode\tDescription\tSource";
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
 
         controller = new ExportActivitiesController();
 
         activitySourceXmlSerializer = registerMockFor(ActivitySourceXmlSerializer.class);
         sourceSerializer = registerMockFor(SourceSerializer.class);
         sourceDao = registerDaoMockFor(SourceDao.class);
 
         controller.setSourceDao(sourceDao);
         controller.setActivitySourceXmlSerializer(activitySourceXmlSerializer);
         controller.setSourceSerializer(sourceSerializer);
 
         source = setId(11, createNamedInstance("Test Source", Source.class));
     }
 
     public void testAuthorizedRoles() {
         Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
         assertRolesAllowed(actualAuthorizations, BUSINESS_ADMINISTRATOR);
     }
 
     public void testTreatIdentAsSourcedFirst() throws Exception {
         request.setPathInfo(String.format("/template/display/%s.xml", source.getId().toString()));
        expect(sourceDao.getByName(SOURCE_IDENT.toString())).andReturn(source);
         expect(activitySourceXmlSerializer.createDocumentString(source)).andReturn("<source/>");
         handle(200);
     }
 
     public void testTreatIdentForCsvAsSourcedFirst() throws Exception {
         request.setPathInfo(String.format("/template/display/%s.csv", source.getId().toString()));
        expect(sourceDao.getByName(SOURCE_IDENT.toString())).andReturn(source);
         expect(sourceSerializer.createDocumentString(source, ',')).andReturn(headerForCSV);
         handle(200);
     }
     
     public void testTreatIdentForXlsAsSourcedFirst() throws Exception {
         request.setPathInfo(String.format("/template/display/%s.xls", source.getId().toString()));
        expect(sourceDao.getByName(SOURCE_IDENT.toString())).andReturn(source);
         expect(sourceSerializer.createDocumentString(source, '\t')).andReturn(headerForXLS);
         handle(200);
     }
 
     public void testHttp400ForMissingIdentifier() throws Exception {
         request.setPathInfo("/etc/etc/.xml");
         handle(400);
     }
 
     public void testHttp400ForNonMatchingUri() throws Exception {
         request.setPathInfo("/etc/etc/11.doc");
        expect(sourceDao.getByName(SOURCE_IDENT.toString())).andReturn(source);
         handle(400);
     }
 
 
     private void handle(int expectedResponseStatus) throws Exception {
         replayMocks();
         assertNull("No model and view should ever be returned", controller.handleRequest(request, response));
         assertEquals("Wrong response status", expectedResponseStatus, response.getStatus());
         verifyMocks();
     }
 }

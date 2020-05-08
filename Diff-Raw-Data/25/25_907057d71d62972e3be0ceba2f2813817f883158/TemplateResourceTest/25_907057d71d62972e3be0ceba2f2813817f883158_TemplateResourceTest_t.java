 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
 import static edu.northwestern.bioinformatics.studycalendar.restlets.UriTemplateParameters.STUDY_IDENTIFIER;
 import edu.northwestern.bioinformatics.studycalendar.service.ImportTemplateService;
import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
 import edu.nwu.bioinformatics.commons.DateUtils;
 import static org.easymock.classextension.EasyMock.expect;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.resource.InputRepresentation;
 
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * @author Rhett Sutphin
  */
 public class TemplateResourceTest extends ResourceTestCase<TemplateResource> {
     private static final String STUDY_IDENT = "elf";
 
     private StudyDao studyDao;
     private Study study;
     private ImportTemplateService importTemplateService;
     private StudyXmlSerializer studyXmlSerializer;
     private Date lastModifiedDate = DateUtils.createDate(2007, Calendar.OCTOBER, 5);
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
 
         studyDao = registerDaoMockFor(StudyDao.class);
         importTemplateService = registerMockFor(ImportTemplateService.class);
         studyXmlSerializer = registerMockFor(StudyXmlSerializer.class);
 
         request.getAttributes().put(STUDY_IDENTIFIER.attributeName(), STUDY_IDENT);
         study = Fixtures.setGridId("44", Fixtures.setId(44, Fixtures.createSingleEpochStudy(STUDY_IDENT, "Treatment")));
         study.setDevelopmentAmendment(new Amendment());
         Delta<Epoch> delta = Delta.createDeltaFor(new Epoch(), PropertyChange.create("name", "A", "B"));
         study.getDevelopmentAmendment().addDelta(delta);
         delta.getChanges().get(0).setUpdatedDate(lastModifiedDate);
         Fixtures.assignIds(study);
 
         expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andStubReturn(study);
     }
 
     @Override
     protected TemplateResource createResource() {
         TemplateResource templateResource = new TemplateResource();
         templateResource.setStudyDao(studyDao);
         templateResource.setXmlSerializer(studyXmlSerializer);
         templateResource.setImportTemplateService(importTemplateService);
         return templateResource;
     }
 
     public void testGetAndPutAllowed() throws Exception {
         replayMocks();
         assertAllowedMethods("GET", "PUT");
     }
 
     public void testGetReturnsXml() throws Exception {
         expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(study);
         expectObjectXmlized(study);
 
         doGet();
 
         assertEquals("Result not success", 200, response.getStatus().getCode());
         assertResponseIsCreatedXml();
 
         assertEquals("modification date incorrect", lastModifiedDate, response.getEntity().getModificationDate());
     }
 
     public void testGetByGridIdReturnsXml() throws Exception {
         expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(null);
         expect(studyDao.getByGridId(STUDY_IDENT)).andReturn(study);
         expectObjectXmlized(study);
 
         doGet();
 
         assertEquals("Result not success", 200, response.getStatus().getCode());
         assertResponseIsCreatedXml();
 
         assertEquals("modification date incorrect", lastModifiedDate, response.getEntity().getModificationDate());
     }
 
     public void testGet404sWhenStudyNotFound() throws Exception {
         expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(null);
         expect(studyDao.getByGridId(STUDY_IDENT)).andReturn(null);
 
         doGet();
 
         assertEquals("Result should be not found", 404, response.getStatus().getCode());
     }
 
     public void testPutExistingXml() throws Exception {
         expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(study);
        InputStream in = expectPutEntity();
        expect(importTemplateService.readAndSaveTemplate(study, in)).andReturn(study);
         expect(studyXmlSerializer.createDocumentString(study)).andReturn(MOCK_XML);
 
         doPut();
 
         assertEquals("Result not success", 200, response.getStatus().getCode());
         assertResponseIsCreatedXml();
     }
 
     public void testPutNewXml() throws Exception {
         expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(null);
         expect(studyDao.getByGridId(STUDY_IDENT)).andReturn(null);
 
        InputStream in = expectPutEntity();
        expect(importTemplateService.readAndSaveTemplate(null, in)).andReturn(study);
         expect(studyXmlSerializer.createDocumentString(study)).andReturn(MOCK_XML);
 
         doPut();
 
         assertResponseStatus(Status.SUCCESS_CREATED);
         assertResponseIsCreatedXml();
     }
 
     public void testEntityIsInDownloadModeWithDownloadParam() throws Exception {
         expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(study);
         expectObjectXmlized(study);
 
         request.getResourceRef().setQuery("download");
         doGet();
 
         assertEquals("Result not success", 200, response.getStatus().getCode());
         assertTrue("Should be downloadable", response.getEntity().isDownloadable());
         assertEquals("Suggested filename should match the assigned ident", STUDY_IDENT + ".xml", response.getEntity().getDownloadName());
     }
 
    protected InputStream expectPutEntity() throws Exception {
         final InputStream in = registerMockFor(InputStream.class);
         request.setEntity(new InputRepresentation(in, MediaType.TEXT_XML));
        return in;
     }
 
 
     @Override
     @SuppressWarnings({"unchecked"})
     protected void expectObjectXmlized(Object o) {
         expect(studyXmlSerializer.createDocumentString(study)).andReturn(MOCK_XML);
     }
 
 }

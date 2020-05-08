 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
 import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
 import edu.northwestern.bioinformatics.studycalendar.service.importer.TemplateImportService;
 import edu.northwestern.bioinformatics.studycalendar.xml.validators.XMLValidator;
 import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudyXmlSerializer;
 import edu.nwu.bioinformatics.commons.DateUtils;
import org.apache.commons.io.IOUtils;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import org.restlet.representation.InputRepresentation;
 
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.Date;
 
 import static edu.northwestern.bioinformatics.studycalendar.restlets.UriTemplateParameters.STUDY_IDENTIFIER;
import static org.easymock.EasyMock.*;
 import static org.easymock.classextension.EasyMock.expect;
 
 /**
  * @author Rhett Sutphin
  */
 public class TemplateResourceTest extends AuthorizedResourceTestCase<TemplateResource> {
     private static final String STUDY_IDENT = "elf";
 
     private StudyDao studyDao;
     private StudyService studyService;
     private Study study, fullStudy;
     private TemplateImportService templateImportService;
     private StudyXmlSerializer studyXmlSerializer;
     private Date lastModifiedDate = DateUtils.createDate(2007, Calendar.OCTOBER, 5);
     private XMLValidator.BasicXMLValidator templateValidator;
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
 
         studyDao = registerDaoMockFor(StudyDao.class);
         templateImportService = registerMockFor(TemplateImportService.class);
         studyService = registerMockFor(StudyService.class);
         studyXmlSerializer = registerMockFor(StudyXmlSerializer.class);
         templateValidator = registerMockFor(XMLValidator.BasicXMLValidator.class);
 
         request.setMethod(Method.GET);
         request.getAttributes().put(STUDY_IDENTIFIER.attributeName(), STUDY_IDENT);
         study = Fixtures.setGridId("44", Fixtures.setId(44, Fixtures.createSingleEpochStudy(STUDY_IDENT, "Treatment")));
         study.setDevelopmentAmendment(new Amendment());
         Delta<Epoch> delta = Delta.createDeltaFor(new Epoch(), PropertyChange.create("name", "A", "B"));
         study.getDevelopmentAmendment().addDelta(delta);
         study.addSite(Fixtures.createSite("T"));
         delta.getChanges().get(0).setUpdatedDate(lastModifiedDate);
         Fixtures.assignIds(study);
         fullStudy = study.transientClone();
 
         expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andStubReturn(study);
         expect(studyService.getCompleteTemplateHistory(study)).andStubReturn(fullStudy);
     }
 
     @Override
     protected TemplateResource createAuthorizedResource() {
         TemplateResource templateResource = new TemplateResource() {
             @Override
             public XMLValidator.BasicXMLValidator getTemplateSchemaValidator() {
                 return templateValidator;
             }
         };
         templateResource.setStudyDao(studyDao);
         templateResource.setXmlSerializer(studyXmlSerializer);
         templateResource.setTemplateImportService(templateImportService);
         templateResource.setStudyService(studyService);
         return templateResource;
     }
 
     public void testGetAndPutAllowed() throws Exception {
         request.setMethod(Method.PUT);
         replayMocks();
         assertAllowedMethods("GET", "PUT");
     }
 
     public void testAllAuthorizedForGet() throws Exception {
         replayMocks();
         assertRolesAllowedForMethod(Method.GET, PscRole.valuesWithStudyAccess());
     }
 
     public void testBuilderAuthorizedForPut() throws Exception {
         replayMocks();
         assertRolesAllowedForMethod(Method.PUT, PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER);
     }
 
     public void testGetReturnsXml() throws Exception {
         expectSuccessfulStudyLoad();
         expectStudyFilledOut();
         expectObjectXmlized(fullStudy);
 
         doGet();
 
         assertEquals("Result not success", 200, response.getStatus().getCode());
         assertResponseIsCreatedXml();
 
         assertEquals("modification date incorrect", lastModifiedDate, response.getEntity().getModificationDate());
     }
 
     public void testGetByGridIdReturnsXml() throws Exception {
         expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(null);
         expect(studyDao.getByGridId(STUDY_IDENT)).andReturn(study);
         expectStudyFilledOut();
         expectObjectXmlized(fullStudy);
 
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
         Study updatedStudy = new Study();
 
         expectSuccessfulStudyLoad();
         InputStream in = expectPutEntity();
        expect(templateValidator.validate(notNull())).andReturn(null);
        expect(templateImportService.readAndSaveTemplate(eq(study), (InputStream) notNull())).andReturn(updatedStudy);
         expect(studyService.getCompleteTemplateHistory(updatedStudy)).andReturn(fullStudy);
         expect(studyXmlSerializer.createDocumentString(fullStudy)).andReturn(MOCK_XML);
 
         doPut();
 
         assertEquals("Result not success", 200, response.getStatus().getCode());
         assertResponseIsCreatedXml();
     }
 
     public void testPutNewXml() throws Exception {
         InputStream in = expectPutEntity();
 
        expect(templateValidator.validate(notNull())).andReturn(null);
         expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(null);
         expect(studyDao.getByGridId(STUDY_IDENT)).andReturn(null);
 
        expect(templateImportService.readAndSaveTemplate((Study) isNull(), (InputStream) notNull())).andReturn(study);
         expectStudyFilledOut();
         expect(studyXmlSerializer.createDocumentString(fullStudy)).andReturn(MOCK_XML);
 
         doPut();
 
         assertResponseStatus(Status.SUCCESS_CREATED);
         assertResponseIsCreatedXml();
     }
 
     public void testPutWithInvalidXml() throws Exception {
         InputStream in = expectPutEntity();
 
        expect(templateValidator.validate(notNull())).andReturn("Schema validation error");
         expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(null);
         expect(studyDao.getByGridId(STUDY_IDENT)).andReturn(null);
 
         doPut();
 
         assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Schema validation error");
     }
 
     public void testEntityIsInDownloadModeWithDownloadParam() throws Exception {
         expectSuccessfulStudyLoad();
         expectObjectXmlized(fullStudy);
 
         request.getResourceRef().setQuery("download");
         doGet();
 
         assertEquals("Result not success", 200, response.getStatus().getCode());
         assertTrue("Should be downloadable", response.getEntity().isDownloadable());
         assertEquals("Suggested filename should match the assigned ident", STUDY_IDENT + ".xml", response.getEntity().getDownloadName());
     }
 
     protected InputStream expectPutEntity() throws Exception {
        final InputStream in = IOUtils.toInputStream("<foo></foo>");
         request.setEntity(new InputRepresentation(in, MediaType.TEXT_XML));
         return in;
     }
 
 
     @Override
     @SuppressWarnings({"unchecked"})
     protected void expectObjectXmlized(Object o) {
         expect(studyXmlSerializer.createDocumentString(study)).andReturn(MOCK_XML);
     }
 
     private void expectSuccessfulStudyLoad() {
         expect(studyDao.getByAssignedIdentifier(STUDY_IDENT)).andReturn(study);
     }
 
     private void expectStudyFilledOut() {
         expect(studyService.getCompleteTemplateHistory(study)).andReturn(fullStudy);
     }
 }

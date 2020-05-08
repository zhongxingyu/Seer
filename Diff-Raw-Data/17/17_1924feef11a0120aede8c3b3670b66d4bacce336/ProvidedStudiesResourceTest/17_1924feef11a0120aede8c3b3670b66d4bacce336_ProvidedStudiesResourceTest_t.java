 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.restlets.representations.StudyListJsonRepresentation;
 import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudyConsumer;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CREATOR;
 import static org.easymock.EasyMock.*;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author Jalpa Patel
  */
 public class ProvidedStudiesResourceTest extends AuthorizedResourceTestCase<ProvidedStudiesResource> {
     private StudyConsumer studyConsumer;
 
     public void setUp() throws Exception {
         super.setUp();
         studyConsumer = registerMockFor(StudyConsumer.class);
     }
 
     @SuppressWarnings({"unchecked"})
     protected ProvidedStudiesResource createAuthorizedResource() {
         ProvidedStudiesResource resource = new ProvidedStudiesResource();
         resource.setXmlSerializer(xmlSerializer);
         resource.setStudyConsumer(studyConsumer);
         return resource;
     }
 
     public void testGetAllowed() throws Exception {
         assertAllowedMethods("GET");
     }
     
     public void testGetWithAuthorizedRoles() {
        assertRolesAllowedForMethod(Method.GET,STUDY_CREATOR);
     }
 
     @SuppressWarnings({"unchecked"})
     public void testGetAllProvidedStudiesAsXml() throws Exception {
         String expectedQ = "s";
         QueryParameters.Q.putIn(request, expectedQ);
         setAcceptedMediaTypes(MediaType.TEXT_XML);
 
         Study study = Fixtures.createBasicTemplate("Study");
         List<Study> expectedStudies = Arrays.asList(study);
 
         expect(studyConsumer.search(expectedQ)).andReturn(expectedStudies);
         expect(xmlSerializer.createDocumentString(expectedStudies)).andReturn(MOCK_XML);
 
         doGet();
         assertResponseStatus(Status.SUCCESS_OK);
         assertResponseIsCreatedXml();
     }
 
     @SuppressWarnings({"unchecked"})
     public void testGetAllProvidedStudiesAsJson() throws Exception {
         String expectedQ = "s";
         QueryParameters.Q.putIn(request, expectedQ);
         setAcceptedMediaTypes(MediaType.APPLICATION_JSON);
 
         Study study = Fixtures.createBasicTemplate("ECOG-2702");
         List<Study> expectedStudies = Arrays.asList(study);
 
         expect(studyConsumer.search(expectedQ)).andReturn(expectedStudies);
 
         doGet();
         assertResponseStatus(Status.SUCCESS_OK);
         assertTrue("Response entity is wrong type", response.getEntity() instanceof StudyListJsonRepresentation);
         assertSame("Response entity is for wrong studies", expectedStudies,
             ((StudyListJsonRepresentation) response.getEntity()).getStudies());
     }
 }

 package edu.northwestern.bioinformatics.studycalendar.xml.writers;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
 import edu.northwestern.bioinformatics.studycalendar.service.presenter.Registration;
 import edu.nwu.bioinformatics.commons.DateUtils;
 import org.dom4j.Element;
 import org.dom4j.tree.BaseElement;
 
 import java.util.Date;
 
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
 import static java.util.Calendar.JANUARY;
 import static org.easymock.EasyMock.expect;
 
 public class RegistrationXmlSerializerTest extends StudyCalendarXmlTestCase {
     private RegistrationXmlSerializer serializer;
     private StudySegment segment;
     private Date date;
     private PscUser expectedCoordinator;
     private SubjectXmlSerializer subjectSerializer;
     private Subject subject;
     private String dateString;
     private String desiredAssignmentId;
    private String studySubjectId;
     private Element element;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         subjectSerializer = registerMockFor(SubjectXmlSerializer.class);
 
         serializer = new RegistrationXmlSerializer();
         serializer.setSubjectXmlSerializer(subjectSerializer);
 
         segment = setGridId("grid0", new StudySegment());
         expectedCoordinator = AuthorizationObjectFactory.createPscUser("sammyc");
         subject = createSubject("John", "Doe");
         date = DateUtils.createDate(2008, JANUARY, 15);
         dateString = "2008-01-15";
         desiredAssignmentId = "12345";
        studySubjectId = "6789";
 
        element = createElement(segment, dateString, expectedCoordinator.getUsername(), desiredAssignmentId, studySubjectId);
     }
 
     public void testReadElement() {
         expectReadSubjectElement();
         replayMocks();
 
         Registration actual = serializer.readElement(element);
         verifyMocks();
 
         assertEquals("Wrong first study segment", segment, actual.getFirstStudySegment());
         assertSameDay("Dates should be the same", date, actual.getDate());
         assertEquals("Wrong Subject coordinator", expectedCoordinator.getUsername(), actual.getStudySubjectCalendarManager().getUsername());
         assertEquals("Wrong desired subject assignment id", "12345", actual.getDesiredStudySubjectAssignmentId());
         assertEquals("Wrong Subject", subject,  actual.getSubject());
     }
 
     public void testReadElementWhenStudySegmentIsNull() {
        Element elt = createElement(null, dateString, expectedCoordinator.getUsername(), desiredAssignmentId, studySubjectId);
         try {
             serializer.readElement(elt);
             fail("An exception should be thrown");
         } catch(StudyCalendarValidationException success) {
             assertEquals("Registration first study segment id is required", success.getMessage());
         }
     }
 
     public void testReadElementWhenDateIsNull() {
        Element elt = createElement(segment, null, expectedCoordinator.getUsername(), desiredAssignmentId, studySubjectId);
         try {
             serializer.readElement(elt);
             fail("An exception should be thrown");
         } catch(StudyCalendarValidationException success) {
             assertEquals("Registration date is required", success.getMessage());
         }
     }
 
     //// HELPERS
 
     private void expectReadSubjectElement() {
         expect(subjectSerializer.readElement(null)).andReturn(subject);
     }
 
    private Element createElement(StudySegment segment, String date, String scName, String desiredAssignmentId, String studySubjectId) {
         Element elt = new BaseElement("registration");
         elt.addAttribute("date", date);
         elt.addAttribute("desired-assignment-id", desiredAssignmentId);
        elt.addAttribute("study-subject-id", studySubjectId);
         if (segment != null) elt.addAttribute("first-study-segment-id", segment.getGridId());
         if (scName != null) elt.addAttribute("subject-coordinator-name", scName);
         return elt;
     }
 }

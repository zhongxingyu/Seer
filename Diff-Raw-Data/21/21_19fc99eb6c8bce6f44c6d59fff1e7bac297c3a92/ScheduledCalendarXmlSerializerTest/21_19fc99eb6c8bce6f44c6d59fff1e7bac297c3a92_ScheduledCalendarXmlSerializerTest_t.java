 package edu.northwestern.bioinformatics.studycalendar.xml.writers;
 
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setGridId;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
 import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarXmlTestCase;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import org.dom4j.Element;
 import org.dom4j.tree.BaseElement;
 import static org.easymock.EasyMock.expect;
 
 /**
  * @author John Dzak
  */
 public class ScheduledCalendarXmlSerializerTest extends StudyCalendarXmlTestCase {
     private ScheduledCalendar schedule;
     private ScheduledCalendarXmlSerializer serializer;
     private ScheduledStudySegmentXmlSerializer segmentSerialzier;
     private ScheduledStudySegment segment0;
     private ScheduledStudySegment segment1;
 
     protected void setUp() throws Exception {
         super.setUp();
 
         segmentSerialzier = registerMockFor(ScheduledStudySegmentXmlSerializer.class);
 
         serializer = new ScheduledCalendarXmlSerializer();
         serializer.setScheduledStudySegmentXmlSerializer(segmentSerialzier);
 
         StudySubjectAssignment assignment = setGridId("assignment-grid-0", new StudySubjectAssignment());
 
         segment0 = new ScheduledStudySegment();
         segment1 = new ScheduledStudySegment();
 
         schedule = setGridId("schedule-grid-0", new ScheduledCalendar());
         schedule.setAssignment(assignment);
         schedule.addStudySegment(segment0);
         schedule.addStudySegment(segment1);
     }
 
     public void testCreateElement() {
         expectSerializeSegments();
         replayMocks();
 
         Element actual = serializer.createElement(schedule);
         verifyMocks();
         
         assertEquals("Wrong element name", "scheduled-calendar", actual.getName());
         assertEquals("Wrong id", "schedule-grid-0", actual.attributeValue("id"));
         assertEquals("Wrong assignment id", "assignment-grid-0", actual.attributeValue("assignment-id"));
         assertEquals("Wrong scheduled study segment size", 2, actual.elements().size());
     }
 
    public void testCreateElementFromNull() throws Exception {
        try {
            serializer.createElement(null);
        } catch (StudyCalendarSystemException actual) {
            assertEquals("scheduledCalendar is required", actual.getMessage());
        }
    }

     public void testReadElement() {
         try {
             serializer.readElement(new BaseElement("schedule"));
             fail("Exception should be thrown, method not implemented");
         } catch(UnsupportedOperationException success) {
             assertEquals("Functionality to read a schedule element does not exist", success.getMessage());
         }
     }
 
     ////// Expect methods
     private void expectSerializeSegments() {
         expect(segmentSerialzier.createElement(segment0)).andReturn(new BaseElement("scheduled-study-segment"));
         expect(segmentSerialzier.createElement(segment1)).andReturn(new BaseElement("scheduled-study-segment"));
     }
 }

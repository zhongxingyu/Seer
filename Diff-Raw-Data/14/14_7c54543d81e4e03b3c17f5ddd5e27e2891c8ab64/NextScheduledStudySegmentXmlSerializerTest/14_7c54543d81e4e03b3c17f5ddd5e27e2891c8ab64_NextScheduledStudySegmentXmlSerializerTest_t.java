 package edu.northwestern.bioinformatics.studycalendar.xml.writers;
 
 import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.domain.NextStudySegmentMode;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
 import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
 import edu.northwestern.bioinformatics.studycalendar.xml.domain.NextScheduledStudySegment;
 import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
 import org.dom4j.Element;
 import org.dom4j.tree.BaseElement;
 
 import java.util.Calendar;
 
 /**
  * @author John Dzak
  */
 public class NextScheduledStudySegmentXmlSerializerTest extends StudyCalendarTestCase {
     private StudySegment segment;
     private NextScheduledStudySegmentXmlSerializer serializer;
 
     protected void setUp() throws Exception {
         super.setUp();
         serializer = new NextScheduledStudySegmentXmlSerializer();
         segment = Fixtures.setGridId("segment-grid0", new StudySegment());
     }
 
     public void testReadElement() {
         replayMocks();
 
         NextScheduledStudySegment actual = serializer.readElement(createTestElement());
         verifyMocks();
 
         assertSameDay("Wrong start date", createDate(2008, Calendar.JANUARY, 1), actual.getStartDate());
         assertSame("Wrong next study segment mode", NextStudySegmentMode.PER_PROTOCOL, actual.getMode());
         assertEquals("Wrong study segment", segment, actual.getStudySegment());
     }
 
    public void testReadElementIgnoresStartDay() {
        Element elt = createTestElement();
        elt.addAttribute("start-day", "5");

        replayMocks();

        NextScheduledStudySegment actual = serializer.readElement(elt);
        verifyMocks();

        assertNotNull("Failed reading study segment", actual);
    }

     ////// Helper methods
     private Element createTestElement() {
         Element elt = new BaseElement("next-scheduled-study-segment");
         elt.addAttribute("start-date", "2008-01-01");
         elt.addAttribute("study-segment-id", "segment-grid0");
         elt.addAttribute("mode", "per-protocol");
         return elt;
     }
 }

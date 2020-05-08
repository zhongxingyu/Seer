 package edu.northwestern.bioinformatics.studycalendar.web.schedule;
 
 import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 import gov.nih.nci.cabig.ctms.lang.DateTools;
 import gov.nih.nci.cabig.ctms.lang.NowFactory;
 import static org.easymock.EasyMock.expect;
 
 import java.util.Calendar;
 
 public class ScheduleReconsentCommandTest extends StudyCalendarTestCase {
     private ScheduleReconsentCommand command;
     private StudyService studyService;
     private NowFactory nowFactory;
 
     protected void setUp() throws Exception {
         super.setUp();
         studyService = registerMockFor(StudyService.class);
         nowFactory = registerMockFor(NowFactory.class);
         command = new ScheduleReconsentCommand(studyService, nowFactory);
     }
 
     public void testValidate() throws Exception {
         command.setStartDate(DateTools.createTimestamp(2005, Calendar.AUGUST, 3));

        expect(nowFactory.getNow()).andReturn(DateTools.createDate(2007, Calendar.AUGUST, 3)).times(2);
         replayMocks();
        command.validate(null);
         verifyMocks();
        assertSameDay("Expected Date different than actual", DateTools.createDate(2007, Calendar.AUGUST, 3), command.getStartDate());
     }
 }

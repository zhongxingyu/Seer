 package edu.northwestern.bioinformatics.studycalendar.web.schedule;
 
 import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 import gov.nih.nci.cabig.ctms.lang.DateTools;
 import gov.nih.nci.cabig.ctms.lang.NowFactory;
 import static org.easymock.EasyMock.expect;
import org.springframework.validation.Errors;
import org.springframework.validation.BindException;
 
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
        BindException errors = new BindException(command, "startDate");
         command.setStartDate(DateTools.createTimestamp(2005, Calendar.AUGUST, 3));
        expect(nowFactory.getNow()).andReturn(DateTools.createDate(2007, Calendar.AUGUST, 3));
         replayMocks();

        command.validate(errors);
         verifyMocks();

        assertEquals("There should be one error: ", 1, errors.getAllErrors().size());
     }
 }

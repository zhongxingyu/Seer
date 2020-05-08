 package edu.northwestern.bioinformatics.studycalendar.web.schedule;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createParticipant;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Occurred;
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
 import edu.nwu.bioinformatics.commons.DateUtils;
 import static org.easymock.EasyMock.expect;
 
 import java.util.List;
 import java.util.Calendar;
 import java.util.Collections;
 
 import gov.nih.nci.cabig.ctms.lang.NowFactory;
 import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;
 import gov.nih.nci.cabig.ctms.lang.DateTools;
 
 public class StudyServiceTest extends StudyCalendarTestCase {
     private StudyService service;
     private StudyDao studyDao;
     private ActivityDao activityDao;
     private Study study;
     private NowFactory nowFactory;
 
     protected void setUp() throws Exception {
         super.setUp();
         studyDao = registerMockFor(StudyDao.class);
         activityDao = registerMockFor(ActivityDao.class);
         nowFactory = registerMockFor(NowFactory.class);
         service = new StudyService();
         service.setStudyDao(studyDao);
         service.setActivityDao(activityDao);
         study = setId(1 , new Study());        
     }
 
     public void testApply() throws Exception{
         StaticNowFactory staticNowFactory = new StaticNowFactory();
         staticNowFactory.setNowTimestamp(DateTools.createTimestamp(2005, Calendar.AUGUST, 3));
 
         StudyParticipantAssignment spa = new StudyParticipantAssignment();
         spa.setParticipant(createParticipant("John", "Doe"));
 
 
         ScheduledCalendar calendar = new ScheduledCalendar();
         spa.setScheduledCalendar(calendar);
         spa.setParticipant(createParticipant("Alice", "Childress"));
         
        ScheduledArm arm1 = new ScheduledArm();
        arm1.addEvent(Fixtures.createScheduledEvent("CBC", 2005, Calendar.JULY, 1,
            new Occurred(null, DateUtils.createDate(2005, Calendar.JULY, 2))));
        arm1.addEvent(Fixtures.createScheduledEvent("CBC", 2005, Calendar.JULY, 3));
        arm1.addEvent(Fixtures.createScheduledEvent("CBC", 2005, Calendar.JULY, 8));
        calendar.addArm(arm1);

        ScheduledArm arm2 = new ScheduledArm();
        arm2.addEvent(Fixtures.createScheduledEvent("CBC", 2005, Calendar.AUGUST, 1,
             new Occurred(null, DateUtils.createDate(2005, Calendar.AUGUST, 2))));
        arm2.addEvent(Fixtures.createScheduledEvent("CBC", 2005, Calendar.AUGUST, 3));
        arm2.addEvent(Fixtures.createScheduledEvent("CBC", 2005, Calendar.AUGUST, 8));
        calendar.addArm(arm2);
 
         List<StudyParticipantAssignment> assignments = Collections.singletonList(spa);
         expect(studyDao.getAssignmentsForStudy(study.getId())).andReturn(assignments);
 
         Activity reconsent = setId(1, createNamedInstance("Reconsent", Activity.class));
         expect(activityDao.getByName("Reconsent")).andReturn(reconsent);
 
         studyDao.save(study);
         replayMocks();
         service.scheduleReconsent(study, staticNowFactory.getNow(), "Details");
         verifyMocks();
 
        List<ScheduledEvent> list = calendar.getScheduledArms().get(1).getEventsByDate().get(
                 DateTools.createTimestamp(2005, Calendar.AUGUST, 3));
         
         assertEquals(2, list.size());
         assertEquals("Reconsent", list.get(1).getActivity().getName());
     }
 }

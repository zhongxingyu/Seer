 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 import org.easymock.classextension.EasyMock;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * @author Saurabh Agrawal
  */
 public class NotificationServiceTest extends StudyCalendarTestCase {
 
     private NotificationService notificationService;
     private StudySubjectAssignmentDao studySubjectAssignmentDao;
     private Integer numberOfDays;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         studySubjectAssignmentDao = registerDaoMockFor(StudySubjectAssignmentDao.class);
         numberOfDays = 14;
         notificationService = new NotificationService();
         notificationService.setNumberOfDays(numberOfDays);
         notificationService.setStudySubjectAssignmentDao(studySubjectAssignmentDao);
 
 
     }
 
     public void testAddNotificationIfNothingIsScheduledForPatient() {
         List<StudySubjectAssignment> studySubjectAssignments = new ArrayList<StudySubjectAssignment>();
 
         StudySubjectAssignment studySubjectAssignment = new StudySubjectAssignment();
         studySubjectAssignments.add(studySubjectAssignment);
        EasyMock.expect(studySubjectAssignmentDao.getAllAssignmenetsWhichHaveNoActivityBeyondADate(EasyMock.isA(Date.class))).andReturn(studySubjectAssignments);
        studySubjectAssignmentDao.save(studySubjectAssignment);
         replayMocks();
         notificationService.addNotificationIfNothingIsScheduledForPatient();
         verifyMocks();
         assertEquals("assignment must have one notification", 1, studySubjectAssignment.getNotifications().size());
     }
 }

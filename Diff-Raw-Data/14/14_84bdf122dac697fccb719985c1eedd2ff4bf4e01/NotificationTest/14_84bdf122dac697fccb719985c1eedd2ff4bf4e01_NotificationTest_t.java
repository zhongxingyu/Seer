 package edu.northwestern.bioinformatics.studycalendar.domain;
 
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createAmendment;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
 import edu.northwestern.bioinformatics.studycalendar.utils.FormatTools;
 import edu.nwu.bioinformatics.commons.DateUtils;
 import edu.nwu.bioinformatics.commons.testing.CoreTestCase;
 
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * @author Saurabh Agrawal
  */
 public class NotificationTest extends CoreTestCase {
 
     private Notification notification;
 
     private AdverseEvent adverseEvent;
     private Date detectionDate;
     private AmendmentApproval amendmentApproval;
 
     private ScheduledActivity scheduledActivity;
 
     private Amendment amendment;
     private StudySite studySite;
     private Subject subject;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         adverseEvent = new AdverseEvent();
         adverseEvent.setDescription("desc");
         detectionDate = DateUtils.createDate(2007, Calendar.SEPTEMBER, 2);
         adverseEvent.setDetectionDate(detectionDate);
         scheduledActivity = Fixtures.createScheduledActivity("sch activity", 2008, 2, 3);
 
         amendment = createAmendment("amendment", new Date(), false);
         amendment.setId(2);
 
         amendmentApproval = new AmendmentApproval();
 
 
         final Study study = createNamedInstance("study", Study.class);
 
         study.setId(3);
         studySite = Fixtures.createStudySite(study, new Site());
         subject = Fixtures.createSubject("first", "last");
     }
 
     public void testCrateNotificationForAdverseEvent() {
 
         notification = new Notification(adverseEvent);
 
         String expectedTitle = "Serious Adverse Event on " + FormatTools.formatDate(detectionDate);
         assertEquals(expectedTitle, notification.getTitle());
         assertTrue("action is required", notification.isActionRequired());
         assertFalse(notification.isDismissed());
         assertEquals("desc", notification.getMessage());
         assertNull(notification.getAssignment());
 
     }
 
     public void testCrateNotificationForReconsents() {
         scheduledActivity.setId(2);
         notification = new Notification(scheduledActivity);
 
        String expectedTitle = "Reconsent scheduled for " + FormatTools.formatDate(scheduledActivity.getActualDate());
 
         assertEquals(expectedTitle, notification.getTitle());
         assertTrue("action is required", notification.isActionRequired());
         assertFalse(notification.isDismissed());
         assertEquals("/pages/cal/scheduleActivity?event=2", notification.getMessage());
         assertNull(notification.getAssignment());
 
     }
 
     public void testCrateNotificationWhenAmendmentIsApproved() {
         amendmentApproval.setAmendment(amendment);
         amendmentApproval.setStudySite(studySite);
         notification = new Notification(amendmentApproval);
 
         String expectedTitle = "Schedule amended according to " + amendment.getDisplayName();
 
         assertEquals(expectedTitle, notification.getTitle());
         assertFalse("action is not required", notification.isActionRequired());
         assertFalse(notification.isDismissed());
         assertEquals("/pages/cal/template/amendments?study=3#amendment=2", notification.getMessage());
         assertNull(notification.getAssignment());
 
     }
 
     public void testCrateNotificationForNonMandatoryAmendment() {
         amendmentApproval.setAmendment(amendment);
         amendmentApproval.setStudySite(studySite);
 
         StudySubjectAssignment studySubjectAssignment = Fixtures.createAssignment(studySite.getStudy(), studySite.getSite(), subject);
 
         notification = Notification.createNotificationForNonMandatoryAmendments(studySubjectAssignment, amendment);
 
         String expectedTitle = "New optional amendment available for first last";
 
         assertEquals(expectedTitle, notification.getTitle());
         assertTrue("action is  required", notification.isActionRequired());
         assertFalse(notification.isDismissed());
         assertEquals("A new optional amendment (" + amendment.getDisplayName() + ") has been released for study.  " +
                 "Determine whether it is appropriate for first last and if so, apply it", notification.getMessage());
         assertNull(notification.getAssignment());
 
     }
     public void testCrateNotificationForPatient() {
 
         notification = Notification.createNotificationForPatient(detectionDate,14);
 
         String expectedTitle = "No activities scheduled past 2007-09-02";
 
         assertEquals(expectedTitle, notification.getTitle());
         assertTrue("action is  required", notification.isActionRequired());
         assertFalse(notification.isDismissed());
         assertEquals("This subject has no activities scheduled after 2007-09-02 (14 days from now).  Consider scheduling his or her next segment or, " +
                 "if appropriate, taking him or her off the study.",
                 notification.getMessage());
         assertNull(notification.getAssignment());
 
     }
 }

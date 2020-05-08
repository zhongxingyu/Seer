 package edu.northwestern.bioinformatics.studycalendar.grid;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.api.ScheduledCalendarService;
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
 import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
 import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
 import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
 import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
 import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
 import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
 import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreatorImpl;
 import gov.nih.nci.cabig.ccts.ae.domain.AENotificationType;
 import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
 import gov.nih.nci.cagrid.common.Utils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.Assert;
 import org.springframework.beans.factory.annotation.Required;
 
 import java.io.FileReader;
 import java.io.Reader;
import java.util.Collections;
 import java.util.Date;
 
 /**
  * Test class added to validate the clean scripts that were added for CCTS roll-back script requirement
  *
  * @author Saurabh Agrawal
  */
 public class PSCAdverseEventMessgeTest /* TODO: renable when working // extends AbstractTransactionalSpringContextTests */ {
 
 
     private final Log logger = LogFactory.getLog(getClass());
 
     private String assignmentGridId = "48a14190-04d8-4c50-b594-588bb4fe8a7d";  //John Doe
 
     private ScheduledCalendarService scheduledCalendarService;
     private PSCAdverseEventConsumer adverseEventConsumer;
     private String aeFile;
     private StudyService studyService;
 
 
     private String assignedIdentifier = "TEST_STUDY";
 
     private String nciCode = "SITE_01";
     private SiteDao siteDao;
     private String shortTitle = "SMOTE_TEST";
     private String longTitle = "Test long title";
 
 
     private StudySubjectAssignmentDao studySubjectAssignmentDao;
 
     private SubjectService subjectService;
 
     private StudyDao studyDao;
 
     private SubjectDao subjectDao;
 
     private String subjectGridId = "91dd4580-801b-4874-adeb-a174361bacea";
     private StudySubjectAssignment studySubjectAssignment;
 
     protected void onSetUpInTransaction() throws Exception {
 
         DataAuditInfo.setLocal(new DataAuditInfo("test", "localhost", new Date(), "/wsrf/services/cagrid/AdverseEventConsumer"));
         aeFile = System.getProperty("psc.test.sampleNotificationFile",
                 "grid/adverse-event-consumer/test/resources/SampleAdverseEventMessage.xml");
         Study study = studyDao.getByAssignedIdentifier(assignedIdentifier);
         if (study == null) {
             logger.error(String.format("no study found for given identifier %s", assignedIdentifier));
             createStudy(); //create study and re-run the test case..
         }
 
         createAssignmentAndAeNotifications();
 
     }
 
     public void testCreateNotificationLocal() throws Exception {
 //        AENotificationType ae = getNotification();
 //        DataAuditInfo.setLocal(new DataAuditInfo("test", "127.0.0.1", new Date(), ""));
 //        adverseEventConsumer.register(ae);
 //        DataAuditInfo.setLocal(null);
 //        Notification notification = studySubjectAssignment.getNotifications().get(0);
 
         // TODO: there are no assertions here.  This is not a test.
 
     }
 
     private AENotificationType getNotification() throws Exception {
         AENotificationType ae = null;
         // InputStream config = getClass().getResourceAsStream(clientConfigFile);
         Reader reader = new FileReader(aeFile);
         ae = (AENotificationType) Utils.deserializeObject(reader, AENotificationType.class);
         return ae;
     }
 
     public void createSubjectAssigment() {
         if (studySubjectAssignmentDao.getByGridId(assignmentGridId) == null) {
             logger.debug("in createSubjectAssigment method");
 
 
             Study study = studyDao.getByAssignedIdentifier(assignedIdentifier);
 
             if (study == null) {
                 String message = "Study identified by assigned Identifier '" + assignedIdentifier + "' doesn't exist";
                 logger.error(message);
                 return;
             }
 
             StudySite studySite = findStudySite(study, nciCode);
             if (studySite == null) {
 
                 String message = "The study '" + study.getLongTitle() + "', identified by assigned Identifier '" + assignedIdentifier
                         + "' is not associated to a site identified by NCI code :'" + nciCode + "'";
                 logger.error(message);
 
                 return;
 
             }
 
             Subject subject = subjectDao.getByGridId(subjectGridId);
 
             if (subject == null) {
                 subject = new Subject();
                 subject.setGridId(subjectGridId);
                 subject.setGender(Gender.MALE);
 
                 subject.setDateOfBirth(new Date());
                 subject.setFirstName("first name");
                 subject.setLastName(" last name");
                 subject.setPersonId("1234");
 
                 subjectDao.save(subject);
                 logger.debug("created subject: " + subject.getId());
 
 
             } else {
                 logger.debug(String.format("subject %s found for given grid id %s", subject.getFullName(), subjectGridId));
 
             }
             StudySegment loadedStudySegment = null;
             try {
                 loadedStudySegment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
             } catch (Exception e) {
                 String message = "The study '" + study.getLongTitle() + "', identified by Coordinating Center Identifier '" + assignedIdentifier
                         + "' does not have any arm'";
                 logger.error(message);
                 return;
 
             }
             StudySubjectAssignment newAssignment = null;
             try {
                 logger.debug("creating subject assignment");
 
                 newAssignment = subjectService.assignSubject(subject, studySite, loadedStudySegment,
                        new Date(), assignmentGridId, Collections.<Population>emptySet(), null);
 
                 ScheduledCalendar scheduledCalendar = newAssignment.getScheduledCalendar();
                 logger.debug("Created assignment " + scheduledCalendar.getId());
 
             } catch (StudyCalendarSystemException exp) {
                 logger.error("Error crating assignment. " + exp.getMessage());
 
             }
 
         } else {
             logger.debug("assignment already exists for given grid id");
         }
     }
 
     private StudySite findStudySite(final Study study, final String siteNCICode) {
         for (StudySite studySite : study.getStudySites()) {
             if (StringUtils.equals(studySite.getSite().getAssignedIdentifier(), siteNCICode)) {
                 return studySite;
             }
         }
         return null;
 
 
     }
 
     public void createStudy() throws Exception {
         if (studyDao.getByAssignedIdentifier(assignedIdentifier) == null) {
             logger.debug("creating study for given identifer:" + assignedIdentifier);
             Study study = TemplateSkeletonCreatorImpl.createBase(shortTitle);
             study.setAssignedIdentifier(assignedIdentifier);
             study.setLongTitle(longTitle);
 
             Site site = siteDao.getByAssignedIdentifier(nciCode);
             if (site == null) {
                 String message = "No site exists for given assigned identifier" + nciCode;
                 logger.error(message);
                 site = new Site();
                 site.setAssignedIdentifier(nciCode);
                 site.setName(nciCode);
                 siteService.createOrUpdateSite(site);
 
             }
             StudySite studySite = new StudySite();
             studySite.setSite(site);
             studySite.setStudy(study);
             study.addStudySite(studySite);
 
             TemplateSkeletonCreatorImpl.addEpoch(study, 0, Epoch.create("Treatment"));
             Epoch epoch = new Epoch();
             epoch.setName("Treatment");
             StudySegment child = new StudySegment();
             child.setName("Arm A");
             epoch.addChild(child);
             study.getPlannedCalendar().addEpoch(epoch);
 
             studyService.save(study);
 
             amendmentService.amend(study);
 
             AmendmentApproval approvals = new AmendmentApproval();
             approvals.setAmendment(study.getAmendment());
             approvals.setDate(new Date());
 
             amendmentService.approve(studySite, approvals);
             logger.info("Created the study :" + study.getId());
 
         } else {
             logger.debug("study already exists for given identifier : " + assignedIdentifier);
         }
     }
 
     private SiteService siteService;
     private AmendmentService amendmentService;
 
     public void setStudyService(StudyService studyService) {
         this.studyService = studyService;
     }
 
     public void setStudyDao(StudyDao studyDao) {
         this.studyDao = studyDao;
     }
 
     public void setSiteService(SiteService siteService) {
         this.siteService = siteService;
     }
 
     public void setAmendmentService(AmendmentService amendmentService) {
         this.amendmentService = amendmentService;
     }
 
     public void setSiteDao(SiteDao siteDao) {
         this.siteDao = siteDao;
     }
 
     public void setAdverseEventConsumer(PSCAdverseEventConsumer adverseEventConsumer) {
         this.adverseEventConsumer = adverseEventConsumer;
     }
 
     private void createAssignmentAndAeNotifications() {
         studySubjectAssignment = studySubjectAssignmentDao.getByGridId(assignmentGridId);
         if (studySubjectAssignment == null) {
             createSubjectAssigment();
             studySubjectAssignment = studySubjectAssignmentDao.getByGridId(assignmentGridId);
 
         } else {
             logger.error(String.format("no assignment found for given grid id %s", assignmentGridId));
 
         }
 
         logger.debug(String.format("Deleting subject's %s ae notifications", studySubjectAssignment.getSubject().getFullName()));
         studySubjectAssignment.getNotifications().clear();
         studySubjectAssignmentDao.save(studySubjectAssignment);
         studySubjectAssignment = studySubjectAssignmentDao.getByGridId(assignmentGridId);
         Assert.assertTrue("must not have any notificaitons.", studySubjectAssignment.getNotifications().isEmpty());
         logger.debug(String.format("Sucessfully deleted subject's %s ae notifications", studySubjectAssignment.getSubject().getFullName()));
 
 
     }
 
 
     protected String[] getConfigLocations() {
 
         String[] configs = {"classpath:applicationContext-grid-ae.xml"};
 
 
         return configs;
     }
 
     public void setSubjectService(SubjectService subjectService) {
         this.subjectService = subjectService;
     }
 
     public void setSubjectDao(SubjectDao subjectDao) {
         this.subjectDao = subjectDao;
     }
 
     protected void onTearDownAfterTransaction() throws Exception {
 
         DataAuditInfo.setLocal(null);
 
     }
 
 
     @Required
     public void setStudySubjectAssignmentDao(StudySubjectAssignmentDao studySubjectAssignmentDao) {
         this.studySubjectAssignmentDao = studySubjectAssignmentDao;
     }
 
     @Required
     public void setScheduledCalendarService(ScheduledCalendarService scheduledCalendarService) {
         this.scheduledCalendarService = scheduledCalendarService;
     }
 }

 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Notification;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
 import edu.northwestern.bioinformatics.studycalendar.utils.mail.MailMessageFactory;
 import edu.northwestern.bioinformatics.studycalendar.utils.mail.ScheduleNotificationMailMessage;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.mail.MailException;
 import org.springframework.mail.MailSender;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * This service will add Notification objects
  *
  * @author <a href="mailto:saurabh.agrawal@semanticbits.com">Saurabh Agrawal</a> Created-on : May 29, 2007
  * @version %I%, %G%
  * @since 1.0
  */
 @Transactional(readOnly = true)
 
 public class NotificationService {
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     private StudySubjectAssignmentDao studySubjectAssignmentDao;
     private Integer numberOfDays;
     private MailSender mailSender;
     private MailMessageFactory mailMessageFactory;
     
     /**
      * This method will add notification when nothing is scheduled for a patient. This method is used by Cron
      * scheduling engine.
      */
     @Transactional(readOnly = false)
     public void addNotificationIfNothingIsScheduledForPatient() {
         Calendar calendar = Calendar.getInstance();
 
         logger.debug("executing scan on " + calendar.getTime());
 
         calendar.add(Calendar.DATE, numberOfDays);
 
         List<StudySubjectAssignment> studySubjectAssignments = studySubjectAssignmentDao.getAllAssignmenetsWhichHaveNoActivityBeyondADate(calendar.getTime());
         logger.debug("found  " + studySubjectAssignments.size() + " assignments");
 
         for (StudySubjectAssignment studySubjectAssignment : studySubjectAssignments) {
             Notification notification = Notification.createNotificationForPatient(Calendar.getInstance().getTime(), numberOfDays);
             notification.setAssignment(studySubjectAssignment);
             //do not send the email
             studySubjectAssignment.getNotifications().add(notification);
             studySubjectAssignmentDao.save(studySubjectAssignment);
         }
     }
 
     public void notifyUsersForNewScheduleNotifications(final Notification notification) {
         String toAddress = null;
         if (notification != null && notification.getAssignment() != null && notification.getAssignment().getStudySubjectCalendarManager() != null) {
             toAddress = notification.getAssignment().getStudySubjectCalendarManager().getEmailId();
         }
 
        ScheduleNotificationMailMessage mailMessage = mailMessageFactory.createScheduleNotificationMailMessage(toAddress, notification);
        if (mailMessage != null) {
            try {
                mailSender.send(mailMessage);
                logger.debug("sending notification to:" + toAddress);
            } catch (MailException e) {
                logger.error("Can not send notification email to:" + toAddress + " exception message:" + e.getMessage());
            } catch (Exception e) {
                logger.error("Can not send notification email to:" + toAddress + "exception: " + e.toString() + " exception message:" + e.getMessage());
             }
         }
     }
 
     @Required
     public void setStudySubjectAssignmentDao(final StudySubjectAssignmentDao studySubjectAssignmentDao) {
         this.studySubjectAssignmentDao = studySubjectAssignmentDao;
     }
 
     @Required
     public void setNumberOfDays(final Integer numberOfDays) {
         this.numberOfDays = numberOfDays;
     }
 
     // @Required
     public void setMailSender(final MailSender mailSender) {
         this.mailSender = mailSender;
     }
 
     // @Required
     public void setMailMessageFactory(final MailMessageFactory mailMessageFactory) {
         this.mailMessageFactory = mailMessageFactory;
     }
 }

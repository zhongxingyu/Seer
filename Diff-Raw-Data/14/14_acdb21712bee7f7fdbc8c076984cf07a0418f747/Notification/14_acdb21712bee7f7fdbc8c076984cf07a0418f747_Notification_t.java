 package edu.northwestern.bioinformatics.studycalendar.domain;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
 import edu.northwestern.bioinformatics.studycalendar.utils.FormatTools;
 import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
 import org.hibernate.annotations.GenericGenerator;
 import org.hibernate.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import java.text.DateFormat;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * @author Saurabh Agrawal
  */
 @Entity
 @Table(name = "notifications")
 @GenericGenerator(name = "id-generator", strategy = "native",
         parameters = {
         @Parameter(name = "sequence", value = "seq_notifications_id")
                 }
 )
 public class Notification extends AbstractMutableDomainObject {
 
     private boolean dismissed = false;
     private String title;
     private String message;
     private boolean actionRequired = true;
 
     private StudySubjectAssignment assignment;
     private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 
 
     public Notification(AdverseEvent adverseEvent) {
         if (adverseEvent != null) {
             setActionRequired(true);
             if (adverseEvent.getDetectionDate() != null) {
                 title = "Serious Adverse Event on " + FormatTools.formatDate(adverseEvent.getDetectionDate());
             }
             message = adverseEvent.getDescription();
         }
     }
 
     /**
      * only for Hibernate.
      * <p/>
      * <li>If you want to create notifications for Adverse event, use {@link edu.northwestern.bioinformatics.studycalendar.domain.Notification#Notification(AdverseEvent)}</li>
      * <li>If you want to create notifications for Amendments,
      * use {@link edu.northwestern.bioinformatics.studycalendar.domain.Notification#Notification(edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval)}</li>
      * <li>If you want to create notifications for Activity,
      * use {@link edu.northwestern.bioinformatics.studycalendar.domain.Notification#Notification(ScheduledActivity)}</li>
      */
     protected Notification() {
 
     }
 
     /**
      * Create notification message  for reconsents
      *
      * @param reconsentEvent
      */
     public Notification(final ScheduledActivity reconsentEvent) {
         if (reconsentEvent != null) {
            title = "Reconsent scheduled for " + FormatTools.formatDate(reconsentEvent.getActualDate());
             message = "/pages/cal/scheduleActivity?event=" + reconsentEvent.getId();
         }
 
     }
 
     public Notification(final AmendmentApproval amendmentApproval) {
         if (amendmentApproval != null && amendmentApproval.getAmendment() != null) {
 
             Amendment amendment = amendmentApproval.getAmendment();
             title = "Schedule amended according to " + amendment.getDisplayName();
 
             StudySite studySite = amendmentApproval.getStudySite();
 
             if (studySite != null && studySite.getStudy() != null) {
                 message = "/pages/cal/template/amendments?study=" + amendmentApproval.getStudySite().getStudy().getId()
                         + "#amendment=" + amendment.getId();
             }
             //Since the schedule is already amended, no action is required.
             actionRequired = false;
         }
     }
 
     public static Notification createNotificationForNonMandatoryAmendments(final StudySubjectAssignment assignment, final Amendment amendment) {
         if (assignment == null || amendment == null)
             return null;
 
         Notification notification = new Notification();
         notification.setActionRequired(true);
 
         String subjectFullName = assignment.getSubject().getFullName();
 
         String title = MessageFormat.format("New optional amendment available for {0}", subjectFullName);
 
         String message = MessageFormat.format("A new optional amendment ({0}) has been released for {1}.  " +
                 "Determine whether it is appropriate for {2} and if so, apply it", amendment.getDisplayName(), assignment.getStudySite().getStudy().getName(),
                 subjectFullName);
 
         notification.setTitle(title);
         notification.setMessage(message);
         return notification;
     }
 
 
     @ManyToOne
     public StudySubjectAssignment getAssignment() {
         return assignment;
     }
 
     public void setAssignment(StudySubjectAssignment assignment) {
         this.assignment = assignment;
     }
 
     public boolean isDismissed() {
         return dismissed;
     }
 
     public void setDismissed(boolean dismissed) {
         this.dismissed = dismissed;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(final String title) {
         this.title = title;
     }
 
     public String getMessage() {
         return message;
     }
 
     public void setMessage(final String message) {
         this.message = message;
     }
 
     public boolean isActionRequired() {
         return actionRequired;
     }
 
     public void setActionRequired(final boolean actionRequired) {
         this.actionRequired = actionRequired;
     }
 
     public boolean equals(final Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Notification that = (Notification) o;
 
         if (actionRequired != that.actionRequired) return false;
         if (dismissed != that.dismissed) return false;
         if (assignment != null ? !assignment.equals(that.assignment) : that.assignment != null) return false;
         if (message != null ? !message.equals(that.message) : that.message != null) return false;
         if (title != null ? !title.equals(that.title) : that.title != null) return false;
 
         return true;
     }
 
     public int hashCode() {
         int result;
         result = (dismissed ? 1 : 0);
         result = 31 * result + (title != null ? title.hashCode() : 0);
         result = 31 * result + (message != null ? message.hashCode() : 0);
         result = 31 * result + (actionRequired ? 1 : 0);
         result = 31 * result + (assignment != null ? assignment.hashCode() : 0);
         return result;
     }
 
     public static Notification createNotificationForPatient(final Date date, final Integer numberOfDays) {
         Notification notification = new Notification();
         notification.setActionRequired(true);
 
         String dateString = dateFormat.format(date);
 
         String title = MessageFormat.format("No activities scheduled past {0}", dateString);
         String message = MessageFormat.format("This subject has no activities scheduled after {0} ({1} days from now).  " +
                 "Consider scheduling his or her next segment or, if appropriate, taking him or her off the study.", dateString, numberOfDays);
         notification.setTitle(title);
         notification.setMessage(message);
         return notification;
     }
 }

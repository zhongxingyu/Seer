 package edu.northwestern.bioinformatics.studycalendar.service.presenter;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
 import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
 
 import java.util.*;
 
 import static java.util.Arrays.asList;
 import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
 
 /**
  * @author Rhett Sutphin
  */
 public class StudyWorkflowStatus {
     private final Study study;
     private final PscUser user;
 
     private final WorkflowMessageFactory workflowMessageFactory;
 
     private final UserTemplateRelationship utr;
     private final RevisionWorkflowStatus revisionWorkflowStatus;
 
     public StudyWorkflowStatus(
         Study study, PscUser user,
         WorkflowMessageFactory factory, DeltaService deltaService
     ) {
         this.study = study;
         this.user = user;
         this.workflowMessageFactory = factory;
 
         this.utr = new UserTemplateRelationship(user, study);
         if (study.isInDevelopment()) {
             this.revisionWorkflowStatus = new RevisionWorkflowStatus(study, user, factory, deltaService);
         } else {
             this.revisionWorkflowStatus = null;
         }
     }
 
     public Collection<WorkflowMessage> getMessages() {
         WorkflowMessage studyMsg = getMessagesIgnoringRevisionMessages();
         if (studyMsg != null && studyMsg.getStep() == WorkflowStep.SET_ASSIGNED_IDENTIFIER) {
             return asList(studyMsg);
         } else if (getRevisionWorkflowStatus() != null && isNotEmpty(revisionWorkflowStatus.getMessages())) {
             return revisionWorkflowStatus.getMessages();
         }
         return studyMsg == null ? Collections.<WorkflowMessage>emptyList() : asList(studyMsg);
     }
 
     @SuppressWarnings("unchecked")
     public WorkflowMessage getMessagesIgnoringRevisionMessages() {
         if (study.getHasTemporaryAssignedIdentifier()) {
             return workflowMessageFactory.createMessage(WorkflowStep.SET_ASSIGNED_IDENTIFIER, utr);
         } else if (study.isReleased() && study.getStudySites().isEmpty()) {
             return workflowMessageFactory.createMessage(WorkflowStep.ASSIGN_SITE, utr);
         }
         return null;
     }
 
     public Collection<WorkflowMessage> getStructureRelatedMessages() {
         Collection<WorkflowMessage> messages = new ArrayList<WorkflowMessage>();
         if (study.getHasTemporaryAssignedIdentifier()) {
             messages.add(workflowMessageFactory.createMessage(WorkflowStep.SET_ASSIGNED_IDENTIFIER, utr));
         } else if (getRevisionWorkflowStatus() != null && isNotEmpty(revisionWorkflowStatus.getMessages())) {
             messages.addAll(revisionWorkflowStatus.getStructureMessages());
         }
         return messages;
     }
 
     public Collection<TemplateAvailability> getTemplateAvailabilities() {
         Set<TemplateAvailability> availabilities = new LinkedHashSet<TemplateAvailability>();
        if (getRevisionWorkflowStatus() != null && utr.getCanSeeDevelopmentVersion()) {
             availabilities.add(TemplateAvailability.IN_DEVELOPMENT);
         }
         if (study.isReleased()) {
             if (getMessagesIgnoringRevisionMessages() != null) {
                 availabilities.add(TemplateAvailability.PENDING);
             }
             for (StudySiteWorkflowStatus status : getStudySiteWorkflowStatuses()) {
                 if (status.getMessage() == null) {
                     availabilities.add(TemplateAvailability.AVAILABLE);
                 } else {
                     availabilities.add(TemplateAvailability.PENDING);
                 }
             }
         }
         return availabilities;
     }
 
     public boolean isRevisionComplete() {
         return (getRevisionWorkflowStatus() != null && getRevisionWorkflowStatus().getStructureMessages().isEmpty());
     }
 
     /**
      * Returns the development revision workflow for this study, or null if the
      * study isn't in development.
      * @return
      */
     public RevisionWorkflowStatus getRevisionWorkflowStatus() {
         return revisionWorkflowStatus;
     }
 
     /**
      * Returns the study site workflows for this study.  If the study doesn't have any
      * associated sites, it returns an empty list.
      * @return
      */
     public List<StudySiteWorkflowStatus> getStudySiteWorkflowStatuses() {
         List<StudySiteWorkflowStatus> statuses = new LinkedList<StudySiteWorkflowStatus>();
         for (UserStudySiteRelationship ussr : utr.getVisibleStudySites()) {
             statuses.add(new StudySiteWorkflowStatus(ussr.getStudySite(), getUser(), workflowMessageFactory));
         }
         return statuses;
     }
 
     public PscUser getUser() {
         return user;
     }
 
     public Study getStudy() {
         return study;
     }
 
     public UserTemplateRelationship getUserRelationship() {
         return utr;
     }
 
     ////// OBJECT METHODS
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof StudyWorkflowStatus)) return false;
 
         StudyWorkflowStatus that = (StudyWorkflowStatus) o;
 
         if (study != null ? !study.equals(that.study) : that.study != null) return false;
         if (user != null ? !user.equals(that.user) : that.user != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = study != null ? study.hashCode() : 0;
         result = 31 * result + (user != null ? user.hashCode() : 0);
         return result;
     }
 
 
     ////// COMPARATORS
 
     public static Comparator<StudyWorkflowStatus> byReleaseDisplayName() {
         return ByPropertyComparator.RELEASE_NAME;
     }
 
     public static Comparator<StudyWorkflowStatus> byDevelopmentDisplayName() {
         return ByPropertyComparator.DEVELOPMENT_NAME;
     }
 
     private abstract static class ByPropertyComparator implements Comparator<StudyWorkflowStatus> {
         public static final Comparator<StudyWorkflowStatus> RELEASE_NAME = new ByPropertyComparator() {
             @Override protected String comparableProperty(StudyWorkflowStatus sws) {
                 return sws.getStudy().getReleasedDisplayName().toLowerCase();
             }
         };
 
         public static final Comparator<StudyWorkflowStatus> DEVELOPMENT_NAME = new ByPropertyComparator() {
             @Override protected String comparableProperty(StudyWorkflowStatus sws) {
                 return sws.getStudy().getDevelopmentDisplayName().toLowerCase();
             }
         };
 
         public int compare(StudyWorkflowStatus o1, StudyWorkflowStatus o2) {
             return comparableProperty(o1).compareTo(comparableProperty(o2));
         }
 
         protected abstract String comparableProperty(StudyWorkflowStatus sws);
     }
 }

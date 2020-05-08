 package edu.northwestern.bioinformatics.studycalendar.service.presenter;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
 
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
 
 /**
  * @author Rhett Sutphin
  */
 public class UserStudySubjectAssignmentRelationship
     implements Comparable<UserStudySubjectAssignmentRelationship>
 {
     private PscUser user;
     private StudySubjectAssignment assignment;
     private UserRelationshipTools tools;
 
     public UserStudySubjectAssignmentRelationship(PscUser user, StudySubjectAssignment assignment) {
         this.user = user;
         this.assignment = assignment;
         this.tools = new UserRelationshipTools(user, assignment.getStudySite().getStudy());
     }
 
     public boolean isVisible() {
         return hasAtLeastOneMatchingRole(
             STUDY_SUBJECT_CALENDAR_MANAGER, STUDY_TEAM_ADMINISTRATOR, DATA_READER);
     }
 
     public boolean getCanUpdateSchedule() {
         return hasMatchingRole(STUDY_SUBJECT_CALENDAR_MANAGER);
     }
 
     public boolean isCalendarManager() {
         return assignment.getManagerCsmUserId() != null
             && assignment.getManagerCsmUserId().equals(user.getCsmUser().getUserId().intValue());
     }
 
     public boolean getCanSetCalendarManager() {
         return hasMatchingRole(STUDY_TEAM_ADMINISTRATOR);
     }
 
     ////// comparable
 
     public int compareTo(UserStudySubjectAssignmentRelationship o) {
         return StudySubjectAssignment.byOnOrOff().compare(getAssignment(), o.getAssignment());
     }
 
     ////// ACCESSORS
 
     public PscUser getUser() {
         return user;
     }
 
     public StudySubjectAssignment getAssignment() {
         return assignment;
     }
 
     /////// HELPERS
 
     private boolean hasMatchingRole(PscRole role) {
         return hasAtLeastOneMatchingRole(role);
     }
 
     private boolean hasAtLeastOneMatchingRole(PscRole... roles) {
         for (PscRole role : roles) {
             if (tools.hasMatchingScope(role, getAssignment().getStudySite().getSite())
                 && tools.hasMatchingStudyScope(role)) {
                 return true;
             }
         }
         return false;
     }
 }
